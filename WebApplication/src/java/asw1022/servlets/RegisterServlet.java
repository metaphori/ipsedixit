/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package asw1022.servlets;

import asw1022.model.User;
import asw1022.repositories.IRepository;
import asw1022.repositories.UserRepository;
import java.io.IOException;
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
public class RegisterServlet extends HttpServlet {
    
    public RegisterServlet(){
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
        } catch (Exception ex) {
            Logger.getLogger(RegisterServlet.class.getName()).log(Level.SEVERE, null, ex);
            request.setAttribute("msg", "Registration FAILED.");
        }
        
        //request.setAttribute("redirectUrl", "index.jsp");
        //request.setAttribute("destinationName", "Go back to index");
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
