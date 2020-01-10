package controllers.migration.cng;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;

import models.Constants;
import models.LimsCNGDAO;
import models.laboratory.common.instance.TraceInformation;
import models.laboratory.container.instance.Container;
import models.laboratory.parameter.Parameter;
import models.laboratory.parameter.index.IlluminaIndex;
import models.laboratory.parameter.index.Index;
import models.utils.InstanceConstants;
import models.utils.InstanceHelpers;
import models.utils.dao.DAOException;
import models.utils.instance.ContainerHelper;

import org.mongojack.JacksonDBCollection;
///import org.springframework.stereotype.Repository;



import play.Logger;
import play.mvc.Result;
///import play.api.modules.spring.Spring;
import play.mvc.Result;
import validation.ContextValidation;
import controllers.CommonController;
import fr.cea.ig.MongoDBDAO;

/**
 * Creation des index Chromium ( avec sequence fictive pour l'instant)
 * @author fdsantos
 * 17/03/2017
 */

public class AddIndexChromium extends  CommonController {
	
	private static final String PARAMETER_COLL_NAME_BCK = InstanceConstants.PARAMETER_COLL_NAME + "_BCK_032017";
	
	public static Result addIndex() {	
		JacksonDBCollection<Parameter, String> parametersCollBck = MongoDBDAO.getCollection(PARAMETER_COLL_NAME_BCK, Parameter.class);
		if (parametersCollBck.count() == 0) {
			// collection backup vide (inexistante ??)=> faire le backup
			backUpParameterColl();
			
			Logger.info("AddIndexChromium starts");
			try {
				createIndexChromium();
			}
			catch(Exception e) {
				Logger.error(e.getMessage());
			}
									
		} else {
			Logger.info("AddIndexChromium already executed !");
		}		
		Logger.info("AddIndexChromium ended");
		
		// est affiché dans le naviguateur
		return ok("Finish");
	}

	private static void backUpParameterColl() {
		Logger.info("\tCopie "+InstanceConstants.PARAMETER_COLL_NAME+" starts");
		MongoDBDAO.save(PARAMETER_COLL_NAME_BCK, MongoDBDAO.find(InstanceConstants.PARAMETER_COLL_NAME, Parameter.class).toList());
		Logger.info("\tCopie "+InstanceConstants.PARAMETER_COLL_NAME+" ended");
	}
	
	// FDS 01/03/2017 creation des index pour processus Chromium (mais utilisés au final en sequencage-illumina)
	// Plaque=> 96 index SI-GA-<ligne>-<col>
	private static void createIndexChromium() throws DAOException{
		
		ContextValidation contextValidation=new ContextValidation(Constants.NGL_DATA_USER);
		
	
		for ( int row = 1; row <=8; row++){
			for(int col = 1 ; col <= 12 ; col++){
				Index index = getChromiumIndex(row,col);				
				if(!MongoDBDAO.checkObjectExistByCode(InstanceConstants.PARAMETER_COLL_NAME, Parameter.class, index.code)){
					Logger.info("creation index : "+ index.code +" / "+ index.categoryCode);
					InstanceHelpers.save(InstanceConstants.PARAMETER_COLL_NAME,index,contextValidation);
				} else {
					Logger.info("index : "+ index.code + " already exists !!");
				}
			}
		}
	}

	// FDS 16/03/2017 !!! si on remplace la sequence par qq chose (ici un nom) il faut que la longueur soit la meme
	// sinon lors du pooling, une regle drools de validation va generer une erreur
	//==> utiliser le format A01 et non A1 pour la position !!! seulement pour la sequence
	private static Index getChromiumIndex(int row, int col) {
		Index index = new IlluminaIndex();
		
		String code = "SI-GA-"+ (char)(64 + row);
		String seq=code;
		if (col < 10 ) { seq = seq +"0"; }
		code=code + col;
		seq=seq+ col;
		
		index.code = code;
		index.name = code;
		index.shortName = code;
		index.sequence = seq ;  //Voir plus tard: il y a 4 sequences pour les POOL-INDEX...Chromium
		index.categoryCode = "POOL-INDEX";
		index.supplierName = new HashMap<String,String>();
		index.supplierName.put("10x Genomics", code);
		index.traceInformation=new TraceInformation("ngl-data");
		
		return index;
	}
}