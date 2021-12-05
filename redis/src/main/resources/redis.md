# redis-learning
## redis的数据类型
### 字符串
字符串类型的值实际上可以是字符串（简单的或复杂的字符串）、 数字（整数、浮点数）、甚至是二进制（图片、音频、视屏），但最大值不能超过512MB。

#### 命令简介

 1. 设置值 set key value [ex seconds] [px millisecondes] [nx] [xx]，时间复杂度O(1)

     set的几个选项：
      * ex seconds：为键设置秒级过期时间，同命令setex key seconds value
      * px millisecondes：为键设置毫秒级过期时间
      * nx：键必须不存在才能设置成功，用于添加,同命令setnx key value
      * xx：与nx相反，键必须存在才能设置成功，用于更新

       由于redis的单线程命令处理机制，如果有多个客户端同时执行setnx key value，根据setnx的特性只有一个客户端能设置成功，setnx可以作为分布式锁的一种实现方案。详见 https://redis.io/topics/distlock

2. 获取值 get key，时间复杂度O(1)
3. 批量设置值 mset key value [key value ...]，时间复杂度O(n)
4. 批量获取值 mget key [key ...]，时间复杂度O(n)
5. 自增 incr key ，自减decr key，自增指定数字 incrby key increment，自减指定数字 decrby key incrment，自增浮点数incrbyfloat key increment，时间复杂度都为O(1)

 incr命令用于对值做自增操作，返回结果分为三种情况：
 * 值不是整数，返回错误
 * 值是整数，返回自增后的结果
 * 键不存在，按照值为0自增，返回值为1

6. 追加值 append key value，时间复杂度O(1)
7. 字符串长度 strlen key (每个中文占3个字节)，时间复杂度O(1)
8. 设置并返回原值 getset key values
9. 设置指定位置的字符 setrange key offset value (从0开始)， 时间复杂度O(1)
10. 获取部分字符串 getrange key start end (从0开始，包含开始和结束位置)，时间复杂度O(n)

#### 字符串的三种内部编码
* int：8个字节的长整型
* embstr：小于等于39个字节的字符串
* raw：大于39个字节的字符串

使用object encoding key查看内部编码类型
#### 使用场景
1. 缓存功能：用户信息等业务数据
2. 计数
3. 共享session
4. 限速：限制验证码发送频率

### 哈希
在redis中，哈希类型是指键值本身又是一个键值对结构，形如value={{field1,value1},...{fieldN,valueN}}。
#### 命令简介
1. 获取值 hget field，键不存在返回nil，时间复杂度O(1)
2. 删除field hdel key field [field....]，结果为成功删除field的个数，时间复杂度O(n)
3. 计算field个数 hlen key，时间复杂度O(1)
4. 批量获取值field-value  hmget key field [field...]，时间复杂度O(n)
5. 批量设置field-value  hmget field value [field value ...]，时间复杂度O(n)
6. 判断field是否存在 hexists key field，时间复杂度O(1)
7. 获取所有field hkeys key，时间复杂度O(n)
8. 获取所有value hvals key，时间复杂度O(n)
9. 获取所有field-value hgetall key ，元素较多时会阻塞，可以使用hmget或hscan代替，时间复杂度O(n)
10. 自增 hincrby key field，hincrbyfloat key field，时间复杂度O(1)
11. 获取值的长度 hstrlen key field，时间复杂度O(1)

#### 哈希类型的两种内部编码
* 压缩列表（ziplist）：当哈希类型元素个数小于hash-max-ziplist-entries配置（默认512），__同时__ 所有值都小于hash-max-ziplist-values配置（默认64字节）,redis会使用ziplist作为哈希的内部实现。ziplist使用更加紧凑的结构实现多个元素的连续存储，所以比hashtable更加节省内存。
* hashtable(哈希表)：当哈希类型无法满足ziplist的条件时，redis会使用hashtable作为哈希的内部实现，因为此时ziplist的读写效率会下降，而hashtable的读写复杂度为O(1)。


#### 使用场景
1. 缓存数据：缓存多对信息时（如用户数据）比字符串更直观且更新更方便


