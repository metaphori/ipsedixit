package asw1022.servlets;

import asw1022.model.dixit.Match;
import asw1022.repositories.IRepository;
import asw1022.repositories.MatchRepository;
import java.io.IOException;
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
public class ShowMatchesServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
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
            request.getRequestDispatcher("jsp/show_matches.jsp").forward(request, response);
            return;
        } catch (Exception ex) {
            Logger.getLogger(RegisterServlet.class.getName()).log(Level.SEVERE, null, ex);
            request.setAttribute("msg", "Error.");
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
        return "Short description";
    }// </editor-fold>

}
