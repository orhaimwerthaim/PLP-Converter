package plp2java;

import jdk.jshell.execution.Util;
import plp.PLP;
import plp.PLP_Achieve;
import plp.PLP_Observe;
import plp.ProblemFile;
import plp.objects.PlanningStateVariable;
import plp.objects.PlanningTypedParameter;
import plp.objects.effect.ConditionalEffect;
import plp.problem_file_objects.InitialStateOption;
import plp.problem_file_objects.StateVariableWithValue;
import plp2java.plp2javaUtils.CPFS_Line_ForStateVariables_JAVA;
import plp2java.plp2javaUtils.PLP2JavaUtils;
import plp2java.plp2javaUtils.StateActionConstraintsForJava;
import rddl.CPFS_Line_ForStateVariables;
import utils.KeyValuePair;
import utils.Triplet;
import utils.Utils;

import java.net.URISyntaxException;
import java.util.*;

public class MicroState_Writer {
    String objectsVarDefinition = "";
    String constantsVarDefinition = "";
    String objectsVarInit = "";
    String constantsVarInit = "";
    String stateVarInit = "";
    String randomOptionWeights = "";
    int numOfinitStateOptions;
    String getAllActions = "";
    String getValidActionsPerStateMethods = "";
    String addValidPlpActionToValidStateActions = "";
    String calcRewardsSection = "";
    String observationCalcMethods = "";
    String calcObservationSection = "";
String calcIntermediateSection = "";
String intermediateCalcMethods = "";
String calcNextStateVariables = "";
    String nextStateForVariableMethods = "";
String nextGoalReachedMethod = "";


    public void initNextGoalReachedMethod(ArrayList<PLP> plps, ProblemFile pf) {
        nextGoalReachedMethod += "\n" +
                "    private static boolean getGoal_reached(MicroStateState state, MicroStateIntermediate interm, MicroStateState next, ArrayList<Pair<String, String[]>> actions) {\n" ;
        ConditionalEffect condEffect = ((ConditionalEffect)pf.GoalReachedEffect.effect);
        ArrayList<PlanningTypedParameter> params = condEffect.condition.condition.GetParams();
        HashSet<String> unique = new HashSet<>();
        ArrayList<PlanningTypedParameter> forAllParam = new ArrayList<>();
        params.forEach(x->
        {
            if(unique.add(x.getName()) && x.getName().startsWith("?"))
            {
                forAllParam.add(x);
            }
        });

        nextGoalReachedMethod += PLP2JavaUtils.GetForAllString(forAllParam);
        nextGoalReachedMethod += "        if (" +
                condEffect.condition.condition.getConditionForIf_JavaNextState(forAllParam,false,true) +
        ")\n" +
        "return true;\n";//"return true;\n}\n";
        nextGoalReachedMethod += PLP2JavaUtils.GetForAllStringClose(forAllParam.size());
        //nextGoalReachedMethod += PLP2JavaUtils.GetForAllStringClose(forAllParam.size()-1);
        nextGoalReachedMethod += "        return false;\n" +
                "    }\n";

    }


    private String getCalcNextStateMethodName(String varName)
    {
        return "getNext" + varName;
    }


    private void initNextStateForVarMethod(ArrayList<PLP> plps, ProblemFile pf)
    {
        for (PlanningStateVariable stateVar : pf.StateVariables) {
            if( stateVar.IsConstant || stateVar.IsObservation || stateVar.IsGlobalIntermediate)continue;
            nextStateForVariableMethods += "\n    private static boolean "+getCalcNextStateMethodName(stateVar.Name)+"(ParameterizedVar<Boolean> var, ArrayList<Pair<String,String[]>> actions, MicroStateState state, MicroStateIntermediate interm) {\n";

            String line = CPFS_Line_ForStateVariables_JAVA.GetCPFS_LineForStateVar(plps, stateVar.Name, pf);
            nextStateForVariableMethods += (line != null ? line :  "    return var.value;") + "\n    }\n";
            //line != null ? line :  "    return var.value;";
            int i=8;
        }

    }

