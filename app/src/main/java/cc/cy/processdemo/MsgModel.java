package cc.cy.processdemo;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by zcy on 2018/5/11.
 */

public class MsgModel implements Parcelable {
    public int id;
    public String content;

    public MsgModel(){

    }

    protected MsgModel(Parcel in) {
        id = in.readInt();
        content = in.readString();
    }

    public static final Creator<MsgModel> CREATOR = new Creator<MsgModel>() {
        @Override
        public MsgModel createFromParcel(Parcel in) {
            return new MsgModel(in);
        }

        @Override
        public MsgModel[] newArray(int size) {
            return new MsgModel[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(content);
    }
}
