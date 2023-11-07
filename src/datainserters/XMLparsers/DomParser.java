package datainserters.XMLparsers;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

//Serves as the base class for the other parsers, providing general purpose functions that all parsers will use
abstract public class DomParser {
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

    protected List<String> getTextList(Element element, String tagName) {
        List<String> textList = new ArrayList<>();
        NodeList nodeList = element.getElementsByTagName(tagName);
        for (int i = 0; i < nodeList.getLength(); i++){
            Node currentNode = nodeList.item(i).getFirstChild();
            if (currentNode != null){
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
}
