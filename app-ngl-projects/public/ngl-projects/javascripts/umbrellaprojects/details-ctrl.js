"use strict";

angular.module('home').controller('DetailsCtrl', ['$sce', '$scope', '$http', '$routeParams', '$filter', 'messages', 'lists', 'mainService', 'tabService', '$window', 'datatable',
                                                                                                    
  function($sce, $scope, $http, $routeParams, $filter, messages, lists, mainService, tabService, $window, datatable) {
		
	$scope.form = {				
	
	}

	$scope.datatableConfig = {
		search:{
			active:false
		},
		pagination:{
			active:true
		},
		order:{
			mode:'local'
		},
		hide:{
			active:false
		 },
		edit:{
			active:false
		},
		exportCSV:{
			active:true
		},
		select:{
			active:false
		},
		cancel : {
			active:false
		},
		name:"Associatied Projects",
		showTotalNumberRecords: false,
		columns:[{
			"header":"Code projet",
			"property":"code",
			"order":true,
			"hide":true,
			"position":1,
			"type":"text"
		}, {
			"header":"Nom projet",
			"property":"name",
			"order":true,
			"hide":true,
			"position":2,
			"type":"text"
		}, {
			"header":"OS",
			"property":"properties.os.value",
			"order":true,
			"hide":true,
			"position":3,
			"type":"text"
		}, {
			"header":"Genre",
			"property":"properties.genre.value",
			"order":true,
			"hide":true,
			"position":4,
			"type":"text"
		}, {
			"header":"Source financement",
			"property":"properties.financingSource.value",
			"order":true,
			"hide":true,
			"position":5,
			"type":"text"
		}]
	};

	
	$scope.isCreationMode=function() {
		return false; 
	};

	/* buttons section */
	$scope.update = function() {
		var objProj = angular.copy($scope.umbrellaProject);
		
		$http.put(jsRoutes.controllers.projects.api.UmbrellaProjects.update($routeParams.code).url, objProj).success(function(data) {
			$scope.messages.setSuccess("save");
			mainService.stopEditMode();
		}).error(function(data, status, headers, config){
			$scope.messages.setError("save");
		}); 
	};
	
	$scope.cancel = function(){
		$scope.messages.clear();
		updateData(true);				
	};
	
	var updateData = function(isCancel){
		$http.get(jsRoutes.controllers.projects.api.UmbrellaProjects.get($routeParams.code).url).success(function(data) {
			$scope.umbrellaProject = data;	
			$scope.stopEditMode();
		});
	};

	$scope.showProjects = function() {
		var projectCodes = "";

		for (var i = 0; i < $scope.associatedProjects.length; i++) {
			projectCodes += $scope.associatedProjects[i].code + "&projectCodes=";
		}

		// On enlève le dernier & qui est en trop.
		if (projectCodes.endsWith("&projectCodes=")) {
			projectCodes = projectCodes.slice(0, -14);
		}

		$window.open(jsRoutes.controllers.projects.tpl.Projects.home('search').url+'?projectCodes='+projectCodes, 'projects');
	}

	$scope.removeDT = function() {
		delete $scope.umbrellaProject.properties.dt;
	};
	
	/* main section  */
	var init = function() {
		$scope.messages = messages();	
		$scope.mainService = mainService;
		$scope.mainService.stopEditMode();

		$http.get(jsRoutes.controllers.projects.api.UmbrellaProjects.get($routeParams.code).url).success(function(data) {
			$scope.umbrellaProject = data;		
		
			if(tabService.getTabs().length == 0){
				tabService.addTabs({label:Messages('projects.menu.search'), href:jsRoutes.controllers.projects.tpl.UmbrellaProjects.home("search").url, remove:true});
				tabService.addTabs({label:$scope.umbrellaProject.name, href:jsRoutes.controllers.projects.tpl.UmbrellaProjects.get($scope.umbrellaProject.code).url, remove:true});
				tabService.activeTab(tabService.getTabs(1));
			}
			
			$http.get(jsRoutes.controllers.projects.api.Projects.list().url + "?datatable=true&paginationMode=local&umbrellaCodes=" + $scope.umbrellaProject.code).success(function(dataProj) {
				$scope.associatedProjects = dataProj.data;

				$scope.associatedProjectsDT = datatable($scope.datatableConfig);
				$scope.associatedProjectsDT.setData($scope.associatedProjects, $scope.associatedProjects.length);
			}).error(function(err){
				$scope.messages.setError(err);
			}); 
		});
		
	};
	
	init();	
}]).controller('CommentsCtrl',['$scope','$sce', '$http','lists','$parse','$filter','datatable', 
	function ($scope, $sce, $http, lists, $parse, $filter, datatable) {

		$scope.currentComment = { comment: undefined };


		$scope.analyseText = function (e) {

			if (e.keyCode === 9) {
				e.preventDefault();
			}
		};

		$scope.convertToBr = function (text) {
			return $sce.trustAsHtml(text.replace(/\n/g, "<br>"));
		};

		$scope.cancel = function () {
			$scope.currentComment = { comment: undefined };
			$scope.index = undefined;
		};

		$scope.save = function () {
			if ($scope.isCreationMode()) {
				$scope.umbrellaProject.comments.push($scope.currentComment);
				$scope.currentComment = { comment: undefined };
			} else {
				$scope.messages.clear();
				$http.post(jsRoutes.controllers.projects.api.UmbrellaProjectComments.save($scope.umbrellaProject.code).url, $scope.currentComment)
					.success(function (data, status, headers, config) {
						if (data != null) {
							$scope.messages.setSuccess("save");
							$scope.umbrellaProject.comments.push(data);
							$scope.currentComment = { comment: undefined };
						}
					})
					.error(function (data, status, headers, config) {
						$scope.messages.setError("save");
						$scope.messages.setDetails(data);
					});
			}
		};

		$scope.isUpdate = function () {
			return ($scope.index != undefined);
		};

		$scope.setUpdate = function (comment, index) {
			$scope.currentComment = angular.copy(comment);
			$scope.index = index;
		};

		$scope.update = function () {
			if ($scope.isCreationMode()) {
				$scope.umbrellaProject.comments[$scope.index] = $scope.currentComment;
				$scope.currentComment = { comment: undefined };
				$scope.index = undefined;
			} else {
				$scope.messages.clear();
				$http.put(jsRoutes.controllers.projects.api.UmbrellaProjectComments.update($scope.project.code, $scope.currentComment.code).url, $scope.currentComment)
					.success(function (data, status, headers, config) {
						if (data != null) {
							$scope.messages.setSuccess("save");
							$scope.umbrellaProject.comments[$scope.index] = $scope.currentComment;
							$scope.currentComment = { comment: undefined };
							$scope.index = undefined;
						}
					})
					.error(function (data, status, headers, config) {
						$scope.messages.setError("save");
						$scope.messages.setDetails(data);
					});
			}
		};

		$scope.remove = function (comment, index) {
			if ($scope.isCreationMode()) {
				$scope.currentComment = { comment: undefined };
				$scope.umbrellaProject.comments.splice(index, 1);
			} else if (confirm(Messages("comments.remove.confirm"))) {
				$scope.messages.clear();
				$http.delete(jsRoutes.controllers.projects.api.UmbrellaProjectComments.delete($scope.umbrellaProject.code, comment.code).url)
					.success(function (data, status, headers, config) {
						if (data != null) {
							$scope.messages.setSuccess("save");
							$scope.currentComment = { comment: undefined };
							$scope.umbrellaProject.comments.splice(index, 1);
						}
					})
					.error(function (data, status, headers, config) {
						$scope.messages.setError("remove");
						$scope.messages.setDetails(data);
					});
			}
		};
	}]);
