package plp2java.plp2javaUtils;

import plp.PLP;
import plp.ProblemFile;
import plp.objects.PlanningStateVariable;
import plp.objects.Predicate;
import plp.objects.effect.*;
import utils.Triplet;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class CPFS_Line_ForStateVariables_JAVA {
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


    private static PlanningStateVariable GetPlanningStateVariable_ByName(String stateVarName, ProblemFile pf)
    {
        ArrayList<PlanningStateVariable> temp = new ArrayList<PlanningStateVariable>();
        pf.StateVariables.forEach(var->{ if (var.Name.equals(stateVarName)) { temp.add(var); } });
        if (temp.size() == 0)return null;
        return temp.get(0);
    }

    private static ArrayList<ConditionalEffectToCPFS_JAVA> GetConditionalEffectsByEffectedPredicate(PLP plp, PlanningStateVariable effectedStateVariable)
    {
        return GetConditionalEffectsByEffectedPredicate(plp, effectedStateVariable, null);
    }

    private static ArrayList<ConditionalEffectToCPFS_JAVA> GetConditionalEffectsByEffectedPredicate(ProblemFile pf, PlanningStateVariable effectedStateVariable)
    {
        return GetConditionalEffectsByEffectedPredicate(null, effectedStateVariable, pf);
    }

    private static ArrayList<ConditionalEffectToCPFS_JAVA> GetConditionalEffectsByEffectedPredicate(PLP plp, PlanningStateVariable effectedStateVariable, ProblemFile pf)
    {
        ArrayList<ConditionalEffectToCPFS_JAVA> res = new ArrayList<>();
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
                    res.add(new ConditionalEffectToCPFS_JAVA(effect, (Predicate) ((ConditionalEffect) effect.effect).effect.effect, effectedStateVariable));
                }

                if (((ConditionalEffect) effect.effect).effect.effectType == EEffectType.Not &&
                        ((NotEffect) ((ConditionalEffect) effect.effect).effect.effect).Predicate.Name.equals(effectedStateVariable.Name)) {
                    res.add(new ConditionalEffectToCPFS_JAVA(effect, ((NotEffect) ((ConditionalEffect) effect.effect).effect.effect).Predicate, effectedStateVariable));
                }

                if (((ConditionalEffect) effect.effect).effect.effectType == EEffectType.Assignment &&
                        ((AssignmentEffect) ((ConditionalEffect) effect.effect).effect.effect).Param_Predicate.Name.equals(effectedStateVariable.Name)) {
                    res.add(new ConditionalEffectToCPFS_JAVA(effect, ((AssignmentEffect) ((ConditionalEffect) effect.effect).effect.effect).Param_Predicate, effectedStateVariable));
                }

            }
            if(effect.effect instanceof NotEffect && (((NotEffect) effect.effect).Predicate.Name.equals(effectedStateVariable.Name)))
            {
                res.add(new ConditionalEffectToCPFS_JAVA(effect, (((NotEffect) effect.effect).Predicate), effectedStateVariable));
            }
            if(effect.effect instanceof Predicate && (((Predicate) effect.effect).Name.equals(effectedStateVariable.Name)))
            {
                res.add(new ConditionalEffectToCPFS_JAVA(effect, (((Predicate) effect.effect)), effectedStateVariable));
            }
        });

        return res;
    }

    private static Triplet<String> GetCPFS_Blocks_ForPLP(PLP plp, String stateVarName, ProblemFile pf)
    {
        Triplet<String> res = new Triplet<>();

        PlanningStateVariable stateVar = GetPlanningStateVariable_ByName(stateVarName, pf);
        if(stateVar == null)return null;

        ArrayList<ConditionalEffectToCPFS_JAVA> conditionalEffects = plp != null ?
                GetConditionalEffectsByEffectedPredicate(plp, stateVar) :
                GetConditionalEffectsByEffectedPredicate(pf, stateVar);

        if(conditionalEffects.size() > 0) {
            for (int i = 0; i < conditionalEffects.get(0).GetNumberOfParametersInEffectedPredicate(); i++) {
                for (ConditionalEffectToCPFS_JAVA o : conditionalEffects
                ) {
                    o.SetParamName(i, "?"  + stateVar.ParameterTypes.get(i) + "_" + (i+1));
                }
            }
        }
        ArrayList<Triplet<String>> tri = new ArrayList<>();
        conditionalEffects.forEach(o-> {
            try {
                tri.add(o.GetCPFS_Parts_JAVA(plp));
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
}
