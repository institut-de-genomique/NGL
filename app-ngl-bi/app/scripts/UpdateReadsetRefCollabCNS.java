package scripts;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.lfw.controllers.scripts.buffered.ScriptWithExcelBody;
import models.laboratory.run.instance.ReadSet;
import models.utils.InstanceConstants;
import play.Logger;
import validation.ContextValidation;

public class UpdateReadsetRefCollabCNS extends ScriptWithExcelBody{

	/**
	 * Script permettant de modifier les refCollab d'un ensemble de readset CNS
	 * Attention, !!!! à n'utiliser que lorsque la cascade de maj de pro a écrasé la ref d'origine,
	 *  ou que la refCollab a été corrigée dans le cadre d'une soumission!!
	 * !!!! Contexte où on doit copier cette ref dans refCollabSub et ré-initialiser readset.readsetOnContainer.referenceCollab
	 *
	
	 * Prend en entrée un fichier excel au format suivant 
	 * CodeReadset RefCollab
	 */
	@Override
	public void execute(XSSFWorkbook workbook) throws Exception {
		workbook.getSheetAt(0).rowIterator().forEachRemaining(row -> {
			if(row.getRowNum() == 0) return; // skip header
			if(row!=null && row.getCell(0)!=null && row.getCell(1)!=null){
				String readsetCode = row.getCell(0).getStringCellValue();
				String newRefCollab = row.getCell(1).getStringCellValue();
				Logger.debug("Code readset "+readsetCode+" ref collab "+newRefCollab);
				//Get readsetCode
				ReadSet readset = MongoDBDAO.findByCode(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, readsetCode);
				if(readset!=null){
					//Create contexte validation 
					ContextValidation ctx = ContextValidation.createUpdateContext("ngl-support");
					//Update refCollab
					readset.sampleOnContainer.referenceCollab=newRefCollab;			

					readset.validate(ctx);
					if(!ctx.hasErrors()){
						MongoDBDAO.update(InstanceConstants.READSET_ILLUMINA_COLL_NAME, readset);
					}else{
						Logger.error("Error readset update refCollab "+readsetCode+" "+ctx.getErrors());
					}
				}else{
					Logger.error("readset code not found "+readsetCode);
				}
			}
		});
		println("End of readsetRefCollab update ");
		Logger.info("End of readsetRefCollab update ");
	}

}
