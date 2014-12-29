$().ready(function(){
   $('#pagetitle').html("Index");
    
   $('#menu a').on('click', function(ev){
       url = $(this).prop('href');
       title = $(this).prop('title');
       $('#menu .selected').removeClass('selected');
       $(this).addClass('selected');
       $('#pagetitle').html(title);
       if(title.length > 0){
           $('#content').load(url);
       } else{
           window.location = url; 
       }
       return false;
   });
   
  // $('#menu a').first().click();
  
  /*
  $("form").on('submit', function(ev) {
    url = this.prop("action");
    $.ajax({
           type: "POST",
           url: url,
           data: $(this).serialize(), // serializes the form's elements.
           success: function(data)
           {
               alert(data); // show response from the php script.
           }
         });
    ev.preventDefault();
    return false; // avoid to execute the actual submit of the form.
  });
  */

});

