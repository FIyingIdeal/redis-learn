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
     * ZRANGE key start stop [WITHSCORES]
     * 返回key中指定score区间的所有成员，如果添加了WITHSCORES，则成员与其对应的score一并返回
     * 如果start > stop，查询并不会报错，只是返回一个空集合
     *
     * @see this#zrevrange()
     * @see this#zrangeByLex()
     * @see this#zrangeByScore()
     */
    @Test
    public void zrange() {
        Map<String, Double> map = new HashMap<>();
        map.put("one", 1D); map.put("two", 2D); map.put("three", 3D); map.put("four", 3D);
        jedis.zadd(sortedSetKey, map);
        logger.info("{} value is {}", sortedSetKey, jedis.zrange(sortedSetKey, 0, -1));

        // WITHSCORES 成员与其对应的score一并返回
        Set<Tuple> tuples = jedis.zrangeByScoreWithScores(sortedSetKey, 0, 2);
        tuples.stream().forEach(tuple -> logger.info("{}'s score is {}", tuple.getElement(), tuple.getScore()));
    }

    /**
     * ZRANGEBYLEX key min max [LIMIT offset count]
     * ZRANGEBYLEX 返回指定成员区间内的成员，按成员字典正序排序, 分数必须相同:
     *      分数必须相同! 如果有序集合中的成员分数有不一致的,返回的结果就不准;
     *      成员字符串作为二进制数组的字节数进行比较;
     *      默认是以ASCII字符集的顺序进行排列，如果成员字符串包含utf-8这类字符集的内容,就会影响返回结果,所以建议不要使用;
     *      默认情况下, “max” 和 “min” 参数前必须加 “[” 符号作为开头。”[” 符号与成员之间不能有空格, 返回成员结果集会包含参数 “min” 和 “max” ;
     *      “max” 和 “min” 参数前可以加 “(“ 符号作为开头表示小于, “(“ 符号与成员之间不能有空格。返回成员结果集不会包含 “max” 和 “min” 成员;
     *      可以使用 “-“ 和 “+” 表示得分最小值和最大值;
     *      “min” 和 “max” 不能反, “max” 放前面 “min”放后面会导致返回结果为空;
     *      与ZRANGEBYLEX获取顺序相反的指令是ZREVRANGEBYLEX;
     *      源码中采用C语言中 memcmp() 函数, 从字符的第0位到最后一位进行排序,如果前面部分相同,那么较长的字符串比较短的字符串排序靠后。
     *
     * @see this#zrange()
     * @see this#zrangeByScore()
     * @see this#zrevrangeByLex()
     * @see this#zremrangeByLex()
     */
    @Test
    public void zrangeByLex() {
        jedis.del("sortedset1");
        Map<String, Double> map = new HashMap<>();
        // 使用该命令的前提是所有成员的score值必须相同，否则返回的结果不准确
        map.put("a", 1D); map.put("b", 1D); map.put("c", 1D);
        map.put("d", 1D); map.put("e", 1D); map.put("f", 1D);
        map.put("g", 1D);
        jedis.zadd("sortedset1", map);

        // 获取所有成员  [a, b, c, d, e, f, g]
        Set<String> elements = jedis.zrangeByLex("sortedset1", "-", "+");
        logger.info("zrangeByLex(String, -, +) result is {}", elements);

        // 获取两个成员之间的所有成员  [c, d, e]
        elements = jedis.zrangeByLex("sortedset1", "[c", "(f");
        logger.info("zrangeByLex(String, \"[c\", \"(f\" ) result is {}", elements);

        // 分页获取成员  [c, d]
        elements = jedis.zrangeByLex("sortedset1", "[c", "(f", 0, 2);
        logger.info("zrangeByLex(String, \"[c\", \"(f\", 0, 2 ) result is {}", elements);
        // 分页获取成员 [e]
        elements = jedis.zrangeByLex("sortedset1", "[c", "(f", 2, 2);
        logger.info("zrangeByLex(String, \"[c\", \"(f\", 2, 2 ) result is {}", elements);
    }

    /**
     * ZRANGEBYSCORE key min max [WITHSCORES] [LIMIT offset count]
     * 指定分数范围的元素列表(也可以返回他们的分数)
     *      可选参数WITHSCORES会返回元素和其分数，而不只是元素。这个选项在redis2.0之后的版本都可用;
     *      min和max可以是-inf和+inf，这样一来，你就可以在不知道有序集的最低和最高score值的情况下，使用ZRANGEBYSCORE这类命令;
     *      默认情况下，区间的取值使用闭区间(小于等于或大于等于)，你也可以通过给参数前增加 ( 符号来使用可选的开区间(小于或大于)
     *
     * @see this#zrange()
     * @see this#zrangeByLex()
     * @see this#zrevrangeByScore()
     * @see this#zremrangeByScore()
     */
    @Test
    public void zrangeByScore() {
        jedis.del("sortedset1");
        Map<String, Double> map = new HashMap<>();
        map.put("a", 0D); map.put("b", 1D); map.put("c", 2D);
        map.put("d", 3D); map.put("e", 4D); map.put("f", 5D);
        map.put("g", 6D);
        jedis.zadd("sortedset1", map);

        // 获取指定score区间内的成员，score为double类型，包含这两个边界  [b, c, d, e, f]
        Set<String> elements = jedis.zrangeByScore("sortedset1", 1, 5);
        logger.info("zrangeByScore(String, 1, 5) result is {}", elements);

        // 获取指定score区间内的成员，score为String类型，是否包含边界取决于score的定义  [c, d, e, f]
        // 注意，如果需要包含边界的话，无需添加 [
        elements = jedis.zrangeByScore("sortedset1", "(1", "5");
        logger.info("zrangeByScore(String, \"(1\", 5) result is {}", elements);

        // 获取所有成员  min和max可以是-inf和+inf
        elements = jedis.zrangeByScore("sortedset1", "-inf", "+inf");
        logger.info("zrangeByScore(String, \"-inf\", \"+inf\") result is {}", elements);

        // 分页获取成员 [a, b, c]
        elements = jedis.zrangeByScore("sortedset1", "-inf", "+inf", 0, 3);
        logger.info("zrangeByScore(String, \"-inf\", \"+inf\", 0, 3) result is {}", elements);

        // 成员与Score一并返回
        Set<Tuple> tuples = jedis.zrangeByScoreWithScores("sortedset1", "-inf", "+inf", 0, 3);
        tuples.stream().forEach(tuple -> logger.info("{}'s score is {}", tuple.getElement(), tuple.getScore()));
    }

    /**
     * ZREVRANGE key start stop [WITHSCORES]
     * 返回有序集key中，指定区间内的成员。其中成员的位置按score值递减(从大到小)来排列;
     * 具有相同score值的成员按字典序的反序排列。
     *
     * @see this#zrange()
     */
    @Test
    public void zrevrange() {
        jedis.del("sortedset1");
        Map<String, Double> map = new HashMap<>();
        map.put("a", 0D); map.put("b", 1D); map.put("c", 2D);
        map.put("d", 2D); map.put("e", 4D); map.put("f", 5D);
        map.put("g", 6D);
        jedis.zadd("sortedset1", map);

        //  [f, e, d, c, b]
        Set<String> elements = jedis.zrevrange("sortedset1", 1, 5);
        logger.info("zrevrange(String, 1, 5) result is {}", elements);

        // []  score的参数顺序没有变，第一个依然是小值，否则获取到的是空集合
        elements = jedis.zrevrange("sortedset1", 5, 1);
        logger.info("zrevrange(String, 1, 5) result is {}", elements);

        Set<Tuple> tuples = jedis.zrevrangeWithScores("sortedset1", 0, -1);
        tuples.stream().forEach(tuple -> logger.info("{}'s score is {}", tuple.getElement(), tuple.getScore()));
    }

    /**
     * ZREVRANGEBYLEX key max min [LIMIT offset count]
     * ZREVRANGEBYLEX 返回指定成员区间内的成员，按成员字典倒序排序, 分数必须相同
     *
     * @see this#zrangeByLex()
     */
    @Test
    public void zrevrangeByLex() {
        jedis.del("sortedset1");
        Map<String, Double> map = new HashMap<>();
        // 使用该命令的前提是所有成员的score值必须相同，否则返回的结果不准确
        map.put("a", 1D); map.put("b", 1D); map.put("c", 1D);
        map.put("d", 1D); map.put("e", 1D); map.put("f", 1D);
        map.put("g", 1D);
        jedis.zadd("sortedset1", map);

        // 获取所有成员的倒叙结果  [g, f, e, d, c, b, a]
        // 这里与zrevrange不同，起始值是大值
        Set<String> elements = jedis.zrevrangeByLex("sortedset1", "+", "-");
        logger.info("zrangeByLex(String, +，-) result is {}", elements);

        // 注意，参数顺序不能写反   []
        elements = jedis.zrevrangeByLex("sortedset1", "-", "+");
        logger.info("zrangeByLex(String, -, +) result is {}", elements);

        // 其他操作参考 this#zrangeByLex()
    }

    /**
     * ZREVRANGEBYSCORE key max min [WITHSCORES] [LIMIT offset count]
     * ZREVRANGEBYSCORE 返回有序集合中指定分数区间内的成员，分数由高到低排序
     *      "max" 和 "min"参数前可以加 "(" 符号作为开头表示小于, "(" 符号与成员之间不能有空格;
     *      可以使用 "+inf" 和 "-inf" 表示得分最大值和最小值;
     *      "max" 和 "min" 不能反, "max" 放后面 "min"放前面会导致返回结果为空;
     *      计算成员之间的成员数量不加 "(" 符号时,参数 "min" 和 "max" 的位置也计算在内;
     *      ZREVRANGEBYSCORE集合中按得分从高到底排序,所以"max"在前面,"min"在后面, ZRANGEBYSCORE集合中按得分从底到高排序,所以"min"在前面,"max"在后面。
     *
     * @see this#zrevrangeByLex()
     * @see this#zrangeByScore()
     */
    @Test
    public void zrevrangeByScore() {
        jedis.del("sortedset1");
        Map<String, Double> map = new HashMap<>();
        map.put("a", 0D); map.put("b", 1D); map.put("c", 2D);
        map.put("d", 3D); map.put("e", 4D); map.put("f", 5D);
        map.put("g", 6D);
        jedis.zadd("sortedset1", map);

        // [g, f, e, d, c, b, a]
        Set<String> elements = jedis.zrevrangeByScore("sortedset1", "+inf", "-inf");
        logger.info("zrevrangeByScore(String, \"+inf\", \"-inf\") result is {}", elements);

        // [f, e, d, c]
        elements = jedis.zrevrangeByScore("sortedset1", 5, 2);
        logger.info("zrevrangeByScore(String, 5, 2) result is {}", elements);

        // [e, d, c]
        elements = jedis.zrevrangeByScore("sortedset1", "(5", "2");
        logger.info("zrevrangeByScore(String, \"(5\", \"2\") result is {}", elements);

        // 其他操作参考 this#zrangeByScore()
    }

    /**
     * ZCARD key
     * 返回key的有序集元素个数
     * 返回值：key存在的时候，返回有序集的元素个数，否则返回0
     *
     * @see this#zcount()
     * @see this#zlexcount()
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
     *
     * @see this#zcard()
     * @see this#zlexcount()
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
        logger.info("zcount({}, \"(1\", \"3\") result is {}", sortedSetKey, count);

        count = jedis.zcount(sortedSetKey, "-inf", "+inf");
        logger.info("zcount({}, \"-inf\", \"+inf\") result is {}", sortedSetKey, count);
    }

    /**
     * ZLEXCOUNT key min max
     * 计算有序集合中指定成员之间的成员数量:
     *      1.成员名称前需要加 [ 符号作为开头, [ 符号与成员之间不能有空格;
     *      2.可以使用 - 和 + 表示得分最小值和最大值;
     *      3.min 和 max 不能反, max 放前面 min放后面会导致返回结果为0;
     *      4.计算成员之间的成员数量时,参数 min 和 max 的位置也计算在内;
     *      5.min 和 max 参数的含义与 zrangebylex 命令中所描述的相同;
     *
     * 返回值： 有序集合中成员名称 min 和 max 之间的成员数量
     *
     * @see this#zcard()
     * @see this#zcount()
     */
    @Test
    public void zlexcount() {
        jedis.del("sortedset1");
        Map<String, Double> map = new HashMap<>();
        map.put("a", 0D); map.put("b", 1D); map.put("c", 2D);
        map.put("d", 3D); map.put("e", 4D); map.put("f", 5D);
        map.put("g", 6D);
        jedis.zadd("sortedset1", map);

        // score值最小到最大之间的元素个数 7
        Long count = jedis.zlexcount("sortedset1", "-", "+");
        logger.info("zlexcount(String, -, +) result is {}", count);

        // score值最大到最小之间的元素个数 0  （顺序反了就没有元素）
        count = jedis.zlexcount("sortedset1", "+", "-");
        logger.info("zlexcount(String, -, +) result is {}", count);

        // 两个成员之间成员的数量，成员名称前需要加 [ 或 ( 符号作为开头, [ 或 ( 符号与成员之间不能有空格  2
        count = jedis.zlexcount("sortedset1", "(d", "[f");
        logger.info("zlexcount(String, \"(d\", \"[f\") result is {}", count);

        // 某个成员之前的成员数量 3
        count = jedis.zlexcount("sortedset1", "-", "[c");
        logger.info("zlexcount(String, \"-\", \"[c\") result is {}", count);

        // 某个成员之后的成员数量 5
        count = jedis.zlexcount("sortedset1", "[c", "+");
        logger.info("zlexcount(String, \"[c\", \"+\") result is {}", count);

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
     * ZSCORE key member
     * 返回有序集key中，成员member的score值
     * 如果member元素不是有序集key的成员，或key不存在，返回nil 但jedis会抛出异常
     *
     */
    @Test
    public void zscore() {
        jedis.del(sortedSetKey);
        Map<String, Double> map = new HashMap<>();
        map.put("a", 0D); map.put("d", 1D); map.put("b", 2D); map.put("c", 2D);
        jedis.zadd(sortedSetKey, map);

        double score = jedis.zscore(sortedSetKey, "d");
        logger.info("d's score is {}", score);

        // 不存在的key或成员会抛出异常
        score = jedis.zscore(sortedSetKey, "g");
        logger.info("g's score is {}", score);
    }

    /**
     * 返回有序集key中成员member的排名。
     * 其中有序集成员按score值递增(从小到大)顺序排列。排名以0为底，也就是说，score值最小的成员排名为0
     *
     * 返回值：
     *      如果member是有序集key的成员，返回integer-reply：member的排名。
     *      如果member不是有序集key的成员，返回bulk-string-reply: nil。
     *
     * @see this#zrevrank()
     * @see this#zremrangeByRank()
     */
    @Test
    public void zrank() {
        jedis.del("sortedset1");
        Map<String, Double> map = new HashMap<>();
        map.put("a", 0D); map.put("d", 1D); map.put("b", 2D); map.put("c", 2D);
        jedis.zadd("sortedset1", map);

        // 0
        Long num = jedis.zrank("sortedset1", "a");
        logger.info("zrank(String, a) result is {}", num);

        // zrank(String, b) result is 2, zrank(String, c) result is 3
        // 从运行结果来看，排名不会重复，即使score相同也不重复
        Long num1 = jedis.zrank("sortedset1", "b");
        Long num2 = jedis.zrank("sortedset1", "c");
        logger.info("zrank(String, b) result is {}, zrank(String, c) result is {}", num1, num2);
    }

    /**
     * ZREVRANK key member
     * 返回有序集key中成员member的排名，其中有序集成员按score值从大到小排列。
     * 排名以0为底，也就是说，score值最大的成员排名为0
     *
     * @see this#zrank()
     */
    @Test
    public void zrevrank() {
        jedis.del("sortedset1");
        Map<String, Double> map = new HashMap<>();
        map.put("a", 0D); map.put("d", 1D); map.put("b", 2D); map.put("c", 2D);
        jedis.zadd("sortedset1", map);

        // 3
        Long num = jedis.zrevrank("sortedset1", "a");
        logger.info("zrevrank(String, a) result is {}", num);

        // zrevrank(String, b) result is 1, zrevrank(String, c) result is 0
        // 从运行结果来看，排名不会重复，即使score相同也不重复
        Long num1 = jedis.zrevrank("sortedset1", "b");
        Long num2 = jedis.zrevrank("sortedset1", "c");
        logger.info("zrevrank(String, b) result is {}, zrevrank(String, c) result is {}", num1, num2);
    }

    /**
     * ZREM key member [member ...]
     * 返回的是从有序集合中删除的成员个数，不包括不存在的成员
     * 当key存在，但是其不是有序集合类型，就返回一个错误
     *
     * @see this#zremrangeByLex()
     * @see this#zremrangeByScore()
     * @see this#zremrangeByRank()
     */
    @Test
    public void zrem() {
        jedis.del(sortedSetKey);
        Map<String, Double> map = new HashMap<>();
        map.put("a", 0D); map.put("d", 1D); map.put("b", 2D); map.put("c", 2D);
        jedis.zadd(sortedSetKey, map);

        // 2 返回的是从有序集合中删除的成员个数，不包括不存在的成员
        Long num = jedis.zrem(sortedSetKey, "a", "b", "g");
        logger.info("zrem result is {}", num);
    }

    /**
     * ZREMRANGEBYLEX key min max
     * ZREMRANGEBYLEX 删除名称按字典由低到高排序成员之间所有成员。
     * 不要在成员分数不同的有序集合中使用此命令, 因为它是基于分数一致的有序集合设计的,如果使用,会导致删除的结果不正确
     *
     * @see this#zrem()
     * @see this#zrangeByLex()
     * @see this#zremrangeByScore()
     * @see this#zremrangeByRank()
     */
    @Test
    public void zremrangeByLex() {
        jedis.del("sortedset1");
        Map<String, Double> map = new HashMap<>();
        // 使用该命令的前提是所有成员的score值必须相同，否则返回的结果不准确
        map.put("a", 1D); map.put("b", 1D); map.put("c", 1D);
        map.put("d", 1D); map.put("e", 1D); map.put("f", 1D);
        map.put("g", 1D);
        jedis.zadd("sortedset1", map);

        // 2
        Long num = jedis.zremrangeByLex("sortedset1", "(c", "[e");
        // [a, b, c, f, g]
        Set<String> result = jedis.zrange("sortedset1", 0, -1);
        logger.info("remove num is {}, after zremrangeByLex(\"sortedset1\", \"(c\", \"[e\"): {}", num, result);
    }

    /**
     * ZREMRANGEBYSCORE key min max
     * 移除有序集key中，所有score值介于min和max之间(包括等于min或max)的成员。
     * 自版本2.1.6开始，score值等于min或max的成员也可以不包括在内
     *
     * @see this#zrem()
     * @see this#zremrangeByLex()
     * @see this#zrangeByScore()
     * @see this#zremrangeByRank()
     */
    @Test
    public void zremrangeByScore() {
        jedis.del("sortedset1");
        Map<String, Double> map = new HashMap<>();
        map.put("a", 0D); map.put("b", 1D); map.put("c", 2D);
        map.put("d", 3D); map.put("e", 4D); map.put("f", 5D);
        map.put("g", 6D);
        jedis.zadd("sortedset1", map);

        // 1
        Long num = jedis.zremrangeByScore("sortedset1", "(0", "1");
        // [a, c, d, e, f, g]
        Set<String> result = jedis.zrange("sortedset1", 0, -1);
        logger.info("remove num is {}, after zremrangeByLex(\"sortedset1\", \"(c\", \"[e\"): {}", num, result);

        // 2
        num = jedis.zremrangeByScore("sortedset1", "-inf", "2");
        // [d, e, f, g]
        result = jedis.zrange("sortedset1", 0, -1);
        logger.info("remove num is {}, after zremrangeByLex(\"sortedset1\", \"(c\", \"[e\"): {}", num, result);
    }

    /**
     * ZREMRANGEBYRANK key start stop
     * 移除有序集key中，指定排名(rank)区间内的所有成员。
     * 下标参数start和stop都以0为底，0处是分数最小的那个元素。这些索引也可是负数，表示位移从最高分处开始数。
     * 例如，-1是分数最高的元素，-2是分数第二高的
     *
     * @see this#zrank()
     * @see this#zrem()
     * @see this#zremrangeByLex()
     * @see this#zrangeByScore()
     */
    @Test
    public void zremrangeByRank() {
        jedis.del("sortedset1");
        Map<String, Double> map = new HashMap<>();
        map.put("a", 0D); map.put("d", 1D); map.put("b", 2D); map.put("c", 2D);
        jedis.zadd("sortedset1", map);

        // 2
        Long num = jedis.zremrangeByRank("sortedset1", 0, 1);
        // [b, c]
        Set<String> result = jedis.zrange("sortedset1", 0, -1);
        logger.info("zremrangeByRank(String, 0, 1) num is {}, result is {}", num, result);
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
        // 参数数量必须要与zunionstore中求并集的Sorted Set数量相同
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

    /**
     * ZINTERSTORE destination numkeys key [key ...] [WEIGHTS weight] [SUM|MIN|MAX]
     * 计算给定的numkeys个有序集合的交集，并且把结果放到destination中
     * 相关解释同{@link this#zunionstore()}相同
     *
     * @see this#zunionstore()
     */
    @Test
    public void zinterstore() {
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
        // 参数数量必须要与zinterstore中求交集的Sorted Set数量相同
        params.weightsByDouble(2, 3);
        params.aggregate(ZParams.Aggregate.SUM);

        Long num = jedis.zinterstore("sortedset3", params, "sortedset1", "sortedset2");
        logger.info("zinterstore result is {}", num);

        // one's score : 1*2 + 1*3 = 5
        // two's score : 2*2 + 2*3 = 10
        Set<Tuple> tuples = jedis.zrangeByScoreWithScores("sortedset3", "-inf", "+inf");
        tuples.stream().forEach(tuple -> logger.info("{}'s score is {}", tuple.getElement(), tuple.getScore()));
    }
}
