package plp.objects;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import plp.objects.condition.ConditionalProbability;

import java.util.ArrayList;

public class ProbabilityType {
    private ArrayList<ConditionalProbability> conditionalProbabilities;
    public ProbabilityType(Node node, ArrayList<PlanningStateVariable> stateVariable) throws Exception {
        conditionalProbabilities = new ArrayList<>();
        NodeList childNodes = node.getChildNodes();
        for(int i=0; i<childNodes.getLength(); i++) {
            Node childNode = childNodes.item(i);
            if (childNode.getNodeType() == Node.ELEMENT_NODE) {
                switch (childNode.getNodeName()) {
                    case "probability":
                        String sProb = ((Element)childNode).getAttribute("value");
                        ConditionalProbability cp = new ConditionalProbability(
                                Float.parseFloat(sProb));
                        conditionalProbabilities.add(cp);
                        return;
                    case "conditional_probability":
                        conditionalProbabilities.add(new ConditionalProbability(childNode, stateVariable));
                }
            }
        }
    }

    public ConditionalProbability[] GetConditionalProbabilities()
    {
        return conditionalProbabilities.toArray(new ConditionalProbability[conditionalProbabilities.size()]);
    }
}
