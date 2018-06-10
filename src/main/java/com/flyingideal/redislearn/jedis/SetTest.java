package com.flyingideal.redislearn.jedis;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author yanchao
 * @date 2018/6/10 23:25
 */
public class SetTest extends BaseTest {

    private static final Logger logger = LoggerFactory.getLogger(SetTest.class);

    private static final String keyName = "setName";

    /**
     * SADD key member [member ...]
     * 添加一个或多个指定的member元素到集合的 key中.指定的一个或者多个元素member 如果已经在集合key中存在则忽略.
     * 如果集合key 不存在，则新建集合key,并添加member元素到集合key中.如果key 的类型不是集合则返回错误
     * 返回值： 返回新成功添加到集合里元素的数量，不包括已经存在于集合中的元素.
     */
    @Test
    public void sadd() {
        long num = jedis.sadd(keyName, "1", "2", "3", "1");
        logger.info("sadd result is {}", num);
        num = jedis.sadd(keyName, "4");
        logger.info("num = {}", num); // 1
    }
}
