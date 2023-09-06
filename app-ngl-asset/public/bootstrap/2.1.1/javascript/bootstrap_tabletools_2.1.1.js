/**
 * @namespace Classes used by TableTools - allows the styles to be override easily.
 *   Note that when TableTools initialises it will take a copy of the classes object
 *   and will use its internal copy for the remainder of its run time.
 */
TableTools.classes = {
	"container": "btn-toolbar", 
	"buttons": {
		"normal": "btn",
		"disabled": "DTTT_disabled"
	},
	"collection": {
		"container": "btn-group",
		"background": "DTTT_collection_background",
		"buttons": {
			"normal": "btn",
			"disabled": "DTTT_disabled"
		}
	},
	"select": {
		"table": "DTTT_selectable",
		"row": "DTTT_selected"
	},
	"print": {
		"body": "DTTT_Print",
		"info": "DTTT_print_info",
		"message": "DTTT_PrintMessage"
	}
};
/**
 * overriding of this method to manage the btngroup
 */
TableTools.prototype._fnButtonBase = function ( o, bCollectionButton ){
	{
		var sTag, sLiner, sClass;
		var nButton,masterS; 
		masterS = this._fnGetMasterSettings();		
		/*start customisation */
		if(o.sExtends == "btngroup"){
			nButton = document.createElement( "div" );
			nButton.className = "btn-group";
			nButton.setAttribute('id', "ToolTables_"+this.s.dt.sInstance+"_"+masterS.buttonCounter );
		}else /*end custo */{
			if ( bCollectionButton )
			{
				sTag = o.sTag !== "default" ? o.sTag : this.s.tags.collection.button;
				sClass = this.classes.collection.buttons.normal;
			}
			else
			{
				sTag = o.sTag !== "default" ? o.sTag : this.s.tags.button;
				sClass = this.classes.buttons.normal;
			}

			nButton = document.createElement( sTag ),			
			nButton.className = sClass+" "+o.sButtonClass;
			nButton.setAttribute('id', "ToolTables_"+this.s.dt.sInstance+"_"+masterS.buttonCounter );
			nButton.innerHTML = o.sButtonText;
		}
		masterS.buttonCounter++;		
		return nButton;
	}
};
/**
 * Manage several button in the same btn-group class.
 * 
 */
TableTools.BUTTONS.btngroup =  $.extend( {}, TableTools.buttonBase, {
	"sButtonClass": "btn",        
    "sButtonText": "",
    "sDropdown" : false,
    "fnInit":	function ( nButton, oConfig ) {
    	if(!oConfig.sDropdown){
    		this._fnButtonDefinations( oConfig.aButtons, nButton );
    	}else{
    		if(oConfig.mainButton !== undefined){
    			this._fnButtonDefinations( new Array(oConfig.mainButton), nButton );
    			$(nButton).append('<a class="btn dropdown-toggle" data-toggle="dropdown" href="#">'
    		    		  +'<span class="caret"></span></a>');
    		}else{
    			$(nButton).append('<a class="btn dropdown-toggle" data-toggle="dropdown" href="#">'
    		    		 +oConfig.sButtonText+' <span class="caret"></span></a>');
    		}
    		
    		    		     		 
    		var ul = document.createElement( "ul" );  
    		ul.className = "dropdown-menu";
    		var buttonSet = oConfig.aButtons;
    		for ( var i=0, iLen=buttonSet.length ; i<iLen ; i++ )
    		{
    			var li = document.createElement( "li" );
    			var o = $.extend( {}, TableTools.BUTTONS[ buttonSet[i].sExtends ], true );
				var buttonDef = $.extend( o, buttonSet[i], true );
				var button = this._fnCreateButton( buttonDef, false );
				$(button).removeClass("btn");
				$(button).attr("href","#");
    			li.appendChild(button);
    			ul.appendChild(li);
    		}
    		$(nButton).append(ul);
    	}
    }
});


/**
 * Edit function for a datatable
 */
