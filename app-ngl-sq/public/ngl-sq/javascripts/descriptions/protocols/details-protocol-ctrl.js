"use strict";

angular.module('home').controller('DetailsCtrl', ["descriptionsProtocolsSearchService", '$scope', '$http', '$routeParams', 'mainService', 'tabService', 'lists', 'messages','$route','$window',
	function (descriptionsProtocolsSearchService, $scope, $http, $routeParams, mainService, tabService, lists, messages,$route,$window) {
		$scope.searchService = descriptionsProtocolsSearchService;

		$scope.isCreateProtocolPage = function(){
			return $routeParams.code =='new';
		}

        if(!$scope.isCreateProtocolPage()){
          detailsPage();
        }else{
           createProtocolPage();
        }
    
    
    //-------------------

        function detailsPage(){
            $scope.initPage = function (){
                $scope.isDisplay = false;
                $scope.searchService.comment = "";
                $http.get(jsRoutes.controllers.protocols.api.Protocols.get($routeParams.code).url).then(function (response) {
                    $scope.protocol = response.data;
                    $scope.oldExperimentTypeCodes = $scope.protocol.experimentTypeCodes;
                    $scope.oldName = $scope.protocol.name;
                    if (tabService.getTabs().length == 0) {
                        tabService.addTabs({ label: $scope.protocols.code, href: jsRoutes.protocols.tpl.Protocols.get($scope.protocols.code).url, remove: true });
                        tabService.activeTab($scope.getTabs(1));
                    }

                    $http.get(jsRoutes.controllers.descriptionHistories.api.DescriptionHistories.get($scope.protocol.code).url).then(function (retour) {

                        $scope.descriptionHistories = retour.data;
                     
                          
                        if ($scope.descriptionHistories.length > 0) {
                            $scope.isDisplay = true;
                        }
                                             
                        
                    });
                        
                });
            
            }
             
                       
            
          
            $scope.isObjEmpty = function (obj){
                if (obj) {
                    return Object.keys(obj).length == 0;
                }
            }
            $scope.getKeysFromObject = function (objMAjJson) {
                var objMaj = JSON.parse(objMAjJson);
                return Object.keys(objMaj).filter(isValidProperty);
            //--
                function isValidProperty (property){
                    return property !== "properties" && property !=="_id";
                }
            };
            $scope.getKey = function (obj){
                var objMaj = JSON.parse(obj);
                return Object.keys(objMaj).filter(isValidProperty);

                function isValidProperty (property){
                    return property !== "properties" && property !=="_id";
                }
                
            }
          

            $scope.getPropertiesValues = function (objMAjJson) {
                var objMaj = JSON.parse(objMAjJson);
                var objMAjProperties = objMaj.properties;
                return objMAjProperties ? getPropertyValues(objMAjProperties) : [];

                //---

                function getPropertyValues(objMAjProperties) {
                    return Object.values(objMAjProperties).map(getPropertyValue);
                }

                function getPropertyValue(property) {
                    return property.value;
                }
            };

            $scope.convertToJson = function (toJson) {
                toJson = JSON.parse(toJson);
                return toJson;
            };
            $scope.setActiveTab = function (value) {
                mainService.put('descriptionActiveTab', value);
            };

            $scope.getTabClass = function (value) {
                if (value === mainService.get('descriptionActiveTab')) {
                    return 'active';
                }
            };
            $scope.isValuesFilled = function(){
                saveInProgress = true;
                setErrorIfEmpty($scope.protocol.name,"errorName");
                setErrorIfEmpty($scope.protocol.experimentTypeCodes,"errorExp");
                setErrorIfEmpty($scope.protocol.experimentTypeCodes,"errorExp");
                setErrorIfEmpty($scope.searchService.comment,"errorComment");
                setErrorIfEmpty($scope.protocol.categoryCode,"errorCatCode");
                setErrorIfEmpty($scope.protocol.filePath,"errorPath");
                setErrorIfEmpty($scope.protocol.version,"errorVersion");
                
                return saveInProgress;

                //---

                function setErrorIfEmpty(protocolValue,errorProtocolValue){
                    if(!protocolValue||protocolValue.length == 0){
                        $scope.validationMessage.setError("create");	
                        $scope[errorProtocolValue] = true;	
                        saveInProgress = false;	
                    }else{
                        $scope[errorProtocolValue] = false;	
                    }
                }
            }

            $scope.save = function () {
                $scope.validationMessage.clear();
                $scope.warningMessage.clear();

                if($scope.isValuesFilled()) {
                    if($scope.oldName != $scope.protocol.name){
                      checkNameExist();
                    }

                    var deletedExperimentTypes = $scope.oldExperimentTypeCodes.filter(isDeletedExpermientType);
                    var deletedExperimentTypesQueryParams = "";
                    deletedExperimentTypes.forEach(buildQueryParams);
                        
                    if (deletedExperimentTypes.length == 0) {
                        querysave();
                    } else {
                        $http.get(jsRoutes.controllers.experiments.api.Experiments.list().url + "?protocolCodes=" + $scope.protocol.code + deletedExperimentTypesQueryParams)
                            .success(function (experimentArray) {
                                if (experimentArray.length == 0) {
                                    return querysave();
                                } else {
                                    $scope.validationMessage.setError("save");
                                    angular.element('#affichageAlert').modal('show');
                                    saveInProgress = false;
                                }
                            });
                    }
                   
                }	
                //----
                function checkNameExist (){
                    $http.get(jsRoutes.controllers.protocols.api.Protocols.list().url).then(function (retour) {
                        $scope.listProto = retour.data;
                        $scope.duplicateName =  $scope.listProto.filter(compareTwoNames);
                        if($scope.duplicateName.length>0){
                            $scope.warningMessage.setWarning("Nom déjà existant");
                        }
                       });
                }
                function compareTwoNames(protocol){
                    return protocol.name == $scope.protocol.name
                }

                function buildQueryParams (expTypcode){
                    return deletedExperimentTypesQueryParams +="&typeCodes=" + expTypcode;
                }

                function isDeletedExpermientType(expTypcode){
                    return  !$scope.protocol.experimentTypeCodes.includes(expTypcode);

                }
                function querysave () {
                    $http.put(jsRoutes.controllers.protocols.api.Protocols.update($scope.protocol.code).url + "?comment=" + $scope.searchService.comment, $scope.protocol)
                        .success(function (data, status, headers, config) {
    
                            $http.get(jsRoutes.controllers.descriptionHistories.api.DescriptionHistories.get($scope.protocol.code).url).then(function (retour) {
                                $scope.descriptionHistories = retour.data;
                            });
                           
                            $scope.protocol = data;
                            $scope.validationMessage.setSuccess("save");
                            mainService.stopEditMode();
                            $scope.oldExperimentTypeCodes = data.experimentTypeCodes;
                            saveInProgress = false;
                            $scope.searchService.comment="";
                            $scope.isDisplay = true;
                            mainService.put('descriptionActiveTab', 'histo');
                            
                          
                        })
                        .error(function (data, status, headers, config) {
                            $scope.validationMessage.setError("save");
                            $scope.validationMessage.setDetails(data);
                            saveInProgress = false;
                        });
                };

            };
            $scope.warningRemoveProtocol = function(){
                $http.get(jsRoutes.controllers.experiments.api.Experiments.list().url + "?protocolCodes=" + $scope.protocol.code)
                .success(function (experimentArray) {
                    if (experimentArray.length == 0) {
                        angular.element('#affichageAlertRemoveSure').modal('show');
                    } else {

                        //bloquage suppression via l'affichage d'une pop up
                        $scope.validationMessage.setError("Echec de suppression");
                        angular.element('#affichageAlertRemove').modal('show');
                        saveInProgress = false;
                    }
                });
            }
            $scope.removeProtocol = function(){

                    $http.delete(jsRoutes.controllers.protocols.api.Protocols.delete($scope.protocol.code).url)
                    .success(function(data, status, headers, config) {

                        $scope.warningMessage.setWarning("Suppression en cours...");
                        setTimeout($window.location.href ="/descriptions/protocols/home",45000);
                    }).error(function(data, status, headers, config) {
                        $scope.messages.setError("Echec de suppression");
                        $scope.messages.setDetails(data);
                    
                });
            }
                
            

            $scope.activeEditMode = function () {
                $scope.validationMessage.clear();
                mainService.startEditMode();
            };

            $scope.cancel = function () {
                $scope.validationMessage.clear();
                mainService.stopEditMode();
                updateData();
            };

            $scope.isSaveInProgress = function () {
                return saveInProgress;
            };

            var updateData = function () {
                $http.get(jsRoutes.controllers.protocols.api.Protocols.get($scope.protocol.code).url).then(function (response) {
                    $scope.protocol = response.data;
                });
            };

            var saveInProgress = false;

            var init = function () {
                $scope.warningMessage = messages();
                $scope.validationMessage = messages();

                $scope.lists = lists;
                $scope.mainService = mainService;
                mainService.stopEditMode();
                mainService.put('descriptionActiveTab', 'general');
                $scope.initPage();
                
            };

            init();
       
    }
	function createProtocolPage() {
        
        $scope.isSaved = false;
        $scope.protocol = {};
        $scope.futurProperties = [];
        $scope.hasValues={};
		$scope.protocol.categoryCode = "production";
		$scope.protocol.filePath = "path1";
		$scope.protocol.version = 1;
        $scope.protocol.active = false;

      $scope.getPropertyKey = function (){
        if(!$scope.availablePropertiesOnProtocol) return [];
        $scope.propertyDefinitions = Object.values($scope.availablePropertiesOnProtocol.properties);
        return $scope.propertyDefinitions;
      }

        $scope.addPropertyDefinition = function(){  
           var propertyDefinition=  $scope.propertyDefinitions.filter(isPropertiesInProtocol)[0];
           $scope.messages.clear();

         	if(propertyDefinition !== undefined){
				$scope.errorProp = false;
				if (!isDuplicate()) {
					if (isDefaultValue()) {
						setDefaultValue();
					} else if (isPossibleValues()) {
						setPossibleValues();
					}
				}
			}else{
			$scope.errorProp = true
			}

            //---

            function isDefaultValue() {
                return propertyDefinition.defaultValue == "name";
            }
            function setDefaultValue(){
                $scope.hasValues[propertyDefinition.code] = false;
                $scope.futurProperties.push({
                    name:  $scope.searchService.propertyDefinition,
                    defaultValue: "",
                    hasMultipleValues : function (){
                        return false;
                    }
                });
                $scope.searchService.propertyDefinition= "";     
            }
            function isPossibleValues() {
                var possibleValues = propertyDefinition.possibleValues;
                if(possibleValues !== undefined || possibleValues != null){
                    $scope.hasValues[propertyDefinition.code] = true
                }
                return possibleValues;   
            }
            function setPossibleValues(){
               $scope.futurProperties.push({
                    name:  $scope.searchService.propertyDefinition,
                    possibleValues: propertyDefinition.possibleValues,
                    hasMultipleValues : function (){
                        return true;
                    }
                });
                $scope.searchService.propertyDefinition= "";
            }
            function isPropertiesInProtocol (property){
                return property.code === $scope.searchService.propertyDefinition ;
            }
            function isDuplicated(displayedProp){
                return displayedProp.name === propertyDefinition.code;
            }          
            function isDuplicate(){
                var duplicates =$scope.futurProperties.filter(isDuplicated);
                testDuplicate(duplicates);
                return duplicates.length !== 0;
            }
            function testDuplicate(tabtest){
                if(tabtest.length > 0){
                    $scope.messages.setError("double");				
                }
            }
        
        };

        $scope.removeProperty = function(futurProperty){
            $scope.futurProperties =$scope.futurProperties.filter(remove)
           //---
           function remove(el){
            return futurProperty.name !== el.name
            }
        };

        $scope.getValuesFromSelectedProperty = function(property) {
            return $scope.futurProperties.filter(selectedProperty)[0].possibleValues;
            //---
            function selectedProperty(proper){
                return proper.name == property.name;
            }
        };

        $scope.isValuesFilled = function(){
            saveInProgress = true;
            setErrorIfEmpty($scope.protocol.name,"errorName");
            setErrorIfEmpty($scope.protocol.code,"errorCode");
            setErrorIfEmpty($scope.protocol.experimentTypeCodes,"errorTypeExp");
            return saveInProgress;

            //---

            function setErrorIfEmpty(protocolValue,errorProtocolValue){
                if(protocolValue == undefined || protocolValue.length == 0 ||protocolValue ==""){
                    $scope.messages.setError("create");	
                    $scope[errorProtocolValue] = true;	
                    saveInProgress = false;	
                }else{
                    $scope[errorProtocolValue] = false;	
                }
            }
        }
        $scope.getPropertienInBase = function(){
            var propertiesInBase = {};
                for (var i = 0; i <$scope.futurProperties.length; i++) {
                    if($scope.futurProperties[i].getProperty !== undefined){
                      
                        propertiesInBase[$scope.futurProperties[i].name] = {
                            "_type": "single",
                            "value":$scope.futurProperties[i].getProperty
                        }
                    }
                    else{
                        propertiesInBase[$scope.futurProperties[i].name] = {
                        "_type": "single",
                        "value":$scope.futurProperties[i].defaultValue
                        }

                    }   
                }
              
            $scope.protocol.properties = propertiesInBase;
        }

        $scope.saveAll = function (){
            if (saveInProgress){
                $scope.getPropertienInBase();
                $http.post(jsRoutes.controllers.protocols.api.Protocols.save().url, $scope.protocol)
                
                 .success(function (data, status, headers, config) {
                    $scope.protocol = data;
                    $scope.isSaved = true;
                    saveInProgress = false;
                    $scope.messages.setSuccess("save");
                    lists.refresh.protocols();
                    tabService.addTabs({label:$scope.protocol.name,href:("/descriptions/protocols/" + $scope.protocol.code), remove:true});
                    setTimeout(decompte,2500);
                 })
                .error(function (data, status, headers, config) {
                    $scope.messages.setError("save");
                    $scope.messages.setDetails(data);
                    saveInProgress = false;

                });
            }            

        }
        function decompte(){
            $route.reload();
        }
        $scope.warningTagPcr =function(array){
            if(array){
                return  array.includes('tag-pcr');
            }
           
              
          }
          $scope.isShowConsigne=false;
	$scope.toggleIsShowInformation=function(){
		if ( $scope.isShowInformation===false) { $scope.isShowInformation=true}
		else {$scope.isShowInformation=false}
	}
        $scope.addProtocol = function () { 
            $scope.messages.clear();
            if( $scope.isValuesFilled()){
                $scope.saveAll();
                lists.refresh.protocols();
            }          

        }; 
        $scope.isSaveInProgress = function(){
            return saveInProgress;
        };

        var saveInProgress = false;
        
        var init = function(){
            $scope.messages = messages();
            $scope.warningMessage = messages();
            $scope.mainService = mainService;
			mainService.put('descriptionActiveTab', 'general');
            $http.get(jsRoutes.controllers.commons.api.Parameters.list().url + "?codes=property-definition-protocol&typeCodes=map-property-definitions").then(function (retour){
             $scope.availablePropertiesOnProtocol = retour.data[0];
            if($scope.availablePropertiesOnProtocol == undefined){
                $scope.messages.setError("get");
                console.log("le document map-property-definitions de la collection Parameter n'est pas présent");
            }
            });       
        };

        init();
	}
	
	}]);