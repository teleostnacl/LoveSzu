package com.teleostnacl.szu.grade.viewmodel;

import androidx.lifecycle.ViewModel;
import androidx.recyclerview.widget.RecyclerView;

import com.teleostnacl.common.android.network.NetworkUtils;
import com.teleostnacl.szu.grade.model.GradesModel;
import com.teleostnacl.szu.grade.repository.GradeRepository;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Single;

public class GradeViewModel extends ViewModel {
    // 显示成绩的公共recyclerviewPool
    public final RecyclerView.RecycledViewPool recyclerViewPool = new RecyclerView.RecycledViewPool();

    public static final int GRADE_AVERAGE_VIEW_TYPE = 1;
    public static final int GRADE_VIEW_TYPE = 2;


    private final GradeRepository gradeRepository = new GradeRepository();

    private final GradesModel gradesModel = new GradesModel();

    public GradeViewModel() {
        // 初始化recyclerViewPool
        recyclerViewPool.setMaxRecycledViews(GRADE_VIEW_TYPE, 10);
    }

    public Single<Boolean> getAllGrade() {
        return gradeRepository.getAllGrade(gradesModel)
                .onErrorReturn(throwable -> {
                    NetworkUtils.errorHandle(throwable);
                    return false;
                })
                .observeOn(AndroidSchedulers.mainThread());
    }

    public GradesModel getGradesModel() {
        return gradesModel;
    }

    @Override
    protected void onCleared() {
        super.onCleared();

        recyclerViewPool.clear();
    }
}
