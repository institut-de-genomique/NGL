"use strict";

angular.module('home').controller('DetailsCtrl', ['$scope', '$sce', '$http', '$routeParams', 'messages', 'lists', 'mainService', 'tabService', 'datatable',  '$window', 
	function($scope, $sce, $http, $routeParams, messages, lists, mainService, tabService,datatable, $window) {
	
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

		config.select = {
			isSelectAll: false,
			showButton: false,
			callback: function(line, data) {
				var isSelectable = true;

				$scope.members.admins.forEach(function (a) {
					if ((a.adminGroups && a.adminGroups.length == 1) && (a.adminGroups[0] == $scope.groupAllAdmins) && (data.adminGroups && data.adminGroups.length == 1) && (data.adminGroups[0] == a.adminGroups[0])) {
						isSelectable = false;
					}
				});

				if (line.selected && !isSelectable) {
					$scope.datatableProjectAdmins.displayResult = $scope.datatableProjectAdmins.displayResult.filter(function (elt) {
						if (elt.line.selected == true) {
							$scope.datatableProjectAdmins.setData($scope.members.admins, $scope.members.admins.length);
						}

						return true;
					});
				}				
			}
		};

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
		$http.get(jsRoutes.controllers.projects.api.MembersProjects.get($scope.project.code).url).success(function (data) {
			$scope.members=data;
			$scope.datatableProjectUsers.setData(data.users,data.users.length);
			$scope.datatableProjectAdmins.setData($scope.members.admins, $scope.members.admins.length);
		});

		$scope.initListUsers=false;

		//Clear listeUserLabos
		$scope.form = {	};
	};

	$scope.getListUsersLabo = function(){
		if($scope.listUserLabo != undefined)
			return $scope.listUserLabo.filter(function(u) { return $scope.defaultUsers.indexOf(u.login)==-1; });
		else {
			return undefined;
		}
	};

	$scope.updateListUsersLabo = function(){
		var labo = $scope.listLabos.filter(function(l) {
			return (l.displayName == $scope.form.groupLabo);
		});

		if(labo.length>0) {
			$scope.listUserLabo=labo[0].members;
		} else {
			$scope.listUserLabo=undefined;
		}
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
				$scope.datatableProjectAdmins.setData($scope.members.admins, $scope.members.admins.length);

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

	$scope.showAnalysisType = function(){
		if($scope.project!=undefined && $scope.project.bioinformaticParameters!=undefined && $scope.project.bioinformaticParameters.biologicalAnalysis){ 
			var listValues = lists.getValues({propertyDefinitionCode:"analysisTypes"},"analysisTypes");
			if(listValues!=undefined && listValues.length>0)
				return true;
		}
		return false;
	}
	
	$scope.getAnalysisTypes = function(){
		if($scope.listAnalysisType!=undefined)
			return $scope.listAnalysisType;
		else{
			$scope.initAnalysisTypes();
			return $scope.listAnalysisType;
		}
	}

	
	$scope.form = {	}


	/* buttons section */
	$scope.update = function(){
		var objProj = angular.copy($scope.project);

		//Update listAnalysis 
		if($scope.project.bioinformaticParameters.biologicalAnalysis){
			if($scope.listAnalysisType!=undefined && $scope.listAnalysisType.length>0){
				var selectedTypes = [];
				for(var i=0; i<$scope.listAnalysisType.length; i++){
					if($scope.listAnalysisType[i].select){
						selectedTypes.push($scope.listAnalysisType[i].code);
					}
				}
				if($scope.project.properties.analysisTypes){
					objProj.properties.analysisTypes.value=selectedTypes;
				}else{
					objProj.properties.analysisTypes = {value: selectedTypes,_type: "list"};
				}
			}
		}

		$http.put(jsRoutes.controllers.projects.api.Projects.update($routeParams.code).url, objProj).success(function(data) {	
			$scope.messages.clear();
			$scope.messages.setSuccess("save");
			mainService.stopEditMode();
			updateData(true);


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
			$scope.initAnalysisTypes();
			$scope.stopEditMode();	
		});
	};

	$scope.convertToBr = function(text){
		if(text)return $sce.trustAsHtml(text.replace(/\n/g, "<br>"));
	};

	$scope.initAnalysisTypes = function(){
		$scope.listAnalysisType = lists.getValues({propertyDefinitionCode:"analysisTypes"},"analysisTypes");
		if($scope.project && $scope.project.properties.analysisTypes){
				for(var i=0; i<$scope.listAnalysisType.length; i++){
					if($scope.project.properties.analysisTypes.value.indexOf($scope.listAnalysisType[i].code)>-1)
						$scope.listAnalysisType[i].select=true;
					else
						$scope.listAnalysisType[i].select=false;
				}
		}else{
			if ($scope.listAnalysisType) {
				for(var i=0; i<$scope.listAnalysisType.length; i++){
					$scope.listAnalysisType[i].select=false;
				}
			}
		}
	};
	
	$scope.getTabClass = function(value){
		if(value === mainService.get('projectActiveTab')){
			return 'active';
		}
	};

	$scope.setActiveTab = function(value){
		mainService.put('projectActiveTab', value);
	};

	$scope.toggleIsShowInformation=function(){
		console.log ('toggleIsShowInformation');
		if ( $scope.isShowInformation===false) { $scope.isShowInformation=true; }
		else {$scope.isShowInformation=false}
	}
	
	// methode utilisée dans le controller CommentCtrl
	$scope.isCreationMode=function() {
		return false; // Dans la vue details, on n'est jamais en mode creation d'un project, le project existe bien en base.
	};

	$scope.openUmbrella = function(umbrellaCode) {
		$window.open("/umbrellaprojects/"+umbrellaCode);
	}

	/* main section  */
	$scope.init = function(groupAllAdmins, isCNSInstitute){
		$scope.groupAllAdmins = groupAllAdmins;
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
		$scope.isShowInformation=false;

		$http.get(jsRoutes.controllers.projects.api.Projects.get($routeParams.code).url).success(function(data) {
			$scope.project = data;	


			if (isCNSInstitute) {
				$http.get(jsRoutes.controllers.projects.api.UmbrellaProjects.get($scope.project.umbrellaProjectCode).url).success(function(umbrellaProject) {
					$scope.umbrellaProjectName = umbrellaProject.name;
				});
			}

			if(tabService.getTabs().length == 0){
				tabService.addTabs({label:Messages('projects.menu.search'), href:jsRoutes.controllers.projects.tpl.Projects.home("search").url, remove:true});
				tabService.addTabs({label:$scope.project.code, href:jsRoutes.controllers.projects.tpl.Projects.get($scope.project.code).url, remove:true});							
				tabService.activeTab(tabService.getTabs(1));
			}
			$scope.initAnalysisTypes();

		});
		
		$http.get(jsRoutes.controllers.projects.api.MembersProjects.get($routeParams.code).url).success(function(data) {
			$scope.members=data;
			if($scope.members!=undefined && $scope.members.admins!=undefined){
			$http.get(jsRoutes.controllers.authorisation.User.get().url).success(function(data) {
				$scope.login=data;

				var loginAdmins = $scope.members.admins.map(function(user){
					return user.login;
				});

				if(loginAdmins.indexOf($scope.login)>-1) {
					$scope.isGroupAdminMember=true;
				} else {
					$scope.isGroupAdminMember=false;
				}
			});
			}
		});

		if(undefined == mainService.get('projectActiveTab')){
				 mainService.put('projectActiveTab', 'general');
			}
	};
}]).controller('CommentsCtrl',['$scope','$sce', '$http','lists','$parse','$filter','datatable', 
                               function($scope,$sce,$http,lists,$parse,$filter,datatable) {

	$scope.currentComment = {comment:undefined};
	
	
	$scope.analyseText = function(e){
		
		if(e.keyCode === 9){
			e.preventDefault();
		}
	};
	
	$scope.convertToBr = function(text){
		return $sce.trustAsHtml(text.replace(/\n/g, "<br>"));
	};
	
	$scope.cancel = function(){	
		$scope.currentComment = {comment:undefined};
		$scope.index = undefined;
	};
	
	$scope.save = function(){	
		if($scope.isCreationMode()){
			$scope.project.comments.push($scope.currentComment);
			$scope.currentComment = {comment:undefined};
		}else{
			$scope.messages.clear();
			$http.post(jsRoutes.controllers.projects.api.ProjectComments.save($scope.project.code).url, $scope.currentComment)
			.success(function(data, status, headers, config) {
				if(data!=null){
					$scope.messages.setSuccess("save");
					$scope.project.comments.push(data);
					$scope.currentComment = {comment:undefined};
				}
			})
			.error(function(data, status, headers, config) {
				$scope.messages.setError("save");
				$scope.messages.setDetails(data);
			});		
		}		
	};
	
	$scope.isUpdate = function(){
		return ($scope.index != undefined);		
	};
	
	$scope.setUpdate = function(comment, index){
		$scope.currentComment = angular.copy(comment);
		$scope.index = index;
	};
	
	$scope.update = function(){		
		if($scope.isCreationMode()){
			$scope.project.comments[$scope.index] = $scope.currentComment;
			$scope.currentComment = {comment:undefined};
			$scope.index = undefined;			
		}else{	
			$scope.messages.clear();
			$http.put(jsRoutes.controllers.projects.api.ProjectComments.update($scope.project.code, $scope.currentComment.code).url, $scope.currentComment)
			.success(function(data, status, headers, config) {
				if(data!=null){
					$scope.messages.setSuccess("save");
					$scope.project.comments[$scope.index] = $scope.currentComment;
					$scope.currentComment = {comment:undefined};
					$scope.index = undefined;
				}
			})
			.error(function(data, status, headers, config) {
				$scope.messages.setError("save");
				$scope.messages.setDetails(data);
			});
		}
	};
	
	$scope.remove = function(comment, index){
		if($scope.isCreationMode()){
			$scope.currentComment = {comment:undefined};
			$scope.project.comments.splice(index, 1);
		}else if (confirm(Messages("comments.remove.confirm"))) {
			$scope.messages.clear();
			$http.delete(jsRoutes.controllers.projects.api.ProjectComments.delete($scope.project.code, comment.code).url)
			.success(function(data, status, headers, config) {
				if(data!=null){
					$scope.messages.setSuccess("save");
					$scope.currentComment = {comment:undefined};
					$scope.project.comments.splice(index, 1);
				}
			})
			.error(function(data, status, headers, config) {
				$scope.messages.setError("remove");
				$scope.messages.setDetails(data);				
			});
		}
	};
}]);


