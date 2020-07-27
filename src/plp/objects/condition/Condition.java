package plp.objects.condition;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import plp.PLP;
import plp.objects.EConditionType;
import plp.objects.ICondition;
import plp.objects.PlanningStateVariable;
import plp.objects.Predicate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Condition {
    public static List<String> ConditionTags = Arrays.asList(
            new String[]{"predicate_condition", "formula_condition",
                    "not_condition",
                    "AND", "OR", "exists_condition", "bernoulli_sample_condition"});

    public EConditionType ConditionType;
    public ICondition condition;

    public Condition()
    {

    }

    public Condition(Node node, boolean isParentNodeOfCondition, ArrayList<PlanningStateVariable> declaredStateVariables, PLP plp) throws Exception {
        ConditionType = EConditionType.Invalid;
        NodeList childNodes = isParentNodeOfCondition ? node.getChildNodes() : null;
        for (int i = 0; i < (isParentNodeOfCondition ? childNodes.getLength() : 1); i++) {
            Node childNode = isParentNodeOfCondition ? childNodes.item(i) : node;
            if (childNode.getNodeType() == Node.ELEMENT_NODE) {
                switch (childNode.getNodeName()) {
                    case "predicate_condition":
                        condition = new Predicate(childNode, declaredStateVariables, plp);
                        ConditionType = EConditionType.Predicate;
                        break;
                    case "formula_condition":
                        condition = new FormulaCondition(childNode);
                        ConditionType = EConditionType.Formula;
                        break;
                    case "not_condition":
                        condition = new NotCondition(childNode, declaredStateVariables, plp);
                        ConditionType = EConditionType.Not;
                        break;
                    case "forall_condition_type":
                        condition = new ForallCondition(childNode);
                        ConditionType = EConditionType.ForAll;
                        break;
                    case "exists_condition":
                        condition = new ExistsCondition(childNode, declaredStateVariables, plp);
                        ConditionType = EConditionType.Exists;
                        break;
                    case "AND":
                        condition = new AndOrCondition(childNode, AndOrCondition.EAndOrCondition.And, declaredStateVariables, plp);
                        ConditionType = EConditionType.And;
                        break;
                    case "OR":
                        condition = new AndOrCondition(childNode, AndOrCondition.EAndOrCondition.Or, declaredStateVariables, plp);
                        ConditionType = EConditionType.Or;
                        break;
                    case "bernoulli_sample_condition":
                        condition = new BernoulliCondition(childNode);
                        ConditionType = EConditionType.Bernoulli;
                        break;
                    default:
                        throw new UnsupportedOperationException();
                }
            }
        }
    }
}


