package scripts;

import java.io.File;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Month;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.mongojack.DBQuery;
import org.mongojack.DBQuery.Query;

import de.flapdoodle.embed.process.io.file.Files;
import fr.cea.ig.DBObject;
import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.MongoDBResult.Sort;
import fr.cea.ig.lfw.controllers.scripts.chunked.ScriptWithExcelBody;
import models.laboratory.common.instance.TransientState;
import models.laboratory.container.instance.Container;
import models.laboratory.experiment.description.ExperimentType;
import models.laboratory.experiment.instance.Experiment;
import models.laboratory.processes.instance.Process;
import models.utils.InstanceConstants;
import play.Logger;

/**
 * Script de reprise des process CNG avec des trous. Ces trous viennent d'un
 * backup trop ancien (23 Juin 2021) alors qu'il a été restauré le 28 Juin 2021.
 * Il y a donc plusieurs jours d'activités à reprendre. Ce script prend en
 * entrée un fichier Excel avec 2 colonnes : CODE_PROCESS | CODE_EXPERIENCE où
 * CODE_EXPERIENCE doit être ajouté au process de la première colonne.
 * 
 * @author jcharpen - Jordi CHARPENTIER - jcharpen@genoscope.cns.fr
 * @author aprotat - Alexandre PROTAT - aprotat@genoscope.cns.fr
 */
public class ScriptRepriseProcessDropJuin2021 extends ScriptWithExcelBody {

	/**
	 * Début de la coupure (i.e, du dernier backup disponible) -> 23 Juin 2021 à
	 * 18h55.
	 */
	private static final Date START_FAILURE = Date.from(LocalDate.of(2021, Month.JUNE, 23)
			.atStartOfDay(ZoneId.systemDefault()).toInstant().plus(16, ChronoUnit.HOURS).plus(55, ChronoUnit.MINUTES));

	/**
	 * Fin de la coupure (i.e, quand le dernier backup a été restauré) -> 28 Juin
	 * 2021 à 15h00.
	 */
	private static final Date END_FAILURE = Date.from(LocalDate.of(2021, Month.JUNE, 28)
			.atStartOfDay(ZoneId.systemDefault()).toInstant().plus(13, ChronoUnit.HOURS));

	/**
	 * L'utilisateur qu'on va utiliser pour tracer les mises à jour des process.
	 */
	private static final String USER_SUPPORT = "ngl-support";

	/**
	 * Nom de la collection de test où sont envoyés les process modifiés par le
	 * script.
	 */
	private static final String PROCESS_COLL_NAME_TEST = "testJordiReprise";

	private static final String QC = "qualitycontrol";

	private static final String ONE_TO_VOID = "OneToVoid";

	private static final String IN_PROGRESS_STATE = "IP";

	private static final String TERMINATED_STATE = "F";

	private static final String NEW_STATE = "N";

	private static final String IN_WAITING_DISPATCH_STATE = "IW-D";

	private static final Set<String> TERMINATING_STATES = Collections
			.unmodifiableSet(Sets.newHashSet("IS", "UA", "IW-P"));

	/**
	 * Map qu'on va remplir au fur et à mesure de l'exécution du script pour avoir
	 * les logs d'exécution.
	 */
	private static Map<String, List<String>> LOG_HISTORY = new HashMap<>();

	/**
	 * Booléen qui permet de savoir si on veut à la fin du script un fichier avec
	 * tous les logs générés par le script.
	 */
	private static boolean HAS_LOG_FILE = true;

	/**
	 * Booléen permettant de savoir si on doit écrire dans la collection de test ou
	 * non.
	 * 
	 * @see PROCESS_COLL_NAME_TEST pour le nom de la collection de test.
	 */
	private static boolean IS_DEBUG_COLLECTION = false;

