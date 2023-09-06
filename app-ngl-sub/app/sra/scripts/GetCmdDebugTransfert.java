package sra.scripts;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;

import org.mongojack.DBQuery;

import fr.cea.ig.lfw.controllers.scripts.buffered.Script;
import fr.cea.ig.ngl.dao.api.sra.ExperimentAPI;
import models.sra.submit.sra.instance.Experiment;
import models.sra.submit.sra.instance.RawData;
import models.sra.submit.util.SraException;
import services.Tools;


/*
 * Script a lancer avec le chemin complet du fichier des collabFileNames à retransferer, le boolean test a true ou false, et le user.
 * le fichier collabFileNames indiqué doit avoir un format ou le premier mot de chaque ligne correspond à un collabFileName.
 * Le mail d'erreur de l'EBI peut convenir mais il faut enlever le texte en debut et fin de mail ainsi que la légende de la forme :
 * FILE_NAME | ERROR | MD5 | FILE_SIZE | DATE | RUN_ID/ANALYSIS_ID 
 * 
 * Script qui cree un repertoire dans l'espace des soumissions (en test ou non selon valeur du boolean test) 
 * avec les different fichiers de commandes permettant de resoumettre les données :
 * - le fichier "links.txt" des commandes des liens :  utiliser ce fichier pour creer des liens dans le repertoire sur les fichiers à retransferer
 * - le fichier "md5sum.glost" des commandes md5 : utiliser ce fichier pour calculer le md5sum des differents fichiers à transferer
 * - le fichier "deleteEbiCollabFileName.sh" des deletions sur le site ftp : utiliser ce fichier pour supprimer sur le site ftp de l'EBI les fichiers mal transferés.
 * - les fichiers "aspera_list_*.txt" qui permettent de retransferer les données brutes.
 * Ne pas faire tourner ce script en local car chemin des fichiers sur windows avec separateur different
 * Exemple de lancement :
 * http://appdev.genoscope.cns.fr:9005/sra/scripts/run/sra.scripts.GetCmdDebugTransfert?test=false&collabFileNames=/env/cns/tmp/sgas/fileProcessingErrors.txt
 *
 * @author sgas
 *
 */
// ok aucune erreur en PROD
public class GetCmdDebugTransfert extends Script<GetCmdDebugTransfert.Args> {

	private final ExperimentAPI experimentAPI;

	private static final play.Logger.ALogger logger = play.Logger.of(GetCmdDebugTransfert.class);
	private String pattern_LineVide = "^\\s*$";   // pour ignorer les lignes sans caracteres visibles  
	private java.util.regex.Pattern plv = Pattern.compile(pattern_LineVide); 

	private String pattern_LineComment = "^\\s*#";   // pour ignorer les lignes de commentaire
	private java.util.regex.Pattern plc = Pattern.compile(pattern_LineComment);
	DateFormat formaterDate = new SimpleDateFormat("yyyy_MM_dd");
    String st_date = formaterDate.format(new Date());

	@Inject
	public GetCmdDebugTransfert (ExperimentAPI  experimentAPI) {
		this.experimentAPI = experimentAPI;

	}


	public static class Args {
		public String collabFileNames;
		public boolean test; 
	}

