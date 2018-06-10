package com.flyingideal.redislearn.jedis;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.ScanResult;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author yanchao
 * @date 2018/6/10 16:28
 */
public class HashTest extends BaseTest {

    private static final Logger logger = LoggerFactory.getLogger(HashTest.class);

    private static final String keyName = "person";

    /**
     *
     * 设置 key 指定的哈希集中指定字段的值。
     *      如果 key 指定的哈希集不存在，会创建一个新的哈希集并与 key 关联。
     *      如果字段在哈希集中存在，它将被重写。
     *
     * 返回值: 1如果field是一个新的字段； 0如果field原来在map里面已经存在
     */
    @Test
    public void hset() {
        long num = jedis.hset(keyName, "name", "zhangsan");
        Assert.assertEquals(num, 1);
    }

    /**
     * HSETNX key field value
     * 只在 key 指定的哈希集中不存在指定的字段时，设置字段的值。如果 key 指定的哈希集不存在，会创建一个新的哈希集并与 key 关联。如果字段已存在，该操作无效果
     * 返回值： 1：如果字段是个新的字段，并成功赋值；
     *        0：如果哈希集中已存在该字段，没有操作被执行
     */
    @Test
    public void hsetnx() {
        long num = jedis.hsetnx(keyName, "name", "lisi");
        Assert.assertEquals(0, num);
        Assert.assertEquals("zhangsan", jedis.hget(keyName,"name"));
        num = jedis.hsetnx(keyName, "hsetnxtest", "hsetnxvalue");
        Assert.assertEquals(num, 1);
        Assert.assertEquals("hsetnxvalue", jedis.hget(keyName,"hsetnxtest"));
        num = jedis.hdel(keyName, "hsetnxtest");
        Assert.assertEquals(1, num);

    }

    /**
     * HMSET key field value [field value ...]
     * 设置 key 指定的哈希集中指定字段的值。该命令将重写所有在哈希集中存在的字段。如果 key 指定的哈希集不存在，会创建一个新的哈希集并与 key 关联
     */
    @Test
    public void hmset() {
        Map<String, String> map = new HashMap<>();
        map.put("age", "18");
        map.put("sex", "boy");
        String result = jedis.hmset(keyName, map);
        logger.info("result is {}", result); // OK
        logger.info("{} is {}", keyName, jedis.hgetAll(keyName));
    }

    /**
     * HGET key field
     * 返回 key 指定的哈希集中该字段所关联的值
     *
     * 返回值：该字段所关联的值。当字段不存在或者 key 不存在时返回nil
     */
    @Test
    public void hget() {
        // hset();
        String name = jedis.hget(keyName, "name");
        Assert.assertEquals("zhangsan", name);
    }

    /**
     * HMGET key field [field ...]
     * 返回 key 指定的哈希集中指定字段的值。
     * 对于哈希集中不存在的每个字段，返回 nil 值。因为不存在的keys被认为是一个空的哈希集，对一个不存在的 key 执行 HMGET 将返回一个只含有 nil 值的列表
     */
    @Test
    public void hmget() {
        List<String> mget = jedis.hmget(keyName, "name", "age", "sex", "notexists");
        logger.info("hmget result is {}", mget);
    }

    /**
     * HGETALL key
     * 返回 key 指定的哈希集中所有的字段和值。返回值中，每个字段名的下一个是它的值，所以返回值的长度是哈希集大小的两倍
     * 返回值： 哈希集中字段和值的列表。当 key 指定的哈希集不存在时返回空列表。
     */
    @Test
    public void hgetAll() {
        Map<String, String> map = jedis.hgetAll(keyName);
        logger.info("hgetAll result is {}", map);
    }

    /**
     * HKEYS key
     * 返回 key 指定的哈希集中所有字段的名字。
     * 返回值： 哈希集中的字段列表，当 key 指定的哈希集不存在时返回空列表
     */
    @Test
    public void hkeys() {
        Set<String> keys = jedis.hkeys(keyName);
        logger.info("keys is {}", keys);
    }

    /**
     * HVALS key
     * 返回 key 指定的哈希集中所有字段的值
     * 返回值： 哈希集中的值的列表，当 key 指定的哈希集不存在时返回空列表
     */
    @Test
    public void hvals() {
        List<String> vals = jedis.hvals(keyName);
        logger.info("vals is {}", vals);
    }

    /**
     * HLEN key
     * 返回 key 指定的哈希集包含的字段的数量
     * 返回值：哈希集中字段的数量，当 key 指定的哈希集不存在时返回 0
     */
    @Test
    public void hlen() {
        Long length = jedis.hlen(keyName);
        logger.info("length is {}", length);
    }

    /**
     * HEXISTS key field
     * 返回hash里面field是否存在
     * 返回值 ： 1(true) hash里面包含该field；0(false) hash里面不包含该field或者key不存在
     */
    @Test
    public void hexists() {
        boolean b = jedis.hexists(keyName, "name");
        boolean b1 = jedis.hexists(keyName, "not_exists_field");
        logger.info("name exists : {}, not_exists_field exists {}", b, b1);
    }

    /**
     * HDEL key field [field ...]
     * 从 key 指定的哈希集中移除指定的域。在哈希集中不存在的域将被忽略。如果 key 指定的哈希集不存在，它将被认为是一个空的哈希集，该命令将返回0。
     * 返回值： 返回从哈希集中成功移除的域的数量，不包括指出但不存在的那些域
     */
    @Test
    public void hdel() {
        jedis.hset(keyName, "delKey", "delvalue");
        Assert.assertNotNull(jedis.hget(keyName, "delKey"));
        long delNum = jedis.hdel(keyName, "delKey");
        logger.info("hdel count is {}", delNum);
    }

    /**
     * HINCRBY key field increment
     * 增加 key 指定的哈希集中指定字段的数值。如果 key 不存在，会创建一个新的哈希集并与 key 关联。如果字段不存在，则字段的值在该操作执行前被设置为 0
     * HINCRBY 支持的值的范围限定在 64位 有符号整数
     * 返回值： 增值操作执行后的该字段的值。
     */
    @Test
    public void hincrBy() {
        long result = jedis.hincrBy(keyName, "age", 1);
        logger.info("after hincrBy 1 : {}", result);
        result = jedis.hincrBy(keyName, "age", -2);
        logger.info("after hincrBy -2 : {}", result);
    }

    /**
     * HINCRBYFLOAT key field increment
     * 为指定key的hash的field字段值执行float类型的increment加。如果field不存在，则在执行该操作前设置为0.如果出现下列情况之一，则返回错误：
     *      1.field的值包含的类型错误(不是字符串);
     *      2.当前field或者increment不能解析为一个float类型。
     * 返回值： field执行increment加后的值
     */
    @Test
    public void hincrByFloat() {
        double result = jedis.hincrByFloat(keyName, "age", 1.5D);
        logger.info("after hincrByFloat 1.5: {}", result);
    }

    /**
     * todo 查看下边的链接内容
     * @see <a href="http://www.redis.cn/commands/scan.html"></a>
     */
    @Test
    public void hscan() {
        ScanResult<Map.Entry<String, String>> result = jedis.hscan(keyName, "0");
        logger.info("{}, {}", result.getResult(), result.getStringCursor());
    }

}
