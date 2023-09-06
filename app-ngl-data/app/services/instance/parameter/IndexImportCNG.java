package services.instance.parameter;

import java.sql.SQLException;
import java.util.List;

import javax.inject.Inject;

import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.ngl.NGLApplication;
import models.Constants;
import models.LimsCNGDAO;
import models.laboratory.common.instance.TraceInformation;
import models.laboratory.parameter.Parameter;
import models.laboratory.parameter.index.IlluminaIndex;
import models.laboratory.parameter.index.Index;
import models.laboratory.parameter.index.NanoporeIndex;
import models.utils.InstanceConstants;
import models.utils.InstanceHelpers;
import models.utils.dao.DAOException;
import services.instance.AbstractImportDataCNG;
import validation.ContextValidation;

/**
 * Import Indexes from CNG's LIMS to NGL, ( no update for index)
 * FDS remplacement de l'appel a Logger par logger
 * 
 * @author dnoisett
 * 
 */

public class IndexImportCNG extends AbstractImportDataCNG {

	@Inject
	public IndexImportCNG(NGLApplication app) {
		super("IndexImportCNS", app);
	}

//	@Override
//	public void runImport() throws SQLException, DAOException {
//		/* 22/08/2018 Ne plus importer les index du LIMS Solexa
//		 *     le modele des Index a changé et la requete d'import 
//		 *     n'est plus adaptée (ajout groupNames) les nouveaux index doivent etre crees directement dans NGL
//		 * importIndexIllumina(limsServices,contextError);  //01/03/2017 chgt de nom + remise des params....
//		 */
//		createIndexChromium(contextError);
//		createIndexNanopore(contextError);
//	}
	@Override
	public void runImport(ContextValidation contextError) throws SQLException, DAOException {
		/* 22/08/2018 Ne plus importer les index du LIMS Solexa
		 *     le modele des Index a changé et la requete d'import 
		 *     n'est plus adaptée (ajout groupNames) les nouveaux index doivent etre crees directement dans NGL
		 * importIndexIllumina(limsServices,contextError);  //01/03/2017 chgt de nom + remise des params....
		 */
		createIndexChromium(contextError);
		createIndexNanopore(contextError);
	}

	//01/03/2017 chgt de nom + remise des params....
	public void importIndexIllumina(LimsCNGDAO limsServices, ContextValidation contextValidation) throws SQLException, DAOException{
		logger.info("start loading indexes");
		
		//-1- chargement depuis la base source Postgresql
		logger.info("1/3 loading from source database...");
		List<Index> indexes = limsServices.findIndexIlluminaToCreate(contextValidation) ;
		logger.info("found "+indexes.size() + " items");
		
		//-2a- trouver les index concernés dans la base mongoDB et les supprimer
		logger.info("2/3 delete from dest database...");
		for (Index index:indexes) {
			if (MongoDBDAO.checkObjectExistByCode(InstanceConstants.PARAMETER_COLL_NAME, Index.class, index.code)) {
				MongoDBDAO.deleteByCode(InstanceConstants.PARAMETER_COLL_NAME, Index.class, index.code);
			}
		}

		//-3- sauvegarde dans la base cible MongoDb
		logger.info("3/3 saving to dest database...");
//		InstanceHelpers.save(InstanceConstants.PARAMETER_COLL_NAME,indexes,contextError);
		InstanceHelpers.save(InstanceConstants.PARAMETER_COLL_NAME,indexes,contextValidation);
		
		logger.info("end loading indexes");
	}
	
	public static void createIndexChromium(ContextValidation contextValidation) throws DAOException{
		
		IndexImportUtils.getChromiumIndex().forEach((k,v)-> {
			Index index = getChromiumIndex(k,  v);
			if(!MongoDBDAO.checkObjectExistByCode(InstanceConstants.PARAMETER_COLL_NAME, Parameter.class, index.code)){
				//Logger.info("creation index : "+ index.code +" / "+ index.categoryCode);
				InstanceHelpers.save(InstanceConstants.PARAMETER_COLL_NAME,index,contextValidation);
			} else {
				//Logger.info("index : "+ index.code + " already exists !!");
			}
		});			
		
	}

	private static Index getChromiumIndex(String code, String seq) {
		Index index = new IlluminaIndex();
		
		index.code = code;
		index.name = code;
		index.shortName = code;
		index.sequence = seq ;  //Voir plus tard: il y a 4 sequences pour les POOL-INDEX...Chromium
		index.categoryCode = "POOL-INDEX";
		index.supplierName = "10x Genomics";
		index.supplierIndexName = code;
		index.traceInformation=new TraceInformation(Constants.NGL_DATA_USER);
		
		return index;
	}
	
	public static void createIndexNanopore(ContextValidation contextValidation) {

		for (int i = 1; i <= 12; i++) {
			Index index = getNanoporeIndex(i);
			if (!MongoDBDAO.checkObjectExistByCode(InstanceConstants.PARAMETER_COLL_NAME, Parameter.class,
					index.code)) {
				InstanceHelpers.save(InstanceConstants.PARAMETER_COLL_NAME, index, contextValidation);
			}
		}

	}

	private static Index getNanoporeIndex(int i) {
		Index index = new NanoporeIndex();
		String code = (i < 10)?"NB0"+i:"NB"+i;
		index.code = code;
		index.name = code;
		index.shortName = code;
		index.sequence = code;
		index.categoryCode = "SINGLE-INDEX";
		index.supplierName = "oxfordNanopore";
		index.supplierIndexName = code;
		index.traceInformation=new TraceInformation(Constants.NGL_DATA_USER);
		return index;
	}
}