	public Set<String> parseUserFile(String fileName) {
		//println("Dans parseUserFile");
		InputStream inputStream;
		Set<String> collabFileNames = new HashSet<String>();
		try {
			inputStream = new FileInputStream(fileName);
		} catch (FileNotFoundException e1) {
			throw new SraException("", e1);
		}
		String ligne;

		try (BufferedReader input_buffer = new BufferedReader(new InputStreamReader(inputStream))) {
			while ((ligne = input_buffer.readLine()) != null) {	
				Matcher mlv = plv.matcher(ligne);
				Matcher mlc = plc.matcher(ligne);
				if ( mlv.find() ) { // si ligne vide, ignorer la ligne
					//logger.debug("!!!!!!!!!!!!!!!!!ligne vide :" + ligne);
					continue;
				}
				if ( mlc.find() ) { // si ligne de commentaires, ignorer la ligne
					//logger.debug("!!!!!!!!!!!!!!!!!commentaires ignores :" + ligne);
					continue;
				}

				String collabFileName = Tools.clean(ligne);
				collabFileName = collabFileName.replaceAll("\\s+.*", ""); // prendre uniquement premiere colonne du fichier qui doit correspondre au nom de fichier
				collabFileName = collabFileName.replaceAll("[^/]*/", "");  // prendre uniquement chemin relatif du fichier si chemin complet indiqué
				if(!collabFileNames.contains(collabFileName)) {
					collabFileNames.add(collabFileName);
				}	
			}
		} catch (IOException e) {
			e.printStackTrace();
			throw new SraException("", e);
		}
		return collabFileNames;
	}

	public void writeShellAndGlostMd5File (String user, String directory, File md5GlostFile, File md5ShellFile, Set<String> collabFileNames ) throws Exception {
		// construction du fichier md5GlostFile
		try (BufferedWriter output_buffer = new BufferedWriter(new java.io.FileWriter(md5GlostFile))) {
			for (String collabFileName : collabFileNames) {
				List <Experiment> experimentList = experimentAPI.dao_find(DBQuery.is("run.listRawData.collabFileName", collabFileName)).toList();
				for (Experiment experiment : experimentList) {
					for (RawData rawData : experiment.run.listRawData) {
						if(rawData.collabFileName.equals(collabFileName)) {
							//printfln("md5sum " + collabFileName);
							output_buffer.write("md5sum " + collabFileName + " >> md5.txt \n");
						}
					}
				}
			}
		}
		try (BufferedWriter output_buffer = new BufferedWriter(new java.io.FileWriter(md5ShellFile))) {
			output_buffer.write("#!/bin/bash \n");
			output_buffer.write("#SBATCH -J md5sum \n");
			output_buffer.write("#SBATCH -A " + user + "\n");  // verifier sur quel compte tournent nos process pour les soumissions
			output_buffer.write("#SBATCH -p normal \n");
			output_buffer.write("#SBATCH -n 3 # un coeurs en plus pour glost \n");
			output_buffer.write("#SBATCH -c 1 \n");
			output_buffer.write("#SBATCH -N 1 \n");
			output_buffer.write("#SBATCH -t 4320 # mins \n");
			output_buffer.write("#SBATCH --qos=h72 \n");
			//output_buffer.write("#SBATCH -o " + directory + "/md5sum_%j.out \n");
			//output_buffer.write("#SBATCH -e " + directory + "/md5sum_%j.err \n\n");

			output_buffer.write("module load glost \n");
			output_buffer.write("hostname\n");
			output_buffer.write("mpirun glost_launch " + directory + "/" + md5GlostFile.getName() + "\n");
		}

	}
	
	public void writeShellLinksFile (File linksFile, Set<String> collabFileNames ) throws Exception {
		try (BufferedWriter output_buffer = new BufferedWriter(new java.io.FileWriter(linksFile))) {
			output_buffer.write("#!/bin/bash \n\n\n");
			for (String collabFileName : collabFileNames) {
				List <Experiment> experimentList = experimentAPI.dao_find(DBQuery.is("run.listRawData.collabFileName", collabFileName)).toList();
				for (Experiment experiment : experimentList) {
					for (RawData rawData : experiment.run.listRawData) {
						if(rawData.collabFileName.equals(collabFileName)) {
							File fileCible = new File(rawData.directory + File.separator + rawData.relatifName);
							//printfln("ln -s " + fileCible + "  " + collabFileName);
							output_buffer.write("ln -s " + fileCible + "  " + collabFileName + "\n");
						}
					}
				}
			}
		}
	}
	
