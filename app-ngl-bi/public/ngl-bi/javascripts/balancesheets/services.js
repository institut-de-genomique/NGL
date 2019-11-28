 "use strict";
 
 angular.module('ngl-bi.BalanceSheetsService', []).
factory('balanceSheetsGeneralSrv', ['$http', 'mainService', 'datatable', '$parse', '$filter',
                                        function($http, mainService, datatable, $parse, $filter){
			
	 
		var balanceSheetsGeneralService = {
		
			computeYearlyData: function(balanceSheets){
				return balanceSheets.sort((bs1, bs2) => parseInt(bs1.year) - parseInt(bs2.year)).map(bs => {
					return {
						nbBases : bs.computations
						.filter(computation => computation.collection == "readsets")
						.map(computation => computation.result.value)
						.reduce((a, b) => a + b, 0),
						year : bs.year
					}
				});
			},

			mergeCategories: function(categories, toMerges){
				var categoryMap = new Map();
				var categoryMonthlyMap = new Map();
				categories.forEach(category => {
					categoryMap.set(category.label, category);
					categoryMonthlyMap.set(category.label, new Map());
					category.monthly.forEach(monthData => categoryMonthlyMap.get(category.label).set(monthData.month, monthData));
				});
				toMerges.forEach(toMerge => {
					toMerge.forEach(category => {
						categoryMap.get(category.label).result.value += category.result.value;
						let localMonthlyMap = categoryMonthlyMap.get(category.label);
						category.monthly.forEach(monthData => {
							localMonthlyMap.get(monthData.month).result.value += monthData.result.value;
						});
					});
				});
			},

			mergeReadSetComputations: function(readSetComputations) {
				var readSetComputation = readSetComputations[0];
				if(readSetComputations.length > 1){
					var monthlyMap = new Map();
					readSetComputation.monthly.forEach(monthData => monthlyMap.set(monthData.month, monthData));
					var sequencingTypes = [];
					var projects = [];
					var sampleTypes = [];
					for(var i=1;i<readSetComputations.length;i++){
						let currentComputation = readSetComputations[i];
						readSetComputation.result.value += currentComputation.result.value;
						currentComputation.monthly.forEach(monthData => monthlyMap.get(monthData.month).result.value += monthData.result.value);
						sequencingTypes.push(currentComputation.by.sequencingTypes);
						projects.push(currentComputation.by.projects)
						sampleTypes.push(currentComputation.by.sampleTypes)
					}
					balanceSheetsGeneralService.mergeCategories(readSetComputation.by.sequencingTypes, sequencingTypes);
					balanceSheetsGeneralService.mergeCategories(readSetComputation.by.projects, projects);
					balanceSheetsGeneralService.mergeCategories(readSetComputation.by.sampleTypes, sampleTypes);
				}
				return readSetComputation;
			},

			computeMonthlyData: function(readsetStats, total){
				var monthly = readsetStats.monthly
				.sort((data1, data2) => data1.month - data2.month)
				.reduce(function fillGaps(array, data) {
					let nextMonth = array.length === 0 ? 1 : array[array.length - 1].month + 1;
					if(data.month === nextMonth){
						array.push(data);
						return array;
					} else {
						array.push({
							month: nextMonth,
							result: {
								value: 0
							}
						});
						return fillGaps(array, data);
					}
				}, []);
				let lastMonth = monthly[monthly.length - 1].month;
				if(lastMonth < 12){
					for(var i=(lastMonth + 1); i <= 12; i++){
						monthly.push({
							month: i,
							result: {
								value: 0
							}
						});
					}
				}
				var accumulator = monthly.map(data => {
					return {
						quarter : balanceSheetsGeneralService.getQuarter(data.month),
						month : balanceSheetsGeneralService.getMonthName(data.month),
						nbBases: data.result.value
					}
				})
				.reduce((accumulator, data) => {
					if(accumulator.results.length > 0 && accumulator.quarterCounter === 3){
						accumulator.results.push({
							quarter : '',
							month : Messages("balanceSheets.sum"),
							nbBases : accumulator.quarterSum
						});
						accumulator.quarterSum = 0;
						accumulator.quarterCounter = 0;
						accumulator.quarterIndex++;
					} 
					accumulator.results.push(data);
					accumulator.quarterSum += data.nbBases;
					accumulator.quarterCounter++;
					accumulator.quarters[accumulator.quarterIndex].push({
						name: data.month,
						y: data.nbBases,
						x: ((accumulator.quarterIndex - 1) * 3) + accumulator.quarterCounter,
						_value: data.nbBases,
						_group: data.quarter
					});
					return accumulator;
				}, {results: [], quarterSum: 0, quarterCounter: 0, quarterIndex: 1, quarters: [null, [], [], [], []]});
				accumulator.results.push({
					quarter : '',
					month : Messages("balanceSheets.sum"),
					nbBases : accumulator.quarterSum
				}); 
				accumulator.results.push({
					quarter : '',
					month : Messages("balanceSheets.totalSum"),
					nbBases : total
				});
				return accumulator;
			},

			computeSequencingData: function(readsetStats, runTypes, total){
				//Initialize runTypeMap
				var sequencerMap = new Map();
				for(var i=0; i<runTypes.length;i++){
					sequencerMap.set(runTypes[i].code, runTypes[i].name);
				}
				var bySequencingType = readsetStats.by.sequencingTypes;
				return bySequencingType.map(sequencingType => {
					return {
						name : sequencerMap.get(sequencingType.label),
						nbBases : sequencingType.result.value,
						percentage : (sequencingType.result.value / total * 100).toFixed(2) + " %"
					}
				});
			},

			getTenBiggestProjects: function(readsetStats){
				return readsetStats.by.projects
				.sort((byProjectA, byProjectB) => byProjectB.result.value - byProjectA.result.value)
				.slice(0, 10);
			},

			computeProjectData: function(tenProjects, projects, totalProject, total){
				var projectMap = new Map();
				for(var i=0; i<projects.length;i++){
					projectMap.set(projects[i].code, projects[i].name);
				}
				return tenProjects.map(project => {
					return {
						code: project.label,
						name : projectMap.get(project.label),
						nbBases : project.result.value,
						percentageForTenProjects: (project.result.value / totalProject * 100).toFixed(2) + " %",
						percentageForYear : (project.result.value / total * 100).toFixed(2) + " %"
					}
				});
			},

			computeSampleData: function(readsetStats, sampleTypes, total){
				var sampleTypeMap = new Map();
				for(var i=0; i<sampleTypes.length;i++){
					sampleTypeMap.set(sampleTypes[i].code, sampleTypes[i].category.code);
				}
				var bySampleType = readsetStats.by.sampleTypes;
				return bySampleType.sort((dataA, dataB) => dataB.result.value - dataA.result.value)
				.map(sampleType => {
					return {
						category: sampleTypeMap.get(sampleType.label),
						type : sampleType.label,
						nbBases : sampleType.result.value,
						percentage : (sampleType.result.value / total * 100).toFixed(2) + " %"
					}
				});
			},

			computeRunSequencingTypes(runStats, runTypes){
				var sequencerMap = new Map();
				for(var i=0; i<runTypes.length;i++){
					sequencerMap.set(runTypes[i].code, runTypes[i].name);
				}
				return runStats.by.sequencingTypes.map(sequencingType => {
					return {
						code: sequencingType.label, 
						name: sequencerMap.get(sequencingType.label)
					}
				});
			},

			computeRunSequencingData: function(runStats, runFailedStats, runExtStats){
				/* init total by month arrays */
				var runStatsTotalMonth = Array(13).fill(0, 1, 13);
				var runFailedStatsTotalMonth = Array(13).fill(0, 1, 13);
				runStats.by.sequencingTypes.flatMap(sequencingType => sequencingType.monthly).forEach(monthdata => {
					runStatsTotalMonth[monthdata.month] += monthdata.result.nbElements;
				});
				runFailedStats.by.sequencingTypes.flatMap(sequencingType => sequencingType.monthly).forEach(monthdata => {
					runFailedStatsTotalMonth[monthdata.month] += monthdata.result.nbElements;
					runStatsTotalMonth[monthdata.month] -= monthdata.result.nbElements;
				});
				if(runExtStats) {
					runExtStats.by.sequencingTypes.flatMap(sequencingType => sequencingType.monthly).forEach(monthdata => {
						runStatsTotalMonth[monthdata.month] -= monthdata.result.nbElements;
					});
				}
				/* init result array */
				var dataRunDT = [];
				for(var i=1;i<=12;i++){
					dataRunDT[i-1] = {
						month: balanceSheetsGeneralService.getMonthName(i),
						nbAborted: runFailedStatsTotalMonth[i],
						total: runStatsTotalMonth[i]
					};
				}
				runStats.by.sequencingTypes.forEach(sequencingType => {
					for(var i=0;i<12;i++){
						dataRunDT[i][sequencingType.label] = 0;
					}
				});
				/* add nb success runs */
				runStats.by.sequencingTypes.forEach(sequencingType => {
					sequencingType.monthly.forEach(monthdata => {
						dataRunDT[monthdata.month - 1][sequencingType.label] = monthdata.result.nbElements;
					});
				});
				/* substract nb failed runs */
				runFailedStats.by.sequencingTypes.forEach(sequencingType => {
					sequencingType.monthly
					.filter(monthdata => dataRunDT[monthdata.month - 1][sequencingType.label] > 0)
					.forEach(monthdata => {
						dataRunDT[monthdata.month - 1][sequencingType.label] -= monthdata.result.nbElements;
					});
				});
				/* substract nb external runs */
				if(runExtStats) {
					runExtStats.by.sequencingTypes.forEach(sequencingType => {
						sequencingType.monthly
						.filter(monthdata => dataRunDT[monthdata.month - 1][sequencingType.label] > 0)
						.forEach(monthdata => {
							dataRunDT[monthdata.month - 1][sequencingType.label] -= monthdata.result.nbElements;
						});
					});
				}
				/* annotate nb failed runs */
				runFailedStats.by.sequencingTypes.forEach(sequencingType => {
					sequencingType.monthly.forEach(monthdata => {
						dataRunDT[monthdata.month - 1][sequencingType.label] += " (+" + monthdata.result.nbElements + " en echec)";
					});
				});
				/* annotate nb external runs  */
				if(runExtStats) {
					runExtStats.by.sequencingTypes.map(sequencingType => {
						sequencingType.monthly.forEach(monthdata => {
							dataRunDT[monthdata.month - 1][sequencingType.label] += " [+" + monthdata.result.nbElements + " externe]";
						});
					});
				}			
				//Add sum by typeSeq
				var dataRunTypeSeq = {month: Messages("balanceSheets.sumNoAborting")};
				/* add nb success runs */
				runStats.by.sequencingTypes.forEach(sequencingType => {
					dataRunTypeSeq[sequencingType.label] = sequencingType.result.nbElements;
				});
				/* substract nb failed runs */
				runFailedStats.by.sequencingTypes.forEach(sequencingType => {
					dataRunTypeSeq[sequencingType.label] -= sequencingType.result.nbElements;
				});
				/* substract nb external runs */
				if(runExtStats) {
					runExtStats.by.sequencingTypes.forEach(sequencingType => {
						dataRunTypeSeq[sequencingType.label] -= sequencingType.result.nbElements;
					});
				}
				dataRunDT.push(dataRunTypeSeq);
				//Add external sum by typeSeq
				if(runExtStats){
					var dataExtRunTypeSeq = {month: Messages("balanceSheets.sumExternal")};
					runStats.by.sequencingTypes.forEach(sequencingType => {
						dataExtRunTypeSeq[sequencingType.label] = 0;
					});
					runExtStats.by.sequencingTypes.forEach(sequencingType => {
						dataExtRunTypeSeq[sequencingType.label] = sequencingType.result.nbElements;
					});
					dataRunDT.push(dataExtRunTypeSeq);
				}
				return dataRunDT;
			},
			
			computeSumData : function(data){
				return [{
					property : Messages('balanceSheets.sum'),
					value : data.map(obj => obj.nbBases).reduce((a, b) => a + b)
			 	}];;
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
						data : data.quarters[i],
						name : "T" + i,
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
				 return monthNames[month - 1];
			 }
	};
			
	return balanceSheetsGeneralService;	
 
 }]);