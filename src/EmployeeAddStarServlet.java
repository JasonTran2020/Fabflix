
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
        PrintWriter out = response.getWriter();
        if (star_name == null || star_name.trim().isEmpty()) {
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

        try (Connection connection = dataSource.getConnection()) {
            String newId = "";
            PreparedStatement getMaxId = connection.prepareStatement("SELECT max(id) AS MAX FROM stars");
            ResultSet resultSet = getMaxId.executeQuery();
            String id = "";
            if (resultSet.next()) {
                String maxId = resultSet.getString("max");
                String prefix = maxId.substring(0, 2);
                String numberPart = maxId.substring(2);
                int num = Integer.parseInt(numberPart);
                num++;
                String incrementedNumberPart = String.format("%07d", num);
                newId = prefix + incrementedNumberPart;

            }

            PreparedStatement insertNewStar = connection.prepareStatement("INSERT INTO stars (id, name, birthYear) VALUES (?, ?, ?)");
            insertNewStar.setString(1, newId);
            insertNewStar.setString(2, star_name);

            if (birth_year == null || birth_year.trim().isEmpty()) {
                insertNewStar.setNull(3, java.sql.Types.INTEGER);
            } else {
                insertNewStar.setInt(3, Integer.parseInt(birth_year));
            }

            insertNewStar.executeUpdate();

            responseJsonObject.addProperty("status", "success");
            responseJsonObject.addProperty("message", "Success! New star with id " + newId + " has been added to the database.");
            out.write(responseJsonObject.toString());
            insertNewStar.close();
            connection.close();
        }

        catch (Exception e) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());

            response.setStatus(500);

        } finally {
            out.close();
        }
    }
}
