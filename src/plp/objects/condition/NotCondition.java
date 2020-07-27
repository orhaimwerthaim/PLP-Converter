package plp.objects.condition;

import org.w3c.dom.Node;
import plp.PLP;
import plp.objects.ICondition;
import plp.objects.PlanningStateVariable;
import plp.objects.PlanningTypedParameter;

import java.util.ArrayList;

public class NotCondition implements ICondition {
    public Condition Condition;
    ArrayList<PlanningTypedParameter> _assignToParams = new ArrayList<>();
    public NotCondition(Node node, ArrayList<PlanningStateVariable> declaredStateVariables, PLP plp) throws Exception {
        Condition = new Condition(node, true, declaredStateVariables, plp);
    }

    @Override
    public ArrayList<PlanningTypedParameter> GetParams() {
        return Condition.condition.GetParams();
    }

    @Override
    public String getConditionForIf(ArrayList<PlanningTypedParameter> assignToParams, boolean underAndOrCondition, boolean IsObservationEffected) {
        _assignToParams = assignToParams;
        return "(~(" + Condition.condition.getConditionForIf(assignToParams, underAndOrCondition, IsObservationEffected) + "))";
    }

    @Override
    public String getConditionForIf_JavaNextState(ArrayList<PlanningTypedParameter> assignToParams, boolean underAndOrCondition, boolean IsObservationEffected) {
        return "(!(" + Condition.condition.getConditionForIf_JavaNextState(assignToParams, underAndOrCondition, IsObservationEffected) + "))";
    }

    @Override
    public String getConditionForIf_JavaValidActions(ArrayList<PlanningTypedParameter> assignToParams, boolean underAndOrCondition, boolean IsObservationEffected) {
        _assignToParams = assignToParams;
        return "(!(" + Condition.condition.getConditionForIf_JavaValidActions(assignToParams, underAndOrCondition, IsObservationEffected) + "))";
    }

    @Override
    public String toString() {
        return getConditionForIf(_assignToParams, false, false);
    }

}
