package cz.cvut.fel.servlets;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/** @author Karel Cemus */
@WebServlet( name = "WelcomeServlet", urlPatterns = { "/faces/welcome.xhtml", "/welcome.xhtml" } )
public class WelcomeServlet extends HttpServlet {

    @Override
    protected void service( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException {

        // Redirect to the initial page.
        response.sendRedirect( request.getContextPath() + "/faces/flights.xhtml" );
    }
}
