import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import datamodels.HttpRequestAttribute;
import datamodels.SessionMovieListParameters;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import javax.xml.transform.Result;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import java.util.*;

@WebServlet(name = "MovieListServlet",urlPatterns = "/api/top-20-movie-list")
public class MovieListServlet extends HttpServlet {
    //Lastest serialVersionUID was 2L, so bumping this up to 3L. L is for Long
    private static final long serialVersionUID = 3L;
    public static final String TAG = "MovieListServlet";
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

            //Get top 20 movies by joining the movies and ratings table by movieId from ratings and id from movies.
            //And then order by descending and limiting by 20
            String query = "SELECT * FROM movies AS m, ratings AS r WHERE (r.movieId = m.id) " + buildOrderByClause(req,"m","r") + " " + buildPaginationClause(req);
            req.getServletContext().log(TAG + " the final query is: " + query);
            ResultSet resultSet = statement.executeQuery(query);

            JsonArray jsonArray = buildMovieJsonArray(resultSet,conn);

            // Log to localhost log
            req.getServletContext().log("getting " + jsonArray.size() + " results");
            //Write out json response
            out.write(jsonArray.toString());

            //Close the statement for finding the movies
            statement.close();
            //Close the resultset
            resultSet.close();
            // Set response status to 200 (OK)
            resp.setStatus(200);

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
    public static void addGenres(String movie_id,JsonObject jsonMovieObject, ResultSet genreResultSet) throws java.sql.SQLException{
        //Add all the associated movieGenres as a JsonArray, since there are a variable amount of them per list
        JsonArray movieGenres = new JsonArray();
        while (genreResultSet.next()){
            JsonObject jsonGenreObject = new JsonObject();
            jsonGenreObject.addProperty("id",genreResultSet.getInt("id"));
            jsonGenreObject.addProperty("name",genreResultSet.getString("name"));


            //Add the genre object to the Json array
            movieGenres.add(jsonGenreObject);
        }
        //Finally genres Jsonarray itself
        jsonMovieObject.add("genres",movieGenres);

    }

    public static void addStars(String movie_id,JsonObject jsonMovieObject, ResultSet starsResultSet) throws java.sql.SQLException{
        //Time to add the stars associated with the movie, also as a JsonArray
        JsonArray movieStars= new JsonArray();
        while (starsResultSet.next()){
            JsonObject jsonStarObject = new JsonObject();
            jsonStarObject.addProperty("id",starsResultSet.getString("id"));
            jsonStarObject.addProperty("name",starsResultSet.getString("name"));
            //Yes, the resultset has birthyear as 1 word with no underscore
            jsonStarObject.addProperty("birth_year",starsResultSet.getInt("birthyear"));

            //Add the star json object to the movieStars JsonArray
            movieStars.add(jsonStarObject);
        }
        //Now we get to add the stars as a Json array
        jsonMovieObject.add("stars",movieStars);

    }

    public static String buildOrderByClause(HttpServletRequest request, String movieTbName, String ratingTbName){
        HttpRequestAttribute<String> orderByAttribute = new HttpRequestAttribute<>(String.class,"orderby");
        request.getServletContext().log(TAG + "orderBy is: "+orderByAttribute.get(request));

        String orderBy = (orderByAttribute.get(request)!=null ) ? orderByAttribute.get(request) : "";
        return parseOrderByParameter(orderBy,movieTbName,ratingTbName);

    }
    private static String parseOrderByParameter(String input,String movieTbName, String ratingTbName ){
        // Functions assumes that the string
        if (input.length()!=4){
            return " ORDER BY " + ratingTbName +".rating DESC, " + movieTbName +".title ASC";
        }
        HashMap<String,String> orderByDict = new HashMap<>();
        orderByDict.put("t",movieTbName+".title");
        orderByDict.put("r",ratingTbName+".rating");
        orderByDict.put("a","ASC");
        orderByDict.put("d","DESC");
        return " ORDER BY " + orderByDict.getOrDefault(input.substring(0,1),ratingTbName+".rating ") + " "+ orderByDict.getOrDefault(input.substring(1,2),"DESC") +
                ", " + orderByDict.getOrDefault(input.substring(2,3),movieTbName+".title ") + " " + orderByDict.getOrDefault(input.substring(3,4),"ASC");

    }


    public static String buildPaginationClause(HttpServletRequest request){
        HttpRequestAttribute<String> perPageAttribute = new HttpRequestAttribute<>(String.class,"pp");
        HttpRequestAttribute<String> pageAttribute = new HttpRequestAttribute<>(String.class,"p");
        request.getServletContext().log(TAG + "perpage is: " + perPageAttribute.get(request));
        request.getServletContext().log(TAG + " page is: " + pageAttribute.get(request));
        List permittedPerPage = Arrays.asList(10, 25, 50, 100);
        try{
            int perPageInt = handleParseInt(perPageAttribute.get(request),25);
            int pageInt = handleParseInt(pageAttribute.get(request),1);
            if (pageInt<=0){
                pageInt = 1;
            }
            if (permittedPerPage.contains(perPageInt)){
                return " LIMIT " + perPageInt +" OFFSET " + (perPageInt*(pageInt-1));
            }
            return " LIMIT 25 " + " OFFSET " + (25*(pageInt-1));

        }
        catch (NumberFormatException e){
            return " LIMIT 25 ";
        }
    }