	/**
	 * Méthode permettant de récupérer la date de création d'une expérience à partir
	 * d'un objet expérience.
	 * 
	 * @param experiment L'expérience où on veut récupérer la date de création.
	 * @return La date de création de l'expérience.
	 */
	private Date getCreationDate(Experiment experiment) {
		return experiment.traceInformation.creationDate;
	}

	/**
	 * Méthode permettant de créer un historique de log pour l'exécution du script.
	 * 
	 * @param processCode Le code du process où on veut ajouter un log.
	 * @param phrase      La phrase de log à ajouter pour le process donné.
	 */
	private void updateLogHistory(String processCode, String phrase) {
		if (!LOG_HISTORY.containsKey(processCode)) {
			LOG_HISTORY.put(processCode, Lists.newArrayList(phrase));
		} else {
			LOG_HISTORY.get(processCode).add(phrase);
		}
	}

	/**
	 * Méthode permettant de mettre à jour le champ "experimentCodes" d'un process
	 * avec une liste d'expériences.
	 * 
	 * @param process    Le process à mettre à jour.
	 * @param experience L'expérience à ajouter à la liste des expériences du
	 *                   process.
	 */
	private void setProcessExperimentCodes(Process process, Experiment experience) {
		process.experimentCodes.add(experience.code);
	}

	/**
	 * Méthode permettant de savoir si une date donnée est pendant la coupure qu'on
	 * veut corriger.
	 * 
	 * @param date La date à comparer.
	 * @return true si la date est pendant la coupure. false sinon.
	 */
	private boolean isDuringFailure(Date date) {
		return date.after(START_FAILURE) && date.before(END_FAILURE);
	}

	/**
	 * Méthode permettant de savoir si une expérience a été terminée durant la
	 * coupure.
	 * 
	 * @param experiment L'expérience à vérifier.
	 * @param return     true si l'expérience a été terminée pendant la coupure.
	 *                   false sinon.
	 */
	private boolean isTerminatedDuringFailure(Experiment experiment) {
		boolean isTerminated = experiment.state.code.equals(TERMINATED_STATE);

		return isTerminated && isDuringFailure(experiment.state.date);
	}

	/**
	 * Méthode permettant de savoir si une expérience est de type one to void.
	 * 
	 * @param exp L'expérience à regarder.
	 * @return true si l'expérience donnée est de type one to void. false sinon.
	 */
	private boolean isOneToVoid(Experiment exp) {
		ExperimentType expType = ExperimentType.find.get().findByCode(exp.typeCode);
		return expType.atomicTransfertMethod.equals(ONE_TO_VOID);
	}

	/**
	 * Méthode permettant de trier une liste d'expériences par date de création.
	 * 
	 * @param expList La liste d'expérience à trier.
	 */
	private void sortExperimentsListByDate(List<Experiment> expList) {
		expList.sort((expA, expB) -> getCreationDate(expA).compareTo(getCreationDate(expB)));
	}

	/**
	 * Méthode permettant de comparer deux dates en ignorant les millisecondes.
	 * 
	 * @param dateA La première date à comparer.
	 * @param dateB La seconde date à comparer.
	 * @return true si les deux dates sont identiques à la seconde près. false sinon.
	 */
	private boolean isSameDate(Date dateA, Date dateB) {
		Instant dateAInst = dateA.toInstant().truncatedTo(ChronoUnit.SECONDS);
		Instant dateBInst = dateB.toInstant().truncatedTo(ChronoUnit.SECONDS);

		long timeDiff = Math.abs(dateAInst.getEpochSecond() - dateBInst.getEpochSecond());

		return dateAInst.equals(dateBInst) || timeDiff <= 1;
	}

