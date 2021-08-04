package net.spirangle.sphinx.text;

import static net.spirangle.sphinx.config.SphinxProperties.APP;

import android.graphics.Typeface;
import android.text.Editable;
import android.text.Html;
import android.text.style.LeadingMarginSpan;
import android.text.style.StrikethroughSpan;
import android.text.style.StyleSpan;
import android.text.style.TypefaceSpan;
import android.util.Log;

import org.xml.sax.XMLReader;

public class CustomTagHandler implements Html.TagHandler {
    private static final String TAG = "CustomTagHandler";

    private static final int UL = 0;
    private static final int OL = 1;
    private static final int LI = 2;
    private static final int DL = 3;
    private static final int DT = 4;
    private static final int DD = 5;
    private static final int HR = 6;
    private static final int CODE = 7;
    private static final int S = 8;
    private static final int STRIKE = 9;

    private static final int UL_INDENT = 15;
    private static final int OL_INDENT = 15;
    private static final int DL_INDENT = 15;

    private static final int LIST_MARGIN_TOP = 15;
    private static final int LIST_MARGIN_BOTTOM = 15;

    private static final String[] tagNames = {"UL","OL","LI","DL","DT","DD","HR","CODE","S","STRIKE"};
    private static final int[] tagLength = {2,2,2,2,2,2,2,4,1,6};
    private static final String tags = "s ul ol li dl dt dd hr strike code";
    private static final int[] tagIndex = {S,-1,UL,-1,-1,OL,-1,-1,LI,-1,-1,DL,-1,-1,DT,-1,-1,DD,-1,-1,
        HR,-1,-1,STRIKE,-1,-1,-1,-1,-1,-1,CODE,-1,-1,-1,-1,};

    private static class ListTag {
        public static int size = 0;
        public int tag;
        public int start;
        public int number;
        public ListTag parent;
        public ListTag child;

        public ListTag(ListTag p,int t) {
            ++size;
            tag = t;
            start = -1;
            number = 0;
            parent = p;
            child = null;
            if(p!=null) p.child = this;
        }

        public ListTag close(int t) {
            if(t==tag) {
                --size;
                return parent;
            }
            for(ListTag lt = parent; lt!=null; lt = lt.parent)
                if(lt.tag==t) {
                    --size;
                    if(lt.parent!=null) lt.parent.child = child;
                    child.parent = parent;
                    break;
                }
            return this;
        }

        public int size() { return size; }
    }

    private ListTag list = null;
    private int[] tagStart = {-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,};

    @Override
    public void handleTag(final boolean opening,final String tag,Editable output,final XMLReader xmlReader) {
        int l = tag.length();
        int t = tags.indexOf(tag.toLowerCase());
        if(t==-1) return;
        t = tagIndex[t];
        if(t==-1 || l!=tagLength[t]) return;
        Log.d(APP,TAG+".handleTag(tag: "+tag+")");
        if(t==S) t = STRIKE;
        switch(t) {
            case UL:
            case OL:
                handleTag(false,output,LI);
                if(opening) {
                    list = new ListTag(list,t);
                    handleTag(true,output,t);
                } else if(list!=null) {
                    handleTag(false,output,t);
                    list = list.close(t);
                }
                break;

            case DL:
                handleTag(false,output,DT);
                handleTag(false,output,DD);
                if(opening) {
                    list = new ListTag(list,t);
                    handleTag(true,output,t);
                } else if(list!=null) {
                    handleTag(false,output,t);
                    list = list.close(t);
                }
                break;

            case LI:
            case DT:
            case DD:
            case HR:
            case CODE:
            case STRIKE:
                handleTag(opening,output,t);
                break;
        }
    }

    private void handleTag(boolean opening,Editable output,int tag) {
        int start = tagStart[tag];
        int len = output.length();
        Log.d(APP,TAG+".handleTag(opening: "+(opening? "true" : "false")+", start: "+start+", len: "+len+", tag: "+tagNames[tag]+", list: "+list+")");
        if(opening) {
            switch(tag) {
                case UL:
                case OL:
                case DL:
                    if(list==null) return;
                    list.start = len;
                    break;
                case LI:
                case DT:
                case DD:
                    if((tag==DT && tagStart[DD]>=0) || (tag==DD && tagStart[DT]>=0))
                        handleTag(false,output,(tag==DT? DD : DT));
                    if(start>=0) handleTag(false,output,tag);
                    tagStart[tag] = len;
                    break;
                case HR:
                    start = len;
                    output.append(" \n");
                    output.setSpan(new CustomHRSpan(0x33000000,5.0f,1.0f,false),start,output.length(),0);
                    output.append("\n");
                    break;
                default:
                    if(start<0) tagStart[tag] = len;
                    break;
            }
        } else {
            switch(tag) {
                case UL:
                case OL:
                case DL:
                    if(list==null || list.start<0 || list.start>=len) return;
                    if(list.size==1) {
                        output.append("\n");
                        output.setSpan(new CustomParagraphMarginSpan(LIST_MARGIN_TOP,LIST_MARGIN_BOTTOM),list.start,output.length(),0);
                    }
                    break;
                case LI:
                    if(start<0 || start>=len || list==null || (list.tag!=UL && list.tag!=OL)) break;
                    output.append("\n");
                    int indent = 0;
                    if(list.tag==UL) {
                        output.insert(start,"• ");
                        indent = UL_INDENT*(list.size-1);
                    } else if(list.tag==OL) {
                        ++list.number;
                        output.insert(start,list.number+". ");
                        indent = OL_INDENT*list.size;
                    }
                    if(indent>0)
                        output.setSpan(new LeadingMarginSpan.Standard(indent),start,output.length(),0);
                    break;
                case DT:
                    if(start<0 || start>=len || list==null || list.tag!=DL) break;
                    output.append("\n");
                    output.setSpan(new StyleSpan(Typeface.BOLD),start,output.length(),0);
                    break;
                case DD:
                    if(start<0 || start>=len || list==null || list.tag!=DL) break;
                    output.append("\n");
                    output.setSpan(new LeadingMarginSpan.Standard(DL_INDENT*list.size),start,output.length(),0);
                    break;
                case HR:
                    break;
                default:
                    if(start<0 || start>=len) break;
                    Object span = null;
                    switch(tag) {
                        case CODE:
                            span = new TypefaceSpan("monospace");
                            break;
                        case STRIKE:
                            span = new StrikethroughSpan();
                            break;
                    }
                    if(span!=null)
                        output.setSpan(span,start,len,0);
                    break;
            }
            tagStart[tag] = -1;
        }
    }
}

