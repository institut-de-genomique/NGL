package controllers.sra.configurations.api;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.mongojack.DBQuery;
import org.mongojack.DBQuery.Query;
import controllers.DBObjectListForm;
import fr.cea.ig.mongo.DBQueryBuilder;
import models.sra.submit.sra.instance.Configuration;

public class ConfigurationsSearchForm extends DBObjectListForm<Configuration>{
	public List<String> projCodes; // meme nom que dans la vue et les services .js
	public List<String> stateCodes;
	public String stateCode = null;
	public List<String> codes;
	public String codeRegex;

	
	@Override
	public Query getQuery() {
		List<Query> queries = new ArrayList<>();

		if (CollectionUtils.isNotEmpty(this.projCodes)) { //
			queries.add(DBQuery.in("projectCodes", this.projCodes)); // doit pas marcher car pour state.code
			// C'est une valeur qui peut prendre une valeur autorisee dans le formulaire. Ici on veut que 
			// l'ensemble des valeurs correspondent à l'ensemble des valeurs du formulaire independamment de l'ordre.
		}

		if (CollectionUtils.isNotEmpty(this.stateCodes)) { //all
			queries.add(DBQuery.in("state.code", this.stateCodes));
		}

		if (StringUtils.isNotBlank(this.stateCode)) { //all
			queries.add(DBQuery.in("state.code", this.stateCode));
		}

		if (CollectionUtils.isNotEmpty(this.codes)) { //all
			queries.add(DBQuery.in("code", this.codes));
		}

		if (CollectionUtils.isNotEmpty(this.codes)) {
			queries.add(DBQuery.in("code", this.codes));
		} 
		if(StringUtils.isNotBlank(this.codeRegex)) {
			queries.add(DBQuery.regex("code", Pattern.compile(this.codeRegex)));
		}

		//		Query query = null;
		//		if (queries.size() > 0)
		//			query = DBQuery.and(queries.toArray(new Query[queries.size()]));
		//		return query;
		
		return DBQueryBuilder.query(DBQueryBuilder.and(queries));
	}
	
}
