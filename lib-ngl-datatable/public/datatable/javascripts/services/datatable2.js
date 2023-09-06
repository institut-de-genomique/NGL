"use strict";

angular.module('datatableServices', []).
    	factory('datatable', ['$http','$filter','$parse','$compile', '$sce', '$window', '$q', function($http, $filter,$parse,$compile,$sce,$window, $q){ //service to manage datatable
    		var constructor = function(iConfig){
				var datatable = {
						configDefault:{
							name:"datatable",
							extraHeaders:{
								number:0,// Number of extra headers
								list:{},//if dynamic=false
								dynamic:true //if dynamic=true, the headers will be auto generated
							},//ex: extraHeaders:{number:2,dynamic:false,list:{0:[{"label":"test","colspan":"1"},{"label":"a","colspan":"1"}],1:[{"label":"test2","colspan":"5"}]}}
							columns : [], /*ex : 
												{
													"header":"Code Container", //the title //used by default Messages
													"property":"code", //the property to bind or function used to extract the value
													"filter":"", angular filter to filter the value only used in read mode
													"render" : function() //render the column used to add style around value
													"editDirectives":""//Add directives to the edit element
													"id":'', //the column id
													"edit":false, //can be edited or not
													"convertValue":{
														active:false, //True if the value have to be converted when displayed to the user
														displayMeasureValue:"",//The unit display to the user, mandatory if active=true
														saveMeasureValue:"" //The unit in database,  mandatory if active=true
													},
													"hide":true, //can be hidden or not
													"order":true, //can be ordered or not
													"type":"text"/"number"/"month"/"week"/"time"/"datetime"/"range"/"color"/"mail"/"tel"/"date", //the column type
													"choiceInList":false, //when the column is in edit mode, the edition is a list of choices or not
													"listStyle":"select"/"radio", //if choiceInList=true, listStyle="select" is a select input, listStyle="radio" is a radio input
													"possibleValues":null, //The list of possible choices
													"format" : null, //number format or date format or datetime format
													"extraHeaders":{"0":"Messages("experiments.inputs")"}, //the extraHeaders list
													"tdClass : function with data and property as parameter than return css class or just the css class",
													"position": position of the column,
													"group": false //if column can be used to group data
													"groupMethod": sum, average, countDistinct, collect
													"defaultValues":"" //If the value of the column is undefined or "" when the user edit, this value show up
													"url"://to lazy data
													"
												  }*/
							columnsUrl:undefined, //Load columns config
							lines : {
								trClass : undefined // function with data than return css class or just the css class
							},
							search : {
								active:true,
								mode:'remote', //or local but not implemented
								url:undefined
							},
							pagination:{
								active:true,
								mode:'remote',
								pageNumber:0,
								numberPageListMax:3,
	    						pageList:[],
								numberRecordsPerPage:10,
	    						numberRecordsPerPageList: [{number:10, clazz:''},{number:25, clazz:''},{number:50, clazz:''},{number:100, clazz:''}]
							},
							order : {
								active:true,
								showButton : true,
								mode:'remote', //or local
								by : undefined,
								reverse : false,
								callback:undefined, //used to have a callback after order all element. the datatable is pass to callback method and number of error
								columns:{}//key is the column index
							},
							show : {
								active:false,
								showButton : true,
								add:function(line){
									console.log("show : add function is not defined in the controller !!!");
								}
							},
							hide:{
								active:false,
								showButton : true,
								columns : {} //columnIndex : true / false
							},
							edit : {
								active:false,
								withoutSelect:false, //edit all line without selected it								
								showButton : true,
								columnMode : false,
								byDefault : false, //put in edit mode when the datatable is build 
								start : false,
								all : false,
								columns : {}, //columnIndex : {edit : true/false, value:undefined}
								lineMode:undefined //function used to define if line is editable. 
							},
							save :{
								active:false,
								withoutEdit:false, //usable only for active/inactive save button by default !!!
								keepEdit:false, //keep in edit mode after safe
								showButton : true,
								changeClass : true, //change class to success or error
								mode:'remote', //or local
								url:undefined,
								batch:false, //for batch mode one url with all data
								method:'post',
								value:undefined, //used to transform the value send to the server
								callback:undefined, //used to have a callback after save all element. the datatable is pass to callback method and number of error
								start:false, //if save started
								number:0, //number of element in progress
								error:0
							},
							remove:{
								active:false,
								withEdit:false, //to authorize to remove a line in edition mode
								showButton : true,
								mode:'remote', //or local
								url:undefined, //function with object in parameter !!!
								callback : undefined, //used to have a callback after remove all element. the datatable is pass to callback method and number of error
								start:false,
								counter:0,
								number:0, //number of element in progress
								error:0,
								ids:{errors:[],success:[]}
							},
							select:{
								active:true,
								showButton:true,
								isSelectAll:false
							},
							cancel : {
								active:true,
								showButton:true
							},
							exportCSV:{
								active:false,
								showButton:true,
								delimiter:";",
								start:false
							},
							otherButtons:{
								active:false,
								template:undefined
							},
							messages:{
								active:false,
								errorClass:'alert alert-danger',
								successClass: 'alert alert-success',
								errorKey:{save:'datatable.msg.error.save',remove:'datatable.msg.error.remove'},
								successKey:{save:'datatable.msg.success.save',remove:'datatable.msg.success.remove'},
								text:undefined,
								clazz:undefined,
								transformKey : function(key, args){
									return Messages(key, args);
								}
							},
							group:{
								active:false, //group add group=true in each line of type group
								callback:undefined,
								by:undefined,
								showButton:true,
								after:true, //to position group line before or after lines
								showOnlyGroups:false, //to display only group line in datatable
								start:false,
								enableLineSelection:false, //used to authorized selection on group line
								columns:{}
							},
							showTotalNumberRecords:true,
							spinner:{
								start:false
							},
							compact:true //mode compact pour le nom des bouttons
							
						},
						config:undefined,
    					configMaster:undefined,
    					allResult:undefined,
    					allGroupResult:undefined,
    					displayResult:undefined,
    					totalNumberRecords:0,
    					urlCache:{}, //used to cache data load from column with url attribut
    					lastSearchParams : undefined, //used with pagination when length or page change
    					inc:0, //used for unique column ids
    					configColumnDefault:{
								edit:false, //can be edited or not
								hide:true, //can be hidden or not
								order:true, //can be ordered or not
								type:"text", //the column type
								choiceInList:false, //when the column is in edit mode, the edition is a list of choices or not
								extraHeaders:{},
								convertValue:{
									active:false
								}
    					},
    					//errors functions
    					/**
		    			 * Reset all the errors for a line
		    			 */
    					resetErrors : function(index){
    						this.displayResult[index].line.errors = {};
    					 },
    					 /**
 		    			 * Add error data in the line index for the field key
 		    			 */
    					addErrorsForKey : function(index, data, key){
    						 if(this.displayResult[index].line.errors === undefined){
								 this.displayResult[index].line.errors  = {};
							 }
							 this.displayResult[index].line.errors[key] = "";
						 	 for(var i=0;i<data[key].length;i++){
						 		this.displayResult[index].line.errors[key] += data[key][i]+" ";
						 	 }
    					 },
    					 /**
  		    			 * Add errors data in the line index in the key of the data key error
  		    			 */
    				    addErrors : function(index, data){
    						 for(var key in data){
    							 this.addErrorsForKey(index, data, key);
    						 }
    					 },
    					/**
    					 * External search réinit pageNumber to 0
    					 */
    					search : function(params){
    						this.config.edit = angular.copy(this.configMaster.edit);
		    				this.config.remove = angular.copy(this.configMaster.remove);
		    				this.config.select = angular.copy(this.configMaster.select);
		    				this.config.messages = angular.copy(this.configMaster.messages);
		    				this.config.pagination.pageNumber = 0;
							this._search(angular.copy(params));							
    					},
    					
    					//search functions
    					/**
		    			 * Internal Search function to populate the datatable
		    			 */
		    			_search : function(params){
		    				if(this.config.search.active && this.isRemoteMode(this.config.search.mode)){
			    				this.lastSearchParams = params;
			    				var url = this.getUrlFunction(this.config.search.url);
			    				if(url){
			    					this.setSpinner(true);
			    					$http.get(url(),{params:this.getParams(params), datatable:this}).success(function(data, status, headers, config) {
			    						config.datatable.setData(data.data, data.recordsNumber);
			    						config.datatable.setSpinner(false);
			    					});
			    				}else{
			    					throw 'no url define for search ! ';
			    				}
		    				}else{
		    					//console.log("search is not active !!")
		    				}
		    			},
		    			/**
		    			 * Search with the last parameters
		    			 */
		    			searchWithLastParams : function(){
		    				this._search(this.lastSearchParams);
		    			},
		    			
		    			/**
		    			 * Set all data used by search method or directly when local data
		    			 */
		    			setData:function(data, recordsNumber){
		    				var configPagination = this.config.pagination;
		    				if(configPagination.active && !this.isRemoteMode(configPagination.mode)){
		    					this.config.pagination.pageNumber=0;
		    				}
		    				if(recordsNumber === undefined)recordsNumber=data.length;
		    				this.allResult = data;
		    				this.totalNumberRecords = recordsNumber;
		    				this.loadUrlColumnProperty();
		    				this.computeGroup();
		    				this.sortAllResult();
		    				this.computePaginationList();
		    				this.computeDisplayResult();		    				
		    			},
		    			/**
		    			 * Return all the data
		    			 */
		    			getData:function(){
		    				return this.allResult;
		    			},
		    			/**
		    			 * Add data
		    			 */
		    			addData: function(data){
		    				if(!angular.isUndefined(data) && (angular.isArray(data) && data.length > 0)){
			    				var configPagination = this.config.pagination;
			    				if(configPagination.active && !this.isRemoteMode(configPagination.mode)){
			    					this.config.pagination.pageNumber=0;
			    				}
			    				for(var i = 0 ; i < data.length; i++){
			    					this.allResult.push(data[i]);				    				
			    				}
			    				this.totalNumberRecords = this.allResult.length;
			    				this.loadUrlColumnProperty();
			    				this.computeGroup();
			    				this.sortAllResult();
			    				this.computePaginationList();
			    				this.computeDisplayResult();			    				
			    			}
		    			},
		    			/**
		    			 * compute the group
		    			 */
		    			computeGroup: function(){
		    				if(this.config.group.active && this.config.group.by){
		    						var propertyGroupGetter = this.config.group.by.property;
		    						var groupGetter = $parse(propertyGroupGetter);
			    					var groupValues = this.allResult.reduce(function(array, value){
			    						var groupValue = groupGetter(value);
				    					if(!array[groupValue]){
				    						array[groupValue]=[];
				    					}
				    					array[groupValue].push(value);
				    					return array;
				    				}, {});
				    				var groups = {};
				    				this.allGroupResult = [];
				    				for(var key in groupValues){
				    					var group = {};
				    					var groupData = groupValues[key];
				    					$parse("group."+this.config.group.by.id).assign(group, key);
				    					var that = this;
				    					//compute for each number column the sum
				    					this.getColumnsConfig().filter(function(column){
					    					return (column.groupMethod !== undefined && column.groupMethod !== null && column.property != propertyGroupGetter);
					    				}).forEach(function(column){
				    						var propertyGetter = column.property;
				    						propertyGetter += that.getFilter(column);
				    						var columnGetter = $parse(propertyGetter);
				    						var columnSetter = $parse("group."+column.id);
				    						
				    						if('sum' === column.groupMethod || 'average' ===  column.groupMethod){
					    						var result = groupData.reduce(function(value, element){
					    							return value += columnGetter(element);
					    						}, 0);	
					    						
					    						if('average' ===  column.groupMethod)result = result / groupData.length;
					    						
					    						if(isNaN(result)){
					    							result = "#ERROR";
					    						}

					    						try{
				    								columnSetter.assign(group, result);
				    							}catch(e){
				    								console.log("computeGroup Error : "+e);
				    							}
				    						}else if('unique' === column.groupMethod){
				    							var result = $filter('unique')(groupData, column.property);
				    							if(result.length > 1){
				    								result = '#MULTI';
				    							}else if(result.length === 1){
				    								result = columnGetter(result[0]);
				    							}else{
				    								result = undefined;
				    							}
				    							columnSetter.assign(group, result);	
				    						}else if('countDistinct' === column.groupMethod){
				    							var result = $filter('countDistinct')(groupData, propertyGetter); 
				    							columnSetter.assign(group, result);
				    						}else if('collect' === column.groupMethod){
				    							var result = $filter('collect')(groupData, propertyGetter); 
				    							columnSetter.assign(group, result);
				    						}else{
				    							console.error("groupMethod is not managed "+column.groupMethod)
				    						}
				    					});
				    					
				    					groups[key] = group;				    					
				    					this.allGroupResult.push(group);				    									    									    				
				    				}
				    				this.config.group.data = groups;				    				
		    				}else{
		    					this.config.group.data = undefined;
		    					this.allGroupResult = undefined;
		    				}
		    				
		    			},
		    			
		    			getGroupColumnValue:function(groupValue, columnProperty){		    				
	    					for(var i = 0 ; i < this.config.columns.length ; i++){
	    						if(this.config.columns[i].property === columnProperty){
	    							var column = this.config.columns[i];
	    							var columnGetter = $parse("group."+column.id);
	    							return columnGetter(groupValue);
	    						}
	    					}
	    					console.log("column not found for property :"+columnProperty);		    					
		    				return undefined;
		    			},
		    			
		    			isGroupActive : function(){
		    				return (this.config.group.active && this.config.group.start);
		    			},
		    			
		    			/**
		    			 * set the order column name
		    			 * @param orderColumnName : column name
		    			 */
		    			setGroupColumn : function(column){
		    				if(this.config.group.active){
		    					var columnId;
		    					column === 'all' ? columnId = 'all' : columnId = column.id;
		    					if(this.config.group.by === undefined || this.config.group.by !== column){
		    						this.config.group.start=true;
		    						if(columnId === "all"){
		    							this.config.group.by = columnId;
		    							this.config.group.columns['all'] = true;
		    						}else{
		    							this.config.group.by = column;
		    							this.config.order.groupReverse = false;
		    							this.config.group.columns[columnId] = true;
		    							if(this.config.group.columns["all"]) this.config.group.columns["all"] = false;
		    						}
		    						
		    						for(var i = 0; i < this.config.columns.length; i++){
			    						if(this.config.columns[i].id === columnId){
			    							this.config.group.columns[this.config.columns[i].id] = true;
			    						}else{
			    							this.config.group.columns[this.config.columns[i].id] = false;
			    						}		    							    						
			    					}
		    					}else{ //degroupe
		    						this.config.group.columns[columnId] = !this.config.group.columns[columnId];
		    						if(!this.config.group.columns[columnId] || this.config.group.columns["all"]){
		    							this.config.group.by = undefined;
		    							this.config.group.columns["all"] = false;
		    						}
		    						this.config.group.start=false;
		    					}
		    					
		    					if(this.config.edit.active && this.config.edit.start){
    								//TODO add a warning popup
    								console.log("edit is active, you lost all modification !!");
    								this.config.edit = angular.copy(this.configMaster.edit); //reinit edit
    							}
		    					
		    					this.computeGroup();
		    					this.sortAllResult(); //sort all the result
		    					this.computePaginationList(); //redefined pagination
				    			this.computeDisplayResult(); //redefined the result must be displayed				    				
				    			
		    					if(angular.isFunction(this.config.group.callback)){
			    					this.config.group.callback(this);
			    				}
		    				} else{
		    					//console.log("order is not active !!!");
		    				}
		    			},
		    			updateShowOnlyGroups : function(){
		    				this.sortAllResult(); //sort all the result
		    				this.computePaginationList(); //redefined pagination
			    			this.computeDisplayResult(); //redefined the result must be displayed				    							    			
		    			},
		    			getGroupColumnClass : function(columnId){
		    				if(this.config.group.active){
		    					if(!this.config.group.columns[columnId]) {return 'fa fa-bars';}
	    						else  {return 'fa fa-outdent';}		    							    							    							    						    					    					
		    				} else{
		    					//console.log("order is not active !!!");
		    				}
		    			},
		    			addGroup : function(displayResultTmp){
		    				var displayResult = [];
	    					var groupGetter = $parse(this.config.group.by.property);
	    					var groupConfig = this.config.group;
	    					displayResultTmp.forEach(function(element, index, array){
	    						/* previous mode */
	    						if(!groupConfig.after && (index === 0 
	    								|| groupGetter(element.data) !== groupGetter(array[index-1].data))){
	    							var line = {edit:undefined, selected:undefined, trClass:undefined, group:true};
	    							this.push({data:groupConfig.data[groupGetter(element.data)], line:line});
	    						}		    						
	    						this.push(element);
	    						
	    						/* after mode */
	    						if(groupConfig.after && (index === (array.length - 1) 
	    								|| groupGetter(element.data) !== groupGetter(array[index+1].data))){
	    							var line = {edit:undefined, selected:undefined, trClass:undefined, group:true};
	    							this.push({data:groupConfig.data[groupGetter(element.data)], line:line});
	    						}		    						
	    						
	    					},displayResult);
	    					return displayResult;
		    			},
		    			/**
		    			 * Selected only the records will be displayed.
		    			 * Based on pagination configuration
		    			 */
		    			computeDisplayResult: function(){
		    				//to manage local pagination
		    				var configPagination = this.config.pagination;
		    				
		    				var _displayResult = [];
		    				if(this.config.group.start && this.config.group.showOnlyGroups){
		    					_displayResult = this.allGroupResult.slice((configPagination.pageNumber*configPagination.numberRecordsPerPage), 
		    							(configPagination.pageNumber*configPagination.numberRecordsPerPage+configPagination.numberRecordsPerPage));
		    					var displayResultTmp = [];
			    				angular.forEach(_displayResult, function(value, key){
			    					 var line = {edit:undefined, selected:undefined, trClass:undefined, group:true};
		    						 this.push({data:value, line:line});
		    					}, displayResultTmp);			    				
			    				this.displayResult = displayResultTmp;		
		    				} else{
			    				if(configPagination.active && !this.isRemoteMode(configPagination.mode)){
			    					_displayResult = angular.copy(this.allResult.slice((configPagination.pageNumber*configPagination.numberRecordsPerPage), 
			    							(configPagination.pageNumber*configPagination.numberRecordsPerPage+configPagination.numberRecordsPerPage)));
			    				}else{ //to manage all records or server pagination
			    					_displayResult = angular.copy(this.allResult);		    					
			    				}
			    				
			    				var displayResultTmp = [];
			    				angular.forEach(_displayResult, function(value, key){
			    					 var line = {edit:undefined, selected:undefined, trClass:undefined, group:false};
		    						 this.push({data:value, line:line});
		    					}, displayResultTmp);
			    				
			    				//group function
			    				if(this.isGroupActive()){
			    					this.displayResult = this.addGroup(displayResultTmp);					
			    				}else{
			    					this.displayResult = displayResultTmp;
			    				}
			    				
			    				if(this.config.edit.byDefault){
			    					this.config.edit.withoutSelect = true;
			    					this.setEdit();
			    				}	
		    				}
		    			},
		    			/**
		    			 * Load all data for url column type
		    			 */
		    			loadUrlColumnProperty :function(){
		    				
		    				var urlColumns = this.getColumnsConfig().filter(function(column){
		    					return (column.url !== undefined && column.url !== null);
		    				});
		    				
		    				var displayResult = this.allResult;
		    				var urlQueries = [];
		    				var urlCache = this.urlCache = {};
		    				
		    				urlColumns.forEach(function(column){
		    					displayResult.forEach(function(value){
	    							var url = $parse(column.url)(value);
		    						if(!angular.isDefined(urlCache[url])){
		    							urlCache[url] = "in waiting data ...";
		    							urlQueries.push($http.get(url, {url:url}));
		    						}		    								    					
		    					});
		    				});
		    				
		    				$q.all(urlQueries).then(function(results){
								angular.forEach(results, function(result, key){
									if(result.status !== 200){
										console.log("Error for load column property : "+result.config.url);
									}else{
										urlCache[result.config.url] = result.data;										
									}																									
								});
							});	
							
		    			},
		    			
		    			//pagination functions
		    			/**
		    			 * compute the pagination item list
		    			 */
		    			computePaginationList: function(){
		    				var configPagination = this.config.pagination;		    						    						    				
		    				if(configPagination.active){
		    					configPagination.pageList = [];
			    				var currentPageNumber = configPagination.pageNumber;
			    				
	    						var nbPages = Math.ceil(this.totalNumberRecords / configPagination.numberRecordsPerPage);
	    						if(this.config.group.active && this.config.group.start && this.config.group.showOnlyGroups){
		    						nbPages = Math.ceil(this.allGroupResult.length / configPagination.numberRecordsPerPage);
		    					}
	    						
	    						if(currentPageNumber > nbPages-1){
	    							configPagination.pageNumber=0;
	    							currentPageNumber = 0;
	    						}
	    						
	    						if(nbPages > 1 && nbPages <= configPagination.numberPageListMax){
			    					for(var i = 0; i < nbPages; i++){
			    						configPagination.pageList.push({number:i, label:i+1,  clazz:(i!=currentPageNumber)?'':'active'});
			    					}
		    					}else if (nbPages > configPagination.numberPageListMax){
		    						var min = currentPageNumber - ((configPagination.numberPageListMax-1)/2);
		    						var max = currentPageNumber + ((configPagination.numberPageListMax-1)/2)+1;
		    						if(min < 0){
		    							min=0;
		    						}else if(min > nbPages - configPagination.numberPageListMax){
		    							min = nbPages - configPagination.numberPageListMax;
		    						}
		    							
		    						if(max < configPagination.numberPageListMax){
		    							max=configPagination.numberPageListMax
		    						}else if(max > nbPages){
		    							max=nbPages;
		    						}
		    						
		    						configPagination.pageList.push({number:0, label:'<<',  clazz:(currentPageNumber!=min)?'':'disabled'});
		    						configPagination.pageList.push({number:currentPageNumber-1, label:'<',  clazz:(currentPageNumber!=min)?'':'disabled'});
		    						
		    						for(; min < max; min++){
		    							configPagination.pageList.push({number:min, label:min+1,  clazz:(min!=currentPageNumber)?'':'active'});
			    					}
		    						
		    						configPagination.pageList.push({number:currentPageNumber+1, label:'>',  clazz:(currentPageNumber!=max-1)?'':'disabled'});
		    						configPagination.pageList.push({number:nbPages-1, label:'>>',  clazz:(currentPageNumber!=max-1)?'':'disabled'});
		    					}		    					
		    				}else{
		    					//console.log("pagination is not active !!!");
		    				}		    				
		    			},
		    			
		    			setSpinner:function(value){
		    				this.config.spinner.start = value;		    				
		    			},
    					/**
    					 * Set the number of records by page
    					 */
    					setNumberRecordsPerPage:function(numberRecordsPerPageElement){
    						if(this.config.pagination.active){
	    						if(angular.isObject(numberRecordsPerPageElement)){
	    							this.config.pagination.numberRecordsPerPage = numberRecordsPerPageElement.number;
	    							numberRecordsPerPageElement.clazz='active';
	    							for(var i = 0; i < this.config.pagination.numberRecordsPerPageList.length; i++){
	    								if(this.config.pagination.numberRecordsPerPageList[i].number != numberRecordsPerPageElement.number){
	    									this.config.pagination.numberRecordsPerPageList[i].clazz='';
	    								}
	    							}
	    							if(this.config.edit.active && this.config.edit.start){
	    								//TODO add a warning popup
	    								console.log("edit is active, you lost all modification !!");
	    								this.config.edit = angular.copy(this.configMaster.edit); //reinit edit
	    							}
	    							//reinit to first page	    							
	    							this.config.pagination.pageNumber=0;
	    							if(this.isRemoteMode(this.config.pagination.mode)){
	    								this.searchWithLastParams();
	    							}else{
	    								this.computePaginationList();
	    								this.computeDisplayResult();	    								
	    							}	    							
	    						}
    						}else{
		    					//console.log("pagination is not active !!!");
		    				}	
    					},
    					/**
    					 * Change the page result
    					 */
    					setPageNumber:function(page){
    						if(this.config.pagination.active){
	    						if(angular.isObject(page) && page.clazz === ''){
	    							if(this.config.edit.active && this.config.edit.start){
	    								//TODO add a warning popup
	    								console.log("edit is active, you lost all modification !!");
	    								this.config.edit = angular.copy(this.configMaster.edit); //reinit edit
	    							}
	    							
		    						this.config.pagination.pageNumber=page.number;
		    						if(this.isRemoteMode(this.config.pagination.mode)){
										this.searchWithLastParams();
									}else{
										this.computePaginationList();
										this.computeDisplayResult();
									}
	    						}
    						}else{
		    					//console.log("pagination is not active !!!");
		    				}    						
    					},
    						
    					//order functions
    					/**
		    			 * Sort all result
		    			 */
		    			sortAllResult : function(){
		    				if(this.config.order.active && !this.isRemoteMode(this.config.order.mode)){
		    					var orderBy = [];
		    					
		    					if(this.config.group.active && this.config.group.start && this.config.group.by !== "all"){
		    						if(!this.config.group.showOnlyGroups){
			    						var orderGroupSense = (this.config.order.groupReverse)?'-':'+';
			    						orderBy.push(orderGroupSense+this.config.group.by.property);
			    						if(angular.isDefined(this.config.order.by)){
			    							var orderProperty = this.config.order.by.property;
					    					orderProperty += (this.config.order.by.filter)?'|'+this.config.order.by.filter:'';
			    							var orderSense = (this.config.order.reverse)?'-':'+';
				    						orderBy.push(orderSense+orderProperty)
			    						}
			    						this.allResult = $filter('orderBy')(this.allResult, orderBy);	
		    						}else{
		    							if(angular.isDefined(this.config.order.by)){
			    							var orderProperty = "group."+this.config.order.by.id;
					    					var orderSense = (this.config.order.reverse)?'-':'+';
				    						orderBy.push(orderSense+orderProperty)
			    						}
		    							this.allGroupResult = $filter('orderBy')(this.allGroupResult, orderBy);
		    						}
		    					}else if(angular.isDefined(this.config.order.by)){
		    						
		    						if(angular.isDefined(this.config.order.by)){
		    							var orderProperty = this.config.order.by.property;
				    					orderProperty += (this.config.order.by.filter)?'|'+this.config.order.by.filter:'';
		    							var orderSense = (this.config.order.reverse)?'-':'+';
			    						orderBy.push(orderSense+orderProperty)
		    						}
		    						this.allResult = $filter('orderBy')(this.allResult,orderBy);	
		    					}		    					    					
		    				}
		    			},	
		    			/**
		    			 * set the order column name
		    			 * @param orderColumnName : column name
		    			 */
		    			setOrderColumn : function(column){
		    				if(this.config.order.active){
		    					// var columnPropertyName = column.property;
		    					var columnId  = column.id;
		    					
		    					if(angular.isDefined(this.config.group.by) &&  this.config.group.by.id === column.id && !this.config.group.showOnlyGroups){
		    						this.config.order.groupReverse = !this.config.order.groupReverse; 		    						
		    					}else{
		    						if(!angular.isDefined(this.config.order.by) || this.config.order.by.id !== column.id){
			    						this.config.order.by = column;
			    						this.config.order.reverse = false;
			    					}else{
			    						this.config.order.reverse = !this.config.order.reverse;		    						
			    					}
			    					
			    					for(var i = 0; i < this.config.columns.length; i++){
			    						if(this.config.columns[i].id === columnId){
			    							this.config.order.columns[this.config.columns[i].id] = true;
			    						}else{
			    							this.config.order.columns[this.config.columns[i].id] = false;
			    						}		    							    						
			    					}
			    					if(this.config.edit.active && this.config.edit.start){
	    								//TODO add a warning popup
	    								console.log("edit is active, you lost all modification !!");
	    								this.config.edit = angular.copy(this.configMaster.edit); //reinit edit
	    							}
		    					}
		    					
		    					if(!this.isRemoteMode(this.config.order.mode)){
		    						this.sortAllResult(); //sort all the result
				    				this.computeDisplayResult(); //redefined the result must be displayed				    				
			    				} else if(this.config.order.active){
			    					this.searchWithLastParams();
			    				}	
		    					
		    					if(angular.isFunction(this.config.order.callback)){
			    					this.config.order.callback(this);
			    				}
		    				} else{
		    					//console.log("order is not active !!!");
		    				}
		    			},
		    			getOrderColumnClass : function(columnId){
		    				if(this.config.order.active){
		    					if(angular.isDefined(this.config.group.by) && this.config.group.by.id === columnId && !this.config.group.showOnlyGroups){
		    						if(!this.config.order.groupReverse) {return 'fa fa-sort-up';}
		    						else {return 'fa fa-sort-down';}	
		    					}else{
		    						if(!this.config.order.columns[columnId]) {return 'fa fa-sort';}
		    						else if(this.config.order.columns[columnId] && !this.config.order.reverse) {return 'fa fa-sort-up';}		    						
		    						else if(this.config.order.columns[columnId] && this.config.order.reverse) {return 'fa fa-sort-down';}	
		    					}
		    						    							    						    					    					
		    				} else{
		    					//console.log("order is not active !!!");
		    				}
		    			},
		    			/**
		    			 * indicate if we can order the table
		    			 */
		    			canOrder: function(){
		    				return (this.config.edit.active ? !this.config.edit.start : (this.config.order.active && !this.isEmpty()));
		    			},
		    			//show
		    			/**
		    			 * show one element
		    			 * work only with tab on the left
		    			 */
		    			show : function(){
		    				if(this.config.show.active && angular.isFunction(this.config.show.add)){
		    					angular.forEach(this.displayResult, function(value, key){
		    						if(value.line.selected){
		    							this.config.show.add(value.data);
		    						}
		    					}, this);
		    							    			
		    				}else{
		    					//console.log("show is not active !");
		    				}
		    			},
		    			//Hide a column
		    			/**
		    			 * set the hide column
		    			 * @param hideColumnName : column name
		    			 */
		    			setHideColumn : function(column){	
		    				if(this.config.hide.active){
		    					var columnId = column.id;
			    				if(!this.config.hide.columns[columnId]){
			    					this.config.hide.columns[columnId]=true;
			    				}else {
			    					this.config.hide.columns[columnId]=false;
			    				}
			    				this.newExtraHeaderConfig();
		    				}else{
		    					//console.log("hide is not active !");
		    				}
		    				
		    			},
		    			
		    			/**
		    			 * Test if a column must be grouped
		    			 * @param columnId : column id
		    			 */
		    			isGroup : function(columnId){
		    				if(this.config.group.active && this.config.group.columns[columnId]){
		    					return this.config.group.columns[columnId];
		    				}else{
		    					return false;
		    				}
		    			},
		    			
		    			/**
		    			 * Test if a column must be hide
		    			 * @param columnId : column id 
		    			 */
		    			isHide : function(columnId){
		    				if(this.config.hide.active && this.config.hide.columns[columnId]){
				    			return this.config.hide.columns[columnId];				    							    		
		    				}else{
		    					//console.log("hide is not active !");
		    					return false;
		    				}
		    			},		    			
		    			//edit
		    			
		    			/**
		    			 * set Edit all column or just one
		    			 * @param editColumnName : column name
		    			 */
		    			setEdit : function(column){	
		    				if(this.config.edit.active){
		    					this.config.edit.columns = {};
			    				var find = false;
			    				for(var i = 0; i < this.displayResult.length; i++){
			    					
			    					if(this.displayResult[i].line.selected || this.config.edit.withoutSelect){
			    						if(angular.isUndefined(this.config.edit.lineMode) || (angular.isFunction(this.config.edit.lineMode) && this.config.edit.lineMode(this.displayResult[i].data))){
			    							this.displayResult[i].line.edit=true;			    						
			    							find = true;			    					
			    						}else
			    							this.displayResult[i].line.edit=false;
			    						
			    					}else{
			    						this.displayResult[i].line.edit=false;
			    					}			    					   					
			    				}
			    				this.selectAll(false);
			    				if(find){
			    					this.config.edit.start = true;
			    					if(column){
			    						var columnId = column.id
			    						if(angular.isUndefined(this.config.edit.columns[columnId])){
			    							this.config.edit.columns[columnId] = {};
			    						}
			    						this.config.edit.columns[columnId].edit=true;			    						
			    					}
			    					else this.config.edit.all = true;
			    				}
		    				}else{
		    					//console.log("edit is not active !");
		    				}
		    			},		    			
		    			/**
		    			 * Test if a column must be in edition mode
		    			 * @param editColumnName : column name
		    			 * @param line : the line in the table
		    			 */
		    			isEdit : function(columnId, line){
		    				var isEdit = false;
		    				if(this.config.edit.active){
		    					if(columnId && line){
		    						if(angular.isUndefined(this.config.edit.columns[columnId])){
		    							this.config.edit.columns[columnId] = {};
		    						}			    								    							    					
			    					var columnEdit = this.config.edit.columns[columnId].edit;
			    					isEdit = (line.edit && columnEdit) || (line.edit && this.config.edit.all);
			    				}else if(columnId){
			    					if(angular.isUndefined(this.config.edit.columns[columnId])){
		    							this.config.edit.columns[columnId] = {};
		    						}			    								    								    					
			    					var columnEdit = this.config.edit.columns[columnId].edit;			    					
			    					isEdit = (columnEdit || this.config.edit.all);
			    				}else{
			    					isEdit = (this.config.edit.columnMode && this.config.edit.start);
			    				}
		    				}
		    				return isEdit;
		    			},
		    			/**
		    			 * indicate if at least one line is selected
		    			 */
		    			canEdit: function(){
		    				return (this.config.edit.withoutSelect ? true : this.isSelect());
		    			},
		    			/**
		    			 * Update all line with the same value
		    			 * @param updateColumnName : column name
		    			 */
		    			updateColumn : function(columnPropertyName, columnId){
		    				if(this.config.edit.active){
			    				var getter = $parse(columnPropertyName);
		    					for(var i = 0; i < this.displayResult.length; i++){
			    					if(this.displayResult[i].line.edit){
										getter.assign(this.displayResult[i].data,this.config.edit.columns[columnId].value);
			    					}
			    				}
		    				}else{
		    					//console.log("edit is not active !");		    				
		    				}
		    			},
		    			//save
		    			/**
		    			 * Save the selected table line
		    			 */
		    			save : function(){
		    				if(this.config.save.active){
		    					this.config.save.number = 0;
		    					this.config.save.error = 0;
		    					this.config.save.start = true;
		    					this.setSpinner(true);
		    					this.config.messages.text = undefined;
		    					this.config.messages.clazz = undefined;
		    					var data = [];
		    					var valueFunction = this.getValueFunction(this.config.save.value);
		    					for(var i = 0; i < this.displayResult.length; i++){
			    					if(this.displayResult[i].line.edit || this.config.save.withoutEdit){
			    						//remove datatable properties to avoid this data are retrieve in the json
			    						this.config.save.number++;
			    						this.displayResult[i].line.trClass = undefined;
				    					this.displayResult[i].line.selected = undefined;
				    					this.resetErrors(i);
			    						if(this.isRemoteMode(this.config.save.mode) && !this.config.save.batch){
			    							//add the url in table to used $q
			    							data.push(this.getSaveRemoteRequest(this.displayResult[i].data, i));			    							
			    						} else if(this.isRemoteMode(this.config.save.mode) && this.config.save.batch){
			    							//add the data in table to send in once all the result
			    							data.push({index:i, data:valueFunction(this.displayResult[i].data)});			    							
			    						} else{	
			    							this.saveLocal(this.displayResult[i].data,i);
			    						}
			    					}						
			    				}
		    					if(!this.isRemoteMode(this.config.save.mode) || data.length === 0){
	    							this.saveFinish();
	    						}else if(this.isRemoteMode(this.config.save.mode) && !this.config.save.batch){
	    							this.saveRemote(data);
	    						} else if(this.isRemoteMode(this.config.save.mode) && this.config.save.batch){
	    							this.saveBatchRemote(data);	    							
	    						}		    					
		    				}else{
		    					//console.log("save is not active !");		    				
		    				}
		    			},
		    			
		    			saveBatchRemote : function(values){
		    				var nbElementByBatch = Math.ceil(values.length / 6); //6 because 6 request max in parrallel with firefox and chrome
		    				var queries = [];
							for(var i = 0; i  < 6 && values.length > 0 ; i++){
								queries.push(this.getSaveRemoteRequest(values.splice(0, nbElementByBatch)));	    								
							}
							$q.all(queries).then(function(results){
								angular.forEach(results, function(result, key){
									if(result.status !== 200){
										console.log("Error for batch save");
									}else{
										angular.forEach(result.data, function(value, key){
											this.datatable.saveRemoteOneElement(value.status, value.data, value.index);	    									
	    								}, result.config);
									}
																									
								});
							});		    							
		    			},
		    			
		    			saveRemote : function(queries){
		    				$q.all(queries).then(function(results){
								angular.forEach(results, function(value, key){
									value.config.datatable.saveRemoteOneElement(value.status, value.data, value.config.index);																
								});				
							});				
		    			},
		    			
		    			saveRemoteOneElement : function(status, value, index){
		    				if(status !== 200){
								if(this.config.save.changeClass){
									this.displayResult[index].line.trClass = "danger";
		    					}
								this.displayResult[index].line.edit = true;
								this.addErrors(index,value);
								this.config.save.error++;
								this.config.save.number--;
								this.saveFinish();
							}else{
								this.resetErrors(index);
								this.saveLocal(value, index);
								this.saveFinish();
							}  				
		    			},
		    			
		    			getSaveRemoteRequest : function(value, i){
		    				var urlFunction = this.getUrlFunction(this.config.save.url);
		    				var method = this.config.save.method;
		    				if(angular.isFunction(method)){
		    					method = method(value);
		    				}
		    				if(urlFunction){
			    				if(this.config.save.batch){
			    					return $http[method](urlFunction(value), value, {datatable:this});
			    				}else{
			    					var valueFunction = this.getValueFunction(this.config.save.value);
			    					return $http[method](urlFunction(value), valueFunction(value), {datatable:this,index:i}).
			    					success(function(data, status, headers, config) {
			    						  config.datatable.saveRemoteOneElement(status, data, config.index);
			    					}).
			    					error(function(data, status, headers, config) {
			    						  config.datatable.saveRemoteOneElement(status, data, config.index);
			    					});				    				
			    				
			    				}
		    				}else{
		    					throw 'no url define for save !';
		    				}
		    			},
		    			
		    			/**
		    			 * Call after save to update the records property
		    			 */
		    			saveLocal: function(data, i){
		    				if(this.config.save.active){
		    					if(data){
		    						this.displayResult[i].data = data;
		    					}
		    					
		    					//update in the all result table
								var j = i;
								if(this.config.pagination.active && !this.isRemoteMode(this.config.pagination.mode)){
									j = i + (this.config.pagination.pageNumber*this.config.pagination.numberRecordsPerPage);
								}
								this.allResult[j] = angular.copy(this.displayResult[i].data);
		    					
		    					if(!this.config.save.keepEdit){
		    						this.displayResult[i].line.edit = undefined;
		    					}else{
		    						this.displayResult[i].line.edit = true;		    						
		    					}
			    				
								if(this.config.save.changeClass){
									this.displayResult[i].line.trClass = "success";
								}
								this.config.save.number--;
		    				}else{
		    					//console.log("save is not active !");		    				
		    				}
		    			},
		    			/**
		    			 * Call when a save local or remote is finish
		    			 */
		    			saveFinish: function(){
		    				if(this.config.save.number === 0){
		    					if(this.config.save.error > 0){
		    						this.config.messages.clazz = this.config.messages.errorClass;
		    						this.config.messages.text = this.config.messages.transformKey(this.config.messages.errorKey.save, this.config.save.error);
		    					}else{
		    						this.config.messages.clazz = this.config.messages.successClass;
		    						this.config.messages.text = this.config.messages.transformKey(this.config.messages.successKey.save);
		    					}
		    					
		    					if(angular.isFunction(this.config.save.callback)){
			    					this.config.save.callback(this, this.config.save.error);
			    				}
		    					if(!this.config.save.keepEdit && this.config.save.error === 0){
		    						this.config.edit.start = false;
		    					}
		    					this.config.save.error = 0;
		    					this.config.save.start = false;
		    					this.setSpinner(false);
		    					
		    				}
	    					
		    			},
		    			/**
		    			 * Test if save mode can be enable
		    			 */
		    			canSave: function(){
		    				if(this.config.edit.active && !this.config.save.withoutEdit && !this.config.save.start){
		    					return this.config.edit.start;
		    				}else if(this.config.edit.active && this.config.save.withoutEdit && !this.config.save.start){
		    					return true;
		    				}else{
		    					return false;
		    				}
		    			},
		    			//remove
		    			/**
		    			 *  Remove the selected table lines
		    			 */
		    			remove : function(){
		    				if(this.config.remove.active && !this.config.remove.start){
		    					if($window.confirm(Messages("datatable.remove.confirm"))){		  
		    						this.setSpinner(true);
			    					this.config.messages.text = undefined;
			    					this.config.messages.clazz = undefined;
			    					
			    					this.config.remove.counter = 0;
			    					this.config.remove.start = true;
			    					this.config.remove.number = 0;
			    					this.config.remove.error = 0;
			    					this.config.remove.ids = {errors:[],success:[]};
			    					
			    					for(var i = 0; i < this.displayResult.length; i++){
			    						if(this.displayResult[i].line.selected && (!this.displayResult[i].line.edit || this.config.remove.withEdit)){
				    						if(this.isRemoteMode(this.config.remove.mode)){
				    							this.config.remove.number++;
				    							this.removeRemote(this.displayResult[i].data, i);				    							
				    						}else{
				    							this.config.remove.ids.success.push(i);				    							
				    						}
				    					}						
				    				}
			    					if(!this.isRemoteMode(this.config.remove.mode)){
			    						this.removeFinish();
			    					}			    					
		    					}
		    				}else{
		    					//console.log("remove is not active !");		    				
		    				}
		    			},
		    			
		    			/**
		    			 * Call after save to update the records property
		    			 */
		    			removeLocal: function(i){
		    				if(this.config.remove.active && this.config.remove.start){
			    				//update in the all result table
								var j ;
								if(this.config.pagination.active && !this.isRemoteMode(this.config.pagination.mode)){
									j = (i + (this.config.pagination.pageNumber*this.config.pagination.numberRecordsPerPage)) - this.config.remove.counter;
								}else{
									j = i - this.config.remove.counter;
								}
								this.allResult.splice(j,1);
								this.config.remove.counter++;
								this.totalNumberRecords--;																						
		    				} else{
		    					//console.log("remove is not active !");		    				
		    				}
		    			},
		    			
		    			removeRemote : function(value, i){
		    				var url = this.getUrlFunction(this.config.remove.url);
			    			if(url){
			    				return $http['delete'](url(value), {datatable:this, index:i, value:value})
			    				.success(function(data, status, headers, config) {
			    					config.datatable.config.remove.ids.success.push(config.index);
			    					config.datatable.config.remove.number--;						    				
			    					config.datatable.removeFinish();
			    				})
			    				.error(function(data, status, headers, config) {
			    					config.datatable.config.remove.ids.errors.push(config.value);
			    					config.datatable.config.remove.error++;
			    					config.datatable.config.remove.number--;						    				
			    					config.datatable.removeFinish();
			    				});			    				
		    				}else{
		    					throw 'no url define for save !';
		    				}
		    			},
		    			
		    				    					    			
		    			/**
		    			 * Call when a remove is done
		    			 */
		    			removeFinish : function(){
		    				if(this.config.remove.number === 0){
		    					
		    					this.config.remove.ids.success.sort(function(a, b) {
		    						return a - b;
		    					}).forEach(function(i){
		    						this.removeLocal(i);
		    					}, this);
		    					
		    					
		    					if(this.config.remove.error > 0){
		    						this.config.messages.clazz = this.config.messages.errorClass;
		    						this.config.messages.text = this.config.messages.transformKey(this.config.messages.errorKey.remove, this.config.remove.error);
		    					}else{
		    						this.config.messages.clazz = this.config.messages.successClass;
		    						this.config.messages.text = this.config.messages.transformKey(this.config.messages.successKey.remove);
		    					}
		    					
		    					if(angular.isFunction(this.config.remove.callback)){
			    					this.config.remove.callback(this,this.config.remove.error);
			    				}	
		    					
		    					this.computePaginationList();
		    					this.computeDisplayResult();
		    					
		    					if(this.config.remove.ids.errors.length > 0){
			    					this.displayResult.every(function(value,index){			    						
			    						var errors = this.config.remove.ids.errors;
			    						for(var i = 0 ; i < errors.length ; i++){
			    							if(angular.equals(value.data,  errors[i])){
			    								value.line.trClass = "danger";
			    								errors.splice(i,1);
			    								break;
			    							}
			    						}
			    						if(errors.length === 0){
			    							return false;
			    						}else{
			    							return true;
			    						}
			    					},this);
		    					}
		    					
		    					
		    					this.config.select.isSelectAll = false;
		    					this.config.remove.error = 0;
		    					this.config.remove.start = false;
		    					this.config.remove.counter = 0;
		    					this.config.remove.ids = {error:[],success:[]};
		    					this.setSpinner(false);
		    				}
		    			},
		    			
		    			/**
		    			 * indicate if at least one line is selected and not in edit mode
		    			 */
		    			canRemove: function(){
		    				if(this.config.remove.active && !this.config.remove.start){
			    				for(var i = 0; this.displayResult && i < this.displayResult.length; i++){
		    						if(this.displayResult[i].line.selected && (!this.displayResult[i].line.edit || this.config.remove.withEdit))return true;	    						
		    					}
		    				}else{
		    					//console.log("remove is not active !");
		    					return false;
		    				}
		    			},
		    			//select
    					/**
		    			 * Select or unselect all line
		    			 * value = true or false
		    			 */
		    			selectAll : function(value){
		    				if(this.config.select.active){
			    				this.config.select.isSelectAll = value;
			    				for(var i = 0; i < this.displayResult.length; i++){
			    					if(value){
			    						if(!this.displayResult[i].line.group){
			    							this.displayResult[i].line.selected=true;
			    							this.displayResult[i].line.trClass="info";
			    						}else if(this.displayResult[i].line.group && this.config.group.enableLineSelection){
			    							this.displayResult[i].line.groupSelected=true;
			    							this.displayResult[i].line.trClass="info";
			    						}
			    						
			    						
			    					}else {
			    						if(!this.displayResult[i].line.group){
			    							this.displayResult[i].line.selected=false;
			    							this.displayResult[i].line.trClass=undefined;
			    						}else if(this.displayResult[i].line.group && this.config.group.enableLineSelection){
			    							this.displayResult[i].line.groupSelected=false;
			    							this.displayResult[i].line.trClass=undefined;
			    						}
			    						
			    					}
		    					}
		    				}else{
								//console.log("select is not active");
							}
		    			},	    			
		    			
		    			/**
		    			 * Return all selected element and unselect the data
		    			 */
		    			getSelection : function(unselect){
		    				var selection = [];
		    				for(var i = 0; i < this.displayResult.length; i++){
		    					if(this.displayResult[i].line.selected){
		    						//unselect selection
		    						if(unselect){
		    							this.displayResult[i].line.selected = false;
		    							this.displayResult[i].line.trClass=undefined;
		    						}
		    						selection.push(angular.copy(this.displayResult[i].data));
		    					}else if(this.displayResult[i].line.groupSelected){
		    						//unselect selection
		    						if(unselect){
		    							this.displayResult[i].line.groupSelected = false;
		    							this.displayResult[i].line.trClass=undefined;
		    						}
		    						selection.push(angular.copy(this.displayResult[i].data));
		    					}
		    				}
		    				if(unselect){this.config.select.isSelectAll = false;}
		    				return selection;
		    			},
		    			/**
		    			 * indicate if at least one line is selected
		    			 */
		    			isSelect: function(){
		    				for(var i = 0; this.displayResult && i < this.displayResult.length; i++){
	    						if(this.displayResult[i].line.selected)return true;	    						
	    					}
		    				return false;
		    			},
		    			isSelectGroup: function(){
		    				for(var i = 0; this.displayResult && i < this.displayResult.length; i++){
	    						if(this.displayResult[i].line.groupSelected)return true;	    						
	    					}
		    				return false;
		    			},
		    			/**
		    			 * cancel edit, hide and selected lines only
		    			 */
		    			cancel : function(){
		    				if(this.config.cancel.active){
			    				/*cancel only edit and hide mode */
			    				this.config.edit = angular.copy(this.configMaster.edit);
			    				this.config.hide = angular.copy(this.configMaster.hide);
			    				this.config.remove = angular.copy(this.configMaster.remove);
			    				this.config.select = angular.copy(this.configMaster.select);
			    				this.config.messages = angular.copy(this.configMaster.messages);
			    				this.computePaginationList();
			    				this.computeDisplayResult();
			    				
		    				}
		    			},
		    			
		    			//template helper functions		    			
		    			isShowToolbar: function(){
		    				return (this.isShowToolbarButtons() || this.isShowToolbarPagination() || this.isShowToolbarResults());
		    			},
		    			
		    			isShowToolbarButtons: function(){
		    				return ( this.isShowCRUDButtons()
		    						|| this.isShowHideButtons()  || (this.config.show.active && this.config.show.showButton)  
		    						|| this.isShowExportCSVButton()
		    						|| this.isShowOtherButtons());
		    			},
		    			isShowCRUDButtons: function(){
		    				return (  (this.config.edit.active && this.config.edit.showButton) 
		    						||  (this.config.save.active && this.config.save.showButton) || (this.config.remove.active && this.config.remove.showButton));
		    			},
		    			isShowHideButtons: function(){
		    				return (this.config.hide.active && this.config.hide.showButton && this.getHideColumns().length > 0);
		    			},
		    			isShowOtherButtons: function(){
		    				return (this.config.otherButtons.active && this.config.otherButtons.template !== undefined) ;
		    			},
		    			isShowToolbarPagination: function(){
		    				return this.config.pagination.active;
		    			},
		    			isShowPagination: function(){
		    				return (this.config.pagination.active && this.config.pagination.pageList.length > 0);
		    			},
		    			isShowToolbarResults: function(){
		    				return this.config.showTotalNumberRecords;
		    			},
		    			
		    			isCompactMode: function(){
		    				return this.config.compact;
		    			},
		    			
		    			isEmpty: function(){
		    				return (this.allResult === undefined || this.allResult === null || this.allResult.length === 0);
		    			},
		    			
		    			/**
		    			 * Function to show (or not) the "CSV Export" button
		    			 */ 
		    			isShowExportCSVButton: function(){
		    				return (this.config.exportCSV.active && this.config.exportCSV.showButton);
		    			},
		    			
		    			isShowButton: function(configParam, column){
		    				if(column){
		    					return (this.config[configParam].active && ((this.config[configParam].showButtonColumn !== undefined && this.config[configParam].showButtonColumn) || this.config[configParam].showButton) && column[configParam]);
		    				}else{
		    					return (this.config[configParam].active && this.config[configParam].showButton);
		    				}
		    			},
		    			
						setShowButton: function(configParam, value){
		    				if(this.config[configParam].active){
		    					this.config[configParam].showButton = value;
		    				}
		    			},
		    			
		    			/**
		    			 * Add pagination parameters if needed
		    			 */
		    			getParams : function(params){
		    				if(angular.isUndefined(params)){
	    						params = {};
	    					}
		    				params.datatable = true;
		    				if(this.config.pagination.active){
		    					params.paginationMode = this.config.pagination.mode;
		    					if(this.isRemoteMode(this.config.pagination.mode)){
		    						params.pageNumber = this.config.pagination.pageNumber;
			    					params.numberRecordsPerPage = this.config.pagination.numberRecordsPerPage;
		    					}
		    				}
		    				
		    				if(this.config.order.active && this.isRemoteMode(this.config.order.mode) && angular.isDefined(this.config.order.by)){
		    					params.orderBy = this.config.order.by.property;
		    					params.orderSense = (this.config.order.reverse)?"-1":"1";
		    				}
		    				return params;
		    			},
		    			/**
		    			 * Return an url from play js object or string
		    			 */
		    			getUrlFunction : function(url){
		    				if(angular.isObject(url)){
		    					if(angular.isDefined(url.url)){
		    						return function(value){return url.url};
		    					}
		    				}else if(angular.isString(url)){
		    					return function(value){return url};
		    				} else if(angular.isFunction(url)){
		    					return url;
		    				}
		    				return undefined;
		    			},
		    			/**
		    			 * Return a function to transform value if exist or the default mode
		    			 */
		    			getValueFunction : function(valueFunction){
		    				if(angular.isFunction(valueFunction)){
		    					return valueFunction;
		    				}
		    				return function(value){return value};
		    			},
		    			/**
		    			 * test is remote mode
		    			 */
		    			isRemoteMode : function(mode){
		    				if(mode && mode === 'remote'){
		    					return true;
		    				}else{
		    					return false;
		    				}
		    			},
		    			/**
		    			 * Set columns configuration
		    			 */
		    			setColumnsConfig: function(columns){
		    				if(angular.isDefined(columns)){
		    					var initPosition = 1000000;
		    					for(var i = 0 ; i < columns.length; i++){
		    						
		    						if(!columns[i].type || columns[i].type.toLowerCase() === "string"){
		    							columns[i].type = "text";
		    						}else{
		    							columns[i].type = columns[i].type.toLowerCase();
		    						}
		    						
		    						if(columns[i].type === "img" || columns[i].type === "image"){
		    							if(!columns[i].format)console.log("missing format for "+columns[i].property);
		    							if(!columns[i].width)columns[i].width='100%';
		    						}
		    						
			    					
			    					columns[i].id = this.generateColumnId();
			    					/*
			    					if(columns[i].hide && !this.config.hide.active){
			    						columns[i].hide = false;
			    					}
			    					if(columns[i].order && !this.config.order.active){
			    						columns[i].order = false;
			    					}
			    					if(columns[i].edit && !this.config.edit.active){
			    						columns[i].edit = false;
			    					}
			    					*/
			    					//TODO: else{Error here ?}
			    					
			    					if(columns[i].choiceInList && !angular.isDefined(columns[i].listStyle)){
			    						columns[i].listStyle = "select";
			    					}
			    					
			    					if(columns[i].choiceInList && !angular.isDefined(columns[i].possibleValues)){
			    						columns[i].possibleValues = [];
			    					}
			    					
			    					if(this.config.group.active && angular.isDefined(this.config.group.by) && (columns[i].property === this.config.group.by || columns[i].property === this.config.group.by.property)){
			    						this.config.group.by = columns[i];
			    						this.config.group.columns[columns[i].id] = true;
			    						columns[i].group = true;
			    					}else{
			    						this.config.group.columns[columns[i].id] = false;
			    					}
			    					if(this.config.order.active && angular.isDefined(this.config.order.by) && (columns[i].property === this.config.order.by || columns[i].property === this.config.order.by.property)){
			    						this.config.order.by = columns[i];
			    						this.config.order.columns[columns[i].id] = true;
			    						columns[i].order = true;
			    					}else{
			    						this.config.order.columns[columns[i].id] = false;
			    					}
			    					
			    					//ack to keep the default order in chrome
			    					if(null === columns[i].position || undefined === columns[i].position){
			    						columns[i].position = initPosition++;
			    					}
			    					
			    					if(columns[i].convertValue !== undefined && columns[i].convertValue.active === true && (columns[i].convertValue.displayMeasureValue === undefined || columns[i].convertValue.saveMeasureValue === undefined)){
			    						 throw "Columns config error: "+columns[i].property+" convertValue=active but convertValue.displayMeasureValue or convertValue.saveMeasureValue is missing";
			    					}
			    				}
		    					
		    					var settings = $.extend(true, [], this.configColumnDefault, columns);
		    					settings = $filter('orderBy')(settings, 'position');
		    					
		    					this.config.columns = angular.copy(settings);
			    	    		this.configMaster.columns = angular.copy(settings);
			    	    		this.newExtraHeaderConfig();
			    	    		if(this.allResult){
				    	    		this.computeGroup();
				    				this.sortAllResult();
				    				this.computePaginationList();
				    				this.computeDisplayResult();
		    					}
		    			    }
		    			},
		    			setColumnsConfigWithUrl : function(){
		    				$http.get(this.config.columnsUrl,{datatable:this}).success(function(data, status, headers, config) {		    						
	    						config.datatable.setColumnsConfig(data);
	    					});
		    			},
		    			getColumnsConfig: function(){
		    				return this.config.columns;		    				
		    			},
		    			
		    			getConfig: function(){
		    				return this.config;		    				
		    			},
		    			setConfig: function(config){
		    				var settings = $.extend(true, {}, this.configDefault, config);
		    	    		this.config = angular.copy(settings);
		    	    		this.configMaster = angular.copy(settings);
		    	    		if(this.config.columnsUrl){
		    					this.setColumnsConfigWithUrl();
		    				}else{
		    					this.setColumnsConfig(this.config.columns);
		    				}
		    	    		
		    	    		if(this.displayResult && this.displayResult.length > 0){
		    	    			this.computePaginationList();
		    	    			this.computeDisplayResult();		    	    			
		    	    		}
		    			},
		    			
		    			/**
		    			 * Return column with hide
		    			 */
		    			getHideColumns: function(){
		    				var c = [];
		    				for(var i = 0 ; i < this.config.columns.length; i++){
		    					if(this.config.columns[i].hide){
		    						c.push(this.config.columns[i]);
		    					}
		    				}
		    				return c;
		    			},
		    			
		    			/**
		    			 * Return column with group
		    			 */
		    			getGroupColumns: function(){
		    				var c = [];
		    				for(var i = 0; i < this.config.columns.length; i++){
		    					if(this.config.columns[i].group){
		    						c.push(this.config.columns[i]);
		    					}
		    				}
		    				return c;
		    			},
		    			
		    			/**
		    			 * Return column with edit
		    			 */
		    			getEditColumns: function(){
		    				var c = [];
		    				for(var i = 0 ; i < this.config.columns.length; i++){
		    					if(this.config.columns[i].edit)c.push(this.config.columns[i]);
		    				}
		    				return c;
		    			},
		    			generateColumnId : function(){
		    				this.inc++;
		    				return "p"+this.inc;
		    			},
		    			newColumn : function(header,property,edit, hide,order,type,choiceInList,possibleValues,extraHeaders){
		    				var column = {};
		    				column.id = this.generateColumnId();
		    				column.header = header;
		    				column.property = property;
		    				column.edit = edit;
		    				column.hide = hide;
		    				column.order = order;
		    				column.type = type;
		    				column.choiceInList = choiceInList;
		    				if(possibleValues!=undefined){
		    					column.possibleValues = possibleValues;
		    				}
		    				
		    				if(extraHeaders!=undefined){
		    					column.extraHeaders = extraHeaders;
		    				}
		    				
		    				return column;
		    			},
		    			/**
		    			 * Add a new column to the table with the <th>title</th>
		    			 * at position
		    			 */
		    			addColumn : function(position, column){
		    				if(position>=0){
			    				column.position = position;
		    				}
		    				
	    					this.config.columns.push(column);
		    				this.setColumnsConfig(this.config.columns);
		    				this.newExtraHeaderConfig();
		    			},
		    			/**
		    			 * Remove a column at position
		    			 */
		    			deleteColumn : function(position){
		    				this.config.columns.splice(position, 1);
		    				this.newExtraHeaderConfig();
		    			},
		    			addToExtraHeaderConfig:function(pos,header){
		    				if(!angular.isDefined(this.config.extraHeaders.list[pos])){
								this.config.extraHeaders.list[pos] = [];
							}
							this.config.extraHeaders.list[pos].push(header);
		    			},
		    			getExtraHeaderConfig : function(){
		    				return this.config.extraHeaders.list;
		    			},
		    			newExtraHeaderConfig : function(){
		    				if(this.config.extraHeaders.dynamic === true){
			    				this.config.extraHeaders.list = {};
			    				var lineUsed = false; // If we don't have label in a line, we don't want to show the line
			    				var count = 0;//Number of undefined extraHeader column beetween two defined ones
			    				//Every level of header
			    				for(var i=0;i<this.config.extraHeaders.number;i++){
			    					lineUsed = false;//re-init because new line
			    					var header = undefined;
			    					//Every column
				    				for(var j=0;j<this.config.columns.length;j++){
				    					if(!this.isHide(this.config.columns[j].id)){
				    					//if the column have a extra header for this level
				    						if(this.config.columns[j].extraHeaders != undefined && this.config.columns[j].extraHeaders[i] != undefined ){
				    							lineUsed = true;
				    							if(count>0){
				    								//adding the empty header of undefined extraHeader columns
				    								this.addToExtraHeaderConfig(i,{"label":"","colspan":count});
				    								count = 0;//Reset the count to 0
				    							}
				    							//The first time the header will be undefined
				    							if(header == undefined){	
				    								//create the new header with colspan 0 (the current column will be counted)
							    					header =  {"label":this.config.columns[j].extraHeaders[i],"colspan":0};
							    				}
				    							
				    							//if two near columns have the same header
				    							if(this.config.columns[j].extraHeaders[i] == header.label){
				    								header.colspan += 1;
				    							}else{
				    								//We have a new header
				    								//adding the current one
				    								this.addToExtraHeaderConfig(i, header);
				    								//and create the new one with colspan 1
				    								//colspan = 1 because we're already on the first column who have this header
				    								header =  {"label":this.config.columns[j].extraHeaders[i],"colspan":1};
				    							}
				    						
				    						}else if(header != undefined){
				    							lineUsed = true;
				    							//If we find a undefined column, we add the old header
				    							this.addToExtraHeaderConfig(i, header);
				    							//and increment the count var
				    							count++;
				    							//The old header is added
			    								header =  undefined;	
				    						}else{
				    							//No header to add, the previous one was a undefined column
				    							//increment the count var
				    							count++;
				    						}
				    					}
				    				}
				    				
				    				//At the end of the level loop
				    				//If we have undefined column left
				    				//And the line have at least one item
				    				if(count>0 && (lineUsed === true || header != undefined)){
				    					this.addToExtraHeaderConfig(i,{"label":"","colspan":count});
				    					count = 0;
				    				}
				    				
				    				//If we have defined column left
				    				if(header != undefined){
		    							this.addToExtraHeaderConfig(i, header);	
				    				}
			    				}
		    				}
		    			},
		    			

		    			/**
		    			 * Function to export data in a CSV file
		    			 */
		    			exportCSV : function(exportType) {
		    				if(this.config.exportCSV.active){
			    				this.config.exportCSV.start = true;
			    				var delimiter = this.config.exportCSV.delimiter, lineValue = "", colValue, that = this; 		    				
			    				
			    				//calcule results ( code extracted from method computeDisplayResult() )
			    				var displayResultTmp = [];
			    				angular.forEach(this.allResult, function(value, key){
			    					 var line = {edit:undefined, selected:undefined, trClass:undefined, group:false};
		    						 this.push({data:value, line:line});
		    					}, displayResultTmp);			    				
			    				if(this.isGroupActive()){
			    					displayResultTmp = this.addGroup(displayResultTmp);					
			    				}
			    				//manage results
			    				if (displayResultTmp) {
			    						    					
			    					var columnsToPrint = this.config.columns;
			    					//header
			    					columnsToPrint.forEach(function(column) {
			    						if(!that.config.hide.columns[column.id]){
			    							
			    							var header = column.header;
			    							if(angular.isFunction(header)){
			    								header = header();
			    							}else{
			    								header = Messages(column.header);
			    							}
			    							if(that.isGroupActive()){
				    							if(column.groupMethod === "sum"){
				    								header = header + Messages('datatable.export.sum'); 
				    							}else if(column.groupMethod === "average"){
				    								header = header + Messages('datatable.export.average');
				    							}else if(column.groupMethod === "unique"){
				    								header = header + Messages('datatable.export.unique');
				    							}else if(column.groupMethod === "countDistinct"){
				    								header = header + Messages('datatable.export.countDistinct');
				    							} 
			    							}
			    							lineValue = lineValue + header + delimiter;
			    							}
			    						}); 
			    					lineValue = lineValue.substr(0, lineValue.length-1) + "\n";
			    					//data
			    					displayResultTmp.forEach(function(result) {
			    						
			    						columnsToPrint.forEach(function(column) {
			    							if(!that.config.hide.columns[column.id]){
			    							//algo to set colValue (value of the column)
				    			    			if (!result.line.group && (column.url === undefined || column.url === null) && exportType !== 'groupsOnly') {
				    			    				var property = column.property;
				    			    				var isFunction = false;
				    			    				if(angular.isFunction(property)){
				    			    					property = property();
				    			    					isFunction = true;
				    			    				}
				    			    				property += that.getFilter(column);
				    			    				property += that.getFormatter(column);
				    			    				colValue = $parse(property)(result.data);
				    			    				
				    			    				if(colValue === null)colValue = undefined;
				    			    				
				    			    				if(colValue === undefined && isFunction === true){//Because the property here is not $parsable
				    			    					//The function have to return a $scope value
				    			    					colValue = property;
				    			    				}
				    			    				if(colValue !==  undefined && column.type === "number"){
				    			    					colValue = colValue.replace(/\u00a0/g,"");
				    			    				}
				    			    				if(colValue === undefined && column.type === "boolean"){
				    			    					colValue = Messages('datatable.export.no');
				    			    				}else if(colValue !== undefined && column.type === "boolean"){
				    			    					if(colValue){
				    			    						colValue = Messages('datatable.export.yes');
				    			    					}else{
				    			    						colValue = Messages('datatable.export.no')
				    			    					}
				    			    				}
					    							lineValue = lineValue + ((colValue!==null)&&(colValue)?colValue:"") + delimiter;
				    			    			} else if(result.line.group) {
				    			    				
				    			    				var v = $parse("group."+column.id)(result.data);
				    			    				//if error in group function
				    			    				if (angular.isDefined(v) && angular.isString(v) && v.charAt(0) === "#") {
				    			    					colValue = v;
				    			    				} else if(angular.isDefined(v)) {
				    			    					//not filtered properties because used during the compute
				    			    					colValue = $parse("group."+column.id+that.getFormatter(column))(result.data);
				    			    				} else {
				    			    					colValue =  undefined;
				    			    				}
				    			    				if(colValue === null)colValue = undefined;
				    			    				
				    			    				if(colValue !==  undefined && column.type === "number"){
				    			    					colValue = colValue.replace(/\u00a0/g,"");
				    			    				}				    			    				
				    			    				lineValue = lineValue + ((colValue!==null)&&(colValue)?colValue:"") + delimiter;
				    			    			}else if(!result.line.group && column.url !== undefined && column.url !== null  && exportType !== 'groupsOnly') {
				    			    				var url = $parse(column.url)(result.data);
				    			    				colValue = $parse(column.property+that.getFilter(column)+that.getFormatter(column))(that.urlCache[url]);
				    			    				if(colValue === null)colValue = undefined;
				    			    				if(colValue !==  undefined && column.type === "number"){
				    			    					colValue = colValue.replace(/\u00a0/g,"");
				    			    				}
				    			    				lineValue = lineValue + ((colValue!==null)&&(colValue)?colValue:"") + delimiter;
				    			    			}				    			    			
			    							}
			    						});
			    						if ((exportType==='all') || ((exportType==='groupsOnly') && result.line.group)) {
			    							lineValue = lineValue.substr(0, lineValue.length-1) + "\n";
			    						}
			    					});
			    					displayResultTmp = undefined;
			    					
			    					//fix for the accents in Excel : add BOM (byte-order-mark)
			    					var fixedstring = "\ufeff" + lineValue;		    							    					
			    					
			    					//save
			    					var blob = new Blob([fixedstring], {type: "text/plain;charset=utf-8"}); 
			    					var currdatetime = $filter('date')(new Date(), 'yyyyMMdd_HHmmss');
			    					var text_filename = (this.config.name || this.configDefault.name) + "_" + currdatetime;		    					
			    					saveAs(blob, text_filename + ".csv");
			    				}
			    				else {
			    					alert("No data to print. Select the data you need");
			    				}
			    				this.config.exportCSV.start = false;
		    				}
		    			},
		    			
		    			/**
		    			 * Sub-function use by (not only) exportCSV() 
		    			 */
	  		    		getFormatter : function(col){
		    				var format = "";
		    				if (col && col.type)
			    				if(col.type === "date"){
			    					format += " | date:'"+(col.format?col.format:Messages("date.format"))+"'";
			    				}else if(col.type === "datetime"){
			    					format += " | date:'"+(col.format?col.format:Messages("datetime.format"))+"'";
			    				}else if(col.type === "number"){
									format += " | number"+(col.format?':'+col.format:'');
								}    				
		    				return format;
		    			},
		    			
		    			getFilter : function(col){
		    				var filter = '';
	    					if(col.convertValue != undefined && col.convertValue.active === true && col.convertValue.saveMeasureValue != col.convertValue.displayMeasureValue){
	    						filter += '|convert:'+JSON.stringify(col.convertValue);
	    						
	    					}
		    				if(col.filter){
		    					return filter+'|'+col.filter;
		    				}
		    				return filter;
		    			},
		    			
		    			/**
		    			 * Function to enable/disable the "CSV Export" button 
		    			 */
		    			canExportCSV: function(){
		    				if(this.config.exportCSV.active && !this.config.exportCSV.start && !this.isEmpty()){
		    					if(this.config.edit.active && this.config.edit.start){
		    						return false;
		    					}else{
		    						return true;
		    					}
		    				} else {
		    					return false;
		    				}
		    			},
		    			onDrop : function(e, draggedCol, droppedCol, datatable, alReadyInTheModel){
		    				var posDrop = droppedCol.position;
		    				var posDrag = draggedCol.position;
		    				for(var i=0;i<datatable.config.columns.length;i++){
		    					if(posDrag < posDrop &&  datatable.config.columns[i].position > posDrag 
		    							&& datatable.config.columns[i].position < posDrop
		    							&& datatable.config.columns[i].id !== draggedCol.id){
		    						datatable.config.columns[i].position--;
		    					}
		    					
		    					if(posDrag > posDrop &&  datatable.config.columns[i].position > posDrop 
		    							&& datatable.config.columns[i].position < posDrag
		    							&& datatable.config.columns[i].id !== draggedCol.id){
		    						datatable.config.columns[i].position++;
		    					}
		    					
		    					if(datatable.config.columns[i].id === draggedCol.id){
		    						datatable.config.columns[i].position = posDrop-1;
		    					}
		    				}
		    				datatable.setColumnsConfig(datatable.config.columns);
		    			}
		    					    			
    			};
				
				if(arguments.length == 2){
					iConfig = arguments[1];
					console.log("used bad constructor for datatable, only one argument is required the config");
				}
				
				datatable.setConfig(iConfig);
    			
				return datatable;
    		}
    		return constructor;
    	}]).directive('datatable', ['$parse', '$q', '$timeout', function($parse, $q, $timeout){
    		return {
  		    	restrict: 'A',
  		    	replace:true,
  		    	//scope:{
  		    	//	dtTable:'=datatable'
  		    	//},
  		    	scope:true,
  		    	transclude:true,
  		    	template:'<div name="datatable" class="datatable">'
  		    		+'<div ng-transclude/>'
  		    		+'<div dt-toolbar ng-if="dtTable.isShowToolbar()"/>'  		    		
  		    		+'<div dt-messages ng-if="dtTable.config.messages.active"/>'
  		    		+'<div dt-table/>'
  		    		+'</div>',
  		    	link: function(scope, element, attr) {
  		    		if(!attr.datatable) return;
  		    		
  		    		scope.$watch(attr.datatable, function(newValue, oldValue) {
  		    			if(newValue && (newValue !== oldValue || !scope.dtTable)){
  		    				//console.log("new datatable")
  		    				scope.dtTable = $parse(attr.datatable)(scope);
  		    			}
		            });
  		    		
  		    		scope.dtTable = $parse(attr.datatable)(scope);
  		    		
  		    		if(!scope.dtTableFunctions){scope.dtTableFunctions = {};}
  		    		
  		    		
  		    		scope.dtTableFunctions.messagesDatatable = function(message,arg){
			    		if(typeof Messages == 'function'){
			    			if(angular.isFunction(message)){
			    				message = message();
			    			}
			    				
			    			if(arg==null || arg==undefined){
			    				return Messages(message);
			    			}else{
			    				return Messages(message,arg);
			    			}
			    		}
			    		
			    		return message;
			    	};
			    	
			    	scope.dtTableFunctions.cancel = function(){
		    			scope.dtTable.setSpinner(true);
		    			$timeout(function(){scope.dtTable.cancel()}).then(function(){
		    				scope.dtTable.setSpinner(false);  		    				
		    			});
		    			
		    					    			
		    		};
			    	
		    		scope.dtTableFunctions.setNumberRecordsPerPage = function(elt){
		    			scope.dtTable.setSpinner(true);
		    			$timeout(function(){scope.dtTable.setNumberRecordsPerPage(elt)}).then(function(){
		    				if(!scope.dtTable.isRemoteMode(scope.dtTable.config.pagination.mode)){
		    					scope.dtTable.setSpinner(false);  		    				
		    				}
		    			});
		    			
		    				    			
		    		};
		    		
		    		scope.dtTableFunctions.setPageNumber = function(page){
		    			scope.dtTable.setSpinner(true);
		    			$timeout(function(){scope.dtTable.setPageNumber(page)}).then(function(){
		    				if(!scope.dtTable.isRemoteMode(scope.dtTable.config.pagination.mode)){
		    					scope.dtTable.setSpinner(false);  		    				
		    				}	    				
		    			});		    			
		    		};
		    		
		    		scope.dtTableFunctions.setEdit = function(column){
		    			scope.dtTable.setSpinner(true);
		    			$timeout(function(){scope.dtTable.setEdit(column)}).then(function(){
		    				scope.dtTable.setSpinner(false);  		    				
		    			});		    			
		    		};
		    		
		    		scope.dtTableFunctions.setOrderColumn = function(column){
		    			scope.dtTable.setSpinner(true);
		    			$timeout(function(){scope.dtTable.setOrderColumn(column)}).then(function(){
		    				if(!scope.dtTable.isRemoteMode(scope.dtTable.config.order.mode)){
		    					scope.dtTable.setSpinner(false);  		    				
		    				} 		    				
		    			});	
		    			
		    		};
		    		
		    		scope.dtTableFunctions.setHideColumn = function(column){
		    			scope.dtTable.setSpinner(true);
		    			$timeout(function(){scope.dtTable.setHideColumn(column)}).then(function(){
		    				scope.dtTable.setSpinner(false);  		    				
		    			});
		    		};
		    		
		    		scope.dtTableFunctions.setGroupColumn = function(column){
		    			scope.dtTable.setSpinner(true);
		    			$timeout(function(){scope.dtTable.setGroupColumn(column)}).then(function(){
		    				scope.dtTable.setSpinner(false);  		    				
		    			});
		    		};			
		    		
		    		
		    		scope.dtTableFunctions.exportCSV = function(exportType){
		    			scope.dtTable.setSpinner(true);
		    			$timeout(function(){scope.dtTable.exportCSV(exportType)}).then(function(){
		    				scope.dtTable.setSpinner(false);  		    				
		    			});
		    		};
		    		
		    		scope.dtTableFunctions.updateShowOnlyGroups = function(){
		    			scope.dtTable.setSpinner(true);
		    			$timeout(function(){scope.dtTable.updateShowOnlyGroups()}).then(function(){
		    				scope.dtTable.setSpinner(false);  		    				
		    			});
		    		};
		    		
		    		scope.dtTableFunctions.getTotalNumberRecords = function(){
		    			if(scope.dtTable.config.group.active && scope.dtTable.config.group.start && !scope.dtTable.config.group.showOnlyGroups){
		    				return scope.dtTable.totalNumberRecords + " - "+scope.dtTable.allGroupResult.length;
		    			}else if(scope.dtTable.config.group.active && scope.dtTable.config.group.start && scope.dtTable.config.group.showOnlyGroups){
		    				return scope.dtTable.allGroupResult.length
		    			}else{
		    				return scope.dtTable.totalNumberRecords;
		    			}
		    			
		    			
		    		};
       		    } 		    		
    		};
    	}]).directive('dtForm', function(){
    		return {
    			restrict: 'A',
  		    	replace:true,
  		    	transclude:true,
  		    	template:'<div name="dt-form"  class="row"><div class="col-md-12 col-lg-12" ng-transclude/></div>',
  		    	link: function(scope, element, attr) {
  		    		//console.log("dtForm");
  		    	}
    		};
    	}).directive('dtToolbar', function(){ 
    		return {
    			restrict: 'A',
  		    	replace:true,
  		    	//transclude:true,
  		    	template:'<div name="dt-toolbar" class="row margin-bottom-3"><div class="col-md-12 col-lg-12">'
  		    		+'<div class="btn-toolbar pull-left" name="dt-toolbar-buttons" ng-if="dtTable.isShowToolbarButtons()">'
  		    		+'<div class="btn-group"  ng-switch on="dtTable.config.select.isSelectAll">'
  		    		+	'<button class="btn btn-default" ng-disabled="dtTable.isEmpty()" ng-click="dtTable.selectAll(true)" ng-show="dtTable.isShowButton(\'select\')" ng-switch-when="false" data-toggle="tooltip" title="{{dtTableFunctions.messagesDatatable(\'datatable.button.selectall\')}}">'
  		    		+		'<i class="fa fa-check-square"></i>'
  		    		+		'<span ng-if="!dtTable.isCompactMode()"> {{dtTableFunctions.messagesDatatable(\'datatable.button.selectall\')}}</span>'
  		    		+	'</button>'
  		    		+	'<button class="btn btn-default" ng-disabled="dtTable.isEmpty()" ng-click="dtTable.selectAll(false)" ng-show="dtTable.isShowButton(\'select\')" ng-switch-when="true" data-toggle="tooltip" title="{{dtTableFunctions.messagesDatatable(\'datatable.button.unselectall\')}}">'
  		    		+		'<i class="fa fa-square"></i>'
    				+		'<span ng-if="!dtTable.isCompactMode()"> {{dtTableFunctions.messagesDatatable(\'datatable.button.unselectall\')}}</span>'
  		    		+	'</button>'
  		    		+	'<button class="btn btn-default" ng-click="dtTableFunctions.cancel()"  ng-if="dtTable.isShowButton(\'cancel\')" data-toggle="tooltip" title="{{dtTableFunctions.messagesDatatable(\'datatable.button.cancel\')}}">'
  		    		+		'<i class="fa fa-undo"></i>'
  		    		+		'<span ng-if="!dtTable.isCompactMode()"> {{dtTableFunctions.messagesDatatable(\'datatable.button.cancel\')}}</span>'
  		    		+	'</button>'
  		    		+	'<button class="btn btn-default" ng-click="dtTable.show()" ng-disabled="!dtTable.isSelect()" ng-if="dtTable.isShowButton(\'show\')" data-toggle="tooltip" title="{{dtTableFunctions.messagesDatatable(\'datatable.button.show\')}}">'
  		    		+		'<i class="fa fa-thumb-tack"></i>'
  		    		+		'<span ng-if="!dtTable.isCompactMode()"> {{dtTableFunctions.messagesDatatable(\'datatable.button.show\')}}</span>'
  		    		+	'</button>'
  		    		+'</div>'
  		    		
  		    		+'<div class="btn-group" ng-if="dtTable.isShowCRUDButtons()">'
  		    		+	'<button class="btn btn-default" ng-click="dtTableFunctions.setEdit()" ng-disabled="!dtTable.canEdit()"  ng-if="dtTable.isShowButton(\'edit\')" data-toggle="tooltip" title="{{dtTableFunctions.messagesDatatable(\'datatable.button.edit\')}}">'
  		    		+		'<i class="fa fa-edit"></i>'
  		    		+		'<span ng-if="!dtTable.isCompactMode()"> {{dtTableFunctions.messagesDatatable(\'datatable.button.edit\')}}</span>'
  		    		+	'</button>'	
  		    		+	'<button class="btn btn-default" ng-click="dtTable.save()" ng-disabled="!dtTable.canSave()" ng-if="dtTable.isShowButton(\'save\')"  data-toggle="tooltip" title="{{dtTableFunctions.messagesDatatable(\'datatable.button.save\')}}" >'
  		    		+		'<i class="fa fa-save"></i>'
  		    		+		'<span ng-if="!dtTable.isCompactMode()"> {{dtTableFunctions.messagesDatatable(\'datatable.button.save\')}}</span>'
  		    		+	'</button>'	
  		    		+	'<button class="btn btn-default" ng-click="dtTable.remove()" ng-disabled="!dtTable.canRemove()" ng-if="dtTable.isShowButton(\'remove\')"  data-toggle="tooltip" title="{{dtTableFunctions.messagesDatatable(\'datatable.button.remove\')}}">'
  		    		+		'<i class="fa fa-trash-o"></i>'
  		    		+		'<span ng-if="!dtTable.isCompactMode()"> {{dtTableFunctions.messagesDatatable(\'datatable.button.remove\')}}</span>'
  		    		+	'</button>'  		    	
  		    		+'</div>'
  		    		
  		     		+'<div class="btn-group" ng-if="dtTable.isShowExportCSVButton()" ng-switch on="dtTable.config.group.active">'
  		     		
  		     		+'<button ng-switch-when="false" class="btn btn-default" ng-click="dtTableFunctions.exportCSV(\'all\')" ng-disabled="!dtTable.canExportCSV()" data-toggle="tooltip" title="{{dtTableFunctions.messagesDatatable(\'datatable.button.exportCSV\')}}">'
  		    		+	'<i class="fa fa-file-text-o"></i>'
  		    		+	'<span ng-if="!dtTable.isCompactMode()"> {{dtTableFunctions.messagesDatatable(\'datatable.button.basicExportCSV\')}}</span>'
  		    		+'</button>'	
  		    		
  		    		+'<button ng-switch-when="true" class="btn btn-default dropdown-toggle" data-toggle="dropdown" ng-disabled="!dtTable.canExportCSV()"  title="{{dtTableFunctions.messagesDatatable(\'datatable.button.exportCSV\')}}">'
  		    		+	'<i class="fa fa-file-text-o"></i> '
  		    		+	'<span ng-if="!dtTable.isCompactMode()"> {{dtTableFunctions.messagesDatatable(\'datatable.button.exportCSV\')}}</span>'
  		    		+	'<span class="caret"/>'
  		    		+'</button>'
  		    		+'<ul class="dropdown-menu">'
  		    		/* Basic Export */
  		    		+	'<li><a href="" ng-click="dtTableFunctions.exportCSV(\'all\')"><i class="fa fa-file-text-o"></i> {{dtTableFunctions.messagesDatatable(\'datatable.button.basicExportCSV\')}}</a></li>'
  		    		/* Grouped Export */
  		    		+	'<li><a href="" ng-click="dtTableFunctions.exportCSV(\'groupsOnly\')"><i class="fa fa-file-text-o"></i> {{dtTableFunctions.messagesDatatable(\'datatable.button.groupedExportCSV\')}}</a></li>'
  		    		+'</ul>'
  		    		
  		    		+'</div>'
  		    		
  		    		+'<div class="btn-group" ng-if="dtTable.isShowButton(\'group\')">'
  		    		+	'<button data-toggle="dropdown" class="btn btn-default dropdown-toggle" ng-disabled="dtTable.isEmpty()" data-toggle="tooltip" title="{{dtTableFunctions.messagesDatatable(\'datatable.button.group\')}}">'
  		    		+		'<i class="fa fa-bars"></i> '
  		    		+		'<span ng-if="!dtTable.isCompactMode()"> {{dtTableFunctions.messagesDatatable(\'datatable.button.group\')}} </span>'
  		    		+		'<span class="caret" />'
  		    		+	'</button>'
  		    		+	'<ul class="dropdown-menu">'
  		    		+		'<li ng-repeat="column in dtTable.getGroupColumns()">'
  		    		+			'<a href="" ng-click="dtTableFunctions.setGroupColumn(column)" ng-switch on="!dtTable.isGroup(column.id)"><i class="fa fa-bars" ng-switch-when="true"></i><i class="fa fa-outdent" ng-switch-when="false"></i> <span ng-bind="dtTableFunctions.messagesDatatable(column.header)"/></a>' 
  		    		+		'</li>'	
  		    		+		'<li class="divider"></li>'
  		    		+		'<li>'
  		    		+			'<a href="" ng-click="dtTable.setGroupColumn(\'all\')" ng-switch on="!dtTable.isGroup(\'all\')"><i class="fa fa-bars" ng-switch-when="true"></i><i class="fa fa-outdent" ng-switch-when="false"></i> <span ng-bind="dtTableFunctions.messagesDatatable(\'datatable.button.generalGroup\')"/></a>'
  		    		+		'</li>'
  		    		+		'<li class="dropdown-header" style="font-size:12px;color:#333">'
  		    		+			'<div class="checkbox"><label><input type="checkbox" ng-model="dtTable.config.group.showOnlyGroups" ng-click="dtTableFunctions.updateShowOnlyGroups()"/>{{dtTableFunctions.messagesDatatable(\'datatable.button.showOnlyGroups\')}}</label></div>'
  		    		+		'</li>'  		    		
  		    		+	'</ul>'
  		    		+'</div>'
		    		
  		    		+'<div class="btn-group" ng-if="dtTable.isShowHideButtons()">' //todo bt-select
  		    		+	'<button data-toggle="dropdown" class="btn btn-default dropdown-toggle" data-toggle="tooltip" title="{{dtTableFunctions.messagesDatatable(\'datatable.button.hide\')}}">'
  		    		+		'<i class="fa fa-eye-slash"></i> '
  		    		+		'<span ng-if="!dtTable.isCompactMode()"> {{dtTableFunctions.messagesDatatable(\'datatable.button.hide\')}} </span>'
  		    		+		'<span class="caret"></span>'  		    		
  		    		+	'</button>'
  		    		+	'<ul class="dropdown-menu">'
  		    		+		'<li ng-repeat="column in dtTable.getHideColumns()">'
  		    		+		'<a href="" ng-click="dtTableFunctions.setHideColumn(column)" ng-switch on="dtTable.isHide(column.id)"><i class="fa fa-eye" ng-switch-when="true"></i><i class="fa fa-eye-slash" ng-switch-when="false"></i> <span ng-bind="dtTableFunctions.messagesDatatable(column.header)"/></a>'
  		    		+		'</li>'
  		    		+	'</ul>'
  		    		+'</div>'
  		    		
  		    		+'<div class="btn-group" ng-if="dtTable.isShowOtherButtons()" dt-compile="dtTable.config.otherButtons.template"></div>'
  		    		+'</div>'
  		    		+'<div class="btn-toolbar pull-right" name="dt-toolbar-results"  ng-if="dtTable.isShowToolbarResults()">'
  		    		+	'<button class="btn btn-info" disabled="disabled" ng-if="dtTable.config.showTotalNumberRecords">{{dtTableFunctions.messagesDatatable(\'datatable.totalNumberRecords\', dtTableFunctions.getTotalNumberRecords())}}</button>'
  		    		+'</div>'
  		    		+'<div class="btn-toolbar pull-right" name="dt-toolbar-pagination"  ng-if="dtTable.isShowToolbarPagination()">'
  		    		+	'<div class="btn-group" ng-if="dtTable.isShowPagination()">'
  		    		+		'<ul class="pagination"><li ng-repeat="page in dtTable.config.pagination.pageList" ng-class="page.clazz"><a href="" ng-click="dtTableFunctions.setPageNumber(page);" ng-bind="page.label"></a></li></ul>'
  		    		+	'</div>'
  		    		+	'<div class="btn-group">'
  		    		+		'<button data-toggle="dropdown" class="btn btn-default dropdown-toggle">'
  		    		+		'{{dtTableFunctions.messagesDatatable(\'datatable.button.length\', dtTable.config.pagination.numberRecordsPerPage)}} <span class="caret"></span>'
  		    		+		'</button>'
  		    		+		'<ul class="dropdown-menu">'
  		    		+			'<li ng-repeat="elt in dtTable.config.pagination.numberRecordsPerPageList" class={{elt.clazz}}>'
  		    		+				'<a href="" ng-click="dtTableFunctions.setNumberRecordsPerPage(elt)">{{elt.number}}</a>' 
  		    		+			'</li>'
  		    		+		'</ul>'
  		    		+	'</div>'
  		    		+'</div>'  		    		  		    	
  		    		+'</div></div>'  		    		
  		    		,
  		    	link: function(scope, element, attr) {
  		    		//console.log("dtToolbar");
  		    		
  		    		
  		    	}
    		};
    	}).directive('dtMessages', function(){
    		return {
    			restrict: 'A',
  		    	replace:true,
  		    	template:
  		    		'<div name="dt-messages" class="row"><div class="col-md-12 col-lg-12">'
  		    		+'<div ng-class="dtTable.config.messages.clazz" ng-if="dtTable.config.messages.text !== undefined"><strong>{{dtTable.config.messages.text}}</strong>'
  		    		+'</div>'
  		    		+'</div></div>'
  		    		,
  		    	link: function(scope, element, attr) {
  		    		//console.log("dtMessages");
  		    	}
    		};
    	}).directive('dtTable', function(){
    		return {
    			restrict: 'A',
  		    	replace:true,
  		    	template:
  		    		'<div name="dt-table" class="row"><div class="col-md-12 col-lg-12">'
  		    		+'<div class="inProgress" ng-if="dtTable.config.spinner.start"><button class="btn btn-primary btn-lg"><i class="fa fa-spinner fa-spin fa-5x"></i></button></div>'
  		    		+'<form class="form-inline">'
  		    		+'<table class="table table-condensed table-hover table-bordered">'
  		    		+'<thead>'
  		    		+'<tr ng-repeat="(key,headers) in dtTable.getExtraHeaderConfig()">'
  		    		+	'<th colspan="{{header.colspan}}" ng-repeat="header in headers"><span ng-bind="dtTableFunctions.messagesDatatable(header.label)"/></th>'
  		    		+'</tr>'
  		    		+'<tr>'
  		    		+	'<th id="{{column.id}}" ng-repeat="column in dtTable.getColumnsConfig()" ng-model="column" draggable ng-if="!dtTable.isHide(column.id)">'
  		    		+	'<span ng-model="dtTable" droppable drop-fn="dtTable.onDrop" drop-item="column" ng-bind="dtTableFunctions.messagesDatatable(column.header)"/>'
  		    		+	'<div class="btn-group pull-right">'
  		    		+	'<button class="btn btn-xs" ng-click="dtTableFunctions.setEdit(column)"        ng-if="dtTable.isShowButton(\'edit\', column)"  ng-disabled="!dtTable.canEdit()" data-toggle="tooltip" title="{{dtTableFunctions.messagesDatatable(\'datatable.button.edit\')}}"><i class="fa fa-edit"></i></button>'
  		    		+	'<button class="btn btn-xs" ng-click="dtTableFunctions.setOrderColumn(column)" ng-if="dtTable.isShowButton(\'order\', column)" ng-disabled="!dtTable.canOrder()" data-toggle="tooltip" title="{{dtTableFunctions.messagesDatatable(\'datatable.button.sort\')}}"><i ng-class="dtTable.getOrderColumnClass(column.id)"></i></button>'
  		    		+	'<button class="btn btn-xs" ng-click="dtTableFunctions.setGroupColumn(column)" ng-if="dtTable.isShowButton(\'group\', column)" ng-disabled="dtTable.isEmpty()"  data-toggle="tooltip" title="{{dtTableFunctions.messagesDatatable(\'datatable.button.group\')}}"><i ng-class="dtTable.getGroupColumnClass(column.id)"></i></button>'  		    		  		    		
  		    		+	'<button class="btn btn-xs" ng-click="dtTableFunctions.setHideColumn(column)"  ng-if="dtTable.isShowButton(\'hide\', column)"  data-toggle="tooltip" title="{{dtTableFunctions.messagesDatatable(\'datatable.button.hide\')}}"><i class="fa fa-eye-slash"></i></button>'
  		    		+	'</div>'
  		    		+	'</th>'
  		    		+'</tr>'
  		    		+'</thead>'
  		    		+'<tbody>'
  		    		+	'<tr ng-if="dtTable.isEdit()" class="editParent">'
  		    		+		'<td ng-repeat="col in dtTable.config.columns" ng-if="!dtTable.isHide(col.id)">'
  		    		+			'<div dt-cell-header/>'
  		    		+		'</td>'
  		    		+	'</tr>'
  		    		+	'<tr ng-repeat="value in dtTable.displayResult" ng-click="dtTableFunctions.select(value.line)" ng-class="dtTableFunctions.getTrClass(value.data, value.line, this)">'
  		    		+		'<td ng-repeat="col in dtTable.config.columns" ng-if="!dtTable.isHide(col.id)" ng-class="dtTableFunctions.getTdClass(value.data, col, this)">'
  		    		+		'<div dt-cell/>'
  		    		+		'</td>'
  		    		+	'</tr>'
  		    		+'</tbody>'
  		    		+'</table>'
  		    		+'</form>'
  		    		+'</div></div>',
  		    	link: function(scope, element, attr) {
  		    		if(!scope.dtTableFunctions){scope.dtTableFunctions = {};}
  		    		
  		    		scope.dtTableFunctions.getTrClass = function(data, line, currentScope){
  		    			var dtTable = scope.dtTable;
	    				if(line.trClass){
	    					return line.trClass; 
	    				} else if(angular.isFunction(dtTable.config.lines.trClass)){
	    					return dtTable.config.lines.trClass(data, line);
	    				} else if(angular.isString(dtTable.config.lines.trClass)){
	    					return currentScope.$eval(dtTable.config.lines.trClass) || dtTable.config.lines.trClass;
	    				} else if(line.group && !dtTable.config.group.showOnlyGroups){
	    					return "active";
	    				} else{
	    					return '';
	    				}		    				
	    			};
	    			scope.dtTableFunctions.getTdClass = function(data, col, currentScope){
	    				if(angular.isFunction(col.tdClass)){
	    					return col.tdClass(data);
	    				} else if(angular.isString(col.tdClass)){
	    					//we try to evaluation the string against the scope
	    					return currentScope.$eval(col.tdClass) || col.tdClass;
	    				}else{
	    					return '';
	    				}
	    			};
	    		
	    			/**
					 * Select all the table line or just one
					 */
					scope.dtTableFunctions.select = function(line){
						var dtTable = scope.dtTable;
						if(dtTable.config.select.active){
		    				if(line){
		    					//separation of line type group and normal to simplify backward compatibility and avoid bugs
		    					//selected is used with edit, remove, save and show button
		    					if(!line.group){
			    					if(!line.selected){
			    						line.selected=true;
			    						line.trClass="info";
			    					} else{
										line.selected=false;
			    						line.trClass=undefined;
									}
		    					}else if(line.group && dtTable.config.group.enableLineSelection){
		    						if(!line.groupSelected){
			    						line.groupSelected=true;
			    						line.trClass="info";
			    					} else{
										line.groupSelected=false;
			    						line.trClass=undefined;
									}
		    					}
		    					
		    				}
						}else{
							//console.log("select is not active");
						}
	    			};
  		    	}
    		};
    	}).directive("dtCell", function(){
    		return {
    			restrict: 'A',
  		    	replace:true,
  		    	template:	
  		    		'<div>'
	  		    		+'<div ng-if="col.edit" dt-editable-cell></div>'
	  		    		+'<div ng-if="!col.edit" dt-cell-read></div>'		    		
  		    		+'</div>',
	    		link: function(scope, element, attr) {
  		    		//console.log("dtCell");
	    			if(!scope.dtTableFunctions){scope.dtTableFunctions = {};}
	    			
	    			scope.dtTableFunctions.getEditElement = function(col, header){
	    				var editElement = '';
	    				var ngChange = '"';
	    				var defaultValueDirective = "";
    			    	if(header){
    			    		ngChange = '" ng-change="dtTable.updateColumn(col.property, col.id)"';	    			    		
    			    	}else{
    			    		defaultValueDirective = 'dt-default-value="col.defaultValues"';
    			    	}
						var userDirectives = col.editDirectives;
						if(angular.isFunction(userDirectives)){
							userDirectives = userDirectives();
						}
	    						    				
	    				if(col.type === "boolean"){
	    					editElement = '<input class="form-control"' +defaultValueDirective+' dt-html-filter="{{col.type}}" '+userDirectives+' type="checkbox" class="input-small" ng-model="'+this.getEditProperty(col, header)+ngChange+'/>';
	    				}else if(!col.choiceInList){
							//TODO: type='text' because html5 autoformat return a string before that we can format the number ourself
	    					editElement = '<input class="form-control" '+defaultValueDirective+' '+this.getConvertDirective(col, header)+' dt-html-filter="{{col.type}}" '+userDirectives+' type="text" class="input-small" ng-model="'+this.getEditProperty(col, header)+ngChange+this.getDateTimestamp(col.type)+'/>';
	    				}else if(col.choiceInList){
	    					switch (col.listStyle) { 
	    						case "radio":
	    							editElement = '<label ng-repeat="opt in col.possibleValues" '+defaultValueDirective+'  for="radio{{col.id}}"><input id="radio{{col.id}}" dt-html-filter="{{col.type}}" '+userDirectives+' type="radio" ng-model="'+this.getEditProperty(col,hearder)+ngChange+' value="{{opt.name}}">{{opt.name}}<br></label>';
	    							break;		    						
	    						case "multiselect":
	    							editElement = '<select class="form-control" multiple="true" '+defaultValueDirective+' ng-options="opt.code as opt.name '+this.getGroupBy(col)+' for opt in '+this.getOptions(col)+'" '+userDirectives+' ng-model="'+this.getEditProperty(col,header)+ngChange+'></select>';
		    						break;
	    						case "bt-select":
	    							editElement = '<div class="form-control" bt-select '+defaultValueDirective+' placeholder="" bt-dropdown-class="dropdown-menu-right" bt-options="opt.code as opt.name  '+this.getGroupBy(col)+' for opt in '+this.getOptions(col)+'" '+userDirectives+' ng-model="'+this.getEditProperty(col,header)+ngChange+'></div>';			        		  	    	
	    							break;
								case "bt-select-filter":
	    							editElement = '<div class="form-control" filter="true" bt-select '+defaultValueDirective+' placeholder="" bt-dropdown-class="dropdown-menu-right" bt-options="opt.code as opt.name  '+this.getGroupBy(col)+' for opt in '+this.getOptions(col)+'" '+userDirectives+' ng-model="'+this.getEditProperty(col,header)+ngChange+'></div>';			        		  	    	
	    							break;
	    						case "bt-select-multiple":
	    							editElement = '<div class="form-control" '+defaultValueDirective+' bt-select multiple="true" bt-dropdown-class="dropdown-menu-right" placeholder="" bt-options="opt.code as opt.name  '+this.getGroupBy(col)+' for opt in '+this.getOptions(col)+'" '+userDirectives+' ng-model="'+this.getEditProperty(col,header)+ngChange+'></div>';			        		  	    	
	    							break;
	    						default:
	    							editElement = '<select class="form-control" '+defaultValueDirective+' ng-options="opt.code as opt.name '+this.getGroupBy(col)+' for opt in '+this.getOptions(col)+'" '+userDirectives+' ng-model="'+this.getEditProperty(col,header)+ngChange+'></select>';
		    						break;
		  	    			}		    					
	    				}else{
	    					editElement = "Edit Not Defined for col.type !";
	    				}		    						    				
	    				return '<div class="form-group" ng-class="{\'has-error\': value.line.errors[\''+col.property+'\'] !== undefined}">'+editElement+'<span class="help-block" ng-if="value.line.errors[\''+col.property+'\'] !== undefined">{{value.line.errors["'+col.property+'"]}}<br></span></div>';
	    			};
	    			
	    			
	    			scope.dtTableFunctions.getEditProperty = function(col, header){
	    				if(header){
    			    		return  "dtTable.config.edit.columns."+col.id+".value";
    			    	} else if(angular.isString(col.property)){
    			    		return "value.data."+col.property;        			    		
    			    	} else {
    			    		throw "Error property is not editable !";
    			    	}		    				
			    	};
			    	
			    	scope.dtTableFunctions.getConvertDirective = function(col, header){
			    		if(col.convertValue != undefined && col.convertValue.active === true && col.convertValue.saveMeasureValue != col.convertValue.displayMeasureValue){
			    			return 'convert-value="col.convertValue"';
			    		}
			    		return "";
			    	}
			    	
			    	scope.dtTableFunctions.getInputType = function(col){
	    				if(col.type === "date" || col.type === "datetime" || col.type === "datetime-local"){
    			    		return "text";
	    				}
	    				return col.type
			    	};
	    			
			    	scope.dtTableFunctions.getFormatter = scope.dtTable.getFormatter;
	    			
	    			scope.dtTableFunctions.getFilter = scope.dtTable.getFilter;
	    			
	    			scope.dtTableFunctions.getOptions = function(col){
	    				if(angular.isString(col.possibleValues)){
	    					return col.possibleValues;
	    				}else{ //function
	    					return 'col.possibleValues';
	    				}
	    			};
	    			
	    			scope.dtTableFunctions.getGroupBy = function(col){
	    				if(angular.isString(col.groupBy)){
	    					return 'group by opt.'+col.groupBy;
	    				}else{
	    					return '';
	    				}
	    					
	    			};
	    			
	    			scope.dtTableFunctions.getDateTimestamp = function(colType){
	    				if(colType==="date"){
	    					return 'dt-date-timestamp';
	    				}
	    				
	    				return '';
	    			};
  		    	}
    		};
    	}).directive("dtEditableCell", function(){
    		return {
    			restrict: 'A',
  		    	replace:true,
  		    	template:	
  		    		'<div ng-switch on="dtTable.isEdit(col.id, value.line)">'
	  		    		+'<div ng-switch-when="true" >'
	  		    		+	'<div dt-cell-edit></div>'  		    		
	  		    		+'</div>'
	  		    		+'<div ng-switch-default dt-cell-read></div>'
  		    		+'</div>',
	    		link: function(scope, element, attr) {
  		    		//console.log("dtEditableCell");  		    		
  		    	}
    		};
    	}).directive("dtCellHeader", function(){
    		return {
    			restrict: 'A',
  		    	replace:true,
  		    	template:	
  		    		'<div ng-if="col.edit" ng-switch on="dtTable.isEdit(col.id)">'  		    			
  		    		+	'<div ng-switch-when="true" dt-compile="dtTableFunctions.getEditElement(col, true)"></div><div ng-switch-default></div>'
  		    		+'</div>',
  		    	link: function(scope, element, attr) {
  	  		    	//console.log("dtCellHeader");  	
  		    		
  		    		
  	  		    }
    		};
    	}).directive("dtCellEdit", function(){
    		return {
    			restrict: 'A',
  		    	replace:true,
  		    	template:'<div dt-compile="dtTableFunctions.getEditElement(col)"></div>', 
  		    	link: function(scope, element, attr) {
  		    		//console.log("dtCellEdit")  		    		
  		    	}
  		    	
    		};
    	}).directive("dtCellRead", function($http){
    		return {
    			restrict: 'A',
  		    	replace:true,
  		    	template:'<div dt-compile="dtTableFunctions.getDisplayElement(col)"></div>'  ,
  		    	link: function(scope, element, attr) {
  		    		//console.log("dtCellRead");
  		    		if(!scope.dtTableFunctions){scope.dtTableFunctions = {};}
  		    		
  		    		scope.dtTableFunctions.getDisplayElement = function(col){
	    				if(angular.isDefined(col.render) && col.render !== null){
    						if(angular.isFunction(col.render)){
    							return '<span dt-compile="dtTable.config.columns[$index].render(value.data, value.line)"></span>';
    						}else if(angular.isString(col.render)){
    							return '<span dt-compile="dtTable.config.columns[$index].render"></span>';
    						}
	    				}else{
	    					if(col.type === "boolean"){
	    						return '<div ng-switch on="cellValue"><i ng-switch-when="true" class="fa fa-check-square-o"></i><i ng-switch-default class="fa fa-square-o"></i></div>';	    						
	    					}else if(col.type === "img" || col.type === "image"){
	    						if(!col.format)console.log("missing format for img !!");
	    						return '<img ng-src="data:image/'+col.format+';base64,{{cellValue}}" style="max-width:{{col.width}}"/>';		    					    
	    					} else{
	    						return '<span ng-bind="cellValue"></span>';
	    					}
	    				}	  
	    			};
	    			
	    			var getDisplayFunction = function(col, onlyProperty){
	    				if(angular.isFunction(col.property)){
    			    		return col.property(scope.value.data);
    			    	}else{
    			    		return getDisplayValue(col, scope.value, onlyProperty, scope);        			    		
    			    	}		    				
			    	};
	    			
			    	var getDisplayValue = function(column, value, onlyProperty, currentScope){
			    		if(onlyProperty){
			    			return currentScope.$eval(column.property, value.data);
			    		}else{
			    			if(!value.line.group && (column.url === undefined || column.url === null)){
			    				return currentScope.$eval(column.property+currentScope.dtTableFunctions.getFilter(column)+currentScope.dtTableFunctions.getFormatter(column), value.data);
			    			}else if(value.line.group){
			    				var v = currentScope.$eval("group."+column.id, value.data);
			    				//if error in group function
			    				if(angular.isDefined(v) && angular.isString(v) &&v.charAt(0) === "#"){
			    					return v;
			    				}else if(angular.isDefined(v) ){
			    					//not filtered properties because used during the compute
			    					return currentScope.$eval("group."+column.id+currentScope.dtTableFunctions.getFormatter(column), value.data);
			    				}else{
			    					return undefined;
			    				}			    							    				
			    			}else if(!value.line.group && column.url !== undefined && column.url !== null){
			    				var url = currentScope.$eval(column.url, value.data);
			    				return currentScope.$eval(column.property+currentScope.dtTableFunctions.getFilter(column)+currentScope.dtTableFunctions.getFormatter(column), scope.dtTable.urlCache[url]);			    				
			    			}
			    		}	    				
	    			};
	    			
	    			if(scope.col.type === "img" || scope.col.type === "image"){
	    				scope.cellValue = getDisplayFunction(scope.col, true);
	    			}else{
	    				scope.cellValue = getDisplayFunction(scope.col, false);
	    			}	    					    		
  		    	}
    		};
    	}).directive('dtCompile', function($compile) {
			// directive factory creates a link function
			return {
				restrict: 'A',
  		    	link: function(scope, element, attrs) {
  					//console.log("dtCompile");
  				    scope.$watch(
  				        function(scope) {
  				             // watch the 'compile' expression for changes
  				            return scope.$eval(attrs.dtCompile);
  				        },
  				        function(value) {
  				            // when the 'compile' expression changes
  				            // assign it into the current DOM
  				            element.html(value);

  				            // compile the new DOM and link it to the current
  				            // scope.
  				            // NOTE: we only compile .childNodes so that
  				            // we don't get into infinite loop compiling ourselves
  				            $compile(element.contents())(scope);
  				        }
  				    );
  				}
			};
						
		}).directive("dtHtmlFilter", function($filter) {
				return {
					  require: 'ngModel',
					  link: function(scope, element, attrs, ngModelController) {
						  //console.log("htmlFilter");
						/* ngModelController.$parsers.push(function(data) {
					      //view to model / same algo than model to view ?????
						   var convertedData = data;
					    	
					    	   if(attrs.dtHtmlFilter == "datetime"){
					    		   convertedData = $filter('date')(convertedData, Messages("datetime.format"));
					    	   }else if(attrs.dtHtmlFilter == "number"){
					    		   convertedData = $filter('number')(convertedData);
					    	   }
					    	
					    	   return convertedData;
					   	});*/

					    ngModelController.$formatters.push(function(data) {
					      //model to view / same algo than view to model ?????
					    	var convertedData = data;
					    	
					    	  if(attrs.dtHtmlFilter == "datetime"){
					    			convertedData = $filter('date')(convertedData, Messages("datetime.format"));
					    	   }else if(attrs.dtHtmlFilter == "date"){
					    		   	convertedData = $filter('date')(convertedData, Messages("date.format"));
					    	   }else if(attrs.dtHtmlFilter == "number"){
					    		   	convertedData = $filter('number')(convertedData);
					    	   }
					    	
					    	  return convertedData;
					    });   
					  }
					};
			}).directive('dtDateTimestamp', function() {
	            return {
	                require: 'ngModel',
	                link: function(scope, ele, attr, ngModel) {
						var typedDate = "01/01/1970";//Initialisation of the date
						
	                	var convertToDate = function(date){
	                		if(date !== null && date !== undefined && date !== ""){
		                		var format = Messages("date.format").toUpperCase();
		                		date = moment(date).format(format);
		                		return date;
	                		}
	                		return "";
	                	};
	                	
	                	var convertToTimestamp = function(date){
	                		if(date !== null && date !== undefined && date !== ""){
		                		var format = Messages("date.format").toUpperCase();
		    					return moment(date, format).valueOf();
	                		}
	                		return "";
	    				};
						
	                	//model to view
	                	scope.$watch(
							function(){
								return ngModel.$modelValue;
							}, function(newValue, oldValue){
								//We check if the
								if(newValue !== null && newValue !== undefined && newValue !== "" && typedDate.length === 10){
									var date = convertToDate(newValue);
	    							ngModel.$setViewValue(date);
									ngModel.$render();
								}
	                    });
						
	                	//view to model
	                    ngModel.$parsers.push(function(value) {
	                    	var date = value;
							typedDate = date;//The date of the user
	                    	if(value.length === 10){//When the date is complete
	                    		date = convertToTimestamp(value);
	                    	}
							return date;
	                    });
	                }
	            }
	          //Write in an input or select in a list element the value passed to the directive when the list or the input ngModel is undefined or empty
	        	//EXAMPLE: <input type="text" default-value="test" ng-model="x">
	        }).directive('dtDefaultValue',['$parse', function($parse) {
	    		return {
	    			require: 'ngModel',
	    			link: function(scope, element, attrs, ngModel) {
	    				var defaultValue = null;
	    				scope.$watch(attrs.dtDefaultValue, function(defaultValues){
	    					if(defaultValues != undefined){
	    						defaultValue = defaultValues;
	    					}
	    				});
	    				
						scope.$watch(ngModel, function(value){
				                if(defaultValue!= null && (ngModel.$modelValue == undefined || ngModel.$modelValue == "")){
									ngModel.$setViewValue(defaultValue);
									ngModel.$render();
								}
					    });
	    			}
	    		};	    	
	    	}]); 
