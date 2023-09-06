package fr.cea.ig.ngl.dao.api.factory;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

import org.apache.commons.lang.RandomStringUtils;

import fr.cea.ig.ngl.TestUtils;
import models.laboratory.common.instance.PropertyValue;
import models.laboratory.common.instance.State;
import models.laboratory.common.instance.property.PropertySingleValue;
import models.laboratory.container.instance.LocationOnContainerSupport;
import models.laboratory.experiment.instance.AtomicTransfertMethod;
import models.laboratory.experiment.instance.Experiment;
import models.laboratory.experiment.instance.InputContainerUsed;
import models.laboratory.experiment.instance.OneToOneContainer;
import models.laboratory.experiment.instance.OneToVoidContainer;
import models.laboratory.experiment.instance.OutputContainerUsed;
import models.laboratory.instrument.instance.InstrumentUsed;

/**
 * Factory pour l'entité "Experiment".
 * 
 * @author jcharpen - Jordi Charpentier - jcharpen@genoscope.cns.fr
 */
public final class ExperimentFactory {
	
	/**
	 * Méthode permettant de générer une expérience de type "Nanopore" aléatoire.
	 * 
	 * @return Un objet "Experiment" représentant cette expérience "Nanopore".
	 */
	public static Experiment getRandomExperimentNanopore() {
		Experiment e = new Experiment();
		e.typeCode = "nanopore-depot";
		e.code = "NANOPORE-DEPOT-20200818_190409CBG";

		Set<String> setString = new HashSet<>();
		setString.add("0AQA4IHNB");
		e.inputContainerSupportCodes = setString;

		InstrumentUsed instrument = new InstrumentUsed();
		instrument.typeCode="minION";
		instrument.code="MN02670";
		e.instrument = instrument;
		
		Map<String, PropertyValue> mapExpProp = new HashMap<String, PropertyValue>();
		
		try {
			mapExpProp.put("runStartDate", new PropertySingleValue(TestUtils.SDF.parse("21/12/2012")));
		} catch (ParseException e1) {
			mapExpProp.put("runStartDate", new PropertySingleValue(new Date()));
		}
		
		e.experimentProperties = mapExpProp;
		
		Map<String, PropertyValue> instruProp = new HashMap<String, PropertyValue>();
		PropertyValue propValueInstru = new PropertySingleValue("R7.3");
		instruProp.put("flowcellChemistry", propValueInstru);
		e.instrumentProperties = instruProp;
		
		OutputContainerUsed ocu = new OutputContainerUsed();
		ocu.code = "BHG_B1";
		ocu.categoryCode = UUID.randomUUID().toString();

		InputContainerUsed icu = new InputContainerUsed();
		icu.code = "BHG_B1";
		icu.categoryCode = UUID.randomUUID().toString();
		
		List<OutputContainerUsed> ocuList = new ArrayList<OutputContainerUsed>();
		ocuList.add(ocu);

		List<InputContainerUsed> icuList = new ArrayList<InputContainerUsed>();
		icuList.add(icu);
		
		OneToOneContainer atm = new OneToOneContainer();
		atm.outputContainerUseds = ocuList;
		atm.inputContainerUseds = icuList;
		
		List<AtomicTransfertMethod> atmList = new ArrayList<AtomicTransfertMethod>();
		atmList.add(atm);
		e.atomicTransfertMethods = atmList;
		
		State state = new State();
		state.code="F";
		
		e.state = state;
		
		return e;
	}

	/**
	 * Méthode permettant de générer une liste d'expériences Bionano aléatoires.
	 * 
	 * @return Une liste d'expériences Bionano aléatoire.
	 */
	public static List<Experiment> getRandomExperimentBionanoList() {
		List<Experiment> res = new ArrayList<>();

		for (int i = 0; i < TestUtils.LIST_SIZE; i++) {
			res.add(getRandomExperimentBionano());
		}

		return res;
	}
	
