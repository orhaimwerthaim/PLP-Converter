package plp.objects.effect;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import plp.PLP;
import plp.objects.ParamBase;
import plp.objects.PlanningStateVariable;
import plp.objects.Predicate;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class AssignmentEffect implements IEffect {
    public Predicate Param_Predicate;
    public String AssignValue;
    private EEffectingUpon effectEffectingUpon = null;

    public void setEffectEffectingUpon(EEffectingUpon effectingUpon)
    {
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
        return toString();
    }

    @Override
    public String getEffectAssignedValue() {
        return AssignValue;
    }

    public AssignmentEffect(Node node, ArrayList<PlanningStateVariable> declaredStateVariables, PLP plp) throws Exception {
        NodeList childNodes = node.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node childNode = childNodes.item(i);
            if (childNode.getNodeType() == Node.ELEMENT_NODE) {
                switch (childNode.getNodeName())
                {
                    case "param":
                        Param_Predicate = new Predicate(childNode, declaredStateVariables, plp);
                        break;
                    case "expression":
                        AssignValue = ((Element)childNode).getAttribute("value");
                        break;
                        default: throw new UnsupportedOperationException();
                }
            }
        }
    }

    @Override
    public String toString() {
        //String sign = Param_Predicate.Params.size() == 0 ? "" :
          //      "(" + Param_Predicate.Params.stream().map(par-> par.getName()).collect(Collectors.joining(",")) + ")";

        //return Param_Predicate.Name + sign;
        return Param_Predicate.toString();
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof AssignmentEffect))return false;
        return ((AssignmentEffect)obj).toString().equals(toString());

    }
}
