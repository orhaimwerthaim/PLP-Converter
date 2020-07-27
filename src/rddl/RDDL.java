package rddl;

import jdk.jshell.execution.Util;
import plp.ProblemFile;
import utils.Utils;

import java.io.*;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Scanner;

public class RDDL {
    public static String DOMAIN_NAME = "plps_domain";
    public static String INSTANCE_NAME = "created_plps_instance";

    public static String INIT_STATE_DOMAIN_NAME = "init_domain";
    public static String INIT_STATE_INSTANCE_NAME = "init_instance";

    private String CREATED_RDDL_DIRECTORY = "/Created_RDDLs/";
    private String CREATED_RDDL_INIT_STATE_DIRECTORY = "/Created_RDDLs/InitStateRDDLs/";
    HashMap<EBlocks, String> domainFileContent = new HashMap<>();
    HashMap<EInstaceBlocks, String> instanceFileContent = new HashMap<>();
    //line1 + "\n" + line2;
    public enum EBlocks
    {
        types,
        pvariables,
        cpfs,
        reward,
        StateActionConstraints,
        types_for_initial_stats,
        cpfs_for_initial_stats,
        pvariables_for_initial_stats
    }

    public enum EInstaceBlocks
    {
        non_fluents,
        init_state,
        objects
    }

    enum EState
    {
        Unknown,
        In_Domain,
        End_DomainHeading,
        In_Types,
        In_Pvariables,
        In_CPFS,
        In_Reward,
        In_StateActionConstraints
    }

    /*private void GetRDDLContent(String rddlFilePath) throws FileNotFoundException {
        Scanner scnr = new Scanner(new File(rddlFilePath));
        EState state = EState.Unknown;
        int inPvarOpen = 0;
        while(scnr.hasNextLine())
        {
            String line = scnr.nextLine().trim();
            line = line.indexOf("//") < 0 ? line : line.substring(0, line.indexOf("//"));
            if(line.length() == 0 || line.startsWith("//"))
            {
                continue;
            }
            switch (state)
            {
                case Unknown:
                    if(line.startsWith("domain"))
                    {
                        state = EState.In_Domain;
                        domainFileContent.put(EBlocks.Header, line);
                        state = (line.contains("{")) ? EState.End_DomainHeading : EState.In_Domain;
                    }
                    break;
                case In_Domain:
                    if(line.startsWith("{"))
                    {
                        domainFileContent.put(EBlocks.Header, domainFileContent.get(EBlocks.Header)+ "\n" + line);
                        state = EState.End_DomainHeading;
                    }
                    break;
                case In_Types:
                    domainFileContent.put(EBlocks.types, domainFileContent.get(EBlocks.types)+ "\n\t" + line);
                    break;
                case In_Pvariables:
                    inPvarOpen += Utils.CountCharInString('{', line);
                    inPvarOpen -= Utils.CountCharInString('}', line);
                    if(inPvarOpen == 0)
                    {
                        state = EState.End_DomainHeading;
                    }
                    domainFileContent.put(EBlocks.pvariables, domainFileContent.get(EBlocks.pvariables)+ "\n\t" + line);
                    break;
                case In_CPFS:
                    domainFileContent.put(EBlocks.cpfs, domainFileContent.get(EBlocks.cpfs)+ "\n\t" + line);
                    break;
                case In_Reward:
                    domainFileContent.put(EBlocks.reward, domainFileContent.get(EBlocks.reward)+ "\n\t" + line);
                    break;
                case In_StateActionConstraints:
                    domainFileContent.put(EBlocks.StateActionConstraints, domainFileContent.get(EBlocks.StateActionConstraints)+ "\n\t" + line);
                    break;
                case End_DomainHeading:

                    if(line.startsWith("types"))
                    {
                        domainFileContent.put(EBlocks.types, line);
                        state = EState.In_Types;
                    }
                    if(line.startsWith("pvariables"))
                    {
                        domainFileContent.put(EBlocks.pvariables, line);
                        state = EState.In_Pvariables;
                        inPvarOpen += Utils.CountCharInString('{', line);
                        inPvarOpen -= Utils.CountCharInString('}', line);
                        if(inPvarOpen == 0)
                        {
                            state = EState.End_DomainHeading;
                        }
                    }
                    if(line.startsWith("cpfs"))
                    {
                        domainFileContent.put(EBlocks.cpfs, line);
                        state = EState.In_CPFS;
                    }
                    if(line.startsWith("reward"))
                    {
                        domainFileContent.put(EBlocks.reward, line);
                        state = EState.In_Reward;
                    }
                    if(line.startsWith("state-action-constraints"))
                    {
                        domainFileContent.put(EBlocks.StateActionConstraints, line);
                        state = EState.In_StateActionConstraints;
                    }
            }
            if((line.endsWith("};") && state != EState.In_Pvariables) || (line.endsWith(";") && state == EState.In_Reward))
            {
                state = EState.End_DomainHeading;
            }
        }
    }
*/
    public void AppendLineToRDDL_InstanceBlock(EInstaceBlocks block, String line)
    {
        line = line.trim();
        String newBlock = "";
        //make sure start blocks exist
        if(!instanceFileContent.containsKey(block))
        {
            //StateActionConstraints is 'state-action-constraints' and reward is not surrounded with '{','}'
            newBlock = (block == EInstaceBlocks.non_fluents) ? "non-fluents {\n\t};" :
                    (block == EInstaceBlocks.objects) ? "objects {\n\t};" :
                            (block == EInstaceBlocks.init_state) ? "init-state {\n\t};" : "";
        }
        else
        {
            newBlock = instanceFileContent.get(block);
        }

        //remove block closing characters
        newBlock = newBlock.substring(0, newBlock.length()-4);
        newBlock = newBlock + "\n\t\t" + line + "\n\t};";
        instanceFileContent.put(block, newBlock);
    }

