"use strict";

angular.module('ngl-sq.barCodeSearchServices', []).
factory('barCodeSearchService', ['$http','$q', function($http,$q){

	var searchService = {
			response: undefined,
			form: undefined,
			search : function() { 
				this.response = {};
            	var that = this;
            	var promise = [];        	
            	if(!angular.isUndefined(this.form)){
            		$http.get(jsRoutes.controllers.containers.api.ContainerSupports.get(this.form.code).url).then(function(result){
            			that.response.support = result.data;
            			if(that.response.support.categoryCode.indexOf('tube')>=0 || that.response.support.categoryCode.indexOf('mapcard')>=0){
            				$http.get(jsRoutes.controllers.containers.api.Containers.list().url, {params:{supportCodeRegex:that.response.support.code}}).then(function(result){
            					that.response.containers = result.data;
            				});
            			}
            		});
        			that.form = undefined;
            		angular.element("#scan").focus();
            	}else{
            		this.form = undefined;
            		angular.element("#scan").focus();
            	}
            }
	}
	return searchService;
}]);