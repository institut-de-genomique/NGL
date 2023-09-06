package models.utils.code;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.mongojack.Aggregation;
import org.mongojack.DBProjection;
import org.mongojack.Aggregation.Pipeline;
import org.mongojack.DBProjection.ProjectionBuilder;
import org.mongojack.DBQuery;
import org.mongojack.DBSort;
import org.mongojack.DBUpdate;

import com.mongodb.BasicDBObject;

import fr.cea.ig.MongoDBDAO;
import models.laboratory.common.instance.Comment;
import models.laboratory.experiment.instance.Experiment;
import models.laboratory.processes.instance.Process;
import models.laboratory.project.instance.Project;
import models.laboratory.sample.instance.Sample;
import models.utils.InstanceConstants;

/**
 * Default code generation implementation. 
 */
public class DefaultCodeImpl implements Code {

	/**
	 * Logger.
	 */
	private static final play.Logger.ALogger logger = play.Logger.of(DefaultCodeImpl.class);

	//	/**
	//	 * New simple date format object.
	//	 * @param format date format
	//	 * @return       simple data format
	//	 */
	//	@Deprecated
	//	private SimpleDateFormat getSimpleDateFormat(String format) {
	//		return new SimpleDateFormat(format);
	//	}

	/**
	 * Format now (Date()) using a given format.
	 * @param format date output format
	 * @return       formatted date
	 */
	private String getNowFormatted(String format) {
		return new SimpleDateFormat(format).format(new Date());
	}

	// Looks like the bar code generation is just a date encoding that could have used
	// something like : Long#toString(System.currentTimeMillis(),36).toUpperCase(). This
	// would have the added benefit of being decoded with Long#valueOf(x, 36) which
	// would be the exact time of the bar code. If the bar code is too long, we could
	// use some offset (say 2015/01/01 00:00:00).
	protected synchronized String generateBarCode_LtS() {
		try {
			Thread.sleep(1);
		} catch (InterruptedException e) {
			logger.error("Interruption error: " + e.getMessage(), e);
			throw new RuntimeException(e);
		}
		return Long.toString(System.currentTimeMillis(), 36);
	}

	// This calendar based implementation is not strictly equivalent to the original
	// as the minute/second handling differ. 
	protected synchronized String generateBarCode_Cal() {
		try {
			Thread.sleep(1);
		} catch (InterruptedException e) {
			logger.error("Interruption error: " + e.getMessage(), e);
			throw new RuntimeException(e);
		}
		Calendar c = Calendar.getInstance();
		c.setTime(new Date());
		StringBuilder s = new StringBuilder();
		s.append(Integer.toString(c.get(Calendar.YEAR) - 2105,  36));
		s.append(Integer.toString(c.get(Calendar.MONTH),        36));
		s.append(Integer.toString(c.get(Calendar.DAY_OF_MONTH), 36));
		int minsec = Integer.valueOf(String.format("%02d%02d", c.get(Calendar.MINUTE), c.get(Calendar.SECOND))) + 36 * 36;
		s.append(Integer.toString(minsec, 36));
		s.append(Integer.toString(c.get(Calendar.MILLISECOND) + 36, 36));
		return s.toString().toUpperCase();
	}

	/**
	 * Generates a unique (in this JVM, if 2 instances of this class are not created) 
	 * identifier using date encoding.   
	 * @return unique identifier
	 */
	protected synchronized String generateBarCode() {
		try {
			Thread.sleep(1);
		} catch (InterruptedException e) {
			logger.error("Interruption error: " + e.getMessage(), e);
			throw new RuntimeException(e);
		}
		// String date = new SimpleDateFormat("yyMMddHHmmssSS").format(new Date());
		// Why use only 2 digits year instead of 4 and subtract 2015 ?
		String date = getNowFormatted("yyMMddHHmmssSS");
		Pattern p = Pattern.compile("([0-9]{2})([0-9]{2})([0-9]{2})([0-9]{2})([0-9]{2})([0-9]{2})([0-9]{3}|[0-9]{2})");
		// Matcher m = p.matcher("151231235959999"); // worst situation
		Matcher m = p.matcher(date);
		if (m.matches()) {
			String code = Integer.toString(Integer.valueOf(Integer.valueOf(m.group(1))-15), //Years 0 is 2015
					36); // year
			code += Integer.toString(Integer.valueOf(m.group(2)), 36); // month
			code += Integer.toString(Integer.valueOf(m.group(3)), 36); // day
			code += Integer.toString(Integer.valueOf(m.group(4)), 36); // hours

			// As the group(5) + group(6) concatenation is 4 digits, this
			// could be: code += Integer.toString(Integer.valueOf(m.group(5) + m.group(6)) + 1296, 36);
			int second = Integer.valueOf(m.group(6)) + 10; // +10 because we can have duplicated like 11 0 and 1 10
			int minsec = Integer.valueOf(m.group(5) + String.valueOf(second)) + 1296; // +1296 (100 base 36 = 36*36) because we want always 3 char
			code += Integer.toString(minsec, 36); // minute

			code += Integer.toString(Integer.valueOf(m.group(7)) + 36, 36);// millisecond
			//Logger.debug("Container code generated "+code);
			logger.debug("generateBarCode {}", code.toUpperCase());
			return code.toUpperCase();
		} else {
			//			try {
			//				logger.error("Error matches of the date fail"+date);
			//				throw new Exception("matches fail " + date);
			//			} catch (Exception e) {
			//				logger.error("Matches error: "+e.getMessage(),e);
			//				return null;
			//			}
			// Supposedly unreachable code
			throw new RuntimeException("pattern matching of the date failed : " + date);
		}
	}

