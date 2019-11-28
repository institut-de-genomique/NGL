package fr.cea.ig.test;

import java.util.function.Function;

import fr.cea.ig.ngl.dao.analyses.AnalysesAPI;
import fr.cea.ig.ngl.dao.analyses.AnalysisTreatmentsAPI;
import fr.cea.ig.ngl.dao.api.APIs;
import fr.cea.ig.ngl.dao.containers.ContainerSupportsAPI;
import fr.cea.ig.ngl.dao.containers.ContainersAPI;
import fr.cea.ig.ngl.dao.experiments.ExperimentCommentsAPI;
import fr.cea.ig.ngl.dao.experiments.ExperimentReagentsAPI;
import fr.cea.ig.ngl.dao.experiments.ExperimentsAPI;
import fr.cea.ig.ngl.dao.processes.ProcessesAPI;
import fr.cea.ig.ngl.dao.projects.ProjectsAPI;
import fr.cea.ig.ngl.dao.protocols.ProtocolsAPI;
import fr.cea.ig.ngl.dao.readsets.FilesAPI;
import fr.cea.ig.ngl.dao.readsets.ReadSetTreatmentsAPI;
import fr.cea.ig.ngl.dao.readsets.ReadSetsAPI;
import fr.cea.ig.ngl.dao.runs.LaneTreatmentsAPI;
import fr.cea.ig.ngl.dao.runs.LanesAPI;
import fr.cea.ig.ngl.dao.runs.RunsAPI;
import fr.cea.ig.ngl.dao.runs.TreatmentsAPI;
import fr.cea.ig.ngl.dao.samples.SamplesAPI;
import fr.cea.ig.play.IGGlobals;

/**
 * Simple static access to APIs.
 * 
 * @author vrd
 *
 * @param <T> API type
 */
public class APIRef<T> {
	
    private final Function<APIs,T> f;
	
	public APIRef(Function<APIs,T> f) { this.f = f; }
	
	public T get() {
		return f.apply(IGGlobals.instanceOf(APIs.class));
	}
	
	
	public static final APIRef<ExperimentsAPI> 		  experiment 	    = new APIRef<>(apis -> apis.experiment());
	public static final APIRef<ContainersAPI>         container         = new APIRef<>(apis -> apis.container());
	public static final APIRef<ContainerSupportsAPI>  containerSupport  = new APIRef<>(apis -> apis.containerSupport());
	public static final APIRef<ProjectsAPI>           project           = new APIRef<>(apis -> apis.project());
	public static final APIRef<SamplesAPI>            sample            = new APIRef<>(apis -> apis.sample());
	public static final APIRef<ExperimentReagentsAPI> experimentReagent = new APIRef<>(apis -> apis.experimentReagent());
	public static final APIRef<ExperimentCommentsAPI> experimentComment = new APIRef<>(apis -> apis.experimentComment());
	public static final APIRef<ProtocolsAPI> 		  protocol 			= new APIRef<>(apis -> apis.protocol());
	public static final APIRef<ProcessesAPI> 		  process 			= new APIRef<>(apis -> apis.process());
	public static final APIRef<RunsAPI>               run               = new APIRef<>(apis -> apis.run());
	public static final APIRef<TreatmentsAPI>         runTreatment      = new APIRef<>(apis -> apis.runTreatment());
	public static final APIRef<LanesAPI>              runLane           = new APIRef<>(apis -> apis.lane());
	public static final APIRef<LaneTreatmentsAPI>     runLaneTreatment  = new APIRef<>(apis -> apis.laneTreatment());
	public static final APIRef<ReadSetsAPI>           readset           = new APIRef<>(apis -> apis.readset());
	public static final APIRef<FilesAPI>              readsetFile       = new APIRef<>(apis -> apis.readsetFile());
	public static final APIRef<ReadSetTreatmentsAPI>  readsetTreatment  = new APIRef<>(apis -> apis.readsetTreatment());
	public static final APIRef<AnalysesAPI>           analysis          = new APIRef<>(apis -> apis.analyses());
	public static final APIRef<AnalysisTreatmentsAPI> analysisTreatment = new APIRef<>(apis -> apis.analysisTreatment());
	public static final APIRef<fr.cea.ig.ngl.dao.analyses.FilesAPI> analysisFile = new APIRef<>(apis -> apis.analysisFile());
	
}