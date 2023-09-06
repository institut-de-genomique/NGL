package sra.scripts.generic;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import fr.cea.ig.lfw.controllers.scripts.buffered.Script;
import fr.cea.ig.ngl.dao.readsets.ReadSetsDAO;
import fr.cea.ig.ngl.dao.sra.ReadsetDAO;
import models.laboratory.run.instance.ReadSet;
import models.sra.submit.sra.instance.RawData;
import models.sra.submit.sra.instance.Run;
import models.sra.submit.util.SraException;
import services.CreateServices;
import services.Tools;


/*
 * Script à utiliser pour avoir les fichiers de commandes à lancer en vue d'une soumission de readSetExternes integrés à minima dans NGL :
 * A lancer avec les arguments 
 * - cmdDirectory : chemin du repertoire qui doit contenir le fichier "readset.txt", et dans lequel seront ecrits les fichiers de sortie
 * de commandes cmd_links, cmd_gz, cmd_md5 et listAspera
 * - submissionDirectory : chemin du repertoire de la soumission avec les données brutes. Ce nom de repertoire sert uniquement
 * pour indiquer le bon chemin dans les differentes commandes gz, md5,...
 * ex de lancement :
 * {@code http://localhost:9000/sra/scripts/run/sra.scripts.generic.ExternalReadsetCmdSubmit?submissionDirectory=/env/cns/submit_traces/SRA/SNTS_output_xml/NGL/soumission_BSE_externalReadset&cmddirectory=C:\Users\sgas\soumission_BSE_cmd}
 * <br>path
 *  
 * @author sgas
 *
 */
public class ExternalReadsetCmdSubmit extends Script<ExternalReadsetCmdSubmit.MyParams> {
	//private static final play.Logger.ALogger logger = play.Logger.of(ReloadMd5.class);
	private final ReadSetsDAO           laboReadSetDAO; // DAO de la collection Illumina
	private final ReadsetDAO            readsetDAO;     // DAO de la collection sra.readset
	private final CreateServices        createServices;
	@Inject
	public ExternalReadsetCmdSubmit(ReadSetsDAO      	laboReadSetDAO ,
						   ReadsetDAO           readsetDAO,
						   CreateServices       createServices) {
		this.laboReadSetDAO   = laboReadSetDAO;
		this.readsetDAO       = readsetDAO;
		this.createServices   = createServices;


	}
	
	
	// Structure de controle et stockage des arguments de l'url.
	public static class MyParams {
		public String submissionDirectory;
		public String cmdDirectory;
	}
	
	public List<Run> getRuns(String pathReadsetFile) throws FileNotFoundException {
		List<Run> runs = new ArrayList<Run>();
		File file = new File(pathReadsetFile);
		InputStream inputStream = new FileInputStream(file);
		List<String> readsetCodes = Tools.loadReadSet(inputStream);
		for(String readsetCode: readsetCodes) {

			ReadSet laboReadset = laboReadSetDAO.getObject(readsetCode);
			if(laboReadset == null) {
				throw new SraException("readsetCode absent de la collection ReadSetIllumina");
			}
			Run run = createServices.createRunEntity(laboReadset);
			runs.add(run);
		}
		return runs;
	}
	
	
	public void write_cmd_links(String directory, List<Run> runs, File outputFile) throws Exception {
		
		int cp_rawData = 0;
		String mess = "";
		for(Run run : runs) {
			for (RawData rawData : run.listRawData) {
				String projectDir = rawData.directory;
				if (! projectDir.endsWith("/")) {
					projectDir = projectDir + "/";
				}
				mess = mess + "ln -s " + projectDir + rawData.relatifName + " " + directory + rawData.relatifName + "\n";
				println("ln -s " + projectDir + rawData.relatifName + " " + directory + rawData.relatifName);
				cp_rawData++;
			}
		}
		println("Ecriture de " + cp_rawData + " commandes_links");
		if (StringUtils.isNotBlank(mess)) {
			try (BufferedWriter output_buffer = new BufferedWriter(new java.io.FileWriter(outputFile))) {
				output_buffer.write(mess);
			}
		}
	}
	
	
	public void write_cmd_gz(String directory, List<Run> runs, File outputFile) throws Exception {
	
		int cp_rawData = 0;
		String mess = "";

		for(Run run : runs) {
			for (RawData rawData : run.listRawData) {
				String relatifName = rawData.relatifName;
				if (! relatifName.endsWith(".fastq")) {
					continue;
				}
				mess = mess + "gzip -c " + directory + relatifName + " > " + directory + relatifName + ".gz \n";
				println("gzip -c " + directory + relatifName + " > " + directory + relatifName + ".gz");
				cp_rawData++;
			}
		}
		println("Ecriture de " + cp_rawData + " commandes gz");
		if (StringUtils.isNotBlank(mess)) {
			try (BufferedWriter output_buffer = new BufferedWriter(new java.io.FileWriter(outputFile))) {
				output_buffer.write(mess);
			}
		}
	}
	
	
	public void write_cmd_md5(String directory, List<Run> runs, File outputFile) throws Exception {
		int cp_rawData = 0;
		String mess = "";
		for(Run run : runs) {
			for (RawData rawData : run.listRawData) {
				String relatifName = rawData.relatifName;
				if (relatifName.endsWith(".fastq")) {
					relatifName = relatifName + ".gz"; 
				}
				mess = mess + "md5sum " + directory + relatifName + " >> " + directory + "md5.txt \n";
				println("md5sum " + directory + relatifName +" >> " + directory + "md5.txt");
				cp_rawData++;
			}
		}
		println("Ecriture de " + cp_rawData + " commandes md5");
		if (StringUtils.isNotBlank(mess)) {
			try (BufferedWriter output_buffer = new BufferedWriter(new java.io.FileWriter(outputFile))) {
				output_buffer.write(mess);
			}
		}
	}
	
	
	public void write_cmd_listAspera(String directory, List<Run> runs, File outputFile) throws Exception {
		
		int cp_rawData = 0;
		String mess = "";
		for(Run run : runs) {
			for (RawData rawData : run.listRawData) {
				String relatifName = rawData.relatifName;
				if(relatifName.endsWith("fastq")) {
					relatifName = relatifName + ".gz";
				}
				mess = mess + directory + relatifName + "\n";
				println(directory + relatifName);
				cp_rawData++;
			}
		}
		println("Ecriture de " + cp_rawData + " données dans list_aspera");
		if (StringUtils.isNotBlank(mess)) {
			try (BufferedWriter output_buffer = new BufferedWriter(new java.io.FileWriter(outputFile))) {
				output_buffer.write(mess);
			}
		}
	}
	
	
	@Override
	public void execute(MyParams args) throws Exception {
		String directory = args.submissionDirectory;
		String cmdDirectory = args.cmdDirectory;
		if (! directory.endsWith("/")) {
			directory = directory + "/";
		}
		if (! cmdDirectory.endsWith("/")) {
			cmdDirectory = cmdDirectory + "/";
		}	
		List<Run> runs = getRuns(cmdDirectory + "readset.txt");
		write_cmd_links(directory, runs, new File(cmdDirectory + "cmd_links"));
		write_cmd_gz(directory, runs, new File(cmdDirectory + "cmd_gz"));
		write_cmd_md5(directory, runs, new File(cmdDirectory + "cmd_md5"));
		write_cmd_listAspera(directory, runs, new File(cmdDirectory + "listAspera"));
	}

}
