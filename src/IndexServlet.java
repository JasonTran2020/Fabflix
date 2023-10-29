import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import javax.sql.DataSource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

/**
 * This IndexServlet is declared in the web annotation below,
 * which is mapped to the URL pattern /api/index.
 */
@WebServlet(name = "IndexServlet", urlPatterns = "/api/index")
public class IndexServlet extends HttpServlet {
    private static final long serialVersionUID = 9L;
    public static String TAG = "IndexServlet: ";
    // Create a dataSource which registered in web.xml
    private DataSource dataSource;

    /**
     * handles GET requests to store session information
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();
        String sessionId = session.getId();
        long lastAccessTime = session.getLastAccessedTime();

        JsonObject responseJsonObject = new JsonObject();
        responseJsonObject.addProperty("sessionID", sessionId);
        responseJsonObject.addProperty("lastAccessTime", new Date(lastAccessTime).toString());

        ArrayList<String> previousItems = (ArrayList<String>) session.getAttribute("previousItems");
        if (previousItems == null) {
            previousItems = new ArrayList<String>();
        }
        // Log to localhost log
        request.getServletContext().log("getting " + previousItems.size() + " items");
        JsonArray previousItemsJsonArray = new JsonArray();
        previousItems.forEach(previousItemsJsonArray::add);
        responseJsonObject.add("previousItems", previousItemsJsonArray);

        // write all the data into the jsonObject
        response.getWriter().write(responseJsonObject.toString());
    }

    /**
     * handles POST requests to add and show the item list information
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String movie_id = request.getParameter("movieid");
        String action = request.getParameter("action");
        System.out.println(movie_id);
        HttpSession session = request.getSession();
        // make a query to DB
        // get the previous items in a ArrayList
        ArrayList<String> previousItems = (ArrayList<String>) session.getAttribute("previousItems");
        if (action.equals("add")) {
            synchronized (session) {
                if (previousItems == null) {
                    previousItems = new ArrayList<String>();
                    previousItems.add(movie_id);
                    session.setAttribute("previousItems", previousItems);
                } else {
                    previousItems.add(movie_id);
                }
            }
        } else if (action.equals("remove")) {
            synchronized (session) {
                if (previousItems != null) {
                    previousItems.remove(movie_id); // This removes the first occurrence of the movie_id in the list.
                    // If the list doesn't contain the element, it remains unchanged.

                }
            }
        }

        else if (action.equals("clear")) {
            synchronized (session) {
                if (previousItems != null) {
                    // Remove all occurrences of movie_id:
                    previousItems.removeIf(id -> id.equals(movie_id));

                }
            }
        }
        // Convert the previousItems list to a comma-separated string
        String previousItemsStr = String.join(", ", previousItems);

        // Log the IDs to the localhost log
        request.getServletContext().log("Previous Items IDs: " + previousItemsStr);
        JsonObject responseJsonObject = new JsonObject();

        JsonArray previousItemsJsonArray = new JsonArray();
        previousItems.forEach(previousItemsJsonArray::add);
        responseJsonObject.add("previousItems", previousItemsJsonArray);

        response.getWriter().write(responseJsonObject.toString());
        response.setStatus(200);
    }
}