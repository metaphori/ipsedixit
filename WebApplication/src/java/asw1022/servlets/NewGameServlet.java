/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package asw1022.servlets;

import asw1022.model.dixit.Match;
import asw1022.model.dixit.Match.MatchVisibility;
import asw1022.repositories.IRepository;
import asw1022.repositories.MatchRepository;
import java.io.IOException;
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
public class NewGameServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession user = request.getSession(false);
        ServletContext app = getServletContext();
        String matchDB = app.getRealPath(app.getInitParameter("matchDB"));
        
        try{
            if(user==null || user.getAttribute("username")==null){ 
                // User not logged
                request.setAttribute("msg", "You must be logged in to perform this action.");
                request.getRequestDispatcher("index.jsp").forward(request, response); 
                return;
            }
        
            // 1) Prepare data
            String username = user.getAttribute("username").toString();
            String name = request.getParameter("matchname");
            int numplayers = Integer.parseInt(request.getParameter("numplayers"));
            String visibilityStr = request.getParameter("visibility");
            MatchVisibility visibility = visibilityStr.equals("all") ?
                    MatchVisibility.All : MatchVisibility.SecretUrl;
            
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
            Logger.getLogger(RegisterServlet.class.getName()).log(Level.SEVERE, null, ex);
            request.setAttribute("msg", "Game creation FAILED.");
        }
        
        request.getRequestDispatcher("index.jsp").forward(request, response);         
    }
    
    
    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "This servlet creates a new Dixit match.";
    }// </editor-fold>

}
