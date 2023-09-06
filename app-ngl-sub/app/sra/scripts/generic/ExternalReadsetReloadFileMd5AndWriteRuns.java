package sra.scripts.generic;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;

import fr.cea.ig.lfw.controllers.scripts.buffered.Script;

import fr.cea.ig.ngl.dao.readsets.ReadSetsDAO;
import models.laboratory.run.instance.ReadSet;
import models.sra.submit.sra.instance.RawData;
import models.sra.submit.sra.instance.Run;
import models.sra.submit.util.SraException;
import services.CreateServices;
import services.Tools;


/*
 * Script à utiliser pour avoir le fichier run.xml (corrige pour ses md5) des readset externes integrés à minima dans NGL :
 * script à lancer avec l'argument cmdDirectory : le chemin du repertoire qui doit contenir le fichier md5.txt et le fichier readset.txt,
 * et dans lequel sera ecrit le fichier de sortie run.xml
 * Ex de lancement
 * {@code http://localhost:9000/sra/scripts/run/sra.scripts.generic.ExternalReadsetReloadFileMd5AndWriteRuns?cmdDirectory=C:\Users\sgas\soumission_BSE_cmd}
 * <br>path
 * Si parametre absent dans url => declenchement d'une erreur.
 *  
 * @author sgas
 *
 */
public class ExternalReadsetReloadFileMd5AndWriteRuns extends Script<ExternalReadsetReloadFileMd5AndWriteRuns.MyParams> {
	//private static final play.Logger.ALogger logger = play.Logger.of(ReloadMd5.class);
	private final ReadSetsDAO           laboReadSetDAO; // DAO de la collection Illumina
	private final CreateServices        createServices;
	@Inject
	public ExternalReadsetReloadFileMd5AndWriteRuns(ReadSetsDAO      	laboReadSetDAO ,
						   CreateServices       createServices) {
		this.laboReadSetDAO   = laboReadSetDAO;
		this.createServices   = createServices;


	}
	
	
	// Structure de controle et stockage des arguments de l'url.
	public static class MyParams {
		public String cmdDirectory;
	}
	
	public List<Run> getRuns(File readsetFile) throws FileNotFoundException {
		List<Run> runs = new ArrayList<Run>();
		InputStream inputStream = new FileInputStream(readsetFile);
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
	
	
	
	public void writeRuns(List<Run> runs, Map<String, String> mapMd5, File runXmlFile) throws IOException {
		
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		StringBuilder sb = new StringBuilder();
		sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n");
		sb.append("<RUN_SET>\n");
		for (Run run: runs) {
			String runCode = run.code;
			String expCode = run.code.replaceFirst("run_", "exp_");
			sb.append("  <RUN alias=\"").append(runCode).append("\" ");
			if (StringUtils.isNotBlank(run.accession)) {
				sb.append(" accession=\"").append(run.accession).append("\" ");
			}
		
			//Format date
			sb.append("run_date=\"").append(formatter.format(run.runDate)).append("\"  run_center=\"").append(run.runCenter).append("\" ");
			sb.append(">\n");
			sb.append("    <EXPERIMENT_REF refname=\"").append(expCode).append("\"/>\n");
			sb.append("    <DATA_BLOCK>\n");
			sb.append("      <FILES>\n");
	
			for (RawData rawData: run.listRawData) {
				String fileType = rawData.extention;
				String collabFileName = rawData.collabFileName;
			
				fileType = fileType.replace(".gz", "");
				sb.append("        <FILE filename=\"").append(collabFileName).append("\" ").append("filetype=\"").append(fileType).append("\" checksum_method=\"MD5\" checksum=\"").append(rawData.md5).append("\">\n");
				if ( run.listRawData.size() == 2 ) {
					sb.append("          <READ_LABEL>F</READ_LABEL>\n");
					sb.append("          <READ_LABEL>R</READ_LABEL>\n");
				}
				sb.append("        </FILE>\n");
			}
			sb.append("      </FILES>\n");
			sb.append("    </DATA_BLOCK>\n");
			sb.append("  </RUN>\n");
		}
		sb.append("</RUN_SET>\n");
		
		try (BufferedWriter output_buffer = new BufferedWriter(new java.io.FileWriter(runXmlFile))) {
			output_buffer.write(sb.toString());
		}				
		
	}
	
	@Override
	public void execute(MyParams args) throws Exception {

		String cmdDirectory = args.cmdDirectory;
		if (! cmdDirectory.endsWith("/")) {
			cmdDirectory = cmdDirectory + "/";
		}	
		List<Run> runs = getRuns(new File(cmdDirectory + "readset.txt"));
		// correction des runs pour le md5 :
		Map<String, String> mapMd5 = Tools.loadMd5File(new File(cmdDirectory + "md5.txt"));
		for (Entry entry : mapMd5.entrySet()) {
			println(entry.getKey() + " " +entry.getValue());
		}
		for (Run run : runs) {
			for (RawData rawData: run.listRawData) {
				if(mapMd5.containsKey(rawData.collabFileName)) {
					println("correction md5 pour " + rawData.collabFileName);
					rawData.md5 = mapMd5.get(rawData.collabFileName);
				}
			}
		}
		writeRuns(runs, mapMd5, new File(cmdDirectory + "run.xml"));
		//File md5File = new File(cmdDirectory + "md5.txt");	
	}

}
