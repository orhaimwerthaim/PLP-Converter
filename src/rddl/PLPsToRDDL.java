package rddl;

import plp.*;
import plp.objects.*;
import plp.objects.effect.*;
import plp2java.*;
import utils.KeyValuePair;
import utils.Triplet;

import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class PLPsToRDDL {
    public static ProblemFile pf = null;
    RDDL fileWriterRDDL;
    final static String PLP_SUCCESS_INTERM_PREFIX = "success_";
    final static String OBSERVATION_PVAR_PREFIX = "obsrv_";
    public PLPsToRDDL(KeyValuePair<ProblemFile, ArrayList<PLP>> plpsWithProblemFile, boolean toRddl) throws Exception {
        pf = plpsWithProblemFile.Key;

        ArrayList<PLP> plps = plpsWithProblemFile.Value;

        fileWriterRDDL = new RDDL();
        AddTypes(pf);
        AddActionIntermPvariablesForActionSuccess(plps);
        AddPvariables(pf, plps);

        //add plps observation pvariables
        plps.forEach(plp-> AddObservationPvariable(plp));

        if(toRddl) {
            AddCPFS(plps, pf);
            AddActionConstraint(plps);
            AddReward(plps, pf);
            AddInstaceFileData(pf);

            fileWriterRDDL.WriteToInstanceFile(pf, false);
            fileWriterRDDL.WriteToInstanceFile(pf, true);
            fileWriterRDDL.WriteToDomainFile();
            fileWriterRDDL.WriteToInitStateDomainFile(pf);
        }
        else {
            writeJavaSimulatorFiles(pf, plps);
        }
    }

    private void writeJavaSimulatorFiles(ProblemFile pf, ArrayList<PLP> plps) throws Exception {
        new MicroState_Gate_Writer(pf);
        new MicroStateIntermediate_Writer(pf, plps);
        new MicroStateObservation_Writer(plps);
        new MicroStateState_Writer(pf,plps);
        new MicroState_Writer(pf, plps);
    }

    private void AddInstaceFileData(ProblemFile pf)
    {
        pf.ObjectsByType.forEach((type,objects)->
        {
            String objectsStr = objects.stream().collect(Collectors.joining(","));
            fileWriterRDDL.AppendLineToRDDL_InstanceBlock(
                    RDDL.EInstaceBlocks.objects, type + " : {" + objectsStr + "};");
        });

        pf.ConstantsAssignment.forEach(constV->
        {
            String str = constV.type +
                    (constV.params.size() == 0 ? "" :
                            "(" + constV.params.stream().collect(Collectors.joining(",")) + ")") +
                " = " + constV.value + ";";

            fileWriterRDDL.AppendLineToRDDL_InstanceBlock(
                    RDDL.EInstaceBlocks.non_fluents, str);
        });

        pf.initalState.DeteministicStateAssignments.forEach(assignV->
        {
            String str = assignV.type +
                    (assignV.params.size() == 0 ? "" :
                            "(" + assignV.params.stream().collect(Collectors.joining(",")) + ")") +
                    " = " + assignV.value + ";";

            fileWriterRDDL.AppendLineToRDDL_InstanceBlock(
                    RDDL.EInstaceBlocks.init_state, str);
        });

    }

    private void AddReward(ArrayList<PLP> plps, ProblemFile pf) {
        fileWriterRDDL.AppendLineToRDDL_DomainBlock(RDDL.EBlocks.reward,
                "(if("+ProblemFile.GoalReachedStateVariableName+")then "+pf.GoalReachedReward +" else 0)");

        for(PLP plp: plps)
        {
            String line = "+" + GetRewardLine(plp);
            fileWriterRDDL.AppendLineToRDDL_DomainBlock(RDDL.EBlocks.reward, line);
        };

    }

    private String GetRewardLine(PLP plp) {
        String sSum = PLP2RDDL_Utils.GetSumString(plp.GetParams());
        StringBuilder line = new StringBuilder("[" + sSum +
                "[ (" + ((plp.ActionCost)*(-1)) + ") * " + plp.PlpNameWithParams(false) + "]]");
        return line.toString();
    }

    private void AddActionConstraint(ArrayList<PLP> plps) {
        plps.forEach(plp->
        {
            String line = StateActionConstraintsLine.GetActionConstraintLine(plp);
            fileWriterRDDL.AppendLineToRDDL_DomainBlock(RDDL.EBlocks.StateActionConstraints, line);
        });
    }
//StateActionConstraintsLine
    public static String GetActionSuccessIntermName(PLP plp)
    {
        return PLP_SUCCESS_INTERM_PREFIX + plp.getName();
    }

    public static String GetObservationValueName(PLP plp)
    {
        return OBSERVATION_PVAR_PREFIX + plp.getName();
    }

    private void AddActionIntermPvariablesForActionSuccess(ArrayList<PLP> plps) {
        plps.forEach(plp->
        {
            String sInterm = GetActionSuccessIntermName(plp) + " : {interm-fluent, bool, level = 1};";
            fileWriterRDDL.AppendLineToRDDL_DomainBlock(RDDL.EBlocks.pvariables, sInterm);
        });
    }

    private void AddObservationPvariable(PLP plp) {
        if(plp instanceof PLP_Achieve && !((PLP_Achieve)plp).IsActionSuccessObservable)
        {
                return;
        }

        String obsrValue = GetObservationValueName(plp) + " : { observ-fluent , int};";
        fileWriterRDDL.AppendLineToRDDL_DomainBlock(RDDL.EBlocks.pvariables, obsrValue);
        //fileWriterRDDL.AppendLineToRDDL_DomainBlock(RDDL.EBlocks.pvariables_for_initial_stats, obsrValue);
    }


    private static Predicate GetEffectAsPredicate(Effect effect)
    {
        Predicate pred = null;
        switch (effect.effectType)
        {
            case Predicate:
                return (Predicate)effect.effect;
            case Not:
                return ((NotEffect)effect.effect).Predicate;
            case Assignment:
                return ((AssignmentEffect)effect.effect).Param_Predicate;
            case Conditional:
                return GetEffectAsPredicate(((ConditionalEffect)effect.effect).effect);
            case ForAll:
            case Invalid:
                throw new UnsupportedOperationException();
        }
        return pred;
    }

    private static ArrayList<Triplet<String>> GetCPFS_Blocks_back(ArrayList<PLP> plps, ProblemFile pf) {
        HashMap<String,PlanningStateVariable> stateVarsNeedCPFS = new HashMap<>();
        //adding all state variables from all plps to one HashMap (we want to make sure anyone of them have CPFS line)
        pf.StateVariables.forEach(stateVar->
        {
            if(!stateVarsNeedCPFS.containsKey(stateVar.Name) && !stateVar.IsConstant)
            {
                stateVarsNeedCPFS.put(stateVar.Name, stateVar);
            }
        });

        ArrayList<Triplet<String>> result = new ArrayList<>();
        HashMap<String, HashMap<PLP, Effect>> effectsByAssignVar = new HashMap<>();
        HashMap<KeyValuePair<Predicate, PLP>, ArrayList<Predicate>> parameterNaming = new HashMap<>();
        plps.forEach(plp ->
                plp.sideEffects.effects.forEach(effect ->
                {
                    Predicate pred = GetEffectAsPredicate(effect);
                    KeyValuePair<Predicate, PLP> key = new KeyValuePair(pred, plp);
                    if (!(parameterNaming.containsKey(key))) {
                        parameterNaming.put(key, new ArrayList<>());
                    }
                    if (!(effectsByAssignVar.containsKey(pred.Name)))
                        effectsByAssignVar.put(pred.Name, new HashMap<>());
                    effectsByAssignVar.get(pred.Name).put(plp, effect);

                    if (effect.effectType == EEffectType.Conditional) {
                        ArrayList<Predicate> preds = PLP2RDDL_Utils.GetConditionPredicates(
                                ((ConditionalEffect) effect.effect).condition);
                        parameterNaming.get(key).addAll(preds);
                    }
                }));

        HashSet<String> addedPreds = new HashSet<>();
        parameterNaming.forEach((key, value) ->
        {
            String varName = key.Key.Name;
            if (addedPreds.add(varName)) {
                HashMap<KeyValuePair<Predicate, PLP>, ArrayList<Predicate>> forSingleCPFS_Line = new HashMap<>();
                ArrayList<Predicate> allP = new ArrayList<>();
                parameterNaming.entrySet().stream()
                        .filter(k -> k.getKey().Key.Name.equals(varName))
                        .forEach(entry ->
                        {
                            if (!(forSingleCPFS_Line.containsKey(entry.getKey()))) {
                                forSingleCPFS_Line.put(entry.getKey(), new ArrayList<>());
                                allP.add(entry.getKey().Key);
                            }
                            forSingleCPFS_Line.get(entry.getKey()).addAll(entry.getValue());
                            allP.addAll(entry.getValue());
                        });
                Predicate outPred = PLP2RDDL_Utils.SetPredicateParameterNamesAndTypes(forSingleCPFS_Line);
                allP.add(outPred);
                if (outPred != null) {
                    PLP2RDDL_Utils.PredicatesNameChangeTo(allP);
                    result.add(CPFS_Line.Get_CPFS_LinePart(key.Key, effectsByAssignVar.get(key.Key.Name)));
                    if(stateVarsNeedCPFS.containsKey(key.Key.Name)) stateVarsNeedCPFS.remove(key.Key.Name);
                    PLP2RDDL_Utils.PredicatesNameToOriginal(allP);
                }
            }
        });

        //adding maintain rules for unassigned state variables
        stateVarsNeedCPFS.forEach((key,missingVar)->
        {
            String sign = missingVar.Name + "'";
            if(missingVar.ParameterTypes.size() > 0)
            {
                AtomicInteger index = new AtomicInteger(0);
                sign += "(" +
                        missingVar.ParameterTypes.stream()
                                .map(x-> "?" + x + index.getAndIncrement())
                                .collect(Collectors.joining(",")) +
                        ")";
            }
            Triplet<String> t = new Triplet<>();
            t.First = sign + "=";
            t.Second = "";
            t.Third = sign.replace("'","") + ";";
            result.add(t);
        });


        return result;
    }

    private void AddCPFS(ArrayList<PLP> plps, ProblemFile pf) throws Exception {
        //adding cpfs lines for all state variables
        HashSet<String> stateVarsNeedCPFS = new HashSet<>();
        pf.StateVariables.forEach(stateVar ->
        {
            if(!stateVar.IsConstant)stateVarsNeedCPFS.add(stateVar.Name);
        });

        for (String stateVar : stateVarsNeedCPFS) {
            String line = CPFS_Line_ForStateVariables.GetCPFS_LineForStateVar(plps, stateVar, pf);
            if (line == null || line.isEmpty()) {
                for (PLP plp:
                        plps) {
                    for (PlanningStateVariable var:
                            pf.StateVariables) {
                        if (var.Name.equals(stateVar)) {
                            String prim = var.IsGlobalIntermediate ? "" : "'";
                            String parameters = "(" + var.ParameterTypes.stream().map(x-> "?"+x).collect(Collectors.joining(","))+")";
                            String fullNameWithPrim = var.Name + prim + (var.ParameterTypes.size() == 0 ? "" : parameters);
                            String nextStateValue =var.IsGlobalIntermediate ? var.GetDefault() : var.Name + (var.ParameterTypes.size() == 0 ? "" : parameters);
                            line = fullNameWithPrim + "=" + nextStateValue + ";";
                            break;
                        }
                    }}
            }
            fileWriterRDDL.AppendLineToRDDL_DomainBlock(RDDL.EBlocks.cpfs, line);

            String initStateDomainCPFS_Line =
                    CPFS_Line_ForStateVariables.GetInitStateRDDL_CPFS_LineForStateVar(stateVar, pf);

            fileWriterRDDL.AppendLineToRDDL_DomainBlock(RDDL.EBlocks.cpfs_for_initial_stats, initStateDomainCPFS_Line);
        }


        //add for each PLP Action it's successFailProbability and for observations the probabilityGivenObservedValue
        for (String obsr_CPFS_Line: CPFS_Utils.Get_CPFS_For_Observations(plps)) {
            fileWriterRDDL.AppendLineToRDDL_DomainBlock(RDDL.EBlocks.cpfs, obsr_CPFS_Line);
        }

        //add cpfs intermediates for action success
        plps.forEach(plp->
        {
            if (plp instanceof PLP_Achieve)
            {
                PLP_Achieve pA = (PLP_Achieve)plp;
                fileWriterRDDL.AppendLineToRDDL_DomainBlock(RDDL.EBlocks.cpfs, CPFS_Utils.Get_CPFS_For_ActionSuccess(plp, pA.successFailProbability, true));
            }
            if (plp instanceof PLP_Observe)
            {
                PLP_Observe pO = (PLP_Observe)plp;
                fileWriterRDDL.AppendLineToRDDL_DomainBlock(RDDL.EBlocks.cpfs,
                        CPFS_Utils.Get_CPFS_For_ActionSuccess(plp, pO.failure_to_observe_probability, false));
            }

        });
    }

    private void AddPvariables(ProblemFile pf, ArrayList<PLP> plps) {
        AddActions(plps);

        for (PlanningStateVariable stateVar :
                pf.StateVariables) {
            String pvarType = "";
            try {
                pvarType = RDDL.GetRDDLType(stateVar.Type);
            } catch (Exception e) {
                System.err.println("Got an error: " + e);
            }

            String pvarDefault = "";
            try {
                pvarDefault = stateVar.GetDefault();
            } catch (Exception e) {
                System.err.println("Got an error: " + e);
            }
            String sPvar = stateVar.Name;

            for (int j = 0; j < stateVar.ParameterTypes.size(); j++) {
                sPvar += (j == 0) ? "(" : ",";
                sPvar += stateVar.ParameterTypes.get(j);
            }
            sPvar += stateVar.ParameterTypes.size() > 0 ? ") " : " ";
            if(stateVar.IsObservation)
            {
                sPvar += ": { observ-fluent, " +
                        pvarType + " };";
            }else if (stateVar.IsGlobalIntermediate) {//: {interm-fluent, bool, level = 1};
                sPvar += ": {interm-fluent, " + pvarType + ", level = " + stateVar.GlobalIntermediateLevel + "};";
            } else {
                sPvar += ": {" +
                        (stateVar.IsConstant ? "non-fluent" : "state-fluent") + ", " +
                        pvarType + ", default = " + pvarDefault + " };";
            }
            fileWriterRDDL.AppendLineToRDDL_DomainBlock(RDDL.EBlocks.pvariables, sPvar);
            fileWriterRDDL.AppendLineToRDDL_DomainBlock(RDDL.EBlocks.pvariables_for_initial_stats, sPvar);
        }
    }

    private void AddTypes(ProblemFile pf) {//discrete_location : object;//rooms etc..
        pf.ObjectsByType.keySet().stream()
                .forEach(objectType ->
                        {
                        fileWriterRDDL.AppendLineToRDDL_DomainBlock(RDDL.EBlocks.types,objectType + ": object;");
                            fileWriterRDDL.AppendLineToRDDL_DomainBlock(RDDL.EBlocks.types_for_initial_stats,objectType + ": object;");
                        });
    }


    private void AddActions(ArrayList<PLP> plps)
    {
        plps.forEach(plp->
        {
            PlanningTypedParameter[] actionParams = plp.GetParams();
            StringBuilder plpAction = new StringBuilder(plp.getName() + (actionParams.length > 0 ? "(" : ""));
            for(int i=0; i < actionParams.length; i++)
            {
                plpAction.append(i == 0 ? "" : ",");
                plpAction.append(actionParams[i].Type);
            }
            plpAction.append(actionParams.length > 0 ? ")" : "");
            plpAction.append(" : { action-fluent, bool, default = false };");
            fileWriterRDDL.AppendLineToRDDL_DomainBlock(RDDL.EBlocks.pvariables, plpAction.toString());
            fileWriterRDDL.AppendLineToRDDL_DomainBlock(RDDL.EBlocks.pvariables_for_initial_stats, plpAction.toString());
        });
    }
}
