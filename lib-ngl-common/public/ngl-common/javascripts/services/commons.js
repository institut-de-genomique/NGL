"use strict";

angular.module('commonsServices', []).
    	factory('messages', function(){
		var constructor = function($scope, iConfig){
			var messages = {
					
					configDefault : {
						errorClass:'alert alert-danger',
						successClass: 'alert alert-success',
						warningClass : 'alert alert-warning', // FDS 24/02/2022
						errorKey:{save:'msg.error.save',remove:'msg.error.remove', get:'msg.error.get',double:'msg.error.double',create:'msg.error.create'},
						successKey:{save:'msg.success.save',remove:'msg.success.remove', get:'msg.success.get'}
					},
					config:undefined,
					configMaster:undefined,
					
					clazz : undefined, 
					text : undefined, 
					showDetails : false, 
					isDetails : false, 
					details : [],
					opening : false,
					getConfig: function(){
						return this.config;
					},
					setConfig: function(config){
						var settings = $.extend(true, {}, this.configDefault, config);
						this.config = angular.copy(settings);
						this.configMaster = angular.copy(settings);
					},
					clear : function() {
						this.clazz = undefined;
						this.text = undefined; 
						this.showDetails = false; 
						this.isDetails = false;
						this.details = {};
						this.opening = false;
					},
					setDetails : function(details){
						this.isDetails = true;
						this.details = details;
					},
					addDetails : function(details){
						for(var pName in details){
							if(this.details[pName] === undefined || this.details[pName] === null){
								this.details[pName] = details[pName];
							}else{
								if(angular.isArray(details[pName])){
									this.details[pName] = this.details[pName].concat(details[pName]);
								}else{
									this.details[pName].push(details[pName]);
								}
							}
						}
						this.isDetails = true;
						
						
					},
					setSuccess : function(type){
						this.clazz=this.config.successClass;
						// FDS c'est pas une erreur => config.errorKey 
						if(this.config.errorKey[type]){
							this.text=this.transformKey(this.config.successKey[type]);
						}else{
							this.text=type;
						}	
						this.open();
					},
					setError : function(type){
						this.clazz=this.config.errorClass;
						if(this.config.errorKey[type]){
							this.text=this.transformKey(this.config.errorKey[type]);
						}else{
							this.text=type;
						}
						this.open();
					},
					// FDS 24/02/2022.
					setWarning : function(text){
						this.clazz=this.config.warningClass;
						this.text=text;
						this.open();
					},
					open : function(){
						this.opening = true;
					},
					close : function(){
						this.opening = false;
					},
					isOpen : function(){
						return this.opening;
					},
					transformKey : function(key, args){
						return Messages(key, args);
					}
			};
			messages.setConfig(iConfig)
			return messages;
		}
	return constructor;
	}).factory('lists', ['$http','$filter', function($http,$filter){   // FDS 29/11/2018 ajout $filter pour getTagGroupNames et getTacCategories
    		var inProgress = {};
    		var results = {
    				valuations : [{code:"TRUE",  name:Messages("valuation.TRUE")},
    				              {code:"FALSE", name:Messages("valuation.FALSE")},
    				              {code:"UNSET", name:Messages("valuation.UNSET")}],
    				status :     [{code:"TRUE",  name:Messages("status.TRUE")},
    				              {code:"FALSE", name:Messages("status.FALSE")},
    				              {code:"UNSET", name:Messages("status.UNSET")}],
    				booleans :   [{code:"true",  name:Messages("boolean.TRUE")}, 
    				              {code:"false", name:Messages("boolean.FALSE")}],
    				// FDS ne marche que ici !!!! valeurs en dur
    				tagTypes :   [{code:'index-illumina-sequencing', name:Messages("techno.index-illumina-sequencing")},
    				      		  {code:'index-nanopore-sequencing', name:Messages("techno.index-nanopore-sequencing")},
								  {code:'index-pacbio-sequencing', name:Messages("techno.index-pacbio-sequencing")},
								  {code:'index-mgi-sequencing', name:Messages("techno.index-mgi-sequencing"),}],
    				reagentCategories : [{code:'Box', name:Messages("reagent.category-box")}, {code:'Reagent', name:Messages("reagent.category-reagent")}]
    			};
    		
    		var refresh = {
    				resolutions : function(params, key){
    					load(jsRoutes.controllers.resolutions.api.Resolutions.list().url,params,(key)?key:'resolutions', true);
    				},
    				instruments : function(params, key){
    					load(jsRoutes.controllers.instruments.api.Instruments.list().url,params,(key)?key:'instruments');
    				},
    				instrumentCategories : function(params, key){
    					load(jsRoutes.controllers.instruments.api.InstrumentCategories.list().url,params,(key)?key:'instrumentCategories');
    				},
    				instrumentUsedTypes : function(params, key){
    					load(jsRoutes.controllers.instruments.api.InstrumentUsedTypes.list().url,params,(key)?key:'instrumentUsedTypes');
    				},
    				containerSupportCategories : function(params, key){
    					load(jsRoutes.controllers.containers.api.ContainerSupportCategories.list().url,params,(key)?key:'containerSupportCategories', true);
    				},
    				processes : function(params, key){
    					load(jsRoutes.controllers.processes.api.Processes.list().url,params,(key)?key:'processes');
    				},
    				processCategories : function(params, key){
    					load(jsRoutes.controllers.processes.api.ProcessCategories.list().url,params,(key)?key:'processCategories', true);
    				},
    				processTypes : function(params, key){
    					load(jsRoutes.controllers.processes.api.ProcessTypes.list().url,params,(key)?key:'processTypes', true);
    				},
    				kitCatalogs : function(params, key){
    					load(jsRoutes.controllers.reagents.api.KitCatalogs.list().url,params,(key)?key:'kitCatalogs');
    				},
    				boxCatalogs : function(params, key) {
    					load(jsRoutes.controllers.reagents.api.BoxCatalogs.list().url,params,(key)?key:'boxCatalogs');
    				},
    				reagentCatalogs : function(params, key) {
    					load(jsRoutes.controllers.reagents.api.ReagentCatalogs.list().url,params,(key)?key:'reagentCatalogs');
    				},
    				reagentReceptions : function(params, key) {
    					load(jsRoutes.controllers.reagents.api.Receptions.list().url,params,(key)?key:'reagentReceptions');
    				},
       				projectCategories : function(params, key){
    					load(jsRoutes.controllers.projects.api.ProjectCategories.list().url,params,(key)?key:'projectCategories');
    				},
    				projectTypes : function(params, key){
    					load(jsRoutes.controllers.projects.api.ProjectTypes.list().url,params,(key)?key:'projectTypes');
    				},
    				umbrellaProjects : function(params, key){
    					load(jsRoutes.controllers.projects.api.UmbrellaProjects.list().url,params,(key)?key:'umbrellaProjects');
    				},
    				bioinformaticParameters : function(params, key){
    					load(jsRoutes.controllers.projects.api.ProjectBioinformaticParameters.list().url,params,(key)?key:'bioinformaticParameters');
    				},
    				valuationCriterias: function(params, key){
    					load(jsRoutes.controllers.valuation.api.ValuationCriterias.list().url,params,(key)?key:'valuationCriterias');
    				},
    				containerSupports : function(params, key){
    					load(jsRoutes.controllers.containers.api.ContainerSupports.list().url,params,(key)?key:'containerSupports');
    				},
    				projects : function(params, key){
    					load(jsRoutes.controllers.projects.api.Projects.list().url,params,(key)?key:'projects');
    				},
    				samples : function(params, key){
    					if(params)params.limit=-1
    					else params = {limit:-1};
    					load(jsRoutes.controllers.samples.api.Samples.list().url,params,(key)?key:'samples');
					},
					importTypes : function(params, key){
						load(jsRoutes.controllers.samples.api.ImportTypes.list().url,params,(key)?key:'importTypes')
					},
    				users : function(params, key){
    					load(jsRoutes.controllers.commons.api.Users.list().url,params,(key)?key:'users');
    				},
    				roles : function(params, key){
    					load(jsRoutes.controllers.commons.api.Roles.list().url, params, (key)?key:'roles');
    				},
    				experiments : function(params, key){
    					load(jsRoutes.controllers.experiments.api.Experiments.list().url,params,(key)?key:'experiments');
    				},
    				states : function(params, key){
    					load(jsRoutes.controllers.commons.api.States.list().url,params,(key)?key:'states', true);
    				},
    				protocols : function(params, key){
    					load(jsRoutes.controllers.protocols.api.Protocols.list().url,params,(key)?key:'protocols');
    				},
    				types : function(params, multi, key){
    					var name = "types";
    					if(multi!=undefined){
    						name = params.objectTypeCode+'Types';
    					}
    					load(jsRoutes.controllers.commons.api.CommonInfoTypes.list().url,params,(key)?key:name);
    				},
    				containerCategories : function(params, key){
    					load(jsRoutes.controllers.containers.api.ContainerCategories.list().url,params,(key)?key:'containerCategories');
    				},
    				experimentCategories : function(params, key){
    					load(jsRoutes.controllers.experiments.api.ExperimentCategories.list().url,params,(key)?key:'experimentCategories');
    				},
    				experimentTypes : function(params, key){
    					load(jsRoutes.controllers.experiments.api.ExperimentTypes.list().url,params,(key)?key:'experimentTypes', true);
    				},   				
    				runs : function(params, key){
    					load(jsRoutes.controllers.runs.api.Runs.list().url,params,(key)?key:'runs');
    				},
    				runCategories : function(params, key){
    					load(jsRoutes.controllers.runs.api.RunCategories.list().url,params,(key)?key:'runCategories');
    				},
    				reportConfigs : function(params, key){
    					load(jsRoutes.controllers.reporting.api.ReportingConfigurations.list().url,params,(key)?key:'reportConfigs', true);
    				},
    				receptionConfigs : function(params, key){
    					load(jsRoutes.controllers.receptions.api.ReceptionConfigurations.list().url,params,(key)?key:'receptionConfigs', true);
    				},
    				filterConfigs : function(params, key){
    					load(jsRoutes.controllers.reporting.api.FilteringConfigurations.list().url,params,(key)?key:'filterConfigs');
    				},
    				statsConfigs : function(params, key){
    					load(jsRoutes.controllers.stats.api.StatsConfigurations.list().url, params, (key)?key:'statsConfigs', true);
    				},
    				propertyDefinitions : function(params, key){
    					load(jsRoutes.controllers.commons.api.PropertyDefinitions.list().url,params,(key)?key:'propertyDefinitions');
    				},
					propertiesForCreation : function(params, key){
						loadWithoutList(jsRoutes.controllers.commons.api.Parameters.list().url,params,(key)?key:'propertiesForCreation');
					},
    				treatmentTypes : function(params, key){
    					load(jsRoutes.controllers.treatmenttypes.api.TreatmentTypes.list().url,params,(key)?key:'treatmentTypes');
    				},
					// NGL-3530 ajout FDS
    				treatmentCategories : function(params, key){
    					load(jsRoutes.controllers.treatmenttypes.api.TreatmentCategories.list().url,params,(key)?key:'treatmentCategories');
    				},
    				values : function(params, key){
    					load(jsRoutes.controllers.commons.api.Values.list().url,params,(key)?key:'values');
    				},
    				tags : function(params, key){
    					if(angular.isUndefined(params)){
    	    				params = {typeCodes:['index-illumina-sequencing','index-nanopore-sequencing', 'index-mgi-sequencing','index-pacbio-sequencing']};
    	    			}
    					//GA 24/07/2015 un peu spécial pour tags car fait partie de la collection parameters...
    					load(jsRoutes.controllers.commons.api.Parameters.list().url,params,(key)?key:'tags');
    				},
    				// FDS 29/11/2018  ==> voir aussi tag-plate-helper.js
    				tagGroupNames: function(key){
    					var types = {typeCodes:['index-illumina-sequencing','index-nanopore-sequencing','index-mgi-sequencing', 'index-pacbio-sequencing']};
    					if(inProgress[key] === undefined){
    						inProgress[key] = true; //avoid multiple load in parallele
    						$http.get(jsRoutes.controllers.commons.api.Parameters.list().url,{params:types})
    							.success(function(data, status, headers, config) {
    							//  certains tags appartiennent a plusieurs groupes, faire une Map pour obtenir une liste sans doublons
    								var groupsMap = new Map();
    								data.forEach (function(tag){
    									if ( tag.groupNames != null) {
    										tag.groupNames.forEach (function(group){
    											groupsMap.set(group,"group");
    										});
    									}
    								});
    								
    								// convertir la map en tableau
    								var grps=Array.from(groupsMap.keys());
    								results[key]=[];
    								grps.forEach( function (grp){
    									results[key].push( {'name':grp} );
    								});
    								// trier le tableau
    								results[key] = $filter('orderBy')(results[key],'name');
    								inProgress[key] = undefined; // reset boolean
    							});
    					} 
    				},
    				// FDS 29/11/2018 sur le meme principe que tagGroupNames creation de tagCategories 
    				tagCategories: function(key){
    					var types = {typeCodes:['index-illumina-sequencing','index-nanopore-sequencing','index-mgi-sequencing','index-pacbio-sequencing']};
    					if(inProgress[key] === undefined){
    						inProgress[key] = true; //avoid multiple load in parallele
    						$http.get(jsRoutes.controllers.commons.api.Parameters.list().url,{params:types})
								.success(function(data, status, headers, config) {
									//console.log('index récupérés depuis Mongo...');
									var categoryMap = new Map();
									data.forEach (function(tag){
										categoryMap.set(tag.categoryCode,"code");
									});
									
									// convertir la map en tableau
									var cats=Array.from(categoryMap.keys());
									results[key]=[];
									cats.forEach( function (cat){
										results[key].push( {'code':cat} );
									});
									// trier le tableau
									results[key] = $filter('orderBy')(results[key],'code');
									inProgress[key] = undefined; // reset boolean
								});
    					}
    				},
    				sampleTypes : function(params, key){
    					if(angular.isUndefined(params)){
    	    				params = {};
    	    			}
    					params.objectTypeCode='Sample';
    					load(jsRoutes.controllers.commons.api.CommonInfoTypes.list().url,params,(key)?key:'sampleTypes', true);
    				},
    				sraStudies : function(params, key){
    					load(jsRoutes.controllers.sra.studies.api.Studies.list().url,params,(key)?key:'sraStudies');
    				},
    				sraProjects : function(params, key){
    					load(jsRoutes.controllers.sra.projects.api.Projects.list().url,params,(key)?key:'sraProjects');
    				},
    				sraSamples : function(params, key){
    					load(jsRoutes.controllers.sra.samples.api.Samples.list().url,params,(key)?key:'sraSamples');
    				},
    				sraExperiments : function(params, key){
    					load(jsRoutes.controllers.sra.experiments.api.Experiments.list().url,params,(key)?key:'sraExperiments');
    				},
    				sraConfigurations : function(params, key){
    					load(jsRoutes.controllers.sra.configurations.api.Configurations.list().url,params,(key)?key:'sraConfigurations');
    				},
    				sraAnalyzes : function(params, key){
    					load(jsRoutes.controllers.sra.analyzes.api.Analyzes.list().url,params,(key)?key:'sraAnalyzes');
    				},
    				readSets : function(params, key){
    					load(jsRoutes.controllers.readsets.api.ReadSets.list().url,params,(key)?key:'readSets');
    				},
					mapParameter : function(params, key){
						if(angular.isUndefined(params)){
    	    				params = {typeCode:'map-parameter'};
    	    			}
    					loadWithoutList(jsRoutes.controllers.commons.api.Parameters.list().url,params,(key)?key:'mapParameter',false);
					},
    				//Solution temporaire en attendant le passage de la description SQL dans Mongo
    				context : function(params,key){
    					if(angular.isUndefined(params)){
    	    				params = {typeCode:'context-description'};
    	    			}
    					load(jsRoutes.controllers.commons.api.Parameters.list().url,params,(key)?key:'context', true);
    				},
    				// FDS 22/11/2018  il en manque plein dans cette liste !!!!!!!! c'est appelé ou ?
    				all : function(params){
    					this.resolutions(params);
    					this.containerCategories(params);
    					this.processCategories(params);
    					this.valuationCriterias(params);
    					this.projects(params);
    					this.bioinformaticParameters(params);
    					this.samples(params);
    					this.states(params);
    					this.types(params);
    					this.users(params);
    					this.roles(params);
    					this.experiments(params);
    					this.experimentTypes(params);
						this.mapParameter(params);
    					this.runs(params);
    					this.runCategories(param);
    					this.protocols(params);
    					this.instruments(params);
    					this.sraStudies(params);
    					this.sraProjects(params);
    					this.sraConfigurations(params);
	    				this.sraAnalyzes(params);
    					this.readSets(params);
   				}
    		};
    		
    		function load(url, params, key, preventSorting){
				if (preventSorting == undefined || preventSorting == null) {
					preventSorting = false;
				}

    			if(inProgress[key] === undefined){
	    			inProgress[key] = true; //avoid multiple load in parallele
	    			if(angular.isUndefined(params)){
	    				params = {};
	    			}
					params.list = true;
	    			$http.get(url,{params:params,key:key}).success(function(data, status, headers, config) {
	    				results[config.key]= preventSorting ? data : data.sort(function(a, b){
							if(a.code < b.code) { return -1; }
							if(a.code > b.code) { return 1; }
							return 0;
						});    // pourquoi config ??
	    				inProgress[key] = undefined;
	    			});
    			}
    		};

			function loadWithoutList(url, params, key, preventSorting){
				if (preventSorting == undefined || preventSorting == null) {
					preventSorting = false;
				}

    			if(inProgress[key] === undefined){
	    			inProgress[key] = true; //avoid multiple load in parallele
	    			if(angular.isUndefined(params)){
	    				params = {};
	    			}
	    			$http.get(url,{params:params,key:key}).success(function(data, status, headers, config) {
	    				results[config.key]= preventSorting ? data : data.sort(function(a, b){
							if(a.code < b.code) { return -1; }
							if(a.code > b.code) { return 1; }
							return 0;
						});    // pourquoi config ??
	    				inProgress[key] = undefined;
	    			});
    			}
    		};
    		
    		function putNoneInResult (result){
    			if(result && result[0] && result[0].code !== "none"){
					result.unshift({name: "None", code: "none"});
				}
				return result;
    		};
    		
    		return {
    			refresh : refresh,
    			get : function(key, addNone){
    				if(addNone){
    					return putNoneInResult(results[key]);
    				}else{
    					return results[key];
    				}
    			},
    			clear : function(key){results[key] = null;},
    			getResolutions : function(){return results['resolutions'];},
    			getValuationCriterias :function(params,key){
    				key = (key)?key:'valuationCriterias';
    				if(results[key] === undefined){
    					refresh.valuationCriterias(params, key);
    				}
    				return results[key];
    			},
    			getProjects : function(params,key){
    				key = (key)?key:'projects';
    				if(results[key] === undefined){
    					refresh.projects(params, key);
    				}
    				return results[key];
    			},
    			getContainerSupportCategories : function(){return results['containerSupportCategories'];},
    			getProcesses : function(){return results['processes'];},
    			getProcessCategories : function(){return results['processCategories'];},
    			getProcessTypes : function(params,key){
					key = (key)?key:'processTypes';
    				if(results[key] === undefined){
    					refresh.processTypes(params, key);
    				}
					return results[key];
				},
    			getProjectCategories : function(){return results['projectCategories'];},
    			getProjectTypes : function(){return results['projectTypes'];},
    			getUmbrellaProjects : function(){return results['umbrellaProjects'];},
    			getBioinformaticParameters : function(){return results['bioinformaticParameters'];},
				getSamples : function(){return results['samples'];},
				getImportTypes : function(){return results['importTypes'];},
    			getUsers : function(){return results['users'];},
    			getRoles : function(){return results['roles'];},
    			getExperiments : function(){return results['experiments'];},
    			getContainerSupports : function(){return results['containerSupports'];},
    			getContainerCategories : function(){return results['containerCategories'];},
    			getExperimentCategories : function(){return results['experimentCategories'];},
    			getExperimentTypes : function(){return results['experimentTypes'];},
				getMapParameter : function(){return results['mapParameter'];},
    			getStates : function(){return results['states'];},
    			getRuns : function(){return results['runs'];},
    			getRunCategories : function(){return results['runCategories'];},
    			getInstrumentCategories : function(){return results['instrumentCategories'];},
    			getProtocols : function(){return results['protocols'];},
    			getTypes : function(params){
	    						if(params != undefined){
	    							return results[params+'Types'];
	    						}else{
	    							return results['types'];
	    						}
    					   },
			    getKitCatalogs : function(params){
    				if(results['kitCatalogs'] === undefined){
    					refresh.kitCatalogs(params);
    				}
    				return results['kitCatalogs'];
    			},
    			getBoxCatalogs : function(params,key){
    				key = (key)?key:'boxCatalogs';
					if (results[key] === undefined) {
    					refresh.boxCatalogs(params,key);
    				}
    				return results[key];
    			},
    			getReagentCatalogs : function(params,key){
    				key = (key)?key:'reagentCatalogs';
    				if (results[key] === undefined) {
    					refresh.reagentCatalogs(params,key);
    				}
    				return results[key];
    			},
    			getReagentReceptions : function(params,key){
    				key = (key)?key:'reagentReceptions';
    				if (results[key] === undefined) {
    					refresh.reagentReceptions(params,key);
    				}
    				return results[key];
    			},
    			getInstruments : function(params,key){
					key = (key)?key:'instruments';
    				if(results[key] === undefined){
    					refresh.instruments(params, key);
    				}
    				return results[key];
				},
				getInstrumentUsedTypes : function(params, key){
					key = (key)?key:'instrumentUsedTypes';
    				if(results[key] === undefined){
    					refresh.instrumentUsedTypes(params, key);
    				}
    				return results[key];
				},
    			getValuations : function(params,key){return results['valuations'];},
    			getValues : function(params, key){
    				if(results[key] === undefined){
    					refresh.values(params, key);
    				}
    				return results[key];
    			},
    			getPropertyDefinitions : function(params, key){
    				key = (key)?key:'propertyDefinitions';
    				if(results[key] === undefined){
    					refresh.propertyDefinitions(params, key);
    				}
    				return results[key];
    			},
    			getTreatmentTypes : function(params, key){
    				key = (key)?key:'treatmentTypes';
    				if(results[key] === undefined){
    					refresh.treatmentTypes(params, key);
    				}
    				return results[key];
    			},
				getPropertiesForCreation : function(params, key){
    				key = (key)?key:'propertiesForCreation';
					
    				if(results[key] === undefined){
    					refresh.propertiesForCreation(params, key);
    				}
					
    				return results[key];
    			},
				

				// NGL-3530 ajout treatmentCategories
    			getTreatmentCategories : function(params, key){
    				key = (key)?key:'treatmentCategories';
    				if(results[key] === undefined){
    					refresh.treatmentCategories(params, key);
						// HARDCODE results[key] = [{'name':'sequencing', 'code':'sequencing'},{'name':'ngsrg', 'code':'ngsrg'}];
    				}
    				return results[key];
					//POUR TEST return results['treatmentTypes']
    			},
    			getSampleTypes : function(params, key){
    				key = (key)?key:'sampleTypes';
    				if(results[key] === undefined){
    					refresh.sampleTypes(params, key);
    				}
    				return results[key];
    			},
    			getSraStudies : function(){return results['sraStudies'];},
    			getSraProjects : function(){return results['sraProjects'];},
    			getSraConfigurations : function(){return results['sraConfigurations'];},
    			getSraAnalyzes : function(){return results['sraAnalyzes'];},
				getSraSamples  : function(){return results['sraSamples'];},

    			getReadSets : function(params,key){
    				key = (key)?key:'readSets';
    				if(results[key] === undefined){
    					refresh.readSets(params, key);
    				}
    				return results[key];
    			},
    			getTags : function(params,key){
    				key = (key)?key:'tags';
    				if(results[key] === undefined){
    					refresh.tags(params,key);
    				}
    				return results[key];
    			},
    			getValuations : function(){return results['valuations'];},
    			getTagTypes : function(){return results['tagTypes'];},  // FDS 22/11/2018 ajout; liste en dur 
    			getTagCategories : function(key){    // FDS 29/11/2018 ajout; version dynamique
    				key = (key)?key:'tagCategories';
    				if(results[key] === undefined){
    					refresh.tagCategories(key);
    				}
    				return results[key];
    			},
    			getTagGroupNames : function(key){// FDS 29/11/2018 ajout; voir aussi tag-plate-helper.js
      				key = (key)?key:'tagGroupNames';
    				if(results[key] === undefined){
    					refresh.tagGroupNames(key);
    				}
    				return results[key];
    			},
    			getContext : function(params,key){
    				key = (key)?key:'context';
    				if(results[key] === undefined){
    					refresh.tags(params, key); // pourquoi refresh tags ??????????   mise en comm change rien pour recherge tags (effets ailleurs ??)
    				}
    				return results[key];
    			},
    			getReagentCategories : function(){return results['reagentCategories'];},
    		};
    		
    	}]).factory('convertValueServices', [function() {
    		var constructor = function($scope){
				return {
				    //Convert the value in inputUnit to outputUnit if the units are different
					convertValue : function(value, inputUnit, outputUnit, precision){
							if(inputUnit !== outputUnit && !isNaN(value)){
								var convert = this.getConversion(inputUnit,outputUnit);
								if(convert != undefined && !angular.isFunction(convert)){
									value = value * convert;
									if(precision !== undefined){
										value = value.toPrecision(precision);
									}
									//else{
									//	value = value.toPrecision(convert.toString().length);
									//}
								}else if(convert == undefined){
									throw "Error: Unknown Conversion "+inputUnit+" to "+outputUnit;
									// return undefined;
								}
							}
							
							return value;
					},
					//Get the multiplier to convert the value
					getConversion : function(inputUnit, outputUnit){
						if((inputUnit === 'pb' && outputUnit === 'Kb') || (inputUnit === 'µg' && outputUnit === 'ng') || (inputUnit === 'ml' && (outputUnit === 'µl' || outputUnit === 'µL')) || (inputUnit === 'pM' && outputUnit === 'nM')){
							return (1/1000);
						}else if((inputUnit === 'Kb' && outputUnit === 'pb') || (inputUnit === 'ng' && outputUnit === 'µg') || ((inputUnit === 'µl' || inputUnit === 'µL') && outputUnit === 'ml') || (inputUnit === 'nM' && outputUnit === 'pM')){
							return 1000;
						} else if(inputUnit === 'Gb' && outputUnit === 'pb') {
							return 1000000000;
						} else if(inputUnit === 'pb' && outputUnit === 'Gb') {
							return (1/1000000000);
						} else if(inputUnit === 'Mb' && outputUnit === 'pb') {
							return 1000000;
						} else if(inputUnit === 'pb' && outputUnit === 'Mb') {
							return (1/1000000);
						}else if((inputUnit === 'pmol' && outputUnit === 'fmol')){
							return 1000;
						}else if((inputUnit === 'fmol' && outputUnit === 'pmol')){
							return 1/1000;
						}
						
						return undefined;
					},
					parse : function(value){
						var valueToConvert = value;
						if(!angular.isNumber(valueToConvert)){
							var valueConverted = value.replace(/\s+/g,"").replace(',','.');
							valueConverted = parseFloat(valueConverted);
							
							return valueConverted;
						}
						
						return value;
					}
				};
			};
    		return constructor;
    	}]).factory('dateServices', ['$filter', function($filter) {
    		// FDS added 12/10/2020 NGL-3000
    		var constructor = function($scope){
				return {
					// msdate is a milliseconds date timestamp
					// dateformat is a date format string  like DD/MM/YYYY
					isValidDateFormat : function(msdate, dateformat){
						console.log ("check :"+ msdate); // en ms !!!!
						// string donnée par la directive date, elle fait deja des corrections !!!!! exemple 08/10/20 ===> 08/10/0020
						var stringdate =$filter('date')(msdate, dateformat); // ici il faut SANS uppercase
						//verifier avec moment si cette string correspond au format attendu. ajouter strict=true
						if ( ! moment(stringdate, dateformat.toUpperCase(), true).isValid() ){
							console.log ("INVALID DATE");
							return false;
						} else { 
							console.log ("VALID DATE");
							return true;
						}
					},
					
					isFuturDate : function(msdate){
						// msdate is a milliseconds date timestamp
						// use moment.js to check : https://momentjscom.readthedocs.io/en/latest/moment/05-query/03-is-after/
						return moment(msdate).isAfter(moment(),'day');
					}
				};
    		};
			return constructor;
    	}]).directive('messages', function() {
    		return {
    			restrict: 'A',
    			scope: {
    				  messages: '=messages'
    				},
    			template: '<div ng-class="messages.clazz" ng-show="messages.isOpen()">'+
    				'<button class="close" ng-click="messages.close()" type="button">&times;</button>'+
    				'<strong>{{messages.text}}</strong><button class="btn btn-link" ng-click="messages.showDetails=!messages.showDetails" ng-show="messages.isDetails">{{messages.transformKey("msg.details")}}</button>'+
    				'<div ng-show="messages.showDetails">'+
    				'    <ul>'+
    				'		<li ng-repeat="message in messages.details | toArray | orderBy: \'$key\' track by $index">{{message.$key}}'+
    				'		<ul>'+
    				'			<li ng-repeat="(key2, value2) in message track by $index"> {{value2}} </li>'+
    			    '		</ul>'+
    			    '		</li>'+
    			    '	</ul>'	+
    			    '</div>'+
    			    '</div>'
    			};
    	}).directive('codes', function() {
    		return {
    			restrict: 'A',
    			require: 'ngModel',
    			link: function(scope, element, attrs, ngModel) {
    				if(!ngModel) return;
    				 var type = attrs.codes;
    				 ngModel.$render = function() {
    					 if(ngModel.$viewValue){
    						 if(angular.isArray(ngModel.$viewValue)){
    							 for(var i=0;i<ngModel.$viewValue.length;i++){
    								 ngModel.$viewValue[i] = Codes(type+"."+ngModel.$viewValue[i]);
    							 }
    							 element.html(ngModel.$viewValue);
    						 }else{
    							 element.html(Messages(Codes(type+"."+ngModel.$viewValue)));
    						 }
    					 }
    				 };
    			}    					
    		};
    	//If the select or multiple choices contain 1 element, this directive select it automaticaly
    	//EXAMPLE: <select ng-model="x" ng-option="x as x for x in x" auto-select>...</select>
    	}).directive('autoSelect',['$parse', function($parse) {
    		return {
    			require: 'ngModel',
    			link: function(scope, element, attrs, ngModel) {
    				var valOption = undefined;
					var multiple = false;
    				if(attrs.ngOptions){	
						valOption = attrs.ngOptions;
					}else if(attrs.btOptions){
						valOption = attrs.btOptions;
					}
					
    				if(attrs.multiple === true || attrs.multiple === "true"){
    					multiple = true;
    				}
    				
					if(valOption != undefined){
						var match = valOption.match(/^\s*(.*?)(?:\s+as\s+(.*?))?(?:\s+group\s+by\s+(.*))?\s+for\s+(?:([\$\w][\$\w\d]*)|(?:\(\s*([\$\w][\$\w\d]*)\s*,\s*([\$\w][\$\w\d]*)\s*\)))\s+in\s+(.*)$/);
						var getModelValue = $parse(match[1].replace(match[4]+'.',''));
						var model = $parse(match[7]);
						scope.$watch(model, function(value){
							if(value){
				                if(value.length === 1 && (ngModel.$modelValue == undefined || ngModel.$modelValue == "")){
									
				                	var value = (multiple)?[getModelValue(value[0])]:getModelValue(value[0]);
				                	
				                	ngModel.$setViewValue(value);
									ngModel.$render();
								}
							}
				        });
					}else{
						console.log("ng-options or bt-options required");
					}
    			}
    		};    	
    	}]).directive('convertValue',['convertValueServices','$filter', function(convertValueServices, $filter) {
            return {
                require: 'ngModel',
                link: function(scope, element, attr, ngModel) {
                	//init service
                	var convertValues = convertValueServices();
                	var property = undefined;
                	
					var watchModelValue = function(){
						return scope.$watch(
									function(){
										return ngModel.$modelValue;
									}, function(newValue, oldValue){
										if(property != undefined){
											var convertedValue = convertValues.convertValue(newValue, property.saveMeasureValue, property.displayMeasureValue);
											ngModel.$setViewValue($filter('number')(convertedValue));
											ngModel.$render();
										}
								});
					};
					
                	scope.$watch(attr.convertValue, function(value){
    					if(value.saveMeasureValue != undefined && value.displayMeasureValue != undefined){
    						property = value;
    					}
    				});
                	
                	//model to view when the user go out of the input
                	element.bind('blur', function () {
                		//var convertedValue = convertValues.convertValue(ngModel.$modelValue, property.saveMeasureValue, property.displayMeasureValue, ngModel.$viewValue.length);
                		var convertedValue = convertValues.convertValue(ngModel.$modelValue, property.saveMeasureValue, property.displayMeasureValue);                		
                		ngModel.$setViewValue($filter('number')(convertedValue));
						ngModel.$render();
						//We restart the watcher when the user is out of the inputs
						scope.currentWatcher = watchModelValue();
                	});
                	
					//when the user go into the input
					element.bind('focus', function () {
						//We need to disable the watcher when the user is typing
						scope.currentWatcher();
                	});
					
                	//model to view whatcher
                	scope.currentWatcher = watchModelValue();
                	
                    //view to model
                    ngModel.$parsers.push(function(value) {
                    	value = convertValues.parse(value);
                    	if(property != undefined){
	                    	value = convertValues.convertValue(value, property.displayMeasureValue, property.saveMeasureValue);
                    	}
                    	return value;
                    });
                }
            };
        }]).directive('dateTimestamp', ['$filter', function($filter) {
            //Convert the date in format(view) to a timestamp date(model)
            return {
                require: 'ngModel',
                link: function(scope, element, attrs, ngModelController) {
					
    				ngModelController.$formatters.push(function(data) {
						var convertedData = data;
						convertedData = $filter('date')(convertedData, Messages("date.format"));
						
						console.log ("OLD $formatters: convertedData="+ convertedData);
					    return convertedData;
					}); 
			    
				    ngModelController.$parsers.push(function(data) {
				    	var convertedData = data;
			    	    if(moment && convertedData !== ""){
			    			var momentDate = moment(data, Messages("date.format").toUpperCase());
			    	    	if(attrs.endOfDay !== undefined ){
			    	    		console.log ("OLD $parsers: ???????");
			    	    		momentDate.endOf('day');
			    			}
			    	    	console.log ("OLD $parsers: valid date");
			    	    	convertedData = momentDate.valueOf();
			    		   }else{
			    			   console.log ("OLD $parsers: INVALID date");
			    			   convertedData = null;
			    			   //console.log("mission moment library to convert string to date");
			    		   }
			    	    
			    	      console.log ("OLD $parsers: convertedData="+ convertedData);
				    	  return convertedData;
				    }); 
    				
                }
            }
        }]).directive('dateTimestamp2', ['$filter', function($filter) {
            // FDS: Nouvelle version NGL-3000, 08/10/2020... peut elle remplacer la directive originale dans tous les cas ????
        	// regarder aussi datatable2.js pour ameliorations/simplifications...

            return {
                require: 'ngModel',
                link: function(scope, element, attrs, ngModelController) {
					
                	// transforme la valeur ( ms ) issue du modèle en date (string) à destination de la vue
                	// ms -> string
    				ngModelController.$formatters.push(function(modelValue) {
						var dateformat = Messages("date.format"); 
						var convertedData = $filter('date')(modelValue, dateformat);  // PAS toUpperCase() !!!!
						
						console.log ("NEW $formatters: convertedData="+ convertedData);
					    return convertedData;
					}); 
    				
    				// transformer la date saisie (si input type="text") par l'utilisateur dans la vue en ms a destination du modèle
    				// string --> ms
				    ngModelController.$parsers.push(function(viewValue) {
				    	var convertedData=viewValue;// ??
			    	    if(moment && viewValue !== ""){
			    	    	console.log ("NEW $parsers   viewvalue= "+viewValue)
			    	    	// si cette valeur ne correspond pas au format attendu ne pas transformer en ms !!!!
			    	    	// ajout strict=true
			    	    	// !!!!!!!!!    SUPSQCNG-902 le timestamp doit etre arrondi ==> TODO !!!!
			    			var momentDate = moment(viewValue, Messages("date.format").toUpperCase(),true);// FAUT toUpperCase() !!!
			    	    	if ( momentDate.isValid() ){ 
			    	    		console.log ("NEW $parsers: valid date");
			    	    		convertedData = momentDate.valueOf();
			    	    	}else{
			    	    		console.log ("NEW $parsers: INVALID date");
			    				//convertedData = null;
			    				convertedData = undefined;
			    	    	}
			    	    }
			    	   
			    	    console.log ("NEW $parsers: convertedData="+ convertedData);
				    	return convertedData;
				    });
                }
            }
        }]).directive('calendarTimestamp', ['$filter', function($filter) {
        	// NGL-3138 : version adaptee depuis datatable2.js ( pas de ngModelControler !!!!!) pour les calendriers ( input type="date" )
            return {
                require: 'ngModel',
                link: function(scope, elem, attrs, ngModel) {					
					/// convertit au format attentu par le calendrier.....
                	var convertToDate = function(timestamp){
                		if(timestamp !== null && timestamp !== undefined && timestamp !== ""){
	                		//var date = moment(timestamp).format('yyyy-MM-dd'); // PAS BON
	                		var date = moment(timestamp).format('YYYY-MM-DD');   // OUI
	                		console.log('converted date :'+ date)
	                		return date;
                		}
                		return "";
                	};
                	
                	var convertToTimestamp = function(date){
                		//// impossible avec un calendrier ....if(date !== null && date !== undefined && date !== ""){
        	    	    	// la view value retournée par un calendrier est au format ISO 8601
        	    			var momentDate = moment(date, moment.ISO_8601);
        	    			var timestamp=momentDate.valueOf();
        	    			console.log('converted timestamp :'+timestamp )
	    					return timestamp;
                		////}
                		////return "";
    				};
					
                	//model to view
                	scope.$watch(
						function(){
							return ngModel.$modelValue;
						}, function(newValue, oldValue){
							//We check if the date is complete.... oui mais ici on ne tape plus la date!!!
							if(newValue !== null && newValue !== undefined && newValue !== "" && "01/01/1970".length === 10){
								var date = convertToDate(newValue);
    							ngModel.$setViewValue(date);
								ngModel.$render();
							}
                    });
					
                	//view to model
                    ngModel.$parsers.push(function(value) {
						return convertToTimestamp(value);
                    });
                    
				    // 2 valeurs possibles pour l'attribut de la directive:  max-today ou min-today
				    // déterminent la valeur de l'attribut max ou min de l'input de type date
                    // on peut creer les valeurs comme 'max-tomorow'
                    var curDate=new Date();
                    var tomDate=new Date();
                    tomDate.setDate(tomDate.getDate() + 1)
				    //OK var today = $filter('date')(Date.now() ,'yyyy-MM-dd'); // Il faut ce format pour l'affichage avec un navigateur en langue FR
				    var today    = $filter('date')(curDate ,'yyyy-MM-dd'); // Il faut ce format pour l'affichage avec un navigateur en langue FR
				    var tomorrow = $filter('date')(tomDate ,'yyyy-MM-dd'); 
				    switch(attrs.calendarTimestamp) {
				    	  case 'max-today':
				    		  attrs.$set('max', today);
				    	    break;
				    	  case  'max-tomorrow':
				    		  attrs.$set('max', tomorrow);
				    		break;
				    	  case 'min-today':
				    		  attrs.$set('min', today);
				    	    break;
				    	  default:
				    		  // ni max, ni min
				    }
                }
            }
        }]).directive('base64File', [function () {
        	return {
        		 restrict: 'A',
        		 scope: {
        			 base64File: "="
        	        },
        		 link: function (scope, elem, attrs, ngModel) {
        			 var nbFiles = 0, counter = 0, files;
	        		  if(scope.base64File != undefined && scope.base64File.value == ""){
	        			  scope.base64File = undefined;
	        		  }
	        		  
	        		  var onload = onload = function (e) {
	        			 if(e.target.result!= undefined && e.target.result != ""){
        					 
        					  var base64File = {};
	        				  base64File.fullname = e.target.file.name;
	        				  
	        				  //Get the extension
	        				  //console.log("File type "+e.target.file.type);
	        				  var matchExtension = e.target.file.type.match(/^application\/(.*)/);
	        				  var matchExtensionText = e.target.file.type.match(/^text\/(.*)/);
	        				  if(matchExtension && matchExtension.length > 1){
		        				  base64File.extension = matchExtension[1];
	        				  }else if(matchExtensionText && matchExtensionText.length > 1){
	        					  base64File.extension = matchExtensionText[1];
	        				  }
	        				  if(base64File.extension != undefined){
		        				  base64File._type = "file";
		        				  
		        				  //Get the base64 without the extension feature
		        				  var matchBase64 = e.target.result.match(/^.*,(.*)/);
		        				  base64File.value = matchBase64[1];
		        				  files.push(base64File);
	        				  }else{
	        					 alert("This is not an authorized file : "+base64File.fullname);		        					 
	        				  }
	        				  counter++;
        				  }
	        			 
	        		  }
	        		  var onloadend = function(e){
	        			  if(nbFiles === counter){
	        				  if(attrs.multiple){
	        					  scope.$apply(function(scope){scope.base64File = files;});
	        				  }else{
	        					  scope.$apply(function(scope){scope.base64File = files[0];});
	        				  }
	        				  
	        			  }
	        		  };
	        		  
	        		  elem.on('change', function() {
	        			  nbFiles = 0, counter = 0;
				    	  files = [];
				    	  if(attrs.multiple){
				    		  scope.base64File = [];
				    		  nbFiles = elem[0].files.length
				    		  angular.forEach(elem[0].files, function(inputFile){
				    			  var reader = new FileReader();
				    			  reader.file = inputFile;
				    			  reader.onload = onload;	
				    			  reader.onloadend = onloadend;
				    			  reader.readAsDataURL(inputFile);				    			  		        						    			  
				    		  });
				    	  }else{
				    		  scope.base64File = undefined;
				    		  var reader = new FileReader();
				    		  nbFiles = elem[0].files.length
				    		  reader.file = elem[0].files[0];
				    		  reader.onload = onload;
				    		  reader.onloadend = onloadend;
			    			  reader.readAsDataURL(elem[0].files[0]);				    			  		    	 
				    	  }				    	  
				      });
				      
	        		  elem.on('click', function() {
	        			  elem[0].value=null;
	        		  });
        		 }
        		};
        }]).directive('base64Img', [function () {
        	return {
        		 restrict: 'A',
        		 scope: {
        			 base64Img: "="
        	        },
        		 link: function (scope, elem, attrs, ngModel) {
	        		  var nbFiles = 0, counter = 0, files;
        			  if(scope.base64Img != undefined && scope.base64Img.value == ""){
	        			  scope.base64Img = undefined;
	        		  }
	        		  
	        		  var onload =  function (e) {
		        		if(e.target.result!= undefined && e.target.result != ""){
	        				  var base64Img = {};
	        				  base64Img._type = "img";
	        				  base64Img.fullname = e.target.file.name;
	        				  //console.log("base64Img.fullname "+base64Img.fullname);
	        				  //Get the extension
	        				  var matchExtension = e.target.file.type.match(/^image\/(.*)/);
		        			  if(matchExtension && matchExtension.length > 1){
		        				  base64Img.extension = matchExtension[1];
		        				  
		        				  //Get the base64 without the extension feature
		        				  var matchBase64 = e.target.result.match(/^.*,(.*)/);
		        				  base64Img.value = matchBase64[1];
		        				  //Load image from the base64 to get the width and height
		        				  var img = new Image();
		        				  img.src =  e.target.result;
	
		        				  img.onload = function(){
		        					  counter++;
		        					  base64Img.width = img.width;
		        					  base64Img.height = img.height;
		        					  files.push(base64Img);
		        					  onloadend();
		        					  
		        				  };		        				  
		        				  		        				  
	        				  }else{
	        					 counter++;
	        					 alert("This is not an image..."+base64Img.fullname);	        					
	        				  }
		        			  
        				  }
	        		  };
	        		  
	        		  var onloadend = function(){
	        			  if(nbFiles === counter){
	        				  if(attrs.multiple){
	        					  scope.$apply(function(scope){scope.base64Img = files;});
	        				  }else{
	        					  scope.$apply(function(scope){scope.base64Img = files[0];});
	        				  }
	        				  
	        			  }
	        		  };
	        		  
				      elem.on('change', function() {
				    	  nbFiles = 0, counter = 0;
				    	  files = [];
				    	  if(attrs.multiple){
				    		  scope.base64Img = [];
				    		  nbFiles = elem[0].files.length
				    		  angular.forEach(elem[0].files, function(inputFile){
				    			  var reader = new FileReader();
				    			  reader.file = inputFile;
				    			  reader.onload = onload;	
				    			  //reader.onloadend = onloadend;
				    			  reader.readAsDataURL(inputFile);				    			  		        						    			  
				    		  });
				    	  }else{
				    		  scope.base64Img = undefined;
				    		  var reader = new FileReader();
				    		  nbFiles = elem[0].files.length
				    		  reader.file = elem[0].files[0];
				    		  reader.onload = onload;
				    		  //reader.onloadend = onloadend;
			    			  reader.readAsDataURL(elem[0].files[0]);				    			  		    	 
				    	  }				    	  
				      });
				      
				      elem.on('click', function() {
	        			  elem[0].value=null;
	        		  });
        		 }
        		};
        		}])
        	.directive('btSelect',  ['$parse', '$document', '$window', '$filter', function($parse,$document, $window, $filter)  {
			//0000111110000000000022220000000000000000000000333300000000000000444444444444444000000000555555555555555000000066666666666666600000000000000007777000000000000000000088888
    		var BT_OPTIONS_REGEXP = /^\s*([\s\S]+?)(?:\s+as\s+([\s\S]+?))?(?:\s+group\s+by\s+([\s\S]+?))?\s+for\s+(?:([\$\w][\$\w]*))\s+in\s+([\s\S]+?)$/;                        
    		 // 1: value expression (valueFn)
            // 2: label expression (displayFn)
            // 3: group by expression (groupByFn)
            // 4: disable when expression (disableWhenFn)
            // 5: array item variable name
            // 6: object item key variable name
            // 7: object item value variable name
            // 8: collection expression
            // 9: track by expression
  		    return {
  		    	restrict: 'A',
  		    	replace:false,
  		    	scope:true,
  		    	template:'<div ng-switch on="isEdit()">'
  		    			+'<div ng-switch-when="false">'
  		    			+'<ul class="list-unstyled form-control-static">'
		    	  		+'<li ng-repeat-start="item in getItems()" ng-if="groupBy(item, $index)" ng-bind="itemGroupByLabel(item)" style="font-weight:bold"></li>'
		    	  		+'<li ng-repeat-end  ng-if="item.selected" ng-bind="itemLabel(item)"></li>'
			    	  	+'</ul>'
  		    			+'</div>'
  		    			+'<div class="dropdown" ng-switch-when="true">'  				        
  		    			
  		    			+'<div class="input-group">'
  		    			
  		    			+'<div class="input-group-btn" ng-if="isTextarea()">'
  		    			//textarea mode
  		    			+'<button tabindex="-1" data-toggle="dropdown" class="btn btn-default btn-xs dropdown-toggle" type="button" ng-disabled="isDisabled()" ng-click="open()">'
  		    			+'<i class="fa fa-list-ul"></i>'
  		    			+'</button>'
  		    			+'<ul class="dropdown-menu dropdown-menu-left"  role="menu">'
  				        +'<li>'
						// NGL-4049 ajout placeholder parametrable. Ne marche que si tout en minuscules; camelCase marche pas !!
  				        +'<textarea placeholder="{{textareaplaceholder}}" ng-class="inputClass" ng-model="textareaValue" ng-change="setTextareaValue(textareaValue)" rows="5"></textarea>'
  				        +'</li>'
		    	  		+'</ul>'
		    	  		+'</div>'
  		    			
		    	  		//select mode
  		    			+'<input type="text" style="background:white" ng-class="inputClass" ng-model="selectedLabels" placeholder="{{placeholder}}" readonly/>'
  		    			+'<div class="input-group-btn">'
  		    			+'<button tabindex="-1" data-toggle="dropdown" class="btn btn-default btn-sm dropdown-toggle" type="button" ng-disabled="isDisabled()" ng-click="open()">'
  		    			+'<span class="caret"></span>'
  		    			+'</button>'
  		    			+'<ul class="dropdown-menu dropdown-menu-right"  role="menu">'
  				        +'<li ng-show="filter"><input ng-class="inputClass" type="text" ng-click="inputClick($event)" ng-model="filterValue" ng-change="setFilterValue(filterValue)" title="{{getMessage(\'bt-select.here.long\')}}" placeholder="{{getMessage(\'bt-select.here\')}}"/></li>'
  				        // Liste des items déja cochés
		    	  		+'<li ng-repeat-start="item in getSelectedItems()" ng-if="groupBy(item, $index) && acceptsMultiple()"></li>'
  				        +'<li class="dropdown-header" ng-if="groupBy(item, $index)" ng-bind="itemGroupByLabel(item)"></li>'
  				        +'<li ng-repeat-end ng-if="item.selected" ng-click="selectItem(item, $event)">'
  				        +'<a href="#">'
		    	  		+'<i class="fa fa-check pull-right" ng-show="item.selected"></i>'
		    	  		+'<span class="text" ng-bind="itemLabel(item)" style="margin-right:30px;"></span>'
		    	  		+'</a></li>'
		    	  		+'<li ng-show="getSelectedItems().length > 0" class="divider pull-left" style="width: 100%;"></li>'
  				        // Liste des items
  				        +'<li ng-repeat-start="item in getItems()" ng-if="groupBy(item, $index)" class="divider"></li>'
  				        +'<li class="dropdown-header" ng-if="groupBy(item, $index)" ng-bind="itemGroupByLabel(item)"></li>'
		    	  		+'<li ng-repeat-end ng-click="selectItem(item, $event)">'
			    	  	+'<a href="#">'
		    	  		+'<i class="fa fa-check pull-right" ng-show="item.selected"></i>'
		    	  		+'<span class="text" ng-bind="itemLabel(item)" style="margin-right:30px;"></span>'
		    	  		+'</a></li>'
		    	  		+'</ul>'
		    	  		+'</div>'
		    	  		
		    	  		+'</div>'
		    	  		+'</div>'
		    	  		+'</div>'
		    	  		,
	    	  		require: ['?ngModel'],
	       		    link: function(scope, element, attr, ctrls) {
	       		  // if ngModel is not defined, we don't need to do anything
	      		      if (!ctrls[0]) return;
	      		      scope.inputClass = element.attr("class");
					  scope.placeholder = attr.placeholder;
    		          scope.textareaplaceholder = attr.textareaplaceholder;

	      		      element.attr("class",''); //remove custom class
	      		     
	      		      var ngModelCtrl = ctrls[0],
	      		          multiple = attr.multiple || false,
	      		          textarea = attr.textarea || false,
	      		          btOptions = attr.btOptions,
	      		          editMode = (attr.ngEdit)?$parse(attr.ngEdit):undefined,
	      		          filter = attr.filter || false;

	      		      var optionsConfig = parseBtsOptions(btOptions);
	      		      var items = [];
	      		      var groupByLabels = {};
	      		      var filterValueRegex;
	      		      var ngFocus = attr.ngFocus;
	      		      var ngModelValue = attr.ngModel;
	      		      function parseBtsOptions(input){
	      		    	  var match = input.match(BT_OPTIONS_REGEXP);
		      		      if (!match) {
		      		        throw new Error(
		      		          "Expected typeahead specification in form of '_modelValue_ (as _label_)? for _item_ in _collection_'" +
		      		            " but got '" + input + "'.");
		      		      }
	
		      		    return {
		      		        itemName:match[4],
		      		        sourceKey:match[5],
		      		        source:$parse(match[5]),
		      		        viewMapper:match[2] || match[1],
		      		        modelMapper:match[1],
		      		        groupBy:match[3],
		      		        groupByGetter:match[3]?$parse(match[3].replace(match[4]+'.','')):undefined
		      		      };
		      		      
	      		      };
	      		      /*
		      		    var displayFn = $parse(match[2] || match[1]),
		                valueName = match[4] || match[6],
		                keyName = match[5],
		                groupByFn = $parse(match[3] || ''),
		                valueFn = $parse(match[2] ? match[1] : valueName),
		                valuesFn = $parse(match[7]),
		                track = match[8],
		                trackFn = track ? $parse(match[8]) : null,
	                */
	      		   
	      		     scope.filter = filter; 
	      		     scope.setFilterValue = function(value){
	      		    	 if(value.match(/^\/.+\/.{0,1}/)){
	      		    		filterValueRegex = new RegExp(value.split("/")[1], value.split("/")[2]);
	      		    	 }else{
	      		    		filterValueRegex = new RegExp(value, "i");
	      		    	 }	      		    	 
	      		     };
	      		     
	      		     scope.isTextarea = function(){
	      		    	return (multiple && textarea); 
	      		     };
	      		     scope.setTextareaValue = function(values, $event){
	      		    	if(multiple){
      		    			var selectedValues = values.split(/\s*[,;\n\t]\s*/);
      		    			ngModelCtrl.$setViewValue(selectedValues);
      		    			ngModelCtrl.$render();      		    			
      		    	  	}	      		    	 	      		    	 
	      		     }; 
	      		     
	      		     scope.open = function(){
	      		    	 if(ngFocus){
	      		    		$parse(ngFocus)(scope);  
	      		    	 }
	      		     };
	      		     
	      		     scope.isDisabled = function(){
	      		    	return (attr.ngDisabled)?scope.$parent.$eval(attr.ngDisabled):false;
	      		     };
	      		     
	      		     scope.isEdit = function(){
	      		    	 return (editMode)?editMode(scope):true;
	      		     };
	      		     
	      		     scope.acceptsMultiple = function(){
	      		    	 return attr.multiple;
	      		     }
	      		     
	      		     scope.getSelectedItems = function(){
	      		    	 var itemsList = items;
	      		    	 var selectedItems = [];
	      		    	 itemsList.forEach(function(s){
	      		    		 if (s.selected){
	      		    			 selectedItems.push(s);
	      		    		 }
	      		    	 });
	      		    	return selectedItems;
	      		     };
	      		     
	      		     scope.getItems = function(){
	      		    	 if(scope.isEdit() && scope.filter){
	      		    		// var filter = {};
	      		    		//Angularjs 1.3.11 change, we don't want the filter to match an undefined
	      		    		//filterValue, so we don't assign it
	      		    		/*
	      		    		if(filterValue){
	      		    			var getter = $parse(optionsConfig.viewMapper.replace(new RegExp(optionsConfig.itemName+'.','g'),''));
	      		    			getter.assign(filter, filterValue);
	      		    		}
	      		    		*/
	      		    		/* old with property system
	      		    		if(filterValue){
	      		    			var pName = optionsConfig.viewMapper.replace(new RegExp(optionsConfig.itemName+'.','g'),'');
	      		    			filter[pName] = filterValue;
	      		    		}
	      		    		*/
	      		    		/*
	      		    		if(filterValue){
	      		    			filterValue=filterValue.toLowerCase();
	      		    		}
	      		    		*/
	      		    		var functionFilter = function(value, index, array){
	      		    			if(filterValueRegex){
	      		    				if(null == scope.itemLabel(value).match(filterValueRegex))return false;	      		    				
	      		    			}
	      		    			return true;	      		    			
	      		    		};
	      		    		
	      		    		//Then here the filter will be empty if the filterValue is undefined
	      		    		return $filter('limitTo')($filter('filter')(items, functionFilter), 20);
	      		    	 }else{
	      		    		return items;
	      		    	 }
	      		     };
	      		    
	      		    scope.groupBy = function(item, index){
	      		    	if(index === 0){ //when several call
	      		    		groupByLabels = {};
	      		    	}
	      		    	
	      		    	if(optionsConfig.groupByGetter && scope.isEdit()){
	      		    		if(index === 0 || (index > 0 && optionsConfig.groupByGetter(items[index-1]) !== optionsConfig.groupByGetter(item))){
	      		    			return true;
	      		    		}	      		    		
	      		    	}else if(optionsConfig.groupByGetter && !scope.isEdit()){
	      		    		if(item.selected && !groupByLabels[optionsConfig.groupByGetter(item)]){
	      		    			groupByLabels[optionsConfig.groupByGetter(item)] = true;
	      		    			return true;
	      		    		}	      		    		
	      		    	}
	      		    	return false;	      		    	
	      		    }; 
	      		  
	      		  scope.getMessage = function(value){
	      			  return Messages(value);
	      		  };
	      		    
      		      scope.itemGroupByLabel = function(item){
      		    	 return optionsConfig.groupByGetter(item);
      		      }
      		      
      		      scope.itemLabel = function(item){	      		    	
      		    	// return item[optionsConfig.viewMapper.replace(optionsConfig.itemName+'.','')];  
      		    	//return $parse(optionsConfig.viewMapper.replace(new RegExp(optionsConfig.itemName+'.','g'),''))(item);
      		    	var obj = {};
  		    		
      		    	if(optionsConfig.viewMapper.indexOf(optionsConfig.itemName) > -1){
      		    		obj[optionsConfig.itemName]=item;
      		    	}else{
      		    		obj = item;
      		    	}
      		    	return $parse(optionsConfig.viewMapper)(obj);  
      		      };
      		      
      		      scope.itemValue = function(item){
      		    	 //return item[optionsConfig.modelMapper.replace(optionsConfig.itemName+'.','')];
      		    	  return $parse(optionsConfig.modelMapper.replace(new RegExp(optionsConfig.itemName+'.','g'),''))(item);
      		      };
      		      scope.textareaValue = undefined;
      		      scope.$watch(ngModelValue, function(newValue, oldValue){
      		    	     if(newValue!= undefined && newValue !== null && oldValue !== newValue){		    		
      		    	    	 render();
      		    	     }
      		    	   if(newValue === undefined || newValue === null){
 			    		  scope.textareaValue = undefined;      		    				    		   		    		
 			    	  }
      		    	     
      		    	     
      		      }, true);
      		      
      		      scope.selectItem = function(item, $event){      		    	  
      		    	  if(multiple){
      		    			var selectedValues = ngModelCtrl.$viewValue || [];
      		    		    var newSelectedValues = [];
      		    			var itemValue = scope.itemValue(item);
      		    			var find = false;
      		    			for(var i = 0; i < selectedValues.length; i ++){
      		    				if(selectedValues[i] !== itemValue){
      		    					newSelectedValues.push(selectedValues[i]);
      		    				}else{
      		    					find = true;
      		    				}
      		    			}
      		    			if(!find){
      		    				newSelectedValues.push(itemValue);
      		    			}
      		    			selectedValues = newSelectedValues;
      		    			
      		    			ngModelCtrl.$setViewValue(selectedValues);
      		    			ngModelCtrl.$render();
      		    			$event.preventDefault();
      		    			$event.stopPropagation();
      		    	  	}else{
      		    	  		if(scope.itemValue(item) !== ngModelCtrl.$viewValue){
      		    	  			ngModelCtrl.$setViewValue(scope.itemValue(item));
      		    	  		}else{
      		    	  			ngModelCtrl.$setViewValue(null);
							}
								
							/**
							 * Si on a ce paramètre, on ne veut pas que l'utilisateur sélectionne une ligne vide. 
							 * Donc on remet la valeur d'avant.
							 */
							if (attr.noBlank) {
								ngModelCtrl.$setViewValue(scope.itemValue(item));
							}

      		    	  		ngModelCtrl.$render();
      		    	  		
      		    	  	}
      		      };
      		      scope.inputClick = function($event){
      		    	$event.preventDefault();
	    			$event.stopPropagation();
      		      };
	      		      
      		      
      		      scope.$watchCollection(optionsConfig.sourceKey, function(newValue, oldValue){
      		    	  if(newValue && angular.isArray(newValue)){
      		    		items = angular.copy(newValue);      		    		
      		    		render();      		    		
      		    	  }else if(oldValue && (null == newValue || undefined == newValue)){
      		    		items = [];
      		    		render();	
      		    	  }
      		      });
	      		      
	      		   ngModelCtrl.$render = render;
	      		   
	      		    function render() {
	      		    	var selectedLabels = [];
	      		    		      		    	
		      	    	var modelValues = ngModelCtrl.$modelValue || [];
		      	    	if(!angular.isArray(modelValues)){
		      	    		modelValues = [modelValues];
		      	    	}		      	    	
		      	    	if(items.length > 0){
			      	    	for(var i = 0; i < items.length; i++){
			      	    		var item = items[i];
			      	    		item.selected = false;
			      	    		for(var j = 0; j < modelValues.length; j++){
			      	    			var modelValue = modelValues[j];
			      	    			if(scope.itemValue(item) === modelValue){
			      	    				item.selected = true;
				      		    		selectedLabels.push(scope.itemLabel(item));
				      	    		}
			      	    		}	      	    		
			      	    	}
		      	    	}else if(textarea){
		      	    		selectedLabels = modelValues;
		      	    	}
		      	    	scope.selectedLabels = selectedLabels;
	      	        };	      	        		      		
	      		  }	      		  
  		    };
    	}]).directive('chart', function() {
    	    return {
    	        restrict: 'E',
    	        template: '<div></div>',
    	        scope: {
    	            chartData: "=value",
    	            chartObj: "=?"
    	        },
    	        transclude: true,
    	        replace: true,
    	        link: function($scope, $element, $attrs) {

    	            //Update when charts data changes
    	            $scope.$watch('chartData', function(value) {
    	                if (!value)
    	                    return;

    	                // use default values if nothing is specified in the given settings
    	                $scope.chartData.chart.renderTo = $scope.chartData.chart.renderTo || $element[0];
    	                if ($attrs.type)
    	                    $scope.chartData.chart.type = $scope.chartData.chart.type || $attrs.type;
    	                if ($attrs.height)
    	                    $scope.chartData.chart.height = $scope.chartData.chart.height || $attrs.height;
    	                if ($attrs.width)
    	                    $scope.chartData.chart.width = $scope.chartData.chart.type || $attrs.width;

    	                $scope.chartObj = new Highcharts.Chart($scope.chartData);
    	               
    	            });
    	            
    	        }
    	    };
    	    
    	}).directive('defaultValue',['$parse', function($parse) {
    		return {
    			require: 'ngModel',
    			link: function(scope, element, attrs, ngModel) {
    				var _defaultValue = null;
    				scope.$watch(attrs.defaultValue, function(defaultValue){
    					if(defaultValue !== undefined && defaultValue !== null ){
    						_defaultValue = defaultValue;
    					}
    				});
    				//TODO GA ?? better way with formatter
					scope.$watch(ngModel, function(value){
			                if(_defaultValue != null && (ngModel.$modelValue === undefined || ngModel.$modelValue === "")){
									ngModel.$setViewValue(_defaultValue);
									ngModel.$render();
							}
				    });
    			}
    		};	    	
    	}]).directive('pdefDefaultValue',['$parse', function($parse) {
    		return {
    			require: 'ngModel',
    			link: function(scope, element, attrs, ngModel) {
    				var _pdef = null;
    				scope.$watch(attrs.pdefDefaultValue, function(pdef){
    					if(pdef !== null && pdef !== undefined && pdef.defaultValue !== undefined && pdef.defaultValue !== null ){
    						_pdef = pdef;
    					}
    				});
    				//TODO GA ?? better way with formatter
					scope.$watch(ngModel, function(value){
			                if(_pdef != null && (ngModel.$modelValue === undefined || ngModel.$modelValue === "")){
								if(_pdef.valueType === "java.lang.Boolean"){
									if(_pdef.defaultValue === "true" || _pdef.defaultValue === true){
										ngModel.$setViewValue(true);
										ngModel.$render();
									}else if(_pdef.defaultValue === "false" || _pdef.defaultValue === false){
										ngModel.$setViewValue(true); // hack to insert false value 
										ngModel.$setViewValue(false);
										ngModel.$render();
									}											
								}else {
									ngModel.$setViewValue(_pdef.defaultValue);
									ngModel.$render();
								}
			                	
							}
				    });
    			}
    		};	    	
    	}]).filter('filters',['$filter',function ($filter) {
    		return function (array, expressions, comparator) {
    			if(!angular.isArray(expressions)) expressions = [expressions];
    			if(comparator === undefined || comparator === null )comparator = false; 
    			var filtered = [];
    			for(var i = 0; i < expressions.length; i++){
    				var result = $filter('filter')(array, expressions[i], comparator);
    				if(result && result.length > 0)filtered = filtered.concat(result);    				
    			}
    			if(filtered.length > 0)return filtered;
    			return undefined;
    		}
    	}]).filter('unique', ['$parse', function($parse) {
    		return function (collection, property) {
				var isUndefined = angular.isUndefined,
				isObject = angular.isObject,
				isArray = angular.isArray,
				equals = angular.equals;
				
				if(!isArray(collection) && !isObject(collection)){
					return collection;
				}
				/**
				* get an object and return array of values
				* @param object
				* @returns {Array}
				*/
				function toArray(object) {
				    var i = -1,
				    props = Object.keys(object),
				    result = new Array(props.length);
				
				    while(++i < props.length) {
				        result[i] = object[props[i]];
				    }
				    return result;
				}

				  collection = (angular.isObject(collection)) ? toArray(collection) : collection;
				  if(collection !== undefined && collection !== null){
					  if (isUndefined(property)) {
						  var results =  collection.filter(function (elm, pos, self) {
							  return self.indexOf(elm) === pos;
						  })
						  
						  //return (results.length === 1)?results[0]:results;
						  return results;
					  }
					  //store all unique members
					  var uniqueItems = [],
						  get = $parse(property);
				
					  var results = collection.filter(function (elm) {
						var prop = get(elm);
						if(some(uniqueItems, prop)) {
						  return false;
						}
						uniqueItems.push(prop);
						return true;
					  });
					  
					  //return (results.length === 1)?results[0]:results;
					  return results;
				  }
				  //checked if the unique identifier is already exist
				  function some(array, member) {
				    if(isUndefined(member)) {
				      return false;
				    }
				    return array.some(function(el) {
				      return equals(el, member);
				    });
				  }
				}
    	}]).filter('sum', ['$parse',function($parse) {
    	    return function(array, key) {
    	    	if(!array)return undefined;
    	    	if(!angular.isArray(array) && (angular.isObject(array) || angular.isNumber(array))) array = [array];
    	    	else if(!angular.isArray(array)) throw "input is not an array, object or a number !";
    	    	else if(angular.isArray(array) && array.length === 0) return undefined;
    	    		
    	    	if(key && !angular.isString(key))throw "key is not valid, only string is authorized";
    	    	
    	    	var params = {sum:0, key:key};
    	    	angular.forEach(array, function(value, index){
    	    		if(params.key && angular.isObject(value))params.sum = params.sum + $parse(params.key)(value);
    	    		else if(!params.key && angular.isObject(value))throw "missing key !";
    	    		else params.sum = params.sum + value;
    	    	}, params);
    	    	return params.sum;
    	    };
//    	    // ajout sgas pour ex, mais mieux d'utiliser un filtre plus generique : "filter": "collect:'readSetCodes' | length",
//    	}]).filter('countReadsetInRun',function() {
//    	    return function(input) {
//    	    	if(!input)return 0;
//    	    	if(!angular.isArray(input)) throw "lanes is not an array, object or a number !";
//    	    	if(angular.isArray(input) && input.length === 0) return 0; 	    		
//    	    	var total = 0;
//    	    	for (var i = 0; i < input.length; i++) {
//    	    		total += input[i].readSetCodes.length;
//    	    	}
//    	    	return total;
//    	    };
    	}]).filter('get', ['$parse',function($parse) {
    	    return function(object, key) {
    	    	if(!object)return undefined;
    	    	if(angular.isArray(object) && object.length === 1) object = object[0];
    	    	else if(angular.isArray(object) && object.length > 1){
    	    		object = object[0];
    	    		console.log("input contains several values take the first !");
    	    	}
    	    	if(!angular.isObject(object))return object;
    	    	if(key && !angular.isString(key))throw "key is not valid, only string is authorized";    	    	
    	    	return $parse(key)(object);
    	    };
    	}]).
    	/**
		* get an object and a key of this object and return array of values
		* @param object
		* @param key
		* @returns [Array]
		*/
    	filter('getArray', ['$parse',function($parse) {
    	    return function(objects, key) {
    	    	if(key && !angular.isString(key))throw "key is not valid, only string is authorized"; 	
    	    	if(!objects)return undefined;    	    	
    	    	if(!angular.isObject(objects))  return objects;    	    
    	    	var data=[];
    	    	var get="";
    	    	if(angular.isObject(objects)  && objects.length > 0){    	    		
    	    		angular.forEach(objects, function(value, index){
    	    			get=$parse(key)(value);    	    			
    	    			if(get !== null && get !== undefined){
    	    				data.push(get);    	    			
    	    			}
    	    		});    	    		
    	    	}   
    	    	
    	    	return data;
    	    };
		}])
		/**
		 * Filtre qui permet, à partir d'un objet et d'une clé, de retourner un tableau de valeurs de cette clé.
		 * Ce filtre prend les mêmes arguments que le filtre getArray.
		 * La seule différence avec le getArray c'est que si la clé n'existe pas dans le sous objet,
		 * un objet vide est retourné, là où le getArray ne renvoie rien.
		 * 
		 * @param objects L'objet où sera cherché la clé.
		 * @param key La clé à chercher dans l'objet.
		 * 
		 * @return Un tableau de valeurs avec la clé donnée en entrée de la fonction.
		 */
		.filter('getArrayNotStrict', ['$parse', function ($parse) {
    	    return function (objects, key) {
    	    	if (key && !angular.isString(key)) throw "key is not valid, only string is authorized"; 	
    	    	if (!objects) return undefined;    	    	
				if (!angular.isObject(objects)) return objects; 
				   	    
    	    	var data = [];
				var get = "";
				
    	    	if (angular.isObject(objects) && objects.length > 0) {   	    		
    	    		angular.forEach(objects, function(value, index) {
						get = $parse(key)(value);   			
    	    			data.push(get);    	    			
    	    		});    	    		
    	    	}   
    	    	
    	    	return data;
    	    };
    	}]).filter('codes', function(){
    		return function(input, key, replaceIfNotFound){
    			
    			if(null === replaceIfNotFound || undefined === replaceIfNotFound){
    				replaceIfNotFound = true;
    			}
    			
    			if(angular.isArray(input) && input.length > 0){
    				var output = [];
    				for(var i=0;i<input.length;i++){
   						var tmp = Messages(Codes(key+"."+input[i]));
   						if(tmp === key+"."+input[i] && !replaceIfNotFound){
   							output[i] = input[i];
   						}else{
   							output[i] = tmp;
   						}
    				}
    			
    				return output;
    			}else if(angular.isDefined(input) && null !== input && input !== "" && !angular.isObject(input)){ 
    				var tmp = Messages(Codes(key+"."+input))
    				if(tmp === key+"."+input && !replaceIfNotFound){
    					tmp = input;
					}
    				return tmp;    				
    			}
    			return undefined;
    		}	
    	}).filter('convert', ['convertValueServices', function(convertValueServices){
    		return function(input, property){
				var convertValues = convertValueServices();
				if(property != undefined){
					input = convertValues.convertValue(input, property.saveMeasureValue, property.displayMeasureValue);
				}
    			return input;
    		}
    	}]).filter('messages', function(){   //FDS 27/11/2018 never used in NGL !!!???
    		return function(input){
    			return Messages(input);
    		}
    	}).filter('messagesPrefix', function(){   //FDS 27/11/2018: add a prefix before calling Messages
    		return function(input, prefix){
    			return Messages(prefix+'.'+input);
    		}
    	}).filter('inttostring', function(){   //FDS 27/11/2018  never used in NGL  !!!???
    		return function(input){
    			return String(input);    			
    		}
    	}).filter('stringToArray', function(){
    		return function(input){
    			var array = [];
    			array = array.concat(input);
    			return array;    			
    		}
    	}).filter('arrayToString', function(){
    		return function(input){
    			if(!input) return;
				if(!Array.isArray(input)) return input;
				return input.join(', ');  			
    		}
    	}).filter('arrayElt', function(){
    		return function(array, position, ifOnlyOne){ //onlyOne return only if one element
    			if(angular.isArray(array) && ifOnlyOne !== true)return array[position]; 
    			else if(angular.isArray(array) && ifOnlyOne === true && array.length === 1)return array[0]
    			else return array;   			    			
    		}
    	}).filter('countDistinct', ['$parse',function($parse) {    //FDS 27/11/2018  never used in NGL  !!!???
    	    return function(array, key) {
    	    	if (!array || array.length === 0)return undefined;
    	    	if (!angular.isArray(array) && (angular.isObject(array) || angular.isNumber(array) || angular.isString(array) || angular.isDate(array))) array = [array];
    	    	else if(!angular.isArray(array)) throw "input is not an array, object, number or string !";
    	    	
    	    	if(key && !angular.isString(key))throw "key is not valid, only string is authorized";
    	    	
    	    	var possibleValues = [];
    	    	angular.forEach(array, function(element){
    	    		if (angular.isObject(element)) {
    	    			var currentValue = $parse(key)(element);
    	    			if(undefined !== currentValue && null !== currentValue && possibleValues.indexOf(currentValue) === -1){
       	    				possibleValues.push(currentValue);
    	    			}
    	    			
    	    			
    	    		}else if (!key && angular.isObject(value)){
    	    			throw "missing key !";
    	    		}
    	    		
    	    	});
    	    	return possibleValues.length;    	    	
    	    };
    	}]).filter('collect', ['$parse',function($parse) {
    	    return function(array, key) {
    	    	if (!array || array.length === 0)return undefined;
    	    	if (!angular.isArray(array) && (angular.isObject(array) || angular.isNumber(array) || angular.isString(array) || angular.isDate(array))) array = [array];
    	    	else if(!angular.isArray(array)) throw "input is not an array, object, number or string !";
    	    	
    	    	if(key && !angular.isString(key))throw "key is not valid, only string is authorized";
    	    	
    	    	var possibleValues = [];
    	    	angular.forEach(array, function(element){
    	    		if (angular.isObject(element)) {
    	    			var currentValue = $parse(key)(element);
    	    			if(undefined !== currentValue && null !== currentValue){
    	    				//Array.prototype.push.apply take only arrays
    	    				if(angular.isArray(currentValue)){
    	    					Array.prototype.push.apply(possibleValues, currentValue);
    	    				   /*var bl = possibleValues.length;
    	    				   var al = currentValue.length;
    	    				   var i = 0;
    	    				  
	    	    			   while (i < bl) {
	    	    				   currentValue[al++] = possibleValues[i++];
	    	    			   }*/
    	    				}else{
    	    					possibleValues.push(currentValue);
    	    				}
    	    			}
    	    			
    	    			
    	    		}else if (!key && angular.isObject(value)){
    	    			throw "missing key !";
    	    		}
    	    		
    	    	});
    	    	return possibleValues;    	    	
    	    };
    	}]).filter('flatArray', function(){
    		return function(array, property){
    			var flatArray = [];
    			if(angular.isArray(array)){
	    			for(var i=0;i<array.length;i++){
	    				if(null != property && undefined != property && array[i][property]){
	    					flatArray = flatArray.concat(array[i][property]);
	    				}else if((null === property || undefined === property) && array[i]){
	    					flatArray = flatArray.concat(array[i]);
	    				}
	    			}
    			}
    			return flatArray;
    		}
    	}).filter('toArray', function () { //transform object to array
    		  return function (obj, addKey) {
    			    if (!angular.isObject(obj)) return obj;
    			    if ( addKey === false ) {
    			      return Object.keys(obj).map(function(key) {
    			        return obj[key];
    			      });
    			    } else {
    			      return Object.keys(obj).map(function (key) {
    			        var value = obj[key];
    			        return angular.isObject(value) ?
    			          Object.defineProperty(value, '$key', { enumerable: false, value: key}) :
    			          { $key: key, $value: value };
    			      });
    			    }
    			  };
    	}).filter('format',  ['$parse',function($parse){ //transform object to array
  		  return function (obj,pattern) {
			  if(obj === null || obj === undefined)return;  
			  if(pattern === null || pattern === undefined)return;
			  return $parse(pattern)(obj);
			  };
		}]).filter('formatProjectListLabel',  ['$filter','$parse',
			function($filter, $parse){ 
	  		  return function (proj) {
				  if(proj === null || proj === undefined)return;  
				  var convert = function(proj){
					  if(angular.isObject(proj)){
						  if(proj.code !== proj.name)
							  return $parse('code+\" \(\"+name+\"\)\"')(proj);
						  else
							  return $parse('code')(proj);
					  } else if(angular.isString(proj)){
						  var name = $filter("codes")(proj, "project");
						  if(proj !== name)
							  return proj+' ('+name+')';
						  else
							  return proj;
					  } 
				  };
				  if(angular.isArray(proj)){
					  return proj.map(function(p){return convert(p);});
				  }else{
					  return convert(proj);
				  }
				 
	  		  };
		}]).filter('length', function(){  // FDS 27/11/2018 :get length of input string
    		return function(input){
    			if(input) {
    				return input.length; 
    			}     		
    		}
		}).filter('tagLength', function(){  // FDS 03/01/2019 :get length of TAG ( a tag is a string but could contain several "-" which must not be counted !!)
    		return function(input){
				return input.replace(/-/g,'').length;
    		}
		}).filter('toStringKeysObject', function() { 
    		return function (object) {
    			var result="";
    			if(angular.isObject(object)){
					var i = -1,
					props = Object.keys(object);

					while(++i < props.length) {
					  result+= props[i];
					  if (props.length > 1) {
						if (i < props.length) {
							result += ", ";
						}
					  }
					}
    				return result;
				}
			}
    	}).filter('toArrayProps', function() { // FDS 28/11/2018 : return array of properties values from an object
    		return function (object) {
				if(angular.isObject(object)){
					return Object.keys(object); // this is already an array !!!
				}
    		}
    	}).filter('percentage', function () {
			return function (input) {
				input = input.replace(",", "."); // Si on mulitplie un nombre avec une , et pas un point ça fait planter le calcul.
				return Math.floor(input * 100) + ' %';
			};
		}).filter('split', function () {
			return function (input, key) {
				return input.split(key);
			};
		}).filter('getIcuFromExperiment', function () {
			return function (input) {				
				if (input && input.objectBeforeUrl) {
					var experiment = input;
					var containerCode = input.objectBeforeUrl.code;

					for (var i = 0; i < experiment.atomicTransfertMethods.length; i++) {
						for (var j = 0; j < experiment.atomicTransfertMethods[i].inputContainerUseds.length; j++) {
							if (experiment.atomicTransfertMethods[i].inputContainerUseds[j].code == containerCode) {
								return experiment.atomicTransfertMethods[i].inputContainerUseds[j];
							}
						}
					}
				}
			};
		}).filter('additionalColumnsFilter', function() {
			var regexPatternE = '[eéèêEÉÈÊ]',	regexE = new RegExp(regexPatternE, 'g');
			var regexPatternA = '[aàAÀ]',		regexA = new RegExp(regexPatternA, 'g');
			var regexPatternC = '[cçCÇ]',		regexC = new RegExp(regexPatternC, 'g');
			var regexPatternI = '[iïîIÏÎ]',		regexI = new RegExp(regexPatternI, 'g');
			var alphabetic = /[a-zA-Z]/g;
			var reserved = /[\[\]\(\)\\?*+.|$^]/g;

			return function(array, headerPattern) {
				if(!array) return [];
				if(!headerPattern) return array;
				var headerImprovedPattern = getImprovedPattern();
				var headerRegex = new RegExp(headerImprovedPattern);
				return array.filter(testHeader);

				//---

				function getImprovedPattern() {
					return Array.from(headerPattern).map(charMapper).join('');
					
					//---

					function charMapper(char) {
						switch(true) {
							case is(regexE): return regexPatternE;	
							case is(regexA): return regexPatternA;
							case is(regexC): return regexPatternC;
							case is(regexI): return regexPatternI;
							case is(alphabetic): return upperAndLowerCase(char);	
							case is(reserved): return escaped(char);
							default: return char;
						}

						//---

						function is(regex) {
							regex.lastIndex = 0;
							return regex.test(char);
						}
					}

					function upperAndLowerCase(char) {
						return "[" + char.toLowerCase() + char.toUpperCase() + "]";
					}

					function escaped(char) {
						return '\\' + char;
					}
				}

				function testHeader(additionalColumn) {
					return additionalColumn.header && headerRegex.test(additionalColumn.header);
				}
			}
		});