package com.flyingideal.redislearn.jedis;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.BinaryClient;

import java.util.List;

/**
 * @author yanchao
 * @date 2018/6/10 16:28
 */
public class ListTest extends BaseTest {

    private static final Logger logger = LoggerFactory.getLogger(ListTest.class);

    /**
     * 1. 一般意义上讲，列表就是有序元素的序列；
     * 2. Redis Lists用linked list实现的原因是：对于数据库系统来说，至关重要的特性是：能非常快的在很大的列表上添加元素。
     *      另一个重要因素是，正如你将要看到的：Redis lists能在常数时间取得常数长度；
     * 3. 一个列表最多可以包含 2^32 - 1 个元素 (4294967295, 每个列表超过40亿个元素)；
     *
     * 4. 如果快速访问集合元素很重要，建议使用可排序集合(sorted sets)；
     */
    @Test
    public void testLists() {
        String keyName = "list";
        // lpush : 向list的左边（头部）添加一个新的元素，返回值为添加元素过后，该lists中元素的数量
        // 可以一次插入多个元素，但要注意插入顺序，前边的元素先插入，后边的元素后插入，即对于lpush来说后边的元素插入后距离头部更近
        long count = jedis.lpush(keyName, "4", "5");
        logger.debug("lpush result = {}", count);

        // rpush : 向list的右边（尾部）添加一个新的元素，返回值为添加元素过后，该lists中元素的数量
        count = jedis.rpush(keyName, "6");
        logger.debug("rpush result = {}", count);

        // lrange : 从list中取出一定范围的元素，方法带有两个索引，一定范围的第一个和最后一个元素。
        // 这两个索引都可以为负来告知Redis从尾部开始计数，因此-1表示最后一个元素，-2表示list中的倒数第二个元素，以此类推
        List<String> lrangeResult = jedis.lrange(keyName, 0 , -1);
        logger.info("lrange result is {}", lrangeResult);

        // lpop : 从list左边（头部）删除元素并同时返回删除的值
        String lpop = jedis.lpop(keyName);
        logger.info("lpop result is {}", lpop);

        /**
         * blpop : lpop的阻塞版本，如果设置的超时时间为0表示list为空时无限期等待，否则如果队列为空，在指定的时间内未获取到元素则返回(nil)
         *         blpop可以指定从多个list中取元素，只要有一个list不为空列表，则这个方法就不会阻塞，其遍历顺序取决于list的定义顺序
         *         且blpop方法的返回值也比较特殊，是一个list（这个list理论上应该只有两个元素），第一个元素表示是list名称，表示是从哪一个list移除的元素，第二个元素表示被移除的元素
         * @see  <a href="http://www.redis.cn/commands/blpop.html"></a> 有关blpop的执行细节查看链接相关内容
         */
        /*for (String s : jedis.lrange(keyName, 0, -1)) {
            List<String> blpop = jedis.blpop(1, keyName);
            logger.info("blpop result is {}", blpop);
        }*/

        // rpop : 从list右边（尾部）删除元素并同时返回删除的值
        String rpop = jedis.rpop(keyName);
        logger.info("rpop result is {}", rpop);

        // brpop同样是rpop的阻塞版本

        /**
         * RPOPLPUSH source destination
         * @see this#list_rpoplpush()
         */

        // ltrim : list从左边截取指定长度，最后该list就变为了截取出来的这一小片段了
        String ltrim = jedis.ltrim(keyName, 0, 5);
        logger.info("ltrim result is {}", ltrim); // OK
        logger.info("after ltrim lists is {}", jedis.lrange(keyName, 0, -1));

        // llen
        long llen = jedis.llen(keyName);
        logger.info("lists length is {}", llen);

        /**
         * linsert : 把 value 插入存于 key 的列表中在基准值 pivot 的前面或后面
         * @see this#list_linsert()
         */

        /**
         * lpushx : 只有当 key 已经存在并且存着一个 list 的时候，在这个 key 下面的 list 的头部插入 value。 与 LPUSH 相反，当 key 不存在的时候不会进行任何操作
         */

        // lindex : 返回key对应列表里 index 索引存储的值。 下标是从0开始索引的;
        //          负数索引用于指定从列表尾部开始索引的元素。在这种方法下，-1 表示最后一个元素
        String first = jedis.lindex(keyName, 0);
        String last = jedis.lindex(keyName, -1);
        logger.info("first element is {}, last element is {}", first, last);

        /**
         * LREM key count value
         * @see this#list_lrem()
         */

        /**
         * 所有的元素被弹出之后， key 不复存在
         */
    }

