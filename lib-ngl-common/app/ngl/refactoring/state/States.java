package ngl.refactoring.state;

import static ngl.refactoring.state.ObjectTypes.Analysis;
import static ngl.refactoring.state.ObjectTypes.Container;
import static ngl.refactoring.state.ObjectTypes.Experiment;
import static ngl.refactoring.state.ObjectTypes.Import;
import static ngl.refactoring.state.ObjectTypes.Instrument;
import static ngl.refactoring.state.ObjectTypes.Process;
import static ngl.refactoring.state.ObjectTypes.Project;
import static ngl.refactoring.state.ObjectTypes.ReadSet;
import static ngl.refactoring.state.ObjectTypes.Reagent;
import static ngl.refactoring.state.ObjectTypes.ReagentReception;
import static ngl.refactoring.state.ObjectTypes.Run;
import static ngl.refactoring.state.ObjectTypes.SRAConfiguration;
import static ngl.refactoring.state.ObjectTypes.SRAExperiment;
import static ngl.refactoring.state.ObjectTypes.SRASample;
import static ngl.refactoring.state.ObjectTypes.SRAStudy;
import static ngl.refactoring.state.ObjectTypes.SRASubmission;
import static ngl.refactoring.state.ObjectTypes.Sample;
import static ngl.refactoring.state.ObjectTypes.Treatment;

import java.util.ArrayList;
import java.util.List;

import models.laboratory.common.description.ObjectType;
import models.laboratory.common.description.State;
import models.laboratory.common.description.StateCategory;

/**
 * Pseudo enumeration definition of states.
 *
 * @author vrd
 *
 */
public class States {

	/**
	 * Available.
	 */
	public static final State A;

	/**
	 * Unavailable.
	 */
	public static final State UA;

	/**
	 * Done ('finished' looks like a French bad naming).
	 */
	public static final State F;
	
	/**
	 * Declared.
	 */
	public static final State IW_U;
	
	/**
	 * Abandoned.
	 */
	public static final State FE;

	/**
	 * Waiting for quality control.
	 */
	public static final State IW_QC;

	/**
	 * Quality control running.
	 */
	public static final State IP_QC;

	/**
	 * Quality control done.
	 */
	public static final State F_QC;

	/**
	 * Waiting for valuation.
	 */
	public static final State IW_V;

	/**
	 * Valuation running.
	 */
	public static final State IP_V;

	/**
	 * Valuation done.
	 */
	public static final State F_V;

	/**
	 * Waiting for container.
	 */
	public static final State IW_C;

	/**
	 * New.
	 */
	public static final State N;

	/**
	 * Running.
	 */
	public static final State IP;

	/**
	 * Waiting for process.
	 */
	public static final State IW_P;

	/**
	 * Waiting for experiment.
	 */
	public static final State IW_E;

	/**
	 * In use.
	 */
	public static final State IU;

	/**
	 * Waiting for dispatch.
	 */
	public static final State IW_D;

	/**
	 * In stock.
	 */
	public static final State IS;

	/**
	 * Available for transformation.
	 */
	public static final State A_TM;

	/**
	 * Available for quality control.
	 */
	public static final State A_QC;

	/**
	 * Available for purification.
	 */
	public static final State A_PF;

	/**
	 * Available for transfer.
	 */
	public static final State A_TF;

	/**
	 * Sequencing running.
	 */
	public static final State IP_S;

	/**
	 * Sequencing done.
	 */
	public static final State F_S;

	/**
	 * Sequencing error.
	 */
	public static final State FE_S;

	/**
	 * Waiting for read generation.
	 */
	public static final State IW_RG;

	/**
	 * Read generation running.
	 */
	public static final State IP_RG;

	/**
	 * Read generation done.
	 */
	public static final State F_RG;

	/**
	 * Defined state values.
	 */
	public static final State[] values;

	/**
	 * Waiting for quality control execution.
	 */
	public static final State IW_VQC;

	/**
	 * Quality control running.
	 */
	public static final State IP_VQC;

	/**
	 * Quality control done.
	 */
	public static final State F_VQC;

	/**
	 * Waiting for bioinformatics analysis.
	 */
	public static final State IW_BA;

	/**
	 * BA analysis running.
	 */
	public static final State IP_BA;

	/**
	 * BA analysis done.
	 */
	public static final State F_BA;

	/**
	 * Waiting for CCRT transfer.
	 */
	public static final State IW_TF;

	/**
	 * CCRT transfer running.
	 */
	public static final State IP_TF;

	/**
	 * CCRT transfer done.
	 */
	public static final State F_TF;

