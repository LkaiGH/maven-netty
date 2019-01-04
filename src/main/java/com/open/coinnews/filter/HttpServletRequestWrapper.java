package com.open.coinnews.filter;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

public class HttpServletRequestWrapper extends javax.servlet.http.HttpServletRequestWrapper {

    private Map<String, String> Headers;

    public HttpServletRequestWrapper(HttpServletRequest request) {
            super(request);
            this.Headers = new HashMap<>();
    }

    public void putHeader(String name, String value) {
            this.Headers.put(name, value);
    }

    @Override
    public String getHeader(String name) {
        String value = this.Headers.get(name);
        if (value != null) {
            return value;
        }
        return ((HttpServletRequest) getRequest()).getHeader(name);
    }

    public Enumeration<String> getHeaderNames() {
        Set<String> set = new HashSet<>(Headers.keySet());
        Enumeration<String> enumeration = ((HttpServletRequest) getRequest()).getHeaderNames();
        while (enumeration.hasMoreElements()) {
            String name = enumeration.nextElement();
            set.add(name);
        }
        return Collections.enumeration(set);
    }
}
