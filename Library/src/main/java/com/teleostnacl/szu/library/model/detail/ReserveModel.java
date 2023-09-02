package com.teleostnacl.szu.library.model.detail;

import android.view.View;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;

import com.teleostnacl.common.android.retrofit.QueryFieldMap;
import com.teleostnacl.common.java.util.StringUtils;
import com.teleostnacl.szu.library.BR;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 预约使用的模型
 */
public class ReserveModel extends BaseObservable {

    // region 公共量
    //可借阅状态
    public final static int CAN_LEND = 0;

    // 请求表单
    public String __EVENTVALIDATION;
    public String __VIEWSTATE;
    public String __VIEWSTATEGENERATOR;
    public String Button1;

    /**
     * 取书地点 :
     * "4" -> 丽湖校区分馆（一期）二楼借还书处
     * "1" -> 汇智楼（南馆）二楼借还书台
     * "3" -> 粤海校区汇典楼（北馆）二楼借还书台
     */
    public String ddlfl;

    //全部可选地点
    public final Map<String, String> places = new LinkedHashMap<>();

    //电子邮件
    public String txtemail;

    //所有卷期的集合
    public final Map<String, String> volumes = new HashMap<>();

    //选择预约或预借的卷期
    public String vol;
    // endregion

    // region 预约使用的参数
    //记录是否可预约状态
    public int reserveFlag = CAN_LEND;

    //当前该书未被借阅
    public final static int CAN_NOT_LEND_NO = 1;
    //当前该书预约已满
    public final static int CAN_NOT_LEND_FULL = 2;

    public String volbooktype;

    public String org;

    public String bktype;

    public String ctrlno;

    public String hiduid;
    // endregion

    // region 预借使用的参数
    //记录是否可预借状态
    public int borrowAdvanceFlag = CAN_LEND;
    //当前没有可预借的图书
    public final static int CAN_NOT_BORROW_ADVANCE = 1;
    //用户当前预借数量已满
    public final static int BORROW_ADVANCE_USER_FULL = 2;

    //是否在西丽(?)
    public String isxili;

    //电话
    public String txtphone;
    // endregion

    /**
     * @return 预借的请求表单信息
     */
    public Map<String, String> getBorrowAdvanceFieldMap() {
        Map<String, String> map = new QueryFieldMap();
        map.put("__EVENTVALIDATION", __EVENTVALIDATION);
        map.put("__VIEWSTATE", __VIEWSTATE);
        map.put("__VIEWSTATEGENERATOR", __VIEWSTATEGENERATOR);
        map.put("Button1", Button1);
        map.put("ddlfl", ddlfl);
        map.put("ddlvolume", vol);
        map.put("isxili", isxili);
        map.put("txtemail", txtemail);
        map.put("txtphone", txtphone);
        map.put("kdaddress", "");
        map.put("kdname", "");
        map.put("kdphone", "");
        return map;
    }

    /**
     * @return 预约请求的表单信息(QueryMap)
     */
    public Map<String, String> getReserveQueryMap() {
        Map<String, String> map = new QueryFieldMap();
        map.put("vol", StringUtils.getOrBlank(vol) + "@");
        map.put("volbooktype", volbooktype);
        map.put("org", org);
        map.put("bktype", bktype);
        map.put("ctrlno", ctrlno);
        return map;
    }

    /**
     * @return 预约请求的表单信息(FieldMap)
     */
    public Map<String, String> getReserveFieldMap() {
        Map<String, String> map = new QueryFieldMap();
        map.put("__EVENTVALIDATION", __EVENTVALIDATION);
        map.put("__VIEWSTATE", __VIEWSTATE);
        map.put("__VIEWSTATEGENERATOR", __VIEWSTATEGENERATOR);
        map.put("Button1", Button1);
        map.put("hiduid", hiduid);
        map.put("ddlfl", ddlfl);
        map.put("txtemail", txtemail);
        return map;
    }

    //region Databinding
    public int getVolumeVisibility() {
        //如果卷期为0,则不显示卷期
        if (volumes.size() == 0) return View.GONE;
        else return View.VISIBLE;
    }

    @Bindable
    public String getEmail() {
        return txtemail;
    }

    public void setEmail(String email) {
        this.txtemail = email;
        notifyPropertyChanged(BR.email);
    }

    @Bindable
    public String getPhone() {
        return txtphone;
    }

    public void setPhone(String phone) {
        this.txtphone = phone;
        notifyPropertyChanged(BR.phone);
    }
    //endregion
}
