package controllers.sra.api;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;

import controllers.ListForm;
import models.sra.submit.util.VariableSRA;

@Deprecated
// utiliser interface ISraSearchForm
public class SraSearchForm extends ListForm {
	private static final play.Logger.ALogger logger = play.Logger.of(ListForm.class);

	public Integer limit = -1;  // redefinir le limit car sinon on est avec limit de ListForm fixé à 5000
	                            // et si limite de 5000 renvoie les 5000 premiers resultats seulement quand on passe 
								// par controllers.MongoCommonController.mongoDBFinder(ListForm, DBQuery.Query) appelé par
	                            // controllers.sra.experiments.api.Experiments.list(), sans error ni warning.
	public List<String> stateCodes = new ArrayList<>();
	public List<String> pseudoStateCodes = new ArrayList<>();

	public static final void copyPseudoStateCodesToStateCodesInFormulaire(SraSearchForm form) {
//		logger.debug("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX        Dans copyPseudoStateCodesToStateCodesInFormulaire form.pseudoStateCodes = ", form.pseudoStateCodes);
//		logger.debug("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX        Dans copyPseudoStateCodesToStateCodesInFormulaire form.stateCodes = ", form.stateCodes);
		if (CollectionUtils.isNotEmpty(form.pseudoStateCodes)) {

			if (CollectionUtils.isEmpty(form.stateCodes)) {
				form.stateCodes = new ArrayList<>();
			}
			for (String pseudoStateCode : form.pseudoStateCodes) {
				//logger.debug("pseudoStateCode : " + pseudoStateCode);
				pseudoStateCode = pseudoStateCode.replace("^\\s+", "");
				pseudoStateCode = pseudoStateCode.replace("\\s+$", "");
				ArrayList<String> tmp_stateCodes = VariableSRA.mapPseudoStateCodeToStateCodes().get(pseudoStateCode);
				if (tmp_stateCodes.isEmpty()) {
					logger.debug("Pas de correspondance de stateCode pour le pseudoStateCode " , pseudoStateCode);
				} else {
					for (String tmp_stateCode : tmp_stateCodes) {
						if( !form.stateCodes.contains(tmp_stateCode)) {
							form.stateCodes.add(tmp_stateCode);
							//logger.debug("xxxxxxxxxxxxx, ajout dans form.stateCodes de " + tmp_stateCode);
						}
					}
				}
			}
			//form.pseudoStateCodes = null;
		}
		//logger.debug("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX        Dans copyPseudoStateCodesToStateCodesInFormulaire, form.stateCodes=" + form.stateCodes);
	}

}
