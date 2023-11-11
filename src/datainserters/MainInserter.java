package datainserters;

import com.mysql.cj.jdbc.MysqlDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

//Role of this class is to connect and use all the other inserts (Star, Movie, StarInMovie)
public class MainInserter {
    private DataSource dataSource;
    private String sqlGetAllStars = "SELECT * FROM stars";
    MainInserter(){
        //As a standalone class not part of the web application, we can't use InitialContext (without prior set up)
        //Instead, manually pass in the parameters to connect to the db. Not preferable due to have duplicate locations holding their own user and password strings
        MysqlDataSource mysqlDataSource= new MysqlDataSource();
        mysqlDataSource.setURL("jdbc:mysql://localhost:3306/moviedb");
        mysqlDataSource.setUser("mytestuser");
        mysqlDataSource.setPassword("My6$Password");

        dataSource = mysqlDataSource;

    }

    public void executeDBUpdateFromXML(String moviePath, String starPath, String castPath){
        try(Connection connection =dataSource.getConnection()){
            MovieInserter movieInserter = new MovieInserter();
            StarInserter starInserter = new StarInserter();
            StarInMovieInserter starInMovieInserter = new StarInMovieInserter();

            movieInserter.executeDBUpdateFromXML(moviePath);
            starInserter.executeDBUpdateFromXML(starPath);
            //Combine with database and XML for stars. Cannot do for movies as it lacks XML ids, while a star's xml id is just their name
            Map<String, String> actorIdMappings = getActorIdMappingsFromExistingDb(connection);
            actorIdMappings.putAll(starInserter.getStarXmlIdToDbId());
            starInMovieInserter.executeDBUpdateFromXMLAndMappings(castPath,movieInserter.getMovieXmlIdToDbId(),actorIdMappings);
        }
        catch (SQLException e){
            System.out.println(e.toString());
        }

    }

    public Map<String, String> getActorIdMappingsFromExistingDb(Connection connection) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(sqlGetAllStars);
        ResultSet rs = statement.executeQuery();
        Map<String, String> result = new HashMap<>();
        while(rs.next()){
            result.put(rs.getString("name"), rs.getString("id"));
        }
        statement.close();
        rs.close();
        return result;
    }

    public static void main(String[] args){
        MainInserter mainInserter = new MainInserter();
        mainInserter.executeDBUpdateFromXML("F:\\CS122BProjectLogs\\xml crap\\stanford-movies\\mains243.xml","F:\\CS122BProjectLogs\\xml crap\\stanford-movies\\actors63.xml",
                "F:\\CS122BProjectLogs\\xml crap\\stanford-movies\\casts124.xml");
    }
}