### 列表
列表类型是用来存储多个有序的字符串，它可以充当栈和队列的角色，一个列表最多存储 2^(32)-1个元素。
#### 命令简介
 添加操作
 1. 从右边插入元素 rpush key value [value ...]，时间复杂度O(n)
 2. 从左边插入元素 lrush key value [value ...]，时间复杂度O(n)
 3. 向某个元素前或后插入元素 linsert key before|after| pivot value ，时间复杂度O(n),n是pivot距离列表投或尾的距离


查找操作
1. 获取去指定范围内的元素 lrange key start end，时间复杂度O(s+n)，s是start偏移量，n是start到end的范围。索引下标的两个特点：
   * 索引下标从左至右分别shiver0到N-1，但是从右至左分别为-1到-N。
   * lrange中的end选项包含自身。
2. 获取列表指定索引下标的元素 lindex key index，时间复杂度O(n)
3. 获取列表长度 llen key，时间复杂度O(1)

删除操作
1. 从列表左侧弹出元素lpop key，时间复杂度O(1)
2. 从列表右侧弹出元素rpop key，时间复杂度O(1)
3. 删除指定元素 lrem key count value,，时间复杂度O(n),lrem命令会从列表中找到等于value的元素进行删除，根据count的不同分为三种情况：
   * count > 0,从左向右，删除最多count个元素
   * count < 0,从左到右，删除最多个count个元素
   * count = 0，删除所有
4. 按照索引范围修剪列表 ltrim key start end ，时间复杂度O(n)

修改操作 修改指定索引下标的元素 lset key index newvalue，时间复杂度O(n)

阻塞操作
1. blpop key [key ...] timeout，时间复杂度O(1)
2. brpop key [key ...] timeout，时间复杂度O(1)
   * 如果是多个键，那么brpop会从从至右遍历键，一旦一个键能弹出元素，客户端立即返回
   * 如果多个客户端对同一个键执行brpop,那么最先执行brpop命令的客户端可以获取弹出的值


#### 列表的两种编码类型
* 压缩列表（ziplist）：当哈了；列表元素个数小于hash-max-ziplist-entries配置（默认512），__同时__ 所有值都小于hash-max-ziplist-values配置（默认64字节）,redis会使用ziplist作为列表的内部实现。
* linkedlist（链表）：当列表类型无法满足ziplist条件时，redis会使用linkedlist作为列表的内部实现。

#### 使用场景
1. 消息队列：使用lpush和brpop命令组合可实现阻塞队列
     * lpush + lpop = Stack(栈)
     * lpush + rpop = Queue(队列)
     * lpush + ltrim = Capped Collection（有限集合）
     * lpush + brpop = Message Queue (消息队列)

2. 文章列表


### 集合
集合中不能存储重复元素，并且元素是无序的,不能通过索引下标获取元素。一个集合最多包含2^(32)-1个元素
#### 命令简介
集合内操作
1. 添加元素 sadd key element [element ...]，时间复杂度O(n)
2. 删除元素 srem key element [element ...]，时间复杂度O(n)
3. 计算元素个数 scard key，时间复杂度O(1)
4. 判断元素是否在集合中 sismember key element，时间复杂度O(1)
5. 随机从集合中返回指定个数元素 srandmember key [count]，count默认为1，时间复杂度O(n)
6. 从集合随机弹出元素 spop key [count],count默认为1，时间复杂度O(1)
7. 获取所有元素 smember key,元素较多会阻塞，可以使用sscan命令，时间复杂度O(n)


集合间操作
1. 求多个集合的交集 sinter key [key ...]，时间复杂度O(m*k),k是多个集合中元素最少的个数，m是键的个数
2. 求多个集合的并集 suinon key [key ...],，时间复杂度O(n)，n是多个集合元素个数和
3. 求多个集合的差集 sdiff  key [key ...]，时间复杂度O(n)，n是多个集合元素个数和
4. 将交集、并集、差集的结果保存
   * sinterstore destination key [key ...]
   * suinonstore destination key [key ...]
   * sdiffstore  destination key [key ...]

