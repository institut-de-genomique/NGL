package fr.cea.ig.ngl.support.api;

import fr.cea.ig.ngl.NGLApplicationHolder;
import fr.cea.ig.ngl.dao.api.ResolutionConfigurationAPI;

public interface ResolutionConfigurationAPIHolder extends NGLApplicationHolder {
	
	default ResolutionConfigurationAPI getResolutionConfigurationAPI() {
		return getNGLApplication().apis().resolutionConfiguration();
	}

}
