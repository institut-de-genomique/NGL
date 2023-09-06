package scripts;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.mongojack.DBQuery;
import org.springframework.util.StringUtils;

import de.flapdoodle.embed.process.io.file.Files;
import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.lfw.controllers.scripts.buffered.ScriptWithExcelBody;
import models.laboratory.common.instance.Comment;
import models.laboratory.sample.instance.Sample;
import models.utils.InstanceConstants;
import play.Logger;
import play.Logger.ALogger;

/**
 * ScriptRepriseCommentaireTechnique - NGL-3499
 * 
 * @author jcharpen - Jordi CHARPENTIER - jcharpen@genoscope.cns.fr
 */
public class ScriptRepriseCommentaireTechnique extends ScriptWithExcelBody {

	private ALogger logger = Logger.of(ScriptRepriseCommentaireTechnique.class);
	public static class Args {

	}

	private StringBuilder CONTENU_LOGS = new StringBuilder();

	@Override
	public void execute(XSSFWorkbook workbook) throws Exception {
		logger.error("Début ScriptRepriseCommentaireTechnique");

		repriseCommentaireSampleFromList(workbook);

		// Ces 3 méthodes sont plus utilisées : elles ont servies à faire l'état des lieux, la correction sera faite ci-dessus.

		repriseCommentaireImportFichier();

		repriseCommentaireStartSUPSQ();

		repriseCommentaireStartNGL();

		logger.error("Fin ScriptRepriseCommentaireTechnique");
	}

	private void repriseCommentaireSampleFromList(XSSFWorkbook workbook) {
		XSSFSheet sheetDataReadSet = workbook.getSheetAt(0);
		
		sheetDataReadSet.rowIterator().forEachRemaining(row -> {
			if(row.getRowNum() == 0) return; // skip header

			String sampleCode = row.getCell(0).getStringCellValue();
			String technicalCommentStr = "";
			
			try {
				technicalCommentStr = row.getCell(2).getStringCellValue();
			} catch (NullPointerException e) {
				// logger.error("pas de commentaire technique pour le sample : " + sampleCode);
			}
			
			String commentStr = "";
			
			try {
				commentStr = row.getCell(3).getStringCellValue();
			} catch (NullPointerException e) {
				// logger.error("pas de commentaire final pour le sample : " + sampleCode);
			}

			if (!StringUtils.isEmpty(sampleCode)) {
				sampleCode = sampleCode.trim();

				logger.error(sampleCode);

				Sample sample = MongoDBDAO.findByCode(InstanceConstants.SAMPLE_COLL_NAME, Sample.class, sampleCode);

				if (sample != null) {
					Comment technicalComment = new Comment(technicalCommentStr, "ngl-support");

					if (sample.technicalComments == null) {
						sample.technicalComments = new ArrayList<Comment>();
					}

					sample.technicalComments.add(technicalComment);

					sample.comments.get(0).comment = commentStr;

					MongoDBDAO.update(InstanceConstants.SAMPLE_COLL_NAME, sample);
				} else {
					logger.error("sample not found for code : " + sampleCode);
				}
			}
		});
	}

	/**
	 * repriseCommentaireImportFichier()
	 * @throws IOException
	 */
	@SuppressWarnings("deprecation")
	private void repriseCommentaireImportFichier() throws IOException {
		logger.error("Reprise commentaires dans les imports fichier.");

		CONTENU_LOGS.append("Code sample; Commentaire actuel; Commentaire technique; Commentaire final\n");

		for (int startYear = 122; startYear >= 114; startYear--) { 
			Date startDate = new Date(startYear, 0, 1);
			Date endDate = new Date(startYear, 11, 31);

			List<Sample> sampleList = MongoDBDAO.find(InstanceConstants.SAMPLE_COLL_NAME, Sample.class, DBQuery.and(
				DBQuery.regex("comments.0.comment", Pattern.compile("échantillon mis à jour via le fichier")),
				DBQuery.greaterThan("traceInformation.creationDate", startDate),
				DBQuery.lessThan("traceInformation.creationDate", endDate)
			)).toList();

			for (int i = 0; i < sampleList.size(); i++) {
				Sample sample = sampleList.get(i);

				updateCommentWithRegex(sample);
				
				// Mise à jour du sample en base.
				// MongoDBDAO.update(InstanceConstants.SAMPLE_COLL_NAME, sample);
			}
		} 

		Files.write(CONTENU_LOGS.toString(), new File("logs_script-reprise_commentaire_technique.txt"));

		logger.error("Fin de la reprise commentaires dans les imports fichier.");
	}

