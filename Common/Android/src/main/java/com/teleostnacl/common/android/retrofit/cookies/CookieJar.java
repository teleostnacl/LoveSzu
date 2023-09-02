package com.teleostnacl.common.android.retrofit.cookies;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.teleostnacl.common.android.log.Logger;
import com.teleostnacl.common.java.util.ExecutorServiceUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.HttpUrl;

public class CookieJar implements okhttp3.CookieJar {
    private Map<String, Cookie> map = new HashMap<>();

    // 数据库持久化
    @Nullable
    private final CookieDao cookieDao;

    // 保证map稳健 使用单线程对数据库进行操作
    private final ExecutorService mapService = Executors.newSingleThreadExecutor();
    // 保证数据库稳健 使用单线程对数据库进行操作
    private final ExecutorService databaseService = Executors.newSingleThreadExecutor();

    // 用于控制部分功能下 cookies不需要存进数据库中
    private boolean needSaveToDatabase = true;

    public CookieJar(@Nullable CookieDao cookieDao) {
        //获取存在cookies数据库的Dao
        this.cookieDao = cookieDao;

        if (cookieDao != null) {
            //从数据库中取出所有cookie并放入map中
            List<Cookie> list = ExecutorServiceUtils.submitByExecutor(
                    databaseService, cookieDao::getCookies, ArrayList::new);
            for (Cookie c : list) {
                // 判断cookies是否过期,过期则从数据库删去
                if (new Date().getTime() >= c.expiresAt) {
                    databaseService.execute(() -> cookieDao.delete(c));
                }
                //有效则存入数据库中
                else {
                    mapService.execute(() -> map.put(getCookieKey(c), c));
                }
            }
        }
    }

    @Override
    public void saveFromResponse(@NonNull HttpUrl url, @NonNull List<okhttp3.Cookie> cookies) {
        for (okhttp3.Cookie cookie : cookies) {
            addCookie(new Cookie(cookie));
        }
    }

    @NonNull
    @Override
    public List<okhttp3.Cookie> loadForRequest(@NonNull HttpUrl url) {
        Logger.v("Cookies", url.toString());

        return getCookies(url.toString());
    }

    /**
     * 添加cookie信息
     *
     * @param cookie 所需添加cookie信息
     */
    public void addCookie(Cookie cookie) {
        //如果是持久化,则存入数据库中
        if (cookieDao != null && needSaveToDatabase && cookie.persistent) {
            databaseService.execute(() -> cookieDao.insertOrUpdate(cookie));
        }
        //存入map中
        mapService.execute(() -> map.put(getCookieKey(cookie), cookie));
    }

    /**
     * 给指定的url添加cookie
     */
    public void addCookie(String url, String setCookies) {
        HttpUrl httpUrl = HttpUrl.parse(url);
        if (httpUrl == null) {
            return;
        }

        okhttp3.Cookie cookie = okhttp3.Cookie.parse(httpUrl, setCookies);
        if (cookie == null) {
            return;
        }

        addCookie(new Cookie(cookie));
    }

    /**
     * 获取指定路径的cookies
     *
     * @param url 指定的路径
     * @return 返回指定路径的请求头格式的Cookies
     */
    public String getCookiesString(String url) {
        StringBuilder cookiesStringBuilder = new StringBuilder();
        for (okhttp3.Cookie cookie : getCookies(url)) {
            cookiesStringBuilder.append(cookie.name()).append("=").append(cookie.value()).append("; ");
        }

        return cookiesStringBuilder.toString();
    }

    /**
     * 获取指定路径的cookies
     *
     * @param url 指定的路径
     * @return 返回指定路径的Cookies集合
     */
    public List<okhttp3.Cookie> getCookies(String url) {
        return ExecutorServiceUtils.submitByExecutor(mapService, () -> {
            List<okhttp3.Cookie> list = new ArrayList<>();
            for (Map.Entry<String, Cookie> entry : map.entrySet()) {
                Cookie cookie = entry.getValue();
                //如果该cookies属于该url的,则传递
                if (url.matches(".*" + cookie.domain + "(:\\d+)?" + cookie.path + ".*"))
                    list.add(cookie.getCookie());
            }
            return list;
        }, ArrayList::new);
    }

    /**
     * 清除指定网址的Cookies
     */
    public void clearCookies(String url) {
        List<okhttp3.Cookie> deleteCookies = getCookies(url);

        mapService.execute(() -> {
            for (okhttp3.Cookie cookie : deleteCookies) {
                Cookie c = map.remove(getCookieKey(cookie));
                if (cookieDao != null && c != null) {
                    databaseService.execute(() -> cookieDao.delete(c));
                }
            }
        });
    }

    /**
     * 清除所有cookies
     */
    public void clearCookies() {
        mapService.execute(() -> {
            for (Map.Entry<String, Cookie> entry : map.entrySet()) {
                Cookie cookie = entry.getValue();
                if (cookieDao != null && cookie != null) {
                    databaseService.execute(() -> cookieDao.delete(cookie));
                }
            }

            map.clear();
        });
    }

    /**
     * @return 该cookie信息在CookieMap中的key值
     */
    public String getCookieKey(@NonNull Cookie cookie) {
        return cookie.domain + cookie.path + cookie.name;
    }

    /**
     * @return 该cookie信息在CookieMap中的key值
     */
    public String getCookieKey(@NonNull okhttp3.Cookie cookie) {
        return cookie.domain() + cookie.path() + cookie.name();
    }

    public Map<String, Cookie> getMap() {
        return map;
    }

    /**
     * 设置新的记录Cookies的Map并返回九的Map
     *
     * @param map 新的cookies
     * @return 旧的cookies
     */
    public Map<String, Cookie> setMap(@NonNull Map<String, Cookie> map) {
        Map<String, Cookie> tmp = getMap();
        this.map = map;
        return tmp;
    }


    /**
     * 设置新的记录Cookies的Map并返回九的Map
     *
     * @param map                新的cookies
     * @param needSaveToDatabase 是否需要将cookies信息存进本地
     * @return 旧的cookies
     */
    public Map<String, Cookie> setMap(@NonNull Map<String, Cookie> map, boolean needSaveToDatabase) {
        setNeedSaveToDatabase(needSaveToDatabase);
        return setMap(map);
    }

    public void setNeedSaveToDatabase(boolean needSaveToDatabase) {
        this.needSaveToDatabase = needSaveToDatabase;
    }
}
