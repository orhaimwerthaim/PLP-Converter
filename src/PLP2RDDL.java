import org.xml.sax.SAXException;
import plp.PLP;
import plp.PLPUtils;
import plp.PLP_TYPE;
import plp.ProblemFile;
import rddl.PLPsToRDDL;
import rddl.RDDL;
import utils.KeyValuePair;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class PLP2RDDL {
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

    private static KeyValuePair<ProblemFile, ArrayList<PLP>> LoadPLPs(String plp_file_or_dir) throws Exception {

        ArrayList<PLP> plps = new ArrayList<>();
        ArrayList<String> xmlFileNames = GetXMLFilesName(plp_file_or_dir);
        String problemFilePath =
                xmlFileNames.stream().filter(s-> (new File(s)).getName().startsWith("problem")).toArray()[0].toString();

        ProblemFile problemFile = new ProblemFile(problemFilePath);

        for(String s: xmlFileNames)
        {
            if(!s.equals(problemFilePath))
            {
                PLP p = PLPUtils.GetPLP(s, problemFile);
                if (p != null) {
                    plps.add(p);
                }
            }
        }
        return new KeyValuePair<>(problemFile,plps);
    }

    public static void main(String[] args) throws Exception, IOException, SAXException, ParserConfigurationException {
        String plpDirOrFilePath = args[0];
        boolean toRddl = args.length > 1 && args[1].toLowerCase().equals("rddl");

        KeyValuePair<ProblemFile, ArrayList<PLP>> plpsWithProblemFile = LoadPLPs(plpDirOrFilePath);

        PLPsToRDDL obj = new PLPsToRDDL(plpsWithProblemFile,
               // "/home/or/PLP2RDDL/PLP2RDDL_JAVA/files/environment/lab_environment_domainTemplate.rddl",
                toRddl);




    }
}
