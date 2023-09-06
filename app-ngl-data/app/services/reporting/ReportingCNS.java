package services.reporting;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.mail.MessagingException;

import org.mongojack.DBQuery;

import com.mongodb.MongoException;
import com.typesafe.config.ConfigFactory;

import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.ngl.NGLApplication;
import mail.MailServiceException;
import mail.MailServices;
import models.laboratory.run.instance.ReadSet;
import models.utils.InstanceConstants;
import services.reporting.txt.reportingCNS;

public class ReportingCNS extends AbstractReporting {

	// Accessed through static methods
	private static final play.Logger.ALogger logger = play.Logger.of(ReportingCNS.class);
	
//	@Inject
//	public ReportingCNS(FiniteDuration durationFromStart, FiniteDuration durationFromNextIteration, NGLContext ctx) {
//		super("ReportingCNS", durationFromStart, durationFromNextIteration, ctx);
//	}
	
//	@Inject
//	public ReportingCNS(NGLContext ctx) {
//		super("ReportingCNS", ctx);
//	}
	
	@Inject
	public ReportingCNS(NGLApplication app) {
		super("ReportingCNS", app);
	}

	@Override
	public void runReporting() throws UnsupportedEncodingException, MessagingException {
		try {
			//Get global parameters for email
			String expediteur = ConfigFactory.load().getString("reporting.email.from"); 
			String dest       = ConfigFactory.load().getString("reporting.email.to");   
			String subject    = ConfigFactory.load().getString("reporting.email.subject") + " " + ConfigFactory.load().getString("institute") + " " + ConfigFactory.load().getString("ngl.env");
		    Set<String> destinataires = new HashSet<>();
		    destinataires.addAll(Arrays.asList(dest.split(",")));
		    
		    MailServices mailService = new MailServices();
		    
		    // Get data 
		    int nbQueries = 5;
		    logger.debug("Call fives query");
		    Integer[] nbResults = new Integer[nbQueries];  
//		    ArrayList<ArrayList<String>> listResults = new ArrayList<ArrayList<String>>();
		    ArrayList<ArrayList<String>> listResults = new ArrayList<>();
		    ArrayList<String> results = new ArrayList<>();
		    String[] subHeaders2 = new String[nbQueries];
		    for (int i=0; i<nbQueries; i++) {
		    	nbResults[i] = getQueryResults(i+1).size();
		    	if (nbResults[i] > 0)  
		    		subHeaders2[i] = getColumnHeaders(i+1);
		    	else
		    		subHeaders2[i] = "";
		    	results = getQueryResults(i+1);
		    	listResults.add(results);
		    }
		    
		    String content = reportingCNS.render(subHeaders2, nbResults, listResults).body();
		    		    
		    //Send mail using global parameters and content
		    mailService.sendMail(expediteur, destinataires, subject, new String(content.getBytes(), "iso-8859-1"));
		    
		} catch (MailServiceException e) {
			logger.error("MailService error: "+e.getMessage(),e);
		}
	}
	
	public static String getColumnHeaders(int queryId) {
		if (queryId == 4) 
			return "code,runCode,stateCode,sampleTypeCode";
		else
			return "code,runCode,stateCode";
	}

	
	public static ArrayList<String> getQueryResults(int queryId) throws MongoException {
		List<ReadSet> readSets = null;
		switch(queryId) {
			case 1:
				readSets = MongoDBDAO.find(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, DBQuery.and(DBQuery.is("state.code", "IP-QC"), DBQuery.is("typeCode", "rsillumina"),  
						DBQuery.notExists("treatments.readQualityRaw"))).toList();
				break;
			case 2:
				readSets = MongoDBDAO.find(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, DBQuery.and(DBQuery.is("state.code", "IW-VQC"), DBQuery.is("typeCode", "rsillumina"), 
						DBQuery.notExists("treatments.readQualityRaw"))).toList();
				break;
			case 3:
				readSets = MongoDBDAO.find(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, DBQuery.and(DBQuery.is("state.code", "IW-VQC"), DBQuery.is("typeCode", "rsillumina"), 
						DBQuery.notExists("treatments.readQualityClean"))).toList();
				break;
			case 4:
				readSets = MongoDBDAO.find(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, DBQuery.and( DBQuery.is("state.code", "IW-VQC"), DBQuery.is("typeCode", "rsillumina"), 
						DBQuery.notExists("treatments.sortingRibo"), DBQuery.in("sampleOnContainer.sampleTypeCode", Arrays.asList("depletedRNA","mRNA","total-RNA","sRNA","cDNA")) )).toList();
				break;
			case 5:
				readSets = MongoDBDAO.find(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, DBQuery.and(DBQuery.is("state.code", "IW-VQC"), DBQuery.is("typeCode", "rsillumina"), 
						DBQuery.notExists("treatments.taxonomy"))).toList();
				break;
			default:
				throw new RuntimeException("unhandled query id " + queryId);
		}
		ArrayList<String> lines = new ArrayList<>(); 
//		StringBuffer buffer;
		for (ReadSet readSet : readSets) { 
//			buffer = new StringBuffer();
			StringBuilder buffer = new StringBuilder(); 
			buffer.append(readSet.code).append(",").append(readSet.runCode).append(",").append(readSet.state.code);
			if (queryId == 4) {
				buffer.append(",").append(readSet.sampleOnContainer.sampleTypeCode);
			}
			lines.add(buffer.toString());
		}
		logger.debug("Result ");
		for (String line : lines) {
			logger.debug("Line "+line);
		}
		return lines;
	}
	
}
