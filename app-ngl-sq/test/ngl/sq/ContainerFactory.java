package ngl.sq;

import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

import com.fasterxml.jackson.databind.JsonNode;

import fr.cea.ig.play.test.DBObjectFactory;
import fr.cea.ig.play.test.DevAppTesting;
import fr.cea.ig.play.test.JsonFacade;
import models.laboratory.container.instance.Container;
import models.laboratory.sample.instance.Sample;
import play.libs.ws.WSClient;

/**
 * Factory methods for Container.
 * 
 * @author vrd
 *
 */
// TODO: provide constructor like method(s). 
public class ContainerFactory extends DBObjectFactory {
	
	public static String res_00 = "data/Container_1AIF37ID7_UAT";
	
	// Modify a prototype to be linked with the sample. 
	public static JsonNode create_00(String code, JsonNode sample) throws IOException {
		JsonFacade s = new JsonFacade(sample);
		JsonFacade n = JsonFacade
				.getJsonFacade(res_00)
				.delete("_id")
				// .delete("traceInformation") -> keep creation date and user
				//.delete("traceInformation/createUser")
				//.delete("traceInformation/creationDate")
				//.delete("traceInformation/modifyUser")
				//.delete("traceInformation/modifyDate")
				.set("code",code)
				.copy(s,"code","contents[0]/sampleCode")
				.copy(s,"projectCodes[0]","contents[0]/projectCode")
				.copy(s,"taxonCode","contents[0]/taxonCode")
				.copy(s,"ncbiScientificName","contents[0]/ncbiScientificName")
				.copy(s,"code","contents[0]/properties/sampleAliquoteCode/value");
		// Copy some parts of the sample into the container
		// sample -> contents[0], assumes ony one source
		// JsonNode content = get(n,"contents[0]");
		// remap stuff or possibly use the DAO to remap stuff.
		// We create the api level sample and the 
		// map("code","sampleCode");
		// Using full path, Json support should use single string paths
		// map("code","contents[0]/smapleCode");
		return n.jsonNode();
	}

	public static Container from(String resourceName) throws IOException {
		return from(resourceName,Container.class);
	}

	// Sample must have been persisted
	public static Container freshInstance(WSClient c, String resourceName, Sample sample) throws IOException {
		Container container = from(resourceName);
		container._id = null;
		// Keep creation date as this does not go through trace stamping
		assertNotNull(container.traceInformation);
		container.traceInformation.creationDate = new Date();
		container.traceInformation.createUser = "joke";
		/*container.traceInformation.modifyDate = null;
		container.traceInformation.modifyUser = null;*/
		// Should clear the contents and a new one.
		container.code = DevAppTesting.newCode();
		container.contents.get(0).sampleCode = sample.code; 
		container.contents.get(0).projectCode = new ArrayList<>(sample.projectCodes).get(0);
		container.contents.get(0).taxonCode = sample.taxonCode;
		container.contents.get(0).ncbiScientificName = sample.ncbiScientificName;
		// container.contents.get(0).properties.get("sampleAliquoteCode").value = sample.code;
		container.contents.get(0).properties.get("sampleAliquoteCode").assignValue(sample.code);
    	DevAppTesting.savage(container,Container.class,models.utils.InstanceConstants.CONTAINER_COLL_NAME);
    	return container;
	}
	
}