    public void AppendLineToRDDL_DomainBlock(EBlocks block, String line)
    {
        line = line.trim();
        String newBlock = "";
        //make sure start blocks exist
        if(!domainFileContent.containsKey(block)) {
            //StateActionConstraints is 'state-action-constraints' and reward is not surrounded with '{','}'
            newBlock = (block == EBlocks.StateActionConstraints) ? "state-action-constraints {};" :
                    (block == EBlocks.cpfs_for_initial_stats) ? "cpfs {};" :
                            (block == EBlocks.types_for_initial_stats) ? "types {};" :
                            (block == EBlocks.pvariables_for_initial_stats) ? "pvariables {};" :
                    (block == EBlocks.reward ? block.toString() + "=;" :
                                    block.toString() + " {};");
        }
        else
        {
            newBlock = domainFileContent.get(block);
        }

        //remove block closing characters
        newBlock = newBlock.substring(0, (block == EBlocks.reward ? newBlock.length()-1 : newBlock.length()-2));
        newBlock = newBlock + "\n\t" + line +  (block == EBlocks.reward ? ";" : "};");
        domainFileContent.put(block, newBlock);
    }

    public RDDL(/*String templatePath*/) throws FileNotFoundException, URISyntaxException {
        //GetRDDLContent(templatePath);
    }

    public void WriteToInstanceFile(ProblemFile pf, boolean forInitState) throws URISyntaxException {
        String fileName = forInitState ? "created_init_instance.rddl2" : "instance.rddl2";
        ;
        String path = Utils.GetApplicationExecutablePath() +
                (forInitState ? CREATED_RDDL_INIT_STATE_DIRECTORY : CREATED_RDDL_DIRECTORY)
                + fileName;

        File directory = new File(path.substring(0, path.lastIndexOf("/")));
        if (!directory.exists()) {
            directory.mkdir();
            // If you require it to make the entire directory path including parents,
            // use directory.mkdirs(); here instead.
        }

        String nfName = forInitState ? "nf_init" : "nf_plps";
        String domainName = forInitState ? INIT_STATE_DOMAIN_NAME : DOMAIN_NAME;
        String instanceName = forInitState ? INIT_STATE_INSTANCE_NAME : INSTANCE_NAME;
        try (FileWriter writer = new FileWriter(path);
             BufferedWriter bw = new BufferedWriter(writer)) {

            bw.write("non-fluents " + nfName + " { \n\t" +
                    "domain = " + domainName + ";\n\t" +
                    instanceFileContent.get(EInstaceBlocks.objects) + "\n\t" +
                    instanceFileContent.get(EInstaceBlocks.non_fluents) + "\n" +
                    "}\n\n\n" +
                    "instance " + instanceName + " {\n\t" +
                    "domain = " + domainName + ";\n\n\t" +
                    "non-fluents = " + nfName + ";\n\n\t" +
                    instanceFileContent.get(EInstaceBlocks.init_state) + "\n\n\t" +
                    "max-nondef-actions = " + pf.MaxConcurrentActions + ";\n\t" +
                    "horizon = " + pf.Horizon + ";\n\t" +
                    "discount = " + pf.Discount + ";\n" +
                    "}"
            );

        } catch (IOException e) {
            System.err.format("IOException: %s%n", e);
        }
    }

