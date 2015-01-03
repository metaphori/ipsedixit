package asw1022.controllers;

import asw1022.model.User;
import asw1022.repositories.IRepository;
import asw1022.repositories.UserRepository;
import java.io.IOException;
import java.io.PrintWriter;
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
public class UserController extends BaseController {

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
        String pageTitle = "unknown";
        
        if (userPath.equals("/Login")) {
            jspPage = "login";
            pageTitle = "Login";
        } else if (userPath.equals("/Register")) {
            jspPage = "register";
            pageTitle = "Register";
        } else if(userPath.equals("/Logout")) {
            jspPage = "home";
            pageTitle = "Home";
            logoutAction(request, response);
        } else{
            jspPage = "error";
            pageTitle = "404 Error";
            request.setAttribute("ErrorTitle", "404 Error");
            request.setAttribute("ErrorMsg", "This page could not be found.");            
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
        
        if (userPath.equals("/Login")) {
            jspPage = "login";
            pageTitle = "Login";
            boolean ok = loginAction(request, response);
            if(ok) request.setAttribute("done", "true");
        } else if (userPath.equals("/Register")) {
            jspPage = "register";
            pageTitle = "Register";
            boolean ok = registerAction(request, response);
            if(ok) request.setAttribute("done", "true");
        } else{
            jspPage = "error";
            pageTitle = "404 Error";
            request.setAttribute("errorTitle", "404 Error");
            request.setAttribute("errorMsg", "This page could not be found.");            
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
    
    public boolean loginAction(HttpServletRequest request, HttpServletResponse res) throws ServletException {
        // 1) Prepare data
        HttpSession session = request.getSession();
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        
        // 2) Check if the user/pass pair exists
        ServletContext app = getServletContext();
        String userDB = app.getRealPath(app.getInitParameter("userDB"));
        boolean found = false;
        try {
            IRepository<User> repo = new UserRepository(userDB);
            List<User> users = repo.readAll();
            for(User us : users){
                if(us.getName().equals(username) &&
                        us.getPassword().equals(password)){
                    found = true;
                    break;
                }
            }
        } catch (Exception ex) {
            Logger.getLogger("UserController#loginAction").log(Level.SEVERE, null, ex);
        }
        
        if(found){
            request.setAttribute("msg", "Login has been performed successfully.");
            session.setAttribute("username", username);
            return true;
        }
        else{
            request.setAttribute("msg", "Login FAILED.");
            return false;
        }        
    }
    
    public void logoutAction(HttpServletRequest request, HttpServletResponse res) throws ServletException {
        HttpSession user = request.getSession(false);
        if(user!=null){
            user.invalidate();
        }
        
        request.setAttribute("msg", "Logout has been performed successfully");
    }
    
    public boolean registerAction(HttpServletRequest request, HttpServletResponse response) throws ServletException {
        // 1) Prepare data
        HttpSession session = request.getSession();
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        
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
