package scripts;

import java.util.*;

import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.lfw.controllers.scripts.buffered.ScriptNoArgs;
import models.laboratory.common.instance.property.PropertyImgValue;
import models.laboratory.container.instance.Container;
import models.laboratory.container.instance.ContainerSupport;
import models.laboratory.experiment.instance.Experiment;
import models.laboratory.run.instance.ReadSet;
import models.laboratory.run.instance.Run;
import models.laboratory.run.instance.Treatment;
import models.utils.InstanceConstants;

import play.Logger;

public class ScriptReprise4732Logique1 extends ScriptNoArgs {

	// A passer à "true" quand je veux me lancer, "false" si je suis en test.
	private boolean UPDATE_MONGO = true;

	@Override
	public void execute() throws Exception {
		Logger.error("Start ScriptReprise4732");

		repriseContainers();

		repriseSupports();

		repriseProcess();

		repriseExperiences();

		repriseRuns();

		repriseReadsets();

		Logger.error("End ScriptReprise4732");
	}

	private void repriseRuns() {
		Logger.error("Start repriseRuns");

		inverserRun("230426_EXTSEQUEL2TLS_DA234859A01", "230426_EXTSEQUEL2TLS_DA234882C01-TEMP", "DA234882C01", "DA234859A01");
		inverserRun("230502_EXTSEQUEL2TLS_DA234883D01", "230502_EXTSEQUEL2TLS_DA234859A01", "DA234859A01", "DA234883D01");
		inverserRun("230502_EXTSEQUEL2TLS_DA234882C01", "230502_EXTSEQUEL2TLS_DA234883D01", "DA234883D01", "DA234882C01");

		inverserRun("230426_EXTSEQUEL2TLS_DA234882C01-TEMP", "230426_EXTSEQUEL2TLS_DA234882C01");

		Logger.error("End repriseRuns");
	}

	private void inverserRun(String codeRun, String bonCodeRun) {
		Logger.error("Traitement du run '" + codeRun + "'.");

		Run run = MongoDBDAO.findByCode(InstanceConstants.RUN_ILLUMINA_COLL_NAME, Run.class, codeRun);

		if (run != null) {
			run.code = bonCodeRun;

			run.traceInformation.modificationStamp("ngl-support");

			if (UPDATE_MONGO) {
				MongoDBDAO.update(InstanceConstants.RUN_ILLUMINA_COLL_NAME, run);
			}
		} else {
			Logger.error("Run non trouvé.");
		}
	}

	private void inverserRun(String codeRun, String bonCodeRun, String newContainerSupportCode, String oldContainerSupportCode) {
		Logger.error("Traitement du run '" + codeRun + "'.");

		Run run = MongoDBDAO.findByCode(InstanceConstants.RUN_ILLUMINA_COLL_NAME, Run.class, codeRun);

		if (run != null) {
			run.traceInformation.modificationStamp("ngl-support");

			run.code = bonCodeRun;
			run.containerSupportCode = newContainerSupportCode;

			run.lanes.forEach(l -> {
				Logger.error("Remplacement du code readset dans la lane du run.");

				String newRsCode = l.readSetCodes.get(0).replace(oldContainerSupportCode, newContainerSupportCode);
				l.readSetCodes = Collections.singletonList(newRsCode);
			});

			if (UPDATE_MONGO) {
				MongoDBDAO.update(InstanceConstants.RUN_ILLUMINA_COLL_NAME, run);
			}
		} else {
			Logger.error("Run non trouvé.");
		}
	}

	private void repriseReadsets() {
		Logger.error("Start repriseReadsets");

		// Cas 1
		inverserReadset("DFK_DG_AAAF_1_DA234859A01", "DFK_DG_AAAF_1_DA234882C01", "DA234859A01", "DA234882C01");

		// Cas 2
		inverserReadset("DDY_DG_AAAA_1_DA234883D01", "DDY_DG_AAAA_1_DA234859A01", "DA234883D01", "DA234859A01");

		// Cas 3
		inverserReadset("CLX_DG_AABE_1_DA234882C01", "CLX_DG_AABE_1_DA234883D01", "DA234882C01", "DA234883D01");

		Logger.error("End repriseReadsets");
	}

