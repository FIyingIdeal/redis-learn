package com.flyingideal.redislearn.jedis;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.concurrent.TimeUnit;

/**
 * @author yanchao
 * @date 2018/6/10 10:47
 */
public class StringTest extends BaseTest {

    private static final Logger logger = LoggerFactory.getLogger(StringTest.class);

    /**
     * redis string 数据类型测试
     */
    @Test
    public void testString() {
        // set
        jedis.set("name", "Jedis");
        jedis.set("subject", "study Jedis");
        // 第三个参数描述：@param nxxx NX|XX
        //    NX -- Only set the key if it does not already exist.  key不存在时才设置
        //    XX -- Only set the key if it already exist.           key存在时才设置
        jedis.set("name", "zhangsan", "NX");      // 由于设置了nxxx为NX，则该设置无效，name依然为Jedis。相当于setnx()
        jedis.set("test", "test", "XX");          // 由于设置了nxxx为XX，则该设置同样无效，因为之前不存在一个key为test，test依然为null
        // get
        logger.info("name is {}", jedis.get("name"));
        logger.info("subject is {}", jedis.get("subject"));
        logger.info("test is {}", jedis.get("test"));

        // strlen
        logger.info("subject length is {}", jedis.strlen("subject"));

        // getrange
        logger.info("getrange subject [0, 2] is {}", jedis.getrange("subject", 0, 2));

        // append
        jedis.append("subject", " form 2018-6-10 11:17:40");
        logger.info("after append, subject is {}", jedis.get("subject"));

        // del : 返回1或0标识值是被删除(值存在)或者没被删除(key对应的值不存在)
        long num = jedis.del("subject");
        logger.info("after del, subject is {}, del num is {}", jedis.get("subject"), num);

        // mset
        jedis.mset("age", "23", "sex", "boy");

        // mget
        logger.info("mget is {}", jedis.mget("age", "sex"));

        // incr : 原子递增
        jedis.set("num", "0");
        // 返回值即为最终值，下同
        long result = jedis.incr("num");
        logger.info("after incr num = {}, result = {}", jedis.get("num"), result);
        // incrBy : 增加一个指定的数字
        result = jedis.incrBy("num", 10);
        logger.info("after incrBy num = {}, result = {}", jedis.get("num"), result);
        // decr : 原子递减
        jedis.decr("num");
        logger.info("after decr num is {}", jedis.get("num"));
        // decrBy
        result = jedis.decrBy("num", 5);
        logger.info("after desrBy num = {}, result = {}", jedis.get("num"), result);

        // getSet : 返回旧值并设置新值
        String sResult = jedis.getSet("num", "0");
        logger.info("getSet result = {}", sResult);
        logger.info("after getSet, num = {}", jedis.get("num"));

        // type
        String type = jedis.type("num");
        logger.info("num type is {}", type);

        // expire : 设置有效时间，单位为秒
        jedis.expire("num", 1);
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        // 超时之后获取到的结果为null
        logger.info("after expire, num is {}", jedis.get("num"));

        // persist : 取消超时  这里这是第三个参数为NX，是因为之前num已经失效了，key为num的记录并不存在
        // 第四个参数说明：expx EX|PX, expire time units: EX = seconds; PX = milliseconds
        jedis.set("num", "10", "NX", "PX", 5000);
        try {
            TimeUnit.MILLISECONDS.sleep(400);
            // 在这里取消超时设置
            //jedis.persist("num");
            TimeUnit.MILLISECONDS.sleep(1000);
        } catch (Exception e) {
            e.printStackTrace();
        }
        logger.info("after persist, num is {}", jedis.get("num"));

        // ttl
        /**
         * @return Integer reply, returns the remaining time to live in seconds of a key that has an
         *         EXPIRE. In Redis 2.6 or older, if the Key does not exists or does not have an
         *         associated expire, -1 is returned. In Redis 2.8 or newer, if the Key does not have an
         *         associated expire, -1 is returned or if the Key does not exists, -2 is returned.
         */
        long ttl = jedis.ttl("num");
        logger.info("num ttl = {}", ttl);

        // setex : 为指定的 key 设置值及其过期时间（单位秒）。如果 key 已经存在， SETEX 命令将会替换旧的值。
        // psetex : 与setex类似，但过期时间的单位为毫秒
        jedis.setex("setex", 1, "setexvalue");
        logger.info("after setex, value is {}", jedis.get("setex"));
        jedis.psetex("setex", 1000L, "setexvalue");

        // setnx : 只有在 key 不存在时设置 key 的值。
        jedis.setnx("setnx", "setnxvalue123");
        logger.info("after setnx, value is {}", jedis.get("setnx"));
    }

}
