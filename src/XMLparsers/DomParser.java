package XMLparsers;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

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
            //Apparently text is considered its own node? hence why you need to call getFirstChild.
            Node currentNode = nodeList.item(i).getFirstChild();
            if (currentNode !=null){
                textVal = currentNode.getNodeValue();
                break;
            }

        }
        return textVal;
    }

    protected int getIntValue(Element ele, String tagName) {
        //Potential exception if value is not a number
        try{
            return Integer.parseInt(getTextValue(ele, tagName));
        }
        catch (NumberFormatException e){
            System.out.println("Failed to get integer from the element: " + ele.toString());
            return -1;
        }

    }

    protected List<String> getTextList(Element element, String tagName) {
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
}
