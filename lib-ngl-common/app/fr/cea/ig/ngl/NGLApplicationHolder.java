package fr.cea.ig.ngl;

public interface NGLApplicationHolder {

	NGLApplication getNGLApplication();
	
	default NGLConfig nglConfig() {
		return getNGLApplication().nglConfig();
	}
	
}
