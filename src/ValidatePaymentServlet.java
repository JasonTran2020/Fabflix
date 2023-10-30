import com.google.gson.JsonObject;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.BufferedReader;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

@WebServlet(name = "ValidatePaymentServlet", urlPatterns = "/api/payment")
public class ValidatePaymentServlet extends HttpServlet {
    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */

    public static final String TAG = "ValidatePaymentServlet";
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedbexample");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // Setting the response content type to JSON
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        // Extracting the payment data from the request
        StringBuilder stringBuilder = new StringBuilder();
        String line;
        try (BufferedReader reader = request.getReader()) {
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
            }
        }

        String jsonData = stringBuilder.toString();
        JsonObject jsonObject = JsonParser.parseString(jsonData).getAsJsonObject();

        String ccId = jsonObject.get("ccId").getAsString();
        String firstName = jsonObject.get("firstName").getAsString();
        String lastName = jsonObject.get("lastName").getAsString();
        String expiryDate = jsonObject.get("expiryDate").getAsString();

        if (validatePaymentDetails(request, ccId, firstName, lastName, expiryDate)) {
            //recordTransaction(ccId, firstName, lastName); // Record the transaction
            response.setStatus(HttpServletResponse.SC_OK);  // 200 OK
            out.write("{\"status\":\"success\",\"message\":\"Payment validated successfully.\"}");
        } else {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);  // 500 Internal Server Error
            out.write("{\"status\":\"error\",\"message\":\"Invalid payment details.\"}");
        }

        out.close();
    }

    private boolean validatePaymentDetails(HttpServletRequest request, String ccId, String firstName, String lastName, String expiryDate) {
        boolean to_return = false;
        HttpSession session = request.getSession(false); // get existing session, don't create a new one
        if (session != null) {
            User retrievedUser = (User) session.getAttribute("user");
            if (retrievedUser != null) {
                // Print retrieved attributes and the arguments for comparison
                System.out.println("Retrieved User CC ID: " + retrievedUser.getCcId() + ", Provided CC ID: " + ccId);
                System.out.println("Retrieved User First Name: " + retrievedUser.getFirstname() + ", Provided First Name: " + firstName);
                System.out.println("Retrieved User Last Name: " + retrievedUser.getLastname() + ", Provided Last Name: " + lastName);
                System.out.println("Retrieved User Expiry Date: " + retrievedUser.getexpiryDate() + ", Provided Expiry Date: " + expiryDate);

                // Print if each attribute is matching
                System.out.println("are credit cards matching?: " + ccId.equals(retrievedUser.getCcId()));
                System.out.println("are first names matching?: " + firstName.equals(retrievedUser.getFirstname()));
                System.out.println("are last names matching?: " + lastName.equals(retrievedUser.getLastname()));
                System.out.println("are expiry dates matching?: " + expiryDate.equals(retrievedUser.getexpiryDate()));

                // Check if all attributes match
                to_return = ccId.equals(retrievedUser.getCcId()) &&
                        firstName.equals(retrievedUser.getFirstname()) &&
                        lastName.equals(retrievedUser.getLastname()) &&
                        expiryDate.equals(retrievedUser.getexpiryDate());
            }
        }
        System.out.println("everything is equal?: " + to_return);
        return to_return;
    }
}