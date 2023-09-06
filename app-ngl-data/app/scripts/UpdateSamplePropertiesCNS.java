package scripts;

import java.util.ArrayList;
import java.util.Date;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.lfw.controllers.scripts.buffered.ScriptWithArgsAndExcelBody;
import models.laboratory.common.instance.Comment;
import models.laboratory.common.instance.PropertyValue;
import models.laboratory.common.instance.property.PropertySingleValue;
import models.laboratory.sample.instance.Sample;
import models.utils.InstanceConstants;
import play.Logger;
import validation.ContextValidation;

/**
 * Script permettant de modifier les prop d'un ensemble d'echantillon CNS
 * !ATTENTION! Penser a faire la correction sur l'échantillon parent lorsqu'il y a un arbre de vie
 * Prend en entrée un fichier excel au format suivant 
 * CodeSample property newPropValue
 * ex de lancement : 
 * http://localhost:9000/scripts/run/scripts.UpdateSamplePropertiesCNS?jira=SUPSQ-4444 + fichier excel dans le body
 */
public class UpdateSamplePropertiesCNS extends ScriptWithArgsAndExcelBody<UpdateSamplePropertiesCNS.Args> {
	public static class Args {
		public String jira;
	}

	@Override
	public void execute(Args args, XSSFWorkbook workbook) throws Exception {
		//Entête
		Logger.info("Code sample;Property;oldValue;newValue");
		//NGL-4111
		if ( ! args.jira.matches("^(SUPSQ|SUPSQCNG|NGL)-\\d+$") ) {
			throw new RuntimeException("argument jira " +  args.jira + " qui n'a pas la forme attendue SUPSQ-XXX ou SUPSQCNG-XXX ou NGL-XXX");
		}
		
		//NGL-4111
		String stComment = args.jira + " UpdateSamplePropertiesCNS";
		
		workbook.getSheetAt(0).rowIterator().forEachRemaining(row -> {
			if(row.getRowNum() == 0) return; // skip header
			if(row!=null && row.getCell(0)!=null && row.getCell(1)!=null) {
				String sampleCode = row.getCell(0).getStringCellValue();
				String property = row.getCell(1).getStringCellValue();
				String newValue = row.getCell(2).getStringCellValue();
				
				PropertyValue valueInDB=new PropertySingleValue("undef");
				
				//Get sampleCode
				Sample sample = MongoDBDAO.findByCode(InstanceConstants.SAMPLE_COLL_NAME, Sample.class, sampleCode);
				if(sample!=null) {
					//Create contexte validation 
					ContextValidation ctx = ContextValidation.createUpdateContext("ngl-support");
					
					//Update Property
					if (sample.properties.get(property) != null)
					valueInDB = sample.properties.get(property);
					
					Logger.info(sampleCode+";"+property+";"+valueInDB.getValue()+";"+newValue);
					
					if (sample.properties.get(property) == null)
						sample.properties.put(property, new PropertySingleValue(newValue));
					
					sample.properties.get(property).assignValue(newValue);
					
					sample.traceInformation.modifyUser="ngl-support";
					sample.traceInformation.modifyDate=new Date();
					//NGL-4111
					if (sample.technicalComments == null) {
						 sample.technicalComments =  new ArrayList<Comment>();
					} 
					sample.technicalComments.add(new Comment(stComment, "ngl-support", true));

					sample.validate(ctx);
					if(!ctx.hasErrors()) {
						MongoDBDAO.update(InstanceConstants.SAMPLE_COLL_NAME, sample);
					} else {
						Logger.error("Error sample property "+sampleCode+" "+ctx.getErrors());
					}
				}else {
					Logger.error("Sample code not found "+sampleCode);
				}
			}
		});
		println("End of update ");
		Logger.info("End of update ");

	}

}
