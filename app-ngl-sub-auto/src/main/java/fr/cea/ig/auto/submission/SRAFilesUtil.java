package fr.cea.ig.auto.submission;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import fr.genoscope.lis.devsi.birds.api.entity.ResourceProperties;
import fr.genoscope.lis.devsi.birds.api.exception.FatalException;
import fr.genoscope.lis.devsi.birds.impl.properties.ProjectProperties;
import org.apache.log4j.Logger;


public class SRAFilesUtil {

	private static Logger log = Logger.getLogger(SubmissionServices.class);
	
	public static String getLocalDirectoryParameter(String directory, String[] filterExtensions)
	{
		String param = "";
		File dir = new File(directory);
		@SuppressWarnings("unchecked")
		List<File> files = (List<File>) FileUtils.listFiles(dir, filterExtensions, true);
		//Get list extension
		Set<String> extensions = new HashSet<String>();
		for(File file : files){
			extensions.add(FilenameUtils.getExtension(file.getName()));
		}
		for(String ext : extensions){
			if(ext.equals("gz"))
				param+=directory+"/*.fastq.gz ";
			else
				param+=directory+"/*."+ext+" ";
		}
		
		return param;
	}
	
	public static Set<ResourceProperties> createPackRawData(ResourceProperties rp, Set<ResourceProperties> rpsRawData, String fileName, int nbMaxPack) throws IOException
	{
		Set<ResourceProperties> newSet = new HashSet<ResourceProperties>();
		int numPack=1;
		int nbRawData=1;
		int rpsRawDataSize = rpsRawData.size();
		int nbPackRawData = 0;
		if(rpsRawDataSize % nbMaxPack ==0){
			nbPackRawData = rpsRawDataSize/nbMaxPack;
		}else{
			nbPackRawData = (rpsRawDataSize/nbMaxPack)+1;
		}
		String directoryPath=rp.getProperty("submissionDirectory");
		log.debug("Nb rawData "+rpsRawData.size());
		//Create first file
		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(directoryPath+File.separator+fileName+"_"+numPack)));
		log.debug("Create first file "+fileName+"_"+numPack);
		for(ResourceProperties rpRawData : rpsRawData) {
			String relatifName = rpRawData.get("relatifName");
			String collabFileName = rpRawData.get("collabFileName");
			String rawDataFileName = null;
			if(relatifName.equals(collabFileName)) {
				rawDataFileName=relatifName;
			}else {
				rawDataFileName=collabFileName;
				//create link lien=collabFileName cible=relatifName
				//ln -s directoryPath+File.separator+relatifName collabFileName
				Runtime.getRuntime().exec("ln -s "+directoryPath+File.separator+relatifName+" "+directoryPath+File.separator+collabFileName);
				//Cannot use createSymbolicLink in java 6
				/*Path lien = Paths.get(directoryPath+File.separator+collabFileName);
				Path cible = Paths.get(directoryPath+File.separator+relatifName);
				log.debug("create symbolic link "+directoryPath+File.separator+collabFileName);
				log.debug("From file "+directoryPath+File.separator+relatifName);
				Files.createSymbolicLink(lien, cible);*/
			}
			if(nbRawData<numPack*nbMaxPack) {
				//out.write(directoryPath+File.separator+rpRawData.get("relatifName")+"\n");
				out.write(directoryPath+File.separator+rawDataFileName+"\n");
				log.debug("write nbData "+nbRawData);
			}else {
				//out.write(directoryPath+File.separator+rpRawData.get("relatifName")+"\n");
				out.write(directoryPath+File.separator+rawDataFileName+"\n");
				log.debug("write nbData "+nbRawData);
				//Create new resourceProperties 
				ResourceProperties newRp = new ResourceProperties();
				newRp.addProperties(rp);
				newRp.setProperty("numPack",""+numPack);
				newRp.setProperty("fileNamePack",fileName+"_"+numPack);
				newRp.setProperty("code",newRp.getProperty("code")+"_"+numPack);
				newSet.add(newRp);
				
				//initialize for next pack
				out.close();
				if(numPack<nbPackRawData) {
					numPack++;
					log.debug("Create file "+fileName+"_"+numPack);
					out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(directoryPath+File.separator+fileName+"_"+numPack)));
				}
				
			}
			nbRawData++;
		}
		//Create last pack
		if(rpsRawDataSize % nbMaxPack !=0){
			ResourceProperties newRp = rp;
			newRp.setProperty("numPack",""+numPack);
			newRp.setProperty("fileNamePack",fileName+"_"+numPack);
			newRp.setProperty("code",rp.getProperty("code")+"_"+numPack);
			newSet.add(newRp);
			out.close();
		}
		
		return newSet;
	}
	
	/**
	 * @Deprecated
	 * Use createPackRawData
	 */
	public static void createWGSFile(String directoryPath, String fileName, Set<ResourceProperties> rps) throws IOException
	{
		//Create file 
		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName)));
		//Write relatifPathName for each resource properties
		for(ResourceProperties rp : rps)
		{
			out.write(directoryPath+File.separator+rp.get("relatifName")+"\n");
		}
		out.close();
	}
	
	public static boolean checkGzipForSubmission(Set<ResourceProperties> rpsRawData)
	{
		for(ResourceProperties rawData: rpsRawData){
			if(rawData.getProperty("gzipForSubmission").equals("true"))
				return true;
		}
		return false;
	}
	
	public static boolean checkDataCCRT(Set<ResourceProperties> rpsRawData, String submissionCode) throws FatalException
	{
		boolean checkDataCCRT = true;
		for(ResourceProperties rawData: rpsRawData){
			if(rawData.getProperty("location").equals("CCRT")){
				File filePath = new File(ProjectProperties.getProperty("tmpDirectory")+File.separator+submissionCode+File.separator+rawData.getProperty("relatifName"));
				log.debug("File path CCRT exist "+filePath.getPath());
				if(!filePath.exists())
					checkDataCCRT=false;
			}
		}
		return checkDataCCRT;
	}
	
	
	
	public static boolean checkMD5(Set<ResourceProperties> rpsRawData)
	{
		for(ResourceProperties rawData : rpsRawData) {
			if(rawData.getProperty("md5sumForSubmission").equals("true")) 
				return false;
		}
		return true;
	}
	
	
	public static void createBigTmpDirectory(Set<ResourceProperties> rpsRawData, String submissionCode) throws FatalException
	{
		boolean createBigTmpDir = false;
		for(ResourceProperties rawData: rpsRawData){
			if(rawData.getProperty("gzipForSubmission").equals("true") || rawData.getProperty("location").equals("CCRT") || rawData.getProperty("md5sumForSubmission").equals("true")){
				createBigTmpDir=true;
				break;
			}
		}
		if(createBigTmpDir){
			try {
				File fileDir = new File(ProjectProperties.getProperty("tmpDirectory")+File.separator+submissionCode);
				if(!fileDir.exists()){
					fileDir.mkdir();
					//Set permission for group
					Runtime.getRuntime().exec("chmod 774 "+fileDir.getAbsolutePath());
				}
			} catch (IOException e) {
				e.printStackTrace();
				throw new FatalException(e);
			}catch (Exception e) {
				e.printStackTrace();
				throw new FatalException(e);
			}
		}
	}
	
	public static boolean isDataCCRT(Set<ResourceProperties> rpsRawData)
	{
		for(ResourceProperties rawData: rpsRawData){
			if(rawData.getProperty("location").equals("CCRT"))
				return true;
		}
		return false;
	}
	
	public static Set<ResourceProperties> filterByGzipForSubmission(Set<ResourceProperties> rpsRawData)
	{
		Set<ResourceProperties> rpsRawDataFilter = new HashSet<ResourceProperties>();
		for(ResourceProperties rp : rpsRawData){
			if(rp.getProperty("gzipForSubmission").equals("true")){
				rp.put("relatifName", rp.getProperty("relatifName").replace(".gz", ""));
				rpsRawDataFilter.add(rp);
			}
		}
		return rpsRawDataFilter;
	}
	
	public static Set<ResourceProperties> filterByLocation(Set<ResourceProperties> rpsRawData)
	{
		Set<ResourceProperties> rpsRawDataFilter = new HashSet<ResourceProperties>();
		for(ResourceProperties rp : rpsRawData){
			if(rp.getProperty("location").equals("CCRT")){
				rpsRawDataFilter.add(rp);
			}
		}
		return rpsRawDataFilter;
	}
	
	public static boolean isNotNullValue(String value)
	{
		if (value!=null && !value.equals("")&& !value.equals("null"))
			return true;
		else
			return false;
	}
	
	public static void main(String[] args)
	{
		String[] extensions = new String[] { "fastq.gz", "sff" , "srf" };
		String param = SRAFilesUtil.getLocalDirectoryParameter("/env/cns/submit_traces/SRA/SNTS_output_xml/autoFtpTest/test_15_04_2014",extensions);
		System.out.println("Param "+param);
	}
}
