package controllers.migration;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import org.mongojack.DBQuery;
import org.mongojack.DBUpdate;

import controllers.CommonController;
import fr.cea.ig.MongoDBDAO;
import models.laboratory.common.instance.PropertyValue;
import models.laboratory.common.instance.State;
import models.laboratory.common.instance.TraceInformation;
import models.laboratory.common.instance.property.PropertySingleValue;
import models.laboratory.container.instance.Container;
import models.laboratory.container.instance.ContainerSupport;
import models.laboratory.container.instance.Content;
import models.laboratory.container.instance.LocationOnContainerSupport;
import models.laboratory.run.instance.InstrumentUsed;
import models.laboratory.run.instance.ReadSet;
import models.laboratory.run.instance.Run;
import models.laboratory.run.instance.Treatment;
import models.laboratory.sample.instance.Sample;
import models.utils.InstanceConstants;
// import play.Logger;
// import play.Logger.ALogger;
// import play.api.modules.spring.Spring;
import play.mvc.Result;
import validation.ContextValidation;
import workflows.run.RunWorkflows;

/**
 * Create new run and readset for nanopore from file (codeRun,typeRun,flowCellCode,project,sample)
 * 
 * @author ejacoby
 *
 */
public class MigrationCreateRunNanopore extends CommonController {

	private static final play.Logger.ALogger logger = play.Logger.of(MigrationCreateRunNanopore.class);
	
	// final static RunWorkflows workflows = Spring.get BeanOfType(RunWorkflows.class);
	private final RunWorkflows workflows;
	
	public MigrationCreateRunNanopore(RunWorkflows workflows) {
		this.workflows = workflows;
	}
	
