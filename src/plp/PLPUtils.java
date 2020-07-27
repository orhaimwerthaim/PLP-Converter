package plp;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;

public class PLPUtils {
    //get plp type and name.
//throw exception if PLP name is different from it's file name
    public static PLP GetPLP(String filePath, ProblemFile pf) throws Exception {
        File xmlFile = new File(filePath);
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(xmlFile);
        Element root = doc.getDocumentElement();
        String fileName = xmlFile.getName();
        PLP plp = null;
        switch (root.getTagName())
        {
            case "plps:achieve_plp":
            case "achieve_plp":
                plp = new PLP_Achieve(doc, pf);
                break;
            case "plps:detect_plp_type":
            case "detect_plp_type":
                break;
            case "plps:maintain_plp":
            case "maintain_plp":
                break;
            case "plps:observe_plp":
            case "observe_plp":
                plp = new PLP_Observe(doc, pf);
                break;
        }
        if(plp != null && !fileName.equals(plp.getName() + ".xml"))
        {
            throw new Exception("PLP 'name' attribute is not equal to PLP file name ('"+plp.getName() + ".xml')");
        }
        return plp;
    }
}
