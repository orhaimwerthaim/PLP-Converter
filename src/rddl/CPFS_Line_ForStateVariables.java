package rddl;

import plp.PLP;
import plp.ProblemFile;
import plp.objects.PlanningStateVariable;
import plp.objects.Predicate;
import plp.objects.effect.*;
import plp.problem_file_objects.InitialStateOption;
import plp.problem_file_objects.StateVariableWithValue;
import rddl.objects.ConditionalEffectToCPFS;
import utils.Triplet;

import java.util.ArrayList;
import java.util.Optional;
import java.util.stream.Collectors;

public class CPFS_Line_ForStateVariables {
    private static PlanningStateVariable GetPlanningStateVariable_ByName(String stateVarName, ProblemFile pf)
    {
        ArrayList<PlanningStateVariable> temp = new ArrayList<PlanningStateVariable>();
        pf.StateVariables.forEach(var->{ if (var.Name.equals(stateVarName)) { temp.add(var); } });
        if (temp.size() == 0)return null;
        return temp.get(0);
    }

    private static Triplet<String> GetCPFS_Blocks_ForPLP(PLP plp, String stateVarName, ProblemFile pf)
    {
        Triplet<String> res = new Triplet<>();

        PlanningStateVariable stateVar = GetPlanningStateVariable_ByName(stateVarName, pf);
        if(stateVar == null)return null;

        ArrayList<ConditionalEffectToCPFS> conditionalEffects = plp != null ?
                GetConditionalEffectsByEffectedPredicate(plp, stateVar) :
                GetConditionalEffectsByEffectedPredicate(pf, stateVar);

        if(conditionalEffects.size() > 0) {
            for (int i = 0; i < conditionalEffects.get(0).GetNumberOfParametersInEffectedPredicate(); i++) {
                for (ConditionalEffectToCPFS o : conditionalEffects
                ) {
                    o.SetParamName(i, "?"  + stateVar.ParameterTypes.get(i) + "_" + (i+1));
                }
            }
        }
        ArrayList<Triplet<String>> tri = new ArrayList<>();
        conditionalEffects.forEach(o-> {
            try {
                tri.add(o.GetCPFS_Parts(plp));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        if(tri.size() > 0) {
            res.First = tri.get(0).First;
            res.Third = tri.get(0).Third;
            res.Second = tri.stream()
                    .map(n -> n.Second)
                    .collect(Collectors.joining(" else ")) + " else ";
        }
        return res;
    }

    private static ArrayList<ConditionalEffectToCPFS> GetConditionalEffectsByEffectedPredicate(PLP plp, PlanningStateVariable effectedStateVariable)
    {
        return GetConditionalEffectsByEffectedPredicate(plp, effectedStateVariable, null);
    }

    private static ArrayList<ConditionalEffectToCPFS> GetConditionalEffectsByEffectedPredicate(ProblemFile pf, PlanningStateVariable effectedStateVariable)
    {
        return GetConditionalEffectsByEffectedPredicate(null, effectedStateVariable, pf);
    }

    private static ArrayList<ConditionalEffectToCPFS> GetConditionalEffectsByEffectedPredicate(PLP plp, PlanningStateVariable effectedStateVariable, ProblemFile pf)
    {
        ArrayList<ConditionalEffectToCPFS> res = new ArrayList<>();
        ArrayList<Effect> effects = new ArrayList<>();
        if(plp != null)
        {
            effects.addAll(plp.sideEffects.effects);
        }
        if(pf != null)
        {
            effects.add(pf.GoalReachedEffect);
        }

        effects.forEach(effect ->
        {
            if(effect.effect instanceof ConditionalEffect) {
                if (((ConditionalEffect) effect.effect).effect.effectType == EEffectType.Predicate &&
                        ((Predicate) ((ConditionalEffect) effect.effect).effect.effect).Name.equals(effectedStateVariable.Name)) {
                    res.add(new ConditionalEffectToCPFS(effect, (Predicate) ((ConditionalEffect) effect.effect).effect.effect, effectedStateVariable));
                }

                if (((ConditionalEffect) effect.effect).effect.effectType == EEffectType.Not &&
                        ((NotEffect) ((ConditionalEffect) effect.effect).effect.effect).Predicate.Name.equals(effectedStateVariable.Name)) {
                    res.add(new ConditionalEffectToCPFS(effect, ((NotEffect) ((ConditionalEffect) effect.effect).effect.effect).Predicate, effectedStateVariable));
                }

                if (((ConditionalEffect) effect.effect).effect.effectType == EEffectType.Assignment &&
                        ((AssignmentEffect) ((ConditionalEffect) effect.effect).effect.effect).Param_Predicate.Name.equals(effectedStateVariable.Name)) {
                    res.add(new ConditionalEffectToCPFS(effect, ((AssignmentEffect) ((ConditionalEffect) effect.effect).effect.effect).Param_Predicate, effectedStateVariable));
                }

            }
            if(effect.effect instanceof NotEffect && (((NotEffect) effect.effect).Predicate.Name.equals(effectedStateVariable.Name)))
            {
                res.add(new ConditionalEffectToCPFS(effect, (((NotEffect) effect.effect).Predicate), effectedStateVariable));
            }
            if(effect.effect instanceof Predicate && (((Predicate) effect.effect).Name.equals(effectedStateVariable.Name)))
            {
                res.add(new ConditionalEffectToCPFS(effect, (((Predicate) effect.effect)), effectedStateVariable));
            }
        });

        return res;
    }

    public static String GetCPFS_LineForStateVar(ArrayList<PLP> plps, String stateVarName, ProblemFile pf) {
        ArrayList<Triplet<String>> cpfsVarParts = new ArrayList<>();

        for (PLP plp:plps) {
            Triplet<String> t = GetCPFS_Blocks_ForPLP(plp, stateVarName, pf);
            if (t != null && t.First != null) {
                cpfsVarParts.add(t);
            } }

        //when plp is null it creates needed CPFS lines from ProblemFile 'pf'
        Triplet<String> t = GetCPFS_Blocks_ForPLP(null, stateVarName, pf);
        if (t != null && t.First != null) {
            cpfsVarParts.add(t);
        }

        StringBuilder res = new StringBuilder();
        if (cpfsVarParts.size()==0)return null;
        res.append(cpfsVarParts.get(0).First);
        cpfsVarParts.forEach(p-> res.append(p.Second));

        res.append(cpfsVarParts.get(0).Third);

        return res.toString();
    }

    public static String GetInitStateRDDL_CPFS_LineForStateVar(String stateVarName, ProblemFile pf) throws Exception {
        StringBuilder line = new StringBuilder();
        PlanningStateVariable stateVar = GetPlanningStateVariable_ByName(stateVarName, pf);

        if(stateVar.IsObservation)
        {
            return stateVar.Name +"=" + (stateVar.Type.equals("bool") ? "false;" : "KronDelta(0);");
        }
        line.append(stateVar.Name + (stateVar.IsGlobalIntermediate ? "" : "'"));
        ArrayList<String> pars = new ArrayList<>();

        if (stateVar.ParameterTypes.size() > 0) {
            line.append("(");
            for (int i = 1; i <= stateVar.ParameterTypes.size(); i++) {
                if (i != 1) line.append(",");
                String p = "?" + stateVar.ParameterTypes.get(i - 1) + i;
                pars.add(p);
                line.append(p);
            }
            line.append(")");
        }
        String varFullName = line.toString().replace("'", "");
        line.append("=");

        for (int i = 0; i < pf.initalState.InitialStateOptions.size(); i++) {
            InitialStateOption op = pf.initalState.InitialStateOptions.get(i);

            for (int j = 0; j < op.assignments.size(); j++) {
                StateVariableWithValue assign = op.assignments.get(j);
                if (assign.type.equals(stateVarName)) {
                    line.append("if(opt == @opt" + (i+1));
                    for (int z = 0; z < pars.size(); z++) {
                        line.append(" ^ " + pars.get(z) + " == $" + assign.params.get(z));
                    }
                    line.append(") then " + assign.value + " else ");
                }
            }
        }
        line.append((stateVar.IsGlobalIntermediate ? stateVar.GetDefault() : varFullName) + ";");
        return line.toString();
    }
}