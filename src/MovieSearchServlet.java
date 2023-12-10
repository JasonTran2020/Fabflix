import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import datamodels.HttpRequestAttribute;
import datamodels.SessionMovieListParameters;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.sql.*;
import java.util.*;

@WebServlet(name = "MovieSearchServlet",urlPatterns = "/api/search-movie")
public class MovieSearchServlet extends HttpServlet {
    //Lastest serialVersionUID was 4L, so bumping this up to 5L.
    private static final long serialVersionUID = 5L;
    public static final String TAG = "MovieSearchServlet";
    // Create a dataSource which registered in web.xml
    private DataSource dataSource;
    private HttpRequestAttribute<String> titleAttribute;
    //int is a primitve type that isn't what you would consider an Object. Integer is a wrapper class to make int into an Object
    //UPDATE. You can just make it a string. Causes less problems anyways
    private HttpRequestAttribute<String> yearAttribute;
    private HttpRequestAttribute<String> directorAttribute;
    //Would make sense to be able to search for multiple stars, but rubric seems to just ask for one. Might change later
    //private List<HttpRequestAttribute<String>> starsAttribute;
    private HttpRequestAttribute<String> starAttribute;
    private HttpRequestAttribute<String> browsingAttribute;
    private HttpRequestAttribute<String> genreNameAttribute;
    private HttpRequestAttribute<String> charAttribute;
    private HttpRequestAttribute<String> fullTextAttribute;
    private final String sqlSelectCountClause = "SELECT COUNT(*) as max ";
    private final String sqlSearchSelectClause = "SELECT * ";
    private final String sqlSearchFromWhereWithStarClause = " FROM movies AS m, ratings AS r, stars AS s, stars_in_movies as sim WHERE (r.movieId = m.id) AND (sim.starId = s.id) AND (sim.movieId = m.id) ";
    private final String sqlSearchFromWhereNoStarClause = " FROM movies AS m, ratings AS r WHERE (r.movieId = m.id) ";
    private final String sqlBrowseSelectClause = "SELECT DISTINCT m.id,m.title,m.year,m.director,r.rating,r.numVotes  ";
    private final String sqlBrowseFromWhereClause = " FROM movies AS m, ratings AS r, genres_in_movies AS gim , genres AS g  WHERE (gim.genreId = g.id) AND (gim.movieId = m.id) AND (r.movieId = m.id)  ";
    private File logFile;
    private FileWriter logFileWriter;

