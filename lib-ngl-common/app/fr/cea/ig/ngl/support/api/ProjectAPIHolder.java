package fr.cea.ig.ngl.support.api;

import fr.cea.ig.ngl.NGLApplicationHolder;
import fr.cea.ig.ngl.dao.projects.ProjectsAPI;

public interface ProjectAPIHolder extends NGLApplicationHolder {
	
	default ProjectsAPI getProjectAPI() { 
		return getNGLApplication().apis().project();
	}
	
}
