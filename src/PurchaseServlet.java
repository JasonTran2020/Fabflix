import com.google.gson.JsonObject;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

@WebServlet(name = "PurchaseServlet", urlPatterns = "/api/purchase")
public class PurchaseServlet extends HttpServlet{


    public static final String TAG = "PurchaseServlet";
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/MasterDB");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // get customer id from session
        // movie id from parameter
        // i need to return sale id to js
        System.out.println("in doPost");
        PrintWriter out = response.getWriter();
        String movieId = request.getParameter("id");
        int quantity = Integer.parseInt(request.getParameter("quantity"));
        HttpSession session = request.getSession(false);
        User retrievedUser = (User) session.getAttribute("user");
        int retrievedCustomerId = retrievedUser.getUserId();
        String customerIdAsString = String.valueOf(retrievedCustomerId);
        try (Connection conn = dataSource.getConnection()) {

            String get_max_id_query = "SELECT MAX(id) FROM sales";

            PreparedStatement statement = conn.prepareStatement(get_max_id_query);

//            statement.setString(1, customerIdAsString);
//            statement.setString(2, movieId);
            ResultSet resultSet = statement.executeQuery();
            int new_sale_number = 0;
            if (resultSet.next()) {
                new_sale_number = resultSet.getInt(1);

            }
            System.out.println(new_sale_number);

            String insert_into_sales_query = "INSERT INTO sales(id, customerId, movieId, saleDate, quantity)\n" +
                    "VALUES (?, ? , ? , CURRENT_DATE(), ?)\n";
            PreparedStatement secondStatement = conn.prepareStatement(insert_into_sales_query);
            secondStatement.setString(1, String.valueOf(new_sale_number + 1));
            secondStatement.setString(2, customerIdAsString);
            secondStatement.setString(3, movieId);
            secondStatement.setInt(4, quantity);
            secondStatement.executeUpdate(); // don't need resultset after inserting into table
            //Close the prepared statements
            // Send the new sale number as a JSON response
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("saleId", new_sale_number + 1);
            out.write(jsonObject.toString());

            secondStatement.close();
            statement.close();
            resultSet.close();

        } catch (Exception e) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());

            response.setStatus(500);

        } finally {
            out.close();
        }
    }
}
