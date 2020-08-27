package plp;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import plp.objects.PlanningStateVariable;
import plp.objects.Predicate;
import plp.objects.effect.ConditionalEffect;
import plp.objects.effect.EEffectType;
import plp.objects.effect.Effect;
import plp.objects.effect.IEffect;
import plp.environment_file_objects.InitialState;
import plp.environment_file_objects.StateVariableWithValue;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

public class EnvironmentFile {
    public static String GoalReachedStateVariableName = "goal_reached";
    public int GoalReachedReward;
    public String Name;
    public int MaxConcurrentActions;
    public int Horizon;
    public double Discount;
    public Effect GoalReachedEffect;
    public HashMap<String, ArrayList<String>> ObjectsByType = new HashMap<>();
    public ArrayList<PlanningStateVariable> StateVariables = new ArrayList<>();
    public ArrayList<StateVariableWithValue> ConstantsAssignment = new ArrayList<>();
    public InitialState initalState;

    public EnvironmentFile(String filePath) throws Exception {
        File xmlFile = new File(filePath);
        String fileName = xmlFile.getName();

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(xmlFile);
        Element root = doc.getDocumentElement();

        this.Name = root.getAttribute("name");
        if (!fileName.equals(this.Name + ".xml")) {
            throw new Exception("The environment file 'name' attribute is not equal to environment file file name");
        }
        MaxConcurrentActions = Integer.parseInt(root.getAttribute("max_concurrent_actions"));
        Horizon = Integer.parseInt(root.getAttribute("horizon"));
        Discount = Double.parseDouble(root.getAttribute("discount"));

        InitObjectsByType(doc);
        InitStateVariables(doc);
        InitIntermediateVariables(doc);
        InitGoalEffects(doc);
        InitConstantsAssignment(doc);
        InitInitialState(doc);
    }

    private void InitInitialState(Document doc) throws Exception {
        Element parent = (Element)doc.getElementsByTagName("initial_state").item(0);
        initalState = new InitialState(parent);
    }

    private void InitConstantsAssignment(Document doc) throws Exception {
        Element parent = (Element)doc.getElementsByTagName("assign_constants_values").item(0);
        NodeList nl = parent.getElementsByTagName("state_variable_with_value");
        for (int i = 0; i < nl.getLength(); i++)
        {

            ConstantsAssignment.add(new StateVariableWithValue(nl.item(i)));
        }
    }

    private void InitGoalEffects(Document doc) throws Exception {
        Node goalNode = doc.getElementsByTagName("goal_state").item(0);
        GoalReachedReward = Integer.parseInt(((Element) goalNode).getAttribute("goal_reward"));

        ConditionalEffect conEffect= new ConditionalEffect(goalNode, StateVariables, null);

        Predicate pred = new Predicate();
        pred.Name = GoalReachedStateVariableName;
        conEffect.effect = new Effect(EEffectType.Predicate, pred);
        conEffect.effect.effect.setEffectingUpon(IEffect.EEffectingUpon.always);
        GoalReachedEffect = new Effect(EEffectType.Conditional, conEffect);
    }


    protected void InitObjectsByType(Document doc) throws Exception {
        NodeList objectsBlockNodes = doc.getElementsByTagName("objects_declaration");
        if (objectsBlockNodes.getLength() == 0) {
            return;
        }
        NodeList typed_objectsNodes = ((Element)objectsBlockNodes.item(0)).getElementsByTagName("typed_objects");
        for (int i = 0; i < typed_objectsNodes.getLength(); i++) {
            Node paramNode = typed_objectsNodes.item(i);
            if (paramNode.getNodeType() == Node.ELEMENT_NODE) {
                Element paramElement = (Element) paramNode;
                if (paramElement.hasAttribute("type")) {
                    String type = paramElement.getAttribute("type");
                    if (type.equals("object")) {
                        throw new Exception("Environment File xml exception: object type cannot be called 'object' (see 'typed_objects.type'");

                    }
                    ArrayList<String> objects = new ArrayList<>();
                    NodeList nl = paramElement.getElementsByTagName("object");
                    for (int j = 0; j < nl.getLength(); j++) {
                        if ((nl.item(j).getNodeType() == Node.ELEMENT_NODE) && ((Element) nl.item(j)).hasAttribute("name")) {
                            objects.add(((Element) nl.item(j)).getAttribute("name"));
                        }
                    }
                    if (objects.size() > 0) {
                        ObjectsByType.put(type, objects);
                    }
                }
            }
        }
    }

