//package com.simon.king.server.config;
//
//import com.like.component.redis.RedisConnectionFactory;
//import com.like.component.redis.template.HashRedisTemplate;
//import com.like.component.redis.template.HyperLogLogTemplate;
//import com.like.component.redis.template.ListRedisTemplate;
//import com.like.component.redis.template.SetRedisTemplate;
//import com.like.component.redis.template.ValueRedisTemplate;
//import com.like.component.redis.template.ZSetRedisTemplate;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import redis.clients.jedis.JedisPoolConfig;
//
///**
// *
// * @author zhaoshuai
// * @date 2018/9/7
// */
//@Configuration
//public class RedisConfig {
//    @Value("${spring.redis.host}")
//    private String host;
//
//    @Value("${spring.redis.port}")
//    private Integer port;
//
//    @Value("${spring.redis.password}")
//    private String password;
//
//    @Value("${spring.redis.pool.max-wait}")
//    private Integer maxWait;
//
//    @Value("${spring.redis.pool.min-idle}")
//    private Integer minIdle;
//
//    @Value("${spring.redis.pool.max-idle}")
//    private Integer maxIdle;
//
//    @Value("${spring.redis.pool.max-active}")
//    private Integer maxActive;
//
//    @Bean
//    public JedisPoolConfig jedisPoolConfig() {
//        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
//        jedisPoolConfig.setMaxWaitMillis(maxWait);
//        jedisPoolConfig.setMinIdle(minIdle);
//        jedisPoolConfig.setMaxIdle(maxIdle);
//        jedisPoolConfig.setMaxTotal(maxActive);
//        return jedisPoolConfig;
//    }
//
//    @Bean(initMethod = "init", destroyMethod = "destroy")
//    public RedisConnectionFactory redisConnectionFactory(JedisPoolConfig jedisPoolConfig) {
//        /*(initMethod = "init", destroyMethod = "destroy")*/
//        RedisConnectionFactory redisConnectionFactory = new RedisConnectionFactory();
//        redisConnectionFactory.setPoolConfig(jedisPoolConfig);
//        redisConnectionFactory.setServers(host + ":" + port);
//        redisConnectionFactory.setPassword(password);
//        return redisConnectionFactory;
//    }
//
//    @Bean
//    public ZSetRedisTemplate zSetRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
//        ZSetRedisTemplate zSetRedisTemplate = new ZSetRedisTemplate();
//        zSetRedisTemplate.setConnectionFactory(redisConnectionFactory);
//        return zSetRedisTemplate;
//    }
//
//    @Bean
//    public ListRedisTemplate listRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
//        ListRedisTemplate listRedisTemplate = new ListRedisTemplate();
//        listRedisTemplate.setConnectionFactory(redisConnectionFactory);
//        return listRedisTemplate;
//    }
//
//    @Bean
//    public SetRedisTemplate setRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
//        SetRedisTemplate setRedisTemplate = new SetRedisTemplate();
//        setRedisTemplate.setConnectionFactory(redisConnectionFactory);
//        return setRedisTemplate;
//    }
//
//    @Bean
//    public ValueRedisTemplate valueRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
//        ValueRedisTemplate valueRedisTemplate = new ValueRedisTemplate();
//        valueRedisTemplate.setConnectionFactory(redisConnectionFactory);
//        return valueRedisTemplate;
//    }
//
//    @Bean
//    public HashRedisTemplate hashRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
//        HashRedisTemplate hashRedisTemplate = new HashRedisTemplate();
//        hashRedisTemplate.setConnectionFactory(redisConnectionFactory);
//        return hashRedisTemplate;
//    }
//
//    @Bean
//    public HyperLogLogTemplate hyperLogLogTemplate(RedisConnectionFactory redisConnectionFactory) {
//        HyperLogLogTemplate hyperLogLogTemplate = new HyperLogLogTemplate();
//        hyperLogLogTemplate.setConnectionFactory(redisConnectionFactory);
//        return hyperLogLogTemplate;
//    }
//}
