"use strict";

angular.module('home').controller('TabMainCtrlXXX', ['$scope', '$location', function($scope, $location){
	//contain each tab of on element of the datatable
	var tabs = [];
	var bcktabs = undefined;
	var basketMaster = undefined;
	var datatableMaster = undefined;
	var form = undefined;
	var homePage = undefined; //the home page who called the ctrl
	var editMode = false; //active or not the edit mode for the details page
	
	$scope.hideTabs = { 
		hide:false,
		clazz:'fa fa-expand'
	};
	
	/**
	 * "public" function to get all tabs or one
	 */
	$scope.getTabs = function(index){
		if(angular.isUndefined(index)){
			return tabs;
		}else{
			return tabs[index];
		}
		
	};
	
	/**
	 * "public" function with the condition to show (or not) the icon which permits to remove the link
	 */
	$scope.canBeRemoved = function(value, index) {
		return ((value.remove) && ((index>0) || (index==0) && ($scope.getTabs().length > 1))); 
	}
	
	/**
	 * "private" function to keep only the first tab and an eventually an other tab
	 * (if active) to remember the last selection
	 */
	var keepOnlyActiveTab = function(keepLastActiveTab, $event) { 
		var firstTab = tabs[0];
		var newtabs = [];
		newtabs[0] = firstTab;		
		//if we want to have the last active tab ...
		if (keepLastActiveTab) {
			var activeTabIndex = getActiveTabIndex(); 
			if (activeTabIndex !== null && activeTabIndex > 0) {
				newtabs[1] = tabs[activeTabIndex]; 
			}
		}
		//.. end
		tabs = newtabs;
		$scope.activeTab(tabs.length-1, true);		
		
		$event.preventDefault();
	}
	
	/**
	 * "public" function to get a tab
	 */
	$scope.getTab = function(index){		
		return tabs[index];				
	};
	
	/**
	 * "public" function to reset all tabs
	 */
	$scope.resetTabs = function(){
		tabs = [];
	};
	
	/**
	 * "public" function to add tabs
	 */
	$scope.addTabs = function(newtabs){
		if(angular.isArray(newtabs)){
			for(var i = 0; i < newtabs.length; i++){
				tabs.push(newtabs[i]);
			}
		}else{
			tabs.push(newtabs);
		}
		
		var doubledTab = [];

		for(var i = 0; i < tabs.length; i++) {
		    var valueIsInArray = false;

		    for(var j = 0; j < doubledTab.length; j++) {
		        if(doubledTab[j].label === tabs[i].label && doubledTab[j].href === tabs[i].href) {
		            valueIsInArray = true;
		        }
		    }

		    if(valueIsInArray) {
		        tabs.splice(i, 1); 
		    } else {
		        doubledTab.push(tabs[i]);
		    }
		}
	};
	
	/**
	 * "public" function to set tab to a specific index
	 */
	$scope.setTab = function(index, tab){
		tabs[index] = tab;	
	};
	
	/**
	 * "public" function to remove one tab
	 */
	$scope.removeTab = function(index){
		tabs.splice(index,1);
	};
	
	/**
	 * "public" function to remove current tab (if inactive) or re-init menu
	 */
	$scope.removeOrKeepOnlyActiveTab = function(index, $event, keepLastActiveTab) {		 
		if (index != 0) {
			$scope.removeTab(index);
			var activeTabIndex = getActiveTabIndex(); 
			if (index == activeTabIndex || null == activeTabIndex) {
				$scope.activeTab(0, true);	
			}
		}
		else {
			keepOnlyActiveTab(keepLastActiveTab, $event);
		} 
	    $event.preventDefault();
	    $event.stopPropagation(); 
	};
	
		
	/**
	 * "public" function to set one element of list active
	 */
	$scope.activeTab = function(value, changeLocation){
		var tab = undefined;
		if(angular.isNumber(value)){
			tab = tabs[value];
		}else if(angular.isObject(value)){
			tab = value;
		}
		
		if(!angular.isUndefined(tab)){
			tab.clazz='active';
			for(var i = 0; i < tabs.length; i++){
				if(tabs[i].href != tab.href){
					tabs[i].clazz='';
				}
			}
		} else{
			for(var i = 0; i < tabs.length; i++){				
				tabs[i].clazz='';
			}
		}
		
		if(changeLocation){
			$location.url(tab.href);
		}
	};
	
	/**
	 * "private" function to get the index of the active tab
	 */
	var getActiveTabIndex = function(){
		for(var i = 0; i < tabs.length; i++){				
			if(tabs[i].clazz == 'active'){
				return i;
			}
		}
		return null;
	}
	
	$scope.getActiveTabIndex = getActiveTabIndex;
	
	$scope.toggleTabs = function(){
		$scope.hideTabs.hide = !$scope.hideTabs.hide;
		if($scope.hideTabs.hide){
			$scope.hideTabs.clazz='fa fa-compress';
		}else{
			$scope.hideTabs.clazz='fa fa-expand';
		}
	};
	
	$scope.setHideTabs =  function(){
		$scope.hideTabs.hide = true;
		$scope.hideTabs.clazz='fa fa-compress';
	};
	
	/**
	 * "public" function to backup the current tabs
	 */
	$scope.backupTabs = function(){
		bcktabs = angular.copy(tabs);
	}
	
	/**
	 * "public" function to know if tabs is backuped
	 */
	$scope.isBackupTabs = function(){
		return !angular.isUndefined(bcktabs);
	}
	
	/**
	 * "public" function to restore the backup tabs
	 */
	$scope.restoreBackupTabs = function(){
		tabs = angular.copy(bcktabs);
		bcktabs = undefined;
	}
	
	/**
	 * "public" function to keep the basket when we switch views
	 */
	$scope.getBasket = function(){
		return basketMaster;
	};
	
	/**
	 * function to return the basket
	 */
	$scope.setBasket = function(basket){
		basketMaster = basket;
	};
	
	/**
	 * function to keep the form when we switch views
	 */
	$scope.getForm = function(){
		return form;
	};
	
	/**
	 * function to return the search form
	 */
	$scope.setForm = function(value){
		form = value;
	};
	
	/**
	 * function to keep the datatable when we display a detail of one element
	 */
	$scope.setDatatable= function(datatable){
		datatableMaster = datatable;
	};
	
	/**
	 * function to return the datatable
	 */
	$scope.getDatatable= function(){
		return datatableMaster;
	};
	
	/**
	 * function to set the origine of a page
	 */
	$scope.getHomePage = function(){
		return homePage;
	};
	
	/**
	 * function to return the origine of a page
	 */
	$scope.setHomePage = function(value){
		homePage = value;
	};
	/**
	 * Test if home page equal value
	 */
	$scope.isHomePage= function(value){
		return homePage === value;
	};
	/**
	 * Start edition in details page
	 */
	$scope.startEditMode = function(){
		editMode = true;
	};
	/**
	 *  Stop edition in details page
	 */
	$scope.stopEditMode = function(){
		editMode = false;
	};
	/**
	 * Edition mode status
	 */
	$scope.isEditMode = function(){
		return editMode;
	};
}]);
