package com.teleostnacl.szu.electricity.repository;

import static com.teleostnacl.szu.electricity.model.ElectricityModel.LIHU_BUILDING;

import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.teleostnacl.common.android.context.ResourcesUtils;
import com.teleostnacl.common.java.util.NumberUtils;
import com.teleostnacl.common.android.utils.TimeUtils;
import com.teleostnacl.szu.electricity.R;
import com.teleostnacl.szu.electricity.database.ElectricityBuyingDao;
import com.teleostnacl.szu.electricity.database.ElectricityDatabase;
import com.teleostnacl.szu.electricity.database.ElectricityDateDao;
import com.teleostnacl.szu.electricity.model.ElectricityBuyingModel;
import com.teleostnacl.szu.electricity.model.ElectricityDateModel;
import com.teleostnacl.szu.electricity.model.ElectricityModel;
import com.teleostnacl.szu.electricity.model.LiHuRemainModel;
import com.teleostnacl.szu.electricity.retrofit.ElectricityApi;
import com.teleostnacl.szu.electricity.retrofit.ElectricityRetrofit;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.subjects.SingleSubject;

public class ElectricityRepository {
    private final ElectricityDateDao electricityDateDao = ElectricityDatabase.getInstance().electricityDateDao();
    private final ElectricityBuyingDao electricityBuyingDao = ElectricityDatabase.getInstance().electricityBuyingDao();

    private final ElectricityApi api = ElectricityRetrofit.getInstance().getApi();

    // 使用单线程处理数据库的读写操作
    private final ExecutorService dataBaseExecutorService = Executors.newSingleThreadExecutor();

    /**
     * 检查是否可联通电费查询系统, 并获取所有校区信息
     *
     * @return 是否可联通电费查询系统
     */
    public Single<Boolean> check(@NonNull ElectricityModel electricityModel) {
        electricityModel.clientMap.clear();
        return api.check().map(responseBody -> {
            Document document = Jsoup.parse(responseBody.string());

            // 获取校区的ip地址
            Element element = document.select("[name=client]").first();
            for (Element element1 : Objects.requireNonNull(element).children()) {
                electricityModel.clientMap.put(element1.text(), element1.val());
            }

            // 当到达此处 则表示能连同校园电费系统, 添加丽湖二期学生宿舍
            electricityModel.clientMap.put(ResourcesUtils.getString(R.string.electricity_set_room_lihu_building), LIHU_BUILDING);

            return true;
        });
    }

    /**
     * 获取楼栋信息
     *
     * @param electricityModel 电费系统模型
     * @return 是否成功
     */
    public Single<Boolean> getBuilding(@NonNull ElectricityModel electricityModel) {
        // 清除已获取的宿舍楼信息
        electricityModel.buildingIdMap.clear();

        // 所选择的是丽湖二期学生宿舍
        if (electricityModel.isLiHuSecond()) {
            return api.getLiHuSecondBuilding().map(responseBody -> {
                Document document = Jsoup.parse(responseBody.string());

                // 更新请求信息
                updateLiHuSecondFieldMap(electricityModel, document);

                // 获取楼栋名和id
                Element element = document.getElementById("drlouming");
                for (int i = 1; i < Objects.requireNonNull(element).children().size(); i++) {
                    String title = element.child(i).text();
                    if (title.endsWith("#")) {
                        title = title.substring(0, title.length() - 1);
                    }
                    electricityModel.buildingIdMap.put(title, element.child(i).val());
                }

                return true;
            });
        }

        // 非丽湖二期学生宿舍
        return api.getBuildingId(electricityModel.client).map(responseBody -> {
            Document document = Jsoup.parse(responseBody.string());

            // 获取楼栋的id
            Element element = document.select("[name=buildingId]").first();
            for (int i = 1; i < Objects.requireNonNull(element).children().size(); i++) {
                electricityModel.buildingIdMap.put(element.child(i).text(), element.child(i).val());
            }

            return true;
        });
    }

    /**
     * 检查房间号是否正确
     *
     * @param electricityModel 电费系统模型
     * @return 是否正确
     */
    public Single<Boolean> checkRoomId(@NonNull ElectricityModel electricityModel) {
        // 丽湖二期宿舍
        if (electricityModel.isLiHuSecond()) {
            return checkLiHuSecondRoomId(electricityModel);
        }

        // 检查粤海校区 丽湖宿舍一期 并获取roomId
        return api.checkRoomId(electricityModel.getCheckRoomIdQueryMap()).map(responseBody -> {
            // 房间号正确 则会跳转到查询页面, 此时含有roomId的attr
            Element element = Jsoup.parse(responseBody.string()).select("[name=roomId]").first();
            if (element == null) {
                return false;
            }

            // 记录房间号(系统中的id)
            electricityModel.roomId = element.val();
            return true;
        });
    }

