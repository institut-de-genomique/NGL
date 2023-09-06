
/**
 * Custom click event on bootstrap tab plugin
 * When user click on a tab the function load an url (data-href) and set the result in data-target.
 * then show the tab
 */	
(function($){
	$(function () {
	    $('body').on('click.tabdyn.data-api', '[data-toggle="tabdyn"], [data-toggle="pilldyn"]', function (e) {
	    	e.preventDefault();
			$($(this).attr('data-target')).load($(this).attr('data-href'));
			$(this).tab('show');
	    })
	  })
})(window.jQuery);	

(function($){
	$(function () {
	    $('body').on('click.closetabdyn.data-api', '[data-toggle="tabdyn"] > [data-dismiss = "tabdyn"], [data-toggle="pilldyn"] > [data-dismiss = "pilldyn"]', function (e) {
	    	e.preventDefault();
	    	e.stopPropagation();
	    	$($(this).parent().attr('data-target')).empty(); //doit- être amélioré pour ne pas afficher une  page blanche	
			$(this).parent().parent().remove(); //to remove the <li/>
			return false;  //avoid to trigger the load event
	    })
	  })
})(window.jQuery);	


/**
 * wrap the submit form action in ajax request
 */
(function($){
	$(function () {		
	    $('body').on('submit.form.data-api', 'form[data-toggle="ajax"]', function (e) {
	    	 /* stop form from submitting normally */
	          e.preventDefault(); 
	          /* get some values from elements on the page: */
	          var form = $(this);
	          var url = form.attr('action');
	          var method = form.attr('method');
	          var submitValues = form.serialize();    
	          /* Send the data using post and put the results in a div */
	          $.ajax({
	        	  url: url,
	        	  type: method,
	        	  data: submitValues,
	        	  context: form
	          }).done(function( msg ) {
	        	  	//$(this).find("#alert").append('<div class="alert alert-success fade in"><a class="close" data-dismiss="alert" href="#">&times;</a>'+msg+'</div>');
	        	    $(this).replaceWith(msg);
	          }).fail(function( msg ){
	        	  //replace all the page by the error
	        	  if(msg.status == 400){
	        		  $(this).replaceWith(msg.responseText);
	        	  }else{
	        		  $("html").html(msg.responseText);
	        	  }
	          }).always(function(msg){
	        	  //alert( "Data Saved: " + msg );
	          }); 
	    })
	  })
})(window.jQuery);


/**
 * used to add an element in ajax mode
 * transform <a> in ajax submission
 * a need custom attribute :
 * 		- data-toggle="add-ajax"
 * 		- data-href = the server request
 * 		- data-target = id of the element where add the result of server request
 * 		- data-index = the start index in case of indexed proprerty		
 * 
 */
(function($){
	$(function () {		
	    $('body').on('click.add.data-api', 'a[data-toggle="add-ajax"]', function (e) {
	    	 /* stop form from submitting normally */
	          e.preventDefault(); 
	          /* get some values from elements on the page: */
	          var a = $(this);
	          var url = a.attr('data-href'); //extract url
	          var targetId = a.attr('data-target'); //extract id of the target element
	          var context = $('#'+targetId); //search the dom element with targetId
	          var index = a.attr('data-index'); //search the last index
	          if(index !== null){
	        	  url = url.replace('-1', index); //replace in the url the index
	        	  a.attr('data-index', ++index);
	          }
	          /* Send the data using post and put the results in a div */
	          $.ajax({
	        	  url: url,
	        	  type: 'GET',	        	  
	        	  context: context	        	  
	          }).done(function( msg ) {
	        	  context.append(msg);
	          }).fail(function( msg ){
	        	  //replace all the page by the error
	        	  //fail when error 500 so server problem or exception not managed
	        	  $("html").html(msg.responseText);	   
	          }).always(function(msg){
	        	  //alert( "Data Saved: " + msg );
	          });
	          
	    })
	  })
})(window.jQuery);

(function($){
	$(function () {		
	    $('body').on('click.add.data-api', 'a[data-toggle="add-content"]', function (e) {
	    	 /* stop form from submitting normally */
	          e.preventDefault(); 
	          /* get some values from elements on the page: */
	          var a = $(this);
	          
	          var targetId = a.attr('data-target'); //extract id of the target element
	          var context = $('#'+targetId); //search the dom element with targetId
	          var index = a.attr('data-index'); //search the last index
	          var html = $($('#'+a.attr('data-href')).html().replace(/valueIndex/g,index)).clone();//extract html
	          
	          if(index !== null){
	        	  a.attr('data-index', ++index);
	          }
	          
	          context.append(html);
	    })
	  })
})(window.jQuery);
