package com.example.administrator.lqlthreadpooldemo;

import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;

/**
 * Created by Administrator on 2016/5/17.
 */
public class MyRunnable implements Runnable{

    private Handler mHandler;
    private boolean cancleTask = false;
    private boolean cancleException = false;

    public MyRunnable(Handler handler){
        this.mHandler = handler;
    }
    @Override
    public void run() {
        if (cancleTask == false)
        {
            running();
            Log.i("KKK", "调用MyRunnable run()方法");
        }
    }

    private void running()
    {
        Log.i("KKK", "running()");
        try
        {
            // 做点有可能会出异常的事情！！！
            int prog = 0;
            if (cancleTask == false && cancleException == false)
            {
                while (prog < 101)
                {
                    if ((prog > 0 || prog == 0) && prog < 70)
                    {
                        SystemClock.sleep(100);
                    }
                    else
                    {
                        SystemClock.sleep(300);
                    }
                    if (cancleTask == false)
                    {
                        mHandler.sendEmptyMessage(prog++);
                        Log.i("KKK", "调用 prog++ = " + (prog));
                    }
                }
            }
        }
        catch (Exception e)
        {
            cancleException = true;
        }
    }

    public void setCancleTaskUnit(boolean cancleTask)
    {
        this.cancleTask = cancleTask;
        Log.i("KKK", "点击了取消任务按钮 ！！！");
        // mHandler.sendEmptyMessage(0);
    }
}
