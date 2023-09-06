package fr.cea.ig.auto.submission;

public class Tools {
	public static Boolean isNotBlank(String string) {
		if (string != null && ! string.equals("") ) {
			return true;
		} else {
			return false;
		}
	}
}

