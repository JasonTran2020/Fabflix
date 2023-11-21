import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import datamodels.HttpRequestAttribute;
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
import java.sql.SQLException;

@WebServlet(name="AutocompleteServlet", urlPatterns = "/api/autocomplete")
public class AutocompleteServlet extends HttpServlet {

    private DataSource dataSource;
    private HttpRequestAttribute<String> phraseAttribute;
    private final String sqlFullAutocompleteClause = "SELECT id, title FROM movies WHERE MATCH (title) AGAINST ( ? IN BOOLEAN MODE) LIMIT 10";
    public void init(ServletConfig config) {
        //Initialize RequestAttributes here with new objects.
        phraseAttribute = new HttpRequestAttribute<>(String.class,"phrase");
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedbexample");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");

        PrintWriter out = resp.getWriter();
        try (Connection connection = dataSource.getConnection()){
            String phrase = phraseAttribute.get(req);
            JsonObject resultJsonObject = new JsonObject();

            if (phrase!=null && phrase.length()>=3){
                PreparedStatement statement = buildAutoCompletePreparedStatement(phrase,connection);
                JsonArray jsonMoviesArray = new JsonArray();
                ResultSet rs = statement.executeQuery();
                while (rs.next()){
                    JsonObject movieJsonObject = new JsonObject();
                    movieJsonObject.addProperty("id",rs.getString(1));
                    movieJsonObject.addProperty("title",rs.getString(2));
                    //Required json property for the 3rd party autocomplete library to know what to show the user in the autocomplete box
                    movieJsonObject.addProperty("value",rs.getString(2));
                    jsonMoviesArray.add(movieJsonObject);
                }
                resultJsonObject.add("movies",jsonMoviesArray);
            }
            else{
                //Give an empty JsonArray if the phrase is null or doesn't meet the minimum character limit
                resultJsonObject.add("movies", new JsonArray());
            }

            out.write(resultJsonObject.toString());


        }
        catch (SQLException e){
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

    protected PreparedStatement buildAutoCompletePreparedStatement(String phrase, Connection connection) throws SQLException {
        PreparedStatement result = connection.prepareStatement(sqlFullAutocompleteClause);
        StringBuilder arg= new StringBuilder();
        //Split by whitespace (space, tab, whatever)
        String[] words = phrase.split("\\s+");
        for (int x =0; x<words.length;x++){
            String word = words[x];
            //Because the AGAINST clause puts everything in a single string with '', we can't repeatedely put ? and set each one individually, as each ? will be put into single quotes
            //and the AGAINST clause only wants one pair of ''
            arg.append("+").append(word).append("* ");
        }
        result.setString(1,arg.toString());
        return result;
    }
}
