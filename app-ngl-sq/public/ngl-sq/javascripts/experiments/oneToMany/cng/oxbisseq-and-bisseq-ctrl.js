// 07/06/2018 adapté depuis small-and-large-rna-extraction pour la creation automatique de plusieurs output containerUsed par inputContainer
// 22/06/2018 utilisation de la factory tagPlates dans le module tools (tag-plate-helpers.js)
angular.module('home').controller('OxBisseqAndBisseqCtrl',['$scope', '$parse', '$filter','atmToSingleDatatable','mainService','$http','tagPlates','lists',
                                                               function($scope, $parse, $filter, atmToSingleDatatable, mainService, $http, tagPlates,lists) {

	//$scope.dispatchConfiguration.orderBy = "container.sampleCodes";
	
	var inputExtraHeaders=Messages("experiments.inputs");
	var outputExtraHeaders=Messages("experiments.outputs");	
	
	var datatableConfig =  {
			name: $scope.experiment.typeCode.toUpperCase(),
			columns:[            
			         { // Projet(s)
			        	"header":Messages("containers.table.projectCodes"),
			 			"property": "inputContainer.projectCodes",
			 			"order":true,
			 			"hide":true,
			 			"type":"text",
			 			"mergeCells" : true,
			 			"position":2,
			 			"render":"<div list-resize='cellValue' list-resize-min-size='3'>",
			            "extraHeaders":{0: inputExtraHeaders}
				     },
				     { // Echantillon(s) 
			        	"header":Messages("containers.table.sampleCodes"),
			 			"property": "inputContainer.sampleCodes",
			 			"order":true,
			 			"hide":true,
			 			"type":"text",
			 			"mergeCells" : true,
			 			"position":3,
			 			"render":"<div list-resize='cellValue' list-resize-min-size='3'>",
			        	"extraHeaders":{0: inputExtraHeaders}
				     },
			         { // Concentration
			        	 "header":Messages("containers.table.concentration") + " (ng/µL)",
			        	 "property":"inputContainerUsed.concentration.value",
			        	 "order":true,
						 "edit":false,
						 "hide":true,
			        	 "type":"number",
			        	 "position":5,
			        	 "extraHeaders":{0: inputExtraHeaders}
			         },
			         { //Volume 
			        	 "header":Messages("containers.table.volume") + " (µL)",
			        	 "property":"inputContainerUsed.volume.value",
			        	 "order":true,
						 "edit":false,
						 "hide":true,
			        	 "type":"number",
			        	 "position":6,
			        	 "extraHeaders":{0: inputExtraHeaders}
			         },
			         { // Etat input Container 
			        	 "header":Messages("containers.table.state.code"),
			        	 "property":"inputContainer.state.code",
			        	 "order":true,
						 "edit":false,
						 "hide":true,
			        	 "type":"text",
			        	 "filter":"codes:'state'",
			        	 "position":7,
			        	 "extraHeaders":{0: inputExtraHeaders}
			         },			        
			         //--->  colonnes specifiques experience s'inserent ici  (inputUsed ??)     
			         
			         //------------------------- OUTPUT containers section --------------------------
			         
			         //--->  colonnes specifiques experience s'inserent ici  (outputUsed ??)
			         /*
			         {
			        	 "header":Messages("containers.table.quantity")+" (ng)",
			        	 "property":"outputContainerUsed.quantity.value",
			        	 "order":true,
						 "edit":true,
						 "hide":true,
						 "type":"number",
			        	 "position":52,
			        	 "extraHeaders":{0: outputExtraHeaders}
			         },
			         {
			        	 "header":Messages("containers.table.volume")+" (µL)",
			        	 "property":"outputContainerUsed.volume.value",
			        	 "editDirectives":" udt-change='updatePropertyFromUDT(value,col)' ",
			        	 "order":true,
						 "edit":true,
						 "hide":true,
						 "type":"number",
			        	 "position":51,
			        	 "extraHeaders":{0: outputExtraHeaders}
			         },
			         */
			         {
			        	 "header":Messages("containers.table.stateCode"),
			        	 "property":"outputContainer.state.code | codes:'state'",
			        	 "order":true,
						 "edit":false,
						 "hide":true,
						 "type":"text",
			        	 "position":500,
			        	 "extraHeaders":{0: outputExtraHeaders}
			         }   
			         ],
			compact:true,
			pagination:{
				active:false
			},		
			search:{
				active:false
			},
			order:{
				mode:'local', 
				active:true,
				by:'inputContainer.code'
			},
			remove:{
				active:false,
			},
			save:{
				active:true,
				withoutEdit: true,
				mode:'local',
				showButton:false,
				changeClass:false,
	        	callback:function(datatable){
	        		  copyContainerSupportCodeAndStorageCodeToDT(datatable);
	        	}
			},
			hide:{
				active:true
			},
			mergeCells:{
				active:true 
			},			
			edit:{
				active: ($scope.isEditModeAvailable() && $scope.isWorkflowModeAvailable('F')),	
				showButton: ($scope.isEditModeAvailable() && $scope.isWorkflowModeAvailable('F')),
				byDefault:($scope.isCreationMode()),
				columnMode:true
			},
			messages:{
				active:false,
				columnMode:true
			},
			exportCSV:{
				active:true,
				showButton:true,
				delimiter:";",
				start:false
			},
			extraHeaders:{
				number:1,
				dynamic:true,
			},
			otherButtons: {
                active: ($scope.isEditModeAvailable() && $scope.isWorkflowModeAvailable('F')),
                complex:true,
                template:''
                	+$scope.plateUtils.templates.buttonLineMode()
                	+$scope.plateUtils.templates.buttonColumnMode()
			}

	};	
	
	// colonne dependant de la categorie support input
	if($scope.experiment.instrument.inContainerSupportCategoryCode !== "tube"){
		datatableConfig.columns.push({
			"header" : Messages("containers.table.supportCode"),
			"property" : "inputContainer.code",
			"order" : true,
			"edit" : false,
			"hide" : true,
			"type" : "text",
			"position" : 1,
			"mergeCells" : true,
			"extraHeaders" : {0: inputExtraHeaders}
		});

	} else {
		datatableConfig.columns.push({
			"header" : Messages("containers.table.code"),
			"property" : "inputContainer.support.code",
			"order" : true,
			"edit" : false,
			"hide" : true,
			"type" : "text",
			"position" : 1,
			"mergeCells" : true,
			"extraHeaders" : {0: inputExtraHeaders}
		});
		
		datatableConfig.order.by = 'inputContainer.sampleCodes';
	}
	
	if($scope.experiment.instrument.outContainerSupportCategoryCode !== "tube") {
		datatableConfig.columns.push({
			// barcode plaque sortie 
			"header" : Messages("containers.table.support.name"),
			"property" : "outputContainerUsed.locationOnContainerSupport.code",
			"hide" : true,
			"type" : "text",
			"position" : 400,
			"extraHeaders" : {0: outputExtraHeaders}
		});
		
		datatableConfig.columns.push({
			// Ligne
 			"header" : Messages("containers.table.support.line"),
 			"property" : "outputContainerUsed.locationOnContainerSupport.line",
 			"edit" : true,
 			"choiceInList":true,
 			"possibleValues":[{"name":'A',"code":"A"},{"name":'B',"code":"B"},{"name":'C',"code":"C"},{"name":'D',"code":"D"},
 			                  {"name":'E',"code":"E"},{"name":'F',"code":"F"},{"name":'G',"code":"G"},{"name":'H',"code":"H"}],
 			"order" : true,
 			"hide" : true,
 			"type" : "text",
 			"position" : 401,
 			"extraHeaders" : {0: outputExtraHeaders}
		});
		
		datatableConfig.columns.push({// colonne
 			"header" : Messages("containers.table.support.column"),
 			// astuce GA: pour pouvoir trier les colonnes dans l'ordre naturel
 			// forcer a numerique.=> type:number, property: *1
 			"property" : "outputContainerUsed.locationOnContainerSupport.column",
 			"edit" : true,
 			"choiceInList":true,
 			"possibleValues":[{"name":'1',"code":"1"},{"name":'2',"code":"2"},{"name":'3',"code":"3"},{"name":'4',"code":"4"},
 			                  {"name":'5',"code":"5"},{"name":'6',"code":"6"},{"name":'7',"code":"7"},{"name":'8',"code":"8"},
 			                  {"name":'9',"code":"9"},{"name":'10',"code":"10"},{"name":'11',"code":"11"},{"name":'12',"code":"12"}], 
 			"order" : true,
 			"hide" : true,
 			"type" : "number",
 			"position" : 402,
 			"extraHeaders" : {0: outputExtraHeaders}
		});

	} else {
		// barcode tube sortie 
		datatableConfig.columns.push({
			"header" : Messages("containers.table.code"),
			"property" : "outputContainerUsed.locationOnContainerSupport.code",
			"order" : true,
			"edit" : true,
			"editDirectives":"udt-change='atmService.emptyToNull(value.data, col.property)'", //NGL-2487 bug si empty
			"hide" : true,
			"type" : "text",
			"position" : 400,
			"extraHeaders" : {0: outputExtraHeaders}
		});		
	}
	
	// FDS 12/03/2019==> remplace  les 3 ligne differentes dans copyContainerSupportCodeAndStorageCodeToDT ????????
	var updateATM = function(experiment){
		if(experiment.instrument.outContainerSupportCategoryCode !== "tube"){
			experiment.atomicTransfertMethods.forEach(function(atm){
				atm.line = atm.outputContainerUseds[0].locationOnContainerSupport.line;
				atm.column = atm.outputContainerUseds[0].locationOnContainerSupport.column;
			});
		}		
	};
	
	$scope.$on('save', function(e, callbackFunction) {	
		console.log("call event save");
			// NGL-2371 pas de message et pas d'erreur de sauvegarde !!!
			$scope.atmService.data.save();
			$scope.atmService.viewToExperimentOneToMany($scope.experiment);
		
			// FDS 12/03/2019==> remplace les 3 lignes differentes dans copyContainerSupportCodeAndStorageCodeToDT ??
			updateATM($scope.experiment);
		
			$scope.$emit('childSaved', callbackFunction);
	});
	
	$scope.$on('refresh', function(e) {
		console.log("call event refresh"); 
		var dtConfig = $scope.atmService.data.getConfig();
		dtConfig.edit.active = ($scope.isEditModeAvailable() && $scope.isWorkflowModeAvailable('F'));
		  ///   dtConfig.edit.showButton = ($scope.isEditModeAvailable() && $scope.isWorkflowModeAvailable('F')); ???
		dtConfig.edit.byDefault = false;
		$scope.atmService.data.setConfig(dtConfig);
		$scope.atmService.refreshViewFromExperiment($scope.experiment);
		// NGL-2371 FDS 20/03/2019 récupérer outputContainerSupport s'il a été généré automatiquement (pas de barcode entré par l'utilisateur)
		$scope.outputContainerSupport.code=$scope.experiment.atomicTransfertMethods[0].outputContainerUseds[0].locationOnContainerSupport.code;
		
		$scope.$emit('viewRefeshed');
	});
	
	$scope.$on('cancel', function(e) {
		console.log("call event cancel");
		
		$scope.atmService.data.cancel();	
		if($scope.isCreationMode()){
			var dtConfig = $scope.atmService.data.getConfig();
			dtConfig.edit.byDefault = false;
			$scope.atmService.data.setConfig(dtConfig);
		}
	});
	
	$scope.$on('activeEditMode', function(e) {
		console.log("call event activeEditMode");
		
		$scope.atmService.data.selectAll(true);
		$scope.atmService.data.setEdit();
	});	
	
	// FDS 12/03/2019==> il n'y a pas  ces 3 lignes de code qu'on trouve partout ailleurs !!!!!
	//   var atm = dataMain[i].atomicTransfertMethod;
    //	 var newContainerCode = outputContainerSupportCode+"_"+atm.line + atm.column;
    //	 $parse('outputContainerUsed.code').assign(dataMain[i],newContainerCode);
	
	var copyContainerSupportCodeAndStorageCodeToDT = function(datatable){

		var dataMain = datatable.getData();
		var outputContainerSupportCode = $scope.outputContainerSupport.code;
		var outputContainerSupportStorageCode = $scope.outputContainerSupport.storageCode;
		
		// 26/03/2019 correction locale pour NGl-2371: ajout && "" !=outputContainerSupportCode
		if ( null != outputContainerSupportCode && undefined != outputContainerSupportCode && "" !=outputContainerSupportCode){
			for(var i = 0; i < dataMain.length; i++){

				if($scope.experiment.instrument.outContainerSupportCategoryCode !== "tube"){
					$parse('outputContainerUsed.locationOnContainerSupport.code').assign(dataMain[i],outputContainerSupportCode);
				}
				
				if( null != outputContainerSupportStorageCode && undefined != outputContainerSupportStorageCode){
				    $parse('outputContainerUsed.locationOnContainerSupport.storageCode').assign(dataMain[i],outputContainerSupportStorageCode);
				}
			}
		}
	}
	
	var atmService = atmToSingleDatatable($scope, datatableConfig);
	
	// line et column sont indefinis au demarrage pour les plaques,ce sont les valeurs definies par l'utilisateur dans le datatable qui sont
	// positionnees dans copyContainerSupportCodeAndStorageCodeToDT au momemt de la sauvegarde
	atmService.newAtomicTransfertMethod = function(l,c){
		return {
			class:"OneToMany",
			line:($scope.experiment.instrument.outContainerSupportCategoryCode !== "tube")?undefined:"1",
			column:($scope.experiment.instrument.outContainerSupportCategoryCode !== "tube")?undefined:"1",
			inputContainerUseds:new Array(0),
			outputContainerUseds:new Array(0)
		};		
	};
	
	$scope.outputContainerSupport = { code : null , storageCode : null};
	
	if ( undefined !== $scope.experiment.atomicTransfertMethods[0]) { 
		 $scope.outputContainerSupport.code=$scope.experiment.atomicTransfertMethods[0].outputContainerUseds[0].locationOnContainerSupport.code;
		 //console.log("previous code: "+ $scope.outputContainerSupport.code);
	}
	if ( undefined !== $scope.experiment.atomicTransfertMethods[0]) {
		$scope.outputContainerSupport.storageCode=$scope.experiment.atomicTransfertMethods[0].outputContainerUseds[0].locationOnContainerSupport.storageCode;
		//console.log("previous storageCode: "+ $scope.outputContainerSupport.storageCode);
	}
	
	// Cas particulier 1 input=> 2 outputs
	// NGL-2211 27/08/2018 supprimer la propriété inputVolume
	atmService.addNewAtomicTransfertMethodsInDatatable = function(){
		if(null != mainService.getBasket() && null != mainService.getBasket().get() && this.isAddNew){
			$that = this;
			
			var type = $that.newAtomicTransfertMethod().class;
			
			$that.$commonATM.loadInputContainerFromBasket(mainService.getBasket().get())
				.then(function(containers) {								
					var allData = [], i = 0;
					
					if($that.data.getData() !== undefined && $that.data.getData().length > 0){
						allData = $that.data.getData();
						i = allData.length;
					}
					
					// chaque input donne 2 outputs
					angular.forEach(containers, function(container){
						var tmpLine = {};
						tmpLine.atomicTransfertMethod = $that.newAtomicTransfertMethod(container.support.line, container.support.column);
						tmpLine.atomicIndex=i/2;i=i+2;
						//console.log("i="+i + "/ atomicIndex="+tmpLine.atomicIndex);	
						
						tmpLine.inputContainer = container;
						tmpLine.inputContainerUsed = $that.$commonATM.convertContainerToInputContainerUsed(tmpLine.inputContainer);
						
						//12/06/2018 Julie pense qu'il est mieux de forcer le libProcessTypeCode      
						//utiliser le code comme value
						var libProcessTypeCode =["FHO","FHB"];
						
						for(var j = 0; j < libProcessTypeCode.length ; j++){
							var line = {};
							
							line.atomicTransfertMethod = tmpLine.atomicTransfertMethod;
							line.atomicIndex = tmpLine.atomicIndex;
							line.inputContainer = tmpLine.inputContainer;
							line.inputContainerUsed = tmpLine.inputContainerUsed;
							line.outputContainerUsed = $that.$commonATM.newOutputContainerUsed($that.defaultOutputUnit,$that.defaultOutputValue,line.atomicTransfertMethod.line,
									line.atomicTransfertMethod.column,line.inputContainer);
							
							// volume et quantité automatiques (modif de qté reste possible par GUI)
							// NGL-2211 27/08/2018 suppression propriété volume engagé (=inputVolume), on peut plus non plus setter la quantité engagée(=inputQuantity)
							//var engagedVol = container.volume.value / 2;
							//var engagedQuant= container.concentration.value * engagedVol;
							//console.log("engagedVol="+engagedVol + "/ engagedQuant="+engagedQuant);
							
							//var setter = $parse("experimentProperties.inputVolume.value").assign;
							//setter(line.inputContainerUsed, engagedVol);
							
							//setter = $parse("experimentProperties.inputQuantity.value").assign;
							//setter(line.inputContainerUsed, engagedQuant);
							
							//12/06/2018 Julie pense qu'il vaut mieux forcer le libProcessTypeCode (mais reste modifiable) 
							var libValue =libProcessTypeCode[j]

							//utiliser le code comme value !!!
							setter = $parse("experimentProperties.libProcessTypeCode.value").assign;
							setter(line.outputContainerUsed, libValue);
							
							line.outputContainer = undefined;
							allData.push(line);
						}						
					});
					
					allData = $filter('orderBy')(allData,'inputContainer.support.code');
					$that.data.setData(allData, allData.length);											
			});
		}					
	};
	
	//defined default output unit
	atmService.defaultOutputUnit = {
			volume : "µL",
			quantity:"ng"
	};
	
	// NGL-1350 aide a la saisie des index
	// !! les surcharges doivent etre faites avant experimentToView 
	atmService.convertOutputPropertiesToDatatableColumn = function(property, pName){
		var column = atmService.$commonATM.convertTypePropertyToDatatableColumn(property,"outputContainerUsed."+pName+".",{"0":Messages("experiments.outputs")});
		if(property.code=="tag"){
			// amelioration: afficher le nom aux utilisateurs et pas le code 
			//column.editTemplate='<input class="form-control" type="text" #ng-model typeahead="tag.code as tag.code for tag in getTags() | filter:{groupNames:selectedTagGroup.value} | filter:{code:$viewValue} | limitTo:20" typeahead-min-length="1" udt-change="updatePropertyFromUDT(value,col)"/>';  
			//column.editTemplate='<input class="form-control" type="text" #ng-model typeahead="tag.code as tag.name for tag in getTags() | filter:{groupNames:selectedTagGroup.value} | filter:{name:$viewValue} | limitTo:20" typeahead-min-length="1" udt-change="updatePropertyFromUDT(value,col)"/>'; 
			// NGL-2246: utiliser bt-select au lieu input / GA: for tag in <variable> au lieu de for tag in <function>
			// NGL-2304: initialiser la liste de tous les index: marche mais ajoute du temps au lancement de la page, utilise fonction locale getTags(); voir plus bas
			//           column.editTemplate='<div class="form-control" bt-select  #ng-model filter="true" bt-options="tag.code as tag.name for tag in getTags()" udt-change="updatePropertyFromUDT(value,col)" /></div>';
			//column.editTemplate='<div class="form-control" bt-select  #ng-model filter="true" bt-options="tag.code as tag.name for tag in tags" udt-change="updatePropertyFromUDT(value,col)" /></div>';
			column.editTemplate='<div class="form-control" bt-select  #ng-model filter="true" bt-options="tag.code as tag.name for tag in lists.getTags()" udt-change="updatePropertyFromUDT(value,col)" /></div>';
			
		}
		return column;
	};
	
	atmService.experimentToView($scope.experiment, $scope.experimentType);
	
	/// TODO ???? limiter a 48 inputContainers dans le cas ou le support est une plaque!!!  sinon on ne peut pas generer 2 output par input !!!
	$scope.atmService = atmService;
	
	// importer un fichier definissant quels index sont déposés dans quels containers
	     // pas demandé pour l'instant...
	
	// Autre mode possible : utiliser une plaque d'index prédéfinis, l'utilisateur a juste a indiquer a partir de quelle colonne de cette plaque le robot doit prelever les index
	     // pas demandé pour l'instant...
	
	// 31/08/2018 NGL-1350 aide a la saisie des tags
	// appeller initTags() isNewState() necessaire sinon ils ne sont pas initialisés au moment du chgt etat de l'experience a InProgress
	if ( $scope.isNewState() || $scope.isInProgressState() || Permissions.check("admin") ){
	   tagPlates.initTags();
	   
	   //$scope.tags = tagPlates.getAllTags(); //ne peut pas marcher tant que la promise dans initTags() n'est pas finie, d'ou le patch NGL-2304
	   
	   $scope.getTagGroups= function(){return tagPlates.getAllTagGroups()};
	   $scope.selectedTagGroup= $scope.getTagGroups()[0]; // valeur defaut du select (qui maintenant existe car definie sans attendre le retour de la promise
	}
 
	/* NGL-2304 a utiliser avec la modification  "column.editTemplate" plus haut ...
	$scope.getTags = function(){
		if($scope.tags==undefined)
			$scope.tags = tagPlates.getAllTags();
		return $scope.tags;
	}*/
	
	/* cette declaration marche, sauf pour l'initialisition du choix "---" de tous les index */
	//$scope.tags = tagPlates.getAllTags();
	
	$scope.selectGroup = {
			isShow:function(){
				// NGL-2246 afficher dès l'etat Nouveau mais pour l'instant un bug existe: la liste de tous les tags n'est pas initialisée !!
				return ( $scope.isNewState() || $scope.isInProgressState() || Permissions.check("admin") );
			},
			// NGL-2246 ajout / GA recuperer ici l'objet groupName
		    select:function(groupName){       	
		    	console.log( 'groupe choisi :'+  groupName.value );
		    	
	        	//GA: creer une variable $scope.tags au lieu de d'ecraser la fonction getTags
	        	if (groupName.value === undefined ){ 
        			//$scope.tags = tagPlates.getAllTags() //!! L'affichage de TOUS les index dans le bt-select qui est long...
        			$scope.lists.refresh.tags({typeCodes:['index-illumina-sequencing','index-nanopore-sequencing']});
        		} else { 
        			//$scope.tags = $filter('filter')(tagPlates.getAllTags(),{groupNames:groupName.value}, true);
        			$scope.lists.refresh.tags({typeCodes:['index-illumina-sequencing','index-nanopore-sequencing'],groupNames:[groupName.value]});
        		}
		    }
	};
	
	// 26/06/2018 pour selection manuelle d'index
	$scope.updatePropertyFromUDT = function(value, col){
		//console.log("update from property : "+col.property);
		if(col.property === 'outputContainerUsed.experimentProperties.tag.value'){
			tagPlates.computeTagCategory(value.data);
		}
	}
}]);