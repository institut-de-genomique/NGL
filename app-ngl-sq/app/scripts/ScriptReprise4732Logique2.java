package scripts;

import java.util.*;

import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.lfw.controllers.scripts.buffered.ScriptNoArgs;
import models.laboratory.common.instance.property.PropertyImgValue;
import models.laboratory.common.instance.property.PropertySingleValue;
import models.laboratory.container.instance.Container;
import models.laboratory.container.instance.ContainerSupport;
import models.laboratory.experiment.instance.Experiment;
import models.laboratory.run.instance.ReadSet;
import models.laboratory.run.instance.Run;
import models.laboratory.run.instance.Treatment;
import models.utils.InstanceConstants;

import play.Logger;

public class ScriptReprise4732Logique2 extends ScriptNoArgs {

	private boolean UPDATE_MONGO = true;

	@Override
	public void execute() throws Exception {
		Logger.error("Start ScriptReprise4732");

		repriseContainers();

		repriseProcess();

		repriseExperiences();

		repriseRuns();

		repriseReadsets();

		Logger.error("End ScriptReprise4732");
	}

	private void repriseRuns() {
		Logger.error("Start repriseRuns");

		inverserRun("230807_EXTSEQUEL2TLS_DA261719B01", "DFK", "DDY", "AAAF", "AAAA");
		inverserRun("230807_EXTSEQUEL2TLS_DA261745A01", "DFK", "DDY", "AAAF", "AAAA");
		inverserRun("230713_EXTSEQUEL2TLS_DA260898D01", "DFK", "DDY", "AAAF", "AAAA");

		Logger.error("End repriseRuns");
	}

