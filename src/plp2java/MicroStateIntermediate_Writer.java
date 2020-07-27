package plp2java;

import plp.PLP;
import plp.ProblemFile;
import plp.objects.PlanningStateVariable;
import rddl.PLPsToRDDL;
import utils.Utils;

import java.net.URISyntaxException;
import java.util.ArrayList;

public class MicroStateIntermediate_Writer {
    String intermediates;
    public MicroStateIntermediate_Writer(ProblemFile pf, ArrayList<PLP> plps) throws URISyntaxException {
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
                    PLPsToRDDL.GetActionSuccessIntermName(plp)+";\r\n";
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