	/**
	 * Méthode permettant de chercher si un container dans un process est passé par
	 * un enchainement de state. 
	 * Si oui, on compare à la date d'expérience donnée en entrée.
	 * Si les deux sont égales, on doit terminer ce process (i.e, passer le state à "F").
	 * 
	 * @param process           Le process qu'on va peut-être terminer.
	 * @param container         Le container où on va vérifier son historique de
	 *                          state.
	 * @param previousStateCode Le state précédent que le container doit avoir.
	 * @param afterStates       Une liste de states vers lequel le container doit
	 *                          aller depuis le "previousStateCode".
	 * @param endDateExp		Date de fin d'expérience utilisée pour trouver le bon enchainement.		
	 * @return 					Un objet "TransientState" représentant la clôture de process si cet enchainement est trouvé
	 * 							et qu'il correspond à la date donnée en entrée. null sinon.
	 */
	private TransientState searchStateChainingInHistory(Process process, Container container, String previousStateCode,
			Set<String> afterStates, Date endDateExp) {
		List<TransientState> historical = container.state.historical.stream().collect(Collectors.toList());
		sortHistoricalListByIndex(historical);

		TransientState previousState = null;

		for (TransientState ts : historical) {
			if (previousState != null && previousState.code.equals(previousStateCode)) {				
				if (isSameDate(previousState.date, endDateExp)) {
					if (afterStates.contains(ts.code) && isDuringFailure(ts.date)) {
						return ts;
					}
				}
			}

			previousState = new TransientState();
			previousState.date = ts.date;
			previousState.code = ts.code;
			previousState.index = ts.index;
		}

		return null;
	}

	/**
	 * Méthode permettant pour un process donné, de vérifier si on doit le terminer
	 * (i.e, passer son state à "F"), et si oui, le terminer.
	 * 
	 * @param process Le process où on veut regarder si on doit le terminer (ou
	 *                non).
	 */
	private void handleTerminateProcess(Process process) {
		Stream<Experiment> experimentsStream = mongoStream(InstanceConstants.EXPERIMENT_COLL_NAME, Experiment.class,
				DBQuery.in("code", process.experimentCodes));

		experimentsStream
			.filter(exp -> TERMINATED_STATE.equals(exp.state.code))
			.forEach(exp -> {
				if (isOneToVoid(exp)) {
					searchTerminatingContainersAfterState(process, exp.inputContainerCodes, IN_WAITING_DISPATCH_STATE, exp);
				} else {
					searchTerminatingContainersAfterState(process, exp.outputContainerCodes, NEW_STATE, exp);
				}
			});
	}

	/**
	 * Méthode permettant de faire un find Mongo dans une collection et de retourner
	 * un stream résultat. Similaire à "mongoStream(String collectionName, Class<T>
	 * entityClass, DBQuery.Query query)". Ici, la query est vide car cette méthode
	 * est principalement utilisée pour faire un findAll().
	 * 
	 * @param <T>            Le type de retour que le stream aura. Doit être
	 *                       identique au paramètre "collectionName".
	 * @param collectionName La collection où on veut exécuter notre requête.
	 * @param entityClass    L'objet mappant la collection où on cherche.
	 * @return Un stream résultat du type donné.
	 */
	private <T extends DBObject> Stream<T> mongoStream(String collectionName, Class<T> entityClass) {
		return mongoStream(collectionName, entityClass, DBQuery.empty());
	}

	/**
	 * Méthode permettant de faire un find Mongo dans une collection et de retourner
	 * un stream résultat.
	 * 
	 * @param <T>            Le type de retour que le stream aura. Doit être
	 *                       identique au paramètre "collectionName".
	 * @param collectionName La collection où on veut exécuter notre requête.
	 * @param entityClass    L'objet mappant la collection où on cherche.
	 * @param query          La requête qu'on veut faire dans la collection.
	 * @return Un stream résultat du type donné.
	 */
	private <T extends DBObject> Stream<T> mongoStream(String collectionName, Class<T> entityClass,
			DBQuery.Query query) {
		return StreamSupport.stream(MongoDBDAO.find(collectionName, entityClass, query).cursor.spliterator(), false);
	}