	/**
	 * CCRT transfer error.
	 */
	public static final State FE_TF;

	/**
	 * Waiting for BA analysis.
	 */
	public static final State IW_VBA;

	/**
	 * BA analysis done.
	 */
	public static final State F_VBA;

	/**
	 * NONE. 
	 */
	public static final State NONE;

	/**
	 * New submission . 
	 */
	public static final State SUB_N;
	
	/**
	 * User validated submission. 
	 */
	public static final State SUB_V;
	
	/**
	 * Waiting for send raw data({@link States#SUB_SRD_IW}). 
	 */
	public static final State SUB_SRD_IW;
	
	/**
	 * Running for send raw data(({@link States#SUB_SRD_IP}). 
	 */
	public static final State SUB_SRD_IP;	
	
	/**
	 * Error for send raw data ({@link States#SUB_SRD_FE}). 
	 */
	public static final State SUB_SRD_FE;
	
	/**
	 * Finished for send raw data ({@link States#SUB_SRD_F}). 
	 */
	public static final State SUB_SRD_F;
	
	/**
	 * In waiting for send meta data ({@link States#SUB_SMD_IW}). 
	 */
	public static final State SUB_SMD_IW;	
	
	/**
	 * Running for send meta data ({@link States#SUB_SMD_IP}). 
	 */
	public static final State SUB_SMD_IP;
	
	/**
	 * Finished for send meta data  ({@link States#SUB_SMD_F}). 
	 */
	public static final State SUB_SMD_F;
	
	/**
	 * Error for send meta data  ({@link States#SUB_SMD_FE}). 
	 */
	public static final State SUB_SMD_FE;	
	
	/**
	 * Submission error ({@link States#SUB_FE}). 
	 */
	public static final State SUB_FE;	
	
	/**
	 * Submission finish ({@link States#SUB_F}). 
	 */
	public static final State SUB_F;	
	
	
	/**
	 * new update ({@link States#SUBR_N}). 
	 */
	public static final State SUBR_N;	
	
	/**
	 * In waiting for send meta data  ({@link States#SUBR_SMD_IW}). 
	 */
	public static final State SUBR_SMD_IW;	
	
	/**
	 * Running for send meta data ({@link States#SUBR_SMD_IP}). 
	 */
	public static final State SUBR_SMD_IP;	
	
	/**
	 * Error for send meta data ({@link States#SUBR_SMD_FE}). 
	 */
	public static final State SUBR_SMD_FE;
	
	/**
	 * Finished for send meta data ({@link States#SUBR_SMD_F}). 
	 */
	public static final State SUBR_SMD_F;
		
	/**
	 * Error for release submission ({@link States#SUBR_FE}). 
	 */
	public static final State SUBR_FE;		

	
	/**
	 * new update ({@link States#SUBU_N}). 
	 */
	public static final State SUBU_N;	
	
	/**
	 * User validated ({@link States#SUBU_V}). 
	 */
	public static final State SUBU_V;	
	
	/**
	 * In waiting for send meta data  ({@link States#SUBU_SMD_IW}). 
	 */
	public static final State SUBU_SMD_IW;	
	
	/**
	 * Running for send meta data ({@link States#SUBU_SMD_IP}). 
	 */
	public static final State SUBU_SMD_IP;	
	
	/**
	 * Error for send meta data ({@link States#SUBU_SMD_FE}). 
	 */
	public static final State SUBU_SMD_FE;	
	
	/**
	 * Finished for send meta data  ({@link States#SUBU_SMD_F}). 
	 */
	public static final State SUBU_SMD_F;
	