	public void writeListesAspera (String user, String stResultDirectory, File asperaGlostFile, File asperaShellFile, Set<String> collabFileNames ) throws Exception {
		int countFile = 1;
		File asperaFile      = new File(stResultDirectory + "/" + "liste_aspera_" + countFile + ".txt");
		int cp = 0;
		int countDataPerFile = 250;
		try {

			BufferedWriter output_shellbuffer = new BufferedWriter(new java.io.FileWriter(asperaShellFile));
			// ecriture du fichier asperaShellFile :
			output_shellbuffer.write("#!/bin/bash \n");
			output_shellbuffer.write("#SBATCH -J aspera \n");
			output_shellbuffer.write("#SBATCH -A " + user + "\n");  // verifier sur quel compte tournent nos process pour les soumissions
			output_shellbuffer.write("#SBATCH -p normal \n");
			output_shellbuffer.write("#SBATCH -n 3 # un coeurs en plus pour glost \n");
			output_shellbuffer.write("#SBATCH -c 1 \n");
			output_shellbuffer.write("#SBATCH -N 1 \n");
			output_shellbuffer.write("#SBATCH -t 4320 # mins \n");
			output_shellbuffer.write("#SBATCH --qos=h72 \n");
			output_shellbuffer.write("#SBATCH -o " + stResultDirectory + "/aspera_%j.out \n");
			output_shellbuffer.write("#SBATCH -e " + stResultDirectory + "/aspera_%j.err \n\n");
			output_shellbuffer.write("module load glost \n");
			output_shellbuffer.write("hostname\n");
			output_shellbuffer.write("mpirun glost_launch " + stResultDirectory + "/" + asperaGlostFile.getName() + "\n");
			output_shellbuffer.close();
			
			BufferedWriter output_buffer = new BufferedWriter(new java.io.FileWriter(asperaFile));
			BufferedWriter output_glostbuffer = new BufferedWriter(new java.io.FileWriter(asperaGlostFile));
			// Ecriture de la ligne de commande dans glost :
			output_glostbuffer.write("ascp -i ~/.ssh/ebi.rsa -T -k2 -l300M --file-list=./" + asperaFile.getName() + " --mode=send --host=webin.ebi.ac.uk --user=Webin-9 . \n"); 
			
			for (String collabFileName : collabFileNames) {
				List <Experiment> experimentList = experimentAPI.dao_find(DBQuery.is("run.listRawData.collabFileName", collabFileName)).toList();
				for (Experiment experiment : experimentList) {
					for (RawData rawData : experiment.run.listRawData) {
						if(rawData.collabFileName.equals(collabFileName)) {
							cp++;
							if (cp > countDataPerFile) {
								countFile++;
								asperaFile    = new File(stResultDirectory + "/" + "liste_aspera_" + countFile + ".txt");
								output_buffer.close();
								output_buffer = new BufferedWriter(new java.io.FileWriter(asperaFile));
								// Ecriture de la ligne de commande dans glost :
								output_glostbuffer.write("ascp -i ~/.ssh/ebi.rsa -T -k2 -l300M --file-list=./" + asperaFile.getName() + " --mode=send --host=webin.ebi.ac.uk --user=Webin-9 . \n"); 
								cp = 1;
							}
							// ecriture du fichier liste aspera
							output_buffer.write(collabFileName + "\n");
						}
					}
				}
			}
			output_buffer.close();
			output_shellbuffer.close();
			output_glostbuffer.close();
		} catch (Exception e) {
			throw new Exception(e);
		}
	}
	public void writeShellDelFileViaFtp(File delFile, Set<String> collabFileNames) throws Exception {
		try (BufferedWriter output_buffer = new BufferedWriter(new java.io.FileWriter(delFile))) {
			output_buffer.write("#!/bin/bash \n\n\n");

			output_buffer.write("# constantes\n");
			output_buffer.write("HOST=webin.ebi.ac.uk\n");
			output_buffer.write("LOGIN=Webin-9\n");
			output_buffer.write("PASSWORD=Axqw16nI\n\n");
			output_buffer.write("# connexion\n");
			output_buffer.write("ftp -i -n $HOST << END_SCRIPT\n");
			output_buffer.write("quote USER $LOGIN\n");
			output_buffer.write("quote PASS $PASSWORD\n");
			output_buffer.write("pwd\n");
			output_buffer.write("bin\n");
			
			output_buffer.write("# les deletions : \n");
			for (String collabFileName : collabFileNames) {
				List <Experiment> experimentList = experimentAPI.dao_find(DBQuery.is("run.listRawData.collabFileName", collabFileName)).toList();
				for (Experiment experiment : experimentList) {
					for (RawData rawData : experiment.run.listRawData) {
						if(rawData.collabFileName.equals(collabFileName)) {
							//printfln("delete " + collabFileName);
							output_buffer.write("delete " + collabFileName + "\n");
						}
					}
				}
			}
			output_buffer.write("quit\n");
			output_buffer.write("END_SCRIPT\n");
		}
	}
	
