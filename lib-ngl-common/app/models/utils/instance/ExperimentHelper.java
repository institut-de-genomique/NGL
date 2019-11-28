package models.utils.instance;

import static fr.cea.ig.play.IGGlobals.configuration;

import java.util.ArrayList;
import java.util.List;

import fr.cea.ig.lfw.utils.Iterables;
import models.laboratory.experiment.instance.Experiment;
import models.laboratory.experiment.instance.InputContainerUsed;
import models.laboratory.experiment.instance.ManyToOneContainer;
import rules.services.RulesServices6;

public class ExperimentHelper {

//	public static List<InputContainerUsed> getAllInputContainers(Experiment expFromDB) {
//		List<InputContainerUsed> containersUsed = new ArrayList<>();
//		if (expFromDB.atomicTransfertMethods != null) {
//			for (int i=0; i<expFromDB.atomicTransfertMethods.size(); i++) {
//				if (expFromDB.atomicTransfertMethods.get(i) != null && expFromDB.atomicTransfertMethods.get(i).inputContainerUseds.size() > 0) {
//					containersUsed.addAll(expFromDB.atomicTransfertMethods.get(i).inputContainerUseds);
//				}
//			}
//		}
//		return containersUsed;
//	}

	public static List<InputContainerUsed> getAllInputContainers(Experiment expFromDB) {
		return Iterables.zen    (expFromDB.atomicTransfertMethods)
				        .filter (a -> a.inputContainerUseds != null)
				        .flatMap(a -> a.inputContainerUseds)
				        .toList ();
	}
	
	public static void doCalculations(Experiment exp, String rulesName) {
		ArrayList<Object> facts = new ArrayList<>();
		facts.add(exp);
//		for (int i=0; i<exp.atomicTransfertMethods.size(); i++) {
//			AtomicTransfertMethod atomic = exp.atomicTransfertMethods.get(i);
//			facts.add(atomic);
//		}
		// The above loop looks like : facts.addAll(exp.atomicTransfertMethods);
		facts.addAll(exp.atomicTransfertMethods);
//		List<Object> factsAfterRules = RulesServices6.getInstance().callRulesWithGettingFacts(Play.application().configuration().getString("rules.key"), rulesName, facts);
		List<Object> factsAfterRules = RulesServices6.getInstance().callRulesWithGettingFacts(configuration().getString("rules.key"), rulesName, facts);
		for (Object obj : factsAfterRules) {
//			if (ManyToOneContainer.class.isInstance(obj)) {
			if (obj instanceof ManyToOneContainer) { // why not instance of AtomicTransfertMethod ?
//				exp.atomicTransfertMethods.remove((ManyToOneContainer)obj);
				exp.atomicTransfertMethods.remove(obj);
				exp.atomicTransfertMethods.add   ((ManyToOneContainer) obj);
			}
		}	
	}

}
