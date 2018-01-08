package services.instance.protocol;

import static services.instance.InstanceFactory.newProtocol;

import java.util.ArrayList;
import java.util.List;

import org.mongojack.DBQuery;

import com.typesafe.config.ConfigFactory;

import fr.cea.ig.MongoDBDAO;
import models.laboratory.protocol.instance.Protocol;
import models.utils.InstanceConstants;
import models.utils.InstanceHelpers;
import play.Logger;
import scala.collection.script.Remove;
import services.instance.InstanceFactory;
import validation.ContextValidation;

public class ProtocolServiceGET {	

	private final static String institute = "GET";

	public static void main(ContextValidation ctx) {	

		Logger.info("Start to create protocols collection for "+institute+"...");
		Logger.info("Remove protocol");
		removeProtocols(ctx);
		Logger.info("Save protocols ...");
		saveProtocols(ctx);
		Logger.info(institute+" Protocols collection creation is done!");
	}
	
	
	private static void removeProtocols(ContextValidation ctx) {
		MongoDBDAO.delete(InstanceConstants.PROTOCOL_COLL_NAME, Protocol.class, DBQuery.empty());
	}


	public static void saveProtocols(ContextValidation ctx){		
		List<Protocol> lp = new ArrayList<Protocol>();
		
		lp.add(newProtocol("FAA_MO_MiSeq_240316.pdf","FAA_MO_MiSeq_240316.pdf","path5","1","production", InstanceFactory.setExperimentTypeCodes("prepa-flowcell","illumina-depot")));
		
//		lp.add(newProtocol("RPG_MO_Hiseq2000 et HiSeq2500_050416.pdf","RPG_MO_Hiseq2000 et HiSeq2500_050416.pdf","path7","1","production", InstanceFactory.setExperimentTypeCodes("prepa-flowcell", "illumina-depot")));
		
		lp.add(newProtocol("RPG_MO_Hiseq3000_250316.pdf","RPG_MO_Hiseq3000_250316.pdf","path8","1","production", InstanceFactory.setExperimentTypeCodes("prepa-flowcell","prepa-fc-ordered", "illumina-depot")));
//		lp.add(newProtocol("QPCR","QPCR","path8","1","production", InstanceFactory.setExperimentTypeCodes("qpcr-quantification")));
//		lp.add(newProtocol("solution_stock","solution_stock","path3","1","production", InstanceFactory.setExperimentTypeCodes("solution-stock")));
//		lp.add(newProtocol("pooling","pooling","path7","1","production", InstanceFactory.setExperimentTypeCodes("pool-tube","pool-x-to-tubes")));
		
		//		if(	!ConfigFactory.load().getString("ngl.env").equals("PROD") ){
//			lp.add(newProtocol("aliquoting","aliquoting","path1","1","production", InstanceFactory.setExperimentTypeCodes("aliquoting")));
//			lp.add(newProtocol("fragmentation","Fragmentation","path1","1","production", InstanceFactory.setExperimentTypeCodes("fragmentation")));
//			lp.add(newProtocol("librairie-indexing","librairie","path2","1","production", InstanceFactory.setExperimentTypeCodes("librairie-indexing"))); 
//			lp.add(newProtocol("amplification","amplification","path3","1","production", InstanceFactory.setExperimentTypeCodes("amplification", "solution-stock")));
//			lp.add(newProtocol("chip-migration-post-pcr","chip-migration-post-pcr","path7","1","production", InstanceFactory.setExperimentTypeCodes("chip-migration-post-pcr", "chip-migration-pre-pcr", "fluo-quantification"/*, "qpcr-quantification"*/)));
//			lp.add(newProtocol("ptr_pool_tube_v1","PTR_POOL_TUBE_v1","path7","1","production", InstanceFactory.setExperimentTypeCodes("pool-tube")));
//			
//			lp.add(newProtocol("map005","MAP005","path7","1","production", InstanceFactory.setExperimentTypeCodes("nanopore-library")));
//			lp.add(newProtocol("map005-on-beads"," MAP005 sur billes","path7","1","production", InstanceFactory.setExperimentTypeCodes("nanopore-library")));
//
//			
//			lp.add(newProtocol("map005-depot","MAP005_dépôt","path7","1","production", InstanceFactory.setExperimentTypeCodes("nanopore-depot")));
//			lp.add(newProtocol("map005-on-bead-depot","MAP005 sur billes_dépôt","path7","1","production", InstanceFactory.setExperimentTypeCodes("nanopore-depot")));
//			lp.add(newProtocol("map006-depot","MAP006_dépôt","path7","1","production", InstanceFactory.setExperimentTypeCodes("nanopore-depot")));
//						
//			lp.add(newProtocol("map005-preCR","MAP005 preCR","path7","1","production", InstanceFactory.setExperimentTypeCodes("nanopore-fragmentation")));
//			lp.add(newProtocol("map006-preCR","MAP006 preCR","path7","1","production", InstanceFactory.setExperimentTypeCodes("nanopore-fragmentation")));
//			lp.add(newProtocol("map006-FFPE","MAP006 FFPE","path7","1","production", InstanceFactory.setExperimentTypeCodes("nanopore-fragmentation")));
//
//			lp.add(newProtocol("map006","MAP006","path7","1","production", InstanceFactory.setExperimentTypeCodes("nanopore-library","nanopore-fragmentation")));
//			
//
//			
//		}
		
		for(Protocol protocole:lp){
			InstanceHelpers.save(InstanceConstants.PROTOCOL_COLL_NAME, protocole,ctx);
			Logger.debug(" Protocole "+protocole.code);
		}
	}
	
	





}
