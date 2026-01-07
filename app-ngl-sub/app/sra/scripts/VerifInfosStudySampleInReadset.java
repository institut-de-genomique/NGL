package sra.scripts;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.mongojack.DBQuery;

import controllers.sra.api.AbstractSampleAPI;
import controllers.sra.api.AbstractStudyAPI;
import controllers.sra.api.AnalysisAPI;
import controllers.sra.api.ExperimentAPI;
import controllers.sra.api.ReadsetAPI;
import controllers.sra.api.SubmissionAPI;
import fr.cea.ig.lfw.controllers.scripts.buffered.ScriptNoArgs;
import models.sra.submit.sra.instance.AbstractSample;
import models.sra.submit.sra.instance.AbstractStudy;
import models.sra.submit.sra.instance.Analysis;
import models.sra.submit.sra.instance.Experiment;
import models.sra.submit.sra.instance.Readset;
import models.sra.submit.sra.instance.Submission;
import play.libs.ws.WSClient;
import services.sra.VariableSRA;
import sra.services.SraEbiAPI;


/*
 * Script a lancer pour lister les incoherences entre les infos de sample et samples dans les experiments de soumission et 
 * les infos dans la table recapitulative des readsets de NGL-SUB
 * Exemple de lancement :
 * http://localhost:9000/sra/scripts/run/sra.scripts.VerifInfosStudySampleInReadset

 * @author sgas
 *
 */
public class VerifInfosStudySampleInReadset extends ScriptNoArgs {
	private final ReadsetAPI        readsetAPI;
	private final AbstractStudyAPI  absStudyAPI;
	private final AbstractSampleAPI absSampleAPI;
	private final ExperimentAPI     expAPI;
	//private final AnalysisAPI       analysisAPI;
	
	@Inject
	public VerifInfosStudySampleInReadset(
			ReadsetAPI        readsetAPI,
			AbstractStudyAPI  absStudyAPI,
			AbstractSampleAPI absSampleAPI,
			ExperimentAPI     expAPI
			//AnalysisAPI       analysisAPI
				) {
		this.readsetAPI   = readsetAPI;
		this.absStudyAPI  = absStudyAPI;
		this.absSampleAPI = absSampleAPI;
		this.expAPI       = expAPI;
		//this.analysisAPI  = analysisAPI;
	}	

	public void infoStudyInReadset() {
		List <Readset> readsets = readsetAPI.dao_find(DBQuery.and(DBQuery.is("state.code", "SUB-F"), DBQuery.or(DBQuery.is("studyAccession", null), DBQuery.is("studyExternalId", null)))).toList();
        println("liste des rs à l'etat SUB-F sans studyAccession ou sans studyExternalId");
		for (Readset rs : readsets) {
			AbstractStudy abstractStudy = absStudyAPI.get(rs.studyCode);
			Experiment exp = expAPI.get(rs.experimentCode);
			
			if(StringUtils.isBlank(rs.studyAccession))
				rs.studyAccession = "NULL";
			if(StringUtils.isBlank(rs.studyExternalId))
				rs.studyExternalId = "NULL";
			if(StringUtils.isBlank(exp.studyIdentifier))
				exp.studyIdentifier = "NULL";
			if(StringUtils.isBlank(abstractStudy.externalId))
				abstractStudy.externalId = "NULL";
			if(StringUtils.isBlank(abstractStudy.accession))
				abstractStudy.accession = "NULL";
			println("rs.code= " + rs.code + ", rs.studyCode=" + rs.studyCode + ", rs.studyAccession="+ rs.studyAccession + ", rs.studyExternalId="+ rs.studyExternalId);
			println("          abstractStudy.code=" + abstractStudy.code +", abstractStudy.accession="+ abstractStudy.accession +", abstractStudy.externalId=" + abstractStudy.externalId);
			if(exp != null) {
				println("          exp.code=" + exp.code + ", exp.studyCode="+ exp.studyCode + ", exp.studyIdentifier=" + exp.studyIdentifier);
			}
	
			println(rs.code);
		}
	}
	
