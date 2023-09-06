package fr.cea.ig.ngl.support;

import static play.mvc.Results.ok;

import fr.cea.ig.ngl.NGLApplicationHolder;
import play.mvc.Result;

// Methods removed from NGLBaseController. Not needed it seems.

public interface NGLURLs extends NGLApplicationHolder {
	
	default Result jsAppURL() {
		StringBuilder sb = 
				new StringBuilder()
					.append("function AppURL (app){")
					.append("if(app===\"sq\") return \"")
//					.append(app.config().getSQUrl())
					.append(nglConfig().getSQUrl())
					.append("\"; else if(app===\"bi\") return \"")
//					.append(app.config().getBIUrl())
					.append(nglConfig().getBIUrl())
					.append("\"; else if(app===\"project\") return \"")
//					.append(app.config().getProjectUrl())
					.append(nglConfig().getProjectUrl())
					.append("\";}");
		return ok(sb.toString()).as("application/javascript");
	}

	default Result jsPrintTag(){
//		boolean tag = app.config().isBarCodePrintingEnabled();		
		boolean tag = nglConfig().isBarCodePrintingEnabled();		
		String js = "PrintTag={}; PrintTag.isActive =(function(){return " + tag + ";});";
		return ok(js).as("application/javascript");
	}
	
}