    public MicroState_Writer(ProblemFile pf, ArrayList<PLP> plps) throws Exception {
        numOfinitStateOptions = pf.initalState.InitialStateOptions.size();
        initNextGoalReachedMethod(plps,pf);
        initNextStateForVarMethod(plps,pf);
        initCalcNextStateVariables(plps,pf);
        initObjectsVarDefinition(pf);
        initObjectsVarInit(pf);
        initConstantsVarDefinition(pf);
        initConstantsVarInit(pf);
        initInitialState(pf);
        InitRandomOption(pf);
        initIntermediateCalcMethods(plps);
        initGellAllActions(plps);
        initObservationCalcMethods(plps);
        initAddValidPlpActionToValidStateActions(plps);
        initCalcRewardSection(plps, pf);
        initCalcObservationSection(plps);
        initCalcIntermediates(plps);
        for(PLP plp:plps)
        {
            getValidActionsPerStateMethods += GetPLPValidActionsMethod(plp);
        }
        FileWriter.WriteToFile("MicroState.java", this.toString());
    }

    @Override
    public String toString() {
        String str="package JavaSim2POMCP.POMCP.JavaGeneratos.Generated;\r\n\r\nimport JavaSim2POMCP.POMCP.JavaGeneratos.fixed.ParameterizedVar;\r\nimport java.util.function.IntConsumer;\r\nimport JavaSim2POMCP.POMCP.POMCP;\r\nimport JavaSim2POMCP.POMCP.UtilsClass.Pair;\r\n\r\nimport java.util.*;\r\nimport java.util.stream.Collectors;\r\n\r\npublic class MicroState {\r\n    public int reward;\r\n"+
                objectsVarDefinition + "\r\n    public static MicroStateIntermediate interm = new MicroStateIntermediate();\r\n\r\n"+
                constantsVarDefinition + "    public MicroStateObservation observation;\r\n    public MicroStateState state;\r\n\r\n    static Random rand = new Random();\r\n\r\n    static\r\n    {\r\n"+
                objectsVarInit + constantsVarInit + "    }\r\n\r\n    //Pair<String:'var name',String:'var type, can be bool/int'>\r\n"+
                "\r\n    private int GetRandomOption()\r\n    {\r\n        float[] "+
                "options = new float[]{"+randomOptionWeights+"};\r\n        Random rand = new Random();\r\n        float fRand = rand.nextFloat();\r\n        float current = 0.0f;\r\n        for(int i=0;;i++)\r\n        {\r\n            if(current <= fRand && fRand <= options[i] + current)\r\n            {\r\n                return i;\r\n            }\r\n            current += options[i];\r\n        }\r\n    }\r\n" +
                "    public MicroState(boolean initState) {\r\n        state = new MicroStateState();\r\n        observation = new MicroStateObservation();\r\n\r\n        "+
                "if (initState) {\r\n"+
                "            int numOfOptions = "+numOfinitStateOptions+";\r\n"+
                "            int selectedOpt= numOfOptions > 0 ? GetRandomOption() : -1;\r\n"+
                stateVarInit +
                "        }\r\n    }\r\n\r\n    public static MicroState NextState(MicroStateState state, ArrayList<Pair<String,String[]>> actions)\r\n    {\r\n        MicroState next = new MicroState(false);\r\n        MicroStateIntermediate interm = CalcIntermediate(state, actions);\r\n        next.state = CalcNextStateVariables(state, interm, actions);\r\n        next.observation = CalcObservation(state, interm, next.state, actions);\r\n        next.reward = CalcReward(state, interm, next.observation, next.state, actions);\r\n        return next;\r\n    }\r\n\r\n"+
                "    public static ArrayList<Pair<String,String[]>> GetAllPossibleActions() {\r\n        ArrayList<Pair<String, String[]>> actions = new ArrayList<>();\r\n\r\n        ArrayList<Pair<String, String[]>> validActions = new ArrayList<>();\r\n"+
                getAllActions + "        return actions;\r\n        }\r\n" +
                getValidActionsPerStateMethods +
                "    public static ArrayList<Pair<String,String[]>> GetStateActions(MicroStateState ms) {\r\n        ArrayList<Pair<String, String[]>> validActions = new ArrayList<>();\r\n"+
                addValidPlpActionToValidStateActions +
                "        return validActions;\r\n    }\r\n\r\n    public static String ActionToString(Pair<String,String[]> action)\r\n    {\r\n        String params = action._o2.length == 0 ? \"\" : (\"(\" +\r\n                Arrays.stream(action._o2)\r\n                        .collect(Collectors.joining(\",\")) + \")\");\r\n        return action._o1 + params;\r\n    }\r\n\r\n"+
                calcRewardsSection +
                calcObservationSection +
                observationCalcMethods +
                calcIntermediateSection +
                intermediateCalcMethods +
                calcNextStateVariables +
                nextStateForVariableMethods +
                nextGoalReachedMethod +
                "}\r\n";
        return str;
    }


