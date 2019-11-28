"use strict";

angular.module('home').controller('DetailsCtrl', ['$scope', '$http', '$routeParams', 'messages', 'lists', 'mainService', 'tabService', 'datatable',   

	function($scope, $http, $routeParams, messages, lists, mainService, tabService,datatable) {
	
	$scope.datatableConfig = function(){
		var config = {
				name:"projectDefault",
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
						active:false
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
		return config;
	};

	$scope.getDatatableConfigProjectUser = function() {
		var config = $scope.datatableConfig();
		config.remove={
			mode:"remote",
			method:'delete',
			active:true,
			url:function(line){
				return jsRoutes.controllers.projects.api.MembersProjects.delete($scope.project.code,line.login,"users").url;
			}
		};
		return config;
	};
	
	$scope.getDatatableConfigProjectAdmin = function() {
		var config = $scope.datatableConfig();
		config.remove={
			mode:"remote",
			method:'delete',
			active:true,
			url:function(line){
				return jsRoutes.controllers.projects.api.MembersProjects.delete($scope.project.code,line.login,"admin").url;
			}
		};
		return config;
	};
	
	
	var updateDatatable = function(){
		$http.get(jsRoutes.controllers.projects.api.MembersProjects.get($scope.project.code).url).success(function(data) {
			$scope.members=data;
			$scope.datatableProjectUsers.setData(data.users,data.users.length);
			$scope.datatableProjectAdmins.setData(data.admins,data.admins.length);
		});
		$scope.initListUsers=false;
		//Clear listeUserLabos
		$scope.form = {	};
		
	};

	$scope.getListUsersLabo = function(){
		if($scope.listUserLabo != undefined)
			return $scope.listUserLabo.filter((u)=>$scope.defaultUsers.indexOf(u.login)==-1);
		else
			return undefined;
	};

	$scope.updateListUsersLabo = function(){
		var labo = $scope.listLabos.filter(function(l) {
			return (l.displayName == $scope.form.groupLabo);
		});
		if(labo.length>0)
			$scope.listUserLabo=labo[0].members;
		else
			$scope.listUserLabo=undefined;
	};

	$scope.askAddUserToGroup = function(){
		if($scope.form.usersLabo!=undefined && $scope.form.usersLabo.length>0){
			angular.element('#addUserModal').modal('show');
		}
	};

	$scope.addUserToGroup = function(){
		$http.put(jsRoutes.controllers.projects.api.MembersProjects.update().url,{codeProjet:$scope.project.code,users:$scope.form.usersLabo}).success(function(data) {
			updateDatatable();
			angular.element('#addUserModal').modal('hide');
		});
	};

	$scope.askAddAdminToGroup = function(){
		if($scope.form.usersLabo!=undefined && $scope.form.usersLabo.length>0){
			angular.element('#addAdminModal').modal('show');
		}
	};
	
	$scope.addAdminToGroup = function(){
		$http.put(jsRoutes.controllers.projects.api.MembersProjects.update().url,{codeProjet:$scope.project.code,admins:$scope.form.usersLabo}).success(function(data) {
			updateDatatable();
			angular.element('#addAdminModal').modal('hide');
		});
	};

	$scope.setUsers = function(defaultGroupAccess, ouGroupLaboName, value){
		if(!$scope.initListUsers){
			$http.get(jsRoutes.controllers.projects.api.UserMembersProjects.list().url,{params:{groupName:defaultGroupAccess}}).success(function(data) {
				$scope.defaultUsers=data.map(function(user){return user.login;});
				$scope.datatableProjectDefaultUsers = datatable($scope.datatableConfig());
				$scope.datatableProjectDefaultUsers.setData(data,data.length);
				
				$scope.datatableProjectUsers = datatable($scope.getDatatableConfigProjectUser());
				$scope.datatableProjectAdmins = datatable($scope.getDatatableConfigProjectAdmin());

				$scope.datatableProjectUsers.setData($scope.members.users,$scope.members.users.length);
				$scope.datatableProjectAdmins.setData($scope.members.admins,$scope.members.admins.length);
				$scope.initListUsers=true;
			});
		}
		if(!$scope.initListLabos){
			$http.get(jsRoutes.controllers.projects.api.GroupMembersProjects.list().url,{params:{applyPatternConfig:true,ouName:ouGroupLaboName}}).success(function(data) {
				$scope.listLabos=data;
			});
		}
		$scope.setActiveTab(value);
	};

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
		$scope.isGroupAdminMember=false;
		$scope.initListUsers=false;
		$scope.initListLabos=false;
		$scope.listLabos=undefined;

		$http.get(jsRoutes.controllers.projects.api.Projects.get($routeParams.code).url).success(function(data) {
			$scope.project = data;	

			if(tabService.getTabs().length == 0){
				tabService.addTabs({label:Messages('projects.menu.search'), href:jsRoutes.controllers.projects.tpl.Projects.home("search").url, remove:true});
				tabService.addTabs({label:$scope.project.code, href:jsRoutes.controllers.projects.tpl.Projects.get($scope.project.code).url, remove:true});							
				tabService.activeTab(tabService.getTabs(1));
			}

		});

		$http.get(jsRoutes.controllers.projects.api.MembersProjects.get($routeParams.code).url).success(function(data) {
			$scope.members=data;
			$http.get(jsRoutes.controllers.authorisation.User.get().url).success(function(data) {
				$scope.login=data;
				var loginAdmins = $scope.members.admins.map(function(user){return user.login;});

				if(loginAdmins.indexOf($scope.login)>-1)
					$scope.isGroupAdminMember=true;
				else
					$scope.isGroupAdminMember=false;
			});
		});
		//if(undefined == mainService.get('projectActiveTab')){
		mainService.put('projectActiveTab', 'general');
		//}

	};

	init();


}]);

