
import com.google.gson.JsonObject;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jasypt.util.password.StrongPasswordEncryptor;
import com.google.gson.Gson;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet(name = "EmployeeMetadataServlet", urlPatterns = "/_dashboard/api/metadata")
public class EmployeeMetadataServlet extends HttpServlet{
    private DataSource dataSource;
    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedbexample");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        List<Map<String, Object>> tableDataList = new ArrayList<>();

        ArrayList<String> tables = getTableNames();
        JsonObject responseJsonObject = new JsonObject();
        for (String table : tables) {
            Map<String, Object> tableData = new HashMap<>();
            Map<String, String> attributes = getAttribs(table);
            tableData.put("tableName", table);
            tableData.put("attributes", attributes);
            tableDataList.add(tableData);

        }

        Gson gson = new Gson();
        String jsonString = gson.toJson(tableDataList);
        System.out.println("JSON String: " + jsonString);

        PrintWriter out = response.getWriter();
        out.write(jsonString);
        out.close();
    }

    private ArrayList<String> getTableNames() {
        ArrayList<String> arrayListToReturn = new ArrayList<String>();
        try {
            Connection connection = dataSource.getConnection();

            PreparedStatement statement = connection.prepareStatement("SHOW TABLES");

            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                arrayListToReturn.add(resultSet.getString(1));

            }

            resultSet.close();
            statement.close();
            connection.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return arrayListToReturn;
    }

    private HashMap<String, String> getAttribs(String table) {
        HashMap<String, String> maptoReturn = new HashMap<String, String>();
        try {
            Connection connection = dataSource.getConnection();

            PreparedStatement statement = connection.prepareStatement("DESCRIBE " + table);

            ResultSet resultSet = statement.executeQuery();



            while (resultSet.next()) {
                maptoReturn.put(resultSet.getString("field"), resultSet.getString("type"));

            }

            resultSet.close();
            statement.close();
            connection.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return maptoReturn;

    }
     public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }
}
