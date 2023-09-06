package fr.cea.ig.ngl.dao.api;

import javax.inject.Inject;
import javax.inject.Singleton;

import fr.cea.ig.ngl.dao.analyses.AnalysesAPI;
import fr.cea.ig.ngl.dao.analyses.AnalysisTreatmentsAPI;
import fr.cea.ig.ngl.dao.codelabels.CodeLabelAPI;
import fr.cea.ig.ngl.dao.containers.ContainerSupportsAPI;
import fr.cea.ig.ngl.dao.containers.ContainersAPI;
import fr.cea.ig.ngl.dao.experiments.ExperimentCommentsAPI;
import fr.cea.ig.ngl.dao.experiments.ExperimentReagentsAPI;
import fr.cea.ig.ngl.dao.experiments.ExperimentsAPI;
import fr.cea.ig.ngl.dao.permissions.PermissionAPI;
import fr.cea.ig.ngl.dao.processes.ProcessesAPI;
import fr.cea.ig.ngl.dao.projects.ProjectCommentsAPI;
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
import fr.cea.ig.ngl.dao.users.UserAPI;
import workflows.project.ProjectWorkflows;

@Singleton
public class APIs {
	
	private final CodeLabelAPI               codeLabelAPI;
	private final ContainersAPI              containerAPI;
	private final ContainerSupportsAPI       containerSupportAPI;
	private final PermissionAPI              permissionAPI;
	private final ProjectsAPI                projectAPI;
	private final ProtocolsAPI               protocolAPI;
	private final ReagentCatalogAPI          reagentCatalogAPI; 
	private final ResolutionConfigurationAPI resolutionConfigurationAPI;
	private final SamplesAPI                 sampleAPI;
	private final SraParameterAPI            sraParameterAPI;
	private final UserAPI                    userAPI;
	private final ValuationCriteriaAPI       valuationCriteriaAPI;
	private final ExperimentsAPI			 experimentAPI;
	private final ExperimentReagentsAPI		 experimentReagentAPI;
	private final ExperimentCommentsAPI		 experimentCommentAPI;
	private final ProjectCommentsAPI		 projectCommentAPI;
	private final ProcessesAPI				 processAPI;
	private final RunsAPI                    runAPI;
	private final TreatmentsAPI              runTreatmentAPI;
    private final LanesAPI                   lanesAPI;
	private final LaneTreatmentsAPI          laneTreatmentAPI;
	private final ReadSetsAPI                readsetAPI;
	private final FilesAPI                   readsetFileAPI;
	private final ReadSetTreatmentsAPI       readsetTreatmentAPI;  
	private final AnalysesAPI                analysesAPI;
    private final AnalysisTreatmentsAPI      analysisTreatmentsAPI;
    private final fr.cea.ig.ngl.dao.analyses.FilesAPI analysisFilesAPI;  
	
    // The APIs being DAOAPI and not higher level APIs, the workflow can be seen as the API
	private final ProjectWorkflows			projectWorkflow;
	
