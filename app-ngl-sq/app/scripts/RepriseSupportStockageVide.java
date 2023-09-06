package scripts;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.mongojack.DBQuery;
import org.mongojack.DBQuery.Query;

import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.lfw.controllers.scripts.chunked.ScriptWithArgs;
import models.laboratory.container.instance.ContainerSupport;
import models.laboratory.container.instance.StorageHistory;
import models.laboratory.experiment.description.ExperimentCategory;
import models.laboratory.experiment.instance.Experiment;
import models.utils.InstanceConstants;

/**
 * Script de reprise des stockages vide, suite au NGL-3789.
 * 
 * @author jcharpen Jordi CHARPENTIER jcharpen@genoscope.cns.fr
 */
public class RepriseSupportStockageVide extends	ScriptWithArgs<RepriseSupportStockageVide.Args> {

	public static class Args {

	}

	@Override
	public void execute(Args args) throws Exception {
		logger.error("Début du script");

		// gestionExperienceQC();

		gestionExperienceNonQC();

		logger.error("Fin du script");
	}

	// NGL-3971
	private void gestionExperienceQC() {
		Query query = DBQuery.or(
									DBQuery.is("atomicTransfertMethods.inputContainerUseds.locationOnContainerSupport.storageCode", ""),
									DBQuery.is("atomicTransfertMethods.outputContainerUseds.locationOnContainerSupport.storageCode", "")
								)
							 .is("state.code", "F");

		List<Experiment> expList = 	MongoDBDAO.find(InstanceConstants.EXPERIMENT_COLL_NAME, Experiment.class, query).toList();

		expList.stream().forEach(exp -> {
			for (int j = 0; j < exp.atomicTransfertMethods.size(); j++) {
				if (exp.categoryCode.equals(ExperimentCategory.CODE.qualitycontrol.name()) && exp.atomicTransfertMethods.get(j).inputContainerUseds != null) {
					logger.error("Gestion des inputContainerUseds");

					for (int i = 0; i < exp.atomicTransfertMethods.get(j).inputContainerUseds.size(); i++) {
						if ((exp.atomicTransfertMethods.get(j).inputContainerUseds.get(i).locationOnContainerSupport != null) && (exp.atomicTransfertMethods.get(j).inputContainerUseds.get(i).locationOnContainerSupport.storageCode != null) && (exp.atomicTransfertMethods.get(j).inputContainerUseds.get(i).locationOnContainerSupport.storageCode.equals(""))) {
							exp.atomicTransfertMethods.get(j).inputContainerUseds.get(i).locationOnContainerSupport.storageCode = null;

							logger.error("Code expérience : " + exp.code);
							logger.error("exp.atomicTransfertMethods.get(j).inputContainerUseds.get(i).code : " + exp.atomicTransfertMethods.get(j).inputContainerUseds.get(i).code);
						}
					}
				} else if (!exp.categoryCode.equals(ExperimentCategory.CODE.qualitycontrol.name())) {
					if (exp.atomicTransfertMethods.get(j).outputContainerUseds != null) {
						logger.error("Gestion des outputContainerUseds");

						for (int i = 0; i < exp.atomicTransfertMethods.get(j).outputContainerUseds.size(); i++) {
							if ((exp.atomicTransfertMethods.get(j).outputContainerUseds.get(i).locationOnContainerSupport != null) && (exp.atomicTransfertMethods.get(j).outputContainerUseds.get(i).locationOnContainerSupport.storageCode != null) && (exp.atomicTransfertMethods.get(j).outputContainerUseds.get(i).locationOnContainerSupport.storageCode.equals(""))) {
								exp.atomicTransfertMethods.get(j).outputContainerUseds.get(i).locationOnContainerSupport.storageCode = null;

								logger.error("Code expérience : " + exp.code);
								logger.error("exp.atomicTransfertMethods.get(j).outputContainerUseds.get(i).code : " + exp.atomicTransfertMethods.get(j).outputContainerUseds.get(i).code);
							}
						}
					}

					if (exp.atomicTransfertMethods.get(j).inputContainerUseds != null) {
						logger.error("Gestion des inputContainerUseds");

						for (int i = 0; i < exp.atomicTransfertMethods.get(j).inputContainerUseds.size(); i++) {
							if ((exp.atomicTransfertMethods.get(j).inputContainerUseds.get(i).locationOnContainerSupport != null) && (exp.atomicTransfertMethods.get(j).inputContainerUseds.get(i).locationOnContainerSupport.storageCode != null) && (exp.atomicTransfertMethods.get(j).inputContainerUseds.get(i).locationOnContainerSupport.storageCode.equals(""))) {
								exp.atomicTransfertMethods.get(j).inputContainerUseds.get(i).locationOnContainerSupport.storageCode = null;

								logger.error("Code expérience : " + exp.code);
								logger.error("exp.atomicTransfertMethods.get(j).inputContainerUseds.get(i).code : " + exp.atomicTransfertMethods.get(j).inputContainerUseds.get(i).code);
							}
						}
					}
				} 
			};

			// MongoDBDAO.update(InstanceConstants.EXPERIMENT_COLL_NAME, exp);
		});
	}