    private void initCalcNextStateVariables(ArrayList<PLP> plps, ProblemFile pf) {
        calcNextStateVariables += "\n" +
                "    private static MicroStateState CalcNextStateVariables(MicroStateState state, MicroStateIntermediate interm, ArrayList<Pair<String,String[]>> actions)\n" +
                "    {\n" +
                "        MicroStateState next = new MicroStateState();\n";
        for (PlanningStateVariable var : pf.StateVariables) {
            if (var.IsConstant || var.IsObservation || var.IsGlobalIntermediate) continue;

            calcNextStateVariables += "        for (ParameterizedVar<Boolean> " + var.Name + "0:\n" +
                    "                state." + var.Name + ") {\n" +
                    "            boolean o = " + getCalcNextStateMethodName(var.Name) + "("
                    + var.Name + "0, actions, state, interm);\n" +
                    "            next." + var.Name + ".add(new ParameterizedVar<Boolean>(" + var.Name +
                    "0.params, o));\n" +
                    "        }\n" +
                    "\n";
        }
        calcNextStateVariables += "        return next;\n" +
                "    }\n" +
                "\n";

    }

    private void initIntermediateCalcMethods(ArrayList<PLP> plps) {
        for (PLP plp : plps) {

            String successProbability = plp instanceof PLP_Observe ?
                    Utils.DecimalToString(1-((PLP_Observe )plp).failure_to_observe_probability.GetConditionalProbabilities()[0].Probability) :
                    plp instanceof PLP_Achieve ?
                            Utils.DecimalToString(((PLP_Achieve )plp).successFailProbability.GetConditionalProbabilities()[0].Probability)
                            :"";

            intermediateCalcMethods += "\n" +
                    "    private static boolean " + getSuccessIntermediateMethodName(plp) +
                    "(ArrayList<Pair<String,String[]>> actions, MicroStateState currentState) {\n" +
                    "        for (Pair<String,String[]> action : actions) {\n" +
                    "            if(!action._o1.equals(\"" + plp.getName() + "\"))continue;\n" +
                    "\n" +
                    "                return rand.nextFloat() < " + successProbability + ";\n" +
                    "            }\n" +
                    "        return false;\n" +
                    "    }\n" +
                    "\n";
        }
    }


    public static String getSuccessIntermediateMethodName(PLP plp)
    {
        return "getIntsuccess_"+plp.getName();
    }

    private void initCalcIntermediates(ArrayList<PLP> plps) {
        calcIntermediateSection += "\n" +
                "    private static MicroStateIntermediate CalcIntermediate(MicroStateState currentState, ArrayList<Pair<String,String[]>> actions) {\n" +
                "        MicroStateIntermediate interm = new MicroStateIntermediate();\n";
        for (PLP plp : plps) {
            calcIntermediateSection += "        interm.success_" + plp.getName() + " = " +
                    getSuccessIntermediateMethodName(plp) + "(actions, currentState);\n";
        }
        calcIntermediateSection += "        return interm;\n" +
                "    }\n" +
                "\n";
    }

