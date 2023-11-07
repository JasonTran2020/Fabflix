package datainserters;

import com.mysql.cj.jdbc.MysqlDataSource;
import datainserters.XMLparsers.MovieDomParser;
import datamodels.dbitems.Movie;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import javax.xml.crypto.Data;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MovieInserter {
    private DataSource dataSource;
    private String sqlInsertMovieClause = "INSERT INTO movies VALUES(?,?,?,?)";
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

    protected void insertSingleMovieIntoDB(Movie movie, Connection conn){

    }

    public static void main(String[] args) {
        MovieInserter domParser = new MovieInserter();
        domParser.testConnection();
    }
}