	/**
	 * Finished for send meta data  ({@link States#SUBU_FE}). 
	 */
	public static final State SUBU_FE;	
			

			

	
	// Functional groups
	public static final String FG_F, FG_QC, FG_V, FG_NONE, FG_N, FG_S, FG_RG, FG_VQC, FG_BA, FG_TF, FG_VBA;

//  extrait de NGL-Data pour vérification par rapport a l'ancien code
//	static {
//
//		FG_BA   = "BA";
//		FG_F    = "F";
//		FG_N    = "N";
//		FG_NONE = null;
//		FG_QC   = "QC";
//		FG_RG   = "RG";
//		FG_S    = "S";
//		FG_TF   = "TF";
//		FG_V    = "V";
//		FG_VBA  = "VBA";
//		FG_VQC  = "VQC";
//
//		values = new State[] {
//			A        = newState(StateNames.A,        "Disponible",                               true, 1000, StateCategories.N,  true,  FG_F,    Container, ReadSet),
//			UA       = newState(StateNames.UA,       "Indisponible",                             true, 1001, StateCategories.N,  true,  FG_F,    Container, ReadSet),
//			F        = newState(StateNames.F,        "Terminé",                                  true, 1000, StateCategories.F,  true,  FG_F,    Container, Project, Experiment, Process, Run, Sample, Instrument, Reagent, Import, Treatment),
//
//			IW_QC    = newState(StateNames.IW_QC,    "Contrôle qualité en attente",              true,  400, StateCategories.IW, true,  FG_QC,   ReadSet),
//			IP_QC    = newState(StateNames.IP_QC,    "Contrôle qualité en cours",                true,  450, StateCategories.IP, true,  FG_QC,   ReadSet),
//			F_QC     = newState(StateNames.F_QC,     "Contrôle qualité terminé",                 true,  500, StateCategories.F,  false, FG_QC,   ReadSet),
//			IW_V     = newState(StateNames.IW_V,     "Evaluation en attente",                    true,  800, StateCategories.IW, true,  FG_V,    Run, Analysis),
//			IP_V     = newState(StateNames.IP_V,     "Evaluation en cours",                      true,  825, StateCategories.IP, true,  FG_V,    Run),
//			F_V      = newState(StateNames.F_V,      "Evaluation terminée",                      true,  849, StateCategories.F,  true,  FG_V,    Run, Analysis),
//
//			IW_C     = newState(StateNames.IW_C,     "En attente de Container",                  true, -100, StateCategories.IW, true,  FG_NONE, Process),
//			N        = newState(StateNames.N,        "Nouveau",                                  true,    0, StateCategories.N,  true,  FG_N,    Project, Experiment, Process, Run, ReadSet, Sample, Instrument, Reagent, Import, Treatment, Container, Analysis, SRASubmission, SRAConfiguration),
//			IP       = newState(StateNames.IP,       "En cours",                                 true,  500, StateCategories.IP, true,  FG_NONE, Project, Experiment, Process, Sample, Instrument, Reagent, Import, Treatment),
//			IW_P     = newState(StateNames.IW_P,     "En attente de Processus",                  true,  100, StateCategories.IW, true,  FG_NONE, Container),
//			IW_E     = newState(StateNames.IW_E,     "Expérience en attente",                    true,  200, StateCategories.IW, true,  FG_NONE, Container),
//			IU       = newState(StateNames.IU,       "En cours d'utilisation",                   true,  250, StateCategories.IP, true,  FG_NONE, Container),
//			IW_D     = newState(StateNames.IW_D,     "Dispatch en attente",                      true,  300, StateCategories.IW, true,  FG_NONE, Container),
//			IS       = newState(StateNames.IS,       "En stock",                                 true, 1000, StateCategories.N,  true,  FG_NONE, Container),
//			A_TM     = newState(StateNames.A_TM,     "Disponible transformation",                true,  900, StateCategories.N,  true,  FG_NONE, Container),
//			A_QC     = newState(StateNames.A_QC,     "Disponible controle qualité",              true,  901, StateCategories.N,  true,  FG_NONE, Container),
//			A_PF     = newState(StateNames.A_PF,     "Disponible purif",                         true,  902, StateCategories.N,  true,  FG_NONE, Container),
//			A_TF     = newState(StateNames.A_TF,     "Disponible transfert",                     true,  903, StateCategories.N,  true,  FG_NONE, Container),
//
//			IP_S     = newState(StateNames.IP_S,     "Séquençage en cours",                      true,  150, StateCategories.IP, true,  FG_S,    Run),
//			F_S      = newState(StateNames.F_S,      "Séquençage terminé",                       true,  195, StateCategories.F,  false, FG_S,    Run),
//			FE_S     = newState(StateNames.FE_S,     "Séquençage en échec",                      true,  199, StateCategories.F,  true,  FG_S,    Run),
//
//			IW_RG    = newState(StateNames.IW_RG,    "Read generation en attente",               true,  200, StateCategories.IW, true,  FG_RG,   Run),
//			IP_RG    = newState(StateNames.IP_RG,    "Read generation en cours",                 true,  250, StateCategories.IP, true,  FG_RG,   Run, ReadSet),
//			F_RG     = newState(StateNames.F_RG,     "Read generation terminée",                 true,  299, StateCategories.F,  false, FG_RG,   Run, ReadSet),
//
//			IW_VQC   = newState(StateNames.IW_VQC,   "EVAL. QC en attente",                      true,  650, StateCategories.IW, true,  FG_VQC,  ReadSet),
//			IP_VQC   = newState(StateNames.IP_VQC,   "EVAL. QC en cours",                        true,  675, StateCategories.IP, true,  FG_VQC,  ReadSet),
//			F_VQC    = newState(StateNames.F_VQC,    "EVAL. QC terminée",                        true,  699, StateCategories.F,  false, FG_VQC,  ReadSet),
//
//			IW_BA    = newState(StateNames.IW_BA,    "Analyse BA en attente",                    true,  700, StateCategories.IW, true,  FG_BA,   ReadSet),
//			IP_BA    = newState(StateNames.IP_BA,    "Analyse BA en cours",                      true,  750, StateCategories.IP, true,  FG_BA,   ReadSet, Analysis),
//			F_BA     = newState(StateNames.F_BA,     "Analyse BA terminée",                      true,  799, StateCategories.F,  false, FG_BA,   ReadSet, Analysis),
//
//			IW_TF    = newState(StateNames.IW_TF,    "Transfert CCRT en attente",                true, 1101, StateCategories.IW, true,  FG_TF,   ReadSet),
//			IP_TF    = newState(StateNames.IP_TF,    "Transfert CCRT en cours",                  true, 1102, StateCategories.IP, true,  FG_TF,   ReadSet),
//			F_TF     = newState(StateNames.F_TF,     "Transfert CCRT terminé",                   true, 1103, StateCategories.F,  false, FG_TF,   ReadSet),
//			FE_TF    = newState(StateNames.FE_TF,    "Transfert CCRT en echec",                  true, 1103, StateCategories.F,  true,  FG_TF,   ReadSet),
//
//			IW_VBA   = newState(StateNames.IW_VBA,   "EVAL. Analyse BA en attente",              true,  800, StateCategories.IW, true,  FG_VBA,  ReadSet),
//			F_VBA    = newState(StateNames.F_VBA,    "EVAL. Analyse BA terminée",                true,  899, StateCategories.F,  false, FG_VBA,  ReadSet),
//
//			V_SUB    = newState(StateNames.V_SUB,    "Soumission Validée utilisateur",           true, 2000, StateCategories.IW, true,  FG_NONE, SRASubmission, SRAStudy, SRASample, SRAExperiment),
//			IW_SUB   = newState(StateNames.IW_SUB,   "Soumission en attente",                    true, 2001, StateCategories.IW, true,  FG_NONE, SRASubmission, SRAStudy, SRASample, SRAExperiment),
//			IP_SUB   = newState(StateNames.IP_SUB,   "Soumission en cours",                      true, 2002, StateCategories.IP, true,  FG_NONE, SRASubmission, SRAStudy, SRASample, SRAExperiment),
//			F_SUB    = newState(StateNames.F_SUB,    "Soumission terminée",                      true, 2003, StateCategories.F,  true,  FG_NONE, SRASubmission, SRAStudy, SRASample, SRAExperiment),
//			FE_SUB   = newState(StateNames.FE_SUB,   "Soumission en echec",                      true, 2004, StateCategories.F,  true,  FG_NONE, SRASubmission, SRAStudy, SRASample, SRAExperiment),
//			N_R      = newState(StateNames.N_R,      "Soumission pour release à New",            true, 2006, StateCategories.N,  true,  FG_NONE, SRASubmission, SRAStudy),
//			IW_SUB_R = newState(StateNames.IW_SUB_R, "Soumission pour release study en attente", true, 2007, StateCategories.IW, true,  FG_NONE, SRASubmission, SRAStudy),
//			IP_SUB_R = newState(StateNames.IP_SUB_R, "Soumission pour release study en cours",   true, 2008, StateCategories.IP, true,  FG_NONE, SRASubmission, SRAStudy),
//			FE_SUB_R = newState(StateNames.FE_SUB_R, "Soumission pour release study en echec",   true, 2009, StateCategories.F,  true,  FG_NONE, SRASubmission, SRAStudy),
//		};
//	}

//	private static State newState(String code, String name, boolean active,	int position, StateCategory category, boolean display, String functionnalGroup, ObjectType... objTypes) {
//		State s = new State();
//		s.code             = code;
//		s.name             = name;
//		s.active           = active;
//		s.position         = position;
//		s.category         = category;
//		s.objectTypes      = Arrays.asList(objTypes);
//		s.display          = display;
//		s.functionnalGroup = functionnalGroup;
//		return s;
//	}