	/**
	 * Méthode permettant à partir d'une liste de containers de rechercher si au
	 * moins un container est "terminant" pour le process (i.e, il a un enchainement
	 * d'état spécifique dans son historique d'état). Si c'est le cas, la méthode va
	 * terminer le process avec l'aide de la méthode updateProcessState().
	 * 
	 * @param process       Le process qu'on va terminer si au moins un container a
	 *                      l'enchainement d'état qu'on cherche.
	 * @param contCodes     Les containers qu'on va regarder.
	 * @param previousState L'état précédent que les containers devront avoir.
	 * @param exp
	 */
	private void searchTerminatingContainersAfterState(Process process, Set<String> contCodes, String previousState, Experiment exp) {
		Stream<Container> containersStream = mongoStream(InstanceConstants.CONTAINER_COLL_NAME, Container.class,
				DBQuery.in("code", contCodes));

		Optional<TransientState> tsEndProcess = containersStream
				.filter(cont -> process.outputContainerCodes.contains(cont.code))
				.map(cont -> searchStateChainingInHistory(process, cont, previousState, TERMINATING_STATES, exp.state.date))
				.filter((TransientState ts) -> ts != null)
				.findFirst();

		if (tsEndProcess.isPresent()) {
			TransientState ts = tsEndProcess.get();

			updateProcessState(process, TERMINATED_STATE, ts.date, ts.user);
		}
	}

	/**
	 * Méthode permettant de trier une liste d'historique de state par le champ
	 * "index".
	 * 
	 * @param historical La liste de state à trier.
	 */
	private void sortHistoricalListByIndex(List<TransientState> historical) {
		historical.sort((h1, h2) -> h1.index.compareTo(h2.index));
	}

	/**
	 * 
	 * 
	 * @param p
	 * @param endDate
	 * @param endUser
	 */
	private void handleManualProcessTermination(Process p, Date endDate, String endUser) {
		if (!p.state.code.equals(TERMINATED_STATE)) {
			updateProcessState(p, TERMINATED_STATE, endDate, endUser);

			if (IS_DEBUG_COLLECTION) {
				MongoDBDAO.save(PROCESS_COLL_NAME_TEST, p);
			} else {
				MongoDBDAO.save(InstanceConstants.PROCESS_COLL_NAME, p);
			}
		}
	}

	/**
	 * Méthode permettant de chercher les process qui ont été terminés manuellement.
	 * On regarde les containers qui ont eu un changement d'état manuel A-XX -> IS,
	 * UA, IW-P.
	 */
	private void manualContainerStateChange() {
		Query queryContainer = DBQuery.and(DBQuery.greaterThan("state.historical.date", START_FAILURE),
				DBQuery.lessThan("state.historical.date", END_FAILURE), DBQuery.in("state.code", TERMINATING_STATES));

		mongoStream(InstanceConstants.CONTAINER_COLL_NAME, Container.class, queryContainer).forEach(c -> {
			List<TransientState> historical = c.state.historical.stream().collect(Collectors.toList());
			sortHistoricalListByIndex(historical);

			TransientState previousState = null;

			for (TransientState ts : historical) {
				if (previousState != null && previousState.code.startsWith("A-")) {
					if (TERMINATING_STATES.contains(ts.code) && isDuringFailure(ts.date)) {
						final Date endDate = ts.date;
						final String endUser = ts.user;

						Stream<Process> stProc = mongoStream(InstanceConstants.PROCESS_COLL_NAME, Process.class, DBQuery.in("outputContainerCodes", c.code));
						long nbProcess = stProc.count();

						if (nbProcess == 1L) {
							mongoStream(InstanceConstants.PROCESS_COLL_NAME, Process.class, DBQuery.in("outputContainerCodes", c.code)).forEach(p -> {
								handleManualProcessTermination(p, endDate, endUser);
							});
						} else if (nbProcess > 1) {
							Logger.error("Plusieurs outputContainers trouvés dans la collection 'Process'.");
						} else {
							Stream<Process> stProcInput = mongoStream(InstanceConstants.PROCESS_COLL_NAME, Process.class, DBQuery.is("inputContainerCode", c.code));
							long nbProcessInput = stProcInput.count();

							if (nbProcessInput == 1L) {
								mongoStream(InstanceConstants.PROCESS_COLL_NAME, Process.class, DBQuery.is("inputContainerCode", c.code)).forEach(p -> {
									handleManualProcessTermination(p, endDate, endUser);
								});
							} else {
								List<Process> procList = mongoStream(InstanceConstants.PROCESS_COLL_NAME, Process.class, DBQuery.is("inputContainerCode", c.code))
									.filter(p -> {
										return p.traceInformation.creationDate.equals(endDate);
									})
									.collect(Collectors.toList());

								if (procList.size() == 1) {
									handleManualProcessTermination(procList.get(0), endDate, endUser);
								} else {
									Logger.error("Plusieurs inputContainerCode trouvés dans la collection 'Process'.");
								}
							}
						}
					}
				}

				previousState = ts;
			}
		});
	}