    public static int handleParseInt(String input, int defaultValue){
        try{
            return Integer.parseInt(input);
        }
        catch (Exception e){
            return defaultValue;
        }
    }
    public static boolean isLastPage(HttpServletRequest request, int max){
        HttpRequestAttribute<String> perPageAttribute = new HttpRequestAttribute<>(String.class,SessionMovieListParameters.parameterMoviesPerPage);
        HttpRequestAttribute<String> pageAttribute = new HttpRequestAttribute<>(String.class,SessionMovieListParameters.parameterCurrentPage);
        request.getServletContext().log(TAG + "perpage is: " + perPageAttribute.get(request));
        request.getServletContext().log(TAG + " page is: " + pageAttribute.get(request));
        List permittedPerPage = Arrays.asList(10, 25, 50, 100);

        int perPageInt = MovieListServlet.handleParseInt(perPageAttribute.get(request),25);
        int pageInt = MovieListServlet.handleParseInt(pageAttribute.get(request),1);
        if (pageInt<=0){
            pageInt = 1;
        }
        if (permittedPerPage.contains(perPageInt)){
            return perPageInt*(pageInt-1)+perPageInt >= max;
        }
        return (25*(pageInt-1))+perPageInt >= max;
    }
    public static void saveMovieListParameters(HttpServletRequest req,String backPage){
        HttpSession session = req.getSession();
        String result = SessionMovieListParameters.parseParametersStringFromRequest(req);
        session.setAttribute(SessionMovieListParameters.sessionKeyName,result);
        session.setAttribute(SessionMovieListParameters.sessionKeyBackPage, backPage);
        req.getServletContext().log(TAG +": The parameters that were saved were " + result);
    }


    public static JsonArray buildMovieJsonArray(ResultSet movies, Connection conn) throws SQLException {
        //This method was created to deal with the conversion of SQL data to JsonArray AND because the MovieSearchServlet does a similar thing, so to avoid
        //copied code, this is all going into a static method. Structurally it work make more sense for MovieSearchServlet to inherit from MovieListServlet or we put all these static methods
        //in a separate class, but I'm getting really tired right now

        // We also need to gather 3 stars and 3 genres for each movie. Unlike ratings and movies, which have a 1 to 1 relationship, genres and stars both
        //have a many-to-many relationship. I don't think it's worth creating one giant query that has everything
        //However, that stuff was refactored to the two private functions with names that sound like what they are suppose to do. I'm losing it
        PreparedStatement genreStatement = conn.prepareStatement("SELECT g.id,g.name FROM genres AS g, genres_in_movies AS gim WHERE gim.movieid = ? AND gim.genreid = g.id ORDER BY g.name");
        //PreparedStatement starsStatement = conn.prepareStatement("SELECT s.id,s.name,s.birthYear, sim.movieid FROM stars AS s, stars_in_movies AS sim WHERE sim.movieid = ? AND sim.starid = s.id GROUP BY (s.id) ORDER BY COUNT(sim.movieid) DESC, s.name ASC");
        //New prepareStatement for stars to order by their movie count and then by their Alphabetical order. Works by doing a subquery to get all the star ids who are in a given movie, and then joins stars and stars in movie, selecting only the rows whose star id are in that subquery.
        //Hence, selecting the correct stars but also being able to count the movies that the stars were in.
        PreparedStatement starsStatement = conn.prepareStatement("SELECT  s.id,s.name,s.birthYear,COUNT(*) FROM stars as s,stars_in_movies AS sim WHERE s.id = sim.starid AND s.id IN (SELECT s.id " +
                "FROM stars AS s, stars_in_movies AS sim WHERE sim.movieid = ? AND sim.starid = s.id) GROUP BY (s.id) ORDER BY COUNT(sim.movieid) DESC, s.name ASC");
        JsonArray jsonArray = new JsonArray();
        try{
            //Convert stuff from resultSet into jsonArray because we said the content type we were going to do that
            //ResultSet holds a cursor pointing to BEFORE the first row initially, hence why we'll still get the first row even though the
            //while loop calls .next() first, which btw does what it sounds like it does to the cursor. It will return false if there are no more rows
            //Apparently some ResultSets are "scrollable" or "updatable", but it's safe to bet that we can at least go forward through the whole thing only once
            //In our case, we don't need more than that
            while (movies.next()){
                String movie_id = movies.getString("id");
                String title = movies.getString("title");
                int year = movies.getInt("year");
                String director =movies.getString("director");
                float rating = movies.getFloat("rating");

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
                jsonMovieObject.addProperty("rating",rating);

                //Adding the genres was relegated to a separate function
                MovieListServlet.addGenres(movie_id,jsonMovieObject,genreResultSet);

                //Same goes for the stars
                MovieListServlet.addStars(movie_id,jsonMovieObject,starsResultSet);

                // STILL NEED TO CLOSE THE genre and stars resultSets here! They are created in each iteration and are not needed for the next!
                genreResultSet.close();
                starsResultSet.close();

                jsonArray.add(jsonMovieObject);
            }

        }
        finally {
            genreStatement.close();
            starsStatement.close();
        }

        return jsonArray;

    }
}
