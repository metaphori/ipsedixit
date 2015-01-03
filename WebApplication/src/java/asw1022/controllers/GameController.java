/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package asw1022.controllers;

import asw1022.model.dixit.Card;
import asw1022.model.dixit.GameExecution;
import asw1022.model.dixit.MatchConfiguration;
import asw1022.model.dixit.Player;
import asw1022.repositories.CardRepository;
import asw1022.repositories.IRepository;
import asw1022.repositories.MatchRepository;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.bind.JAXBException;

/**
 *
 * @author Roberto Casadei <roberto.casadei12@studio.unibo.it>
 */
public class GameController extends BaseController { 
    private IRepository<Card> crepo;
    private List<Card> cards;
    
    @Override public void init() throws ServletException {
        try {
            ServletContext app = getServletContext();
            Object obj = app.getAttribute("Matches");
            if(obj==null){
                HashMap<String,GameExecution> matches = new HashMap<String, GameExecution>();
                getServletContext().setAttribute("Matches", matches);
            }     
            
            String cardDB = app.getRealPath(app.getInitParameter("cardDB"));
            this.crepo = new CardRepository(cardDB);
            
            // We can read all the cards during initialization
            this.cards = crepo.readAll();        
        } catch (JAXBException ex) {
            Logger.getLogger(GameController.class.getName()).log(Level.SEVERE, "Init error.", ex);
        }
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
        
        logger.info("GameController GET");
        
        if (userPath.equals("/NewMatch")) {
            jspPage = "new_match";
            pageTitle = "New match";
        } else if (userPath.startsWith("/Play")) {
            GameExecution match = getMatch(request);
            if(match!=null){
                pageTitle = "Play";
                jspPage = "play";
                request.setAttribute("MatchId", match.getName());
                request.setAttribute("MatchConfiguration", match.getMatchConfiguration());
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
        String url = "/WEB-INF/jsp/" + jspPage + ".jsp";

        try {
            request.getRequestDispatcher(url).forward(request, response);
        } catch (Exception ex) {
            logger.severe("Eccezione: " + ex.getMessage());
        }
    }
    
    public GameExecution getMatch(HttpServletRequest request){
        String matchId = request.getParameter("match");
        String random = request.getParameter("random");

        logger.info("Looking for an existing match");
        
        HashMap<String,GameExecution> matches = (HashMap<String, GameExecution>)
            getServletContext().getAttribute("Matches"); 
        synchronized(matches){
            if(matchId!=null){
                logger.info("Retrieving match with id " + matchId);                
                return matches.get(matchId);
            }

            if (random!=null){
                for(String matchName : matches.keySet()){
                    if(matches.get(matchName).waitingForPlayers())
                        return matches.get(matchName);
                }
            }

            return null;
        }
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
        String matchId = null;
        
        if (userPath.equals("/NewMatchConfig")) {
            jspPage = "new_match_config";
            pageTitle = "New match config";
            boolean ok = newMatchConfigAction(request, response);
            if(ok) request.setAttribute("done", "true");
        } else if(userPath.equals("/NewMatch")){
            jspPage = "new_match_config";
            pageTitle = "New match config";
            logger.info("Creating a new match");
            String match = newMatchAction(request, response);
            logger.info("Match " + match + " created");
            if(match!=null){ 
                request.setAttribute("done", "true");
                String dest = "/Play?match="+match;
                logger.info("Moving to dest " + dest);
                request.getRequestDispatcher(dest).forward(request, response);
                return;
            }        
        } else if (userPath.startsWith("/Play")) {
            GameExecution match = getMatch(request);
            if(match!=null){
                pageTitle = "Play";
                jspPage = "play";                
                request.setAttribute("MatchId", match.getName());
                request.setAttribute("MatchConfiguration", match.getMatchConfiguration());                
            } else{
                jspPage = "error";
                pageTitle  = "Match not found";
                request.setAttribute("errorTitle", "Error");
                request.setAttribute("errorMsg", "This match could not be found");                
            }
        } else{
            request.setAttribute("errorTitle", "404 Error");
            request.setAttribute("errorMsg", "This page could not be found: " + request.getRequestURI()
                + "?" + request.getQueryString());
            jspPage = "error";
            pageTitle = "404 Error";
        }
        
        request.setAttribute(Attrs.PageTitle, pageTitle);

        // use RequestDispatcher to forward request internally
        String url = "/WEB-INF/jsp/" + jspPage + ".jsp";

        try {
            request.getRequestDispatcher(url).forward(request, response);
        } catch (Exception ex) {
            logger.severe("Eccezione: " + ex.getMessage());
        }
    }
    
    public String newMatchAction(HttpServletRequest request, HttpServletResponse response) {
        HttpSession user = request.getSession(false);
        ServletContext app = getServletContext();
        String matchname = null;
        HashMap<String,GameExecution> matches = (HashMap<String, GameExecution>)
            getServletContext().getAttribute("Matches");
        
        try{
            if(user==null || user.getAttribute("username")==null){ 
                // User not logged
                request.setAttribute("msg", "You must be logged in to perform this action.");
                return null;
            }
        
            // 1) Prepare data
            String username = user.getAttribute("username").toString();
            String matchTitle = request.getParameter("matchtitle");
            int numplayers = Integer.parseInt(request.getParameter("numplayers"));
            int numpoints = Integer.parseInt(request.getParameter("npoints"));
            int numcards = Integer.parseInt(request.getParameter("ncardsforplayer"));
            String visibilityStr = request.getParameter("visibility");
            MatchConfiguration.MatchVisibility visibility = visibilityStr.equals("all") ?
                    MatchConfiguration.MatchVisibility.All : MatchConfiguration.MatchVisibility.SecretUrl;
            
            // 2) Create new match configuration
            MatchConfiguration m = new MatchConfiguration();
            m.setVisiblity(visibility);
            m.setNumPlayers(numplayers);
            m.setNumPoints(numpoints);
            m.setNumCardsForPlayers(numcards);
            
            // 3) Create new game execution
            Player player = new Player();
            player.setName(username);
            GameExecution newge = new GameExecution(m, this.cards, player);
            newge.setTitle(matchTitle);
            matchname = newge.getName();
            synchronized(matches){
                matches.put(newge.getName(), newge);
            }
            
            request.setAttribute("msg", "The match has been created successfully.");
        } catch (Exception ex) {
            Logger.getLogger("GameController#NewMatch").log(Level.SEVERE, null, ex);
            request.setAttribute("msg", "Game creation FAILED.");
            return null;
        }
        
        return matchname;        
    }
    
    public boolean newMatchConfigAction(HttpServletRequest request, HttpServletResponse response) {
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
            String configName = request.getParameter("matchname");
            int numplayers = Integer.parseInt(request.getParameter("numplayers"));
            int numpoints = Integer.parseInt(request.getParameter("npoints"));
            int numcards = Integer.parseInt(request.getParameter("ncardsforplayer"));
            String visibilityStr = request.getParameter("visibility");
            MatchConfiguration.MatchVisibility visibility = visibilityStr.equals("all") ?
                    MatchConfiguration.MatchVisibility.All : MatchConfiguration.MatchVisibility.SecretUrl;
            
            IRepository<MatchConfiguration> repo = new MatchRepository(matchDB);
            List<MatchConfiguration> matchConfigs = repo.readAll();
            
            // 2) Register new match configuration
            MatchConfiguration m = new MatchConfiguration();
            m.setName(configName);
            m.setVisiblity(visibility);
            m.setNumPlayers(numplayers);
            m.setNumPoints(numpoints);
            m.setNumCardsForPlayers(numcards);
            
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

        try{
            if(user==null || user.getAttribute("username")==null){ 
                // User not logged
                request.setAttribute("msg", "You must be logged in to perform this action.");
                request.getRequestDispatcher("index.jsp").forward(request, response); 
                return false;
            }
        
            // 1) Get matches
            HashMap<String,GameExecution> matchesDb = (HashMap<String, GameExecution>)
                getServletContext().getAttribute("Matches");            
            List<GameExecution> matches = new ArrayList<GameExecution>();
            
            // 2) Filter by visibility
            for(GameExecution ge : matchesDb.values()){
                if(ge.getMatchConfiguration().getVisibility()!=MatchConfiguration.MatchVisibility.SecretUrl)
                    matches.add(ge);
            }
            
            request.setAttribute("matches", matches);
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