	/**
	 * Méthode permettant de vérifier si une expérience a été créée pendant la
	 * coupure.
	 * 
	 * @param experiment L'expérience à regarder.
	 * @return true si l'expérience donnée en argument a été créée pendant la
	 *         coupure. false sinon.
	 */
	private boolean isExperimentCreatedDuringFailure(Experiment experiment) {
		return isDuringFailure(getCreationDate(experiment));
	}

	/**
	 * Méthode permettant de retourner la première expérience d'un process.
	 * On se base ici sur la date de création dans le "traceInformation" des objets.
	 * 
	 * @param process Le process où on veut récupérer la première expérience.
	 * @return Un objet "Experiment" représentant la première expérience du process.
	 */
	private Experiment getFirstExperimentOfProcess(Process process) {
		List<Experiment> expList = MongoDBDAO.find(InstanceConstants.EXPERIMENT_COLL_NAME, Experiment.class,
				DBQuery.in("code", process.experimentCodes)).toList();
		sortExperimentsListByDate(expList);

		Experiment firstExp = expList.get(0);

		return firstExp;
	}

	/**
	 * Méthode permettant de vérifier si une expérience a été créée pendant le trou.
	 * 
	 * @param exp L'expérience qu'on va vérifier.
	 * @return true si la première expérience du process a été créée pendant le
	 *         trou. false sinon.
	 */
	private boolean isExperimentStartedDuringFailure(Experiment exp) {
		Optional<TransientState> inProgressState = exp.state.historical.stream()
				.filter(h -> h.code.equals(IN_PROGRESS_STATE)).findFirst();

		return inProgressState.isPresent() ? isDuringFailure(inProgressState.get().date) : false;
	}

	/**
	 * Méthode permettant de mettre à jour le champ "currentExperimentTypeCode" d'un
	 * process.
	 * 
	 * @param process Le process à mettre à jour.
	 */
	private void updateCurrentExperimentTypeCode(Process process) {
		List<Experiment> lastExperienceList = MongoDBDAO
				.find(InstanceConstants.EXPERIMENT_COLL_NAME, Experiment.class,
						DBQuery.in("code", process.experimentCodes))
				.sort("traceInformation.creationDate", Sort.DESC).limit(1).toList();

		if (!lastExperienceList.isEmpty()) {
			process.currentExperimentTypeCode = lastExperienceList.get(0).typeCode;

			updateLogHistory(process.code,
					"Mise à jour du 'currentExperimentTypeCode' à '" + process.currentExperimentTypeCode + "'.");
		}
	}

	/**
	 * Méthode permettant de savoir si une expérience est un QC ou non.
	 * 
	 * @param experiment L'expérience à regarder.
	 * @return true si l'expérience est une QC. false sinon.
	 */
	private boolean isQC(Experiment experiment) {
		return experiment.categoryCode.equals(QC);
	}

