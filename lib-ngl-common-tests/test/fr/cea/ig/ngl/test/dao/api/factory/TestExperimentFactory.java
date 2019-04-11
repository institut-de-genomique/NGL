package fr.cea.ig.ngl.test.dao.api.factory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

import controllers.experiments.api.ExperimentSearchForm;
import fr.cea.ig.lfw.utils.Iterables;
import fr.cea.ig.ngl.support.ListFormWrapper;
import fr.cea.ig.play.test.DevAppTesting;
import models.laboratory.common.instance.PropertyValue;
import models.laboratory.common.instance.State;
import models.laboratory.common.instance.TBoolean;
import models.laboratory.common.instance.TransientState;
import models.laboratory.common.instance.Valuation;
import models.laboratory.common.instance.property.PropertySingleValue;
import models.laboratory.container.instance.Container;
import models.laboratory.experiment.instance.AtomicTransfertMethod;
import models.laboratory.experiment.instance.Experiment;
import models.laboratory.experiment.instance.InputContainerUsed;
import models.laboratory.experiment.instance.ManyToOneContainer;
import models.laboratory.experiment.instance.OneToOneContainer;
import models.laboratory.experiment.instance.OneToVoidContainer;
import models.laboratory.experiment.instance.OutputContainerUsed;
import models.laboratory.instrument.instance.InstrumentUsed;
import models.laboratory.processes.instance.Process;
import models.laboratory.reagent.instance.ReagentUsed;
import ngl.refactoring.state.ExperimentStateNames;

public class TestExperimentFactory {
    //private static final play.Logger.ALogger logger = play.Logger.of(TestExperimentFactory.class);

    public static Experiment experimentQC(String user, List<Container> containers) {
        return experimentQC(user, "fluo-quantification", "annexe-dosage-qubit-v2", containers.get(0).projectCodes.iterator().next(), containers);
    }

    public static Experiment experimentIlluminaDepot(String user, List<Container> containers) {
        Experiment exp = experimentTransformation(user,
                                                  "illumina-depot",
                                                  "hiseq_4000_system_guide",
                                                  containers.get(0).projectCodes.iterator().next(),
                                                  containers,
                                                  oneToVoidATM(),
                                                  instrumentHiSeq());
        exp.atomicTransfertMethods = new ArrayList<>();
        for(Container c : containers) {
            exp.atomicTransfertMethods.add(oneToVoidATM().apply(new Integer(c.support.line), c));
        }
        Valuation status = new Valuation();
        status.date = new Date();
        status.user = user;
        status.valid = TBoolean.TRUE;
        exp.status = status;
        exp.instrumentProperties = depotIlluminaInstrumentProperties();
        exp.experimentProperties = depotIlluminaExperimentProperties();
        exp.projectCodes = containers.get(0).projectCodes;
        exp.sampleCodes = containers.get(0).sampleCodes;
        return exp;
    }

    public static Experiment experimentNanoporeDepot(String user, List<Container> containers) {
        Experiment exp = experimentTransformation(user, 
                                                  "nanopore-depot", 
                                                  "genomic-dna-ligation-sqk-lsk109-promethion-1d", 
                                                  containers.get(0).projectCodes.iterator().next(), 
                                                  containers, 
                                                  oneToFlowcellATM(),
                                                  instrumentPromethion());
        Valuation status = new Valuation();
        status.user = user;
        status.valid = TBoolean.TRUE;
        status.date = new Date();
        exp.status = status;
        return exp;
    }
    

    public static Experiment experimentPlateToTubes(String user, List<Container> containers) {
        return experiment(user, 
                          "plate-to-tubes",
                          "transfert",
                          "prt_wait_2", 
                          containers.get(0).projectCodes.iterator().next(), 
                          containers,
                          oneToOneATM(TestContainerFactory.TUBE),
                          instrumentHandPlateToTube());
    }

