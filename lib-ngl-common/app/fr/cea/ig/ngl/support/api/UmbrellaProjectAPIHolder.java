package fr.cea.ig.ngl.support.api;

import fr.cea.ig.ngl.NGLApplicationHolder;
import fr.cea.ig.ngl.dao.projects.UmbrellaProjectsAPI;

public interface UmbrellaProjectAPIHolder extends NGLApplicationHolder {
	
	default UmbrellaProjectsAPI getUmbrellaProjectAPI() { 
		return getNGLApplication().apis().umbrellaProject();
	}
	
}
