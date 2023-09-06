// 07/06/2018 adapté depuis small-and-large-rna-extraction pour la creation automatique de plusieurs output containerUsed par inputContainer
// 22/06/2018 utilisation de la factory tagPlates dans le module tools (tag-plate-helpers.js)
// 03/12/2020 NGL-3175 modifier de OneToMany => OneToOne
// 02/02/2022 NGL-3710 ajout tagService qui remprend certaines fonctions de tagPlates
angular.module('home').controller('OxBisseqAndBisseqCtrl',['$scope','$parse','$filter','atmToSingleDatatable','mainService','$http','tagPlates','tagService','lists',
                                                    function($scope, $parse,  $filter,  atmToSingleDatatable,  mainService,  $http,  tagPlates, tagService, lists) {

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
			 			"position":3,
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
			 			"position":4,
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
	
	//  $scope.plateFluoUtils est dans details-ctrl.js; details-ctrl.js est deja appelé/inclus par ????
	var inputSupportCategoryCode = $scope.plateFluoUtils.getSupportCategoryCode();
	// retourne 'tube', 'plate-96-well' ou 'mixte'
	console.log ('INPUT='+inputSupportCategoryCode );
	
	if ( inputSupportCategoryCode !== "tube") {
		datatableConfig.columns.push({
			"header" : Messages("containers.table.supportCode"),
			"property" : "inputContainer.support.code",
			"order" : true,
			"type" : "text",
			"position" : 1,
			//"mergeCells" : true,
			"extraHeaders" : {0: inputExtraHeaders}
		});
		datatableConfig.columns.push({
			// Ligne
			"header":Messages("containers.table.support.line"),
			"property":"inputContainer.support.line",
			"order":true,
			"hide":true,
			"type":"text",
			"position":2,
			"extraHeaders":{0: inputExtraHeaders}
		});
		datatableConfig.columns.push({
			// colonne
			"header":Messages("containers.table.support.column"),
			// astuce GA: pour pouvoir trier les colonnes dans l'ordre naturel forcer a numerique.=> type:number,   property:  *1
			"property":"inputContainer.support.column*1",
			"order":true,
			"hide":true,
			"type":"number",
			"position":2.5,
			"extraHeaders":{0: inputExtraHeaders}
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
	
	// FDS 12/03/2019==> remplace  les 3 lignes differentes dans copyContainerSupportCodeAndStorageCodeToDT ????????
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
		
		// 26/03/2019 correction locale pour NGL-2371: ajout && "" != outputContainerSupportCode
		if ( null != outputContainerSupportCode && undefined != outputContainerSupportCode && "" != outputContainerSupportCode){
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

	//Init 
	var atmService = atmToSingleDatatable($scope, datatableConfig);

	// line et column sont indefinis au demarrage pour les plaques, ce sont les valeurs definies par l'utilisateur dans le datatable qui sont
	// positionnees dans copyContainerSupportCodeAndStorageCodeToDT au momemt de la sauvegarde
	// NGL-3175 03/12/2020 modif OneToMany => OneToOne
	atmService.newAtomicTransfertMethod = function(l,c){
		return {
			class:"OneToOne",
			// 20/02/2020 attention si tube en sortie forcer l=1 et c=1 !!!
			line:($scope.experiment.instrument.outContainerSupportCategoryCode !== "tube")?undefined:"1",
			column:($scope.experiment.instrument.outContainerSupportCategoryCode !== "tube")?undefined:"1",
			inputContainerUseds:new Array(0),
			outputContainerUseds:new Array(0)
		};
	};
	
	//defined default output unit
	atmService.defaultOutputUnit = {
			volume : "µL",
			quantity:"ng"
	};
	
/*===== 02/12/2020 NGL-3175 l'experience change de OneToMany a OneToOne
	        le code ci dessous est spécifique !!! garder au cas ou....
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
*/
	
	// NGL-1350 aide a la saisie des index
	// !! les surcharges doivent etre faites avant experimentToView 
	atmService.convertOutputPropertiesToDatatableColumn = function(property, pName){
		var column = atmService.$commonATM.convertTypePropertyToDatatableColumn(property,"outputContainerUsed."+pName+".",{"0":Messages("experiments.outputs")});
		if(property.code==="tag"){
			column.editTemplate='<div class="form-control" bt-select  #ng-model filter="true" bt-options="tag.code as tag.name for tag in lists.getTags()" udt-change="updatePropertyFromUDT(value,col)" /></div>';
		}
		return column;
	};

	atmService.experimentToView($scope.experiment, $scope.experimentType);
	
	//02/09/2021 blocker la sortie strip-8 ( il n'y a que l'instrument 'main' )
	if ($scope.experiment.instrument.outContainerSupportCategoryCode === "strip-8"){
		$scope.messages.setError(Messages('experiments.input.error.categoryCodeNotSupported'));		
	} else {
		$scope.messages.clear();
		$scope.atmService = atmService;
	}

	// pour le cas de sortie en plaque !!
	$scope.outputContainerSupport = { code : null , storageCode : null};
	
	if ( undefined !== $scope.experiment.atomicTransfertMethods[0]) { 
		 $scope.outputContainerSupport.code=$scope.experiment.atomicTransfertMethods[0].outputContainerUseds[0].locationOnContainerSupport.code;
		 //console.log("previous code: "+ $scope.outputContainerSupport.code);
	}
	if ( undefined !== $scope.experiment.atomicTransfertMethods[0]) {
		$scope.outputContainerSupport.storageCode=$scope.experiment.atomicTransfertMethods[0].outputContainerUseds[0].locationOnContainerSupport.storageCode;
		//console.log("previous storageCode: "+ $scope.outputContainerSupport.storageCode);
	}

	// importer un fichier definissant quels index sont déposés dans quels containers
	// pas demandé pour l'instant...

	// Autre mode possible : utiliser une plaque d'index prédéfinis, l'utilisateur a juste a indiquer a partir de quelle colonne de cette plaque le robot doit prelever les index
	// 01/09/2021 NGL-3519: ajouter la plaque Nuo96
	$scope.columns = [ {name:'---', position: undefined },
		{name:'1', position:0}, {name:'2', position:8}, {name:'3', position:16}, {name:'4',  position:24}, {name:'5',  position:32}, {name:'6',  position:40},
		{name:'7', position:48},{name:'8', position:56},{name:'9', position:64}, {name:'10', position:72}, {name:'11', position:80}, {name:'12', position:88}
		];

	$scope.tagPlateColumn = $scope.columns[0]; // defaut du select

	// 12/04/2018 NGL-2012 ne rien mettre par defaut !!!
	$scope.plates=[];
	$scope.plates.push( {name: "---",                                           tagCategory: undefined,      tags: undefined });
	$scope.plates.push( {name:"NUGEN Ovation Ultralow Methyl-Seq System 1-96",  tagCategory: "SINGLE-INDEX", tags: tagPlates.populateIndex_Nuo96() });

	$scope.tagPlate = $scope.plates[0]; // defaut du select
	
	// NGL-2972 04/08/2020 mémorisation des choix pour le nettoyage éventuel....
	$scope.tagPlateToClean= undefined;
	$scope.tagPlateColumnToClean=undefined;

	// NGL-2012 :Ajouter les permissions pour admin; supprimer condition sur EditMode; NGL-2272 etre coherent, tous les boutons s'affichent au meme etat!!!
	$scope.selectColOrPlate = {
			isShow:function(){
				return ( $scope.isInProgressState() || Permissions.check("admin") );
			},	
			select:function(){
				console.log ('scope.tagPlate.name ='+ $scope.tagPlate.name);
				console.log ('scope.tagPlateColumn.name ='+ $scope.tagPlateColumn.name);
				
				// NGL-2972 04/08/2020 nettoyage avec paramètres mémorisés
				if (($scope.tagPlate.name === "---") && ($scope.tagPlateColumn.name === "---")){
					return tagService.unsetTags ($scope.tagPlateToClean, $scope.tagPlateColumnToClean, atmService, $scope.messages)
				} else if ( ($scope.tagPlate.name !== "---") && ($scope.tagPlateColumn.name !== "---") ){
					// NGL-2944 02/06/2020 avant de mettre une nouvelle plaque nettoyer l'ancienne si existait !!!
					if ($scope.tagPlateToClean != undefined){
						tagService.unsetTags ($scope.tagPlateToClean, $scope.tagPlateColumnToClean, atmService, $scope.messages)
					}
					// il ne faudrait positionner ces 2 valeurs QUE si setTags s'est bien passée...!!!!!!
					$scope.tagPlateToClean=$scope.tagPlate;
					$scope.tagPlateColumnToClean= $scope.tagPlateColumn;

					return tagService.setTags($scope.tagPlate, $scope.tagPlateColumn, atmService, $scope.messages)
				} 
				// dans le dernier cas (1 seul des 2 selects est positionné=> ne rien faire)
			}
	};
	
	// 31/08/2018 NGL-1350 aide a la saisie des tags
	// appeller initTags() isNewState() necessaire sinon ils ne sont pas initialisés au moment du chgt etat de l'experience a InProgress
	// l'appel a tagService.initTags() est maintenant obligatoire pour l'assignation automatique de tagCategory
	if ( $scope.isNewState() || $scope.isInProgressState() || Permissions.check("admin") ){
	   tagService.initTags("index-illumina-sequencing");
	   
	   $scope.getTagGroups= function(){return tagService.getAllTagGroups()};
	   $scope.selectedTagGroup= $scope.getTagGroups()[0]; // valeur defaut du select (qui maintenant existe car definie sans attendre le retour de la promise
	}
	
	// fonctionnalité de choix d'index par groupes
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
					// 01/09/2021 supprimer les nanopores
        			$scope.lists.refresh.tags({typeCodes:'index-illumina-sequencing'});
        		} else { 
					// 01/09/2021 supprimer les nanopores
        			$scope.lists.refresh.tags({typeCodes:'index-illumina-sequencing',groupNames:[groupName.value]});
        		}
		    }
	};
	
	// 26/06/2018 pour selection manuelle d'index
	$scope.updatePropertyFromUDT = function(value, col){
		console.log("update from property : "+col.property);
		// 03/12/2020 ajout calcul automatique de la qty
		if(col.property === 'inputContainerUsed.experimentProperties.inputVolume.value'){
			computeQuantity(value.data);
		} else if (col.property === 'outputContainerUsed.experimentProperties.tag.value'){
			tagService.computeTagCategory(value.data);
		}
	};

	// 03/12/2020 ajout pour calcul automatique de la qty
	var computeQuantity = function(udtData){
		var getter = $parse("inputContainerUsed.experimentProperties.inputQuantity.value");

		var compute = {
				inputConc : $parse("inputContainerUsed.concentration.value")(udtData),
				inputVolume : $parse("inputContainerUsed.experimentProperties.inputVolume.value")(udtData),		
				isReady:function(){
					// traiter le cas ou il y a 1 des 2 valeurs (en general c'est la conc) est a 0
					return (this.inputConc >= 0 && this.inputVolume >= 0 );
				}
			};
		
		if(compute.isReady()){
			var result = $parse("inputConc * inputVolume")(compute);
			console.log("quant result = "+result);
			
			if(angular.isNumber(result) && !isNaN(result)){
				inputQuantity = Math.round(result*10)/10;				
			}else{
				inputQuantity = 0;
			}	
			getter.assign(udtData, inputQuantity);
			
		}else{
			console.log("Missing values to exec computeQuantity");
		}
	}

}]);