	public Result migration() {
		//Get File
		MigrationForm form = filledFormQueryString(MigrationForm.class);

		BufferedReader reader = null;
		//Parse file
		try {
			//Remove from database
			removeFromDateBase(form.file);
			reader = new BufferedReader(new FileReader(new File(form.file)));
			//Read header
			String line = reader.readLine();
			while ((line = reader.readLine()) != null) {
				logger.debug("Line "+line);
				String[] tabLine = line.split(";");
				String runCode = tabLine[0];
				String runType = tabLine[1];
				String flowCellCode = tabLine[2];
				String projectCode = tabLine[3];
				String sampleCode = tabLine[4];
				logger.debug("Code sample "+sampleCode);
				ContextValidation ctxVal = new ContextValidation("ngl-bi");
				ctxVal.setCreationMode();
				Sample sample = MongoDBDAO.findByCode(InstanceConstants.SAMPLE_COLL_NAME, Sample.class, sampleCode);
				if (sample!=null) {
					logger.debug("Sample "+sample);
					//Create containerSupport (FlowCell)
					ContainerSupport containerSupport = new ContainerSupport();
					containerSupport.code=flowCellCode;
					containerSupport.categoryCode="flowcell-1";
					containerSupport.state=new State("UA", "ngl-bi");
					containerSupport.traceInformation=new TraceInformation();
					containerSupport.traceInformation.setTraceInformation("ngl-bi");
					containerSupport.projectCodes.add(projectCode);
					containerSupport.sampleCodes.add(sampleCode);
					containerSupport.fromTransformationTypeCodes.add("nanopore-depot");

					//Validate containerSupport
					containerSupport.validate(ctxVal);

					//Create Container
					Container container = new Container();
					container.code=flowCellCode;
					container.categoryCode="lane";
					container.state=new State("UA","ngl-bi");
					container.traceInformation=new TraceInformation();
					container.traceInformation.setTraceInformation("ngl-bi");
					container.support=new LocationOnContainerSupport();
					container.support.code=flowCellCode;
					container.support.categoryCode="flowcell-1";
					container.support.column="1";
					container.support.line="1";
					Content content = new Content(sampleCode, sample.typeCode, sample.categoryCode);
					content.projectCode=projectCode;
					content.percentage=new Double(100);
					content.referenceCollab=sample.referenceCollab;
					content.properties.put("libProcessTypeCode", new PropertySingleValue("ONT"));
					container.contents.add(content);
					container.projectCodes.add(projectCode);
					container.sampleCodes.add(sampleCode);
					container.fromTransformationTypeCodes.add("nanopore-depot");

					//Validate container
					container.validate(ctxVal);

					if (!ctxVal.hasErrors()) {
						//Insert containerSupport in database
						MongoDBDAO.save(InstanceConstants.CONTAINER_SUPPORT_COLL_NAME, containerSupport);
						//Insert container in database
						MongoDBDAO.save(InstanceConstants.CONTAINER_COLL_NAME,container);
					} else {
						ctxVal.displayErrors(logger);
						return badRequest("Validation errors");
					}
					//Create Run
					//Creation run
					Run run=new Run();
					//Date runStartDate = DateUtils.addHours(convertRunCodeToDate(runCode),-24);
					Date runStartDate = convertRunCodeToDate(runCode);
					logger.debug("Run start date "+runStartDate);
					run.sequencingStartDate=runStartDate;
					run.state=new State("N","ngl-bi");

					run.containerSupportCode=flowCellCode;
					//run.projectCodes = $experiment.projectCodes; //done with readset creation
					//run.sampleCodes = $experiment.sampleCodes; //done with readset creation
					run.traceInformation=new TraceInformation();
					run.traceInformation.setTraceInformation("ngl-bi");

					run.typeCode=runType;	

					run.instrumentUsed=new InstrumentUsed();
					run.instrumentUsed.code=runCode.split("_")[1];
					run.categoryCode="nanopore";
					if(runType.equals("RMINION")){
						run.instrumentUsed.typeCode="minION";
					}else if(runType.equals("RMKI")){
						run.instrumentUsed.typeCode="mk1";
					}
					run.code=runCode;
					Treatment treatment=new Treatment();
					treatment.code="minknowMetrichor";
					treatment.typeCode="minknow-metrichor";
					treatment.categoryCode="sequencing";
					treatment.results.put("default",new HashMap<String, PropertyValue>(0));
					run.treatments.put("minknowMetrichor",treatment);

					//Validate run
					run.validate(ctxVal);

					if (!ctxVal.hasErrors()) {
						//Insert run in database
						MongoDBDAO.save(InstanceConstants.RUN_ILLUMINA_COLL_NAME,run);
						
					}else{
						ctxVal.displayErrors(logger);
						return badRequest("Validation errors");
					}
					//Create ReadSet
					ReadSet readSet=new ReadSet();
					readSet.typeCode="rsnanopore";
					readSet.code=sampleCode+"_ONT_1_"+flowCellCode;
					readSet.state=new State("N","ngl-bi");

					readSet.runCode=runCode;
					readSet.runTypeCode=runType;
					readSet.runSequencingStartDate=runStartDate;
					readSet.laneNumber=1;
					readSet.sampleCode=sampleCode;
					readSet.projectCode=projectCode;
					readSet.path="A_RENSEIGNER";
					readSet.location="CNS";	
					readSet.traceInformation=new TraceInformation();
					readSet.traceInformation.setTraceInformation("ngl-bi");

					//Validate readSet
					readSet.validate(ctxVal);
					if (!ctxVal.hasErrors()) {
						//Insert readSet in database
						MongoDBDAO.save(InstanceConstants.READSET_ILLUMINA_COLL_NAME, readSet);
						MongoDBDAO.update(InstanceConstants.RUN_ILLUMINA_COLL_NAME, Run.class
								,DBQuery.is("code",run.code)
								,DBUpdate.addToSet("sampleCodes", content.sampleCode).addToSet("projectCodes",content.projectCode));
					} else {
						ctxVal.displayErrors(logger);
						return badRequest("Validation errors");
					}
					ctxVal = new ContextValidation("ngl-bi");
					State nextState = new State();
					nextState.code = "IP-S";
					nextState.date = new Date();
					nextState.user = "ngl-bi";
					workflows.setState(ctxVal, run, nextState);
				}else
					return badRequest("No sample "+sampleCode);
			}
		} catch (FileNotFoundException e) {
			return badRequest(e.getMessage());
		} catch (IOException e) {
			return badRequest(e.getMessage());
		} catch (ParseException e) {
			return badRequest(e.getMessage());
		} finally {
			try {
				reader.close();
			} catch (IOException e) {
				return badRequest(e.getMessage());
			}
		}
		return ok();
	}

	private static Date convertRunCodeToDate(String runCode) throws ParseException {
		DateFormat df = new SimpleDateFormat("yyMMdd-HHmmss");
		String dateFromRunCode = runCode.split("_")[0]+"-000000";
		return df.parse(dateFromRunCode);
	}

	private static void removeFromDateBase(String file) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(new File(file)));
		String line = reader.readLine();
		while ((line = reader.readLine()) != null) {
			logger.debug("Remove "+line);
			String[] tabLine = line.split(";");
			String runCode = tabLine[0];
			String flowCellCode = tabLine[2];
			String sampleCode = tabLine[4];
			//Remove ContainerSupport
			MongoDBDAO.deleteByCode(InstanceConstants.CONTAINER_SUPPORT_COLL_NAME, ContainerSupport.class, flowCellCode);
			//Remove Container
			MongoDBDAO.deleteByCode(InstanceConstants.CONTAINER_COLL_NAME, Container.class, flowCellCode);
			//Remove Run 
			MongoDBDAO.deleteByCode(InstanceConstants.RUN_ILLUMINA_COLL_NAME, Run.class, runCode);
			//Remove ReadSet
			MongoDBDAO.deleteByCode(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, sampleCode+"_ONT_1_"+flowCellCode);
		}
		reader.close();
	}
	
}
