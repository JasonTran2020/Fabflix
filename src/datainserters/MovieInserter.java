package datainserters;

import com.mysql.cj.exceptions.MysqlErrorNumbers;
import com.mysql.cj.jdbc.MysqlDataSource;
import datainserters.XMLparsers.DomParser;
import datainserters.XMLparsers.MovieDomParser;
import datamodels.dbitems.Genre;
import datamodels.dbitems.Movie;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import javax.xml.crypto.Data;
import java.sql.*;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

public class MovieInserter {
    public MovieDomParser movieDomParser= null;
    private DataSource dataSource;
    protected SortedSet<String> existingGenresSet;
    protected SortedSet<String> allGenresSet;
    protected Map<String,String> genreMappings;
    protected Map<String,String> movieXmlIdToDbId = new HashMap<>();

    protected static final double minSimilarityPercent = 0.6;
    private static String sqlInsertMovieClause = "INSERT INTO movies VALUES(?,?,?,?,10)";
    private static String sqlInsertGenreClause = "INSERT INTO genres VALUES(NULL,?)";
    private static String sqlGetAllGenres = "SELECT * FROM genres g";
    private static String sqlGetAllMovieIds = "SELECT id FROM movies";
    private static String sqlInsertGenreInMovieClause = "INSERT INTO genres_in_movies VALUES(?,?)";
    private static String sqlInsertDefaultRatingInMovieClause = "INSERT INTO ratings VALUES(?,?,?)";
    protected static final int maxBatchSize = 100;
    MovieInserter(){
        //As a standalone class not part of the web application, we can't use InitialContext (without prior set up)
        //Instead, manually pass in the parameters to connect to the db. Not preferable due to have duplicate locations holding their own user and password strings
        MysqlDataSource mysqlDataSource= new MysqlDataSource();
        mysqlDataSource.setURL("jdbc:mysql://localhost:3306/moviedb");
        mysqlDataSource.setUser("mytestuser");
        mysqlDataSource.setPassword("My6$Password");

        dataSource = mysqlDataSource;

    }

