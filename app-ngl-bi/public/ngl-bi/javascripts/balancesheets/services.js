 "use strict";
 
 angular.module('ngl-bi.BalanceSheetsService', []).
factory('balanceSheetsGeneralSrv', ['$http', 'mainService', 'datatable', '$parse', '$filter',
                                        function($http, mainService, datatable, $parse, $filter){
			
	 
		var balanceSheetsGeneralService = {
		
			computeDataByYear : function(data,startYear,dataByYear){
			/*	var actualYear = new Date().getFullYear();
				var startYear = 2008;
				if(typeCode=='rsnanopore'){
					startYear=2014;
				}
				var dataByYear = [];
				 for (var i = startYear; i <= actualYear; i++){
					 dataByYear[i-startYear] = {
							 nbBases : 0,
							 year : i
					 };
				 }*/
						 
				 // Calculating our bases for each year
				 for(var i = 0; i < data.length; i++){
					var readsetDate =  balanceSheetsGeneralService.convertToDate(data[i].runSequencingStartDate);
					dataByYear[readsetDate.getFullYear() - startYear].nbBases += balanceSheetsGeneralService.getProperty(data[i]);
				 }
				 return dataByYear;
			},
			
			computeDataForYear : function(data,runTypes,projectData,runData,year){
				var dataReadSet = {
						total : 0,
						totalProject : 0,
						readsets : [],
						months : [],
						quarters :[],
						dataQuarterDT:[],
						lineToColorQuarter:[],
						dataSequencingDT:[],
						dataProjectDT:[],
						dataSampleDT:[],
						totalRun:0,
						monthsRuns:[],
						typeSeqRuns:new Map(),
						listSeq:new Map(),
						dataRunDT:[]
				};
				
				//initialize months
				for(var i = 0; i <12; i++){
					dataReadSet.months[i] = {
						quarter : balanceSheetsGeneralService.getQuarter(i),
						month : balanceSheetsGeneralService.getMonthName(i),
						nbBases : 0
					 }
					
					dataReadSet.monthsRuns[i] = {
							quarter : balanceSheetsGeneralService.getQuarter(i),
							month : balanceSheetsGeneralService.getMonthName(i),
							nbRunTotal : 0,
							nbRunAborted : 0
						 }
				 }
				//initialize quarters
				for(var i=1;i<=4;i++){
					dataReadSet.quarters[i] = {
						nbBases : 0,
						data : []
					}
				}
				//Initialize runTypeMap
				var sequencerMap = new Map();
				for(var i=0; i<runTypes.length;i++){
					sequencerMap.set(runTypes[i].code, runTypes[i].name);
				}
				 
				//Initialize projectMap
				var projectMap = new Map();
				for(var i=0; i<projectData.length;i++){
					projectMap.set(projectData[i].code,projectData[i].name);
				}
				var dataSequencing = new Map();
				var dataProject = new Map();
				var dataSample = new Map();
				
				
				for(var i = 0; i < data.length; i++){
					data[i].runSequencingStartDate = balanceSheetsGeneralService.convertToDate(data[i].runSequencingStartDate);
					 var fullYear = data[i].runSequencingStartDate.getFullYear();
					 var monthValue = data[i].runSequencingStartDate.getMonth();
					 
					 if(data[i].runSequencingStartDate.getFullYear() == year){
						 var valueNbBases = balanceSheetsGeneralService.getProperty(data[i]);
						 dataReadSet.total += valueNbBases;
						 dataReadSet.months[data[i].runSequencingStartDate.getMonth()].nbBases += valueNbBases;
						 dataReadSet.quarters[balanceSheetsGeneralService.getQuarter(data[i].runSequencingStartDate.getMonth())].nbBases += valueNbBases;
						 if(data[i].sampleOnContainer == null || data[i].sampleOnContainer == undefined){
							 data[i].sampleOnContainer = {
									 sampleTypeCode : 'not-defined',
									 sampleCategoryCode : 'unknown'
							 };
						 }
						 var codeSequencer = data[i].runTypeCode;
						 if(dataSequencing.get(codeSequencer) != undefined){
							 dataSequencing.get(codeSequencer).nbBases +=valueNbBases;
						 }else{
							 dataSequencing.set(codeSequencer, {name : sequencerMap.get(codeSequencer),
																			nbBases : valueNbBases,
																			percentage : null});
						 }
						 var projectCode = data[i].projectCode;
						 if(dataProject.get(projectCode) != undefined){
							 dataProject.get(projectCode).nbBases += valueNbBases;
						 }else{
							 dataProject.set(projectCode, {code : projectCode,
								 							name : projectMap.get(projectCode),
								 							nbBases : valueNbBases,
								 							percentageForTenProjects:null,
								 							percentageForYear:null
								 							})
						 }
						 if(dataSample.get(data[i].sampleOnContainer.sampleTypeCode) != undefined){
							 dataSample.get(data[i].sampleOnContainer.sampleTypeCode).nbBases += valueNbBases;
						 }else{
							 dataSample.set(data[i].sampleOnContainer.sampleTypeCode, {category : data[i].sampleOnContainer.sampleCategoryCode,
								 														type : data[i].sampleOnContainer.sampleTypeCode,
								 														nbBases : valueNbBases,
								 														percentage : null
							 															})
						 }
						 dataReadSet.readsets.push(data[i]);
					 }
				}
				
				//calculate dataSample
				var dataSampleArray = Array.from(dataSample.values());
				dataSampleArray.sort(function(a, b){return parseInt(b.nbBases) - parseInt(a.nbBases)});
				dataReadSet.dataSampleDT = dataSampleArray;
				
				 // Percentage
				 for(var i = 0; i < dataReadSet.dataSampleDT.length; i++){
					 dataReadSet.dataSampleDT[i].percentage = parseFloat((dataReadSet.dataSampleDT[i].nbBases * 100 / dataReadSet.total).toFixed(2)).toLocaleString() + " %";
				 }
				 
				//Calculate TenFirstProject
				 // We sort the projects by balanceSheetsFirstTen.nbBases
				var dataProjectArray = Array.from(dataProject.values());
				dataProjectArray.sort(function(a, b){return parseInt(b.nbBases) - parseInt(a.nbBases)});
				 
				 // We only keep the top ten
				dataReadSet.dataProjectDT = dataProjectArray.slice(0,10);
				 
				 var nbBasesForTenProjects = 0;
				 
				 for(var i = 0; i < dataReadSet.dataProjectDT.length; i++){
					 nbBasesForTenProjects += dataReadSet.dataProjectDT[i].nbBases;
				 }
				 dataReadSet.totalProject = nbBasesForTenProjects;
				 
				 // We calculate percentage for each project
				 for(var i = 0; i < dataReadSet.dataProjectDT.length; i++){
					 dataReadSet.dataProjectDT[i].percentageForTenProjects = parseFloat((dataReadSet.dataProjectDT[i].nbBases * 100 / nbBasesForTenProjects).toFixed(2)).toLocaleString() + " %";
					 dataReadSet.dataProjectDT[i].percentageForYear = parseFloat((dataReadSet.dataProjectDT[i].nbBases *100 / dataReadSet.total).toFixed(2)).toLocaleString() + " %";
				 }
				 
				
				//Calculate percentage for dataSequencingDT
				var index=0;
				for(var key of dataSequencing.keys()){
					dataSequencing.get(key).percentage = (dataSequencing.get(key).nbBases /  dataReadSet.total *100).toFixed(2) + " %";
					dataReadSet.dataSequencingDT[index]=dataSequencing.get(key);
					index++;
				}

				var previousQuarter=1;
				var valueQuarter = 1;
				var pos=0;
				for(i=0;i<12;i++){
					valueQuarter=dataReadSet.months[i].quarter;
					if(previousQuarter!=valueQuarter){
						//Add somme quarter
						var line = {
								 quarter : '',
								 month : Messages("balanceSheets.sum"),
								 nbBases : dataReadSet.quarters[previousQuarter].nbBases
						 };
						dataReadSet.dataQuarterDT.push(line);
						pos=i+valueQuarter-2;
						dataReadSet.lineToColorQuarter.push(pos);
						
					}
					dataReadSet.dataQuarterDT.push(dataReadSet.months[i]);
					dataReadSet.quarters[valueQuarter].data.push({
						name : dataReadSet.months[i].month,
						y :  dataReadSet.months[i].nbBases,
						x : i,
						_value : dataReadSet.months[i].nbBases,
						_group : valueQuarter
					});
					previousQuarter=valueQuarter;
				}
				
				var line = {
						 quarter : '',
						 month : Messages("balanceSheets.sum"),
						 nbBases : dataReadSet.quarters[4].nbBases
				 };
				dataReadSet.dataQuarterDT.push(line);
				//Add somme total
				var yearLine = {
						quarter : '',
						month : Messages("balanceSheets.totalSum"),
				 		nbBases : dataReadSet.total
				 };
				dataReadSet.dataQuarterDT.push(yearLine);
				
				dataReadSet.lineToColorQuarter.push(dataReadSet.dataQuarterDT.length-2);
				dataReadSet.lineToColorQuarter.push(dataReadSet.dataQuarterDT.length-1);
				
				//Calculate dataRun
				for(var i = 0; i < runData.length; i++){
					runData[i].sequencingStartDate = balanceSheetsGeneralService.convertToDate(runData[i].sequencingStartDate);
					var fullYear = runData[i].sequencingStartDate.getFullYear();
					var monthValue = runData[i].sequencingStartDate.getMonth();
					 
					if(fullYear == year){
						dataReadSet.totalRun++;
						dataReadSet.monthsRuns[monthValue].nbRunTotal++;
						var codeSequencer = runData[i].typeCode;
						if(dataReadSet.monthsRuns[monthValue].dataRun == undefined){
							dataReadSet.monthsRuns[monthValue].dataRun = new Map();
						}
						if(dataReadSet.monthsRuns[monthValue].dataRun.get(codeSequencer) == undefined){
							dataReadSet.monthsRuns[monthValue].dataRun.set(codeSequencer, {nbRun : 0, nbRunAbort:0});
						}
						
						if(runData[i].state.code === "FE-S"){
							dataReadSet.monthsRuns[monthValue].nbRunAborted++;
							dataReadSet.monthsRuns[monthValue].dataRun.get(codeSequencer).nbRunAbort++;
						}else{
							dataReadSet.monthsRuns[monthValue].dataRun.get(codeSequencer).nbRun++;
							if(dataReadSet.typeSeqRuns.get(codeSequencer)==undefined){
								dataReadSet.typeSeqRuns.set(codeSequencer,1);
							}else{
								dataReadSet.typeSeqRuns.set(codeSequencer,dataReadSet.typeSeqRuns.get(codeSequencer)+1);
							}
						}
						if(dataReadSet.listSeq.get(codeSequencer) == undefined){
							dataReadSet.listSeq.set(codeSequencer,sequencerMap.get(codeSequencer));
						}
						
						
					}
				};
				
				for(i=0;i<12;i++){
					var dataRunMonth={month:dataReadSet.monthsRuns[i].month,
								 nbAborted:dataReadSet.monthsRuns[i].nbRunAborted,
								 total:dataReadSet.monthsRuns[i].nbRunTotal};
					for(var seqKey of dataReadSet.listSeq.keys()){
						if(dataReadSet.monthsRuns[i].dataRun!=undefined && dataReadSet.monthsRuns[i].dataRun.get(seqKey)!=undefined){
							dataRunMonth["type_"+seqKey]=dataReadSet.monthsRuns[i].dataRun.get(seqKey).nbRun;
							if(dataReadSet.monthsRuns[i].dataRun.get(seqKey).nbRunAbort!=0){
								dataRunMonth["type_"+seqKey]+=" ("+dataReadSet.monthsRuns[i].dataRun.get(seqKey).nbRunAbort+")";
							}
						}else{
							dataRunMonth["type_"+seqKey]=0;
						}
					}
					dataReadSet.dataRunDT.push(dataRunMonth);
				};
				//Add sum by typeSeq
				var dataRunTypeSeq = {month: Messages("balanceSheets.sumNoAborting")};
				for(var seqKey of dataReadSet.listSeq.keys()){
					if(dataReadSet.typeSeqRuns.get(seqKey)!=undefined){
						dataRunTypeSeq["type_"+seqKey]=dataReadSet.typeSeqRuns.get(seqKey);
					}else{
						dataRunTypeSeq["type_"+seqKey]=0;
					}
				}
				dataReadSet.dataRunDT.push(dataRunTypeSeq);
				return dataReadSet;
			
			},
				
			
			getProperty : function(data){
				var value=0;
				if(data.typeCode=="rsillumina"){
					if(data.treatments.ngsrg!=null){
						value=data.treatments.ngsrg.default.nbBases.value;
					}
				}
				if(data.typeCode=="rsnanopore"){
					if(data.treatments.ngsrg!=null){
						value=data.treatments.ngsrg.default['1DForward'].value.nbBases;
						if(data.treatments.ngsrg.default['1DReverse']!=null){
							value+=data.treatments.ngsrg.default['1DReverse'].value.nbBases;
						}
					}else if(data.treatments.readQuality!=null){
						value=data.treatments.readQuality.default['1DForward'].value.nbBases;
						if(data.treatments.readQuality.default['1DReverse']!=null){
							value+=data.treatments.readQuality.default['1DReverse'].value.nbBases;
						}
					}
				}
				return value;
			},
			
			computeSumData : function(data){
				 var sum = [{
						property : Messages('balanceSheets.sum'),
						value : 0
				 }];
				// Calculing sum
				 for(var i = 0; i < data.length; i++){
					 sum[0].value += data[i].nbBases;
				 }
				return sum;
			},
					
			computeChartYearlyBalanceSheets : function(data){
				 var years = [];
				 var statData = [];
				 for(var i = 0; i < data.length; i++){
					 years[i] = data[i].year;
					 statData[i] = data[i].nbBases;
				 }
				 
				 var chartYearlyBalanceSheets = {
						chart : {
			                zoomType : 'x',
							height : 770,	
						},
						title : {
							text : Messages("balanceSheets.yearlyBases")
						},
						xAxis : {
							categories: years,
							crosshair: true,
							title : {
								text : 'Année',
							},
							labels : {
								enabled : true,
								rotation : -75
							},
							type : "category",
							tickPixelInterval : 1
						},
			
						yAxis : {
							title : {
								text : Messages("balanceSheets.nbBases")
							},
							labels: {
				                formatter: function () {
				                    return (this.value/Math.pow(10,12)).toFixed(2) + ' Tb';
				                }
				            },
							tickInterval : 2,
						},
						exporting : {
							enabled : true,
							filename : Messages('balanceSheets.export.general') + $filter('date')(new Date(), 'yyyyMMdd_HHmmss'),
							sourceWidth : 1200
							},
						series : [{
							type : 'column',
							name : Messages("balanceSheets.nbBases"), 
							data : statData,
							turboThreshold : 0
						}]
				};
				return chartYearlyBalanceSheets;
			},
			
			computeChartQuarters : function(data){
				 
				 var allSeries = [];
				 for(var i=1;i<=4;i++){
					 allSeries.push({
						data : data.quarters[i].data,
						name : "T"+i,
						type : 'column',
						turboThreshold : 0
					});	
				 }
					 
				 var chartQuarters = {
					 chart : {
						 zoomType : 'x',
						 height : 770,
						 reflow : true
					 },
					 title : {
						 text : Messages("balanceSheets.quarterBases")
					 },
					 xAxis : {
						 title : {
							 text : "Mois",
						 },
						 labels : {
							 enabled : true,
							 rotation : -45
						 },
						 type : "category",
						 tickPixelInterval : 1
					 },
					 yAxis : {
						 labels : {
							 formatter : function(){
								 return (this.value/Math.pow(10,9)) + ' Gb';
							 }
						 },
						 title : {
							 text : Messages("balanceSheets.nbBases")
						 }
					 },
					 exporting : {
						 enabled : true,
						 filename : Messages('balanceSheets.export.quarters') + $filter('date')(new Date(), 'yyyyMMdd_HHmmss'),
						 sourceWidth : 1200
					 },
					 series : allSeries,
					 plotOptions : {column:{grouping:false}}
				 };
				 return chartQuarters;
			 },
			 
			 computeChartSequencing : function(data){
				var dataSorted = data.sort(function(a,b){
					return b.nbBases - a.nbBases;
				});
				 var typeCode = [];
				 var statData = [];
				 for(var i = 0; i < dataSorted.length; i++){
					 typeCode.push(dataSorted[i].name);
					 statData.push(dataSorted[i].nbBases);
				 }
				 
				 var chartSequencing = {
						chart : {
			                zoomType : 'x',
							height : 770,
							type : 'column'
						},
						title : {
							text : Messages("balanceSheets.nbSequencingType")
						},
						xAxis : {
							categories: typeCode,
							title : {
								text : Messages("balanceSheets.sequencingType")
							},
							labels : {
								enabled : true,
								rotation : -75
							},
							type : "category",
							tickPixelInterval : 1
						},
			
						yAxis : {
							title : {
								text : Messages("balanceSheets.nbBases")
							},
							labels: {
				                formatter: function () {
				                    return (this.value/Math.pow(10,12)).toFixed(2) + ' Tb';
				                }
				            },
							tickInterval : 2,
						},
						exporting : {
							enabled : true,
							filename : Messages('balanceSheets.export.sequencingType') + $filter('date')(new Date(), 'yyyyMMdd_HHmmss'),
							sourceWidth : 1200
							},
						series : [{
							name : Messages("balanceSheets.nbBases"), 
							data : statData,
							turboThreshold : 0
							
						}]
				};
				 return chartSequencing;
			 },
			 
			 computeChartProject : function(data,sum){
				 var allData = [];
				 for(var i = 0; i < data.length; i++){
					 var temp = [];
					 temp.push(data[i].code);
					 var percentageValue = parseFloat((data[i].nbBases * 100 / sum).toFixed(2));
					 temp.push(percentageValue);
					 allData[i] = temp;
				 }
				 
				 var chartFirstTen = {
						 chart : {
							 type : 'pie',
							 options3d: {
					                enabled: true,
					                alpha: 45,
					                beta: 0
							 }
						 },
						 title : {
							 text : Messages("balanceSheets.tab.firstTen")
						 },
						 plotOptions : {
							 pie : {
								 size : 350,
								 allowPointSelect : true,
								 cursor : 'pointer',
								 depth: 35,
								 dataLabels : {
									 enabled : true,
									 format : "<b>{point.name}</b>	: {point.percentage:.2f} %"
								 },
						 showInLegend : true
							 }
						 },
						 exporting : {
							 enabled : true,
							 filename : Messages('balanceSheets.export.firstTen') + $filter('date')(new Date(), 'yyyyMMdd_HHmmss'),
							 sourceWidth : 1000
							 },
						 series : [{
							 name : Messages("balanceSheets.percentage"),
							 data : allData,
							 turboThreshold : 0
						 }]
				 }; 
				 return chartFirstTen;
			 },
			 
			 computeChartSample : function(data,sum){
				 var allData = [];
				 for(var i = 0; i < data.length; i++){
					 var temp = [];
					 temp.push(data[i].type);
					 var percentageValue = parseFloat((data[i].nbBases * 100 / sum).toFixed(2));
					 temp.push(percentageValue);
					 allData[i] = temp;
				 }
				 
				 var chartSample = {
						 chart : {
							 type : 'pie',
							 options3d: {
					                enabled: true,
					                alpha: 45,
					                beta: 0
					         }
						 },
						 title : {
							 text : Messages("balanceSheets.tab.projectType")
						 },
						 plotOptions : {
							 pie : {
								 size : 350,
								 allowPointSelect : true,
								 cursor : 'pointer',
								 depth: 35,
								 dataLabels : {
									 enabled : true,
									 format : "<b>{point.name}</b>	: {point.percentage:.2f} %"
								 },
						 		 showInLegend : true
							 }
						 },
						 exporting : {
							 enabled : true,
							 filename : Messages('balanceSheets.export.projectType') + $filter('date')(new Date(), 'yyyyMMdd_HHmmss'),
							 sourceWidth : 1000
						 },
						 series : [{
							 name : Messages("balanceSheets.percentage"),
							 data : allData,
							 turboThreshold : 0
						 }]
				 }; 
				 return chartSample;
			 },
			
			convertToDate : function(dateInMilliSeconds){
				 return new Date(dateInMilliSeconds);
			},
					
			 getQuarter : function(month){
				 return parseInt(month/3) + 1;
			 },
			 
			 getMonthName : function(month){
				 var monthNames = [Messages("balanceSheets.january"), Messages("balanceSheets.february"), Messages("balanceSheets.march"),
				                   Messages("balanceSheets.april"), Messages("balanceSheets.may"), Messages("balanceSheets.june"),
				                   Messages("balanceSheets.july"),Messages("balanceSheets.august"), Messages("balanceSheets.september"),
				                   Messages("balanceSheets.october"), Messages("balanceSheets.november"), Messages("balanceSheets.december")];
				 return monthNames[month];
			 }
	};
			
	return balanceSheetsGeneralService;	
 
 }]);