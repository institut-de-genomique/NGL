package sra.scripts;



import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;


import org.mongojack.DBQuery;

import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.lfw.controllers.scripts.buffered.ScriptNoArgs;
import models.laboratory.run.instance.Run;
import models.utils.InstanceConstants;
import validation.ContextValidation;


/*
 * Script TEST
 * {@code http://localhost:9000/sra/scripts/run/sra.scripts.Test3}
 * <br> *  
 * @author sgas
 *
 */

public class Test3 extends  ScriptNoArgs { 

public Test3() {
	
}

public void validationRun() {
	// attention pour run externes, il faut ajouter "external" au context de validation
	Pattern p = Pattern.compile("EXT");
	List <Run> runList = MongoDBDAO.find(InstanceConstants.RUN_ILLUMINA_COLL_NAME, Run.class, DBQuery.regex("code", p)).toList();
	
	int cp =0;
	for (Run run : runList) {
		cp++;
		ContextValidation ctx = ContextValidation.createUpdateContext("ngsrg");
		Map<String, Object> contextObjects = new HashMap<String, Object>();
		contextObjects.put("external", new Boolean("True"));
		ctx.setContextObjects(contextObjects);
		run.validate(ctx);
		if(ctx.hasErrors()) {
			println(run.code);
			println(ctx);
			println("");
		}
	}
	println("cp=" + cp);

}


	@Override
	public void execute() throws Exception {
		List<String> strings = new ArrayList<String>();
		strings.add("_toto");
		strings.add("toto");
		Collections.sort(strings);
		for(String s:strings) {
			println(s);
		}
	}
		
		
}