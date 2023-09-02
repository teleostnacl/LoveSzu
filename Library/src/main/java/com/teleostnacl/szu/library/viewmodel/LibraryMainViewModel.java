package com.teleostnacl.szu.library.viewmodel;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModel;

import com.teleostnacl.common.android.network.NetworkUtils;
import com.teleostnacl.common.android.context.ColorResourcesUtils;
import com.teleostnacl.common.java.util.NumberUtils;
import com.teleostnacl.szu.library.fragment.LibraryMainFragment;
import com.teleostnacl.szu.library.model.TitleAndAuthorModel;
import com.teleostnacl.szu.library.model.main.LibraryMainModel;
import com.teleostnacl.szu.library.repository.LibraryMainRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;

public class LibraryMainViewModel extends ViewModel {

    public final LibraryMainModel libraryMainModel = new LibraryMainModel();

    public final LibraryMainRepository libraryMainRepository = new LibraryMainRepository();

    private final int[] textColor;

    // 当前展示热门所用的models
    private List<LibraryMainFragment.HotSearchModel> hotSearchModelList;
    private List<TitleAndAuthorModel> hotLoanModelList;

    public LibraryMainViewModel() {
        // 初始化字体颜色
        textColor = ColorResourcesUtils.getDeepColors();
    }

    /**
     * 初始化
     *
     * @return 初始化是否成功
     */
    public Observable<Boolean> init() {
        // 当两个不为空时 表示已经初始化完成
        if (hotSearchModelList != null && hotLoanModelList != null) {
            return Observable.just(true);
        }

        return libraryMainRepository.getForm().flatMap(o ->
                        libraryMainRepository.getHotSearchAndRankAndCollection(libraryMainModel))
                .onErrorReturn(throwable -> {
                    NetworkUtils.errorHandle(throwable);
                    return false;
                })
                .observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * @return 获取12条热门搜索关键字 并随机排序
     */
    public List<LibraryMainFragment.HotSearchModel> getHotSearch(boolean refresh) {
        if (refresh || hotSearchModelList == null) {
            hotSearchModelList = new ArrayList<>(textColor.length);
            // 原始数据为空时
            if (libraryMainModel.hotSearchList.size() == 0) {
                return hotSearchModelList;
            } else {
                List<Integer> textRandomList = NumberUtils.getDifferentRandom(
                        0, libraryMainModel.hotSearchList.size(), textColor.length);
                List<Integer> textColorRandomList =
                        NumberUtils.getOrderDifferentRandom(0, textColor.length);

                for (int i = 0; i < textColor.length; i++) {
                    hotSearchModelList.add(new LibraryMainFragment.HotSearchModel(
                            libraryMainModel.hotSearchList.get(textRandomList.get(i)),
                            textColor[textColorRandomList.get(i)]));
                }
            }
        }

        return hotSearchModelList;
    }

    /**
     * @return 获取热门的借阅与收藏
     */
    public List<TitleAndAuthorModel> getHotLoan(boolean refresh) {
        if (refresh || hotLoanModelList == null) {
            hotLoanModelList = new ArrayList<>(50);

            // 原始数据为空时
            if (libraryMainModel.hotLoanList.size() == 0) {
                return hotLoanModelList;
            } else {
                List<Integer> textColorRandomList = NumberUtils.getDifferentRandom(
                        0, libraryMainModel.hotLoanList.size(), 50);

                Random random = new Random();

                for (int i : textColorRandomList) {
                    hotLoanModelList.add(new TitleAndAuthorModel(libraryMainModel.hotLoanList.get(i),
                            new Drawable() {
                                private final Paint paint = new Paint();

                                @Override
                                public void draw(@NonNull Canvas canvas) {
                                    float width = getBounds().width();
                                    float height = getBounds().height();
                                    float length = Math.min(width, height) / 2;
                                    // 设置随机颜色
                                    paint.setColor(textColor[random.nextInt(textColor.length)]);
                                    paint.setAntiAlias(true);
                                    paint.setStyle(Paint.Style.STROKE);
                                    paint.setStrokeWidth(3);

                                    // 画圆
                                    canvas.drawCircle(width / 2, height / 2, length / 2, paint);
                                }

                                @Override
                                public void setAlpha(int alpha) {
                                }

                                @Override
                                public void setColorFilter(@Nullable ColorFilter colorFilter) {
                                }

                                @Override
                                public int getOpacity() {
                                    return PixelFormat.TRANSPARENT;
                                }
                            }));
                }
            }
        }

        return hotLoanModelList;
    }

    /**
     * @return 获取颜色数组
     */
    public int[] getTextColor() {
        return textColor;
    }
}
