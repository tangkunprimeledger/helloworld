package com.higgs.trust.rs.tx.sender;

import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.fastjson.FastJsonConverterFactory;

import java.util.concurrent.TimeUnit;

/**
 * @author Chen Jiawei
 * @date 2019-01-03
 */
@Slf4j
public class HttpClient {
    private Retrofit retrofit;

    public HttpClient(String serverIp, int serverPort) {
        this(serverIp, serverPort, 60, 60, 60);
    }

    public HttpClient(String serverIp, int serverPort, long connectTimeout, long readTimeout, long writeTimeout) {
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(connectTimeout, TimeUnit.SECONDS)
                .writeTimeout(writeTimeout, TimeUnit.SECONDS)
                .readTimeout(readTimeout, TimeUnit.SECONDS)
                .build();

        retrofit = new Retrofit.Builder()
                .baseUrl(String.format("http://%s:%s/", serverIp, serverPort))
                .addConverterFactory(FastJsonConverterFactory.create())
                .client(okHttpClient)
                .build();

        log.info("HttpClient [serverIp=" + serverIp + ", serverPort=" + serverPort + "] is created");
    }

    public <T> T createApi(Class<T> apiClazz) {
        T api = retrofit.create(apiClazz);
        log.info("Service api " + apiClazz.getName() + " is ready");
        return api;
    }
}