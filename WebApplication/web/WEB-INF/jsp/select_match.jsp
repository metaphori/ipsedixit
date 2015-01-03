<%-- 
    Document   : index
    Created on : 20-Dec-2014, 12:19:56
    Author     : Roberto Casadei <roberto.casadei12@studio.unibo.it>
--%>

<%@page import="asw1022.model.dixit.GameExecution"%>
<%@page import="java.util.List"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>

<!DOCTYPE html>
<html>
    <head>

        <%@ include file="/WEB-INF/jspf/head.jspf" %>
        
    </head>
    <body>

        <%@ include file="/WEB-INF/jspf/prologue.jspf" %>

        <h1>Matches</h1>
        
        <%@ include file="/WEB-INF/jspf/msg.jspf" %>        

        <% 
            List<GameExecution> matches = (List<GameExecution>)request.getAttribute("matches"); 
            if(matches!=null && matches.size()>0){
        %>
        
        <ul>
        <% for(GameExecution m : matches){ %>
            <li><a href="${pageContext.request.contextPath}/Play?match=<%= m.getName() %>"><%= m.getTitle() %></a></li>
        <% } %>
        </ul>
        
        <% } else { %>
        <p>There are no matches.</p>
        <% } %>

        <%@ include file="/WEB-INF/jspf/epilogue.jspf" %>

    </body>
    
</html>