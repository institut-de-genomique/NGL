package scripts;

import java.util.List;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.regex.Pattern;

import org.mongojack.DBQuery;
import org.mongojack.DBQuery.Query;

import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.lfw.controllers.scripts.buffered.Script;

import models.utils.InstanceConstants;
import models.laboratory.common.instance.property.PropertySingleValue;
import models.laboratory.container.instance.Container;
import models.laboratory.processes.instance.Process;
import models.laboratory.run.instance.ReadSet;

import play.Logger;

/**
 * NGL-2957 12/06/2020 
 * ATTENTION ce script ne prend pas en compte les Tags ni les tags secondaires!!!
 * 
 * Script permettant d'ajouter une nouvelle propriété de process de niveau content et de la propager aux containers et aux readSets
 * 
 * paramètres
 *    -processTypeCode : code du type de process auquel ajouter la propriété
 *    -newPropertyKey  : nom de la propriéte à ajouter
 *    -newPropertyValue: valeur de la propriéte (il s'agit d'une propriété de type "single value")
 *     NE PEUVENT PAS ETRE OPTIONNELS ....
 *    -processFilter: propriété de la collection Process sur laquelle filtrer
 *    -processFilterRegex: expression regulière pour le filtrage
 * 
 * USAGE:
 * curl -X POST -H 'User-Agent: bot' \
 * -i "http://localhost:9001/scripts/run/scripts.ScriptAddAndPropagateProcessusProperty?processTypeCode=xxx&
 *                                                                                      processFilter=zzz&
 *                                                                                      processFilterRegex=yyy&
 *                                                                                      newPropertyKey=PROP&
 *                                                                                      newPropertyValue=VALUE"<br>
 *                                                                                      
 * si pas besoin de filtrage additionnel mettre:  &processFilter=code&processFilterRegex=.
 * (sont obligatoires !!!)
 *
 * Algorithme inspiré de MigrationProcessusProperties.java
 * 
 * @author fernando
*/

public class ScriptAddAndPropagateProcessusProperty extends Script<ScriptAddAndPropagateProcessusProperty.Args> {

	/// la class Script n'accepte que des arguments obligatoires !!!!!
	public static class Args {
		public String processTypeCode; 
		public String newPropertyKey;
		public String newPropertyValue;
		public String processFilter; 
		public String processFilterRegex;
	}