    public void executeDBUpdateFromXML(String filePath){
        //Inserts genres, then movies, then genres in movies in that order
        try (Connection connection = dataSource.getConnection()){
            movieDomParser = new MovieDomParser();
            movieDomParser.executeMoviesParsingFromXmlFile(filePath);
            Set<Movie> movies = movieDomParser.getMovies();
            Set<String> parsedGenres = movieDomParser.getParsedGenres();

            setupExistingGenresAndGroupGenresTogether(parsedGenres,connection);
            insertNewGenresIntoDb(connection);
            insertMoviesIntoDb(movies,getExistingGenresAndIdFromDb(connection),connection);

        } catch (SQLException e){
            System.out.println(e.toString());
        }
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
    protected void addSingleMovieToBatch(Movie movie, PreparedStatement insertStatement) throws SQLException {
        insertStatement.setString(1, movie.movieId);
        insertStatement.setString(2, movie.title);
        insertStatement.setInt(3,movie.year);
        insertStatement.setString(4,movie.director);

        insertStatement.addBatch();
    }

    protected void insertMoviesIntoDb(Set<Movie> movies,Map<String,Integer> genreDBIdMappings, Connection connection) throws SQLException {
        //Doesn't check against duplicate movies currently in DB
        //Also adds entries to genres in movies
        int currentBatchSize =0;
        PreparedStatement statement = connection.prepareStatement(sqlInsertMovieClause);
        PreparedStatement genreStatement = connection.prepareStatement(sqlInsertGenreInMovieClause);
        PreparedStatement ratingStatement = connection.prepareStatement(sqlInsertDefaultRatingInMovieClause);
        Set<String> existingMovieIds = getExistingMovieIdFromDb(connection);
        connection.setAutoCommit(false);
        int count = 1;
        for (Movie movie :movies){
            int offset = 0;
            while(true){
                //Try catch to handle duplicate primary keys
                try{
                    while(true){
                        movie.movieId = movie.generateDBIdFromHashCode(offset);
                        if (existingMovieIds.contains(movie.movieId)){
                            //System.out.println("Duplicate key of "+movie.movieId+". Attempting to make new primary key");
                            offset+=1;
                            continue;
                        }
                        existingMovieIds.add(movie.movieId);
                        break;
                    }
                    //System.out.println(count+". Adding movie to batch: " + movie);
                    //insertSingleMovieIntoDB(movie,statement);
                    addSingleMovieToBatch(movie,statement);
                    currentBatchSize+=1;
                    addMovieToIdMapping(movie);
                    //insertGenresInMovieIntoDb(connection,movie,genreDBIdMappings);
                    //insertRatingIntoDb(connection,movie);
                    addGenresInMovieToBatch(genreStatement,movie,genreDBIdMappings);
                    adddRatingToBatch(ratingStatement,movie);
                    count+=1;
                    break;
                }
                catch (SQLException e){
                    if (e.getErrorCode()==MysqlErrorNumbers.ER_DUP_ENTRY){
                        System.out.println("Duplicate key of "+movie.movieId+". Attempting to make new primary key");
                        offset+=1;
                    }
                    else{
                        System.out.println(e);
                        throw e;
                    }
                }
            }
            if (currentBatchSize>maxBatchSize){
                System.out.println("Executing small movie batch");
                statement.executeBatch();
                genreStatement.executeBatch();
                ratingStatement.executeBatch();

                statement.clearBatch();
                genreStatement.clearBatch();
                ratingStatement.clearBatch();
                currentBatchSize=0;
            }

        }
        System.out.println("Executing movie batch");
        statement.executeBatch();
        genreStatement.executeBatch();
        ratingStatement.executeBatch();

        connection.commit();

        statement.close();
        genreStatement.close();
        ratingStatement.close();
    }
    protected void insertSingleGenreIntoDb(String genreName, PreparedStatement insertStatement) throws SQLException {
        insertStatement.setString(1,genreName);
        insertStatement.executeUpdate();
    }
    protected void addMovieToIdMapping(Movie movie){
        //No id? not adding to mapping
        if (movie.xmlId == null || movie.xmlId.isEmpty()){
            return;
        }
        movieXmlIdToDbId.put(movie.xmlId, movie.movieId);
    }

    protected void insertNewGenresIntoDb(Connection connection) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(sqlInsertGenreClause);
        for (Map.Entry<String, String> entry : genreMappings.entrySet()) {
            //Only insert genre if it isn't in the DB
            if (!existingGenresSet.contains(entry.getValue())){
                insertSingleGenreIntoDb(entry.getValue(),statement);
                //After inserting, also add it to the existingGenresSet so we don't add it again
                existingGenresSet.add(entry.getValue());
            }
        }
    }
    protected void insertGenresInMovieIntoDb(Connection connection, Movie movie, Map<String, Integer> genreDBIdMappings ) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(sqlInsertGenreInMovieClause);
        for (String genreName: movie.genres){
            //Needed due to grouping certain genres together
            String actualGenreName = genreMappings.get(genreName);
            int genreDbId = genreDBIdMappings.get(actualGenreName);
            statement.setInt(1,genreDbId);
            statement.setString(2,movie.movieId);
            try{
                statement.executeUpdate();
            }
            catch (SQLException e){
                //Some movies in the xml have duplicate genres. Handle it here, do not let it populate up
                if (e.getErrorCode() == MysqlErrorNumbers.ER_DUP_ENTRY){
                    System.out.println("Duplicate genre of "+genreName+" for the movie "+movie.movieId+". No point in have duplicate genre in movies. Ignoring and continuing");
                }
                else{
                    System.out.println("Ran into this SQL exception when adding "+genreName+" to the movie "+movie.movieId+": "+e+".\n Ignoring and moving on");
                }
            }

        }
        statement.close();
    }
    protected void addGenresInMovieToBatch(PreparedStatement statement, Movie movie, Map<String, Integer> genreDBIdMappings ) throws SQLException {
        Set<String> uniqueGenres = new HashSet<>(movie.genres);
        for (String genreName: uniqueGenres){
            //Needed due to grouping certain genres together
            String actualGenreName = genreMappings.get(genreName);
            int genreDbId = genreDBIdMappings.get(actualGenreName);
            statement.setInt(1,genreDbId);
            statement.setString(2,movie.movieId);
            try{
                statement.addBatch();
            }
            catch (SQLException e){
                //Some movies in the xml have duplicate genres. Handle it here, do not let it populate up
                if (e.getErrorCode() == MysqlErrorNumbers.ER_DUP_ENTRY){
                    System.out.println("Duplicate genre of "+genreName+" for the movie "+movie.movieId+". No point in have duplicate genre in movies. Ignoring and continuing");
                }
                else{
                    System.out.println("Ran into this SQL exception when adding "+genreName+" to the movie "+movie.movieId+": "+e+".\n Ignoring and moving on");
                }
            }
        }
    }
    protected void insertRatingIntoDb(Connection connection, Movie movie) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(sqlInsertDefaultRatingInMovieClause);
        statement.setString(1,movie.movieId);
        statement.setFloat(2,-1);
        statement.setInt(3,0);
        try{
            statement.executeUpdate();
        }
        catch (SQLException e){
            if (e.getErrorCode() == MysqlErrorNumbers.ER_DUP_ENTRY){
                System.out.println("Duplicate rating. Ignoring current insertion");
            }
            else{
                System.out.println(e);
            }
        }
        statement.close();
    }
    protected void adddRatingToBatch(PreparedStatement statement, Movie movie) throws SQLException {

        statement.setString(1,movie.movieId);
        statement.setFloat(2,0);
        statement.setInt(3,0);
        try{
            statement.addBatch();
        }
        catch (SQLException e){
            if (e.getErrorCode() == MysqlErrorNumbers.ER_DUP_ENTRY){
                System.out.println("Duplicate rating. Ignoring current insertion");
            }
            else{
                System.out.println(e);
            }
        }

    }
    protected void testGenreGrouping(){
        try {
            Connection connection = dataSource.getConnection();
            MovieDomParser movieDomParser = new MovieDomParser();
            movieDomParser.executeMoviesParsingFromXmlFile("F:\\CS122BProjectLogs\\xml crap\\stanford-movies\\mains243.xml");
            setupExistingGenresAndGroupGenresTogether(movieDomParser.getParsedGenres(),connection);
            for (Map.Entry<String, String> entry : genreMappings.entrySet()) {
                if (!entry.getKey().equals(entry.getValue()))
                    System.out.println(entry.getKey() + ":" + entry.getValue());
            }
        } catch (SQLException e){
            throw new RuntimeException(e);
        }
    }
    protected Set<String> getExistingMovieIdFromDb(Connection conn) throws SQLException{
        Statement statement = conn.createStatement();
        ResultSet rs =statement.executeQuery(sqlGetAllMovieIds);
        Set<String> result = new HashSet<>();
        while (rs.next()){
            result.add(rs.getString(1));
        }
        return result;
    }
    protected SortedSet<String> getExistingGenresFromDb(Connection conn) throws SQLException {
        Statement statement = conn.createStatement();
        ResultSet rs = statement.executeQuery(sqlGetAllGenres);
        //Decision based on this https://stackoverflow.com/questions/18564744/fastest-way-to-find-strings-in-string-collection-that-begin-with-certain-chars
        SortedSet<String> result = new TreeSet<>();
        while (rs.next()){
            result.add(rs.getString("name"));
        }
        statement.close();
        rs.close();
        return result;
    }

    protected Map<String,Integer> getExistingGenresAndIdFromDb(Connection conn) throws SQLException{
        Statement statement = conn.createStatement();
        ResultSet rs = statement.executeQuery(sqlGetAllGenres);
        //Decision based on this https://stackoverflow.com/questions/18564744/fastest-way-to-find-strings-in-string-collection-that-begin-with-certain-chars
        Map<String,Integer> result = new HashMap<>();
        while (rs.next()){
            result.put(rs.getString("name"),rs.getInt("id"));
        }
        statement.close();
        rs.close();
        return result;
    }

    protected void setupExistingGenresAndGroupGenresTogether(Set<String> parsedGenres, Connection conn) throws SQLException {
        existingGenresSet = getExistingGenresFromDb(conn);
        allGenresSet = new TreeSet<>();
        allGenresSet.addAll(existingGenresSet);
        genreMappings = new HashMap<>();
        //Existing genres map to themselves, not to new genres added from a data source
        existingGenresSet.forEach((genre) -> genreMappings.put(genre,genre));
        parsedGenres.forEach((genre) -> groupGenreOrAddNew(allGenresSet,genreMappings,genre));
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
        //Experimental. Should we compare against genre names that we just added?
        //genreSet.add(newGenreName);
        //Case1: Some existing genre is similar enough, we will map newGenreName to this existing genre
        if (maxSimilarity >= minSimilarityPercent )
            genreMappings.put(newGenreName,currentSimilarGenre);

        //Case2: The existing genre is pretty different compared to anything else we currently have. Add a mapping to itself
        //Need to decide if we should add to the set as well
        else
            genreMappings.put(newGenreName,newGenreName);

    }

    public Map<String, String> getMovieXmlIdToDbId() {
        return movieXmlIdToDbId;
    }

    public static void main(String[] args) {
        MovieInserter domParser = new MovieInserter();
        //domParser.testConnection();
        //domParser.testGenreGrouping();
        Instant start = Instant.now();
        //domParser.executeDBUpdateFromXML("F:\\CS122BProjectLogs\\xml crap\\stanford-movies\\mains243.xml");
        Instant finish = Instant.now();
        long timeElapsed = Duration.between(start, finish).toMillis();
        System.out.println("Seconds it took to parse and insert the movies into the DB (auto-commit off): " +timeElapsed+" milliseconds");

    }
}
