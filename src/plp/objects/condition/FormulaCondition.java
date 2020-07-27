package plp.objects.condition;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import plp.objects.EOperator;
import plp.objects.ICondition;
import plp.objects.PlanningTypedParameter;
import plp.objects.Range;
import utils.Utils;

import java.util.ArrayList;

public class FormulaCondition implements ICondition {
    public String FirstExpression;
    public String KeyDescription;
    public EFormulaType FormulaType;//if
    public Range InRange;
    public WithOperator WithOperator;
    ArrayList<PlanningTypedParameter> _assignToParams = new ArrayList<>();
    public FormulaCondition(Node node) throws Exception
    {
        FirstExpression = null;
        InRange = null;
        KeyDescription = ((Element)node).getAttribute("key_description");
        FormulaType = EFormulaType.Invalid;
        FormulaType = FormulaType.Invalid;
        WithOperator withOperator = new WithOperator();

        NodeList childNodes = node.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node childNode = childNodes.item(i);
            if (childNode.getNodeType() == Node.ELEMENT_NODE) {
                switch (childNode.getNodeName())
                {
                    case "operator":
                        withOperator.Operator = Utils.GetOperator(((Element) childNode).getAttribute("type"));
                        break;
                    case "expression":
                        if(FirstExpression == null)
                        {
                            FirstExpression = ((Element) childNode).getAttribute("value");
                        }
                        else {
                            withOperator.SecondExpression = ((Element) childNode).getAttribute("value");
                        }
                        break;
                    case "inside_range":
                        NodeList childNodes2 = node.getChildNodes();

                        for (int j = 0; j < childNodes2.getLength(); j++) {
                            Node childNode2 = childNodes.item(j);

                            if (childNode2.getNodeType() == Node.ELEMENT_NODE &&
                                    childNode2.getNodeName().equals("range"))
                            {
                                InRange = new Range(childNode2);
                            }
                        }
                        break;
                }
            }
        }
        FormulaType = (InRange != null) ? EFormulaType.InRange : EFormulaType.WithOperator;
        this.WithOperator = (InRange == null) ? withOperator : null;
    }

    @Override
    public ArrayList<PlanningTypedParameter> GetParams() {
        return new ArrayList<>();
    }

    @Override
    public String getConditionForIf(ArrayList<PlanningTypedParameter> assignToParams, boolean underAndOrCondition, boolean IsObservationEffected) {
        _assignToParams = assignToParams;
        String result = "";
        if(FormulaType.equals(EFormulaType.InRange))
        {
            result = "((" + FirstExpression + ") >" +
                    (InRange.MinInclusive ? "=" : "") +
                    " (" + InRange.MinValue + ")) " +
                    "^" +
                    "((" + FirstExpression + ") <" +
                    (InRange.MinInclusive ? "=" : "") +
                    " (" + InRange.MaxValue + ")) ";
        }else
        {
            result = "((" + FirstExpression + ") " +
                    Utils.ToStringForRDDL(WithOperator.Operator) +
                    " (" + WithOperator.SecondExpression + "))";
        }
        return "(" + result + ")";
    }


    @Override
    public String getConditionForIf_JavaNextState(ArrayList<PlanningTypedParameter> assignToParams, boolean underAndOrCondition, boolean IsObservationEffected) {
        return null;
    }

    @Override
    public String getConditionForIf_JavaValidActions(ArrayList<PlanningTypedParameter> assignToParams, boolean underAndOrCondition, boolean IsObservationEffected) {
        return null;
    }

    @Override
    public String toString() {
        return getConditionForIf(_assignToParams, false, false);
    }

    //	<xs:complexType name="formula_condition_type">
//		<xs:sequence minOccurs="1" maxOccurs="1">
//			<xs:element name="expression" type="simple_value_type" />
//			<xs:choice minOccurs="1" maxOccurs="1">
//				<xs:sequence minOccurs="1" maxOccurs="1">
//					<xs:element name="operator">
//						<xs:complexType>
//							<xs:attribute name="type" type="operator_type" />
//						</xs:complexType>
//					</xs:element>
//					<xs:element name="expression" type="simple_value_type" />
//				</xs:sequence>
//				<xs:sequence minOccurs="1" maxOccurs="1">
//					<xs:element name="inside_range">
//						<xs:complexType>
//							<xs:sequence minOccurs="1" maxOccurs="1">
//								<xs:element name="range" type="range_type" />
//							</xs:sequence>
//						</xs:complexType>
//					</xs:element>
//				</xs:sequence>
//			</xs:choice>
//		</xs:sequence>
//		<xs:attribute name="key_description" type="xs:string" use="required" />
//	</xs:complexType>
    public class WithOperator
    {
        public String SecondExpression;
        public EOperator Operator;
        public WithOperator()
        {
            SecondExpression = null;
            Operator = null;
        }
    }

    public enum EFormulaType
    {
        Invalid,
        InRange,
        WithOperator
    }

}