    // TODO maybe need some improvements (generalization)
    public static Experiment experimentTransformation(String user, 
                                                      String typeCode, 
                                                      String protocolCode, 
                                                      String projectCode, 
                                                      List<Container> containers,
                                                      BiFunction<Integer, Container, AtomicTransfertMethod> atm,
                                                      InstrumentUsed instrument) {
        Experiment exp = experiment(user, typeCode, "transformation", protocolCode, projectCode, containers, atm, instrument);
        ReagentUsed ru = new ReagentUsed();
        ru.boxCatalogCode = "295D46GMU";
        ru.kitCatalogCode = "295D46GKL";
        ru.code = "004788-11-1- lot  142-156_";
        ru.description = "146";
        exp.reagents = Arrays.asList(ru);
        return exp;
    }
    

    public static Experiment experimentPrepFCIllumina(String user,
                                                      Container container) {
        String fcCode = DevAppTesting.newCode() + "-HWG5GBBXX";
        Experiment exp = experimentTransformation(user,
                                                  "prepa-fc-ordered",
                                                  "hiseq_4000_system_guide",
                                                  container.projectCodes.iterator().next(),
                                                  Arrays.asList(container),
                                                  manyToOneATM("lane", fcCode),
                                                  instrumentCBot());
        exp.atomicTransfertMethods = new ArrayList<>();
        for(int i=1; i<=8; i++) {
            exp.atomicTransfertMethods.add(manyToOneATM("lane", fcCode).apply(i, container));
        }
        Valuation status = new Valuation();
        status.user = user;
        status.valid = TBoolean.TRUE;
        status.date = new Date();
        exp.status = status;
        exp.instrumentProperties = prepFCIlluminaInstrumentProperties(fcCode);
        exp.experimentProperties = prepFCIlluminaExperimentProperties();
        exp.projectCodes = container.projectCodes;
        exp.sampleCodes = container.sampleCodes;
        return exp;
    }


    

    public static Experiment experimentQC(String user, 
                                          String typeCode, 
                                          String protocolCode, 
                                          String projectCode, 
                                          List<Container> containers) {
        return experiment(user, typeCode, "qualitycontrol", protocolCode, projectCode, containers, oneToVoidATM(), instrumentQuBit());
    }


    private static Experiment experiment(String user, 
                                         String typeCode, 
                                         String categoryCode, 
                                         String protocolCode, 
                                         String projectCode, 
                                         List<Container> containers, 
                                         BiFunction<Integer, Container, AtomicTransfertMethod> atm,
                                         InstrumentUsed instrument) {
        Experiment exp = new Experiment(DevAppTesting.newCode());
        exp.typeCode = typeCode;
        exp.categoryCode = categoryCode;
        exp.protocolCode = protocolCode;
        exp.projectCodes = new HashSet<>(Arrays.asList(projectCode));
        exp.instrument = instrument;
        exp.state = state(user);
        Iterables.zip(Iterables.range(0), containers).forEach(p -> {
            Integer index = p.getLeft();
            Container c = p.getRight();
            if (exp.sampleCodes == null) {
                exp.sampleCodes = new HashSet<>(c.sampleCodes);
            } else {
                exp.sampleCodes.addAll(c.sampleCodes);
            }
            if (exp.inputContainerCodes == null) {
                exp.inputContainerCodes = new HashSet<>(Arrays.asList(c.code));
            } else {
                exp.inputContainerCodes.add(c.code);
            }
            if (exp.atomicTransfertMethods == null) {
                exp.atomicTransfertMethods = new ArrayList<>(Arrays.asList(atm.apply(index, c)));
            } else {
                exp.atomicTransfertMethods.add(atm.apply(index, c));
            }
        });

        exp.inputProcessCodes = new HashSet<>();
        return exp;
    }

    /* - AtomicTransfertMethod generators - */

    private static BiFunction<Integer, Container, AtomicTransfertMethod> oneToVoidATM() {
        return (index, container) -> {
            return oneToVoidContainer(index, container);
        };
    }