	public void infoSampleInReadset() {
		List <Readset> readsets = readsetAPI.dao_find(DBQuery.and(DBQuery.is("state.code", "SUB-F"), DBQuery.or(DBQuery.is("sampleAccession", null), DBQuery.is("sampleExternalId", null)))).toList();
        println("liste des rs à l'etat SUB-F sans sampleAccession ou sans sampleExternalId");
		for (Readset rs : readsets) {
			AbstractSample abstractSample = absSampleAPI.get(rs.sampleCode);
			Experiment exp = expAPI.get(rs.experimentCode);
			
			if(StringUtils.isBlank(rs.sampleAccession))
				rs.sampleAccession = "NULL";
			if(StringUtils.isBlank(rs.sampleExternalId))
				rs.sampleExternalId = "NULL";
			if(StringUtils.isBlank(exp.sampleIdentifier))
				exp.sampleIdentifier = "NULL";
			if(StringUtils.isBlank(abstractSample.externalId))
				abstractSample.externalId = "NULL";
			if(StringUtils.isBlank(abstractSample.accession))
				abstractSample.accession = "NULL";
			println("rs.code= " + rs.code + ", rs.sampleCode=" + rs.sampleCode + ", rs.sampleAccession="+ rs.sampleAccession + ", rs.sampleExternalId="+ rs.sampleExternalId);
			println("          abstractSample.code=" + abstractSample.code +", abstractSample.accession="+ abstractSample.accession +", abstractSample.externalId=" + abstractSample.externalId);
			if(exp != null) {
				println("          exp.code=" + exp.code + ", exp.sampleCode="+ exp.sampleCode + ", exp.sampleIdentifier=" + exp.sampleIdentifier);
			}
	
			println(rs.code);
		}
	}	
	