    /**
     * 检查西丽二期宿舍的房间号是否有效
     *
     * @param electricityModel 电费系统模型
     * @return 是否正确
     */
    private Single<Boolean> checkLiHuSecondRoomId(@NonNull ElectricityModel electricityModel) {
        return Single.just(electricityModel).flatMap(electricityModel1 -> {
                    // 若参数信息不全 则进行获取参数
                    if (electricityModel1.__EVENTARGUMENT == null ||
                            electricityModel1.__LASTFOCUS == null ||
                            electricityModel1.__VIEWSTATE == null ||
                            electricityModel1.__VIEWSTATEGENERATOR == null) {
                        return api.getLiHuSecondBuilding().map(responseBody -> {
                            Document document = Jsoup.parse(responseBody.string());

                            // 更新请求信息
                            updateLiHuSecondFieldMap(electricityModel, document);

                            return electricityModel;
                        });
                    }
                    return Single.just(electricityModel);
                }).
                flatMap(aBoolean -> api.checkLiHuSecondRoom(electricityModel.getLiHuCheckRoomIdQueryMap(ElectricityModel.LIHU_drlouming))
                        .flatMap(responseBody -> {
                            Document document = Jsoup.parse(responseBody.string());

                            // 更新请求信息
                            updateLiHuSecondFieldMap(electricityModel, document);

                            return api.checkLiHuSecondRoom(electricityModel.getLiHuCheckRoomIdQueryMap(ElectricityModel.LIHU_ablou));
                        })
                        .flatMap(responseBody -> {
                            Document document = Jsoup.parse(responseBody.string());

                            // 更新请求信息
                            updateLiHuSecondFieldMap(electricityModel, document);

                            return api.checkLiHuSecondRoom(electricityModel.getLiHuCheckRoomIdQueryMap(ElectricityModel.LIHU_drceng));

                        })
                        .map(responseBody -> {
                            Document document = Jsoup.parse(responseBody.string());

                            // 更新请求信息
                            updateLiHuSecondFieldMap(electricityModel, document);

                            // 检查房间号是否有效(即检查每一个id的selected的value是否都不为空)
                            String[] keys = new String[]{"drlouming", "ablou", "drceng"};
                            for (String key : keys) {
                                Element element = document.getElementById(key);
                                if (element == null) {
                                    return false;
                                }
                                Elements selected = element.getElementsByAttributeValue("selected", "selected");

                                if (TextUtils.isEmpty(selected.val())) {
                                    return false;
                                }
                            }

                            return true;
                        }));
    }

    /**
     * 获取一个月的记录
     *
     * @param date 年+月组成的数字 如 202212
     * @return 记录
     */
    public Single<List<ElectricityDateModel>> getMonthData(ElectricityModel electricityModel,
                                                           int date) {

        // 除100 取整数 为年
        int year = date / 100;
        // 除100 取余数 为月
        int month = date % 100;

        return getMonthDataFromDatabase(year, month).flatMap(electricityDateModels -> {
            // 强制使用本地时, 直接返回结果
            if (electricityModel.useLocal) {
                return Single.just(electricityDateModels);
            } else if (electricityModel.getLocalDate() > date && electricityDateModels.size() != 0) {
                // 获取的年月刚好与本地的相同 为了避免不能更新数据库, 此时应该从网络获取
                return Single.just(electricityDateModels);
            }

            // 从网络获取结果
            return electricityModel.isLiHuSecond() ?
                    getLiHuSecondMonthDataFromNetwork(electricityModel, year, month) :
                    getMonthDataFromNetwork(electricityModel, year, month);
        });
    }

    /**
     * 从数据库中获取一个月的记录
     *
     * @param year  年
     * @param month 月
     * @return 一个月的记录
     */
    public Single<List<ElectricityDateModel>> getMonthDataFromDatabase(int year, int month) {
        SingleSubject<List<ElectricityDateModel>> subject = SingleSubject.create();

        dataBaseExecutorService.execute(() -> {
            List<ElectricityDateModel> list = electricityDateDao.getElectricityDateModel(year, month);
            if (list == null || list.size() == 0) {
                subject.onSuccess(new ArrayList<>());
                return;
            }

            for (ElectricityDateModel model : list) {
                // 获取购电记录
                List<ElectricityBuyingModel> dateModels = electricityBuyingDao.getElectricityBuyModel(year, month, model.day);

                if (dateModels != null && dateModels.size() != 0) {
                    model.buyingModelList = dateModels;
                }
            }

            subject.onSuccess(list);
        });

        return subject;
    }