TableTools.BUTTONS.edit = $.extend( {}, TableTools.buttonBase, {
	"sButtonClass": "btn",        
    "sButtonText": "Edit",
	"fnClick": function( nButton, oConfig ) {
    	var currentDataTable = this.s.dt.oInstance;	    	
    	var oSettings = this.s.dt;
    	var datas=oSettings.aoData;
		var iRow, iLen;
		//if only one column is selected
		var columnSelected = oConfig.aTargets;
		var first = true;
		for ( iRow=0, iLen=datas.length ; iRow<iLen ; iRow++ )
    	{
			//edition mode
    		if ( datas[iRow]._DTTT_selected &&  (!datas[iRow]._DTTT_edited || columnSelected !== undefined))
    		{    			
    			var data = datas[iRow];
    			var _aDataOri = $.extend(true, {}, data._aData);    			
    			data._aDataOri = _aDataOri;
    			data._DTTT_edited = true;
    			//put each column in edition mode. call aoColumnDefs[colums].mRender    			
    			for (var iColumn=0; iColumn < oSettings.aoColumns.length; iColumn++ )
    			{
    				if( columnSelected === undefined || columnSelected === iColumn){
    					sDisplay = currentDataTable._fnGetCellData(iRow, iColumn, 'edit' );
    					$(currentDataTable._fnGetTdNodes(iRow )[iColumn]).html(sDisplay);
    					//edited headers
    					if(first && columnSelected !== undefined){
    						first = false;
    						oConfig.fnEnableEditColumn(columnSelected, oConfig, currentDataTable, oSettings);
    						//$(oSettings.aoColumns[columnSelected].nTh).html(sDisplay);
    					}
    				}
    			}
    			var currentTr = $(currentDataTable._fnGetTrNodes()[iRow]);
	    		this.fnDeselect(currentTr); //deselect row    			    			
    		}
    	}			
    },
    "fnEnableEditColumn" : function(columnIdex, oConfig, dataTable, oSettings){
    	var editheader = oConfig.fnGetEditHeader(dataTable, oSettings);
    	$(editheader).find("#editheadercol"+columnIdex).children().css('display','');
    	$(editheader).css('display','');
    },
    "fnGetEditHeader" : function(dataTable, oSettings){
    	var editheader = $(dataTable).find("#editheader");
    	if(editheader[0] === undefined){
    		$(dataTable).find("thead").append("<tr id='editheader' style='display:none'></tr>");
    		var editheader = $(dataTable).find("#editheader");
    		for (var columnIdex=0; columnIdex < oSettings.aoColumns.length; columnIdex++ ){
    			editheader.append("<th id='editheadercol"+columnIdex+"'/>");				
    			if( typeof oSettings.aoColumns[columnIdex].mRender === 'function'){
    				var editheadecol = $(editheader).children("#editheadercol"+columnIdex);
    				var htmleditcolumn = oSettings.aoColumns[columnIdex].mRender(null, 'edit', null); //warning we pass an empty full object and a null value
    				$(htmleditcolumn).css('display','none');
    				$(htmleditcolumn).find('input, select').on('keyup',function(event){
    					//improve selection	
    					$("[name="+event.target.name+"]").val(this.value)
    					});
    				editheadecol.append(htmleditcolumn);
    			}
			}
    	}
    	return editheader;
    }
});
/**
 * Save function for a datatable
 */
