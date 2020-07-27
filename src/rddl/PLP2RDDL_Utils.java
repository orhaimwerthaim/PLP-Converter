package rddl;

import plp.PLP;
import plp.objects.EOperator;
import plp.objects.PlanningTypedParameter;
import plp.objects.Predicate;
import plp.objects.condition.AndOrCondition;
import plp.objects.condition.Condition;
import plp.objects.condition.ExistsCondition;
import plp.objects.condition.NotCondition;
import utils.KeyValuePair;
import utils.LambdaCounter;

import java.util.*;

public class PLP2RDDL_Utils {
    //recursive function receives the condition c and a new clean Array list.
    //the function will fill the Array list
    public static ArrayList<Predicate> GetConditionPredicates(Condition c)
    {
        ArrayList<Predicate> preds = new ArrayList<>();
        GetConditionPredicates(c, preds);
        return preds;

    }
    /*
    @parameter ArrayList<Predicate> predicates: this method will fill it with the condition predicates
     */
    public static void GetConditionPredicates(Condition c, ArrayList<Predicate> predicates)
    {
        switch (c.ConditionType)
        {
            case And:
            case Or:
                for(Condition con: ((AndOrCondition) c.condition).conditions)
                {
                    GetConditionPredicates(con, predicates);
                }
                break;
            case Not:
                GetConditionPredicates(((NotCondition) c.condition).Condition, predicates);
                break;
            case Exists:
                GetConditionPredicates(((ExistsCondition) c.condition).condition, predicates);
                break;
            case Predicate:
                predicates.add(((Predicate) c.condition));
            default: return;
        }
    }

    public static void PredicatesNameChangeTo(List<Predicate> predicates)
    {
        predicates.forEach(pred-> pred.Params.forEach(par-> par.setName(par.ChangeNameTo)));
    }

    public static void ParametersNameChangeTo(PlanningTypedParameter[] params)
    {
        for(PlanningTypedParameter par: params)
        {
            par.setName(par.ChangeNameTo);
        }
    }

    public static void ParametersNameToOriginal(PlanningTypedParameter[] params)
    {
        for(PlanningTypedParameter par: params)
        {
            par.setName(par.getOriginalName());
        }
    }

    public static void PredicatesNameToOriginal(List<Predicate> predicates)
    {
        predicates.forEach(pred-> pred.Params.forEach(par-> par.setName(par.getOriginalName())));
    }

