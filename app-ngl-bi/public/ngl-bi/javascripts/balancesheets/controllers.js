"use strict";

angular.module('home').controller('BalanceSheetsGeneralCtrl', ['$scope', '$http', '$q', 'mainService', 'tabService', 'datatable', 'balanceSheetsGeneralSrv', '$routeParams',
                                                               function($scope, $http, $q, mainService, tabService, datatable, balanceSheetsGeneralSrv, $routeParams){

	var configYearlyDT = {
			name:'yearlyDT',
			group : {
				active : false
			},
			search : {
				active:false
			},
			pagination:{
				active : false
			},
			hide:{
				active:false
			},
			select : {
				active : false
			},
			columns : [
			           {"property":"year",
			        	   "header": Messages("balanceSheets.year"),
			        	   "type" :"text",
			        	   "position":1
			           },
			           {"property":"nbBases",
			        	   "header": Messages("balanceSheets.nbBases"),
			        	   "type" :"number",
			        	   "position":2
			           }
			           ]
	};
	var configYearlySumDT = {
			name : 'yearlySumDT',
			showTotalNumberRecords : false,
			search : {
				active:false
			},
			pagination:{
				active : false
			},
			hide:{
				active:false
			},
			select : {
				active : false
			},
			callbackEndDisplayResult : function(){
				colorBlue($scope.dtSumYearlyBalanceSheets, 0);	
			},
			columns : [
			           {"property":"property",
			        	   "header": Messages("balanceSheets.property"),
			        	   "type" :"text",
			        	   "position":1
			           },
			           {"property":"value",
			        	   "header": Messages("balanceSheets.value"),
			        	   "type" :"number",
			        	   "position":2,
			           }
			           ]
	};

	var colorBlue = function(datatable, pos){
		datatable.displayResult[pos].line.trClass="text-primary";
	}


	$scope.balanceSheetsGeneralService = balanceSheetsGeneralSrv;

	mainService.put('activeYear', 'general');


	$scope.isLoading = function(){
		return $scope.loading;
	};

	var loadData = function()
	{
		var balanceSheetForm = {}
		balanceSheetForm.type = $routeParams.typeCode.replace("rs", "");
		$http.get(jsRoutes.controllers.balancesheets.api.BalanceSheets.list().url, {params : balanceSheetForm})
		.success(balanceSheets => {
			$scope.dataByYear = $scope.balanceSheetsGeneralService.computeYearlyData(balanceSheets);
			mainService.put($routeParams.typeCode+'-general', $scope.dataByYear);
			calculateData();
		});
	}

	var calculateData = function()
	{
		$scope.dtYearlyBalanceSheets = datatable(configYearlyDT);
		$scope.dtYearlyBalanceSheets.setData($scope.dataByYear, $scope.dataByYear.length);

		var sumData = $scope.balanceSheetsGeneralService.computeSumData($scope.dataByYear);
		$scope.dtSumYearlyBalanceSheets = datatable(configYearlySumDT);
		$scope.dtSumYearlyBalanceSheets.setData(sumData, 1);

		$scope.chartYearlyBalanceSheets = $scope.balanceSheetsGeneralService.computeChartYearlyBalanceSheets($scope.dataByYear);

		$scope.loading = false;
	}


var init = function(){
	// Tabs
	tabService.addTabs({label:Messages("balanceSheets.tab.generalBalanceSheets"), href:jsRoutes.controllers.balancesheets.tpl.BalanceSheets.home($routeParams.typeCode, "general").url});
	var form = {
		includes: ["year"],
		type: $routeParams.typeCode.replace("rs", "")
	}
	$http.get(jsRoutes.controllers.balancesheets.api.BalanceSheets.list().url, {params : form})
	.success(data => {
		var years = Array.from(new Set(data.map(bs => bs.year))).sort((a, b) => b - a);
		years.forEach(year => {
			tabService.addTabs({label:Messages("balanceSheets.tab.year") +" "+ year,href:jsRoutes.controllers.balancesheets.tpl.BalanceSheets.home($routeParams.typeCode, year).url});
		});
		tabService.activeTab(0);
		$scope.loading=true;
		if(angular.isDefined(mainService.get($routeParams.typeCode+'-general'))){
			$scope.dataByYear = mainService.get($routeParams.typeCode+'-general');
			calculateData();
		}else{
			loadData();
		}
	})
};
init();	
}]);

