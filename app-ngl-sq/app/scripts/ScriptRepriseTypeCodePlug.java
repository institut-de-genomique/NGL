package scripts;

import java.util.Date;
import java.util.List;

import org.mongojack.DBQuery;

import models.laboratory.container.instance.Container;
import models.laboratory.experiment.instance.Experiment;
import models.laboratory.processes.instance.Process;
import models.laboratory.run.instance.ReadSet;
import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.lfw.controllers.scripts.chunked.ScriptWithArgs;
import models.laboratory.sample.instance.Sample;
import models.utils.InstanceConstants;
import play.Logger;
import validation.ContextValidation;

public class ScriptRepriseTypeCodePlug extends ScriptWithArgs<ScriptRepriseTypeCodePlug.Args> {

	public static class Args {

    }

	@Override
	public void execute(ScriptRepriseTypeCodePlug.Args args) throws Exception {
		Logger.error("Début 'ScriptRepriseTypeCodePlug'");

        repriseExperienceSansCodeCommentaire();

        updateOldSampleRessourcesContents();

        updateTypeCodeToPlug();

        deletePropertyPlugFromSamples();

        getDNAPlugSamples();

        updateCategoryCodeToDNA();

        Logger.error("Fin 'ScriptRepriseTypeCodePlug'");
	}

    private void repriseExperienceSansCodeCommentaire() {
        DBQuery.Query query = DBQuery.is("comments.code", null);
        List<Experiment> expList = MongoDBDAO.find(InstanceConstants.EXPERIMENT_COLL_NAME, Experiment.class, query).toList();
		
        Logger.error("Liste des expériences avec un code commentaire à null :");

        for (Experiment exp : expList) {
            Logger.error(exp.code);

            for (int i = 0; i < exp.comments.size(); i++) {
                if (exp.comments.get(i).code == null) {
                    exp.comments.get(i).code = "NGL-3656_" + new Date().getTime();
                }
            }
            

            MongoDBDAO.update(InstanceConstants.EXPERIMENT_COLL_NAME, exp);
        }
    }

