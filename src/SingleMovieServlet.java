import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
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
@WebServlet(name = "SingleMovieServlet", urlPatterns = "/api/single-movie")
public class SingleMovieServlet extends HttpServlet {
    private static final long serialVersionUID = 4L;
    public static String TAG = "SingleMovieServlet: ";
    // Create a dataSource which registered in web.xml
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedbexample");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }


    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException,IOException {

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

            //We don't need to join the movies table and the star tables. Duplicate data
            // Construct a query with parameter represented by "?"
//            String query = "SELECT * from stars as s, stars_in_movies as sim, movies as m, ratings as r " +
//                    "where m.id = sim.movieId and sim.starId = s.id and r.movieId = m.id and m.id = ?";

            String query = "SELECT * FROM movies AS m, ratings AS r WHERE r.movieId = m.id and m.id = ?";

            // Declare our statement
            PreparedStatement statement = conn.prepareStatement(query);
            //Sorting by name in accordance to proj 2
            PreparedStatement genreStatement = conn.prepareStatement("SELECT g.id,g.name FROM genres AS g, genres_in_movies AS gim WHERE gim.movieid = ? AND gim.genreid = g.id ORDER BY g.name");
            //New prepareStatement for stars to order by their movie count and then by their Alphabetical order. Works by doing a subquery to get all the star ids who are in a given movie, and then joins stars and stars in movie, selecting only the rows whose star id are in that subquery.
            //Hence, selecting the correct stars but also being able to count the movies that the stars were in.
            PreparedStatement starsStatement = conn.prepareStatement("SELECT  s.id,s.name,s.birthYear,COUNT(*) FROM stars as s,stars_in_movies AS sim " +
                    "WHERE s.id = sim.starid AND s.id IN (SELECT s.id FROM stars AS s, stars_in_movies AS sim WHERE sim.movieid = ? AND sim.starid = s.id) GROUP BY (s.id) ORDER BY COUNT(sim.movieid) DESC, s.name ASC");

            // Set the parameter represented by "?" in the query to the id we get from url,
            // num 1 indicates the first "?" in the query
            statement.setString(1, id);

            // Perform the query
            ResultSet resultSet = statement.executeQuery();

            JsonArray jsonArray = new JsonArray();

            // Iterate through each row of result set
            //while (resultSet.next()) {
            resultSet.next();

            //Add arguments to the statement before executing
//                String starId = resultSet.getString("starId");
//                String starName = resultSet.getString("name");
            String movieId = resultSet.getString("movieId");
            String movieTitle = resultSet.getString("title");
            String movieYear = resultSet.getString("year");
            String movieDirector = resultSet.getString("director");
            String rating = resultSet.getString("rating");

            genreStatement.setString(1, movieId);
            starsStatement.setString(1,movieId);

            ResultSet genreResultSet = genreStatement.executeQuery();
            ResultSet starsResultSet = starsStatement.executeQuery();

            // Create a JsonObject based on the data we retrieve from rs

            JsonObject jsonObject = new JsonObject();
//                jsonObject.addProperty("star_id", starId);
//                jsonObject.addProperty("star_name", starName);
            jsonObject.addProperty("movie_id", movieId);
            jsonObject.addProperty("movie_title", movieTitle);
            jsonObject.addProperty("movie_year", movieYear);
            jsonObject.addProperty("movie_director", movieDirector);
            jsonObject.addProperty("rating", rating);
            //Adding the genres was relegated to a separate function
            MovieListServlet.addGenres(movieId,jsonObject,genreResultSet);

            //Same goes for the stars
            MovieListServlet.addStars(movieId,jsonObject,starsResultSet);

            jsonArray.add(jsonObject);


            genreResultSet.close();
            starsResultSet.close();
            //}
            //Need to close preparedStatements as well
            genreStatement.close();
            starsStatement.close();

            resultSet.close();
            statement.close();


            // Log to localhost log
            request.getServletContext().log("getting " + jsonArray.size() + " results");
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


