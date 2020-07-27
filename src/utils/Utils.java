package utils;

import plp.objects.EOperator;

import java.io.File;
import java.net.URISyntaxException;

public class Utils {
    public static String GetApplicationExecutablePath() throws URISyntaxException {
        return new File(Utils.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getPath();
    }

    public static String DecimalToString(double number)
    {
        return String.format("%.2f", number);
    }

    public static int CountCharInString(char c, String s)
    {
        if(s == null)return 0;
        return s.length() - s.replace(String.valueOf(c), "").length();
    }

    public static EOperator GetOperator(String sOperator)
    {
        switch (sOperator)
        {
            case "=":
                return EOperator.Equal;
            case "!=":
                return EOperator.NotEqual;
            case "less":
                return EOperator.Less;
            case "less_equal":
                return EOperator.LessEqual;
            case "greater":
                return EOperator.Greater;
            case "greater_equal":
                return EOperator.GreaterEqual;
                default: throw new UnsupportedOperationException();
        }
    }

    public static String ToStringForRDDL(EOperator op)
    {
        switch (op)
        {
            case Less:
                return "<";
            case Equal:
                return "==";
            case Greater:
                return ">";
            case NotEqual:
                return "~=";
            case LessEqual:
                return "<=";
            case GreaterEqual:
                return ">=";
            default: throw new UnsupportedOperationException();
        }
    }
}