	private void inverserReadset(String codeReadset, String bonCodeReadset, String oldContainerCode, String codeContainerCorrect) {
		Logger.error("Traitement du readset '" + codeReadset + "'.");

		ReadSet readset = MongoDBDAO.findByCode(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, codeReadset);

		if (readset != null) {
			readset.traceInformation.modificationStamp("ngl-support");

			readset.code = bonCodeReadset;
			readset.runCode = readset.runCode.replace(oldContainerCode, codeContainerCorrect);
			readset.path = readset.path.replace(oldContainerCode, codeContainerCorrect);

			readset.files.forEach(f -> {
				f.fullname = f.fullname.replace(oldContainerCode, codeContainerCorrect);
			});

			Treatment readQuality = readset.treatments.get("readQuality");

			readQuality.results.forEach((treatmentName, treatmentContent) -> {
				treatmentContent.forEach((propertyName, propertyValue) -> {
					if (propertyValue._type.equals("img")) {
						Logger.error("Mise à jour de la propriété '" + propertyName + "' dans le traitement '" + treatmentName + "'.");

						((PropertyImgValue) propertyValue).fullname = ((PropertyImgValue) propertyValue).fullname.replace(oldContainerCode, codeContainerCorrect);
					}
				});
			});

			readset.sampleOnContainer.containerCode = codeContainerCorrect;
			readset.sampleOnContainer.containerSupportCode = codeContainerCorrect;

			if (UPDATE_MONGO) {
				MongoDBDAO.update(InstanceConstants.READSET_ILLUMINA_COLL_NAME, readset);
			}
		} else {
			Logger.error("Readset non trouvé.");
		}
	}

	private void repriseSupports() {
		Logger.error("Start repriseSupports");

		// Cas 1
		inverserContainerSupport("DA234859A01", "DA234882C01-TEMP");

		// Cas 2
		inverserContainerSupport("DA234883D01", "DA234859A01");

		// Cas 3
		inverserContainerSupport("DA234882C01", "DA234883D01");

		inverserContainerSupport("DA234882C01-TEMP", "DA234882C01");

		Logger.error("End repriseSupports");
	}

	private void repriseContainers() {
		Logger.error("Start repriseContainers");

		// Cas 1
		inverserContainer("DA234859A01", "DA234882C01-TEMP");

		// Cas 2
		inverserContainer("DA234883D01", "DA234859A01");

		// Cas 3
		inverserContainer("DA234882C01", "DA234883D01");

		inverserContainer("DA234882C01-TEMP", "DA234882C01");

		Logger.error("End repriseContainers");
	}

	private void inverserContainerSupport(String oldContainerCode, String newContainerCode) {
		Logger.error("Traitement du support '" + oldContainerCode + "'.");
		ContainerSupport containerToUdpate = MongoDBDAO.findByCode(InstanceConstants.CONTAINER_SUPPORT_COLL_NAME, ContainerSupport.class, oldContainerCode);
		
		if (containerToUdpate != null) {
			containerToUdpate.traceInformation.modificationStamp("ngl-support");

			containerToUdpate.code = newContainerCode;

			if (UPDATE_MONGO) {
				MongoDBDAO.update(InstanceConstants.CONTAINER_SUPPORT_COLL_NAME, containerToUdpate);
			}
		} else {
			Logger.error("Support non trouvé.");
		}
	}

	private void inverserContainer(String oldContainerCode, String newContainerCode) {
		Logger.error("Traitement du container '" + oldContainerCode + "'.");
		Container containerToUdpate = MongoDBDAO.findByCode(InstanceConstants.CONTAINER_COLL_NAME, Container.class, oldContainerCode);
		
		if (containerToUdpate != null) {
			containerToUdpate.traceInformation.modificationStamp("ngl-support");

			containerToUdpate.code = newContainerCode;
			containerToUdpate.support.code = newContainerCode;

			if (UPDATE_MONGO) {
				MongoDBDAO.update(InstanceConstants.CONTAINER_COLL_NAME, containerToUdpate);
			}
		} else {
			Logger.error("Container non trouvé.");
		}
	}

	private void repriseProcess() {
		Logger.error("Start repriseProcess");

		// Cas 1
		inverserProcess("DFK_AAAF_PACBIO-WG-OR-METAGENOME-LIBRARY_87BE10T1V", "DA234859A01", "DA234859A01", "DA234882C01", "DA234882C01");

		// Cas 2
		inverserProcess("DDY_AAAA_PACBIO-WG-OR-METAGENOME-LIBRARY_87BE10T1H", "DA234883D01", "DA234883D01", "DA234859A01", "DA234859A01");

		// Cas 3
		inverserProcess("CLX_AABE_PACBIO-WG-OR-METAGENOME-LIBRARY_87BE10T1L", "DA234882C01", "DA234882C01", "DA234883D01", "DA234883D01");

		Logger.error("End repriseProcess");
	}

