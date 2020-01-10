package lims.cns.dao;

import java.util.HashMap;
import java.util.Map;



public class FileLine {

	public Map<String, String> map = new HashMap<String, String>();
	
	public FileLine(String[] header, String[] array) {
		
		for(int i = 0; i < header.length - 1; i++){
			String head = header[i];
			if(map.containsKey(head)){
				head += "_"+i;
			}
			map.put(head.trim(), array[i].trim());
		}
		
	}
	
	
}