    /**
     * RPOPLPUSH source destination
     *
     * 原子性地返回并移除存储在 source 的列表的最后一个元素（列表尾部元素）， 并把该元素放入存储在 destination 的列表的第一个元素位置（列表头部）。
     * 可用于 安全的队列/循环列表
     * 对应的阻塞版本 : BRPOPLPUSH source destination timeout  当source是空列表时阻塞
     * @see <a href="http://www.redis.cn/commands/rpoplpush.html"></a>
     * @see <a href="http://www.redis.cn/commands/brpoplpush.html"></a>
     */
    @Test
    public void list_rpoplpush() {
        jedis.rpush("list11", "1", "2", "3", "4");
        jedis.rpush("list12", "a", "b", "c", "d");
        jedis.rpoplpush("list11", "list12");
        logger.info("after rpoplpush, list11 is {}", jedis.lrange("list11",0, -1));
        logger.info("after rpoplpush, list12 is {}", jedis.lrange("list12",0, -1));
    }

    /**
     * LINSERT key BEFORE|AFTER pivot value : 把 value 插入存于 key 的列表中在基准值 pivot 的前面或后面。
     * 当 key 不存在时，这个list会被看作是空list，任何操作都不会发生。
     * 当 key 存在，但保存的不是一个list的时候，会返回error。
     */
    @Test
    public void list_linsert() {
        jedis.rpush("list21", "1", "2");
        //linsert方法返回值：经过插入操作后的list长度，或者当 pivot 值找不到的时候返回 -1
        jedis.linsert("list21", BinaryClient.LIST_POSITION.BEFORE, "2", "a");
        logger.info("after linsert a exists element, list21 is {}", jedis.lrange("list21", 0, -1));
        long num = jedis.linsert("list21", BinaryClient.LIST_POSITION.BEFORE, "5", "b");
        logger.info("after linsert not exists element, list21 is {}, num is {}", jedis.lrange("list21", 0, -1), num);
    }

    /**
     * LPUSHX key value : 只有当 key 已经存在并且存着一个 list 的时候，在这个 key 下面的 list 的头部插入 value。 与 LPUSH 相反，当 key 不存在的时候不会进行任何操作
     */
    @Test
    public void list_lpushx() {
        // 方法返回值 ： 在 push 操作后的 list 长度。
        jedis.lpushx("list", "123");
        logger.info("exists(list): {}, list is {}", jedis.exists("list"), jedis.lrange("list", 0, -1));
        jedis.lpushx("not_exist_list", "123");
        logger.info("exists(not_exist_list): {}, list is {}",
                jedis.exists("not_exist_list"), jedis.lrange("not_exist_list", 0, -1));
    }

    /**
     * LREM key count value
     * 从存于 key 的列表里移除前 count 次出现的值为 value 的元素。 这个 count 参数通过下面几种方式影响这个操作：
     *    count > 0: 从头往尾移除值为 value 的元素；
     *    count < 0: 从尾往头移除值为 value 的元素；
     *    count = 0: 移除所有值为 value 的元素。
     *
     * 如果list里没有存在key就会被当作空list处理，所以当 key 不存在的时候，这个命令会返回 0
     * 返回值： 被移除的元素个数
     */
    @Test
    public void list_lrem() {
        jedis.rpush("list31", "1","1","2","1","2","1");
        jedis.lrem("list31", 3, "1");
        logger.info("after lrem list31 is {}", jedis.lrange("list31", 0, -1));
    }
}
