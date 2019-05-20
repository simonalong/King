package com.simon.king.groovy;

import com.alibaba.fastjson.JSON;
import com.simon.neo.NeoMap;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request.Builder;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author zhouzhenyong
 * @since 2019/5/20 下午4:44
 */
@Slf4j
@Service
public class HttpService {

    static OkHttpClient httpClient;
    Builder builder = new Builder();

    static {
        httpClient = new OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .readTimeout(60, TimeUnit.SECONDS).build();
    }

    @Autowired
    private NameSpaceInterface nameSpaceInterface;

    private ThreadLocal<String> bodyJson = new ThreadLocal<>();

    /**
     * 将含有命名空间的前缀解析为指定的ip域名，比如：name/shop/getCount 到 http://xxx.xxx.xxx.xxx:port/shop/getCount
     *
     * @param url restful路径，比如name/shop/getCount
     * @return 转换后的全url路径
     */
    
    public HttpService url(String url) {
        if (url.contains("/") && !url.contains(":")) {
            Integer index = url.indexOf("/");
            String namespace = url.substring(0, index);
            String ipData = nameSpaceInterface.getIpAndPort(namespace);
            if(!StringUtils.isEmpty(ipData)){
                String resultUrl = ipData + url.substring(index);
                log.info("url = " + resultUrl);
                builder.url(resultUrl);
                return this;
            }
        }
        builder.url(url);
        return this;
    }

    public HttpService headers(NeoMap headMap) {
        builder.headers(Headers.of(headMap.getDataMapAssignValueType(String.class)));
        return this;
    }

    
    public HttpService body(NeoMap bodyMap) {
        bodyJson.set(JSON.toJSONString(bodyMap));
        return this;
    }

    public String get() {
        try {
            return getResponseBody(httpClient.newCall(builder.get().build()).execute());
        } catch (IOException e) {
            log.error("获取异常， {}", e);
            e.printStackTrace();
        }
        return null;
    }

    public NeoMap head() {
        try {
            return getResponseHead(httpClient.newCall(builder.head().build()).execute());
        } catch (IOException e) {
            log.error("获取异常， {}", e);
            e.printStackTrace();
        }
        return null;
    }

    public String post() {
        try {
            return getResponseBody(httpClient.newCall(builder
                .post(RequestBody.create(MediaType.parse("application/json"), bodyJson.get()))
                .build()
            ).execute());
        } catch (IOException e) {
            log.error("获取异常， {}", e);
            e.printStackTrace();
        }
        return null;
    }

    public String put() {
        try {
            return getResponseBody(httpClient.newCall(builder
                .put(RequestBody.create(MediaType.parse("application/json"), bodyJson.get()))
                .build()
            ).execute());
        } catch (IOException e) {
            log.error("获取异常， {}", e);
            e.printStackTrace();
        }
        return null;
    }

    public String patch() {
        try {
            return getResponseBody(httpClient.newCall(builder
                .patch(RequestBody.create(MediaType.parse("application/json"), bodyJson.get()))
                .build()
            ).execute());
        } catch (IOException e) {
            log.error("获取异常， {}", e);
            e.printStackTrace();
        }
        return null;
    }

    public String delete() {
        try {
            return getResponseBody(httpClient.newCall(builder
                .delete(RequestBody.create(MediaType.parse("application/json"), bodyJson.get()))
                .build()
            ).execute());
        } catch (IOException e) {
            log.error("获取异常， {}", e);
            e.printStackTrace();
        }
        return null;
    }

    public String getResponseBody(Response response) throws IOException {
        if (!response.isSuccessful()) {
            try (ResponseBody body = response.body()) {
                assert body != null;
                throw new HttpException("code = " + response.code()
                    + ", url = " + response.request().url().toString()
                    + ", body = " + body.string());
            }
        }else{
            return response.body().string();
        }
    }

    public NeoMap getResponseHead(Response response) throws IOException {
        if (!response.isSuccessful()) {
            Headers headers = response.headers();
            NeoMap.fromMap(headers.toMultimap());
        }
        return NeoMap.of();
    }

    public class HttpException extends RuntimeException{
        HttpException(){
            super();
        }

        HttpException(String msg){
            super(msg);
        }
    }
}
