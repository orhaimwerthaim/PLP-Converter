package plp.objects.condition;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import plp.objects.PlanningStateVariable;
import plp.objects.effect.Effect;

import java.util.ArrayList;

//if Condition is null then the probability is not conditional
public class ConditionalProbability {
    public float Probability;
    public plp.objects.condition.Condition Condition;

    public ConditionalProbability(float probability) {
        Condition = null;
        Probability = probability;
    }

    public ConditionalProbability(Node node, ArrayList<PlanningStateVariable> declaredStateVariables) throws Exception {
        throw new UnsupportedOperationException();
        /*NodeList childNodes = node.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node childNode = childNodes.item(i);
            if (childNode.getNodeType() == Node.ELEMENT_NODE) {
                if (Condition.ConditionTags.contains(childNode.getNodeName())) {
                    Condition = new Condition(childNode, false, declaredStateVariables);
                }
                if (childNode.getNodeName().equals("probability")) {*/
//        Element el = (Element)node;
//        Name = el.getAttribute("name");
//        NodeList nl = ((Element) node).getElementsByTagName("field");
//        for(int i = 0; i < nl.getLength(); i++)
//        {
//            Node n = nl.item(i);
//            if(n.getNodeType() == Node.ELEMENT_NODE) {
//                Params.add(((Element)n).getAttribute("value"));
//            }
//        }
    }
}
