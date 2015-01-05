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
        
        <title>Ipse Dixit: Select match</title>

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
        
        <p>NOTE: the match status might be out of sync</p>
        
        <table>
            <thead>
                <tr>
                    <td></td>
                    <td>Name</td>
                    <td># players</td>
                    <td># joins</td> 
                    <td>Phase</td>
                </tr>
            </thead>
            <tbody>
        <% for(GameExecution m : matches){ %>
            <tr>
                <td>
                    <% if(m.getPlayers().size() < m.getMatchConfiguration().getNumPlayers()){ %>
                    <a href="${pageContext.request.contextPath}/Play?match=<%= m.getName() %>">Play</a>
                    <% } %>
                </td>
                <td><%= m.getTitle() %></td>
                <td><%= m.getMatchConfiguration().getNumPlayers() %></td>
                <td><%= m.getPlayers().size() %></td>
                <td><%= m.getPhase().toString() %></td>
        <% } %>
            </tbody>
        </table>
        
        <% } else { %>
        <p>There are no matches.</p>
        <% } %>

        <%@ include file="/WEB-INF/jspf/epilogue.jspf" %>

    </body>
    
</html>