package plp2java;

import plp.PLP;

import java.net.URISyntaxException;
import java.util.ArrayList;

import static convert.PLP_Converter.GetObservationValueName;


public class MicroStateObservation_Writer {
    String observationsVariables;
    String observationsToString;

    public MicroStateObservation_Writer(ArrayList<PLP> plps) throws URISyntaxException {
        observationsVariables = "    public boolean goal_reached;\r\n";
        observationsVariables += "    public boolean invalid_action_observation;\r\n";

        observationsToString = "\"goal_reached:\" + goal_reached";
        observationsToString +=" + \" | \" +\n" +
                "                \"invalid_action_observation:\" + invalid_action_observation";
        for (PLP plp:
                plps) {
            String plpObsVar = GetObservationValueName(plp);
            observationsVariables += "    public int "+plpObsVar+";\r\n";
            observationsToString +=" + \" | \" +\n" +
                    "                \""+plpObsVar+":\" + " + plpObsVar;
        }

        FileWriter.WriteToFile("MicroStateObservation.java", this.toString());
    }
    @Override
    public String toString() {
        String str = "package JavaSim2POMCP.POMCP.JavaGeneratos.Generated;\r\n\r\n"+
                "public class MicroStateObservation\r\n{\r\n"+observationsVariables+
                "\r\n\r\n"+
                "\r\n    public MicroStateObservation()\r\n    {\r\n        this(false);\r\n    }\r\n\r\n    public MicroStateObservation(boolean IsObservationForInvalidAction)\r\n    {\r\n        if(IsObservationForInvalidAction)\r\n        {\r\n            invalid_action_observation=true;\r\n        }\r\n    }\r\n"+
                "    @Override\r\n    public String toString() {\r\n        return "+
                observationsToString + ";\r\n    }\r\n}\r\n";
        return str;
    }
}