集合类型两种内部编码
* intset(整数集合)：当集合中的元素都是整数且元素的个数小于set-max-intset-entries配置（默认512），redis会使用intset作为集合的内部实现，从而减少内存使用。
* hashtable(哈希表)：当集合类型无法满足intset的条件时，redis会使用hashtable作为集合的内部实现。

### 有序集合
有序集合不能包含重复元素但元素之间可以有序。它给每个元素设置一个分数（score）（score值可以重复）来实现排序。
#### 命令简介
 集合内
1. 添加成员 zadd key score member [score member ...]，关于zadd命令有两点需要注意：
  1. redis 3.2为add命令添加了nx、xx、ch、incr四个选项
     * nx：member必须不存在，才可以设置成功，用于添加
     * xx：member必须存在，才可以设置成功，用于更新
     * ch：返回此次操作后，有序集合元素和分数发生变化的个数
     * incr：对score做增加，相当于后面介绍的zincrby
   2. 有序集合相比集合添加了排序字段，但也产生了代价，zadd的时间复杂度为O(log(n)),而sadd的时间复杂度为O(1)
2. 计算成员个数 zcard key
3. 计算某个成员的分数 zscore key member
4. 计算成员的排名 zrank key member(从低到高)，zrevrank key member (从高到低)，排名从0开始
5. 删除成员 zrem key member [member ...]
6. 增加成员的分数 zincrby key  increment member
7. 返回指定排名范围的成员
    * zrange key strat end [withscores] (由低到高),withscores选项会返回成员的分数
    * zrevrange key start end [withscores] (由高到低)
8. 返回指定分数范围的成员
   *  zrangebysccore key min max [withscores] [limit offset count] (按分数从低到高)，withscores选项会返回成员的分数，[limit offset count]选项可以限制输出的起始位置和个数，同时min和max还支持开区间 和闭区间，-inf和+inf分别代表无限小和无限大
   *  zrevrangebysccore key min max [withscores] [limit offset count] (按分数从高到低)
9. 返回指定分数范围成员个数 zcount key min max
10. 删除指定排名内的升序元素 zremrangebyrank key start end
11. 删除指定分数范围的成员 zremrangestore key min max

集合间操作
1. 交集 zinterstore destination numkeys key [key ...] [weights weight [weight ...]] [aggregate sum|mix|max]
  * destination：交集计算结果保存到这键
  * munkeys：需要做交集计算的个数
  * key [key ...]：需要做交集计算的键
  * weights weight [weight ...]：每个键的权重，在做交集计算时，每个键中的每个member会将自己分数乘以这个权重，每个键的权重默认是1
  * aggregate sum|mix|max：计算成员交集后，分值可以按照sum、min、max做汇总 默认值是sum
2. 并集
zunionstore destination numkeys key [key ...] [weights weight [weight ...]] [aggregate sum|mix|max]

#### 有序集合类型两种内部编码
* 压缩列表（ziplist）：当哈有序集合元素个数小于hash-max-ziplist-entries配置（默认512），__同时__ 所有值都小于hash-max-ziplist-values配置（默认64字节）,redis会使用ziplist作为有序集合的内部实现。
* skiplist(跳跃表)：当列有序集合无法满足ziplist条件时，redis会使用linkedlist作为有序集合的内部实现。

#### 使用场景
1. 添加用户点赞数 zadd+zincrby
2. 取消用户点赞数 zrem
3. 展示获取赞数最多的十个用户 zrevrange
4. 展示用户信息及用户分数

## 键管理
单个键管理
1. 键重命名 rename key newkwy,如果newkey已经存在，值则会覆盖，避免被覆盖则用renamenx命令，使用时需注意：
   * 由于重命名键期间会执行del命令删除旧的键，如果对应的值比较大，会存在阻塞的可能
   * 如果rename和renamenx中的key和newkey是相同的，3.2版本后会报错，之前版本会返回OK

