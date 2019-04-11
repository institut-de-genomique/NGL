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
		//loadData();
		
		var actualYear = new Date().getFullYear();
		$scope.startYear = 2008;
		if($routeParams.typeCode=='rsnanopore'){
			$scope.startYear=2014;
		}
		$scope.dataByYear = [];
		 var queries = [];
		 for (var i = $scope.startYear; i <= actualYear; i++){
			 $scope.dataByYear[i-$scope.startYear] = {
					 nbBases : 0,
					 year : i
			 };
			 var formQuery = {};
			 formQuery.includes = [];
			 formQuery.includes.push("default");
			 formQuery.includes.push("runSequencingStartDate");
			 formQuery.includes.push("typeCode");
			 formQuery.typeCode=$routeParams.typeCode;
			//For rsillumina
			 formQuery.includes.push("treatments.ngsrg.default.nbBases");
			//for rsnanopore
			 formQuery.includes.push("treatments.ngsrg.default.1DReverse");
			 formQuery.includes.push("treatments.ngsrg.default.1DForward");
			 formQuery.includes.push("treatments.readQuality.default.1DReverse");
			 formQuery.includes.push("treatments.readQuality.default.1DForward");
			 formQuery.limit = 100000;
			 formQuery.fromDate = moment("01/01/"+i, Messages("date.format").toUpperCase()).valueOf();
			 formQuery.toDate = moment("31/12/"+i, Messages("date.format").toUpperCase()).valueOf();
			 queries.push( $http.get(jsRoutes.controllers.readsets.api.ReadSets.list().url, {params : formQuery}) );
		 }
		 
		 $q.all(queries).then(function(results) {
				results.forEach(function(result){
					$scope.dataByYear = $scope.balanceSheetsGeneralService.computeDataByYear(result.data,$scope.startYear,$scope.dataByYear);
				});
				mainService.put($routeParams.typeCode+'-general',$scope.dataByYear);
				calculateData($scope.dataByYear);
         });		
	}

	var calculateData = function(dataByYear)
	{
		$scope.dtYearlyBalanceSheets = datatable(configYearlyDT);
		$scope.dtYearlyBalanceSheets.setData(dataByYear, dataByYear.length);

		var sumData = $scope.balanceSheetsGeneralService.computeSumData(dataByYear);
		$scope.dtSumYearlyBalanceSheets = datatable(configYearlySumDT);
		$scope.dtSumYearlyBalanceSheets.setData(sumData, 1);

		$scope.chartYearlyBalanceSheets = $scope.balanceSheetsGeneralService.computeChartYearlyBalanceSheets(dataByYear);

		$scope.loading = false;
	}


var init = function(){
	// Tabs
	var actualYear = new Date().getFullYear();
	tabService.addTabs({label:Messages("balanceSheets.tab.generalBalanceSheets"), href:jsRoutes.controllers.balancesheets.tpl.BalanceSheets.home($routeParams.typeCode, "general").url});
	var startYear = 2008;
	if($routeParams.typeCode=='rsnanopore'){
		startYear=2014;
	}
	for(var i = actualYear; i >= startYear ; i--){
		tabService.addTabs({label:Messages("balanceSheets.tab.year") +" "+ i,href:jsRoutes.controllers.balancesheets.tpl.BalanceSheets.home($routeParams.typeCode, i).url});
	}

	tabService.activeTab(0);

	$scope.loading=true;

	if(angular.isDefined(mainService.get($routeParams.typeCode+'-general'))){
		var dataByYear = mainService.get($routeParams.typeCode+'-general');
		calculateData(dataByYear);
	}else{
		loadData();
		
	}
	
};
init();	
}]);

