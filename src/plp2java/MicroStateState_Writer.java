package plp2java;

import plp.PLP;
import plp.EnvironmentFile;
import plp.objects.PlanningStateVariable;

import java.net.URISyntaxException;
import java.util.ArrayList;

public class MicroStateState_Writer {
    String variableDefinitions;
    String constructorVarInit;
    String toStringPart1;
    String toStringPart2;
    public MicroStateState_Writer(EnvironmentFile pf, ArrayList<PLP> plps) throws URISyntaxException {
        variableDefinitions = "";
        constructorVarInit = "";
        toStringPart1 = "";
        toStringPart2 = "";

        boolean first= true;
        for (PlanningStateVariable v:
                pf.StateVariables) {
            if(!v.IsGlobalIntermediate && !v.IsConstant && !v.IsObservation)
            {
                variableDefinitions += "    public ArrayList<ParameterizedVar<Boolean>> "+v.Name+";\r\n";
                constructorVarInit += "        "+v.Name+" = new ArrayList<>();\n";
                toStringPart1 += "        String s_" + v.Name + " = "+v.Name+".stream().filter(x-> x.value).map(x-> \""+v.Name+"\" + x.toString())\r\n" +
                        "                .collect(Collectors.joining(\",\"));\r\n\r\n";

                toStringPart2 += first ? " s_" + v.Name :
                    " + \" | \" + s_" + v.Name;
                first= false;
            }
        }

        FileWriter.WriteToFile("MicroStateState.java", this.toString());
    }
    @Override
    public String toString() {
        String str = "package JavaSim2POMCP.POMCP.JavaGeneratos.Generated;\r\n\r\nimport JavaSim2POMCP.POMCP.JavaGeneratos.fixed.ParameterizedVar;\r\n\r\nimport java.util.ArrayList;\r\nimport java.util.stream.Collectors;\r\n\r\npublic class MicroStateState{\r\n"+
                variableDefinitions + "    public MicroStateState()\r\n    {\r\n"+
                constructorVarInit +"    }\r\n\r\n    @Override\r\n    public String toString() {\r\n" +
                toStringPart1 + "        return"+
                toStringPart2 + ";\r\n    }\r\n}\r\n";
        return str;
    }
}
