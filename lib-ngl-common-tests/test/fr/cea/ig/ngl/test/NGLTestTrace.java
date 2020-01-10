package fr.cea.ig.ngl.test;

import java.util.List;

import fr.cea.ig.TracingMongoDBDAO;
import fr.cea.ig.TracingMongoDBDAO.MongoTracingExceptionBase;
import fr.cea.ig.play.test.DevAppTesting;
import fr.cea.ig.util.function.CC1;
import validation.ValidationContext;

public class NGLTestTrace {
	
	private static final play.Logger.ALogger logger = play.Logger.of(NGLTestTrace.class);
	
	static class TestTraceException extends RuntimeException {
		private static final long serialVersionUID = 1L;
		public TestTraceException(Exception e) {
			super(e);
		}
		public Exception innerException() { return (Exception)getCause(); }
	}
	
//	public static <R> F0<R> runC0(F1<Runnable, R> f, C0 c) throws Exception {
//		return () -> {
//			try {
//				return f.apply(() ->  {
//					try {
//						c.accept();
//					} catch (Exception e) {
//						throw new TestTraceException(e);
//					}
//				});
//			} catch (TestTraceException e) {
//				throw e.innerException();
//			}		
//		};
//	}
//	
//	public static F0<List<TracingMongoDBDAO.Command>> traceRun(C0 c) throws Exception {
////		try {
////			List<TracingMongoDBDAO.Command> commands = TracingMongoDBDAO.run(() -> {
////				try {
////					c.accept();
////				} catch (Exception e) {
////					throw new TestTraceException(e);
////				}
////			});
////			return commands;		
////		} catch (TestTraceException e) {
////			throw e.innerException();
////		}
//		return runC0(TracingMongoDBDAO::run, c);
//	}
//	
//	// This would be better as: (F0<R> -> F0<R>).
//	public static <R> R exRun(F0<R> c) throws Exception {
//		try  {
//			return ValidationContext.errorsAsException(c);
//		} catch (TestTraceException e) {
//			throw e.innerException();
//		}
//	}
//	
//	public static List<TracingMongoDBDAO.Command> run(C0 c) throws Exception {
//		return exRun(traceRun(c));
//	}
//	
//	public static void showTrace(C0 c) throws Exception {
//		try {
//			logger.error("tracing {}", c);
//			List<TracingMongoDBDAO.Command> commands = run(c);
//			showCommands(commands);
//		} catch (MongoTracingException e)  {
//			List<TracingMongoDBDAO.Command> commands = e.getCommands();
//			logger.error("mongo error", e);
//			showCommands(commands);
//			throw (Exception)e.getCause();
//		} catch (Exception e) {
//			logger.error("traced call failed");
//			throw e;
//		} finally {
//			logger.error("tracing done {}", c);
//		}
//	}
//	
//	private static void showCommands(List<TracingMongoDBDAO.Command> commands) {
//		logger.info("showing {} dao layer commands", commands.size());
//		for (TracingMongoDBDAO.Command c : commands)
//			logger.info("command {}", c);
//	}
	
//	public static void run(C0 c) throws Exception {
//		
//	}

	public static <A> CC1<A> ex(CC1<A> cc) {
		return c -> ValidationContext.errorsAsException(() -> cc.accept(c));
	}
	
//	public static <A> CC1<A> dblog(CC1<A> cc) {
//		return c -> {
//			try {
//				System.out.println("# tracing " + c);
//				logger.error("tracing {}", c);
//				try {
//					List<TracingMongoDBDAO.Command> commands = TracingMongoDBDAO.run(() ->  { 
//						try {
//							cc.accept(c);	
//						} catch (Exception e) {
//							throw new TestTraceException(e);
//						}
//					});
//					showCommands(commands);
//				} catch (TestTraceException e) {
//					throw e.innerException();
//				}
//			} catch (MongoTracingException e)  {
//				List<TracingMongoDBDAO.Command> commands = e.getCommands();
//				logger.error("mongo error", e);
//				showCommands(commands);
//				throw (Exception)e.getCause();
//			} catch (Exception e) {
//				logger.error("traced call failed");
//				throw e;
//			} finally {
//				logger.error("tracing done {}", c);
//			}
//		};
//	}
	
	public static <A> CC1<A> dblog(CC1<A> cc) {
		return c -> cc.accept(a -> {
			try {
				logger.error("tracing {}", c);
				try {
					List<TracingMongoDBDAO.Command> commands = TracingMongoDBDAO.run(() ->  { 
						try {
							c.accept(a);
						} catch (Exception e) {
							throw new TestTraceException(e);
						}
					});
					showCommands(commands);
				} catch (TestTraceException e) {
					throw e.innerException();
				}
			} catch (MongoTracingExceptionBase e) {
				List<TracingMongoDBDAO.Command> commands = e.getCommands();
				logger.error("mongo error", e);
				showCommands(commands);
				e.throwCause();
			} catch (Exception e) {
				logger.error("traced call failed {}", e.getClass());
				throw e;
			} finally {
				logger.error("tracing done {}", c);
			}
		});
	}
	
	public static <A> CC1<A> exlog(CC1<A> cc) {
		return ex(dblog(cc));
	}
	
	
	private static void showCommands(List<TracingMongoDBDAO.Command> commands) {
		logger.info("showing {} dao layer commands (session code prefix:{})", commands.size(), DevAppTesting.codePrefix());
		for (TracingMongoDBDAO.Command c : commands)
			logger.info("command : {}", c);
	}

//	public static F0<List<TracingMongoDBDAO.Command>> traceRun(C0 c) throws Exception {
//		try {
//			List<TracingMongoDBDAO.Command> commands = TracingMongoDBDAO.run(() -> {
//				try {
//					c.accept();
//				} catch (Exception e) {
//					throw new TestTraceException(e);
//				}
//			});
//			return commands;		
//		} catch (TestTraceException e) {
//			throw e.innerException();
//		}
//
//	}

}
