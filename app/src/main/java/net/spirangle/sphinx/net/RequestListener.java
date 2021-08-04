package net.spirangle.sphinx.net;

import java.util.List;
import java.util.Map;

public interface RequestListener {
    void result(Map<String,List<String>> headers,String data,int status,long id,Object object);
}
