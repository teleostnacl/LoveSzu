package com.teleostnacl.szu.electricity.model;

import android.text.TextUtils;

import androidx.lifecycle.MutableLiveData;

import com.teleostnacl.common.android.retrofit.QueryFieldMap;
import com.teleostnacl.common.android.utils.UrlUtils;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public class ElectricityModel {
    //用电查询
    public final static String USING = "2";
    //够电记录
    public final static String BUYING = "1";

    public final static String LIHU_BUILDING = "172.25.100.105:8010";

    // 丽湖二期宿舍 检查房间号是否合法的参数
    public final static String LIHU_drlouming = "drlouming";
    public final static String LIHU_ablou = "ablou";
    public final static String LIHU_drceng = "drceng";

    // 标记是否使用本地数据
    public boolean useLocal = false;
    // 记录储存在本地最晚的数据的年月
    public MutableLiveData<Integer> localDate = new MutableLiveData<>();

    // 宿舍区 ip
    public final Map<String, String> clientMap = new LinkedHashMap<>();

    // 宿舍楼栋 代号
    public final Map<String, String> buildingIdMap = new LinkedHashMap<>();

    // 记录宿舍区对应的名字
    public String clientName;
    // 记录宿舍区对应的地址
    public String client;
    // 记录宿舍楼的名字
    public String buildingName;
    // 记录宿舍楼栋的id
    public String buildingId;
    // 记录宿舍的名字
    public int roomName;
    // 记录房间号(服务器的)
    public String roomId;

    // region 与丽湖有关的请求参数
    public String __EVENTARGUMENT;
    public String __LASTFOCUS;
    public String __VIEWSTATE;
    public String __VIEWSTATEGENERATOR;

    // 用于丽湖切换查询购电和用电的__VIEWSTATE
    private String tmp__VIEWSTATE;

    public String __EVENTVALIDATION;

    private String ablou;
    private String drceng;

    // 丽湖校区剩余电费
    public final MutableLiveData<LiHuRemainModel> lihuRemain = new MutableLiveData<>();
    // endregion

    /**
     * @return 当前宿舍区 宿舍楼栋 名字 及 房间号是否有效
     */
    public boolean checkValid() {
        return !TextUtils.isEmpty(client) &&
                !TextUtils.isEmpty(buildingName) && !TextUtils.isEmpty(buildingId) &&
                roomName != 0 && !TextUtils.isEmpty(roomId);
    }

    /**
     * @return 检查粤海 西丽一期学生宿舍房间号是否正确的请求表单
     */
    public Map<String, String> getCheckRoomIdQueryMap() {
        Map<String, String> map = new QueryFieldMap();

        map.put("client", client);
        map.put("buildingName", "");
        map.put("buildingId", buildingId);
        map.put("roomName", String.valueOf(roomName));
        map.put("select", "+%B2%E9%D1%AF+");

        return map;
    }

    /**
     * @return 检查西丽二期学生宿舍房间号是否正确的请求表单
     */
    public Map<String, String> getLiHuCheckRoomIdQueryMap(String __EVENTTARGET) {
        Map<String, String> map = new QueryFieldMap();

        map.put("__EVENTTARGET", __EVENTTARGET);
        map.put("__EVENTARGUMENT", __EVENTARGUMENT);
        map.put("__LASTFOCUS", __LASTFOCUS);
        map.put("__VIEWSTATE", __VIEWSTATE);
        map.put("__VIEWSTATEGENERATOR", __VIEWSTATEGENERATOR);

        map.put("drlouming", buildingId);

        if (TextUtils.isEmpty(ablou) || TextUtils.isEmpty(drceng)) {
            String room = String.format(Locale.CHINA, "%04d", roomName);
            ablou = buildingId + room.substring(0, 2);
            drceng = roomId = buildingId + room;
        }

        switch (__EVENTTARGET) {
            case LIHU_drlouming: {
                map.put("ablou", "");
                map.put("drceng", "");
                break;
            }
            case LIHU_ablou: {
                map.put("ablou", ablou);
                map.put("drceng", "");
                break;
            }
            case LIHU_drceng: {
                map.put("ablou", ablou);
                map.put("drceng", drceng);
                break;
            }
        }

        return map;
    }

    /**
     * 切换查询西丽二期宿舍用电/购电记录的请求表单
     *
     * @param use true - 用电记录; false - 购电记录
     * @return 请求表单
     */
    public Map<String, String> getLiHuSecondSwitch(boolean use) {
        Map<String, String> map = getLiHuCheckRoomIdQueryMap(LIHU_drceng);

        // 为空时, 第一次切换 直接从
        if (tmp__VIEWSTATE == null) {
            tmp__VIEWSTATE = __VIEWSTATE;
        }
        map.put("__VIEWSTATE", tmp__VIEWSTATE);

        map.put("ablou", ablou);
        map.put("drceng", drceng);
        map.put("__EVENTTARGET", "");
        map.put("radio", use ? "usedR" : "buyR");

        map.put("ImageButton1.x", "0");
        map.put("ImageButton1.y", "0");

        return map;
    }

    /**
     * 获取粤海 西丽一期学生宿舍的查询结果的请求表单
     *
     * @param type      用电/购电
     * @param beginTime 开始时间
     * @param endTime   结束时间
     * @return 请求表单
     */
    public Map<String, String> getQueryMap(String type, String beginTime, String endTime) {
        Map<String, String> map = new QueryFieldMap();

        map.put("beginTime", beginTime);
        map.put("endTime", endTime);
        map.put("type", type);
        map.put("client", client);
        map.put("roomId", roomId);

        StringBuilder roomNameBuilder = new StringBuilder(String.valueOf(roomName));
        for (int i = 0; i < 20 - String.valueOf(roomName).length(); i++) {
            roomNameBuilder.append("+");
        }
        map.put("roomName", roomNameBuilder.toString());
        map.put("building", UrlUtils.encode(buildingId, "GB2312") + "++++++++++++++");

        return map;
    }

    /**
     * 获取丽湖二期学生宿舍的查询结果的请求表单
     *
     * @return 请求表单
     */
    public Map<String, String> getLiHuSecondFieldMap(String txtstart, String txtend) {
        Map<String, String> map = new QueryFieldMap();
        map.put("__VIEWSTATE", __VIEWSTATE);
        map.put("__VIEWSTATEGENERATOR", __VIEWSTATEGENERATOR);
        map.put("__EVENTVALIDATION", __EVENTVALIDATION);

        map.put("txtstart", txtstart);
        map.put("txtend", txtend);

        map.put("btnser", "查询");

        return map;
    }

    /**
     * 设置宿舍楼的名称
     *
     * @param buildingName map的key
     */
    public void setBuildingName(String buildingName) {
        this.buildingName = buildingName;

        this.ablou = null;
        this.drceng = null;
        this.tmp__VIEWSTATE = null;

        this.buildingId = buildingIdMap.get(buildingName);
    }

    /**
     * @return 获取记录在SP的本地的时间
     */
    public int getLocalDate() {
        // 使用本地 且本地时间不为空时 才使用本地时间
        if (localDate.getValue() != null) {
            return localDate.getValue();
        } else {
            return 0;
        }
    }

    /**
     * @return 判断是否为丽湖二期宿舍
     */
    public boolean isLiHuSecond() {
        return LIHU_BUILDING.equals(client);
    }

}