    private static BiFunction<Integer, Container, AtomicTransfertMethod> oneToOneATM(String outputCategoryCode) {
        return (index, container) -> {
            return oneToOneContainer(index, container, outputCategoryCode);
        };
    }

    private static BiFunction<Integer, Container, AtomicTransfertMethod> oneToFlowcellATM() {
        return (index, container) -> {
            return oneToOneContainer(index, container, inputContainerUsedInDepotNanopore(container), nanoporeFlowcellOCU(container));
        };
    }

    private static BiFunction<Integer, Container, AtomicTransfertMethod> manyToOneATM(String outputCategoryCode, String outContainerSupportCode) {
        return (index, container) -> {
            return manyToOneContainer(index, container, outputCategoryCode, outContainerSupportCode);
        };
    }

    /* ----------------------------------- */

    private static ManyToOneContainer manyToOneContainer(Integer index,
                                                         Container c,
                                                         String outputCategoryCode,
                                                         String outContainerSupportCode) {
        ManyToOneContainer mtoc = new ManyToOneContainer();
        mtoc.line   = new String(""+index.intValue());
        mtoc.column = "1";
        mtoc.viewIndex = new Integer(mtoc.line);
        State state = new State();
        state.code = "IW-E";
        state.date = new Date();
        state.user = c.state.user;
        int tsIndex = 0;
        if(c.state != null && c.state.historical != null) {
            state.historical = c.state.historical;
            int maxIndex = -1;
            for(TransientState t : c.state.historical) {
                if(t.index > maxIndex) {
                    maxIndex = t.index;
                }
            }
            tsIndex = maxIndex + 1;
        } else {
            state.historical = new HashSet<>();
        }
        TransientState ts = new TransientState(c.state, tsIndex);
        state.historical.add(ts);
        c.state = state;
        InputContainerUsed icu = inputContainerUsed(c);
        icu.experimentProperties = new HashMap<>();
        icu.experimentProperties.put("NaOHConcentration",    new PropertySingleValue(0.2));
        icu.experimentProperties.put("NaOHVolume",           new PropertySingleValue(2.5,     "µL"));
        icu.experimentProperties.put("finalConcentration1",  new PropertySingleValue(4.22,    "nM"));
        icu.experimentProperties.put("finalConcentration2",  new PropertySingleValue(0.633,   "nM"));
        icu.experimentProperties.put("finalVolume1",         new PropertySingleValue(7.5,     "µL"));
        icu.experimentProperties.put("inputVolume",          new PropertySingleValue(7.5,     "µL"));
        icu.experimentProperties.put("inputVolume2",         new PropertySingleValue(7.5,     "µL"));
        icu.experimentProperties.put("masterEPXVolume",      new PropertySingleValue(35.0,    "µL"));
        icu.experimentProperties.put("phixConcentration",    new PropertySingleValue(1.0,     "nM"));
        icu.experimentProperties.put("phixVolume",           new PropertySingleValue(0.3165,  "µL"));
        icu.experimentProperties.put("rsbVolume",            new PropertySingleValue(-0.3165, "µL"));
        icu.experimentProperties.put("trisHCLConcentration", new PropertySingleValue(2.0E8,   "nM"));
        icu.experimentProperties.put("trisHCLVolume",        new PropertySingleValue(5.0,     "µL"));
        mtoc.inputContainerUseds = Arrays.asList(icu);
        mtoc.outputContainerUseds = Arrays.asList(illuminaFlowcellOCU(index, c, outContainerSupportCode));

        return mtoc;
    }

