<%-- 
    Document   : index
    Created on : 20-Dec-2014, 12:19:56
    Author     : Roberto Casadei <roberto.casadei12@studio.unibo.it>
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>

<!DOCTYPE html>
<html>
    <head>

        <%@ include file="/WEB-INF/jspf/head.jspf" %>
        
    </head>
    <body>

<%@ include file="/WEB-INF/jspf/prologue.jspf" %>
        
<% if(request.getAttribute("msg")!=null){ %>
    <p class="message"><%= request.getAttribute("msg") %></p>
    <% if(request.getAttribute("redirectUrl")!=null){ %>
        <p class="message"><a href="<%= request.getAttribute("redirectUrl") %>"><%= request.getAttribute("destinationName") %></a>.</p>
    <% } %>
    <hr />
<% } %>

<h1>How to play</h1>

<ol>
    <li>Register to the site</li>
    <li>Log in</li>
    <li>Create a match or join an existing match</li>
    <li>Enjoy</li>
</ol>
             
<p><i class="fa fa-smile-o" style="font-size:40px;"></i></p>

<%@ include file="/WEB-INF/jspf/epilogue.jspf" %>


    </body>
    
</html>