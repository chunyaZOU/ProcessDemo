// MsgSender.aidl
package cc.cy.processdemo;

// Declare any non-default types here with import statements
import cc.cy.processdemo.MsgModel;
import cc.cy.processdemo.MsgReceiver;
interface MsgSender {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    //void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat, double aDouble, String aString);

    void sendMsg(in MsgModel msgModel);
    MsgModel getMsg();

    void registerReciverListener(MsgReceiver receiver);
    void unRegisterReceiverListener(MsgReceiver receiver);
}
