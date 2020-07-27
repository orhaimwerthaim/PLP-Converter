package plp.objects;

import rddl.RDDL;

import java.util.ArrayList;

public class PlanningStateVariable {
    public String Name;
    public boolean IsObservation;
    public boolean IsConstant;
    public String Type;
    public boolean IsGlobalIntermediate;
    public Integer GlobalIntermediateLevel;
    private String Default;


    //ParameterTypes: his items are taken from where the actual variable is defined,
    //not like other properties that are taken from the 'planning_state_variable' XML element
    public ArrayList<String> ParameterTypes;
    public PlanningStateVariable()
    {
        Default = null;
        ParameterTypes = new ArrayList<>();
    }

    public void FillTypesBySameVariable(ArrayList<PlanningTypedParameter> params)
    {
        if(params.size() == ParameterTypes.size())
        {
            for(int i =0; i < params.size(); i++)
            {
                params.get(i).Type = ParameterTypes.get(i);
            }
        }
    }

    @Override
    public int hashCode() {
        return Name != null ? Name.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "Name:"+Name +"("+ParameterTypes.toString()+")";
    }

    public void setDefault(String deafult) {
        Default = deafult;
    }

    public String GetDefault() throws Exception {
        if (Default == null) {
            return RDDL.GetFixedDefaults(Type);
        } else {
            return Default;
        }
    }
}