	/**
	 * States available for object type X are defined in corresponding XStateNames.
	 */
	static {

		// State functional groups
		FG_BA   = "BA";
		FG_F    = "F";
		FG_N    = "N";
		FG_NONE = null;
		FG_QC   = "QC";
		FG_RG   = "RG";
		FG_S    = "S";
		FG_TF   = "TF";
		FG_V    = "V";
		FG_VBA  = "VBA";
		FG_VQC  = "VQC";

		values = new State[] {
			A        = newState(StateNames.A,        "Disponible",                               true, 1000, StateCategories.N,  true,  FG_F   ),
			UA       = newState(StateNames.UA,       "Indisponible",                             true, 1001, StateCategories.N,  true,  FG_F   ),
			F        = newState(StateNames.F,        "Terminé",                                  true, 1000, StateCategories.F,  true,  FG_F   ),
			
			IW_U     = newState(StateNames.IW_U,     "Déclaré",                                  true, 450, StateCategories.N,  true,  FG_NONE   ),
			FE       = newState(StateNames.FE,       "Abandonné",                                true, 1050, StateCategories.F,  true,  FG_NONE   ),

			IW_QC    = newState(StateNames.IW_QC,    "Contrôle qualité en attente",              true,  400, StateCategories.IW, true,  FG_QC  ),
			IP_QC    = newState(StateNames.IP_QC,    "Contrôle qualité en cours",                true,  450, StateCategories.IP, true,  FG_QC  ),
			F_QC     = newState(StateNames.F_QC,     "Contrôle qualité terminé",                 true,  500, StateCategories.F,  false, FG_QC  ),
			IW_V     = newState(StateNames.IW_V,     "Evaluation en attente",                    true,  800, StateCategories.IW, true,  FG_V   ),
			IP_V     = newState(StateNames.IP_V,     "Evaluation en cours",                      true,  825, StateCategories.IP, true,  FG_V   ),
			F_V      = newState(StateNames.F_V,      "Evaluation terminée",                      true,  849, StateCategories.F,  true,  FG_V   ),

			IW_C     = newState(StateNames.IW_C,     "En attente de Container",                  true, -100, StateCategories.IW, true,  FG_NONE),
			N        = newState(StateNames.N,        "Nouveau",                                  true,    0, StateCategories.N,  true,  FG_N   ),
			IP       = newState(StateNames.IP,       "En cours",                                 true,  500, StateCategories.IP, true,  FG_NONE),
			IW_P     = newState(StateNames.IW_P,     "En attente de Processus",                  true,  100, StateCategories.IW, true,  FG_NONE),
			IW_E     = newState(StateNames.IW_E,     "Expérience en attente",                    true,  200, StateCategories.IW, true,  FG_NONE),
			IU       = newState(StateNames.IU,       "En cours d'utilisation",                   true,  250, StateCategories.IP, true,  FG_NONE),
			IW_D     = newState(StateNames.IW_D,     "Dispatch en attente",                      true,  300, StateCategories.IW, true,  FG_NONE),
			IS       = newState(StateNames.IS,       "En stock",                                 true, 1000, StateCategories.N,  true,  FG_NONE),
			A_TM     = newState(StateNames.A_TM,     "Disponible transformation",                true,  900, StateCategories.N,  true,  FG_NONE),
			A_QC     = newState(StateNames.A_QC,     "Disponible controle qualité",              true,  901, StateCategories.N,  true,  FG_NONE),
			A_PF     = newState(StateNames.A_PF,     "Disponible purif",                         true,  902, StateCategories.N,  true,  FG_NONE),
			A_TF     = newState(StateNames.A_TF,     "Disponible transfert",                     true,  903, StateCategories.N,  true,  FG_NONE),

			IP_S     = newState(StateNames.IP_S,     "Séquençage en cours",                      true,  150, StateCategories.IP, true,  FG_S   ),
			F_S      = newState(StateNames.F_S,      "Séquençage terminé",                       true,  195, StateCategories.F,  false, FG_S   ),
			FE_S     = newState(StateNames.FE_S,     "Séquençage en échec",                      true,  199, StateCategories.F,  true,  FG_S   ),

			IW_RG    = newState(StateNames.IW_RG,    "Read generation en attente",               true,  200, StateCategories.IW, true,  FG_RG  ),
			IP_RG    = newState(StateNames.IP_RG,    "Read generation en cours",                 true,  250, StateCategories.IP, true,  FG_RG  ),
			F_RG     = newState(StateNames.F_RG,     "Read generation terminée",                 true,  299, StateCategories.F,  false, FG_RG  ),

			IW_VQC   = newState(StateNames.IW_VQC,   "EVAL. QC en attente",                      true,  650, StateCategories.IW, true,  FG_VQC ),
			IP_VQC   = newState(StateNames.IP_VQC,   "EVAL. QC en cours",                        true,  675, StateCategories.IP, true,  FG_VQC ),
			F_VQC    = newState(StateNames.F_VQC,    "EVAL. QC terminée",                        true,  699, StateCategories.F,  false, FG_VQC ),

			IW_BA    = newState(StateNames.IW_BA,    "Analyse BA en attente",                    true,  700, StateCategories.IW, true,  FG_BA  ),
			IP_BA    = newState(StateNames.IP_BA,    "Analyse BA en cours",                      true,  750, StateCategories.IP, true,  FG_BA  ),
			F_BA     = newState(StateNames.F_BA,     "Analyse BA terminée",                      true,  799, StateCategories.F,  false, FG_BA  ),
				
			IW_TF    = newState(StateNames.IW_TF,    "Transfert CCRT en attente",         true, 1101, StateCategories.IW, true,  FG_TF  ),
			IP_TF    = newState(StateNames.IP_TF,    "Transfert CCRT en cours",                  true, 1102, StateCategories.IP, true,  FG_TF  ),		
			F_TF     = newState(StateNames.F_TF,     "Transfert CCRT terminé",                   true, 1103, StateCategories.F,  false, FG_TF  ),	
			FE_TF    = newState(StateNames.FE_TF,    "Transfert CCRT en echec",                  true, 1103, StateCategories.F,  true,  FG_TF  ),

			IW_VBA   = newState(StateNames.IW_VBA,   "EVAL. Analyse BA en attente",              true,  800, StateCategories.IW, true,  FG_VBA ),
			F_VBA    = newState(StateNames.F_VBA,    "EVAL. Analyse BA terminée",                true,  899, StateCategories.F,  false, FG_VBA ),
				
//			NONE        = newState(StateNames.NONE,  	    "Aucune soumission associée",                             true, 2000, StateCategories.N,  true,  FG_NONE),
//			SUB_N       = newState(StateNames.SUB_N,        "Nouvelle soumission",                                    true, 2001, StateCategories.N,  true,  FG_NONE),
//			SUB_V       = newState(StateNames.SUB_V,        "Soumission Validée utilisateur",                         true, 2002, StateCategories.N,  true,  FG_NONE),		
//			SUB_SRD_IW  = newState(StateNames.SUB_SRD_IW,   "Soumission en cours(attente envoie des données brutes)", true, 2003, StateCategories.IW, true,  FG_NONE),
//			SUB_SRD_IP  = newState(StateNames.SUB_SRD_IP,   "Soumission en cours(envoie des données brutes)",         true, 2004, StateCategories.IP, true,  FG_NONE),
//			SUB_SRD_F   = newState(StateNames.SUB_SRD_F,    "Soumission en cours(fin envoie des données brutes)",     true, 2005, StateCategories.F,  true,  FG_NONE),
//			SUB_SRD_FE  = newState(StateNames.SUB_SRD_FE,   "Soumission en cours(echec envoie des données brutes)",   true, 2006, StateCategories.F,  true,  FG_NONE),
//			SUB_SMD_IW  = newState(StateNames.SUB_SMD_IW,   "Soumission en cours(attente envoie des métadonnées)",    true, 2007, StateCategories.F,  true,  FG_NONE),
//			SUB_SMD_IP  = newState(StateNames.SUB_SMD_IP,   "Soumission en cours(envoie des métadonnées)",            true, 2008, StateCategories.IW, true,  FG_NONE),
//			SUB_SMD_F   = newState(StateNames.SUB_SMD_F,    "Soumission en cours(fin envoie des métadonnées)",        true, 2010, StateCategories.IP, true,  FG_NONE),
//			SUB_SMD_FE  = newState(StateNames.SUB_SMD_FE,   "Soumission en cours(echec envoie des métadonnées)",      true, 2011, StateCategories.F,  true,  FG_NONE),
//			SUB_F       = newState(StateNames.SUB_F,        "Soumission terminée",                                    true, 2012, StateCategories.F,  true,  FG_NONE),
//			SUB_FE      = newState(StateNames.SUB_FE,       "Soumission en echec",                                    true, 2013, StateCategories.F,  true,  FG_NONE),
//
//			SUBU_N       = newState(StateNames.SUBU_N,      "Nouvelle mise à jour",                                   true, 2020, StateCategories.N,  true,  FG_NONE),
//			SUBU_V       = newState(StateNames.SUBU_V,      "Mise à jour validée",                                    true, 2021, StateCategories.IW, true,  FG_NONE),
//			SUBU_SMD_IW  = newState(StateNames.SUBU_SMD_IW, "Mise à jour en attente",                                 true, 2022, StateCategories.IW, true,  FG_NONE),
//			SUBU_SMD_IP  = newState(StateNames.SUBU_SMD_IP, "Mise à jour en cours",                                   true, 2023, StateCategories.IP, true,  FG_NONE),
//			SUBU_SMD_F   = newState(StateNames.SUBU_SMD_F,  "Mise à jour en cours",                                   true, 2024, StateCategories.F,  true,  FG_NONE),
//			SUBU_SMD_FE  = newState(StateNames.SUBU_SMD_FE, "Mise à jour en echec",                                   true, 2025, StateCategories.F,  true,  FG_NONE),
//			SUBU_FE      = newState(StateNames.SUBU_FE,	    "Update en echec",                                        true, 2026, StateCategories.F,  true,  FG_NONE),
//			
//			SUBR_N       = newState(StateNames.SUBR_N,      "Nouvelle soumission pour release",                       true, 2040, StateCategories.N,  true,  FG_NONE),
//			SUBR_SMD_IW  = newState(StateNames.SUBR_SMD_IW, "Release en attente",                                     true, 2041, StateCategories.IW, true,  FG_NONE),
//			SUBR_SMD_IP  = newState(StateNames.SUBR_SMD_IP, "Release en cours",                                       true, 2042, StateCategories.IP, true,  FG_NONE),
//			SUBR_SMD_F   = newState(StateNames.SUBR_SMD_F,  "Release en cours",                                       true, 2043, StateCategories.F,  true,  FG_NONE),
//			SUBR_SMD_FE  = newState(StateNames.SUBR_SMD_FE, "Release en echec",                                       true, 2044, StateCategories.F,  true,  FG_NONE),
//			SUBR_FE      = newState(StateNames.SUBR_FE,	"Release en echec",                                       true, 2045, StateCategories.F,  true,  FG_NONE),

			NONE        = newState(StateNames.NONE,  	    "Pas associé à une soumission",                             true, 2000, StateCategories.N,  true,  FG_NONE),
			SUB_N       = newState(StateNames.SUB_N,        "create - new",                                             true, 2001, StateCategories.N,  true,  FG_NONE),
			SUB_V       = newState(StateNames.SUB_V,        "create - validé utilisateur",                              true, 2002, StateCategories.N,  true,  FG_NONE),		
			SUB_SRD_IW  = newState(StateNames.SUB_SRD_IW,   "create - en cours (en attente envoie des données brutes)", true, 2003, StateCategories.IW, true,  FG_NONE),
			SUB_SRD_IP  = newState(StateNames.SUB_SRD_IP,   "create - en cours (envoie des données brutes)",            true, 2004, StateCategories.IP, true,  FG_NONE),
			SUB_SRD_F   = newState(StateNames.SUB_SRD_F,    "create - en cours (fin envoie des données brutes)",        true, 2005, StateCategories.F,  true,  FG_NONE),
			SUB_SRD_FE  = newState(StateNames.SUB_SRD_FE,   "create - echec (echec envoie des données brutes)",         true, 2006, StateCategories.F,  true,  FG_NONE),
			SUB_SMD_IW  = newState(StateNames.SUB_SMD_IW,   "create - en cours (en attente envoie des métadonnées)",    true, 2007, StateCategories.F,  true,  FG_NONE),
			SUB_SMD_IP  = newState(StateNames.SUB_SMD_IP,   "create - en cours (envoie des métadonnées)",               true, 2008, StateCategories.IW, true,  FG_NONE),
			SUB_SMD_F   = newState(StateNames.SUB_SMD_F,    "create - en cours (fin envoie des métadonnées)",           true, 2010, StateCategories.IP, true,  FG_NONE),
			SUB_SMD_FE  = newState(StateNames.SUB_SMD_FE,   "create - echec (echec envoie des métadonnées)",            true, 2011, StateCategories.F,  true,  FG_NONE),
			SUB_F       = newState(StateNames.SUB_F,        "terminé",                                                  true, 2012, StateCategories.F,  true,  FG_NONE),
			SUB_FE      = newState(StateNames.SUB_FE,       "create - echec (echec recuperation des AC)",               true, 2013, StateCategories.F,  true,  FG_NONE),

			SUBU_N       = newState(StateNames.SUBU_N,      "update - new",                                             true, 2020, StateCategories.N,  true,  FG_NONE),
			SUBU_V       = newState(StateNames.SUBU_V,      "update - validé utilisateur",                              true, 2021, StateCategories.IW, true,  FG_NONE),
			SUBU_SMD_IW  = newState(StateNames.SUBU_SMD_IW, "update - en cours (en attente envoie des métadonnées)",    true, 2022, StateCategories.IW, true,  FG_NONE),
			SUBU_SMD_IP  = newState(StateNames.SUBU_SMD_IP, "update - en cours (envoie des métadonnées)",               true, 2023, StateCategories.IP, true,  FG_NONE),
			SUBU_SMD_F   = newState(StateNames.SUBU_SMD_F,  "update - en cours (fin envoie des métadonnées)",           true, 2024, StateCategories.F,  true,  FG_NONE),
			SUBU_SMD_FE  = newState(StateNames.SUBU_SMD_FE, "update - echec (echec envoie des métadonnées)",            true, 2025, StateCategories.F,  true,  FG_NONE),
			SUBU_FE      = newState(StateNames.SUBU_FE,	    "update - echec (echec recuperation des AC)",               true, 2026, StateCategories.F,  true,  FG_NONE),
			
			SUBR_N       = newState(StateNames.SUBR_N,      "release - new",                                            true, 2040, StateCategories.N,  true,  FG_NONE),
			SUBR_SMD_IW  = newState(StateNames.SUBR_SMD_IW, "release - en cours (en attente envoie des métadonnées)",   true, 2041, StateCategories.IW, true,  FG_NONE),
			SUBR_SMD_IP  = newState(StateNames.SUBR_SMD_IP, "release - en cours (envoie des métadonnées)",              true, 2042, StateCategories.IP, true,  FG_NONE),
			SUBR_SMD_F   = newState(StateNames.SUBR_SMD_F,  "release - en cours (fin envoie des métadonnées)",          true, 2043, StateCategories.F,  true,  FG_NONE),
			SUBR_SMD_FE  = newState(StateNames.SUBR_SMD_FE, "release - echec (echec envoie des métadonnées)",           true, 2044, StateCategories.F,  true,  FG_NONE),
			SUBR_FE      = newState(StateNames.SUBR_FE,	    "release - echec (echec recuperation des AC)",              true, 2045, StateCategories.F,  true,  FG_NONE),

		};
	}