angular.module('home').controller('BalanceSheetsYearCtrl', ['$scope', '$http','mainService', 'tabService', 'lists', 'datatable', 'balanceSheetsGeneralSrv', '$routeParams', 
                                                            function($scope, $http, mainService, tabService, lists, datatable, balanceSheetsGeneralSrv, $routeParams){

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
				colorBlue($scope.dtRun,$scope.dataForYear.dataRunDT.length-1);
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
	var actualYear = new Date().getFullYear();

	var loadData = function(activeYear)
	{
		var startDate = moment("01/01/"+activeYear, Messages("date.format").toUpperCase()).valueOf();
		var endDate = moment("31/12/"+activeYear, Messages("date.format").toUpperCase()).valueOf();
		
		var form = {includes : [], typeCodes : []};
		form.includes.push("default");
		//For rsillumina
		form.includes.push("treatments.ngsrg.default.nbBases");
		//for rsnanopore
		form.includes.push("treatments.ngsrg.default.1DReverse");
		form.includes.push("treatments.ngsrg.default.1DForward");
		form.includes.push("treatments.readQuality.default.1DReverse");
		form.includes.push("treatments.readQuality.default.1DForward");
		form.includes.push("projectCode");
		form.includes.push("runTypeCode");
		form.includes.push("runSequencingStartDate");
		form.includes.push("sampleOnContainer.sampleTypeCode");
		form.includes.push("sampleOnContainer.sampleCategoryCode");
		form.fromDate = startDate;
		form.toDate = endDate;
		form.typeCode=$routeParams.typeCode;
		form.limit = 20000;

		var projectForm = {includes : []};
		projectForm.includes.push("code");
		projectForm.includes.push("name");
		projectForm.includes.push("traceInformation.creationDate");
		
		var runForm = {includes:[]};
		runForm.includes.push("default");
		runForm.categoryCode=$routeParams.typeCode.replace('rs','');
		runForm.fromDate=startDate;
		runForm.toDate=endDate;
		runForm.limit = 20000;
		
		$http.get(jsRoutes.controllers.readsets.api.ReadSets.list().url, {params : form})
		.success(function(data, status, headers, config) {
			$http.get(jsRoutes.controllers.commons.api.CommonInfoTypes.list().url,{params:{objectTypeCode:"Run"},key:"runTypes"})
			.success(function(results, status, headers, config) {
				$http.get(jsRoutes.controllers.projects.api.Projects.list().url, {params : projectForm})
				.success(function(projectData, status, headers, config) {
					$http.get(jsRoutes.controllers.runs.api.Runs.list().url, {params : runForm})
					.success(function(runData, status, headers, config) {
					$scope.dataForYear = $scope.balanceSheetsGeneralService.computeDataForYear(data,results,projectData,runData,activeYear);
					mainService.put($routeParams.typeCode+'-'+activeYear,$scope.dataForYear );
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
		for(var key of $scope.dataForYear.listSeq.keys()){
			position++;
			configRunDT.columns.push({
		     	   "property" : "type_"+key,
		    	   "header" : $scope.dataForYear.listSeq.get(key),
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
		$scope.loading=true;
		// Year managing
		var actualYear = new Date().getFullYear();
		var activeYear = $routeParams.year;

		// Tabs
		var startYear = 2008;
		if($routeParams.typeCode=='rsnanopore'){
			startYear=2014;
		}
		tabService.addTabs({label:Messages("balanceSheets.tab.generalBalanceSheets"), href:jsRoutes.controllers.balancesheets.tpl.BalanceSheets.home($routeParams.typeCode, "general").url});
		for(var i = actualYear; i >= startYear ; i--){
			tabService.addTabs({label:Messages("balanceSheets.tab.year") +" "+ i,href:jsRoutes.controllers.balancesheets.tpl.BalanceSheets.home($routeParams.typeCode, i).url});
		}
		// Activate the tab corresponding to the selected year
		tabService.activeTab(actualYear - activeYear + 1);

		if(mainService.get('balanceSheetActiveTab') == undefined){
			mainService.put('balanceSheetActiveTab', 'quarter');
		}


		if(angular.isDefined(mainService.get($routeParams.typeCode+'-'+activeYear))){
			$scope.dataForYear = mainService.get($routeParams.typeCode+'-'+activeYear);
			calculateData();
		}else{
			loadData(activeYear);
		}
	
	}
	init();

}]);