	// NGL-3972
	private void gestionExperienceNonQC() {
		logger.error("Seuil;Code support;Code expérience;Date fin expérience;User fin expérience;Ancienne valeur storageCode;Nouvelle valeur storageCode;Ancien historique;Nouvel historique");

		Query query = DBQuery.is("storages.code", "");

		List<ContainerSupport> contSuppList = MongoDBDAO.find(InstanceConstants.CONTAINER_SUPPORT_COLL_NAME, ContainerSupport.class, query).toList();
		
		contSuppList.stream().forEach(containerSupport -> {
			if ((containerSupport.storages != null) && (!containerSupport.storages.isEmpty())) {
				List<StorageHistory> emptyStorageList = containerSupport.storages.stream().filter(storageHistory -> (storageHistory.code != null) && (storageHistory.code.equals(""))).collect(Collectors.toList());

				emptyStorageList.stream().forEach(storageHistory -> {
					Query queryExp = DBQuery.notEquals("categoryCode", "qualityControl")
											.regex("atomicTransfertMethods.outputContainerUseds.code", Pattern.compile(containerSupport.code));

					List<Experiment> expList = MongoDBDAO.find(InstanceConstants.EXPERIMENT_COLL_NAME, Experiment.class, queryExp).toList();
					
					expList.forEach(exp -> {
						SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
								
						try {
							String dateExp = simpleDateFormat.format(exp.state.date);
							Date dateExpObj = simpleDateFormat.parse(dateExp);
					
							String dateCont = simpleDateFormat.format(storageHistory.date);
							Date dateContObj = simpleDateFormat.parse(dateCont);
					
							long res = Math.abs(dateExpObj.getTime() - dateContObj.getTime());

							String ancienHistorique = StringUtils.join(containerSupport.storages.stream().map(st -> st.code).collect(Collectors.toList()), ",");
							String ancienStorageCode = containerSupport.storageCode;

							if ((res >= 0  && res <= 5000) && ((exp.state.user.equals(storageHistory.user)) || exp.state.user.equals("ngl-data"))) {		
								if (containerSupport.storageCode.equals("")) {
									if (containerSupport.storages.size() == 1) {
										containerSupport.storageCode = null;
									} else {
										containerSupport.storageCode = containerSupport.storages.get(containerSupport.storages.size() - 2).code;
									}

									containerSupport.storages.remove(containerSupport.storages.size() - 1);
								} else {
									containerSupport.storages = containerSupport.storages.stream().filter(stHist -> !(stHist.user.equals(storageHistory.user) && stHist.code.equals(""))).collect(Collectors.toList());
								}											

								String nouvelHistorique = StringUtils.join(containerSupport.storages.stream().map(st -> st.code).collect(Collectors.toList()), ",");

								logger.error(res + ";" + containerSupport.code + ";" + exp.code + ";" + exp.state.date + ";" + exp.state.user + ";" + ancienStorageCode + ";" + containerSupport.storageCode + ";" + ancienHistorique + ";" + nouvelHistorique);
																
								MongoDBDAO.update(InstanceConstants.CONTAINER_SUPPORT_COLL_NAME, containerSupport);
							} 
						} catch (ParseException e) {
							e.printStackTrace();
						}
					});
				});
			}		
		});
	}
}