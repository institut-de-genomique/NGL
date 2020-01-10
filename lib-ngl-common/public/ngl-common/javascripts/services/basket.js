"use strict";

angular.module('basketServices', []).
factory('basket', ['$http', function($http){ //service to manage baskets local
	var constructor = function(iConfig){
		var baskets = {
			config : undefined,
			configMaster : undefined,
			basket : [],
			
			configDefault:{			
			},
			/**
			 * function to keep the basket when we switch views
			 */
			get: function(){
				return this.basket;
			},
			
			/**
			 * function to add an element or several to the basket
			 */
			add: function(element){
				if(angular.isArray(element)){
					for(var i = 0; i < element.length; i++){
						this.basket.push(element[i]);
					}
				}else{
					this.basket.push(element);
				}				
			},
			
			/**
			 * function to remove one element in the basket
			 */
			remove: function(index){
				this.basket.splice(index, 1);
			},
			/**
			 * return the basket size
			 */
			length : function(){
				return this.basket.length;
			},
			/**
			 * Reinitialize the basket
			 */
			reset : function(){
				this.basket = [];
			}
	
	};
	    var settings = $.extend(true, {}, baskets.configDefault, iConfig);
	    baskets.config = angular.copy(settings);
	    baskets.configMaster = angular.copy(settings);    
	    return baskets;
	}
	return constructor;
}]);