	/**
	 * Méthode permettant de générer une expérience de type "Bionano" aléatoire.
	 * 
	 * @return Un objet "Experiment" représentant cette expérience "Bionano".
	 */
	public static Experiment getRandomExperimentBionano() {
		Experiment e = new Experiment();
		e.typeCode = "bionano-depot";
		e.code = "BIONANO-DEPOT-20191219_140843DBI";
		
		InstrumentUsed instrument = new InstrumentUsed();
		instrument.typeCode="SAPHYR";
		instrument.code="ILIADE";
		e.instrument = instrument;
		
		Map<String, PropertyValue> mapExpProp = new HashMap<String, PropertyValue>();
		mapExpProp.put("chipIteration", new PropertySingleValue("A"));
		
		try {
			mapExpProp.put("runStartDate", new PropertySingleValue(TestUtils.SDF.parse("21/12/2019")));
		} catch (ParseException e1) {
			mapExpProp.put("runStartDate", new PropertySingleValue(new Date()));
		}
		
		e.experimentProperties = mapExpProp;
		
		State state = new State();
		state.code="F";
		
		Set<String> setICSC = new HashSet<String>();
		setICSC.add("XPJY-2U6L-PQLG-RNWU");
		e.inputContainerSupportCodes = setICSC;
		
		Set<String> setICC = new HashSet<String>();
		setICC.add("XPJY-2U6L-PQLG-RNWU_1");
		setICC.add("XPJY-2U6L-PQLG-RNWU_2");
		e.inputContainerCodes = setICC;
		
		e.state = state;

		LocationOnContainerSupport locs = new LocationOnContainerSupport();
		locs.code = "AAA111";
		locs.categoryCode = "AAA111";

		OutputContainerUsed ocu = new OutputContainerUsed();
		ocu.locationOnContainerSupport = locs;
		ocu.code = "FAA54955_A";
		
		List<OutputContainerUsed> ocuList = new ArrayList<OutputContainerUsed>();
		ocuList.add(ocu);

		InputContainerUsed icu = new InputContainerUsed();
		icu.locationOnContainerSupport = locs;
		icu.code = "FAA54955_A";
		
		List<InputContainerUsed> icuList = new ArrayList<InputContainerUsed>();
		icuList.add(icu);

		OneToOneContainer atm = new OneToOneContainer();
		atm.inputContainerUseds = icuList;
		atm.outputContainerUseds = ocuList;

		List<AtomicTransfertMethod> atmList = new ArrayList<AtomicTransfertMethod>();
		atmList.add(atm);
		e.atomicTransfertMethods = atmList;
		
		return e;
	}
	
	/**
	 * Méthode permettant de générer une expérience de type "Bionano" aléatoire.
	 * 
	 * @return Un objet "Experiment" représentant cette expérience "Bionano".
	 */
	public static Experiment getRandomExperimentMiseq() {
		Experiment e = new Experiment();
		e.typeCode = "miseq-qc";
		e.code = "MISEQ-QC-20210311_105315FIB";
		
		State state = new State();
		state.code="F";
		
		e.state = state;

		List<AtomicTransfertMethod> atmList = new ArrayList<AtomicTransfertMethod>();
		
		for(String character: new String[] {"A", "B", "C", "D", "E", "F", "G", "H"}) {
			for(int i=1; i <= 12; i++) {
				OneToVoidContainer atm = new OneToVoidContainer();
				atm.line = character;
				atm.column = String.valueOf(i);
				LocationOnContainerSupport locs = new LocationOnContainerSupport();
				locs.code = "A005RJV";
				locs.categoryCode = "96-well-plate";
				locs.line = character;
				locs.column = String.valueOf(i);
				InputContainerUsed icu = new InputContainerUsed();
				icu.code = "A005RJV_H9";
				icu.locationOnContainerSupport = locs;
				atm.inputContainerUseds = Arrays.asList(icu);
				atmList.add(atm);
			}
		}
		
		e.atomicTransfertMethods = atmList;
		
		return e;
	}

	/**
	 * Méthode permettant de générer un code expérience aléatoire en gardant le format "NANOPORE-DEPOT-ANNEE MOIS JOUR_CHAINE RANDOM 9 CARACTERES".
	 * "NANOPORE-DEPOT-" n'est pas aléatoire pour le test, mais possible de le faire évoluer.
	 * 
	 * @return Un code expérience aléatoire.
	 */
	public static String getRandomExperimentCode() {
		String expCode = "NANOPORE-DEPOT-" + Calendar.getInstance().get(Calendar.YEAR) + 
												 Calendar.getInstance().get(Calendar.MONTH) +
												 Calendar.getInstance().get(Calendar.DAY_OF_MONTH) +
												"_" + RandomStringUtils.random(9, true, true);

		return expCode;
	}

	/**
	 * Méthode permettant de générer une liste de code expériences aléatoires.
	 * 
	 * @return Une liste de chaîne de caractères correspondant à liste de code expériences.
	 */
	public static TreeSet<String> getRandomExperimentCodesList() {
		TreeSet<String> list = new TreeSet<>();

		for (int i = 0; i < TestUtils.LIST_SIZE; i++) {
			list.add(getRandomExperimentCode());
		}

		return list;
	}
}
