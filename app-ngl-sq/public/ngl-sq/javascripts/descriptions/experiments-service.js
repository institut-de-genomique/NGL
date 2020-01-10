"use strict";
 
 angular.module('ngl-sq.descriptionsServices.experiments', []).
    factory('descriptionsExperimentsSearchService', ['$http', 'mainService', 'lists', 'datatable', 
        function($http,   mainService,   lists,   datatable){

            var getColumns = function(index){
                var columns = [];
    
                if(!index || index === "details"){
                    columns.push({
                        "header":Messages("descriptions.experiments.table.atomic.transfert.method"),
                        "property":"experimentType.atomicTransfertMethod",
                        "order":true,
                        "hide":false,
                        "position":1,
                        "type":"text",
                        "mergeCells":true,
                    });
                    columns.push({
                        "header":Messages("descriptions.experiments.table.category.code"),
                        "property":"experimentType.category.code",
                        "order":true,
                        "hide":false,
                        "position":2,
                        "type":"text",
                        "mergeCells":true,
                    });
                    columns.push({
                        "header":Messages("descriptions.experiments.table.type.name"),
                        "property":"experimentType.name",
                        "order":false,
                        "hide":true,
                        "position":3,
                        "type":"text"    
                    });
                    columns.push({
                        "header":Messages("descriptions.experiments.table.type.code"),
                        "property":"experimentType.code",
                        "order":false,
                        "hide":true,
                        "position":4,
                        "type":"text",
                    });
                    columns.push({
                        "header":Messages("descriptions.experiments.table.do.transfert"),
                        "property":"doTransfert",
                        "order":false,
                        "hide":true,
                        "position":5,
                        "type":"boolean",
                    });
                    columns.push({
                        "header":Messages("descriptions.experiments.table.mandatory.transfert"),
                        "property":"mandatoryTransfert",
                        "order":false,
                        "hide":true,
                        "position":6,
                        "type":"boolean",
                    });
                    columns.push({
                        "header":Messages("descriptions.experiments.table.do.quality.control"),
                        "property":"doQualityControl",
                        "order":false,
                        "hide":true,
                        "position":7,
                        "type":"boolean",
                    });
                    columns.push({
                        "header":Messages("descriptions.experiments.table.mandatory.quality.control"),
                        "property":"mandatoryQualityControl",
                        "order":false,
                        "hide":true,
                        "position":8,
                        "type":"boolean",
                    });
                    columns.push({
                        "header":Messages("descriptions.experiments.table.do.purification"),
                        "property":"doPurification",
                        "order":false,
                        "hide":true,
                        "position":9,
                        "type":"boolean",
                    });
                    columns.push({
                        "header":Messages("descriptions.experiments.table.mandatory.purification"),
                        "property":"mandatoryPurification",
                        "order":false,
                        "hide":true,
                        "position":10,
                        "type":"boolean",
                    });
                } else if(index === "properties"){
                    columns.push({
                        "header":Messages("descriptions.experiments.table.type.name"),
                        "property":"typeName",
                        "order":true,
                        "hide":false,
                        "position":1,
                        "type":"text" ,
                        "mergeCells":true,  
                    });
                    columns.push({
                        "header":Messages("descriptions.experiments.table.type.code"),
                        "property":"typeCode",
                        "order":true,
                        "hide":false,
                        "position":2,
                        "type":"text",
                        "mergeCells":true,
                    }); 
                    columns.push({
                        "header":Messages("descriptions.experiments.table.properties.definitions.name"),
                        "property":"name",
                        "order":false,
                        "hide":true,
                        "position":3,
                        "type":"text"    
                    });
                    columns.push({
                        "header":Messages("descriptions.experiments.table.properties.definitions.code"),
                        "property":"code",
                        "order":false,
                        "hide":true,
                        "position":4,
                        "type":"text",
                    }); 
                    columns.push({
                        "header":Messages("descriptions.experiments.table.properties.definitions.levels.code"),
                        "property":"levels",
                        "order":false,
                        "hide":true,
                        "position":5,
                        "type":"text",
                        "render":"<div list-resize='cellValue | getArray:\"code\"' list-resize-min-size='3'>",
                    });
                    columns.push({
                        "header":Messages("descriptions.experiments.table.properties.definitions.required"),
                        "property":"required",
                        "order":false,
                        "hide":true,
                        "position":6,
                        "type":"boolean"    
                    });
                    columns.push({
                        "header":Messages("descriptions.experiments.table.properties.definitions.value.type"),
                        "property":"valueType",
                        "order":false,
                        "hide":true,
                        "position":7,
                        "type":"text",
                    });
                    columns.push({
                        "header":Messages("descriptions.experiments.table.properties.definitions.display.format"),
                        "property":"displayFormat",
                        "order":false,
                        "hide":true,
                        "position":8,
                        "type":"text",
                    });
                    columns.push({
                        "header":Messages("descriptions.experiments.table.properties.definitions.display.order"),
                        "property":"displayOrder",
                        "order":false,
                        "hide":true,
                        "position":9,
                        "type":"text",
                    });
                    columns.push({
                        "header":Messages("descriptions.experiments.table.properties.definitions.value.default"),
                        "property":"defaultValue",
                        "order":false,
                        "hide":true,
                        "position":10,
                        "type":"text",
                    });
                    columns.push({
                        "header":Messages("descriptions.experiments.table.properties.definitions.choice"),
                        "property":"choiceInList",
                        "order":false,
                        "hide":true,
                        "position":11,
                        "type":"boolean",
                    });             
                } else if(index === "instruments"){
                    columns.push({
                        "header":Messages("descriptions.experiments.table.type.name"),
                        "property":"name",
                        "order":true,
                        "hide":false,
                        "position":1,
                        "type":"text" ,
                        "mergeCells":true,  
                    });
                    columns.push({
                        "header":Messages("descriptions.experiments.table.instrument.type.name"),
                        "property":"typeName",
                        "order":true,
                        "hide":false,
                        "position":2,
                        "type":"text" ,
                        "mergeCells":true,  
                    });
                    columns.push({
                        "header":Messages("descriptions.experiments.table.instrument.type.code"),
                        "property":"typeCode",
                        "order":true,
                        "hide":false,
                        "position":3,
                        "type":"text" ,
                        "mergeCells":true,  
                    });
                    columns.push({
                        "header":Messages("descriptions.experiments.table.in.support.name"),
                        "property":"inSupportName",
                        "order":false,
                        "hide":true,
                        "position":4,
                        "type":"text" ,
                    });
                    columns.push({
                        "header":Messages("descriptions.experiments.table.in.support.code"),
                        "property":"inSupportCode",
                        "order":false,
                        "hide":true,
                        "position":5,
                        "type":"text" ,
                    });
                    columns.push({
                        "header":Messages("descriptions.experiments.table.in.category.name"),
                        "property":"inCategoryName",
                        "order":false,
                        "hide":true,
                        "position":6,
                        "type":"text" ,
                    });
                    columns.push({
                        "header":Messages("descriptions.experiments.table.in.category.code"),
                        "property":"inCategoryCode",
                        "order":false,
                        "hide":true,
                        "position":7,
                        "type":"text" ,
                    });
                    columns.push({
                        "header":Messages("descriptions.experiments.table.in.nb.column"),
                        "property":"inNbCol",
                        "order":false,
                        "hide":true,
                        "position":8,
                        "type":"text" ,
                    });
                    columns.push({
                        "header":Messages("descriptions.experiments.table.in.nb.line"),
                        "property":"inNbLine",
                        "order":false,
                        "hide":true,
                        "position":9,
                        "type":"text" ,
                    });
                    columns.push({
                        "header":Messages("descriptions.experiments.table.in.nb.container"),
                        "property":"inNbContainer",
                        "order":false,
                        "hide":true,
                        "position":10,
                        "type":"text" ,
                    });
                    columns.push({
                        "header":Messages("descriptions.experiments.table.out.support.name"),
                        "property":"outSupportName",
                        "order":false,
                        "hide":true,
                        "position":11,
                        "type":"text" ,
                    });
                    columns.push({
                        "header":Messages("descriptions.experiments.table.out.support.code"),
                        "property":"outSupportCode",
                        "order":false,
                        "hide":true,
                        "position":12,
                        "type":"text" ,
                    });
                    columns.push({
                        "header":Messages("descriptions.experiments.table.out.category.name"),
                        "property":"outCategoryName",
                        "order":false,
                        "hide":true,
                        "position":13,
                        "type":"text" ,
                    });
                    columns.push({
                        "header":Messages("descriptions.experiments.table.out.category.code"),
                        "property":"outCategoryCode",
                        "order":false,
                        "hide":true,
                        "position":14,
                        "type":"text" ,
                    });
                    columns.push({
                        "header":Messages("descriptions.experiments.table.out.nb.column"),
                        "property":"outNbCol",
                        "order":false,
                        "hide":true,
                        "position":15,
                        "type":"text" ,
                    });
                    columns.push({
                        "header":Messages("descriptions.experiments.table.out.nb.line"),
                        "property":"outNbLine",
                        "order":false,
                        "hide":true,
                        "position":16,
                        "type":"text" ,
                    });
                    columns.push({
                        "header":Messages("descriptions.experiments.table.out.nb.container"),
                        "property":"outNbContainer",
                        "order":false,
                        "hide":true,
                        "position":17,
                        "type":"text" ,
                    });
                } else if(index === "previous"){
                    columns.push({
                        "header":Messages("descriptions.experiments.table.type.name"),
                        "property":"experimentTypeName",
                        "order":true,
                        "hide":false,
                        "position":1,
                        "type":"text",
                        "mergeCells":true,  
                    });
                    columns.push({
                        "header":Messages("descriptions.experiments.table.previous.exp.type.name"),
                        "property":"previousName",
                        "order":false,
                        "hide":false,
                        "position":2,
                        "type":"text" ,
                    });
                    columns.push({
                        "header":Messages("descriptions.experiments.table.previous.exp.type.code"),
                        "property":"previousCode",
                        "order":false,
                        "hide":false,
                        "position":3,
                        "type":"text" ,
                    });
                } 
                return columns;
            };

            var getTableColumns = function(index, data){

                var columns = [];
                columns.push({
                    "header":Messages("descriptions.experiments.table.type.name"),
                    "property":"name",
                    "order":true,
                    "hide":false,
                    "position":1,
                    "type":"text" ,
                });
                columns.push({
                    "header":Messages("descriptions.experiments.table.type.code"),
                    "property":"code",
                    "order":true,
                    "hide":false,
                    "position":2,
                    "type":"text" , 
                });
                data.forEach(function(col) {
                    columns.push({
                        "header":col.code,
                        "property":col.code,
                        "order":true,
                        "hide":true,
                        "position":3,
                        "type":"boolean" , 
                        "extraHeaders":{0:col.name},
                    });
                    
                });
                return columns;
            };
            
            var searchService = {
                    getColumns:getColumns,
                    getDefaultColumns:getColumns,
                    datatable:undefined,
                    isRouteParam:false,
                    lists : lists,
                    sublists: {},
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
                    
                    search : function(){
                        mainService.setForm(this.form);				
                        this.datatable.search(this.form);
                        this.clearSublists();
                    },
    
                    clearSublists : function() {
                        this.sublists.details = null;
                        this.sublists.properties = null;
                        this.sublists.instruments = null;
                        this.sublists.previous = null;
                        this.sublists.table = null;
                        this.sublists.tableColumns = null;
                    },

                    generateTableColumns: function(data) {
                        var columnSet = new Set();
                        var tableColumns = [];
                        data.forEach(function(expTypeNode) {
                            expTypeNode.experimentType.instrumentUsedTypes.forEach(function(instrUsed){
                                if(!columnSet.has(instrUsed.code)){
                                    tableColumns.push({
                                        "name": instrUsed.name,
                                        "code": instrUsed.code,
                                    });
                                    columnSet.add(instrUsed.code);
                                }
                            });
                        });
                        return tableColumns;
                    },

                    generateTableSubList : function() {

                        if(!(this.sublists.table && this.sublists.tableColumns)) {
                            var data = this.datatable.getData();
                            var tableColumns = this.generateTableColumns(data);
                            var table = [];
                            var nonEmptyColumns = new Set();
                            data.forEach(function(expTypeNode) {
                                var expType = expTypeNode.experimentType;
                                var nonEmptyRow = false;
                                var obj = {
                                    "name": expType.name,
                                    "code": expType.code,
                                };
                                tableColumns.forEach(function(col){
                                    var isCodePresent = expType.instrumentUsedTypes.map(x => x.code).indexOf(col.code) >= 0;
                                    obj[col.code] = isCodePresent;
                                    if(isCodePresent){
                                        nonEmptyRow = true;
                                        nonEmptyColumns.add(col.code);
                                    }
                                });
                                if(nonEmptyRow){
                                    table.push(obj);
                                }
                            });
                            this.sublists.tableColumns = tableColumns.filter(x => nonEmptyColumns.has(x.code));
                            this.sublists.table = table;
                        }
                    },
    
                    generateSubLists : function() {
    
                        if(!(this.sublists.details && this.sublists.properties && this.sublists.instruments)){
                            var details = [];
                            var properties = [];
                            var instruments = [];
                            var previous = [];
                            this.datatable.getData().forEach(function(expTypeNode) {
                                var expType = expTypeNode.experimentType;
                                details.push({
                                    "experimentType": {
                                        "atomicTransfertMethod": expType.atomicTransfertMethod,
                                        "category": {
                                            "code": expType.category.code,
                                        }, 
                                        "name": expType.name, 
                                        "code": expType.code,
                                    },
                                    "doPurification": expTypeNode.doPurification,
                                    "mandatoryPurification": expTypeNode.mandatoryPurification,
                                    "doQualityControl": expTypeNode.doQualityControl,
                                    "mandatoryQualityControl": expTypeNode.mandatoryQualityControl,
                                    "doTransfert": expTypeNode.doTransfert,
                                    "mandatoryTransfert": expTypeNode.mandatoryTransfert,
                                });
                                expType.propertiesDefinitions.forEach(function(propdef){
                                    properties.push({
                                        "typeName": expType.name, 
                                        "typeCode": expType.code, 
                                        "name": propdef.name, 
                                        "code": propdef.code, 
                                        "required": propdef.required, 
                                        "levels": propdef.levels,
                                        "valueType": propdef.valueType,
                                        "defaultValue": propdef.defaultValue,
                                        "possibleValues": propdef.possibleValues,
                                        "displayFormat": propdef.displayFormat,
                                        "displayOrder": propdef.displayOrder,
                                        "choiceInList": propdef.choiceInList,
                                    });
                                });
                                expType.instrumentUsedTypes.forEach(function(instrUsed){
                                    var imax = Math.max(instrUsed.inContainerSupportCategories.length, instrUsed.outContainerSupportCategories.length);
                                    for(var i=0; i<imax; i++){
                                        var obj = {
                                            "name": expType.name,
                                            "typeName": instrUsed.name,
                                            "typeCode": instrUsed.code,
                                        }
                                        if(instrUsed.inContainerSupportCategories.length > i) {
                                            obj["inSupportName"] = instrUsed.inContainerSupportCategories[i].name
                                            obj["inSupportCode"] = instrUsed.inContainerSupportCategories[i].code
                                            obj["inCategoryName"] = instrUsed.inContainerSupportCategories[i].containerCategory.name
                                            obj["inCategoryCode"] = instrUsed.inContainerSupportCategories[i].containerCategory.code
                                            obj["inNbCol"] = instrUsed.inContainerSupportCategories[i].nbColumn
                                            obj["inNbLine"] = instrUsed.inContainerSupportCategories[i].nbLine
                                            obj["inNbContainer"] = instrUsed.inContainerSupportCategories[i].nbUsableContainer
                                        }
                                        if(instrUsed.outContainerSupportCategories.length > i) {
                                            obj["outSupportName"] = instrUsed.outContainerSupportCategories[i].name
                                            obj["outSupportCode"] = instrUsed.outContainerSupportCategories[i].code
                                            obj["outCategoryName"] = instrUsed.outContainerSupportCategories[i].containerCategory.name
                                            obj["outCategoryCode"] = instrUsed.outContainerSupportCategories[i].containerCategory.code
                                            obj["outNbCol"] = instrUsed.outContainerSupportCategories[i].nbColumn
                                            obj["outNbLine"] = instrUsed.outContainerSupportCategories[i].nbLine
                                            obj["outNbContainer"] = instrUsed.outContainerSupportCategories[i].nbUsableContainer
                                        }
                                        instruments.push(obj);
                                    }
                                });
                                expTypeNode.previousExperimentTypes.forEach(function(prevExpType){
                                    previous.push({
                                        "experimentTypeName": expType.name,
                                        "previousName": prevExpType.name,
                                        "previousCode": prevExpType.code,
                                    })
                                });
                            });
                            this.sublists.details = details;
                            this.sublists.properties = properties;
                            this.sublists.instruments = instruments;
                            this.sublists.previous = previous;
                        }
                    },
                    
                    changeTab : function(index){

                        this.generateSubLists();
                        this.generateTableSubList();

                        if(index === "table"){
                            this.datatable.setData(this.sublists.table, this.sublists.table.length);
                            this.datatable.setColumnsConfig(getTableColumns(index, this.sublists.tableColumns));
                        } else {
                            if(index === "details"){
                                this.datatable.setData(this.sublists.details, this.sublists.details.length);
                            } else if(index === "properties"){
                                this.datatable.setData(this.sublists.properties, this.sublists.properties.length);
                            } else if(index === "instruments"){
                                this.datatable.setData(this.sublists.instruments, this.sublists.instruments.length);
                            } else if(index === "previous"){
                                this.datatable.setData(this.sublists.previous, this.sublists.previous.length);
                            }
                            this.datatable.setColumnsConfig(getColumns(index));
                        }
                    },
                    
                    /**
                     * initialise the service
                     */
                    init : function($routeParams, datatableConfig){
                        
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
]);                                