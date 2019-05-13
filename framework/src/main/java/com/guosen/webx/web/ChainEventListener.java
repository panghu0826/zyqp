package com.guosen.webx.web;

import java.util.regex.Pattern;

/**
 * Created by kerry on 2016/6/2.
 */
public interface ChainEventListener {
    void addRouter(String method, String urlPat, Pattern pat);

    void addFilter(String name, String type, String urlPat, Pattern pat);

    void reset();
}
