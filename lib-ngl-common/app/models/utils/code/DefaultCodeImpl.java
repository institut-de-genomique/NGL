package models.utils.code;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.mongojack.DBQuery;
import org.mongojack.DBUpdate;

import play.Logger;
import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.MongoDBResult.Sort;
import models.laboratory.common.instance.Comment;
import models.laboratory.experiment.instance.Experiment;
import models.laboratory.processes.instance.Process;
import models.laboratory.project.instance.Project;
import models.laboratory.sample.instance.Sample;
import models.utils.InstanceConstants;

public class DefaultCodeImpl implements Code {

	private SimpleDateFormat getSimpleDateFormat(String format) {
		return new SimpleDateFormat(format);
	}

	protected synchronized String generateBarCode(){
		Logger.debug("DefaultCodeImpl generateBarCode");
		try {
			Thread.sleep(1);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			Logger.error("Interruption error: "+e1.getMessage(),e1);
		}
		String date = new SimpleDateFormat("yyMMddHHmmssSS").format(new Date());
		Pattern p = Pattern
				.compile("([0-9]{2})([0-9]{2})([0-9]{2})([0-9]{2})([0-9]{2})([0-9]{2})([0-9]{3}|[0-9]{2})");
		// Matcher m = p.matcher("151231235959999");//worst situation
		Matcher m = p.matcher(date);
		if (m.matches()) {
			String code = Integer.toString(Integer.valueOf(Integer.valueOf(m.group(1))-15),//Years 0 is 2015
					36);// year
			code += Integer.toString(Integer.valueOf(m.group(2)), 36);// month
			code += Integer.toString(Integer.valueOf(m.group(3)), 36);// day
			code += Integer.toString(Integer.valueOf(m.group(4)), 36);// hours

			int second = Integer.valueOf(m.group(6)) + 10;// +10 because we can have duplicated like 11 0 and 1 10
			int minsec = Integer.valueOf(m.group(5) + String.valueOf(second)) + 1296;// +1296 because we want always 3 char
			code += Integer.toString(minsec, 36);// minute

			code += Integer.toString(Integer.valueOf(m.group(7)) + 36, 36);// millisecond
			Logger.debug("Container code generated "+code);
			return code.toUpperCase();
		} else {
			try {
				Logger.error("Error matches of the date fail"+date);
				throw new Exception("matches fail " + date);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				Logger.error("Matches error: "+e.getMessage(),e);;
				return null;
			}
		}
	}
	
	public synchronized String generateContainerSupportCode() {
		 return generateBarCode();
	}

	// ProcessusTypeCode-ProjectCode-SampeCode-YYYYMMDDHHMMSSSS
	public synchronized String generateProcessCode(Process process) {
		Logger.debug("DefaultCodeImpl generateProcessCode ");
		return (process.sampleOnInputContainer.sampleCode + "_" + process.typeCode + "_" + generateBarCode()).toUpperCase();
	}

	public synchronized String generateExperimentCode(Experiment exp) {
		return generateExperimentCode(exp.typeCode);
	}
	
	
	public synchronized String generateExperimentCode(String typeCode) {
		try {
			Thread.sleep(1);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			Logger.error("Interruption error: "+e1.getMessage(),e1);
		}
		
		String date = getSimpleDateFormat("yyyyMMdd_HHmmssSSS").format(new Date());
		Pattern p = Pattern.compile("([0-9]{8})_([0-9]{6})([0-9]{1})([0-9]{1})([0-9]{1})");
		// Matcher m = p.matcher("151231235959999");//worst situation
		Matcher m = p.matcher(date);
		if (m.matches()) {
			String code = m.group(1)+"_"+m.group(2)+""
					+Integer.toString(Integer.valueOf(m.group(3)) +10,36)
					+Integer.toString(Integer.valueOf(m.group(4)) +10,36)
					+Integer.toString(Integer.valueOf(m.group(5)) +10,36);
			return (typeCode + "-" + code).toUpperCase();
		} else {
			try {
				Logger.error("Error matches of the date fail"+date);
				throw new Exception("matches fail " + date);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				Logger.error("Matches error: "+e.getMessage(),e);;
				return null;
			}
		}
	}

