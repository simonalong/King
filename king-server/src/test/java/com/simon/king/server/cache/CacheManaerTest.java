package com.simon.king.server.cache;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.simon.neo.NeoMap;
import java.util.concurrent.TimeUnit;
import org.junit.Test;

/**
 * @author zhouzhenyong
 * @since 2019/5/18 下午5:59
 */
public class CacheManaerTest {

    /**
     * guava 缓存用法
     */
    @Test
    public void test1() {
        LoadingCache<Integer, String> cacheManagerLocal = CacheBuilder
            .newBuilder()
            //设置写缓存后3秒钟过期
            .expireAfterWrite(3, TimeUnit.MINUTES)
            //设置缓存容器的初始容量为10
            .initialCapacity(10)
            //设置要统计缓存的命中率
            .recordStats()
            .build(new CacheLoader<Integer, String>() {
                @Override
                public String load(Integer index) throws Exception {
                    System.out.println("重新加载");
                    return "你好" + index;
                }
            });

        try {
            for (int index = 0; index < 20; index++) {
                System.out.println(cacheManagerLocal.get(4));
                TimeUnit.SECONDS.sleep(1);
            }

            System.out.println("***********");
            System.out.println(cacheManagerLocal.stats().toString());
            System.out.println("***********");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 缓存key为NeoMap时候的处理
     */
    @Test
    public void test2() {
        LoadingCache<NeoMap, String> cacheManagerLocal = CacheBuilder
            .newBuilder()
            //设置写缓存后3秒钟过期
            .expireAfterWrite(3, TimeUnit.MINUTES)
            //设置缓存容器的初始容量为10
            .initialCapacity(10)
            //设置要统计缓存的命中率
            .recordStats()
            .build(new CacheLoader<NeoMap, String>() {
                @Override
                public String load(NeoMap index) throws Exception {
                    System.out.println("重新加载");
                    return "你好" + index;
                }
            });

        try {
            for (int index = 0; index < 20; index++) {
                System.out.println(cacheManagerLocal.get(NeoMap.of("a", 1)));
                TimeUnit.SECONDS.sleep(1);
            }

            System.out.println("***********");
            System.out.println(cacheManagerLocal.stats().toString());
            System.out.println("***********");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
