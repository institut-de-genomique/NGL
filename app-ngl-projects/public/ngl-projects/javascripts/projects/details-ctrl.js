"use strict";

angular.module('home').controller('DetailsCtrl', ['$scope', '$http', '$routeParams', 'messages', 'lists', 'mainService', 'tabService', 'datatable',   
                                                  
  function($scope, $http, $routeParams, messages, lists, mainService, tabService,datatable) {
	
	/*$scope.datatableConfig = function(){
		var config = {
			 compact:true,
	         pagination:{
	        	 mode:'local',
	        	 active:false
	         },		
	         search:{
	        	 active:false
	         },
	         order:{
	        	 mode:'local',
	        	 active:true
	         },
	         remove:{
	        	 mode:'local',
	        	 active:false,
	         },
	         hide:{
	        	 active:false
	         },
	         edit:{
	        	 active:true,
	        	 columnMode:false,
	        	 showButton : false,
	        	 withoutSelect:true,
	        	 byDefault : false
	         },
	         messages:{
	        	 active:false,
	        	 columnMode:true
	         },
	         exportCSV:{
	        	 active:false
	         },
	         add:{
	        	 active:$scope.isGroupAdminMember
	         }
	       /* TODO groupe labo operationnel 
	        * otherButtons:{
					active:$scope.isGroupAdminMember,
					template:'<button class="btn btn-default" ng-click="" data-toggle="tooltip" title="'+Messages("button.query.addbasket")+'"><i class="fa fa-shopping-cart"></i></button>'
				}*/
	/*	};
		return config;
	};
	
	$scope.getDatatableConfigProjectUsers = function() {
			var configUser = $scope.datatableConfig();
			configUser.save={
					active:true,
					showButton:true,
					withoutEdit:false,
					changeClass : false,
					url: jsRoutes.controllers.projects.api.MembersProjects.update().url,
					method:'put',
					batch: false,
					value:function(line){return {codeProjet:$scope.project.code,users:[{login:line.displayName}]};},
					callback:function(datatable, nbError){
						if(nbError == 0){
						updateDatatable();					
						}else{
						$scope.messages.setError("save");
						}
					}
			}
		return configUser;
	};
	
	$scope.getDatatableConfigProjectAdmins = function() {
		var configAdmin = $scope.datatableConfig();
		configAdmin.save={
				active:true,
				showButton:true,
				withoutEdit:false,
				changeClass : false,
				url: jsRoutes.controllers.projects.api.MembersProjects.update().url,
				method:'put',
				batch: false,
				value:function(line){return {codeProjet:$scope.project.code,admins:[{login:line.displayName}]};},
				callback:function(datatable, nbError){
					if(nbError == 0){
					updateDatatable();					
					}else{
					$scope.messages.setError("save");
					}
				}
		}
	return configAdmin;
};
	
	
	$scope.getDefaultColumnsProjectUsers = function(){
		var columns = [];
		columns.push({
					"header":Messages("login.name"),
					"property":"displayName",
					"order":true,
					"type":"text",
					"edit":true,
					"choiceInList":true,
					"listStyle":'bt-select-filter',
					"possibleValues": 'getListUsersProject()'
			});
		return columns;
	};
	
	$scope.getDefaultColumnsProjectAdmins = function(){
		var columns = [];
		columns.push({
					"header":Messages("login.name"),
					"property":"displayName",
					"order":true,
					"type":"text",
					"edit":true,
					"choiceInList":true,
					"listStyle":'bt-select-filter',
					"possibleValues": 'getListUsersAdmin()'
		});
		return columns;
	};
	
	$scope.datatableConfigProjectDefault = {
			name:"projectAdminsDefault",
			columns:[
			         {
			        	 "header":Messages("login.name"),
			        	 "property":"displayName",
			        	 "order":true,
			        	 "type":"text",
			        	 "edit":false
			         }
			         ],
			         compact:true,
			         pagination:{
			        	 mode:'local',
			        	 active:false
			         },		
			         search:{
			        	 active:false
			         },
			         order:{
			        	 mode:'local',
			        	 active:true
			         },
			         remove:{
			        	 mode:'local',
			        	 active:false,
			         },
			         hide:{
			        	 active:false
			         },
			         edit:{
			        	 active:false
			         },
			         save:{
			        	 active:false
			         },
			         messages:{
			        	 active:false,
			        	 columnMode:true
			         },
			         exportCSV:{
			        	 active:false
			         }
			        
	};
	
	var updateDatatable = function(){
		$http.get(jsRoutes.controllers.projects.api.MembersProjects.get($scope.project.code).url).success(function(data) {
			$scope.members=data;
			$scope.datatableProjectUsers.setData(data.users,data.users.length);
			$scope.datatableProjectAdmins.setData(data.admins,data.admins.length);
		});
		$scope.initListUsers=false;
	};
	
	$scope.getListUsers = function(){
		return $scope.listUsers;
	};
	
	$scope.getListUsersProject = function(){
		return $scope.listUsersProject;
	};
	
	$scope.getListUsersAdmin = function(){
		return $scope.listUsersAdmin;
	};
	
	$scope.getTabClass = function(value){
		 if(value === mainService.get('projectActiveTab')){
			 return 'active';
		 }
	 };
	
	 $scope.setActiveTab = function(value){
		 mainService.put('projectActiveTab', value);
	 };
	 
	 $scope.setUsers = function(defaultGroupAccess, ouUsersName,value){
		 if(!$scope.initListUsers){
		//get members
			$http.get(jsRoutes.controllers.projects.api.MembersProjects.get($routeParams.code).url).success(function(data) {
				$scope.members=data;
				//get userMember
				$http.get(jsRoutes.controllers.authorisation.User.get().url).success(function(data) {
					$scope.login=data;
					
					$http.get(jsRoutes.controllers.projects.api.UserMembersProjects.get($scope.login).url).success(function(data) {
						$scope.userMembers=data;
						if(data.adminGroups.indexOf($scope.members.adminGroupName)>-1)
							$scope.isGroupAdminMember=true;
						else
							$scope.isGroupAdminMember=false;
					
						$http.get(jsRoutes.controllers.projects.api.UserMembersProjects.list().url,{params:{groupName:defaultGroupAccess}}).success(function(data) {
							$scope.defaultUsers=data.map(function(user){return user.login;});
							$scope.datatableProjectDefaultUsers = datatable($scope.datatableConfigProjectDefault);
							$scope.datatableProjectDefaultUsers.setData(data,data.length);
							
							$scope.listUsers=[];
							$http.get(jsRoutes.controllers.projects.api.UserMembersProjects.list().url,{params:{ouName:ouUsersName}}).success(function(data) {
								console.log(data);
								for(var i=0;i<data.length;i++){
									$scope.listUsers.push({"code":data[i].login,"name":data[i].displayName});
								}
								var loginUsers = $scope.members.users.map(function(user){return user.login;});
								var loginAdmins = $scope.members.admins.map(function(user){return user.login;});
								$scope.listUsers=$scope.listUsers.filter((u)=>$scope.defaultUsers.indexOf(u.code)==-1);
								$scope.listUsersProject=$scope.listUsers.filter((u)=>loginUsers.indexOf(u.code)==-1);
								$scope.listUsersAdmin=$scope.listUsers.filter((u)=>loginAdmins.indexOf(u.code)==-1);
								
								$scope.datatableConfigProjectUsers = $scope.getDatatableConfigProjectUsers();
								$scope.datatableConfigProjectUsers.add={active:$scope.isGroupAdminMember};
								$scope.datatableConfigProjectAdmins = $scope.getDatatableConfigProjectAdmins();
								$scope.datatableConfigProjectAdmins.add={active:$scope.isGroupAdminMember};
								
								$scope.datatableProjectUsers = datatable($scope.datatableConfigProjectUsers);
								$scope.datatableProjectUsers.setColumnsConfig($scope.getDefaultColumnsProjectUsers());
								$scope.datatableProjectAdmins = datatable($scope.datatableConfigProjectAdmins);
								$scope.datatableProjectAdmins.setColumnsConfig($scope.getDefaultColumnsProjectAdmins());
								
								$scope.datatableProjectUsers.setData($scope.members.users,$scope.members.users.length);
								$scope.datatableProjectAdmins.setData($scope.members.admins,$scope.members.admins.length);
								$scope.initListUsers=true;
							});
						});
						//$scope.datatableConfigProjectUsers.otherButtons={
						//		active:$scope.isGroupAdminMember,
						//		template:'<button class="btn btn-default" ng-click="addUser()" data-toggle="tooltip" title="'+Messages("button.add.user")+'"><i class="fa fa-plus"></i></button>'};
						//$scope.datatableConfigProjectAdmins.otherButtons={active:$scope.isGroupAdminMember};
					});
				});
			});
		 }
		 $scope.setActiveTab(value);
	 };
	
	*/	
	$scope.form = {	}
	
	
	/* buttons section */
	$scope.update = function(){
		var objProj = angular.copy($scope.project);
		
		$http.put(jsRoutes.controllers.projects.api.Projects.update($routeParams.code).url, objProj).success(function(data) {	
			$scope.messages.setSuccess("save");
			mainService.stopEditMode();
		}).error(function(data, status, headers, config){
			$scope.messages.setError("save");
			$scope.messages.setDetails(data);
		});
	};
	
	$scope.cancel = function(){
		$scope.messages.clear();
		updateData(true);				
	};
	
	var updateData = function(isCancel){
		$http.get(jsRoutes.controllers.projects.api.Projects.get($routeParams.code).url).success(function(data) {
			$scope.project = data;	
			$scope.stopEditMode();
		});
	};
	
	$scope.getTabClass = function(value){
		 if(value === mainService.get('projectActiveTab')){
			 return 'active';
		 }
	 };
	
	 $scope.setActiveTab = function(value){
		 mainService.put('projectActiveTab', value);
	 };

	/* main section  */
	var init = function(){
		$scope.messages = messages();	
		$scope.lists = lists;
		$scope.lists.refresh.states({objectTypeCode:"Project"});
		$scope.lists.refresh.projectTypes();
		$scope.lists.refresh.projectCategories();
		$scope.lists.refresh.umbrellaProjects();
		$scope.mainService = mainService;
		$scope.mainService.stopEditMode();
		/* TODO EJACOBY AD
		$scope.isGroupAdminMember=false;
		$scope.initListUsers=false;
		$scope.listUsers=undefined;
		$scope.listUsersProject=undefined;
		$scope.listUsersAdmin=undefined;*/
		
		$http.get(jsRoutes.controllers.projects.api.Projects.get($routeParams.code).url).success(function(data) {
			$scope.project = data;	
			
			if(tabService.getTabs().length == 0){
				tabService.addTabs({label:Messages('projects.menu.search'), href:jsRoutes.controllers.projects.tpl.Projects.home("search").url, remove:true});
				tabService.addTabs({label:$scope.project.code, href:jsRoutes.controllers.projects.tpl.Projects.get($scope.project.code).url, remove:true});							
				tabService.activeTab(tabService.getTabs(1));
			}
			
		});
		if(undefined == mainService.get('projectActiveTab')){
			 mainService.put('projectActiveTab', 'general');
		 }
		
	};
	
	init();
	
	
}]).controller('ModalAddUserCtrl', [ '$scope', '$modalInstance', function($scope, $modalInstance) {
	$scope.close = function () {
		$modalInstance.dismiss('cancel');
		};
}]);

