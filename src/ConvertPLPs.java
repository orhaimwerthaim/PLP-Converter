import convert.EConvertType;
import convert.PLP_Converter;
import org.xml.sax.SAXException;
import plp.PLP;
import plp.PLPUtils;
import plp.EnvironmentFile;
import utils.KeyValuePair;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class ConvertPLPs {
    private static ArrayList<String> GetXMLFilesName(String file_or_directory)
    {
        ArrayList<String> result = new ArrayList<>();

        File f = new File(file_or_directory);
        if (f.isDirectory()) {
            for (File f2 : f.listFiles())
                if (f2.getName().endsWith(".xml")) {
                    result.add(f2.getAbsolutePath());
                }
        }
        else
        {
            if (f.getName().endsWith(".xml")) {
                result.add(f.getAbsolutePath());
            }
        }
        return result;
    }

    private static KeyValuePair<EnvironmentFile, ArrayList<PLP>> LoadPLPs(String plp_file_or_dir) throws Exception {

        ArrayList<PLP> plps = new ArrayList<>();
        ArrayList<String> xmlFileNames = GetXMLFilesName(plp_file_or_dir);
        String environmentFilePath =
                xmlFileNames.stream().filter(s-> (new File(s)).getName().startsWith("environment")).toArray()[0].toString();

        EnvironmentFile environmentFile = new EnvironmentFile(environmentFilePath);

        for(String s: xmlFileNames)
        {
            if(!s.equals(environmentFilePath))
            {
                PLP p = PLPUtils.GetPLP(s, environmentFile);
                if (p != null) {
                    plps.add(p);
                }
            }
        }
        return new KeyValuePair<>(environmentFile,plps);
    }

    private static String GetArgByType(String type, String[] args)
    {
        if (Arrays.stream(args).filter(x-> x.startsWith(type + "=")).count() == 0) return null;
        String var = Arrays.stream(args).filter(x-> x.startsWith(type + "=")).toArray()[0].toString();
        return var.substring(type.length()+1);
    }

    public static void main(String[] args) throws Exception, IOException, SAXException, ParserConfigurationException {


        String plpDirOrFilePath = GetArgByType("plp_src_dir", args);
        String sConvertTo = GetArgByType("convertTo", args);
        sConvertTo = sConvertTo == null ? "javaSim" : sConvertTo.toLowerCase();
        String simulatorPath = GetArgByType("solver_dir", args);

        EConvertType convertType =  sConvertTo.equals("rddl") ? EConvertType.RDDL_FromPLP :
                sConvertTo.equals("rddlfromenv") ? EConvertType.RDDL_InitialStateFromEnvironmentFile :
                        EConvertType.JavaSimulatorFromPLP;

        if(simulatorPath != null)
        {
            plp2java.FileWriter.SetJavaSimulatorProjectPath(simulatorPath);
        }

        KeyValuePair<EnvironmentFile, ArrayList<PLP>> plpsWithEnvironmentFile = LoadPLPs(plpDirOrFilePath);

        PLP_Converter obj = new PLP_Converter(plpsWithEnvironmentFile, convertType);




    }


}
