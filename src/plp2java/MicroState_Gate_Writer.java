package plp2java;

import plp.EnvironmentFile;
import utils.Utils;

import java.net.URISyntaxException;

public class MicroState_Gate_Writer {
    int horizon;
    String discount;

    public MicroState_Gate_Writer(EnvironmentFile pf) throws URISyntaxException {
        horizon=pf.Horizon;
        discount = Utils.DecimalToString(pf.Discount);

        FileWriter.WriteToFile("MicroState_Gate.java", this.toString());
    }


    @Override
    public String toString() {
        String str = "package JavaSim2POMCP.POMCP.JavaGeneratos.Generated;\r\n\r\nimport JavaSim2POMCP.POMCP.JavaGeneratos.fixed.ActionsDataStore;\r\nimport JavaSim2POMCP.POMCP.JavaGeneratos.fixed.JavaSimulatorUtils;\r\nimport POMDP_Solver.Atom;\r\nimport JavaSim2POMCP.POMCP.JavaGeneratos.fixed.MiniStateDataStore;\r\nimport POMDP_Solver.POMCP_Gate;\r\nimport util.Pair;\r\n\r\nimport java.util.ArrayList;\r\n\r\n"+
                "public class MicroState_Gate implements POMCP_Gate {\r\n\r\n    public MicroState_Gate() throws Exception {\r\n        ActionsDataStore.InitActionsDataStore();\r\n    }\r\n\r\n    @Override\r\n    "+
                "public int GetHorizon() {\r\n        return "+horizon+";\r\n    }\r\n\r\n    @Override\r\n    "+
                "public float GetDiscount() {\r\n        return "+ discount +"f;\r\n    }"+
                "\r\n\r\n    @Override\r\n    public Atom GetObservationFromROS_Response(String rosResponse) {\r\n        String observationTemplate  = null;\r\n        try {\r\n            observationTemplate =  (new MicroStateObservation(false)).toString();//) domainAndProblem.Generator(domainAndProblem.GetPossibleInitialStates(1).get(0), actionHash)._o1.Observation.toString();\r\n            observationTemplate = observationTemplate.replaceAll(\"[0-9]+\", \"0\");\r\n        } catch (Exception e) {\r\n            System.out.println(\"Fatal Exception:\" + e);\r\n        }\r\n\r\n\r\n        String currentStringObservation = observationTemplate;\r\n        String[] observations = rosResponse.split(\",\");\r\n        for (int i = 0; i < observations.length; i++) {\r\n            String[] bits = observations[i].split(\":\");\r\n            currentStringObservation = JavaSimulatorUtils.InsertValueToObservation(currentStringObservation, bits[0], bits[1]);\r\n        }\r\n        return new Atom(currentStringObservation.hashCode(), currentStringObservation);\r\n    }\r\n\r\n\r\n    "
                +"@Override\r\n    public ArrayList<Atom> GetPossibleInitialStates(int numOfStates) throws Exception {\r\n        ArrayList<Atom> res = new ArrayList<>();\r\n        for(int i=0;i<numOfStates;i++) {\r\n            MicroState ms = new MicroState(true);\r\n            Atom mss = MiniStateDataStore\r\n                    .AddStateAndObservation(ms)[0];\r\n            res.add(mss\r\n            );\r\n        }\r\n        return res;\r\n    }\r\n"+
                "    @Override\r\n    public ArrayList<Atom> GetStateActions(Atom state) throws Exception {\r\n        ArrayList<Atom> res =  MiniStateDataStore.GetStateValidActions(state);\r\n        return res;\r\n    }"+
                "\r\n\r\n    @Override\r\n    public Atom GetIllegalActionObservation() {\r\n        return MiniStateDataStore.GetObservationAtom(new MicroStateObservation(true));\r\n    }\r\n\r\n    @Override\r\n    public boolean IsGoalReached(Atom observation) {\r\n        MicroStateObservation observationReceivedO = MiniStateDataStore.GetObservation(observation);\r\n        return observationReceivedO.goal_reached;\r\n    }\r\n\r\n"
                +"@Override\r\n    public Pair<GeneratorResult, Boolean> Generator(Atom state, Atom action) throws Exception {\r\n        boolean validAction = MiniStateDataStore.GetStateValidActions(state).contains(action);\r\n\r\n        Atom nextState = state;\r\n        Atom observation = null;\r\n        double reward = 0;\r\n        if(validAction) {\r\n            Pair<String, String[]> act = ActionsDataStore.GetAction(action);\r\n            ArrayList<Pair<String, String[]>> actions = new ArrayList<>();\r\n            actions.add(act);\r\n            MicroState res = MicroState.NextState(MiniStateDataStore.GetState(state), actions);\r\n            nextState = MiniStateDataStore.GetStateAtom(res.state);\r\n            observation = MiniStateDataStore.GetObservationAtom(res.observation);\r\n            MiniStateDataStore.AddStateAndObservation(res.state, res.observation);\r\n            reward = res.reward;\r\n        }\r\n\r\n        GeneratorResult gRes = new GeneratorResult(nextState, observation, reward);\r\n        return new Pair<>(gRes, validAction);\r\n    }\r\n}\r\n";
        return str;
    }

    }
