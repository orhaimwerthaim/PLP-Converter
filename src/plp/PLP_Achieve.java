package plp;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import plp.objects.ProbabilityType;

public class PLP_Achieve extends PLP{
    public ProbabilityType successFailProbability;
    public boolean IsActionSuccessObservable;

        public PLP_Achieve(Document doc, EnvironmentFile pf) throws Exception {
            super(doc, PLP_TYPE.ACHIEVE, pf);
            successFailProbability = InitSuccessProbability(doc, pf);
            IsActionSuccessObservable =
                    ((Element)doc.getElementsByTagName("plps:achieve_plp").item(0))
                            .getAttribute("is_action_success_observable").toLowerCase().equals("true");


        }

    //success_probability
    protected ProbabilityType InitSuccessProbability(Document doc, EnvironmentFile pf) throws Exception {
        Node node = doc.getElementsByTagName("success_probability").item(0);
        return new ProbabilityType(node, pf.StateVariables);
    }
    }
