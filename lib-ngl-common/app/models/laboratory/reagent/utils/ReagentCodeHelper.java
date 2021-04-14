package models.laboratory.reagent.utils;

import models.utils.code.DefaultCodeImpl;

public class ReagentCodeHelper extends DefaultCodeImpl {
	
    private static class SingletonHolder {
		private final static ReagentCodeHelper instance = new ReagentCodeHelper();
	}
    
	public static ReagentCodeHelper getInstance() {			
		return SingletonHolder.instance;
	}

	public synchronized String generateKitCatalogCode() {
		return generateBarCode();
	}

	public synchronized String generateBoxCatalogCode(String kitCatalogCode) {
		return generateBarCode();
	}

	public synchronized String generateReagentCatalogCode(String boxCatalogCode) {
		return generateBarCode();
	}
	
	public synchronized String generateKitCode() {
		return generateBarCode();
	}

	public synchronized String generateBoxCode(String kitCode) {
		return generateBarCode();
	}
	
	public synchronized String generateBoxCode() {
		return generateBarCode();
	}

	public synchronized String generateReagentCode() {
		return generateBarCode();
	}
	
	public synchronized String generateReceptionCode() {
		return generateBarCode();
	}
	
}
