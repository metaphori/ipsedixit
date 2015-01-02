/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package asw1022.controllers;

import asw1022.model.dixit.Match;
import asw1022.repositories.IRepository;
import asw1022.repositories.MatchRepository;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 *
 * @author Roberto Casadei <roberto.casadei12@studio.unibo.it>
 */
public class GameController extends HttpServlet {

    protected class Attrs {
        public static final String PageTitle = "PageTitle";       
    }    
    
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String userPath = request.getServletPath();
        String jspPage = "";
        String pageTitle  = "";
        String matchId = null;
        
        if (userPath.equals("/NewMatch")) {
            jspPage = "new_match";
            pageTitle = "New match";
        } else if (userPath.startsWith("/Play")) {
            matchId = request.getParameter("match");
            if(matchId!=null){
                if(matchId.toLowerCase().equals("random"))
                    matchId = findAMatch();
                pageTitle = "Play";
                jspPage = "play";                
            } else{
                jspPage = "select_match";
                pageTitle  = "Select a match";
                selectMatchAction(request, response);
            }
        } else{
            request.setAttribute("errorTitle", "404 Error");
            request.setAttribute("errorMsg", "This page could not be found.");
            jspPage = "error";
            pageTitle = "404 Error";
        }
        
        request.setAttribute(Attrs.PageTitle, pageTitle);

        // use RequestDispatcher to forward request internally
        String url = "/jsp/" + jspPage + ".jsp";

        try {
            request.getRequestDispatcher(url).forward(request, response);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    public String findAMatch(){
        return "test";
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String userPath = request.getServletPath();
        String jspPage = "";
        String pageTitle = "unknown";
        
        if (userPath.equals("/NewMatch")) {
            jspPage = "new_match";
            pageTitle = "New match";
            boolean ok = newMatchAction(request, response);
            if(ok) request.setAttribute("done", "true");
        } else{
            request.setAttribute("errorTitle", "404 Error");
            request.setAttribute("errorMsg", "This page could not be found.");
            jspPage = "error";
            pageTitle = "404 Error";
        }
        
        request.setAttribute(Attrs.PageTitle, pageTitle);

        // use RequestDispatcher to forward request internally
        String url = "/jsp/" + jspPage + ".jsp";

        try {
            request.getRequestDispatcher(url).forward(request, response);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    public boolean newMatchAction(HttpServletRequest request, HttpServletResponse response) {
        HttpSession user = request.getSession(false);
        ServletContext app = getServletContext();
        String matchDB = app.getRealPath(app.getInitParameter("matchDB"));
        
        try{
            if(user==null || user.getAttribute("username")==null){ 
                // User not logged
                request.setAttribute("msg", "You must be logged in to perform this action.");
                return false;
            }
        
            // 1) Prepare data
            String username = user.getAttribute("username").toString();
            String name = request.getParameter("matchname");
            int numplayers = Integer.parseInt(request.getParameter("numplayers"));
            String visibilityStr = request.getParameter("visibility");
            Match.MatchVisibility visibility = visibilityStr.equals("all") ?
                    Match.MatchVisibility.All : Match.MatchVisibility.SecretUrl;
            
            IRepository<Match> repo = new MatchRepository(matchDB);
            List<Match> matches = repo.readAll();
            
            // 2) Register new match
            Match m = new Match();
            m.setName(name);
            m.setVisiblity(visibility);
            m.setOwner(username);
            m.setNumPlayers(numplayers);
            
            repo.add(m);
            
            request.setAttribute("msg", "The match has been created successfully.");
        } catch (Exception ex) {
            Logger.getLogger("GameController#NewMatch").log(Level.SEVERE, null, ex);
            request.setAttribute("msg", "Game creation FAILED.");
            return false;
        }
        
        return true;
    }
    
    public boolean selectMatchAction(HttpServletRequest request, HttpServletResponse response) {    
        HttpSession user = request.getSession(false);
        ServletContext app = getServletContext();
        String matchDB = app.getRealPath(app.getInitParameter("matchDB"));
        
        try{
            if(user==null || user.getAttribute("username")==null){ 
                // User not logged
                request.setAttribute("msg", "You must be logged in to perform this action.");
                request.getRequestDispatcher("index.jsp").forward(request, response); 
                return false;
            }
        
            // 1) Get matches
            IRepository<Match> repo = new MatchRepository(matchDB);
            List<Match> matches = repo.readAll();
            List<Match> visibleMatches = new ArrayList<Match>();
            
            // 2) Filter by visibility
            for(Match m : matches){
                if(m.getVisibility()!=Match.MatchVisibility.SecretUrl)
                    visibleMatches.add(m);
            }
            
            request.setAttribute("matches", visibleMatches);
            return true;
        } catch (Exception ex) {
            Logger.getLogger("GameController#selectMatchAction").log(Level.SEVERE, null, ex);
            request.setAttribute("msg", "Error: cannot retrieve matches.");
            return false;
        }
    }
    
    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
