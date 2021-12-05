一、Single Threaded Execution模式
所谓Single Threaded Execution模式，意即“以一个线程执行”，就像独木桥同一时间内只允许一个人同行一样，该模式用于设置限制，以确保同一时间只能让一个线程执行处理。

##### 简单示例
创建一个门类（Gate），并让三个人(User)不断的通过，并且门类(Gate)会记录最后一次通过的人的信息及总次数。

Gate类
````java
public class Gate {
    private String name;
    private String address;
    private int count;


    public synchronized void pass(String nane, String address) {
        this.count++;
        this.name = nane;
        this.address = address;
        check();
    }

    public synchronized String toString() {
        return String.format("count=%d  name=%s  address=%s", count, name, address);
    }

    private void check() {
        if (name.charAt(0) != address.charAt(0)) {
            System.out.println("---broken---" + toString());
        }
    }
}

````

User类
````java
public class User extends Thread{
    private final Gate gate;
    private final String name;
    private final String address;

    public User(Gate gate,String name,String address){
        this.gate = gate;
        this.name = name;
        this.address = address;
    }

    @Override
    public void run() {
        System.out.println("---begin---:"+name);
        while (true){
            gate.pass(name,address) ;
        }
    }
}
````
执行
````java
public class Client {
    public static void main(String[] args) {
        Gate gate = new Gate();
        new User(gate, "Alice", "Alaska").start();
        new User(gate, "Bobby", "Brazil").start();
        new User(gate, "Chris", "Canada").start();
    }
}
````
角色分析

ShareResource(共享资源)
在此模式中需要一个发挥ShareResource作用的类，在示例中Gate扮演此角色，此角色可以被多个线程访问，包含多个方法,对于线程不安全的方法需声明为synchronized方法来保护。

##### 何时使用

在多线程环境下，ShareResource角色的状态有可能被改变，可能会发生线程安全性问题时使用

##### 存在的风险
死锁的产生
假如Alice和Bobby一起吃大盘子中意大利面，而盘子中间只有一把勺子和叉子，但是要想吃意大利面，勺子和叉子又缺一不可。仅有的叉子被Alice拿走了，而仅有的一把叉子被Bobby拿走了，于是...
* 拿勺子的Alice一直等着Bobby放下叉子
* 那叉子的Bobby一直等着Alice放下勺子

像这样多个线程僵持下去，无法运行的状态就称为死锁。

此模式用可能会发生死锁的风险，满足以下问题时，死锁就会发生。
1. 存在多个ShareResource角色
2. 线程在持有者某个ShareResource角色的锁的同时，还想获取其他ShareResource角色的锁
3. 获取ShareResource角色的锁的顺序并不固定（角色是对称的）

只要破坏1、2、3中的一个条件就可以防止死锁发生。

##### 对性能的影响
1. 获取锁花费时间，进入synchronized方法是需要获取锁，此处理需要花费时间。可以适当减少ShareResource角色的数量，那获取锁的数量也会相应的减少，从而能够抑制性能的下降。
2. 线程阻塞，当一个线程进入临界区内处理时，其他想要进入临界区的线程必须等待。可以尽量减少临界区范围的大小，降低程序阻塞的概率。
