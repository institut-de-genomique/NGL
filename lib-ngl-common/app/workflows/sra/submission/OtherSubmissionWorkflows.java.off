//package workflows.sra.submission;
//
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//import javax.inject.Inject;
//import javax.inject.Singleton;
//
//import org.mongojack.DBQuery;
//import org.mongojack.DBUpdate;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//
//import fr.cea.ig.MongoDBDAO;
//import models.laboratory.common.description.ObjectType;
//import models.laboratory.common.instance.State;
//import models.sra.submit.common.instance.Submission;
//import models.sra.submit.util.SraException;
//import models.utils.InstanceConstants;
////import play.Logger;
//import validation.ContextValidation;
//import validation.common.instance.CommonValidationHelper;
//import workflows.Workflows;
//
///**
// * 
// * @author sgas
// *
// * @param <K1>   cle1
// * @param <K2>   cle2
// * @param <V>    valeur de type transition associée à la paire de cles
// */
////class DoubleKeyMap < K1, K2, V > {
////	private Map < K1, Map<K2, V> > map = new HashMap<>();
////	
////	public void put(K1 k1, K2 k2 , V v){
////		Map<K2, V> tmp = map.get(k1);
////		if (tmp == null) {
////			tmp = new HashMap<>();
////			map.put(k1, tmp);
////		}
////		tmp.put(k2, v);
////	}
////	public V get(K1 k1, K2 k2) {
////		Map<K2, V> tmp = map.get(k1);
////		if (tmp == null) {
////			return null;
////		}
////		return tmp.get(k2);
////	}
////}
//
////@Service
//@Singleton
//public class OtherSubmissionWorkflows extends TransitionWorkflows<Submission> {
//	
//	private static final play.Logger.ALogger logger = play.Logger.of(SubmissionWorkflows.class);
//
////	public static final String IW_SUB_R = "IW-SUB-R";
//	
////	enum StateCode {
////		ReleaseEnAttenteBirds       ("IW-SUB-R"),
////		ReleaseEnCoursBirds         ("IP-SUB-R"),
////		SoumissionEnAttenteBirds    ("IW-SUB"), 
////		SoumissionEnCoursBirds      ("IP-SUB"),
////		SoumissionOk                ("F-SUB");
////		
////		private final String code;
////		
////		StateCode (String code) {
////			this.code = code;
////		}
////		
////		public static StateCode fromString(String code) {
////			for (StateCode s : values()) {
////				if (s.code.equals(code)) {
////					return s;
////				}
////			}
////			throw new RuntimeException("stateCode non trouvé " + code);
////		}
////		public String getCode() {
////			return this.code;
////		}	
////	}
////	
//	//@Autowired
//	private SubmissionWorkflowsHelper submissionWorkflowsHelper;
//
//	@Inject
//	public OtherSubmissionWorkflows(SubmissionWorkflowsHelper submissionWorkflowsHelper) {
//		super();
//		this.submissionWorkflowsHelper = submissionWorkflowsHelper;
//	}
//	
////	public interface APSR {
////		void run (SubmissionWorkflows self, ContextValidation validation, Submission submission);
////	}
////	
////	public interface Fonction <A,B> {
////		B applique(A x);
////	}
////	
////	public static class UpperCase implements Fonction <String, String> {
////		public String applique(String x) {
////			return x.toUpperCase();
////		}
////	}
//	
////	public static class Tools {
////		
////		public static List<String> xxx(List<String> l) {
////			return map(l,new UpperCase());
////		}
////		
////		public static List<String> anonimous(List<String> l) {
////			return map(l,new Fonction<String, String>(){
////				public String applique(String x) {
////					return x.toLowerCase();
////				}
////			});
////		}
////
////
////		public static <A,B>List<B> map(List<A> l, Fonction <A,B> f) {
////			List<B> bs = new ArrayList<>();
////			for (A i : l) {
////				bs.add(f.applique(i));
////			}
////			return bs;
////		}
////		
////	}
//	
////	static class IP_SUB_R__F_SUB implements SubmissionTransition {
////
////		@Override
////		public void execute(SubmissionWorkflows self, ContextValidation validation, Submission submission,
////				 State nextState) {
////			logger.debug("call update submission Release");
////			self.submissionWorkflowsHelper.updateSubmissionRelease(submission);	
////		}
////		@Override
////		public void success(SubmissionWorkflows self, ContextValidation validation, Submission submission,State nextState) {
////			self.submissionWorkflowsHelper.updateSubmissionChildObject(submission, validation);
////		}
////		@Override
////		public void error(SubmissionWorkflows self, ContextValidation validation, Submission submission,State nextState) {
////			logger.error("Problem on SubmissionWorkflow.applyErrorPostStateRules : " + validation.errors.toString());
////		}
////	}
////	
////	static class F_SUB__IW_SUB_R implements SubmissionTransition {
////
////		
////	}
////	
////	static class V_SUB__IW_SUB implements SubmissionTransition {
////
////	
////	}
//	
////	static final IP_SUB__F_SUB IP_SUB__F_SUB = new IP_SUB__F_SUB();
////	
////	
////	static class IP_SUB__F_SUB implements SubmissionTransition {
////
////		@Override
////		public void execute(SubmissionWorkflows self, ContextValidation validation, Submission submission,
////				 State nextState) {
////			logger.debug("call update submission Release");
////			self.submissionWorkflowsHelper.updateSubmissionForDates(submission);
////		}
////		
////	}
////	
//	//private static final DoubleKeyMap<String, String, APSR> dkm;
//
//	private DoubleKeyMap<String, String, Transition<Submission>> trs;
//	
//	public static final String 
//		N        = "N",
//		N_R      = "NR",
//		V_SUB    = "V-SUB",
//		IW_SUB	 = "IW-SUB",
//		IP_SUB   = "IP_SUB",
//		FE_SUB   = "FE-SUB",
//		F_SUB    = "F-SUB",
//		IW_SUB_R = "IW-SUB-R",
//		IP_SUB_R = "IP-SUB-R",
//		FE_SUB_R = "FE_SUB-R";
//	
//	public class BasicTransition implements SubmissionTransition {
//		public void execute(ContextValidation contextValidation, Submission submission, State nextState) {}
//		public void success(ContextValidation contextValidation, Submission submission, State nextState) {
//			submissionWorkflowsHelper.updateSubmissionChildObject(submission, contextValidation);
//
//		}
//		public void error(ContextValidation contextValidation, Submission object, State nextState) {}		
//	}
//
//	OtherSubmissionWorkflows() {
////		dkm = new DoubleKeyMap<>();
////		//dkm.put(StateCode.ReleaseEnAttente,StateCode.ReleaseEnCoursBirds, new IP_SUB_R__F_SUB());
////		dkm.put("IP-SUB-R", "F-SUB", new IP_SUB_R__F_SUB());
////		dkm.put("F-SUB", IW_SUB_R, new F_SUB__IW_SUB_R());
////		dkm.put("V-SUB","IW-SUB", new V_SUB__IW_SUB());
////		dkm.put("IP-SUB", "F-SUB", IP_SUB__F_SUB);
////		trs = new DoubleKeyMap<>();
////		trs.put("IP-SUB-R", "F-SUB", new IP_SUB_R__F_SUB());
////		trs.put("F-SUB", IW_SUB_R, new F_SUB__IW_SUB_R());
////		trs.put("V-SUB","IW-SUB", new V_SUB__IW_SUB());
////		trs.put("IP-SUB", "F-SUB", IP_SUB__F_SUB);
////		trs.put("IP-SUB-R", "F-SUB", new Transition3<Submission>(
////				(self, validation, submission, nextState)->{
////					self.submissionWorkflowsHelper.createDirSubmission(submission, validation);
////				},
////				(self, validation, submission, nextState)->{
////					self.submissionWorkflowsHelper.updateSubmissionChildObject(submission, validation);
////				},
////				(self, validation, submission, nextState)->{
////					logger.error("Problem on SubmissionWorkflow.applyErrorPostStateRules : "+validation.errors.toString());
////				}));
//		
//		
////		trs.from(N)
////             .to(V_SUB, (ctx,sub,st) -> { })
////             .to(IW_SUB,(ctx,sub,st) -> {
////        	                 submissionWorkflowsHelper.activationPrimarySubmission(contextValidation, submission);     
////                        })
////             .to(IP_SUB, new SubmissionTransition() {
////     			            @Override
////     			            public void execute(ContextValidation contextValidation, Submission submission, State nextState) {}
////     			            @Override
////     			            public void success(ContextValidation contextValidation, Submission submission, State nextState) {}
////     			            @Override
////     			            public void error(ContextValidation contextValidation, Submission object, State nextState) {}		
////    		             });)
//
//		//--------------------------------------------------
//		// workflow d'une premiere soumission des données :
//		//--------------------------------------------------
//		// implementation d'une sous-classe de SubmissionTransition anonyme et interne 
//		// classe interne qui existera au sein de l'instance environnemental SubmissionWorkflows
//		trs.put(N     , V_SUB   , new BasicTransition() {		
//			@Override public void success(ContextValidation contextValidation, Submission submission, State nextState) {
//				submissionWorkflowsHelper.updateSubmissionChildObject(submission, contextValidation);
//			}
//		});				
//		trs.put(V_SUB , IW_SUB  , new BasicTransition() {
//			@Override public void execute(ContextValidation contextValidation, Submission submission, State nextState) {
//				submissionWorkflowsHelper.activationPrimarySubmission(contextValidation, submission);
//			}			
//			@Override public void success(ContextValidation contextValidation, Submission submission, State nextState) {
//				submissionWorkflowsHelper.updateSubmissionChildObject(submission, contextValidation);
//			}
//		});
//		trs.put(IW_SUB, IP_SUB  , new BasicTransition() {
//			@Override public void success(ContextValidation contextValidation, Submission submission, State nextState) {
//				submissionWorkflowsHelper.updateSubmissionChildObject(submission, contextValidation);
//			}
//		});			
//		trs.put(IP_SUB, F_SUB   , new BasicTransition() {	
//			@Override public void success(ContextValidation contextValidation, Submission submission, State nextState) {
//				submissionWorkflowsHelper.updateSubmissionChildObject(submission, contextValidation);
//				submissionWorkflowsHelper.updateSubmissionForDates(submission);
//
//			}
//		});	
//		trs.put(IP_SUB, FE_SUB  , new BasicTransition() {
//			@Override public void success(ContextValidation contextValidation, Submission submission, State nextState) {
//				submissionWorkflowsHelper.updateSubmissionChildObject(submission, contextValidation);
//			}
//		});
//		trs.put(FE_SUB, IW_SUB  , new BasicTransition() {
//			@Override public void success(ContextValidation contextValidation, Submission submission, State nextState) {
//				submissionWorkflowsHelper.updateSubmissionChildObject(submission, contextValidation);
//			}			
//		});
//		
//		
//		//-------------------------------------------------------
//		// workflow d'une soumission  pour release des données :
//		//-------------------------------------------------------
//		trs.put(N_R   , IW_SUB_R, new BasicTransition() {
//			@Override public void execute(ContextValidation contextValidation, Submission submission, State nextState) {
//				submissionWorkflowsHelper.createDirSubmission(submission, contextValidation);
//			}
//			@Override public void success(ContextValidation contextValidation, Submission submission, State nextState) {
//				submissionWorkflowsHelper.updateSubmissionChildObject(submission, contextValidation);
//			}
//		});	
//		
//		trs.put(IW_SUB_R, IP_SUB_R, new BasicTransition() {
//			@Override public void success(ContextValidation contextValidation, Submission submission, State nextState) {
//				submissionWorkflowsHelper.updateSubmissionChildObject(submission, contextValidation);
//			}
//			@Override public void error(ContextValidation contextValidation, Submission submission, State nextState) {		
//				submissionWorkflowsHelper.rollbackSubmission(submission, contextValidation);
//			}
//		});			
//		trs.put(IP_SUB_R, F_SUB, new BasicTransition() {
//			@Override public void success(ContextValidation contextValidation, Submission submission, State nextState) {
//				submissionWorkflowsHelper.updateSubmissionRelease(submission);
//				submissionWorkflowsHelper.updateSubmissionChildObject(submission, contextValidation);
//			}
//		});	
//		trs.put(IP_SUB_R, FE_SUB_R, new BasicTransition() {
//			@Override public void success(ContextValidation contextValidation, Submission submission, State nextState) {
//				submissionWorkflowsHelper.updateSubmissionRelease(submission);
//				submissionWorkflowsHelper.updateSubmissionChildObject(submission, contextValidation);
//			}
//		});	
//		
//
//	}
//
//
//	public Transition<Submission> get(String currentStateCode, String nextStateCode) {
//		return trs.get(currentStateCode, nextStateCode);
//	}
//	public ObjectType.CODE getObjectType() {
//		return ObjectType.CODE.SRASubmission;
//	}
//
//
//	@Override
//	public String getCollectionName() {
//		return InstanceConstants.SRA_SUBMISSION_COLL_NAME;
//	}
//	@Override
//	public Class<Submission> getElementClass() {
//		return Submission.class;
//	}
//
//
//}
