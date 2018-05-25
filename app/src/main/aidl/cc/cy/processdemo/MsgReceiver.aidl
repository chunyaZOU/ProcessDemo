// MsgReceiver.aidl
package cc.cy.processdemo;

// Declare any non-default types here with import statements
import cc.cy.processdemo.MsgModel;
interface MsgReceiver {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat,
            double aDouble, String aString);
    void onMsgReceived(in MsgModel msgModel);
}