	private static void append(List<ObjectType> l, String[] states, String state, ObjectType t) {
		for (final String s : states) {
			if (s.equals(state)) {
				l.add(t);
				return;
			}
		}
	}

	/**
	 * Searches for types associated to states.
	 * @param stateName states name to find types for
	 * @return          object types that use/define this state
	 */
	private static List<ObjectType> getObjectTypesForStateCode(String stateName) {
		final List<ObjectType> types = new ArrayList<>();
		append(types, ProjectStateNames.values,          stateName, Project);
		append(types, ProcessStateNames.values,          stateName, Process);
		append(types, SampleStateNames.values,           stateName, Sample );
		append(types, ContainerStateNames.values,        stateName, Container);
		append(types, InstrumentStateNames.values,       stateName, Instrument);
		append(types, ReagentStateNames.values,          stateName, Reagent);
		append(types, ExperimentStateNames.values,       stateName, Experiment);
		append(types, ImportStateNames.values,           stateName, Import);
		append(types, RunStateNames.values,              stateName, Run);
		append(types, TreatmentStateNames.values,        stateName, Treatment);
		append(types, ReadSetStateNames.values,          stateName, ReadSet);
		append(types, AnalysisStateNames.values,         stateName, Analysis);
		append(types, SRASubmissionStateNames.values,    stateName, SRASubmission);
		append(types, SRAConfigurationStateNames.values, stateName, SRAConfiguration);
		append(types, SRAStudyStateNames.values,         stateName, SRAStudy);
		append(types, SRASampleStateNames.values,        stateName, SRASample);
		append(types, SRAExperimentStateNames.values,    stateName, SRAExperiment);
		append(types, ReagentReceptionStateNames.values, stateName, ReagentReception);
		return types;
	}

	// Populate object types using the states by type definitions.
	private static State newState(String code, String name, boolean active, int position, StateCategory category, boolean display, String functionnalGroup) {
		final State s = new State();
		s.code             = code;
		s.name             = name;
		s.active           = active;
		s.position         = position;
		s.category         = category;
		s.objectTypes      = getObjectTypesForStateCode(code);
		s.display          = display;
		s.functionnalGroup = functionnalGroup;
		return s;
	}

}
