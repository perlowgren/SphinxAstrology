package net.spirangle.sphinx;


public class Base36 {
    private static final String TAG = "Base36";

    private static final char[] base36 = {
        '0','1','2','3','4','5','6','7','8','9',
        'A','B','C','D','E','F','G','H','I','J',
        'K','L','M','N','O','P','Q','R','S','T',
        'U','V','W','X','Y','Z'
    };

    public static final String encode(long n) {
        if(n==0) return "0";
        char[] b = new char[15];
        int l = 0, o = b.length;
        boolean m = false;
        if(n<0) {
            m = true;
            n = -n;
        }
        for(; n>0; n /= 36) {
            b[--o] = base36[(int)(n%36)];
            ++l;
        }
        if(m) {
            b[--o] = '-';
            ++l;
        }
        return new String(b,o,l);
    }

    public static final long decode(String s) {
        int l = s.length();
        if(l>14) return 0;
        int i = 0;
        long n;
        char c;
        boolean m = false;
        if(s.charAt(0)=='-') {
            m = true;
            ++i;
        }
        for(n = 0; i<l; ++i) {
            c = s.charAt(i);
            if(c>='0' && c<='9') n = (n*36)+(long)(c-'0');
            else if(c>='A' && c<='Z') n = (n*36)+(long)(c+10-'A');
            else if(c>='a' && c<='z') n = (n*36)+(long)(c+10-'a');
        }
        if(m) n = -n;
        return n;
    }
}

