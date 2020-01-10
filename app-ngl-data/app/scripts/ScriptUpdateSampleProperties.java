package scripts;

import javax.inject.Inject;

import fr.cea.ig.lfw.controllers.scripts.buffered.Script;
import fr.cea.ig.ngl.NGLApplication;
import models.Constants;
import services.instance.sample.UpdateSamplePropertiesCNS;
import validation.ContextValidation;

/**
 * Script permettant d'exécuter la mise à jour des propriétés sample (propagation des propriétés)
 * Uniquement CNS
 * 
 * @author ejacoby
 *
 */
public class ScriptUpdateSampleProperties extends Script<ScriptUpdateSampleProperties.Args>{

	//TODO prevoir de mettre en argument l'institut CNS/CNG pour un script commun
	public static class Args {}

	private final NGLApplication app;
	
	@Inject
	public ScriptUpdateSampleProperties(NGLApplication app) {
		super();
		this.app=app;
	}

	@Override
	public void execute(Args args) throws Exception {
		ContextValidation contextError = ContextValidation.createUndefinedContext(Constants.NGL_DATA_USER);
		contextError.addKeyToRootKeyName("import");
		UpdateSamplePropertiesCNS update = new UpdateSamplePropertiesCNS(app);
		update.runImport(contextError);
	}

}
