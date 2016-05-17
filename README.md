# LqLThreadPoolTest
android 异步任务之线程池的使用
Android 性能优化之使用线程池处理异步任务


    在加载大量数据的时候，经常会用到异步加载，所谓异步加载，就是把耗时的工作放到子线程里执行，当数据加载完毕的时候再到主线程进行UI刷新。在数据量非常大的情况下，我们通常会使用

两种技术来进行异步加载，一是通过AsyncTask来实现，另一种方式则是通过ThreadPool来实现。
    创建一个线程并执行，它在任务结束后GC会自动回收该线程，一切看起来如此美妙，是的，它在线程并发不多的程序中确实不错，而假如这个程序有很多地方需要开启大量线程来处理任务，那么

如果还是用上述的方式去创建线程处理的话，那么将导致系统的性能表现的非常糟糕。
    所以线程的频繁创建有以下影响：

	1、线程的创建和销毁都需要时间，当有大量的线程创建和销毁时，那么这些时间的消耗则比较明显，将导致性能上的缺失
	2、大量的线程创建、执行和销毁是非常耗cpu和内存的，这样将直接影响系统的吞吐量，导致性能急剧下降，如果内存资源占用的比较多，还很可能造成OOM
	3、大量的线程的创建和销毁很容易导致GC频繁的执行，从而发生内存抖动现象，而发生了内存抖动，对于移动端来说，最大的影响就是造成界面卡顿
    而针对上述所描述的问题，解决的办法归根到底就是：重用已有的线程，从而减少线程的创建。 为解决这个线程的弊端，java 提供了ExecutorService线程池来优化和管理线程的使用。所以这就

涉及到线程池（ExecutorService）的概念了，线程池的基本作用就是进行线程的复用
ExecutorService简介

    通常来说我们说到线程池第一时间想到的就是它：ExecutorService，它是一个接口，其实如果要从真正意义上来说，它可以叫做线程池的服务，因为它提供了众多接口api来控制线程池中的线程

，而真正意义上的线程池就是：ThreadPoolExecutor，它实现了ExecutorService接口，并封装了一系列的api使得它具有线程池的特性，其中包括工作队列、核心线程数、最大线程数等。
线程池：ThreadPoolExecutor

    既然线程池就是ThreadPoolExecutor，所以我们要创建一个线程池只需要new ThreadPoolExecutor(…);就可以创建一个线程池，而如果这样创建线程池的话，我们需要配置一堆东西，非常麻烦，

我们可以看一下它的构造方法就知道了：

public ThreadPoolExecutor(int corePoolSize,
                              int maximumPoolSize,
                              long keepAliveTime,
                              TimeUnit unit,
                              BlockingQueue<Runnable> workQueue,
                              ThreadFactory threadFactory,
                              RejectedExecutionHandler handler) {...}

    所以，官方也不推荐使用这种方法来创建线程池，而是推荐使用Executors的工厂方法来创建线程池，Executors类是官方提供的一个工厂类，它里面封装好了众多功能不一样的线程池，从而使得

我们创建线程池非常的简便，主要提供了如下五种功能不一样的线程池。

1、newFixedThreadPool() ：
获取方式：ExecutorService fixedThreadPool = Executors.newFixedThreadPool(5); 
作用：该方法返回一个固定线程数量的线程池，该线程池中的线程数量始终不变，即不会再创建新的线程，也不会销毁已经创建好的线程，自始自终都是那几个固定的线程在工作，所以该线程池可以

控制线程的最大并发数。 
栗子：假如有一个新任务提交时，线程池中如果有空闲的线程则立即使用空闲线程来处理任务，如果没有，则会把这个新任务存在一个任务队列中，一旦有线程空闲了，则按FIFO方式处理任务队列中

的任务。

2、newCachedThreadPool() ： 
获取方式：ExecutorService cachedThreadPool = Executors.newCachedThreadPool();
作用：该方法返回一个可以根据实际情况调整线程池中线程的数量的线程池。即该线程池中的线程数量不确定，是根据实际情况动态调整的。 
栗子：假如该线程池中的所有线程都正在工作，而此时有新任务提交，那么将会创建新的线程去处理该任务，而此时假如之前有一些线程完成了任务，现在又有新任务提交，那么将不会创建新线程去

处理，而是复用空闲的线程去处理新任务。那么此时有人有疑问了，那这样来说该线程池的线程岂不是会越集越多？其实并不会，因为线程池中的线程都有一个“保持活动时间”的参数，通过配置它

