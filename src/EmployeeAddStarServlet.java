
import com.google.gson.JsonObject;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jasypt.util.password.StrongPasswordEncryptor;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@WebServlet(name = "EmployeeAddStarServlet", urlPatterns = "/_dashboard/api/addstar")
public class EmployeeAddStarServlet extends HttpServlet {
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedbexample");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String star_name = request.getParameter("star_name");
        String birth_year = request.getParameter("birth_year");

        JsonObject responseJsonObject = new JsonObject();

        if (star_name.equals("")) {
            responseJsonObject.addProperty("status", "fail");
            responseJsonObject.addProperty("message", "Missing star name");
            response.getWriter().write(responseJsonObject.toString());
            return;
        }

        // make a connection to database
        // make a prepared statement (sql query)
        // execute it, and get the info back - idk if ihave to create a new query to retrieve
        // info, or original resultset will do it for me
        // either way i need to send info back

    }
}
