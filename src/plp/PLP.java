package plp;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import plp.objects.*;
import plp.objects.effect.EEffectType;
import plp.objects.effect.NotEffect;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.stream.Collectors;

public abstract class PLP {
    protected String Name;
    protected PLP_TYPE Type;
    public int ActionCost = 0;
    public SideEffects sideEffects;
    public Preconditions preconditions;
    private ArrayList<PlanningTypedParameter> plp_action_paramsOrdered = new ArrayList<>();//this variable is only to maintain the order of parameters
   // public ArrayList<PlanningStateVariable> stateVariables = new ArrayList<>();

    public PLP(Document doc, PLP_TYPE type, ProblemFile pf)throws Exception {
        Type = type;
        Element root = doc.getDocumentElement();

        this.Name = root.getAttribute("name");
        ActionCost = Integer.parseInt(root.getAttribute("action_cost"));

        InitParams(doc);
        InitSideEffects(doc, pf);
        InitPreconditions(doc, pf);
        //InitStateVariables(doc);
    }


    //
    /*protected void InitStateVariables(Document doc) throws Exception {//<planning_state_variable name="near" is_constant="false" type="boolean">
        NodeList nodes = doc.getElementsByTagName("planning_state_variable_used_in_this_plp");
        for(int i=0; i<nodes.getLength(); i++)
        {
            Node node = nodes.item(i);
            if(node.getNodeType() == Node.ELEMENT_NODE)
            {
                PlanningStateVariable var = new PlanningStateVariable();
                Element paramElement = (Element) node;

                var.Name = paramElement.getAttribute("name");
                var.Type = paramElement.getAttribute("type");
                var.IsConstant = paramElement.getAttribute("is_constant").toLowerCase().equals("true");
                var.IsGlobalIntermediate = paramElement.getAttribute("is_global_intermediate").toLowerCase().equals("true");
                try {
                    var.GlobalIntermediateLevel = Integer.parseInt(paramElement.getAttribute("intermediate_level"));
                }
                catch (Exception e)
                {
                    var.GlobalIntermediateLevel = var.IsGlobalIntermediate  ? 2 : null;
                }
                var.setDefault(paramElement.getAttribute("default"));

                NodeList childNodes = node.getChildNodes();

                for (int j = 0; j < childNodes.getLength(); j++) {
                    Node cNode = childNodes.item(j);
                    if (cNode.getNodeType() == cNode.ELEMENT_NODE) {
                        var.ParameterTypes.add(cNode.getTextContent());
                    }
                }
                stateVariables.add(var);
            }
        }
    //InitSideEffects(doc); needs the state variables initialized, so it is called here
        InitSideEffects(doc);
        InitPreconditions(doc);
    }*/

    //fill 'params' property with all the rddl parameters in the file (they all start with '?')
    protected void InitParams(Document doc)
    {//<planning_plp_action_parameter name="?robot" type="discrete_location"/>
        NodeList paramPNodes = doc.getElementsByTagName("execution_parameters");
        if(paramPNodes.getLength() == 0) {return;}
        NodeList paramNodes =  paramPNodes.item(0).getChildNodes();
        for(int i=0; i<paramNodes.getLength(); i++)
        {
            Node paramNode = paramNodes.item(i);
            if(paramNode.getNodeType() == Node.ELEMENT_NODE)
            {
                Element paramElement = (Element) paramNode;
                if(paramElement.hasAttribute("name"))
                {
                    String name = paramElement.getAttribute("name");
                    name = name.startsWith("?") ? name : "?" + name;
                    ArrayList<String> types = new ArrayList<>();
                    NodeList nl =  paramNode.getChildNodes();
                    for(int j=0;j<nl.getLength();j++)
                    {
                        if((nl.item(j).getNodeType() == Node.ELEMENT_NODE) && ((Element) nl.item(j)).getTagName().equals("field") && ((Element) nl.item(j)).hasAttribute("value"))
                        {
                            types.add(((Element) nl.item(j)).getAttribute("value"));
                        }
                    }
                    String type = paramElement.getAttribute("type");

                    PlanningTypedParameter param = new PlanningTypedParameter(name, types);
                    plp_action_paramsOrdered.add(param);
                }
            }
        }
    }

    protected void InitPreconditions(Document doc, ProblemFile pf)throws Exception {
        Node node = doc.getElementsByTagName("preconditions").item(0);//only one tag with this name in the file
        preconditions =  new Preconditions(node, pf.StateVariables, this);
    }

