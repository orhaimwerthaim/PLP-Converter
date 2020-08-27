package rddl;

import convert.PLP_Converter;
import plp.PLP;
import plp.objects.*;
import plp.objects.effect.*;
import utils.Triplet;

import java.util.HashMap;

public class CPFS_Line {
//    public static  Triplet Get_CPFS_ConditionalLine(ConditionalEffect conditionalEffect, HashMap<PLP, ConditionalEffect> effectsByPLP) {
//        Triplet result = new Triplet();
//        result.First = conditionalEffect.effect.effect.getName() + "'=";
//        StringBuilder second = new StringBuilder();
//
//        effectsByPLP.forEach((plp, plpEffect)->
//        {
//            String sEffectingUpon = conditionalEffect.effect.effect.getEffectingUpon() == IEffect.EEffectingUpon.success ?
//                    "("+PLP_Converter.GetActionSuccessIntermName(plp)+")^" :
//                    conditionalEffect.effect.effect.getEffectingUpon() == IEffect.EEffectingUpon.failure ?
//                            "(~"+PLP_Converter.GetActionSuccessIntermName(plp)+")^" : "";
//
//            second.append("if(" + sEffectingUpon +
//                    "(" + plpEffect.condition.condition.getConditionForIf() +
//                    ")) then " +
//                    conditionalEffect.getEffectAssignedValue() +
//                    " else ");
//            plp.PlpNameWithParams(false);
//        });
//
//        result.Second = second.toString();
//        return result;
//    }



    public static Triplet<String> Get_CPFS_LinePart(Predicate assignToVar, HashMap<PLP, Effect> effectsByPLP)
    {
        return Get_CPFS_LinePart(assignToVar, effectsByPLP, false);
    }

    public static Triplet<String> Get_CPFS_LinePart(Predicate assignToVar, HashMap<PLP, Effect> effectsByPLP, boolean IsAssignToObservationVariable)
    {
        Triplet result = new Triplet();
        result.First = assignToVar.baseToString(true, true) + "=";
        result.Third = assignToVar.baseToString(false, true) + ";";

        StringBuilder second = new StringBuilder();

        effectsByPLP.forEach((plp,plpEffect)->
        {
            PLP2RDDL_Utils.ParametersNameChangeTo(plp.GetParams());
            String sEffectingUpon = plpEffect.effect.getEffectingUpon() == IEffect.EEffectingUpon.success ?
                    "("+ PLP_Converter.GetActionSuccessIntermName(plp)+")^" :
                    plpEffect.effect.getEffectingUpon() == IEffect.EEffectingUpon.failure ?
                            "(~"+ PLP_Converter.GetActionSuccessIntermName(plp)+")^" : "";
            if(plpEffect.effectType == EEffectType.Conditional) {
                ConditionalEffect conEffect = (ConditionalEffect)plpEffect.effect;
                second.append("if(" + sEffectingUpon +
                                "(" + PLP2RDDL_Utils.GetExistsForPredicate(plp.GetParams(), assignToVar.Params) +
                                " (" + plp.PlpNameWithParams(false) + "))^" +
                        "(" + conEffect.condition.condition.getConditionForIf(assignToVar.Params, false, IsAssignToObservationVariable) +
                        ")) then " +
                        conEffect.getEffectAssignedValue() +
                        " else ");
            }else {
                second.append("if(" + sEffectingUpon +
                        "(" + PLP2RDDL_Utils.GetExistsForPredicate(plp.GetParams(), assignToVar.Params) +
                        " (" + plp.PlpNameWithParams(false) + "))) then " +
                        plpEffect.effect.getEffectAssignedValue() +
                        " else ");
            }
            PLP2RDDL_Utils.ParametersNameToOriginal(plp.GetParams());
            plp.PlpNameWithParams(false);
        });

        result.Second = second.toString();
        return result;
    }

//    public static Triplet<String> Get_CPFS_AssignLine(AssignmentEffect assignmentEffect, HashMap<PLP, AssignmentEffect> effectsByPLP)
//    {
//        Triplet result = new Triplet();
//        result.First = assignmentEffect.Param_Predicate.baseToString(true, true) + "'=";
//        result.Third = assignmentEffect.Param_Predicate.Name + ";";
//
//        StringBuilder second = new StringBuilder();
//
//        effectsByPLP.forEach((plp,plpEffect)->
//        {
//            String sEffectingUpon = plpEffect.getEffectingUpon() == IEffect.EEffectingUpon.success ?
//                    "("+PLP_Converter.GetActionSuccessIntermName(plp)+")^" :
//                    plpEffect.getEffectingUpon() == IEffect.EEffectingUpon.failure ?
//                            "(~"+PLP_Converter.GetActionSuccessIntermName(plp)+")^" : "";
//
//            second.append("if(" + sEffectingUpon +
//                    "(" + GetExistsForPredicate(plp.GetParams()) +
//                    " (" + plp.PlpNameWithParams(false) + "))) then " +
//                    plpEffect.getEffectAssignedValue() +
//                    " else ");
//            plp.PlpNameWithParams(false);
//        });
//
//        result.Second = second.toString();
//        return result;
//    }


