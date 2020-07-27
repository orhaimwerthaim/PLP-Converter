package plp.objects;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;

public class ParamBase {
    public String Name;
    public ArrayList<String> Params = new ArrayList<>();

    public ParamBase(Node node) throws Exception {
        Element el = (Element)node;
        Name = el.getAttribute("name");
        NodeList nl = ((Element) node).getElementsByTagName("field");
        for(int i = 0; i < nl.getLength(); i++)
        {
            Node n = nl.item(i);
            if(n.getNodeType() == Node.ELEMENT_NODE) {
                Params.add(((Element)n).getAttribute("value"));
            }
        }
    }
}
