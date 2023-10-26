import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

// Declaring a WebServlet called SingleStarServlet, which maps to url "/api/single-star"
@WebServlet(name = "SingleStarServlet", urlPatterns = "/api/single-star")
public class SingleStarServlet extends HttpServlet {
    private static final long serialVersionUID = 2L;
    public static String TAG = "SingleStarServlet: ";
    // Create a dataSource which registered in web.xml
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedbexample");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
     * response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        response.setContentType("application/json"); // Response mime type

        // Retrieve parameter id from url request.
        String id = request.getParameter("id");

        // The log message can be found in localhost log
        request.getServletContext().log(TAG + "getting id: " + id);

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        // Get a connection from dataSource and let resource manager close the connection after usage.
        try (Connection conn = dataSource.getConnection()) {
            // Get a connection from dataSource

            // Construct a query with parameter represented by "?"
            String query = "SELECT * from stars as s " +
                    "where s.id = ?";
            String movieQuery = "SELECT DISTINCT * FROM stars_in_movies as sim, movies AS m WHERE m.id = sim.movieId and sim.starId = ? ORDER BY year DESC, title ASC";
            // Declare our statement
            PreparedStatement statement = conn.prepareStatement(query);
            PreparedStatement movieStatement = conn.prepareStatement(movieQuery);

            // Set the parameter represented by "?" in the query to the id we get from url,
            // num 1 indicates the first "?" in the query
            statement.setString(1, id);
            movieStatement.setString(1,id);

            // Perform the query
            ResultSet rs = statement.executeQuery();
            ResultSet mrs = movieStatement.executeQuery();
            JsonArray jsonArray = new JsonArray();

            rs.next();
            JsonObject jsonObject = new JsonObject();
            String starId = rs.getString("id");
            String starName = rs.getString("name");
            String starDob = rs.getString("birthYear");
            if (rs.wasNull()) {
                starDob = "N/A"; // set it "N/A" if the birthYear was null.
            }
            jsonObject.addProperty("star_id", starId);
            jsonObject.addProperty("star_name", starName);
            jsonObject.addProperty("star_dob", starDob);

            JsonArray jsonMovieArray = new JsonArray();
            // Iterate through each row of the movies assocaited with this star
            while (mrs.next()) {


                String movieId = mrs.getString("movieId");
                String movieTitle = mrs.getString("title");
                String movieYear = mrs.getString("year");
                String movieDirector = mrs.getString("director");

                // Create a JsonObject based on the data we retrieve from mrs

                JsonObject jsonMovieObject = new JsonObject();

                jsonMovieObject.addProperty("movie_id", movieId);
                jsonMovieObject.addProperty("movie_title", movieTitle);
                jsonMovieObject.addProperty("movie_year", movieYear);
                jsonMovieObject.addProperty("movie_director", movieDirector);

                jsonMovieArray.add(jsonMovieObject);
            }
            //Close statements and result sets
            rs.close();
            mrs.close();
            statement.close();
            movieStatement.close();

            jsonObject.add("movies",jsonMovieArray);
            jsonArray.add(jsonObject);
            // Write JSON string to output
            out.write(jsonArray.toString());
            // Set response status to 200 (OK)
            response.setStatus(200);

        } catch (Exception e) {
            // Write error message JSON object to output
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());

            // Log error to localhost log
            request.getServletContext().log(TAG + "Error:", e);
            // Set response status to 500 (Internal Server Error)
            response.setStatus(500);
        } finally {
            out.close();
        }

        // Always remember to close db connection after usage. Here it's done by try-with-resources

    }

}
