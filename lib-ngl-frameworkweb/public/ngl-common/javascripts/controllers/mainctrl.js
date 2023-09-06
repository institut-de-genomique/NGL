"use strict";

angular.module('home').controller('TabMainCtrl', ['$scope', '$location', 'mainService', 'tabService', function($scope, $location, mainService, tabService){
	
	$scope.hideTabs = tabService.hideTabs;
	/**
	 * "public" function to get all tabs or one
	 */
	$scope.getTabs =  function(index){
		return tabService.getTabs.call(tabService, index);
	}
	/**
	 * "public" function with the condition to show (or not) the icon which permits to remove the link
	 */
	$scope.canBeRemoved = function(value, index) {
		return tabService.canBeRemoved.call(tabService, value, index);
	}
	/**
	 * "public" function to get a tab
	 */
	$scope.getTab = function(index){		
		return tabService.getTab.call(tabService, index);
	}
	/**
	 * "public" function to reset all tabs
	 */
	$scope.resetTabs = function(){
		tabService.resetTabs.call(tabService)
	};
	/**
	 * "public" function to add tabs
	 */
	$scope.addTabs = function(newtabs){
		tabService.addTabs.call(tabService, newtabs);
	}
	/**
	 * "public" function to set tab to a specific index
	 */
	$scope.setTab = function(index, tab){
		tabService.setTab.call(tabService, index, tab);
	}
	/**
	 * "public" function to remove one tab
	 */
	$scope.removeTab = function(index){
		tabService.removeTab.call(tabService, index);
	}
	/**
	 * "public" function to remove current tab (if inactive) or re-init menu
	 */
	$scope.removeOrKeepOnlyActiveTab =  function(index, $event, keepLastActiveTab) {	
		tabService.removeOrKeepOnlyActiveTab.call(tabService, index,$event, keepLastActiveTab); 
	}
		
	/**
	 * "public" function to set one element of list active
	 */
	$scope.activeTab = function(value, changeLocation){
		tabService.activeTab.call(tabService, value, changeLocation);
	}
	
	$scope.toggleTabs = function(){
		tabService.toggleTabs.call(tabService); 
	}
		
	$scope.setHideTabs  = function(){
		tabService.setHideTabs.call(tabService); 
	}
	/**
	 * "public" function to backup the current tabs
	 */
	$scope.backupTabs = function(){
		tabService.backupTabs.call(tabService); 
	}
	/**
	 * "public" function to know if tabs is backuped
	 */
	$scope.isBackupTabs = function(){
		tabService.isBackupTabs.call(tabService); 
	}
	/**
	 * "public" function to restore the backup tabs
	 */
	$scope.restoreBackupTabs = function(){
		tabService.restoreBackupTabs.call(tabService); 
	}
	
	/**
	 * "public" function to keep the basket when we switch views
	 */
	$scope.getBasket  = function(){
		return mainService.getBasket.call(mainService);  
	}
	
	/**
	 * function to return the basket
	 */
	$scope.setBasket  = function(basket){
		mainService.setBasket.call(mainService,basket); 
	}
	
	/**
	 * function to keep the form when we switch views
	 */
	$scope.getForm  = function(){
		return mainService.getForm.call(mainService);
	}
	
	/**
	 * function to return the search form
	 */
	$scope.setForm = function(value){
		mainService.setForm.call(mainService,value);  
	}
	
	/**
	 * function to keep the datatable when we display a detail of one element
	 */
	$scope.setDatatable = function(datatable){
		mainService.setDatatable.call(mainService,datatable); 
	}
	
	/**
	 * function to return the datatable
	 */
	$scope.getDatatable = function(){
		return mainService.getDatatable.call(mainService); 
	}
	
	/**
	 * function to set the origine of a page
	 */
	$scope.getHomePage = function(){
		return mainService.getHomePage.call(mainService); 
	}
	/**
	 * function to return the origine of a page
	 */
	$scope.setHomePage  = function(value){
		mainService.setHomePage.call(mainService,value); 
	}
	/**
	 * Test if home page equal value
	 */
	$scope.isHomePage = function(value){
		return mainService.isHomePage.call(mainService,value); 
	}
	/**
	 * Start edition in details page
	 */
	$scope.startEditMode = function(){
		mainService.startEditMode.call(mainService); 
	}
	/**
	 *  Stop edition in details page
	 */
	$scope.stopEditMode = function(){
		mainService.stopEditMode.call(mainService); 
	}
	/**
	 * Edition mode status
	 */
	$scope.isEditMode = function(){
		return mainService.isEditMode.call(mainService);
	}
	
}]);



