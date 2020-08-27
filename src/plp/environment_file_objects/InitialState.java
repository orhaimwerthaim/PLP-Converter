package plp.environment_file_objects;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;

public class InitialState {
    public ArrayList<StateVariableWithValue> DeteministicStateAssignments = new ArrayList<>();
    public ArrayList<InitialStateOption> InitialStateOptions = new ArrayList<>();

    public InitialState(Node node) throws Exception {
        Element el = (Element)node;
        NodeList nl = el.getChildNodes();

        ArrayList<InitialStateOption> withoutWeight = new ArrayList<>();
        Double totalWeight = 0.0;

        for(int i = 0; i < nl.getLength(); i++)
        {
            if(nl.item(i).getNodeType() != Node.ELEMENT_NODE)continue;
            Element elm = (Element)nl.item(i);
            if (elm.getTagName().equals("state_variable_with_value"))
            {
                DeteministicStateAssignments.add(new StateVariableWithValue(elm));
            }
            if (elm.getTagName().equals("choice")) {
                NodeList nl2 = elm.getChildNodes();
                for (int j = 0; j < nl2.getLength(); j++) {
                    if (nl2.item(j).getNodeType() != Node.ELEMENT_NODE) continue;
                    Element elm2 = (Element) nl2.item(j);
                    if (elm2.getTagName().equals("option")) {
                        InitialStateOption op= new InitialStateOption(elm2);
                        InitialStateOptions.add(op);
                        if(op.weight == null)
                        {
                            withoutWeight.add(op);
                        }
                        else{
                            totalWeight+=op.weight;
                        }
                    }
                }
            }
        }
        if(withoutWeight.size() > 0)
        {
            Double opWeight = (1.0 - totalWeight)/withoutWeight.size();
            for(int i=0; i < withoutWeight.size(); i++)
            {
                withoutWeight.get(i).weight = opWeight;
            }
        }

        if(totalWeight > 1) throw new Exception("initial_state.choice.option total sum of 'weight' is more than 1");
        if(totalWeight > 0 && totalWeight < 1 && withoutWeight.size() == 0)throw new Exception("initial_state.choice.option total sum of 'weight' is less than 1");
    }
}