TableTools.BUTTONS.save = $.extend( {}, TableTools.buttonBase, {
	"sButtonClass": "btn",        
    "sButtonText": "Save",
    "url":"",
    "method":"POST",
    "playJSRoute":null,
	"fnClick": function( nButton, oConfig ) {
    	var oSettings = this.s.dt;
    	var datas=oSettings.aoData;
    	var dataTable = oSettings.oInstance;
		var iRow, iLen;
			
		for ( iRow=0, iLen=datas.length ; iRow<iLen ; iRow++ )
	    	{
				var data = datas[iRow];
				
	    		//edition mode
	    		if (data._DTTT_edited)
	    		{
	    			for (var iColumn=0 ; iColumn < oSettings.aoColumns.length ; iColumn++ )
	    			{
	    				var inputs = $(dataTable._fnGetTdNodes(iRow)[iColumn]).find("input, select");
	    				if(inputs.length == 1){
	    					dataTable._fnSetCellData( iRow, iColumn, inputs[0].value );	    //set the value in data	cache					    				
	    				}
	    			}
	    			
	    			var rowData = data._aData;
	    			//alert(JSON.stringify(rowData));
	    			var ajaxContext = {
	    					"data" : data,
	    					"iRow" : iRow,
	    					"oConfig": oConfig,
	    					"oSettings":oSettings
	    			}
	    			//ajax call
	    			$.ajax({
	    	        	  url: (oConfig.playJSRoute)?oConfig.playJSRoute.url:oConfig.url,
	    	        	  type: (oConfig.playJSRoute)?oConfig.playJSRoute.method:oConfig.method,
	    	        	  contentType: 'application/json;charset=UTF-8',
	    	        	  data: JSON.stringify(rowData),
	    	        	  context: ajaxContext
	    	          }).done(function( msg ) {
	    	        	  this.oConfig.fnSaveDoneCallback(this, msg);	    	        	  		        	 
	    	          }).fail(function( msg ){
	    	        	  //replace all the page by the error
	    	        	  if(msg.status == 400){
	    	        		  this.oConfig.fnSaveFailCallback(this, msg);
	    	        	  }else{
	    	        		  $("html").html(msg.responseText);
	    	        	  }
	    	          }).always(function(msg){
	    	        	  //alert( "Data Saved: " + msg );
	    	          });
	    	}
	    }			
    },
    "fnSaveDoneCallback":function(ajaxContext, msg){
    	var data = ajaxContext.data;
    	data._DTTT_edited = false;
    	data._aData = msg;
    	data.dataOri = null;
    	
    	var oSettings = ajaxContext.oSettings;
    	var dataTable = ajaxContext.oSettings.oInstance;    	
    	var iRow = ajaxContext.iRow;
    	for (var iColumn=0 ; iColumn < oSettings.aoColumns.length ; iColumn++ )
		{
			var inputs = $(dataTable._fnGetTdNodes(iRow)[iColumn]).find("input, select");
			if(inputs.length == 1){		
				var sDisplay = dataTable._fnGetCellData(iRow, iColumn, 'display' );
				$(dataTable._fnGetTdNodes(iRow )[iColumn]).html(sDisplay);	    				
			}
		}
    	var currentTr = $(dataTable._fnGetTrNodes()[iRow]);
		currentTr.removeClass("success error");	 //remove css class
		currentTr.addClass("success");
		$(dataTable).find("#editheader").css("display","none"); //remove column edit header
		$(dataTable).find("#editheader th >").css("display","none");
    },
    "fnSaveFailCallback":function(ajaxContext, msg){    	
    	var dataTable = ajaxContext.oSettings.oInstance; 
    	var oSettings = ajaxContext.oSettings;
    	var iRow = ajaxContext.iRow;
    	$(dataTable._fnGetTrNodes()[iRow]).addClass("error");
    	var errorMsg = $.parseJSON(msg.responseText);
    	for (var iColumn=0 ; iColumn < oSettings.aoColumns.length ; iColumn++ )
		{
			var inputs = $(dataTable._fnGetTdNodes(iRow)[iColumn]).find("input, select");
			if(inputs.length == 1){
				var input = inputs[0];
				//cree une fonction anonyme pour faire un appel dynamique à une propriété de errorMsg
				var tmpfunction = new Function("errorMsg", "return errorMsg."+input.name+";");
				if(tmpfunction(errorMsg) !== undefined){
					$(input).parent().addClass("error");
					//utilise un bootstrap popover pour l'affichage des erreurs
					$(input).popover({
						"title":"Error",
						"trigger":"focus",
						"placement":"bottom",
						"content":tmpfunction(errorMsg)
					});
				}
			}
		}    	
    }

});
/**
 * Cancel edition function for a datatable
 */
