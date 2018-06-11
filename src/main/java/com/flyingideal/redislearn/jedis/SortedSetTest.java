package com.flyingideal.redislearn.jedis;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Tuple;
import redis.clients.jedis.ZParams;
import redis.clients.jedis.params.sortedset.ZAddParams;
import redis.clients.jedis.params.sortedset.ZIncrByParams;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author yanchao
 * @date 2018/6/11 18:47
 */
public class SortedSetTest extends BaseTest {

    private static final Logger logger = LoggerFactory.getLogger(SortedSetTest.class);

    private static final String sortedSetKey = "sortedSetKey";

    /**
     * ZADD key [NX|XX] [CH] [INCR] score member [score member ...]
     * 添加元素到Sorted Set中
     */
    @Test
    public void zadd() {
        jedis.del(sortedSetKey);
        // 添加一个元素
        long num = jedis.zadd(sortedSetKey, 1D, "one");
        logger.info("zadd(String, Double, String) num is {}", num);

        /**
         * 根据条件添加元素：
         *      XX: 仅仅更新存在的成员，不添加新成员；
         *      NX: 不更新存在的成员。只添加新成员；
         *      CH: 修改返回值为发生变化的成员总数，原始是返回新添加成员的总数 (CH 是 changed 的意思)
         *      INCR: 当ZADD指定这个选项时，成员的操作就等同ZINCRBY命令，对成员的分数进行递增操作
         */
        num = jedis.zadd(sortedSetKey, 5D, "one", ZAddParams.zAddParams().xx().ch());
        logger.info("zadd(String, double, String, ZAddParams) num is {}", num);

        // 添加多个元素，Map中的key值为Sorted Set中的元素，value为该元素对应的score
        Map<String, Double> map = new HashMap<>();
        map.put("two", 2D);
        map.put("three", 3D);
        num = jedis.zadd(sortedSetKey, map);
        logger.info("zadd(String, Map) num is {}", num);
        logger.info("{} value is {}", sortedSetKey, jedis.zrange(sortedSetKey, 0, -1));

        // 返回指定score范围内的Sorted Set元素
        Set<String> elements = jedis.zrangeByScore(sortedSetKey, 0, 3);
        logger.info("zrangeByScore result is {}", elements);

        // 返回指定score范围内的Sorted Set元素及其score值
        Set<Tuple> tuples = jedis.zrangeByScoreWithScores(sortedSetKey, 0, 2);
        tuples.stream().forEach(tuple -> logger.info("{}'s score is {}", tuple.getElement(), tuple.getScore()));
    }

    /**
     * ZCARD key
     * 返回key的有序集元素个数
     * 返回值：key存在的时候，返回有序集的元素个数，否则返回0
     */
    @Test
    public void zcard() {
        jedis.del(sortedSetKey);
        jedis.zadd(sortedSetKey, 1, "one");
        jedis.zadd(sortedSetKey, 1, "two");
        long size = jedis.zcard(sortedSetKey);
        logger.info("{} length is {}", sortedSetKey, size);
    }

    /**
     * ZCOUNT key min max
     * 返回有序集key中，score值在min和max之间(默认包括score值等于min或max)的成员个数
     * 与ZCARD相比，ZCOUNT的范围规则更加灵活和强大
     */
    @Test
    public void zcount() {
        jedis.del(sortedSetKey);
        Map<String, Double> map = new HashMap<>();
        map.put("one", 1D); map.put("two", 2D); map.put("three", 3D);
        jedis.zadd(sortedSetKey, map);
        long count = jedis.zcount(sortedSetKey, 0, 3);
        logger.info("zcount({}, 0, -1) result is {}", sortedSetKey, count);

        count = jedis.zcount(sortedSetKey, "(1", "3");
        logger.info("zcount({}, (1, 3) result is {}", sortedSetKey, count);

        count = jedis.zcount(sortedSetKey, "-inf", "+inf");
        logger.info("zcount({}, -inf, +inf) result is {}", sortedSetKey, count);
    }

    /**
     * ZINCRBY key increment member
     * 为有序集key的成员member的score值加上增量increment。
     *  如果key中不存在member，就在key中添加一个member，score是increment（就好像它之前的score是0.0）;
     *  如果key不存在，就创建一个只含有指定member成员的有序集合;
     *  当key不是有序集类型时，返回一个错误。
     *  score值必须是字符串表示的整数值或双精度浮点数，并且能接受double精度的浮点数。也有可能给一个负数来减少score的值。
     *
     *  返回值： member成员的新score值
     */
    @Test
    public void zincrby() {
        jedis.del(sortedSetKey);
        jedis.zadd(sortedSetKey, 1, "one");
        double newScore = jedis.zincrby(sortedSetKey, 2, "one");
        logger.info("after zincrby 2 score is {}", newScore);

        newScore = jedis.zincrby(sortedSetKey, 3, "one", ZIncrByParams.zIncrByParams().xx());
        logger.info("after zincrby 3 with xx score is {}", newScore);
    }

    /**
     * ZUNIONSTORE destination numkeys key [key ...] [WEIGHTS weight] [SUM|MIN|MAX]
     *
     * 计算给定的numkeys个有序集合的并集，并且把结果放到destination中。
     * 在给定要计算的key和其它参数之前，必须先给定key个数(numberkeys)。
     * 默认情况下，结果集中某个成员的score值是所有给定集下该成员score值之和。
     *
     * 使用WEIGHTS选项，你可以为每个给定的有序集指定一个乘法因子，
     *      即每个给定有序集的所有成员的score值在传递给聚合函数之前都要先乘以该因子。如果WEIGHTS没有给定，默认就是1。
     *
     * 使用AGGREGATE选项，你可以指定并集的结果集的聚合方式。
     *      默认使用的参数SUM，可以将所有集合中某个成员的score值之和作为结果集中该成员的score值。
     *      如果使用参数MIN或者MAX，结果集就是所有集合中元素最小或最大的元素。
     *
     * Jedis已经将WEIGHTS和AGGREGATE封装成了ZParams选项。
     *
     * 如果key destination存在，就被覆盖。
     *
     * 返回值： 结果有序集合destination中元素个数
     */
    @Test
    public void zunionstore() {
        jedis.del("sortedset1", "sortedset2", "sortedset3");
        Map<String, Double> map = new HashMap<>();
        map.put("one", 1D); map.put("two", 2D);
        // [one, two]
        jedis.zadd("sortedset1", map);
        map.put("three", 3D);
        // [one, two, three]
        jedis.zadd("sortedset2", map);
        logger.info("sortedset1 = {}, sortedset2 = {}", jedis.zrange("sortedset1", 0, -1), jedis.zrange("sortedset2", 0, -1));

        ZParams params = new ZParams();
        params.weightsByDouble(2, 3);
        params.aggregate(ZParams.Aggregate.SUM);

        long num = jedis.zunionstore("sortedset3", params, "sortedset1", "sortedset2");
        logger.info("zunionstore result is {}", num);

        // one's score : 1*2 + 1*3 = 5
        // two's score : 2*2 + 2*3 = 10
        // three's score : 0*3 + 3*3 = 9
        Set<Tuple> tuples = jedis.zrangeByScoreWithScores("sortedset3", "-inf", "+inf");
        tuples.stream().forEach(tuple -> logger.info("{}'s score is {}", tuple.getElement(), tuple.getScore()));
    }
}
