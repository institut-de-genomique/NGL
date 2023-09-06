package services.instance.parameter;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.ngl.NGLApplication;
import models.Constants;
import models.LimsCNSDAO;
import models.laboratory.common.instance.TraceInformation;
import models.laboratory.parameter.Parameter;
import models.laboratory.parameter.index.IlluminaIndex;
import models.laboratory.parameter.index.Index;
import models.laboratory.parameter.index.NanoporeIndex;
import models.utils.InstanceConstants;
import models.utils.InstanceHelpers;
import models.utils.dao.DAOException;
import services.instance.AbstractImportDataCNS;
import validation.ContextValidation;

public class IndexImportCNS extends AbstractImportDataCNS {

	@Inject
	public IndexImportCNS(NGLApplication app) {
		super("IndexImportCNS", app);
	}

//	@Override
//	public void runImport() throws SQLException, DAOException {
//		createIndexIllumina(limsServices,contextError);
//		createIndexNanopore(contextError);
//		createIndexChromium(contextError);
//		createIndexNEBNext(contextError);
//		createIndexCustom(contextError);
//		createIndexNEXTflexSmRna(contextError);
//	}
	
	@Override
	public void runImport(ContextValidation contextError) throws SQLException, DAOException {
		createIndexIllumina     (limsServices,contextError);
		createIndexNanopore     (contextError);
		createIndexChromium     (contextError);
		createIndexNEBNext      (contextError);
		createIndexCustom       (contextError);
		createIndexNEXTflexSmRna(contextError);
	}

	public static void createIndexIllumina(LimsCNSDAO limsServices,ContextValidation contextValidation) throws SQLException, DAOException {		
		List<Index> indexs = limsServices.findIndexIlluminaToCreate(contextValidation) ;

		for (Index index : indexs) {
			if (MongoDBDAO.checkObjectExistByCode(InstanceConstants.PARAMETER_COLL_NAME, Parameter.class, index.code)) {
				MongoDBDAO.deleteByCode(InstanceConstants.PARAMETER_COLL_NAME, Parameter.class, index.code);
			}
			InstanceHelpers.save(InstanceConstants.PARAMETER_COLL_NAME,index,contextValidation);
		}			
	}
	
	public static void createIndexNanopore(ContextValidation contextValidation) {
		for (int i = 1; i <= 12; i++) {
			Index index = getNanoporeIndex(i);
			if (!MongoDBDAO.checkObjectExistByCode(InstanceConstants.PARAMETER_COLL_NAME, Parameter.class, index.code)) {
				InstanceHelpers.save(InstanceConstants.PARAMETER_COLL_NAME, index, contextValidation);
			}
		}
	}