    private void initGellAllActions(ArrayList<PLP> plps)
    {
        for (PLP plp: plps) {
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
        }
    }

    private void initCalcObservationSection(ArrayList<PLP> plps) {
        calcObservationSection += "\n" +
                "    \n" +
                "    private static MicroStateObservation CalcObservation(MicroStateState state, MicroStateIntermediate interm, MicroStateState next, ArrayList<Pair<String,String[]>> actions)\n" +
                "    {\n" +
                "        MicroStateObservation observation = new MicroStateObservation();\n" +
                "        observation.goal_reached = getGoal_reached(state, interm, next, actions);\n";

        for (PLP plp : plps
        ) {
            calcObservationSection += "        observation.obsrv_" + plp.getName() + " = " + getCalcObservationMethodNameByPLP(plp) + "(state, interm, next, actions);\n";
        }

        calcObservationSection += "        return observation;\n" +
                "    }\n" +
                "\n";
    }

    public static String getCalcObservationMethodNameByPLP(PLP plp)
    {
        return "getObsrv_"+plp.getName()+"";
    }

    public void initObservationCalcMethods(ArrayList<PLP> plps)
    {
        for (PLP plp:plps) {
            if(plp instanceof PLP_Achieve)
            {
                observationCalcMethods += "\n" +
                        "    private static int "+getCalcObservationMethodNameByPLP(plp)+"(MicroStateState state, MicroStateIntermediate interm, MicroStateState next, ArrayList<Pair<String,String[]>> actions)\n" +
                        "    {\n" +
                        "        return interm.success_"+plp.getName()+" ? 1:0;\n" +
                        "    }\n";
            }

            if(plp instanceof PLP_Observe) {
                PLP_Observe pO = (PLP_Observe) plp;
                observationCalcMethods += "\n" +
                        "    private static int " + getCalcObservationMethodNameByPLP(plp) + "(MicroStateState state, MicroStateIntermediate interm, MicroStateState next, ArrayList<Pair<String,String[]>> actions)\n" +
                        "    {\n" +
                        "        if(!interm.success_" + plp.getName() + ")return 0;\n" +
                        "        int result = 0;\n" +
                        "        result += interm.success_" + plp.getName() + " ? 1 : 0;\n" +
                        "        boolean exists = false;\n" +
                        "        for (Pair<String,String[]> action : actions) {\n" +
                        "            if (!action._o1.equals(\"" + plp.getName() + "\")) continue;\n";

                //match Observe_PLP goal predicate parameters to action parameters by param name
                HashMap<Integer, Integer> observePredicateParamIndexToActionParIndex = new HashMap<>();
                for (int i = 0; i < pO.observationGoalPredicate.Params.size(); i++) {
                    PlanningTypedParameter goalPredicateParam = pO.observationGoalPredicate.Params.get(i);
                    for (int j = 0; j < plp.GetParams().length; j++) {
                        PlanningTypedParameter actionParam = plp.GetParams()[j];
                        if (goalPredicateParam.getName_Java().equals(actionParam.getName_Java())) {
                            observePredicateParamIndexToActionParIndex.put(i, j);
                        }
                    }
                }

                for (Map.Entry<Integer, Integer> entry : observePredicateParamIndexToActionParIndex.entrySet()) {
                    Integer goalPredicateParamIndex = entry.getKey();
                    Integer actionParamIndex = entry.getValue();
                    observationCalcMethods += "            String " +
                            pO.observationGoalPredicate.Params.get(goalPredicateParamIndex).getName_Java() +
                            " = action._o2[" + actionParamIndex + "];\n";
                }

                String predName = pO.observationGoalPredicate.Name;
                PlanningStateVariable stateVar = PLP2JavaUtils.GetStateVariableByName(predName);
                observationCalcMethods += "\n" +
                        "            if (interm.success_" + plp.getName() + ") {\n";
                if(stateVar.IsGlobalIntermediate)
                {
                    observationCalcMethods += "                            if(interm."+predName+")break;\n";
                }
                else{
                    observationCalcMethods += "                for (ParameterizedVar<Boolean> " +
                            predName + "O :\n" +
                            "                        next." + predName + ") {\n";

                    int count = 0;
                    for (Map.Entry<Integer, Integer> entry : observePredicateParamIndexToActionParIndex.entrySet()) {
                        Integer goalPredicateParamIndex = entry.getKey();
                        Integer actionParamIndex = entry.getValue();
                        String prefix = count == 0 ? "if (" : "";
                        String sufix = count == observePredicateParamIndexToActionParIndex.entrySet().size() - 1 ? ")" : "&&";
                        observationCalcMethods += "                    " + prefix + predName + "O.params.get(" + goalPredicateParamIndex +
                                ").equals("+
                                pO.observationGoalPredicate.Params.get(goalPredicateParamIndex).getName_Java() +
                                ") " + sufix + "\n";
                        count++;
                    }

                    observationCalcMethods += "                        if (!"+predName +"O.value) break;\n" +
                            "                        else {\n" ;
                }




                observationCalcMethods += "                            result += rand.nextFloat() < " + Utils.DecimalToString(pO.probabilityGivenObservedValue.GetConditionalProbabilities()[0].Probability) + " ? 1 : 0;\n" +
                        "                            break;\n";
                if(!stateVar.IsGlobalIntermediate) {
                    observationCalcMethods += "                        }\n" +
                            "                    }\n";
                }

                observationCalcMethods += "                }\n" +
                        "            }\n" +
                        "        return result;\n" +
                        "    }\n";
            }
        }
    }

