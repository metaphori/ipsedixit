package asw1022.controllers.user;

import asw1022.controllers.BaseServlet;
import asw1022.model.User;
import asw1022.repositories.IRepository;
import asw1022.repositories.UserRepository;
import asw1022.util.security.SecurityUtils;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Servlet controller that handles the registration of a new user.
 * @author Roberto Casadei <roberto.casadei12@studio.unibo.it>
 */
@WebServlet(urlPatterns = "/Register")
public class RegisterServlet extends BaseServlet {

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
        Forward(request, response, "register", "New user");
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
        boolean ok = registerAction(request, response);
        if(ok) {
            request.setAttribute("done", "true");
        }
        Forward(request, response, "register", "New user");
    }
    
    
    public boolean registerAction(HttpServletRequest request, HttpServletResponse response) throws ServletException {
        // 1) Prepare data
        HttpSession session = request.getSession();
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        
        password = SecurityUtils.SHA(password);
        
        logger.info("Password hash: " + password);
        
        // 2) Register username
        ServletContext app = getServletContext();
        String userDB = app.getRealPath(app.getInitParameter("userDB"));
        try {
            IRepository<User> repo = new UserRepository(userDB);
            User newuser = new User();
            newuser.setName(username);
            newuser.setPassword(password);
            repo.add(newuser);
            request.setAttribute("msg", "Registration has been performed successfully.");
            return true;
        } catch (Exception ex) {
            Logger.getLogger("UserController#registerAction").log(Level.SEVERE, null, ex);
            request.setAttribute("msg", "Registration FAILED.");
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
