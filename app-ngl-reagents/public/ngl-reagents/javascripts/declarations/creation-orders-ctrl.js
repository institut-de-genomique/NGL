 "use strict";
 
 angular.module('home').controller('CreationOrdersCtrl', ['$scope', 'datatable','mainService','tabService','$q','$http','$filter','lists','$routeParams','$location', function ($scope, datatable,mainService,tabService,$q,$http,$filter,lists,$routeParams,$location) {
	 
	 $scope.datatableConfig = {
				columns : [
					{
						 "header":Messages("reagents.table.catalogCode"),
						 "property":"catalogCode",
						 "order":true,
						 "filter":"codes:'boxCatalogs'"
					},
					{
						 "header":Messages("reagents.table.category"),
						 "property":"category",
						 "order":true,
						 "type":"text"
					},
					{
						 "header":Messages("reagents.table.catalogRefCode"),
						 "property":"catalogRefCode",
						 "order":true,
						 "type":"text",
						 "edit":true
					},
					 {
			        	 "header":Messages("reagents.table.lotNumber"),
			        	 "property":"lotNumber",
			        	 "order":true,
			        	 "type":"text",
			        	 "edit":true
			         },
					{
			        	 "header":Messages("reagents.table.providerID"),
			        	 "property":"providerID",
			        	 "order":true,
			        	 "type":"text",
			        	 "edit":true
			         },
			        
			         {
			        	 "header":Messages("reagents.table.receptionDate"),
			        	 "property":"receptionDate",
			        	 "order":true,
			        	 "type":"date",
			        	 "edit":true
			         },
			         {
			        	 "header":Messages("reagents.table.expirationDate"),
			        	 "property":"expirationDate",
			        	 "order":true,
			        	 "type":"date",
			        	 "edit":true
			         },
			         {
			        	 "header":Messages("reagents.table.stockInformation"),
			        	 "property":"stockInformation",
			        	 "order":true,
			        	 "type":"text",
			        	 "edit":true
			         },
			         {
			        	 "header":Messages("reagents.table.comment"),
			        	 "property":"comment",
			        	 "order":true,
			        	 "type":"text",
			        	 "edit":true
			         },
			         {
			        	 "header":Messages("reagents.table.createUser"),
			        	 "property":"traceInformation.createUser",
			        	 "order":true,
			        	 "type":"text",
			        	 "edit":false
			         },
			         {
			        	 "header":Messages("reagents.table.creationDate"),
			        	 "property":"traceInformation.creationDate",
			        	 "order":true,
			        	 "type":"date",
			        	 "edit":false
			         },
			         {
			        	 "header":Messages("reagents.table.modifyUser"),
			        	 "property":"traceInformation.modifyUser",
			        	 "order":true,
			        	 "type":"text",
			        	 "edit":false
			         },
			         {
			        	 "header":Messages("reagents.table.modifyDate"),
			        	 "property":"traceInformation.modifyDate",
			        	 "order":true,
			        	 "type":"date",
			        	 "edit":false
			         }
				],
				compact:true,
				pagination:{
					active:false
				},		
				search:{
					active:true,
					url:jsRoutes.controllers.reagents.api.Reagents.list().url
				},
				order:{
					mode:'local',
					active:true,
					by:'code'
				},
				remove:{
					active:true,
					mode:"remote",
					url:function(reagent){ return jsRoutes.controllers.reagents.api.Reagents.delete(reagent.code).url;}
				},
				save:{
					active:true,
					mode:'remote',
					value:function(obj){
						//TODO add orderCode etc... before saving
						obj.orderCode = $scope.order.code;
						obj.providerOrderCode = $scope.order.providerCode;
						obj.shippedOrderCode = $scope.order.shippedCode;
						
						return obj;
						
					},
					method:function(reagent){
						if(reagent.code === undefined || reagent.code === ""){
							return 'post';
						}
						
						return 'put';
					},
					url: function(reagent){
						if(reagent.category === "Reagent"){
							if(reagent.code === undefined || reagent.code === ""){
								return jsRoutes.controllers.reagents.api.Reagents.save().url;;
							}
							return jsRoutes.controllers.reagents.api.Reagents.update(reagent.code).url;
						}else{
							if(reagent.code === undefined || reagent.code === ""){
								return jsRoutes.controllers.reagents.api.Boxes.save().url;;
							}
							return jsRoutes.controllers.reagents.api.Boxes.update(reagent.code).url;
						}
					},
					callback : function(datatable, errors){
						 if(errors === 0){
							 $scope.message.clazz="alert alert-success";
							 $scope.message.text=Messages('reagents.msg.save.sucess');
							 $scope.message.isDetails = false;
						 }else if(errors > 0){
							 $scope.message.clazz = 'alert alert-danger';
							 $scope.message.text = Messages('reagents.msg.save.error');
							 $scope.message.details = errors;
							 $scope.message.isDetails = true;
						 }
					},
					showButton : true,
					withoutEdit:true
				},
				hide:{
					active:true
				},
				 edit:{
		        	 active:true,
		        	 columnMode:true, // Mode bandeau du haut
		        	 showButton : true,
		        	 byDefault : false
		         },
				messages:{
					active:false,
					columnMode:true
				},
				exportCSV:{
					active:true,
					showButton:true,
					delimiter:";",
					start:false
				}
	 };
	 
	 $scope.scan = function(e){
			console.log(e);
			if(e.keyCode === 9 || e.keyCode === 13){
				$scope.boxesDatatable.setData($scope.boxes);
				$scope.newBox();
			}
	};
	
	 $scope.getType = function(e, object){
			console.log(e);
			if(e.keyCode === 9 || e.keyCode === 13){
				$http.get(jsRoutes.controllers.reagents.api.BoxCatalogs.list().url, {"params":{"includes":"catalogCode", "catalogRefCode":object.catalogRefCode}})
				.success(function(data, status, headers, config) {
					if(data!=null && data.length === 1){
						object.catalogCode = data[0].code;
						object.declarationType = "box";
						$scope.currentBox = object;
					}else{
						$http.get(jsRoutes.controllers.reagents.api.ReagentCatalogs.list().url, {"params":{"includes":"catalogCode", "catalogRefCode":object.catalogRefCode}})
						.success(function(data, status, headers, config) {
							if(data!=null && data.length === 1){
								object.category = "Reagent";
								object.catalogCode = data[0].code;
								object.declarationType = "box";
								object.boxCatalogRefCode = $scope.currentBox.catalogRefCode;
								object.boxBarCode =  $scope.currentBox.providerID;
							}else{
								$scope.message.clazz = 'alert alert-danger';
								$scope.message.text = Messages('reagents.msg.catalogCode.error');
								$scope.saveInProgress = false;
								$scope.message.isDetails = false;
							}
						})
						.error(function(data, status, headers, config) {
							$scope.message.clazz = 'alert alert-danger';
							$scope.message.text = Messages('reagents.msg.catalogCode.error');
							$scope.saveInProgress = false;
							$scope.message.isDetails = false;
						});
					}
				})
				.error(function(data, status, headers, config) {
				});
			}
	};
	
	$scope.getName = function(){
		 if($scope.kit.code === undefined){
			 return Messages("declarations.kit.creation");
		 }
		 var code = $scope.kit.code;
			 if(code !== undefined){
			 if(code.length > 30){
				 code = code.substring(0,30)+"...";
			 }
			 
			 return code;
		 }
	     return "";
	 };
	 
	 $scope.newBox = function(){
		 $scope.boxes.push({"category":"Box", "receptionDate":moment(new Date()).valueOf(), "state":{code:"N"}});
	 };
	 
	 $scope.getRefCatalogs = function(){
			$http.get(jsRoutes.controllers.reagents.api.KitCatalogs.list().url, {"params":{"includes":"catalogRefCode"}})
			.success(function(data, status, headers, config) {
				if(data!=null){
					$scope.catalogRefCodes = data;
				}
			})
			.error(function(data, status, headers, config) {
				$scope.message.clazz = 'alert alert-danger';
				$scope.message.text = Messages('reagents.msg.save.error');
				$scope.saveInProgress = false;
				$scope.message.details = {"Ref catalogue":["invalid"]};
				$scope.message.isDetails = true;
			});
		};
	 
	 //init
	 var promises = [];
	 $scope.lists = lists;
	 $scope.mainService = mainService;
	 $scope.boxesDatatable = datatable($scope.datatableConfig);
	 $scope.boxesDatatable.setData([]);
	 $scope.boxes = [];
	 $scope.newBox();
	 $scope.editMode = true;
	 $scope.boxCatalogs = [];
	 $scope.order = {};
	 $scope.currentBox = {};
	 $scope.message = {};
	 
	 $scope.editMode = true;
	 
	 if($routeParams.code !== undefined){
		 $scope.order.code = $routeParams.code;
		 $scope.editMode = false;
		 $http.get(jsRoutes.controllers.reagents.api.Boxes.list().url, {"params":{"orderCode":$scope.order.code}})
			.success(function(data, status, headers, config) {
				if(data!=null){
					 $scope.boxesDatatable.setData(data);
				}
			})
			.error(function(data, status, headers, config) {
				$scope.message.clazz = 'alert alert-danger';
				$scope.message.text = Messages('reagents.msg.save.error');
				$scope.saveInProgress = false;
				$scope.message.details = data;
				$scope.message.isDetails = true;
			});
	 }else{
		 $scope.order.receptionDate = moment(new Date()).valueOf();
	 }
	 $q.all(promises).then(function (res) {
		 $scope.getRefCatalogs();
		 $scope.lists.refresh.experimentTypes();
		 $scope.lists.refresh.kitCatalogs();
		 $scope.lists.refresh.states({"objectTypeCode":"Reagent"});
		 if(angular.isUndefined($scope.getHomePage())){
				$scope.mainService.setHomePage('new');
				tabService.addTabs({label:Messages('kitDeclarations.tabs.create'),href:jsRoutes.controllers.reagents.tpl.Kits.home("new").url,remove:false});
				tabService.activeTab(0);
		 }
	 });
}]);