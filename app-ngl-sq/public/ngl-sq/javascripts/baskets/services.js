angular.module('basketsServices', []).
    	factory('baskets', ['$http', function($http){ //service to manage baskets
    		var constructor = function($scope, iConfig, data){
				var baskets = {
					config:undefined,
					configMaster:undefined,
					data:undefined,
					experimentTypeCode:undefined,
					/**
					*	Main function of the service
					*	If new basket, create the basket and then insert the container
					*	If not a new basket, juste insert the container
					*	@param newBasket boolean: true it's a new basket, false it's not
					*	@param experimentTypeCode String: the code of the type experiment
					*/
					add: function(newBasket,experimentTypeCode){
						var that = this;
						if(this.data.displayResult != null){
							var selected = false;
							for(var i = 0; i < this.data.displayResult.length; i++){
									if(this.data.displayResult[i].selected){
										selected=true;
										break;
									}
							}
							
							if(selected){
								if(!newBasket){
									this.getBasketList(this.config,experimentTypeCode);
									this.fnModalControl(this.config,this.config.textModal);
								} else {
									this.fnModalControl(this.config,"Name of the basket: <input type='text' id='basketName' name='basketName'/>");
								}
								
								$("#Add").click( function () {
									if(!newBasket){
										//get code here
										var e = document.getElementById("basketName");
										code = e.options[e.selectedIndex].value;
										that.addToBasket();	
									}else{
										code = document.getElementById("basketName").value;
										that.addBasket(code,experimentTypeCode);
									}									
								});
							}
						}
					},
					/**
					*	Control the modal
					*	@param Oconfig : the configuration of the basket
					*	@param text String: the html to add in the body of the modal
					*/
					fnModalControl:function(oConfig,text){
						if(!oConfig.manualModal)
						$('body').append('<div class="modal fade" id="'+oConfig.modalId+'"><div class="modal-header"><a class="close" data-dismiss="modal">&times;</a><h3>'+oConfig.titleModal+'</h3></div><div class="modal-body" id="modal-body"><p>'+text+'</p></div><div class="modal-footer"><a class="btn btn-default" data-dismiss="modal">'+oConfig.textCancelModal+'</a><a class="btn btn-primary" id="'+oConfig.idBtnModal+'">Add</a></div></div>');

						$('#'+oConfig.modalId).modal('show');

						if(!oConfig.manualModal)
						{
							$('#'+oConfig.modalId).on('hidden', function () {
								$('#'+oConfig.modalId).remove();
							})

							$('#'+oConfig.modalId).on('dismiss', function () {
								$('#'+oConfig.modalId).remove();
							})
						}
					},
					/**
					*	Create a <select> with the basket (filter by experimenttypecode)
					*	@param Oconfig : the configuration of the basket
					*	@param experimentTypeCode String: the code of the type experiment
					*/
					getBasketList:function(oConfig,experimentTypeCode){
						 oConfig.textModal = "Chargement...";
						 $("#modal-body").html("");
						 $http.get(oConfig.urlList+"/"+experimentTypeCode).success(function(datas) {
							var i = 0;
							var txtModal="";
							txtModal += "Name of the basket: <select id='basketName' name='basketName'> ";
							for(i=0;i<datas.iTotalRecords;i++){
								txtModal += "<option value='"+datas.aaData[i].code+"'>"+datas.aaData[i].code+"</option>"
							}
							txtModal += "</select> ";  
							oConfig.textModal = txtModal;
							$("#modal-body").html(txtModal);
		    			});
					},
					/**
					*	Call the url to create a basket with json param
					*	@param code : the code of the basket
					*	@param experimentTypeCode String: the code of the type experiment
					*/
					addBasket:function(code,experimentTypeCode){
						var that = this;
						var myJSONObject = {
							"code" : code,
							"experimentTypeCode" : experimentTypeCode,
						};
						$http({
							method: 'POST',
							url: that.config.url,
							data: JSON.stringify(myJSONObject),
							headers: {'Content-Type': 'application/json'}
						}).success(function(datas) {
							that.addToBasket();
						});
					},
					/**
					*	Set the experiment type of the basket
					*	@param experimentTypeCode String: the code of the type experiment
					*/
					setExperiment:function(experiment){
						if(experiment){
							this.experimentTypeCode = experiment.code;
						}
					},
					/**
					*	Call the url to add a container to a backet
					*/
					addToBasket:function() {
						var that = this;
						for(var i = 0; i < data.displayResult.length; i++){
							if(data.displayResult[i].selected){
								$http({
									method: 'POST',
									url: that.config.url+"/"+code,
									data: JSON.stringify({ "container": data.displayResult[i].code }),
									headers: {'Content-Type': 'application/json'}
								});
							}
							$('#'+this.config.modalId).modal('hide');
						}	
					}
				};
				
				baskets.data = data;
				baskets.config = iConfig;
    			baskets.configMaster = angular.copy(iConfig);    			
    			return baskets;
			}
		return constructor;
    	}]);