    /**
     * 从互联网中获取粤海 丽湖一期宿舍一个月的记录
     *
     * @param electricityModel 电费系统模型
     * @param year             年
     * @param month            月
     * @return 电费记录
     */
    private Single<List<ElectricityDateModel>> getMonthDataFromNetwork(ElectricityModel electricityModel, int year, int month) {
        String monthString = String.format(Locale.CHINA, "%02d", month);

        return Single.zip(
                //获取1-15号的用电记录(获取上一月最后一天的用电记录 计算当月第一天当天的用电量)
                getUsingData(electricityModel,
                        TimeUtils.getLastMonthLastDay(year, month, "%04d-%02d-%02d"),
                        year + "-" + monthString + "-" + "15"),

                //获取16-月底的用电记录
                getUsingData(electricityModel,
                        year + "-" + monthString + "-" + "16",
                        TimeUtils.getMonthLastDay(year, month, "%04d-%02d-%02d")),

                //获取1-15号的购电记录
                getBuyingData(electricityModel,
                        year + "-" + monthString + "-" + "01",
                        year + "-" + monthString + "-" + "15"),

                //获取16-月底的够电记录
                getBuyingData(electricityModel,
                        year + "-" + monthString + "-" + "16",
                        TimeUtils.getMonthLastDay(year, month, "%04d-%02d-%02d")),

                //进行组合
                (electricityDateModels, electricityDateModels2, electricityBuyingModels, electricityBuyingModels2) -> {
                    electricityDateModels.addAll(electricityDateModels2);

                    //遍历electricityDateModels,设置每天的使用量
                    float lastDayUsingSum = 0;
                    for (ElectricityDateModel model : electricityDateModels) {
                        model.using = model.usingSum - lastDayUsingSum;

                        lastDayUsingSum = model.usingSum;
                    }

                    electricityBuyingModels.addAll(electricityBuyingModels2);

                    // 无数据, 结束
                    if (electricityDateModels.size() == 0) {
                        return new ArrayList<>();
                    }

                    //去掉第一个 即上月的末尾 并反序
                    List<ElectricityDateModel> tmp = electricityDateModels.subList(1, electricityDateModels.size());
                    Collections.reverse(tmp);

                    // 取出第一个的时间, 用于判断是否需要更新本地时间
                    int date = NumberUtils.parseInt("" + tmp.get(0).year + tmp.get(0).month, 0);
                    // 为空时 或者旧时需要更新
                    if (electricityModel.getLocalDate() < date) {
                        electricityModel.localDate.postValue(date);
                    }

                    Map<Integer, ElectricityDateModel> map = new HashMap<>();
                    for (ElectricityDateModel model : tmp) {
                        map.put(model.day, model);
                    }

                    // 遍历购电记录
                    for (ElectricityBuyingModel model : electricityBuyingModels) {
                        ElectricityDateModel dateModel = map.get(model.day);
                        // 为空 则不记录该购电记录
                        if (dateModel == null) {
                            continue;
                        }

                        // 存进数据库中
                        dataBaseExecutorService.execute(() -> electricityBuyingDao.insertOrUpdate(model));

                        // 购电记录为空, 则创建list
                        if (dateModel.buyingModelList == null) {
                            dateModel.buyingModelList = new ArrayList<>();
                        }
                        dateModel.buying += model.buyingSum;
                        dateModel.buyingModelList.add(model);
                    }

                    // 储存进数据库
                    for (ElectricityDateModel model : tmp) {
                        dataBaseExecutorService.execute(() -> electricityDateDao.insertOrUpdate(model));
                    }

                    return tmp;
                });
    }

    /**
     * @param electricityModel 电费系统模型
     * @param beginTime        开始时间
     * @param endTime          结束时间
     * @return 获取使用记录
     */
    private Single<List<ElectricityDateModel>> getUsingData(@NonNull ElectricityModel electricityModel,
                                                            String beginTime, String endTime) {
        return api.query(electricityModel.getQueryMap(ElectricityModel.USING, beginTime, endTime))
                .map(responseBody -> {
                    List<ElectricityDateModel> list = new ArrayList<>();

                    Elements elements = Objects.requireNonNull(Jsoup.parse(responseBody.string())
                            .getElementById("oTable")).select("tr");

                    for (int i = 1; i < elements.size() - 2; i++) {
                        ElectricityDateModel date = new ElectricityDateModel(elements.get(i).select("td"));
                        list.add(date);
                    }

                    return list;
                });
    }

