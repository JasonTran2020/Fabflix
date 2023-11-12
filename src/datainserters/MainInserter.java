package datainserters;

import com.mysql.cj.jdbc.MysqlDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

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
            //connection.setAutoCommit(false);

            MovieInserter movieInserter = new MovieInserter();
            StarInserter starInserter = new StarInserter();
            StarInMovieInserter starInMovieInserter = new StarInMovieInserter();

            movieInserter.executeDBUpdateFromXML(moviePath);
            starInserter.executeDBUpdateFromXML(starPath);
            //Combine with database and XML for stars. Cannot do for movies as it lacks XML ids, while a star's xml id is just their name
            Map<String, String> actorIdMappings = getActorIdMappingsFromExistingDb(connection);
            actorIdMappings.putAll(starInserter.getStarXmlIdToDbId());
            starInMovieInserter.executeDBUpdateFromXMLAndMappings(castPath,movieInserter.getMovieXmlIdToDbId(),actorIdMappings,starInserter.getExistingStarIds());
        }
        catch (SQLException e){
            System.out.println(e.toString());
        }

    }
    public void executeWithThreadsDBUpdateFromXML(String moviePath, String starPath, String castPath){
        try(Connection connection =dataSource.getConnection()){
            //connection.setAutoCommit(false);
            MovieInserter movieInserter = new MovieInserter();
            StarInserter starInserter = new StarInserter();
            StarInMovieInserter starInMovieInserter = new StarInMovieInserter();

            ExecutorService executor = Executors.newFixedThreadPool(2);

            //Lambda that just calls our executeDBUpdate function
            executor.execute(() -> movieInserter.executeDBUpdateFromXML(moviePath));
            executor.execute(() -> starInserter.executeDBUpdateFromXML(starPath));
            executor.shutdown();
            //10 minutes max. Hopefullt the AWS machine isn't THAT slow
            executor.awaitTermination(600, TimeUnit.SECONDS);

            //Combine with database and XML for stars. Cannot do for movies as it lacks XML ids, while a star's xml id is just their name
            Map<String, String> actorIdMappings = getActorIdMappingsFromExistingDb(connection);
            actorIdMappings.putAll(starInserter.getStarXmlIdToDbId());
            starInMovieInserter.executeDBUpdateFromXMLAndMappings(castPath,movieInserter.getMovieXmlIdToDbId(),actorIdMappings,starInserter.getExistingStarIds());
            printInconsistencyReport(movieInserter,starInserter,starInMovieInserter);
        }
        catch (SQLException e){
            System.out.println(e.toString());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }

    public void printInconsistencyReport(MovieInserter movieInserter, StarInserter starInserter, StarInMovieInserter starInMovieInserter){
        System.out.println("Duplicate movies: " +movieInserter.movieDomParser.countDuplicateMovies);
        System.out.println("No movie name: " +movieInserter.movieDomParser.countMovieNoName);
        System.out.println("No movie year: " +movieInserter.movieDomParser.countMovieNoYear);
        System.out.println("No movie director name: " + movieInserter.movieDomParser.countMovieNoDirector);
        System.out.println("No xml FID: " + movieInserter.movieDomParser.countNoFid);

        System.out.println("Duplicate actors: " + starInserter.starDomParser.countActorDuplicate);
        System.out.println("No actor name: " + starInserter.starDomParser.countActorNoName);
        System.out.println("No actor DOB: " + starInserter.starDomParser.countActorNoDOB);

        System.out.println("Actors in cast but not actors file: " + starInMovieInserter.countActorsAddedInCast);
        System.out.println("Duplicate cast: " + starInMovieInserter.castDomParser.countDuplicateCast);
        System.out.println("Movie FIDs that could not be mapped: " + starInMovieInserter.countNoMovieFid);
        System.out.println("No movie FID for cast: " + starInMovieInserter.castDomParser.countNoMovieName);
        System.out.println("No actor name for cast: " + starInMovieInserter.castDomParser.countNoStarName);
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
        Instant start = Instant.now();
//        mainInserter.executeDBUpdateFromXML("F:\\CS122BProjectLogs\\xml crap\\stanford-movies\\mains243.xml","F:\\CS122BProjectLogs\\xml crap\\stanford-movies\\actors63.xml",
//                "F:\\CS122BProjectLogs\\xml crap\\stanford-movies\\casts124.xml");
        if (args.length<3){
            mainInserter.executeWithThreadsDBUpdateFromXML("F:\\CS122BProjectLogs\\xml crap\\stanford-movies\\mains243.xml","F:\\CS122BProjectLogs\\xml crap\\stanford-movies\\actors63.xml",
                    "F:\\CS122BProjectLogs\\xml crap\\stanford-movies\\casts124.xml");
        }
        else{
            mainInserter.executeWithThreadsDBUpdateFromXML(args[0],args[1],args[2]);
        }

        Instant finish = Instant.now();
        long timeElapsed = Duration.between(start, finish).toMillis();
        System.out.println("Seconds it took to parse and insert everything into the DB: " +timeElapsed+" milliseconds");
    }
}
