package datainserters;

import com.mysql.cj.exceptions.MysqlErrorNumbers;
import com.mysql.cj.jdbc.MysqlDataSource;
import datainserters.XMLparsers.CastDomParser;
import datainserters.XMLparsers.StarsDomParser;
import datamodels.dbitems.Star;
import datamodels.dbitems.StarInMovie;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class StarInMovieInserter {

    private DataSource dataSource;
    public static String sqlInsertStarInMovieClause = "INSERT INTO stars_in_movies VALUES(?,?)";
    private Map<String,String> moviesXmlIdToDbId = new HashMap<>();
    private Map<String,String> starsXmlIdToDbId = new HashMap<>();
    StarInMovieInserter(){
        //As a standalone class not part of the web application, we can't use InitialContext (without prior set up)
        //Instead, manually pass in the parameters to connect to the db. Not preferable due to have duplicate locations holding their own user and password strings
        MysqlDataSource mysqlDataSource= new MysqlDataSource();
        mysqlDataSource.setURL("jdbc:mysql://localhost:3306/moviedb");
        mysqlDataSource.setUser("mytestuser");
        mysqlDataSource.setPassword("My6$Password");

        dataSource = mysqlDataSource;
    }

    public void executeDBUpdateFromXMLAndMappings(String filePath, Map<String,String> moviesMapping, Map<String,String> starsMapping){
        moviesXmlIdToDbId = moviesMapping;
        starsXmlIdToDbId = starsMapping;
        try (Connection connection = dataSource.getConnection()){
            CastDomParser castDomParser = new CastDomParser();
            castDomParser.executeStarsParsingFromXmlFile(filePath);

            Set<StarInMovie> starsInMovies = castDomParser.getStarsInMovies();
            insertStarsInMoviesIntoDb(starsInMovies,connection);

        } catch (SQLException e){
            System.out.println(e.toString());
        }
    }

    public void insertStarsInMoviesIntoDb(Set<StarInMovie> starsInMovies,Connection connection) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(sqlInsertStarInMovieClause);
        int count =1;
        for (StarInMovie starInMovie: starsInMovies){
            try{
                insertSingleStarInMovieIntoDb(starInMovie,statement);
                System.out.println(count+". Inserting star in movie into DB: " + starInMovie);
                count+=1;
            }
            catch(SQLException|MissingPrimaryKeyException error){
                if (error.getClass() == SQLException.class){
                    SQLException e = (SQLException) error;
                    if (e.getErrorCode()== MysqlErrorNumbers.ER_DUP_ENTRY){
                        System.out.println("Duplicate entry of star in movie "+ starInMovie+". Skipping insertion into DB");
                    }
                    else{
                        throw e;
                    }
                }
                else if (error.getClass() == MissingPrimaryKeyException.class){
                    System.out.println(error);
                }
            }
        }
        statement.close();

    }

    public void insertSingleStarInMovieIntoDb(StarInMovie starInMovie, PreparedStatement preparedStatement) throws SQLException {
        if (!moviesXmlIdToDbId.containsKey(starInMovie.xmlMovieId)) throw new MissingPrimaryKeyException("Cannot map movie xml id to MySql id of "+starInMovie+". Skipping entry");
        if (!starsXmlIdToDbId.containsKey(starInMovie.xmlStarId)) throw new MissingPrimaryKeyException("Cannot map star xml id to MySql id of " + starInMovie+". Skipping entry");
        preparedStatement.setString(1,starsXmlIdToDbId.get(starInMovie.xmlStarId));
        preparedStatement.setString(2,moviesXmlIdToDbId.get(starInMovie.xmlMovieId));
        preparedStatement.executeUpdate();
    }

    public static class MissingPrimaryKeyException extends RuntimeException{
        public MissingPrimaryKeyException(String message){
            super(message);
        }
    }


}