    private static OneToVoidContainer oneToVoidContainer(int index, Container c) {
        OneToVoidContainer otvc = new OneToVoidContainer();
        otvc.line = c.support.line;
        otvc.column = c.support.column;
        
//        State state = new State();
//        state.code = "IW-E";
//        state.date = new Date();
//        state.user = c.state.user;
//        int tsIndex = 0;
//        if(c.state != null && c.state.historical != null) {
//            state.historical = c.state.historical;
//            int maxIndex = -1;
//            for(TransientState t : c.state.historical) {
//                if(t.index > maxIndex) {
//                    maxIndex = t.index;
//                }
//            }
//            tsIndex = maxIndex + 1;
//        } else {
//            state.historical = new HashSet<>();
//        }
//        TransientState ts = new TransientState(c.state, tsIndex);
//        state.historical.add(ts);
//        c.state = state;
        
        otvc.inputContainerUseds = Arrays.asList(inputContainerUsed(c));
        otvc.viewIndex = index;
        return otvc;
    }

    private static OneToOneContainer oneToOneContainer(int index, Container c, String outputCategoryCode) {
        return oneToOneContainer(index, c, inputContainerUsed(c), outputContainerUsed(c, outputCategoryCode));
    }
    
    private static OneToOneContainer oneToOneContainer(int index, Container c, InputContainerUsed icu, OutputContainerUsed ocu) {
        OneToOneContainer otoc = new OneToOneContainer();
        otoc.line = c.support.line;
        otoc.column = c.support.column;
        otoc.inputContainerUseds = Arrays.asList(icu);
        otoc.outputContainerUseds = Arrays.asList(ocu);
        return otoc;
    }

    private static OutputContainerUsed outputContainerUsed(Container c, String categoryCode) {
        OutputContainerUsed ocu = new OutputContainerUsed();
        ocu.code = DevAppTesting.newCode() + c.code;
        ocu.categoryCode = categoryCode;
        ocu.locationOnContainerSupport = TestContainerFactory.support(DevAppTesting.newCode(), TestContainerFactory.TUBE);
        return ocu;
    }
    
    private static OutputContainerUsed nanoporeFlowcellOCU(Container c) {
        OutputContainerUsed ocu = new OutputContainerUsed();
        String code = "PAD01060_A";
        ocu.code = code;
        ocu.categoryCode = "lane";
        ocu.locationOnContainerSupport = TestContainerFactory.support(code, "flowcell-1");
        return ocu;
    }
    
    private static OutputContainerUsed illuminaFlowcellOCU(Integer index, Container c, String fcCode) {
        OutputContainerUsed ocu = new OutputContainerUsed();
        ocu.code = fcCode + "_" + index.intValue();
        ocu.categoryCode = "lane";
        ocu.locationOnContainerSupport = TestContainerFactory.support(fcCode, "flowcell-8", new String(""+index.intValue()), "1");
        ocu.experimentProperties = new HashMap<>();
        ocu.experimentProperties.put("finalVolume", new PropertySingleValue(50.0, "µL"));
        ocu.experimentProperties.put("phixPercent", new PropertySingleValue(1.0));
        return ocu;
    }
    
    public static InputContainerUsed inputContainerUsedInQC(Container c) {
        InputContainerUsed icu = inputContainerUsed(c);
        icu.experimentProperties = qcExperimentProperties();
        return icu;
    }
    
    public static InputContainerUsed inputContainerUsedInDepotNanopore(Container c) {
        InputContainerUsed icu = inputContainerUsed(c);
        icu.experimentProperties = depotNanoporeICUExperimentProperties();
        //icu.contents.get(0).processProperties = depotNanoporeICUProcessProperties();
        return icu;
    }

    private static InputContainerUsed inputContainerUsed(Container c) {
        InputContainerUsed icu = new InputContainerUsed();
        icu.code = c.code;
        icu.categoryCode = c.categoryCode;
        icu.locationOnContainerSupport = c.support;
        icu.contents = c.contents;
        icu.volume = c.volume;
        icu.concentration = c.concentration;
        icu.size = c.size;
        icu.projectCodes = c.projectCodes;
        icu.sampleCodes = c.sampleCodes;
        icu.fromTransformationTypeCodes = c.fromTransformationTypeCodes;
        icu.fromTransformationCodes = c.fromTransformationCodes;
        icu.processCodes = c.processCodes;
        icu.processTypeCodes = c.processTypeCodes;
        icu.state = c.state;
        icu.percentage = new Double(100);
        //icu.experimentProperties = qcExperimentProperties();
        return icu;
    }
    
