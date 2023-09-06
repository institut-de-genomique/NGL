 "use strict";
 
 angular.module('ngl-sq.experimentsServices', []).
	factory('experimentsSearchService', ['$http', 'mainService', 'lists', 'datatable', 
		                         function($http,   mainService,   lists,   datatable){
		var getColumns = function(){
			var columns = [];
			
			columns.push({
			            	 "header":Messages("experiments.table.typeCode"),
			            	 "property":"typeCode",
			            	 "filter":"codes:'type'",
			            	 "order":true,
			            	 "hide":true,
			            	 "position":1,
			            	 "type":"text"
			            	 
			});
			columns.push({
							"header":Messages("experiments.table.code"),
							"property":"code",
							"order":true,
							"hide":false,
							"position":2,
							"type":"text",
							"groupMethod":"count:true"
			});
			columns.push({
							"header":Messages("experiments.instrument"),
							"property":"instrument.code",
							"order":true,
							"hide":true,
							"position":3,
							"type":"text",
							"filter":"codes:'instrument'",
							"groupMethod":"collect:true"
			});
			columns.push({
							"header":Messages("experiments.table.projectCodes"),
							"property":"projectCodes",
							"order":false,
							"render":"<div list-resize='cellValue' list-resize-min-size='3'>",
							"hide":true,
							"position":7,
							"type":"text",
							"filter":"unique",
							"groupMethod":"collect:true"
			});
			columns.push({
							"header":Messages("containers.table.sampleCodes"),
							"property":"sampleCodes",
							"order":false,
							"hide":true,
							"groupMethod":"collect:true",
							"position":9,
							"type":"text",
							"render":"<div list-resize='cellValue | unique' list-resize-min-size='3'>"					
			});
			columns.push({
							"header":Messages("experiments.table.creationDate"),
							"property":"traceInformation.creationDate",
							"order":true,
							"hide":true,
							"position":11,
							"type":"date"
			});
			columns.push({
							"header":Messages("experiments.table.createUser"),
							"property":"traceInformation.createUser",
							"order":true,
							"hide":true,
							"position":12,
							"type":"text"
			});
			
			if(mainService.getHomePage() === 'reagents'){
				
				columns.push({
		        	 "header":Messages("reagents.table.kitname"),
		        	 "property":"reagents[0].kitCatalogCode",
		        	 "filter":"codes:'reagentKit'",
		        	 "order":true,
		        	 "type":"text",
		        	 "position":13

		        });
				columns.push({
		        	 "header":Messages("reagents.table.boxname"),
		        	 "property":"reagents[0].boxCatalogCode",
		        	 "filter":"codes:'reagentBox'",
		        	 
		        	 "order":false,
		        	 "type":"text",
		        	 "position":14
				});
				columns.push({
		        	 "header":Messages("reagents.table.boxcode"),
		        	 "property":"reagents[0].boxCode",
		        	 "order":true,
		        	 "type":"text",
		        	 "position":15			
				});
				columns.push({
		        	 "header":Messages("reagents.table.reagentname"),
		        	 "property":"reagents[0].reagentCatalogCode",
		        	 "filter":"codes:'reagentReagent'",
		        	 "order":false,
		        	 "type":"text",
		        	 "position":16	
				});
				columns.push({
		        	 "header":Messages("reagents.table.reagentcode"),
		        	 "property":"reagents[0].code",
		        	 "order":true,
		        	 "type":"text",
		        	 "position":17			
				});
				columns.push({
		        	 "header":Messages("reagents.table.description"),
		        	 "property":"reagents[0].description",
		        	 "order":true,
		        	 "type":"text",
		        	 "position":18			
				});				
				
			}else{	// getHomePage() = "new" || "search"				
				
				columns.push({
					"header":Messages("experiments.table.categoryCode"),
					"property":"categoryCode",
					"order":true,
					"hide":true,
					"position":4,
					"type":"text",
					"filter":"codes:'experiment_cat'",
					"groupMethod":"unique"
				});
				columns.push({
					"header":Messages("experiments.table.state.code"),
					"property":"state.code",
					"order":true,
					"type":"text",
					"position":5,
					"hide":true,
					"filter":"codes:'state'",
					"groupMethod":"collect:true"
				});
				/*columns.push({
					"header":Messages("experiments.table.status"),
					"property":"status.valid",
					"render":"<div bt-select ng-model='value.data.status.valid' bt-options='valid.code as valid.name for valid in searchService.lists.get(\"status\")'  ng-edit='false'></div>",
					"order":false,
					"hide":true,
					"position":5.5,
					"type":"text",
					"groupMethod":"collect"
				});*/
				columns.push({
					"header":Messages("experiments.table.status"),
					"property":"status.valid",
					"filter":"codes:'status'",
					"order":false,
					"hide":true,
					"edit":true,
					"position":5.5,
					"type":"text",
					"choiceInList":true,
				    "listStyle":"bt-select-multiple",
					 "possibleValues":"searchService.lists.get(\"status\")",
					"groupMethod":"collect:true"
				});
				/*columns.push({
					"header":Messages("experiments.table.resolutionCodes"),
					"property":"state.resolutionCodes",
					"render":"<div bt-select ng-model='value.data.state.resolutionCodes' bt-options='valid.code as valid.name for valid in searchService.lists.getResolutions()'  ng-edit=\"false\"></div>",
					"order":false,
					"hide":true,
					"position":6,
					"type":"text",
					"groupMethod":"collect:true"
				});*/
				columns.push({
					"header":Messages("experiments.table.resolutionCodes"),
					"property":"state.resolutionCodes",
					"filter":"codes:'resolution'",
					"order":false,
					"hide":true,
					"edit" : true,
					"position":6,
					"type":"text",
					"choiceInList":true,
				    "listStyle":"bt-select-multiple",
					 "possibleValues":"searchService.lists.getResolutions()",
					"groupMethod":"collect:true"
				});
				columns.push({
					"header":Messages("containers.table.sampleCodes.length"),
					"property":"sampleCodes.length",
					"order":true,
					"hide":true,
					"position":8,
					"type":"text"					
				});
				columns.push({
					"header":Messages("containers.table.tags"),
					"property":"atomicTransfertMethods",
					"order":true,
					"hide":true,
					"render":"<div list-resize='cellValue' list-resize-min-size='3'>",
					"filter":"flatArray:\"inputContainerUseds\" | flatArray:\"contents\" | getArray:\"properties.tag.value\" | unique",
					"position":10,
					"type":"text"				
				});
				
			}
		         
			return columns;
		};
		
		
		var isInit = false;
		
		
		var initListService = function(){
			if(!isInit){
				lists.refresh.types({objectTypeCode:"Process"}, true);
				lists.refresh.processCategories();
				lists.refresh.experimentCategories();
				lists.refresh.projects();
				lists.refresh.users();
				lists.refresh.containerSupports();
				lists.refresh.states({objectTypeCode:"Experiment"});
				lists.refresh.experimentTypes({categoryCode:"purification"}, "purifications");
				lists.refresh.experimentTypes({categoryCode:"qualitycontrol"}, "qualitycontrols");
				lists.refresh.experimentTypes({categoryCode:"transfert"}, "transferts");
				lists.refresh.experimentTypes({categoryCode:"transformation"}, "transformations");
				lists.refresh.experimentTypes({categoryCode:"transformation"}, "fromTransformations");
				lists.refresh.reportConfigs({pageCodes:["experiments-addcolumns"]}, "experiments-addcolumns");
				lists.refresh.filterConfigs({pageCodes:["experiments-search-addfilters"]}, "experiments-search-addfilters");
				lists.refresh.protocols({}, 'all-protocols');
				//lists.refresh.instruments();
				lists.refresh.resolutions({objectTypeCode:"Experiment",distinct:true});
				isInit=true;
			}
		};
		
		var searchService = {
				getColumns:getColumns,
				getDefaultColumns:getColumns,
				datatable:undefined,
				isRouteParam:false,
				lists : lists,
				additionalFilters:[],
				additionalColumns:[],
				selectedAddColumns:[],
				setRouteParams:function($routeParams){
					var count = 0;
					for(var p in $routeParams){
						count++;
						break;
					}
					if(count > 0){
						this.isRouteParam = true;
						this.form = $routeParams;
					}
				},
				
				updateForm : function(){
					this.form.includes = [];
					if(this.reportingConfiguration){
						for(var i = 0 ; i < this.reportingConfiguration.columns.length ; i++){
							if(this.reportingConfiguration.columns[i].queryIncludeKeys && this.reportingConfiguration.columns[i].queryIncludeKeys.length > 0){
								this.form.includes = this.form.includes.concat(this.reportingConfiguration.columns[i].queryIncludeKeys);
							}else{
								this.form.includes.push(this.reportingConfiguration.columns[i].property.replace('.value','').replace(".unit", ''));
							}
						}
					}else{
						this.form.includes = ["default"];
					}
					
					
					//this.form.includes = ["default"];
					for(var i = 0 ; i < this.selectedAddColumns.length ; i++){
						//remove .value if present to manage correctly properties (single, list, etc.)
						if(this.selectedAddColumns[i].queryIncludeKeys && this.selectedAddColumns[i].queryIncludeKeys.length > 0){
							this.form.includes = this.form.includes.concat(this.selectedAddColumns[i].queryIncludeKeys);
						}else{
							this.form.includes.push(this.selectedAddColumns[i].property.replace('.value','').replace(".unit", ''));
						}
						
					}
				},
				convertForm : function(){
					var _form = angular.copy(this.form);
					if(_form.fromDate)_form.fromDate = moment(_form.fromDate, Messages("date.format").toUpperCase()).valueOf();
					if(_form.toDate)_form.toDate = moment(_form.toDate, Messages("date.format").toUpperCase()).valueOf();		
					return _form
				},
				
				resetForm : function(){					
					this.form = {};					
				},

				resetTextareas : function(){
					Array.from(document.getElementsByTagName('textarea')).forEach(function(element) {
						var elementScope = angular.element(element).scope();
						if(elementScope.textareaValue){
							elementScope.textareaValue = null;
						}
					});
				},
				
				resetSampleCodes : function(){
					this.form.sampleCodes = [];									
				},
				
				resetSampleCodes : function(){
					this.form.sampleCodes = [];									
				},
				
				search : function(){
					this.updateForm();
					mainService.setForm(this.form);				
					this.datatable.search(this.convertForm());
					
				},
				initAdditionalColumns : function(){
					this.additionalColumns=[];
					this.selectedAddColumns=[];
					
					if(lists.get("experiments-addcolumns") && lists.get("experiments-addcolumns").length === 1){
						var formColumns = [];
						var allColumns = angular.copy(lists.get("experiments-addcolumns")[0].columns);
						var nbElementByColumn = Math.ceil(allColumns.length / 5); //5 columns
						for(var i = 0; i  < 5 && allColumns.length > 0 ; i++){
							formColumns.push(allColumns.splice(0, nbElementByColumn));	    								
						}
						//complete to 5 five element to have a great design 
						while(formColumns.length < 5){
							formColumns.push([]);
						}
						this.additionalColumns = formColumns;
					}
				},
				refreshSamples : function(){
					if(this.form.projectCodes && this.form.projectCodes.length > 0){
						this.lists.refresh.samples({projectCodes:this.form.projectCodes});
					}
				},
				changeTypeCode : function(){
					//this.search();					
				},
				useMoment: function(date, format){
					//ex: 2014-10-02
					var patt = /[0-9]{4}-[0-9]{2}-[0-9]{2}/;
					
					//chrome browser always return input with type=date value as AAAA-MM-DD
					if(date.search(patt) != -1){
						return moment(date).valueOf();
					}
					//fifox browser return the specified format
					return moment(date, Messages("date.format").toUpperCase()).valueOf();
				},
				changeExperimentType : function(){											
					lists.refresh.instruments({"experimentTypes":this.form.typeCode}, "instruments-search-list");
					this.form.instruments = undefined;
				},
				
				changeProcessCategory : function(){
					//this.form.experimentType = undefined;
					//this.form.experimentCategory = undefined;
					this.form.processTypeCode = undefined;
					if(this.form.processCategory){
						lists.refresh.processTypes({categoryCode:this.form.processCategory});
					}
					
				},
				
				changeProcessType : function(){
					this.form.experimentType = undefined;
					this.form.experimentCategory = undefined;
				},
				
				changeExperimentCategory : function(){
					this.form.experimentType = undefined;
					if(this.form.processType && this.form.experimentCategory){
						lists.refresh.experimentTypes({categoryCode:this.form.experimentCategory, processTypeCode:this.form.processType});
					}else if(this.form.experimentCategory){
						lists.refresh.experimentTypes({categoryCode:this.form.experimentCategory});
					}
				},
				changeContainerSupportCode: function(val){
					return $http.get(jsRoutes.controllers.containers.api.ContainerSupports.list().url,{params:{"codeRegex":val}}).success(function(data, status, headers, config) {						
						return [data];				
	    			});
					
				},
				getAddColumnsToForm : function(){
					if(this.additionalColumns.length === 0){
						this.initAdditionalColumns();
					}
					return this.additionalColumns;									
				},
				addColumnsToDatatable:function(){
					//this.reportingConfiguration = undefined;
					//this.reportingConfigurationCode = undefined;
					
					this.selectedAddColumns = [];
					for(var i = 0 ; i < this.additionalColumns.length ; i++){
						for(var j = 0; j < this.additionalColumns[i].length; j++){
							if(this.additionalColumns[i][j].select){
								this.selectedAddColumns.push(this.additionalColumns[i][j]);
							}
						}
					}
					if(this.reportingConfigurationCode){
						this.datatable.setColumnsConfig(this.reportingConfiguration.columns.concat(this.selectedAddColumns));
					}else{
						this.datatable.setColumnsConfig(this.getDefaultColumns().concat(this.selectedAddColumns));						
					}
					this.search();
				},	
				resetDatatableColumns:function(){
					this.initAdditionalColumns();
					this.datatable.setColumnsConfig(this.getDefaultColumns());
					this.search();
				},
				/**
				 * Update column when change reportingConfiguration
				 */
				updateColumn : function(){
					this.initAdditionalColumns();
					if(this.reportingConfigurationCode){
						$http.get(jsRoutes.controllers.reporting.api.ReportingConfigurations.get(this.reportingConfigurationCode).url,{searchService:this, datatable:this.datatable})
								.success(function(data, status, headers, config) {
									config.searchService.reportingConfiguration = data;
									config.searchService.search();
									config.datatable.setColumnsConfig(data.columns);																								
						});
					}else{
						this.reportingConfiguration = undefined;
						this.datatable.setColumnsConfig(this.getDefaultColumns());
						this.search();
					}
					
				},
				initAdditionalFilters:function(){
					this.additionalFilters=[];
					
					if(lists.get("experiments-search-addfilters") && lists.get("experiments-search-addfilters").length === 1){
						var formFilters = [];
						var allFilters = angular.copy(lists.get("experiments-search-addfilters")[0].filters);
						
						/* add static filters here*/
						allFilters.push({property:"protocolCode",html:"<div bt-select multiple=true filter=true placeholder='"+Messages("experiments.select.protocols")+"' class='form-control' ng-model='searchService.form.protocolCodes' bt-options='protocol.code as protocol.name for protocol in searchService.lists.get(\"all-protocols\")'></div>",position:allFilters.length+1});
						var nbElementByColumn = Math.ceil(allFilters.length / 5); //5 columns
						for(var i = 0; i  < 5 && allFilters.length > 0 ; i++){
							formFilters.push(allFilters.splice(0, nbElementByColumn));	    								
						}
						//complete to 5 five element to have a great design 
						while(formFilters.length < 5){
							formFilters.push([]);
						}
							
						this.additionalFilters = formFilters;
					}
				},
				
				getAddFiltersToForm : function(){
					if(this.additionalFilters.length === 0){
						this.initAdditionalFilters();
					}
					return this.additionalFilters;									
				},	
				
				/**
				 * initialise the service
				 */
				init : function($routeParams, datatableConfig){
					initListService();
					
					datatableConfig.messages = {
							transformKey: function(key, args) {
		                        return Messages(key, args);
		                    }
					};
					
					//to avoid to lost the previous search
					if(datatableConfig && angular.isUndefined(mainService.getDatatable())){
						searchService.datatable = datatable(datatableConfig);
						mainService.setDatatable(searchService.datatable);
						searchService.datatable.setColumnsConfig(getColumns());		
					}else if(angular.isDefined(mainService.getDatatable())){
						searchService.datatable = mainService.getDatatable();			
					}	
					
					
					if(angular.isDefined(mainService.getForm())){
						searchService.form = mainService.getForm();
					}else{
						searchService.resetForm();						
					}
					
					if(angular.isDefined($routeParams)){
						this.setRouteParams($routeParams);
					}					
					
					
				}
		};
		
		return searchService;				
	}
])// nouveau nom tagService (code déplacé depuis cng/tag-plate-helpers.js)
.factory('tagService', ['$parse', '$filter','$http',
                   function($parse, $filter,  $http){
	
		var factory = {
			
			setTags:function(plate, plateColumn, atmService, messages, auto){
				/* 04/08/2020 nouvelle version
				* Fonction qui dispose les index contenus dans une plaque => plaque output a indexer
				* faite pour fonctionner avec des robots qui travaillenr en mode colonne (colonne plaque index=>colonne plaque output)
				* il est possible de dire qu'on commense a utiliser la plaque d'index en colonne N et pas la 1ere
				* 07/05/2020 ajout un parametre optionnel "auto" : dans les cas ou l'utilisateur n'a pas le choix de la colonne de départ 
				* (exemple: pcr-indexing-and-purification-qmp-seq_ctrl.js=> colonne 1 est imposée) 
				* les warnings concernant cette colonne ne sont pas pertinents => ne pas les afficher
				* NGL-2944 11/05/2020 version sans le unset des tags qui est déporté dans une fonction dédiée  unsetTags
				* 
				* SUPSQCNG-866: dans le cas de l'administrateur il faut pouvoir écraser un index déjà présent
				*/
			
				console.log("...SETTING INDEX..."); 
					
				var showColWarnings=true;
				if (auto != undefined){ showColWarnings=false;}
					
				console.log(">>selected plate :"+ plate.name + "; start column: "+plateColumn.name+ ";start position :"+ plateColumn.position  );	
				messages.clear();
					
				// attention à certaines colonnes de départ
				// le controle doit porter sur la valeur maximale de colonne trouvee sur la plaque a indexer
				// =>dernier puit si on a trié  dans l'ordre "colonne d'abord"
				var dataMain = atmService.data.getData();
				// trier dans l'ordre "colonne d'abord"
				var dataMain = $filter('orderBy')(dataMain, ['atomicTransfertMethod.column*1','atomicTransfertMethod.line']); 
				var last=dataMain.slice(-1)[0];
				var lastInputCol=last.atomicTransfertMethod.column*1;
				console.log("last col in input plate="+ lastInputCol);
						
				var lastTagCol=plate.tags.length / 8;  // ce sont des colonnes de 8
				console.log("last col in tag plate="+ lastTagCol);
						
				// Même en prennant tous les index, il n'y en a pas assez pour indexer tout les puits de la plaque!! 
				// (NB il existe des plaques de 48 index seulement)
				// 17/07/2018 l'utilisateur à la possibilité de compléter manuellement => warning et pas danger
				// 07/05/2020 ajout showColWarnings
				if ( (lastTagCol < lastInputCol) && showColWarnings ){
					messages.clazz="alert alert-warning";
					messages.text='Remarque: '+ Messages('select.msg.error.notEnoughTags.tagPlate',plate.name);
					messages.showDetails = false;
					messages.open();
						
					//return;     NON, NE PAS BLOQUER
				}
					
				// la liste des colonnes proposée est fixe et ne dépend pas du nbre de colonnes dans les plaque d'index...
				// choisir la colonne 7 pour une plaque qui ne continent que 48 index (max=colonne 6) est une erreur !
				// 07/05/2020 cette erreur doit etre tracée meme si le choix n'est pas due à l'utilisateur
				if ( plateColumn.name*1 > lastTagCol ){	
					messages.clazz="alert alert-danger";
					messages.text=Messages('select.msg.error.emptyStartColumn.tagPlate', plateColumn.name, plate.name );
					messages.showDetails = false;
					messages.open();
					
					return;
				}
					
				// la colonne choisie ne permet pas a tous les puits de la plaque input de recevoir un index
				// 17/07/2018 l'utilisateur à la possibilité de compléter manuellement => warning et pas danger
				// 07/05/2020 ajout showColWarnings
				if ( ((lastTagCol - plateColumn.name*1  +1) < lastInputCol ) && showColWarnings ) { 
					messages.clazz="alert alert-warning";
					messages.text='Remarque: '+ Messages('select.msg.error.wrongStartColumn.tagPlate', plateColumn.name);
					messages.showDetails = false;
					messages.open();
						
					//return;      NON, NE PAS BLOQUER, 
				}
					
				// utiliser displayResult au lieu de dataMain
				var wells = atmService.data.displayResult;
				angular.forEach(wells, function(well){
					var ocu = well.data.outputContainerUsed;
					// 02/04/2020 ne pas écraser un index déjà présent dans le puit!!!!
					/* => 18/08/2020 NGL-2972 JG demande que l'écrasement soit toujours fait !!!!! mise en commentaire du code ajouté 02/04/2020
					var curentTagValue = $parse("experimentProperties.tag.value")(ocu);
					console.log("current tag="+ curentTagValue);
					// SUPSQCNG-866: 04/08/2020 l'administrateur doit pouvoir corriger des erreurs sur une experience terminée => autoriser l'écrasement!!! 
					if ( curentTagValue == undefined || curentTagValue == null || Permissions.check("admin")) {
					*/
					// 02/04/2020 si cette fonction est appellee alors que la position du puit n'est pas encore définie!!! on ne peut rien calculer
					if ( ocu.locationOnContainerSupport.line && ocu.locationOnContainerSupport.column ){
						//calculer la position sur la plaque:   pos= (col -1)*8 + line      (line est le code ascii - 65)
						var libPos=(ocu.locationOnContainerSupport.column  -1 )*8 + (ocu.locationOnContainerSupport.line.charCodeAt(0) -65);
						var indexPos= libPos + plateColumn.position;
						// Si la position calculee ne correspond pas a celle d'une plaque d'index (plaque d'index ne couvre pas toute la plaque de sortie)
						// => ne rien faire
						if ( plate.tags[indexPos]) {
							console.log("==> setting index "+indexPos+ ": "+ plate.tags[indexPos] +" in well "+ocu.locationOnContainerSupport.line+ ocu.locationOnContainerSupport.column);
							$parse("experimentProperties.tag.value").assign(ocu, plate.tags[indexPos]);
							$parse("experimentProperties.tagCategory.value").assign(ocu, plate.tagCategory);
						}
					}
					//} 
				});
			},
			unsetTags:function(plate, plateColumn, atmService, messages){
			/* NGL-2944 11/05/2020 trop tordu de faire le unset dans la fonction setTags !!!=> création nouvelle fonction dédiée
			*            => permet aussi de faire unset plus propre avec valeurs de plate et plateColumn mémorisées
			*            => !! reprendre toutes les expériences qui faisait appel a setTags
			*            18/08/2020 avec l'écrasement des index meme pour un utilisateur normal, le unset propre ne sert plus a rien !!!!
			*/
				
				console.log("...UNSETTING INDEX..."); 
					
				//utilisation de displayResult => besoin de setData(dataMain)...plus rapide ????
				var wells = atmService.data.displayResult;
				angular.forEach(wells, function(well){
					//  NOTE: les puits deja remplis et sautés lors de l'affectation n'ont pas été mémorisés => on les vide aussi !!!!
					var ocu = well.data.outputContainerUsed;
					if ( ocu.locationOnContainerSupport.line && ocu.locationOnContainerSupport.column ){
						//calculer la position sur la plaque:   pos= (col -1)*8 + line      (line est le code ascii - 65)
						var libPos=(ocu.locationOnContainerSupport.column  -1 )*8 + (ocu.locationOnContainerSupport.line.charCodeAt(0) -65);
						var indexPos= libPos + plateColumn.position;
						// Si la position calculee ne correspond pas a celle d'une plaque d'index (plaque d'index ne couvre pas toute la plaque de sortie)
						// =>ne rien faire
						if ( plate.tags[indexPos]) {
							// la position correspond bien a une plaque...
							// 12/05/2020 MAIS vérifer qu'il ne s'agit pas d'un index positionné manuellement
							/*  => 18/08/2020 nettoyer sans verifier !!! mise en commentaire du code ajouté 12/05/2020
							var curentTagValue = $parse("experimentProperties.tag.value")(ocu);
							if ( curentTagValue === plate.tags[indexPos]){
							*/
								$parse("experimentProperties.tag.value").assign(ocu,null);
								$parse("experimentProperties.tagCategory.value").assign(ocu,null);
							//}
						}
					}
				});
			},
			computeTagCategory:function(udtData){
			// affecter automatiquement la categorie de tag sur modifiction d'un tag
					
				var getter = $parse("outputContainerUsed.experimentProperties.tagCategory.value");
				var compute = {
						tagValue : $parse("outputContainerUsed.experimentProperties.tag.value")(udtData),
						// le filtrage au niveau du name des index est fait dans chaque experience =>   typeahead="v.code as v.name for v in getTags()
						tag : $filter("filter")(factory.allTags,{code:$parse("outputContainerUsed.experimentProperties.tag.value")(udtData)},true),
						isReady:function(){
							return (this.tagValue && this.tag && this.tag.length === 1);
						}
				};
					
				if(compute.isReady()){
					var tagCategory = undefined;
					var result = compute.tag[0].categoryCode;
					if(result){
						tagCategory = result;				
					}
					getter.assign(udtData, tagCategory);
				}else{
					getter.assign(udtData, undefined);
				}
			},
			// NGL-1350: recuperer les tags et les groupe des tags
			getAllTags: function(){	 
				return this.allTags;
			},
			getAllTagGroups: function(){
				return this.allGroupTags;
			},
			initTags: function(types, categories){
			// FDS 29/11/2018  ajouter un paramètre pour filtrer sur les types
			//      => appel:  initTags() ou initTags('index-illumina-sequencing') ou  initTags('index-nanopore-sequencing')
			// FDS 08/01/2021  ajouter un paramètre pour filtrer en plus sur la catégorie si nécessaire 
			//                 ( peut on avoir besoin de ne filtrer QUE sur category ???)
				
				factory.allGroupTags=[{'name':'---', 'value':undefined}]; //initialisé ici pour faire patienter pendant l'execution de la promise
				var filters={};
				// if(angular.isUndefined(types)){          pourquoi cette écriture ??
				if ( types === undefined ) {
					filters = {typeCodes:['index-illumina-sequencing','index-nanopore-sequencing', 'index-mgi-sequencing','index-pacbio-sequencing']};
				} else {
					if ( categories !== undefined) {
						console.log('initTags: types ET categories '+ types + '/'+ categories);
						filters= {typeCodes: types, categoryCodes: categories};
					} else {
						console.log('initTags: types '+ types );
						filters= {typeCodes: types};
					}
				}
					
				$http.get(jsRoutes.controllers.commons.api.Parameters.list().url,{params: filters })
					.success(function(data, status, headers, config) {
						console.log('index récupérés depuis Mongo...')
						// attention certains tags appartiennent a plusieurs groupes, faire une Map pour obtenir une liste sans doublons
						var groupsMap = new Map();
						data.forEach (function(tag){
							if ( tag.groupNames != null) { 	
								tag.groupNames.forEach (function(group){
									groupsMap.set(group,"group");
								});
							}
						});
							
						var grps=Array.from(groupsMap.keys()); // convertir la map en tableau
						// creer un tableau d'objet pour les groupes car il y a un cas spécial: il faut un element 'undefined' pour pouvoir saisir 
						// des tags qui n'ont aucun groupName defini...ou pour lesquel l'utilisateur ne le connait pas...
						grps.forEach( function (grp){
							factory.allGroupTags.push ({'name':grp,'value':grp});
						});
							
						// trier le tableau d'objets avec orderBy angular
						factory.allGroupTags = $filter('orderBy')(factory.allGroupTags,'name');
						// 04/10/2018 trier aussi les tags
						factory.allTags=$filter('orderBy')(data,'name');
				});
			}
		}
		return factory;
	}
]);




;