	public void writeShellDelFileViaLftp(File delFile, File delShellFile, Set<String> collabFileNames) throws Exception {
		try (BufferedWriter output_buffer = new BufferedWriter(new java.io.FileWriter(delFile))) {
			// ecriture du fichier delFile :
			for (String collabFileName : collabFileNames) {
				List <Experiment> experimentList = experimentAPI.dao_find(DBQuery.is("run.listRawData.collabFileName", collabFileName)).toList();
				for (Experiment experiment : experimentList) {
					for (RawData rawData : experiment.run.listRawData) {
						if(rawData.collabFileName.equals(collabFileName)) {
							output_buffer.write(rawData.collabFileName + "\n" );
						}
					}
				}
			}
			output_buffer.close();
		}
		// ecriture du fichier delShellFile :
		try (BufferedWriter output_buffer  = new BufferedWriter(new java.io.FileWriter(delShellFile))) {
			output_buffer.write("#!/bin/bash \n\n\n");
			output_buffer.write("# constantes\n");
			output_buffer.write("HOST=webin.ebi.ac.uk\n");
			output_buffer.write("LOGIN=Webin-9\n");
			output_buffer.write("HOME=/env/cns/home/cnsnglapps\n\n");

			output_buffer.write("sed -e 's/^\\([^ ].*\\)$/rm \"\\1\"/' " + 
					delFile.getName() + " | LD_PRELOAD=/usr/lib64/libpassinjector.so lftp -u " + "\"$LOGIN,@passfile=$HOME/.webinaccess@\"" +  "  \"$HOST\"");
			output_buffer.close();
		}
	}
	