    //return predicate left part of CPFS
    public static  Predicate SetPredicateParameterNamesAndTypes(HashMap<KeyValuePair<Predicate, PLP>, ArrayList<Predicate>> conditionPredByEffectPreded)
    {
        if(conditionPredByEffectPreded.size() == 0)return null;
        Predicate effectedPredForOutput = ((KeyValuePair<Predicate, PLP>)conditionPredByEffectPreded.keySet().toArray()[0]).Key;
        HashMap<PlanningTypedParameter, ArrayList<PlanningTypedParameter>> effectedParamLinkedToConditionParam = new HashMap<>();
        HashMap<PLP, HashSet<PlanningTypedParameter>> actionParamNotLinked = new HashMap<>();

        //HashMap<plp action param, HashMap<plp, <params in plp predicates with same name>>>
        HashMap<KeyValuePair<PLP,PlanningTypedParameter>, ArrayList<PlanningTypedParameter>> plpActionParamToPlpConditionPredicateParam = new HashMap<>();

        HashSet<PlanningTypedParameter> linkedToEffectPar = new HashSet<>();
        ArrayList<PlanningTypedParameter> conditionParamsNotLinked= new ArrayList<>();

        //connecting plp Action parameters to the plp predicate parameter if they have the same name
        //saving plp predicate parameters not linked to any action parameter
        conditionPredByEffectPreded.forEach((key, conditionPredicates) ->
        {
            Predicate effectedPred = key.Key;
            PLP plp = key.Value;

            //marking (not)linked PLP action parameters
            for (int j = 0; j < plp.GetParams().length; j++) {
                boolean notLinked = true;
                PlanningTypedParameter conditionPar = plp.GetParams()[j];
                for (int i = 0; i < effectedPred.Params.size(); i++) {
                    PlanningTypedParameter effectedPar = effectedPred.Params.get(i);
                    PlanningTypedParameter effectedOutPar = effectedPredForOutput.Params.get(i);
                    if (conditionPar.getName().equals(effectedPar.getName())) {
                        notLinked = false;
                        if (!(effectedParamLinkedToConditionParam.containsKey(effectedOutPar)))
                            effectedParamLinkedToConditionParam.put(effectedOutPar, new ArrayList<>());

                        effectedParamLinkedToConditionParam.get(effectedOutPar).add(conditionPar);
                    }
                }
                if (notLinked) {
                    if (!(actionParamNotLinked.containsKey(plp))) actionParamNotLinked.put(plp, new HashSet<>());
                    actionParamNotLinked.get(plp).add(conditionPar);
                }
            }
            conditionPredicates.forEach(predeicate->
            {
                for (int j = 0; j < predeicate.Params.size(); j++) {
                    PlanningTypedParameter conditionPar = predeicate.Params.get(j);
                    for (int i = 0; i < effectedPred.Params.size(); i++) {
                        PlanningTypedParameter effectedPar = effectedPred.Params.get(i);
                        PlanningTypedParameter effectedOutPar = effectedPredForOutput.Params.get(i);
                        if (conditionPar.getName().equals(effectedPar.getName())) {
                            if (!(effectedParamLinkedToConditionParam.containsKey(effectedOutPar)))
                                effectedParamLinkedToConditionParam.put(effectedOutPar, new ArrayList<>());

                            effectedParamLinkedToConditionParam.get(effectedOutPar).add(conditionPar);
                            linkedToEffectPar.add(conditionPar);
                        }
                    }
                }
            });

            //marking condition predicate param linked only to plp action param (not to effected param)
            KeyValuePair<PLP, PlanningTypedParameter> tempCondPar = new KeyValuePair<>();
            tempCondPar.Key = plp;
            for (int i = 0; i < conditionPredicates.size(); i++) {
                boolean linked = false;
                Predicate condPred = conditionPredicates.get(i);
                for (int z = 0; z < condPred.Params.size(); z++) {
                    PlanningTypedParameter condPar = condPred.Params.get(z);
                    for (int j = 0; j < plp.GetParams().length; j++) {
                        PlanningTypedParameter conditionActionPar = plp.GetParams()[j];
                        if (actionParamNotLinked.size()== 0 || !actionParamNotLinked.get(plp).contains(conditionActionPar)) continue;
                        if (conditionActionPar.getName().equals(condPar.getName())) {
                            linked = true;
                            tempCondPar.Value = conditionActionPar;
                            if (!(plpActionParamToPlpConditionPredicateParam.containsKey(tempCondPar)))
                                plpActionParamToPlpConditionPredicateParam.put(new KeyValuePair<>(plp, conditionActionPar), new ArrayList<>());
                            plpActionParamToPlpConditionPredicateParam.get(tempCondPar).add(condPar);
                        }
                    }
                    if (!linked && !linkedToEffectPar.contains(condPar)) {
                        conditionParamsNotLinked.add(condPar);
                    }
                }
            }
        });

        LambdaCounter counter = new LambdaCounter(1);
        effectedParamLinkedToConditionParam.forEach((par, linkedParams)->
        {

            par.ChangeNameTo = par.Type + counter.getCounter();
            linkedParams.forEach(p->
            {
                p.ChangeNameTo = par.ChangeNameTo;
            });
            counter.Increment();
        });

        plpActionParamToPlpConditionPredicateParam.forEach((key, params)->{
            key.Value.ChangeNameTo = key.Value.getName() + counter.getCounter();
            params.forEach(par->{
                par.ChangeNameTo = key.Value.ChangeNameTo;
            });
            counter.Increment();
        });

        actionParamNotLinked.forEach((plp, params)->
                params.forEach(par-> {
                    par.ChangeNameTo = par.getParamType() + counter.getCounter();
                    counter.Increment();
                }));

        conditionParamsNotLinked.forEach(par-> {
            par.ChangeNameTo = par.getParamType() + counter.getCounter();
            counter.Increment();
        });
        for(Object entry : conditionPredByEffectPreded.keySet().toArray())
        {
            Predicate pred = ((KeyValuePair<Predicate, PLP>)entry).Key;
            for(int i = 0; i < pred.Params.size(); i++)
            {
                pred.Params.get(i).ChangeNameTo = effectedPredForOutput.Params.get(i).ChangeNameTo;
            }
        }

        return effectedPredForOutput;
    }
    public static String GetExistsForPredicate(PlanningTypedParameter[] paramsOfVar, ArrayList<PlanningTypedParameter> effectParams)
    {
        paramsOfVar = Arrays.stream(paramsOfVar).filter(var-> !var.getName().startsWith("$")).toArray(size -> new PlanningTypedParameter[size]);
        HashSet<Integer> indexes = new HashSet<>();
        for(int j = 0; j < paramsOfVar.length; j++)
        {
            PlanningTypedParameter p = paramsOfVar[j];
            boolean hasMatch = false;
            for(int i = 0; i < effectParams.size(); i++)
            {
                PlanningTypedParameter par = effectParams.get(i);
                if(par.getName().equals(p.getName()))
                {
                    hasMatch = true;
                    break;
                }
            }
            if(!hasMatch)
            {
                indexes.add(j);
            }
        }
        return GetExistsForPredicate(paramsOfVar, indexes, EOpType.exists);
    }

    public static String GetForAllString(PlanningTypedParameter[] paramsOfVar)
    {
        return GetExistsForPredicate(paramsOfVar, null, EOpType.for_all);
    }

    public static String GetSumString(PlanningTypedParameter[] paramsOfVar)
    {
        return GetExistsForPredicate(paramsOfVar, null, EOpType.sum);
    }

    public static String GetExistsForPredicate(PlanningTypedParameter[] paramsOfVar)
    {
        return GetExistsForPredicate(paramsOfVar, (HashSet<Integer>) null, EOpType.exists);
    }
    private static String GetExistsForPredicate(PlanningTypedParameter[] paramsOfVar, HashSet<Integer> indexes, EOpType eOpType)
    {//exists_{?loc : discrete_location,?o : obj}
        String op= "";
        switch (eOpType)
        {
            case sum:
                op= "sum_";
                break;
            case exists:
                op= "exists_";
                break;
            case for_all:
                op= "forall_";
                break;
                default:throw new UnsupportedOperationException();
        }

        if(indexes == null)//we want for all
        {
            indexes = new HashSet<>();
            for(int i=0; i < paramsOfVar.length; i++)
            {
                indexes.add(i);
            }
        }else if(indexes.size() == 0)return "";

        StringBuilder result = new StringBuilder(op + "{");

        LambdaCounter counter = new LambdaCounter();
        indexes.forEach(index->
        {
            result.append(counter.getCounter() == LambdaCounter.COUNTER_INIT ? "" : ", ");
            counter.Increment();
            result.append(paramsOfVar[index].getName()+" : " + paramsOfVar[index].Type);

        });
        result.append("}");
        return result.toString();
    }
    enum EOpType
    {
        for_all,
        exists,
        sum
    }
}
