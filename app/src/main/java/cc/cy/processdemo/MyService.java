package cc.cy.processdemo;

import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.util.Log;

public class MyService extends Service {
    private static final String TAG = "MyService";
    private MsgModel mMsgModel;
    private RemoteCallbackList<MsgReceiver> mRemoteCallbackList = new RemoteCallbackList<>();

    public MyService() {
    }

    IBinder mBinder = new MsgSender.Stub() {
        @Override
        public void sendMsg(MsgModel msgModel) throws RemoteException {
            mMsgModel = msgModel;
            Log.i(TAG, msgModel.id + msgModel.content);
        }

        @Override
        public MsgModel getMsg() throws RemoteException {
            return mMsgModel;
        }

        @Override
        public void registerReciverListener(MsgReceiver receiver) throws RemoteException {
            mRemoteCallbackList.register(receiver);
        }

        @Override
        public void unRegisterReceiverListener(MsgReceiver receiver) throws RemoteException {
            mRemoteCallbackList.unregister(receiver);
        }

        @Override
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            //通信包名验证
            String packageName = "";
            String[] packageNames = getPackageManager().getPackagesForUid(getCallingUid());
            if (packageNames != null && packageNames.length > 0) {
                packageName = packageNames[0];
            }
            if (packageName == null || !packageName.startsWith("cc.cy.processdemo"))
                return false;
            return super.onTransact(code, data, reply, flags);
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        //自定义权限验证
        if (checkCallingOrSelfPermission("cc.cy.processdemo.MyService_Permission") == PackageManager.PERMISSION_DENIED)
            return null;
        return mBinder;
    }

    private int i;

    @Override
    public void onCreate() {
        super.onCreate();
        //执行耗时任务
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    i++;
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    MsgModel msgModel = new MsgModel();
                    msgModel.id = i;
                    msgModel.content = "跨进程通信Demo" + i;
                    final int callbackListCount = mRemoteCallbackList.beginBroadcast();
                    for (int j = 0; j < callbackListCount; j++) {
                        MsgReceiver msgReceiver = mRemoteCallbackList.getBroadcastItem(j);
                        try {
                            msgReceiver.onMsgReceived(msgModel);
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }
                    mRemoteCallbackList.finishBroadcast();
                }
            }
        }).start();
    }
}
