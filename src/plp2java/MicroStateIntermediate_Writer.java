package plp2java;

import convert.PLP_Converter;
import plp.PLP;
import plp.EnvironmentFile;
import plp.objects.PlanningStateVariable;

import java.net.URISyntaxException;
import java.util.ArrayList;

public class MicroStateIntermediate_Writer {
    String intermediates;
    public MicroStateIntermediate_Writer(EnvironmentFile pf, ArrayList<PLP> plps) throws URISyntaxException {
        intermediates = "";

        for (PlanningStateVariable v:
             pf.StateVariables) {
            if(v.IsGlobalIntermediate)
            {
                intermediates += "    public boolean "+v.Name+";\r\n";
            }
        }

        for (PLP plp:
             plps) {
            intermediates += "    public boolean "+
                    PLP_Converter.GetActionSuccessIntermName(plp)+";\r\n";
        }
        FileWriter.WriteToFile("MicroStateIntermediate.java", this.toString());
    }
    @Override
    public String toString() {
        String str = "package JavaSim2POMCP.POMCP.JavaGeneratos.Generated;\r\n\r\n"+
                "public class MicroStateIntermediate\r\n{\r\n"+intermediates+"}";
        return str;
    }
}
