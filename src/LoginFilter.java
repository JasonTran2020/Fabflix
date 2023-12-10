import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import javax.sql.DataSource;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Servlet Filter implementation class LoginFilter
 */
@WebFilter(filterName = "LoginFilter", urlPatterns = "/*")
public class LoginFilter implements Filter {

    private static final long serialVersionUID = 8L;
    public static final String TAG = "LoginFilter";
    private DataSource dataSource;


    private final ArrayList<String> allowedURIs = new ArrayList<>();

    /**
     * @see Filter#doFilter(ServletRequest, ServletResponse, FilterChain)
     */
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        //System.out.println("LoginFilter: " + httpRequest.getRequestURI());

        // Check if this URL is allowed to access without logging in
        if (this.isUrlAllowedWithoutLogin(httpRequest.getRequestURI())) {
            // Keep default action: pass along the filter chain
            chain.doFilter(request, response);
            return;
        }
        HttpSession session = httpRequest.getSession(false);
        String userRole = (session != null) ? (String) session.getAttribute("userRole") : null;

        // Redirect to login page if the "user" attribute doesn't exist in session
        if (userRole == null) {
            httpResponse.sendRedirect("login.html");
        }
        else if ("customer".equals(userRole) || "employee".equals(userRole)) {
                chain.doFilter(request, response); // User is logged in and has a valid role{

        }

        else {
            chain.doFilter(request, response);
        }
    }

    private boolean isUrlAllowedWithoutLogin(String requestURI) {
        /*
         Setup your own rules here to allow accessing some resources without logging in
         Always allow your own login related requests(html, js, servlet, etc..)
         You might also want to allow some CSS files, etc..
         */
        if (requestURI.startsWith("/project1/_dashboard/") || allowedURIs.stream().anyMatch(requestURI.toLowerCase()::endsWith)) {
            return true;
        }
        return false;
    }

    public void init(FilterConfig fConfig) {
        allowedURIs.add("login.html");
        allowedURIs.add("login.js");
        allowedURIs.add("api/login");
        allowedURIs.add("general-css-files/tokens.css");
        allowedURIs.add("base.css");
        allowedURIs.add("login.css");
        allowedURIs.add("general-css-files/typography.module.css");
    }

    public void destroy() {
        // ignored.
    }

}