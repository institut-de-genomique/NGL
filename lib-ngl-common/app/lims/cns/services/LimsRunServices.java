package lims.cns.services;

import static fr.cea.ig.play.IGGlobals.configuration;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.time.DateUtils;
import org.mongojack.DBQuery;
import org.mongojack.DBQuery.Query;
import org.mongojack.DBUpdate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mongodb.BasicDBObject;

//import play.Logger;
//import play.Logger.ALogger;
// import play.Play;
import fr.cea.ig.MongoDBDAO;
import lims.cns.dao.LimsAbandonDAO;
import lims.cns.dao.LimsExperiment;
import lims.cns.dao.LimsLibrary;
import lims.models.LotSeqValuation;
import lims.models.experiment.ContainerSupport;
import lims.models.experiment.Experiment;
import lims.models.experiment.illumina.BanqueSolexa;
import lims.models.experiment.illumina.DepotSolexa;
import lims.models.experiment.illumina.Flowcell;
import lims.models.experiment.illumina.Library;
import lims.models.instrument.Instrument;
import lims.models.runs.ResponProjet;
import lims.models.runs.TacheHD;
import lims.services.ILimsRunServices;
import mail.MailServiceException;
import mail.MailServices;
import models.laboratory.common.instance.TBoolean;
import models.laboratory.common.instance.Valuation;
import models.laboratory.common.instance.property.PropertySingleValue;
import models.laboratory.run.instance.Lane;
//import models.laboratory.run.instance.Lane;
import models.laboratory.run.instance.ReadSet;
import models.laboratory.run.instance.Run;
import models.laboratory.sample.instance.Sample;
import models.utils.InstanceConstants;
import models.utils.dao.DAOException;


@Service
public class LimsRunServices implements ILimsRunServices{

	@Autowired
	LimsAbandonDAO dao;

	// ALogger logger = Logger.of("CNS");
	private static final play.Logger.ALogger logger = play.Logger.of(LimsRunServices.class);

	private Map<String, Integer> crScoring;
	private Map<Integer, Integer> scoreMapping;

	/*
	 *
Conta:mat ori        					9	TAXO-contaMatOri
Qlte:duplicat>30    					42	Qlte-duplicat
Qlte:repartition bases       			41	Qlte-repartitionBases

Conta mat ori + duplicat>30				43	TAXO-contaMatOri ; Qlte-duplicat
Conta mat ori + rep bases				44	TAXO-contaMatOri ; Qlte-repartitionBases
Duplicat>30 + rep bases					45	Qlte-duplicat ; Qlte-repartitionBases
Conta mat ori + duplicat>30 + rep bases	46	TAXO-contaMatOri ; Qlte-duplicat ; Qlte-repartitionBases


	 */

	public LimsRunServices() {
		crScoring = new HashMap<>();
		crScoring.put("TAXO-contaMatOri", 1);
		crScoring.put("Qlte-duplicat", 2);
		crScoring.put("Qlte-repartitionBases", 4);

		scoreMapping = new HashMap<>();
		scoreMapping.put(1, 9);
		scoreMapping.put(2, 42);
		scoreMapping.put(4, 41);
		scoreMapping.put(3, 43);
		scoreMapping.put(5, 44);
		scoreMapping.put(6, 45);
		scoreMapping.put(7, 46);
	}

	@Override
	public List<Instrument> getInstruments() {
		throw new RuntimeException("Not Implemented");
	}

	private Query getQuery(Experiment experiment) {
		Query q = DBQuery.is("typeCode", "illumina-depot");
		
		if(null != experiment.containerSupportCode)
			q.in("inputContainerSupportCodes", experiment.containerSupportCode);
		
		if( null != experiment.date){
			q.greaterThanEquals("experimentProperties.runStartDate.value", experiment.date);
			q.lessThanEquals("experimentProperties.runStartDate.value", (DateUtils.addDays(experiment.date, 1)));		
		}
		
		if(null != experiment.instrument && null != experiment.instrument.code){
			q.is("instrument.code", experiment.instrument.code);
		}
		return q; 
	}
	
