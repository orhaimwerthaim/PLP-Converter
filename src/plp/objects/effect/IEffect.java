package plp.objects.effect;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

public interface IEffect {
    EEffectingUpon getEffectingUpon();
    void setEffectingUpon(EEffectingUpon upon);
    String getName();
    String getEffectAssignedValue();
    public static EEffectingUpon  GetEffectingUponFromNode(Node node)
    {
        if((node.getNodeType() == Node.ELEMENT_NODE))
        {
            return ((Element)node).hasAttribute("effecting_upon") ?
                    EEffectingUpon.valueOf(((Element)node).getAttribute("effecting_upon")) :
                    EEffectingUpon.always;
        }
        throw new UnsupportedOperationException("Node is not an element");
    }
    public enum EEffectingUpon {
        always,
        success,
        failure
    }
}

