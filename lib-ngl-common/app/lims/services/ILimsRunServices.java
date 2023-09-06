package lims.services;

import java.util.List;

import lims.models.experiment.ContainerSupport;
import lims.models.experiment.Experiment;
import lims.models.instrument.Instrument;
import mail.MailServiceException;
import models.laboratory.run.instance.ReadSet;
import models.laboratory.run.instance.Run;
import models.laboratory.sample.instance.Sample;

/**
 * Common interface to extract data from CNS or CNG Lims
 * @author galbini
 *
 */
public interface ILimsRunServices {

	List<Instrument> getInstruments();
	
	Experiment getExperiments(Experiment experiment);
	
	ContainerSupport getContainerSupport(String supportCode);

	void valuationRun(Run run);
	
	void valuationReadSet(ReadSet readSet, boolean firstTime);
	
	
	void insertRun(Run run, List<ReadSet> readSets, boolean deleteBeforeInsert);
	
	void updateReadSetAfterQC(ReadSet readSet);
	
	public void updateReadSetEtat(ReadSet readset, int etat);
	
	public void updateReadSetArchive(ReadSet readset);
	
	public void linkRunWithMaterielManip();
	
	public Sample findSampleToCreate(String sampleCode);
	
	public void sendMailFVQC(ReadSet readSet) throws MailServiceException;
}
