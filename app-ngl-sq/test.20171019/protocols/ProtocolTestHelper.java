package protocols;

import controllers.protocols.api.ProtocolsSearchForm;
import models.laboratory.protocol.instance.Protocol;

public class ProtocolTestHelper {
	
	public static Protocol getFakeProtocol(){
		Protocol protocol = new Protocol();
		return protocol;
	}
	
	public static Protocol getFakeProtocol(String code){
		Protocol protocol = new Protocol();
		protocol.code = code;		
		return protocol;
	}
	
	public static ProtocolsSearchForm getFakeProtocolsSearchForm(){
		ProtocolsSearchForm psf = new ProtocolsSearchForm();
		return psf;
	}

}
