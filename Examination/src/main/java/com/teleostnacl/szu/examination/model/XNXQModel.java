package com.teleostnacl.szu.examination.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.teleostnacl.common.java.util.NumberUtils;

import java.util.Objects;

public class XNXQModel implements Parcelable {
    @SerializedName("DM")
    @Expose
    public String dm;
    @SerializedName("MC")
    @Expose
    public String mc;

    /**
     * @return 获取学期学年所组成的long值
     */
    public long getXQXMDm() {
        return NumberUtils.parseLong(dm.replaceAll("-", ""), 0);
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        XNXQModel xnxqModel = (XNXQModel) o;
        return Objects.equals(dm, xnxqModel.dm) && Objects.equals(mc, xnxqModel.mc);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dm, mc);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(this.dm);
        dest.writeString(this.mc);
    }

    public void readFromParcel(@NonNull Parcel source) {
        this.dm = source.readString();
        this.mc = source.readString();
    }

    public XNXQModel() {
    }

    protected XNXQModel(@NonNull Parcel in) {
        this.dm = in.readString();
        this.mc = in.readString();
    }

    public static final Parcelable.Creator<XNXQModel> CREATOR = new Parcelable.Creator<>() {
        @Override
        public XNXQModel createFromParcel(Parcel source) {
            return new XNXQModel(source);
        }

        @Override
        public XNXQModel[] newArray(int size) {
            return new XNXQModel[size];
        }
    };
}
