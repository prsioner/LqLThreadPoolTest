package com.example.administrator.lqlthreadpooldemo;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private String TAG = "MainActivity";
    /** 任务执行队列 */
    private ConcurrentLinkedQueue<MyRunnable> taskQueue;
    /**
     * 正在等待执行或已经完成的任务队列
     * 备注：Future类，一个用于存储异步任务执行的结果，比如：判断是否取消、是否可以取消、是否正在执行、是否已经完成等
     * */
    private ConcurrentHashMap<Future,MyRunnable> taskMap;
    private ExecutorService cacheFixedTreadPool;
    private ProgressBar pb;
    private Object lock = new Object();
    /** 线程池是否处于运行状态(即:是否被释放!) */
    private boolean isRuning = true;

    /** 唤醒标志，是否唤醒线程池工作 */
    private boolean isNotify = true;
    public Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            pb.setProgress(msg.what);
        }
    } ;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();




    }
    private void init(){
        pb = (ProgressBar)findViewById(R.id.progressBar1);
        findViewById(R.id.button1).setOnClickListener(this);
        findViewById(R.id.button2).setOnClickListener(this);
        findViewById(R.id.button3).setOnClickListener(this);
        findViewById(R.id.button4).setOnClickListener(this);
        findViewById(R.id.button5).setOnClickListener(this);
        cacheFixedTreadPool = Executors.newCachedThreadPool();
        taskQueue = new ConcurrentLinkedQueue<MyRunnable>();
        taskMap = new ConcurrentHashMap<Future, MyRunnable>();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.button1:
                startTask();
                break;
            case R.id.button2:
                cancelTask();
                break;
            case R.id.button3:
                reloadTask(new MyRunnable(mHandler));
                break;
            case R.id.button4:
                releaseTask();
                break;
            case R.id.button5:
                addTask(new MyRunnable(mHandler));
                break;
        }
    }

    private void addTask(final Runnable runnable){
        mHandler.sendEmptyMessage(0);
        if (cacheFixedTreadPool == null)
        {
            cacheFixedTreadPool = Executors.newCachedThreadPool();
            notifyWork();
        }
        if (taskQueue == null)
        {
            taskQueue = new ConcurrentLinkedQueue<MyRunnable>();
        }
        if (taskMap == null)
        {
            taskMap = new ConcurrentHashMap<Future, MyRunnable>();
        }
        cacheFixedTreadPool.execute(new Runnable() {
            @Override
            public void run() {
                /** 插入一个Runnable到任务队列中 */
                taskQueue.offer((MyRunnable) runnable);
                // taskQueue.add(mr);
                notifyWork();
            }
        });
        Toast.makeText(MainActivity.this, "已添加一个新任务到线程池中 ！", Toast.LENGTH_SHORT).show();
    }

    private void startTask(){
        if (cacheFixedTreadPool == null || taskQueue == null || taskMap == null)
        {
            Log.i(TAG, "某资源是不是已经被释放了？");
            return;
        }
        cacheFixedTreadPool.execute(new Runnable() {
            @Override
            public void run() {
              if (isRuning){
                  MyRunnable myRunnable = null;
                synchronized (lock){
                    myRunnable = taskQueue.poll(); // 从线程队列中取出一个Runnable对象来执行，如果此队列为空，则调用poll()方法会返回null
                    if (myRunnable ==null){
                        isNotify = true;
                    }
                    if (myRunnable != null)
                    {
                        taskMap.put(cacheFixedTreadPool.submit(myRunnable), myRunnable);
                    }
                }
              }
            }
        });

    }

    private void cancelTask(){
        if (!taskMap.isEmpty()&&(taskMap !=null)) {
            for (MyRunnable runnable : taskMap.values()) {
                runnable.setCancleTaskUnit(true);
            }
        }

    }


    private void reloadTask(final MyRunnable mr){

        {
            mHandler.sendEmptyMessage(0);
            if (cacheFixedTreadPool == null)
            {
                cacheFixedTreadPool = Executors.newCachedThreadPool();
                notifyWork();
            }

            if (taskQueue == null)
            {
                taskQueue = new ConcurrentLinkedQueue<MyRunnable>();
            }

            if (taskMap == null)
            {
                taskMap = new ConcurrentHashMap<Future, MyRunnable>();
            }

            cacheFixedTreadPool.execute(new Runnable()
            {

                @Override
                public void run()
                {
                    /** 插入一个Runnable到任务队列中 */
                    taskQueue.offer(mr);
                    // taskQueue.add(mr);
                    notifyWork();
                }
            });

            cacheFixedTreadPool.execute(new Runnable()
            {
                @Override
                public void run()
                {
                    if (isRuning)
                    {
                        MyRunnable myRunnable = null;
                        synchronized (lock)
                        {
                            myRunnable = taskQueue.poll(); // 从线程队列中取出一个Runnable对象来执行，如果此队列为空，则调用poll()方法会返回null
                            if (myRunnable == null)
                            {
                                isNotify = true;
                            }
                        }

                        if (myRunnable != null)
                        {
                            taskMap.put(cacheFixedTreadPool.submit(myRunnable), myRunnable);
                        }
                    }
                }
            });
        }
    }

    private void releaseTask(){
        /** 将ProgressBar进度置为0 */
        mHandler.sendEmptyMessage(0);
        isRuning = false;
        cancelTask();

        Iterator iter = taskMap.entrySet().iterator();
        if (iter != null) {
            while (iter.hasNext()) {
                Map.Entry<Future, MyRunnable> entry = (Map.Entry<Future, MyRunnable>) iter.next();
                Future result = entry.getKey();
                if (result == null) {
                    continue;
                }
                result.cancel(true);
                taskMap.remove(result);
            }
        }
        if (null != cacheFixedTreadPool)
        {
            cacheFixedTreadPool.shutdown();
        }

        cacheFixedTreadPool = null;
        taskMap = null;
        taskQueue = null;
    }


    private void notifyWork()
    {
        synchronized (lock)
        {
            if (isNotify)
            {
                lock.notifyAll();
                isNotify = !isNotify;
            }
        }
    }
}