	public synchronized String generateExperimentCommentCode(Comment com) {
		return (com.createUser + getSimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + Math
				.random()).toUpperCase();
	}
	
	public synchronized String generateSampleCode(String projectCode, boolean updateProject){
		Project project =MongoDBDAO.findByCode(InstanceConstants.PROJECT_COLL_NAME, Project.class, projectCode);
		return generateSampleCode(project, updateProject);
	}
	
	
	
	public synchronized String generateSampleCode(Project project, boolean updateProject){
		String newCode=nextSampleCode(project);
		if(updateProject){
			updateProjectSampleCodeIfNeeded(project.code, newCode);
		}
		return newCode;
	}
		
	private static String nextSampleCode(Project project){
		
		String currentCode = (null != project.lastSampleCode)?project.lastSampleCode.replace(project.code+"_", ""):null;
		
		String newCode=null;
		String beginCode=null;
		char lastLetter='Z';

		if(currentCode==null){
			newCode="A";
			while(newCode.length() < project.nbCharactersInSampleCode){
				newCode=newCode+"A";
			}			
		}else {
			
			int nbCharacter=currentCode.length();
			int lastCharacter=nbCharacter;

			//Recupère la position à partir de laquelle il faut changer de lettre
			while (lastCharacter!=0 && currentCode.substring(lastCharacter-1, lastCharacter).equals(Character.toString(lastLetter))) {
				lastCharacter--;
			}
			
			if( lastCharacter>1 || (lastCharacter==1 && !currentCode.substring(lastCharacter-1, lastCharacter).equals(String.valueOf(lastLetter))))
			{
				beginCode=currentCode.substring(0, lastCharacter-1); // debut du code sample a conserver
				newCode=beginCode+Character.toString((char) (currentCode.charAt(lastCharacter-1)+1)); // Concatenation debut code sample + lettre suivante
			}
			else {
				newCode="A";
				while(newCode.length() < project.nbCharactersInSampleCode+1){
					newCode=newCode+"A";
				}
				lastCharacter=1;
			}
			
			while (lastCharacter<nbCharacter){
				newCode=newCode+'A';
				lastCharacter++;
			}
		}
		
		return project.code+"_"+newCode;
	}
	
	public synchronized void updateProjectSampleCodeIfNeeded(String projectCode, String newSampleCode){
		Integer nbCharactersInSampleCode = newSampleCode.replace(projectCode+"_", "").length();
		
		MongoDBDAO.update(InstanceConstants.PROJECT_COLL_NAME, Project.class, DBQuery.is("code", projectCode)
				.or(DBQuery.notExists("lastSampleCode"), DBQuery.lessThan("nbCharactersInSampleCode", nbCharactersInSampleCode), 
						DBQuery.is("nbCharactersInSampleCode", nbCharactersInSampleCode).lessThan("lastSampleCode", newSampleCode)),
				DBUpdate.set("lastSampleCode",newSampleCode).set("nbCharactersInSampleCode", nbCharactersInSampleCode));
	}

	@Override
	public void updateProjectSampleCodeWithLastSampleCode(String projectCode) {
		List<Sample> samples = MongoDBDAO.find(InstanceConstants.SAMPLE_COLL_NAME, Sample.class, DBQuery.in("projectCodes", projectCode))
				.sort("code", Sort.DESC).limit(1).toList();
			
		if(samples.size() == 1){
			MongoDBDAO.update(InstanceConstants.PROJECT_COLL_NAME, Project.class, DBQuery.is("code", projectCode),
					DBUpdate.set("lastSampleCode",samples.get(0).code));
		}else{
			MongoDBDAO.update(InstanceConstants.PROJECT_COLL_NAME, Project.class, DBQuery.is("code", projectCode),
					DBUpdate.unset("lastSampleCode"));
		}
			
		
	}
}