	@Override
	public synchronized String generateContainerSupportCode() {
		return generateBarCode();
	}

	// ProcessusTypeCode-ProjectCode-SampeCode-YYYYMMDDHHMMSSSS
	@Override
	public synchronized String generateProcessCode(Process process) {
		if (process.sampleOnInputContainer != null) {
			return (process.sampleOnInputContainer.sampleCode + "_" + process.typeCode + "_" + generateBarCode()).toUpperCase();
		} else if (process.sampleCodes != null && process.sampleCodes.size() > 0) {
			return (process.sampleCodes.iterator().next() + "_" + process.typeCode + "_" + generateBarCode()).toUpperCase();
		} else {
			throw new RuntimeException("Cannot generate process code");
		}
	}

	@Override
	public synchronized String generateExperimentCode(Experiment exp) {
		return generateExperimentCode(exp.typeCode);
	}

	@Override
	public synchronized String generateExperimentCode(String typeCode) {
		try {
			Thread.sleep(1);
		} catch (InterruptedException e1) {
			logger.error("Interruption error: " + e1.getMessage(), e1);
		}

		//		String date = getSimpleDateFormat("yyyyMMdd_HHmmssSSS").format(new Date());
		String date = getNowFormatted("yyyyMMdd_HHmmssSSS");
		Pattern p = Pattern.compile("([0-9]{8})_([0-9]{6})([0-9]{1})([0-9]{1})([0-9]{1})");
		// Matcher m = p.matcher("151231235959999");//worst situation
		Matcher m = p.matcher(date);
		if (m.matches()) {
			String code =
					m.group(1)           // yyyyMMdd
					+ "_" + m.group(2)   // HHmmss
					+ ""  + Integer.toString(Integer.valueOf(m.group(3)) + 10, 36)  // millisecond digit 1
					+       Integer.toString(Integer.valueOf(m.group(4)) + 10, 36)  // millisecond digit 2
					+       Integer.toString(Integer.valueOf(m.group(5)) + 10, 36); // millisecond digit 3
			//			return (typeCode + "-" + code).toUpperCase();
			String exCode = (typeCode + "-" + code).toUpperCase();
			logger.debug("generateExperimentCode {}", exCode);
			return exCode;
		} else {
			//			try {
			//				logger.error("Error matches of the date fail"+date);
			//				throw new Exception("matches fail " + date);
			//			} catch (Exception e) {
			//				logger.error("Matches error: "+e.getMessage(),e);
			//				return null;
			//			}
			// Supposedly unreachable code
			throw new RuntimeException("pattern matching of the date failed : " + date);
		}
	}

	@Override
	public synchronized String generateExperimentCommentCode(Comment com) {
		//		return (com.createUser + getSimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + Math.random()).toUpperCase();
		return (com.createUser + getNowFormatted("yyyyMMdd_HHmmss") + Math.random()).toUpperCase();
	}

	@Override
	public synchronized String generateSampleCode(String projectCode, boolean updateProject) {
		Project project = MongoDBDAO.findByCode(InstanceConstants.PROJECT_COLL_NAME, Project.class, projectCode);
		return generateSampleCode(project, updateProject);
	}

