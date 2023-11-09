import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import javax.sql.DataSource;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Servlet Filter implementation class LoginFilter
 */
@WebFilter(filterName = "EmployeeLoginFilter", urlPatterns = "/_dashboard/*")
public class EmployeeLoginFilter implements Filter {
    private DataSource dataSource;


    private final ArrayList<String> allowedURIs = new ArrayList<>();

    /**
     * @see Filter#doFilter(ServletRequest, ServletResponse, FilterChain)
     */
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        System.out.println("EmployeeLoginFilter: " + httpRequest.getRequestURI());

        // Check if this URL is allowed to access without logging in
        if (this.isUrlAllowedWithoutLogin(httpRequest.getRequestURI())) {
            // Keep default action: pass along the filter chain
            chain.doFilter(request, response);
            return;
        }

        // Redirect to login page if the "user" attribute doesn't exist in session
        if (httpRequest.getSession().getAttribute("employee") == null) {
            httpResponse.sendRedirect("/_dashboard/login.html");
        } else {
            chain.doFilter(request, response);
        }
    }

    private boolean isUrlAllowedWithoutLogin(String requestURI) {
        /*
         Setup your own rules here to allow accessing some resources without logging in
         Always allow your own login related requests(html, js, servlet, etc..)
         You might also want to allow some CSS files, etc..
         */
        requestURI = requestURI.toLowerCase();

        return allowedURIs.stream().anyMatch(requestURI.toLowerCase()::endsWith) || requestURI.endsWith("api/login");
    }

    public void init(FilterConfig fConfig) {
        allowedURIs.add("login.html");
        allowedURIs.add("login.js");
        allowedURIs.add("api/login");
        allowedURIs.add("general-css-files/tokens.css");
        allowedURIs.add("base.css");
        allowedURIs.add("login.css");
        allowedURIs.add("general-css-files/typography.module.css");
        allowedURIs.add("_dashboard/login.html");
        allowedURIs.add("_dashboard/login.js");
        allowedURIs.add("_dashboard/login.css");

    }

    public void destroy() {
        // ignored.
    }
}
