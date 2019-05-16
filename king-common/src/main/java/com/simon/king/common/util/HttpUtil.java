package com.simon.king.common.util;

import com.alibaba.fastjson.JSON;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import lombok.experimental.UtilityClass;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * @author zhouzhenyong
 * @since 2019/2/28 上午10:38
 */
@UtilityClass
public class HttpUtil {

    private static OkHttpClient httpClient;

    static {
        httpClient = new OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .readTimeout(60, TimeUnit.SECONDS).build();
    }

    public String get(String url) throws IOException {
        return handleResponse(httpClient.newCall(new Request.Builder().url(url).get().build()).execute()).body().string();
    }

    public String get(String url, String charsetName) throws IOException {
        return new String(httpClient.newCall(new Request.Builder().url(url).get().build()).execute().body().bytes(), charsetName);
    }

    public byte[] getByte(String url) throws IOException {
        return httpClient.newCall(new Request.Builder().url(url).get().build()).execute().body().bytes();
    }

    public String post(String url, Map<String, Object> body) throws IOException {
        return handleResponse(httpClient
            .newCall(new Request.Builder().url(url)
                .post(RequestBody.create(MediaType.parse("application/json"), JSON.toJSONString(body)))
                .build()).execute()).body().string();
    }

    public String post(String url, Map<String, Object> body, String charsetName) throws IOException {
        return new String(httpClient.newCall(new Request.Builder().url(url)
            .post(RequestBody.create(MediaType.parse("application/json"), JSON.toJSONString(body)))
            .build()).execute().body().bytes(), charsetName);
    }

    public String post(String url, Map<String, Object> body, Map<String, String> headers) throws IOException {
        return handleResponse(httpClient.newCall(new Request.Builder().url(url)
            .post(RequestBody.create(MediaType.parse("application/json"), JSON.toJSONString(body)))
            .headers(Headers.of(headers))
            .build()).execute()).body().string();
    }

    public String post(String url, Map<String, Object> body, Map<String, String> headers, String charsetName)
        throws IOException {
        return new String(httpClient.newCall(new Request.Builder().url(url)
            .post(RequestBody.create(MediaType.parse("application/json"), JSON.toJSONString(body)))
            .headers(Headers.of(headers))
            .build()).execute().body().bytes(), charsetName);
    }

    public byte[] postAndGetBytes(String url, Map<String, Object> body) throws IOException {
        return handleResponse(httpClient.newCall(new Request.Builder().url(url)
            .post(RequestBody.create(MediaType.parse("application/json"), JSON.toJSONString(body)))
            .build()).execute()).body().bytes();
    }

    public byte[] postAndGetBytes(String url, Map<String, Object> body, Map<String, String> headers)
        throws IOException {
        return handleResponse(httpClient.newCall(new Request.Builder().url(url)
            .post(RequestBody.create(MediaType.parse("application/json"), JSON.toJSONString(body)))
            .headers(Headers.of(headers))
            .build()).execute()).body().bytes();
    }

    public class HttpException extends RuntimeException{
        HttpException(){
            super();
        }

        HttpException(String msg){
            super(msg);
        }
    }

    public Response handleResponse(Response response) throws IOException {
        if (!response.isSuccessful()) {
            try (ResponseBody body = response.body()) {
                throw new HttpException("code = " + response.code()
                    + ", url = " + response.request().url().toString()
                    + ", body = " + body.string());
            }
        }
        return response;
    }
}
