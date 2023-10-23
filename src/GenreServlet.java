import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

@WebServlet(name = "GenreServlet", urlPatterns = "/api/genres")
public class GenreServlet extends HttpServlet {

    //Lastest serialVersionUID was 5L, so bumping this up to 6L.
    private static final long serialVersionUID = 6L;
    public static final String TAG = "GenreServlet";
    private DataSource dataSource;


    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedbexample");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");// Response mime type
        // Output stream to STDOUT
        PrintWriter out = resp.getWriter();
        try (Connection conn = dataSource.getConnection()){
            // Vast majority of this was dervied from MovietListServlet. Check for more detailed comments. Comments here are more specific to searching

            //Build out a query and arguments using parameters from the HttpServletRequest which may contain the title, year, director, and/or star
            //Making a prepare statement as the arguments come from a user with potential malicious intent of using SQL injection
            Statement statement = conn.createStatement();

            ResultSet resultSet = statement.executeQuery("SELECT * FROM genres as g ORDER BY g.name");
            JsonArray jsonArray = new JsonArray();
            while(resultSet.next()){
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("id",resultSet.getInt("id"));
                jsonObject.addProperty("name",resultSet.getString("name"));

                jsonArray.add(jsonObject);
            }


            // Log to localhost log
            req.getServletContext().log("getting " + jsonArray.size() + " results");
            out.write(jsonArray.toString());
            //Close the prepared statements

            statement.close();
            //Close the resultset
            resultSet.close();
            // Set response status to 200 (OK)
            resp.setStatus(200);

        }
        catch (Exception e){
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());

            // Set response status to 500 (Internal Server Error)
            resp.setStatus(500);

        }
        finally {
            out.close();
        }
    }


}
