/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package asw1022.controllers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author Roberto Casadei <roberto.casadei12@studio.unibo.it>
 */
public class BaseController extends HttpServlet {
    
    public class Attrs {
        public static final String PageTitle = "PageTitle";       
    }    
    
    protected Logger logger = Logger.getLogger(this.getClass().getName());
    
    public class DispatchInfo {
        public final List<String> urls;
        public final String jspPage;
        public final IServletAction action;
        public final IServletAction onError;
        public final IServletAction onSuccess;
        public final Map<String,Object> attrs;
        
        public DispatchInfo(final List<String> urls, 
                final String jspPage, 
                final IServletAction action,
                final IServletAction onError,
                final IServletAction onSuccess){
            this.urls = urls;
            this.jspPage = jspPage;
            this.action = action;
            this.onError = onError;
            this.onSuccess = onSuccess;
            this.attrs = new HashMap<String, Object>();            
        }
        
        public void SetAttribute(String attr, Object value){
            this.attrs.put(attr, value);
        }
    }
    
    public interface IServletAction {
        boolean Execute(HttpServletRequest request, HttpServletResponse response);
    }
    
    protected void Dispatch(List<DispatchInfo> routes, 
            HttpServletRequest request,
            HttpServletResponse response){
        for(DispatchInfo di : routes){
            
            /*
            String userPath = request.getServletPath();
            String jspPage = "";
            String pageTitle  = "";
            String matchId = null;

            if (userPath.equals("/NewMatch")) {
                jspPage = "new_match";
            } else if (userPath.startsWith("/Play")) {
                int indexMatchId = userPath.indexOf("/", 1);
                request.setAttribute("matchId", matchId);
                jspPage = "play";
            } else{
                request.setAttribute("errorTitle", "404 Error");
                request.setAttribute("errorMsg", "This page could not be found.");
                jspPage = "error";
            }

            request.setAttribute(Attrs.PageTitle, pageTitle);

            // use RequestDispatcher to forward request internally
            String url = "/jsp/" + jspPage + ".jsp";

            try {
                request.getRequestDispatcher(url).forward(request, response);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            */
            
        } // end for
    }
    
}
