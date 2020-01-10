 "use strict";
 
 angular.module('home').controller('CreationKitsCtrl', ['$scope', 'datatable','mainService','tabService','$q','$http','$filter','lists','$routeParams','$location', function ($scope, datatable,mainService,tabService,$q,$http,$filter,lists,$routeParams,$location) {
	 
	 $scope.datatableConfig = {
				columns : [
					{
						 "header":Messages("reagents.table.catalogCode"),
						 "property":"catalogCode",
						 "order":true,
						 "type":"text",
						 "filter":"codes:'reagentCatalogs'"
					},
					{
			        	 "header":Messages("reagents.table.providerID"),
			        	 "property":"providerID",
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
			        	 "header":Messages("reagents.table.receptionDate"),
			        	 "property":"receptionDate",
			        	 "order":true,
			        	 "type":"date",
			        	 "edit":true
			         },
			         {
			        	 "header":Messages("reagents.table.startToUseDate"),
			        	 "property":"startToUseDate",
			        	 "order":true,
			        	 "type":"date",
			        	 "edit":true
			         },
			         {
			        	 "header":Messages("reagents.table.stopToUseDate"),
			        	 "property":"stopToUseDate",
			        	 "order":true,
			        	 "type":"date",
			        	 "edit":true
			         },
			         {
			        	 "header":Messages("reagents.table.stateCode"),
			        	 "property":"state.code",
			        	 "order":true,
			        	 "listStyle":"bt-select",
			        	 "choiceInList":true,
			        	 "possibleValues": 'lists.getStates()',
			        	 "render":'<div bt-select ng-model="value.data.state.code" bt-options="state.code as state.name for state in lists.getStates()" ng-edit="false"></div>',			        	 
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
					method:function(reagent){
						if(reagent.code === undefined || reagent.code === ""){
							return 'post';
						}
						
						return 'put';
					},
					url: function(reagent){
							if(reagent.code === undefined || reagent.code === ""){
								return jsRoutes.controllers.reagents.api.Reagents.save().url;
							}
							return jsRoutes.controllers.reagents.api.Reagents.update(reagent.code).url;
						},
					showButton : false,
					withoutEdit:true,
					callback : function(datatable, errors){
						 if(errors === 0 && $routeParams.kitCode === undefined){
							$scope.datatableSaved++;
							if($scope.datatableSaved === $scope.datatables.length){
								//All the datatables are now saved
								if($scope.kit.declarationType === "kit"){
									$location.path(jsRoutes.controllers.reagents.tpl.Kits.get($scope.kit.code).url);
								}else{
									$location.path(jsRoutes.controllers.reagents.tpl.Kits.get($scope.boxes[0].code).url);
								}
							}
						 }else if(errors > 0){
							 $scope.message.clazz = 'alert alert-danger';
								$scope.message.text = Messages('reagents.msg.save.error');
								$scope.message.isDetails = false;
						 }
					}
				},
				hide:{
					active:true
				},
				 edit:{
		        	 active:true,
		        	 columnMode:true,
		        	 showButton : true,
		        	 withoutSelect:true,
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
	 $scope.message = {};
	 $scope.kit = {"category":"Kit"};
	 $scope.boxes = [];
	 $scope.datatables = [];
	 $scope.datatableSaved = 0;
	 //$scope.declaration = {"type":"kit"};
	 $scope.objectTypes = [{code:"kit", name:Messages("declarations.kit")},{code:"box", name:Messages("declarations.box")}];
	 
	 $scope.checkCatalogRefCode = function(i){
	 	if($scope.catalogRefCodeVerifications[i].code === $scope.boxes[i].catalogRefCode){
	 		console.log("ok");
	 		$scope.mainService.resetErrors();
	 	}else{
	 		var data = [];
	 		data["catalogRefCode"] = ["Bad value"];
	 		$scope.mainService.addErrors("boxes["+i+"]",data);
	 	}
	 };
	 
	 $scope.scan = function(e, property, propertyName){
			console.log(property);
			console.log(e);
			if(e.keyCode === 9 || e.keyCode === 13){
				property[propertyName] += '_';
				console.log(property);
				e.preventDefault();
			}
	};

	$scope.copyOrderInformations = function(obj){
		obj.orderCode = $scope.orderInformations.orderCode;
		obj.providerOrderCode = $scope.orderInformations.providerOrderCode;
		obj.shippedOrderCode = $scope.orderInformations.shippedOrderCode;
		
		return obj;
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
			$scope.message.details = data;
			$scope.message.isDetails = true;
		});
	};
		
	$scope.removeKit = function(){
		 if($scope.kit !== undefined && $scope.kit.code !== ""  && confirm("Etes vous sur de vouloir supprimer le kit "+$scope.kit.name+" ?")){
			 $http.delete(jsRoutes.controllers.reagents.api.Kits.delete($scope.kit.code).url)
				.success(function(data, status, headers, config) {
					if(data!=null){
						$scope.message.clazz="alert alert-success";
						$scope.message.text=Messages('reagents.msg.delete.sucess');
					}
				})
				.error(function(data, status, headers, config) {
					$scope.message.clazz = 'alert alert-danger';
					$scope.message.text = Messages('reagents.msg.delete.error');
					$scope.mainService.addErrors("kit",data);
					$scope.message.details = data;
					$scope.message.isDetails = true;
				});
		 }
	 };
	 
	 $scope.removeBox = function(index,code){
		 if(confirm("Etes vous sur de vouloir supprimer cette boîte ?")){
			 if(code !== undefined  && code !== ""){
				 $http.delete(jsRoutes.controllers.reagents.api.Boxes.delete(code).url)
					.success(function(data, status, headers, config) {
						if(data!=null){
							for(var i=0;i<$scope.boxes.length;i++){
								if($scope.boxes[i].code === code){
									$scope.datatables.splice(i,1);
									$scope.boxes.splice(i,1);
									break;
								}
							}
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
				 $scope.datatables.splice(index,1);
				 $scope.boxes.splice(index,1);
			 }
		 }
	 }
	 
	 $scope.getClass = function(fieldName){
		 if($scope.mainService.getError(fieldName) !== undefined && $scope.mainService.getError(fieldName) !== ""){
			 return "has-error";
		 }
		 return "";
	 };
	 
	 $scope.lists = lists;
	 $scope.mainService = mainService;
	 
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
	 
	 $scope.boxBarCodeUpdate = function(index, box){
		 var data = $scope.datatables[index].getData();
		 for(var i=0;i<data.length;i++){
			 data[i].boxBarCode = box.barCode;
		 }
		 
		 $scope.datatables[index].setData(data);
	 };
	 
	 $scope.newReagent = function(index, box, reagentCatalog){
		 console.log(index);
		 for(var i = 0; i < $scope.datatables[index].displayResult.length; i++){
			if($scope.datatables[index].displayResult[i].line.edit){
				$scope.datatables[index].saveLocal($scope.datatables[index].displayResult[i].data,i);
			}
		 }
		 $scope.datatables[index].addData([{"category":"Reagent", "catalogCode":reagentCatalog.code, "receptionDate":moment(new Date()).valueOf(), "catalogRefCode":reagentCatalog.catalogRefCode, "declarationType":$scope.kit.declarationType,"boxCatalogRefCode":box.catalogRefCode,"boxBarCode":box.barCode, "state":{code:"N"}}]);
		 $scope.datatables[index].setEdit();
		 console.log($scope.boxes);
	 };
	 
	 $scope.edit = function(){
		 $scope.editMode = true;
	 }
	 
	 $scope.unedit = function(){
		 $scope.editMode = false;
	 }
	 
	 $scope.loadKit = function(){
		 if($scope.kit.code !== undefined && $scope.kit.code !== ""){
			 return $http.get(jsRoutes.controllers.reagents.api.Kits.list().url, {"params":{"code":$scope.kit.code}})
				.success(function(data, status, headers, config) {
					if(data!=null && data.length > 0){
						$scope.kit = data[0];
						$scope.orderInformations.orderCode = $scope.kit.orderCode;
						$scope.orderInformations.providerOrderCode = $scope.kit.providerOrderCode;
						$scope.orderInformations.shippedOrderCode =  $scope.kit.shippedOrderCode;
						$scope.loadKitSuccess = true;
					}
					else{
						$scope.loadKitSuccess = false;
					}
				})
				.error(function(data, status, headers, config) {
					/*$scope.message.clazz = 'alert alert-danger';
					$scope.message.text = Messages('reagents.msg.load.error');
					
					$scope.message.details = data;
					$scope.message.isDetails = true;*/
				});
		 }
	 };
	 
	 $scope.addBox = function(boxCatalog){
		 var boxCatalogCode = undefined;
		 if(boxCatalog !== undefined){
			 boxCatalogCode = boxCatalog.code;
		 }
		 $scope.boxes.push({"category":"Box", "state":{"code":"N"}, "declarationType":$scope.kit.declarationType, "catalogRefCode":boxCatalog.catalogRefCode, "receptionDate":moment(new Date()).valueOf(), "catalogCode":boxCatalogCode});
		 $scope.datatables[$scope.boxes.length-1] = datatable($scope.datatableConfig);
		 $scope.datatables[$scope.boxes.length-1].setData([]);
	 };
	 
	 $scope.loadBoxes = function(){
		 var searchForm = {"code":$scope.kit.code};
		 if($scope.loadKitSuccess === true){
			 searchForm = {"kitCode":$scope.kit.code};
		 }
		 $http.get(jsRoutes.controllers.reagents.api.Boxes.list().url, {"params":searchForm})
				.success(function(data, status, headers, config) {
					if(data!=null && data.length > 0){
						$scope.boxes = data;
						for(var i=0;i<$scope.boxes.length;i++){
							$scope.boxes[i].category = "Box";
							$scope.datatables[i] = datatable($scope.datatableConfig);
							$scope.datatables[i].setData([]);
							var jsonSearch = {"boxBarCode":$scope.boxes[i].barCode};
							$scope.datatables[i].search(jsonSearch);
							$scope.orderInformations.orderCode = $scope.boxes[i].orderCode;
							$scope.orderInformations.providerOrderCode = $scope.boxes[i].providerOrderCode;
							$scope.orderInformations.shippedOrderCode =  $scope.boxes[i].shippedOrderCode;
						}
						$scope.loadBoxSuccess = true;
					}else{
						$scope.loadBoxSuccess = false;
					}
				})
				.error(function(data, status, headers, config) {
					$scope.message.clazz = 'alert alert-danger';
					$scope.message.text = Messages('reagents.msg.save.error');
					$scope.saveInProgress = false;
					$scope.message.details = data;
					$scope.message.isDetails = true;
				});
	 };
	 
	 $scope.saveReagents = function(index, box){
		for(var i = 0; i < $scope.datatables[index].displayResult.length; i++){
			$scope.datatables[index].displayResult[i].data.category = "Reagent";
			$scope.datatables[index].displayResult[i].data.boxBarCode = box.barCode;
			$scope.datatables[index].displayResult[i].data.kitCode = $scope.kit.code;
			$scope.datatables[index].displayResult[i].data.declarationType = $scope.kit.declarationType;
			$scope.copyOrderInformations($scope.datatables[index].displayResult[i].data);
		 }
	 };
	 
	 $scope.saveBoxes = function(){
		var promises = [];
		for(var i=0;i<$scope.boxes.length;i++){
			$scope.boxes[i].kitCode = $scope.kit.code;
			$scope.boxes[i].declarationType = $scope.kit.declarationType;
			$scope.copyOrderInformations($scope.boxes[i]);
			
			if($scope.boxes[i].code === undefined || $scope.boxes[i].code === ""){
				promises.push($scope.saveBox(i,$scope.boxes[i]));
			}else{
				promises.push($scope.updateBox(i,$scope.boxes[i]));
			}
		}
		return promises;
	};
	 
	 $scope.saveBox = function(i,box){
		 return $http.post(jsRoutes.controllers.reagents.api.Boxes.save().url, box)
			.success(function(data, status, headers, config) {
				if(data!=null){
					$scope.message.clazz="alert alert-success";
					$scope.message.text=Messages('reagents.msg.save.sucess');
					$scope.boxes[i] = data;
					var reagents = $scope.datatables[i].displayResult.data;
					if(reagents !== undefined){
						for(var j=0;j<reagents.length;j++){
							reagents[j].boxBarCode = $scope.boxes[i].barCode;
							reagents[j].kitCode = $scope.boxes[i].kitCode;
						}
					}
				}
			})
			.error(function(data, status, headers, config) {
				$scope.message.clazz = 'alert alert-danger';
				$scope.message.text = Messages('reagents.msg.save.error');
				$scope.saveInProgress = false;
				$scope.message.details = data;
				$scope.mainService.addErrors("boxes["+i+"]",data);
				$scope.message.isDetails = true;
			});
	 };
	 
	 $scope.updateBox = function(i,box){
		 return $http.put(jsRoutes.controllers.reagents.api.Boxes.update(box.code).url, box)
			.success(function(data, status, headers, config) {
				if(data!=null){
					$scope.message.clazz="alert alert-success";
					$scope.message.text=Messages('reagents.msg.save.sucess');
							$scope.boxes[i] = data;
				}
			})
			.error(function(data, status, headers, config) {
				$scope.message.clazz = 'alert alert-danger';
				$scope.message.text = Messages('reagents.msg.save.error');
				$scope.saveInProgress = false;
				$scope.message.details = data;
				$scope.mainService.addErrors("boxes["+i+"]",data);
				$scope.message.isDetails = true;
			});
	 };
	 
	 $scope.saveAll = function(){
		$scope.message = {};
		$scope.mainService.resetErrors();
		$scope.saveInProgress = true;
		var promises = [];
		if($scope.kit.declarationType === "kit"){
			if($scope.kit.code === undefined || $scope.kit.code === ""){
				promises.push($scope.saveKit());
			}else{
				promises.push($scope.updateKit($scope.kit.code));
			}
		}
		 $q.all(promises).then(function (res) {
				if($scope.message.text != Messages('reagents.msg.save.error')){
					$scope.message.clazz="alert alert-success";
					$scope.message.text=Messages('reagents.msg.save.sucess');
				}
				if($scope.kit.declarationType === "kit" || $scope.kit.declarationType === "box"){
					promises = $scope.saveBoxes();
				}
				$q.all(promises).then(function (res) {
					if($scope.message.text != Messages('reagents.msg.save.error')){
						$scope.message.clazz="alert alert-success";
						$scope.message.text=Messages('reagents.msg.save.sucess');
						$scope.datatableSaved = 0;
						for(var i=0;i<$scope.boxes.length;i++){
							$scope.saveReagents(i,$scope.boxes[i]);
						}
						for(var i=0;i<$scope.datatables.length;i++){
							$scope.datatables[i].save();
						}
					}
					$scope.saveInProgress = false;
				},function(reason) {
					$scope.message.clazz = "alert alert-danger";
					$scope.message.text = Messages('reagents.msg.save.error');
					
					$scope.message.details = reason.data;
					$scope.message.isDetails = true;
					$scope.saveInProgress = false;
				 });
			},function(reason) {
				$scope.message.clazz = "alert alert-danger";
				$scope.message.text = Messages('reagents.msg.save.error');
				
				$scope.message.details = reason.data;
				$scope.message.isDetails = true;
				$scope.saveInProgress = false;
			 });
	 };
	 
	 $scope.saveKit = function(){
		 $scope.copyOrderInformations($scope.kit);
		 return $http.post(jsRoutes.controllers.reagents.api.Kits.save().url, $scope.kit)
			.success(function(data, status, headers, config) {
				if(data!=null){
					$scope.message.clazz="alert alert-success";
					$scope.message.text=Messages('reagents.msg.save.sucess');
					$scope.kit = data;
				}
			})
			.error(function(data, status, headers, config) {
				$scope.message.clazz = 'alert alert-danger';
				$scope.message.text = Messages('reagents.msg.save.error');
				$scope.mainService.addErrors("kit",data);
				$scope.message.details = data;
				$scope.message.isDetails = true;
			});
	 };
	 
	 $scope.updateKit = function(kitCode){
		 $scope.copyOrderInformations($scope.kit);
		 return $http.put(jsRoutes.controllers.reagents.api.Kits.update(kitCode).url, $scope.kit)
			.success(function(data, status, headers, config) {
				if(data!=null){
					$scope.message.clazz="alert alert-success";
					$scope.message.text=Messages('reagents.msg.save.sucess');
					$scope.kit = data;
				}
			})
			.error(function(data, status, headers, config) {
				$scope.message.clazz = 'alert alert-danger';
				$scope.message.text = Messages('reagents.msg.save.error');

				$scope.message.details = data;
				$scope.message.isDetails = true;
			});
	 };
	 
	 $scope.insertBoxes = function(addBoxes){
		 if($scope.kit.catalogCode !== null && $scope.kit.catalogCode !== undefined && $scope.kit.catalogCode !== ""){
			 $scope.boxes = [];
			 return $http.get(jsRoutes.controllers.reagents.api.BoxCatalogs.list().url, {params:{"kitCatalogCode":$scope.kit.catalogCode}})
				.success(function(data, status, headers, config) {
					if(data!=null){
						if(data !== undefined && data !== null){
							for(var i=0;i<data.length;i++){
								if(addBoxes){
									$scope.boxCatalogs.push(data[i]);
									$scope.addBox(data[i]);
									$scope.insertReagents(i,addBoxes);
								}
							}
						}
					}
				})
				.error(function(data, status, headers, config) {
					
				});
		 }else{
			 $scope.boxes = [];
		 }
	 };
	 
	 $scope.insertReagents = function(boxIndex, addReagents){
		 if($scope.boxes[boxIndex].catalogCode !== null && $scope.boxes[boxIndex].catalogCode !== undefined && $scope.boxes[boxIndex].catalogCode !== ""){
			return $http.get(jsRoutes.controllers.reagents.api.ReagentCatalogs.list().url, {params:{"boxCatalogCode":$scope.boxes[boxIndex].catalogCode}})
					.success(function(data, status, headers, config) {
						if(data!=null){
							if(data !== undefined && data !== null){
								for(var i=0;i<data.length;i++){
									if(addReagents){
										$scope.newReagent(boxIndex, $scope.boxes[boxIndex], data[i]);
									}
								}
							}
						}
					})
					.error(function(data, status, headers, config) {
						
					});
		 }
	 };
	 
	 $scope.getBoxCatalogName = function(code){
		 for(var i=0;i<$scope.boxCatalogs.length;i++){
			 if($scope.boxCatalogs[i].code === code){
				 return $scope.boxCatalogs[i].name;
			 }
		 }
	 };
	 
	 //init
	 var promises = [];
	 $scope.editMode = true;
	 $scope.boxCatalogs = [];
	 $scope.orderInformations = {};
	 $scope.info = {};
	 $scope.catalogRefCodeVerifications = [];
	 
	 $scope.kit.declarationType = "kit";
	 
	 if($routeParams.kitCode !== undefined){
		 $scope.kit.code = $routeParams.kitCode;
		 promises.push($scope.loadKit());
		 $scope.editMode = false;
	 }else{
		 $scope.kit.receptionDate = moment(new Date()).valueOf();
	 }
	 $q.all(promises).then(function (res) {
		 if($routeParams.kitCode !== undefined){
			 $scope.loadBoxes();
		 }
		 $scope.getRefCatalogs();
		 $scope.lists.refresh.experimentTypes();
		 $scope.lists.refresh.kitCatalogs();
		 $scope.lists.refresh.states({"objectTypeCode":"Reagent"});
		 if(angular.isUndefined($scope.getHomePage())){
			 if($routeParams.kitCode !== undefined){
				tabService.addTabs({label:Messages('kitDeclarations.tabs.search'),href:jsRoutes.controllers.reagents.tpl.Kits.home("search").url,remove:false});
				tabService.addTabs({label:$routeParams.kitCode,href:jsRoutes.controllers.reagents.tpl.Kits.get($routeParams.kitCode).url,remove:true});
				tabService.activeTab(1);
			 }else{
				$scope.mainService.setHomePage('new');
				tabService.addTabs({label:Messages('kitDeclarations.tabs.create'),href:jsRoutes.controllers.reagents.tpl.Kits.home("new").url,remove:false});
				tabService.activeTab(0);
			 }
		 }
	 });
}]);