2. 随机返回一个键 randomkey
3. 键过期
   * expire key seconds 键在seconds秒后过期
   * expire key timestamp 键在秒级时间戳timestamp后过期
   * pexpire key millisecondes：键在milliseconds毫秒后过期
   * pexpire key millisecondes-timestamp 键在毫秒级时间戳timestamp后过期
     * 如果expire key 的键不存在，返回结果为0
     * 如果过期时间为负值，键会被立即删除
     * persist命令可以将键的过期时间清除
     * 对于字符串类型键，执行set命令会去掉过期时间
     * redis不支持二级数据结构（如哈希、列表）内部元素的过期功能
     * setex命令作为set + expire 的组合，可减少一次网络通讯且为原子执行

   * 查看键的剩余过期时间 ttl（秒）和pttl（毫秒），有三种返回结果
      * 大于等于0的整数：键剩余的过期时间
      * -1：键没有设置过期时间
      * -2：键不存在
4. 键迁移
   * move key db 在redis内部把指定的键从源数据库移动到目标数据库，原子操作
   * dump + restore 在不同的redis实例之间进行数据迁移，迁移过程分两步：
      1. 在源redis上，dump命令会将键值序列化，格式采用的是RDB格式
      2. 在目标redis上，restore命令将上面序列化的值进行复原，其中ttl参数代表过期时间，如果ttl=0代表没有过期时间
         * 整个迁移过程非原子操作，需开启两个客户端连接。

   * migrate host port key|"" destination-db timeout [copy] [replace] [keys key [key ...]],参数说明：
      * host：目标redis的IP
      * port：目标redis的端口
      * key|""：key迁移一个键，""迁移多个键
      * destination-db：目标redis的数据索引
      * timeout：迁移的超时时间
      * copy：如果添加此项，迁移后并不删除源键
      * replace：如果添加此项，migrate不管目标redis是否存在该键都会正常迁移进行数据覆盖
      * key key [key ...]：迁移多个键
         * 整个过程是原子操作
         * 目标redis完成restore后会发送OK给源redis，源redis接受后会根据migrate对应的选项来决定是否在源redis上删除对应的键

5. 遍历键
  * 全量遍历键 key pattern，键很多时会阻塞
    * \*代表匹配任意字符
    * ？代表匹配一个字符
    * []代表匹配部分字符，例如[1,3]代表比配1,3，[1-10]代表匹配1-10代表1到10的任意数字
    * \x 用来做转义，例如要匹配星号
  * 渐进式遍历 scan cursor [match] [count number],时间复杂度为O(1),除了scan以外，redis还提供了面向哈希类型(hscan)、集合类型(sscan)、有序集合(zscan)的扫描命令，解决了如hgetall、smember 、zrange可能产生的阻塞问题。参数说明：
     * cursor 必须参数，实际上cursor是一个游标，第一次遍历从0开始，每次scan遍历玩都会返回当前的游标值，知道游标值为0，表示遍历结束。
     * match pattern：做模式匹配
     * count pattern：表明每次要遍历的键个数，默认值为10
       * scan并非完美，如果在scan的过程中如果有键的变化（增加、删除、修改），那么遍历效果可能会碰到新增的键可能没有遍历、遍历出现了重复的键情况。

6. 数据库管理
   * 切换数据库 select dbindex
   * 清除数据库 flushdb：清除当前数据库,flushall：清除所有的数据库

### 慢查询分析
redis使用一个列表存储慢查询日志，慢查询统计的时间为命令执行的时间，不包括网络和命令排队的时间。

#### 慢查询的两个配置参数
* slowlog-log-slower-than：预设阈值，默认10000微妙，等于0时会记录所有命令，小于0时不会记录任何命令。
* slowlog-max-len 存储的最大条数。慢查询日志列表已处于最大长度时，此时再插入新值，则最早插入的那条记录将被从列表中删除。

#### 命令
* slowlog get [n] 获取慢查询日志 n为条数。
返回内容包含四个字段
  * 日志标识ID
  * 发生时间戳
  * 命令耗时
  * 执行的命令和参数
*  slowlog len  获取慢查询日志列表的长度
*  slowlog reset 慢查询日志重置

## Redis Shell

