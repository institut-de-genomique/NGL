package controllers.sra.api;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.collections4.CollectionUtils;
import models.sra.submit.util.VariableSRA;

public interface ISraSearchForm {
	static final play.Logger.ALogger logger = play.Logger.of(ISraSearchForm.class);

	// Declaration des methodes abstraites
	List<String> getStateCodes();
	void  setStateCodes(List<String> stateCodes);
	void addStateCode(String stateCode);
	List<String> getPseudoStateCodes();
	void setPseudoStateCodes(List<String> pseudoStateCodes);

	public default void copyPseudoStateCodesToStateCodesInFormulaire() {
		if (CollectionUtils.isNotEmpty(getPseudoStateCodes())) {
			if (CollectionUtils.isEmpty(getStateCodes())) {
				setStateCodes(new ArrayList<>());
			}
			for (String pseudoStateCode : getPseudoStateCodes()) {
				//logger.debug("pseudoStateCode : " + pseudoStateCode);
				pseudoStateCode = pseudoStateCode.replace("^\\s+", "");
				pseudoStateCode = pseudoStateCode.replace("\\s+$", "");
				ArrayList<String> tmp_stateCodes = VariableSRA.mapPseudoStateCodeToStateCodes().get(pseudoStateCode);
				if (tmp_stateCodes.isEmpty()) {
					logger.debug("Pas de correspondance de stateCode pour le pseudoStateCode " , pseudoStateCode);
				} else {
					for (String tmp_stateCode : tmp_stateCodes) {
						if( ! getStateCodes().contains(tmp_stateCode)) {
							addStateCode(tmp_stateCode);
							//logger.debug("xxxxxxxxxxxxx, ajout dans form.stateCodes de " + tmp_stateCode);
						}
					}
				}
			}
		}
	}

}
