package controllers.commons.api;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
//import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Pattern;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.lang3.StringUtils;
import org.mongojack.DBQuery;
import org.mongojack.DBQuery.Query;

import com.fasterxml.jackson.annotation.JsonIgnore;

import controllers.DBObjectListForm;
import controllers.ListObject;
import models.laboratory.parameter.Parameter;
import models.laboratory.protocol.instance.Protocol;
import play.Logger;
import play.libs.Json;

public class ParametersSearchForm extends DBObjectListForm<Parameter>{
	
	public String typeCode; 
	public Set<String> typeCodes;     // drop down multiple
	public Set<String> categoryCodes; //drop down multiple
	public String categoryCode;
	
	/* FDS/EJ 27/11/2018 Il faudra creer une ressource "Index/Tag" distincte des parametres avec sa propre collection Mongo...
	   FDS NGL-836: Pour l'instant completer Parameter 
	    note: dans SampleSearchForm il y a les variable ET la methode public DBQuery.Query getQuery() pourquoi pas ici ???? */
	
	public Set<String> codes; //textarea
	public String codeRegex;  //regex
	
	public Set<String> names; //textarea
	public String nameRegex;  //regex
	
	public Set<String> shortNames; //textarea
	public String shortNameRegex;  //regex
	
	public Set<String> groupNames;  // drop down multiple
	public String supplierNameRegex;//regex
	
	public String sequence;
	public Set<String> sequences;  //textarea
	public String sequenceRegex;   //regex
	//public Integer size;           //input   ne filtre rien...
	public String size;
	
	public Date fromDate;     //input
	public Date toDate;       //input
	public String createUser; //input avec liste

	/**
	 * build a query from a Form
	 * @param form
	 * @return query
	 */
	@Override
	public  Query getQuery() {
		List<Query> queries = new ArrayList<>();
		Query query = null;
		
		//!!utiliser CollectionUtils.isNotEmpty pour tester les parametres qui sont de type Set<String>
		
		if(CollectionUtils.isNotEmpty(this.typeCodes)){  
			queries.add(DBQuery.in("typeCode", this.typeCodes));
		} else if (StringUtils.isNotBlank(this.typeCode)) { 
			queries.add(DBQuery.is("typeCode", this.typeCode));
		}else {
			//------------------ filtres pour NGL-836------------------------------------
			// Attention au CNS il y des types parametres qui n'ont rien a voir avec les tag/index de sequencage...
			queries.add(DBQuery.in("typeCode","index-illumina-sequencing","index-nanopore-sequencing","index-mgi-sequencing","index-pacbio-sequencing")); // !! rend cette classe specifique aux index de sequencage...
		}
		//modifié FDS NGL-836
		if(CollectionUtils.isNotEmpty(this.sequences)){
			queries.add(DBQuery.in("sequence", this.sequences));
		}else if (StringUtils.isNotBlank(this.sequence)) { 
			queries.add(DBQuery.is("sequence", this.sequence)); // necessaire quand ???
		} else if(StringUtils.isNotBlank(this.sequenceRegex)){
			queries.add(DBQuery.in("sequence",  Pattern.compile(this.sequenceRegex)));
		}
		
		if(CollectionUtils.isNotEmpty(this.categoryCodes)){
				queries.add(DBQuery.in("categoryCode", this.categoryCodes));
		}else if(StringUtils.isNotBlank(this.categoryCode)){
			queries.add(DBQuery.is("categoryCode", this.categoryCode));
		}
		
		
		if(CollectionUtils.isNotEmpty(this.codes)){
			queries.add(DBQuery.in("code", this.codes));
		} else if(StringUtils.isNotBlank(this.codeRegex)){
			queries.add(DBQuery.regex("code", Pattern.compile(this.codeRegex)));
		}
		
		if(CollectionUtils.isNotEmpty(this.names)){
			queries.add(DBQuery.in("name", this.names));
		} else if(StringUtils.isNotBlank(this.nameRegex)){
			queries.add(DBQuery.regex("name", Pattern.compile(this.nameRegex)));
		}
		
		if(CollectionUtils.isNotEmpty(this.groupNames)){
			queries.add(DBQuery.in("groupNames", this.groupNames));
		}
		
		if(CollectionUtils.isNotEmpty(this.shortNames)){
			queries.add(DBQuery.in("shortName", this.shortNames));
		} else if(StringUtils.isNotBlank(this.shortNameRegex)){
			queries.add(DBQuery.regex("shortName", Pattern.compile(this.shortNameRegex)));
		}
		
		if(StringUtils.isNotBlank(this.supplierNameRegex)){
			queries.add(DBQuery.regex("supplierName", Pattern.compile(this.supplierNameRegex)));
		}
		
		if(null != this.size ) {
			if ( StringUtils.isNumeric(this.size)) {
				//queries.add(DBQuery.where("function() {return (this.sequence.length=="+form.size+")}"));
				// !! les index DUAL et POOL contiennent des "-"
				queries.add(DBQuery.where("function() {return (this.sequence.replace(/-/g,'').length=="+this.size+")}"));
			} else {
				//empecher autrement ???
				queries.add(DBQuery.where("function() {return (this.sequence.length==9999)}"));
			}
		}
		
		if(null != this.fromDate){
			queries.add(DBQuery.greaterThanEquals("traceInformation.creationDate", this.fromDate));
		}
		if(null != this.toDate){
			queries.add(DBQuery.lessThan("traceInformation.creationDate", (DateUtils.addDays(this.toDate, 1))));
		}

		if(StringUtils.isNotBlank(this.createUser)){
			queries.add(DBQuery.is("traceInformation.createUser", this.createUser));
		}
		
		if(queries.size() > 0){
			query = DBQuery.and(queries.toArray(new Query[queries.size()]));
		}

		
		return query;
	}
	
	@JsonIgnore
	private ListObject toListObj(Parameter parameter) {
		return new ListObject(parameter.code, parameter.name); 
	}
	
	@Override
	@JsonIgnore
	public Function<Parameter, ListObject> conversion() {
		return this::toListObj;
	}
}
