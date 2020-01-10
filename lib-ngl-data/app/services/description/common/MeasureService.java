package services.description.common;

import static services.description.DescriptionFactory.newMeasureUnit;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import models.laboratory.common.description.MeasureCategory;
import models.laboratory.common.description.MeasureUnit;
import models.laboratory.common.description.dao.MeasureCategoryDAO;
import models.utils.ModelDAOs;
import models.utils.dao.DAOException;
//import models.utils.dao.DAOHelpers;
import play.data.validation.ValidationError;

public class MeasureService {
	
	public static final String MEASURE_CAT_CODE_CONCENTRATION = "concentration";
	public static final String MEASURE_CAT_CODE_QUANTITY = "quantity";
	public static final String MEASURE_CAT_CODE_VOLUME = "volume";
	public static final String MEASURE_CAT_CODE_SIZE = "size";
	public static final String MEASURE_CAT_CODE_TIME = "time";
	public static final String MEASURE_CAT_CODE_SPEED = "speed";

	private final ModelDAOs mdao;
	
	public MeasureService(ModelDAOs mdao) {
		this.mdao = mdao;
	}
	
	public void saveData(Map<String, List<ValidationError>> errors) throws DAOException {		
		saveMesureCategories(errors);	
		saveMesureUnits(errors);	
	}
		
	/**
	 * Save all ExperimentCategory.
	 * @param errors        error manager
	 * @throws DAOException DAO problem
	 */
	public void saveMesureCategories(Map<String,List<ValidationError>> errors) throws DAOException {
		List<MeasureCategory> l = new ArrayList<>();
//		l.add(DescriptionFactory.newSimpleCategory(MeasureCategory.class, "Concentration", MEASURE_CAT_CODE_CONCENTRATION));
//		l.add(DescriptionFactory.newSimpleCategory(MeasureCategory.class, "Quantité",      MEASURE_CAT_CODE_QUANTITY));
//		l.add(DescriptionFactory.newSimpleCategory(MeasureCategory.class, "Volume",        MEASURE_CAT_CODE_VOLUME));
//		l.add(DescriptionFactory.newSimpleCategory(MeasureCategory.class, "Taille",        MEASURE_CAT_CODE_SIZE));
//		l.add(DescriptionFactory.newSimpleCategory(MeasureCategory.class, "Temps",         MEASURE_CAT_CODE_TIME));
//		l.add(DescriptionFactory.newSimpleCategory(MeasureCategory.class, "Vitesse",       MEASURE_CAT_CODE_SPEED));
		l.add(new MeasureCategory(MEASURE_CAT_CODE_CONCENTRATION, "Concentration"));
		l.add(new MeasureCategory(MEASURE_CAT_CODE_QUANTITY,      "Quantité"));
		l.add(new MeasureCategory(MEASURE_CAT_CODE_VOLUME,        "Volume"));
		l.add(new MeasureCategory(MEASURE_CAT_CODE_SIZE,          "Taille"));
		l.add(new MeasureCategory(MEASURE_CAT_CODE_TIME,          "Temps"));
		l.add(new MeasureCategory(MEASURE_CAT_CODE_SPEED,         "Vitesse"));
//		DAOHelpers.saveModels(MeasureCategory.class, l, errors);
		mdao.saveModels(MeasureCategory.class, l, errors);
	}
	
	/**
	 * Save all ExperimentCategory.
	 * @param errors        error manager
	 * @throws DAOException DAO problem
	 */
	public void saveMesureUnits(Map<String,List<ValidationError>> errors) throws DAOException {
		List<MeasureUnit> l = new ArrayList<>();
		MeasureCategoryDAO mcfind = MeasureCategory.find.get();

		l.add(newMeasureUnit("mL",     "mL",     true,  mcfind.findByCode(MEASURE_CAT_CODE_VOLUME)));
		l.add(newMeasureUnit("µL",     "µL",     false, mcfind.findByCode(MEASURE_CAT_CODE_VOLUME)));
		l.add(newMeasureUnit("nL",     "nL",     false, mcfind.findByCode(MEASURE_CAT_CODE_VOLUME)));
		l.add(newMeasureUnit("pL",     "pL",     false, mcfind.findByCode(MEASURE_CAT_CODE_VOLUME)));
		
		l.add(newMeasureUnit("µmol",   "µmol",   true,  mcfind.findByCode(MEASURE_CAT_CODE_QUANTITY)));
		l.add(newMeasureUnit("nmol",   "nmol",   false, mcfind.findByCode(MEASURE_CAT_CODE_QUANTITY)));
		l.add(newMeasureUnit("pmol",   "pmol",   false, mcfind.findByCode(MEASURE_CAT_CODE_QUANTITY)));
		l.add(newMeasureUnit("fmol",   "fmol",   false, mcfind.findByCode(MEASURE_CAT_CODE_QUANTITY)));
		l.add(newMeasureUnit("ng",     "ng",     false, mcfind.findByCode(MEASURE_CAT_CODE_QUANTITY)));
		l.add(newMeasureUnit("µg",     "µg",     false, mcfind.findByCode(MEASURE_CAT_CODE_QUANTITY)));

		l.add(newMeasureUnit("ng/µl",  "ng/µl",  false, mcfind.findByCode(MEASURE_CAT_CODE_CONCENTRATION)));
		l.add(newMeasureUnit("nM",     "nM",     false, mcfind.findByCode(MEASURE_CAT_CODE_CONCENTRATION)));
		l.add(newMeasureUnit("pM",     "pM",     false, mcfind.findByCode(MEASURE_CAT_CODE_CONCENTRATION)));
		l.add(newMeasureUnit("mM",     "mM",     false, mcfind.findByCode(MEASURE_CAT_CODE_CONCENTRATION)));
		l.add(newMeasureUnit("Kc/mm²", "Kc/mm²", false, mcfind.findByCode(MEASURE_CAT_CODE_CONCENTRATION)));
		l.add(newMeasureUnit("c/mm²",  "c/mm²",  false, mcfind.findByCode(MEASURE_CAT_CODE_CONCENTRATION)));
		
		l.add(newMeasureUnit("pb",     "pb",     true,  mcfind.findByCode(MEASURE_CAT_CODE_SIZE)));
		l.add(newMeasureUnit("Mb",     "Mb",     false, mcfind.findByCode(MEASURE_CAT_CODE_SIZE)));
		l.add(newMeasureUnit("kb",     "kb",     false, mcfind.findByCode(MEASURE_CAT_CODE_SIZE)));
		
		l.add(newMeasureUnit("s",      "s",      false, mcfind.findByCode(MEASURE_CAT_CODE_TIME)));
		l.add(newMeasureUnit("h",      "h",      false, mcfind.findByCode(MEASURE_CAT_CODE_TIME)));

		l.add(newMeasureUnit("rpm",    "rpm",    false, mcfind.findByCode(MEASURE_CAT_CODE_SPEED)));
		
//		DAOHelpers.saveModels(MeasureUnit.class, l, errors);
		mdao.saveModels(MeasureUnit.class, l, errors);
	}

}
