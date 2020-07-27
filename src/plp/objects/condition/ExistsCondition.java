package plp.objects.condition;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import plp.PLP;
import plp.objects.ICondition;
import plp.objects.PlanningStateVariable;
import plp.objects.PlanningTypedParameter;
import rddl.PLP2RDDL_Utils;

import java.util.ArrayList;

public class ExistsCondition implements ICondition {
    public ArrayList<String> params = new ArrayList<>();
    public Condition condition;

    public ExistsCondition(Node node, ArrayList<PlanningStateVariable> declaredStateVariables, PLP plp) throws Exception {
        NodeList childNodes = node.getChildNodes();
        Node conditionNode = null;
        for (int i = 0; i < childNodes.getLength(); i++) {
            conditionNode = childNodes.item(i);
            if (conditionNode.getNodeType() == Node.ELEMENT_NODE && conditionNode.getNodeName().contains("predicate_condition")) {
                break;
            }
        }
        condition = new Condition(conditionNode, false, declaredStateVariables, plp);

//        NodeList nl = node.getChildNodes();
//        for(int i = 0; i < nl.getLength(); i++)
//        {
//            Node n = nl.item(i);
//            if(n.getNodeType() == Node.ELEMENT_NODE) {
//                switch (n.getNodeName()) {
//                    case "param":
//                        String param = ((Element) n).getAttribute("name");
//                        params.add((param));
//                        break;
//                    case "predicate_condition":
//                    case "formula_condition":
//                    case "forall_condition":
//                    case "not_condition":
//                    case "AND":
//                    case "OR":
//                        //condition = new Condition(n, false);
//                        break;
//                }
//            }
//        }
    }

    @Override
    public String getConditionForIf(ArrayList<PlanningTypedParameter> assignToParams, boolean underAndOrCondition, boolean IsObservationEffected) {
        String s = condition.condition.getConditionForIf(assignToParams, underAndOrCondition, IsObservationEffected);
        return "(" + s + ")";
    }


    @Override
    public String getConditionForIf_JavaNextState(ArrayList<PlanningTypedParameter> assignToParams, boolean underAndOrCondition, boolean IsObservationEffected) {
        String s = condition.condition.getConditionForIf_JavaNextState(assignToParams, underAndOrCondition, IsObservationEffected);
        return s;



    }

    @Override
    public String getConditionForIf_JavaValidActions(ArrayList<PlanningTypedParameter> assignToParams, boolean underAndOrCondition, boolean IsObservationEffected) {
        String s = condition.condition.getConditionForIf_JavaValidActions(assignToParams, underAndOrCondition, IsObservationEffected);
        return "(" + s + ")";
    }

    @Override
    public ArrayList<PlanningTypedParameter> GetParams() {
        return condition.condition.GetParams();
    }

}
