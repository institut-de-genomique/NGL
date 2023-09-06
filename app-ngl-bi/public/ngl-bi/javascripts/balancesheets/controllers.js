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
					   },
					   {"property":"nbReadsets",
			        	   "header": Messages("balanceSheets.nbReadsets"),
			        	   "type" :"number",
			        	   "position":3
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
	$scope.headerText = Messages("balanceSheets.menu." + $routeParams.typeCode );

	mainService.put('activeYear', 'general');


	$scope.isLoading = function(){
		return $scope.loading;
	};

	var loadData = function()
	{
		$http.get(balanceSheetsUrl(), {params :balanceSheetsForm()})
		.success(function(balanceSheets) {
			$http.get(sampleTypesUrl(), {params : sampleTypesform()})
			.success(function(sampleTypes){
				$scope.dataByYear = $scope.balanceSheetsGeneralService.computeYearlyData(balanceSheets, sampleTypes);
				mainService.put($routeParams.typeCode+'-general', $scope.dataByYear);
				calculateData();
			});
		});

		//---

		function balanceSheetsUrl() {
			return jsRoutes.controllers.balancesheets.api.BalanceSheets.list().url;
		}

		function balanceSheetsForm() {
			return {
				type : $routeParams.typeCode.replace("rs", "bi-"),
				orderBy : "year"
			};
		}

		function sampleTypesUrl() {
			return jsRoutes.controllers.sampletypes.api.SampleTypes.list().url;
		}

		function sampleTypesform() {
			return {
				includes : ["code", "category"]
			};
		}
	};

	var calculateData = function()
	{
		$scope.dtYearlyBalanceSheets = datatable(configYearlyDT);
		$scope.dtYearlyBalanceSheets.setData($scope.dataByYear, $scope.dataByYear.length);

		var sumData = $scope.balanceSheetsGeneralService.computeSumData($scope.dataByYear);
		$scope.dtSumYearlyBalanceSheets = datatable(configYearlySumDT);
		$scope.dtSumYearlyBalanceSheets.setData(sumData, 1);

		$scope.ChartYearlyNbBases = $scope.balanceSheetsGeneralService.computeChartYearlyNbBases($scope.dataByYear, $routeParams.typeCode);
		$scope.ChartYearlyNbReadsets = $scope.balanceSheetsGeneralService.computeChartYearlyNbReadsets($scope.dataByYear, $routeParams.typeCode);

		$scope.loading = false;
	}

	var getBilanGeneralTitle = function(typeCode) {
		var d = new Date();
		var strDate = ('0'+ d.getDate()).slice(-2)+"/"+('0'+(d.getMonth()+1)).slice(-2)+"/"+d.getFullYear();
		var title = "Bilan général ";
		if (typeCode.includes("rsillumina")) {
			title = title + "Readsets Illumina (généré le " + strDate + ")";
		} else if (typeCode.includes("rsnanopore")) {
			title = title + "Readsets Nanopore (généré le " + strDate + ")";
		} else {
			title = title + typeCode + " (généré le " + strDate + ")";
		}
		return title;
	}

	
	$scope.computeExcelExport = function() {
		var output = "Bilan-General-" + $routeParams.typeCode + ".xlsx";
		var sheets = [
			{
				name: Messages("balanceSheets.export.excel.table.general"),
				tables: Array.from(document.getElementsByTagName("table"))
				.map(function(table) {
					return {
						rows: Array.from(table.rows)
						.map(function(row) {
							return {
								values: Array.from(row.children)
								.map(function(child) { return child.innerText; })
							}
						})
					}
				})
			}
		];
		//NGL-3420 :
		var title = getBilanGeneralTitle($routeParams.typeCode);
		$scope.balanceSheetsGeneralService.computeExcelExport(title, output, sheets);
	};
	

	$scope.computePdfExport = function() {
		var output = "Bilan-General-" + $routeParams.typeCode + ".pdf";
		//NGL-3420 :
//		$scope.balanceSheetsGeneralService.computePdfExport(output, [
//			{"name": Messages("balanceSheets.tab.generalBalanceSheets")}
//		]);
				
		var title = getBilanGeneralTitle($routeParams.typeCode);
		$scope.balanceSheetsGeneralService.computePdfExport(output, [
			{"name": title}
		]);
	};

var init = function(){
	// Tabs
	tabService.addTabs({label:Messages("balanceSheets.tab.generalBalanceSheets"), href:jsRoutes.controllers.balancesheets.tpl.BalanceSheets.home($routeParams.typeCode, "general").url});
	$http.get(balanceSheetsUrl(), {params : balanceSheetsForm()})
	.success(function(data) {
		var years = Array.from(new Set(data.map(function(bs) { return bs.year }))).sort(function(a, b) { return b - a });
		years.forEach(function(year) {
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
	});

	//---

	function balanceSheetsUrl() {
		return jsRoutes.controllers.balancesheets.api.BalanceSheets.list().url;
	}

	function balanceSheetsForm() {
		return {
			includes: ["year"],
			type: $routeParams.typeCode.replace("rs", "bi-"),
			orderBy : "year"
		};
	}
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
					   },
					   {
						"property":"nbRS",
						"header": Messages("balanceSheets.nbReadsets"),
						"type":"Number",
						"order" : true,
						"position":5
						}
			           ]
	}; 

	var configSampleSumDT = {
		name:'sampleSumDT',
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
					   "header": Messages("balanceSheets.nbBases"),
					   "type" :"number",
					   "position":2
				   },
				   {	"property":"nbReadsets",
					   "header": Messages("balanceSheets.nbReadsets"),
					   "type" :"number",
					   "position":3
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
				colorBlue($scope.dtRun, 12);
				colorRed($scope.dtRun, 13);
			}
	}; 

	var configRunSumDT = {
		name:'runSumDT',
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
			colorBlue($scope.dtRunSum, 0);
		},
		columns : [
				   {	"property":"property",
					   "header": Messages("balanceSheets.total.runs"),
					   "type" :"text",
					   "render":function(obj, line){
							return "<strong>"+obj.property+"</strong>";
						},
					   "position":1
				   },
				   {	"property":"value",
					   "header": Messages("balanceSheets.value"),
					   "type" :"number",
					   "render":function(obj, line){
							return "<strong>"+obj.value+"</strong>";
						},
					   "position":2
				   }			           
				   ]
	}; 

	var colorBlue = function(datatable, pos){
		if(datatable.displayResult != undefined){
			datatable.displayResult[pos].line.trClass="text-primary";
		}
	}

	var colorRed = function(datatable, pos){
		if(datatable.displayResult != undefined){
			datatable.displayResult[pos].line.trClass="text-danger";
		}
	}

	
	var getGenericBilanAnneeTitle = function(annee, typeCode) {
		var d = new Date();
		var strDate = ('0'+ d.getDate()).slice(-2)+"/"+('0'+(d.getMonth()+1)).slice(-2)+"/"+d.getFullYear();
		var title = "Bilan ";
		if (typeCode.includes("rsillumina")) {
			title = title + " Readsets Illumina";
		} else if (typeCode.includes("rsnanopore")) {
			title = title + " Readsets Nanopore";
		} else {
			title = title + typeCode;
		}
		title = title + " " + annee + " (généré le " + strDate + ")";
		return title;
	}

	
	var getBilanAnneeTitle = function(annee, typeCode, typeBilanAnnuel) {
		var d = new Date();
		var yy = d.getFullYear().toString(); 
		yy = yy.substring(2, yy.length);
		var strDate = ('0'+ d.getDate()).slice(-2)+"/"+('0'+(d.getMonth()+1)).slice(-2)+"/"+yy;
		var title = "Bilan ";
		if (typeCode.includes("rsillumina")) {
			title = title + "RS Illumina";
		} else if (typeCode.includes("rsnanopore")) {
			title = title + "RS Nanopore";
		} else {
			title = title + typeCode;
		}
		title = title +" "+ annee + " ";
		title = title + " " + typeBilanAnnuel;
		title = title + " (généré le " + strDate + ")";
		return title;
	}
	
	// Service
	$scope.balanceSheetsGeneralService = balanceSheetsGeneralSrv;
	$scope.headerText = Messages("balanceSheets.menu." + $routeParams.typeCode);
	// NGL-3420
	$scope.headerText += " " + $routeParams.year;
	
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
		$scope.dataForYear = {
			year: activeYear,
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

		$http.get(balanceSheetsUrl(), {params : balanceSheetsForm()})
		.success(function(balanceSheets){
			$http.get(objectTypesUrl(), {params : objectTypesForm()})
			.success(function(runTypes){		
				$http.get(projectsUrl(), {params : projectsForm()})
				.success(function(projects){
					$http.get(samplesUrl(), {params : samplesForm()})
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
							balanceSheets[0].computations.filter(function(computation) {
								return computation.collection === "readsets" && computation.method === "sum" && readsetProperties.includes(computation.property)
							})
						);
						var total = readsetStats.result.value || 0;
						$scope.dataForYear.total = total;
						$scope.dataForYear.totalRs = readsetStats.result.nbElements || 0;
						/* by-Month Readsets Data */
						var accumulator = $scope.balanceSheetsGeneralService.computeMonthlyData(readsetStats, total);
						$scope.dataForYear.dataQuarterDT = accumulator.results;
						$scope.dataForYear.quarters = accumulator.quarters;
						/* by-SequencingType Readsets Data */
						$scope.dataForYear.dataSequencingDT = $scope.balanceSheetsGeneralService.computeSequencingData(readsetStats, runTypes, total);
						/* by-Project Readsets Data */
						var projectTen = $scope.balanceSheetsGeneralService.getTenBiggestProjects(readsetStats);
						var totalProject = projectTen.map(function(project) { return project.result.value; }).reduce(function(a, b) { return a + b; }, 0);
						$scope.dataForYear.totalProject = totalProject;
						$scope.dataForYear.dataProjectDT = $scope.balanceSheetsGeneralService.computeProjectData(projectTen, projects, totalProject, total);
						/* by-SampleType Readsets Data */
						$scope.dataForYear.dataSampleDT = $scope.balanceSheetsGeneralService.computeSampleData(readsetStats, sampleTypes, total);
						/* by-SequencingType Runs Data */
						var runStats = balanceSheets[0].computations.find(function(computation) {
							return computation.collection === "runs" && computation.method === "count" && computation.matches.length === 0
						});
						var runFailedStats = balanceSheets[0].computations.find(function(computation) {
							return computation.collection === "runs" && computation.method === "count" && computation.matches.includes("state.code=FE-S")
						});
						var runExtStats = balanceSheets[0].computations.find(function(computation) {
							return computation.collection === "runs" && computation.method === "count" && computation.matches.includes("instrumentUsed.code=^EXT.+")
						});
						$scope.dataForYear.totalRun = runStats.result.nbElements || 0;
						$scope.dataForYear.listSeq = $scope.balanceSheetsGeneralService.computeRunSequencingTypes(runStats, runTypes);
						$scope.dataForYear.dataRunDT = $scope.balanceSheetsGeneralService.computeRunSequencingData(runStats, runFailedStats, runExtStats);

						calculateData();
					});
				});
			});
		});

		//---

		function balanceSheetsUrl() {
			return jsRoutes.controllers.balancesheets.api.BalanceSheets.list().url;
		}

		function balanceSheetsForm() {
			return {
				year : activeYear,
				type : $routeParams.typeCode.replace("rs", "bi-"),
				orderBy : "year"
			};
		}

		function objectTypesUrl() {
			return jsRoutes.controllers.commons.api.CommonInfoTypes.list().url;
		}

		function objectTypesForm() {
			return {
				objectTypeCode : "Run",
				includes : ["code", "name"]
			};
		}

		function projectsUrl() {
			return jsRoutes.controllers.projects.api.Projects.list().url;
		}

		function projectsForm() {
			return {
				includes : ["code", "name"]
			};
		}

		function samplesUrl() {
			return jsRoutes.controllers.sampletypes.api.SampleTypes.list().url;
		}

		function samplesForm() {
			return {
				includes : ["code", "name", "category"]
			}
		}
	};
	
	var calculateData = function(){
		$scope.dtQuarters = datatable(configQuarterDT);
		$scope.dtQuarters.setData($scope.dataForYear.dataQuarterDT, $scope.dataForYear.dataQuarterDT.length);

		$scope.chartQuarter = $scope.balanceSheetsGeneralService.computeChartQuarters($scope.dataForYear, $routeParams.typeCode, $scope.dataForYear.year);

		$scope.dtSequencing = datatable(configSequencingDT);
		$scope.dtSequencing.setData($scope.dataForYear.dataSequencingDT, $scope.dataForYear.dataSequencingDT.length);

		var sumData = [{
			"property" : Messages('balanceSheets.sum'),
			"value" : $scope.dataForYear.total,
			"nbReadsets": $scope.dataForYear.totalRs
		}];

		$scope.dtSequencingSum = datatable(configSumDT);
		$scope.dtSequencingSum.setData(sumData, 1);
		$scope.chartSequencing = $scope.balanceSheetsGeneralService.computeChartSequencing($scope.dataForYear.dataSequencingDT, $routeParams.typeCode, $scope.dataForYear.year);

		$scope.dtProject = datatable(configProjectDT);
		$scope.dtProject.setData($scope.dataForYear.dataProjectDT, $scope.dataForYear.dataProjectDT.length);

		$scope.dtProjectSum = datatable(configProjectSumDT);
		$scope.dtProjectSum.setData([
		                             {
		                            	 "property" : Messages('balanceSheets.totalTen'),
		                            	 "value" : $scope.dataForYear.totalProject,
		                            	 "percentage" : $scope.dataForYear.total ? ($scope.dataForYear.totalProject * 100 / $scope.dataForYear.total).toFixed(2) + " %" : "100%" 
		                             },
		                             {
		                            	 "property" : Messages('balanceSheets.totalSum'),
		                            	 "value" : $scope.dataForYear.total,
		                            	 percentage : "100 %"
		                             }], 2);
		$scope.chartProject = $scope.balanceSheetsGeneralService.computeChartProject($scope.dataForYear.dataProjectDT,$scope.dataForYear.totalProject, $routeParams.typeCode, $scope.dataForYear.year);

		$scope.dtSample = datatable(configSampleDT);
		$scope.dtSample.setData($scope.dataForYear.dataSampleDT, $scope.dataForYear.dataSampleDT.length);

		$scope.dtSampleSum = datatable(configSampleSumDT);
		$scope.dtSampleSum.setData(sumData, 1);

		$scope.chartSample = $scope.balanceSheetsGeneralService.computeChartSample($scope.dataForYear.dataSampleDT, $scope.dataForYear.total, $routeParams.typeCode, $scope.dataForYear.year);

		
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
		for(var i = 0; i < $scope.dataForYear.listSeq.length; i++){
			var sequencingType = $scope.dataForYear.listSeq[i];
			position++;
			configRunDT.columns.push({
		     	   "property" : sequencingType.code,
		    	   "header" : sequencingType.name,
				   "type" : "text",
				   "render": (function(sequencingTypeCode) {
						return function(obj, line){
							var runInfo = obj[sequencingTypeCode]
							var display = runInfo.nbRuns || runInfo.nbRuns === 0 ? runInfo.nbRuns : '';
							if(runInfo.nbFailed){
								display += ' <span class="text-danger">(+' + runInfo.nbFailed + ' en echec)</span>'
							}
							if(runInfo.nbExternal){
								display += ' [+' + runInfo.nbExternal + ' ' + (runInfo.nbExternal > 1 ? 'externes' : 'externe') + ']'
							}
						return display;
						}
					})(sequencingType.code),
		    	   "position" : position
				});
		}
		position++;
		configRunDT.columns.push(
					{	"property":"nbAborted",
		        	   "header": Messages("balanceSheets.nbAborted"),
					   "type" :"Number",
					   "render":function(obj, line){
							return obj.nbAborted || obj.nbAborted === 0 ? '<div class="text-danger">' + obj.nbAborted + '</div>' : '';
						},
		        	   "position":position
		           });
		position++;
		configRunDT.columns.push(
		           {
		        	   "property":"total",
		        	   "header": Messages("balanceSheets.sumNoAborting"),
					   "type":"Number",
					   "render":function(obj, line){
							return obj.total || obj.total === 0 ? '<div class="text-primary">' + obj.total + '</div>' : '';
						},
		        	   "position":position
		           }
		);
		
		$scope.dtRun = datatable(configRunDT);
		$scope.dtRun.setData($scope.dataForYear.dataRunDT, $scope.dataForYear.dataRunDT.length);
		
		var sumData = [{
			"property" : Messages('balanceSheets.sum'),
			"value" : $scope.dataForYear.totalRun 
		}];
		$scope.dtRunSum = datatable(configRunSumDT);
		$scope.dtRunSum.setData(sumData, 1);
		
		$scope.loading=false;
	};

	$scope.computeExcelExport = function() {
		var output = "Bilan-" + $routeParams.year + "-" + $routeParams.typeCode + ".xlsx";
	
		var title = getGenericBilanAnneeTitle($routeParams.year, $routeParams.typeCode);
		var formatTables = function(tabPane) {
			return Array.from(document.getElementById(tabPane).getElementsByTagName("table"))
			.map(function(table) {
				return {
					rows: Array.from(table.rows)
					.map(function(row) {
						return {
							values: Array.from(row.children)
							.map(function(child) { return child.innerText; })
						}
					})
				}
			})
		}
		var sheets = [
			{
				name: Messages("balanceSheets.export.excel.table.quarters"),
				tables: formatTables("quarter")
			},
			{
				name: Messages("balanceSheets.export.excel.table.sequencingType"),
				tables: formatTables("sequencingType")
			},
			{
				name: Messages("balanceSheets.export.excel.table.firstTen"),
				tables: formatTables("firstTen")
			},
			{
				name: Messages("balanceSheets.export.excel.table.projectType"),
				tables: formatTables("sampleType")
			},
			{
				name: Messages("balanceSheets.export.excel.table.runType"),
				tables: formatTables("runType")
			}
		];
		$scope.balanceSheetsGeneralService.computeExcelExport(title, output, sheets);
	};

	$scope.computePdfExport = function() {
		var output = "Bilan-" + $routeParams.year + "-" + $routeParams.typeCode + ".pdf";
		
		$scope.balanceSheetsGeneralService.computePdfExport(output, [
			{
				"name": getBilanAnneeTitle($routeParams.year, $routeParams.typeCode, "- bilan par trimestre"),
				"id": "quarter"
			},
			{
				"name":  getBilanAnneeTitle($routeParams.year, $routeParams.typeCode, "- bilan par type séquençage"),
				"id": "sequencingType"
			},
			{
				"name": getBilanAnneeTitle($routeParams.year, $routeParams.typeCode, "- bilan des 10 premiers projets de l'année"),
				"id": "firstTen",
				"crop": 25
			},
			{
				"name":  getBilanAnneeTitle($routeParams.year, $routeParams.typeCode, "- bilan par type d'échantillon *"),
				"id": "sampleType",
				"crop": 20
			},
			{
				"name": getBilanAnneeTitle($routeParams.year, $routeParams.typeCode, "- bilan mensuel par type séquençage"),
				"id": "runType"
			},
		]);
	};

	$scope.balanceSheetsGeneralService = balanceSheetsGeneralSrv;
	
	
	var init = function(){
		var activeYear = $routeParams.year;
		tabService.addTabs({label:Messages("balanceSheets.tab.generalBalanceSheets"), href:jsRoutes.controllers.balancesheets.tpl.BalanceSheets.home($routeParams.typeCode, "general").url});
		$http.get(balanceSheetsUrl(), {params : balanceSheetsForm()})
		.success(function(data) {
			var years = Array.from(new Set(data.map(function (bs) { return bs.year; }))).sort(function(a, b) { return b - a; });
			years.forEach(function(year) {
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
		});

		//---

		function balanceSheetsUrl() {
			return jsRoutes.controllers.balancesheets.api.BalanceSheets.list().url;
		}

		function balanceSheetsForm() {
			return {
				includes: ["year"],
				type: $routeParams.typeCode.replace("rs", "bi-"),
				orderBy : "year"
			};
		}
	}
	init();
}]);