	@Override
	public synchronized String generateSampleCode(Project project, boolean updateProject) {
		String newCode = nextSampleCode(project);
		if (updateProject) {
			updateProjectSampleCodeIfNeeded(project.code, newCode);
		}
		return newCode;
	}

	
	public static String nextSampleCode(Project project) {
		String currentCode = (project.lastSampleCode != null) ? project.lastSampleCode.replace(project.code + "_", "") : null;

		String newCode   = null;
		String beginCode = null;
		char lastLetter  = 'Z';

		if (StringUtils.isBlank(currentCode)) {
			newCode = "A";
			while (newCode.length() < project.nbCharactersInSampleCode) {
				newCode = newCode + "A";
			}
			// ALTERNATE: Apache version : newCode = StringUtils.repeat('A', project.nbCharactersInSampleCode);
		} else {			
			int nbCharacter   = currentCode.length();
			int lastCharacter = nbCharacter;

			// Recupère la position à partir de laquelle il faut changer de lettre
			while (lastCharacter != 0 && currentCode.substring(lastCharacter-1, lastCharacter).equals(Character.toString(lastLetter))) {
				// ALTERNATE: while (lastCharacter != 0 && currentCode.charAt(lastCharacter-1) == lastLetter) {
				lastCharacter--;
			}

			if ( lastCharacter > 1 || (lastCharacter == 1 && !currentCode.substring(lastCharacter-1, lastCharacter).equals(String.valueOf(lastLetter)))) {
				beginCode = currentCode.substring(0, lastCharacter-1); // debut du code sample a conserver
				newCode   = beginCode + Character.toString((char) (currentCode.charAt(lastCharacter-1) + 1)); // Concatenation debut code sample + lettre suivante
			} else {
				newCode = "A";
				while (newCode.length() < project.nbCharactersInSampleCode + 1) {
					newCode = newCode + "A";
				}
				lastCharacter = newCode.length();
			}
			while (lastCharacter < nbCharacter) {
				newCode = newCode + 'A';
				lastCharacter++;
			}
		}
		return project.code + "_" + newCode;
	}

	@Override
	public synchronized void updateProjectSampleCodeIfNeeded(String projectCode, String newSampleCode) {
		Integer nbCharactersInSampleCode = newSampleCode.replace(projectCode + "_", "").length();

		MongoDBDAO.update(InstanceConstants.PROJECT_COLL_NAME, Project.class, 
				DBQuery.is("code", projectCode)
				.or(DBQuery.notExists("lastSampleCode"), 
						DBQuery.lessThan("nbCharactersInSampleCode", nbCharactersInSampleCode), 
						DBQuery.is("nbCharactersInSampleCode", nbCharactersInSampleCode).lessThan("lastSampleCode", newSampleCode)),
				DBUpdate.set("lastSampleCode", newSampleCode)
				.set("nbCharactersInSampleCode", nbCharactersInSampleCode));
	}

	@Override
	public void updateProjectSampleCodeWithLastSampleCode(String projectCode) {
		ProjectionBuilder dbProject = DBProjection.include("code");
		dbProject.append("name", new BasicDBObject("$strLenCP", "$code"));
		Pipeline pipeline = Aggregation.match(DBQuery.in("projectCodes", projectCode)).project(dbProject).sort(DBSort.desc("code")).sort(DBSort.desc("name")).limit(1);
		Sample lastSample = null;
		List<Sample> samples = new ArrayList<>();
		Iterator<Sample> it = MongoDBDAO.aggregate(InstanceConstants.SAMPLE_COLL_NAME, Sample.class, pipeline).iterator();
		it.forEachRemaining((s) -> samples.add(s));
		if(samples!=null && samples.size()==1){
			lastSample = samples.get(0);
		}
		//TODO pas de règle sur la nomenclature sample car code commun CNS/CNG
		//if(lastSample!=null && lastSample.code.contains("_")){
		if(lastSample!=null){
			Integer nbCharactersInSampleCode = lastSample.code.replace(projectCode + "_", "").length();
			MongoDBDAO.update(InstanceConstants.PROJECT_COLL_NAME, Project.class, 
					DBQuery.is("code", projectCode),
					DBUpdate.set("lastSampleCode", lastSample.code)
					.set("nbCharactersInSampleCode", nbCharactersInSampleCode));
		}else{
			//TODO get lastSampleCode from LIMS if exist
			MongoDBDAO.update(InstanceConstants.PROJECT_COLL_NAME, Project.class, 
					DBQuery.is("code", projectCode),
					DBUpdate.unset("lastSampleCode"));
		}
		
	}

	@Override
	public String generateUmbrellaProjectCode() {
		return "UP_" + generateBarCode();
	}

}
