package com.robotikflow.core.services.collections.services;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class NetScriptService 
{
    public String urlEncode(
        final String src)
    {
        return URLEncoder.encode(src, StandardCharsets.UTF_8);
    }

    public String urlDecode(
        final String src)
    {
        return URLDecoder.decode(src, StandardCharsets.UTF_8);
    }
}