angular.module('home').factory('tabService', ['$location', function($location){
	//contain each tab of on element of the datatable
		 return {
			tabs : [],
			bcktabs : undefined,
			
			/**
			 * "private" function to keep only the first tab and an eventually an other tab
			 * (if active) to remember the last selection
			 */
			keepOnlyActiveTab : function(keepLastActiveTab, $event) { 
				var firstTab = this.tabs[0];
				var newtabs = [];
				newtabs[0] = firstTab;		
				//if we want to have the last active tab ...
				if (keepLastActiveTab) {
					var activeTabIndex = this.getActiveTabIndex(); 
					if (activeTabIndex !== null && activeTabIndex > 0) {
						newtabs[1] = this.tabs[activeTabIndex]; 
					}
				}
				//.. end
				this.tabs = newtabs;
				this.activeTab(this.tabs.length-1, true);		
				
				$event.preventDefault();
			},
			
			/**
			 * "private" function to get the index of the active tab
			 */
			getActiveTabIndex : function(){
				for(var i = 0; i < this.tabs.length; i++){				
					if(this.tabs[i].clazz == 'active'){
						return i;
					}
				}
				return null;
			},
				
			hideTabs : { 
				hide:false,
				clazz:'fa fa-expand'
			},
		
			/**
			 * "public" function to get all tabs or one
			 */
			getTabs : function(index){
				if(angular.isUndefined(index)){
					return this.tabs;
				}else{
					return this.tabs[index];
				}
				
			},
		
			/**
			 * "public" function with the condition to show (or not) the icon which permits to remove the link
			 */
			canBeRemoved : function(value, index) {
				return ((value.remove) && ((index>0) || (index==0) && (this.getTabs().length > 1))); 
			},	
		
			/**
			 * "public" function to get a tab
			 */
			getTab : function(index){		
				return this.tabs[index];				
			},
		
			/**
			 * "public" function to reset all tabs
			 */
			resetTabs : function(){
				this.tabs = [];
			},
		
			/**
			 * "public" function to add tabs
			 */
			addTabs : function(newtabs){
				if(angular.isArray(newtabs)){
					for(var i = 0; i < newtabs.length; i++){
						this.tabs.push(newtabs[i]);
					}
				}else{
					this.tabs.push(newtabs);
				}
				
				var doubledTab = [];
		
				for(var i = 0; i < this.tabs.length; i++) {
				    var valueIsInArray = false;
		
				    for(var j = 0; j < doubledTab.length; j++) {
				    	if(doubledTab[j].label === this.tabs[i].label && doubledTab[j].href === this.tabs[i].href) {
				            valueIsInArray = true;
				        }
				    }
		
				    if(valueIsInArray) {
				    	this.tabs.splice(i, 1); 
				    } else {
				        doubledTab.push(this.tabs[i]);
				    }
				}
			},
		
			/**
			 * "public" function to set tab to a specific index
			 */
			setTab : function(index, tab){
				this.tabs[index] = tab;	
			},
		
			/**
			 * "public" function to remove one tab
			 */
			removeTab : function(index){
				this.tabs.splice(index,1);
			},
		
			/**
			 * "public" function to remove current tab (if inactive) or re-init menu
			 */
			removeOrKeepOnlyActiveTab : function(index, $event, keepLastActiveTab) {		 
				if (index != 0) {
					var activeTabIndex = this.getActiveTabIndex(); 
					this.removeTab(index);
					if (index == activeTabIndex || null == activeTabIndex) {
						this.activeTab(0, true);	
					}
				}
				else {
					this.keepOnlyActiveTab(keepLastActiveTab, $event);
				} 
			    $event.preventDefault();
			    $event.stopPropagation(); 
			},
		
			
			/**
			 * "public" function to set one element of list active
			 */
			activeTab : function(value, changeLocation){
				var tab = undefined;
				if(angular.isNumber(value)){
					tab = this.tabs[value];
				}else if(angular.isObject(value)){
					tab = value;
				}
				
				if(!angular.isUndefined(tab)){
					tab.clazz='active';
					for(var i = 0; i < this.tabs.length; i++){
						if(this.tabs[i].href != tab.href){
							this.tabs[i].clazz='';
						}
					}
				} else{
					for(var i = 0; i < this.tabs.length; i++){				
						this.tabs[i].clazz='';
					}
				}
				
				if(changeLocation){
					$location.url(tab.href);
				}
			},
			
			toggleTabs : function(){
				this.hideTabs.hide = !this.hideTabs.hide;
				if(this.hideTabs.hide){
					this.hideTabs.clazz='fa fa-compress';
				}else{
					this.hideTabs.clazz='fa fa-expand';
				}
			},
			
			setHideTabs :  function(){
				this.hideTabs.hide = true;
				this.hideTabs.clazz='fa fa-compress';
			},
			
			/**
			 * "public" function to backup the current tabs
			 */
			backupTabs : function(){
				this.bcktabs = angular.copy(this.tabs);
			},
			
			/**
			 * "public" function to know if tabs is backuped
			 */
			isBackupTabs : function(){
				return !angular.isUndefined(this.bcktabs);
			},
			
			/**
			 * "public" function to restore the backup tabs
			 */
			restoreBackupTabs : function(){
				this.tabs = angular.copy(this.bcktabs);
				this.bcktabs = undefined;
			}			
		};			
}]);

