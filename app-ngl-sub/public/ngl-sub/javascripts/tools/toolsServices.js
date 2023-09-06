"use strict";
 
 angular.module('ngl-sub.ToolsServices', []).
	factory('toolsServices', ['$http', 'mainService', 'lists', 'datatable',  
		function($http, mainService, lists, datatable) {

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
		}
		

		
		} // end var toolsService
				
		return toolsService;
	}]);

	
	
