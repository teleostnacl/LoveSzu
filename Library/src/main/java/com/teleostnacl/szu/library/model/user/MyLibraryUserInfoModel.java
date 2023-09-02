package com.teleostnacl.szu.library.model.user;

import androidx.annotation.NonNull;
import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;
import androidx.databinding.library.baseAdapters.BR;

import com.teleostnacl.common.android.retrofit.QueryFieldMap;
import com.teleostnacl.common.android.context.ResourcesUtils;
import com.teleostnacl.common.java.util.NumberUtils;
import com.teleostnacl.common.java.util.StringUtils;
import com.teleostnacl.szu.library.R;

import org.jsoup.select.Elements;

import java.util.Map;

public class MyLibraryUserInfoModel extends BaseObservable {
    //卡号
    public long no;

    //姓名
    public String name;

    //类型
    public String type;

    //单位
    public String institution;

    //当前状态
    public String currentStatus;

    //电话
    public String phone;

    //手机
    public String mobile;

    //备注
    public String notes;

    //电子邮件
    public String email;

    //预存余额
    public String depositBalance;

    //积分
    public String points;

    //修改联系方式使用的变量
    public String editPhone;
    public String editMobile;
    public String editMail;

    // 修改联系方式时的请求表单信息
    public String __VIEWSTATE;
    public String __VIEWSTATEGENERATOR;
    public String __EVENTVALIDATION;

    public MyLibraryUserInfoModel(@NonNull Elements elements) {
        no = NumberUtils.parseLong(elements.get(0).text(), 0);
        name = elements.get(1).text();
        type = elements.get(2).text();
        institution = elements.get(3).text();
        currentStatus = elements.get(4).text();
        phone = elements.get(5).text();
        mobile = elements.get(6).text();
        notes = elements.get(7).text();
        email = elements.get(8).text().split(" （")[0];
        depositBalance = elements.get(9).text();
        points = elements.get(10).text();
    }

    public void clearEdit() {
        editPhone = null;
        editMobile = null;
        editMail = null;
    }

    /**
     * @return 修改联系方式时的请求的POST表单
     */
    public Map<String, String> getFieldMap() {
        Map<String, String> map = new QueryFieldMap();
        map.put("__VIEWSTATE", __VIEWSTATE);
        map.put("__VIEWSTATEGENERATOR", __VIEWSTATEGENERATOR);
        map.put("__EVENTVALIDATION", __EVENTVALIDATION);

        map.put("ctl00$cpRight$txtphone", editPhone);
        map.put("ctl00$cpRight$txtcellphone", editMobile);
        map.put("ctl00$cpRight$txtemail", editMail);

        map.put("ctl00$cpRight$btnModify", "保存");

        return map;
    }

    //region binding
    public String getName() {
        return ResourcesUtils.getString(R.string.my_library_user_info_name) + StringUtils.getOrBlank(name);
    }

    public String getNo() {
        return ResourcesUtils.getString(R.string.my_library_user_info_no) + no;
    }

    public String getType() {
        return ResourcesUtils.getString(R.string.my_library_user_info_type) + StringUtils.getOrBlank(type);
    }

    public String getInstitution() {
        return ResourcesUtils.getString(R.string.my_library_user_info_institution) + StringUtils.getOrBlank(institution);
    }

    public String getCurrentStatus() {
        return ResourcesUtils.getString(R.string.my_library_user_info_status) + StringUtils.getOrBlank(currentStatus);
    }

    public String getPhone() {
        return ResourcesUtils.getString(R.string.my_library_user_info_phone) + StringUtils.getOrBlank(phone);
    }

    @Bindable
    public String getEditPhone() {
        if (editPhone == null) editPhone = phone;
        return editPhone;
    }

    public void setEditPhone(String phone) {
        this.editPhone = phone;
        notifyPropertyChanged(BR.editPhone);
    }

    public String getMobile() {
        return ResourcesUtils.getString(R.string.my_library_user_info_mobile) + mobile;
    }

    @Bindable
    public String getEditMobile() {
        if (editMobile == null) editMobile = mobile;
        return editMobile;
    }

    public void setEditMobile(String mobile) {
        this.editMobile = mobile;
        notifyPropertyChanged(BR.editMobile);
    }

    public String getEmail() {
        return ResourcesUtils.getString(R.string.my_library_user_info_email) + StringUtils.getOrBlank(email);
    }

    @Bindable
    public String getEditMail() {
        if (editMail == null) editMail = email;
        return editMail;
    }

    public void setEditMail(String mail) {
        this.editMail = mail;
        notifyPropertyChanged(BR.editMail);
    }

    public String getNotes() {
        return ResourcesUtils.getString(R.string.my_library_user_info_notes) + StringUtils.getOrBlank(notes);
    }

    public String getDepositBalance() {
        return ResourcesUtils.getString(R.string.my_library_user_info_deposit_balance) + StringUtils.getOrBlank(depositBalance);
    }

    public String getPoints() {
        return ResourcesUtils.getString(R.string.my_library_user_info_points) + StringUtils.getOrBlank(points);
    }
    //endregion
}
