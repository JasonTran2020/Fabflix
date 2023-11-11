package datainserters.XMLparsers;

import datamodels.dbitems.Movie;
import datamodels.dbitems.Star;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.sql.PreparedStatement;
import java.util.HashSet;
import java.util.Set;

public class StarsDomParser extends DomParser{
    Set<Star> stars = new HashSet<>();
    private void parseAllStars(){
        Element documentElement = dom.getDocumentElement();
        NodeList starNodeList = documentElement.getElementsByTagName("actor");
        //Because I don't want to go through the whole xml file yet
        int currentMax = 100;
        for (int i = 0; i < starNodeList.getLength() ; i++) {
            Element element = (Element) starNodeList.item(i);
            Star star = parseStar(element);
            try{
                verifyStar(star,element,i);
                //System.out.println(i+1+": " + star);
                stars.add(star);
            }
            catch (StarsDomParser.StarParseError e){
                System.out.println(e);
            }

        }
    }

    private Star parseStar(Element element){
        String starName = getTextValue(element,"stagename");
        Integer birthYear = getYearValue(element,"dob");
        return new Star("Default",starName,birthYear);
    }

    private void verifyStar(Star star, Element element, int position){
        if (star.name == null || star.name.isEmpty()){
            throw new StarParseError("Error parsing actor"+position+": entry doesn't have a name at element \"stagename\". Skipping it");
        }
        if (star.birthYear == null || star.birthYear==-1){
            System.out.println("Error parsing actor"+position+": The actor " +star.name+" doesn't have a birth year at element \"dob\". Setting birthyear to null");
            star.birthYear = null;
        }
    }

    public static class StarParseError extends RuntimeException{
        public StarParseError(String errorMessage){
            super(errorMessage);
        }
    }

    private void printAllDataStatistics(){
        System.out.println("Total parsed " + stars.size() + " stars");
    }
    public void executeStarsParsingFromXmlFile(String filePath){
        createDomFromXmlFile(filePath);
        parseAllStars();
    }

    public Set<Star> getStars() {
        return stars;
    }
    public static void main(String[] args){
        StarsDomParser starsDomParser = new StarsDomParser();

        starsDomParser.executeStarsParsingFromXmlFile("F:\\CS122BProjectLogs\\xml crap\\stanford-movies\\actors63.xml");
        starsDomParser.printAllDataStatistics();
    }
}
