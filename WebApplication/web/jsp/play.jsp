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
        
        <script type="text/javascript">
        function getCtx(){
            return '${pageContext.request.contextPath}';
        }
        </script>
            
        <script type="text/javascript" src="${pageContext.request.contextPath}/js/dixit.js"></script>
        
    </head>
    <body>

<%@ include file="/WEB-INF/jspf/prologue.jspf" %>

<div class="center" id="request">
        <input type="hidden" name="match" value="<%= request.getParameter("match") %>" />
        <input type="hidden" name="user" value="<%= session.getAttribute("username") %>" />
        <input type="button" value="Enter the match!" id="request_button" class="fancybutton" />
        <input type="button" id="proceedbtn" value="Next" class="fancybutton" style="display:none;"/> 
</div>
        
<p id="todo"></p>

<div id="table">
    <h1>Table</h1>
    
    <p>Clue: <span id="clue"></span></p>
    
    <div id="tablecards"></div>
    
    <div id="tableinfo"></div>
    
</div>
        
<div id="gameinfo">
    <h1>Info</h1>
    <p class="info"><span class="label">You are </span> <span id="username"><%= session.getAttribute("username") %></span></p>
    <p class="info"><span class="label">Turn:</span> <span id="turntxt">--</span></p>
    <p class="info"><span class="label">Phase:</span> <span id="phasetxt">setup</span></p>
    
    <h1>Results</h1>
    
    <table>
        <thead>
            <tr>
                <td>Player</td>
                <td>Point</td>
            </tr>
        </thead>
        <tbody id="resultsbody">
        </tbody>
    </table>
    
</div>        

<div id="playerpanel">
    
    <div>
        <h1>Your panel</h1>
    </div>
    
    <div id="playercards"></div>
    
    <div>
        <form>
            <label for="cluetxt">Clue:</label>
            <input type="text" name="clue" id="cluetxt" disabled="disabled" />
            <input type="button" value="Give it!" id="phrasebtn" disabled="disabled" />
        </form>
    </div>
</div>      
    
<div class="clearer"></div>  


<p data-bind="text: suggestion"></p>
        

<%@ include file="/WEB-INF/jspf/epilogue.jspf" %>


    </body>
    
</html>