TableTools.BUTTONS.cancel = $.extend( {}, TableTools.buttonBase, {
	"sButtonClass": "btn",        
    "sButtonText": "Cancel",
	"fnClick": function( nButton, oConfig ) {
    	var dataTable = this.s.dt.oInstance;	    	
    	var oSettings = this.s.dt;
    	var datas=oSettings.aoData;
		var iRow, iLen;			
		for ( iRow=0, iLen=datas.length ; iRow<iLen ; iRow++ )
	    	{
				var data = datas[iRow];
	    		//edition mode
	    		if (data._DTTT_edited)
	    		{
	    			data._aData = data._aDataOri;
	    			data._aDataOri = null;	    			
	    			
	    			for (var iColumn=0 ; iColumn < oSettings.aoColumns.length ; iColumn++ )
	    			{
	    				var inputs = $(dataTable._fnGetTdNodes(iRow)[iColumn]).find("input, select");
	    				if(inputs.length == 1){
	    					var sDisplay = dataTable._fnGetCellData(iRow, iColumn, 'display' );
	    					$(dataTable._fnGetTdNodes(iRow )[iColumn]).html(sDisplay);	    					
	    				}
	    			}
	    			data._DTTT_edited = false;	    			
	    		}
	    		var currentTr = $(dataTable._fnGetTrNodes()[iRow]);
	    		currentTr.removeClass("success error");	 //remove css class
	    		this.fnDeselect(currentTr); //deselect row
	    		$(dataTable).find("#editheader").css("display","none"); //remove column edit header
	    		$(dataTable).find("#editheader th >").css("display","none");
	    	}			
    	},
        "fnSaveFailCallback":function(ajaxContext, msg){    	
        	var dataTable = ajaxContext.oSettings.oInstance; 
        	var oSettings = ajaxContext.oSettings;
        	var iRow = ajaxContext.iRow;
        	$(dataTable._fnGetTrNodes()[iRow]).addClass("error");
        	var errorMsg = $.parseJSON(msg.responseText);
        	for (var iColumn=0 ; iColumn < oSettings.aoColumns.length ; iColumn++ )
    		{
    			var inputs = $(dataTable._fnGetTdNodes(iRow)[iColumn]).find("input, select");
    			if(inputs.length == 1){
    				var input = inputs[0];
    				//cree une fonction anonyme pour faire un appel dynamique à une propriété de errorMsg
    				var tmpfunction = new Function("errorMsg", "return errorMsg."+input.name+";");
    				if(tmpfunction(errorMsg) !== undefined){
    					$(input).parent().addClass("error");
    					//utilise un bootstrap popover pour l'affichage des erreurs
    					$(input).popover({
    						"title":"Error",
    						"trigger":"focus",
    						"placement":"bottom",
    						"content":tmpfunction(errorMsg)
    					});
    				}
    			}
    		}    	
        }
});
/**
 * Delete function for a datatable
 */