	/**
	 * Méthode permettant de vérifier si l'inputContainerCode d'un process se
	 * retrouve dans l'abre de vie d'un container.
	 * 
	 * @param cont    Le container à vérifier.
	 * @param process Le process à vérifier.
	 * @return true si l'inputContainerCode du process se retrouve bien dans l'abre
	 *         de vie du container en entrée.
	 */
	private boolean isContainerInTreeOfLife(Container cont, Process process) {
		return cont.treeOfLife.paths.stream().anyMatch(p -> p.endsWith("," + process.inputContainerCode)
				|| p.contains("," + process.inputContainerCode + ","));
	}

	/**
	 * Méthode permettant de mettre à jour les champs "outputContainerCodes" et
	 * "outputContainerSupportCodes" d'un process.
	 * 
	 * @param process    Le process à mettre à jour.
	 * @param experience L'expérience utilisée pour mettre à jour le process.
	 */
	private void updateOutputContainerCodesAndOutputContainerSupportCodes(Process process, Experiment experience) {
		if (!isQC(experience) && isTerminatedDuringFailure(experience) && CollectionUtils.isNotEmpty(process.experimentCodes)) {
			if (CollectionUtils.isNotEmpty(experience.outputContainerCodes)) {
				mongoStream(InstanceConstants.CONTAINER_COLL_NAME, Container.class, DBQuery.in("code", experience.outputContainerCodes))
					.filter(cont -> isContainerInTreeOfLife(cont, process))
					.forEach(c -> {
						if (CollectionUtils.isEmpty(process.outputContainerCodes)) {
							process.outputContainerCodes = new HashSet<>();
						}

						process.outputContainerCodes.add(c.code);

						if (CollectionUtils.isEmpty(process.outputContainerSupportCodes)) {
							process.outputContainerSupportCodes = new HashSet<>();
						}

						process.outputContainerSupportCodes.add(c.support.code);
					});
			}
		}
	}

	/**
	 * Méthode permettant de mettre à jour le state d'un process donné en argument.
	 * On utilise pour le moment cette méthode qu'avec le state "IP" et "F".
	 * 
	 * @param process   Le process à mettre à jour.
	 * @param stateCode Le code du state qu'on veut mettre dans le process.
	 * @param date La date à mettre dans le state du process.
	 * @param user L'utilisateur à mettre dans le state du process.
	 */
	private void updateProcessState(Process process, String stateCode, Date date, String user) {
		process.state.code = stateCode;
		process.state.date = date;
		process.state.user = user;

		updateLogHistory(process.code, "Mise à jour du state à '" + stateCode + "'.");

		TransientState ts = new TransientState();
		ts.code = stateCode;
		ts.date = date;
		ts.user = user;
		ts.index = process.state.historical.stream().map(h -> h.index).max(Comparator.naturalOrder()).get() + 1;

		process.state.historical.add(ts);

		updateLogHistory(process.code, "Ajout dans l'historique du state '" + stateCode + "'.");
	}

	/**
	 * Méthode permettant de mettre à jour le traceInformation du process donné en
	 * argument avec comme modifyUser "ngl-support" et comme date la date courante
	 * d'exécution du script.
	 * 
	 * @param process Le process à mettre à jour.
	 */
	private void updateTraceInformationProcess(Process process) {
		process.traceInformation.modifyDate = new Date();

		updateLogHistory(process.code, "Mise à jour du champ 'traceInformation.modifyDate' avec '"
				+ process.traceInformation.modifyDate + "'.");

		process.traceInformation.modifyUser = USER_SUPPORT;

		updateLogHistory(process.code,
				"Mise à jour du champ 'traceInformation.modifyUser' avec '" + USER_SUPPORT + "'.");
	}

