package plp2java;

import plp.PLP;
import plp.EnvironmentFile;
import plp.objects.EConditionType;
import plp.objects.PlanningTypedParameter;
import plp.objects.Predicate;
import plp.objects.condition.AndOrCondition;
import plp.objects.condition.Condition;
import rddl.PLP2RDDL_Utils;
import utils.KeyValuePair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

public class ValidActionsMethodWriter {
    public static String GetActionConstraintLine(PLP plp)
    {
        AndOrCondition andCondition = new AndOrCondition(AndOrCondition.EAndOrCondition.And);
        Condition condition = new Condition();
        condition.condition = andCondition;
        condition.ConditionType = EConditionType.And;

        andCondition.conditions = plp.preconditions.Conditions;
        ArrayList<Predicate> conditionPredicates = new ArrayList<>();
        conditionPredicates.addAll(PLP2RDDL_Utils.GetConditionPredicates(condition));

        HashMap<KeyValuePair<Predicate, PLP>, ArrayList<Predicate>> preds = new HashMap<>();
        Predicate pred = new Predicate();
        pred.Name = plp.getName();
        pred.Params = new ArrayList<>(Arrays.asList(plp.GetParams()));

        KeyValuePair<Predicate, PLP> key = new KeyValuePair<>(pred, plp);
        preds.put(key, conditionPredicates);
        PLP2RDDL_Utils.SetPredicateParameterNamesAndTypes(preds);

        //PlanningTypedParameter[] plpParams = plp.GetParams();

        PLP2RDDL_Utils.PredicatesNameChangeTo(conditionPredicates);
        pred.Params.forEach(par-> par.setName(par.ChangeNameTo));

        //find missing params from plp action paremeters that are not in conditionPredicates params
        ArrayList<PlanningTypedParameter> missingPar = new ArrayList<>();
        for(int z=0; z< plp.GetParams().length;z++) {
            PlanningTypedParameter p = plp.GetParams()[z];
            int indexInHisType = 1;
            for (PlanningTypedParameter pt:
                    plp.GetParams()) {
                if(p.getName().equals(pt.getName()))break;
                if(p.Type.equals(pt.Type))indexInHisType++;
            }
            boolean inList = false;
            HashSet<String> matched = new HashSet<>();
            for(int i=0; i< conditionPredicates.size();i++)
            {
                int countOfType = 0;
                for (PlanningTypedParameter ppt:
                        conditionPredicates.get(i).GetParams()) {
                    if(ppt.Type.equals(p.Type))countOfType++;
                }
                for(int j=0; j < conditionPredicates.get(i).Params.size(); j++)
                {
                    PlanningTypedParameter pp = conditionPredicates.get(i).Params.get(j);
                    if(pp.Type == null)
                    {
                        try {
                            throw new Exception("'" + conditionPredicates.get(i).Name + "' is used as a pre-condition but never defined ('"+plp.getName() +".xml')");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    if(pp.Type.equals(p.Type) && matched.add(i +","+j) && countOfType >= indexInHisType)
                    {
                        inList=true;
                        break;
                    }
                }
                if(inList)break;
            }
            if(!inList)
            {
                missingPar.add(p);
            }
        }

        ArrayList<PlanningTypedParameter> distinctPar = new ArrayList<>();
        HashSet<String> distinct = new HashSet<>();
        conditionPredicates.forEach(cpred->
                cpred.Params.forEach(par-> {
                    if(distinct.add(par.getName())) {
                        distinctPar.add(par);
                    }
                }));
        missingPar.forEach(par1-> {if(distinct.add(par1.getName())) {
            distinctPar.add(par1);}});
        PlanningTypedParameter[] allPars = distinctPar
                .toArray(new PlanningTypedParameter[distinctPar.size()]);


        //get parameters not in plp action parameters (they should by in 'exists_' block not in 'forall_' block)
        //ArrayList<PlanningTypedParameter> notInActionParams

        String sForAll = PLP2RDDL_Utils.GetForAllString(allPars);
        StringBuilder constraint = new StringBuilder(sForAll);
        constraint.append("[");
        constraint.append(plp.PlpNameWithParams(pred.Params) + "=>"); //distinctPar)+ "=>");
        constraint.append(condition.condition.getConditionForIf(distinctPar, false, false));
        constraint.append("];");
        PLP2RDDL_Utils.PredicatesNameToOriginal(conditionPredicates);

        return constraint.toString();
    }

    public static String getPLPValidActionsForStateMethod(PLP plp, EnvironmentFile pf)
    {

        String getAllActions = "";
        String arrayParts = "";
        for (int i = 0; i < plp.GetParams().length; i++) {
            String comma = i == 0 ? "" : ",";
            getAllActions += "        for (String p" + i + " :\r\n                " + plp.GetParams()[i].Type + ") {\r\n";
            arrayParts += comma + "p" + i;
        }
        getAllActions += "                    actions.add(new Pair<>(\"" + plp.getName() +
                "\", new String[]{" + arrayParts + "}));\r\n";

        for (int i = 0; i < plp.GetParams().length; i++) {
            getAllActions += "                }\r\n";
        }
        getAllActions += "\r\n";

        String str = "\r\n    private static ArrayList<Pair<String,String[]>> "+
                MicroState_Writer.getValidActionsMethodNameByPLP(plp)+
                "(MicroStateState ms)\r\n    {\r\n        ArrayList<Pair<String,String[]>> validActions = new ArrayList<>();\r\n"+
                "        for (ParameterizedVar<Boolean> var:\r\n                ms.near) {\r\n            if(!var.value)continue;\r\n            String rob = var.params.get(0);\r\n            String location = var.params.get(1);\r\n            for (String o:\r\n                    obj) {\r\n                validActions.add(new Pair<>(\"observe_can\", new String[]{rob, location,o}));\r\n            }\r\n        }\r\n        return validActions;\r\n    }\r\n";
        return str;
    }


}
