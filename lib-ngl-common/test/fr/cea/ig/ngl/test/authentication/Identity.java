package fr.cea.ig.ngl.test.authentication;

/**
 * Test identities, default Identity is supposed to be ReadWrite.
 * 
 * @author vrd
 *
 */
public enum Identity {
	Nobody,
	Read,
	Write,
	ReadWrite,
	Admin;
	
	public static Identity lowerValueOf(String value) {
	    switch (value.toLowerCase()) {
        case "nobody":      return Nobody;
        case "read":        return Read;
        case "write":       return Write;
        case "readwrite":   return ReadWrite;
        case "admin":       return Admin;      
        default:            throw new IllegalArgumentException("No enum constant fr.cea.ig.ngl.test.authentication.Identity."+value);
        }
	}
}
