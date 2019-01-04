package com.open.coinnews.utils;

import com.mysql.jdbc.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.EntityUtils;

import javax.net.ssl.SSLContext;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * Created by fgl on 11/22/2016.
 */
public class HttpUtil {

    private static final String DEFAULT_CHARSET = "UTF-8";

    private static final int CONNECT_TIME_OUT = 5000; //链接超时时间3秒

    private static final RequestConfig REQUEST_CONFIG = RequestConfig.custom().setConnectTimeout(CONNECT_TIME_OUT).build();



    public static SSLContext getSSLContenxt(String certPath,String certPassword,String keyStore){

        try {
            KeyStore keystore = KeyStore.getInstance("PKCS12");
            FileInputStream fis = new FileInputStream(certPath);
            char[] keyPassword = certPassword.toCharArray(); //证书密码
            keystore.load(fis, keyPassword);
            SSLContext sslContext = SSLContexts.custom().loadKeyMaterial(keystore, keyPassword).build();
            fis.close();
            return sslContext;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * @description 功能描述: get 请求
     * @param url 请求地址
     * @param params 参数
     * @param headers headers参数
     * @return 请求失败返回null
     */
    public static String get(String url, Map<String, String> params, Map<String, String> headers) {

        CloseableHttpClient httpClient = null;
        if (params != null && !params.isEmpty()) {
            StringBuffer param = new StringBuffer();
            boolean flag = true; // 是否开始
            for (Entry<String, String> entry : params.entrySet()) {
                if (flag) {
                    param.append("?");
                    flag = false;
                } else {
                    param.append("&");
                }
                param.append(entry.getKey()).append("=");

                try {
                    param.append(URLEncoder.encode(entry.getValue(), DEFAULT_CHARSET));
                } catch (UnsupportedEncodingException e) {
                    //编码失败
                }
            }
            url += param.toString();
        }

        String body = null;
        CloseableHttpResponse response = null;
        try {
            httpClient = HttpClients.custom()
                    .setDefaultRequestConfig(REQUEST_CONFIG)
                    .build();
            HttpGet httpGet = new HttpGet(url);
            response = httpClient.execute(httpGet);
            body = EntityUtils.toString(response.getEntity(), DEFAULT_CHARSET);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (response != null) {
                try {
                    response.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (httpClient != null) {
                try {
                    httpClient.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return body;
    }

    /**
     * @description 功能描述: get 请求
     * @param url 请求地址
     * @return 请求失败返回null
     */
    public static String get(String url) {
        return get(url, null);
    }

    /**
     * @description 功能描述: get 请求
     * @param url 请求地址
     * @param params 参数
     * @return 请求失败返回null
     */
    public static String get(String url, Map<String, String> params) {
        return get(url, params, null);
    }

    /**
     * @description 功能描述: post 请求
     * @param url 请求地址
     * @param params 参数
     * @return 请求失败返回null
     */
    public static String post(String url, Map<String, String> params) {
        CloseableHttpClient httpClient = null;
        HttpPost httpPost = new HttpPost(url);
        List<NameValuePair> nameValuePairs = new ArrayList<>();
        if (params != null && !params.isEmpty()) {
            for (Entry<String, String> entry : params.entrySet()) {
                nameValuePairs.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
            }
        }

        String body = null;
        CloseableHttpResponse response = null;
        try {
            httpClient = HttpClients.custom()
                    .setDefaultRequestConfig(REQUEST_CONFIG)
                    .build();
            httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs, DEFAULT_CHARSET));
            response = httpClient.execute(httpPost);
            body = EntityUtils.toString(response.getEntity(), DEFAULT_CHARSET);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (response != null) {
                try {
                    response.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (httpClient != null) {
                try {
                    httpClient.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return body;
    }

    /**
     * @description 功能描述: post 请求
     * @param url 请求地址
     * @param s 参数xml
     * @return 请求失败返回null
     */
    public static String post(String url, String s) {
        CloseableHttpClient httpClient = null;
        HttpPost httpPost = new HttpPost(url);
        String body = null;
        CloseableHttpResponse response = null;
        try {
            httpClient = HttpClients.custom()
                    .setDefaultRequestConfig(REQUEST_CONFIG)
                    .build();
            httpPost.setEntity(new StringEntity(s, DEFAULT_CHARSET));
            response = httpClient.execute(httpPost);
            body = EntityUtils.toString(response.getEntity(), DEFAULT_CHARSET);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (response != null) {
                try {
                    response.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (httpClient != null) {
                try {
                    httpClient.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return body;
    }

    /**
     * @description 功能描述: post https请求，服务器双向证书验证
     * @param url 请求地址
     * @param params 参数
     * @return 请求失败返回null
     */
    public static String posts(String url, Map<String, String> params,SSLContext sslContext) {
        CloseableHttpClient httpClient = null;
        HttpPost httpPost = new HttpPost(url);
        List<NameValuePair> nameValuePairs = new ArrayList<>();
        if (params != null && !params.isEmpty()) {
            nameValuePairs.addAll(params.entrySet().stream().map(entry -> new BasicNameValuePair(entry.getKey(), entry.getValue())).collect(Collectors.toList()));
        }

        String body = null;
        CloseableHttpResponse response = null;
        try {
            httpClient = HttpClients.custom()
                    .setDefaultRequestConfig(REQUEST_CONFIG)
                    .setSSLSocketFactory(getSSLConnectionSocket(sslContext))
                    .build();
            httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs, DEFAULT_CHARSET));
            response = httpClient.execute(httpPost);
            body = EntityUtils.toString(response.getEntity(), DEFAULT_CHARSET);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (response != null) {
                try {
                    response.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (httpClient != null) {
                try {
                    httpClient.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return body;
    }

    /**
     * @description 功能描述: post https请求，服务器双向证书验证
     * @param url 请求地址
     * @param s 参数xml
     * @return 请求失败返回null
     */
    public static String posts(String url, String s,SSLContext sslContext) {
        CloseableHttpClient httpClient = null;
        HttpPost httpPost = new HttpPost(url);
        String body = null;
        CloseableHttpResponse response = null;
        try {
            httpClient = HttpClients.custom()
                    .setDefaultRequestConfig(REQUEST_CONFIG)
                    .setSSLSocketFactory(getSSLConnectionSocket(sslContext))
                    .build();
            httpPost.setEntity(new StringEntity(s, DEFAULT_CHARSET));
            response = httpClient.execute(httpPost);
            body = EntityUtils.toString(response.getEntity(), DEFAULT_CHARSET);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (response != null) {
                try {
                    response.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (httpClient != null) {
                try {
                    httpClient.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return body;
    }

    //获取ssl connection链接
    private static SSLConnectionSocketFactory getSSLConnectionSocket(SSLContext sslContext) {
        return new SSLConnectionSocketFactory(sslContext, new String[] {"TLSv1", "TLSv1.1", "TLSv1.2"}, null,
                SSLConnectionSocketFactory.getDefaultHostnameVerifier());
    }


    // realIP, proxy1, proxy2
    public static String[] getProxyChain(String XForwardedFor){

        if (StringUtils.isNullOrEmpty(XForwardedFor)) {
            return null;
        }

        return XForwardedFor.split(",");
    }

    public static String getRealIP(String XForwardedFor){

        String[] chain = getProxyChain(XForwardedFor);

        if (chain == null || chain.length == 0) {
            return "should config nginx";
        }

        return chain[0];
    }
}