    /**
     * @param electricityModel 电费系统模型
     * @param beginTime        开始时间
     * @param endTime          结束时间
     * @return 获取购电记录
     */
    private Single<List<ElectricityBuyingModel>> getBuyingData(@NonNull ElectricityModel electricityModel,
                                                               String beginTime, String endTime) {
        return api.query(electricityModel.getQueryMap(ElectricityModel.BUYING, beginTime, endTime))
                .map(responseBody -> {
                    List<ElectricityBuyingModel> list = new ArrayList<>();

                    Elements elements = Objects.requireNonNull(Jsoup.parse(responseBody.string())
                            .getElementById("oTable")).select("tr");

                    for (int i = 1; i < elements.size() - 2; i++) {
                        ElectricityBuyingModel date = new ElectricityBuyingModel(elements.get(i).select("td"));
                        list.add(date);
                    }

                    return list;
                });
    }

    /**
     * 从互联网中获取丽湖二期宿舍一个月的记录
     *
     * @param year  年
     * @param month 月
     * @return 一个月的记录
     */
    public Single<List<ElectricityDateModel>> getLiHuSecondMonthDataFromNetwork(
            @NonNull ElectricityModel electricityModel, int year, int month) {
        return Single.just(electricityModel).flatMap(electricityModel1 -> {
                    // 若参数为空 则进行获取参数
                    if (electricityModel.__VIEWSTATE == null ||
                            electricityModel.__VIEWSTATEGENERATOR == null ||
                            electricityModel.__EVENTVALIDATION == null) {
                        return checkLiHuSecondRoomId(electricityModel);
                    }

                    return Single.just(electricityModel);
                })
                .flatMap(o -> getLiHuSecondMonthUsingData(electricityModel, year, month).flatMap(electricityDateModels -> {
                    // 用电记录为空时 不再获取购电记录
                    if (electricityDateModels.size() == 0) {
                        return Single.just(electricityDateModels);
                    }

                    // 购电记录
                    return getLiHuSecondMonthBuyingData(electricityModel, year, month).map(electricityBuyingModels -> {
                        Map<Integer, ElectricityDateModel> map = new HashMap<>();
                        for (ElectricityDateModel model : electricityDateModels) {
                            map.put(model.day, model);
                        }

                        // 遍历购电记录
                        for (ElectricityBuyingModel model : electricityBuyingModels) {
                            ElectricityDateModel dateModel = map.get(model.day);
                            // 为空 则不记录该购电记录
                            if (dateModel == null) {
                                continue;
                            }

                            // 存进数据库中
                            dataBaseExecutorService.execute(() -> electricityBuyingDao.insertOrUpdate(model));

                            // 购电记录为空, 则创建list
                            if (dateModel.buyingModelList == null) {
                                dateModel.buyingModelList = new ArrayList<>();
                            }
                            dateModel.buying += model.buyingSum;
                            dateModel.buyingModelList.add(model);
                        }

                        // 储存进数据库
                        for (ElectricityDateModel model : electricityDateModels) {
                            dataBaseExecutorService.execute(() -> electricityDateDao.insertOrUpdate(model));
                        }

                        // 反序
                        Collections.reverse(electricityDateModels);

                        // 取出第一个的时间, 用于判断是否需要更新本地时间
                        int date = NumberUtils.parseInt("" + electricityDateModels.get(0).year + electricityDateModels.get(0).month, 0);
                        // 为空时 或者旧时需要更新
                        if (electricityModel.getLocalDate() < date) {
                            electricityModel.localDate.postValue(date);
                        }

                        return electricityDateModels;
                    });
                }));
    }


