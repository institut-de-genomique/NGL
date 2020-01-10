package services.instance.experiment;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.mongojack.DBQuery;

import fr.cea.ig.MongoDBDAO;
import models.Constants;
import models.laboratory.common.instance.State;
import models.laboratory.common.instance.TraceInformation;
import models.laboratory.common.instance.property.PropertySingleValue;
import models.laboratory.container.instance.Container;
import models.laboratory.container.instance.LocationOnContainerSupport;
import models.laboratory.experiment.description.ExperimentType;
import models.laboratory.experiment.instance.AtomicTransfertMethod;
import models.laboratory.experiment.instance.Experiment;
import models.laboratory.experiment.instance.InputContainerUsed;
import models.laboratory.experiment.instance.OneToVoidContainer;
import models.laboratory.instrument.description.InstrumentUsedType;
import models.laboratory.instrument.instance.InstrumentUsed;
import models.utils.InstanceConstants;
import models.utils.dao.DAOException;
import validation.ContextValidation;

public class ExperimentImport {

	private static final play.Logger.ALogger logger = play.Logger.of(ExperimentImport.class);
	
	protected static final String  EXPERIMENT_TYPE_CODE_DEFAULT = "illumina-depot"; 
	
	public static Experiment experimentDepotIlluminaMapRow(ResultSet rs, int rowNum, ContextValidation ctxErr, String protocoleCode) throws SQLException {
		
		//create new experiment (without any attribute)
		Experiment experiment = new Experiment();

		//define code
		experiment.code=rs.getString("code_exp");
		logger.debug("Experiment code :"+experiment.code);
		
		//define experimentTypeCode
		String experimentTypeCode=EXPERIMENT_TYPE_CODE_DEFAULT;
		
		//verification existence of this experimentTypeCode
		ExperimentType experimentType=null;
		try {
			experimentType = ExperimentType.find.get().findByCode(experimentTypeCode);
		} catch (DAOException e) {
			logger.error("",e);
			return null;
		}
		if (experimentType == null) {
			ctxErr.addError("code", "error.codeNotExist", experimentTypeCode, experiment.code);
			return null;
		}
		
		experiment.typeCode=experimentType.code;
		
		//define categoryCode
		experiment.categoryCode=experimentType.category.code;
		
		//define trace information for this experiment
		experiment.traceInformation = new TraceInformation();
		experiment.traceInformation.setTraceInformation(Constants.NGL_DATA_USER);
		
		//define instrumentProperties attributes
		experiment.instrumentProperties = new HashMap<>(); // <String, PropertyValue>();
		experiment.instrumentProperties.put("sequencingProgramType", new PropertySingleValue(rs.getString("type_lecture")));
		experiment.instrumentProperties.put("nbCyclesRead1", new PropertySingleValue(rs.getString("nb_cycles_read1")));
		experiment.instrumentProperties.put("nbCyclesReadIndex1", new PropertySingleValue(rs.getString("nb_cycles_index1")));
		experiment.instrumentProperties.put("nbCyclesRead2", new PropertySingleValue(rs.getString("nb_cycles_read2")));
		experiment.instrumentProperties.put("nbCyclesReadIndex2", new PropertySingleValue(rs.getString("nb_cycles_index2")));
			
		if (rs.getString("type_instr").equals("HISEQ2500")) {
			experiment.instrumentProperties.put("runMode", new PropertySingleValue(rs.getString("mode_run")));
		}			
		if (rs.getString("type_instr").equals("HISEQ2000") || rs.getString("type_instr").equals("HISEQ2500")) {
			experiment.instrumentProperties.put("position", new PropertySingleValue(rs.getString("position")));
		}
			
		//create new empty instrument
		InstrumentUsed instrumentUsed = new InstrumentUsed(); 		
		instrumentUsed.code = rs.getString("code_instr");
		
		String instrumentUsedTypeCode = rs.getString("type_instr");
		InstrumentUsedType instrumentUsedType=null;
		try {
			instrumentUsedType = InstrumentUsedType.find.get().findByCode(instrumentUsedTypeCode);
		} catch (DAOException e) {
			logger.error("",e);
			return null;
		}
		if (instrumentUsedType==null) {
			ctxErr.addError("code", "error.codeNotExist", instrumentUsedTypeCode, instrumentUsed.code);
			return null;
		}
		
		instrumentUsed.typeCode = instrumentUsedType.code; 				
		instrumentUsed.categoryCode = instrumentUsedType.category.code;
		instrumentUsed.inContainerSupportCategoryCode = rs.getString("type_flowcell");
		
		//finally, associate this new instrument to the current experiment
		experiment.instrument = instrumentUsed; 
		
		//define protocol
		experiment.protocolCode = protocoleCode;  
		//Logger.warn("Protocol must be find dynamically !"); 

		
		//define experiment state
		State state = new State();
		state.code = "F";   // GA: mapping
		state.date = rs.getDate("min_date"); 
		state.user = "ngl";
		
		//List<String> rCodes = new ArrayList<String>();
		//rCodes.add("correct");
		//state.resolutionCodes = rCodes; 
		
		experiment.state = state;	
		
		
		//define atomicTransfertMethods
		List<AtomicTransfertMethod> hm = new ArrayList<>(); 

		List<Container> containers = MongoDBDAO.find(InstanceConstants.CONTAINER_COLL_NAME, Container.class, DBQuery.is("support.code",rs.getString("code_flowcell"))).toList();
		Set<String> projectCodes = new HashSet<>();
		Set<String> sampleCodes = new HashSet<>();
		
		if (containers == null || containers.size() == 0) {
			logger.error("Containers with support.code =" + rs.getString("code_flowcell") + " non trouvés dans la base !");
		} else {
			int i = 0;
			for (Container c : containers) {			
				//define one atomicTransfertMethod for each container
				OneToVoidContainer atomicTransfertMethod = new OneToVoidContainer();		
				atomicTransfertMethod.line = "1";
				atomicTransfertMethod.column = "1";
				atomicTransfertMethod.inputContainerUseds = new ArrayList<>();
				InputContainerUsed cnt = new InputContainerUsed();
				cnt.code = c.code;
				atomicTransfertMethod.inputContainerUseds.add(cnt);
				
				LocationOnContainerSupport locationOnContainerSupport = new LocationOnContainerSupport(); 
				locationOnContainerSupport.code = rs.getString("code_flowcell"); 
				locationOnContainerSupport.line = c.support.line; 
				locationOnContainerSupport.column = "1"; 
				atomicTransfertMethod.inputContainerUseds.get(atomicTransfertMethod.inputContainerUseds.indexOf(cnt)).locationOnContainerSupport = locationOnContainerSupport;
				
				
				
								
				hm.add(i, atomicTransfertMethod);
				i++;
				
				for (String pc : c.projectCodes) {
					if (!projectCodes.contains(pc)) {
						projectCodes.add(pc);
					}
				}
				for (String sc : c.sampleCodes) {
					if (!sampleCodes.contains(sc)) {
						sampleCodes.add(sc);
					}
				}
			}
		}
		experiment.atomicTransfertMethods =  hm; 
		
		
		//projectCodes & sampleCodes from the container 
		experiment.projectCodes = projectCodes;
		experiment.sampleCodes = sampleCodes;	

		
		//set limsCode
		experiment.experimentProperties = new HashMap<>(); // <String, PropertyValue>();
		experiment.experimentProperties.put("limsCode", new PropertySingleValue(rs.getString("lims_code")));
		
		//set runStartDate
		//experiment.experimentProperties.put("runStartDate", new PropertySingleValue(rs.getDate("min_date")));
		
		//set inputContainerSupportCodes
		//experiment.inputContainerSupportCodes = ExperimentHelper.getInputContainerSupportCodes(experiment);

		//return the object with this main attributes defined
		return experiment;
	}

}
