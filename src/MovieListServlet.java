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
import javax.xml.transform.Result;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

@WebServlet(name = "MovieListServlet",urlPatterns = "/api/top-20-movie-list")
public class MovieListServlet extends HttpServlet {
    //Lastest serialVersionUID was 2L, so bumping this up to 3L. L is for Long
    private static final long serialVersionUID = 3L;

    // Create a dataSource which registered in web.xml
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            //Could use some understanding
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedbexample");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");// Response mime type
        //So what is mime type? basically tells the whoever reading the media type in two parts, "part1/part2"
        //"part1" is type, which you can usually derive in human types what it is (oh it's a text, video, image, etc)
        //"part2" is subtype, often specifying the format of the content which are often seen as file extensions, like .jpeg, .mp4, .txt.

        // Output stream to STDOUT
        PrintWriter out = resp.getWriter();

        try (Connection conn = dataSource.getConnection()){
            // Declare our general statement which we'll use to gather all the movies with the top 20 ratings
            Statement statement = conn.createStatement();

            // We also need to gather 3 stars and 3 genres for each movie. Unlike ratings and movies, which have a 1 to 1 relationship, genres and stars both
            //have a many-to-many relationship. I don't think it's worth creating one giant query that has everything
            PreparedStatement genreStatement = conn.prepareStatement("SELECT g.id,g.name FROM genres AS g, genres_in_movies AS gim WHERE gim.movieid = ? AND gim.genreid = g.id LIMIT 3");
            PreparedStatement starsStatement = conn.prepareStatement("SELECT s.id,s.name,s.birthYear FROM stars AS s, stars_in_movies AS sim WHERE sim.movieid = ? and sim.starid = s.id LIMIT 3");

            //Get top 20 movies by joining the movies and ratings table by movieId from ratings and id from movies.
            //And then order by descending and limiting by 20
            String query = "SELECT * FROM movies AS m, ratings AS r WHERE (r.movieId = m.id) ORDER BY (r.rating) DESC LIMIT 20";

            ResultSet resultSet = statement.executeQuery(query);

            //Conver stuff from resultSet into jsonArray because we said the content type we were going to do that
            JsonArray jsonArray = new JsonArray();

            //ResultSet holds a cursor pointing to BEFORE the first row initially, hence why we'll still get the first row even though the
            //while loop calls .next() first, which btw does what it sounds like it does to the cursor. It will return false if there are no more rows
            //Apparently some ResultSets are "scrollable" or "updatable", but it's safe to bet that we can at least go forward through the whole thing only once
            //In our case, we don't need more than that
            while (resultSet.next()){
                String movie_id = resultSet.getString("id");
                String title = resultSet.getString("title");
                int year = resultSet.getInt("year");
                String director =resultSet.getString("director");

                //Add arguments to the statement before executing
                genreStatement.setString(1, movie_id);
                starsStatement.setString(1,movie_id);

                ResultSet genreResultSet = genreStatement.executeQuery();
                ResultSet starsResultSet = starsStatement.executeQuery();

                JsonObject jsonMovieObject = new JsonObject();
                jsonMovieObject.addProperty("movie_id", movie_id);
                jsonMovieObject.addProperty("title",title);
                jsonMovieObject.addProperty("year",year);
                jsonMovieObject.addProperty("director",director);

            }

        }
        catch (Exception e){
            //Oopsies
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
