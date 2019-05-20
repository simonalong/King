package com.simon.king.server;

import com.alibaba.fastjson.JSON;
import com.simon.king.groovy.HttpService;
import com.simon.king.groovy.HttpService.HttpException;
import com.simon.neo.NeoMap;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Request.Builder;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author zhouzhenyong
 * @since 2019/5/20 下午7:08
 */
@Slf4j
public class HttpServiceTest extends BaseSpringBootTest {

    @Autowired
    private HttpService httpService;

    private static OkHttpClient httpClient;

    static {
        httpClient = new OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .readTimeout(60, TimeUnit.SECONDS).build();
    }

    @Test
    public void testGet(){
        System.out.println(httpService.get("namespace3/test/get/haode").send());
    }

    @Test
    public void testPost(){
        System.out.println(httpService.post("namespace3/test/post").body(NeoMap.of("a", 1, "c", 2)).send());
    }

//    @Test
//    public void testHead(){
//        System.out.println(httpService.url("namespace3/test/head/ena").head());
//    }

    /**
     * delete必须有body
     */
    @Test
    public void testDelete(){
        System.out.println(httpService.delete("namespace3/test/delete/ena").body(NeoMap.of("a", 1)).send());
    }

    @Test
    public void testPut(){
        System.out.println(httpService.put("namespace3/test/put").body(NeoMap.of("a", 1222)).send());
    }

    @Test
    public void testPatch(){
        System.out.println(httpService.patch("namespace3/test/patch").body(NeoMap.of("patch", 199222)).send());
    }
}
