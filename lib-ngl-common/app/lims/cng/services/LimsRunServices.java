package lims.cng.services;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.time.DateUtils;
import org.mongojack.DBQuery;
import org.mongojack.DBQuery.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.cea.ig.MongoDBDAO;
import lims.cng.dao.LimsExperiment;
import lims.cng.dao.LimsLibrary;
import lims.cng.dao.LimsRunDAO;
import lims.models.experiment.ContainerSupport;
import lims.models.experiment.Experiment;
import lims.models.experiment.illumina.Flowcell;
import lims.models.experiment.illumina.Lane;
import lims.models.experiment.illumina.Library;
import lims.models.instrument.Instrument;
import lims.services.ILimsRunServices;
import mail.MailServiceException;
import models.laboratory.run.instance.ReadSet;
import models.laboratory.run.instance.Run;
import models.laboratory.sample.instance.Sample;
import models.utils.InstanceConstants;
import play.Logger;


/**
 * CNG Implementation
 */
@Service
public class LimsRunServices implements ILimsRunServices {

	

	@Autowired
	LimsRunDAO dao;

	@Override
	public List<Instrument> getInstruments() {
		return dao.getInstruments();
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
			List<LimsExperiment> limsExps = dao.getExperiments(experiment);
			if(limsExps.size() == 1){
				LimsExperiment limsExp = limsExps.get(0);
				Experiment exp = new Experiment();
				exp.date = limsExp.date;
				exp.containerSupportCode = experiment.containerSupportCode;
				exp.instrument = new Instrument();
				exp.instrument.code = limsExp.code;
				exp.instrument.categoryCode = limsExp.categoryCode;	
				exp.nbCycles = limsExp.nbCycles;
				Logger.debug(limsExp.toString());		
				return exp;
			}else{
				return null;
			}
		}
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
	public ContainerSupport getContainerSupport(String supportCode) {
		List<LimsLibrary> limsReadSets = dao.geContainerSupport(supportCode);
		Flowcell flowcell = null;
		if (limsReadSets.size() > 0) {
			flowcell = new Flowcell();
			flowcell.containerSupportCode = supportCode;

			Map<Integer, Lane> lanes = new HashMap<>();

			for (LimsLibrary lrs : limsReadSets) {
				Lane currentLane = lanes.get(lrs.laneNumber);
				if (null == currentLane) {
					currentLane = new Lane();
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
		Logger.warn("Not Implemented");		
	}
	
	@Override
	public void valuationReadSet(ReadSet readSet, boolean firstTime) {
		Logger.warn("Not Implemented");		
	}

	
	
	@Override
	public void updateReadSetEtat(ReadSet readset, int etat) {
		Logger.warn("Not Implemented");		
		
	}

	@Override
	public void insertRun(Run run, List<ReadSet> readSets, boolean deleteBeforeInsert) {
		Logger.warn("Not Implemented");
	}

	@Override
	public void updateReadSetAfterQC(ReadSet readSet) {
		Logger.warn("Not Implemented");
		
	}

	@Override
	public void updateReadSetArchive(ReadSet readset) {
		Logger.warn("Not Implemented");		
	}

	@Override
	public void linkRunWithMaterielManip() {
		Logger.warn("Not Implemented");	
	}

	@Override
	public Sample findSampleToCreate(String sampleCode) {
		Logger.warn("Not Implemented");	
		return null;
	}

	@Override
	public void sendMailFVQC(ReadSet readSet) throws MailServiceException {
		Logger.warn("Not Implemented");		
	}
	
}
