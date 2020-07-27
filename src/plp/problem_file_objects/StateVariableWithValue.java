package plp.problem_file_objects;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import plp.PLP;
import plp.objects.PlanningStateVariable;
import plp.objects.PlanningTypedParameter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class StateVariableWithValue {
    public String type;
    public ArrayList<String> params = new ArrayList<>();
    public String value;

    public StateVariableWithValue(Node node) throws Exception {
        Element el = (Element)node;
        type = ((Element)el.getElementsByTagName("state_variable").item(0)).getAttribute("type");
        NodeList nl = ((Element) node).getElementsByTagName("param");
        for(int i = 0; i < nl.getLength(); i++) {
            if (nl.item(i).getNodeType() != Node.ELEMENT_NODE) continue;
            params.add(((Element) nl.item(i)).getAttribute("name"));
        }
        value = ((Element)((Element) node).getElementsByTagName("value").item(0)).getTextContent();
    }
}