	private static Index getNanoporeIndex(int i) {
		Index index = new NanoporeIndex();
		String code = (i < 10) ? "NB0"+i : "NB"+i;
		index.code         = code;
		index.name         = code;
		index.shortName    = code;
		index.sequence     = code;
		index.categoryCode = "SINGLE-INDEX";
		index.supplierName = "oxfordNanopore";
		index.supplierIndexName = code;
		index.traceInformation = new TraceInformation(Constants.NGL_DATA_USER);
		return index;
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
	
	private void createIndexNEBNext(ContextValidation contextValidation) {
		List<Index> indexes = new ArrayList<>();
		indexes.add(getNEBNextIndex("NEBNext1", "ATCACG", "IND1"));
		
		indexes.forEach(index-> {
			if(!MongoDBDAO.checkObjectExistByCode(InstanceConstants.PARAMETER_COLL_NAME, Parameter.class, index.code)){
				logger.info("creation index : "+ index.code +" / "+ index.categoryCode);
				InstanceHelpers.save(InstanceConstants.PARAMETER_COLL_NAME,index,contextValidation);
			} else {
				logger.info("index : "+ index.code + " already exists !!");
			}
		});			
	}
	
	private static Index getNEBNextIndex(String code, String seq, String shortName) {
		Index index = new IlluminaIndex();
		
		index.code = code;
		index.name = code;
		index.shortName = shortName;
		index.sequence = seq ; 
		index.categoryCode = "SINGLE-INDEX";
		index.supplierName = "NEB";
		index.supplierIndexName = code;
		index.traceInformation=new TraceInformation(Constants.NGL_DATA_USER);
		
		return index;
	}
	
	private void createIndexNEXTflexSmRna(ContextValidation contextValidation) {
		List<Index> indexes = new ArrayList<>();
		
		indexes.add(getNEXTflexSmRna("NEXTflexSmRna01", "ATCACG", "IND1","PCRprimer1"));
		indexes.add(getNEXTflexSmRna("NEXTflexSmRna02", "CGATGT", "IND2","PCRprimer2"));
		indexes.add(getNEXTflexSmRna("NEXTflexSmRna04", "TGACCA", "IND4","PCRprimer4"));
		indexes.add(getNEXTflexSmRna("NEXTflexSmRna05", "ACAGTG", "IND5","PCRprimer5"));
		indexes.add(getNEXTflexSmRna("NEXTflexSmRna09", "GATCAG", "IND9","PCRprimer9"));
		indexes.add(getNEXTflexSmRna("NEXTflexSmRna10", "TAGCTT", "IND10","PCRprimer10"));
		indexes.add(getNEXTflexSmRna("NEXTflexSmRna12", "CTTGTA", "IND12","PCRprimer12"));
		indexes.add(getNEXTflexSmRna("NEXTflexSmRna19", "GTGAAA", "IND19","PCRprimer19"));
		
		indexes.forEach(index-> {
			if(!MongoDBDAO.checkObjectExistByCode(InstanceConstants.PARAMETER_COLL_NAME, Parameter.class, index.code)){
				logger.info("creation index : "+ index.code +" / "+ index.categoryCode);
				InstanceHelpers.save(InstanceConstants.PARAMETER_COLL_NAME,index,contextValidation);
			} else {
				logger.info("index : "+ index.code + " already exists !!");
			}
		});			
	}
	
	private static Index getNEXTflexSmRna(String code, String seq, String shortName, String supplier) {
		Index index = new IlluminaIndex();
		
		index.code = code;
		index.name = code;
		index.shortName = shortName;
		index.sequence = seq ; 
		index.categoryCode = "SINGLE-INDEX";
		index.supplierName = "BiooScientific";
		index.supplierIndexName = supplier;
		index.traceInformation=new TraceInformation(Constants.NGL_DATA_USER);
		
		return index;
	}
	private void createIndexCustom(ContextValidation contextValidation) {
		List<Index> indexes = new ArrayList<>();
		
		indexes.add(getCustomIndex("EXT001","AAACAA","EXT001"));
		indexes.add(getCustomIndex("EXT002","ACATAC","EXT002"));
		indexes.add(getCustomIndex("EXT003","ACCATC","EXT003"));
		indexes.add(getCustomIndex("EXT004","ACGCAT","EXT004"));
		indexes.add(getCustomIndex("EXT005","ACTGCC","EXT005"));
		indexes.add(getCustomIndex("EXT006","AGATCG","EXT006"));
		indexes.add(getCustomIndex("EXT007","AGGGGA","EXT007"));
		indexes.add(getCustomIndex("EXT008","ATACCT","EXT008"));
		indexes.add(getCustomIndex("EXT009","ATGGTT","EXT009"));
		indexes.add(getCustomIndex("EXT010","ATTAAA","EXT010"));
		indexes.add(getCustomIndex("EXT011","ATTCTC","EXT011"));
		indexes.add(getCustomIndex("EXT012","CAAAAT","EXT012"));
		indexes.add(getCustomIndex("EXT013","CAACTG","EXT013"));
		indexes.add(getCustomIndex("EXT014","CACGAA","EXT014"));
		indexes.add(getCustomIndex("EXT015","CATAGA","EXT015"));
		indexes.add(getCustomIndex("EXT016","CCGAGT","EXT016"));
		indexes.add(getCustomIndex("EXT017","CGGCAC","EXT017"));
		indexes.add(getCustomIndex("EXT018","CTATCA","EXT018"));
		indexes.add(getCustomIndex("EXT019","CTCGGT","EXT019"));
		indexes.add(getCustomIndex("EXT020","CTCTAG","EXT020"));
		indexes.add(getCustomIndex("EXT021","GACCCC","EXT021"));
		indexes.add(getCustomIndex("EXT022","GATGCA","EXT022"));
		indexes.add(getCustomIndex("EXT023","GCAACG","EXT023"));
		indexes.add(getCustomIndex("EXT024","GCTAGC","EXT024"));
		indexes.add(getCustomIndex("EXT025","GGGCCG","EXT025"));
		indexes.add(getCustomIndex("EXT026","GTAAAC","EXT026"));
		indexes.add(getCustomIndex("EXT027","GTGGGG","EXT027"));
		indexes.add(getCustomIndex("EXT028","GTGTAT","EXT028"));
		indexes.add(getCustomIndex("EXT029","TAGTAA","EXT029"));
		indexes.add(getCustomIndex("EXT030","TCAGCT","EXT030"));
		indexes.add(getCustomIndex("EXT031","TCCCGG","EXT031"));
		indexes.add(getCustomIndex("EXT032","TCCTTT","EXT032"));
		indexes.add(getCustomIndex("EXT033","TCTCAA","EXT033"));
		indexes.add(getCustomIndex("EXT034","TGCATA","EXT034"));
		indexes.add(getCustomIndex("EXT035","TGTCTG","EXT035"));
		indexes.add(getCustomIndex("EXT036","TGTGAC","EXT036"));
		indexes.add(getCustomIndex("EXT037","TTTTGG","EXT037"));
		indexes.add(getCustomIndex("EXT038","CGTATA","EXT038"));
		indexes.add(getCustomIndex("EXT039","TGATCG","EXT039"));
		indexes.add(getCustomIndex("EXT040","CGCTAT","EXT040"));
		indexes.add(getCustomIndex("EXT041","TGAACA","EXT041"));
		indexes.add(getCustomIndex("EXT042","GTATCT","EXT042"));
		indexes.add(getCustomIndex("EXT043","CAGCTA","EXT043"));
		indexes.add(getCustomIndex("EXT044","TGAGCC","EXT044"));
		indexes.add(getCustomIndex("EXT045","CGATGA","EXT045"));
		indexes.add(getCustomIndex("EXT046","TAGATG","EXT046"));
		indexes.add(getCustomIndex("EXT047","TCTCGC","EXT047"));
		indexes.add(getCustomIndex("EXT048","TGATGC","EXT048"));
		indexes.add(getCustomIndex("EXT049","GACGAC","EXT049"));
		indexes.add(getCustomIndex("EXT050","GACACT","EXT050"));
		indexes.add(getCustomIndex("EXT051","GACCGG","EXT051"));
		indexes.add(getCustomIndex("EXT052","GAGATA","EXT052"));
		indexes.add(getCustomIndex("EXT053","CTGACA","EXT053"));
		indexes.add(getCustomIndex("EXT054","TGCAGG","EXT054"));
		indexes.add(getCustomIndex("EXT055","CGACCT","EXT055"));
		indexes.add(getCustomIndex("EXT056","TGACGT","EXT056"));
		indexes.add(getCustomIndex("EXT057","ACGTGC","EXT057"));
		indexes.add(getCustomIndex("EXT058","TGATCC","EXT058"));
		indexes.add(getCustomIndex("EXT059","GAGAAG","EXT059"));
		indexes.add(getCustomIndex("EXT060","ACAGTC","EXT060"));
		indexes.add(getCustomIndex("EXT061","CATCGT","EXT061"));
		indexes.add(getCustomIndex("EXT062","TTGAAC","EXT062"));
		indexes.add(getCustomIndex("EXT063","ACGTCG","EXT063"));
		indexes.add(getCustomIndex("EXT064","AATATG","EXT064"));
		indexes.add(getCustomIndex("EXT065","GACTTG","EXT065"));

		indexes.forEach(index-> {
			if(!MongoDBDAO.checkObjectExistByCode(InstanceConstants.PARAMETER_COLL_NAME, Parameter.class, index.code)){
				logger.info("creation index : "+ index.code +" / "+ index.categoryCode);
				InstanceHelpers.save(InstanceConstants.PARAMETER_COLL_NAME,index,contextValidation);
			} else {
				logger.info("index : "+ index.code + " already exists !!");
			}
		});			
	}	
	
	private static Index getCustomIndex(String code, String seq, String shortName) {
		Index index = new IlluminaIndex();
		
		index.code = code;
		index.name = code;
		index.shortName = shortName;
		index.sequence = seq ; 
		index.categoryCode = "SINGLE-INDEX";
		index.traceInformation=new TraceInformation(Constants.NGL_DATA_USER);
		
		return index;
	}
}
