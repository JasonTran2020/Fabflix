package datainserters.XMLparsers;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;



//Serves as the base class for the other parsers, providing general purpose functions that all parsers will use
abstract public class DomParser {
    Document dom;
    protected void createDomFromXmlFile(String filePath) {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            // parse using builder to get DOM representation of the XML file
            dom = documentBuilder.parse(filePath);
        } catch (ParserConfigurationException | SAXException | IOException error) {
            error.printStackTrace();
        }
    }
    protected String getTextValue(Element element, String tagName) {
        String textVal = null;
        NodeList nodeList = element.getElementsByTagName(tagName);
        //Only take a single value, even if there are multiple
        //First element might not have text, so keep checking all children until a text is found
        for (int i = 0; i < nodeList.getLength(); i++){
            if (nodeList.item(i).getFirstChild() == null){
                continue;
            }
            //Use getTextContent, instead of getNodeValue, as it gets the text of the entire element, including text nested deep into other elements
            textVal = toISO8859_1(nodeList.item(i).getTextContent());
            break;
        }
        return textVal;
    }

    protected int getIntValue(Element ele, String tagName) {
        //Potential exception if value is not a number
        try{
            return Integer.parseInt(getTextValue(ele, tagName));
        }
        catch (NumberFormatException e){
            //System.out.println("Failed to get integer from the element: " + ele.toString());
            return -1;
        }

    }
    protected int getYearValue(Element ele, String tagName){
        //Expects at least 4 digits. Ignores the rest. Created due to some actors having a birthyear format like 1990+.
        // Clearly the year is 1990, but the + causes a parsing error in getIntValue
        try{
            String yearAsString = getTextValue(ele,tagName);
            if (yearAsString!=null && yearAsString.length()>=4){
                yearAsString = yearAsString.substring(0,4);
                return Integer.parseInt(yearAsString);
            }
            return -1;
        }
        catch (NumberFormatException e){
            //System.out.println("Failed to get integer from the element: " + ele.toString());
            return -1;
        }
    }

    protected List<String> getTextList(Element element, String tagName) {
        List<String> textList = new ArrayList<>();
        NodeList nodeList = element.getElementsByTagName(tagName);
        for (int i = 0; i < nodeList.getLength(); i++){
            Node currentNode = nodeList.item(i).getFirstChild();
            if (currentNode != null && currentNode.getNodeValue() != null && !currentNode.getNodeValue().isEmpty()){
                textList.add(toISO8859_1(currentNode.getNodeValue()));
            }
        }
        return textList;
    }

    protected String toISO8859_1(String text)  {
        try{
            if (text == null){
                return "";
            }
            return new String(text.getBytes(),"ISO-8859-1");
        }
        catch(UnsupportedEncodingException e){
            return "Should have never happened";
        }
    }

    protected  void capitalizeStringList(List<String> listOfStrings){
        for (int index = 0 ; index <listOfStrings.size();index++){
            listOfStrings.set(index,capitalizeAndStripString(listOfStrings.get(index)));
        }
    }
    protected String capitalizeAndStripString(String line){
        line = line.toLowerCase();
        String result = Character.toUpperCase(line.charAt(0)) + line.substring(1);
        result = result.strip();
        return result;
    }


    //Java implementation of Levenshtein distance. From this post: https://stackoverflow.com/questions/955110/similarity-string-comparison-in-java
    private static int editDistance(String s1, String s2) {
        s1 = s1.toLowerCase();
        s2 = s2.toLowerCase();

        int[] costs = new int[s2.length() + 1];
        for (int i = 0; i <= s1.length(); i++) {
            int lastValue = i;
            for (int j = 0; j <= s2.length(); j++) {
                if (i == 0)
                    costs[j] = j;
                else {
                    if (j > 0) {
                        int newValue = costs[j - 1];
                        if (s1.charAt(i - 1) != s2.charAt(j - 1))
                            newValue = Math.min(Math.min(newValue, lastValue),
                                    costs[j]) + 1;
                        costs[j - 1] = lastValue;
                        lastValue = newValue;
                    }
                }
            }
            if (i > 0)
                costs[s2.length()] = lastValue;
        }
        return costs[s2.length()];
    }

    public static double similarity(String s1, String s2) {
        String longer = s1, shorter = s2;
        if (s1.length() < s2.length()) { // longer should always have greater length
            longer = s2; shorter = s1;
        }
        int longerLength = longer.length();
        if (longerLength == 0) { return 1.0; /* both strings are zero length */ }

        return (longerLength - editDistance(longer, shorter)) / (double) longerLength;

    }
}