    private static Map<String, PropertyValue> qcExperimentProperties() {
        Map<String, PropertyValue> props = new HashMap<>();
        props.put("dilutionFactorHS1", new PropertySingleValue("1/1"));
        props.put("inputVolumeHS1",    new PropertySingleValue("20.0", "µL"));
        props.put("calculationMethod", new PropertySingleValue("HS 1 seul"));
        props.put("volume1", 		   new PropertySingleValue("20.0", "µL"));
        return props;
    }
    
    private static Map<String, PropertyValue> depotNanoporeICUExperimentProperties() {
        Map<String, PropertyValue> props = new HashMap<>();
        props.put("loadingQuantity", new PropertySingleValue("10", "ng"));
        return props;
    }
    
    public static Map<String, PropertyValue> depotNanoporeOCUInstrumentProperties(String supportCode) {
        Map<String, PropertyValue> props = new HashMap<>();
        props.put("containerSupportCode", new PropertySingleValue(supportCode));
        props.put("flowcellChemistry",    new PropertySingleValue("PRO002"));
        props.put("position",             new PropertySingleValue("PH_p-104_1"));
        return props;
    }

    public static Map<String, PropertyValue> depotIlluminaInstrumentProperties() {
        Map<String, PropertyValue> props = new HashMap<>();
        props.put("nbCyclesRead1",          new PropertySingleValue(76));
        props.put("nbCyclesRead2",          new PropertySingleValue(76));
        props.put("nbCyclesReadIndex1",     new PropertySingleValue(6));
        props.put("nbCyclesReadIndex2",     new PropertySingleValue(6));
        props.put("position",               new PropertySingleValue("A"));
        props.put("sequencingProgramType",  new PropertySingleValue("PE"));
        return props;
    }
    
    public static Map<String, PropertyValue> depotIlluminaExperimentProperties() {
        Map<String, PropertyValue> props = new HashMap<>();
        props.put("rgActivation",          new PropertySingleValue(true));
        return props;
    }

    private static State state(String user) {
        State s = new State(ExperimentStateNames.N, user);
        s.date = new Date();
        return s;
    }
    
    private static Map<String, PropertyValue> prepFCIlluminaInstrumentProperties(String supportCode) {
        Map<String, PropertyValue> props = new HashMap<>();
        props.put("containerSupportCode",  new PropertySingleValue(supportCode));
        props.put("controlLane",           new PropertySingleValue("Pas de piste contrôle (auto-calibrage)"));
        props.put("sequencingProgramType", new PropertySingleValue("PE"));
        return props;
    }

    private static Map<String, PropertyValue> prepFCIlluminaExperimentProperties() {
        Map<String, PropertyValue> props = new HashMap<>();
        props.put("worksheet", new PropertySingleValue("4000"));
        return props;
    }
    
//    private static Map<String, PropertyValue> depotNanoporeICUProperties() {
//        Map<String, PropertyValue> props = new HashMap<>();
//        props.put("libProcessTypeCodes", new PropertyListValue(Arrays.asList("ONT")));
//        return props;
//    }
    
//    private static Map<String, PropertyValue> depotNanoporeICUProcessProperties() {
//        Map<String, PropertyValue> props = new HashMap<>();
//        props.put("libProcessTypeCodes", new PropertySingleValue("ONT"));
//        return props;
//    }

    /* ---------------- InstrumentUsed ----------------------------------- */
    