	public void infoStudy() {
		//for (AbstractStudy abstractStudy : absStudyAPI.dao_all()) {
		int cp = 0;
//	    for (AbstractStudy abstractStudy : absStudyAPI.dao_find(DBQuery.in("code", "STUDY_8CJM3NULJ")).toList()) {
		for (AbstractStudy abstractStudy : absStudyAPI.dao_all()) {
			//println(abstractStudy.code);
			List <Readset> readsets = readsetAPI.dao_find(DBQuery.in("studyCode", abstractStudy.code).is("state.code", "SUB-F")).toList();
			for(Readset rs: readsets) {
				//println(rs.code);
				cp ++;
				if(StringUtils.isBlank(rs.studyAccession)) {
					println("rs.code " + rs.code + " avec studyAccession vide");
					continue;
				}
				if(StringUtils.isBlank(rs.studyExternalId)) {
					println("rs.code " + rs.code + " avec studyExternalId vide");
					continue;
				}					
				if(! rs.studyAccession.equals(abstractStudy.accession)) {
					println("rs.code " + rs.code + " avec studyAccession != de abstractStudy.accession pour studyCode=" + rs.studyCode);
				}
				if(! rs.studyExternalId.equals(abstractStudy.externalId)) {
					println("rs.code " + rs.code + " avec studyExternalId != de abstractStudy.externalId pour studyCode=" + rs.studyCode);
				}
				Experiment exp = expAPI.get(rs.experimentCode);
				if(exp != null) {
					if (! rs.studyAccession.equals(exp.studyIdentifier) && !rs.studyExternalId.equals(exp.studyIdentifier)) {
						if(StringUtils.isBlank(rs.studyAccession))
							rs.studyAccession = "NULL";
						if(StringUtils.isBlank(rs.studyExternalId))
							rs.studyExternalId = "NULL";
						if(StringUtils.isBlank(exp.studyIdentifier))
							exp.studyIdentifier = "NULL";
						if(StringUtils.isBlank(abstractStudy.externalId))
							abstractStudy.externalId = "NULL";
						if(StringUtils.isBlank(abstractStudy.accession))
							abstractStudy.accession = "NULL";
						println("rs.code= " + rs.code + ", rs.studyCode=" + rs.studyCode + ", rs.studyAccession="+ rs.studyAccession + ", rs.studyExternalId="+ rs.studyExternalId);
						println("          exp.code=" + exp.code + ", exp.studyIdentifier=" + exp.studyIdentifier);
						println("          abstractStudy.code=" + abstractStudy.code +", abstractStudy.accession"+ abstractStudy.accession +", abstractStudy.externalId" + abstractStudy.externalId);
					}
				} else {
					// Analyses verifiées en PROD et OK. 
					// Plus d'analyses produites (plus de bionano soumis)
//					Analysis analysis = analysisAPI.get(rs.analysisCode);
//					if(analysis == null) {
//						println("Aucun exp ni analyse pour le readset " + rs.code);
//					}
//					// analysis ne 
//					if(StringUtils.isBlank(rs.studyAccession))
//						rs.studyAccession = "NULL";
//					if(StringUtils.isBlank(rs.studyExternalId))
//						rs.studyExternalId = "NULL";
//					if(StringUtils.isBlank(analysis.studyIdentifier))
//						analysis.studyIdentifier = "NULL";
//					if(StringUtils.isBlank(abstractStudy.externalId))
//						abstractStudy.externalId = "NULL";
//					if(StringUtils.isBlank(abstractStudy.accession))
//						abstractStudy.accession = "NULL";
//					println("rs.code= " + rs.code + ", rs.studyCode=" + rs.studyCode + ", rs.studyAccession="+ rs.studyAccession + ", rs.studyExternalId="+ rs.studyExternalId);
//					println("          analysis.code=" + analysis.code + ", analysis.studyIdentifier=" + analysis.studyIdentifier);
//					println("          abstractStudy.code=" + abstractStudy.code +", abstractStudy.accession"+ abstractStudy.accession +", abstractStudy.externalId" + abstractStudy.externalId);	
				}
				//println ("" + cp);
			}
		}
		println(""+cp);
	}

	
	public void infoSample() {
		//for (AbstractSample abstractSample : absSampleAPI.dao_all()) {
		int cp = 0;
//	    for (AbstractSample abstractSample : absSampleAPI.dao_find(DBQuery.in("code", "STUDY_8CJM3NULJ")).toList()) {
		for (AbstractSample abstractSample : absSampleAPI.dao_all()) {
			//println(abstractSample.code);
			List <Readset> readsets = readsetAPI.dao_find(DBQuery.in("sampleCode", abstractSample.code).is("state.code", "SUB-F")).toList();
			for(Readset rs: readsets) {
				//println(rs.code);
				cp ++;
				if(StringUtils.isBlank(rs.sampleAccession)) {
					println("rs.code " + rs.code + " avec sampleAccession vide");
					continue;
				}
				if(StringUtils.isBlank(rs.sampleExternalId)) {
					println("rs.code " + rs.code + " avec sampleExternalId vide");
					continue;
				}					
				if(! rs.sampleAccession.equals(abstractSample.accession)) {
					println("rs.code " + rs.code + " avec sampleAccession != de abstractSample.accession pour sampleCode=" + rs.sampleCode);
				}
				if(! rs.sampleExternalId.equals(abstractSample.externalId)) {
					println("rs.code " + rs.code + " avec sampleExternalId != de abstractSample.externalId pour sampleCode=" + rs.sampleCode);
				}
				Experiment exp = expAPI.get(rs.experimentCode);
				if(exp != null) {
					if (! rs.sampleAccession.equals(exp.sampleIdentifier) && !rs.sampleExternalId.equals(exp.sampleIdentifier)) {
						if(StringUtils.isBlank(rs.sampleAccession))
							rs.sampleAccession = "NULL";
						if(StringUtils.isBlank(rs.sampleExternalId))
							rs.sampleExternalId = "NULL";
						if(StringUtils.isBlank(exp.sampleIdentifier))
							exp.sampleIdentifier = "NULL";
						if(StringUtils.isBlank(abstractSample.externalId))
							abstractSample.externalId = "NULL";
						if(StringUtils.isBlank(abstractSample.accession))
							abstractSample.accession = "NULL";
						println("rs.code= " + rs.code + ", rs.sampleCode=" + rs.sampleCode + ", rs.sampleAccession="+ rs.sampleAccession + ", rs.sampleExternalId="+ rs.sampleExternalId);
						println("          exp.code=" + exp.code + ", exp.sampleIdentifier=" + exp.sampleIdentifier);
						println("          abstractSample.code=" + abstractSample.code +", abstractSample.accession="+ abstractSample.accession +", abstractSample.externalId=" + abstractSample.externalId);
					}
				} else {
//					Analysis analysis = analysisAPI.get(rs.analysisCode);
//					if(analysis == null) {
//						println("Aucun exp ni analyse pour le readset " + rs.code);
//					}
//					if(StringUtils.isBlank(rs.sampleAccession))
//						rs.sampleAccession = "NULL";
//					if(StringUtils.isBlank(rs.sampleExternalId))
//						rs.sampleExternalId = "NULL";
//					if(StringUtils.isBlank(analysis.sampleIdentifier))
//						analysis.sampleIdentifier = "NULL";
//					if(StringUtils.isBlank(abstractSample.externalId))
//						abstractSample.externalId = "NULL";
//					if(StringUtils.isBlank(abstractSample.accession))
//						abstractSample.accession = "NULL";
//					println("rs.code= " + rs.code + ", rs.sampleCode=" + rs.sampleCode + ", rs.sampleAccession="+ rs.sampleAccession + ", rs.sampleExternalId="+ rs.sampleExternalId);
//					println("          analysis.code=" + analysis.code + ", analysis.sampleIdentifier=" + analysis.sampleIdentifier);
//					println("          abstractSample.code=" + abstractSample.code +", abstractSample.accession"+ abstractSample.accession +", abstractSample.externalId" + abstractSample.externalId);	
				}
				//println ("" + cp);
			}
		}
		println(""+cp);
	}
	
	@Override
	public void execute() throws Exception {
		infoStudy();
		println("\n\n");
		println("*************** Fin du traitement ***************");

	}		
}
