"use strict";
 
 angular.module('ngl-sub.ToolsServices', []).
	factory('toolsServices', ['$http', 'mainService', 'lists', 'tabService', 'messages',  'datatable', 
		function($http, mainService, lists, tabService, messages, datatable) {

		var toolsService = {
		
		isBlank:function(str) {
			return (!str || /^\s*$/.test(str));
		},

		
		isNotBlank:function(str) {
			return !(this.isBlank(str));
		},	
		
		isNullOrEmpty:function(str) {
			//console.log("XXXX  str=", str);
  			return !(typeof str === "string" && str.length > 0);
		},
		
		clean:function(str) {
			//console.log("XXXX  str=", str);
			str = str.replace(/^\s+/, ""); // Enlève les espaces au début
			str = str.replace(/\s+$/,"");  // Enlève les espaces à la fin
			//var chaine = "dans clean, chaine renvoyée ='" +str+"'";
			//console.log(chaine);
			return str;
		},	
		
		getNoLoginAlert:function( {}){
			return "Vous devez vous identifier pour continuer";
		},
			
		replacePseudoStateCodesToStateCodesInFormulaire:function(pseudoStateCodeToStateCodes, form) {
			//console.log("YYYYYYYYYYYYY form : ", form);
			//var pseudoStateCodeToStateCodes = consultationService.sraVariables.pseudoStateCodeToStateCodes
			if (typeof form.stateCodes === 'undefined' || form.stateCodes === null) {
				form.stateCodes = [];
			}
//			// show the values stored
//			for (var k in pseudoStateCodeToStateCodes) {
//			    // use hasOwnProperty to filter out keys from the Object.prototype
//			    if (pseudoStateCodeToStateCodes.hasOwnProperty(k)) {
//			        console.log("EEEEEEEEEE   key is: '",  k, "', value is: '", pseudoStateCodeToStateCodes[k],"'");
//			    }
//			}
			for (var i in form.pseudoStateCodes) {
				//console.log ("i=",i);
				var pseudoStateCode = form.pseudoStateCodes[i];
				pseudoStateCode = pseudoStateCode.replace(/^[\s]+/, ""); // Enlève les espaces au début
				pseudoStateCode = pseudoStateCode.replace(/[\s]+$/,"");      // Enlève les espaces à la fin
				//console.log("XXXXXXXXXXXXXXXXXXXX   pseudoStateCode = '", pseudoStateCode,"'");
				if (pseudoStateCodeToStateCodes.hasOwnProperty(pseudoStateCode)) {
					//console.log("IIIIIIIIIIIII   existe bien ", pseudoStateCode);
					var stateCodes = pseudoStateCodeToStateCodes[pseudoStateCode];
					for (var j in stateCodes){
						form.stateCodes.push(stateCodes[j]);
					}
				} else {
					console.log("Pas de correspondance de stateCode pour le peusoStateCode: ", pseudoStateCode);
				}
			}
			delete form.pseudoStateCodes;
			//console.log("Dans replaceSimplifiedStateCodesToStateCodesInFormulaire, form", form);
		}, // end replacePseudoStateCodesToStateCodesInFormulaire
		
		
	getStringAttributes:function(json) {
		var mess = "";
		//console.log("Dans getStringAttributes, json=", json);
		if(typeof json.attributes === 'string' || json.attributes instanceof String) {
			return json.attributes;
		}
		if(json.attributes != null && json.attributes.length != 0) {
			
			for(var i = 0; i < json.attributes.length; i++) {
				
				var attType = json.attributes[i]._type;
				var tag     = json.attributes[i].tag;
				var value   = json.attributes[i].value;
				var unit    = json.attributes[i].unit;
				if(this.isBlank(attType)) {
					attType = "";
				} else {
					attType = this.clean(attType);
				}
				if(this.isBlank(tag)) {
					tag = "";
				} else {
					tag = this.clean(tag);
				}
				if(this.isBlank(value)) {
					value = "";
				} else {
					value = this.clean(value);
				}
				if(this.isBlank(unit)) {
					unit = "";
				} else {
					unit = this.clean(unit);
				}
				if(this.isBlank(unit)){		
					// pas lisible pour utilisateur dans datatable : mess = mess + encodeURI("<"+ attType+"><TAG>"+ tag +"</TAG><VALUE>" + value + "</VALUE></" + attType + ">");
					mess = mess + "&lt;" + attType + "&gt;&lt;TAG&gt;" + tag + "&lt;/TAG&gt;&lt;VALUE&gt;" + value + "&lt;/VALUE&gt;&lt;/" + attType + "&gt;";
				} else {
					// pas lisible pour utilisateur dans datatable : mess = mess + encodeURI("<"+ attType+"><TAG>"+ tag +"</TAG><VALUE>" + value + "</VALUE></" + attType + "><UNIT>" + unit +"</UNIT>");
					mess = mess + "&lt;" + attType + "&gt;&lt;TAG&gt;" + tag + "&lt;/TAG&gt;&lt;VALUE&gt;" + value + "&lt;/VALUE&gt;&lt;UNIT&gt;" + unit + "&lt;/UNIT&gt;&lt;/" + attType + "&gt;";
				}
			}
		}
		//console.log("message des attributes=", mess);
		return mess;	
	},		
		
	// transforme le json du study manipulé par le datatable (avec chaine de caracteres séparées par ',' pour representer liste de string ou liste d'entier), 
	// en json correspondant à la classe Study (avec liste de string et liste d'entiers)
	// traitement au niveau des champs locusTagPrefix et idsPubmed (chaine de car dans datatable et liste de string dans le model java)
	toJsonStudyModel:function(json, errorDetails) {
		//console.log("hhhhhhhhhhhhh               json=", json);
		//console.log("hhhhhhhhhhhhh               errorDetails=", errorDetails);
		if(json==null) {
			errorDetails["absence de json dans methode toJsonStudyModel"]=[""];
			return;
		}
		// si on edite la valeur de locusTagPrefixs dans datatable alors string, et sinon array d'ou teste sur le type :
		if(typeof json.locusTagPrefixs === 'string' || json.locusTagPrefixs instanceof String) {
			//console.log("je suis bien dans le cas string");
			if(this.isNotBlank(json.locusTagPrefixs)){
				//console.log("XXXXXXXXXXXXXX  je suis dans le cas d'une chaine sans caractere");
				var strLocusTagPrefixs = json.locusTagPrefixs.replace(/,\s*$/, "");
				strLocusTagPrefixs = strLocusTagPrefixs.replace(/,\s*$/, "");
				delete json.locusTagPrefixs;
				json.locusTagPrefixs = strLocusTagPrefixs.split(','); 
		    	// Preferer boucle for au forEach ici car passage par valeur du locusTag d'ou modification dans le forEach qu'il faut sauvegarder
            	// hors de la boucle forEach. De plus, boucle for plus efficace.
				for(var i= 0; i < json.locusTagPrefixs.length; i++) {
     				json.locusTagPrefixs[i] = this.clean(json.locusTagPrefixs[i]);
				}
			} else  {
				//console.log("XXXXXXXXXXXXXX  je delete le champs locusTagPrefixs");
				json.locusTagPrefixs = null;
			}
		} else {
			//console.log("XXXXXXXXXXXXXX  locusTagPrefixs n'est pas une String")
		} // end if else typeOf
		if(typeof json.idsPubmed === 'string' || json.idsPubmed instanceof String) {
			//console.log("je suis bien dans le cas string");
			if(this.isNotBlank(json.idsPubmed)){
				//console.log("XXXXXXXXXXXXXX  je suis dans le cas d'une chaine sans caractere");
				var strIdsPubmed = json.idsPubmed.replace(/,\s*$/, "");
				strIdsPubmed = strIdsPubmed.replace(/,\s*$/, "");
				delete json.idsPubmed;
				json.idsPubmed = strIdsPubmed.split(','); 
		    	// Preferer boucle for au forEach ici car passage par valeur du locusTag d'ou modification dans le forEach qu'il faut sauvegarder
            	// hors de la boucle forEach. De plus, boucle for plus efficace.
				for(var i= 0; i < json.idsPubmed.length; i++) {
     				json.idsPubmed[i] = this.clean(json.idsPubmed[i]);
				}
			} else  {
				//console.log("XXXXXXXXXXXXXX  je delete le champs idsPubmed");
				json.idsPubmed = null;
			}
		} else {
			//console.log("XXXXXXXXXXXXXX  idsPubmed n'est pas une String")
		} // end if else typeOf
		
		if(typeof json.parentProjectAccessions === 'string' || json.parentProjectAccessions instanceof String) {
			//console.log("je suis bien dans le cas string");
			if(this.isNotBlank(json.parentProjectAccessions)){
				//console.log("XXXXXXXXXXXXXX  je suis dans le cas d'une chaine sans caractere");
				var strParentProjectAccessions = json.parentProjectAccessions.replace(/,\s*$/, "");
				strParentProjectAccessions = strParentProjectAccessions.replace(/,\s*$/, "");
				delete json.parentProjectAccessions;
				json.parentProjectAccessions = strParentProjectAccessions.split(','); 
		    	// Preferer boucle for au forEach ici car passage par valeur du parentProjectAccessions d'ou modification dans le forEach qu'il faut sauvegarder
            	// hors de la boucle forEach. De plus, boucle for plus efficace.
				for(var i= 0; i < json.parentProjectAccessions.length; i++) {
     				json.parentProjectAccessions[i] = this.clean(json.parentProjectAccessions[i]);
				}
			} else  {
				//console.log("XXXXXXXXXXXXXX  je delete le champs parentProjectAccessions");
				json.parentProjectAccessions = null;
			}
		} else {
			//console.log("XXXXXXXXXXXXXX  parentProjectAccessions n'est pas une String")
		} // end if else typeOf
		
		
		
// A laisser en commentaire tant que les attributes ne sont pas gérés comme des listes d'objets attributes :
//		if(typeof json.attributes === 'string' || json.attributes instanceof String) {
//			if(this.isNotBlank(json.attributes)) {
//				//oter retours charriots :
//				var strAttributes = json.attributes.replace(/\n|\r/g, "" );
//				// supprimer espaces en debut et fin de chaine :
//				strAttributes = json.attributes.replace(/^\s+/g, "" );
//				strAttributes = json.attributes.replace(/\s+$/g, "" );
//				//console.log("ooooooooooooo   yyyyyyyyy                          strAttributes=", strAttributes);
//				var projectPatternTagValueDoc = new RegExp("^\\s*<PROJECT_ATTRIBUTE>\\s*<TAG>\\s*([^<>]+)\\s*</TAG>\\s*<VALUE>\\s*([^<>]+)\\s*</VALUE>\\s*</PROJECT_ATTRIBUTE>");
//				var projectPatternTagValue = new RegExp("^\\s*<PROJECT_ATTRIBUTE>\\s*<TAG>\\s*([^<>]+)\\s*</TAG>\\s*<VALUE>\\s*([^<>]+)\\s*</VALUE>\\s*");
//                var projectPatternTagValueUnitsDoc = new RegExp("^\\s*<PROJECT_ATTRIBUTE>\\s*<TAG>\\s*([^<>]+)\\s*</TAG>\\s*<VALUE>\\s*([^<>]+)\\s*</VALUE>\\s*<UNITS>\\s*([^<>]+)\\s*</UNITS>\\s*</PROJECT_ATTRIBUTE>");
//                var projectPatternTagValueUnits = new RegExp("^\\s*<PROJECT_ATTRIBUTE>\\s*<TAG>\\s*([^<>]+)\\s*</TAG>\\s*<VALUE>\\s*([^<>]+)\\s*</VALUE>\\s*<UNITS>\\s*([^<>]+)\\s*</UNITS>\\s*");
//
//				var res = strAttributes.split(new RegExp("</PROJECT_ATTRIBUTE>"));
//					
//				//console.log("ooooooooooooooo  yyyyyyyyyy              res=", res);
//				var tab_attributes = [];
//				
//				for(var i= 0; i < res.length; i++) {
//					if(this.isNotBlank(res[i])) {
//     					//console.log("dans tableau des resultats", res[i]);
//						if(res[i].match(projectPatternTagValue)){
//							//console.log("je matche avec projectPatternTagValue");
//							var tag = res[i].replace(projectPatternTagValue, '$1');
//							var value = res[i].replace(projectPatternTagValue, '$2');
//							//console.log("ooooooooooo   tag=", tag);
//							//console.log("ooooooooooo   value=", value);
//
//							var attribute = {"tag"   : res[i].replace(projectPatternTagValue, '$1'),
//											 "value" : res[i].replace(projectPatternTagValue, '$2'),
//					                         "_type" : "PROJECT_ATTRIBUTE"
//											};
//							
//							tab_attributes.push(attribute);
//						} else if(res[i].match(projectPatternTagValueUnits)){
//							var attribute = {"tag"   : res[i].replace(projectPatternTagValue, '$1'),
//											 "value" : res[i].replace(projectPatternTagValue, '$2'),
//											 "unit"  : res[i].replace(projectPatternTagValue, '$3'),
//					                         "_type" : "PROJECT_ATTRIBUTE"
//											};
//							tab_attributes.push(attribute);
//						} else {
//							//console.log("La chaine des attributs '" + attribute + "' n'est pas au format attendue " + projectPatternTagValueDoc + " ou " + projectPatternTagValueUnitsDoc);
//							errorDetails[json.code + ".attributes"]=["La chaine des attributs n'est pas au format attendue " + projectPatternTagValueDoc + " ou " + projectPatternTagValueUnitsDoc];
//							delete json.attributes;
//						}
//					}
//				}			
//				json.attributes = tab_attributes;
//			} else  {
//				//console.log("XXXXXXXXXXXXXX  je delete le champs attributes");
//				json.attributes = null;
//			}
//		} else {
//			//console.log("XXXXXXXXXXXXXX  attributes n'est pas une String")
//		}// end if typeOf

	}, // end toJsonStudyModel
	
		
    toJsonUmbrellaModel:function(json, errorDetails) {
		if(json==null) {
			errorDetails["absence de json dans methode toJsonStudyModel"]=[""];
			return;
		}
		this.toJsonStudyModel(json, errorDetails);

		if(typeof json.childrenProjectAccessions === 'string' || json.childrenProjectAccessions instanceof String) {
			//console.log("je suis bien dans le cas string");
			if(this.isNotBlank(json.childrenProjectAccessions)){
				//console.log("XXXXXXXXXXXXXX  je suis dans le cas d'une chaine sans caractere");
				var strChildrenProjectAccessions = json.childrenProjectAccessions.replace(/,\s*$/, "");
				strChildrenProjectAccessions = strChildrenProjectAccessions.replace(/,\s*$/, "");
				delete json.childrenProjectAccessions;
				json.childrenProjectAccessions = strChildrenProjectAccessions.split(','); 
		    	// Preferer boucle for au forEach ici car passage par valeur du childrenProjectAccessions d'ou modification dans le forEach qu'il faut sauvegarder
            	// hors de la boucle forEach. De plus, boucle for plus efficace.
				for(var i= 0; i < json.childrenProjectAccessions.length; i++) {
     				json.childrenProjectAccessions[i] = this.clean(json.childrenProjectAccessions[i]);
				}
			} else  {
				//console.log("XXXXXXXXXXXXXX  je delete le champs childrenProjectAccessions");
				json.childrenProjectAccessions = null;
			}
		} else {
			//console.log("XXXXXXXXXXXXXX  parentProjectAccessions n'est pas une String")
		} // end if else typeOf
	    
	}, // end toJsonUmbrellaModel
	// Definitions des methodes :
	//---------------------------
	// !!!!!  Ne marche pas car methode message() inaccessible dans ce service : 
	// mais permet de garder code en securité 
	updateCallback : function(datatable, countError, monError) {
		//messages = messages(); // si on re-sauve sans recharger page.
		//messages.clear();      // si on re-sauve sans recharger page.
		//console.log(datatable);
		console.log(countError);
		console.log("YYYYYYYY   TOTOTO   YYYYYYYYYYY    dans callback, displayResult : ", datatable.displayResult);
		console.log("xxxxxxxxxx    dans callback, displayResult.line : ", datatable.displayResult.line);
		//console.log("eeeeeeeeeeeeee  dans callback scope.dbSamples = ", $scope.dbSamples);
		var monError = {};
		for (var i = 0; i < datatable.displayResult.length; i++) {
			var keysError =  Object.keys(datatable.displayResult[i].line.errors);
			console.log("eeeeeeeee keysError", keysError);
			console.log("eeeeeeeee keysError.length", keysError.length);
			if ( keysError.length >= 1 ) {
				// Recuperation message erreur :
				var monObj = datatable.displayResult[i].line.errors;
				console.log("dans callback, monObj", monObj);
				console.log("dans callback, monObj.typeOf", monObj.typeOf);
				console.log("dans callback, monObj.hasOwnProperty()", monObj.hasOwnProperty());
				for (var [key, values] of Object.entries(monObj)) {
					var cle = `${key}`;
					var values = `${values}`; 
  					console.log("xxxxxxxxxxx    cle = ", cle);
					console.log("xxxxxxxxxxx    values = ", values);
					console.log("monObjValues = ", monObj[cle]);
					
					monError[cle]=[values];
					//messages.addDetails(monError);
				} // end for
			} // end if						
		}// end for
		// Affichage message succes ou erreur :
		// Dans le cas d'une exception, countError n'est pas definie :
		if ( countError == undefined || countError == null || countError > 0) {
			//messages.setError("save");
		} else {
			//messages.setSuccess("save");			
		}
	}, // end updateCallback

		
		poubelleBleue:function(datatablesDT) {
			//var datatablesDT = angular.copy(datatableDT);
			//console.log("Dans poubelle bleue");
			//console.log("datatablesDT = ", datatablesDT);
			var samples = [];
			
			var deletedSamples = {}; 
			// Attention iil n'y a pas forcement concordance des index de displayResult et allResult car si pagination
			// index max de displayResult = max d'elt par page
			// On pourrait avoir approche avec splice mais quand plusieurs splice successifs sur un tableau
			// il faut tenir compte des modifications d'index.
			
			// Utilisation d'un filtre : conserver dans datatablesDT.displayResult uniquement les samples
			// qui n'ont pas ete selectionnés par l'utilisateur pour suppression :
			
			datatablesDT.displayResult = datatablesDT.displayResult.filter(function(elt) { 
				if (elt.line.selected) {
					//console.log("sample à retirer de la liste : ", elt.data.code);
					deletedSamples[elt.data.code]="toto";
					return false;
				} else {
					return true;
				}
			});
				
			// Utilisation d'un filtre : conserver dans datatablesDT.allResult uniquement les samples
			// dont le code n'existe pas dans le hash deletedSamples:
			datatablesDT.allResult = datatablesDT.allResult.filter(function(elt) { 
				if (deletedSamples.hasOwnProperty(elt.code)){
					//console.log("XXXXXXXXXXXXXXXX sample à retirer de la liste : ", elt.code);
					return false;
				} else {
					return true;
				}
			});
			datatablesDT.totalNumberRecords = datatablesDT.allResult.length;
			//console.log("allResult=", datatablesDT.allResult);
			//console.log("Total elt = " , datatablesDT.allResult.length);
			//console.log("sortie poubelle bleue");
		} // end poubelleBleue
		

		
		
		
		
		} // end var toolsService
			
		return toolsService;
	}]);

	
	
