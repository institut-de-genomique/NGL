//import java.text.SimpleDateFormat;
//import java.util.Date;
//import java.util.Locale;
//
//import play.Application;
//import play.data.format.Formatters;
//
//
//// public class Global extends GlobalSettings {
//class UNUSED_Global { // extends GlobalSettings {
//	
//	// Should use the same start sequence as NGL-SQ.
//	
//	// @Override
//	public void onStart(Application app) {
//		Logger.info("NGL-BI has started");		   	
//		//Formatters.register(Date.class,new DateFormatter("yyyy-MM-dd"));
//		
//		/* Started using the module DI
//		Logger.info("Load knowledge base");
//		
//		
//		try {
//			RulesServices6.getInstance();
//		} catch (Throwable e) {
//			Logger.error("Error Load knowledge base");
//			e.printStackTrace();
//			//Shutdown application
//			Play.stop(app.getWrappedApplication());
//		}
//		*/
//		/*
//		RulesServices rulesServices = new RulesServices();
//		try {
//			rulesServices.buildKnowledgeBase();
//			
//		} catch (RulesException e) {
//			Logger.error("Error Load knowledge base");
//			e.printStackTrace();
//			//Shutdown application
//			Play.stop();
//		}
//		*/
//		Logger.error("Start NGL-BI done");
//	}  
//
//	
//
//
//	// @Override
//	public void onStop(Application app) {
//		Logger.info("NGL-BI shutdown...");
//	}  
//
//	
//	
//	/*
//	// @Override
//	public Action onRequest(Request request, Method actionMethod) {
//		//if(Integer.valueOf(request.getHeader("Content-Length")).intValue() < (100*1024) ){
//		if(!request.uri().contains("/authentication")){
//			Logger.debug("Request: "+request.body().toString());
//		}
//		//}
//		//return new fr.cea.ig.authentication.Authenticate();
//		return super.onRequest(request, actionMethod);
//	}
//
//	
//	@Override
//    public <A> A getControllerInstance(Class<A> controllerClass) throws Exception {
//      try{
//		return Spring.getBeanOfType(controllerClass);
//      } catch (NoSuchElementException e){
//    	  return super.getControllerInstance(controllerClass);
//      }
//	}
//*/
//
//	/**
//     * Formatter for <code>java.util.Date</code> values.
//     * Override the default formatter to manage the date in milliseconds from 1970
//     */
//
//    public static class DateFormatter extends Formatters.SimpleFormatter<Date> {
//        
//        private final String pattern;
//        
//        /**
//         * Creates a date formatter.
//         *
//         * @param pattern date pattern, as specified for {@link SimpleDateFormat}.
//         */
//        public DateFormatter(String pattern) {
//            this.pattern = pattern;
//        }
//        
//        /**
//         * Binds the field - constructs a concrete value from submitted data.
//         *
//         * @param text the field text
//         * @param locale the current <code>Locale</code>
//         * @return a new value
//         */
//        public Date parse(String text, Locale locale) throws java.text.ParseException {
//            if(text == null || text.trim().isEmpty()) {
//                return null;
//            }
//            try{
//            	Long l = Long.valueOf(text);
//            	return new Date(l);
//            }catch(NumberFormatException e){
//            	SimpleDateFormat sdf = new SimpleDateFormat(pattern, locale);
//                sdf.setLenient(false);  
//                return sdf.parse(text);
//            }
//            
//            
//        }
//        
//        /**
//         * Unbinds this fields - converts a concrete value to a plain string.
//         *
//         * @param value the value to unbind
//         * @param locale the current <code>Locale</code>
//         * @return printable version of the value
//         */
//        public String print(Date value, Locale locale) {
//            if(value == null) {
//                return "";
//            }
//            return new SimpleDateFormat(pattern, locale).format(value);
//        }
//        
//    }
//}