	@Override
	public Experiment getExperiments(Experiment experiment) {
		//NGL
		List<models.laboratory.experiment.instance.Experiment> nglExps =  MongoDBDAO.find(InstanceConstants.EXPERIMENT_COLL_NAME, models.laboratory.experiment.instance.Experiment.class, 
				getQuery(experiment)).toList();
		if(nglExps.size() == 1){
			
			models.laboratory.experiment.instance.Experiment nglExp = nglExps.get(0);
			Experiment exp = new Experiment();
			exp.containerSupportCode = experiment.containerSupportCode;
			exp.instrument = new Instrument();
			exp.instrument.code = nglExp.instrument.code;
			exp.instrument.categoryCode = nglExp.instrument.typeCode;
			
			if(nglExp.experimentProperties.containsKey("runStartDate")){
				exp.date = (Date)nglExp.experimentProperties.get("runStartDate").value;
				
			}else{
				exp.date = nglExp.traceInformation.creationDate;
			}
			
			exp.nbCycles = (Integer)nglExp.instrumentProperties.get("nbCyclesRead1").value
						+ (Integer)nglExp.instrumentProperties.get("nbCyclesRead2").value
						+ (Integer)nglExp.instrumentProperties.get("nbCyclesReadIndex1").value
						+ (Integer)nglExp.instrumentProperties.get("nbCyclesReadIndex2").value;
					
			//exp.date = limsExp.date; //runStartDate
			//exp.nbCycles = limsExp.nbCycles; //instrument
			
			return exp;
		}else if(nglExps.size() > 1){
			return null;
		}	else{
			//old lims
			List<LimsExperiment> limsExps = dao.getExperiments(experiment);
			if (limsExps.size() == 1) {
				LimsExperiment limsExp = limsExps.get(0);
				Experiment exp = new Experiment();
				exp.date = limsExp.date;
				exp.containerSupportCode = experiment.containerSupportCode;
				exp.instrument = new Instrument();
				exp.instrument.code = limsExp.code;
				exp.instrument.categoryCode = getInstrumentCategoryCode(exp);
				exp.nbCycles = limsExp.nbCycles;
				logger.debug(limsExp.toString());		
				return exp;
			} else {
				return null;
			}
		}
	}