	@Inject
	public APIs(CodeLabelAPI               codeLabelAPI,
				ContainersAPI              containerAPI,
				ContainerSupportsAPI       containerSupportAPI,
				PermissionAPI              permissionAPI,
				ProjectsAPI                projectAPI,
				ProtocolsAPI               protocolAPI,
				ReagentCatalogAPI          reagentCatalogAPI,
				ResolutionConfigurationAPI resolutionConfigurationAPI,
				SamplesAPI                 sampleAPI,
				SraParameterAPI            sraParameterAPI,
				UserAPI                    userAPI,
				ValuationCriteriaAPI       valuationCriteriaAPI,
				ExperimentsAPI			   experimentAPI,
				ExperimentReagentsAPI	   experimentReagentAPI,
				ExperimentCommentsAPI	   experimentCommentAPI,
				ProjectCommentsAPI	   	   projectCommentAPI,
				ProcessesAPI			   processAPI,
				RunsAPI                    runAPI,
				TreatmentsAPI              runTreatmentAPI,
                LanesAPI                   lanesAPI,
                LaneTreatmentsAPI          laneTreatmentAPI,
				ReadSetsAPI                readsetAPI,
				FilesAPI                   readsetFileAPI,
				ReadSetTreatmentsAPI       readsetTreatmentAPI,
				ProjectWorkflows		   projectWorkflow,
				AnalysesAPI                analysesAPI,
                AnalysisTreatmentsAPI      analysisTreatmentsAPI,
                fr.cea.ig.ngl.dao.analyses.FilesAPI analysisFilesAPI) {
		this.codeLabelAPI         = codeLabelAPI;
		this.containerAPI         = containerAPI;
		this.containerSupportAPI  = containerSupportAPI;
		this.permissionAPI        = permissionAPI;
		this.projectAPI           = projectAPI;
		this.protocolAPI          = protocolAPI; 
		this.reagentCatalogAPI    = reagentCatalogAPI;
		this.resolutionConfigurationAPI = resolutionConfigurationAPI;
		this.userAPI              = userAPI;
		this.sampleAPI            = sampleAPI;
		this.sraParameterAPI      = sraParameterAPI;
		this.valuationCriteriaAPI = valuationCriteriaAPI;
		this.experimentAPI        = experimentAPI;
		this.experimentReagentAPI = experimentReagentAPI;
		this.experimentCommentAPI = experimentCommentAPI;
		this.projectCommentAPI    = projectCommentAPI;
		this.processAPI			  = processAPI;
		this.runAPI               = runAPI;
		this.runTreatmentAPI      = runTreatmentAPI;
        this.lanesAPI             = lanesAPI;
		this.laneTreatmentAPI     = laneTreatmentAPI;
		this.readsetAPI           = readsetAPI;
		this.readsetFileAPI       = readsetFileAPI;
		this.readsetTreatmentAPI  = readsetTreatmentAPI;
		this.projectWorkflow	  = projectWorkflow;
		this.analysesAPI          = analysesAPI;
		this.analysisTreatmentsAPI = analysisTreatmentsAPI;
		this.analysisFilesAPI     = analysisFilesAPI;
	}
	
	public CodeLabelAPI               codeLabel()               { return codeLabelAPI;               }
	public ContainersAPI              container()               { return containerAPI;               }
	public ContainerSupportsAPI       containerSupport()        { return containerSupportAPI;        }
	public PermissionAPI              permission()              { return permissionAPI;              }
	public ProjectsAPI                project()                 { return projectAPI;                 }
	public ProtocolsAPI               protocol()                { return protocolAPI;                }
	public ReagentCatalogAPI          reagentCatalog()          { return reagentCatalogAPI;          }
	public ResolutionConfigurationAPI resolutionConfiguration() { return resolutionConfigurationAPI; }
	public SamplesAPI                 sample()                  { return sampleAPI;                  }
	public SraParameterAPI            sraParameter()            { return sraParameterAPI;            }
	public UserAPI                    user()                    { return userAPI;                    }
	public ValuationCriteriaAPI       valuationCriteria()       { return valuationCriteriaAPI;       }
	public ExperimentsAPI			  experiment()   		    { return experimentAPI;       		 }
	public ExperimentReagentsAPI	  experimentReagent()   	{ return experimentReagentAPI;       }
	public ExperimentCommentsAPI	  experimentComment()   	{ return experimentCommentAPI;       }
	public ProjectCommentsAPI	      projectComment()   	    { return projectCommentAPI;          }
	public ProcessesAPI 			  process() 				{ return processAPI;				 }
	public RunsAPI                    run()                     { return runAPI;                     }
	public TreatmentsAPI              runTreatment()            { return runTreatmentAPI;            }
	public LanesAPI                   lane()                    { return lanesAPI;                   }
	public LaneTreatmentsAPI          laneTreatment()           { return laneTreatmentAPI;           }
	public ReadSetsAPI                readset()                 { return readsetAPI;                 }
    public FilesAPI                   readsetFile()             { return readsetFileAPI;             }
    public ReadSetTreatmentsAPI       readsetTreatment()        { return readsetTreatmentAPI;        }
	public ProjectWorkflows			  projectWorkflow()			{ return projectWorkflow;			 }
	public AnalysesAPI                analyses()                { return analysesAPI;                }
    public fr.cea.ig.ngl.dao.analyses.FilesAPI analysisFile()   { return analysisFilesAPI;           }
    public AnalysisTreatmentsAPI       analysisTreatment()      { return analysisTreatmentsAPI;      }
	
}
