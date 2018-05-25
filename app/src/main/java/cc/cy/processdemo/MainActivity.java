package cc.cy.processdemo;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.Process;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String MEMORY_LOG = "memory";
    private MsgSender mMsgSender;
    private TextView mTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        isLocalAppProcess();
        bindSer();
        mTextView = findViewById(R.id.tv);
        mTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    mTextView.setText(mMsgSender.getMsg().id + mMsgSender.getMsg().content);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        });




        /*ImageLoader imageLoader = ImageLoader.getInstance();
        //ImageLoaderConfiguration configuration=ImageLoaderConfiguration.createDefault(this);
        ImageLoaderConfiguration configuration =
                new ImageLoaderConfiguration.Builder(this)
                        .defaultDisplayImageOptions(DisplayImageOptions.createSimple())
                        .diskCache(null)
                        .imageDecoder(null)
                        .imageDownloader(null)
                        .memoryCache(null)
                        .taskExecutor(null)
                        .build();
        imageLoader.init(configuration);
        */
        /*ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newSingleThreadExecutor();
        executor.execute(new Runnable() {
            @Override
            public void run() {

            }
        });*/
        ExecutorService service = Executors.newCachedThreadPool();
        // ScheduledThreadPoolExecutor executor1= (ScheduledThreadPoolExecutor) Executors.newScheduledThreadPool(2);
        ScheduledThreadPoolExecutor executor1 = new ScheduledThreadPoolExecutor(2);
        executor1.schedule(new Runnable() {
            @Override
            public void run() {

            }
        }, 1000, TimeUnit.SECONDS);
        //ExecutorService service1=new ThreadPoolExecutor(3,10,60l, TimeUnit.SECONDS,new LinkedBlockingQueue<Runnable>(10));
        service.execute(new Runnable() {
            @Override
            public void run() {
                Log.i(TAG, "execute runnable");
            }
        });

        Future future1 = service.submit(new Runnable() {
            @Override
            public void run() {
                Log.i(TAG, "submit runnable");
            }
        });
        //future1.cancel(false);
        try {
            //阻塞等待任务完成并取得结果
            future1.get();
            Log.i(TAG, future1.get() == null ? "null" : future1.get().toString());
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        Future<String> future2 = service.submit(new Callable<String>() {
            @Override
            public String call() throws Exception {
                return "submit callable";
            }
        });
        try {
            Log.i(TAG, future2.get());
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        String result = "done";
        Future<String> future3 = service.submit(new Runnable() {
            @Override
            public void run() {

            }
        }, result);
        try {
            Log.i(TAG, future3.get());
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        service.shutdown();
        for (int i = 1; i <= 9; i++) {
            print99(i);
        }

        for (int i = 1; i <= 9; i++) {
            for (int j = 1; j <= i; j++) {
                System.out.print(j + "*" + i + "=" + j * i + "\t");
            }
            System.out.println();
        }
    }

    private void bindSer() {
        Intent intent = new Intent(this, MyService.class);
        bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
        startService(intent);
    }


    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mMsgSender = MsgSender.Stub.asInterface(service);

            try {
                mMsgSender.registerReciverListener(mMsgReceiver);
                mMsgSender.asBinder().linkToDeath(mDeathRecipient, 0);
                MsgModel msgModel = new MsgModel();
                msgModel.id = 0;
                msgModel.content = "hhhhhhhhhhhhhhhh";
                mMsgSender.sendMsg(msgModel);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mMsgSender = null;
        }
    };

    private final MsgReceiver mMsgReceiver = new MsgReceiver.Stub() {
        @Override
        public void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat, double aDouble, String aString) throws RemoteException {

        }

        @Override
        public void onMsgReceived(final MsgModel msgModel) throws RemoteException {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mTextView.setText(msgModel.id + msgModel.content);
                }
            });
        }
    };

    IBinder.DeathRecipient mDeathRecipient = new IBinder.DeathRecipient() {
        @Override
        public void binderDied() {
            if (mMsgSender != null) {
                mMsgSender.asBinder().unlinkToDeath(this, 0);
                mMsgSender = null;
            }
            bindSer();
        }
    };

    private boolean isLocalAppProcess() {
        int pId = Process.myPid();
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.RunningAppProcessInfo runningAppProcessInfo = null;
        List runningProcess = manager.getRunningAppProcesses();
        if (runningProcess != null) {
            Iterator iterator = runningProcess.iterator();
            while (iterator.hasNext()) {
                ActivityManager.RunningAppProcessInfo processInfo = (ActivityManager.RunningAppProcessInfo) iterator.next();
                if (processInfo.pid == pId) {
                    runningAppProcessInfo = processInfo;
                }
            }
        }
        if (runningAppProcessInfo == null) return false;
        Log.i(MEMORY_LOG, manager.getMemoryClass() + "");
        Log.i(MEMORY_LOG, manager.getLargeMemoryClass() + "");
        Log.i(MEMORY_LOG, Runtime.getRuntime().maxMemory() / (1024 * 1024) + "");
        Log.i(MEMORY_LOG, runningAppProcessInfo.processName + getPackageName());
        return runningAppProcessInfo.processName.equals(getPackageName());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mMsgSender != null && mMsgSender.asBinder().isBinderAlive()) {
            try {
                mMsgSender.unRegisterReceiverListener(mMsgReceiver);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        unbindService(mServiceConnection);
    }


    private void print99(int num) {
        switch (num) {
            case 1:
                printLine(num);
                break;
            case 2:
                printLine(num);
                break;
            case 3:
                printLine(num);
                break;
            case 4:
                printLine(num);
                break;
            case 5:
                printLine(num);
                break;
            case 6:
                printLine(num);
                break;
            case 7:
                printLine(num);
                break;
            case 8:
                printLine(num);
                break;
            case 9:
                printLine(num);
                break;
            default:
                break;
        }
    }

    private void printLine(int num) {
        for (int i = 1; i <= num; i++) {
            System.out.print(num + "*" + i + "=" + num * i);
        }
        System.out.println();
    }

}
