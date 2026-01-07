package sra.services;

import javax.inject.Inject;
import javax.inject.Singleton;

import controllers.sra.api.AbstractStudyAPI;
import controllers.sra.api.ProjectAPI;
import controllers.sra.api.StudyAPI;
import controllers.sra.api.SubmissionAPI;
import fr.cea.ig.ngl.dao.api.AnalysisAPI;
import fr.cea.ig.ngl.dao.sra.AtlaseaDAO;
import services.sra.EbiAPI;
import services.sra.NGLSUBConfig;
import services.taxonomy.TaxonomyServices;
import sra.api.SubmissionNewAPI;

// classe qui charge tous les services utiles pour les crons de NGL-SUB
// permet de ne pas avoir trop d'arguments pour l'injection dans les crons de ngl-sub.
// Devenu inutile. On evite d'avoir trop d'arguments en utilisant l'injection dans l'appel du scheduler.

@Singleton
@Deprecated
public class SRA {
	public final SubmissionAPI            submissionAPI;
	public final SubmissionNewAPI         submissionNewAPI;
	public final AbstractStudyAPI         abstractStudyAPI;
	public final XmlServices              xmlServices;
	public final TaxonomyServices         taxonomyServices;
	public final ProjectAPI               projectAPI;
	public final AtlaseaDAO               atlaseaDAO;
	public final EbiAPI                   ebiAPI;
	public final EbiFileResponseServices  ebiFileResponseServices;
	public final AnalysisAPI              biAnalysisAPI;   // pour faire un update des analyses de BI, sans regles sur les proprietes car pas implémentées.
	public final NGLSUBConfig             config;
	
	@Inject
	public SRA(			
	NGLSUBConfig            config,
	SubmissionAPI           submissionAPI,
	SubmissionNewAPI        submissionNewAPI,
	AbstractStudyAPI        abstractStudyAPI,
	XmlServices             xmlServices,
	TaxonomyServices        taxonomyServices,
	ProjectAPI              projectAPI,
	AtlaseaDAO              atlaseaDAO,
	EbiAPI                  ebiAPI,
	EbiFileResponseServices ebiFileResponseServices,
	AnalysisAPI             biAnalysisAPI) {
			this.submissionAPI           = submissionAPI;
			this.submissionNewAPI        = submissionNewAPI;
			this.abstractStudyAPI        = abstractStudyAPI;
			this.xmlServices             = xmlServices;
			this.taxonomyServices        = taxonomyServices;
			this.projectAPI              = projectAPI;
			this.atlaseaDAO              = atlaseaDAO;
			this.ebiAPI                  = ebiAPI;
			this.config                  = config;
			this.ebiFileResponseServices = ebiFileResponseServices;
			this.biAnalysisAPI           = biAnalysisAPI;
	}

	public SubmissionAPI                      submissionAPI()               { return submissionAPI;   }
    public SubmissionNewAPI                   submissionNewAPI()            { return submissionNewAPI;}
    public AbstractStudyAPI                   abstractStudyAPI()            { return abstractStudyAPI;}
    public XmlServices                        xmlServices()                 { return xmlServices;}
    public TaxonomyServices                   taxonomyServices()            { return taxonomyServices;}
    public ProjectAPI                         projectAPI()                  { return projectAPI;}
    public AtlaseaDAO                         atlaseaDAO()                  { return atlaseaDAO;}
    public EbiFileResponseServices            ebiFileResponseServices()     { return ebiFileResponseServices;}
    public AnalysisAPI                        biAnalysisAPI()               { return biAnalysisAPI;}
    public EbiAPI                             ebiAPI()                      { return ebiAPI;}
    public NGLSUBConfig                       config()                      { return config;}


}