	private String getInstrumentCategoryCode(Experiment exp) {
		try {
			return models.laboratory.instrument.description.Instrument.find.get().findByCode(exp.instrument.code).typeCode;
		} catch (DAOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public ContainerSupport getContainerSupport(String supportCode) {
		List<LimsLibrary> limsReadSets = dao.geContainerSupport(supportCode);
		Flowcell flowcell = null;
		if (limsReadSets != null && limsReadSets.size() > 0) {
			flowcell = new Flowcell();
			flowcell.containerSupportCode = supportCode;

			Map<Integer, lims.models.experiment.illumina.Lane> lanes = new HashMap<>();

			for (LimsLibrary lrs : limsReadSets) {
				lims.models.experiment.illumina.Lane currentLane = lanes.get(lrs.laneNumber);
				if (null == currentLane) {
					currentLane = new lims.models.experiment.illumina.Lane();
					currentLane.number = lrs.laneNumber;
					currentLane.librairies = new ArrayList<>();
					lanes.put(lrs.laneNumber, currentLane);
				}

				Library lib = new Library();
				lib.sampleContainerCode = lrs.sampleBarCode;
				lib.sampleCode = lrs.sampleCode;
				lib.tagName = lrs.indexName;
				lib.tagSequence = lrs.indexSequence;
				lib.projectCode = lrs.projectCode;
				lib.insertLength = lrs.insertLength;
				lib.typeCode = lrs.experimentTypeCode;
				if(null != lrs.indexName && lrs.indexTypeCode != 3)lib.isIndex = Boolean.TRUE;
				else if(null != lrs.indexName)lib.isIndex = Boolean.FALSE;
				currentLane.librairies.add(lib);
			}
			flowcell.lanes = lanes.values();
		}
		return flowcell;
	}

	@Override
	public void valuationRun(Run run) {
		try{
			dao.updateRunAbandon(run.code, getAbandon(run.valuation, run.code), 47);
			for(Lane lane: run.lanes){
				if(dao.isPistco(run.code, lane.number)){
					dao.updatePisteAbandon(run.code, lane.number, getAbandon(lane.valuation, run.code), 47);
				}
			}
		}catch(Throwable t){
			logger.error(run.code+" : "+t.getMessage());
		}
	}
	@Override
	public void valuationReadSet(ReadSet readSet, boolean firstTime) {
		try {			
//			logger.info("valuationReadSet : "+readSet.code+" / "+firstTime);
			logger.info("valuationReadSet : {} / {}", readSet.code, firstTime);
			if (firstTime)
				sendMailFVQC(readSet);
			
			Integer cptreco = null;
			Integer tacheId = null;
			if (firstTime && dao.isLseqco(readSet)) {
				List<TacheHD> taches = dao.listTacheHD(readSet.code);
				if (taches.size() > 1) {
					logger.error(readSet.code+" : Plusieurs Taches");					
				} else if(taches.size() == 1) {
					tacheId = taches.get(0).tacco;
				} else {
					logger.error(readSet.code+" : O Tache");
				}
				LotSeqValuation lsv = dao.getLotsequenceValuation(readSet.code);
				if (lsv != null) {
					logger.debug(lsv.toString());
					if (lsv.tacco != null)
						tacheId = lsv.tacco;
					if (lsv.cptreco != null)
						cptreco = lsv.cptreco;				
				}				
				
				if (cptreco == null || cptreco == 47) { //used to manage history recovery
					cptreco = getCR(readSet.productionValuation);
				}
				
				try{
					dao.updateLotsequenceAbandon(readSet.code, getSeqVal(readSet.productionValuation, readSet.code), cptreco, tacheId, 55);
					if(!TBoolean.UNSET.equals(readSet.bioinformaticValuation.valid)){
						dao.updateLotsequenceAbandonBI(readSet.code, getAbandon(readSet.bioinformaticValuation, readSet.code));
					}
				}catch(Throwable t){  //in case of deadlock situation or other error we retry
					logger.warn(readSet.code+" : first : "+t.getMessage());
					dao.updateLotsequenceAbandon(readSet.code, getSeqVal(readSet.productionValuation, readSet.code), cptreco, tacheId, 55);
					if(!TBoolean.UNSET.equals(readSet.bioinformaticValuation.valid)){
						dao.updateLotsequenceAbandonBI(readSet.code, getAbandon(readSet.bioinformaticValuation, readSet.code));
					}
				}
				
			}else if(dao.isLseqco(readSet)){
				try{
					LotSeqValuation lsv = dao.getLotsequenceValuation(readSet.code);
					if (lsv != null) {
						logger.debug(lsv.toString());
						if (lsv.tacco != null) 
							tacheId = lsv.tacco;
						if (lsv.cptreco != null)
							cptreco = lsv.cptreco;				
					} else {
						logger.error("LotSeqValuation is null for "+readSet.code);
					}
					if (cptreco == null || cptreco == 47) { //used to manage history recovery
						cptreco = getCR(readSet.productionValuation);
					}
					dao.updateLotsequenceAbandon(readSet.code, getSeqVal(readSet.productionValuation, readSet.code), cptreco, tacheId, 55);
					if (!TBoolean.UNSET.equals(readSet.bioinformaticValuation.valid)) {
						dao.updateLotsequenceAbandonBI(readSet.code, getAbandon(readSet.bioinformaticValuation, readSet.code));
					}
				} catch (Exception t) { // in case of deadlock situation or other error we retry
					logger.warn(readSet.code+" : second : "+t.getMessage());
//					LotSeqValuation lsv = 
							dao.getLotsequenceValuation(readSet.code);
					dao.updateLotsequenceAbandon(readSet.code, getSeqVal(readSet.productionValuation, readSet.code), cptreco, tacheId, 55);
					if (!TBoolean.UNSET.equals(readSet.bioinformaticValuation.valid)) {
						dao.updateLotsequenceAbandonBI(readSet.code, getAbandon(readSet.bioinformaticValuation, readSet.code));
					}
				}
				
			}
		} catch (Exception t) {
			logger.error(readSet.code + " : " + t.getMessage(), t);
		}
	}

	@Override
	public synchronized void sendMailFVQC(ReadSet readSet) throws MailServiceException {
		logger.debug("send mail agirs");
		if (!MongoDBDAO.checkObjectExist(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, 
				DBQuery.is("runCode", readSet.runCode).notIn("state.historical.code", "F-VQC"))
				&& MongoDBDAO.checkObjectExist(InstanceConstants.RUN_ILLUMINA_COLL_NAME, Run.class, 
						DBQuery.is("code", readSet.runCode).notEquals("properties.sendMailAgirs.value", Boolean.TRUE))) {
			
			logger.debug("send mail agirs now");
			String biurl = "http://ngl-bi.genoscope.cns.fr";
			
			List<ReadSet> readsets = MongoDBDAO.find(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, 
					DBQuery.is("runCode", readSet.runCode), getReadSetKeys()).toList();
			
			Map<String, List<ReadSet>> mReadSets = readsets.stream()
					 .collect(Collectors.groupingBy((ReadSet r) -> r.projectCode));
			
			StringBuffer message = new StringBuffer();
			message.append("<html><meta http-equiv='content-type' content='text/html; charset=ISO-8859-1'>");
			message.append("<div>Bonjour,<br/>"
					+ "<br/>Tous les readsets du run <a href='"+biurl+"/runs/"+readSet.runCode+"'>"+readSet.runCode+"</a> ont ete evalues.<br/>"
					+"<br/>Vous trouverez ci-dessous les readsets classes par projet.<br/>"
					+"N'hesitez pas a cliquer sur le nom d'un readset pour voir le details de ses traitements."
					+ "</div>");
			message.append("<h3 style='text-decoration: underline;'>").append(readSet.runCode).append("</h3>");
			
			for (String key : mReadSets.keySet()) {				
				ResponProjet rp = dao.getResponProjet(key);				
				message.append("<h4 style='text-decoration: underline;'>Projet : ").append(key).append("</h4>");
				message.append("<div style='color:green;'>").append(rp.name).append("</div>");
				message.append("<div style='color:black;'>").append(rp.biomanager).append("</div>");
				message.append("<div style='color:black;'>").append(rp.infomanager).append("</div>").append("<br/>");				
				mReadSets.get(key).forEach((ReadSet r) -> message.append("<a href='"+biurl+"/readsets/"+r.code+"'>").append(r.code).append("</a><br/>"));
				message.append("<br/>");
			}
			message.append("<br/>Merci et a bientot sur <a href='"+biurl+"'>NGL-BI</a> !");
			message.append("</html>");
			
//			String alertMailExp = Play.application().configuration().getString("validation.mail.from"); 
//			String alertMailDest = Play.application().configuration().getString("validation.mail.to");    	
			String alertMailExp  = configuration().getString("validation.mail.from"); 
			String alertMailDest = configuration().getString("validation.mail.to");    	
			MailServices mailService = new MailServices();
			Set<String> destinataires = new HashSet<>();
			destinataires.addAll(Arrays.asList(alertMailDest.split(",")));
			mailService.sendMail(alertMailExp, destinataires, "[NGL-BI] Tous les readsets du run "+readSet.runCode+" ont ete evalues.", message.toString());
			
			//update run properties
			MongoDBDAO.update(InstanceConstants.RUN_ILLUMINA_COLL_NAME,  Run.class, 
					DBQuery.is("code", readSet.runCode),
					DBUpdate.set("properties.sendMailAgirs", new PropertySingleValue(Boolean.TRUE)));
		}				
	}

	private static BasicDBObject getReadSetKeys() {
		BasicDBObject keys = new BasicDBObject();
		keys.put("treatments", 0);
		return keys;
	}

	
	private Integer getCR(Valuation valuation) {
		int score = 0;
		if(valuation.resolutionCodes !=null){
			for(String cr : valuation.resolutionCodes){
				score += (crScoring.get(cr) != null)?crScoring.get(cr).intValue():0;

			}
		}
		Integer crId = scoreMapping.get(score);
		return (crId != null) ? crId : 47;		
	}

	private Integer getAbandon(Valuation valuation, String code) {
		if (TBoolean.FALSE.equals(valuation.valid)) {
			return 1; //abandon=true
		} else if(TBoolean.TRUE.equals(valuation.valid)) {
			return 0; //abandon = false;
		} else {
			throw new RuntimeException("Abandon : Mise à jour abandon run ou readset (" + code + ") dans lims mais valuation à UNSET");
		}
	}

	private Integer getSeqVal(Valuation valuation, String code) {
		if (TBoolean.FALSE.equals(valuation.valid)) {
			return 0; //a abandonner
		} else if(TBoolean.TRUE.equals(valuation.valid)) {
			return 1; //valide;
		} else {
			return 2;
		}
	}

	@Override
	public void insertRun(Run run, List<ReadSet> readSets, boolean deleteBeforeInsert) {
		try {
			if (deleteBeforeInsert) {
				try {
					dao.deleteRun(run.code);
					dao.deleteFlowcellNGL(run.containerSupportCode);					
				} catch (Exception t) {
					throw new RuntimeException("Delete RUN : " + run.code + " : " + t.getMessage(), t);
				}
			}
			SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yy");
			DepotSolexa ds = dao.getDepotSolexa(run.containerSupportCode, sdf.format(run.sequencingStartDate));
			if (ds == null)
				ds = insertFlowcellNGL(run);
			if (ds != null) {
				Map<String, BanqueSolexa> mapBanques = new HashMap<>();
				for(BanqueSolexa banque:  dao.getBanqueSolexa(run.containerSupportCode)){
					String key = banque.prsco+"_"+banque.adnnom+"_"+banque.lanenum+"_"+banque.tagkeyseq;
					//Logger.debug("key banque = "+key);
					mapBanques.put(key, banque);
				}
				if (mapBanques.size() == 0) {
					for(BanqueSolexa banque:  dao.getBanqueSolexaFlowcellNGL(run.containerSupportCode)){
						String key = banque.prsco+"_"+banque.adnnom+"_"+banque.lanenum+"_"+((banque.tagkeyseq != null)?banque.tagkeyseq:"");
						logger.debug("key banque = "+key);
						mapBanques.put(key, banque);
					}
				}
				Map<String, ReadSet> mapReadSets = new HashMap<>();
				for (ReadSet readSet:  readSets) {
					String index = (readSet.code.contains("."))?readSet.code.split("\\.")[1]:"";
					String key = readSet.sampleCode+"_"+readSet.laneNumber+"_"+index;
					//Logger.debug("key readSet = "+key);
					//we insert only that we find in dblims
					if (mapBanques.containsKey(key)) {
						mapReadSets.put(key, readSet);
					}
				}
				logger.debug("Load DepotSolexa = "+ds);
				//Delete run if exist ???
				
				dao.insertRun(run, ds);
				dao.insertLanes(run.lanes, ds);
				for (Map.Entry<String, ReadSet> entry : mapReadSets.entrySet()) {
					try {
						dao.insertReadSet(entry.getValue(), mapBanques.get(entry.getKey()));
						dao.insertFiles(entry.getValue(), false);
					}catch(NullPointerException e){
						logger.error("No readSet "+entry.getValue());
					}
				}

				dao.dispatchRun(run);
				dao.updateRunInNGL(run);
				//passe l'etat à traite
				dao.updateRunEtat(run, 2);
			} else {
				throw new RuntimeException("DepotSolexa is null");
			}
	    	// GA: Etat
	    	// GA: RunInNGL
		
		} catch (Exception t) { 
			logger.error("Synchro RUN : "+run.code+" : "+t.getMessage(),t);
		}
	}

	@Override
	public void updateReadSetAfterQC(ReadSet readset) {
		try {
			if(dao.isLseqco(readset)){
				dao.updateReadSetEtat(readset, 2);
				dao.updateReadSetBaseUtil(readset);
				dao.insertFiles(readset, true);
			}
		} catch (Exception t) {
			logger.error("Synchro READSET AfterQC: " + readset.code + " : " + t.getMessage(), t);
		}
	}

	@Override
	public void updateReadSetEtat(ReadSet readset, int etat){
		if (dao.isLseqco(readset)) {
			dao.updateReadSetEtat(readset, etat);
		}
	}
	
	@Override
	public void updateReadSetArchive(ReadSet readset) {
		try {
			if(dao.isLseqco(readset)){
				dao.updateReadSetArchive(readset);
			}
		} catch (Exception t) {
			logger.error("Synchro READSET Archive: "+readset.code+" : "+t.getMessage(),t);
		}
	}
	
	@Override
	public void linkRunWithMaterielManip() {
		try {
			dao.linkRunWithMaterielManip();
		} catch (Exception t) {
			logger.error("Synchro LINK RUN / MATERIEL_MANIP: "+t.getMessage(),t);
		}
	}
	
	public DepotSolexa insertFlowcellNGL(Run run){
		List<models.laboratory.experiment.instance.Experiment> expPrepaflowcell = MongoDBDAO.find(InstanceConstants.EXPERIMENT_COLL_NAME, models.laboratory.experiment.instance.Experiment.class,DBQuery.in("outputContainerSupportCodes", run.containerSupportCode).in("typeCode", "prepa-flowcell","prepa-fc-ordered")).toList();
		if (CollectionUtils.isEmpty(expPrepaflowcell)) {
			throw new RuntimeException("Prepaflowcell Experiment with containerOutPut "+run.containerSupportCode+" not found in NGL");
		}
		
		List<models.laboratory.experiment.instance.Experiment> expDepotIllumina = MongoDBDAO.find(InstanceConstants.EXPERIMENT_COLL_NAME, models.laboratory.experiment.instance.Experiment.class,DBQuery.in("inputContainerSupportCodes", run.containerSupportCode).is("typeCode", "illumina-depot")).toList();
		if (CollectionUtils.isEmpty(expDepotIllumina)) {
			throw new RuntimeException("DepotIllumina Experiment with containerOutPut "+run.containerSupportCode+" not found in NGL");
		}

		//Create Manip FlowcellNGL
		DepotSolexa ds = dao.insertFlowcellNGL(expPrepaflowcell.get(0),expDepotIllumina.get(0), run);
		return ds;

	}

	@Override
	public Sample findSampleToCreate(String sampleCode) {
		return dao.getMateriel(sampleCode);
	}
	
}
