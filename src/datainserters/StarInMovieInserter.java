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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class StarInMovieInserter {
    public CastDomParser castDomParser = null;
    private DataSource dataSource;
    public static String sqlInsertStarInMovieClause = "INSERT INTO stars_in_movies VALUES(?,?)";
    private Map<String,String> moviesXmlIdToDbId = new HashMap<>();
    private Map<String,String> starsXmlIdToDbId = new HashMap<>();
    public int countActorsAddedInCast = 0;
    public int countNoMovieFid = 0;
    private Set<Star> newStarsFromCast = new HashSet<>();
    protected static final int maxBatchSize = 100;
    StarInMovieInserter(){
        //As a standalone class not part of the web application, we can't use InitialContext (without prior set up)
        //Instead, manually pass in the parameters to connect to the db. Not preferable due to have duplicate locations holding their own user and password strings
        MysqlDataSource mysqlDataSource= new MysqlDataSource();
        mysqlDataSource.setURL("jdbc:mysql://localhost:3306/moviedb");
        mysqlDataSource.setUser("mytestuser");
        mysqlDataSource.setPassword("My6$Password");

        dataSource = mysqlDataSource;
    }

    public void executeDBUpdateFromXMLAndMappings(String filePath, Map<String,String> moviesMapping, Map<String,String> starsMapping, Set<String> existingStarIds){
        moviesXmlIdToDbId = moviesMapping;
        starsXmlIdToDbId = starsMapping;
        try (Connection connection = dataSource.getConnection()){
            castDomParser = new CastDomParser();
            castDomParser.executeStarsParsingFromXmlFile(filePath);

            Set<StarInMovie> starsInMovies = castDomParser.getStarsInMovies();
            addNonExistantStar(starsInMovies,existingStarIds,connection);
            insertStarsInMoviesIntoDb(starsInMovies,connection);

        } catch (SQLException e){
            System.out.println(e.toString());
        }
    }

    public void insertStarsInMoviesIntoDb(Set<StarInMovie> starsInMovies,Connection connection) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(sqlInsertStarInMovieClause);
        connection.setAutoCommit(false);
        int currentBatchSize =0;
        int count =1;
        for (StarInMovie starInMovie: starsInMovies){
            try{
                //insertSingleStarInMovieIntoDb(starInMovie,statement);
                addSingleStarInMovieToBatch(starInMovie,statement);
                currentBatchSize +=1;
                //System.out.println(count+". Adding star in movie to batch: " + starInMovie);
                //System.out.println(count+". Inserting star in movie into DB: " + starInMovie);
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
            if (currentBatchSize>maxBatchSize){
                System.out.println("Executing small batch to add Star in Movie entries");
                statement.executeBatch();
                statement.clearBatch();
                currentBatchSize=0;
            }
        }

        System.out.println("Executing batch to add Star in Movie entries into DB");
        statement.executeBatch();
        connection.commit();
        statement.close();

    }

    protected void insertSingleStarInMovieIntoDb(StarInMovie starInMovie, PreparedStatement preparedStatement) throws SQLException {
        if (!moviesXmlIdToDbId.containsKey(starInMovie.xmlMovieId)) throw new MissingPrimaryKeyException("Cannot map movie xml id to MySql id of "+starInMovie+". Skipping entry");
        if (!starsXmlIdToDbId.containsKey(starInMovie.xmlStarId)) throw new MissingPrimaryKeyException("Cannot map star xml id to MySql id of " + starInMovie+". Skipping entry");
        preparedStatement.setString(1,starsXmlIdToDbId.get(starInMovie.xmlStarId));
        preparedStatement.setString(2,moviesXmlIdToDbId.get(starInMovie.xmlMovieId));
        preparedStatement.executeUpdate();
    }

    protected void addSingleStarInMovieToBatch(StarInMovie starInMovie, PreparedStatement preparedStatement) throws SQLException {
        if (!moviesXmlIdToDbId.containsKey(starInMovie.xmlMovieId)) {
            countNoMovieFid +=1;
            throw new MissingPrimaryKeyException("Cannot map movie xml id to MySql id of "+starInMovie+". Skipping entry");
        }
        if (!starsXmlIdToDbId.containsKey(starInMovie.xmlStarId)){
            if (starInMovie.xmlStarId == null){
                throw new MissingPrimaryKeyException("Cannot map null star xml id to MySql id of " + starInMovie+". Skipping entry");
            }
            //System.out.println("Cannot map star xml id (stagename) to MySql id of " + starInMovie+". Saving star stagename to be inserted later");
        }
        preparedStatement.setString(1,starsXmlIdToDbId.get(starInMovie.xmlStarId));
        preparedStatement.setString(2,moviesXmlIdToDbId.get(starInMovie.xmlMovieId));
        preparedStatement.addBatch();
    }
    protected void addNonExistantStar(Set<StarInMovie> starsInMovies,Set<String> existingStarIds,Connection connection) throws SQLException {
        Set<Star> starsOnlyInCast = new HashSet<>();
        for (StarInMovie starInMovie: starsInMovies) {
            if (starInMovie.xmlStarId == null){
                System.out.println("Cannot map null star xml id to MySql id of " + starInMovie+". Skipping entry");
            }
            else if(!starsXmlIdToDbId.containsKey(starInMovie.xmlStarId)){
                //System.out.println("Cannot map star xml id (stagename) to MySql id of " + starInMovie+". Saving star stagename to be inserted later");
                Star temp = new Star("",starInMovie.xmlStarId,-1);
                temp.birthYear=null;
                countActorsAddedInCast+=1;
                starsOnlyInCast.add(temp);
            }
        }
        StarInserter starInserter = new StarInserter();
        starInserter.setExistingStarIds(existingStarIds);
        starInserter.insertStarsIntoDb(starsOnlyInCast,connection);
        starsXmlIdToDbId.putAll(starInserter.getStarXmlIdToDbId());

    }

    public static class MissingPrimaryKeyException extends RuntimeException{
        public MissingPrimaryKeyException(String message){
            super(message);
        }
    }


}