    public void WriteToInitStateDomainFile(ProblemFile pf) throws URISyntaxException {
        String fileName = "created_init_state_domain.rddl";
        String path = Utils.GetApplicationExecutablePath() + CREATED_RDDL_INIT_STATE_DIRECTORY + fileName;

        File directory = new File(path.substring(0, path.lastIndexOf("/")));
        if (! directory.exists()){
            directory.mkdir();
            // If you require it to make the entire directory path including parents,
            // use directory.mkdirs(); here instead.
        }

        if (pf.initalState.InitialStateOptions.size() > 0)
        {
            String values = "";
            for(int i = 1; i <= pf.initalState.InitialStateOptions.size(); i++)
            {
                String optName = "@opt" + i;
                values += (i == pf.initalState.InitialStateOptions.size()) ? optName : optName + ",";
            }
            AppendLineToRDDL_DomainBlock(EBlocks.types_for_initial_stats, "enum_level : { " + values + " };");
            AppendLineToRDDL_DomainBlock(EBlocks.pvariables_for_initial_stats, "opt : {interm-fluent, enum_level, level = 1};");



            String optCPFS = "opt = Discrete ( enum_level ,";
            for(int i = 0; i < pf.initalState.InitialStateOptions.size(); i++)
            {
                        optCPFS += "@opt"+(i+1)+" :  "+
                        Utils.DecimalToString(pf.initalState.InitialStateOptions.get(i).weight)+
                                (i + 1 == pf.initalState.InitialStateOptions.size() ? " );" : " ,");
            }
            AppendLineToRDDL_DomainBlock(EBlocks.cpfs_for_initial_stats, optCPFS);

        }

        try (FileWriter writer = new FileWriter(path);
             BufferedWriter bw = new BufferedWriter(writer)) {

            bw.write( "//" + "\n\n" +
                    "domain " + INIT_STATE_DOMAIN_NAME + " {\n\n" +
                    (domainFileContent.containsKey(EBlocks.types_for_initial_stats) ? domainFileContent.get(EBlocks.types_for_initial_stats) : "") + "\n\n" +
                    (domainFileContent.containsKey(EBlocks.pvariables_for_initial_stats) ? domainFileContent.get(EBlocks.pvariables_for_initial_stats) : "") + "\n\n" +
                    (domainFileContent.containsKey(EBlocks.cpfs_for_initial_stats) ? domainFileContent.get(EBlocks.cpfs_for_initial_stats) : "") + "\n\n" +
                    "reward= 0;\n}");

        } catch (IOException e) {
            System.err.format("IOException: %s%n", e);
        }
    }

    public void WriteToDomainFile() throws URISyntaxException {
        String fileName = "created_rddl_domain.rddl";
        String path = Utils.GetApplicationExecutablePath() + CREATED_RDDL_DIRECTORY + fileName;

        File directory = new File(path.substring(0, path.lastIndexOf("/")));
        if (! directory.exists()){
            directory.mkdir();
            // If you require it to make the entire directory path including parents,
            // use directory.mkdirs(); here instead.
        }

        try (FileWriter writer = new FileWriter(path);
             BufferedWriter bw = new BufferedWriter(writer)) {

            bw.write( "//" + "\n\n" +
                            "domain " + DOMAIN_NAME + " {\n\n" +
                            (domainFileContent.containsKey(EBlocks.types) ? domainFileContent.get(EBlocks.types) : "") + "\n\n" +
                            (domainFileContent.containsKey(EBlocks.pvariables) ? domainFileContent.get(EBlocks.pvariables) : "") + "\n\n" +
                            (domainFileContent.containsKey(EBlocks.cpfs) ? domainFileContent.get(EBlocks.cpfs) : "") + "\n\n" +
                            (domainFileContent.containsKey(EBlocks.reward) ? domainFileContent.get(EBlocks.reward) : "") + "\n\n" +
                            (domainFileContent.containsKey(EBlocks.StateActionConstraints) ? domainFileContent.get(EBlocks.StateActionConstraints) : "") + "\n" +
                            "}"
                    );

        } catch (IOException e) {
            System.err.format("IOException: %s%n", e);
        }
    }

    public static String GetFixedDefaults(String type) throws Exception {
        switch (type) {
            case "boolean":
            case "bool":
                return "false";
            case "real":
                return "0.0";
            case "integer":
            case "int":
                return "0";
        }
        throw new Exception("This Type does not have a fixed default and default was not selected");
    }

    public static String GetRDDLType(String type) throws Exception {
        switch (type) {
            case "boolean":
            case "bool":
                return "bool";
            case "real":
                return "rel";
            case "integer":
            case "int":
                return "int";
        }
        throw new Exception("This pvariable type is not supported:('" + type + "')");
    }
}