，如果线程池中的空闲线程的空闲时间超过该“保存活动时间”则立刻停止该线程，而该线程池默认的“保持活动时间”为60s。

3、newSingleThreadExecutor() ： 
获取方式：ExecutorService singleThreadPool = Executors.newSingleThreadExecutor();
作用：该方法返回一个只有一个线程的线程池，即每次只能执行一个线程任务，多余的任务会保存到一个任务队列中，等待这一个线程空闲，当这个线程空闲了再按FIFO方式顺序执行任务队列中的任

务。

4、newScheduledThreadPool() ：
获取方式：ScheduledExecutorService scheduledThreadPool = Executors.newScheduledThreadPool(5); 
作用：该方法返回一个可以控制线程池内线程定时或周期性执行某任务的线程池。

5、newSingleThreadScheduledExecutor() ： 
获取方式：ExecutorService singleThreadPool = Executors.newSingleThreadExecutor();
作用：该方法返回一个可以控制线程池内线程定时或周期性执行某任务的线程池。只不过和上面的区别是该线程池大小为1，而上面的可以指定线程池的大小。
可以看到通过Executors的工厂方法来创建线程池极其简便，其实它的内部还是通过new ThreadPoolExecutor(…)的方式创建线程池的，我们看一下这些工厂方法的内部实现：
public static ExecutorService newSingleThreadExecutor(ThreadFactory threadFactory) {
        return new FinalizableDelegatedExecutorService
            (new ThreadPoolExecutor(1, 1,
                                    0L, TimeUnit.MILLISECONDS,
                                    new LinkedBlockingQueue<Runnable>(),
                                    threadFactory));
    }

    
    public static ExecutorService newCachedThreadPool() {
        return new ThreadPoolExecutor(0, Integer.MAX_VALUE,
                                      60L, TimeUnit.SECONDS,
                                      new SynchronousQueue<Runnable>());
    }

    
    public static ExecutorService newCachedThreadPool(ThreadFactory threadFactory) {
        return new ThreadPoolExecutor(0, Integer.MAX_VALUE,
                                      60L, TimeUnit.SECONDS,
                                      new SynchronousQueue<Runnable>(),
                                      threadFactory);
    }

    
    public static ScheduledExecutorService newSingleThreadScheduledExecutor() {
        return new DelegatedScheduledExecutorService
            (new ScheduledThreadPoolExecutor(1));
    }

    
    public static ScheduledExecutorService newSingleThreadScheduledExecutor(ThreadFactory threadFactory) {
        return new DelegatedScheduledExecutorService
            (new ScheduledThreadPoolExecutor(1, threadFactory));
    }

