package fr.cea.ig.ngl.support.api;

import fr.cea.ig.ngl.NGLApplicationHolder;
import fr.cea.ig.ngl.dao.protocols.ProtocolsAPI;

public interface ProtocolAPIHolder extends NGLApplicationHolder {
	
	default ProtocolsAPI getProtocolAPI() { 
		return getNGLApplication().apis().protocol();
	}

}
