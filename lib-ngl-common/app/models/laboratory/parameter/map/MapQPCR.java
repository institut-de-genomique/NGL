package models.laboratory.parameter.map;

import java.util.List;
import java.util.Map;

import models.laboratory.parameter.Parameter;

/**
 * Mapping position et facteur de dilution des plaques de QPCR
 * Necessaire à l'importation des résultats des robots de QPCR
 * @author ejacoby
 *
 */
public class MapQPCR extends Parameter{

	//taille de reference de fragment (parametre logiciel lightcycler)
	public Integer refFrag;
	//taille par defaut des librairies
	public Integer defaultSize;
	//Mapping plaque origin vers plaque destination
	public Map<String, List<String>> map96To384;
	//Mapping plaque destination et facteur de dilution
	public Map<String, Integer> map384ToDil;
	
	protected MapQPCR(){
		super("map-qpcr-parameter");
	}
}