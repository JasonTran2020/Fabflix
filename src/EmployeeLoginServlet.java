
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


@WebServlet(name = "EmployeeLoginServlet", urlPatterns = "/_dashboard/api/login")
public class EmployeeLoginServlet extends HttpServlet {
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
            PreparedStatement statement = conn.prepareStatement("SELECT * FROM employees WHERE email = ? LIMIT 1");
            statement.setString(1, username);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()){
                String encryptedPassword = resultSet.getString("password");
                if (verifyEncryptedPassword(password, encryptedPassword)) {
                    Employee employee = new Employee(resultSet.getString("email"), resultSet.getString("password"), resultSet.getString("fullname"));
                    request.getSession().setAttribute("employee", employee);
                    responseJsonObject.addProperty("status", "success");
                    responseJsonObject.addProperty("message", "success");
                    request.getSession().setAttribute("userRole", "employee");
                }

                else {
                    responseJsonObject.addProperty("status", "fail");
                    responseJsonObject.addProperty("message", "incorrect password");
                }
            }

            else {
                responseJsonObject.addProperty("status", "fail");
                responseJsonObject.addProperty("message", "employee " + username + " doesn't exist");
            }
            out.write(responseJsonObject.toString());
            resultSet.close();
            statement.close();

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
    protected boolean verifyEncryptedPassword(String inputtedPassword, String dbPassword)  {
        return new StrongPasswordEncryptor().checkPassword(inputtedPassword, dbPassword);
    }
}