    public void initAddValidPlpActionToValidStateActions(ArrayList<PLP> plps)
    {
        for (PLP plp: plps) {
            addValidPlpActionToValidStateActions += "        validActions.addAll("+getValidActionsMethodNameByPLP(plp)+"(ms));\r\n";
        }
    }

    public static String getValidActionsMethodNameByPLP(PLP plp)
    {
        return "GetValid"+plp.getName()+"Actions";
    }

    private String GetPLPValidActionsMethod(PLP plp) {
        String s = StateActionConstraintsForJava.GetActionConstraintLine(plp);
        String str = "\r\n    private static ArrayList<Pair<String,String[]>> "+
                getValidActionsMethodNameByPLP(plp) + "(MicroStateState ms)\r\n    {\r\n"+"" +
                "        ArrayList<Pair<String,String[]>> validActions = new ArrayList<>();\r\n"+
                s+
                "        return validActions;\r\n    }\r\n";
        return str;
    }


    private void initCalcRewardSection(ArrayList<PLP> plps, ProblemFile pf) {
        calcRewardsSection += "\n" +
                "    private static int CalcReward(MicroStateState state, MicroStateIntermediate interm, MicroStateObservation observation, MicroStateState next, ArrayList<Pair<String,String[]>> actions)\n" +
                "    {\n" +
                "        int reward = 0;\n" +
                "        if(observation.goal_reached)\n" +
                "        {\n" +
                "            reward +=" + pf.GoalReachedReward + ";\n" +
                "        }\n" +
                "\n";
        for (PLP plp : plps) {
            calcRewardsSection += "        for (Pair<String,String[]> action : actions) {\n" +
                    "            if(action._o1.equals(\"" + plp.getName() + "\"))\n" +
                    "                reward +=" + (-plp.ActionCost) + ";\n" +
                    "        }\n" +
                    "\n";
        }
        calcRewardsSection += "        return reward;\n" +
                "    }\n";
    }


