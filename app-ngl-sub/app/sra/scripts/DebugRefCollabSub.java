package sra.scripts;


import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.mongojack.DBQuery;
import org.mongojack.DBUpdate;
import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.MongoDBResult;
import fr.cea.ig.lfw.controllers.scripts.buffered.ScriptNoArgs;
import models.laboratory.run.instance.ReadSet;
import models.utils.InstanceConstants;


/*
 * Script a lancer pour lister les refCollab utilisées dans les soumissions
 * Exemple de lancement :
 * http://localhost:9000/sra/scripts/run/sra.scripts.DebugRefCollabSub

 * @author sgas
 *
 */
public class DebugRefCollabSub extends ScriptNoArgs {



	@Override
	public void execute() throws Exception {
		String pattern = "\\s+"; // presence espace
		java.util.regex.Pattern p = Pattern.compile(pattern);
		//List<ReadSet> readsets =  MongoDBDAO.find(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, DBQuery.notEquals("properties.refCollabSub.value", null)).skip(0).limit(100).toList();
		//List<ReadSet> readsets =  MongoDBDAO.find(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, DBQuery.exists("properties.refCollabSub.value")).toList();
		MongoDBResult<ReadSet> readsets =  MongoDBDAO
				.find(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, DBQuery.notEquals("properties.refCollabSub.value", null))
				//.skip(0)
				//.limit(100)
				//.toList()
				;

		int count = 0;
		int countBug = 0;


		for(ReadSet readset : readsets.cursor) {
			count++;
			//println("readset =" + readset.code );

			String refCollabSubValue = (String) readset.properties.get("refCollabSub").value;
			Matcher m = p.matcher(refCollabSubValue);

			if ( m.find() ) { 
				countBug++;
				String newRefCollabSubValue = refCollabSubValue.trim();
				println("bad refCollabSub '" + refCollabSubValue +"' pour readset "+ readset.code );
				println("correction refCollabSub '" + newRefCollabSubValue +"'");
				readset.properties.get("refCollabSub").value = newRefCollabSubValue;
				//MongoDBDAO.update(InstanceConstants.READSET_ILLUMINA_COLL_NAME, readset);
				MongoDBDAO.update(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, 
						DBQuery.is("code", readset.code),
						DBUpdate.set("properties.refCollabSub.value", newRefCollabSubValue));
				
			}
		}
		println("Nombre de refCollabSub en base = " + count);
		println("Nombre de refCollabSub avec espace en base corriges en base = " + countBug);
		println("*************** Fin du traitement ***************");

	}		
}
