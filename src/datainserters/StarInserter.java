package datainserters;

import com.mysql.cj.MysqlType;
import com.mysql.cj.exceptions.MysqlErrorNumbers;
import com.mysql.cj.jdbc.MysqlDataSource;
import datainserters.XMLparsers.StarsDomParser;
import datamodels.dbitems.Star;

import javax.sql.DataSource;
import javax.swing.plaf.nimbus.State;
import java.sql.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class StarInserter {
    StarsDomParser starDomParser = null;
    private static String sqlInsertStarClause = "INSERT INTO stars VALUES(?,?,?)";
    private static String sqlSelectStarClause = "SELECT * FROM stars";
    private Set<Star> existingStars = new HashSet<>();
    protected Set<String> existingStarIds = new HashSet<>();
    protected Map<String,String> starXmlIdToDbId = new HashMap<>();
    protected static final int maxBatchSize = 100;
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
            starDomParser = new StarsDomParser();
            starDomParser.executeStarsParsingFromXmlFile(filePath);
            existingStarIds = getExistingStarsIdFromDb(connection);
            Set<Star> stars = starDomParser.getStars();
            insertStarsIntoDb(stars,connection);

        } catch (SQLException e){
            System.out.println(e.toString());
        }
    }

    public void insertStarsIntoDb(Set<Star> stars, Connection connection) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(sqlInsertStarClause);
        int currentBatchSize =0;
        connection.setAutoCommit(false);
        int count = 1;
        for (Star star: stars){
            int offset = 0;
            while(true){
                try{
                    while(true){
                        star.starId = star.generateDBIdFromHashCode(offset);
                        if (existingStarIds.contains(star.starId)){
                            //System.out.println("Duplicate key of "+star.starId+". Attempting to make new primary key");
                            offset+=1;
                            continue;
                        }
                        existingStarIds.add(star.starId);
                        break;
                    }

                    addSingleStarToBatch(star,statement);
                    currentBatchSize+=1;
                    //insertSingleStarIntoDb(star,statement);
                    addStarToIdMapping(star);
                    //System.out.println(count+". Inserting star into DB: " + star);
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
            if (currentBatchSize>maxBatchSize){
                statement.executeBatch();
                statement.clearBatch();
                //System.out.println("Executing small star batch");
                currentBatchSize=0;
            }
        }
        //System.out.println("Executing star batch");
        statement.executeBatch();
        statement.close();
        connection.commit();
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

    protected void addSingleStarToBatch(Star star, PreparedStatement statement) throws SQLException {
        statement.setString(1, star.starId);
        statement.setString(2,star.name);
        if (star.birthYear==null){
            statement.setNull(3, MysqlType.FIELD_TYPE_INT24);
        }
        else{
            statement.setInt(3,star.birthYear);
        }
        statement.addBatch();
    }

    private Set<String> getExistingStarsIdFromDb(Connection connection) throws SQLException {
        Statement statement =connection.createStatement();
        ResultSet rs = statement.executeQuery(sqlSelectStarClause);
        Set<String> result = new HashSet<>();
        while (rs.next()){
            result.add(rs.getString(1));
            existingStars.add(new Star(rs.getString(1),rs.getString(2),rs.getInt(3)));
        }
        rs.close();
        statement.close();
        return result;
    }

    public Set<String> getExistingStarIds() {
        return existingStarIds;
    }

    public void setExistingStarIds(Set<String> existingStarIds) {
        this.existingStarIds = existingStarIds;
    }

    public Map<String, String> getStarXmlIdToDbId() {
        return starXmlIdToDbId;
    }

    public static void main(String[] args){
        StarInserter starInserter = new StarInserter();
//        starInserter.executeDBUpdateFromXML("F:\\CS122BProjectLogs\\xml crap\\stanford-movies\\actors63.xml");
    }
}
