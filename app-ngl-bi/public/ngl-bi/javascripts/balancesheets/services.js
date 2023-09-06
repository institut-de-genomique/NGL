 "use strict";
 
 angular.module('ngl-bi.BalanceSheetsService', []).
factory('balanceSheetsGeneralSrv', ['$http', '$q', 'mainService', 'datatable', '$parse', '$filter',
                                        function($http, $q, mainService, datatable, $parse, $filter){
			
	 
		var balanceSheetsGeneralService = {
		
			computeYearlyData: function(balanceSheets, sampleTypes){
				var sampleTypeMap = new Map();
				for(var i=0; i<sampleTypes.length;i++){
					sampleTypeMap.set(sampleTypes[i].code, sampleTypes[i].category.name);
				}
				return balanceSheets.sort(function(bs1, bs2) { parseInt(bs1.year) - parseInt(bs2.year)} ).map(function (bs) {
					return {
						year : bs.year,
						nbBases : bs.computations
							.filter(function(computation) { return computation.collection == "readsets"; })
							.map(function(computation) { return computation.result.value; })
							.reduce(function(a, b) { return a + b; }, 0),
						nbReadsets : bs.computations
							.filter(function(computation) { return computation.collection == "readsets"; })[0]
							.result.nbElements,
						sampleCategories : bs.computations
							.filter(function (computation) { return computation.collection == "readsets"; })[0]
							.by.sampleTypes
							.reduce(function(categories, sampleType) {
								var category = categories[sampleTypeMap.get(sampleType.label)];
								if(category) {
									categories[sampleTypeMap.get(sampleType.label)] = category + sampleType.result.nbElements;
								} else {
									categories[sampleTypeMap.get(sampleType.label)] = sampleType.result.nbElements;
								} return categories;
							}, {})
					}
				});
			},

			mergeCategories: function(categories, toMerges){
				var categoryMap = new Map();
				var categoryMonthlyMap = new Map();
				categories.forEach(function(category) {
					categoryMap.set(category.label, category);
					categoryMonthlyMap.set(category.label, new Map());
					category.monthly.forEach(function(monthData) { categoryMonthlyMap.get(category.label).set(monthData.month, monthData) });
				});
				toMerges.forEach(function(toMerge) {
					toMerge.forEach(function(category) {
						categoryMap.get(category.label).result.value += category.result.value;
						var localMonthlyMap = categoryMonthlyMap.get(category.label);
						category.monthly.forEach(function(monthData) {
							localMonthlyMap.get(monthData.month).result.value += monthData.result.value;
						});
					});
				});
			},

			mergeReadSetComputations: function(readSetComputations) {
				var readSetComputation = readSetComputations[0];
				if(readSetComputations.length > 1){
					var monthlyMap = new Map();
					readSetComputation.monthly.forEach(function (monthData) { monthlyMap.set(monthData.month, monthData) });
					var sequencingTypes = [];
					var projects = [];
					var sampleTypes = [];
					for(var i=1;i<readSetComputations.length;i++){
						var currentComputation = readSetComputations[i];
						readSetComputation.result.value += currentComputation.result.value;
						currentComputation.monthly.forEach(function(monthData) { monthlyMap.get(monthData.month).result.value += monthData.result.value; });
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
				.sort(function(data1, data2) { return data1.month - data2.month; })
				.reduce(function fillGaps(array, data) {
					var nextMonth = array.length === 0 ? 1 : array[array.length - 1].month + 1;
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
				var startAccumulator = {results: [], quarterSum: 0, quarterCounter: 0, quarterIndex: 1, quarters: [null, [], [], [], []]};
				if(monthly && monthly.length) {
					var lastMonth = monthly[monthly.length - 1].month;
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
					var accumulator = monthly.map(function(data) {
						return {
							quarter : balanceSheetsGeneralService.getQuarter(data.month),
							month : balanceSheetsGeneralService.getMonthName(data.month),
							nbBases: data.result.value
						};
					})
					.reduce(function(accumulator, data) {
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
					}, startAccumulator);
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
				} else {
					return startAccumulator;
				}				
			},

			computeSequencingData: function(readsetStats, runTypes, total){
				//Initialize runTypeMap
				var sequencerMap = new Map();
				for(var i=0; i<runTypes.length;i++){
					sequencerMap.set(runTypes[i].code, runTypes[i].name);
				}
				var bySequencingType = readsetStats.by.sequencingTypes;
				return bySequencingType.map(function(sequencingType) {
					return {
						name : sequencerMap.get(sequencingType.label),
						nbBases : sequencingType.result.value,
						percentage : (sequencingType.result.value / total * 100).toFixed(2) + " %"
					}
				});
			},

			getTenBiggestProjects: function(readsetStats){
				return readsetStats.by.projects
				.sort(function(byProjectA, byProjectB) { return byProjectB.result.value - byProjectA.result.value; })
				.slice(0, 10);
			},

			computeProjectData: function(tenProjects, projects, totalProject, total){
				var projectMap = new Map();
				for(var i=0; i<projects.length;i++){
					projectMap.set(projects[i].code, projects[i].name);
				}
				return tenProjects.map(function(project) {
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
					sampleTypeMap.set(sampleTypes[i].code, sampleTypes[i].category.name);
				}
				var bySampleType = readsetStats.by.sampleTypes;
				return bySampleType.sort(function(dataA, dataB) { return dataB.result.value - dataA.result.value; })
				.map(function(sampleType) {
					return {
						category: sampleTypeMap.get(sampleType.label),
						type : sampleType.label,
						nbBases : sampleType.result.value,
						percentage : (sampleType.result.value / total * 100).toFixed(2) + " %",
						nbRS : sampleType.result.nbElements
					};
				});
			},

			computeRunSequencingTypes: function(runStats, runTypes){
				var sequencerMap = new Map();
				for(var i=0; i<runTypes.length;i++){
					sequencerMap.set(runTypes[i].code, runTypes[i].name);
				}
				return runStats.by.sequencingTypes.map(function(sequencingType) {
					return {
						code: sequencingType.label, 
						name: sequencerMap.get(sequencingType.label)
					};
				}).sort(function(a, b) { return a.name < b.name ? -1 : 1; });
			},

			computeRunSequencingData: function(runStats, runFailedStats, runExtStats){
				/* init total by month arrays */
				var runStatsTotalMonth = Array(13).fill(0, 1, 13);
				var runFailedStatsTotalMonth = Array(13).fill(0, 1, 13);
				runStats.by.sequencingTypes.flatMap(function(sequencingType) { return sequencingType.monthly; }).forEach(function(monthdata) {
					runStatsTotalMonth[monthdata.month] += monthdata.result.nbElements;
				});
				if(!runFailedStats) {
					runFailedStats = {by: {sequencingTypes: []}};
				}
				runFailedStats.by.sequencingTypes.flatMap(function(sequencingType) { return sequencingType.monthly; }).forEach(function(monthdata) {
					runFailedStatsTotalMonth[monthdata.month] += monthdata.result.nbElements;
					runStatsTotalMonth[monthdata.month] -= monthdata.result.nbElements;
				});
				if(!runExtStats){
					runExtStats = {by: {sequencingTypes: []}};
				}
				runExtStats.by.sequencingTypes.flatMap(function (sequencingType) { return sequencingType.monthly; }).forEach(function(monthdata) {
					runStatsTotalMonth[monthdata.month] -= monthdata.result.nbElements;
				});
				/* init result array */
				var dataRunDT = [];
				for(var i=1;i<=12;i++){
					dataRunDT[i-1] = {
						month: balanceSheetsGeneralService.getMonthName(i),
						nbAborted: runFailedStatsTotalMonth[i],
						total: runStatsTotalMonth[i]
					};
				}
				runStats.by.sequencingTypes.forEach(function(sequencingType) {
					for(var i=0;i<12;i++){
						dataRunDT[i][sequencingType.label] = {nbRuns: 0, nbFailed: null, nbExternal: null};
					}
				});
				/* add nb success runs */
				runStats.by.sequencingTypes.forEach(function(sequencingType) {
					sequencingType.monthly.forEach(function(monthdata) {
						dataRunDT[monthdata.month - 1][sequencingType.label].nbRuns = monthdata.result.nbElements;
					});
				});
				/* substract nb failed runs */
				runFailedStats.by.sequencingTypes.forEach(function(sequencingType) {
					sequencingType.monthly
					.filter(function(monthdata) { return dataRunDT[monthdata.month - 1][sequencingType.label].nbRuns > 0; })
					.forEach(function(monthdata) {
						dataRunDT[monthdata.month - 1][sequencingType.label].nbRuns -= monthdata.result.nbElements;
					});
				});
				/* substract nb external runs */
				runExtStats.by.sequencingTypes.forEach(function(sequencingType) {
					sequencingType.monthly
					.filter(function(monthdata) { return dataRunDT[monthdata.month - 1][sequencingType.label].nbRuns > 0; })
					.forEach(function(monthdata) {
						dataRunDT[monthdata.month - 1][sequencingType.label].nbRuns -= monthdata.result.nbElements;
					});
				});
				/* annotate nb failed runs */
				runFailedStats.by.sequencingTypes.forEach(function(sequencingType) {
					sequencingType.monthly.forEach(function(monthdata) {
						dataRunDT[monthdata.month - 1][sequencingType.label].nbFailed = monthdata.result.nbElements;
					});
				});
				/* annotate nb external runs  */
				runExtStats.by.sequencingTypes.map(function(sequencingType) {
					return sequencingType.monthly.forEach(function(monthdata) {
						dataRunDT[monthdata.month - 1][sequencingType.label].nbExternal = monthdata.result.nbElements;
					});
				});			
				//Add sum by typeSeq
				var dataRunTypeSeq = {month: Messages("balanceSheets.sumNoAborting")};
				/* add nb success runs */
				runStats.by.sequencingTypes.forEach(function(sequencingType) {
					dataRunTypeSeq[sequencingType.label] = {nbRuns: 0, nbFailed: null, nbExternal: null};
				});
				runStats.by.sequencingTypes.forEach(function(sequencingType) {
					dataRunTypeSeq[sequencingType.label].nbRuns = sequencingType.result.nbElements;
				});
				/* substract nb failed runs */
				runFailedStats.by.sequencingTypes.forEach(function(sequencingType) {
					dataRunTypeSeq[sequencingType.label].nbRuns -= sequencingType.result.nbElements;
				});
				/* substract nb external runs */
				runExtStats.by.sequencingTypes.forEach(function(sequencingType) {
					dataRunTypeSeq[sequencingType.label].nbRuns -= sequencingType.result.nbElements;
				});
				dataRunDT.push(dataRunTypeSeq);
				//Add failed sum by typeSeq
				var dataFailedRunTypeSeq = {month: Messages("balanceSheets.nbAborted")};
				runStats.by.sequencingTypes.forEach(function(sequencingType) {
					dataFailedRunTypeSeq[sequencingType.label] = {nbRuns: 0, nbFailed: null, nbExternal: null};
				});
				runFailedStats.by.sequencingTypes.forEach(function(sequencingType) {
					dataFailedRunTypeSeq[sequencingType.label].nbRuns = sequencingType.result.nbElements;
				});
				dataRunDT.push(dataFailedRunTypeSeq);
				//Add external sum by typeSeq
				var dataExtRunTypeSeq = {month: Messages("balanceSheets.sumExternal")};
				runStats.by.sequencingTypes.forEach(function(sequencingType) {
					dataExtRunTypeSeq[sequencingType.label] = {nbRuns: 0, nbFailed: null, nbExternal: null};
				});
				runExtStats.by.sequencingTypes.forEach(function(sequencingType) {
					dataExtRunTypeSeq[sequencingType.label].nbRuns = sequencingType.result.nbElements;
				});
				dataRunDT.push(dataExtRunTypeSeq);
				return dataRunDT;
			},
			
			computeSumData : function(data){
				return [{
					property : Messages('balanceSheets.sum'),
					value : data.map(function(obj) { return obj.nbBases; }).reduce(function(a, b) { return a + b; })
			 	}];;
			},
					
			computeChartYearlyNbBases : function(data, type){
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
							filename : Messages('balanceSheets.export.general.nbBases') + "_" + $filter('date')(new Date(), 'yyyyMMdd_HHmmss'),
							chartOptions: {
								title : {
									text : Messages("balanceSheets.yearlyBases") + " (" + type + ")"
								}
							},
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

			computeChartYearlyNbReadsets : function(data, type) {
				var years = data.map(function(y) { return y.year; });

				var statData = data
				.flatMap(function(y) { return Object.keys(y.sampleCategories); })
				.reduce(function(set, key) { return set.add(key); }, new Set());
				statData = Array.from(statData)
				.map(function(category) {
					return {
						name: category,
						data: data.map(function(y) { return y.sampleCategories[category] || 0; }),
						turboThreshold : 0
					};
				});
				return {
					colors: ['#7cb5ec', '#f7a35c', '#90ee7e', '#7798BF', '#aaeeee', '#ff0066', '#eeaaee', '#55BF3B', '#DF5353', '#7798BF', '#aaeeee'],
					chart : {
			            zoomType : 'x',
						height : 770,	
						type: 'column'
					},
					title : {
						text : Messages("balanceSheets.yearlyReadsets")
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
						min: 0,
						title : {
							text : Messages("balanceSheets.nbReadsets")
						},
						tickInterval : 2,
					},
					tooltip: {
						headerFormat: '<b>{point.x}</b><br/>',
						pointFormat: '{series.name}: {point.y}<br/>Total: {point.stackTotal}'
					},
					plotOptions: {
						column: {
							stacking: 'normal',
							dataLabels: {
								enabled: true
							}
						}
					},
					exporting : {
						enabled : true,
						filename : Messages('balanceSheets.export.general.nbReadSets') + "_" + $filter('date')(new Date(), 'yyyyMMdd_HHmmss'),
						chartOptions: {
							title : {
								text : Messages("balanceSheets.yearlyReadsets") + " (" + type + ")"
							}
						},
						sourceWidth : 1200
					},
					series : statData
				};
			},
			
			computeChartQuarters : function(data, type, year){
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
						 filename : Messages('balanceSheets.export.quarters') + "_" + year + "_" + $filter('date')(new Date(), 'yyyyMMdd_HHmmss'),
						 chartOptions: {
							title : {
								text : Messages("balanceSheets.quarterBases") + " (" + type + "/" + year + ")"
							}
						},
						 sourceWidth : 1200
					 },
					 series : allSeries,
					 plotOptions : {column:{grouping:false}}
				 };
				 return chartQuarters;
			 },
			 
			 computeChartSequencing : function(data, type, year){
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
							filename : Messages('balanceSheets.export.sequencingType') + "_" + year + "_" + $filter('date')(new Date(), 'yyyyMMdd_HHmmss'),
							chartOptions: {
								title : {
									text : Messages("balanceSheets.nbSequencingType") + " (" + type + "/" + year + ")"
								}
							},
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
			 
			 computeChartProject : function(data, sum, type, year){
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
							 filename : Messages('balanceSheets.export.firstTen') + "_" + year + "_" + $filter('date')(new Date(), 'yyyyMMdd_HHmmss'),
							 chartOptions: {
								title : {
									text : Messages("balanceSheets.tab.firstTen") + " (" + type + "/" + year + ")"
								}
							},
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
			 
			 computeChartSample : function(data, sum, type, year){
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
							 filename : Messages('balanceSheets.export.projectType') + "_" + year + "_" + $filter('date')(new Date(), 'yyyyMMdd_HHmmss'),
							 chartOptions: {
								title : {
									text : Messages("balanceSheets.tab.projectType") + " (" + type + "/" + year + ")"
								}
							},
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

			/**
			 * Download an xlsx representation of current balancesheet tables.
		     * @param {*} title 
			 * @param {*} outputFile 
			 * @param {*} sheets 
			 */
			computeExcelExport: function(title, output, sheets){
				$http.post(jsRoutes.controllers.balancesheets.api.BalanceSheets.excelReport().url, {sheets: sheets, title:title}, {responseType: "blob"})
				.success(function(data){
					var blob = new Blob([data], {type: "text/plain;charset=utf-8"});    					
					saveAs(blob, output);
				});
			},

			/**
			 * Write title on top of current page in the pdf.
			 * @param {*} pdf 
			 * @param {*} title 
			 */
			setPdfTitlePage: function(pdf, title) {
				pdf.setFontSize(15);
				pdf.text(title, pdf.internal.pageSize.getWidth()/2, 10, { align: "center" });
			},

			/**
			 * Include balancesheet's tables in the pdf.
			 * @param {*} pdf 
			 * @param {*} doc 
			 * @param {*} yOffset 
			 * @param {*} title 
			 */
			computePdfTables: function(pdf, doc, yOffset, title) {
				var y = yOffset;
				// width to apply when too many columns
				var smallWidth = (pdf.internal.pageSize.getWidth() - 10) / 8;
				var columnStyles = {
					// reduce width of last columns
					3: {cellWidth: smallWidth},
					4: {cellWidth: smallWidth}
				};
				var columnMap = {
					"Pourcentage sur dix projets": "% sur dix projets",
					"Pourcentage sur cette année": "%  sur cette année"
				};
				Array.from(doc.getElementsByTagName("table"))
				// convert table
				.map(function(table) { return pdf.autoTableHtmlToJson(table); })
				// add table
				.forEach(function(table) {
					// map column names
					var columns = table.columns.map(function(col) {
						if(Object.keys(columnMap).includes(col.content)) {
							col.content = columnMap[col.content];
						} return col;
					});
					// compute autoTable
					pdf.autoTable(columns, table.data, {
						margin: {top: y}, 
						columnStyles: columnStyles,
						// use hook to obtain printed table height
						didDrawPage: function(HookData) {y += HookData.table.height}
					});
				});
				return y + 12;
			},

			/**
			 * Include balancesheet's charts in the pdf.
			 * @param {*} pdf 
			 * @param {*} doc 
			 * @param {*} yOffset 
			 * @param {*} title 
			 */
			computePdfCharts: function(pdf, doc, yOffset, title) {
				var y = yOffset;
				var imgWidth = pdf.internal.pageSize.getWidth() - 10;
				var that = this;
				Array.from(doc.getElementsByTagName("svg"))
				// clone node to avoid side effects
				.map(function(svg) { return svg.cloneNode(true); })
				.forEach(function(svg) {
					var width = svg.width.baseVal.value;
					var height = svg.height.baseVal.value;
					var xOffset = 5;
					// if pieChart under ratio, it needs resize
					var minRatio = 0.4;
					if(height === 400 && height / width < minRatio) {
						var correctWidth = height / minRatio;
						xOffset -= (((imgWidth/correctWidth) * width) - imgWidth) / 2;
						width = correctWidth;
					}
					// calcul image scale for printing
					var scale = imgWidth / width;
					var imgHeight = scale * height;
					// if need page break
					if((pdf.internal.pageSize.getHeight() - y) < imgHeight){
						pdf.addPage();
						that.setPdfTitlePage(pdf, title);
						y = 15;
					}
					// add svg to pdf
					svg2pdf(svg, pdf, {
						xOffset: xOffset,
						yOffset: y,
						scale: scale
					});
					y += imgHeight;
				});
				return y;
			},

			/**
			 * Write balancesheet's alerts messages in the pdf.
			 * @param {*} pdf 
			 * @param {*} doc 
			 * @param {*} yOffset 
			 * @param {*} title 
			 */
			computePdfAlerts: function(pdf, doc, yOffset, title){
				pdf.setFontSize(10);
				var nbCharPerLine = 95;
				var linePadding = 5;
				var y = yOffset + linePadding;
				Array.from(doc.getElementsByClassName("alert alert-info")).forEach(function(alert) {
					// init values
					var txt = alert.textContent;
					var lineStart = 0;
					var lineEnd = nbCharPerLine;
					while(txt.length > lineEnd) {
						// find space to break on the line
						var tail = txt.lastIndexOf(' ', lineEnd);
						if(tail > -1) {
							lineEnd = tail + 1;
						}
						// cut line
						var line = txt.substring(lineStart, lineEnd);
						// write line
						pdf.text(15, y, line);
						// set values to process remaining text
						y += linePadding;
						lineStart = lineEnd;
						lineEnd += nbCharPerLine;
					}
					// process remaining text
					if(txt.length <= lineEnd && txt.lastIndexOf(' ') > 0) {
						var line = txt.substring(lineStart, lineEnd);
						pdf.text(15, y, line);
						y += linePadding;
					}
					y += linePadding;
				});
				return y;
			},

			/**
			 * Create a single pdf page (represents a balancesheet's section).
			 * @param {*} pdf 
			 * @param {*} section 
			 * @param {*} titlePage 
			 */
			computePdfPage: function(pdf, section, titlePage){
				var y = 15;
				var doc = section ? document.getElementById(section) : document.getElementsByTagName("BODY")[0];
				this.setPdfTitlePage(pdf, titlePage);	
				y = this.computePdfTables(pdf, doc, y, titlePage);
				y = this.computePdfCharts(pdf, doc, y, titlePage);
				y = this.computePdfAlerts(pdf, doc, y, titlePage);
			},

			/**
			 * Create a pdf representation of current balancesheet and save on desktop.
			 * @param {*} outputFile 
			 * @param {*} pages 
			 */
			computePdfExport: function(outputFile, pages) {
				var pdf = new jsPDF();
				var page = pages[0];
				this.computePdfPage(pdf, page.id, page.name);
				for(var i=1; i<pages.length; i++){
					page = pages[i];
					pdf.addPage();
					this.computePdfPage(pdf, page.id, page.name);
				}
				pdf.save(outputFile);
			},
			
			convertToDate : function(dateInMilliSeconds){
				 return new Date(dateInMilliSeconds);
			},
					
			 getQuarter : function(month){
				 return parseInt(Math.ceil(month/3));
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