#### redis-cli
 参数说明：
 1. -r 代表将命令执行几次，例如 redis-cli -r 3 ping 表示将会执行三次
 2. -i 代表每隔几秒执行一次命令，但-i选项必须和-r选项一起才能使用，单位是秒，不支持毫秒。例如 redis-cli -r 5 -i 1 ping 表示每隔一秒执行一次，一共执行5次
 3. -x 代表从标准读取数据作为redis-cli的最后一个参数，例如 echo "world" | redis-cli -x set hello
 4. -c 连接redis cluster节点时需要使用，-c选项可以防止 moved 和 ask 异常
 5. -a 配置密码
 6. --scan和--pattern 用于扫描指定模式的键，相当于scan
 7. --slave 把当前客户端模拟成当前redis节点的从节点。
 8. --rdb 请求redis实例生成并发送RDB持久化文件，保存到本地，可以用此选项做定时备份。
 9. --pip 用于将命令封装成redis通讯协议定义的数据格式，批量发送给redis执行。
 10. --bigkeys 使用scan命令对redis的键进行采样，从中找到内存占用比较大的键值。
 11. --eval 用于执行指定lua脚本
 12. --latency 用于检测网络延迟，有三个选项 --latency（用于测试客户端到目标redis的网络延迟）、--latency-history（执行结果只有一条）、--latency-dist（使用统计图表的形式从控制台输出）
 13. --stat 实时获取redis的重要统计信息
 14. --raw和--no-raw  --raw返回格式化的结果，-no-raw返回原始格式的结果

#### redis-server
1. 启动redis
2. --test-memoey 检测当前操作系统能否稳定的分配指定容量的内存给redis

###  redis-benchmark
1. -c(clitens) 代表客户端的并发量，默认50
2. -n(num) 代表客户端请求总量，默认100000
3. -q 显示requests per second信息
4. -P 代表每个 请求pipeline的数量量，默认1
5. -k 代表客户端是否使用keepalive，1位使用，0位不使用，默认1

## 客户端
客户端的通信协议（RESP），格式为：
````
*<参数数量> CRLF
$<参数1的字节数量>CRLF
<参数1>
...
$<参数n的字节数量>CRLF
<参数n>
````
返回结果的五种格式为：
  1. 状态回复：在RESP中第一个字节为"+"
  2. 错误回复：在RESP中第一个字节为"-"
  3. 整数回复：在RESP中第一个字节为":"
  4. 字符串回复：在RESP中第一个字节为"$"
  5. 多条字符串回复：在RESP中第一个字节为"\*"

#### 客户端管理
客户端API

* client list 列出与redis服务端连接的所有客户端的连接信息。
 1. id 客户端连接的唯一标识，id随着redis的连接自增，重启会重置为0.
 2. addr 客户端连接的IP和端口。
 3. fd scoket的文件描述符，与lsof命令结果中的fd是同一个，如果fd=-1代表此客户端不是外部客户端，而是redis内部的伪装客户端。
 4. name 客户端的名称
 5. 输入缓存区
    * redis为每个客户端分配了输入缓存区，将客户端发送的命令临时缓存起来，同时redis会从输入缓存区中拉去命令执行。
    * qbuf 输入缓存区的总容量，qbuf-free 输入缓存区剩余容量。
    * redis没有提供配置来修改每个缓存区的大小，此大小会根据输入内容的大小动态调整，但要求每个缓存区大小不能超过1G，超过后客户端会被关闭，__另外输入缓存区不受maxmemory控制__，假设一个redis实例设置了maxmemory为4G，已经存储了2G数据，但是如果此时输入缓存区使用了3G，已经超过maxmemory限制，可能产生数据丢失、键值淘汰、OOM等情况。

      造成输入缓存区过大的原因：
       1. redis的处理速度跟不上输入缓存区的输入熟速度，并且每次进入输入缓存区的命令包含大量bigkey、从而造成输入缓存区过大
       2. redis发生了阻塞，短期内不能处理命令，造成输入缓存区命令积压在了输入缓存区

      如何发现与监控：
      1. 通过定期执行client list 命令，收集qbuf和qbuf-free找到异常的连接记录
      2. 通过info命令的info clients模块，找到最大的输入缓存区（client_biggest_input_buf），可以设置阈值进行报警

  监控输入缓存区异常的两种方式

