package models.laboratory.parameter.map;

import java.util.List;
import java.util.Map;

import models.laboratory.parameter.Parameter;

/**
 * NGL-4902
 * Mappings position des plaques de QPCR
 * Necessaire à l'importation des résultats des robots de QPCR en mode conversion
 * @author fdsantos
 *
 */
public class MapQPCRconvert extends Parameter{

	public Map<String, List<String>> map96To384geno;
	public Map<String, List<String>> map96To384conv;

	protected MapQPCRconvert(){
		super("map-qpcr-convert-parameter");
	}
}