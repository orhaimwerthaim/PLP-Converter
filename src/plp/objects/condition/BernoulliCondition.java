package plp.objects.condition;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import plp.PLP;
import plp.objects.ICondition;
import plp.objects.PlanningStateVariable;
import plp.objects.PlanningTypedParameter;

import java.util.ArrayList;

public class BernoulliCondition implements ICondition {
    public Double Probability;
    public BernoulliCondition(Node node) throws Exception {
        {
            NodeList childNodes = node.getChildNodes();
            Node conditionNode = null;
            for (int i = 0; i < childNodes.getLength(); i++) {
                conditionNode = childNodes.item(i);
                if (conditionNode.getNodeType() == Node.ELEMENT_NODE && conditionNode.getNodeName().equals("probability")) {
                    Probability = Double.parseDouble(((Element) conditionNode).getAttribute("value"));
                    break;
                }
            }
        }
    }


    @Override
    public ArrayList<PlanningTypedParameter> GetParams() {
        return new ArrayList<>();
    }

    @Override
    public String getConditionForIf(ArrayList<PlanningTypedParameter> assignToParams, boolean underAndOrCondition, boolean IsObservationEffected) {
        return "Bernoulli("+Probability+")";
    }


    @Override
    public String getConditionForIf_JavaNextState(ArrayList<PlanningTypedParameter> assignToParams, boolean underAndOrCondition, boolean IsObservationEffected) {
        return null;
    }

    @Override
    public String getConditionForIf_JavaValidActions(ArrayList<PlanningTypedParameter> assignToParams, boolean underAndOrCondition, boolean IsObservationEffected) {
        return null;
    }

}
