package datainserters.XMLparsers;

import datamodels.dbitems.Star;
import datamodels.dbitems.StarInMovie;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.HashSet;
import java.util.Set;

public class CastDomParser extends DomParser{
    protected Set<StarInMovie> starsInMovies = new HashSet<>();

    private void parseAllCast(){
        Element documentElement = dom.getDocumentElement();
        NodeList castNodeList = documentElement.getElementsByTagName("m");
        for (int i = 0; i < castNodeList.getLength() ; i++) {
            Element element = (Element) castNodeList.item(i);
            StarInMovie starInMovie = parseCast(element);
            try{
                verifyStarInMovie(starInMovie);
                System.out.println(i+1+": " + starInMovie);
                starsInMovies.add(starInMovie);
            }
            catch (SIMParseError e){
                System.out.println(e);
            }

        }
    }

    private StarInMovie parseCast(Element element){
        String xmlMovieId = getTextValue(element,"f");
        String xmlStarId = getTextValue(element,"a");

        return new StarInMovie(xmlMovieId,xmlStarId);
    }

    private void verifyStarInMovie(StarInMovie starInMovie) throws SIMParseError{
        if (starInMovie.xmlMovieId == null || starInMovie.xmlMovieId.isEmpty()){
            throw new SIMParseError("No movieId for particular cast. Ignoring element");
        }
        if (starInMovie.xmlStarId == null || starInMovie.xmlStarId.isEmpty()){
            throw new SIMParseError("No name/actor id for particular cast. Ignoring element");
        }
    }

    public static class SIMParseError extends RuntimeException{
        public SIMParseError(String errorMessage){
            super(errorMessage);
        }
    }

    public Set<StarInMovie> getStarsInMovies(){
        return starsInMovies;
    }
    private void printAllDataStatistics(){
        System.out.println("Total parsed " + starsInMovies.size() + " stars in movies");
    }
    public void executeStarsParsingFromXmlFile(String filePath){
        createDomFromXmlFile(filePath);
        parseAllCast();
    }

    public static void main(String[] args){
        CastDomParser castDomParser = new CastDomParser();
        castDomParser.executeStarsParsingFromXmlFile("F:\\CS122BProjectLogs\\xml crap\\stanford-movies\\casts124.xml");
    }
}