    /**
     * 获取丽湖二期宿舍指定年月的用电记录
     *
     * @param electricityModel 电费系统模型
     * @param year             指定年
     * @param month            指定月
     * @return 用电记录
     */
    private Single<List<ElectricityDateModel>> getLiHuSecondMonthUsingData(
            @NonNull ElectricityModel electricityModel, int year, int month) {
        String monthString = String.format(Locale.CHINA, "%02d", month);

        // 切换到查询购电记录
        return api.checkLiHuSecondRoom(electricityModel.getLiHuSecondSwitch(true))
                .map(responseBody -> {
                    Document document = Jsoup.parse(responseBody.string());

                    // 获取原始剩余的电量信息
                    LiHuRemainModel origin = electricityModel.lihuRemain.getValue();
                    // 获取剩余的电量信息
                    LiHuRemainModel liHuRemainModel = new LiHuRemainModel(
                            document.getElementsByClass("crumb").text());
                    // 如果两者不相等 则更新剩余的电量信息
                    if (!liHuRemainModel.equals(origin)) {
                        electricityModel.lihuRemain.postValue(liHuRemainModel);
                    }

                    // 更新请求表单信息
                    electricityModel.__VIEWSTATE = Objects.requireNonNull(
                            document.getElementById("__VIEWSTATE")).val();
                    electricityModel.__VIEWSTATEGENERATOR = Objects.requireNonNull(
                            document.getElementById("__VIEWSTATEGENERATOR")).val();
                    electricityModel.__EVENTVALIDATION = Objects.requireNonNull(
                            document.getElementById("__EVENTVALIDATION")).val();

                    return electricityModel;
                })
                // 获取1到9号的用电记录
                .flatMap(o -> getLiHuSecondUsingData(electricityModel,
                        year + "-" + monthString + "-" + "1",
                        year + "-" + monthString + "-" + "9"))
                // 获取10号到18号的用电记录
                .flatMap(electricityDateModels -> getLiHuSecondUsingData(electricityModel,
                        year + "-" + monthString + "-" + "10",
                        year + "-" + monthString + "-" + "18")
                        .map(electricityDateModels1 -> {
                            // 合并
                            electricityDateModels.addAll(electricityDateModels1);
                            return electricityDateModels;
                        }))
                // 获取19号到27号的用电记录
                .flatMap(electricityDateModels -> getLiHuSecondUsingData(electricityModel,
                        year + "-" + monthString + "-" + "19",
                        year + "-" + monthString + "-" + "27")
                        .map(electricityDateModels1 -> {
                            // 合并
                            electricityDateModels.addAll(electricityDateModels1);
                            return electricityDateModels;
                        }))
                // 获取27号到月底的用电记录
                .flatMap(electricityDateModels -> getLiHuSecondUsingData(electricityModel,
                        year + "-" + monthString + "-" + "28",
                        TimeUtils.getMonthLastDay(year, month, "%04d-%02d-%02d"))
                        .map(electricityDateModels1 -> {
                            // 合并
                            electricityDateModels.addAll(electricityDateModels1);
                            return electricityDateModels;
                        }));
    }

    /**
     * 获取丽湖二期宿舍指定年月的购电记录
     *
     * @param electricityModel 电费系统模型
     * @param year             指定年
     * @param month            指定月
     * @return 购电记录
     */
    private Single<List<ElectricityBuyingModel>> getLiHuSecondMonthBuyingData(
            @NonNull ElectricityModel electricityModel, int year, int month) {
        String monthString = String.format(Locale.CHINA, "%02d", month);

        // 切换到查询购电记录
        return api.checkLiHuSecondRoom(electricityModel.getLiHuSecondSwitch(false))
                .map(responseBody -> {
                    Document document = Jsoup.parse(responseBody.string());

                    // 更新请求表单信息
                    electricityModel.__VIEWSTATE = Objects.requireNonNull(
                            document.getElementById("__VIEWSTATE")).val();
                    electricityModel.__VIEWSTATEGENERATOR = Objects.requireNonNull(
                            document.getElementById("__VIEWSTATEGENERATOR")).val();
                    electricityModel.__EVENTVALIDATION = Objects.requireNonNull(
                            document.getElementById("__EVENTVALIDATION")).val();

                    return electricityModel;
                })
                // 获取1到9号的购电记录
                .flatMap(o -> getLiHuSecondBuyingData(electricityModel,
                        year + "-" + monthString + "-" + "1",
                        year + "-" + monthString + "-" + "9"))
                // 获取10到18号的购电记录
                .flatMap(electricityBuyingModels -> getLiHuSecondBuyingData(electricityModel,
                        year + "-" + monthString + "-" + "10",
                        year + "-" + monthString + "-" + "18")
                        .map(electricityBuyingModels1 -> {
                            electricityBuyingModels.addAll(electricityBuyingModels1);
                            return electricityBuyingModels;
                        }))
                // 获取19到27号的购电记录
                .flatMap(electricityBuyingModels -> getLiHuSecondBuyingData(electricityModel,
                        year + "-" + monthString + "-" + "19",
                        year + "-" + monthString + "-" + "27")
                        .map(electricityBuyingModels1 -> {
                            electricityBuyingModels.addAll(electricityBuyingModels1);
                            return electricityBuyingModels;
                        }))
                // 获取28到月底的购电记录
                .flatMap(electricityBuyingModels -> getLiHuSecondBuyingData(electricityModel,
                        year + "-" + monthString + "-" + "28",
                        TimeUtils.getMonthLastDay(year, month, "%04d-%02d-%02d"))
                        .map(electricityBuyingModels1 -> {
                            electricityBuyingModels.addAll(electricityBuyingModels1);
                            return electricityBuyingModels;
                        }));
    }