	private void inverserProcess(String processCode, String oldContainerCodeEnfait, String oldContainerSupportCodeEnfant, String newContainerCodeEnfant, String newContainerSupportCodeEnfant) {
		Logger.error("Traitement du process '" + processCode + "'.");
		models.laboratory.processes.instance.Process process = MongoDBDAO.findByCode(InstanceConstants.PROCESS_COLL_NAME, models.laboratory.processes.instance.Process.class, processCode);

		if (process != null) {
			process.traceInformation.modificationStamp("ngl-support");

			process.outputContainerCodes.remove(oldContainerCodeEnfait);
			process.outputContainerCodes.add(newContainerCodeEnfant);

			process.outputContainerSupportCodes.remove(oldContainerSupportCodeEnfant);
			process.outputContainerSupportCodes.add(newContainerSupportCodeEnfant);

			if (UPDATE_MONGO) {
				MongoDBDAO.update(InstanceConstants.PROCESS_COLL_NAME, process);
			}
		} else {
			Logger.error("Process non trouvé.");
		}
	}	

	private void repriseExperiences() {
		Logger.error("Start repriseExperiences");

		// Logique numéro 1
		inverserExperienceOutput("PREP-SMRT-CELL-20230711_161951CAJ", "DA234882C01");

		// Logique numéro 1 b
		inverserExperienceInput("PACBIO-DEPOT-20230711_162424EGE", "DA234882C01");

		// Logique numéro 1
		inverserExperienceOutput("PREP-SMRT-CELL-20230711_161301EJE", "DA234859A01");

		// Logique numéro 1 b
		inverserExperienceInput("PACBIO-DEPOT-20230711_162315AAA", "DA234859A01");

		// Logique numéro 1
		inverserExperienceOutput("PREP-SMRT-CELL-20230711_161750AFJ", "DA234883D01");

		// Logique numéro 1 b
		inverserExperienceInput("PACBIO-DEPOT-20230711_162347HBB", "DA234883D01");		

		Logger.error("End repriseExperiences");
	}

	private void inverserExperienceInput(String expCode, String newInputContUsedCode) {
		Logger.error("Traitement de l'expérience '" + expCode + "'.");
		Experiment exp = MongoDBDAO.findByCode(InstanceConstants.EXPERIMENT_COLL_NAME, Experiment.class, expCode);

		if (exp != null) {
			exp.traceInformation.modificationStamp("ngl-support");

			exp.inputContainerSupportCodes = new HashSet<String>() {{ add(newInputContUsedCode); }};
			exp.inputContainerCodes = new HashSet<String>() {{ add(newInputContUsedCode); }};

			exp.atomicTransfertMethods.get(0).inputContainerUseds.get(0).code = newInputContUsedCode;
			exp.atomicTransfertMethods.get(0).inputContainerUseds.get(0).locationOnContainerSupport.code = newInputContUsedCode;	

			if (UPDATE_MONGO) {
				MongoDBDAO.update(InstanceConstants.EXPERIMENT_COLL_NAME, exp);
			}
		} else {
			Logger.error("Expérience non trouvée.");
		}
	}

	private void inverserExperienceOutput(String expCode, String newSmrtCellCode) {
		Logger.error("Traitement de l'expérience '" + expCode + "'.");
		Experiment exp = MongoDBDAO.findByCode(InstanceConstants.EXPERIMENT_COLL_NAME, Experiment.class, expCode);

		if (exp != null) {
			exp.traceInformation.modificationStamp("ngl-support");

			exp.instrumentProperties.get("containerSupportCode").value = newSmrtCellCode;

			exp.atomicTransfertMethods.get(0).outputContainerUseds.get(0).code = newSmrtCellCode;
			exp.atomicTransfertMethods.get(0).outputContainerUseds.get(0).locationOnContainerSupport.code = newSmrtCellCode;

			exp.outputContainerSupportCodes = new HashSet<String>() {{ add(newSmrtCellCode); }};
			exp.outputContainerCodes = new HashSet<String>() {{ add(newSmrtCellCode); }};

			if (UPDATE_MONGO) {
				MongoDBDAO.update(InstanceConstants.EXPERIMENT_COLL_NAME, exp);
			}
		} else {
			Logger.error("Expérience non trouvée.");
		}
	}
}