    //Predicate flPredicate:the fluent to change, HashMap<PLP, ArrayList<Effect>> effectsByPL:all effects for that fluent by plp
//    public static  Triplet Get_CPFS_PredicateLine(Predicate effectedPredicate, HashMap<PLP, ArrayList<Effect>> conditionPredicatesByPLP)
//    {
//        Triplet result = new Triplet();
//        //HashMap<Predicate param index, HashMap<plpName, <matching param in action param>>>
//        HashMap<Integer, HashMap<PLP, HashSet<Integer>>> PredicateParamMatchesToActionParam = new HashMap<>();
//
//        HashMap<PLP, HashSet<Integer>> plpParamsNotInPredicate = new HashMap<>();
//
//        conditionPredicatesByPLP.forEach((plp, effects) ->
//        {
//            effects.forEach(effect ->
//            {
//                if (effect.effectType == EEffectType.Predicate || effect.effectType == EEffectType.Not) {
//                    Predicate plpPredicate = effect.effectType == EEffectType.Predicate ? (Predicate) effect.effect
//                            : ((NotEffect) effect.effect).Predicate;
//                    if (effectedPredicate.Name.equals(plpPredicate.Name)) {
//                        for (int j = 0; j < plp.GetParams().length; j++) {
//                            boolean hasMatch = false;
//                            for (int i = 0; i < plpPredicate.Params.size(); i++) {
//                                if (plpPredicate.Params.get(i).getName().equals(plp.GetParams()[j].getName())) {
//                                    if (!PredicateParamMatchesToActionParam.containsKey(i)) {
//                                        PredicateParamMatchesToActionParam.put(i, new HashMap<>());
//                                    }
//                                    if (!PredicateParamMatchesToActionParam.get(i).containsKey(plp)) {
//                                        PredicateParamMatchesToActionParam.get(i).put(plp, new HashSet<>());
//                                    }
//                                    hasMatch = true;
//                                    PredicateParamMatchesToActionParam.get(i).get(plp).add(j);
//                                }
//                            }
//                            if (!hasMatch) {
//                                if (!plpParamsNotInPredicate.containsKey(plp))
//                                    plpParamsNotInPredicate.put(plp, new HashSet<>());
//                                plpParamsNotInPredicate.get(plp).add(j);
//                            }
//                        }
//                    }
//                }
//            });
//        });
//
//
//        //save original params: we need to return original names for next predicate
//        HashMap<PLP, ArrayList<String>> originalPlpParamNames = new HashMap<>();
//        conditionPredicatesByPLP.keySet().forEach(plp->
//        {
//            if(!originalPlpParamNames.containsKey(plp))originalPlpParamNames.put(plp, new ArrayList<>());
//            for(PlanningTypedParameter par: plp.GetParams())
//            {
//                originalPlpParamNames.get(plp).add(par.getName());
//            }
//        });
//
//        //set the same name for fluent predicate and all matching plp action params
//        LambdaCounter counter = new LambdaCounter();
//        PredicateParamMatchesToActionParam.forEach((key, value)->
//        {
//            counter.Increment();
//            String parName = effectedPredicate.Params.get(key).getName() + (counter.getCounter());
//            effectedPredicate.Params.get(key).setName(parName);
//            effectedPredicate.Params.get(key).setName(parName);
//            value.forEach((plp, indexes)->
//            {
//                indexes.forEach(index->
//                {
//                    plp.GetParams()[index].setName(parName);
//                });
//            });
//        });
//
//        result.First = effectedPredicate.baseToString(true,  true) + "=";
//        result.Third = effectedPredicate.baseToString(false,  true) + ";";
//        StringBuilder second = new StringBuilder();
//
//
//        conditionPredicatesByPLP.forEach((plp, plpEffects)->
//        {
//            //if the predicate effect is from type predicate then it is a 'true' effect (not like if it was a 'Not' effect)
//            boolean isTrueEffect = conditionPredicatesByPLP.get(plp).stream().filter(effect->
//                    effect.effectType == EEffectType.Predicate && ((Predicate)effect.effect).Name.equals(effectedPredicate.Name))
//                    .collect(Collectors.toList()).size() > 0;
//
//            IEffect ieffect = plpEffects.get(0).effect;
//            String sEffectingUpon = ieffect.getEffectingUpon() == IEffect.EEffectingUpon.success ?
//                    "("+PLP_Converter.GetActionSuccessIntermName(plp)+")^" :
//                    ieffect.getEffectingUpon() == IEffect.EEffectingUpon.failure ?
//                            "(~"+PLP_Converter.GetActionSuccessIntermName(plp)+")^" : "";
//
//            second.append("if(" + sEffectingUpon +
//                    "(" + PLP2RDDL_Utils.GetExistsForPredicate(plp.GetParams(), plpParamsNotInPredicate.get(plp)) +
//                    " (" + plp.PlpNameWithParams(false) + "))) then " +
//                    (isTrueEffect ? " true " : " false ") +
//                    " else ");
//            plp.PlpNameWithParams(false);
//        });
//
//
//        //restore original plp param names
//        originalPlpParamNames.forEach((plp, originalParamValues)->
//        {
//            for(int i=0; i < plp.GetParams().length; i++)
//            {
//                plp.GetParams()[i].setName(originalParamValues.get(i));
//            }
//        });
//
//        result.Second = second.toString();
//        return result;
//    }


}