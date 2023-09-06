angular.module('valuationServices', []).factory('valuationService',['$parse', '$filter', 'lists', function($parse, $filter, lists){
    	    		var criterias = undefined;
    	    		var valuationCriteriaClass = function(value, criteriaCode, expressionToParse){
    	    			//init criterias
    	    			if((!criterias || !criterias[criteriaCode]) && lists.getValuationCriterias() && lists.getValuationCriterias().length > 0 ){
    	    				var values = lists.getValuationCriterias();
    	    				criterias = {};
    	    				for(var i = 0 ; i < values.length; i++){
    	    					criterias[values[i].code] = values[i]; 
    	    				}
    	    			}
    	    			
    	    			if (angular.isDefined(criterias) && criteriaCode && criterias[criteriaCode]) {
    	    				var criteria = criterias[criteriaCode];
    	    				var property;
    	    				for(var i = 0; i < criteria.properties.length; i++){
    	    					if(criteria.properties[i].name === expressionToParse){
    	    						property = criteria.properties[i];
    	    						break;
    	    					}
    	    				}
    	    				if(property){
    	    					for(var i = 0; i  < property.expressions.length; i++){
    	    						var expression = property.expressions[i];
    		    					if($parse(expression.rule)({context:value, pValue : $parse(expressionToParse)(value)})){
    		    						return expression.result;
    		    					}
    	    					}
    	    				}
    	    			}
    	    			return undefined;			
    	    		};
    	    		return function() {
    	    			criterias = undefined;
    	    			return {valuationCriteriaClass : valuationCriteriaClass};
    	    		};
    	    	}])