    private void updateOldSampleRessourcesContents() {
        // Process avec 'sampleOnInputContainer.sampleCategoryCode' à 'DNAplug'.
        // On met la categoryCode à 'DNA'.

        Logger.error("Liste des process avec 'sampleOnInputContainer.sampleCategoryCode' à 'DNAplug'.");

        DBQuery.Query query = DBQuery.is("sampleOnInputContainer.sampleCategoryCode", "DNAplug");
        List<Process> processList = MongoDBDAO.find(InstanceConstants.PROCESS_COLL_NAME, Process.class, query).toList();
		
        for (Process process : processList) {
            Logger.error(process.code);

            process.sampleOnInputContainer.sampleCategoryCode = "DNA";

            MongoDBDAO.update(InstanceConstants.PROCESS_COLL_NAME, process);
        }

        // Experiment avec : 
        // 'atomicTransfertMethods.inputContainerUseds.contents.sampleCategoryCode' à 'DNAplug'.
        // 'atomicTransfertMethods.outputContainerUseds.contents.sampleCategoryCode' à 'DNAplug'.
        // On met la categoryCode à 'DNA'.

        Logger.error("expériences des readset avec 'atomicTransfertMethods.inputContainerUseds.contents.sampleCategoryCode' ou 'atomicTransfertMethods.outputContainerUseds.contents.sampleCategoryCode' à 'DNAplug'.");

        query = DBQuery.or(
            DBQuery.is("atomicTransfertMethods.inputContainerUseds.contents.sampleCategoryCode", "DNAplug"),
            DBQuery.is("atomicTransfertMethods.outputContainerUseds.contents.sampleCategoryCode", "DNAplug")
        );

        List<Experiment> expList = MongoDBDAO.find(InstanceConstants.EXPERIMENT_COLL_NAME, Experiment.class, query).toList();
		
        for (Experiment exp : expList) {
            Logger.error(exp.code);

            for (int i = 0; i < exp.atomicTransfertMethods.size(); i++) {
                for (int j = 0; j < exp.atomicTransfertMethods.get(i).inputContainerUseds.size(); j++) {
                    for (int k = 0; k < exp.atomicTransfertMethods.get(i).inputContainerUseds.get(j).contents.size(); k++) {
                        if (exp.atomicTransfertMethods.get(i).inputContainerUseds.get(j).contents.get(k).sampleCategoryCode.equals("DNAplug")) {
                            exp.atomicTransfertMethods.get(i).inputContainerUseds.get(j).contents.get(k).sampleCategoryCode = "DNA";

                            Logger.error("Mise à jour de l'atm avec 'icu': " + exp.atomicTransfertMethods.get(i).inputContainerUseds.get(j).code);
                        
                            MongoDBDAO.update(InstanceConstants.EXPERIMENT_COLL_NAME, exp);
                        }
                    }
                }

                if (exp.atomicTransfertMethods.get(i).outputContainerUseds != null) {
                    for (int j = 0; j < exp.atomicTransfertMethods.get(i).outputContainerUseds.size(); j++) {
                        for (int k = 0; k < exp.atomicTransfertMethods.get(i).outputContainerUseds.get(j).contents.size(); k++) {
                            if (exp.atomicTransfertMethods.get(i).outputContainerUseds.get(j).contents.get(k).sampleCategoryCode.equals("DNAplug")) {
                                exp.atomicTransfertMethods.get(i).outputContainerUseds.get(j).contents.get(k).sampleCategoryCode = "DNA";

                                Logger.error("Mise à jour de l'atm avec 'ocu': " + exp.atomicTransfertMethods.get(i).outputContainerUseds.get(j).code);

                                MongoDBDAO.update(InstanceConstants.EXPERIMENT_COLL_NAME, exp);
                            }
                        }
                    }
                }
            }
        }

        // Readset avec 'sampleOnContainer.sampleCategoryCode' à 'DNAplug'.
        // On met la categoryCode à 'DNA'.

        Logger.error("Liste des readset avec 'sampleOnContainer.sampleCategoryCode' à 'DNAplug'.");

        /* query = DBQuery.is("sampleOnContainer.sampleCategoryCode", "DNAplug");
        List<ReadSet> rsList = MongoDBDAO.find(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, query).toList();
		
        for (ReadSet readSet : rsList) {
            Logger.error(readSet.code);

            readSet.sampleOnContainer.sampleCategoryCode = "DNA";

            MongoDBDAO.update(InstanceConstants.READSET_ILLUMINA_COLL_NAME, readSet);
        } */

        // Container avec 'contents.sampleCategoryCode' à 'DNAplug'.
        // On met la categoryCode à 'DNA'.

        Logger.error("Liste des containers avec 'contents.sampleCategoryCode' à 'DNAplug'.");

        query = DBQuery.is("contents.sampleCategoryCode", "DNAplug");
        List<Container> contList = MongoDBDAO.find(InstanceConstants.CONTAINER_COLL_NAME, Container.class, query).toList();
		
        for (Container container : contList) {
            Logger.error(container.code);

            for (int i = 0; i < container.contents.size(); i++) {
                if (container.contents.get(i).sampleCategoryCode.equals("DNAplug")) {
                    container.contents.get(i).sampleCategoryCode = "DNA";

                    Logger.error("Mise à jour du content avec comme 'sampleCode': " + container.contents.get(i).sampleCode);
                }
            }

            MongoDBDAO.update(InstanceConstants.CONTAINER_COLL_NAME, container);
        }
    }