	/**
	 * repriseCommentaireStartNGL()
	 */
	@SuppressWarnings("deprecation")
	private void repriseCommentaireStartNGL() {
		logger.error("Reprise des commentaires commençant par 'NGL-'.");

		logger.error("Code sample; Commentaire actuel; Commentaire technique; Commentaire final");

		for (int startYear = 122; startYear >= 120 /* 114 */; startYear--) { 
			Date startDate = new Date(startYear, 0, 1);
			Date endDate = new Date(startYear, 11, 31);

			List<Sample> sampleList = MongoDBDAO.find(InstanceConstants.SAMPLE_COLL_NAME, Sample.class, DBQuery.and(
				DBQuery.regex("comments.0.comment", Pattern.compile("NGL-")),
				DBQuery.greaterThan("traceInformation.creationDate", startDate),
				DBQuery.lessThan("traceInformation.creationDate", endDate)
			)).toList();

			for (int i = 0; i < sampleList.size(); i++) {
				Sample sample = sampleList.get(i);

				if (sample.comments.get(0).comment.startsWith(("NGL-"))) {
					String technicalComment = "";
					String finalComment = "";

					updateComment(sample, technicalComment, finalComment);
					
					// Mise à jour du sample en base.
					// MongoDBDAO.update(InstanceConstants.SAMPLE_COLL_NAME, sample);
				}
			}
		}

		logger.error("Fin de la reprise des commentaires commençant par 'NGL-'.");
	}

	/**
	 * repriseCommentaireStartSUPSQ()
	 */
	@SuppressWarnings("deprecation")
	private void repriseCommentaireStartSUPSQ() {
		logger.error("Reprise des commentaires commençant par 'SUPSQ-'.");

		logger.error("Code sample; Commentaire actuel; Commentaire technique; Commentaire final");

		for (int startYear = 122; startYear >= 114; startYear--) { 
			Date startDate = new Date(startYear, 0, 1);
			Date endDate = new Date(startYear, 11, 31);

			List<Sample> sampleList = MongoDBDAO.find(InstanceConstants.SAMPLE_COLL_NAME, Sample.class, DBQuery.and(
				DBQuery.regex("comments.0.comment", Pattern.compile("SUPSQ-")),
				DBQuery.greaterThan("traceInformation.creationDate", startDate),
				DBQuery.lessThan("traceInformation.creationDate", endDate)
			)).toList();

			for (int i = 0; i < sampleList.size(); i++) {
				Sample sample = sampleList.get(i);

				if (sample.comments.get(0).comment.startsWith(("SUPSQ-")) && !sample.comments.get(0).comment.contains("import type")) {
					String technicalComment = "";
					String finalComment = "";

					/**
					 * Si le commentaire contient ::, on met tout ce qu'il y a avant dans le commentaire technique et tout ce qu'il y a après on garde.
					 */
					if (sample.comments.get(0).comment.contains("::")) { 
						technicalComment = sample.comments.get(0).comment.substring(0, sample.comments.get(0).comment.indexOf(" :: "));
						finalComment = sample.comments.get(0).comment.replace(technicalComment + " :: ", "");
					} 
					/**
					 * Si le commentaire contient uniquement le nom du ticket, on le met dans un commentaire technique.
					 * Exemple : Pour DCE_AGK, 'SUPSQ-5009'.
					 */
					else if (sample.comments.get(0).comment.length() <= 11) {
						technicalComment = sample.comments.get(0).comment;
						finalComment = "";
				    } 
					/**
					 * Si le commentaire contient 'old ref collab', on met tout dans le commentaire technique.
					 * Exemple : Pour CEB_AJM, 'SUPSQ-4312 old ref collab Tneg_Tag_eau_Ambion @Temoin_Negatif_Tag_eau_Ambion@'.
					 */
					else if (sample.comments.get(0).comment.contains("old ref collab")) {
						technicalComment = sample.comments.get(0).comment;
						finalComment = "";
					}
					/**
					 * A définir
					 */
					else { 
						// logger.error(sample.code + " / " + sample.comments.get(0).comment);
					}

					updateComment(sample, technicalComment, finalComment);

					// Mise à jour du sample en base.
					// MongoDBDAO.update(InstanceConstants.SAMPLE_COLL_NAME, sample);
				}	
			}
		}

		logger.error("Fin de la reprise des commentaires commençant par 'SUPSQ-'.");
	}

