package scripts;

import java.util.*;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.mongojack.DBQuery;

import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.lfw.controllers.scripts.buffered.ScriptWithExcelBody;
import models.laboratory.sample.instance.Sample;
import models.utils.InstanceConstants;
import play.Logger;

/**
 * Script permettant de mettre à jour le champ 'processesToLaunchDate' sur les samples donnés en entrée du script.
 * Pour chaque sample, on regarde également l'ascendance et on met à jour le champ 'processesToLaunchDate' si nécessaire. 
 * 
 * Ce script est à utiliser dans un contexte de reporting nocturne.
 * 
 * @author Jordi CHARPENTIER jcharpen@genoscope.cns.fr 
 */
public class UpdateSampleLaunchDateFromSamples extends ScriptWithExcelBody {

	@Inject
	public UpdateSampleLaunchDateFromSamples() {

	}

	@Override
	public void execute(XSSFWorkbook workbook) throws Exception { 		
		println("Début du script");

		workbook.getSheetAt(0).rowIterator().forEachRemaining(row -> {	
			if (row.getRowNum() == 0) {
				return; 
			}

			if (row != null && row.getCell(0) != null) { 
				String sampleCode = row.getCell(0).getStringCellValue().trim();
				   
				Logger.info("Traitement du sample '" + sampleCode + "'.");

				Sample sample = MongoDBDAO.findByCode(InstanceConstants.SAMPLE_COLL_NAME, Sample.class, sampleCode);

				if (sample != null) {
					List<String> scToFind = new ArrayList<>();

					if (sample.life != null) {
						List<String> sampleCodeFromChild = Arrays.asList(sample.life.path.split(","));
						sampleCodeFromChild = sampleCodeFromChild.stream().filter(sc -> sc.length() > 0).collect(Collectors.toList());
			
						scToFind.addAll(sampleCodeFromChild);
					}

					List<Sample> sampleList = MongoDBDAO.find(InstanceConstants.SAMPLE_COLL_NAME, Sample.class, DBQuery.in("code", scToFind)).toList();
					sampleList.add(sample);

					sampleList.forEach(sampleToUpdate -> {
						sampleToUpdate.setProcessesToLaunchDate(new Date());

						MongoDBDAO.update(InstanceConstants.SAMPLE_COLL_NAME, sampleToUpdate);
					});
				} else {
					println("Le code du sample donné n'existe pas (code : '" + sampleCode + "').");
				}
			} else {
				println("Ligne vide, elle sera ignorée.");
			}
		});

		println("Fin du script");
	}
}