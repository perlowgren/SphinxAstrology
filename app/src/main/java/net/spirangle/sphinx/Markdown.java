package net.spirangle.sphinx;

import static net.spirangle.sphinx.SphinxProperties.APP;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Markdown {
    private static final String TAG = "Markdown";

//	private static String htmlTags = "a|b|big|blockquote|br|cite|dfn|div|em|font|h1|h2|h3|h4|h5|h6|i|img|p|small|strong|sub|sup|tt|u";
//	private static String htmlBlockTags = "a|b|big|blockquote|cite|dfn|div|em|font|h1|h2|h3|h4|h5|h6|i|p|small|strong|sub|sup|tt|u";

    private static final String[] charStyles = {"tt","em","strong","u","strike","sup","big","small"};

    private static final Pattern references = Pattern.compile("\\{\\@([rhqltsu])(\\d+)\\}");
    private static final Pattern stripHtml = Pattern.compile("<.*?>",Pattern.DOTALL);
    private static final Pattern stripHtmlComments = Pattern.compile("<!--.*?-->",Pattern.DOTALL);
    private static final Pattern encodeHtml = Pattern.compile("[&\"\'<>]");
    private static final Pattern stripComments = Pattern.compile("^%(%%\n.*?%%%|.*?)$",Pattern.MULTILINE|Pattern.DOTALL);
    private static final Pattern quotes = Pattern.compile("^(>.*?\\n)+",Pattern.MULTILINE);
    private static final Pattern stripQuotes = Pattern.compile("^> ?",Pattern.MULTILINE);
    private static final Pattern headers = Pattern.compile("^([#=][2-6]|[#=]{1,6})[ \\t]*(.+?)(?<!\\\\)[ \\t#=]*\\n",Pattern.MULTILINE);
    private static final Pattern paragraphs = Pattern.compile("^(?!\\{\\@[rhqltsu]\\d+\\}\\n$).+(\\n.+)*$",Pattern.MULTILINE);
    private static final Pattern horizLines = Pattern.compile("^\\n--+\\n$",Pattern.MULTILINE);
    private static final Pattern inlineLinksTags = Pattern.compile("(?<!\\\\)(\\[(?:.*? )?(?:#[0-9a-fA-F]+(?::[0-9a-fA-F]+)?|[\\w\\.\\:\\/]+)\\]|\\{[\\w\\.]+\\})");
    private static final Pattern inlineStyles = Pattern.compile("(?<!\\\\)(''|``|\\/\\/|\\*\\*|__|~~|\\^\\^|\\+\\+|--|(?<!\\w)[`/*_~^+-]+(?=\\w)|(?<=\\w)[`/*_~^+-]+(?!\\w))+");
    private static final Pattern stripBlankLines = Pattern.compile("\\n{3,}");

    public static class Header {
        public final int id;
        public final int number;
        public final String title;
        public final String html;

        public Header(int i,int n,String t) {
            id = i;
            number = n;
            title = t;
            html = "<h"+n+">"+t+"</h"+n+">";
        }

        @Override
        public String toString() { return html; }
    }

    public static String stripBlankLines(String text) {
        return Regex.replace(text,stripBlankLines,"\n\n");
    }

    public static String stripHtml(String text) {
        return Regex.replace(text,stripHtml);
    }

    public static String stripHtmlComments(String text) {
        return Regex.replace(text,stripHtmlComments);
    }

    public static String encodeHtml(String text) {
        return Regex.replace(text,encodeHtml,new Regex.Callback() {
            @Override
            public String replace(Matcher m) {
                switch(m.group().charAt(0)) {
                    case '&':
                        return "&amp;";
                    case '\"':
                        return "&quot;";
                    case '\'':
                        return "&apos;";
                    case '<':
                        return "&lt;";
                    case '>':
                        return "&gt;";
                    default:
                        return m.group();
                }
            }
        });
    }

    public static String stripComments(String text) {
        return Regex.replace(text,stripComments);
    }

    private final List<Object> ref;
    private final List<Object> hdrs;
    private final String text;
    private final String html;
    private Header title;
    private int charStyle;

    public Markdown(String text) {
        hdrs = new ArrayList<Object>();
        ref = new ArrayList<Object>();
        this.text = text;
        title = null;
        text = "\n\n"+text+"\n\n";
        text = stripHtml(text);
        text = stripComments(text);
        text = extractRefs(text);
        text = parseSections(text);
        Log.d(APP,TAG+".parse(\ntext:\n"+text+"\n)");
        text = injectRefs(text);
        text = stripBlankLines(text);
        html = text.trim();
    }

    public String getText() { return text; }

    public String getHtml() { return html; }

    public String getTitle() { return title!=null? title.title : null; }

    public int getHeaders() { return hdrs.size(); }

    public Header getHeader(int n) { return n>=0 && n<hdrs.size()? (Header)hdrs.get(n) : null; }

    @Override
    public String toString() { return html; }

    private String addSectionRef(Object obj,char tag) {
        if(obj==null) return "";
        ref.add(obj);
//echo "<pre>Add ref: {$id}, {$tag}\n".htmlspecialchars(print_r($ref,true))."</pre>";
        return "\n\n{@"+tag+ref.size()+"}\n\n";
    }

    private String addRef(Object obj,char tag) {
        if(obj==null) return "";
        ref.add(obj);
//echo "<pre>Add ref: {$id}, {$tag}\n".htmlspecialchars(print_r($ref,true))."</pre>";
        return "{@"+tag+ref.size()+'}';
    }

    private String addHeader(int n,String t) {
        int id = ref.size();
        Header h = new Header(id,n,t);
        hdrs.add(h);
        if(title==null || h.number<title.number) title = h;
        return addSectionRef(h,'r');
    }

    private String parseSections(String text) {
        text = parseQuotes(text);
        text = parseHeaders(text);
        text = parseHorizontalLines(text);
        text = parseParagraphs(text);
        return text;
    }

    private String parseQuotes(String text) {
        return Regex.replace(text,quotes,new Regex.Callback() {
            @Override
            public String replace(Matcher m) {
                String t = m.group();
                t = Regex.replace(t,stripQuotes);
//echo "<p>parseQuote1(".htmlspecialchars($ret).")</p>";
//		$ret = $this->parsePreformatted($ret);
                t = parseSections(t);
//echo "<p>parseQuote2(".htmlspecialchars($ret).")</p>";
                return addSectionRef(t,'q');
            }
        });
    }

    private String parseHeaders(String text) {
        return Regex.replace(text,headers,new Regex.Callback() {
            @Override
            public String replace(Matcher m) {
                String t = m.group(2);
                if(t.length()==0) return "";
                String h = m.group(1);
                int n = h.length();
                if(n==2 && h.charAt(1)!='#') n = (h.charAt(1)-'0');
                return addHeader(n,t);
//				return addSectionRef("<h"+n+">"+t+"</h"+n+">",'r');
            }
        });
    }

    private String parseHorizontalLines(String text) {
        return Regex.replace(text,horizLines,new Regex.Callback() {
            @Override
            public String replace(Matcher m) {
                String t = parseInline(m.group());
                return addSectionRef("\n<hr>\n",'r');
            }
        });
    }

    private String parseParagraphs(String text) {
        return Regex.replace(text,paragraphs,new Regex.Callback() {
            @Override
            public String replace(Matcher m) {
                String t = parseInline(m.group());
                return "<p>"+t+"</p>\n";
            }
        });
    }

    private String parseInline(String text) {
        text = parseTags(text);
        text = parseStyles(text);
        return text;
    }

    private String parseTags(String text) {
        text = Regex.replace(text,inlineLinksTags,new Regex.Callback() {
            @Override
            public String replace(Matcher m) {
                String l = m.group(1), t = null;
                char c = l.charAt(0);
                l = l.substring(1,l.length()-1);
                Log.d(APP,TAG+".parseTags(l: "+l+")");
                int i = l.lastIndexOf(' ');
                if(i>=0) {
                    t = parseStyles(l.substring(0,i).trim());
                    l = l.substring(i+1).trim();
                }
                if(l.charAt(0)=='#') {
                    if(t==null) {
                        Symbol s = new Symbol(l);
                        t = s.getTitle();
                    }
                }
                if(t==null) t = l;
                return addRef("<a href=\""+l+"\">"+t+"</a>",'r');
            }
        });
        return text;
    }

    private String parseStyles(String text) {
        charStyle = 0;
        text = Regex.replace(text,inlineStyles,new Regex.Callback() {
            @Override
            public String replace(Matcher m) {
                int i = 0, n = 0, s = 0;
                String t = m.group();
                Log.d(APP,TAG+".parseInline(t: "+t+")");
                for(i = 0,n = t.length(); i<n; ++i)
                    switch(t.charAt(i)) {
                        case '\'':
                        case '`':
                            s |= 0x0001;
                            break;
                        case '/':
                            s |= 0x0002;
                            break;
                        case '*':
                            s |= 0x0004;
                            break;
                        case '_':
                            s |= 0x0008;
                            break;
                        case '~':
                            s |= 0x0010;
                            break;
                        case '^':
                            s |= 0x00A0;
                            break;
                        case '+':
                            s |= 0x0040;
                            break;
                        case '-':
                            s |= 0x0080;
                            break;
                    }
                if(s!=0) {
                    t = "";
                    for(i = 0; s!=0; ++i,s >>= 1)
                        if((s&1)!=0) {
                            n = 1<<i;
                            if((charStyle&n)!=0) {
                                charStyle &= ~n;
                                t += "</"+charStyles[i]+">";
                            } else {
                                charStyle |= n;
                                t += "<"+charStyles[i]+">";
                            }
                        }
                }
                return t;
            }
        });
        if(charStyle!=0) {
            String close = "";
            for(int i = 0; charStyle!=0; ++i,charStyle >>= 1)
                if((charStyle&1)!=0) close += "</"+charStyles[i]+">";
            if(close.length()>0) text += close;
        }
        return text;
    }

    private String extractRefs(String text) {
        return Regex.replace(text,references,new Regex.Callback() {
            @Override
            public String replace(Matcher m) {
                return addRef(m.group(),'r');
            }
        });
    }

    private String injectRefs(String text) {
        return Regex.replace(text,references,new Regex.Callback() {
            @Override
            public String replace(Matcher m) {
                char tag = m.group(1).charAt(0);
                int id = Integer.parseInt(m.group(2));
                Object obj = ref.get(id-1);
                String str = obj==null? null : obj.toString();
//				if(obj instanceof String) str = (String)obj;
//				else if((obj instanceof Link)) str = ((Link)obj).getHTML();
//				else if(obj instanceof Template) str = ((Template)ref).getHTML();
//echo '<p>injectRef(tag: '.$tag.', id: '.$id.', str: '.$str.')</p>';

                if(str==null) return "";
                if(str.indexOf("{@")>=0) str = injectRefs(str);
                if(tag=='q') str = "\n<blockquote>"+str.trim()+"</blockquote>\n";
                return str;
            }
        });
    }
}

