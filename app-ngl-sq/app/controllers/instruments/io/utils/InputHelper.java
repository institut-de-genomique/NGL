package controllers.instruments.io.utils;

import java.util.ArrayList;
import java.util.List;

import org.mongojack.DBQuery;

import fr.cea.ig.MongoDBDAO;
import models.laboratory.experiment.instance.InputContainerUsed;
import models.laboratory.parameter.index.Index;
import models.utils.InstanceConstants;
import validation.ContextValidation;

/**
 * 
 * @author Fernando Dos santos
 * 
 */
public class InputHelper {
	
	private static final play.Logger.ALogger logger = play.Logger.of(InputHelper.class);
	

	public static boolean isPlatePosition(ContextValidation contextValidation, String position, int plFormat, int lineNumber){
		//07/02/2019 PB, peut etre appellee alors qu'on a pas l'information de numero de ligne...
		String lineNum=null;
		if (lineNumber==0) { lineNum="?";}
		else { lineNum=Integer.toString(lineNumber); }

		if ((position.length() < 2) || (position.length() > 3 )) {
			contextValidation.addError("Erreurs fichier", "experiments.msg.import.position.unknown", lineNum, position);
			return false;
		}
		
		String row    = position.substring(0,1);
		String column = position.substring(1);
		logger.info("isplateposition row:{} column:{}", row, column );
		
		// et si la string ne correspond pas a un nombre ???
		//   : NumberFormatException
		int col = Integer.parseInt(column);
		
		switch (plFormat) {
		case 96:
			if (row.matches("[A-H]") && col >= 1 && col <= 12) 
				return true;
			contextValidation.addError("Erreurs fichier", "experiments.msg.import.position.outofbonds", lineNum, position, plFormat);
			return false; 
		case 384:
	    	if (row.matches("[A-P]") && col >= 1 && col <= 24) 
				return true;
	    	contextValidation.addError("Erreurs fichier", "experiments.msg.import.position.outofbonds", lineNum , position, plFormat);
	    	return false;
	    default:
			// unsupported plate format
			return false;
		}
	}
	
	public static Index getIndexByName(String name, String typeCode){
		Index index  = MongoDBDAO.findOne(InstanceConstants.PARAMETER_COLL_NAME, Index.class, DBQuery.is("typeCode", typeCode).and(DBQuery.is("name", name)));
		return index;
	}
	
	//05/04/2016 ajoute "0" dans les positions ex A1=> A01
	public static String add02pos(String pos){
		String row = pos.substring(0,1);
		String col = pos.substring(1);
		if (col.length() == 1) { 
			return row + "0" + col ;
		} else {
			return pos;
		}
	}
	
	// __FDS__: evaluate this implementation
	public static String add02pos_(String pos) {
		switch (pos.length()) {
		case  0 : 
		case  1 : throw new IllegalArgumentException("position string '" + pos + "' does not have at least two characters");
		case  2 : return new StringBuilder(3).append(pos.charAt(0)).append('0').append(pos.charAt(1)).toString();
		case  3 : return pos;
		default : throw new RuntimeException("position string '" + pos + "' has more than three characters"); 
		}
	}
	
	//FDS 11/02/2019 NGL-2399: rien n'interdit au code barre d'une plaque de contenir un "_" et donc ZZZ_WW_A1 doit etre autorisé !!!
	// ==> utiliser locationOnContainerSupport !!!!!
	public static String getIcuPosition(InputContainerUsed icu ) {
		//System.out.println("DEBUG...ICU Position="+ icu.locationOnContainerSupport.line+ icu.locationOnContainerSupport.column);
		return  icu.locationOnContainerSupport.line+icu.locationOnContainerSupport.column;
	}

	// Author: Nicolas Wiart
	// retourne un tableau a partir d'une ligne au format CSV
	// used CSVReader
	// TODO __FDS__: suggest fix 
//	@Deprecated 
	public static String[] parseCSVLine(String s) {
	  int start = 0;
	  int end = 0;
	  int len = s.length();
	  boolean inquotes = false;
	  List<String> fields = new ArrayList<>();

	  for (int i = 0; i < len; i++) {
	    if (s.charAt(i) == '\"') {
	      if (inquotes) {
	         end = i;
	         fields.add(s.substring(start, end));
	         start = i + 1;
	         while (start < len) {
	             if (s.charAt(start) == '\n') {
	                 start = len;
	                 break;
	             }
	             if (s.charAt(start) == ',') {
	                start++;
	                break;
	             }
	             if (s.charAt(start) != ' ') {
	                throw new RuntimeException("unexpected chars " + s.charAt(start) + " after closing quote.");
	             }
	             start++;
	         }
	         i = start - 1;
	      } else {
	         // check for non-space between start et i exclus... TODO
	         start = end = i + 1;
	      }
	      inquotes = !inquotes;
	    } else if (s.charAt(i) == ',' && !inquotes) {
	      end = i;
	      fields.add(s.substring(start, end));
	      start = i + 1;
	    }
	  }

	  if (inquotes) throw new RuntimeException("Missing closing quote.");
	  if (start < len) fields.add(s.substring(start, len));

	  // convertir la List en tableau
	  return fields.toArray(new String[fields.size()]);
	}
	
}