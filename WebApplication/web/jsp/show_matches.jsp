<%-- 
    Document   : show_matches
    Created on : 23-Dec-2014, 12:33:53
    Author     : Roberto Casadei <roberto.casadei12@studio.unibo.it>
--%>

<%@page import="asw1022.model.dixit.Match"%>
<%@page import="java.util.List"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>

<h1>Matches</h1>

<ul>
<% for(Match m : (List<Match>)request.getAttribute("matches")){ %>
    <li><a href="/WebApplication/jsp/play.jsp?match=<%= m.getName() %>"><%= m.getName().toString() %></a></li>
<% } %>
</ul>
