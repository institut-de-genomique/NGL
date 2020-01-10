"use strict";

angular.module('home').controller('DetailsCtrl', ['$scope', '$http', '$routeParams','datatable', 'basket', 'lists', '$window', 
                                                  function($scope, $http, $routeParams, datatable, basket, lists, $window) {
	
	var datatableConfig = {
			pagination:{
				active:false
			},		
			search:{
				active:false
			},
			order:{
				active:true,
				mode:'local'
			},
			save:{
				active:true,
				mode:'local',
				keepEdit:false,
				changeClass:false,
				showButton:false
			},
			edit : {
				active:true,
				withoutSelect:true,
				showButton:false
			},
			hide : {
				active : true,
				showButton:false,
				showButtonColumn:true				
			},
			remove:{
				active:true,
				withEdit:true,
				mode:'local',
				showButton:false,
				callback : function(datatable){
					var dataInitial = $scope.basket.get();
					var dataFinal = []; 
					var dataDatatable = $scope.datatable.getData();
					for(var i = 0; i < dataInitial.length ; i++){
						for(var j = 0; j < dataDatatable.length ; j++){
							if(dataInitial[i].code === dataDatatable[j].code){
								dataFinal.push(dataInitial[i]);
								break;
							}
						}
					}
					$scope.basket.reset();
					$scope.basket.add(dataFinal);
				}
			},
			cancel : {
				showButton:false
			},
			exportCSV : {
				active:true,
				showButton:false,
				delimiter:','
			},
			lines : {
				trClass : function(value){
					if(value.valid === "TRUE"){
		    			return "success";
		    		}else if(value.valid === "FALSE") {
		    			return "danger";
		    		}		    		
				}
			},
			columns : [
					    
						{  	property:"y+x", //first position to be compatible with the lapchip sample import
							header: Messages("plates.table.well.yx"),
					    	type :"String",
					    	edit:false,
					    	hide:true,
					    	order:true
						},
						{  	property:"name",
					    	header: Messages("plates.table.well.name"),
					    	type :"String",
					    	hide:true,
					    	order:true
						},
						{  	property:"code",
					    	header: Messages("plates.table.well.code"),					    	
					    	type :"String",
					    	hide:true,
					    	order:true
						},
						{  	property:"typeName",
					    	header: Messages("plates.table.typeName"),
					    	type :"String",
					    	hide:true,
					    	order:true
						},
						{  	property:"name.substring(0, name.indexOf('O',0))",
					    	header: Messages("plates.table.materialName"),					    	
					    	type :"String",
					    	hide:true,
					    	order:true
						},
						{  	property:"typeMaterial",
					    	header: Messages("plates.table.materialType"),
					    	type :"String",
					    	hide:true,
					    	order:true
						},
						{  	property:"x",
					    	header: Messages("plates.table.well.x"),
					    	type :"number",
					    	edit:true,
					    	hide:true,
					    	order:true
						}, 
						{  	property:"y",
					    	header: Messages("plates.table.well.y"),
					    	type :"String",
					    	edit:true,
					    	hide:true,
					    	order:true
						}
					]
		};
	
	var init = function(){
		$scope.clearMessages();		
		$scope.datatable = datatable(datatableConfig);
		$scope.plate = {code:undefined, wells:undefined, typeCode:undefined, typeName:undefined, validQC:'UNSET', validRun:'UNSET'};
		$scope.lists = lists;
		if(angular.isUndefined($scope.getHomePage())){
			$scope.setHomePage('search');
			$scope.addTabs({label:Messages('plates.tabs.search'),href:jsRoutes.controllers.plates.tpl.Plates.home("search").url,remove:false});
			$scope.addTabs({label:$routeParams.code,href:jsRoutes.controllers.plates.tpl.Plates.get($routeParams.code).url,remove:false});			
			$scope.activeTab($scope.getTabs(1));
		}
		
		if(angular.isUndefined($scope.getBasket())){
			$scope.basket = basket();			
			$scope.setBasket($scope.basket);			
		}else{			
			$scope.basket = $scope.getBasket();
		}
		
		//on traite le details
		if($routeParams.code === 'new'){
			$scope.datatable.setData($scope.basket.get(),$scope.basket.get().length);
			$scope.edit();
		}else{
			$http.get(jsRoutes.controllers.plates.api.Plates.get($routeParams.code).url).success(function(data) {
				$scope.plate = data;
				$scope.datatable.setData(data.wells, data.wells.length);
				$scope.datatable.addData($scope.basket.get());	
				if($scope.isEditMode()){
					$scope.edit();
				}else {
					$scope.unedit();
				}
			});		
		}			
	};
		
	/**
	 * Pass in edit mode
	 */
	$scope.edit = function(){
		$scope.setEditConfig(true);		
		$scope.datatable.setEdit();
		$scope.datatable.setShowButton('remove', true);
		if($scope.isHomePage('search') && !$scope.isBackupTabs()){
			$scope.backupTabs();
			$scope.resetTabs();
			$scope.addTabs({label:Messages('plates.tabs.searchmanips'),href:jsRoutes.controllers.plates.tpl.Plates.home("new").url,remove:false});
			$scope.addTabs({label:$scope.plate.code,href:jsRoutes.controllers.plates.tpl.Plates.get($scope.plate.code).url,remove:false});
			$scope.activeTab(1);
			//reinit datatable and form
			$scope.setDatatable(undefined);	
			$scope.setForm(undefined);			
		}
	};
	
	/**
	 * Remove all change
	 */
	$scope.unedit = function(){
		$scope.clearMessages();
		$scope.setEditConfig(false);
		$scope.datatable.cancel();
		$scope.datatable.setShowButton('remove', false);
		if($scope.isHomePage('search') && $scope.isBackupTabs()){
			$scope.restoreBackupTabs();
			$scope.activeTab(1);
			$scope.setDatatable(undefined);	
			$scope.setForm(undefined);			
		}		
	}
	/**
	 * delete a plate
	 * 
	 */
	$scope.remove = function(){
		$scope.clearMessages();
		
		var r= $window.confirm( Messages("plates.remove.confirm", $scope.plate.code));
		if (r){
			$http["delete"](jsRoutes.controllers.plates.api.Plates["remove"]($scope.plate.code).url).
			success(function(data, status, headers, config){
				$scope.plate = {code:undefined, wells:undefined, typeCode:undefined, typeName:undefined};
				if($scope.isHomePage('search') && $scope.isBackupTabs()){
					$scope.restoreBackupTabs();
					$scope.setDatatable(undefined);	
					$scope.setForm(undefined);			
				}
				
				if($scope.isHomePage('search') && !angular.isUndefined($scope.getDatatable())){
					$scope.getDatatable().searchWithLastParams();
				}
				$scope.removeTab($scope.getActiveTabIndex());
				$scope.activeTab(0, true);				
										
			}).error(function(data, status, headers, config){
				$scope.message.clazz="alert alert-danger";
				$scope.message.text=Messages('plates.msg.delete.error');
			});
		}
				
	}
	
	/**
	 * Configure edit and remote on datatable
	 */
	$scope.setEditConfig = function(value){
		$scope.editMode = value;
		//var config = $scope.datatable.getConfig();
		//config.edit.active=value;		
		//config.remove.active=value;
		//config.save.active=value;
		//$scope.datatable.setConfig(config);
		if(value){
			$scope.startEditMode();								
		}else{
			$scope.stopEditMode();
		}						
	};
	
	/**
	 * Save the entire plate
	 */
	$scope.save = function(){
		$scope.clearMessages();
		$scope.datatable.save();
		$scope.plate.wells = $scope.datatable.getData();
		
		$http.post(jsRoutes.controllers.plates.api.Plates.save().url, $scope.plate).
			success(function(data, status, headers, config){
				$scope.plate=data;
				$scope.datatable.setData(data.wells,data.wells.length);
				$scope.basket.reset();
				$scope.message.clazz="alert alert-success";
				$scope.message.text=Messages('plates.msg.save.sucess')
				$scope.edit();				
				if($scope.isHomePage('creation')){
					$scope.setTab(1,{label:$scope.plate.code,href:jsRoutes.controllers.plates.tpl.Plates.get($scope.plate.code).url,remove:false});
					$scope.activeTab(1);
				}			
		}).error(function(data, status, headers, config){
				$scope.message.clazz="alert alert-danger";
				$scope.message.text=Messages('plates.msg.save.error');
				
				var columns = $scope.datatable.getColumnsConfig();
				var msg = {};
				for(var i = 0; i < $scope.plate.wells.length; i++){
					var isError = false;
					if(!angular.isUndefined(data["wells["+i+"]"])){
						isError = true;
						if(angular.isUndefined(msg["["+i+"] : "+$scope.plate.wells[i].name])){
							msg["["+i+"] : "+$scope.plate.wells[i].name] = {};
						}
						msg["["+i+"] : "+$scope.plate.wells[i].name].global = data["wells["+i+"]"];
					}
					for(var j = 0; j < columns.length ; j++){
						if(!angular.isUndefined(data["wells["+i+"]."+columns[j].property])){
							$scope.edit();
							var error = data["wells["+i+"]."+columns[j].property];
							var property = columns[j].property;
							var objError = {};
							objError[property] = error;
							$scope.datatable.addErrors(i, objError);
							isError = true;
							if(angular.isUndefined(msg["["+i+"] : "+$scope.plate.wells[i].name])){
								msg["["+i+"] : "+$scope.plate.wells[i].name] = {};
							}
							msg["["+i+"] : "+$scope.plate.wells[i].name][columns[j].property] = data["wells["+i+"]."+columns[j].property];
						}						
					}		
					if(isError){
						//$scope.plate.wells[i].line.trClass = "danger";						
					}else{
						//$scope.plate.wells[i].line.trClass = "success";
					}
				} 
				
				
				$scope.message.details = msg;
				$scope.message.isDetails = true;
		});
	};
	
	$scope.clearMessages  = function(){
		$scope.message = {clazz : undefined, text : undefined, showDetails : false, isDetails : false, details : []};
	}
	/**
	 * Compute the coordinates
	 */
	$scope.computeXY = function(){
		var wells = $scope.datatable.displayResult;
		var nbCol = 12;
		var nbLine = ['A', 'B', 'C', 'D', 'E', 'F', 'G', 'H'];
		var x = 0;
		for(var i = 0; i < nbCol ; i++){
			for(var j = 0; j < nbLine.length; j++){
				if(x < wells.length){
					wells[x].data.y = nbLine[j]+'';
					wells[x].data.x = i+1;					
				}
				x++;
			}
		}		
	};
	
	$scope.displayCellPlaque =function(x, y){
		var wells = $scope.datatable.displayResult;
		if(!angular.isUndefined(wells)){
	        for (var i = 0; i <wells.length; i++) {
		         if (wells[i].data.x === (x) && wells[i].data.y===(y+'')) {
		        	 return wells[i].data.name.replace(/_/g,' ');
		         }
	        }
		}
        return "------";
     }
	
	$scope.getClass = function(x, y){
		var wells = $scope.datatable.displayResult;
		if(!angular.isUndefined(wells)){
	        for (var i = 0; i <wells.length; i++) {
		         if (wells[i].data.x === (x) && wells[i].data.y===(y+'')) {
		        	 var well = wells[i];
		        	 if(well.data.valid === "FALSE"){
		        		 return "alert alert-danger hidden-print";
		        	 }else if(well.data.valid === "TRUE"){
		        		 return "alert alert-success hidden-print";
		        	 }	        	
		         }
	        }
		}
        return "hidden-print";
     }
	
	init();
}]);
