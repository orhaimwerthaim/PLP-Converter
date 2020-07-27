package plp.problem_file_objects;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;

public class InitialStateOption {
   public Double weight;
   public ArrayList<StateVariableWithValue> assignments = new ArrayList<>();

    public InitialStateOption(Element el) throws Exception {
        weight = el.hasAttribute("weight") ? Double.parseDouble(el.getAttribute("weight")) : null;

        NodeList nl = el.getElementsByTagName("state_variable_with_value");
        for(int i = 0; i < nl.getLength(); i++) {
            if (nl.item(i).getNodeType() != Node.ELEMENT_NODE) continue;
            assignments.add(new StateVariableWithValue(nl.item(i)));
        }
    }
}
