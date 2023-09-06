package fr.cea.ig.ngl.support.api;

import fr.cea.ig.ngl.NGLApplicationHolder;
import fr.cea.ig.ngl.dao.api.SraParameterAPI;

public interface SraParameterAPIHolder extends NGLApplicationHolder {
	
	default SraParameterAPI getSraParameterAPI() { 
		return getNGLApplication().apis().sraParameter();
	}

}