| 命令         | 优点                                  | 缺点                                  |
| ------------ | ------------------------------------- | ------------------------------------- |
| client list  | 能精准分析每个客户端来定位问题        | 执行速度较慢频繁执行有阻塞redis的可能 |
| info clients | 执行速度比client list快，分析过程简单 | 不能精准定位到客户端，不能显示所有输入缓存的区的总量，只能显示最大量                                      |


  6. 输出缓存区:obl、oll、omem， 保存命令执行的结果返回给客户端,__不受maxmemory限制__，使用不当会造成数据丢失、键值淘汰、OOM等情况。

  输出缓存区的容量可以通过参数client-output-buffer-limit进行设置，根据客户端的不同分为三种

   * 普通客户端
   * 发布订阅客户端
   * slave客户端

 对应的配置规则：
````
client-output-buffer-limit <class> <hard limit> <soft limt> <soft seconds>
````
   * class:客户端类型，分为三种。normal:普通客户端、slave:slave客户端、pubsub:发布订阅客户端
   * hard limit: 如果客户端使用的输出缓存区大于<hard limit>,客户端会被立即关闭。
   * soft limit 和 soft seconds:如果客户端使用的输出缓存区超过了soft limit并且持续了soft seconds秒，客户端会被立即关闭。

  redis的默认配置
   * client-output-buffer-limit normal 0 0 0
   * client-output-buffer-limit slave 256mb 64mb 60
   * client-output-buffer-limit pubsub 32mb 8mb 60

      输出缓存区由两部分组成：固定缓存区（16KB）和动态缓存区。固定缓存区存满后，新的返回结果会存入动态缓存区队列中。固定缓存区返回比较小的执行结果，使用字节数组实现。动态缓存区返回比较大的执行结果，使用列表实现。

       client list 中的obl固定缓存区字节数组的长度，oll代表动态缓存区的列表的长度，omem代表使用的字节总数。

     监控输入缓存区的方法：
      1. 通过定期执行client list 命令，收集obl、oll、omem找到异常并分析
      2. 通过info命令的info clients模块，找到输出缓存区列表最大的对象（client_longest_output_list），可以设置阈值进行报警

     如何防范：
      1. 进行上述监控，设置阈值，超过阈值及时处理
      2. 限制普通客户端输出缓存区<hard limit> <soft limt> <soft seconds>
      3. 适当增大slave的输出缓存区<hard limit> <soft limt> <soft seconds>，如果master节点写入较大，slave客户端的输出缓存区可能会比较大，一旦slave客户端连接因为输出缓存区溢出被kill，会造成复制重连。
      4. 限制容易让输出缓存区增大的命令，如monitor命令
      5. 及时监控内存，一旦发现内存抖动频繁，可能使输出缓存区过大


   7. 客户端的存活状态 age代表客户端已经连接的时间，idle最后一次的空闲时间。
   8. 客户端的限制maxclients和timeout,maxclients参数限制最大客户端连接数，一旦连接超过maxclients，新的连接将被拒绝，默认值为10000. timeout设置超时时间,默认为0，如果客户端连接的idle时间超过timeout，连接将会被关闭。
   9. 客户端类型 flag用于标识当前客户端的类型

| 序号 | 客户端类型 | 说明                                   |
| ---- | ---------- | -------------------------------------- |
| 1    | N          | 普通客户端                             |
| 2    | M          | 当前客户端是master节点                 |
| 3    | S          | 当前客户端是slave节点                  |
| 4    | O          | 当前客户端正在执行monitor              |
| 5    | x          | 当前客户端正在执行事务                 |
| 6    | b          | 当前客户端正在等待阻塞事件             |
| 7    | i          | 当前客户端正在等待阻塞VM I/O，已废弃   |
| 8    | d          | 一个受监视的键已被修改，EXEC命令将失败  |
| 9    | u          | 客户端未被阻塞                       |
| 10   | c          | 回复完整输出后，关闭连接               |
| 11   | A          | 尽可能快的关闭连接                    |