	/**
	 * Méthode permettant de mettre à jour un process à partir d'une expérience. On
	 * met à jour (si nécessaire) : 
	 * - Le champ "experimentCodes" du process.
	 * - Le champ "outputContainerCodes" du process.
	 * - Le champ "outputContainerSupportCodes" du process.
	 * 
	 * @param process    Le process à mettre à jour.
	 * @param experience L'expérience utilisée pour la mise à jour du process.
	 */
	private void updateProcessFromExperiment(Process process, Experiment experience) {
		if (isExperimentCreatedDuringFailure(experience)) {
			setProcessExperimentCodes(process, experience);
		}

		updateOutputContainerCodesAndOutputContainerSupportCodes(process, experience);
	}

	/**
	 * Méthode permettant de mettre à jour le process. On met à jour (si nécessaire) : 
	 * - Le champ "currentExperimentCode".
	 * - Le champ "traceInformation". 
	 * - Le state.
	 * 
	 * @param process Le process à mettre à jour.
	 */
	private void updateProcess(Process process) {
		updateCurrentExperimentTypeCode(process);

		updateTraceInformationProcess(process);

		Experiment firstExp = getFirstExperimentOfProcess(process);

		if (isExperimentStartedDuringFailure(firstExp) && !process.state.code.equals(IN_PROGRESS_STATE)) {
			Optional<TransientState> optTS = firstExp.state.historical.stream().filter(h -> h.code.equals(IN_PROGRESS_STATE)).findFirst();
			
			if (optTS.isPresent()) {
				Date date = optTS.get().date;
				String user = optTS.get().user;

				updateProcessState(process, IN_PROGRESS_STATE, date, user);
			}
		}

		if (process.state.code.equals(IN_PROGRESS_STATE)) {
			handleTerminateProcess(process);
		}
	}

	/**
	 * Méthode permettant de mettre à jour le log du script avec une liste de codes
	 * (expérience, container, support).
	 * 
	 * @param fieldName   Le champ qu'on ajoute au log.
	 * @param codeList    La liste de code à ajouter au log du script.
	 * @param processCode Le code du process dans lequel cette mise à jour se fait.
	 */
	private void logListContent(String fieldName, Collection<String> codeList, String processCode) {
		StringBuilder log = new StringBuilder();

		log.append("Le champ '").append(fieldName).append("' contient : ").append(String.join(", ", codeList));

		updateLogHistory(processCode, log.toString());
	}

	/**
	 * Méthode "générale" du script : c'est à partir d'ici que l'algorithme de
	 * reprise est lancé, à partir du fichier Excel lu.
	 * 
	 * @param mapProcessExpList Une map de process et d'expériences représentant les
	 *                          process et expériences à reprendre.
	 */
	private void repriseProcessFromExperiencesCreeesApresTrou(Map<Process, List<Experiment>> mapProcessExpList) {
		Logger.error("Début repriseProcessFromExperiencesCreeesApresTrou()");

		for (Map.Entry<Process, List<Experiment>> processExpEntry : mapProcessExpList.entrySet()) {
			Process process = processExpEntry.getKey();
			List<Experiment> expList = processExpEntry.getValue();

			expList.forEach(exp -> updateProcessFromExperiment(process, exp));

			logListContent("experimentCodes", process.experimentCodes, process.code);

			updateProcess(process);

			if (IS_DEBUG_COLLECTION) {
				MongoDBDAO.save(PROCESS_COLL_NAME_TEST, process);
			} else {
				MongoDBDAO.save(InstanceConstants.PROCESS_COLL_NAME, process);
			}

			logListContent("outputContainerSupportCodes", process.outputContainerSupportCodes, process.code);

			logListContent("outputContainerCodes", process.outputContainerCodes, process.code);

			updateLogHistory(process.code, "Le champ 'state.code' vaut : " + process.state.code);
		}

		manualContainerStateChange();
		
		Logger.error("Fin repriseProcessFromExperiencesCreeesApresTrou()");
	}