TableTools.BUTTONS.remove = $.extend( {}, TableTools.buttonBase, {
	"sButtonClass": "btn btn-danger",        
    "sButtonText": "Delete",
    "idBtnModal":"confirm",
    "method":"DELETE",
    "titleModal":"Remove data",
    "texteModal":"Remove data ?",
    "modalId":"confirm_modal_delete",
	"textCancelModal":"Cancel",
	"objectField":"id",
    "manualModal":false,
    "playJSRoute":"",
	"fnClick": function( nButton, oConfig ) {
			var oTT = this.s.dt.oInstance;
			var oSettings = this.s.dt;
    		var datas=oSettings.aoData;
        	var dataTable = oSettings.oInstance;
	    	if(this.fnGetSelectedData().length > 0){
	    		oConfig.fnModalControl(oConfig);
				
	    		$("#confirm").click( function () {
	    			if(datas){
		            	for(var i = 0; i < datas.length; i++){	
		            		var data = datas[i];
		            		if(data._DTTT_selected) {
		            			var rowData = data._aData;
		            			var ajaxContext = {
			    					"data" : data,
			    					"iRow" : i,
			    					"oConfig": oConfig,
			    					"oSettings":oSettings
		            			}
		            			$.ajax({
			    	        	  url: (oConfig.playJSRoute)?oConfig.playJSRoute.url.replace("undefined",rowData[oConfig.objectField]):oConfig.url.replace("undefined",rowData[oConfig.objectField]),
			    	        	  type: (oConfig.playJSRoute)?oConfig.playJSRoute.method:oConfig.method,
			    	        	  context: ajaxContext
		            			}).done(function( msg ) {
		            				this.oConfig.fnSaveDoneCallback(this, msg,oTT);	   
		            				
		            			}).fail(function( msg ){
			    	        	  //replace all the page by the error
			    	        	  if(msg.status == 400){
			    	        		  $('#'+oConfig.modalId).modal('hide');
			    	        		  this.oConfig.fnSaveFailCallback(this, msg);
			    	        	  }else{
			    	        		  $("html").html(msg.responseText);
			    	        	  }
			    	          }).always(function(msg){
			    	        	  //alert( "Data Saved: " + msg );
			    	          });
		            	} 
		            	}
		        	}
	    	    });
	    	
	    	}
    	},
    "fnSaveDoneCallback":function(ajaxContext, msg, oTT){
	        $('#'+ajaxContext.oConfig.modalId).modal('hide');
	        oTT.fnClearTable(0);
	        oTT.fnDraw();
    },
        "fnSaveFailCallback":function(ajaxContext, msg){    	
        	var dataTable = ajaxContext.oSettings.oInstance; 
        	var oSettings = ajaxContext.oSettings;
        	var iRow = ajaxContext.iRow;
        	$(dataTable._fnGetTrNodes()[iRow]).addClass("error");
        	var errorMsg = $.parseJSON(msg.responseText);
        	for (var iColumn=0 ; iColumn < oSettings.aoColumns.length ; iColumn++ )
    		{
    			var inputs = $(dataTable._fnGetTdNodes(iRow)[iColumn]).find("input, select");
    			if(inputs.length == 1){
    				var input = inputs[0];
    				//cree une fonction anonyme pour faire un appel dynamique à une propriété de errorMsg
    				var tmpfunction = new Function("errorMsg", "return errorMsg."+input.name+";");
    				if(tmpfunction(errorMsg) !== undefined){
    					$(input).parent().addClass("error");
    					//utilise un bootstrap popover pour l'affichage des erreurs
    					$(input).popover({
    						"title":"Error",
    						"trigger":"focus",
    						"placement":"bottom",
    						"content":tmpfunction(errorMsg)
    					});
    				}
    			}
    		}    	
        },
		"fnModalControl":function(oConfig){
			if(!oConfig.manualModal) 
	    			$('body').append('<div class="modal fade" id="'+oConfig.modalId+'"><div class="modal-header"><a class="close" data-dismiss="modal">&times;</a><h3>'+oConfig.titleModal+'</h3></div><div class="modal-body"><p>'+oConfig.textModal+'</p></div><div class="modal-footer"><a href="#" class="btn" data-dismiss="modal">'+oConfig.textCancelModal+'</a><a href="#" class="btn btn-primary" id="'+oConfig.idBtnModal+'">Yes</a></div></div>');
		    		
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
		}
});

TableTools.BUTTONS.refresh = $.extend( {}, TableTools.buttonBase, {
	"sButtonClass": "btn",
	"sButtonText":  "Refresh",
	"fnClick": function( nButton, oConfig ) {
		var oTT = this.s.dt.oInstance;
		 oTT.fnClearTable(0);
	     oTT.fnDraw();
	}
});