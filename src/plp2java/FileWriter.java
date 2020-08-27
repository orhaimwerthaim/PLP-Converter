package plp2java;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

public class FileWriter {

    static String JavaSimDir = "";//""/home/or/IdeaProjects/JavaSim2POMCP_JAVA/src/JavaSim2POMCP/POMCP/JavaGeneratos/Generated/";
    public static void SetJavaSimulatorProjectPath(String solverProjectPath)
    {
        solverProjectPath += solverProjectPath.endsWith("/") ? "" : "/";
        JavaSimDir = solverProjectPath + "src/JavaSim2POMCP/POMCP/JavaGeneratos/Generated/";
    }
    public static void WriteToFile(String fileName, String content) throws URISyntaxException {
        String path = JavaSimDir + fileName;
        File directory = new File(path.substring(0, path.lastIndexOf("/")));
        if (! directory.exists()){
            directory.mkdir();
            // If you require it to make the entire directory path including parents,
            // use directory.mkdirs(); here instead.
        }

        try (java.io.FileWriter writer = new java.io.FileWriter(path);
             BufferedWriter bw = new BufferedWriter(writer)) {

            bw.write( content);

        } catch (IOException e) {
            System.err.format("IOException: %s%n", e);
        }
    }
}