angular.module('home').controller('BalanceSheetsYearCtrl', ['$scope', '$http','mainService', 'tabService', 'lists', 'datatable', 'balanceSheetsGeneralSrv', '$routeParams','$q', 
                                                            function($scope, $http, mainService, tabService, lists, datatable, balanceSheetsGeneralSrv, $routeParams,$q){

	var configQuarterDT = {
			name:'quarterDT',
			showTotalNumberRecords : false,
			search : {
				active:false
			},
			pagination:{
				active : false
			},
			hide:{
				active:false
			},
			select : {
				active : false
			},
			callbackEndDisplayResult : function(){
				for(var i = 0; i < $scope.dataForYear.lineToColorQuarter.length; i++){
					colorBlue($scope.dtQuarters, $scope.dataForYear.lineToColorQuarter[i]);
				}
			},
			columns : [
			           {	"property":"quarter",
			        	   "header":Messages("balanceSheets.quarters"),
			        	   "type":"text",
			        	   "position":1
			           },
			           {	"property":"month",
			        	   "header":Messages("balanceSheets.monthRun"),
			        	   "type":"text",
			        	   "position":2
			           },
			           {
			        	   "property":"nbBases",
			        	   "header":Messages("balanceSheets.nbBases"),
			        	   "type":"number",
			        	   "position":3
			           }
			           ]
	};


	// Initializing our components
	var configSequencingDT = {
			name:'sequencingDT',
			search : {
				active:false
			},
			pagination:{
				active : false
			},
			hide:{
				active:false
			},
			select : {
				active : false
			},
			order : {
				mode : 'local',
				active : true,
				by : 'nbBases',
				reverse : true,
				callback:function(datatable){
					$scope.balanceSheetsGeneralService.computeChartSequencing($scope.dataForYear.dataSequencingDT);
				}
			},
			columns : [
			           {	"property":"name",
			        	   "header":Messages("balanceSheets.runTypeCode"),
			        	   "type":"text",
			        	   "order":true,
			        	   "position":1
			           },
			           {	"property":"nbBases",
			        	   "header":Messages("balanceSheets.nbBases"),
			        	   "type":"number",
			        	   "order":true,
			        	   "position":2
			           },
			           {
			        	   "property":"percentage",
			        	   "header":Messages("balanceSheets.percentage"),
			        	   "type":"text",
			        	   "order":true,
			        	   "position":3
			           }
			           ]
	}; 
	var configSumDT = {
			name:'sumDT',
			showTotalNumberRecords : false,
			search : {
				active:false
			},
			pagination:{
				active : false
			},
			hide:{
				active:false
			},
			select : {
				active : false
			},
			callbackEndDisplayResult : function(){
				colorBlue($scope.dtSequencingSum, 0);
				colorBlue($scope.dtSampleSum, 0);
			},
			columns : [
			           {	"property":"property",
			        	   "header": Messages("balanceSheets.property"),
			        	   "type" :"text",
			        	   "position":1
			           },
			           {	"property":"value",
			        	   "header": Messages("balanceSheets.value"),
			        	   "type" :"number",
			        	   "position":2
			           }
			           ]
	};

	var configProjectDT = {
			name:'projectDT',
			group : {
				active : false,
			},
			search : {
				active:false
			},
			pagination:{
				active : false
			},
			hide:{
				active:false
			},
			select : {
				active : false
			},
			order : {
				active : true,
				mode : 'local'
			},
			columns : [
			           {	"property":"code",
			        	   "header": Messages("balanceSheets.projectCode"),
			        	   "type" :"text",
			        	   "order" : true,
			        	   "position":1
			           },
			           {	"property":"name",
			        	   "header": Messages("balanceSheets.projectName"),
			        	   "type" :"text",
			        	   "order" : true,
			        	   "position":2
			           },
			           {
			        	   "property":"nbBases",
			        	   "header": Messages("balanceSheets.nbBases"),
			        	   "type":"number",
			        	   "order" : true,
			        	   "position":3
			           },
			           {
			        	   "property":"percentageForTenProjects",
			        	   "header": Messages("balanceSheets.percentageForTenProjects"),
			        	   "type":"text",
			        	   "order" : true,
			        	   "position":4
			           },
			           {
			        	   "property":"percentageForYear",
			        	   "header":Messages("balanceSheets.percentageForYear"),
			        	   "type":"text",
			        	   "order":true,
			        	   "position":5
			           }
			           ]
	}; 
	var configProjectSumDT = {
			name:'projectSumDT',
			showTotalNumberRecords : false,
			search : {
				active:false
			},
			pagination:{
				active : false
			},
			hide:{
				active:false
			},
			select : {
				active : false
			},
			callbackEndDisplayResult : function(){
				// Color text in blue
				colorBlue($scope.dtProjectSum, 0);
				colorBlue($scope.dtProjectSum, 1);
			},
			columns : [
			           {	"property":"property",
			        	   "header": Messages("balanceSheets.property"),
			        	   "type" :"text",
			        	   "position":1
			           },
			           {	"property":"value",
			        	   "header": Messages("balanceSheets.value"),
			        	   "type" :"number",
			        	   "position":2
			           },
			           {
			        	   "property" :"percentage",
			        	   "header": Messages("balanceSheets.percentage"),
			        	   "type" : "text",
			        	   "position" : 3
			           }			           
			           ]
	}; 

	var configSampleDT = {
			name:'sampleDT',
			order : {
				active : true,
				by : 'nbBases',
				reverse : true,
				mode: 'local'
			},
			search : {
				active:false
			},
			pagination:{
				active : false
			},
			hide:{
				active:false
			},
			select : {
				active : false
			},
			columns : [
			           {
			        	   "property" : "category",
			        	   "header" : Messages("balanceSheets.categoryType"),
			        	   //filter: "codes:'sample_cat'",
			        	   "type" : "text",
			        	   "order" : true,
			        	   "position" : 1
			           },
			           {	"property":"type",
			        	   "filter": "codes:'type'",
			        	   "header": Messages("balanceSheets.projectType"),
			        	   "type" :"text",
			        	   "order" : true,
			        	   "position":2
			           },
			           {	"property":"nbBases",
			        	   "header": Messages("balanceSheets.nbBases"),
			        	   "type" :"Number",
			        	   "order" : true,
			        	   "position":3
			           },
			           {
			        	   "property":"percentage",
			        	   "header": Messages("balanceSheets.percentage"),
			        	   "type":"text",
			        	   "order" : true,
			        	   "position":4
			           }
			           ]
	}; 

	
	var configRunDT = {
			name:'runDT',
			order : {
				active : false
			},
			search : {
				active:false
			},
			pagination:{
				active : false
			},
			hide:{
				active:false
			},
			select : {
				active : false
			},
			callbackEndDisplayResult : function(){
				for(var i=12; i < $scope.dataForYear.dataRunDT.length; i++){
					colorBlue($scope.dtRun, i);
				}
			}
	}; 

	var colorBlue = function(datatable, pos){
		if(datatable.displayResult != undefined){
			datatable.displayResult[pos].line.trClass="text-primary";
		}
	}

	// Service
	$scope.balanceSheetsGeneralService = balanceSheetsGeneralSrv;

	$scope.isLoading = function(){
		return $scope.loading;
	};

	
	$scope.setActiveTab = function(value){
		mainService.put('balanceSheetActiveTab', value);
		
	};
	
	$scope.getTabClass = function(value){
		if(value === mainService.get('balanceSheetActiveTab')){
			return 'active';
		}
	};

	var loadData = function(activeYear)
	{
		var balanceSheetForm = {}
		balanceSheetForm.year = activeYear;
		balanceSheetForm.type = $routeParams.typeCode.replace("rs", "");

		var typeForm = {includes : []};
		typeForm.objectTypeCode="Run";
		typeForm.includes.push("code");
		typeForm.includes.push("name");

		var projectForm = {includes : []};
		projectForm.includes.push("code");
		projectForm.includes.push("name");

		var sampleForm = {includes : []};
		sampleForm.includes.push("code");
		sampleForm.includes.push("name");
		sampleForm.includes.push("category");

		$scope.dataForYear = {
			total : 0,
			totalProject : 0,
			quarters : [],
			dataQuarterDT: [],
			lineToColorQuarter: [3, 7, 11, 15, 16],
			dataSequencingDT: [],
			dataProjectDT: [],
			dataSampleDT: [],
			totalRun: 0,
			listSeq: [],
			dataRunDT:[]
		};
		mainService.put($routeParams.typeCode + '-' + activeYear, $scope.dataForYear);

		$http.get(jsRoutes.controllers.balancesheets.api.BalanceSheets.list().url, {params : balanceSheetForm})
		.success(function(balanceSheets){
			$http.get(jsRoutes.controllers.commons.api.CommonInfoTypes.list().url, {params : typeForm})
			.success(function(runTypes){		
				$http.get(jsRoutes.controllers.projects.api.Projects.list().url, {params : projectForm})
				.success(function(projects){
					$http.get(jsRoutes.controllers.sampletypes.api.SampleTypes.list().url, {params : sampleForm})
					.success(function(sampleTypes){
						/* Readsets Data  */
						var readsetProperties = [
							"treatments.ngsrg.default.nbBases.value", 
							"treatments.ngsrg.default.1DForward.value.nbBases", 
							"treatments.ngsrg.default.1DReverse.value.nbBases",
							"treatments.readQuality.default.1DForward.value.nbBases",
							"treatments.readQuality.default.1DReverse.value.nbBases"
						];
						var readsetStats = $scope.balanceSheetsGeneralService.mergeReadSetComputations(
							balanceSheets[0].computations.filter(computation => {
								return computation.collection === "readsets" && computation.method === "sum" && readsetProperties.includes(computation.property)
							})
						);
						var total = readsetStats.result.value;
						$scope.dataForYear.total = total;
						/* by-Month Readsets Data */
						var accumulator = $scope.balanceSheetsGeneralService.computeMonthlyData(readsetStats, total);
						$scope.dataForYear.dataQuarterDT = accumulator.results;
						$scope.dataForYear.quarters = accumulator.quarters;
						/* by-SequencingType Readsets Data */
						$scope.dataForYear.dataSequencingDT = $scope.balanceSheetsGeneralService.computeSequencingData(readsetStats, runTypes, total);
						/* by-Project Readsets Data */
						var projectTen = $scope.balanceSheetsGeneralService.getTenBiggestProjects(readsetStats);
						var totalProject = projectTen.map(project => project.result.value).reduce((a, b) => a + b);
						$scope.dataForYear.totalProject = totalProject;
						$scope.dataForYear.dataProjectDT = $scope.balanceSheetsGeneralService.computeProjectData(projectTen, projects, totalProject, total);
						/* by-SampleType Readsets Data */
						$scope.dataForYear.dataSampleDT = $scope.balanceSheetsGeneralService.computeSampleData(readsetStats, sampleTypes, total);
						/* by-SequencingType Runs Data */
						var runStats = balanceSheets[0].computations.find(computation => {
							return computation.collection === "runs" && computation.method === "count" && computation.matches.length === 0
						});
						var runFailedStats = balanceSheets[0].computations.find(computation => {
							return computation.collection === "runs" && computation.method === "count" && computation.matches.includes("state.code=FE-S")
						});
						var runExtStats = balanceSheets[0].computations.find(computation => {
							return computation.collection === "runs" && computation.method === "count" && computation.matches.includes("instrumentUsed.code=^EXT.+")
						});
						$scope.dataForYear.totalRun = runStats.result.nbElements;
						$scope.dataForYear.listSeq = $scope.balanceSheetsGeneralService.computeRunSequencingTypes(runStats, runTypes);
						$scope.dataForYear.dataRunDT = $scope.balanceSheetsGeneralService.computeRunSequencingData(runStats, runFailedStats, runExtStats);

						calculateData();
					});
				});
			});
		});
	};
	
	var calculateData = function(){
		$scope.dtQuarters = datatable(configQuarterDT);
		$scope.dtQuarters.setData($scope.dataForYear.dataQuarterDT, $scope.dataForYear.dataQuarterDT.length);

		$scope.chartQuarter = $scope.balanceSheetsGeneralService.computeChartQuarters($scope.dataForYear);

		$scope.dtSequencing = datatable(configSequencingDT);
		$scope.dtSequencing.setData($scope.dataForYear.dataSequencingDT, $scope.dataForYear.dataSequencingDT.length);

		var sumData = [{
			"property" : Messages('balanceSheets.sum'),
			"value" : $scope.dataForYear.total
		}];

		$scope.dtSequencingSum = datatable(configSumDT);
		$scope.dtSequencingSum.setData(sumData, 1);
		$scope.chartSequencing = $scope.balanceSheetsGeneralService.computeChartSequencing($scope.dataForYear.dataSequencingDT);

		$scope.dtProject = datatable(configProjectDT);
		$scope.dtProject.setData($scope.dataForYear.dataProjectDT, $scope.dataForYear.dataProjectDT.length);

		$scope.dtProjectSum = datatable(configProjectSumDT);
		$scope.dtProjectSum.setData([
		                             {
		                            	 "property" : Messages('balanceSheets.totalTen'),
		                            	 "value" : $scope.dataForYear.totalProject,
		                            	 "percentage" : (parseFloat(($scope.dataForYear.totalProject * 100 / $scope.dataForYear.total).toFixed(2))).toLocaleString() + " %"
		                             },
		                             {
		                            	 "property" : Messages('balanceSheets.totalSum'),
		                            	 "value" : $scope.dataForYear.total,
		                            	 percentage : "100 %"
		                             }], 2);
		$scope.chartProject = $scope.balanceSheetsGeneralService.computeChartProject($scope.dataForYear.dataProjectDT,$scope.dataForYear.totalProject);

		$scope.dtSample = datatable(configSampleDT);
		$scope.dtSample.setData($scope.dataForYear.dataSampleDT, $scope.dataForYear.dataSampleDT.length);

		$scope.dtSampleSum = datatable(configSumDT);
		$scope.dtSampleSum.setData(sumData, 1);

		$scope.chartSample = $scope.balanceSheetsGeneralService.computeChartSample($scope.dataForYear.dataSampleDT,$scope.dataForYear.total);

		
		//run Datatable
		configRunDT.columns=[];
		configRunDT.columns.push({
     	   "property" : "month",
    	   "header" : Messages("balanceSheets.monthRun"),
    	   //filter: "codes:'sample_cat'",
    	   "render":function(value, line){
	    		return "<strong>"+value.month+"</strong>";
	    	},
    	   "type" : "text",
    	   "position" : 1
		});
		
		var position=1;
		for(var sequencingType of $scope.dataForYear.listSeq){
			position++;
			configRunDT.columns.push({
		     	   "property" : sequencingType.code,
		    	   "header" : sequencingType.name,
		    	   "type" : "text",
		    	   "position" : position
				});
		}
		position++;
		configRunDT.columns.push(
					{	"property":"nbAborted",
		        	   "header": Messages("balanceSheets.nbAborted"),
		        	   "type" :"Number",
		        	   "position":position
		           });
		position++;
		configRunDT.columns.push(
		           {
		        	   "property":"total",
		        	   "header": Messages("balanceSheets.sumNoAborting"),
		        	   "type":"Number",
		        	   "position":position
		           }
		);
		
		$scope.dtRun = datatable(configRunDT);
		$scope.dtRun.setData($scope.dataForYear.dataRunDT, $scope.dataForYear.dataRunDT.length);
		
		var sumData = [{
			"property" : Messages('balanceSheets.sum'),
			"value" : $scope.dataForYear.totalRun
		}];
		$scope.dtRunSum = datatable(configSumDT);
		$scope.dtRunSum.setData(sumData, 1);
		
		$scope.loading=false;
	};
	
	
	var init = function(){
		var activeYear = $routeParams.year;
		tabService.addTabs({label:Messages("balanceSheets.tab.generalBalanceSheets"), href:jsRoutes.controllers.balancesheets.tpl.BalanceSheets.home($routeParams.typeCode, "general").url});
		var form = {
			includes: ["year"],
			type: $routeParams.typeCode.replace("rs", "")
		}
		$http.get(jsRoutes.controllers.balancesheets.api.BalanceSheets.list().url, {params : form})
		.success(data => {
			var years = Array.from(new Set(data.map(bs => bs.year))).sort((a, b) => b - a);
			years.forEach(year => {
				tabService.addTabs({label:Messages("balanceSheets.tab.year") + " " + year,href:jsRoutes.controllers.balancesheets.tpl.BalanceSheets.home($routeParams.typeCode, year).url});
			});
			tabService.activeTab(years.indexOf(activeYear) + 1);
			$scope.loading=true;
			if(mainService.get('balanceSheetActiveTab') == undefined) {
				mainService.put('balanceSheetActiveTab', 'quarter');
			}
			if(angular.isDefined(mainService.get($routeParams.typeCode + '-' + activeYear))) {
				$scope.dataForYear = mainService.get($routeParams.typeCode + '-' + activeYear);
				calculateData();
			} else {
				loadData(activeYear);
			}
		})
	}
	init();
}]);


