package com.teleostnacl.szu.documentation.model;

import androidx.annotation.NonNull;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.teleostnacl.common.android.log.Logger;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Objects;

/**
 * 历史下载的证明文件
 */
public class HistoryFileModel implements Comparable<HistoryFileModel> {
    private final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
    @SerializedName("orderId")
    @Expose
    public String orderId;
    @SerializedName("itemId")
    @Expose
    public String itemId;
    @SerializedName("title")
    @Expose
    public String title;
    @SerializedName("payTime")
    @Expose
    public String payTime;
    @SerializedName("expireTime")
    @Expose
    public String expireTime;
    @SerializedName("takeType")
    @Expose
    public Integer takeType;

    @Override
    public int compareTo(@NonNull HistoryFileModel o) {
        return Long.compare(getExpireTime(), o.getExpireTime());
    }

    /**
     * @return 获取过期时间的long值
     */
    public long getExpireTime() {
        long time = 0;
        try {
            time = Objects.requireNonNull(format.parse(expireTime)).getTime();
        } catch (Exception e) {
            Logger.e(e);
        }

        return time;
    }
}
