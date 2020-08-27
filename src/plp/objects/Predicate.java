package plp.objects;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import plp.PLP;
import plp.objects.effect.IEffect;
import plp2java.plp2javaUtils.PLP2JavaUtils;
import rddl.PLP2RDDL_Utils;
import convert.PLP_Converter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Predicate implements ICondition, IEffect, IParameterized {
    public String Name;
    public ArrayList<PlanningTypedParameter> Params = new ArrayList<>();
    private EEffectingUpon effectEffectingUpon = null;

    public void setEffectEffectingUpon(EEffectingUpon effectingUpon) {
        effectEffectingUpon = effectingUpon;
    }

    @Override
    public EEffectingUpon getEffectingUpon() {
        return effectEffectingUpon;
    }

    @Override
    public void setEffectingUpon(EEffectingUpon upon) {
        effectEffectingUpon = upon;
    }

    @Override
    public String getName() {
        return baseToString(false, true);
    }

    @Override
    public String getEffectAssignedValue() {
        return "true";
    }

    public Predicate() {

    }

    public Predicate(Node node, ArrayList<PlanningStateVariable> declaredStateVariables, PLP plp) throws Exception {
        Element el = (Element) node;
        Name = el.getAttribute("name");
        NodeList nl = ((Element) node).getElementsByTagName("field");
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                Params.add(new PlanningTypedParameter(((Element) n).getAttribute("value"), null));
            }
        }
        List<PlanningStateVariable> vars = declaredStateVariables.stream().filter(var -> var.Name.equals(Name)).collect(Collectors.toList());
        if (vars.size() > 0) {
            vars.get(0).FillTypesBySameVariable(Params);
        }
        if (plp != null && plp.getName().equals(Name) && Params.size() == plp.GetParams().length) {
            for (int i = 0; i < plp.GetParams().length; i++) {
                Params.get(i).Type = plp.GetParams()[i].Type;
            }
        }
    }

    @Override
    public String toString() {
        return baseToString(false, true);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Predicate)) return false;

        return ((Predicate) obj).baseToString(false, false)
                .equals(baseToString(false, false));
    }

    @Override
    public int hashCode() {
        return baseToString(false, false).hashCode();
    }

    public String toStringWithPrim() {
        return baseToString(true, true);
    }

    public String baseToString(boolean withPrim, boolean byName) {
        String sign = "";
        if (byName) {
            sign = Params.size() == 0 ? "" :
                    "(" + Params.stream().map(par -> par.getName() == null ? "" : par.getName()).collect(Collectors.joining(",")) + ")";
        } else {
            sign = Params.size() == 0 ? "" :
                    "(" + Params.stream().map(par -> par.Type == null ? "" : par.Type).collect(Collectors.joining(",")) + ")";
        }
        return Name + (withPrim ? "'" : "") + sign;
    }

    @Override
    public ArrayList<PlanningTypedParameter> GetParams() {
        return Params;
    }

    @Override
    public String getConditionForIf(ArrayList<PlanningTypedParameter> assignToParams, boolean underAndOrCondition, boolean IsObservationEffected) {
        String exists = underAndOrCondition ? "" :
                PLP2RDDL_Utils.GetExistsForPredicate(Params.toArray(new PlanningTypedParameter[Params.size()]), assignToParams);
        StringBuilder result = new StringBuilder("(" + exists + " " + Name + (IsObservationEffected ? "'" : ""));
        if (Params.size() > 0) {
            result.append("(");
            result.append(Params.stream().map(p -> p.getName()).collect(Collectors.joining(",")));
            result.append(")");
        }
        result.append(")");
        return result.toString();
    }

/*if ((next.near.stream().filter(x -> x.value &&\n" +
                "                    x.params.get(0).equals(anyRobot) &&\n" +
                "                    x.params.get(1).equals(\"robot_lab\"))\n" +
                "                    .count() > 0) &&\n" +*/
    @Override
    public String getConditionForIf_JavaNextState(ArrayList<PlanningTypedParameter> assignToParams, boolean underAndOrCondition, boolean IsObservationEffected) {
        String res = "";
        if (IsObservationEffected) {
            res += "(next." + Name + ".stream().filter(x -> x.value";
            for (int i = 0; i < Params.size(); i++) {
                if (Params.get(i).IsActualObject_NotParam()) {
                    res += " && x.params.get(" + i + ").equals(\"" + Params.get(i).getName_Java() + "\")\n";
                } else {
                    for (PlanningTypedParameter assignedParam : assignToParams
                    ) {
                        if (assignedParam.getName_Java().equals(Params.get(i).getName_Java())) {//nearO.params.get(1).equals("robot_lab"))
                            res += " && x.params.get(" + i + ").equals(" + assignedParam.getName_Java() + ")\n";
                        }
                    }
                }
            }
            res += ").count() > 0)\n";
        } else {
            PlanningStateVariable var = PLP2JavaUtils.GetStateVariableByName(Name);
            if (var!=null && var.IsGlobalIntermediate) return " interm." + Name;
            boolean first = true;
            for (int i = 0; i < Params.size(); i++) {
                for (PlanningTypedParameter assignedParam : assignToParams
                ) {
                    if (assignedParam.getName_Java().equals(Params.get(i).getName_Java())) {//nearO.params.get(1).equals("robot_lab"))
                        res += (first ? "" : " && ") + assignedParam.getName_Java() +
                                ".equals(actions.get(0)._o2[" + i + "])";
                        first = false;
                    }
                }
            }
        }
        return res;
    }

    @Override
    public String getConditionForIf_JavaValidActions(ArrayList<PlanningTypedParameter> assignToParams, boolean underAndOrCondition, boolean IsObservationEffected) {
        boolean IsConstant = false;
        for(PlanningStateVariable var: PLP_Converter.pf.StateVariables)
        {
            if(Name.equals(var.Name)
                    && var.IsConstant)
            {
                IsConstant = true;
            }
        }

        String varJavaName = IsConstant ? Name : "ms."+Name;
        String result = "("+varJavaName+".stream().filter(x-> x.value.equals(true)";
        for(int i=0;i<Params.size();i++)
        {
            boolean found = false;
            for(PlanningTypedParameter par: assignToParams)
            {
                if(Params.get(i).getName_Java().equals(par.getName_Java()))
                {
                    found = true;
                    break;
                }
            }
            if(found)
            {
                result += " && x.params.get("+i+").equals("+Params.get(i).getName_Java()+")";
            }
        }
        result += ").count() > 0)";
        return result;
//                "\n" +
//                "                                ("+varJavaName+".stream().filter(x-> x.value.equals(true) && x.params.get(0).equals(robot1) && x.params.get(1).equals(discrete_location3)).count() > 0)";
    }

}
