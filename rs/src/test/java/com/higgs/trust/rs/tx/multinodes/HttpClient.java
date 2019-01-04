package com.higgs.trust.rs.tx.multinodes;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.fastjson.FastJsonConverterFactory;

import java.util.concurrent.TimeUnit;

/**
 * @author Chen Jiawei
 * @date 2019-01-03
 */
public class HttpClient {
    private String serverIp;
    private int serverPort;

    private long connectTimeout;
    private long readTimeout;
    private long writeTimeout;

    private Retrofit retrofit;


    public HttpClient() {
        this(null, 0);
    }

    public HttpClient(String serverIp, int serverPort) {
        this(serverIp, serverPort, 60, 60, 60);
    }

    public HttpClient(String serverIp, int serverPort, long connectTimeout, long readTimeout, long writeTimeout) {
        this.serverIp = serverIp;
        this.serverPort = serverPort;
        this.connectTimeout = connectTimeout;
        this.readTimeout = readTimeout;
        this.writeTimeout = writeTimeout;
    }


    public String getServerIp() {
        return serverIp;
    }

    public void setServerIp(String serverIp) {
        this.serverIp = serverIp;
    }

    public int getServerPort() {
        return serverPort;
    }

    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
    }

    public long getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(long connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public long getReadTimeout() {
        return readTimeout;
    }

    public void setReadTimeout(long readTimeout) {
        this.readTimeout = readTimeout;
    }

    public long getWriteTimeout() {
        return writeTimeout;
    }

    public void setWriteTimeout(long writeTimeout) {
        this.writeTimeout = writeTimeout;
    }

    private synchronized void buildRetrofit() {
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
    }

    public synchronized <T> T createApi(Class<T> apiClazz) {
        if (retrofit == null) {
            buildRetrofit();
        }

        return retrofit.create(apiClazz);
    }
}