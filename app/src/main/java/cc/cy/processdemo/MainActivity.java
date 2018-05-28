package cc.cy.processdemo;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private MsgSender mMsgSender;
    private TextView mTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
                msgModel.content = "跨进程通信Demo";
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
            //重新绑定
            bindSer();
        }
    };

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
}