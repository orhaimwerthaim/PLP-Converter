package plp.objects;

import java.util.ArrayList;

public class PlanningTypedParameter {
    private String Name;
    public String Type;
    public ArrayList<String> Types;

    public String ChangeNameTo;
    private String OriginalName;
/*
    public PlanningTypedParameter(String name, String type)
    {
        setName(name);
        OriginalName = name;
        Type = type;
        Types = new ArrayList<>();
        Types.add(type);
    }*/

    public PlanningTypedParameter(String name, ArrayList<String> types)
    {
        setName(name);
        OriginalName = name;
        Type = types == null || types.size() == 0 ? null : types.get(0);
        Types = Type == null ? new ArrayList<>() : new ArrayList<>(types);
    }

    public void setName(String name)
    {
        Name = name != null ? ((name.startsWith("?") || name.startsWith("$")) ? name : "?" + name)
        : (Name.startsWith("?") ? Name : "?" + Name);
    }

    public String getName()
    {
        return Name;
    }

    public boolean IsActualObject_NotParam()
    {
        return Name.startsWith("$");
    }

    public String getName_Java()
    {
        return Name.replace("?","").replace("$","");
    }

    public String getOriginalName()
    {
        return OriginalName;
    }

    public String getParamType()
    {
        return Type;
    }
    @Override
    public int hashCode() {
        return (Name != null ? Name.hashCode() : 0) + 1000*(Type!=null ? Type.hashCode():0);
    }

    @Override
    public String toString() {
        return "Name:"+Name+", Type:"+Type;
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof PlanningTypedParameter))return false;
        PlanningTypedParameter pObj = (PlanningTypedParameter)obj;
        return Name == pObj.Name && Type == pObj.Type;
    }
}
