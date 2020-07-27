package plp.objects.effect;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import plp.PLP;
import plp.objects.PlanningStateVariable;
import plp.objects.Predicate;
import plp.objects.effect.IEffect;

import java.util.ArrayList;

public class NotEffect implements IEffect {
    public plp.objects.Predicate Predicate;
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
        return Predicate.getName();
    }

    @Override
    public String getEffectAssignedValue() {
        return "false";
    }

    public NotEffect(Node node, ArrayList<PlanningStateVariable> declaredStateVariables, PLP plp) throws Exception {
        NodeList childNodes = node.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node childNode = childNodes.item(i);

            if (childNode.getNodeType() == Node.ELEMENT_NODE) {
                if (childNode.getNodeName().equals("predicate_effect")) {
                    Predicate = new Predicate(childNode, declaredStateVariables, plp);
                    return;
                } else throw new Exception("predicate_effect should be the only element in side_effect");
            }
        }
    }

    @Override
    public String toString()
    {
        return Predicate.toString();
    }

    @Override
    public int hashCode()
    {
        return toString().hashCode();
    }

    @Override
    public boolean equals(Object obj)
    {
        if(!(obj instanceof NotEffect)) return false;
        return ((NotEffect)obj).toString().equals(toString());
    }
}