    public void init(ServletConfig config) throws ServletException {
        //Initialize RequestAttributes here with new objects.
        titleAttribute = new HttpRequestAttribute<>(String.class,"title");
        yearAttribute = new HttpRequestAttribute<>(String.class,"year");
        directorAttribute = new HttpRequestAttribute<>(String.class,"director");
        starAttribute = new HttpRequestAttribute<>(String.class,"star");

        browsingAttribute = new HttpRequestAttribute<>(String.class,"browsing");
        genreNameAttribute = new HttpRequestAttribute<>(String.class,"genre");
        charAttribute = new HttpRequestAttribute<>(String.class,"char");

        fullTextAttribute = new HttpRequestAttribute<>(String.class,"fulltext");
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedbexample");
        } catch (NamingException e) {
            e.printStackTrace();
        }
        //Set up file for logging time
        //Gotta use the config parameter, not getServletConfig or just getServletContext because we didn't call super.init
        System.out.println(config.getServletContext());
        System.out.println(config.getServletContext().getRealPath("/"));
        String logFilePath = config.getServletContext().getRealPath("/")+ FileSystems.getDefault().getSeparator()+"search_time_log.txt";
        System.out.println("Search servlet log file path: "+logFilePath);
        File logFile = new File(logFilePath);
        try {
            logFile.createNewFile();
            logFileWriter = new FileWriter(logFile.getAbsoluteFile(), true);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        long startTimeTS = System.nanoTime();
        long totalTimeTJ = 0;
        resp.setContentType("application/json");// Response mime type

        // Output stream to STDOUT
        PrintWriter out = resp.getWriter();

        //Include the time to get the connection
        long totalConnTime = 0;
        long connStartTime = System.nanoTime();
        try (Connection conn = dataSource.getConnection()){
            // Vast majority of this was dervied from MovietListServlet. Check for more detailed comments. Comments here are more specific to searching
            long connEndTime = System.nanoTime();
            totalConnTime = connEndTime-connStartTime;

            //Build out a query and arguments using parameters from the HttpServletRequest which may contain the title, year, director, and/or star
            //Making a prepare statement as the arguments come from a user with potential malicious intent of using SQL injection
            String isBrowsing = browsingAttribute.get(req);
            String isFullText = fullTextAttribute.get(req);
            String backPage = "";
            int max=0;
            PreparedStatement statement;
            if (isBrowsing!=null && isBrowsing.equals("true")){
                statement = buildBrowsePrepareStatement(req,conn,sqlBrowseSelectClause,true);
                //Build a statement with the same WHERE clause as the user wants, just counts how many movies. getMax deals with the MySql details of executing and getting the result
                //This isn't integrated with the above SQL query because it uses limits and offset, which would give us the wrong count
                max = getMax(buildBrowsePrepareStatement(req,conn,sqlSelectCountClause,false));
                backPage = "browse";
            }
            else if(isFullText!=null && isFullText.equals("true")){
                statement = buildFullTextPrepareStatement(req,conn,sqlSearchSelectClause,true);
                max = getMax(buildFullTextPrepareStatement(req,conn,sqlSelectCountClause,false));
                backPage = "fulltext";
            }
            else{
                statement = buildSearchPrepareStatement(req,conn,sqlSearchSelectClause,true);
                max = getMax(buildSearchPrepareStatement(req,conn,sqlSelectCountClause,false));
                backPage = "search";
            }
            long starTimeTJ = System.nanoTime();
            ResultSet resultSet = statement.executeQuery();

            //Convert stuff from resultSet into jsonArray because we said the content type we were going to do that
            JsonArray jsonArray = MovieListServlet.buildMovieJsonArray(resultSet,conn);
            long endTimeTJ = System.nanoTime();
            // Log to localhost log
            req.getServletContext().log("getting " + jsonArray.size() + " results");
            JsonObject jsonObject = new JsonObject();
            jsonObject.add("movies",jsonArray);
            jsonObject.addProperty("isLastPage",MovieListServlet.isLastPage(req,max));
            out.write(jsonObject.toString());
            //Close the prepared statements
            statement.close();
            resultSet.close();
            //Method to save current parameters to a session to be used when "jumping" between pages
            MovieListServlet.saveMovieListParameters(req,backPage);
            // Set response status to 200 (OK)
            resp.setStatus(200);
            long endTimeTS = System.nanoTime();
            long totalTimeTS = endTimeTS-startTimeTS;
            totalTimeTJ = endTimeTJ - starTimeTJ + totalConnTime;

            logFileWriter.write("search servlet total execution time:"+totalTimeTS+", JDBC execution time:"+totalTimeTJ+"\n");
            logFileWriter.flush();

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

    private int getMax( PreparedStatement fromWhereClause) throws SQLException {


        ResultSet rs = fromWhereClause.executeQuery();
        rs.next();
        int result = rs.getInt("max");
        rs.close();
        fromWhereClause.close();
        return result;

    }
    //We return a List of Objects, the base class for all other classes in Java
    //The type List<?> is a valid syntax, BUT, it means something different. It essentially means the list can be any type, BUT ONLY 1 TYPE. As in it can be List<String>, List<int>, BUT NOT List<String and int>
    //List<Object> is what enables the latter
    //UPDATE: Yea not going that way anymore, but leaving the comments here because it's useful to learn the diff between ? and Object
    private PreparedStatement buildSearchPrepareStatement(HttpServletRequest request,Connection conn, String selectClause, boolean usePagination) throws SQLException {
        //This method returns a prepared statement, with all the appropiate arguments from "request" already set in the correct positions
        String title = titleAttribute.get(request);
        String year = yearAttribute.get(request);
        String director = directorAttribute.get(request);
        String star = starAttribute.get(request);
        //boolean first = true;
        ArrayList<String> args = new ArrayList<>();
        String result;
        //Assumption that star is null, in which we don't need to include the star in movies table in our search
        if (star==null || star.isEmpty()){
            result = selectClause + sqlSearchFromWhereNoStarClause;
        }
        //star is not NULL
        else {
            result = selectClause + sqlSearchFromWhereWithStarClause;
            result += " AND " + this.buildLikeQueryString(star,"s.name", args);
        }

        if (title!=null /*&& first*/ && !title.isEmpty()){
            result += " AND " + this.buildLikeQueryString(title,"m.title", args);
        }

        if (director != null && !director.isEmpty()){
            result += " AND " + this.buildLikeQueryString(director, "m.director",args);
        }

        if (year != null  && !year.isEmpty()){
            result += " AND ( m.year = ?" + ") ";
            //Manually add year here as we don't use the buildLikeQueryString method here
            args.add(year);
            //first = false;
        }

        //Terminate the statement
        if (usePagination){
            result += MovieListServlet.buildOrderByClause(request,"m","r" ) + " " + MovieListServlet.buildPaginationClause(request);
        }

        request.getServletContext().log(TAG + " The complete SQL statement is \"" + result + "\"");
        request.getServletContext().log(TAG + " The number of args are \"" + args.size() + "\"");
        request.getServletContext().log(TAG + " Args are \"" + args.toString() + "\"");
        PreparedStatement statement = conn.prepareStatement(result);
        //Making a prepare statement as the arguments come from a user with potential malicious intent of using SQL injection

        for (int x = 0; x < args.size(); x++){
            //The only argument in this whole statement that would NOT be a string is the year, which will have a special if statement to use setInt instead
            //setString works locally, BUT, there is no assruance that it will work with other db drivers, so let's just be careful and use setInt
            //Have to check that year is both not null and not empty. Possible to have a year="" by manipulating url
            if (x == args.size()-1 && year !=null && !year.isEmpty()){
                statement.setInt(x+1,Integer.parseInt(args.get(x)));
            }
            else{
                //Remember, SQL is 1-based index
                statement.setString(x+1, args.get(x));
            }

        }
        return statement;

    }


    private String buildLikeQueryString(String argument, String columnName, ArrayList<String> args){
        //Ok, why are we storing the args in a parameter in stead of just sticking it into a string? Well, it's for SQL injection security
        //Quite certain that the setArg on a prepared statement is safer than directly building a query from a string.
        //This function will take a string in, split it into words, and create a LIKE sql clause in parentheses
        // On MySql8, ILIKE does NOT exist. However
        StringBuilder result = new StringBuilder("( ");
        //The way the requested by the TAs
        result.append(" ").append(columnName).append(" LIKE ").append("?").append(" ");
        args.add("%"+argument+"%");
        //The older way that gave more results (albeit not as relevant)
//        boolean first = true;
//        // \s means whitespace, but \ means an escape character, so we need \\s. the + means 1 or more, which is what we want
//        String[] words =argument.split("\\s+");
//        for (String word : words) {
//            if (!first) {
//                result.append(" OR ");
//            }
//            //Appending a "?" instead of the argument directly for safety
//            result.append(" ").append(columnName).append(" LIKE ").append("?").append(" ");
//            //So that next iteration there is OR
//            first = false;
//            //Store the argument separately and also add the wildcard characters here, rather than when setting the arguments in the prepared statement
//            // (because year doesn't deserve wildcards)
//            args.add("%" + word + "%");
//
//        }
        result.append(")");
        return result.toString();

    }

    private PreparedStatement buildBrowsePrepareStatement(HttpServletRequest request,Connection conn, String selectClause,boolean usePagination) throws SQLException {
        String genre = genreNameAttribute.get(request);
        String character = charAttribute.get(request);
        ArrayList<String> args = new ArrayList<>();

        String result = selectClause + sqlBrowseFromWhereClause;

        if (genre!=null && !genre.isEmpty()){
            result += " AND (g.name = ?) ";
            args.add(genre);
        }

        if (character != null && !character.isEmpty() ){
            //Special case wildcard characters, as we looking for titles that DON'T start with ANY alpha-numeric characters
            //No insertion to args here
            if (character.equals("*")){
                //Honestly not that good with regex, but learned something from this
                //There are two main things the caret symbol (^) does – it matches the start of a line or the start of a string, and it negates a character set when you put it inside the square brackets
                //Therefore, this is REGEXP is basically saying "Select the movies where the title STARTS with characers from this set, and the set is saying NOT a-z and 0-9. MySql by default doesn't seem to care for capital letters
                result += " AND (m.title REGEXP \"^[^a-z0-9]+\" )";
            }
            else{
                result += " AND (m.title LIKE ?)";
                //Add 0 or more wild card to the end of character
                args.add(character + "%");
            }

        }
        if (usePagination){
            result += MovieListServlet.buildOrderByClause(request,"m","r" ) + " " + MovieListServlet.buildPaginationClause(request);
        }

        request.getServletContext().log(TAG + " The complete SQL statement is \"" + result + "\"");
        request.getServletContext().log(TAG + " The number of args are \"" + args.size() + "\"");
        request.getServletContext().log(TAG + " Args are \"" + args.toString() + "\"");
        PreparedStatement statement = conn.prepareStatement(result);

        for (int x = 0; x < args.size(); x++){
            //Remember, SQL is 1-based index
            statement.setString(x+1, args.get(x));
        }
        return statement;

    }

    private PreparedStatement buildFullTextPrepareStatement(HttpServletRequest request,Connection conn, String selectClause,boolean usePagination) throws SQLException{
        String title = titleAttribute.get(request);
        ArrayList<String> args = new ArrayList<>();
        StringBuilder arg = new StringBuilder();
        StringBuilder resultStringBuilder = new StringBuilder();
        resultStringBuilder.append(selectClause);
        resultStringBuilder.append(sqlSearchFromWhereNoStarClause);
        if (title != null && !title.isEmpty()){
            resultStringBuilder.append(" AND MATCH (m.title) AGAINST (");
            //Split by whitespace (space, tab, whatever)
            String[] words = title.split("\\s+");
            for (int x =0; x<words.length;x++){
                String word = words[x];
                //Because the AGAINST clause puts everything in a single string with '', we can't repeatedely put ? and set each one individually, as each ? will be put into single quotes
                //and the AGAINST clause only wants one pair of ''
                arg.append("+").append(word).append("* ");
            }
            resultStringBuilder.append("? ");
            resultStringBuilder.append(" IN BOOLEAN MODE)");
        }

        if (usePagination){
            resultStringBuilder.append(MovieListServlet.buildOrderByClause(request, "m", "r")).append(" ").append(MovieListServlet.buildPaginationClause(request));
        }
        String resultString = resultStringBuilder.toString();
        request.getServletContext().log(TAG + " The complete SQL statement is \"" + resultString + "\"");
        request.getServletContext().log(TAG + " The number of args are \"" + args.size() + "\"");
        request.getServletContext().log(TAG + " Args are \"" + args.toString() + "\"");
        PreparedStatement resultStatement = conn.prepareStatement(resultString);
        if (!arg.toString().isEmpty()){
            resultStatement.setString(1, arg.toString());
        }


        request.getServletContext().log(TAG + " The complete SQL statement after inserting arguments is \"" + resultStatement + "\"");
        return resultStatement;
    }

}
