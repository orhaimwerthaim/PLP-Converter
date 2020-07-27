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
import java.util.HashSet;
import java.util.stream.Collectors;

public class AndOrCondition implements ICondition {
    public final EAndOrCondition ConditionType;

    public ArrayList<Condition> conditions = new ArrayList<>();

    public AndOrCondition(EAndOrCondition conditionType)
    {
        ConditionType = conditionType;
    }

    public AndOrCondition(Node node, EAndOrCondition conditionType, ArrayList<PlanningStateVariable> declaredStateVariables, PLP plp) throws Exception {
        ConditionType = conditionType;
        NodeList nl = node.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                switch (n.getNodeName()) {
                    case "predicate_condition":
                    case "formula_condition":
                    case "forall_condition":
                    case "exists_condition":
                    case "not_condition":
                    case "AND":
                    case "OR":
                    case "bernoulli_sample_condition":
                        conditions.add(new Condition(n, false, declaredStateVariables, plp));
                        break;
                }
            }
        }
    }

    @Override
    public ArrayList<PlanningTypedParameter> GetParams() {
        ArrayList<PlanningTypedParameter> params = new ArrayList<>();
        for (Condition con:conditions
             ) {
            params.addAll(con.condition.GetParams());
        }

        return params;
    }

    @Override
    public String getConditionForIf(ArrayList<PlanningTypedParameter> assignToParam, boolean underAndOrCondition, boolean IsObservationEffected) {
        ArrayList<PlanningTypedParameter> params = new ArrayList<>();
        HashSet<String> temp = new HashSet<>();
        for (PlanningTypedParameter par:GetParams()) { if(temp.add(par.getName())) params.add(par); }

        String existForIf = underAndOrCondition ? "" :
                PLP2RDDL_Utils.GetExistsForPredicate(params.toArray(new PlanningTypedParameter[params.size()]), assignToParam);

        String operator = ConditionType == EAndOrCondition.And ? "^" : "|";
        StringBuilder result = new StringBuilder(
                conditions.stream()
                        .map(con-> con.condition.getConditionForIf(assignToParam, true, IsObservationEffected))
                        .collect(Collectors.joining(operator)));
        return "(" + existForIf + " " + result + ")";
    }

    @Override
    public String getConditionForIf_JavaNextState(ArrayList<PlanningTypedParameter> assignToParams, boolean underAndOrCondition, boolean IsObservationEffected) {
        String operator = ConditionType == EAndOrCondition.And ? "&&" : "||";
        StringBuilder result = new StringBuilder(
                conditions.stream()
                        .map(con-> con.condition.getConditionForIf_JavaNextState(assignToParams, true, IsObservationEffected))
                        .collect(Collectors.joining(operator)));
        return result.toString();
    }

    @Override
    public String getConditionForIf_JavaValidActions(ArrayList<PlanningTypedParameter> assignToParams, boolean underAndOrCondition, boolean IsObservationEffected) {
//        ArrayList<PlanningTypedParameter> params = new ArrayList<>();
//        HashSet<String> temp = new HashSet<>();
//        for (PlanningTypedParameter par:GetParams()) { if(temp.add(par.getName())) params.add(par); }


        String operator = ConditionType == EAndOrCondition.And ? ")&&(" : ")||(";
        StringBuilder result = new StringBuilder(
                conditions.stream()
                        .map(con-> con.condition.getConditionForIf_JavaValidActions(assignToParams, true, IsObservationEffected))
                        .collect(Collectors.joining(operator)));
        return "("+result.toString()+")";
    }

    public enum EAndOrCondition
    {
        And,
        Or
    }

}