	@Override
	public void execute(Args args) throws Exception {
		
		/* inutile car deja testé par Class Script ???
		if(args.processTypeCode == null || args.newPropertyKey == null || args.newPropertyValue == null) {
			throw new InvalidParameterException("Les paramètres 'processTypeCode', 'newPropertyKey' et 'newPropertyValue' sont tous obligatoires.");
		}
		*/
		
		Logger.debug("\nprocessTypeCode="+ args.processTypeCode +"\nnewPropertyKey="+ args.newPropertyKey +"\nnewPropertyValue="+ args.newPropertyValue); // dans la fenêtre de SBT 
		
		/* paramètres optionnels  NON!!!!
		if (args.processFilter != null ) {
			if (args.processFilterValue == null || args.processFilterRegex == null) {
				throw new InvalidParameterException("Quand processFilter est fourni il faut aussi fournir soit processFilterValue soit processFilterRegex.");
			}
			if (args.processFilterValue != null && args.processFilterRegex != null) {
				throw new InvalidParameterException("processFilterValue et processFilterRegex ne doivent pas être fournis en même temps.");
			}
		}
		
		if ((args.processFilter == null ) && (args.processFilterValue != null || args.processFilterRegex != null)) {
			throw new InvalidParameterException("processFilterValue ou processFilterRegex trouvé sans processFilter.");
		}
		*/
		Logger.debug("\nprocessFilter="+ args.processFilter +"\nprocessFilterRegex="+ args.processFilterRegex );
		
		// trouver les process a traiter
		// filtrage initial de Adrien pourquoi le .exists ???; en plus erreur: pas outputContainerSupportCodes   mais  outputContainerCodes !!!!
		//                                              ==> pour zapper ceux qui sont en cours ????? et qui n'ont pas encore leurs containers en sortie ???
		Query query = DBQuery.is("typeCode", args.processTypeCode).exists("outputContainerCodes"); 
		
		// ajout filtrage avec regex
		query = DBQuery.and(DBQuery.is("typeCode", args.processTypeCode),DBQuery.regex(args.processFilter, Pattern.compile(args.processFilterRegex)));

		List<Process> processes = MongoDBDAO.find(InstanceConstants.PROCESS_COLL_NAME, Process.class, query).toList();
		Logger.debug(processes.size()+" processes found");
		
		//Check same sample Code in list content container child      ??????
		Logger.debug("Checking Child Container unique sampleCode...please wait");  // verifier qu'on a un seul sample code par container  ???????

		
		//// erreur List<String> errorMessagesSample = processes.stream().map(process->process.outputContainerSupportCodes).flatMap(container->container.stream()).filter(containerCode->{
		List<String> errorMessagesSample = processes.stream().map(process->process.outputContainerCodes).flatMap(container->container.stream()).filter(containerCode->{
			//Get container 
			Set<String> containersError = new HashSet<String>();
			Container container = MongoDBDAO.findByCode(InstanceConstants.CONTAINER_COLL_NAME, Container.class, containerCode);
			if(container!=null){
				// ca fait quoi ce bordel ?????
				Set<String> counts = container.contents.stream().collect(Collectors.collectingAndThen(Collectors.groupingBy(c->c.sampleCode, Collectors.counting()), map->{map.values().removeIf(l -> l==1); return map.keySet();}));
				if(counts.size()>0)
						containersError.add(container.code); // on a trouvé plus que 1 seul sample code par container  ???????
			}
			return containersError.size()>0;
		}).collect(Collectors.toList());

		String error=null;

		if(errorMessagesSample.size()>0){
			error=" Child Container with multiple same sample ";
			for(String errorMessage : errorMessagesSample){
					error+=errorMessage+" ";
			}
		}

		if(error!=null)
			throw new Exception("errors "+ error);
			//return(error);                refusé
			//return badRequest(error);    quel include ??????
		
		// Pour tous les process à traiter
		for(Process p : processes){
			Logger.debug("\n--- Processing process "+p.code + "---");
	
			//  0 - ajouter d'abord des properties si le process n'en a pas
			if(null==p.properties) {
				Logger.debug("  0- ajout hashmap properties au process");
				p.properties=new HashMap<>();
			}
				
			//verifier si la propriété n'existe pas déja...
			if(null==p.properties.get(args.newPropertyKey)){
				// -1- ajouter la propriété au process 
				Logger.debug("  1- >> ajout property "+args.newPropertyKey+ "="+args.newPropertyValue+ " au process");
				p.properties.put(args.newPropertyKey, new PropertySingleValue(args.newPropertyValue));
				
				// -2- boucler sur les containers en sortie du process 
				for(String containerCode : p.outputContainerCodes){
					Logger.debug("  2- traitement container :"+containerCode);
					
					// -2.1- Mettre a jour les contents du container courant
					Container container = MongoDBDAO.findByCode(InstanceConstants.CONTAINER_COLL_NAME, Container.class, containerCode);
					if(container!=null){
						Logger.debug("  2.1- container trouvé");
						
						// filtrage de Adrien: sampleCode et projectCode du content doivent correspondre a ceux du process ???? garder ???
						container.contents.stream().filter(c-> p.sampleCodes.contains(c.sampleCode) && p.projectCodes.contains(c.projectCode) ).forEach(c->{
							// un content a toujours des properties ???
							//si la propriété n'existe pas déja alors ajouter, rien sinon
							if(null==c.properties.get(args.newPropertyKey)){
								Logger.debug("    >> ajout property "+args.newPropertyKey+ "="+args.newPropertyValue+ "dans le content du container :"+containerCode);
								c.properties.put(args.newPropertyKey, new PropertySingleValue(args.newPropertyValue));
							} else {
								Logger.debug("    >> le content du  container :"+containerCode+ " a déjà une property "+args.newPropertyKey +" !!!");	
							}
						});
						// SAUVEGARDE container
						MongoDBDAO.update(InstanceConstants.CONTAINER_COLL_NAME, container);
					} else {
					    // peut etre null ?????
						Logger.debug("  2.1- CONTAINER PAS TROUVE DANS LA COLLECTION MONGO !!");
					}
					
					// -2.2 trouver les readSets des samples des containers en sortie du process
					List<ReadSet> readSets = MongoDBDAO.find(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, DBQuery.is("sampleOnContainer.containerCode", containerCode)).toList();
					if(readSets!=null){
						int sizeAllReadSets = readSets.size();
						Logger.debug("  2.2- nbre de readSets trouvés (avant filtrage) ="+sizeAllReadSets);
						
						// filtrage de Adrien: sampleCode et projectCode du readSet doivent correspondre a ceux du process ???? garder ???
						readSets.stream().filter(readset-> p.sampleCodes.contains(readset.sampleCode) && p.projectCodes.contains(readset.projectCode) ).forEach(r->{
							
							//un readSet a toujours des properties ???
							//si la propriété n'existe pas déja alors ajouter, rien sinon
							if(null==r.properties.get(args.newPropertyKey)){
								Logger.debug("    >> ajout property "+args.newPropertyKey+ "="+args.newPropertyValue+ "dans readSet :"+r.code);
								r.properties.put(args.newPropertyKey, new PropertySingleValue(args.newPropertyValue));
							} else {
								Logger.debug("    >> readSet :"+r.code+ " a déjà une property "+args.newPropertyKey +" !!!");
							}
						});
						
						int sizeUpdateReadSets = readSets.size();
						
						/// inconsistance possible ??
						if(sizeAllReadSets!=sizeUpdateReadSets){
							Logger.warn("    2.2- nbre de readSet apres filtrage ="+sizeUpdateReadSets);
							Logger.warn("         => Check ReadSet for container "+containerCode);
						}
						
						// SAUVEGARDE reasdset
						for(ReadSet readSet : readSets){
							MongoDBDAO.update(InstanceConstants.READSET_ILLUMINA_COLL_NAME, readSet);
						}
					} else { // n'arrive pas car le toList rend une liste vide ( donc c'est pas null!!!!)
						Logger.debug("    READSET NOT FOUND IN COLLECTION !!");
					}
				}
			
			} else {
				Logger.debug("  1- property "+args.newPropertyKey +" existe déjà !!!");
			}
			
			// SAUVEGARDE process
			MongoDBDAO.update(InstanceConstants.PROCESS_COLL_NAME, p);
		}
		Logger.debug(".............................Tous les process sont traités........................................");
	}
}
