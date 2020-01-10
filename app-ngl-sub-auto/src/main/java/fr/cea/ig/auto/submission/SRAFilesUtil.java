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

public class SRAFilesUtil {

	
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
				if(!filePath.exists())
					checkDataCCRT=false;
			}
		}
		return checkDataCCRT;
	}
	
	public static void createBigTmpDirectory(Set<ResourceProperties> rpsRawData, String submissionCode) throws FatalException
	{
		boolean createBigTmpDir = false;
		for(ResourceProperties rawData: rpsRawData){
			if(rawData.getProperty("gzipForSubmission").equals("true") || rawData.getProperty("location").equals("CCRT")){
				createBigTmpDir=true;
				break;
			}
		}
		if(createBigTmpDir){
			File fileDir = new File(ProjectProperties.getProperty("tmpDirectory")+File.separator+submissionCode);
			if(!fileDir.exists()){
				fileDir.mkdir();
				//Set permission for group
				try {
					Runtime.getRuntime().exec("chmod 774 "+fileDir.getAbsolutePath());
				} catch (IOException e) {
					throw new FatalException(e);
				}
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