    private static InstrumentUsed instrumentQuBit() {
        InstrumentUsed instrument                  = new InstrumentUsed();
        instrument.code 		                   = "QuBit3";
        instrument.categoryCode   				   = "fluorometer";
        instrument.typeCode       				   = "qubit";
        instrument.inContainerSupportCategoryCode  = TestContainerFactory.TUBE;
        instrument.outContainerSupportCategoryCode = "void";
        return instrument;
    }

    private static InstrumentUsed instrumentHiSeq() {
        InstrumentUsed instrument                  = new InstrumentUsed();
        instrument.code 		                   = "TORNADE";
        instrument.categoryCode   				   = "illumina-sequencer";
        instrument.typeCode       				   = "HISEQ4000";
        instrument.inContainerSupportCategoryCode  = "flowcell-8";
        instrument.outContainerSupportCategoryCode = "void";
        return instrument;
    }

    private static InstrumentUsed instrumentHandPlateToTube() {
        InstrumentUsed instrument                  = new InstrumentUsed();
        instrument.code 		                   = "hand";
        instrument.categoryCode   				   = "hand";
        instrument.typeCode       				   = "hand";
        instrument.inContainerSupportCategoryCode  = TestContainerFactory.PLATE_96;
        instrument.outContainerSupportCategoryCode = TestContainerFactory.TUBE;
        return instrument;
    }

    private static InstrumentUsed instrumentPromethion() {
        InstrumentUsed iu = new InstrumentUsed();
        iu.code                            = "PCT0004";
        iu.categoryCode                    = "nanopore-sequencer";
        iu.typeCode                        = "promethION";
        iu.inContainerSupportCategoryCode  = "tube";
        iu.outContainerSupportCategoryCode = "flowcell-1";
        return iu;
    }
    
    private static InstrumentUsed instrumentCBot() {
        InstrumentUsed iu = new InstrumentUsed();
        iu.code                            = "cBot2";
        iu.typeCode                        = "cBot";
        iu.categoryCode                    = "cbot";
        iu.inContainerSupportCategoryCode  = "tube";
        iu.outContainerSupportCategoryCode = "flowcell-8";
        return iu;
    }
    
    /* ------------------------------------------------------------------ */
    
    
    public static ListFormWrapper<Experiment> wrapper(String projCode, QueryMode reporting, RenderMode render) throws Exception {
        ExperimentSearchForm form = new ExperimentSearchForm();
        form.projectCode = projCode;
        return new TestListFormWrapperFactory<Experiment>().wrapping().apply(form, reporting, render);
    }

    public static ListFormWrapper<Experiment> wrapper(String projCode) throws Exception {
        return wrapper(projCode, null, null);
    }

    // Temporary ---------------------------------------------------------------
    // TODO remove it and use dedicated factory
    public static Process processQC(String containerCode, String supportCode) {
        Process p      = process(containerCode, supportCode);
        p.categoryCode = "satellites";
        p.typeCode     = "qc-transfert-purif";
        return p;
    }
    // TODO remove it and use dedicated factory
    private static Process process(String containerCode, String supportCode) {
        Process p = new Process();
        p.inputContainerCode = containerCode;
        p.inputContainerSupportCode = supportCode;
        return p;
    }
    // TODO remove it and use dedicated factory
    public static Process processTransformation(String containerCode, String supportCode) {
        Process p = process(containerCode, supportCode);
        p.categoryCode = "sequencing";
        p.typeCode = "norm-fc-depot-illumina";

        // required properties
        p.properties = new HashMap<>();
        p.properties.put("sequencingType", 			new PropertySingleValue("Hiseq 4000"));
        p.properties.put("devProdContext", 			new PropertySingleValue("PROD"));
        p.properties.put("readLength", 	  			new PropertySingleValue("undefined"));
        p.properties.put("estimatedPercentPerLane", new PropertySingleValue("20"));
        p.properties.put("readType", 				new PropertySingleValue("PE"));

        return p;
    }

    // ---------------------------------------------------------------
}
