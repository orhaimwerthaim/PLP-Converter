package plp.objects;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class Range {
    public String MinValue;
    public boolean MinInclusive;
    public String MaxValue;
    public boolean MaxInclusive;

    public Range(Node node) {
        Element el = (Element)node;
        MinValue = el.getAttribute("min_value");
        MinValue = el.getAttribute("max_value");
        MinInclusive = el.getAttribute("min_inclusive").toLowerCase().equals("true");
        MaxInclusive = el.getAttribute("max_inclusive").toLowerCase().equals("true");
    }
}
