angular.module('nglServices', []).
    	factory('datatable', ['$http', function($http){ //service to manage datatable
    		var constructor = function($scope, iConfig){
				var datatable = {
    					config:undefined,
    					configMaster:undefined,
    					searchresult:undefined,
    					searchresultMaster:undefined,
    					/**
    					 * Select all the table line or just one
    					 */
						select : function(line){
		    				if(line){
		    					if(!line.selected){
		    						line.selected=true;
		    						line.trClass="row_selected";
		    					}
								else{
									line.selected=false;
		    						line.trClass="";
								}
		    				}else {
		    					for(var i = 0; i < this.searchresult.length; i++){
		    						this.searchresult[i].selected=true;
		    						this.searchresult[i].trClass="row_selected";
		    					}
		    				}
		    			},
		    			/**
		    			 * cancel all actions (edit, hide, order, etc.)
		    			 */
		    			cancel : function(){
		    				this.searchresult = angular.copy(this.searchresultMaster);
		    				this.config = angular.copy(this.configMaster);
		    			},
		    			
		    			search : function(params){
		    				if(this.config.url.search){
		    					$http.get(this.config.url.search,{params:params}).success(function(data) {
									$scope.datatable.searchresult = data;
									$scope.datatable.searchresultMaster = angular.copy(data);

		    					});
		    				}else{
		    					alert('no url define for search ! ');
		    				}
		    			},
		    			
		    			/**
		    			 * Save the selected table line
		    			 */
		    			save : function(){
		    				for(var i = 0; i < this.searchresult.length; i++){
		    					if(this.searchresult[i].selected){
		    						if(this.config.url.save){
		    							this.saveObject(this.searchresult[i], i);
		    						}else{
		    							this.searchresult[i].selected = false;
		    							this.searchresult[i].edit=false;
		    							this.searchresult[i].trClass = undefined;
		    							this.searchresultMaster[i] = angular.copy(this.searchresult[i]);
		    							this.searchresult[i].trClass = "success";
		    						}
		    					}						
		    				}		    				
		    			},
		    			saveObject : function(value, i){
		    				$http.post(this.config.url.save, value)
		    				.success(function(data) {
		    					this.searchresult[i].selected = false;
		    					this.searchresult[i].edit=false;
		    					this.searchresult[i].trClass = undefined;
		    					this.searchresultMaster[i] = angular.copy(this.searchresult[i]);
		    					this.searchresult[i].trClass = "success";
		    				})
		    				.error(function(data) {
		    					this.searchresult[i].trClass = "error";
		    				});
		    				
		    			},
		    			
		    			/**
		    			 *  Remove the selected table line
		    			 */
		    			remove : function(){
		    				for(var i = 0; i < this.searchresult.length; i++){
		    					if(this.searchresult[i].selected){
		    						this.searchresult.splice(i,1);				
		    						this.searchresultMaster.splice(i,1);
		    						//missing update in db
		    						i--;
		    					}						
		    				}
		    				this.config = angular.copy(this.configMaster);
		    			},					    			
		    			/**
		    			 * set Edit all column or just one
		    			 * @param editColumnName : column name
		    			 */
		    			setEditColumn : function(editColumnName){		
		    				var find = false;
		    				for(var i = 0; i < this.searchresult.length; i++){
		    					if(this.searchresult[i].selected){
		    						this.searchresult[i].edit=true;
		    						find = true;
		    					
		    					}else{
		    						this.searchresult[i].edit=false;
		    					}
		    				}
		    				if(find){
		    					this.config.edit = true;			
		    					if(editColumnName){  (new Function("config","config.editColumn."+editColumnName+"=true"))(this.config);}
		    					else this.config.editColumn.all = true;
		    				}
		    			},
		    			/**
		    			 * Test if a column must be in edition mode
		    			 * @param editColumnName : column name
		    			 * @param line : the line in the table
		    			 */
		    			isEdit : function(editColumnName, line){
		    				if(editColumnName && line){
		    					var columnEdit = (new Function("config","return config.editColumn."+editColumnName))(this.config);
		    					return (line.edit && columnEdit) || (line.edit && this.config.editColumn.all);
		    				}else if(editColumnName){
		    					var columnEdit =  (new Function("config","return config.editColumn."+editColumnName))(this.config);
		    					return (columnEdit || this.config.editColumn.all);
		    				}else{
		    					return this.config.edit;
		    				}
		    			},
		    			/**
		    			 * Update all line with the same value
		    			 * @param updateColumnName : column name
		    			 */
		    			updateColumn : function(updateColumnName){	
		    				for(var i = 0; i < this.searchresult.length; i++){
		    					if(this.searchresult[i].selected){
		    						var fn = new Function("searchresult", "config","searchresult."+updateColumnName+"=config.updateColumn."+updateColumnName);
		    						fn(this.searchresult[i], this.config);				
		    					}
		    				}
		    			},
		    			//Hide a column
		    			/**
		    			 * set the hide column
		    			 * @param hideColumnName : column name
		    			 */
		    			setHideColumn : function(hideColumnName){	
		    				var fn = new Function("config", "if(!config.hideColumn."+hideColumnName+"){config.hideColumn."+hideColumnName+"=true;} else{ config.hideColumn."+hideColumnName+"=false;}");
		    				fn(this.config);		
		    			},
		    			/**
		    			 * Test if a column must be hide
		    			 * @param hideColumnName : column name 
		    			 */
		    			isHide : function(hideColumnName){
		    				var fn = new Function("config", "if(config.hideColumn."+hideColumnName+") return config.hideColumn."+hideColumnName+";else return false;");
		    				return fn(this.config);
		    			},
		    			/**
		    			 * set the order column name
		    			 * @param orderColumnName : column name
		    			 */
		    			setOrderColumn : function(orderColumnName){
		    				this.config.orderBy = orderColumnName;
		    				var fn = new Function("config", "if(!config.orderColumn."+orderColumnName+"){config.orderColumn."+orderColumnName+"=true; config.orderReverse=true;} else{ config.orderColumn."+orderColumnName+"=false; config.orderReverse=false;}");
		    				fn(this.config);
		    			}
    			};
    			
    			datatable.config = iConfig;
    			datatable.configMaster = angular.copy(iConfig);    			
    			return datatable;
    		}
    		return constructor;
    	}]);
