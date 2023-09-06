function SearchMainCtrl($scope){
	//contain each tab of on element of the datatable
	$scope.tabs = [];	
	
	$scope.hideTabs = { 
		hide:false,
		clazz:'icon-resize-full'
	};
	
	$scope.searchTab = {
		clazz:'active'	
	};
	
	/**
	 * function to reset all tabs
	 */
	$scope.resetTabs = function(){
		$scope.tabs = [];
	};
	
	/**
	 * function to reset all tabs
	 */
	$scope.addTabs = function(tabs){
		if(angular.isArray(tabs)){
			for(var i = 0; i < tabs.length; i++){
				$scope.tabs.push(tabs[i]);
			}
		}else{
			$scope.tabs.push(tabs);
		}		
	};
	
	/**
	 * function to keep the basket when we switch views
	 */
	$scope.getBasket = function(){
		return $scope.basketMaster;
	};
	
	/**
	 * function to return the basket
	 */
	$scope.setBasket = function(basket){
		$scope.basketMaster = basket;
	};
	
	/**
	 * function to keep the form when we switch views
	 */
	$scope.getForm = function(){
		return $scope.form;
	};
	
	/**
	 * function to return the form
	 */
	$scope.setForm = function(form){
		$scope.form = form;
	};
	
	/**
	 * function to keep the datatable when we display a detail of one element
	 */
	$scope.setDatatable= function(datatable){
		$scope.datatableMaster = datatable;
	};
	
	/**
	 * function to return the datatable
	 */
	$scope.getDatatable= function(){
		return $scope.datatableMaster;
	};
	
	/**
	 * Set one element of list active
	 */
	$scope.activeTab = function(tab){
		if(angular.isObject(tab)){
			tab.clazz='active';
			$scope.searchTab.clazz='';
			for(var i = 0; i < $scope.tabs.length; i++){
				if($scope.tabs[i].href != tab.href){
					$scope.tabs[i].clazz='';
				}
			}
		}else{
			$scope.searchTab.clazz='active';
			for(var i = 0; i < $scope.tabs.length; i++){				
					$scope.tabs[i].clazz='';
			}
		}
		
	};
	/**
	 * remove one tab
	 */
	$scope.removeTab = function(index){
		$scope.tabs.splice(index,1);
	};
	
	
	$scope.toggleTabs = function(){
		$scope.hideTabs.hide = !$scope.hideTabs.hide;
		if($scope.hideTabs.hide){
			$scope.hideTabs.clazz='icon-resize-small';
		}else{
			$scope.hideTabs.clazz='icon-resize-full';
		}
	};
	
	$scope.setHideTabs =  function(){
		$scope.hideTabs.hide = true;
		$scope.hideTabs.clazz='icon-resize-small';
	};
}