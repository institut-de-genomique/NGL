package controllers.migration.cns;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import models.laboratory.common.instance.PropertyValue;
import models.laboratory.common.instance.property.PropertySingleValue;
import models.laboratory.container.instance.Container;
import models.laboratory.container.instance.Content;
import models.laboratory.experiment.instance.AtomicTransfertMethod;
import models.laboratory.experiment.instance.Experiment;
import models.laboratory.experiment.instance.InputContainerUsed;
import models.laboratory.experiment.instance.OutputContainerUsed;
import models.laboratory.processes.instance.Process;
import models.utils.InstanceConstants;

import org.mongojack.DBQuery;
import org.mongojack.DBUpdate;

import play.Logger;
import play.Logger.ALogger;
import play.mvc.Result;
import controllers.CommonController;
import fr.cea.ig.MongoDBDAO;

public class MigrationErrorPool extends CommonController{

			protected static ALogger logger=Logger.of("MigrationErrorPool");

			public static String oldSample="BZA_AABZ";
			public static String oldContainer="221D4N4IK_C3";
			public static String newSample="BZA_AACB";
			public static String newContainer="221D4N4IK_E3";
			public static String poolError="226E2A03Z";
			public static String oldPath=",21V913TBI,21VF36W9N,21VH393AX_C3,22191C6P8_C3,221B2HHB2_C3";
			public static String newPath=",21V913TBP,21VF36W79,21VH393AX_E3,22191C6P8_E3,221B2HHB2_E3";

			public static Result migration() {

				updateExperiments();
				updateContainers();
				updateProcess();
				return ok("Migration Error Pool");
			}

			private static void updateExperiments() {
				String[] exp = new String[] { 
						"POOL-20170206_141500CJG",
						"SIZING-20170206_143123EAJ",
						"FLUO-QUANTIFICATION-20170206_143258IHC",
						"CHIP-MIGRATION-20170206_143428BIH",
						"QPCR-QUANTIFICATION-20170214_111605FJI",
						"SOLUTION-STOCK-20170214_171546DGJ",
						"PREPA-FLOWCELL-20170217_113115BDC",
						"ILLUMINA-DEPOT-20170217_114051HIE"
				};
				
				List<Experiment> experiments=MongoDBDAO.find(InstanceConstants.EXPERIMENT_COLL_NAME, Experiment.class,DBQuery.in("code", Arrays.asList(exp))).toList();

				for(Experiment e:experiments){
					e.sampleCodes.remove(oldSample);
					e.sampleCodes.add(newSample);
					if(e.inputContainerCodes.contains(oldContainer)){
						e.inputContainerCodes.remove(oldContainer);
						e.inputContainerCodes.add(newContainer);
					}
					e.atomicTransfertMethods.forEach((AtomicTransfertMethod atm)-> updateAtomic(atm));
					
					MongoDBDAO.save(InstanceConstants.EXPERIMENT_COLL_NAME, e);
				}
				
			}

			private static void updateAtomic(AtomicTransfertMethod atm) {
				if(atm.outputContainerUseds!=null){
							atm.outputContainerUseds.forEach((OutputContainerUsed out)->{
							
								out.contents.stream().filter(c-> c.sampleCode.equals(oldSample))
								.forEach(c->{ c.sampleCode=newSample;
								updateContent(c);
								});
							
							});
				}
					
				atm.inputContainerUseds.forEach((InputContainerUsed in)->{
					
					if(in.code.equals(oldContainer)){
						in.code=newContainer;
					}
					
					in.contents.stream().filter(c->c.sampleCode.equals(oldSample))
					.forEach(c->{
						c.sampleCode=newSample;
						updateContent(c);
					});
					
				});;
			}

			private static void updateContent(Content c) {
				c.sampleCode=newSample;
				c.referenceCollab="H2O dnase rnase free (ZR duet)";
				c.taxonCode="32644";
				updateProperties(c.properties);
			}


			
			private static void updateProperties(Map<String, PropertyValue> properties) {
						replaceKey(properties,"fromSampleCode","BYU_AAAP");
						replaceKey(properties,"targetedRegion", "16S_V4V5");
						replaceKey(properties,"expectedAmpliconSize","400");
						replaceKey(properties,"amplificationPrimers","Fuhrman primer");
						replaceKey(properties,"tag" ,"12BA128");
						replaceKey(properties,"sampleAliquoteCode","21V913TBP");
						replaceKey(properties,"libLayoutNominalLength",429); 
		                replaceKey(properties,"insertSize",429);
		                
		        }
			private static void replaceKey(Map<String, PropertyValue> properties, String key, Object newValue) {
				if(properties.containsKey(key)){
					PropertySingleValue property=(PropertySingleValue) properties.get(key);
					property.value=newValue;
				}
			
			}

			private static void updateContainers() {
				List<Container> containers=MongoDBDAO.find(InstanceConstants.CONTAINER_COLL_NAME,Container.class,DBQuery.regex("code", Pattern.compile(poolError))).toList();
				for(Container c:containers){
					
					c.sampleCodes.remove(oldSample);
					c.sampleCodes.add(newSample);
					
					c.treeOfLife.from.containers.stream().filter(p->p.code.equals(oldContainer)).forEach(p->{
						p.code=newContainer;
					});
					
					c.treeOfLife.paths.stream().filter(s->s.contains(oldPath)).forEach(s->{
						s.replace(oldPath, newPath);
					});
					
					c.contents.stream().filter(content->content.sampleCode.equals(oldSample)).forEach(content->{
						updateContent(content);
					});
					
					MongoDBDAO.save(InstanceConstants.CONTAINER_COLL_NAME, c);
					MongoDBDAO.update(InstanceConstants.CONTAINER_COLL_NAME, Container.class,DBQuery.is("code",c.support.code),DBUpdate.push("sampleCodes",newSample).pull("samplesCodes", oldSample));
				}
			}
			

			private static void updateProcess() {
				
				Process process=MongoDBDAO.findByCode(InstanceConstants.PROCESS_COLL_NAME, Process.class,"BZA_AABZ_SIZING-STK-ILLUMINA-DEPOT_226E3BY5N");
				process.sampleCodes.remove(oldSample);
				process.sampleCodes.add(newSample);
				
				process.sampleOnInputContainer.sampleCode=newSample;
				updateProperties(process.sampleOnInputContainer.properties);
				MongoDBDAO.save(InstanceConstants.PROCESS_COLL_NAME,process);
			}

}
