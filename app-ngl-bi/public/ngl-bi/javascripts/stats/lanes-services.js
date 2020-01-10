"use strict";

angular.module('ngl-bi.LanesStatsServices', []).
factory('chartsLanesService', ['$http', '$q','$parse', '$window', '$filter', 'datatable', 'lists', 'mainService','runSearchService',
	function($http, $q, $parse, $window, $filter, datatable, lists, mainService,runSearchService){

	var datatableConfig = {
			group : {
				active : true,
				callback:function(datatable){
					computeChart();
				}
			},
			search : {
				active:false
			},
			pagination:{
				mode:'local'
			},
			order:{
				mode:'local',
				orderBy:'code',
				callback:function(datatable){
					computeChart();
				}
			},
			hide:{
				active:true
			}
	};
	var defaultDatatableColumns = [
		{  	"property":"code",
			"header": Messages("runs.code"),
			"type" :"text",
			"order":true,
			"position":1,
			"groupMethod":"count:true",
		},
		{	"property":"typeCode",
			"header": Messages("runs.typeCode"),
			"type" :"text",
			"order":true,
			"group":true,
			"position":2
		},
		{	"property":"sequencingStartDate",
			"header": Messages("runs.sequencingStartDate"),
			"type" :"date",
			"order":true,
			"position":3
		},
		{	"property":"state.historical|filter:'F-RG'|get:'date'",
			"header": Messages("runs.endOfRG"),
			"type" :"date",
			"order":true,
			"position":4
		},
		{	"property":"state.code",
			"filter":"codes:'state'",					
			"header": Messages("runs.stateCode"),
			"type" :"text",
			"edit":true,
			"order":true,
			"choiceInList":true,
			"listStyle":'bt-select',
			"possibleValues":'searchService.lists.getStates()',
			"position":5	
		},
		{	"property":"valuation.valid",
			"filter":"codes:'valuation'",					
			"header": Messages("runs.valuation.valid"),
			"type" :"text",
			"order":true,
			"position":100
		},
		{	"property":"valuation.resolutionCodes",
			"header": Messages("runs.valuation.resolutions"),
			"render":'<div bt-select ng-model="value.data.valuation.resolutionCodes" bt-options="valid.code as valid.name group by valid.category.name for valid in searchService.lists.getResolutions()" ng-edit="false"></div>',
			"type" :"text",
			"hide":true,
			"position":101
		} ,
		{  	"property":"lanes",
			"render": function(value){
			if(angular.isDefined(value) && angular.isDefined(value.lanes)){
				var display = "";
				var treatment = statsConfigs.treatment;
				var valueColumn = statsConfigs.value;
				display+="<table class=\"table table-condensed table-hover table-bordered\">";
				display+="<thead>";
				display+="<tr>";
				display+="<th>Lane</th>";
				for(var l=0; l<value.lanes.length; l++){
					var nbLane = value.lanes[l].number;
					display+="<th>"+nbLane+"</th>";
				}
				display+="</tr></thead><tbody>";
				var mapData = getDataLane(value.lanes,treatment,valueColumn);
				for(var key of mapData.keys()){
					display += "<tr><td>"+key+"</td>";
					var tabData = mapData.get(key);
					for(var t=0; t<tabData.length; t++){
						display+="<td>{{"+tabData[t]+"|number:2}}</td>";
					}
					display+="</tr>";
				}
				display +="</tbody></table>";
				
				if(mapData.size==0)
					display="";
				return display;
			}
				
				
		},
    	"header": function(){return statsConfigs.label},
    	"type":"text",
    	"order":false
		}
		];	
	
	var getDataLane = function(lanes, treatment,valueColumn){
		var mapData = new Map();
		for(var l=0; l<lanes.length; l++){
			if(lanes[l].treatments[treatment] !=null){
				if(lanes[l].treatments[treatment].read1!=null && lanes[l].treatments[treatment].read1[valueColumn]!=null){
					var tabValue = [];
					if(mapData.get("read1")!=undefined){
						tabValue=mapData.get("read1");
					}
					tabValue.push(lanes[l].treatments[treatment].read1[valueColumn].value);
					mapData.set("read1",tabValue);
				}
				if(lanes[l].treatments[treatment].read2!=null && lanes[l].treatments[treatment].read2[valueColumn]!=null){
					var tabValue = [];
					if(mapData.get("read2")!=undefined){
						tabValue=mapData.get("read2");
					}
					tabValue.push(lanes[l].treatments[treatment].read2[valueColumn].value);
					mapData.set("read2",tabValue);
				}
				if(lanes[l].treatments[treatment].default!=null && lanes[l].treatments[treatment].default[valueColumn]!=null){
					var tabValue = [];
					if(mapData.get("default")!=undefined){
						tabValue=mapData.get("default");
					}
					tabValue.push(lanes[l].treatments[treatment].default[valueColumn].value);
					mapData.set("default",tabValue);
				}
			}
		}
		return mapData;
	}
	var queriesConfigs = [];
	var readsetDatatable;
	var excludeData;
	var charts = [];
	var chartMean;
	var mapSeriesLane = new Map();
	var mapSeries = new Map();
	var allData;
	var allDataGroup;
	var statsConfigs;
	var propertyGroupGetter;
	var symbols = {read1:{'symbol':'triangle'},
				   read2:{'symbol':'square'},
				   default:{'symbol':'diamond'}};
	

	var generateCharts = function(property) {
		readsetDatatable = datatable(datatableConfig);
		readsetDatatable.setColumnsConfig(defaultDatatableColumns);
		readsetDatatable.config.spinner.start = true;

		var properties = ["default"];
		var propExistingFiels = [];
		properties.push(statsConfigs.property);	
		propExistingFiels.push(statsConfigs.property);	
		properties.push("lanes.number");
		var form = angular.copy(runSearchService.convertForm());		
		form.excludes = undefined;
		form.includes = properties;
		form.existingFields = propExistingFiels;
		$http.get(jsRoutes.controllers.runs.api.Runs.list().url,{params:form}).success(function(data) {
			readsetDatatable.setData(data, data.length);
			readsetDatatable.config.spinner.start = false;
			computeChart();
		});
		
	};	
	
	var computeExcludeData = function(property){
		var properties = ["default"];
		var propNotExistingFiels = [];
		properties.push(statsConfigs.property);	
		propNotExistingFiels.push(statsConfigs.property);	
		properties.push("lanes.number");
		var form = angular.copy(runSearchService.convertForm());		
		form.excludes = undefined;
		form.includes = properties;
		form.notExistingFields = propNotExistingFiels;
		$http.get(jsRoutes.controllers.runs.api.Runs.list().url,{params:form}).success(function(data) {
			excludeData=data;
		});
	}

	var computeChart = function() {	
		var data = readsetDatatable.getData();
		//compute data
		charts = [];
		chartMean=undefined;
		propertyGroupGetter = undefined;
		if(readsetDatatable.config.group.by != undefined){
			propertyGroupGetter = readsetDatatable.config.group.by.property;
		}
		
		mapSeriesLane = new Map();
		if(propertyGroupGetter!=undefined){
			computeAllDataGroup(data, propertyGroupGetter);
		}else{
			computeAllData(data);
		}
		
		for(var key of allData.laneData.keys()){
			if(propertyGroupGetter!=undefined){
				charts.push(getChartGroup(key, allDataGroup.laneDataGroup.get(key),propertyGroupGetter));
			}else{
				charts.push(getChart(allData.laneData.get(key)));
			}
		}
		if(propertyGroupGetter!=undefined){
			chartMean=getChartMeanGroup(allDataGroup.dataGroup,propertyGroupGetter);
		}else{
			chartMean=getChartMean(allData.data);
		}
		
		console.log(charts);
	};
	
	
	var computeAllData = function(dataRun)
	{
		var treatment = statsConfigs.treatment;
		var value = statsConfigs.value;
		
		allData = {laneData:new Map(),
				   data:{dataRead1:[],
						dataRead2:[],
						dataDefault:[],
				   		},
				   };
		
		for(var i=0; i<dataRun.length; i++){
			var runCode = dataRun[i].code;
			var dataToCompute = {
					sumRead1:0,
					sumRead2:0,
					sumDefault:0,
					nbRead1:0,
					nbRead2:0,
					nbDefault:0,
					dataLane:{},
					dataLaneMean:allData.data,
			}
			
			if(dataRun[i].lanes !=null){
				for(var l=0; l<dataRun[i].lanes.length; l++){
					var nbLane = dataRun[i].lanes[l].number;
					var dataLane ={
							laneNumber:nbLane,
							dataRead1:[],
							dataRead2:[],
							dataDefault:[],
						};
						
					if(allData.laneData.get(nbLane)!=null){
						dataLane=allData.laneData.get(nbLane);
					}
					
					dataToCompute.dataLane=dataLane;
					
					if(dataRun[i].lanes[l].treatments[treatment] !=null){
						dataToCompute=computePropertyLane(dataRun, i, l, runCode, dataToCompute, undefined);
					}
					allData.laneData.set(nbLane,dataToCompute.dataLane);
				}
				
				dataToCompute=computeProperty(i, runCode, dataToCompute, undefined);
				allData.data=dataToCompute.dataLaneMean;
			}
		}
	};
	
	var computeAllDataGroup = function(dataRun, groupProperty)
	{
		var treatment = statsConfigs.treatment;
		var value = statsConfigs.value;
		allDataGroup = {laneDataGroup:new Map(),
				   		dataGroup:new Map(),
				   		};
		for(var i=0; i<dataRun.length; i++){
			var runCode = dataRun[i].code;
			var group = dataRun[i][groupProperty];
			var dataGroup ={dataGroup:[]};
			var dataLaneGroup =new Map();
			if(allDataGroup.dataGroup.get(group)!=null){
				dataGroup=allDataGroup.dataGroup.get(group);
			}
			if(allDataGroup.laneDataGroup.get(group)!=null){
				dataLaneGroup=allDataGroup.laneDataGroup.get(group);
			}
			
			var dataToCompute = {
					sumRead1:0,
					sumRead2:0,
					sumDefault:0,
					nbRead1:0,
					nbRead2:0,
					nbDefault:0,
					dataGroup:[],
					dataGroupMean:dataGroup,
			}
			if(dataRun[i].lanes !=null){
				for(var l=0; l<dataRun[i].lanes.length; l++){
					var nbLane = dataRun[i].lanes[l].number;
					var dataLaneGroup = new Map();
					var dataLane = {laneNumber:nbLane,
										groupValue:group,
										dataGroup:[]
										};
					if(allDataGroup.laneDataGroup.get(nbLane)!=null){
						dataLaneGroup=allDataGroup.laneDataGroup.get(nbLane);
						if(dataLaneGroup.get(group)!=null){
							dataLane=dataLaneGroup.get(group);
						}
					}
					dataToCompute.dataGroup=dataLane.dataGroup;
					if(dataRun[i].lanes[l].treatments[treatment] !=null){
						dataToCompute = computePropertyLane(dataRun, i, l, runCode, dataToCompute, group);
						dataLane.dataGroup=dataToCompute.dataGroup;
						dataLaneGroup.set(group,dataLane);
						allDataGroup.laneDataGroup.set(nbLane,dataLaneGroup);
					}
				}
				
			}
			
			dataToCompute.dataGroupMean=dataGroup;
			dataToCompute=computeProperty(i, runCode, dataToCompute, group);
			allDataGroup.dataGroup.set(group,dataToCompute.dataGroupMean);
		}
	};
		
	var computePropertyLane = function(dataRun, posRun, nbLane, runCode, data, propertyGroup){
		
		var valuePropertyR1 = getValueProperty(dataRun,posRun,nbLane,"read1");
		if(valuePropertyR1!=null){
			if(propertyGroup!=undefined){
				data.dataGroup.push(getPointPropertyGroup(runCode,posRun,valuePropertyR1,symbols.read1,'read1'));
			}else{
				data.dataLane.dataRead1.push(getPointProperty(runCode,posRun,valuePropertyR1,symbols.read1));
			}
			data.sumRead1=data.sumRead1+valuePropertyR1;
			data.nbRead1=data.nbRead1+1;
		}
		var valuePropertyR2 = getValueProperty(dataRun,posRun,nbLane,"read2");
		if(valuePropertyR2!=null){
			if(propertyGroup!=undefined){
				data.dataGroup.push(getPointPropertyGroup(runCode,posRun,valuePropertyR2,symbols.read2,'read2'));
			}else{
				data.dataLane.dataRead2.push(getPointProperty(runCode,posRun,valuePropertyR2,symbols.read2));
			}
			data.sumRead2=data.sumRead2+valuePropertyR2;
			data.nbRead2=data.nbRead2+1;
		}
		var valuePropertyDef = getValueProperty(dataRun,posRun,nbLane,"default");
		if(valuePropertyDef!=null){
			if(propertyGroup!=undefined){
				data.dataGroup.push(getPointPropertyGroup(runCode,posRun,valuePropertyDef,symbols.default,'default'));
			}else{
				data.dataLane.dataDefault.push(getPointProperty(runCode,posRun,valuePropertyDef,symbols.default));
			}
			data.sumDefault=data.sumDefault+valuePropertyDef;
			data.nbDefault=data.nbDefault+1;
		}
		
		return data;
	};
	
	var computeProperty = function(posRun, runCode, data, propertyGroup){
		if(data.nbRead1>0){
			var meanRead1 = data.sumRead1/data.nbRead1;
			if(propertyGroup!=undefined){
				data.dataGroupMean.dataGroup.push(getPointPropertyGroup(runCode,posRun,meanRead1,symbols.read1,'read1'));
			}else{
				data.dataLaneMean.dataRead1.push(getPointProperty(runCode,posRun,meanRead1,symbols.read1));
			}
		}
		if(data.nbRead2>0){
			var meanRead2 = data.sumRead2/data.nbRead2;
			if(propertyGroup!=undefined){
				data.dataGroupMean.dataGroup.push(getPointPropertyGroup(runCode,posRun,meanRead2,symbols.read2,'read2'));
			}else{
				data.dataLaneMean.dataRead2.push(getPointProperty(runCode,posRun,meanRead2,symbols.read2));
			}
			
		}
		if(data.nbDefault>0){
			var meanDefault = data.sumDefault/data.nbDefault;
			if(propertyGroup!=undefined){
				data.dataGroupMean.dataGroup.push(getPointPropertyGroup(runCode,posRun,meanDefault,symbols.default,'default'));
			}else{
				data.dataLaneMean.dataDefault.push(getPointProperty(runCode,posRun,meanDefault,symbols.default));
			}
		}
		return data;
	};
	
	var getValueProperty = function(dataRun, positionRun, laneNumber, keyValue)
	{
		var treatment = statsConfigs.treatment;
		var value = statsConfigs.value;
		var valueProperty=undefined;
		if(dataRun[positionRun].lanes[laneNumber].treatments[treatment][keyValue]!=null && dataRun[positionRun].lanes[laneNumber].treatments[treatment][keyValue][value]!=null){
			valueProperty=dataRun[positionRun].lanes[laneNumber].treatments[treatment][keyValue][value].value;
		}
		return valueProperty;
	};
	
	var getPointProperty = function(runCode,positionRun,valueProperty,symbol)
	{
		return {'name':runCode,'x':positionRun,'y':valueProperty, events : {
			// Redirects to valuation page of the clicked readset
			click : function() {
				$window.open(jsRoutes.controllers.runs.tpl.Runs.get(this.name).url);
			}
		}}
	};
	
	var getPointPropertyGroup = function(runCode,positionRun,valueProperty,symbol,group)
	{
		return {'name':runCode,'x':positionRun,'y':valueProperty,'marker':symbol,'_group':group,events : {
			// Redirects to valuation page of the clicked readset
			click : function() {
				$window.open(jsRoutes.controllers.runs.tpl.Runs.get(this.name).url);
			}
		}}
	};
	
	
	var getCommonChart = function()
	{
		var chart = {
				chart : {
					type: 'scatter',
					zoomType : 'x',
					height : 770
				},	
				xAxis : {
					title : {
						text : 'RunCode',
					},
					type : "category",
					tickPixelInterval : 1
				},

				yAxis : {
					title : {
						text :  statsConfigs.name
					},
					tickInterval : 2,
				},
				plotOptions: {
			        scatter: {
			            marker: {
			                radius: 4,
			                states: {
			                    hover: {
			                        enabled: true,
			                        lineColor: 'rgb(100,100,100)'
			                    }
			                }
			            },
			            states: {
			                hover: {
			                    marker: {
			                        enabled: false
			                    }
			                }
			            }
			        }
			    },
			    
		        
		}
		return chart;
	}
	
	var getChart = function(dataLane) {
		
		var allSeries = [];
		
		if(dataLane.dataRead1.length>0){
			allSeries.push({name:'read1',marker:symbols.read1, data:dataLane.dataRead1});
		}
		if(dataLane.dataRead2.length>0){
			allSeries.push({name:'read2',marker:symbols.read2,data:dataLane.dataRead2});
		}
		if(dataLane.dataDefault.length>0){
			allSeries.push({name:'default',marker:symbols.default,data:dataLane.dataDefault});
		}
		
		var chart = getCommonChart();
		chart.title = {text : statsConfigs.header+' Lane '+dataLane.laneNumber,};
		chart.plotOptions.scatter.tooltip={
                headerFormat: '<b>{series.name} </b><br>',
                pointFormat: '{point.y}'
            };
		chart.series=allSeries;
		return chart;
	};


	var getChartGroup = function(laneNumber, dataLane,propertyGroup) {
		
		var allSeries = [];
		for(var key of dataLane.keys()){
			var data = dataLane.get(key);
			allSeries.push({name:key,data:data.dataGroup});
		}
		var chart = getCommonChart();
		chart.title = {text : statsConfigs.header+' Lane '+laneNumber,};
		chart.plotOptions.scatter.tooltip={
				headerFormat: '<b>{series.name} </b><br>',
                pointFormat: '{point.y} ({point._group})'
            };
		chart.series=allSeries;
		chart.legend={
				layout: 'horizontal',
		        align: 'center',
		        verticalAlign: 'bottom',
				symbolWidth:'0px',
				symbolHeight:'0px',
				labelFormatter: function () {
					return '<span style="color:' + this.color + ';">' + this.name + '</span>';
				},
				itemWidth:100
			};
		return chart;
	};
	
	var getChartMean = function(dataMean) {
		
		var allSeries = [];
		
		if(dataMean.dataRead1.length>0){
			allSeries.push({name:'read1',marker:symbols.read1,data:dataMean.dataRead1});
		}
		if(dataMean.dataRead2.length>0){
			allSeries.push({name:'read2',marker:symbols.read2,data:dataMean.dataRead2});
		}
		if(dataMean.dataDefault.length>0){
			allSeries.push({name:'default',marker:symbols.default,data:dataMean.dataDefault});
		}
		
		var chart = getCommonChart();
		chart.title = {text :  statsConfigs.header,};
		chart.plotOptions.scatter.tooltip={
				headerFormat: '<b>{series.name} </b><br>',
                pointFormat: '{point.y}'
            };
		chart.series=allSeries;
		return chart;
	};

	var getChartMeanGroup = function(dataMean,propertyGroup) {
		
		var allSeries = [];
		for(var key of dataMean.keys()){
			var data = dataMean.get(key);
			allSeries.push({name:key,data:data.dataGroup});
		}
		
		var chart = getCommonChart();
		chart.title = {text :  statsConfigs.header,};
		chart.plotOptions.scatter.tooltip={
				headerFormat: '<b>{series.name} </b><br>',
                pointFormat: '{point.y} ({point._group})'
            };
		chart.series=allSeries;
		
		
		chart.legend={
			layout: 'horizontal',
	        align: 'center',
	        verticalAlign: 'bottom',
			symbolWidth:'0px',
			symbolHeight:'0px',
			labelFormatter: function () {
				return '<span style="color:' + this.color + ';">' + this.name + '</span>';
			},
			itemWidth:100
		};
		
		return chart;
	};
	
	
	
	var initListService = function(){
			lists.refresh.treatmentTypes({levels:'Lane'});
	};
	
	var getLabelProperty = function()
	{
		for(var l=0; l<chartService.properties.length; l++){
			if(chartService.properties[l].code==chartService.property){
				return chartService.properties[l].name;
			}
		}
		return null;
	}
	
	var chartService = {
			lists : lists,
			treatmentType:undefined,
			treatmentNames:undefined,
			properties:[],
			property:undefined,
			
			datatable : function() {return readsetDatatable;},

			init : function() {
				initListService();
				//loadData();
			},
			
			charts : function() {
				return charts;
			},
			
			chartMean : function() {
				return chartMean;
			},
			refreshProperty:function()
			{
				if(this.treatmentType!=undefined){
					$http.get(jsRoutes.controllers.treatmenttypes.api.TreatmentTypes.get(this.treatmentType).url,{params:{levels:"Lane"}}).success(function(data) {
						chartService.properties=data.propertiesDefinitions;
						chartService.treatmentNames=data.names;
						
					});
				};
			},
			selectProperty:function()
			{
				if(this.property!=undefined){
					this.loadData();
				}
			},
			isData:function()
			{
				if(runSearchService.datatable!=undefined && runSearchService.datatable.getData()!=undefined && runSearchService.datatable.getData().length>0 && this.property!=undefined){
					return true;
				}else{
					return false;
				}
			},
			loadData:function() {
				if(this.isData()){
					statsConfigs = {header:this.treatmentType+' '+this.property, code:this.treatmentType+'.'+this.property,property:'lanes.treatments.'+this.treatmentNames,value:this.property,treatment:this.treatmentNames,label:getLabelProperty()};
					computeExcludeData(this.property);
					generateCharts(this.property);
					this.getExcludeData();
				}else{
					charts=[];
					readsetDatatable=undefined;
				}
			},
			getExcludeData:function()
			{
				return excludeData;
			},
	};
	return chartService;
}]);
