package rddl.objects;

import plp.PLP;
import plp.objects.PlanningStateVariable;
import plp.objects.PlanningTypedParameter;
import plp.objects.Predicate;
import plp.objects.condition.Condition;
import plp.objects.effect.ConditionalEffect;
import plp.objects.effect.Effect;
import plp.objects.effect.IEffect;
import plp2java.MicroState_Writer;
import plp2java.plp2javaUtils.PLP2JavaUtils;
import rddl.PLP2RDDL_Utils;
import rddl.PLPsToRDDL;
import utils.Triplet;

import java.util.ArrayList;

public class ConditionalEffectToCPFS {
    Condition condition;
    //for each effect param the condition params that are linked to it.
    //so we can change all their names by it's name.
    //the first item in the array list is for the first param in the effect predicate. all the object in his array list are condition parameters that should have the same name as he has
    Effect effect;
    Predicate stateVariablePredicate;
    ArrayList<ArrayList<PlanningTypedParameter>> effectLinkedParam;
    PlanningStateVariable effectedStateVariable;
    public ConditionalEffectToCPFS(Effect conditionalEffect, Predicate _stateVariablePredicate, PlanningStateVariable _effectedStateVariable)
    {
        effectedStateVariable = _effectedStateVariable;
        condition = (conditionalEffect.effect instanceof ConditionalEffect) ? ((ConditionalEffect)conditionalEffect.effect).condition : null;
        effect = conditionalEffect;
        stateVariablePredicate = _stateVariablePredicate;
        LinkConditionParamsToEffectParams();
    }


    public int GetNumberOfParametersInEffectedPredicate()
    {
        return stateVariablePredicate.Params.size();
    }
    /**
     *This method need to be called after ConditionalEffectToCPFS.SetParamName(index, name) was called for all indexes
     * @return Triplet<String> are the parts fo theCPFS line, Second in Triplet is if condition
     */
    public Triplet<String> GetCPFS_Parts(PLP plp) throws Exception {
        Triplet<String> res = new Triplet<>();
        boolean IsIntermediateOrObservation = effectedStateVariable.IsGlobalIntermediate || effectedStateVariable.IsObservation;
        res.First = IsIntermediateOrObservation ? stateVariablePredicate.baseToString(false, true) + "="
                : stateVariablePredicate.baseToString(true, true) + "=";

        String upon = plp == null ? "" : effect.effect.getEffectingUpon() == IEffect.EEffectingUpon.failure ? "~" + PLPsToRDDL.GetActionSuccessIntermName(plp) :
                effect.effect.getEffectingUpon() == IEffect.EEffectingUpon.success ? PLPsToRDDL.GetActionSuccessIntermName(plp) :
                        effect.effect.getEffectingUpon() == IEffect.EEffectingUpon.always ? PLP2RDDL_Utils.GetExistsForPredicate(plp.GetParams()) : "";
        String conditionIf = condition == null ? "" : condition.condition.getConditionForIf(stateVariablePredicate.Params, false, effectedStateVariable.IsObservation);
        upon += (upon.isEmpty() || conditionIf.isEmpty()) ? "" : "^";
        res.Second =  "if (" + upon + conditionIf + ") then " + effect.effect.getEffectAssignedValue();
        res.Third = " " + (IsIntermediateOrObservation ? effectedStateVariable.GetDefault()
                : stateVariablePredicate.baseToString(false, true)) + ";";
        return res;
    }

   /* public Triplet<String> GetCPFS_Parts_JAVA(PLP plp) throws Exception {
        Triplet<String> res = new Triplet<>();
        boolean IsIntermediateOrObservation = effectedStateVariable.IsGlobalIntermediate || effectedStateVariable.IsObservation;
        res.First = PLP2JavaUtils.GetForAllString(stateVariablePredicate.Params.toArray(new PlanningTypedParameter[stateVariablePredicate.Params.size()]));


        String upon = plp == null ? "" : effect.effect.getEffectingUpon() == IEffect.EEffectingUpon.failure ? "!interm.success_" + plp.getName():
                effect.effect.getEffectingUpon() == IEffect.EEffectingUpon.success ? "!interm.success_" + plp.getName() :
                        effect.effect.getEffectingUpon() == IEffect.EEffectingUpon.always ? "action._o1.equals(\""+plp.getName()+"\")" : "";

        String conditionIf = condition == null ? "" : condition.condition.getConditionForIf_Java(stateVariablePredicate.Params, false, effectedStateVariable.IsObservation);
        upon += (upon.isEmpty() || conditionIf.isEmpty()) ? "" : " && ";
        res.Second =  "if (" + upon + conditionIf + ") {return " + effect.effect.getEffectAssignedValue() + ";}\n";
        res.Third = " " + (IsIntermediateOrObservation ? (effectedStateVariable.GetDefault() + ";\n")
                : " var.value;\n");
        for(int i = stateVariablePredicate.Params.size() - 1; i >= 0; i--) {
            ;
            res.Third += "    ".repeat(i + 2) + "}";
        }
        return res;
    }
*/

    private void LinkConditionParamsToEffectParams()
    {
        effectLinkedParam = new ArrayList<>();
        if(condition == null) return;
        ArrayList<Predicate> conditionPredicates = new ArrayList<>();
        PLP2RDDL_Utils.GetConditionPredicates(condition, conditionPredicates);

        String parameterName;
        for(int i = 0; i < stateVariablePredicate.Params.size(); i++) {
            parameterName = stateVariablePredicate.Params.get(i).getName();
            ArrayList<PlanningTypedParameter> linkedParameters = new ArrayList<>();

            for (Predicate pred:conditionPredicates
                 ) {
                for (PlanningTypedParameter predParam:pred.Params
                     ) {
                    if(parameterName.equals(predParam.getName())) {
                        linkedParameters.add(predParam);
                    } } }
            effectLinkedParam.add(linkedParameters);
        }
    }

    public void SetParamName(int predicateParamIdndex, String paramName)
    {
        stateVariablePredicate.Params.get(predicateParamIdndex).setName(paramName);
        if(effectLinkedParam.size() > 0) {
            for (int i = 0; i < effectLinkedParam.get(predicateParamIdndex).size(); i++) {
                effectLinkedParam.get(predicateParamIdndex).get(i).setName(paramName);
            }
        }
    }
}
