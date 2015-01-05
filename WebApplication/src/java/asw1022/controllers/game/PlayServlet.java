package asw1022.controllers.game;

import asw1022.controllers.BaseServlet;
import asw1022.model.dixit.GameExecution;
import asw1022.model.dixit.MatchConfiguration;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Servlet controller that handles the selection of a match and the game playing.
 * @author Roberto Casadei <roberto.casadei12@studio.unibo.it>
 */
@WebServlet(urlPatterns = "/Play")
public class PlayServlet extends BaseServlet {  
 
    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        GameExecution match = getMatch(request);
        if (match != null) {
            request.setAttribute("Match", match);
            request.setAttribute("MatchId", match.getName());
            request.setAttribute("MatchConfiguration", match.getMatchConfiguration());
            Forward(request, response, "play", "Play: " + match.getTitle());  
        } else {
            selectMatchAction(request, response);
            Forward(request, response, "select_match", "Select match");  
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
        processRequest(request, response);
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
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
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
       
    
    public GameExecution getMatch(HttpServletRequest request){
        String matchId = request.getParameter("match");
        String random = request.getParameter("random");

        logger.info("Looking for an existing match");
        
        HashMap<String,GameExecution> matches = (HashMap<String, GameExecution>)
            getServletContext().getAttribute("Matches");
        if(matches!=null){
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
            }
        }
        
        return null;
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