    private ArrayList<KeyValuePair<Integer,StateVariableWithValue>> GetInitStateAssignments(ProblemFile pf)
    {
        ArrayList<KeyValuePair<Integer,StateVariableWithValue>> res = new ArrayList<>();

        for (StateVariableWithValue varAssign:
                pf.initalState.DeteministicStateAssignments) {
            res.add(new KeyValuePair<>(-1, varAssign));
        }

        for(int i=0;i<pf.initalState.InitialStateOptions.size();i++)
        {
            for (StateVariableWithValue varAssign:
                    pf.initalState.InitialStateOptions.get(i).assignments) {
                res.add(new KeyValuePair<>(i, varAssign));
            }
        }

        return res;
    }

    private void initInitialState(ProblemFile pf) throws Exception {
        ArrayList<KeyValuePair<Integer, StateVariableWithValue>> assignmentsByOption = GetInitStateAssignments(pf);
        //ArrayList<StateVariableWithValue> assignments = pf.initalState.DeteministicStateAssignments;

        for (PlanningStateVariable sType : pf.StateVariables) {
            if (sType.IsConstant || sType.IsObservation || sType.IsGlobalIntermediate) continue;

            stateVarInit += "        state." + sType.Name + " = new ArrayList<>();\r\n\r\n";

            int paramCount = 0;
            if (sType.ParameterTypes.size() == 0) {
                stateVarInit += "    ".repeat(2) + "{\r\n";
            }
            for (String paramType : sType.ParameterTypes) {
                String padding = "    ".repeat(paramCount);
                stateVarInit += padding + "        for (String i" + paramCount + ": " + paramType + ")\n" +
                        padding + "        {\r\n";
                paramCount++;
            }
            String padding = "    ".repeat(paramCount);
            stateVarInit += padding + "                ArrayList<String> a1 =\r\n";
            String params = "";
            for (int i = 0; i < paramCount; i++) {
                params += (i == 0) ? "i0" : ", i" + i;
            }
            stateVarInit += padding + "                        new ArrayList<String>(Arrays.asList(" + params + "));\r\n";


            Object[] oTypeAssignment =
                    assignmentsByOption.stream()
                            .filter(x -> {
                                try {
                                    return x.Value.type.equals(sType.Name) && !x.Value.value.equals(sType.GetDefault());
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                return false;
                            }).toArray();

            KeyValuePair<Integer, StateVariableWithValue>[] typeAssignment =
                    new KeyValuePair[oTypeAssignment.length];
            for (int i = 0; i < oTypeAssignment.length; i++) {
                typeAssignment[i] = (KeyValuePair<Integer, StateVariableWithValue>) oTypeAssignment[i];
            }

            stateVarInit += padding + "                boolean value = ";

            for (KeyValuePair<Integer, StateVariableWithValue> assign : typeAssignment) {
                if (assign.Value.params.size() > 0 || assign.Key >= 0) {
                    boolean first = true;
                    stateVarInit += "(";
                    if (assign.Key >= 0) {
                        stateVarInit += "(selectedOpt == " + assign.Key + ")";
                        first = false;
                    }
                    for (int j = 0; j < assign.Value.params.size(); j++) {
                        stateVarInit += (first ? "" : " && ") + "i" + j + ".equals(\"" + assign.Value.params.get(j) + "\")";
                        first = false;
                    }
                    stateVarInit += ") ? " + assign.Value.value + " : ";
                }

            }

            stateVarInit += sType.GetDefault() + ";";

            stateVarInit += "\r\n";
            stateVarInit += "\r\n                ParameterizedVar<Boolean> p = " +
                    "new ParameterizedVar<Boolean>(a1, value);\r\n                state." + sType.Name + ".add(p);\r\n";

            for (int zz = sType.ParameterTypes.size(); zz > 0; zz--) {
                stateVarInit += "    ".repeat(zz + 1) + "}\r\n";
            }

            if (sType.ParameterTypes.size() == 0) stateVarInit += "    ".repeat(2) + "}\r\n";
        }
    }



    private void InitRandomOption(ProblemFile pf)
    {
        for(int i =0; i < pf.initalState.InitialStateOptions.size();i++)
        {
            InitialStateOption op = pf.initalState.InitialStateOptions.get(i);
            randomOptionWeights += i==0 ? "" : ",";

            randomOptionWeights += Utils.DecimalToString(op.weight) + "f";
        }
    }


    private void initConstantsVarInit(ProblemFile pf) throws Exception {
        for(PlanningStateVariable cType:pf.StateVariables)
        {
            if(!cType.IsConstant) continue;

            constantsVarInit += "        " + cType.Name +" = new ArrayList<>();\r\n\r\n";

            int paramCount = 0;
            for(String paramType : cType.ParameterTypes)
            {
                String padding = "    ".repeat( paramCount );
                constantsVarInit += padding+ "        for (String i"+paramCount+": "+ paramType +")\n" +
                        padding+ "        {\r\n";
                paramCount++;
            }
            String padding = "    ".repeat( paramCount );
            constantsVarInit += padding + "                ArrayList<String> a1 =\r\n";
            String params = "";
            for(int i=0; i < paramCount; i++)
            {
                params += (i==0) ? "i0" : ", i"+i;
            }
            constantsVarInit += padding + "                        new ArrayList<String>(Arrays.asList("+params+"));\r\n";




            StateVariableWithValue[] typeAssignment=
                    pf.ConstantsAssignment.stream()
                            .filter(x-> {
                                try {
                                    return x.type.equals(cType.Name) && !x.value.equals(cType.GetDefault());
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                return false;
                            }).toArray(StateVariableWithValue[]::new);


            constantsVarInit += padding + "                boolean value = ";

            for (StateVariableWithValue assign : typeAssignment) {
                if(assign.params.size() > 0)
                {
                    constantsVarInit += "(";
                    for (int j=0; j <  assign.params.size();j++) {
                        constantsVarInit += (j==0 ? "" : " && ") + "i"+j+".equals(\""+assign.params.get(j)+"\")";
                    }
                    constantsVarInit += ") ? " + assign.value + " : ";
                }

            }


            constantsVarInit += cType.GetDefault() + ";";

            constantsVarInit += "\r\n";
            constantsVarInit += "\r\n                ParameterizedVar<Boolean> p = "+
                    "new ParameterizedVar<Boolean>(a1, value);\r\n                "+cType.Name+".add(p);\r\n";
            for(int zz= cType.ParameterTypes.size(); zz > 0;zz--)
            {
                constantsVarInit += "    ".repeat(zz+1) + "}\r\n";
            }
        }

    }

    private void initObjectsVarDefinition(ProblemFile pf)
    {
        for(Map.Entry<String, ArrayList<String>> entry : pf.ObjectsByType.entrySet()) {
            String type = entry.getKey();
            ArrayList<String> objects = entry.getValue();
            objectsVarDefinition += "    static HashSet<String> "+type+";\r\n";
        }
    }

    private void initObjectsVarInit(ProblemFile pf)
    {
        for(Map.Entry<String, ArrayList<String>> entry : pf.ObjectsByType.entrySet()) {
            String type = entry.getKey();
            ArrayList<String> objects = entry.getValue();
            objectsVarInit += "        "+ type +" = new HashSet<>();\r\n";
            for (String obj:
                    objects) {
                objectsVarInit += "        "+type+".add(\"" + obj + "\");\n";
            }
            objectsVarInit += "\r\n";
        }
    }

    private void initConstantsVarDefinition(ProblemFile pf)
    {
        for(PlanningStateVariable v:pf.StateVariables)
        {
            if(v.IsConstant)
            {
                constantsVarDefinition += "    public static ArrayList<ParameterizedVar<Boolean>> "+v.Name+";\r\n";
            }
        }
    }
}
