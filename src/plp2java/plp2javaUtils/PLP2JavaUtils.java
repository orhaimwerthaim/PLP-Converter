package plp2java.plp2javaUtils;

import convert.PLP_Converter;
import plp.objects.PlanningStateVariable;
import plp.objects.PlanningTypedParameter;
import utils.LambdaCounter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

public class PLP2JavaUtils {

    public static PlanningStateVariable GetStateVariableByName(String varName)
    {
        for (PlanningStateVariable var: PLP_Converter.pf.StateVariables
             ) {
            if(var.Name.equals(varName))
            {
                return var;
            }
        }
        return null;
    }
    public static String GetForAllStringClose(int count) {
        String str = "";
        for (int i = count - 1; i >= 0; i--) {
            String padding = "    ".repeat(i);
            str += padding + "        }\r\n";
        }
        return str;
    }


    public static String GetVariablesAssignments(PlanningTypedParameter[] params)
    {
        String result = "";
        for(int i=0;i<params.length;i++)
        {
            result += "String "+params[i].getName_Java()+" = var.params.get("+i+");\n";
        }
        return result;
    }

    public static String GetForAllString(ArrayList<PlanningTypedParameter> paramsOfVar) {
        String str = "";
        for (int i = 0; i < paramsOfVar.size(); i++) {
            String padding = "    ".repeat(i);
            str += padding + "        for (String " + paramsOfVar.get(i).getName_Java() + " :\r\n" +
                    padding + "                " + paramsOfVar.get(i).Type + ") {\r\n";
        }
        return str;
    }

    public static String GetExistsForPredicate(PlanningTypedParameter[] paramsOfVar, ArrayList<PlanningTypedParameter> effectParams) {
        paramsOfVar = Arrays.stream(paramsOfVar).filter(var -> !var.getName_Java().startsWith("$")).toArray(size -> new PlanningTypedParameter[size]);
        HashSet<Integer> indexes = new HashSet<>();
        for (int j = 0; j < paramsOfVar.length; j++) {
            PlanningTypedParameter p = paramsOfVar[j];
            boolean hasMatch = false;
            for (int i = 0; i < effectParams.size(); i++) {
                PlanningTypedParameter par = effectParams.get(i);
                if (par.getName_Java().equals(p.getName_Java())) {
                    hasMatch = true;
                    break;
                }
            }
            if (!hasMatch) {
                indexes.add(j);
            }
        }
        return GetExistsForPredicate(paramsOfVar, indexes);
    }

    private static String GetExistsForPredicate(PlanningTypedParameter[] paramsOfVar, HashSet<Integer> indexes)
    {//exists_{?loc : discrete_location,?o : obj}
        String op= "";

        if(indexes == null)//we want for all
        {
            indexes = new HashSet<>();
            for(int i=0; i < paramsOfVar.length; i++)
            {
                indexes.add(i);
            }
        }else if(indexes.size() == 0)return "";

        StringBuilder result = new StringBuilder(op + "{");

        LambdaCounter counter = new LambdaCounter();
        indexes.forEach(index->
        {
            result.append(counter.getCounter() == LambdaCounter.COUNTER_INIT ? "" : ", ");
            counter.Increment();
            result.append(paramsOfVar[index].getName_Java()+" : " + paramsOfVar[index].Type);

        });
        result.append("}");
        return result.toString();
    }
}


