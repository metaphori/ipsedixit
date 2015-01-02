package asw1022.controllers;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author Roberto Casadei <roberto.casadei12@studio.unibo.it>
 */
public class HomeController extends HttpServlet {
    
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
        String pageTitle = "unknown";
        
        if (userPath.equals("/") || userPath.equals("/Home")) {
            jspPage = "home";
            pageTitle = "Home";
        } else if (userPath.equals("/About")) {
            jspPage = "about";
            pageTitle = "About";
        } else{
            jspPage = "error";
            pageTitle = "404 Error";
            request.setAttribute("errorTitle", "404 Error");
            request.setAttribute("errorMsg", "This page could not be found.");
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
