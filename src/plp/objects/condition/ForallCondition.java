package plp.objects.condition;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import plp.objects.ICondition;
import plp.objects.PlanningTypedParameter;

import java.util.ArrayList;

public class ForallCondition implements ICondition {
    public String Name;
    public ArrayList<String> Params = new ArrayList<>();

    public ForallCondition(Node node) throws Exception {
        throw new UnsupportedOperationException();
//        Element el = (Element)node;
//        Name = el.getAttribute("name");
//        NodeList nl = ((Element) node).getElementsByTagName("field");
//        for(int i = 0; i < nl.getLength(); i++)
//        {
//            Node n = nl.item(i);
//            if(n.getNodeType() == Node.ELEMENT_NODE) {
//                Params.add(((Element)n).getAttribute("value"));
//            }
//        }
    }


    @Override
    public ArrayList<PlanningTypedParameter> GetParams() {
        throw  new UnsupportedOperationException();
    }

    @Override
    public String getConditionForIf(ArrayList<PlanningTypedParameter> assignToParams, boolean underAndOrCondition, boolean IsObservationEffected) {
        throw  new UnsupportedOperationException();
    }


    @Override
    public String getConditionForIf_JavaNextState(ArrayList<PlanningTypedParameter> assignToParams, boolean underAndOrCondition, boolean IsObservationEffected) {
        return null;
    }

    @Override
    public String getConditionForIf_JavaValidActions(ArrayList<PlanningTypedParameter> assignToParams, boolean underAndOrCondition, boolean IsObservationEffected) {
        return null;
    }
}
//<xs:complexType name="forall_condition_type">
    //<xs:sequence minOccurs="1" maxOccurs="1">
        //<xs:sequence minOccurs="1" maxOccurs="unbounded">
            //<xs:element name="param">
            //<xs:complexType>
            //<xs:attribute name="name" type="xs:string" use="required" />
            //</xs:complexType>
            //</xs:element>
        //</xs:sequence>
    //<xs:choice minOccurs="1" maxOccurs="1">
        //<xs:element name="predicate_condition" type="predicate_type" />
        //<xs:element name="formula_condition" type="formula_condition_type" />
        //<xs:element name="exists_condition" type="exists_condition_type" />
        //<xs:element name="not_condition" type="not_condition_type" />
        //<xs:element name="AND" type="and_or_condition_type" />
        //<xs:element name="OR" type="and_or_condition_type" />
    //</xs:choice>
    //</xs:sequence>
//</xs:complexType>