所以主要就是要了解ThreadPoolExecutor，从构造方法开始：

    public ThreadPoolExecutor(int corePoolSize,                 //线程池中的核心线程数量
                              int maximumPoolSize,              //线程池中的最大线程数量 
                              long keepAliveTime,               //这个就是上面说到的“保持活动时间“，它起作用必须在一个前提下，就是当线程池中的线程数量超过了corePoolSize时，

它表示多余的空闲线程的存活时间，即：多余的空闲线程在超过keepAliveTime时间内没有任务的话则被销毁。而这个主要应用在缓存线程池中 
                              TimeUnit unit,                    //它是一个枚举类型，表示keepAliveTime的单位 s 、ms
                              BlockingQueue<Runnable> workQueue,//任务队列，主要用来存储已经提交但未被执行的任务，不同的线程池采用的排队策略不一样
                              ThreadFactory threadFactory,      //线程工厂，用来创建线程池中的线程，通常用默认的即可 
                              RejectedExecutionHandler handler) {//...}  //做拒绝策略，1、在线程池已经关闭的情况下 2、任务太多导致最大线程数和任务队列已经饱和，无法再接收新的

任务,默认的拒绝策略是抛一个RejectedExecutionException异常

线程池ThreadPoolExecutor的使用

newFixedThreadPool 
创建一个固定线程数量的线程池，示例为：
ExecutorService fixThreadPool = Executors.newFixedThreadPool(5);
        for (int i = 0;i<10;i++) {
            final int index = i;
            fixThreadPool.execute(new Runnable() {
                @Override
                public void run() {
                    Log.e("fixThreadPool","线程"+Thread.currentThread().getName()+"执行的任务"+ index +"");
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
我们创建了一个线程数为3的固定线程数量的线程池，同理该线程池支持的线程最大并发数也是3，而我模拟了10个任务让它处理，执行的情况则是首先执行前三个任务，后面7个则依次进入任务队列进

行等待，执行完前三个任务后，再通过FIFO的方式从任务队列中取任务执行，直到最后任务都执行完毕。

newCachedThreadPool
创建一个可以根据实际情况调整线程池中线程的数量的线程池 
ExecutorService cachedThreadPool = Executors.newCachedThreadPool();
        for (int i = 1; i <= 10; i++) {
            final int index = i;
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            cachedThreadPool.execute(new Runnable() {
                @Override
                public void run() {
                    String threadName = Thread.currentThread().getName();
                    Log.v("cachedThreadPool", "线程：" + threadName + ",正在执行第" + index + "个任务");
                    try {
                        long time = index * 1000;
                        Thread.sleep(time);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
线程池大小为无界，当执行第二个任务时第一个任务已经完成，会复用执行第一个任务的线程，而不用每次新建线程。

newSingleThreadExecutor 
创建一个只有一个线程的线程池，每次只能执行一个线程任务，多余的任务会保存到一个任务队列中，等待线程处理完再依次处理任务队列中的任务
ExecutorService singleThreadPool = Executors.newSingleThreadExecutor();
        for (int i = 1; i <= 10; i++) {
            final int index = i;
            singleThreadPool.execute(new Runnable() {
                @Override
                public void run() {
                    String threadName = Thread.currentThread().getName();
                    Log.v("zxy", "线程："+threadName+",正在执行第" + index + "个任务");
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });
        }

代码还是差不多，只不过改了线程池的实现方式，效果我想大家都知道，即依次一个一个的处理任务，而且都是复用一个线程

newScheduledThreadPool 
创建一个可以定时或者周期性执行任务的线程池，示例为：
        ScheduledExecutorService scheduledThreadPool = Executors.newScheduledThreadPool(3);
        //延迟2秒后执行该任务
        scheduledThreadPool.schedule(new Runnable() {
            @Override
            public void run() {

            }
        }, 2, TimeUnit.SECONDS);
        //延迟1秒后，每隔2秒执行一次该任务
        scheduledThreadPool.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {

            }
        }, 1, 2, TimeUnit.SECONDS);

newSingleThreadScheduledExecutor 
创建一个可以定时或者周期性执行任务的线程池，该线程池的线程数为1，示例为：

        ScheduledExecutorService singleThreadScheduledPool = Executors.newSingleThreadScheduledExecutor();
        //延迟1秒后，每隔2秒执行一次该任务
        singleThreadScheduledPool.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                String threadName = Thread.currentThread().getName();
                Log.v("zxy", "线程：" + threadName + ",正在执行");
            }
        },1,2,TimeUnit.SECONDS);

实际上这个和上面的没什么太大区别，只不过是线程池内线程数量的不同


下面写个测试代码，用于测试通过创建线程来更新进度条模拟执行耗时任务。
先了解任务队列
1.ConcurrentLinkedQueue
ConcurrentLinkedQueue是Queue的一个安全实现．Queue中元素按FIFO原则进行排序．采用CAS操作，来保证元素的一致性。ConcurrentLinkedQueue.size()是要遍历一遍集合的，比较耗时，所以尽量

要避免用size而改用isEmpty()
LinkedBlockingQueue是一个线程安全的阻塞队列，它实现了BlockingQueue接口
2.ConcurrentHashMap
ConcurrentHashMap是Java5中新增加的一个线程安全的Map集合，可以用来替代HashTable。对于ConcurrentHashMap是如何提高其效率的，可能大多人只是知道它使用了多个锁代替HashTable中的单个

锁，也就是锁分离技术（Lock Stripping）。实际上，ConcurrentHashMap对提高并发方面的优化
    1.使用CachedThreadPool线程池
    2.使用ConcurrentLinkedQueue<MyRunnable> 队列管理线程

参考链接：http://blog.csdn.net/u010687392/article/details/49850803
使用线程池管理线程的优点

	1、线程的创建和销毁由线程池维护，一个线程在完成任务后并不会立即销毁，而是由后续的任务复用这个线程，从而减少线程的创建和销毁，节约系统的开销
	2、线程池旨在线程的复用，这就可以节约我们用以往的方式创建线程和销毁所消耗的时间，减少线程频繁调度的开销，从而节约系统资源，提高系统吞吐量
	3、在执行大量异步任务时提高了性能
	4、Java内置的一套ExecutorService线程池相关的api，可以更方便的控制线程的最大并发数、线程的定时任务、单线程的顺序执行等
