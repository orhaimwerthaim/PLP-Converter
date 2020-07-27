package plp.objects.effect;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import plp.PLP;
import plp.objects.PlanningStateVariable;
import plp.objects.Predicate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Effect {
    public static List<String> EffectTags = Arrays.asList(new String[]{
            "not_effect", "predicate_effect", "assignment_effect", "conditional_effect"});

    public EEffectType effectType;
    public IEffect effect;

    public Effect(EEffectType _effectType, IEffect _effect)
    {
        effectType = _effectType;
        effect = _effect;
    }

    public Effect(Node node, boolean isParentNodeOfCondition, ArrayList<PlanningStateVariable> declaredStateVariables, PLP plp) throws Exception {
        effectType = EEffectType.Invalid;
        NodeList childNodes = isParentNodeOfCondition ? node.getChildNodes() : null;
        for (int i = 0; i < (isParentNodeOfCondition ? childNodes.getLength() : 1); i++) {
            Node childNode = isParentNodeOfCondition ? childNodes.item(i) : node;
            if (childNode.getNodeType() == Node.ELEMENT_NODE) {
                switch (childNode.getNodeName()) {
                    case "forall_effect":
                        effect = new ForAllEffect(childNode);
                        effectType = EEffectType.ForAll;
                        effect.setEffectingUpon(IEffect.GetEffectingUponFromNode(childNode));
                        break;
                    case "not_effect":
                        effect = new NotEffect(childNode, declaredStateVariables, plp);
                        effectType = EEffectType.Not;
                        effect.setEffectingUpon(IEffect.GetEffectingUponFromNode(childNode));
                        break;
                    case "predicate_effect":
                        effect = new Predicate(childNode, declaredStateVariables, plp);
                        effectType = EEffectType.Predicate;
                        effect.setEffectingUpon(IEffect.GetEffectingUponFromNode(childNode));
                        break;
                    case "assignment_effect":
                        effect = new AssignmentEffect(childNode, declaredStateVariables, plp);
                        effectType = EEffectType.Assignment;
                        effect.setEffectingUpon(IEffect.GetEffectingUponFromNode(childNode));
                        break;
                    case "conditional_effect":
                        effect = new ConditionalEffect(childNode, declaredStateVariables, plp);
                        effectType = EEffectType.Conditional;
                        effect.setEffectingUpon(IEffect.GetEffectingUponFromNode(childNode));
                        break;
                    default:
                        throw new UnsupportedOperationException();
                }
            }
        }
    }
}
