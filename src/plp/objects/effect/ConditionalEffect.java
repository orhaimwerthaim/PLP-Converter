package plp.objects.effect;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import plp.PLP;
import plp.objects.PlanningStateVariable;
import plp.objects.condition.Condition;

import java.util.ArrayList;

public class ConditionalEffect implements IEffect{

    public Condition condition;
    public Effect effect;
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
        return effect.effect.getName();
    }

    @Override
    public String getEffectAssignedValue() {
        return effect.effect.getEffectAssignedValue();
    }

    public ConditionalEffect(Node node, ArrayList<PlanningStateVariable> declaredStateVariables, PLP plp) throws Exception {
        NodeList childNodes = node.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node childNode = childNodes.item(i);
            if (childNode.getNodeType() == Node.ELEMENT_NODE) {
                if(Condition.ConditionTags.contains(childNode.getNodeName()))
                {
                    condition = new Condition(childNode, false, declaredStateVariables, plp);
                }
                if(Effect.EffectTags.contains(childNode.getNodeName()))
                {
                    effect = new Effect(childNode, false, declaredStateVariables, plp);
                }
            }
        }
    }

    @Override
    public String toString()
    {
        return effect.effect.toString();
    }

    @Override
    public int hashCode()
    {
        return toString().hashCode();
    }

    @Override
    public boolean equals(Object obj)
    {
        if(!(obj instanceof ForAllEffect)) return false;
        return ((ConditionalEffect)obj).toString().equals(toString());
    }
}