	// Logique différente entre pacbio et illumina / nanopore. Ce qu'on fait ici fonctionne que pour pacbio.
	private void inverserRun(String codeRun, String oldProjectCode, String newProjectCode, String oldSampleCode, String newSampleCode) {
		Logger.error("Traitement du run '" + codeRun + "'.");

		Run run = MongoDBDAO.findByCode(InstanceConstants.RUN_ILLUMINA_COLL_NAME, Run.class, codeRun);

		if (run != null) {
			run.traceInformation.modificationStamp("ngl-support");

			run.projectCodes = new HashSet<String>() {{ add(newProjectCode); }};
			run.sampleCodes = new HashSet<String>() {{ add(newProjectCode + "_" + newSampleCode); }};

			run.lanes.forEach(l -> {
				Logger.error("Remplacement du code readset dans la lane du run.");

				String newRsCode = l.readSetCodes.get(0).replace(oldSampleCode, newSampleCode);
				newRsCode = newRsCode.replace(oldProjectCode, newProjectCode);

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

		inverserReadset("DFK_DG_AAAF_1_DA261719B01", "DFK", "DDY", "AAAF", "AAAA", "DDY_DG_AAAA_1_DA234859A01");
		inverserReadset("DFK_DG_AAAF_1_DA261745A01", "DFK", "DDY", "AAAF", "AAAA", "DDY_DG_AAAA_1_DA234859A01");
		inverserReadset("DFK_DG_AAAF_1_DA260898D01", "DFK", "DDY", "AAAF", "AAAA", "DDY_DG_AAAA_1_DA234859A01");

		Logger.error("End repriseReadsets");
	}

	private void inverserReadset(String codeReadset, String oldProjectCode, String newProjectCode, String oldSampleCode, String newSampleCode, String bonRsCode) {
		Logger.error("Traitement du readset '" + codeReadset + "'.");

		ReadSet readset = MongoDBDAO.findByCode(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, codeReadset);

		if (readset != null) {
			readset.traceInformation.modificationStamp("ngl-support");

			readset.code = readset.code.replace(oldSampleCode, newSampleCode);
			readset.code = readset.code.replace(oldProjectCode, newProjectCode);

			readset.sampleCode = newProjectCode + "_" + newSampleCode;
			readset.projectCode = newProjectCode;

			readset.path = readset.path.replace(oldSampleCode, newSampleCode);
			readset.path = readset.path.replace(oldProjectCode, newProjectCode);

			ReadSet bonReadSet = MongoDBDAO.findByCode(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, bonRsCode);
			
			if (bonReadSet == null) {
				Logger.error("Pas de readset trouvé avec un 'code' à '" + bonRsCode + "'.");
			} else {
				String oldContainerSupportCode = readset.sampleOnContainer.containerSupportCode;
				String oldContainerCode = readset.sampleOnContainer.containerCode;

				readset.sampleOnContainer = bonReadSet.sampleOnContainer;
				readset.sampleOnContainer.containerSupportCode = oldContainerSupportCode;
				readset.sampleOnContainer.containerCode = oldContainerCode;
			}

			readset.files.forEach(f -> {
				Logger.error("Mise à jour du fullname du file '" + f.fullname + "'.");
				
				f.fullname = f.fullname.replace(oldProjectCode, newProjectCode);
				f.fullname = f.fullname.replace(oldSampleCode, newSampleCode);
			});

			Treatment readQuality = readset.treatments.get("readQuality");

			readQuality.results.forEach((treatmentName, treatmentContent) -> {
				treatmentContent.forEach((propertyName, propertyValue) -> {
					if (propertyValue._type.equals("img")) {
						Logger.error("Mise à jour de la propriété '" + propertyName + "' dans le traitement '" + treatmentName + "'.");

						((PropertyImgValue) propertyValue).fullname = ((PropertyImgValue) propertyValue).fullname.replace(oldProjectCode, newProjectCode);
						((PropertyImgValue) propertyValue).fullname = ((PropertyImgValue) propertyValue).fullname.replace(oldSampleCode, newSampleCode);
					}
				});
			});

			if (UPDATE_MONGO) {
				MongoDBDAO.update(InstanceConstants.READSET_ILLUMINA_COLL_NAME, readset);
			}
		} else {
			Logger.error("Readset non trouvé.");
		}
	}

	private void repriseContainers() {
		Logger.error("Start repriseContainers");

		inverserContainer("DA260898D01", "87BF3L541", "87BF3L541", "87BF3L53P", "DDY", "DDY_AAAA");
		inverserContainerSupport("DA260898D01", "DDY", "DDY_AAAA");

		inverserContainer("DA261745A01", "87BF3L541", "87BF3L541", "87BF3L53P", "DDY", "DDY_AAAA");
		inverserContainerSupport("DA261745A01", "DDY", "DDY_AAAA");

		inverserContainer("DA261719B01", "87BF3L541", "87BF3L541", "87BF3L53P", "DDY", "DDY_AAAA");
		inverserContainerSupport("DA261719B01", "DDY", "DDY_AAAA");

		Logger.error("End repriseContainers");
	}

	private void inverserContainerSupport(String codeSupport, String newProjectCode, String newSampleCode) {
		Logger.error("Traitement du container support '" + codeSupport + "'.");
		ContainerSupport containerSupportToUdpate = MongoDBDAO.findByCode(InstanceConstants.CONTAINER_SUPPORT_COLL_NAME, ContainerSupport.class, codeSupport);
		
		if (containerSupportToUdpate != null) {
			containerSupportToUdpate.traceInformation.modificationStamp("ngl-support");

			containerSupportToUdpate.projectCodes = new HashSet<String>() {{ add(newProjectCode); }};
			containerSupportToUdpate.sampleCodes = new HashSet<String>() {{ add(newSampleCode); }};

			if (UPDATE_MONGO) {
				MongoDBDAO.update(InstanceConstants.CONTAINER_SUPPORT_COLL_NAME, containerSupportToUdpate);
			}
		} else {
			Logger.error("Container support non trouvé.");
		}
	}

	private void inverserContainer(String containerCode, String fromContainerCode, String fromSupportCode, String oldPathContainerCode, String newProjectCode, String newSampleCode) {
		Logger.error("Traitement du container '" + containerCode + "'.");
		Container containerToUdpate = MongoDBDAO.findByCode(InstanceConstants.CONTAINER_COLL_NAME, Container.class, containerCode);

		if (containerToUdpate != null) {
			containerToUdpate.traceInformation.modificationStamp("ngl-support");

			containerToUdpate.projectCodes = new HashSet<String>() {{ add(newProjectCode); }};
			containerToUdpate.sampleCodes = new HashSet<String>() {{ add(newSampleCode); }};

			containerToUdpate.treeOfLife.from.containers.get(0).code = fromContainerCode;
			containerToUdpate.treeOfLife.from.containers.get(0).supportCode = fromSupportCode;

			Container newContainer = MongoDBDAO.findByCode(InstanceConstants.CONTAINER_COLL_NAME, Container.class, "DA234859A01");

			if (newContainer != null) {
				containerToUdpate.contents = newContainer.contents;
				containerToUdpate.treeOfLife.paths = newContainer.treeOfLife.paths;
			} else {
				Logger.error("Container 'DA234859A01' non trouvé.");
			}			

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
		inverserProcess("DFK_AAAF_PACBIO-RUN_89LC3Z3CW", "87BF3L541", "87BF3L541", "DDX", "DDY", "DDX_AAAT", "DDY_AAAA");
		inverserProcess("DFK_AAAF_PACBIO-RUN_89LC49CHU", "87BF3L541", "87BF3L541", "DDX", "DDY", "DDX_AAAT", "DDY_AAAA");
		inverserProcess("DFK_AAAF_PACBIO-RUN_89IJ2CLII", "87BF3L541", "87BF3L541", "DDX", "DDY", "DDX_AAAT", "DDY_AAAA");

		Logger.error("End repriseProcess");
	}

	private void inverserProcess(String processCode, String newInputContainerSupportCode, String newInputContainerCode, String newProjectCode1, String newProjectCode2, String sampleCode1, String sampleCode2) {
		Logger.error("Traitement du process '" + processCode + "'.");
		models.laboratory.processes.instance.Process process = MongoDBDAO.findByCode(InstanceConstants.PROCESS_COLL_NAME, models.laboratory.processes.instance.Process.class, processCode);

		if (process != null) {
			process.traceInformation.modificationStamp("ngl-support");

			process.inputContainerSupportCode = newInputContainerSupportCode;
			process.inputContainerCode = newInputContainerCode;

			models.laboratory.processes.instance.Process goodProcessSampleOnInput = MongoDBDAO.findByCode(InstanceConstants.PROCESS_COLL_NAME, models.laboratory.processes.instance.Process.class, "DDY_AAAA_PACBIO-WG-OR-METAGENOME-LIBRARY_87BE10T1H");
			
			if (goodProcessSampleOnInput == null) {
				Logger.error("Aucun process trouvé pour le code 'DDY_AAAA_PACBIO-WG-OR-METAGENOME-LIBRARY_87BE10T1H'.");
			} else {
				process.sampleOnInputContainer = goodProcessSampleOnInput.sampleOnInputContainer;

				process.sampleOnInputContainer.containerSupportCode = "87BF3L541";
				process.sampleOnInputContainer.containerCode = "87BF3L541";
				process.sampleOnInputContainer.containerConcentration = new PropertySingleValue(287);
				process.sampleOnInputContainer.containerVolume = new PropertySingleValue(225);
				process.sampleOnInputContainer.containerQuantity = new PropertySingleValue(64575);
			}

			process.projectCodes = new HashSet<String>() {{ add(newProjectCode1); add(newProjectCode2); }};
			process.sampleCodes = new HashSet<String>() {{ add(sampleCode1); add(sampleCode2); }};

			if (UPDATE_MONGO) {
				MongoDBDAO.update(InstanceConstants.PROCESS_COLL_NAME, process);
			}
		} else {
			Logger.error("Process non trouvé.");
		}
	}

	private void repriseExperiences() {
		Logger.error("Start repriseExperiences");

		inverserExperienceInput("PREP-SMRT-CELL-20230921_124015BFD", "87BF3L541", true, "PREP-SMRT-CELL-20230711_161301EJE", "DDY", "DDY_AAAA", false);
		inverserExperienceInput("PACBIO-DEPOT-20230921_124108CHI", "DA261745A01", true, "PREP-SMRT-CELL-20230921_124015BFD", "DDY", "DDY_AAAA", true);

		inverserExperienceInput("PREP-SMRT-CELL-20230921_124304CIE", "87BF3L541", true, "PREP-SMRT-CELL-20230711_161301EJE", "DDY", "DDY_AAAA", false);
		inverserExperienceInput("PACBIO-DEPOT-20230921_124343CFC", "DA261719B01", true, "PREP-SMRT-CELL-20230921_124304CIE", "DDY", "DDY_AAAA", true);

		inverserExperienceInput("PREP-SMRT-CELL-20230921_120002FCI", "87BF3L541", true, "PREP-SMRT-CELL-20230711_161301EJE", "DDY", "DDY_AAAA", false);
		inverserExperienceInput("PACBIO-DEPOT-20230921_123556IHD", "DA260898D01", true, "PREP-SMRT-CELL-20230921_120002FCI", "DDY", "DDY_AAAA", true);

		Logger.error("End repriseExperiences");
	}

	// "newInputContUsedCode" : si c'est un dépôt, pas utilisé, car pas à corriger.
	private void inverserExperienceInput(String expCode, String newInputContUsedCode, boolean updateContent, String codeExpAvecBonnesValeurs, String projectCode, String sampleCode, boolean isDepot) {
		Logger.error("Traitement de l'expérience '" + expCode + "'.");
		Experiment exp = MongoDBDAO.findByCode(InstanceConstants.EXPERIMENT_COLL_NAME, Experiment.class, expCode);

		if (exp != null) {
			exp.traceInformation.modificationStamp("ngl-support");

			if (!isDepot) {
				exp.inputContainerSupportCodes = new HashSet<String>() {{ add(newInputContUsedCode); }};
				exp.inputContainerCodes = new HashSet<String>() {{ add(newInputContUsedCode); }};

				exp.atomicTransfertMethods.get(0).inputContainerUseds.get(0).code = newInputContUsedCode;
				exp.atomicTransfertMethods.get(0).inputContainerUseds.get(0).locationOnContainerSupport.code = newInputContUsedCode;
			}
			
			exp.sampleCodes = new HashSet<String>() {{ add(sampleCode); }};
			exp.projectCodes = new HashSet<String>() {{ add(projectCode); }};

			if (updateContent && codeExpAvecBonnesValeurs != null) {
				Experiment expBonnesValeurs = MongoDBDAO.findByCode(InstanceConstants.EXPERIMENT_COLL_NAME, Experiment.class, codeExpAvecBonnesValeurs);

				if (expBonnesValeurs != null) {
					Logger.error("Mise à jour du contents de l'expérience '" + expCode + "'.");
				
					if (!isDepot) {
						exp.atomicTransfertMethods.get(0).inputContainerUseds.get(0).contents = expBonnesValeurs.atomicTransfertMethods.get(0).inputContainerUseds.get(0).contents;
						exp.atomicTransfertMethods.get(0).inputContainerUseds.get(0).volume = expBonnesValeurs.atomicTransfertMethods.get(0).inputContainerUseds.get(0).volume;
						exp.atomicTransfertMethods.get(0).inputContainerUseds.get(0).concentration = expBonnesValeurs.atomicTransfertMethods.get(0).inputContainerUseds.get(0).concentration;
						exp.atomicTransfertMethods.get(0).inputContainerUseds.get(0).quantity = expBonnesValeurs.atomicTransfertMethods.get(0).inputContainerUseds.get(0).quantity;
						exp.atomicTransfertMethods.get(0).inputContainerUseds.get(0).projectCodes = expBonnesValeurs.atomicTransfertMethods.get(0).inputContainerUseds.get(0).projectCodes;
						exp.atomicTransfertMethods.get(0).inputContainerUseds.get(0).sampleCodes = expBonnesValeurs.atomicTransfertMethods.get(0).inputContainerUseds.get(0).sampleCodes;
						exp.atomicTransfertMethods.get(0).inputContainerUseds.get(0).state = expBonnesValeurs.atomicTransfertMethods.get(0).inputContainerUseds.get(0).state;

						exp.atomicTransfertMethods.get(0).outputContainerUseds.get(0).contents = expBonnesValeurs.atomicTransfertMethods.get(0).outputContainerUseds.get(0).contents;
					} else {
						exp.atomicTransfertMethods.get(0).inputContainerUseds.get(0).contents = expBonnesValeurs.atomicTransfertMethods.get(0).outputContainerUseds.get(0).contents;
					}
				} else {
					Logger.error("Expérience '" + codeExpAvecBonnesValeurs + "' non trouvée.");
				}
			}		

			if (UPDATE_MONGO) {
				MongoDBDAO.update(InstanceConstants.EXPERIMENT_COLL_NAME, exp);
			}
		} else {
			Logger.error("Expérience non trouvée.");
		}
	}
}