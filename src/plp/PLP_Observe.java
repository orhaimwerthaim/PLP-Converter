package plp;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import plp.objects.Predicate;
import plp.objects.ProbabilityType;
import plp.objects.condition.Condition;

public class PLP_Observe extends PLP {
    public ProbabilityType failure_to_observe_probability;
    public ProbabilityType probabilityGivenObservedValue;
    public Predicate observationGoalPredicate;
    public PLP_Observe(Document doc,  ProblemFile pf) throws Exception {
        super(doc, PLP_TYPE.OBSERVE, pf);

        Node node = doc.getElementsByTagName("failure_to_observe_probability").item(0);
        failure_to_observe_probability = new ProbabilityType(node, pf.StateVariables);

        node = doc.getElementsByTagName("probability_given_observed_value").item(0);
        if(node != null) {
            probabilityGivenObservedValue = new ProbabilityType(node, pf.StateVariables);
        }

        node = doc.getElementsByTagName("observation_goal_parameter").item(0);
        node = ((Element)node).getElementsByTagName("param").item(0);
        observationGoalPredicate = new Predicate(node, pf.StateVariables, null);
    }
}