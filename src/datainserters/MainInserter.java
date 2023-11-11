package datainserters;

import com.mysql.cj.jdbc.MysqlDataSource;

import javax.sql.DataSource;

//Role of this class is to connect and use all the other inserts (Star, Movie, StarInMovie)
public class MainInserter {
    private DataSource dataSource;
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
        MovieInserter movieInserter = new MovieInserter();
        StarInserter starInserter = new StarInserter();
        StarInMovieInserter starInMovieInserter = new StarInMovieInserter();

        movieInserter.executeDBUpdateFromXML(moviePath);
        starInserter.executeDBUpdateFromXML(starPath);
        starInMovieInserter.executeDBUpdateFromXMLAndMappings(castPath,movieInserter.getMovieXmlIdToDbId(),starInserter.getStarXmlIdToDbId());
    }

    public static void main(String[] args){
        MainInserter mainInserter = new MainInserter();
        mainInserter.executeDBUpdateFromXML("F:\\CS122BProjectLogs\\xml crap\\stanford-movies\\mains243.xml","F:\\CS122BProjectLogs\\xml crap\\stanford-movies\\actors63.xml",
                "F:\\CS122BProjectLogs\\xml crap\\stanford-movies\\casts124.xml");
    }
}
