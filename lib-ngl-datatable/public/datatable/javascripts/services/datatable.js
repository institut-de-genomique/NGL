"use strict";

angular.module('datatableServices', []).
    	factory('datatable', ['$http','$filter','$parse','$compile', '$sce', '$window', function($http, $filter,$parse,$compile,$sce,$window){ //service to manage datatable
    		var constructor = function($scope, iConfig){
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
													"header":"Code Container", //the title
													"property":"code", //the property to bind
													"render" : function() //render the column
													"id":'', //the column id
													"edit":false, //can be edited or not
													"hide":true, //can be hidden or not
													"order":true, //can be ordered or not
													"type":"String"/"Number"/"Month"/"Week"/"Time"/"DateTime"/"Range"/"Color"/"Mail"/"Tel"/"Url"/"Date", //the column type
													"choiceInList":false, //when the column is in edit mode, the edition is a list of choices or not
													"listStyle":"select"/"radio", //if choiceInList=true, listStyle="select" is a select input, listStyle="radio" is a radio input
													"possibleValues":null, //The list of possible choices
													"extraHeaders":{"0":"Messages("experiments.inputs")"}, //the extraHeaders list
												  }*/
							columnsUrl:undefined, //Load columns config
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
								mode:'remote', //or local
								by : undefined,
								reverse : false,
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
							},
							save :{
								active:false,
								withoutEdit:false, //usable only for active/inactive save button by default !!!
								keepEdit:false, //keep in edit mode after safe
								showButton : true,
								changeClass : true, //change class to success or error
								mode:'remote', //or local
								url:undefined,
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
								error:0								
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
							otherButtons:{
								active:false
							},
							messages:{
								active:false,
								errorClass:'alert alert-error',
								successClass: 'alert alert-success',
								errorKey:{save:'datatable.msg.error.save',remove:'datatable.msg.error.remove'},
								successKey:{save:'datatable.msg.success.save',remove:'datatable.msg.success.remove'},
								text:undefined,
								clazz:undefined,
								transformKey : function(key, args){
									return Messages(key, args);
								}
							},
							showTotalNumberRecords:true,
							compact:true //mode compact pour le nom des bouttons
						},
						config:undefined,
    					configMaster:undefined,
    					allResult:undefined,
    					displayResult:undefined,
    					displayResultMaster:undefined,
    					totalNumberRecords:undefined,
    					lastSearchParams : undefined, //used with pagination when length or page change
    					inc:0, //used for unique column ids
    					configColumnDefault:{
								edit:false, //can be edited or not
								hide:true, //can be hidden or not
								order:true, //can be ordered or not
								type:"String", //the column type
								choiceInList:false, //when the column is in edit mode, the edition is a list of choices or not
								extraHeaders:{}
    					},
    					
    					//search functions
    					/**
		    			 * Search function to populate the datatable
		    			 */
		    			search : function(params){
		    				if(this.config.search.active && this.isRemoteMode(this.config.search.mode)){
			    				this.lastSearchParams = params;
			    				var url = this.getUrlFunction(this.config.search.url);
			    				if(url){
			    					$http.get(url(),{params:this.getParams(params), datatable:this}).success(function(data, status, headers, config) {		    						
			    						config.datatable.setData(data.data, data.recordsNumber);		    						
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
		    				this.search(this.lastSearchParams);
		    			},
		    			
		    			/**
		    			 * Set all data used by search method or directly when local data
		    			 */
		    			setData:function(data, recordsNumber){
		    				var configPagination = this.config.pagination;
		    				if(configPagination.active && !this.isRemoteMode(configPagination.mode)){
		    					this.config.pagination.pageNumber=0;
		    				}
		    				this.allResult = data;
		    				this.totalNumberRecords = recordsNumber;
		    				this.sortAllResult();
		    				this.computeDisplayResult();
		    				this.computePaginationList();
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
			    				this.sortAllResult();
			    				this.computeDisplayResult();
			    				this.computePaginationList();
			    			}
		    			},
		    			/**
		    			 * Selected only the records will be displayed.
		    			 * Based on pagination configuration
		    			 */
		    			computeDisplayResult: function(){
		    				//to manage local pagination
		    				var configPagination = this.config.pagination;
		    				if(configPagination.active && !this.isRemoteMode(configPagination.mode)){
		    					this.displayResult = angular.copy(this.allResult.slice((configPagination.pageNumber*configPagination.numberRecordsPerPage), 
		    							(configPagination.pageNumber*configPagination.numberRecordsPerPage+configPagination.numberRecordsPerPage)));
		    				}else{ //to manage all records or server pagination
		    					this.displayResult = angular.copy(this.allResult);		    					
		    				}
		    				
		    				if(this.config.edit.byDefault){
		    					this.config.edit.withoutSelect = true;
		    					this.setEdit();
		    				}
		    				
		    				this.displayResultMaster = angular.copy(this.displayResult);		    				
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
	    								this.computeDisplayResult();
	    								this.computePaginationList();
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
										this.computeDisplayResult();
										this.computePaginationList();
									}
	    						}
    						}else{
		    					//console.log("pagination is not active !!!");
		    				}
    					},
    					isShowPagination: function(){
		    				return (this.config.pagination.active && this.config.pagination.pageList.length > 0);
		    			},	
    					//order functions
    					/**
		    			 * Sort all result
		    			 */
		    			sortAllResult : function(){
		    				if(this.config.order.active && !this.isRemoteMode(this.config.order.mode)){
		    					this.allResult = $filter('orderBy')(this.allResult,this.config.order.by,this.config.order.reverse);		    					
		    				}
		    			},	
		    			/**
		    			 * set the order column name
		    			 * @param orderColumnName : column name
		    			 */
		    			setOrderColumn : function(columnPropertyName, columnId){
		    				if(this.config.order.active){
		    					if(this.config.order.by != columnPropertyName){
		    						this.config.order.by = columnPropertyName;
		    						this.config.order.reverse = false;
		    					}else{
		    						this.config.order.reverse = !this.config.order.reverse;
		    					}
		    					
		    					for(var i = 0; i < this.config.columns.length; i++){
		    						var fn = undefined;
		    						if(this.config.columns[i].id === columnId){
		    							fn = new Function("config", "config.order.columns."+this.config.columns[i].id+"=true;");
		    						}else{
		    							fn = new Function("config", "config.order.columns."+this.config.columns[i].id+"=false;");
		    						}
		    						fn(this.config);		    						
		    					}
		    					if(this.config.edit.active && this.config.edit.start){
    								//TODO add a warning popup
    								console.log("edit is active, you lost all modification !!");
    								this.config.edit = angular.copy(this.configMaster.edit); //reinit edit
    							}
		    					if(!this.isRemoteMode(this.config.order.mode)){
		    						this.sortAllResult(); //sort all the result
				    				this.computeDisplayResult(); //redefined the result must be displayed
			    				} else if(this.config.order.active){
			    					this.searchWithLastParams();
			    				}		    					
		    				} else{
		    					//console.log("order is not active !!!");
		    				}
		    			},
		    			getOrderColumnClass : function(columnId){
		    				if(this.config.order.active){
		    					var fn = new Function("config", 
		    							"if(!config.order.columns."+columnId+") {return 'fa fa-sort';}" +
			    						"else if(config.order.columns."+columnId+" && !config.order.reverse) {return 'fa fa-sort-up';}" +			    						
			    						"else if(config.order.columns."+columnId+" && config.order.reverse) {return 'fa fa-sort-down';}");
			    				return fn(this.config);			    						    					    					
		    				} else{
		    					//console.log("order is not active !!!");
		    				}
		    			},
		    			/**
		    			 * indicate if we can order the table
		    			 */
		    			canOrder: function(){
		    				return (this.config.edit.active ? !this.config.edit.start : this.config.order.active);
		    			},
		    			//show
		    			/**
		    			 * show one element
		    			 * work only with tab on the left
		    			 */
		    			show : function(){
		    				if(this.config.show.active && angular.isFunction(this.config.show.add)){
			    				for(var i = 0; i < this.displayResult.length; i++){
			    					if(this.displayResult[i].selected){
			    						this.config.show.add(this.displayResult[i]);
			    					}						
			    				}		    			
		    				}else{
		    					//console.log("show is not active !");
		    				}
		    			},
		    			//Hide a column
		    			/**
		    			 * set the hide column
		    			 * @param hideColumnName : column name
		    			 */
		    			setHideColumn : function(columnId){	
		    				if(this.config.hide.active){
			    				var fn = new Function("config", "if(!config.hide.columns."+columnId+")" +
			    						"{config.hide.columns."+columnId+"=true;} else {config.hide.columns."+columnId+"=false;}");
			    				fn(this.config);
			    				this.newExtraHeaderConfig();
		    				}else{
		    					//console.log("hide is not active !");
		    				}
		    				
		    			},
		    			/**
		    			 * Test if a column must be hide
		    			 * @param columnId : column id 
		    			 */
		    			isHide : function(columnId){
		    				if(this.config.hide.active){
				    			var fn = new Function("config", "if(config.hide.columns."+columnId+") return config.hide.columns."+columnId+";else return false;");
				    			return fn(this.config);
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
		    			setEdit : function(columnId){	
		    				if(this.config.edit.active){
		    					this.config.edit.columns = {};
			    				var find = false;
			    				for(var i = 0; i < this.displayResult.length; i++){
			    					
			    					if(this.displayResult[i].selected || this.config.edit.withoutSelect){
			    						this.displayResult[i].edit=true;			    						
			    						find = true;			    					
			    					}else{
			    						this.displayResult[i].edit=false;
			    					}			    					   					
			    				}
			    				this.selectAll(false);
			    				if(find){
			    					this.config.edit.start = true;			
			    					if(columnId){  
			    						(new Function("config","if(angular.isUndefined(config.edit.columns."+columnId+"))config.edit.columns."+columnId+"={}"))(this.config);			    						
			    						(new Function("config","config.edit.columns."+columnId+".edit=true"))(this.config);
			    					}
			    					else this.config.edit.all = true;
			    				}
		    				}else{
		    					//console.log("edit is not active !");
		    				}
		    			},
		    			/**
		    			 * set Edit all column or just one
		    			 * @param editColumnName : column name
		    			 * @deprecated
		    			 */
		    			setEditColumn : function(columnId){	
		    				this.setEdit(columnId);
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
			    					(new Function("config","if(angular.isUndefined(config.edit.columns."+columnId+"))config.edit.columns."+columnId+"={}"))(this.config);			    								    							    					
			    					var columnEdit = (new Function("config","return config.edit.columns."+columnId+".edit"))(this.config);
			    					isEdit = (line.edit && columnEdit) || (line.edit && this.config.edit.all);
			    				}else if(columnId){
			    					(new Function("config","if(angular.isUndefined(config.edit.columns."+columnId+"))config.edit.columns."+columnId+"={}"))(this.config);			    								    								    					
			    					var columnEdit = (new Function("config","return config.edit.columns."+columnId+".edit"))(this.config);			    					
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
			    					if(this.displayResult[i].edit){
										getter.assign(this.displayResult[i],this.config.edit.columns[columnId].value);
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
		    					for(var i = 0; i < this.displayResult.length; i++){
			    					if(this.displayResult[i].edit || this.config.save.withoutEdit){
			    						//remove datatable properties to avoid this data are retrieve in the json
			    						this.config.save.number++;
			    						this.displayResult[i].trClass = undefined;
				    					this.displayResult[i].selected = undefined;
				    					//this.displayResult[i].edit = undefined;
				    					
			    						if(this.isRemoteMode(this.config.save.mode)){
			    							this.saveRemote(this.displayResult[i], i);
			    						} else{	
			    							this.saveLocal(this.displayResult[i],i);
			    						}
			    					}						
			    				}
		    					
		    					if(!this.isRemoteMode(this.config.save.mode)){
	    							this.saveFinish();
	    						}
		    				}else{
		    					//console.log("save is not active !");		    				
		    				}
		    			},
		    			saveRemote : function(value, i){
		    				var urlFunction = this.getUrlFunction(this.config.save.url);
		    				if(urlFunction){
		    					var valueFunction = this.getValueFunction(this.config.save.value);				    			
			    				//call url
		    					//to avoid to send edit to the server but without change the datatable
		    					value = angular.copy(value);
		    					value.edit = undefined;
			    				$http[this.config.save.method](urlFunction(value), valueFunction(value), {datatable:this,index:i})
				    				.success(function(data, status, headers, config) {
				    					config.datatable.saveLocal(data, config.index);
				    					config.datatable.saveFinish();
				    				})
				    				.error(function(data, status, headers, config) {
				    					if(config.datatable.config.save.changeClass){
				    						config.datatable.displayResult[config.index].trClass = "error";
				    					}
				    					config.datatable.displayResult[config.index].edit = true;
				    					config.datatable.config.save.error++;
				    					config.datatable.config.save.number--;
				    					config.datatable.saveFinish();
				    					//TODO add error messages as in datatable angular.element
				    				});
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
		    						this.displayResult[i] = data;
		    					}
		    					
		    					//update in the all result table
								var j = i;
								if(this.config.pagination.active && !this.isRemoteMode(this.config.pagination.mode)){
									j = i + (this.config.pagination.pageNumber*this.config.pagination.numberRecordsPerPage);
								}
								this.allResult[j] = angular.copy(this.displayResult[i]);
		    					
		    					if(!this.config.save.keepEdit){
		    						this.config.edit.start = false;
		    						this.displayResult[i].edit = undefined;
		    					}else{
		    						this.displayResult[i].edit = true;
		    					}
			    				
								if(this.config.save.changeClass){
									this.displayResult[i].trClass = "success";
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
		    					
		    					this.config.save.error = 0;
		    					this.config.save.start = false;		    					
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
		    					var r= $window.confirm(Messages("datatable.remove.confirm"));
		    					if(r){		    					
			    					var localDisplayResult = angular.copy(this.displayResult);
			    					this.config.remove.counter = 0;
			    					this.config.remove.start = true;
			    					this.config.remove.number = 0;
			    					this.config.remove.error = 0;
			    					for(var i = 0; i < localDisplayResult.length; i++){
				    					if(localDisplayResult[i].selected && (!localDisplayResult[i].edit || this.config.remove.withEdit)){
				    						this.config.remove.number++;
				    						this.removeLocal(i);			    										    						
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
									j = i - this.config.remove.counter
								}
								var removeArray = this.allResult.splice(j,1);
								this.displayResult.splice((i - this.config.remove.counter),1);
								this.config.remove.counter++;
								this.totalNumberRecords--;
								
								if(this.isRemoteMode(this.config.remove.mode)){
	    							this.removeRemote(removeArray[0]);
	    						}else{
	    							this.config.remove.number--;	    		    				
	    						}
								
		    				} else{
		    					//console.log("remove is not active !");		    				
		    				}
		    			},
		    			
		    			removeRemote : function(value){
		    				if(this.config.remove.active && this.config.remove.start){
			    				var url = this.getUrlFunction(this.config.remove.url);
				    			if(url){
				    				$http['delete'](url(value), {datatable:this,value:value})
					    				.success(function(data, status, headers, config) {
					    					config.datatable.config.remove.number--;						    				
					    					config.datatable.removeFinish();
					    				})
					    				.error(function(data, status, headers, config) {
					    					config.datatable.config.remove.error++;
					    					config.datatable.config.remove.number--;						    				
					    					config.datatable.removeFinish();
					    				});
			    				}else{
			    					throw  'no url define for remove ! ';
			    				}
		    				} else{
		    					//console.log("remove is not active !");		    				
		    				}		    				
		    			},
		    			/**
		    			 * Call when a remove is done
		    			 */
		    			removeFinish : function(){
		    				if(this.config.remove.number === 0){
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
		    					this.config.remove.error = 0;
		    					this.config.remove.start = false;
		    					this.config.remove.counter = 0;
		    				}
		    			},
		    			
		    			/**
		    			 * indicate if at least one line is selected and not in edit mode
		    			 */
		    			canRemove: function(){
		    				if(this.config.remove.active && !this.config.remove.start){
			    				for(var i = 0; this.displayResult && i < this.displayResult.length; i++){
		    						if(this.displayResult[i].selected && (!this.displayResult[i].edit || this.config.remove.withEdit))return true;	    						
		    					}
		    				}else{
		    					//console.log("remove is not active !");
		    					return false;
		    				}
		    			},
		    			//select
    					/**
    					 * Select all the table line or just one
    					 */
						select : function(line){
							if(this.config.select.active){
			    				if(line){
			    					if(!line.selected){
			    						line.selected=true;
			    						line.trClass="info";
			    					}
									else{
										line.selected=false;
			    						line.trClass=undefined;
									}
			    				}
							}else{
								//console.log("select is not active");
							}
		    			},
		    			/**
		    			 * Select or unselect all line
		    			 */
		    			selectAll : function(value){
		    				if(this.config.select.active){
			    				this.config.select.isSelectAll = value;
			    				for(var i = 0; i < this.displayResult.length; i++){
			    					if(value){
			    						this.displayResult[i].selected=true;
			    						this.displayResult[i].trClass="info";
			    					}else{
			    						this.displayResult[i].selected=false;
			    						this.displayResult[i].trClass=undefined;
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
		    					if(this.displayResult[i].selected){
		    						//unselect selection
		    						if(unselect){
		    							this.displayResult[i].selected = false;
		    							this.displayResult[i].trClass=undefined;
		    						}
		    						selection.push(angular.copy(this.displayResult[i]));
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
	    						if(this.displayResult[i].selected)return true;	    						
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
			    				
			    				this.computeDisplayResult();
			    				this.computePaginationList();
		    				}
		    			},
		    			
		    			//helper functions		    			
		    			isShowToolbar: function(){
		    				return (this.isShowToolbarLeft() || this.isShowToolbarRight());
		    			},
		    			isShowToolbarLeft: function(){
		    				return (  (this.config.edit.active && this.config.edit.showButton) 
		    						||  (this.config.save.active && this.config.save.showButton) || (this.config.remove.active && this.config.remove.showButton) 
		    						|| this.config.hide.active  || (this.config.show.active && this.config.show.showButton) 
		    						|| this.config.otherButtons.active);
		    			},
		    			isShowToolbarRight: function(){
		    				return (this.isShowPagination() || this.config.showTotalNumberRecords);
		    			},
		    			showButton: function(configParam){
		    				return (this.config[configParam].active && this.config[configParam].showButton);
		    			},
		    			/**
		    			 * Add pagination parameters if needed
		    			 */
		    			getParams : function(params){
		    				if(angular.isUndefined(params)){
	    						params = {};
	    					}
		    				params.datatable = true;
		    				if(this.config.pagination.active && this.isRemoteMode(this.config.pagination.mode)){
		    					params.pageNumber = this.config.pagination.pageNumber;
		    					params.numberRecordsPerPage = this.config.pagination.numberRecordsPerPage;		    					
		    				}
		    				if(this.config.order.active && this.isRemoteMode(this.config.order.mode)){
		    					params.orderBy = this.config.order.by;
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
		    					for(var i = 0 ; i < columns.length; i++){
			    					if(columns[i].id === null){
			    						columns[i].id = this.generateColumnId();
			    					}
			    					if(columns[i].hide && !this.config.hide.active){
			    						columns[i].hide = false;
			    					}
			    					if(columns[i].order && !this.config.order.active){
			    						columns[i].order = false;
			    					}
			    					if(columns[i].edit && !this.config.edit.active){
			    						columns[i].edit = false;
			    					}
			    					
			    					if(columns[i].choiceInList && !angular.isDefined(columns[i].listStyle)){
			    						columns[i].listStyle = "select";
			    					}
			    					
			    					if(columns[i].choiceInList && !angular.isDefined(columns[i].possibleValues)){
			    						columns[i].possibleValues = [];
			    					}
			    					
			    					columns[i].cells = [];//Init
			    				}
		    					
		    					var settings = $.extend(true, [], this.configColumnDefault, columns);
			    	    		this.config.columns = angular.copy(settings);
			    	    		this.configMaster.columns = angular.copy(settings);
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
		    			getColumn : function(props){
		    				if(angular.isDefined(this.config.columns)){
			    				for(var i = 0 ; i < this.config.columns.length; i++){
			    					if(angular.isDefined(props.property) && this.config.columns[i].property === props.property){
			    						return this.config.columns[i];
			    					}
			    				}
		    				}
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
		    	    		this.newExtraHeaderConfig();
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
		    			 * Return column with hide
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
		    					this.config.columns.splice(position,0,column);
		    				}else{
		    					this.config.columns.push(column);
		    				}
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
			    				var count = 0;//Number of undefined extraHeader column beetween two defined ones
			    				//Every level of header
			    				for(var i=0;i<this.config.extraHeaders.number;i++){
			    					var header = undefined;
			    					//Every column
				    				for(var j=0;j<this.config.columns.length;j++){
				    					if(!this.isHide(this.config.columns[j].id)){
				    					//if the column have a extra header for this level
				    						if(this.config.columns[j].extraHeaders != undefined && this.config.columns[j].extraHeaders[i] != undefined ){
				    							if(count>0){
				    								//adding the empty header of undefined extraHeader columns
				    								this.addToExtraHeaderConfig(i,{"label":"","colspan":count});
				    								count = 0;//Reset the count to 0
				    							}
				    							//The first time the header will be undefined
				    							if(header === undefined){	
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
				    				if(count>0){
				    					this.addToExtraHeaderConfig(i,{"label":"","colspan":count});
				    					count = 0;
				    				}
				    				//If we have defined column left
				    				if(header != undefined){
		    							this.addToExtraHeaderConfig(i, header);	
				    				}	
			    				}
		    				}
		    			}
    			};
				
				datatable.setConfig(iConfig);
    			
				return datatable;
    		}
    		return constructor;
    	}]).directive("datatable", function($compile) {
  		  return {
			    restrict: 'A',
			    link: function(scope, element, attrs) {
			    	var config = {}; 
			    	var buttonsConfig = ""; //add other button by adding a <div id="datatableButtonsConfiguration"> your button </div>
			    	var formConfig = ""; //add form on top by adding a <div id="datatableFormConfiguration"> your form </div>

			    	//get the buttons and form in the divs
			    	var getOtherConfig = function(){
			    		var divs = angular.element(element).children("div");
			    		for(var i=0;i<divs.length;i++){
			    			if(divs[i].id === "datatableFormConfiguration"){
			    				formConfig += divs[i].innerHTML;
			    			}
			    			if(divs[i].id === "datatableButtonsConfiguration"){
			    				buttonsConfig += divs[i].innerHTML;
			    			}
			    		}
			    	};

			    		
			    	var MessagesDatatable = function(message,arg){
			    		if(typeof Messages == 'function'){
			    			if(arg==null || arg==undefined){
			    				return Messages(message);
			    			}else{
			    				return Messages(message,arg);
			    			}
			    		}
			    		
			    		return message;
			    	};
			    	
			    	
			    	var setConfig = function(newConfig){
			    		config = newConfig;
			    	};
			    	
			    	//generate the datatable
			    	var generateDatatable = function(){
			    		getOtherConfig();//Loading divs for other buttons and forms
			    		//Form Section
			    		//var formDiv = angular.element('<div class="datatable" ng-init="'+config.name+'.setColumnsConfig('+config+')"></div>');
			    		
			    		
			    		//Buttons Section
			    		var toolbar = angular.element('<div class="row" ng-show="'+config.name+'.isShowToolbar()"></div>');
			    		var divButtons = angular.element('<div class="col-lg-8"></div>');
			    		
		    			var buttons = angular.element('<div class="btn-toolbar pull-left" ng-show="'+config.name+'.isShowToolbarLeft()"></div>  ');
		    			var buttonsSelect = '<div class="btn-group" ng-switch on="'+config.name+'.config.select.isSelectAll"><button class="btn btn-default" ng-click="'+config.name+'.selectAll(true)"  ng-show="'+config.name+'.showButton(\'select\')"  ng-switch-when="false" data-toggle="tooltip" title="'+MessagesDatatable("datatable.button.selectall")+'"><i class="fa fa-check-square-o"></i>';
		    			
		    			if(!config.compact){ 
		    				buttonsSelect+=  " "+MessagesDatatable("datatable.button.selectall");
		    			}
		    			
		    			buttonsSelect += '</button><button class="btn btn-default" ng-click="'+config.name+'.selectAll(false)" ng-show="'+config.name+'.showButton(\'select\')"  ng-switch-when="true" data-toggle="tooltip" title="'+MessagesDatatable("datatable.button.unselectall")+'"><i class="fa fa-square-o"></i>';
		    			
		    			if(!config.compact){
		    				buttonsSelect +=  " "+MessagesDatatable("datatable.button.unselectall");
		    			}
		    			
		    			buttonsSelect += '</button><button class="btn btn-default" ng-click="'+config.name+'.cancel()"  ng-show="'+config.name+'.showButton(\'cancel\')" data-toggle="tooltip" title="'+MessagesDatatable("datatable.button.cancel")+'"><i class="fa fa-undo"></i>';
		    			if(!config.compact){ 
		    				buttonsSelect +=  " "+MessagesDatatable("datatable.button.cancel");
		    			}
		    			buttonsSelect += '</button></div>';
		    			angular.element(divButtons).append(buttons);
		    			angular.element(buttons).append(buttonsSelect);
		    			
		    			if(config.show){
		    				var buttonsShow = '<button class="btn btn-default" ng-click="'+config.name+'.show()" ng-disabled="!'+config.name+'.isSelect()" ng-show="'+config.name+'.showButton(\'show\')" data-toggle="tooltip" title="'+MessagesDatatable("datatable.button.show")+'"><i class="fa fa-thumb-tack"></i>';
		    				if(!config.compact){
		    					buttonsShow +=   " "+MessagesDatatable("datatable.button.show");
		    				}
		    				buttonsShow += '</button>';
		    				angular.element(buttons).append(buttonsShow);
		    			}
		    			
		    			if(config.edit || config.remove || config.save){	
		    				var divCrud = angular.element('<div class="btn-group"></div>');
		    					if(config.edit.active){
		    						
		    						var buttonEdit = '<button class="btn btn-default" ng-click="'+config.name+'.setEditColumn()" ng-disabled="!'+config.name+'.canEdit()"  ng-show="'+config.name+'.showButton(\'edit\')" data-toggle="tooltip" title="'+MessagesDatatable("datatable.button.edit")+'"><i class="fa fa-edit"></i>';
		    						if(!config.compact){ 
		    							buttonEdit += " "+MessagesDatatable("datatable.button.edit");
		    						}
		    						
		    						buttonEdit += '</button>';

		    						angular.element(divCrud).append(buttonEdit);
		    					}
		    					
		    					if(config.save.active){
		    						var buttonSave = '<button class="btn btn-default" ng-click="'+config.name+'.save()" ng-disabled="!'+config.name+'.canSave()" ng-show="'+config.name+'.showButton(\'save\')"  data-toggle="tooltip" title="'+MessagesDatatable("datatable.button.save")+'" ><i class="fa fa-save"></i>';
		    						
		    						if(!config.compact){ 
		    							buttonSave+=  " "+MessagesDatatable("datatable.button.save");
		    							}
		    						buttonSave+= '</button>';
		    						angular.element(divCrud).append(buttonSave);
		    					}
		    					
		    					if(config.remove.active){
		    						var buttonRemove = '<button class="btn btn-default" ng-click="'+config.name+'.remove()" ng-disabled="!'+config.name+'.canRemove()" ng-show="'+config.name+'.showButton(\'remove\')"  data-toggle="tooltip" title="'+MessagesDatatable("datatable.button.remove")+'"><i class="fa fa-trash-o"></i>';
		    						if(!config.compact){ 
		    							buttonRemove +=  " "+MessagesDatatable("datatable.button.remove");
		    						}
		    						
		    						buttonRemove += '</button>';
		    						angular.element(divCrud).append(buttonRemove);
		    					}
		    					
		    					angular.element(buttons).append(divCrud);
		    				}
		    			
		    			if(config.hide.active){
		    				var divHide = angular.element('<div class="btn-group" ng-show="'+config.name+'.config.hide.active"></div>');
		    				var buttonHide = '<button data-toggle="dropdown" class="btn btn-default dropdown-toggle" data-toggle="tooltip" title="'+MessagesDatatable("datatable.button.hide")+'"><i class="icon-eye-close icon-large"></i>';
		    				if(!config.compact){ 
		    					buttonHide+= " "+MessagesDatatable("datatable.button.hide");
		    				}
		    				buttonHide+= '<span class="caret"></span></button>';
		    				var dropdown = '<ul class="dropdown-menu"><li ng-repeat="column in '+config.name+'.getHideColumns()"><a href="#" ng-click="'+config.name+'.setHideColumn(column.id)"><i class="icon-eye-open" ng-show="'+config.name+'.isHide(column.id)""></i><i class="icon-eye-close" ng-hide="'+config.name+'.isHide(column.id)"></i> {{column.header}}</a></li></ul>';
		    				divHide.append(buttonHide);
		    				divHide.append(dropdown);
		    				angular.element(buttons).append(divHide);
		    			}
		    			if(buttonsConfig){
							angular.element(divButtons).append(buttonsConfig);
						}

			    		angular.element(toolbar).append(divButtons);
			    		
			    		var divPagination = angular.element('<div class="col-lg-4"></div>');
			    		var paginationToolBar = angular.element('<div class="btn-toolbar"  role="toolbar" style="margin: 0;" ng-show="'+config.name+'.isShowToolbarRight()"></div>');
			    		var paginationButtonTotal = '	<button class="btn btn-info" disabled="disabled" ng-show="'+config.name+'.config.showTotalNumberRecords">'+MessagesDatatable("datatable.totalNumberRecords", "{{"+config.name+".totalNumberRecords}}")+'</button>';
			    		angular.element(paginationToolBar).append(paginationButtonTotal);
			    		//Pagination Section
			    		if(config.pagination.active){
							
							//var pagination = '<div class="col-lg-7"><div class="pagination pagination-right" ng-show="'+config.name+'.isShowPagination()"><ul><li ng-repeat="page in '+config.name+'.config.pagination.pageList" ng-class="page.clazz"><a href="#" ng-click="'+config.name+'.setPageNumber(page)">{{page.label}}</a></li></ul></div></div>';
							
			    			var pagination = '<div class="col-lg-6"><div class="btn-group pull-right" ng-show="'+config.name+'.isShowPagination()"><ul class="pagination"><li ng-repeat="page in '+config.name+'.config.pagination.pageList" ng-class="page.clazz"><a href="#" ng-click="'+config.name+'.setPageNumber(page)">{{page.label}}</a></li></ul></div></div>';
			    			
			    			var paginationDropDown = '<div class="btn-group" ng-show="'+config.name+'.config.pagination.active"><button data-toggle="dropdown" class="btn btn-default dropdown-toggle">'+MessagesDatatable("datatable.button.length", "{{"+config.name+".config.pagination.numberRecordsPerPage}}")+' <span class="caret"></span></button><ul class="dropdown-menu pull-right"><li	ng-repeat="elt in '+config.name+'.config.pagination.numberRecordsPerPageList" class={{elt.clazz}}><a href="#" ng-click="'+config.name+'.setNumberRecordsPerPage(elt)">{{elt.number}}</a></li></ul></div>';
							
							angular.element(paginationToolBar).append(paginationDropDown);
							angular.element(divPagination).append(pagination);
							
			    		}
			    		var divPaginationButton =  angular.element('<div class="col-lg-6"></div>');
			    		angular.element(divPaginationButton).append(paginationToolBar);
						angular.element(divPagination).append(divPaginationButton);
			    		angular.element(toolbar).append(divPagination);	
			    		
						//Messages Section
			    		var messages = '<div class="row" ng-show="'+config.name+'.config.messages.active"><div ng-class="'+config.name+'.config.messages.clazz" ng-show="'+config.name+'.config.messages.text !== undefined"><strong>{{'+config.name+'.config.messages.text}}</strong></div></div>';
			    		
			    		//Table Section
				    	var datatable =  angular.element('<div class="row"></div>');
				    	var form = angular.element('<form class="form-inline"></form>');
				    	var table = angular.element('<table class="table table-condensed table-hover table-bordered"></table>');
				    	
				    	var tableHead = '<thead>';
				    		
				    	tableHead += '<tr ng-repeat="(key,headers) in '+config.name+'.getExtraHeaderConfig()"><th colspan="{{header.colspan}}" ng-repeat="header in headers">{{header.label}}</th></tr>';
				    		
				    	tableHead += '<tr><th id="{{column.id}}" ng-repeat="column in '+config.name+'.getColumnsConfig()"';
				    	
				    	if(config.hide.active){
				    		tableHead += ' ng-hide="'+config.name+'.isHide(column.id)"';
				    	}
				    	tableHead+= '>{{column.header}} <div class="btn-group pull-right">';
				    	if(config.edit.columnMode && config.edit.showButton ){
				    		tableHead += '<button class="btn btn-xs" ng-click="'+config.name+'.setEditColumn(column.id)" ng-show="column.edit" ng-disabled="!'+config.name+'.canEdit()" data-toggle="tooltip" title="'+MessagesDatatable("datatable.button.edit")+'"><i class="fa fa-edit"></i></button>'
						}
						if(config.order){
							tableHead += '<button class="btn btn-xs" ng-click="'+config.name+'.setOrderColumn(column.property, column.id)" ng-show="column.order" ng-disabled="!'+config.name+'.canOrder()" data-toggle="tooltip" title="'+MessagesDatatable("datatable.button.sort")+'"><i ng-class="'+config.name+'.getOrderColumnClass(column.id)"></i></button>'
						}
						if(config.hidding){
							tableHead += '<button class="btn btn-xs" ng-click="'+config.name+'.setHideColumn(column.id)" ng-show="column.hide" data-toggle="tooltip" title="'+MessagesDatatable("datatable.button.hide")+'"><i class="icon-eye-close"></i></button>'
						}			
						tableHead += '</div></th></tr></thead>';
						
						var tableBody = '<tbody><tr ng-show="'+config.name+'.isEdit()"><td ng-repeat="col in '+config.name+'.config.columns" ng-hide="'+config.name+'.isHide(col.id)"><div class="controls" ><div  html-input="col" datatable-name="'+config.name+'" header></div></div></td></tr>';
						tableBody += '<tr ng-repeat="value in '+config.name+'.displayResult | orderBy:'+config.name+'.config.orderBy:'+config.name+'.config.orderReverse" ng-click="'+config.name+'.select(value)" ng-class="value.trClass"><td rowspan="{{col.cells[$parent.$index].rowSpan}}" ng-hide="'+config.name+'.isHide(col.id)" ng-repeat="col in '+config.name+'.config.columns"> <div class="controls" ><div datatable-name="'+config.name+'"  html-input="col" index="{{$index}}"></div></div></td></tr></tbody>';
	
				    	table.html(tableHead+tableBody);
				    	
				    	form.append(table);
				    	
				    	datatable.append(form);
				    	
				    	/*formDiv.append(form);
				    	formDiv.append(toolbar);
				    	formDiv.append(messages);
				    	formDiv.append(datatable);*/
				    	
				    	element.html("");
				    	
				    	//Adding all to the DOM
				    	//element.append($compile(formDiv)(scope));
				    	if(formConfig){
			    			var formHead = angular.element('<div class="row"></div>');
			    			formHead.append(formConfig);//Adding the form
			    			element.append($compile(formHead)(scope));					    	
			    		}
				    	element.append($compile(toolbar)(scope));
				    	element.append($compile(messages)(scope));
				    	element.append($compile(datatable)(scope));
			    	};
			    	
			    	//Whatcher of the datatable config value
			    	scope.$watch(attrs.datatable, function(newValue, oldValue) {
		                if (newValue){//when a new value is find
		                	setConfig(newValue);//set the config
		                	generateDatatable();//generate the datatable
		                }
		            });
			    },
			  };
			}).directive("htmlInput", function($compile) {
    		  return {
    			    restrict: 'A',
    			    link: function(scope, element, attrs) {
    			    	var name="datatable";
    			    	if(attrs.datatableName != undefined){
    			    		name=attrs.datatableName;    			    		
    			    	}
    			    	
    			    	var ngChange = "";
    			    	var ngShow = name+".isEdit(col.id,value)";   			    	
    			    	if(angular.isDefined(attrs.header)){
    			    		ngChange = name+".updateColumn(col.property, col.id)";
    			    		ngShow = name+".isEdit(col.id)";
    			    	}
    			    	
    			    	var getNgModel = function(col){
        			    	if(angular.isDefined(attrs.header)){
        			    		return   name+".config.edit.columns."+col.id+".value";
        			    	}else if(angular.isFunction(col.property)){
        			    		return name+".config.columns."+attrs.index+".property(value)";
        			    	}else{
        			    		return "value."+col.property;
        			    		
        			    	}
    			    	};
    			    	var columnFormatter = function(col){
		    				var format = "";
		    				if(col.type == "Date"){
		    					format += "| date:'"+Messages("date.format")+"'";
		    				}else if(col.type == "DateTime"){
		    					format += "| date:'"+Messages("datetime.format")+"'";
		    				}else if(col.type == "Number"){
								format += "| number";
							}
		    				
		    				return format;
		    			};
		    			
		    			var getValueElement = function(col){
		    				if(angular.isDefined(col.render)){
		    					return '<span dt-compile="'+name+".config.columns."+attrs.index+".render(value)"+'"></span>';
		    				}else{
		    					if(col.type == "Boolean"){
		    						return '<div ng-switch on="'+getNgModel(col)+'"><i ng-switch-when="true" class="fa fa-check-square-o"></i><i ng-switch-default class="fa fa-square-o"></i></div>';
		    						
		    					} else{
		    						return '<span ng-bind="'+getNgModel(col)+' '+columnFormatter(col)+'"></span>';
		    					}
		    				}
		    						    				
		    			};
		    			
		    			var getOptions = function(col){
		    				if(angular.isString(col.possibleValues)){
		    					return col.possibleValues;
		    				}else{ //function
		    					return 'col.possibleValues';
		    				}
		    			};
		    			
		    			var getGroupBy = function(col){
		    				if(angular.isString(col.groupBy)){
		    					return 'group by opt.'+col.groupBy;
		    				}else{
		    					return '';
		    				}
		    					
		    			};
		    			
		    			var getEditElement = function(col){
		    				var editElement = "";
		    				if(col.edit && (col.type === "String" || col.type === undefined || col.type === "Number"
        		  	    		|| col.type === "Month" || col.type === "Week"  || col.type === "Time" || col.type === "DateTime"
        		  	    		|| col.type === "Range" || col.type === "Color" || col.type === "Mail" || col.type === "Tel"
        		  	    		|| col.type === "Url" || col.type === "Date")){
        		  	    			if(!col.choiceInList){
	        		  	    			switch (col.type) 
	        		  	    			{ 
		        		  	    			case "String": 
		        		  	    				editElement = '<input html-filter="{{col.type}}" type="text" class="input-sm" ng-model="'+getNgModel(col)+'" ng-change="'+ngChange+'"/>';
		        		  	    			break; 
		        		  	    			case "Number": 
		        		  	    				editElement = '<input html-filter="{{col.type}}" type="number" class="input-sm" ng-model="'+getNgModel(col)+'" ng-change="'+ngChange+'"/>';
		        		  	    			break; 
		        		  	    			case "Month": 
		        		  	    				editElement = '<input html-filter="{{col.type}}" type="month" class="input-sm" ng-model="'+getNgModel(col)+'" ng-change="'+ngChange+'"/>';
		        		  	    			break; 
		        		  	    			case "Week": 
		        		  	    				editElement = '<input html-filter="{{col.type}}" type="week" class="input-sm" ng-model="'+getNgModel(col)+'" ng-change="'+ngChange+'"/>';
		        		  	    			break;
		        		  	    			case "Time": 
		        		  	    				editElement = '<input html-filter="{{col.type}}" type="time" class="input-sm" ng-model="'+getNgModel(col)+'" ng-change="'+ngChange+'"/>';
		        		  	    			break;
		        		  	    			case "DateTime": 
		        		  	    				editElement = '<input html-filter="{{col.type}}" type="datetime" class="input-sm" ng-model="'+getNgModel(col)+'" ng-change="'+ngChange+'"/>';
		        		  	    			break;
		        		  	    			case "Range": 
		        		  	    				editElement = '<input html-filter="{{col.type}}" type="range" class="input-sm" ng-model="'+getNgModel(col)+'" ng-change="'+ngChange+'"/>';
		        		  	    			break;
		        		  	    			case "Color": 
		        		  	    				editElement = '<input html-filter="{{col.type}}" type="color" class="input-sm" ng-model="'+getNgModel(col)+'" ng-change="'+ngChange+'"/>';
		        		  	    			break;
		        		  	    			case "Mail": 
		        		  	    				editElement = '<input html-filter="{{col.type}}" type="mail" class="input-sm" ng-model="'+getNgModel(col)+'" ng-change="'+ngChange+'"/>';
		        		  	    			break;
		        		  	    			case "Tel": 
		        		  	    				editElement = '<input html-filter="{{col.type}}" type="tel" class="input-sm" ng-model="'+getNgModel(col)+'" ng-change="'+ngChange+'"/>';
		        		  	    			break;
		        		  	    			case "Url": 
		        		  	    				editElement = '<input html-filter="{{col.type}}" type="url" class="input-sm" ng-model="'+getNgModel(col)+'"  ng-change="'+ngChange+'"/>';
		        		  	    			break;
		        		  	    			case "Date": 
		        		  	    				editElement = '<input html-filter="{{col.type}}" type="date" class="input-sm" ng-model="'+getNgModel(col)+'"  ng-change="'+ngChange+'"/>';
		        		  	    			break;
		        		  	    			default: 
		        		  	    				editElement = '<input html-filter="{{col.type}}" type="text" class="input-sm" ng-model="'+getNgModel(col)+'" ng-change="'+ngChange+'"/>';
		        		  	    			break; 
	        		  	    			}
	        		  	    		}else{
	        		  	    			if(col.listStyle == "radio"){
	        		  	    				editElement = '<label ng-repeat="opt in col.possibleValues"  for="radio{{col.id}}"><input id="radio{{col.id}}" html-filter="{{col.type}}" type="radio" ng-model="'+getNgModel(col)+'" ng-change="'+ngChange+'" value="{{opt.name}}">{{opt.name}}<br></label>';
	            		  	    		}else if(col.listStyle == "select"){
	            		  	    			editElement = '<select html-filter="{{col.type}}" ng-options="opt.code as opt.name '+getGroupBy(col)+' for opt in '+getOptions(col)+' '+columnFormatter(col)+'" ng-model="'+getNgModel(col)+'" ng-change="'+ngChange+'"></select>';
	            		  	    		}else if(col.listStyle == "multiselect"){
	            		  	    			editElement = '<select multiple html-filter="{{col.type}}"  ng-options="opt.code as opt.name  '+getGroupBy(col)+' for opt in '+getOptions(col)+' '+columnFormatter(col)+'" ng-model="'+getNgModel(col)+'" ng-change="'+ngChange+'"></select>';
	            		  	    		}else if(col.listStyle == "bt-select"){
	            		  	    			editElement = '<div bt-select html-filter="{{col.type}}" placeholder="" bt-options="opt.code as opt.name  '+getGroupBy(col)+' for opt in '+getOptions(col)+' '+columnFormatter(col)+'" ng-model="'+getNgModel(col)+'" ng-change="'+ngChange+'"></div>';	
	            		  	    		}else if(col.listStyle == "bt-select-multiple"){
	            		  	    			editElement = '<div bt-select html-filter="{{col.type}}" placeholder="" multiple="true" bt-options="opt.code as opt.name '+getGroupBy(col)+' for opt in '+getOptions(col)+' '+columnFormatter(col)+'" ng-model="'+getNgModel(col)+'" ng-change="'+ngChange+'"></div>';
	            		  	    		}else{
	            		  	    			editElement = '<select html-filter="{{col.type}}" ng-options="opt.code as opt.name '+getGroupBy(col)+' for opt in '+getOptions(col)+' '+columnFormatter(col)+'"  ng-model="'+getNgModel(col)+'" ng-change="'+ngChange+'"></select>';
	            		  	    		}
	        		  	    		}
        		  	    	}else if(col.edit && col.type =="Boolean"){
        		  	    		editElement = '<input html-filter="{{col.type}}" type="checkbox" class="input-sm" ng-model="'+getNgModel(col)+'" ng-change="'+ngChange+'"/>';
        		  	    	}
		    				return editElement;
		    			};
		    			
    			    	var addHtmlElement = function(col){
    			    		var newElement = undefined;
    			    		if(!angular.isDefined(attrs.header)){
	    			    		if(col.edit){
	    			    			newElement = '<div ng-switch on="'+ngShow+'"><div ng-switch-when="true">'+getEditElement(col)+'</div><div ng-switch-default>'+getValueElement(col)+'</div></div>';
	    			    		}else{
	    			    			newElement = getValueElement(col);
	    			    		}
    			    		}else{
    			    			newElement = '<div ng-switch on="'+ngShow+'"><div ng-switch-when="true">'+getEditElement(col)+'</div><div ng-switch-default></div></div>';	    			    		
    			    		}
    			    		newElement = $compile(newElement)(scope);
        		  	    	element.html("");
        		  	    	element.append(newElement);
    			    	};
    			    	
    			        scope.$watch(attrs.htmlInput, function(newValue, oldValue) {
    			        	
    		                if (newValue){
    		                	addHtmlElement(newValue);
    		                }
    		            });


    			    },
    			  };
    			}).directive("htmlFilter", function($filter) {
    				return {
    					  require: 'ngModel',
    					  link: function(scope, element, attrs, ngModelController) {
    						  
    						  ngModelController.$parsers.push(function(data) {
    					      //view to model
    						   var convertedData = data;
   					    	
   					    	   if(attrs.htmlFilter.toLowerCase() == "datetime"){
   					    		   convertedData = $filter('date')(convertedData, Messages("datetime.format"));
   					    	   }else if(attrs.htmlFilter.toLowerCase() == "date"){
   					    		   convertedData = $filter('date')(convertedData, Messages("date.format"));
   					    	   }else if(attrs.htmlFilter.toLowerCase() == "number"){
   					    		   convertedData = $filter('number')(convertedData);
   					    	   }
   					    	
   					    	   return convertedData;
    					   	});

    					    ngModelController.$formatters.push(function(data) {
    					      //model to view
    					    	var convertedData = data;
    					    	
    					    	  if(attrs.htmlFilter.toLowerCase() == "datetime"){
     					    			convertedData = $filter('date')(convertedData, Messages("datetime.format"));
     					    	   }else if(attrs.htmlFilter.toLowerCase() == "date"){
     					    		   	convertedData = $filter('date')(convertedData, Messages("date.format"));
     					    	   }else if(attrs.htmlFilter == "Number"){
     					    		   	convertedData = $filter('number')(convertedData);
     					    	   }
    					    	
    					    	return convertedData;
    					    });   
    					  }
    					}
    			}).directive('dtCompile', function($compile) {
    				// directive factory creates a link function
    				return function(scope, element, attrs) {
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
    				};
    				});
