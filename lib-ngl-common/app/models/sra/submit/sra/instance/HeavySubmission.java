package models.sra.submit.sra.instance;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jetty.util.StringUtil;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;

import controllers.sra.api.AbstractSampleAPI;
import controllers.sra.api.AbstractStudyAPI;
import controllers.sra.api.ConfigurationAPI;
import controllers.sra.api.ExperimentAPI;
import controllers.sra.api.SubmissionAPI;
import fr.cea.ig.DBObject;
import fr.cea.ig.lfw.utils.Iterables;
import models.laboratory.common.description.ObjectType;
import models.laboratory.common.instance.Comment;
import models.laboratory.common.instance.State;
import models.laboratory.common.instance.TraceInformation;
import models.utils.InstanceConstants;
import services.sra.SraException;
import validation.ContextValidation;
import validation.IValidation;
import validation.common.instance.CommonValidationHelper;
import validation.utils.ValidationHelper;

// objet qui contient l'integralité des objets crées lors d'une création de soumission, ou d'un update, ou d'une release.
// objet qui n'est pas stocké en base.
//
// alias des objets si pas integrés en base ???? 
public class HeavySubmission {
	public String     code;                    //          
	public Submission submission;              
	public Study      study;                   //           
	public Project    umbrella;                // ?                         
	public Analysis   analysis;                // on ne devrait plus en avoir              
	public List<AbstractSample> samples;       // alias logique
	public List<Experiment>     experiments;   // alias logique
	public List<Run>            runs;          // alias logique
	public List<Readset>        readsets;      // alias logique
 
	public List<String>         warnings;  
	@SuppressWarnings("unused")
	private static final play.Logger.ALogger logger = play.Logger.of(HeavySubmission.class);

	public HeavySubmission(String code) {
		this.code = code;
	} 
}