* client setName 设置客户端名称 和 client getName 获取客户端名称
* client kill ip:port 用于杀掉指定IP和端口的客户端
* client pause timeout（毫秒） 用于阻塞redis客户端timeout毫秒，在此期间，redis客户端连接将被阻塞。
* monitor 用于监控redis正在执行的命令

## 持久化
 ### RDB
 rdb持久化的把当前进程数据生成快照保存到硬盘的过程，触发rdb持久化过程分为手动触发和自动触发

 触发机制：
  * save命令：阻塞当前reds服务器，直到rdb过程完成为止。
  * bgsave命令：redis进程执行fork操作创建子进程，rdb持久化过程由子进程完成。阻塞只会发生在fork阶段。

自动触发机制：
* 使用save相关配配置
* 如果从节点执行全量复制操作，主节点自动执行bgsave生成rdb文件并发送给从节点
* 执行debug reload命令重新加载redis时也会自动触发save操作
* 默认执行shutdown命令时，如果没有开启AOF持久化则自从执行bgsave

RDB文件处理
* 保存：RDB文件保存在dir配置指定的目录下，文件名通过dbfilename配置指定，可通过config命令运行期动态修改
* 压缩：redis默认采用LZF算法对生成的RDB文件进行压缩，默认开启，可通过 config set rdbcompression {yes|no}动态修改

RDB优缺点：

  优点：
   * RBD是一个紧凑压缩的二进制文件，代表redis在某个时间点上的数据快照。
   * redis加载RDB恢复数据远远快于AOF的方式

  缺点：
  * RDB方式没办法做到实时持久/秒级持久化
  * RDB文件使用特定的二进制格式保存，redis在演进过程中有对个RDB版本，存在老版本redis不兼容新版RDB格式的问题。

### AOF
 以独立日志的方式记录每次写命令，重启时重新再加载执行AOF中的命令达到恢复数据的目的。开启AOF需配置 appendonly yes,默认不开启，文件名通过appendfilename进行配置，默认为appendonly.aof，保存目录和RDB一致，通过dir配置。

 文件同步策略 由参数appendfsync控制

| 可配置值 | 说明                                                                                             |
| -------- | ------------------------------------------------------------------------------------------------ |
| always   | 命令写入aof_buf后调用系统fsync操作同步到AOF文件，fsync文成后线程返回                             |
| everysec | 命令写入aof_buf后系统调用write操作，write完成后线程返回。fsync同步文件操作有专门线程每秒调用一次 |
|  no      | 命令写入aof_buf后系统调用write操作，不对AOF文件做fsync同步，同步硬盘操作由操作系统负责，通常同步周期最长30秒      |

重写机制
* 手动触发：直接调用bgrewriteraof命令
* 自动触发：根据auto-aof-rewrite-min-size和auto-aof-rewriter-percentage参数确实自动触发机制
    * auto-aof-rewriter-min-size表示运行AOF重写时文件最小体积，默认为64M
    * auto-aof-rewriter-percentage代表当前AOF文件空间（aof_current_size）和上次重写后AOF文件空间（aof_base_size）的比值
    * 自动触发机制=aof_current_size > auto-aof-rewite-min-size && (aof_current_size - aof_base_size) / aof_base_size >= auto-aof-rewriter-percentage



## 哨兵
redis Sentinel 具有以下功能：
* 监控：Sentinel节点会定期检查redis数据节点、其余sentinel是否可达
* 通知：sentinel 节点会将故障转移的结果通知给应用方。
* 主节点故障转移：实现从节点晋升为主节点并维护后续正确的朱主从关系
* 配置提供者：在redis sentinel结构中，客户端在初始化时连接的是sentinel节点集合，从中获取主节点信息。

sentinel节点本身就是独立的redis节点，只不过他们有些特殊，他们不存储数据，只支持部分命令。

