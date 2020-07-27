package plp.objects;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import plp.PLP;
import plp.objects.condition.Condition;
import plp.objects.effect.Effect;

import java.util.ArrayList;

public class Preconditions {
    public ArrayList<Condition> Conditions = new ArrayList<>();

    public Preconditions(Node node, ArrayList<PlanningStateVariable> declaredStateVariables, PLP plp) throws Exception {
        NodeList childNodes = node.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node childNode = childNodes.item(i);

            if (childNode.getNodeType() == Node.ELEMENT_NODE &&
                    Condition.ConditionTags.contains(childNode.getNodeName()))
            {
                Conditions.add(new Condition(childNode, false, declaredStateVariables, plp));
            }
        }
    }
}
