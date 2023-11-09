package datainserters;

import com.mysql.cj.jdbc.MysqlDataSource;
import datainserters.XMLparsers.DomParser;
import datainserters.XMLparsers.MovieDomParser;
import datamodels.dbitems.Movie;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import javax.xml.crypto.Data;
import java.sql.*;
import java.util.*;

public class MovieInserter {
    private DataSource dataSource;
    protected SortedSet<String> existingGenresSet;
    protected Map<String,String> genreMappings;
    protected static final double minSimilarityPercent = 0.6;
    private String sqlInsertMovieClause = "INSERT INTO movies VALUES(?,?,?,?)";
    private String sqlInsertGenreClause = "INSERT INTO genres(Col2) VALUES(?)";
    private String sqlGetAllGenres = "SELECT * FROM genres g";
    MovieInserter(){
        //As a standalone class not part of the web application, we can't use InitialContext (without prior set up)
        //Instead, manually pass in the parameters to connect to the db. Not preferable due to have duplicate locations holding their own user and password strings
        MysqlDataSource mysqlDataSource= new MysqlDataSource();
        mysqlDataSource.setURL("jdbc:mysql://localhost:3306/moviedb");
        mysqlDataSource.setUser("mytestuser");
        mysqlDataSource.setPassword("My6$Password");

        dataSource = mysqlDataSource;

    }

    public void testConnection(){
        try {
            Connection connection = dataSource.getConnection();
            String query = "SELECT COUNT(*) FROM movies";
            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet rs = statement.executeQuery();
            rs.next();
            System.out.println("Movie count is: "+rs.getInt(1));

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    protected void insertSingleMovieIntoDB(Movie movie, PreparedStatement insertStatement) throws SQLException {
        insertStatement.setString(1, movie.movieId);
        insertStatement.setString(2, movie.title);
        insertStatement.setInt(3,movie.year);
        insertStatement.setString(4,movie.director);

        insertStatement.executeUpdate();
    }
    protected void insertSingleGenreIntoDb(String genreName, PreparedStatement insertStatement) throws SQLException {
        insertStatement.setString(1,genreName);
        insertStatement.executeUpdate();
    }
    protected void testGenreGrouping(){
        try {
            Connection connection = dataSource.getConnection();
            MovieDomParser movieDomParser = new MovieDomParser();
            movieDomParser.getMoviesFromXmlFile("F:\\CS122BProjectLogs\\xml crap\\stanford-movies\\mains243.xml");
            setupExistingGenresAndGroupGenresTogether(movieDomParser.getParsedGenres(),connection);
            for (Map.Entry<String, String> entry : genreMappings.entrySet()) {
                if (!entry.getKey().equals(entry.getValue()))
                    System.out.println(entry.getKey() + ":" + entry.getValue());
            }
        } catch (SQLException e){
            throw new RuntimeException(e);
        }
    }
    protected SortedSet<String> getExistingGenresFromDb(Connection conn) throws SQLException {
        Statement statement = conn.createStatement();
        ResultSet rs = statement.executeQuery(sqlGetAllGenres);
        //Decision based on this https://stackoverflow.com/questions/18564744/fastest-way-to-find-strings-in-string-collection-that-begin-with-certain-chars
        SortedSet<String> result = new TreeSet<>();
        while (rs.next()){
            result.add(rs.getString("name"));
        }

        return result;
    }

    protected void setupExistingGenresAndGroupGenresTogether(Set<String> parsedGenres, Connection conn) throws SQLException {
        existingGenresSet = getExistingGenresFromDb(conn);
        genreMappings = new HashMap<>();
        //Existing genres map to themselves, not to new genres added from a data source
        existingGenresSet.forEach((genre) -> genreMappings.put(genre,genre));
        parsedGenres.forEach((genre) -> groupGenreOrAddNew(existingGenresSet,genreMappings,genre));
    }

    protected static void groupGenreOrAddNew(SortedSet<String> genreSet, Map<String,String> genreMappings, String newGenreName){
        if (genreSet.contains(newGenreName) || genreMappings.containsKey(newGenreName)){
            return;
        }
        char startChar = newGenreName.charAt(0);
        char nextChar = (char) (startChar+1);
        SortedSet<String> potentialGenresSet = genreSet.subSet(String.valueOf(startChar), String.valueOf(nextChar));
        double maxSimilarity = 0.0;
        String currentSimilarGenre = "";
        for (String potentialGenre: potentialGenresSet){
            double currentSimilarity = DomParser.similarity(potentialGenre,newGenreName);
            if (currentSimilarity > maxSimilarity){
                maxSimilarity = currentSimilarity;
                currentSimilarGenre = potentialGenre;
            }
        }
        //Case1: Some existing genre is similar enough, we will map newGenreName to this existing genre
        if (maxSimilarity >= minSimilarityPercent )
            genreMappings.put(newGenreName,currentSimilarGenre);

        //Case2: The existing genre is pretty different compared to anything else we currently have. Add a mapping to itself
        //Need to decide if we should add to the set as well
        else
            genreMappings.put(newGenreName,newGenreName);

    }
    public static void main(String[] args) {
        MovieInserter domParser = new MovieInserter();
        //domParser.testConnection();
        domParser.testGenreGrouping();
    }
}
