package sra.scripts;

import static ngl.refactoring.state.SRASubmissionStateNames.NONE;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.inject.Inject;

import org.mongojack.DBQuery;
import org.mongojack.DBUpdate;

import fr.cea.ig.lfw.controllers.scripts.buffered.Script;
import fr.cea.ig.ngl.dao.api.sra.ExperimentAPI;
import fr.cea.ig.ngl.dao.api.sra.SubmissionAPI;
import fr.cea.ig.ngl.dao.readsets.ReadSetsAPI;
import fr.cea.ig.ngl.dao.readsets.ReadSetsDAO;
import fr.cea.ig.ngl.dao.sra.ReadsetDAO;
import models.laboratory.run.instance.ReadSet;
import models.sra.submit.common.instance.Submission;
import models.sra.submit.sra.instance.Experiment;
import models.sra.submit.sra.instance.RawData;
import services.Tools;


/*
 * Script à utiliser pour recharger les md5 d'une soumission a partir d'un fichier md5.txt
 * {@code http://localhost:9000/sra/scripts/run/sra.scripts.InitReadsetList?pathReadsetFile=pathFile}
 * <br>path
 * Si parametre absent dans url => declenchement d'une erreur.
 *  
 * @author sgas
 *
 */
public class InitReadsetList extends Script<InitReadsetList.MyParams> {
	//private static final play.Logger.ALogger logger = play.Logger.of(ReloadMd5.class);
	private final ReadSetsDAO           laboReadSetDAO; // DAO de la collection Illumina
	private final ReadsetDAO            readsetDAO;     // DAO de la collection sra.readset

	@Inject
	public InitReadsetList(ReadSetsDAO      	laboReadSetDAO ,
						   ReadsetDAO           readsetDAO) {
		this.laboReadSetDAO   = laboReadSetDAO;
		this.readsetDAO       = readsetDAO;


	}
	
	
	// Structure de controle et stockage des arguments de l'url.
	public static class MyParams {
		public String pathReadsetFile;
	}
	
	
	
	@Override
	public void execute(MyParams args) throws Exception {
		String user = "sgas";
		
		File file = new File(args.pathReadsetFile);
		InputStream inputStream = new FileInputStream(file);
		List<String> readsetCodes = Tools.loadReadSet(inputStream);
		int cp = 0;
		for(String readsetCode: readsetCodes) {
			// Reinitialiser à NONE le submissionState des readsets de la collection illumina :
			laboReadSetDAO.update(DBQuery.is("code", readsetCode),
					DBUpdate.set("submissionState.code", NONE)
					.set("submissionDate", null)
					.set("submissionUser", null)
					.set("traceInformation.modifyUser", user)
					.set("traceInformation.modifyDate", new Date()));

			// Enlever les readsets de la collection sra :
			readsetDAO.deleteByCode(readsetCode);
			cp++;
		}
		printfln("Reinitialisation des %s readsets du fichier %s", cp, args.pathReadsetFile);
	}

}
