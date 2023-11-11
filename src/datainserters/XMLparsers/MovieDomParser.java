package datainserters.XMLparsers;

import datamodels.dbitems.Genre;
import datamodels.dbitems.Movie;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MovieDomParser extends DomParser {

    Set<Movie> movies = new HashSet<>();
    Set<String> genreNames = new HashSet<>();
    private void parseAllMovies(){
        // get the document root Element
        Element documentElement = dom.getDocumentElement();

        NodeList filmNodeList = documentElement.getElementsByTagName("film");
        //Because I don't want to go through the whole xml file yet
        int currentMax = 100;
        for (int i = 0; i < filmNodeList.getLength() ; i++) {
            Element element = (Element) filmNodeList.item(i);
            Movie movie = parseMovie(element);
            try{
                verifyMovie(movie,element,i);
                //System.out.println(i+1+": " + movie);
                genreNames.addAll(movie.genres);
                movies.add(movie);
            }
            catch (MovieParseError e){

            }

        }
    }

    private Movie parseMovie(Element element) {
        //Assumed element is a "film" element. Not "movies" or "directorfilms"
        String title = getTextValue(element, "t");
        String director = getTextValue(element, "dirn");
        int year = getIntValue(element, "year");
        List<String> genres = getTextList(element,"cat");
        capitalizeStringList(genres);
        String xmlId = getTextValue(element,"fid");


        // create a new Employee with the value read from the xml nodes
        return new Movie(generateRandomMovieId(), title, year, director,genres,xmlId);
    }



    private String generateRandomMovieId(){
        //TODO figure out how to generate a string for a movie id. Asking MySQL to make one was recommended against as a final solution
        return "Default";
    }

    private void printAllDataStatistics(){
        System.out.println("Total parsed " + movies.size() + " movies");
        System.out.println("Total genres found: " + genreNames.size());
        System.out.println("All genres found: " + genreNames);
    }

    public void verifyMovie(Movie movie, Element element,int position) throws MovieParseError{
        boolean failed = false;
        if (movie.title==null || movie.title.isEmpty()){
            System.out.println("Error parsing movie"+position+" "+movie+": Movie doesn't have a title at element \"t\". Not inserting movie.");
            throw new MovieParseError();
        }
        if (movie.director == null || movie.director.isEmpty()){
            System.out.println("Error parsing movie"+position+" "+movie+": Movie doesn't have a director at element \"dirn\". Setting directory as Unknown.");
            movie.director = "Unknown";
        }
        if (movie.year==-1){
            System.out.println("Error parsing movie"+position+" "+movie+": Failed to parse year from element \"year\". Had a value of "+getTextValue(element,"year")+".Setting date as -1.");
        }
        if (movie.xmlId == null || movie.xmlId.isEmpty()){
            movie.xmlId=null;
            System.out.println("Error parsing movie"+position+" "+movie+": Failed to parse xml id from element \"fid\". Setting to null. Will be unable to link stars later on");
        }

    }
    //This is quite slow and will use a lot of RAM if the xml file is big, since DOM loads the entire tree, compared to SAX which goes one at a time and uses events
    public void executeMoviesParsingFromXmlFile(String filePath){
        createDomFromXmlFile(filePath);
        parseAllMovies();
    }
    //Can only be called after getMoviesFromXmlFile
    public Set<String> getParsedGenres(){
        return genreNames;
    }
    public Set<Movie> getMovies(){return movies;}

    public static class MovieParseError extends RuntimeException{

    }
    //For testing. Another class should be used to use MovieDomParser and insert into the mysql database
    public static void main(String[] args) {

        MovieDomParser domParser = new MovieDomParser();
        //You're gonna have to change this
        domParser.createDomFromXmlFile("F:\\CS122BProjectLogs\\xml crap\\stanford-movies\\mains243.xml");
        domParser.parseAllMovies();
        domParser.printAllDataStatistics();

    }
}
