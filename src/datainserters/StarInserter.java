package datainserters;

import com.mysql.cj.MysqlType;
import com.mysql.cj.exceptions.MysqlErrorNumbers;
import com.mysql.cj.jdbc.MysqlDataSource;
import datainserters.XMLparsers.StarsDomParser;
import datamodels.dbitems.Star;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class StarInserter {

    private static String sqlInsertStarClause = "INSERT INTO stars VALUES(?,?,?)";
    protected Map<String,String> starXmlIdToDbId = new HashMap<>();
    private DataSource dataSource;
    StarInserter(){
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
            StarsDomParser starDomParser = new StarsDomParser();
            starDomParser.executeStarsParsingFromXmlFile(filePath);

            Set<Star> stars = starDomParser.getStars();
            insertStarsIntoDb(stars,connection);

        } catch (SQLException e){
            System.out.println(e.toString());
        }
    }

    protected void insertStarsIntoDb(Set<Star> stars, Connection connection) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(sqlInsertStarClause);
        int count = 1;
        for (Star star: stars){
            int offset = 0;
            while(true){
                try{
                    star.starId = star.generateDBIdFromHashCode(offset);
                    insertSingleStarIntoDb(star,statement);
                    addStarToIdMapping(star);
                    System.out.println(count+". Inserting star into DB: " + star);
                    count+=1;
                    break;
                }
                catch (SQLException e){
                    if (e.getErrorCode()== MysqlErrorNumbers.ER_DUP_ENTRY){
                        System.out.println("Duplicate key of Star "+ star.starId+". Attempting to make new primary key");
                        offset+=1;
                    }
                    else{
                        System.out.println(e);
                        throw e;
                    }
                }
            }
        }
        statement.close();
    }

    protected void addStarToIdMapping(Star star){
        if (star.name == null || star.name.isEmpty()){
            return;
        }
        starXmlIdToDbId.put(star.name,star.starId);
    }

    protected void insertSingleStarIntoDb(Star star, PreparedStatement statement) throws SQLException {
        statement.setString(1, star.starId);
        statement.setString(2,star.name);
        if (star.birthYear==null){
            statement.setNull(3, MysqlType.FIELD_TYPE_INT24);
        }
        else{
            statement.setInt(3,star.birthYear);
        }


        statement.executeUpdate();
    }

    public Map<String, String> getStarXmlIdToDbId() {
        return starXmlIdToDbId;
    }

    public static void main(String[] args){
        StarInserter starInserter = new StarInserter();
        starInserter.executeDBUpdateFromXML("F:\\CS122BProjectLogs\\xml crap\\stanford-movies\\actors63.xml");
    }
}
