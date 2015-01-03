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
        
        <h1>User Registration</h1>

        <%@ include file="/WEB-INF/jspf/msg.jspf" %>
        
        <% if(request.getAttribute("done")==null) { %>
        
        <form method="post" action="${pageContext.request.contextPath}/Register">

            <div class="formrow">
                <label for="username">Username</label>
                <input type="text" size="20" maxlength="20" name="username" />
            </div>    

            <div class="formrow">
                <label for="password">Password</label>
                <input type="password" size="20" maxlength="20" name="password" />
            </div>

            <div class="formrow">
                <input type="submit" value="Register!" />
            </div>

        </form>

        <% } %>
        
        <%@ include file="/WEB-INF/jspf/epilogue.jspf" %>

    </body>
    
</html>