### 实现原理
#### 三个定时监控任务
1.  每个10秒，每个Sentinel节点会向主节点和从节点发送info命令获取最新的拓扑结构。
    这个定时任务的作用具体表现在三个方面：
    * 通过向主节点执行info命令，获取从节点的信息，这也是为什么sentinel节点不需要显式配置监控从节点。
    * 当有新的从节点加入时可以立刻感知出来。
    * 节点不可达或者故障转以后，可以通过info命令实时更新节点拓扑信息。
2. 每隔2秒，每个sentinel节点会向redis数据节点的_sentinel_:hello频道发送sentinel节点对于主节点的判断以及当前sentinel节点的信息，同时每个sentinel节点也会订阅这个频道来了解其他sentinel节点对主节点的判断。所有这个节点完成以下两个任务：
   * 发现新的sentinel节点：通过订阅节点的_sentinel_:hello了解其他sentinel节点信息，如果是新加入的sentinel节点，将该节点信息保存起来，并与改sentinel节点创建连接。
   * sentinel节点之间交换主节点的状态，最为后面客观下线以及领导者选举的依据。
3. 每隔1秒每隔sentinel节点会向主节点、从节点、其余sentinel节点发送一条平命令做一次心跳监测，来确定这些节点当前是否可达。

#### 主观下线和客观下下线
* 主观下线 ：每个sentinel节点会每隔秒对主节点、从节点。其他sentinel几点发送ping命令做心跳检查，当这些节点超过down-after-millisecodes没有进行有效回复，sentinel节点就会对该节点做失败判定，这个行为就叫主观下线。（主观下线是当前sentinel节点的一家之言，存在捂误判的可能）。
* 客观下线：当sentinel主观下线节点为主节点时，改sentinel节点会通过sentinel is-master-down-by-addr命令向其他sentinel节点询问对主节点的判断，当超过quorum 个数，sentinel节点认为主节点确实有问题，这时该sentinel节点会做出客观下线的决定。
* 命令使用简介：sentinel is-master-down-by-addr ip port current_epoch runid
   1. ip:主节点IP
   2. port：主节点端口
   3. current_epoch：当前配置纪元
   4. runid: 此参数有两种类型：1.当runid等于*时，作用是sentinel节点直接交换对主节点下线的判断。2.当runid等于当前sentinel节点的runid时，作用时当前sentinel节点希望目标sentinel节点同意自己成为领导者。

  返回结果：
  1. down_state:目标sentinel节点对于主节点的下线判断，1是下线，0是在线
  2. leader_runid：当leader_runid等于*时，代表返回结果是用来做主节点是否可达，当leader_runid等于具体烦人runid，代表目标节点同意runid成为领导者。
  3. leader_epoch:领导者纪元。

#### 领导者选举
redis使用Raft算法实现领导者选举，大致思路如下：
1. 每个在线的sentinel都有资格成为领导者，当他确认主节点主观下线的时候，会向其他sentinel节点发送sentinel is-master-down-by-addr命令，要求将自己设置为主节点。
2. 收到命令的sentinel节点，如果没用统一过其他sentinel节点的sentinel is-master-down-by-addr命令，将同意请求，否则拒绝。
3. 如果该sentinel节点发现自己的票数已经大于max(quorum,num(sentinels)/2 + 1),那么他将成为领导者。
4. 如果此过程没用选举出领导者，将进入下一次选举。

#### 故障转移
1. 在从节点列表中选出一个作为新的主节点，方法如下：
   * 过滤：“不健康”（主观下线、断线）、5秒内没有回复过sentinel节点ping相应、与主节点失联超过down-after-millisecondes \*10秒。
   * 选择slave-priority（从节点优先级）最高的从节点列表，如果存在则返回，不存在则继续。
   * 选择估值偏移量最大的从节点（复制的最完整），如果存在则返回。不存在则返回。
   * 选择runid最小的从节点。
2. sentinel领导者会对第一步选出来的从节点执行slaveof no one 命令让其成为主节点。
3. sentinel领导者节点会向剩余的从节点发送命令，让他们成为新主节点的从节点，复制规则和parallel-syncs参数有关。
