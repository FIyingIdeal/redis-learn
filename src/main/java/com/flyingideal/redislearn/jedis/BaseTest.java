package com.flyingideal.redislearn.jedis;

import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;

/**
 * @author yanchao
 * @date 2018/6/10 16:28
 */
public abstract class BaseTest {

    private static final Logger logger = LoggerFactory.getLogger(BaseTest.class);

    protected Jedis jedis;

    @Before
    public void setJedis() {
        jedis = new Jedis("192.168.99.236");
        logger.info("redis connect success");
    }
}
