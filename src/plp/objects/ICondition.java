package plp.objects;

import java.util.ArrayList;

public interface ICondition {
    ArrayList<PlanningTypedParameter> GetParams();
    String getConditionForIf(ArrayList<PlanningTypedParameter> assignToParams, boolean underAndOrCondition, boolean IsObservationEffected);
    String getConditionForIf_JavaValidActions(ArrayList<PlanningTypedParameter> assignToParams, boolean underAndOrCondition, boolean IsObservationEffected);
    String getConditionForIf_JavaNextState(ArrayList<PlanningTypedParameter> assignToParams, boolean underAndOrCondition, boolean IsObservationEffected);
}
