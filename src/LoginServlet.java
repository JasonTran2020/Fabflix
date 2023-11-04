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

@WebServlet(name = "LoginServlet", urlPatterns = "/api/login")
public class LoginServlet extends HttpServlet {
    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */
    //Lastest serialVersionUID was 6L, so bumping this up to 7L.
    private static final long serialVersionUID = 7L;
    public static final String TAG = "LoginServlet";
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedbexample");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        PrintWriter out = response.getWriter();
        JsonObject responseJsonObject = new JsonObject();

        if (username.equals("") || password.equals("")) {
            responseJsonObject.addProperty("status", "fail");
            responseJsonObject.addProperty("message", "Missing username or password");
            response.getWriter().write(responseJsonObject.toString());
            return;
        }

        try (Connection conn = dataSource.getConnection()) {
            String login_info_query = "SELECT c.*, cr.expiration \n" +
                    "FROM customers c \n" +
                    "JOIN creditcards cr ON c.ccId = cr.id \n" +
                    "WHERE c.email = ?;\n";

            PreparedStatement statement = conn.prepareStatement(login_info_query);

            statement.setString(1, username);

            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                String dbEmail = resultSet.getString("email");
                String dbPassword = resultSet.getString("password");
                int dbId = resultSet.getInt("id");
                String dbFirstName = resultSet.getString("firstName");
                String dbLastName = resultSet.getString("lastName");
                String dbCcId = resultSet.getString("ccId");
                String dbExpiryDate = resultSet.getString("expiration");
                String dbAddress = resultSet.getString("address");
                // Assuming passwords are stored as plaintext for this example, but hashing should be used.
                if (username.equals(dbEmail) && verifyEncryptedPassword(password,dbPassword)) {
                    request.getSession().setAttribute("user", new User(dbEmail, dbId, dbFirstName, dbLastName, dbCcId, dbExpiryDate, dbAddress));
                    responseJsonObject.addProperty("status", "success");
                    responseJsonObject.addProperty("message", "success");

                } else {
                    responseJsonObject.addProperty("status", "fail");
                    responseJsonObject.addProperty("message", "incorrect password");
                }
            }
            else {
                responseJsonObject.addProperty("status", "fail");
                responseJsonObject.addProperty("message", "user " + username + " doesn't exist");
            }
            response.getWriter().write(responseJsonObject.toString());
        } catch (Exception e) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());

            response.setStatus(500);

        } finally {
            out.close();
        }
    }
    protected boolean verifyEncryptedPassword(String inputtedPassword, String dbPassword)  {
        return new StrongPasswordEncryptor().checkPassword(inputtedPassword, dbPassword);
    }
}