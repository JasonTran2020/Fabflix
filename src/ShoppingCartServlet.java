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
import java.util.Collections;
import java.util.HashMap;


@WebServlet(name = "ShoppingCartServlet", urlPatterns = "/api/cart")
public class ShoppingCartServlet extends HttpServlet {

    private static final long serialVersionUID = 10L;
    public static String TAG = "ShoppingCartServlet: ";
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
        String itemIdsParam = request.getParameter("itemIds");
        // The log message can be found in localhost log
        request.getServletContext().log(TAG + "getting ids: " + itemIdsParam);

        String[] itemIdArray = itemIdsParam.split(",");

        HashMap<String, Integer> itemCountMap = new HashMap<>();
        for (String itemId : itemIdArray) {
            itemCountMap.put(itemId, itemCountMap.getOrDefault(itemId, 0) + 1);
        }

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        // Get a connection from dataSource and let resource manager close the connection after usage.
        try (Connection conn = dataSource.getConnection()) {
            JsonArray jsonMovieArray = new JsonArray();

            String movie_query = "SELECT * FROM movies WHERE id IN (" + String.join(",", Collections.nCopies(itemIdArray.length, "?")) + ")";
            PreparedStatement statement = conn.prepareStatement(movie_query);
            for (int i = 0; i < itemIdArray.length; i++) {
                statement.setString(i + 1, itemIdArray[i]);
            }

            ResultSet resultSet = statement.executeQuery();


            while (resultSet.next()) {
                // Create a JsonObject based on the data we retrieve from mrs

                JsonObject jsonMovieObject = new JsonObject();


                jsonMovieObject.addProperty("movie_id", resultSet.getString("id"));
                jsonMovieObject.addProperty("movie_title", resultSet.getString("title"));
                jsonMovieObject.addProperty("movie_price", resultSet.getString("price"));

                int frequency = itemCountMap.getOrDefault(resultSet.getString("id"), 0);
                jsonMovieObject.addProperty("movie_frequency", frequency);

                jsonMovieArray.add(jsonMovieObject);
            }

            //Close statements and result sets
            resultSet.close();
            statement.close();
            out.write(jsonMovieArray.toString());
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
