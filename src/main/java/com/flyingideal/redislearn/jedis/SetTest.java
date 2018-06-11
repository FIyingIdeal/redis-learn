package com.flyingideal.redislearn.jedis;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Set;

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
        logger.info("sadd result is {}", num); // 3
        num = jedis.sadd(keyName, "4");
        logger.info("num = {}", num); // 1
    }

    /**
     * SMEMBERS key
     * 返回key集合所有的元素.该命令的作用与使用一个参数的SINTER 命令作用相同
     * 返回值：集合中的所有元素.
     * @see this#sinter()
     */
    @Test
    public void smembers() {
        jedis.del(keyName);
        jedis.sadd(keyName, "1", "2", "3", "1");
        Set<String> members = jedis.smembers(keyName);
        logger.info("members is {}", members);
    }

    /**
     * SISMEMBER key member
     * 返回成员 member 是否是存储的集合 key的成员
     * 返回值：
     *       如果member元素是集合key的成员，则返回1；
     *       如果member元素不是key的成员，或者集合key不存在，则返回0；
     */
    @Test
    public void sismember() {
        jedis.del(keyName);
        jedis.sadd(keyName, "1", "2", "3", "1");
        boolean notExists = jedis.sismember(keyName, "4");
        boolean exists = jedis.sismember(keyName, "1");
        logger.info("notExists = {}, exists = {}", notExists, exists);
    }

    /**
     * SCARD key
     * 返回集合存储的key的基数 (集合元素的数量).
     * 返回值：集合的基数(元素的数量),如果key不存在,则返回 0
     */
    @Test
    public void scard() {
        long size = jedis.scard(keyName);
        logger.info("set size is {}", size);
    }

    /**
     * SDIFF key [key ...]
     * Returns the members of the set resulting from the difference between the first set and all the successive sets
     * 返回的集合元素是第一个key的集合与后面所有key的集合的差集
     * 返回值： Set
     */
    @Test
    public void sdiff() {
        // 先将测试的key清除掉，防止其影响测试结果
        jedis.del("set1", "set2", "set3");
        jedis.sadd("set1", "1", "2", "3");
        jedis.sadd("set2", "1", "2", "4");
        jedis.sadd("set3", "1", "5", "6");
        Set<String> diff = jedis.sdiff("set1", "set2", "set3");
        logger.info("sdiff result is {}", diff);
    }

    /**
     * SDIFFSTORE destination key [key ...]
     * 该命令类似于 SDIFF, 不同之处在于该命令不返回结果集，而是将结果存放在destination集合中.
     * 如果destination已经存在, 则将其覆盖重写
     * 返回值： 结果集元素的个数
     */
    @Test
    public void sdiffstore() {
        jedis.del("set1", "set2", "set3", "set4");
        jedis.sadd("set1", "1", "2", "3");
        jedis.sadd("set2", "1", "2", "4");
        jedis.sadd("set3", "1", "5", "6");
        // 测试 ： “如果destination已经存在, 则将其覆盖重写”
        jedis.sadd("set4", "1");
        // set4中只有1
        logger.info("before sdiffsotre set4 is  {}", jedis.smembers("set4"));
        Long num = jedis.sdiffstore("set4", "set1", "set2", "set3");
        // 1
        logger.info("diff count is {}", num);
        // set4中只有3
        logger.info("after sdiffsotre set4 is {}", jedis.smembers("set4"));
    }

    /**
     * SMOVE source destination member
     * 将member从source集合移动到destination集合中. 对于其他的客户端,在特定的时间元素将会作为source或者destination集合的成员出现.
     *      如果source 集合不存在或者不包含指定的元素,这smove命令不执行任何操作并且返回0.
     *      否则对象将会从source集合中移除，并添加到destination集合中去;
     *      如果destination集合已经存在该元素，则smove命令仅将该元素从source集合中移除.
     *      如果source 和destination不是集合类型,则返回错误.
     * 返回值：
     *      如果该元素成功移除（只要从source中移除就ok，不管是否添加到destination中）,返回1；
     *      如果该元素不是 source集合成员,无任何操作,则返回0.
     */
    @Test
    public void smove() {
        jedis.del("set1", "set2");
        jedis.sadd("set1", "1", "2", "3");
        jedis.sadd("set2", "1", "2", "4");
        // smove一个set1和set2都存在的元素
        long num1 = jedis.smove("set1", "set2", "1");
        logger.info("num1 = {}, set1 = {}, set2 = {}", num1, jedis.smembers("set1"), jedis.smembers("set2"));
        // smove一个set1存在，set2不存在的元素
        long num2 = jedis.smove("set1", "set2", "3");
        logger.info("num2 = {}, set1 = {}, set2 = {}", num2, jedis.smembers("set1"), jedis.smembers("set2"));
        // 移除一个set1，set2不存在的元素，这个不存在的元素并不会被添加到set2中
        long num3 = jedis.smove("set1", "set2", "5");
        logger.info("num3 = {}, set1 = {}, set2 = {}", num3, jedis.smembers("set1"), jedis.smembers("set2"));
    }

    /**
     * SPOP key [count]
     * Removes and returns one or more random elements from the set value store at key.
     * 随机移除并返回set对应key中的一个或多个元素（count指定移除的个数，默认是1个），如果count指定数字大于set中元素的总数，只返回set中的所有元素
     * This operation is similar to SRANDMEMBER, that returns one or more random elements from a set but does not remove it.
     * 这个操作与SRANDMEMBER，但SRANDMEMBER仅仅返回元素并不移除，而SPOP返回的同时会将元素从set中移除掉
     * The count argument will be available in a later version and is not available in 2.6, 2.8, 3.0
     * count参数在之后的版本中会提供支持（3.1版本中正是支持）但在2.6, 2.8, 3.0版本中并不支持
     *
     * 返回值：the removed element, or nil when key does not exist.
     *
     * @see this#srandmember()
     */
    @Test
    public void spop() {
        jedis.del("set1");
        jedis.sadd("set1", "1", "2", "3", "4");
        String removedElement = jedis.spop("set1");
        // 随机返回，多次运行结果spop结果并不相同
        logger.info("spop(key) result is {}, after spop set1 is {}", removedElement, jedis.smembers("set1"));
        Set<String> removedElements = jedis.spop("set1", 2);
        logger.info("spop(key, count) result is {}, after spop set1 is {}", removedElements, jedis.smembers("set1"));
    }

    /**
     *
     * 仅提供key参数,那么随机返回key集合中的一个元素.
     * Redis 2.6开始, 可以接受 count 参数：
     *      如果count是整数且小于元素的个数，返回含有 count 个不同的元素的数组
     *      如果count是个整数且大于集合中元素的个数时,仅返回整个集合的所有元素
     *      当count是负数,则会返回一个包含count的绝对值的个数元素的数组
     *      如果count的绝对值大于元素的个数,则返回的结果集里会出现一个元素出现多次的情况.
     *      仅提供key参数时,该命令作用类似于SPOP命令, 不同的是SPOP命令会将被选择的随机元素从集合中移除, 而SRANDMEMBER仅仅是返回该随记元素,而不做任何操作.
     * 返回值：
     *      不使用count 参数的情况下该命令返回随机的元素,如果key不存在则返回nil.
     *      使用count参数,则返回一个随机的元素数组,如果key不存在则返回一个空的数组.
     *
     * @see this#spop()
     */
    @Test
    public void srandmember() {
        jedis.del("set1");
        jedis.sadd("set1", "1", "2", "3", "4");
        String randomElement = jedis.srandmember("set1");
        logger.info("spop(key) result is {}, after spop set1 is {}", randomElement, jedis.smembers("set1"));
        // 当指定count数字大于Set中元素总数，只返回Set所有元素，并不会重复返回元素，与描述不符...
        List<String> randomElements = jedis.srandmember("set1", 7);
        logger.info("spop(key, count) result is {}, after spop set1 is {}", randomElements, jedis.smembers("set1"));
    }

    /**
     * SREM key member [member ...]
     * 在key集合中移除指定的元素. 如果指定的元素不是key集合中的元素则忽略 如果key集合不存在则被视为一个空的集合，该命令返回0.
     * 如果key的类型不是一个集合,则返回错误.
     * 返回值：
     *      从集合中移除元素的个数，不包括不存在的成员
     */
    @Test
    public void srem() {
        jedis.del("set1");
        jedis.sadd("set1", "1", "2", "3", "4");
        long num = jedis.srem("set1", "1", "5");
        logger.info("srem result is {}, after srem set1 is {}", num, jedis.smembers("set1"));
    }

    /**
     * SINTER key [key ...]
     * 返回指定所有的集合的成员的交集.
     * 如果key不存在则被认为是一个空的集合,当给定的集合为空的时候,结果也为空.(一个集合为空，结果一直为空)
     */
    @Test
    public void sinter() {
        jedis.del("set1", "set2", "set3");
        jedis.sadd("set1", "1", "2", "3");
        jedis.sadd("set2", "1", "2", "4");
        jedis.sadd("set3", "1", "5", "6");
        Set<String> inter = jedis.sinter("set1", "set2", "set3");
        logger.info("sinter result is {}", inter);
    }

    /**
     * SINTERSTORE destination key [key ...]
     * 这个命令与SINTER命令类似, 但是它并不是直接返回结果集,而是将结果保存在 destination集合中.
     * 如果destination 集合存在, 则会被重写
     * 返回值：结果集中成员的个数
     */
    @Test
    public void sinterstore() {
        jedis.del("set1", "set2", "set3", "set4");
        jedis.sadd("set1", "1", "2", "3");
        jedis.sadd("set2", "1", "2", "4");
        jedis.sadd("set3", "1", "2", "6");
        // 测试 ： “如果destination已经存在, 则将其覆盖重写”
        jedis.sadd("set4", "0");
        // set4中只有[0]
        logger.info("before sinterstore set4 is  {}", jedis.smembers("set4"));
        Long num = jedis.sinterstore("set4", "set1", "set2", "set3");
        // 2
        logger.info("inter count is {}", num);
        // set4中只有[1, 2]
        logger.info("after sinterstore set4 is {}", jedis.smembers("set4"));
    }

    /**
     * SUNION key [key ...]
     * 返回给定的多个集合的并集中的所有成员.
     * 不存在的key可以认为是空的集合.
     * 返回值：
     *      并集的成员列表
     */
    @Test
    public void sunion() {
        jedis.del("set1", "set2");
        jedis.sadd("set1", "1", "2", "3");
        jedis.sadd("set2", "1", "2", "4");
        Set<String> union = jedis.sunion("set1", "set2");
        logger.info("set1 = {}, set2 = {}, after union is {}", jedis.smembers("set1"), jedis.smembers("set2"), union);
    }

    /**
     * SUNIONSTORE destination key [key ...]
     * 该命令作用类似于SUNION命令,不同的是它并不返回结果集,而是将结果存储在destination集合中.
     * 如果destination 已经存在,则将其覆盖.
     * 返回值：
     *      结果集中元素的个数
     */
    @Test
    public void sunionstore() {
        jedis.del("set1", "set2", "set3", "set4");
        jedis.sadd("set1", "1", "2", "3");
        jedis.sadd("set2", "1", "2", "4");
        jedis.sadd("set3", "1", "2", "6");
        // 测试 ： “如果destination已经存在, 则将其覆盖重写”
        jedis.sadd("set4", "0");
        // set4中只有[0]
        logger.info("before sunionstore set4 is  {}", jedis.smembers("set4"));
        Long num = jedis.sunionstore("set4", "set1", "set2", "set3");
        // 5
        logger.info("union count is {}", num);
        // set4中只有[1, 2, 3, 4, 6]，没有0
        logger.info("after sunionstore set4 is {}", jedis.smembers("set4"));
    }

}
