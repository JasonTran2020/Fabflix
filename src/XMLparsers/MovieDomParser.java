package XMLparsers;

import datamodels.dbitems.Movie;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MovieDomParser {

    List<Movie> movies = new ArrayList<>();
    Document dom;

    private void createDomFromXmlFile(String filePath) {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            // parse using builder to get DOM representation of the XML file
            dom = documentBuilder.parse(filePath);
        } catch (ParserConfigurationException | SAXException | IOException error) {
            error.printStackTrace();
        }
    }

    private void parseAllMovies(){
        // get the document root Element
        Element documentElement = dom.getDocumentElement();

        NodeList filmNodeList = documentElement.getElementsByTagName("film");
        //Because I don't want to go through the whole xml file yet
        int currentMax = 100;
        for (int i = 0; i < filmNodeList.getLength() ; i++) {
            Element element = (Element) filmNodeList.item(i);
            Movie movie = parseMovie(element);
            System.out.println(i+1+": " + movie);
            movies.add(movie);
        }
    }

    private Movie parseMovie(Element element) {
        //Assumed element is a "film" element. Not "movies" or "directorfilms"
        String title = getTextValue(element, "t");
        String director = getTextValue(element, "dirn");
        int year = getIntValue(element, "year");
        if (year == -1){
            System.out.println("Failed to get integer from the film: " + title);
        }
        List<String> genres = getTextList(element,"cat");

        // create a new Employee with the value read from the xml nodes
        return new Movie(generateRandomMovieId(), title, year, director,genres);
    }

    private String getTextValue(Element element, String tagName) {
        String textVal = null;
        NodeList nodeList = element.getElementsByTagName(tagName);
        //Only take a single value, even if there are multiple
        //First element might not have text, so keep checking all children until a text is found
        for (int i = 0; i < nodeList.getLength(); i++){
            //Apparently text is considered its own node? hence why you need to call getFirstChild.
            Node currentNode = nodeList.item(i).getFirstChild();
            if (currentNode !=null){
                textVal = currentNode.getNodeValue();
                break;
            }

        }
        return textVal;
    }

    private int getIntValue(Element ele, String tagName) {
        //Potential exception if value is not a number
        try{
            return Integer.parseInt(getTextValue(ele, tagName));
        }
        catch (NumberFormatException e){
            System.out.println("Failed to get integer from the element: " + ele.toString());
            return -1;
        }

    }

    private List<String> getTextList(Element element, String tagName) {
        List<String> textList = new ArrayList<>();
        NodeList nodeList = element.getElementsByTagName(tagName);
        for (int i = 0; i < nodeList.getLength(); i++){
            Node currentNode = nodeList.item(i).getFirstChild();
            if (currentNode != null){
                textList.add(currentNode.getNodeValue());
            }
        }
        return textList;
    }

    private String generateRandomMovieId(){
        //TODO figure out how to generate a string for a movie id. Asking MySQL to make one was recommended against as a final solution
        return "Default";
    }

    private void printAllDataStatistics(){
        System.out.println("Total parsed " + movies.size() + " movies");
    }

    //This is quite slow and will use a lot of RAM if the xml file is big, since DOM loads the entire tree, compared to SAX which goes one at a time and uses events
    public List<Movie> getMoviesFromXmlFile(String filePath){
        createDomFromXmlFile(filePath);
        parseAllMovies();
        return movies;
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
