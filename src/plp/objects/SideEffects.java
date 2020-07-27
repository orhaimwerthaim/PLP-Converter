package plp.objects;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import plp.PLP;
import plp.objects.effect.Effect;

import java.util.ArrayList;

public class SideEffects {
    public ArrayList<Effect> effects = new ArrayList<>();

    public SideEffects(Node node, ArrayList<PlanningStateVariable> declaredStateVariables, PLP plp) throws Exception {
        NodeList childNodes = node.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node childNode = childNodes.item(i);

            if (childNode.getNodeType() == Node.ELEMENT_NODE &&
                Effect.EffectTags.contains(childNode.getNodeName()))
            {
                    effects.add(new Effect(childNode, false, declaredStateVariables, plp));
            }
        }
    }
}