angular.module('home').factory('mainService', function(){
	//contain each tab of on element of the datatable
		return {
			cache : {},
			errors: {},
			basketMaster : undefined,
			datatableMaster : undefined,
			form : undefined,
			homePage : undefined, //the home page who called the ctrl
			editMode : false, //active or not the edit mode for the details page
			
			/**
			 * "public" function to reset all errors
			 */
			resetErrors : function(){
				 this.errors = {};
			},
			/**
			 * "public" function to add an error
			 */
			addErrors : function(objectName, data){
				for(var key in data){
				 	this.errors[objectName+"."+key] = "";
				 	for(var i=0;i<data[key].length;i++){
				 		this.errors[objectName+"."+key] += data[key][i]+" ";
				 	}
				}
			},
			getError : function(key){
				return this.errors[key];
			},
			/**
			 * "public" function to keep the basket when we switch views
			 */
			getBasket : function(){
				return this.basketMaster;
			},
			
			/**
			 * function to return the basket
			 */
			setBasket : function(basket){
				this.basketMaster = basket;
			},
			
			/**
			 * function to keep the form when we switch views
			 */
			getForm : function(key){
				if(key && this.form === undefined){
					this.form = this.getLocalSessionStorage(key);
				}
				return this.form;
			},
			
			/**
			 * function to return the search form
			 */
			setForm : function(value, key){
				this.form = angular.copy(value);
				if(key){
					this.setLocalSessionStorage(key, this.form);
				}				
			},
			
			getLocalSessionStorage : function(key){
				if(sessionStorage){
					return (sessionStorage.getItem(key) !== null)?JSON.parse(sessionStorage.getItem(key)):undefined;
				}
				return undefined;
			},
			
			setLocalSessionStorage : function(key, object){
				if(sessionStorage){
					return sessionStorage.setItem(key, JSON.stringify(object));
				}				
			},
			
			/**
			 * function to keep the datatable when we display a detail of one element
			 */
			setDatatable : function(datatable){
				this.datatableMaster = datatable;
			},
			
			/**
			 * function to return the datatable
			 */
			getDatatable : function(){
				return this.datatableMaster;
			},
			
			/**
			 * function to set the origine of a page
			 */
			getHomePage : function(){
				return this.homePage;
			},
			
			/**
			 * function to return the origine of a page
			 */
			setHomePage : function(value){
				this.homePage = value;
			},
			/**
			 * Test if home page equal value
			 */
			isHomePage : function(value){
				return this.homePage === value;
			},
			/**
			 * Start edition in details page
			 */
			startEditMode : function(){
				this.editMode = true;
			},
			/**
			 *  Stop edition in details page
			 */
			stopEditMode : function(){
				this.editMode = false;
			},
			/**
			 * Edition mode status
			 */
			isEditMode : function(){
				return this.editMode;
			},
			
			put : function(name, value){
				this.cache[name] = value;
			},
			get : function(name){
				return this.cache[name];
			},
			remove : function(name){
				this.cache[name] = undefined;
			}
		};			
});