	// methode utilisée par autre script mais difficile de factoriser car utilisation de println
	// retourne true si ok et false sinon
	public boolean checkCollabFileNamesInNglsub(Set<String> collabFileNames)  throws Exception {
		boolean ok = true;
		// verifier que pour chaque collabFileName on a bien un et un seul experiment :
		for (String collabFileName : collabFileNames) {
			//println("collabFileName = " + collabFileName);
			List <Experiment> experimentList = experimentAPI.dao_find(DBQuery.is("run.listRawData.collabFileName", collabFileName)).toList();
			if(experimentList.size() <= 0) {
				logger.debug("Aucun experiment avec rawData.collabFileName=" + collabFileName);
				println("Aucun experiment avec rawData.collabFileName=" + collabFileName);
				ok = false;
				continue;	
			} else if (experimentList.size()>1) {
				String mess = "";
				for (Experiment experiment : experimentList) {
					mess = mess + experiment.code + ", ";
				}				
				int endIndex   = mess.lastIndexOf(",");
				mess = mess.substring(0, endIndex);
				logger.debug("plusieurs experiment avec rawData.collabFileName=" + collabFileName + " => " + mess );
				println("plusieurs experiment avec rawData.collabFileName=" + collabFileName + " => " + mess );
				ok = true;
			} else {
				// rien : cas normal
			}
		}
		return ok;
	}
	
	
	// pour chaque collabFileName, va chercher pour son run si existe un autre fichier collabFileName
	public Set<String> complementeCollabFileNames(Set<String> userCollabFileNames)  throws Exception {
		Set<String> completeCollabFileNames = new HashSet<String>();
		// verifier que pour chaque collabFileName on a bien un et un seul experiment :
		for (String userCollabFileName : userCollabFileNames) {
			//println("collabFileName = " + collabFileName);
			List <Experiment> experimentList = experimentAPI.dao_find(DBQuery.is("run.listRawData.collabFileName", userCollabFileName)).toList();
			Experiment experiment = experimentList.get(0);
			for (RawData rawData: experiment.run.listRawData) {
				String collabFileName = rawData.collabFileName;
				if(! completeCollabFileNames.contains(collabFileName)) {
					completeCollabFileNames.add(collabFileName);
				}
			}		
		}
		return completeCollabFileNames;
	}
	
	
	@Override
	public void execute(Args args) throws Exception {
		String pathCollabFileNames = args.collabFileNames;
		boolean test               = args.test;
		String user                = "soumissions";
		String stResultDirectory; 
		if(test) {
			stResultDirectory = "/env/cns/submit_traces/SRA/SNTS_output_xml/NGL-TEST/FIX_TRANSFERT/" + st_date;
		} else {
			stResultDirectory = "/env/cns/submit_traces/SRA/SNTS_output_xml/NGL/FIX_TRANSFERT/" + st_date;
		}
		
		File resultDirectory = new File(stResultDirectory);
		
		if (resultDirectory.exists()) {
			//logger.debug("Le repertoire " + dataRep + " existe deja !!! (soumission concurrente ?)");
			throw new SraException("Le repertoire " + resultDirectory + " existe deja !!!");
		} else {
			if (!resultDirectory.mkdirs()) {	
				//logger.debug("Impossible de creer le repertoire " + resultDirectory + " ");
				throw new SraException("Impossible de creer le repertoire " + resultDirectory + " ");
			}
		}
		File md5GlostFile = new File(stResultDirectory + "/" + "md5sum.glost");
		File md5ShellFile = new File(stResultDirectory + "/" + "md5sum.sh");
		File linksFile    = new File(stResultDirectory + "/" + "links.sh");
		File delFile      = new File(stResultDirectory + "/" + "deleteEbiCollabFileName.txt");
		File delShellFile = new File(stResultDirectory + "/" + "deleteEbiCollabFileName.sh");

		File asperaGlostFile = new File(stResultDirectory + "/" + "aspera.glost");
		File asperaShellFile = new File(stResultDirectory + "/" + "aspera.sh");


		Set<String> collabFileNames = parseUserFile(pathCollabFileNames);
		Set<String> setCollabFileNames = new HashSet<>(collabFileNames);
		if ( ! checkCollabFileNamesInNglsub(setCollabFileNames)) {
			throw new Exception ("Erreurs voir logger");
		}
		Set<String> completeCollabFileNames = complementeCollabFileNames(collabFileNames);
		writeShellLinksFile(linksFile, completeCollabFileNames);
		//printfln("\n\n\n");
		writeShellAndGlostMd5File(user, stResultDirectory, md5GlostFile, md5ShellFile,  completeCollabFileNames);	
		//printfln("\n\n\n");	
		writeShellDelFileViaLftp(delFile, delShellFile, completeCollabFileNames);
		writeListesAspera(user, stResultDirectory, asperaGlostFile, asperaShellFile, completeCollabFileNames);
		printfln("Fin du traitement :  voir repertoire " + stResultDirectory + " avec ses differents fichiers");	

	}

}








