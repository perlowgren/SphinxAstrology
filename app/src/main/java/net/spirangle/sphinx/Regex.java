package net.spirangle.sphinx;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Regex {

//	private static final Pattern escapeSigns = Pattern.compile("[\"\']");

    public interface Callback {
        String replace(Matcher match);
    }

    public static String replace(String text,Pattern regex) {
        return replace(text,regex,"");
    }

    public static String replace(String text,Pattern regex,String replace) {
        if(text==null || text.length()==0 || regex==null) return text;
        if(replace==null) replace = "";
        return regex.matcher(text).replaceAll(replace);
    }

    public static String replace(String text,Pattern regex,Callback callback) {
        StringBuffer result = new StringBuffer();
        Matcher matcher = regex.matcher(text);
        while(matcher.find()) {
            matcher.appendReplacement(result,callback.replace(matcher));
        }
        matcher.appendTail(result);
        return result.toString();
    }

	/*public static String escapeString(String text) {
		if(text==null || text.length()==0) return text;
		return escapeSigns.matcher(text).replaceAll();
	}*/
}
