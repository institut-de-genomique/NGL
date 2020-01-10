 "use strict";
 
 angular.module('home').controller('CreationKitsCtrl', ['$scope', 'datatable','mainService','tabService','$q','$http','$filter','lists','$routeParams','$location', function ($scope, datatable,mainService,tabService,$q,$http,$filter,lists,$routeParams,$location) {
	 
	 $scope.datatableConfig = {
				columns : [					
			         {
			        	 "header":Messages("reagents.table.name"),
			        	 "property":"name",
			        	 "order":true,
			        	 "type":"text",
			        	 "edit":true
			         },
			         {
			        	 "header":Messages("reagents.table.catalogRefCode"),
			        	 "property":"catalogRefCode",
			        	 "order":true,
			        	 "type":"text",
			        	 "edit":true
			         },
			         {
			        	 "header":Messages("reagents.table.storageConditions"),
			        	 "property":"storageConditions",
			        	 "order":true,
			        	 "type":"text",
			        	 "edit":true
			         },
			         {
			        	 "header":Messages("reagents.table.possibleUseNumber"),
			        	 "property":"possibleUseNumber",
			        	 "order":true,
			        	 "type":"text",
			        	 "edit":true
			         },
			         {
			        	 "header":Messages("reagents.table.code"),
			        	 "property":"code",
			        	 "order":true,
			        	 "type":"text",
			        	 "edit":false
			         },
				],
				compact:true,
				pagination:{
					active:false
				},		
				search:{
					active:true,
					url:jsRoutes.controllers.reagents.api.ReagentCatalogs.list().url
				},
				order:{
					mode:'local',
					active:true,
					by:'code'
				},
				remove:{
					active:true,
					mode:"remote",
					url:function(reagent){ return jsRoutes.controllers.reagents.api.ReagentCatalogs.delete(reagent.code).url;}
				},
				save:{
					active:true,
					url: function(reagent){
						if(reagent.code === undefined || reagent.code === ""){
							return jsRoutes.controllers.reagents.api.ReagentCatalogs.save().url;
						}
						return jsRoutes.controllers.reagents.api.ReagentCatalogs.update(reagent.code).url;
						
					},
					showButton : false,
					callback : function(datatable, errors){
						 if(errors === 0 && $routeParams.kitCatalogCode === undefined){
							$scope.datatableSaved++;
							if($scope.datatableSaved === $scope.datatables.length){
								//All the datatables are now saved
								$location.path(jsRoutes.controllers.reagents.tpl.KitCatalogs.get($scope.kit.code).url);
							}
						 }else if(errors > 0){
							 $scope.message.clazz = 'alert alert-danger';
								$scope.message.text = Messages('reagents.msg.save.error');
								$scope.message.isDetails = false;
						 }
					},
					method:function(reagent){
						if(reagent.code === undefined || reagent.code === ""){
							return 'post';
						}
						
						return 'put';
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
				},
				otherButtons:{
					active:true,
					template:'<button class="btn btn btn-info" ng-click="newReagent($index, box)" ng-disabled="!editMode" title="'+Messages("reagents.add")+'">'+Messages("reagents.add")+'</button>'
				}
	 };
	 $scope.message = {};
	 $scope.kit = {"category":"Kit"};
	 $scope.boxes = [];
	 $scope.datatables = [];
	 $scope.datatableSaved = 0;
	 
	 $scope.removeKit = function(){
		 if($scope.kit !== undefined && $scope.kit.code !== ""  && confirm("Etes vous sur de vouloir supprimer le kit "+$scope.kit.name+" ?")){
			 $http.delete(jsRoutes.controllers.reagents.api.KitCatalogs.delete($scope.kit.code).url)
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
	 
	 $scope.removeBox = function(index,code,name){
		 if(code !== undefined  && code !== ""  && confirm("Etes vous sur de vouloir supprimer la boite "+name+" ?")){
			 $http.delete(jsRoutes.controllers.reagents.api.BoxCatalogs.delete(code).url)
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
		 }else if(confirm("Etes vous sur de vouloir supprimer la boite ?")){
			 $scope.datatables.splice(index,1);
			 $scope.boxes.splice(index,1);
		 }
	 }
	 
	 $scope.getClass = function(fieldName){
		 if($scope.mainService.getError(fieldName) !== undefined && $scope.mainService.getError(fieldName) !== ""){
			 return "has-error";
		 }
		 return "";
	 };
	 
	 $scope.getBoxClass = function(fieldName){
		 if($scope.mainService.getError(fieldName) !== undefined && $scope.mainService.getError(fieldName) !== ""){
			 return "box-error";
		 }
		 return "";
	 };
	 
	 $scope.lists = lists;
	 $scope.mainService = mainService;
	 
	 $scope.getName = function(){
		 if($scope.kit.code === undefined){
			 return Messages("catalogs.kit.creation");
		 }
		 var name = $scope.kit.name;
			 if(name !== undefined){
			 if(name.length > 60){
				 name = name.substring(0,60)+"...";
			 }
			 
			 return name;
		 }
	     return "";
	 }
	 
	 $scope.newReagent = function(index, box){
		 console.log(index);
		 for(var i = 0; i < $scope.datatables[index].displayResult.length; i++){
			if($scope.datatables[index].displayResult[i].line.edit){
				$scope.datatables[index].saveLocal($scope.datatables[index].displayResult[i].data,i);
			}
		 }
		 $scope.datatables[index].addData([{"category":"Reagent"}]);
		 $scope.datatables[index].setEdit();
		 console.log($scope.boxes);
	 };
	 
	 $scope.edit = function(){
		 for(var i=0;i<$scope.datatables.length;i++){
			 $scope.datatables[i].config.edit.active = true;
		 }
		 $scope.editMode = true;
	 }
	 
	 $scope.unedit = function(){
		 for(var i=0;i<$scope.datatables.length;i++){
			 $scope.datatables[i].config.edit.active = false;
		 }
		 $scope.editMode = false;
	 }
	 
	 $scope.loadKit = function(){
		 if($scope.kit.code !== undefined && $scope.kit.code !== ""){
			 return $http.get(jsRoutes.controllers.reagents.api.KitCatalogs.get($scope.kit.code).url)
				.success(function(data, status, headers, config) {
					if(data!=null){
						$scope.kit = data;
					}
				})
				.error(function(data, status, headers, config) {
					$scope.message.clazz = 'alert alert-danger';
					$scope.message.text = Messages('reagents.msg.load.error');
					
					$scope.message.details = data;
					$scope.message.isDetails = true;
				});
		 }
	 };
	 
	 $scope.addBox = function(){
		 $scope.boxes.push({"category":"Box"});
		 $scope.datatables[$scope.boxes.length-1] = datatable($scope.datatableConfig);
		 $scope.datatables[$scope.boxes.length-1].setData([]);
	 };
	 
	 $scope.loadBoxes = function(){
		 if($scope.kit.code !== undefined && $scope.kit.code !== ""){
			 $http.get(jsRoutes.controllers.reagents.api.BoxCatalogs.list().url, {"params":{"kitCatalogCode":$scope.kit.code}})
				.success(function(data, status, headers, config) {
					if(data!=null){
						$scope.boxes = data;
						for(var i=0;i<$scope.boxes.length;i++){
							$scope.boxes[i].category = "Box";
							$scope.datatables[i] = datatable($scope.datatableConfig);
							$scope.datatables[i].setData([]);
							var jsonSearch = {"boxCatalogCode":$scope.boxes[i].code,"kitCatalogCode":$scope.kit.code};
							$scope.datatables[i].search(jsonSearch);
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
		 }
	 };
	 
	 $scope.saveReagents = function(index, box){
		for(var i = 0; i < $scope.datatables[index].displayResult.length; i++){
			$scope.datatables[index].displayResult[i].data.category = "Reagent";
			$scope.datatables[index].displayResult[i].data.boxCatalogCode = box.code;
			$scope.datatables[index].displayResult[i].data.kitCatalogCode = $scope.kit.code;
		 }
	 };
	 
	 $scope.getBoxeTabClass = function(index){
		return (index === 0)?"active":"";
	 };
	 
	 $scope.saveBoxes = function(){
		var promises = [];
		if($scope.kit.code !== undefined && $scope.kit.code !== ""){
			for(var i=0;i<$scope.boxes.length;i++){
				$scope.boxes[i].kitCatalogCode = $scope.kit.code;
				if($scope.boxes[i].code === undefined || $scope.boxes[i].code === ""){
					if ($scope.boxes[i].active === undefined) {
						$scope.boxes[i].active = false;
					}
					promises.push($scope.saveBox(i,$scope.boxes[i]));
					console.log("box info : "+ $scope.boxes[i].name + " - " + $scope.boxes[i].active);
				}else{
					promises.push($scope.updateBox(i,$scope.boxes[i]));					
				}
			}
		}else{
			//TODO:error on the field
		}
		return promises;
	 };
	 
	 $scope.updateBox = function(i,box){
		 return $http.put(jsRoutes.controllers.reagents.api.BoxCatalogs.update(box.code).url, box)
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
	 
	 $scope.saveBox = function(i,box){
		 return $http.post(jsRoutes.controllers.reagents.api.BoxCatalogs.save().url, box)
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
		if($scope.kit.code === undefined){
			promises.push($scope.saveKit());
		}else{
			promises.push($scope.updateKit());
		}
		 $q.all(promises).then(function (res) {
				if($scope.message.text != Messages('reagents.msg.save.error')){
					$scope.message.clazz="alert alert-success";
					$scope.message.text=Messages('reagents.msg.save.sucess');
					//$location.path(jsRoutes.controllers.reagents.tpl.KitCatalogs.get($scope.kit.code).url);
				}
				promises = $scope.saveBoxes();
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
	 }
	 
	 $scope.saveKit = function(){
		 return $http.post(jsRoutes.controllers.reagents.api.KitCatalogs.save().url, $scope.kit)
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
	 }
	 
	 $scope.updateKit = function(){
		 return $http.put(jsRoutes.controllers.reagents.api.KitCatalogs.update($scope.kit.code).url, $scope.kit)
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
	 }
	 
	 //init
	 var promises = [];
	 $scope.editMode = true;
	 if($routeParams.kitCatalogCode !== undefined){
		 $scope.kit.code = $routeParams.kitCatalogCode;
		 promises.push($scope.loadKit());
		 $scope.editMode = false;
	 }
	 $q.all(promises).then(function (res) {
		 $scope.loadBoxes()
		 $scope.lists.refresh.experimentTypes({categoryCodes:['transformation','transfert','qualitycontrol','purification']});
		 if(angular.isUndefined($scope.getHomePage())){
				$scope.mainService.setHomePage('new');
				tabService.addTabs({label:Messages('kitCatalogs.tabs.create'),href:jsRoutes.controllers.reagents.tpl.KitCatalogs.home("new").url,remove:false});
				tabService.activeTab(0);
		 }
	 });
}]);