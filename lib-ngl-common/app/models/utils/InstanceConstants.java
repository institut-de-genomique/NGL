package models.utils;

import fr.cea.ig.authentication.keys.KeyUtils;

// Link to this : {@link models.utils.InstanceConstants}

/**
 * MongoDB collection names.
 */
public interface InstanceConstants {

	public static final String PROJECT_COLL_NAME            = "ngl_project.Project";
	public static final String UMBRELLA_PROJECT_COLL_NAME   = "ngl_project.UmbrellaProject";

	/**
	 * Name of the collection of {@link models.laboratory.container.instance.ContainerSupport}.
	 */
	public static final String CONTAINER_SUPPORT_COLL_NAME  = "ngl_sq.ContainerSupport";

	public static final String REAGENT_CATALOG_COLL_NAME    = "ngl_sq.ReagentCatalog";

	/**
	 * Name of the collection of {@link models.laboratory.reagent.reception.AbstractReception}
	 */
	public static final String REAGENT_RECEPTION_COLL_NAME	= "ngl_sq.ReagentReception";

	/**
	 * Name of the collection of {@link models.laboratory.container.instance.Container}
	 */
	public static final String CONTAINER_COLL_NAME          = "ngl_sq.Container";

	public static final String PROCESS_COLL_NAME            = "ngl_sq.Process";
	public static final String EXPERIMENT_COLL_NAME         = "ngl_sq.Experiment";

	/**
	 * Name of the collection of {@link models.laboratory.sample.instance.Sample}.
	 */
	public static final String SAMPLE_COLL_NAME             = "ngl_bq.Sample";

	public static final String STORAGE_COLL_NAME            = "ngl_common.Storage";
	public static final String REAGENT_INSTANCE_COLL_NAME   = "ngl_sq.ReagentInstance";

	public static final String RUN_ILLUMINA_COLL_NAME       = "ngl_bi.RunIllumina";
	public static final String READSET_ILLUMINA_COLL_NAME   = "ngl_bi.ReadSetIllumina";
	public static final String ANALYSIS_COLL_NAME           = "ngl_bi.Analysis";
	public static final String ALERT_COLL_NAME              = "ngl_bi.Alert";

	public static final String SRA_STUDY_COLL_NAME          = "ngl_sub.Study";
	public static final String SRA_PROJECT_COLL_NAME        = "ngl_sub.Project";
	public static final String SRA_ANALYSIS_COLL_NAME        = "ngl_sub.Analysis";
	public static final String SRA_SAMPLE_COLL_NAME         = "ngl_sub.Sample";
	public static final String SRA_EXPERIMENT_COLL_NAME     = "ngl_sub.Experiment";
	public static final String SRA_SUBMISSION_COLL_NAME     = "ngl_sub.Submission";
	public static final String SRA_CONFIGURATION_COLL_NAME  = "ngl_sub.Configuration";
	public static final String SRA_READSET_COLL_NAME        = "ngl_sub.Readset";
	public static final String SRA_PARAMETER_COLL_NAME      = "ngl_sub.Parameter";

	public static final String PARAMETER_COLL_NAME          = "ngl_common.Parameter";
	public static final String REPORTING_CONFIG_COLL_NAME   = "ngl_common.ReportingConfiguration";
	public static final String STATS_CONFIG_COLL_NAME       = "ngl_common.StatsConfiguration";
	public static final String FILTERING_CONFIG_COLL_NAME   = "ngl_common.FilteringConfiguration";
	public static final String VALUATION_CRITERIA_COLL_NAME = "ngl_common.ValuationCriteria";
	public static final String RESOLUTION_COLL_NAME         = "ngl_common.ResolutionConfiguration";
	public static final String PROTOCOL_COLL_NAME           = "ngl_sq.Protocol";
	public static final String DESCRIPTION_HISTORY_COLL_NAME= "ngl_common.DescriptionHistory";

	public static final String RECEPTION_CONFIG_COLL_NAME   = "ngl_common.ReceptionConfiguration";

	//public static final String SRA_MAP_LIBPROCESSTYPECODEVALUE_ORIENTATION_COLL_NAME="ngl_sub.map_libProcessTypeCodeValue_orientation";

	public static final String TAG_PROPERTY_NAME = "tag";
	public static final String SECONDARY_TAG_PROPERTY_NAME = "secondaryTag";

	/**
	 * Name of the collection of {@link models.laboratory.balancesheet.instance.BalanceSheet}.
	 */
	public static final String BALANCE_SHEET_COLL_NAME     = "ngl_common.BalanceSheet";
	
	/**
	 * Name of the collection of {@link models.administration.authorisation.instance.KeyDescription}. 
	 */
	public static final String API_KEY_COLL_NAME     		= KeyUtils.API_KEY_COLL_NAME;
}
