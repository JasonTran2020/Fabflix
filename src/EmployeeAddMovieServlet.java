
import com.google.gson.JsonObject;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jasypt.util.password.StrongPasswordEncryptor;

import java.sql.CallableStatement;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;

@WebServlet(name = "EmployeeAddMovieServlet", urlPatterns = "/_dashboard/api/addmovie")
public class EmployeeAddMovieServlet extends HttpServlet {

    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/MasterDB");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String movie_name = request.getParameter("movie_name");
        String movie_year = request.getParameter("movie_year");
        String movie_director = request.getParameter("movie_director");
        String star_name = request.getParameter("star_name");
        String birth_year = request.getParameter("birth_year");
        String movie_genre = request.getParameter("movie_genre");

        JsonObject responseJsonObject = new JsonObject();
        PrintWriter out = response.getWriter();
        try (Connection connection = dataSource.getConnection()) {
            String statusMessage = addMovieByStoredProcedure(movie_name, Integer.parseInt(movie_year), movie_director, star_name, birth_year, movie_genre);
            if (statusMessage.equals("New movie, star, and genre were successfully added and linked.")) {
                String newMovieId = getNewMovieId(movie_name, Integer.parseInt(movie_year), movie_director);
                int genreId = getCorrespondingGenreId(movie_genre);
                String starId = getCorrespondingStarId(star_name);
                responseJsonObject.addProperty("status", "success");
                responseJsonObject.addProperty("message", "Success! New movie with id " + newMovieId + " has been added." + " Genre id: " + genreId + ", star id:  " + starId);
            }
            else {
                responseJsonObject.addProperty("status", "fail");
                responseJsonObject.addProperty("message", "Movie already exists in database, no changes were made.");
            }

            out.write(responseJsonObject.toString());
        }


        catch (Exception e) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());

            response.setStatus(500);

        } finally {
            out.close();
        }

    }

    private String getNewMovieId(String movieName, int movieYear, String movieDirector) throws SQLException  {
        String movieId = null;
        Connection connection = dataSource.getConnection();
        PreparedStatement statement = connection.prepareStatement("SELECT id FROM movies WHERE title = ? AND year = ? AND director = ?");
        statement.setString(1, movieName);
        statement.setInt(2, movieYear);
        statement.setString(3, movieDirector);
        ResultSet resultSet = statement.executeQuery();
        if (resultSet.next()) {
            movieId = resultSet.getString("id");
        }

        statement.close();
        connection.close();
        resultSet.close();

        return movieId;
    }

    private int getCorrespondingGenreId(String genreName) throws SQLException  {
        int genreId = 0;
        Connection connection = dataSource.getConnection();
        PreparedStatement statement = connection.prepareStatement("SELECT id FROM genres WHERE name = ?");
        statement.setString(1, genreName);
        ResultSet resultSet = statement.executeQuery();
        if (resultSet.next()) {
            genreId = resultSet.getInt("id");
        }
        statement.close();
        connection.close();
        resultSet.close();

        return genreId;

    }

    private String getCorrespondingStarId(String starName) throws SQLException  {
        String starId = null;
        Connection connection = dataSource.getConnection();
        PreparedStatement statement = connection.prepareStatement("SELECT id FROM stars WHERE name = ?");
        statement.setString(1, starName);
        ResultSet resultSet = statement.executeQuery();
        if (resultSet.next()) {
            starId = resultSet.getString("id");
        }
        statement.close();
        connection.close();
        resultSet.close();
        return starId;

    }
    private String addMovieByStoredProcedure(String movieName, int movieYear, String movieDirector, String starName, String birthYear, String genreName)
            throws SQLException {
        Connection connection = dataSource.getConnection();
        String statusMessage;
        try {
            CallableStatement statement = connection.prepareCall("{CALL add_movie(?, ?, ?, ?, ?, ?, ?)}");

            statement.setString(1, movieName);
            statement.setInt(2, movieYear);
            statement.setString(3, movieDirector);
            statement.setString(4, starName);
            if (birthYear == null || birthYear.trim().isEmpty()) {
                statement.setNull(5, java.sql.Types.INTEGER);
            } else {
                statement.setInt(5, Integer.parseInt(birthYear));
            }
            statement.setString(6, genreName);

            // Register the OUT parameter
            statement.registerOutParameter(7, java.sql.Types.VARCHAR);

            // Execute the stored procedure
            statement.execute();

            // Retrieve the status message from the OUT parameter
            statusMessage = statement.getString(7);

            // Close the statement
            statement.close();
        } finally {
            // Ensure the connection is closed
            connection.close();
        }

        // Return the status message
        return statusMessage;
    }
}
