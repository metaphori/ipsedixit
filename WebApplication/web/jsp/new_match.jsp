<%-- 
    Document   : newjspnew_game
    Created on : 02-Jan-2015, 12:01:03
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

        <h1>Create a new game</h1>
        
        <%@ include file="/WEB-INF/jspf/msg.jspf" %>
        
        <% if(request.getAttribute("done")==null) { %>

        <form method="post">

            <div class="formrow">
                <label for="matchname">Name of the match</label>
                <input type="text" size="20" maxlength="20" name="matchname" />
            </div>    

            <div class="formrow">
                <label for="numplayers">Number of players</label>
                <input type="text" size="2" maxlength="2" name="numplayers" />
            </div>

            <div class="formrow">
                <label for="visibility">Visibility</label>
                <select name="visibility">
                    <option value="all">All</option>
                    <option value="onlyurl">Only those who know the URL</option>
                </select>
            </div>

            <div class="formrow">
                <input type="submit" value="Create a new match!" />
            </div>

        </form>
        
        <% } %>

        <%@ include file="/WEB-INF/jspf/epilogue.jspf" %>

    </body>
    
</html>
