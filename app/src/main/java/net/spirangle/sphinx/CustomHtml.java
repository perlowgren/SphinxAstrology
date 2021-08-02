package net.spirangle.sphinx;

import static net.spirangle.sphinx.SphinxProperties.APP;

import android.os.Build;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ClickableSpan;
import android.text.style.QuoteSpan;
import android.text.style.URLSpan;
import android.util.Log;
import android.view.View;


public class CustomHtml {
    private static final String TAG = "CustomHtml";

    public static final int SYMBOLS = 0x0001;
    public static final int ICONS = 0x0002;
    public static final int QUOTES = 0x0004;
    public static final int LINKS = 0x0008;
    public static final int ALL = 0x000f;

    public static interface CustomHtmlListener {
        public void onLinkClick(View view,String url);
    }

    @SuppressWarnings("deprecation")
    public static Spanned fromHtml(String html,int customFlags,float symbolSize,final CustomHtmlListener listener) {
        Spanned spanned = null;
        try {
//Log.d(APP,TAG+".fromHtml(html:\n"+html+"\n)");
            if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.N) {
                spanned = Html.fromHtml(html,Html.FROM_HTML_MODE_LEGACY,null,new CustomTagHandler());
            } else {
                spanned = Html.fromHtml(html,null,new CustomTagHandler());
            }

            if(customFlags!=0) {
                SpannableStringBuilder strBuilder = new SpannableStringBuilder(spanned);
                int links = 0;

                if((customFlags&(QUOTES|LINKS))!=0) {
                    Object[] spans = strBuilder.getSpans(0,spanned.length(),Object.class);
                    Object replace;
                    int start, end, flags;
                    for(Object span : spans) {
                        Log.d(APP,TAG+".fromHtml(span: "+span.getClass().getName()+")");
                        replace = null;
                        if((span instanceof QuoteSpan) && (customFlags&QUOTES)!=0) {
                            replace = new CustomQuoteSpan(
                                0x11000000,
                                0x33000000,
                                3.0f,
                                10.0f
                            );
                        } else if((span instanceof URLSpan) && (customFlags&LINKS)!=0) {
                            final URLSpan urlSpan = (URLSpan)span;
                            replace = new ClickableSpan() {
                                @Override
                                public void onClick(View view) {
                                    if(listener!=null) {
                                        String url = urlSpan.getURL().trim();
                                        if(url.charAt(0)=='"' && url.charAt(url.length()-1)=='"')
                                            url = url.substring(1,url.length()-1);
                                        listener.onLinkClick(view,url);
                                    }
                                }
                            };
                            ++links;
                        }
                        if(replace!=null) {
                            start = strBuilder.getSpanStart(span);
                            end = strBuilder.getSpanEnd(span);
                            flags = strBuilder.getSpanFlags(span);
                            strBuilder.setSpan(replace,start,end,flags);
                            strBuilder.removeSpan(span);
                        }
                    }
                }

/*
			URLSpan[] urls = strBuilder.getSpans(0,spanned.length(),URLSpan.class);
			for(URLSpan span : urls)
				replaceURLSpan(strBuilder,span);
*/
                if((customFlags&(SYMBOLS|ICONS))!=0) {
                    int i, e, n;
                    char c1, c2;
                    boolean sym = (customFlags&SYMBOLS)!=0;
                    boolean ic = (customFlags&ICONS)!=0;
                    for(i = 0,n = strBuilder.length(); i<n; ++i) {
                        c1 = strBuilder.charAt(i);
                        if(i+1<n) c2 = strBuilder.charAt(i+1);
                        else c2 = '\0';
                        if(sym &&
                           (c1>='\u22B2' && c1<='\u2A02') ||
                           (c1>='\uF580' && c1<='\uF58F') ||
                           (c1>='\uD83C' && c1<='\uD83D' && c2>='\uDF11' && c2<='\uDF73') ||
                           (c1>='\uD880' && c1<='\uD880' && c2>='\uDC41' && c2<='\uDC5A')) {
                            e = i+1;
                            if((c1>='\uD83C' && c1<='\uD83D' && c2>='\uDF11' && c2<='\uDF73') ||
                               (c1>='\uD880' && c1<='\uD880' && c2>='\uDC41' && c2<='\uDC5A')) ++e;
                            for(; e<n; ++e) {
                                c1 = strBuilder.charAt(e);
                                if(e+1<n) c2 = strBuilder.charAt(e+1);
                                else c2 = '\0';
                                if((c1>='\u22B2' && c1<='\u2A02') ||
                                   (c1>='\uF580' && c1<='\uF58F')) continue;
                                if((c1>='\uD83C' && c1<='\uD83D' && c2>='\uDF11' && c2<='\uDF73') ||
                                   (c1>='\uD880' && c1<='\uD880' && c2>='\uDC41' && c2<='\uDC5A')) {
                                    ++e;
                                    continue;
                                }
                                break;
                            }
                            CustomTypefaceSpan typefaceSpan = new CustomTypefaceSpan(BasicActivity.symbolFont,0,symbolSize);
                            strBuilder.setSpan(typefaceSpan,i,e,0);
                            i = e-1;
                        } else if(ic && c1>='\ue000' && c1<='\uebff') {
                            for(e = i+1; e<n; ++e) {
                                c1 = strBuilder.charAt(e);
                                if(c1>='\ue000' && c1<='\uebff') continue;
                                break;
                            }
                            CustomTypefaceSpan typefaceSpan = new CustomTypefaceSpan(BasicActivity.iconFont,0,0.0f);
                            strBuilder.setSpan(typefaceSpan,i,e,0);
                            i = e-1;
                        }
                    }
                }
                spanned = strBuilder;
//Log.d(APP,TAG+".fromHtml(html:\n"+strBuilder+"\n)");
//			text.setText(strBuilder,TextView.BufferType.SPANNABLE);
//			text.setMovementMethod(LinkMovementMethod.getInstance());
//		} else {
//			text.setText(spanned);
            }
        } catch(Exception e) {
            Log.e(APP,TAG+".fromHtml",e);
        }

        return spanned;
    }
}

