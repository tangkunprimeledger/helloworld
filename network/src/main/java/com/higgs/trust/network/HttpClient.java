package com.higgs.trust.network;

import com.alibaba.fastjson.JSON;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Random;

/**
 * @author duhongming
 * @date 2018/9/14
 */
public class HttpClient {

    private static final MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json; charset=utf-8");

    private final Logger log = LoggerFactory.getLogger(getClass());

    private Random random;

    private NetworkManage networkManage;

    private OkHttpClient httpClient;

    public HttpClient(NetworkManage networkManage) {
        this.networkManage = networkManage;
        this.random = new Random();
        httpClient = new OkHttpClient.Builder().build();
    }

    public <T> T postJson(String nodeName, String resourceUrl, Object json, Class<T> resultClass) {
        return postJson(getUrl(nodeName, resourceUrl), json, resultClass);
    }

    public <T> T postJson(String nodeName, String resourceUrl, Object json, Type resultType) {
        return postJson(getUrl(nodeName, resourceUrl), json, resultType);
    }

    public <T> T postJson(String url, Object json, Class<T> resultClass) {
        RequestBody body = RequestBody.create(MEDIA_TYPE_JSON, JSON.toJSONString(json));
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        return execute(request, resultClass);
    }

    private <T> T postJson(String url, Object json, Type resultType) {
        RequestBody body = RequestBody.create(MEDIA_TYPE_JSON, JSON.toJSONString(json));
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        return execute(request, resultType);
    }

    public <T> T get(String nodeName, String url, Type resultType) {
        return get(getUrl(nodeName, url), resultType);
    }

    public <T> T get(String url, Type resultType) {
        Request request = new Request.Builder().url(url).build();
        return execute(request, resultType);
    }

    public <T> List<T> getList(String nodeName, String url, Class<T> resultClass) {
        return getList(getUrl(nodeName, url), resultClass);
    }

    public <T> List<T> getList(String url, Class<T> resultClass) {
        log.trace("get {}", url);
        Request request = new Request.Builder().url(url).build();
        return executeList(request, resultClass);
    }

    private <T> T execute(Request request, Class<T> resultClass) {
        return this.<T>execute(request, (Type) resultClass);
    }

    private <T> T execute(Request request, Type resultType) {
        try {
            Call call = httpClient.newCall(request);
            Response response = call.execute();
            String jsonText = response.body().string();
            if (!response.isSuccessful()) {
                throw new RuntimeException(jsonText);
            }
            return JSON.<T>parseObject(jsonText, resultType);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private <T> List<T> executeList(Request request, Class<T> resultClass) {
        try {
            Call call = httpClient.newCall(request);
            Response response = call.execute();
            String jsonText = response.body().string();
            if (!response.isSuccessful()) {
                throw new RuntimeException(jsonText);
            }
            return JSON.parseArray(jsonText, resultClass);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String getUrl(String nodeName, String resourcePath) {
        Peer peer = getPeerByName(nodeName);
        return String.format("http://%s:%s%s", peer.getAddress().getHost(), peer.getHttpPort(), resourcePath);
    }

    private Peer getPeerByName(String nodeName) {
        Peer peer = NetworkManage.getInstance().getPeerByName(nodeName);
        if (peer == null) {
            throw new NullPointerException(String.format("The node %s not fond", nodeName));
        }
        return peer;
    }

    public Peer getRandomPeer(List<String> names) {
        String localName = networkManage.localPeer().getNodeName();
        String nodeName = localName;
        while (nodeName.equals(localName)) {
            nodeName = names.get(random.nextInt(names.size()));
        }
        return getPeerByName(nodeName);
    }
}
