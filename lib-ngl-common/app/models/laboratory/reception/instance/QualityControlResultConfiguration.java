package models.laboratory.reception.instance;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Date;
import java.util.Map;

import models.laboratory.common.instance.Valuation;
import models.laboratory.container.instance.QualityControlResult;
import models.laboratory.reception.instance.ReceptionConfiguration.Action;
import models.utils.CodeHelper;
import validation.ContextValidation;

public class QualityControlResultConfiguration extends ObjectFieldConfiguration<QualityControlResult> {

	public QualityControlResultConfiguration() {
		super(AbstractFieldConfiguration.qualityControlResultsType);		
	}

	@Override
	public void populateField(Field field, Object dbObject, Map<Integer, String> rowMap, ContextValidation contextValidation, Action action) throws Exception {
		//we create or update all the comments
		QualityControlResult qcrObject = new QualityControlResult();
		qcrObject.index = 0;
		qcrObject.date = new Date();
		qcrObject.valuation = new Valuation();
		populateSubFields(qcrObject, rowMap, contextValidation, action);
		qcrObject.code = CodeHelper.getInstance().generateExperimentCode(qcrObject.typeCode);
		populateField(field, dbObject, Collections.singletonList(qcrObject));						
	}

}