	/**
	 * updateCommentWithRegex()
	 * 
	 * @param sample
	 */
	private void updateCommentWithRegex(Sample sample) {
		String regex = "([-_] +\\d\\d/\\d\\d/\\d\\d : .+)$";
		String sampleComment = sample.comments.get(0).comment;
		String sampleCode = sample.code;
		String initialComment = sample.comments.get(0).comment;

		Matcher matcher = Pattern.compile(regex).matcher(sampleComment);
								
		if (matcher.find()) { // Si on a un (ou plusieurs) commentaire technique, on le garde.					
			Comment comment = new Comment();
			comment.comment = matcher.group(1);
			comment.createUser = "ngl-data";
			comment.creationDate = sample.comments.get(0).creationDate;

			if (sample.technicalComments == null) {
				sample.technicalComments = new ArrayList<Comment>();
			}

			sample.technicalComments.add(comment);

			// On enlève le commentaire technique du commentaire du sample.
			sample.comments.get(0).comment = sample.comments.get(0).comment.replace(comment.comment, "");

			String finalComment = sample.comments.get(0).comment;

			// Si le commentaire final contient toujours une date, je suppose qu'il a encore un commentaire technique à enlever.
			if (finalComment.contains("/")) {
				String regex2 = "(\\d\\d/\\d\\d/\\d\\d : .+)$";
				Matcher matcher2 = Pattern.compile(regex2).matcher(finalComment);

				if (matcher2.find()) {
					Comment comment2 = new Comment(matcher2.group(1), "ngl-data");
					sample.technicalComments.get(0).comment = comment2.comment + sample.technicalComments.get(0).comment;

					finalComment = initialComment.replace(matcher.group(1), "").replace(matcher2.group(1), "");
				}	
			}

			List<Comment> commentsList = sample.technicalComments;

			CONTENU_LOGS.append(sampleCode + ";" + initialComment + ";" + commentsList.get(0).comment + ";" + finalComment + "\n");

			logger.error(sampleCode + ";" + initialComment + ";" + commentsList.get(0).comment + ";" + finalComment);
		} 
	}

	/**
	 * updateComment()
	 * 
	 * @param sample
	 */
	private void updateComment(Sample sample, String technicalComment, String finalComment) {
		String initialComment = sample.comments.get(0).comment;

		Comment technicalCommentObj = new Comment();
		technicalCommentObj.comment = technicalComment;
		technicalCommentObj.createUser = "ngl-data";
		technicalCommentObj.creationDate = new Date();

		if (sample.technicalComments == null) {
			sample.technicalComments = new ArrayList<Comment>();
		}
		
		sample.technicalComments.add(technicalCommentObj);

		logger.error(sample.code + ";" + initialComment.replaceAll("\\r", "") + ";" + technicalComment.replaceAll("\\r", "") + ";" + finalComment.replaceAll("\\r", ""));
	}
}