    protected void InitSideEffects(Document doc, ProblemFile pf) throws Exception {
        Node node = doc.getElementsByTagName("side_effects").item(0);//only one tag with this name in the file
        sideEffects =  new SideEffects(node, pf.StateVariables, this);
        sideEffects.effects.forEach(effect->
        {
            if(effect.effectType == EEffectType.Predicate || effect.effectType == EEffectType.Not)
            {
                Predicate pred = effect.effectType == EEffectType.Predicate ? (Predicate) effect.effect
                        : ((NotEffect) effect.effect).Predicate;

                PlanningStateVariable declarVar = pf.StateVariables.stream().filter(var-> var.Name.equals(pred.Name)).collect(Collectors.toList()).get(0);
                for(int i = 0; i < declarVar.ParameterTypes.size(); i++)
                {
                    pred.Params.get(i).Type = declarVar.ParameterTypes.get(i);
                }
            }
        });
    }

/*//get plp type and name.
//throw exception if PLP name is different from it's file name
protected boolean InitPLP(Element root, String fileName) throws Exception {
        switch (root.getTagName())
        {
            case "plps:achieve_plp":
            case "achieve_plp":
                Type = PLP_TYPE.ACHIEVE;
                break;
            case "plps:detect_plp_type":
            case "detect_plp_type":
                Type = PLP_TYPE.DETECT;
                break;
            case "plps:maintain_plp":
            case "maintain_plp":
                Type = PLP_TYPE.MAINTAIN;
                break;
            case "plps:observe_plp":
            case "observe_plp":
                Type = PLP_TYPE.OBSERVE;
                break;
            default:
                Type = PLP_TYPE.INVALID;
                Name = null;
                return false;
        }
        Name = (getType() == PLP_TYPE.INVALID) ? null : root.getAttribute("name");
        if(!fileName.equals(getName() + ".xml"))
        {
            throw new Exception("PLP 'name' attribute is not equal to PLP file name");
        }
        return true;
    }*/



    public String getName() {
        return Name;
    }

    public String PlpNameWithParams(boolean byType)
    {
        return PlpNameWithParams(byType, null);
    }

    public String PlpNameWithParams(ArrayList<PlanningTypedParameter> assignToParams)
    {
        return PlpNameWithParams(false, assignToParams);
    }

    protected String PlpNameWithParams(boolean byType, ArrayList<PlanningTypedParameter> assignToParams)
    {
        String name = Name;
        String sign = "";
        if (byType)
        {
            sign = plp_action_paramsOrdered.size() == 0 ? "" :
                            "(" + plp_action_paramsOrdered.stream().map(var-> var.Type).collect(Collectors.joining(",")) + ")";
        }
        else if (assignToParams == null || assignToParams.size() == 0)
        {
            sign = plp_action_paramsOrdered.size() == 0 ? "" :
                            "(" + plp_action_paramsOrdered.stream().map(var-> var.getName()).collect(Collectors.joining(",")) + ")";
        }
        else
        {
            StringBuilder s = new StringBuilder();
            s.append("(");
            HashSet<Integer> used = new HashSet<>();
            for (PlanningTypedParameter param:plp_action_paramsOrdered) {
                for(int i=0; i < assignToParams.size(); i++)
                {
                    if(!used.contains(i) && param.Type.equals(assignToParams.get(i).Type))
                    {
                        if(used.size() == 0)
                        {
                            s.append(assignToParams.get(i).getName());
                        }
                        else
                        {
                            s.append("," +assignToParams.get(i).getName());
                        }
                        used.add(i);
                    }
                }
            }
            s.append(")");
            sign = s.toString();
        }
        return Name + sign;
    }


    @Override
    public String toString() {
        return PlpNameWithParams(true);
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof PLP)) return false;

        return ((PLP)obj).toString().equals(toString());
    }

    public PLP_TYPE getType() {
        return Type;
    }

    public PlanningTypedParameter[] GetParams()
    {
        return plp_action_paramsOrdered.toArray(new PlanningTypedParameter[plp_action_paramsOrdered.size()]);
        //return plp_action_params.toArray(new PlanningTypedParameter[plp_action_params.size()]);
    }
    //PlanningStateVariable> stateVariables
   /* public PlanningStateVariable[] GetStateVariables()
    {
        return stateVariables.toArray(new PlanningStateVariable[stateVariables.size()]);
    }*/

}