    private void updateSampleRessourcesContents(Sample sample) {
        // Process avec 'sampleOnInputContainer.code' à 'sample.code'.
        // On met la typeCode à 'DNAplug'.

        Logger.error("Liste des process avec 'sampleOnInputContainer.sampleCode' à '" + sample.code + "'.");

        DBQuery.Query query = DBQuery.is("sampleOnInputContainer.sampleCode", sample.code);
        List<Process> processList = MongoDBDAO.find(InstanceConstants.PROCESS_COLL_NAME, Process.class, query).toList();
		
        for (Process process : processList) {
            Logger.error(process.code);

            process.sampleOnInputContainer.sampleTypeCode = "DNAplug";

            MongoDBDAO.update(InstanceConstants.PROCESS_COLL_NAME, process);
        }


        // Experiment avec : 
        // 'atomicTransfertMethods.inputContainerUseds.contents.code' à 'sample.code'.
        // 'atomicTransfertMethods.outputContainerUseds.contents.sampleCategoryCode' à 'sample.code'.
        // On met la typeCode à 'DNAplug'.

        Logger.error("Liste des expériences avec 'atomicTransfertMethods.inputContainerUseds.contents.sampleCode' ou 'atomicTransfertMethods.outputContainerUseds.contents.sampleCode' à '" + sample.code + "'.");

        query = DBQuery.or(
            DBQuery.is("atomicTransfertMethods.inputContainerUseds.contents.sampleCode", sample.code),
            DBQuery.is("atomicTransfertMethods.outputContainerUseds.contents.sampleCode", sample.code)
        );

        List<Experiment> expList = MongoDBDAO.find(InstanceConstants.EXPERIMENT_COLL_NAME, Experiment.class, query).toList();
		
        for (Experiment exp : expList) {
            Logger.error(exp.code);

            for (int i = 0; i < exp.atomicTransfertMethods.size(); i++) {
                for (int j = 0; j < exp.atomicTransfertMethods.get(i).inputContainerUseds.size(); j++) {
                    for (int k = 0; k < exp.atomicTransfertMethods.get(i).inputContainerUseds.get(j).contents.size(); k++) {
                        if (exp.atomicTransfertMethods.get(i).inputContainerUseds.get(j).contents.get(k).sampleCode.equals(sample.code)) {
                            exp.atomicTransfertMethods.get(i).inputContainerUseds.get(j).contents.get(k).sampleTypeCode = "DNAplug";

                            Logger.error("Mise à jour de l'atm avec 'icu': " + exp.atomicTransfertMethods.get(i).inputContainerUseds.get(j).code);
                        
                            MongoDBDAO.update(InstanceConstants.EXPERIMENT_COLL_NAME, exp);
                        }
                    }
                }


                if (exp.atomicTransfertMethods.get(i).outputContainerUseds != null) {
                    for (int j = 0; j < exp.atomicTransfertMethods.get(i).outputContainerUseds.size(); j++) {
                        for (int k = 0; k < exp.atomicTransfertMethods.get(i).outputContainerUseds.get(j).contents.size(); k++) {
                            if (exp.atomicTransfertMethods.get(i).outputContainerUseds.get(j).contents.get(k).sampleCategoryCode.equals(sample.code)) {
                                exp.atomicTransfertMethods.get(i).outputContainerUseds.get(j).contents.get(k).sampleTypeCode = "DNAplug";

                                Logger.error("Mise à jour de l'atm avec 'ocu': " + exp.atomicTransfertMethods.get(i).outputContainerUseds.get(j).code);
                            
                                MongoDBDAO.update(InstanceConstants.EXPERIMENT_COLL_NAME, exp);
                            }
                        }
                    }
                }
            }
        }

        // Readset avec 'sampleOnContainer.code' à 'sample.code'.
        // On met la typeCode à 'DNAplug'.

        Logger.error("Liste des readset avec 'sampleOnContainer.sampleCode' à '" + sample.code + "'.");

        query = DBQuery.is("sampleOnContainer.sampleCode", sample.code);
        List<ReadSet> rsList = MongoDBDAO.find(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, query).toList();
		
        for (ReadSet readSet : rsList) {
            Logger.error(readSet.code);

            readSet.sampleOnContainer.sampleTypeCode = "DNAplug";

            MongoDBDAO.update(InstanceConstants.READSET_ILLUMINA_COLL_NAME, readSet);
        }


        // Container avec 'contents.code' à 'sample.code'.
        // On met la typeCode à 'DNAplug'.

        Logger.error("Liste des containers avec 'contents.sampleCode' à '" + sample.code + "'.");

        query = DBQuery.is("contents.sampleCode", sample.code);
        List<Container> contList = MongoDBDAO.find(InstanceConstants.CONTAINER_COLL_NAME, Container.class, query).toList();
		
        for (Container container : contList) {
            Logger.error(container.code);

            for (int i = 0; i < container.contents.size(); i++) {
                if (container.contents.get(i).sampleCode.equals(sample.code)) {
                    container.contents.get(i).sampleTypeCode = "DNAplug";

                    Logger.error("Mise à jour du content avec comme 'sampleCode': " + container.contents.get(i).sampleCode);
                }
            }

            MongoDBDAO.update(InstanceConstants.CONTAINER_COLL_NAME, container);
        }
    }