	/**
	 * Méthode permettant de vider la collection de test. Utilisée avant chaque
	 * exécution du script.
	 */
	private void deleteCollectionTest() {
		if (IS_DEBUG_COLLECTION) {
			MongoDBDAO.delete(PROCESS_COLL_NAME_TEST, Process.class, DBQuery.in("code", mongoStream(PROCESS_COLL_NAME_TEST, Process.class).map(p -> p.code).collect(Collectors.toList())));
		}
	}

	/**
	 * Méthode permettant de lire un fichier Excel et de le transformer en objets
	 * Java qu'on manipule ensuite pour la reprise.
	 * 
	 * @param workbook Le fichier Excel lu.
	 * @return Une map avec comme clé les codes process à mettre à jour et en valeur
	 *         la liste des expériences à ajouter au process en clé.
	 */
	private Map<Process, List<Experiment>> getProcessAndExperimentsFromFile(XSSFWorkbook workbook) {
		Map<String, List<String>> processExpListString = new HashMap<>();

		Set<String> allExpCodes = new HashSet<>();

		workbook.getSheetAt(0).rowIterator().forEachRemaining(row -> {
			String processCode = row.getCell(0).getStringCellValue();
			String expCode = row.getCell(1).getStringCellValue();

			if (processCode != null && expCode != null) {
				processCode = processCode.trim();
				expCode = expCode.trim();
				allExpCodes.add(expCode);

				if (processExpListString.containsKey(processCode)) {
					processExpListString.get(processCode).add(expCode);
				} else {
					List<String> expCodeList = new ArrayList<>();
					expCodeList.add(expCode);

					processExpListString.put(processCode, expCodeList);
				}
			}
		});

		return getMapFromProcessAndExperimentsCodes(allExpCodes, processExpListString);
	}

	/**
	 * 
	 * 
	 * @param allExpCodes
	 * @param processExpListString
	 * @return
	 */
	private Map<Process, List<Experiment>> getMapFromProcessAndExperimentsCodes(Set<String> allExpCodes, Map<String, List<String>> processExpListString) {
		Map<Process, List<Experiment>> processExpList = new HashMap<>();
		Map<String, Experiment> mapCodesExperiments = getExperimentsMapFromCodes(allExpCodes);

		getProcessesFromCodes(processExpListString.keySet()).forEach(p -> {
			List<String> expCodesList = processExpListString.get(p.code);
			List<Experiment> experimentsList = expCodesList.stream().map(mapCodesExperiments::get).collect(Collectors.toList());
			processExpList.put(p, experimentsList);
		});

		return processExpList;
	}

	/**
	 * 
	 * 
	 * @param codes
	 * @return
	 */
	private Map<String, Experiment> getExperimentsMapFromCodes(Collection<String> codes) {
		return getExperimentsFromCodes(codes).collect(Collectors.toMap((exp -> exp.code), (exp -> exp)));
	}

	/**
	 * 
	 * 
	 * @param codes
	 * @return
	 */
	private Stream<Process> getProcessesFromCodes(Collection<String> codes) {
		return mongoStream(InstanceConstants.PROCESS_COLL_NAME, Process.class, DBQuery.in("code", codes));
	}

	/**
	 * 
	 * 
	 * @param codes
	 * @return
	 */
	private Stream<Experiment> getExperimentsFromCodes(Collection<String> codes) {
		return mongoStream(InstanceConstants.EXPERIMENT_COLL_NAME, Experiment.class, DBQuery.in("code", codes));
	}

	@Override
	public void execute(XSSFWorkbook workbook) throws Exception {
		Logger.error("Début reprise globale");

		deleteCollectionTest();

		Map<Process, List<Experiment>> mapProcessExpFromFile = getProcessAndExperimentsFromFile(workbook);
		repriseProcessFromExperiencesCreeesApresTrou(mapProcessExpFromFile);

		if (HAS_LOG_FILE) {
			Files.write(LOG_HISTORY.toString(), new File("logs_script-reprise_process.txt"));
		}

		Logger.error("Fin reprise globale");
	}
}