    protected void InitStateVariables(Document doc) throws Exception {
        NodeList nodes = doc.getElementsByTagName("state_variable_types").item(0).getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            if (!(nodes.item(i).getNodeType() == Node.ELEMENT_NODE &&
                    ((Element) nodes.item(i)).getTagName().equals("state_variable"))) {
                continue;
            }

            Node node = nodes.item(i);
            Element varElement = (Element) node;

            PlanningStateVariable var = new PlanningStateVariable();

            var.Name = varElement.getAttribute("name");
            var.Type = varElement.getAttribute("type");
            var.IsConstant = varElement.getAttribute("is_constant").toLowerCase().equals("true");
            var.IsGlobalIntermediate = false;
            var.IsObservation = false;
            var.setDefault(varElement.getAttribute("default"));

            NodeList childNodes = node.getChildNodes();

            for (int j = 0; j < childNodes.getLength(); j++) {
                Node cNode = childNodes.item(j);
                if (cNode.getNodeType() == cNode.ELEMENT_NODE) {
                    var.ParameterTypes.add(cNode.getTextContent());
                    if(!ObjectsByType.containsKey(cNode.getTextContent()))
                    {
                        throw new Exception("Environment File xml exception: Environment File defines a parameter type '"+cNode.getTextContent()+"' that was not defined in 'typed_objects.type'");
                    }
                }
            }
            StateVariables.add(var);

        }


        //goal reached state variable
        PlanningStateVariable goalReachedVar = new PlanningStateVariable();
        goalReachedVar.Name = GoalReachedStateVariableName;
        goalReachedVar.IsConstant = false;
        goalReachedVar.IsObservation = true;
        goalReachedVar.IsGlobalIntermediate=false;
        goalReachedVar.Type = "bool";
        goalReachedVar.setDefault("false");
        StateVariables.add(goalReachedVar);
    }


    protected void InitIntermediateVariables(Document doc) throws Exception {
        NodeList nodes = doc.getElementsByTagName("intermediate_variables").item(0).getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            if (!(nodes.item(i).getNodeType() == Node.ELEMENT_NODE &&
                    ((Element) nodes.item(i)).getTagName().equals("intermediate_variable"))) {
                continue;
            }

            Node node = nodes.item(i);
            Element varElement = (Element) node;

            PlanningStateVariable var = new PlanningStateVariable();

            var.Name = varElement.getAttribute("name");
            var.Type = varElement.getAttribute("type");
            var.IsConstant = false;
            var.IsObservation = false;
            var.IsGlobalIntermediate = true;
            try {
                var.GlobalIntermediateLevel = Integer.parseInt(varElement.getAttribute("intermediate_level"));
            } catch (Exception e) {
                var.GlobalIntermediateLevel = 2;
            }

            var.setDefault(varElement.getAttribute("default"));

            NodeList childNodes = node.getChildNodes();

            for (int j = 0; j < childNodes.getLength(); j++) {
                Node cNode = childNodes.item(j);
                if (cNode.getNodeType() == cNode.ELEMENT_NODE) {
                    var.ParameterTypes.add(cNode.getTextContent());
                    if(!ObjectsByType.containsKey(cNode.getTextContent()))
                    {
                        throw new Exception("Environment File xml exception: Environment File defines a parameter type '"+cNode.getTextContent()+"' that was not defined in 'typed_objects.type'");
                    }
                }
            }
            StateVariables.add(var);
        }
    }
}