    /**
     * @param electricityModel 电费系统模型
     * @param beginTime        开始时间
     * @param endTime          结束时间
     * @return 获取丽湖二期宿舍的用电记录
     */
    private Single<List<ElectricityDateModel>> getLiHuSecondUsingData(
            @NonNull ElectricityModel electricityModel, String beginTime, String endTime) {
        return api.queryLiHuSecondUsing(electricityModel.getLiHuSecondFieldMap(beginTime, endTime)).map(responseBody -> {
            Document document = Jsoup.parse(responseBody.string());

            Elements elements = document.getElementsByClass("contentLine");

            List<ElectricityDateModel> list = new ArrayList<>();
            for (Element element : elements) {
                ElectricityDateModel model = new ElectricityDateModel();

                String[] date = element.child(0).text().split("-");
                model.year = NumberUtils.parseInt(date[0], 0);
                model.month = NumberUtils.parseInt(date[1], 0);
                model.day = NumberUtils.parseInt(date[2], 0);

                model.using = NumberUtils.parseFloat(element.child(2).text(), 0);

                list.add(model);
            }

            Collections.reverse(list);

            return list;
        });
    }

    /**
     * @param electricityModel 电费系统模型
     * @param beginTime        开始时间
     * @param endTime          结束时间
     * @return 获取丽湖二期宿舍的购电记录
     */
    private Single<List<ElectricityBuyingModel>> getLiHuSecondBuyingData(
            @NonNull ElectricityModel electricityModel, String beginTime, String endTime) {
        return api.queryLiHuSecondBuying(electricityModel.getLiHuSecondFieldMap(beginTime, endTime)).map(responseBody -> {
            Document document = Jsoup.parse(responseBody.string());

            Elements elements = document.getElementsByClass("contentLine");

            List<ElectricityBuyingModel> list = new ArrayList<>();

            for (Element element : elements) {
                ElectricityBuyingModel model = new ElectricityBuyingModel();

                String[] date = element.child(0).text().split(" ");

                String[] day = date[0].split("/");
                model.year = NumberUtils.parseInt(day[0], 0);
                model.month = NumberUtils.parseInt(day[1], 0);
                model.day = NumberUtils.parseInt(day[2], 0);

                model.time = date[1];

                model.buyingSum = NumberUtils.parseFloat(element.child(2).text(), 0);
                model.buyingMoney = NumberUtils.parseFloat(element.child(3).text(), 0);
                model.buyingPerson = element.child(4).text();

                list.add(model);
            }


            return list;
        });
    }

    /**
     * 删除数据库的所有数据
     */
    public void deleteAll() {
        dataBaseExecutorService.execute(electricityDateDao::deleteAll);
        dataBaseExecutorService.execute(electricityBuyingDao::deleteAll);
    }

    /**
     * 更新丽湖二期宿舍的请求表单信息
     *
     * @param document         请求的结果HTML
     * @param electricityModel 电费模型
     */
    private void updateLiHuSecondFieldMap(@NonNull ElectricityModel electricityModel,
                                          @NonNull Document document) {
        // 更新请求信息
        electricityModel.__EVENTARGUMENT = Objects.requireNonNull(
                document.getElementById("__EVENTARGUMENT")).val();
        electricityModel.__LASTFOCUS = Objects.requireNonNull(
                document.getElementById("__LASTFOCUS")).val();
        electricityModel.__VIEWSTATE = Objects.requireNonNull(
                document.getElementById("__VIEWSTATE")).val();
        electricityModel.__VIEWSTATEGENERATOR = Objects.requireNonNull(
                document.getElementById("__VIEWSTATEGENERATOR")).val();
    }
}