    private void updateTypeCodeToPlug() {
        ContextValidation ctxVal = ContextValidation.createUpdateContext("ngl-support");

        DBQuery.Query query = DBQuery.is("properties.dnaTreatment.value", "plug");
        List<Sample> samplesList = MongoDBDAO.find(InstanceConstants.SAMPLE_COLL_NAME, Sample.class, query).toList();
		
        for (Sample sample : samplesList) {
            Logger.error("Modification du sample : '" + sample.code + "'.");

            if (sample.life == null) {
                sample.typeCode = "DNAplug";

                updateSampleRessourcesContents(sample);

                Logger.error("Modification du 'typeCode' en  : 'DNAplug'.");

                if (!sample.categoryCode.equals("DNA")) {
                    Logger.error("'categoryCode' n'est pas 'DNA' ('" + sample.categoryCode + "') pour le sample '" + sample.code + "'.");
                }

                Logger.error(sample.code + " : NGL-3656 - Nouvelle gestion des plugs");

                sample.traceInformation.modifyDate = new Date();
                sample.traceInformation.modifyUser = "ngl-support";

                Logger.error("Modification de la 'modifyDate' et du 'modifyUser'.");
            } else {
                if (!sample.typeCode.equals("DNA")) {
                    ctxVal.addError("typeCode", "'typeCode n'est pas 'DNA' pour l'enfant '" + sample.code + "'.");
                }

                Sample sampleParent = MongoDBDAO.findByCode(InstanceConstants.SAMPLE_COLL_NAME, Sample.class, sample.life.from.sampleCode);

                if (!sampleParent.properties.containsKey("dnaTreatment") || !sampleParent.properties.get("dnaTreatment").value.equals("plug")) {
                    Logger.error("'typeCode' du sample parent : " + sampleParent.typeCode);
                    Logger.error("Modification du 'typeCode' '" + sample.typeCode + "' en  : 'DNAplug'.");

                    sample.typeCode = "DNAplug"; 

                    Logger.error(sample.code + " : NGL-3656 - Nouvelle gestion des plugs");
                    
                    sample.traceInformation.modifyDate = new Date();
                    sample.traceInformation.modifyUser = "ngl-support";

                    updateSampleRessourcesContents(sample);
    
                    Logger.error("Modification de la 'modifyDate' et du 'modifyUser'.");
                }
            }

            MongoDBDAO.update(InstanceConstants.SAMPLE_COLL_NAME, sample);
		}

        if (ctxVal.hasErrors()) {
            Logger.error("Erreurs de validation : " + ctxVal.getErrors());
        }
    }

    private void deletePropertyPlugFromSamples() {
        DBQuery.Query query = DBQuery.is("properties.dnaTreatment.value", "plug");
        List<Sample> samplesList = MongoDBDAO.find(InstanceConstants.SAMPLE_COLL_NAME, Sample.class, query).toList();
		
        for (Sample sample : samplesList) {
            sample.properties.remove("dnaTreatment");

            Logger.error("Suppression de la propriété 'dnaTreatment' pour le sample '" + sample.code + "'.");

            MongoDBDAO.update(InstanceConstants.SAMPLE_COLL_NAME, sample);
        }
    }

    private void updateCategoryCodeToDNA() {
        DBQuery.Query query = DBQuery.is("categoryCode", "DNAplug");
        List<Sample> samplesList = MongoDBDAO.find(InstanceConstants.SAMPLE_COLL_NAME, Sample.class, query).toList();
		
        for (Sample sample : samplesList) {
            Logger.error("'categoryCode' n'est pas 'DNA' ('" + sample.categoryCode + "') pour le sample '" + sample.code + "'.");

            sample.categoryCode = "DNA";

            Logger.error("Changement du champs 'categoryCode' pour la valeur 'DNA'.");

            MongoDBDAO.update(InstanceConstants.SAMPLE_COLL_NAME, sample);
        }
    }

    private void getDNAPlugSamples() {
        // On ne veut pas récupérer les anciens samples déjà en plug. On cherche donc ceux qu'on vient de modifier (typeCode => DNAplug).
        // Ceux qui ont la categoryCode à DNAplug sont les "anciens", on mettra donc à jour la categoryCode dans la dernière étape du script.
        DBQuery.Query queryFinal = DBQuery.is("typeCode", "DNAplug").notEquals("categoryCode", "DNAplug");
        List<Sample> samplesListFinal = MongoDBDAO.find(InstanceConstants.SAMPLE_COLL_NAME, Sample.class, queryFinal).toList();
		
        Logger.error("Liste des samples pour utilisation du script 'UpdateBatchContentProperties' :");

        for (Sample sample : samplesListFinal) {
            Logger.error(sample.code);
        }
    }
}