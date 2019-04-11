package fr.cea.ig.lfw.controllers.scripts;


import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import play.mvc.Http.RequestBody;
import play.mvc.Result;
import play.mvc.Results;


/**
 * Classe abstraite avec une methode execute lancée avec les parametres parsés et controlés de l'url.
 * La classe Script prend comme parametre une classe de controle et stockage des arguments de l'url.
 * Tous les champs publics de la classe definissent les parametres obligatoires de l'url, qui doivent etre 
 * associes à des valeurs non nulles. Si l'url contient un parametre absent de la classe de controle des 
 * arguments, une erreur est declenchee.
 * <p>
 * La methode execute sera executé au chargement de l'url
 * de la forme: {@literal http://localhost:9000/sra/scripts/run/NameClasseFille?arg1=val1&...}
 *
 * @param <T> argument class
 * 
 * @author sgas
 *
*/
public abstract class Script<T> {
	
	public enum LogLevel {
		None, Debug, Info
	}
	
	private final play.Logger.ALogger logger;
	
	public play.Logger.ALogger getLogger() {
        return logger;
    }

    private final StringBuilder sb;
	
	private boolean windowsLf = false;
	
	public Script() {
		logger = play.Logger.of(getClass());
		sb     = new StringBuilder();
	}
	
	public void println() {
		if (windowsLf)
			sb.append("\r\n");
		else
			sb.append('\n');
	}
	
	public void println(String arg) {
		sb.append(arg);
		println();
		log(arg);
	}	
	
	public void printfln(String format, Object... args ) {
		String arg = String.format(format, args);
		sb.append(arg);
		println();
		log(arg);
	}	
		
	private void log(String s) {
		switch (logLevel()) {
		case None:
			break;
		case Debug : 
			logger.debug(s); 
			break;
		case Info : 
			logger.info(s); 
			break;
		default:
			throw new RuntimeException("unkonw log level " + logLevel());
		}
	}
	
	public LogLevel logLevel() {
		return LogLevel.Debug;
	}	
	
	public Result run() {
		try {
			T t = parser(argClass(), play.mvc.Http.Context.current().request().queryString());
			execute(t, play.mvc.Http.Context.current().request().body());
			return Results.ok(sb.toString());
		} catch (Exception e) {
			sb.append("***************** ERROR ***********************\n");
			//sb.append(e.getMessage());// println(e);
			sb.append(org.apache.commons.lang.exception.ExceptionUtils.getStackTrace(e));
			sb.append("***************** ERROR ***********************\n");
			logger.error(e.getMessage(), e);
			return Results.notFound(sb.toString());			
		}
	}

//	public List<Field>getDeclaredFields(Class<?> c) {
//		List <Field> lf = new ArrayList<>();
//		while(c != Object.class) {
//			for (Field f : c.getDeclaredFields()) {
//				lf.add(f);
//			}
//			c = c.getSuperclass();
//		}
//		return lf;
//	}
//	
	public T parser(Class<T> c, Map<String, String[]> args) throws Exception {
		T t = c.newInstance();
		if (args.isEmpty()) {
			printfln ("Aucun argument dans l'url ?");
		}
		// liste des champs (type Fields) publics de la classe:
		List<Field> listChamps = new ArrayList<>();
		
		for (Field f : c.getFields()) {
			// f isPublic
			if ((f.getModifiers() & Modifier.STATIC)==Modifier.STATIC) {
//				printfln ("Champs statiques ignoré %s", f.getName());
			} else {
				listChamps.add(f);
			}
		}
		Set<String> nameChampsAttendus = new HashSet<>();
		for (Field f : listChamps) {
			nameChampsAttendus.add(f.getName());
//			printfln("champs : %s : ** %s **", f.getName(),f.getType().toString());
//			println("champs '" + f.getName() + "' = '"+ f.getType().toString()+"'");
		}
		printfln("Nombre de champs publics (nombre  de parametres attendus dans url) : %d", nameChampsAttendus.size());
		// test arguments attendus contre arguments url:
		for (String s : nameChampsAttendus) {
			if (!args.containsKey(s)) {
				throw new RuntimeException("Le champs attendu "+ s + " n'est pas dans l'url");
			}
		}
		for (String s : args.keySet()) {
			if (! nameChampsAttendus.contains(s)) {
				throw new RuntimeException("Le champs de l'url "+ s + " n'est pas attendu");
			}
		}
		for (Field f: listChamps) {
			switch (f.getType().toString()) {
			case "class java.lang.String":{
				String[] values = args.get(f.getName());
				if (values==null || StringUtils.isBlank(values[0])) {
					throw new RuntimeException("valeur nulle pour le parametre "+ f.getName());
				}
				if (values.length!=1) {
					throw new RuntimeException("valeur multiple pour le parametre "+ f.getName());
				}
				
				f.set(t, values[0]);
				break;
			}
			case "class [Ljava.lang.String;": {
				String[] values = args.get(f.getName());
				if (values==null) {
					throw new RuntimeException("valeur nulle "+ f.getName());
				}
				for (String value: values) {
					if (StringUtils.isBlank(value)) {
						throw new RuntimeException("valeur nulle pour un des parametres" +f.getName());
					}
				}
				f.set(t, values);
				break;
			}
			case "int": {
				String[] values = args.get(f.getName());
				if (values==null || StringUtils.isBlank(values[0])) {
					throw new RuntimeException("valeur nulle pour le parametre "+ f.getName());
				}
				if (values.length!=1) {
					throw new RuntimeException("valeur multiple pour le parametre "+ f.getName());
				}
				
				f.set(t, Integer.parseInt(values[0]));
				break;
			}
			case "class [I": {
				String[] values = args.get(f.getName());
				if (values==null) {
					throw new RuntimeException("valeur nulle "+ f.getName());
				}
				for (String value: values) {
					if (StringUtils.isBlank(value)) {
						throw new RuntimeException("valeur nulle pour un des parametres" +f.getName());
					}
				}
				
				int[] tab = new int[values.length];
				
				for (int i=0; i<tab.length;i++) {
					tab[i]= Integer.parseInt(values[i]);
				}
				f.set(t, tab);
				break;
			}			
			default:
				throw new RuntimeException("Type "+f.getType().toString() + " non gere");
			}
		}
		return t;
	}

	@SuppressWarnings("unchecked")
	public  Class <T> argClass() {
		Class<?> c = getClass();
		while (c.getSuperclass() != Script.class) {
			c = c.getSuperclass();
		}
		ParameterizedType t = (ParameterizedType) c.getGenericSuperclass(); // OtherClass<String>
		Class<?> clazz = (Class<?>) t.getActualTypeArguments()[0];
		return (Class <T>) clazz;
	}
	
	public abstract void execute(T args) throws Exception;
	
	public void execute(T args, RequestBody body) throws Exception {
	    execute(args);
	}
	
}