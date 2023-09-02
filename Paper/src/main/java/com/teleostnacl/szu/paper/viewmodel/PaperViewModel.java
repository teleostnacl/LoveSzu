package com.teleostnacl.szu.paper.viewmodel;

import androidx.lifecycle.ViewModel;

import com.teleostnacl.szu.paper.model.PaperModel;

public class PaperViewModel extends ViewModel {
    /**
     * 正在编辑的论文
     */
    private PaperModel paperModel = new PaperModel();

    public PaperModel getPaperModel() {
        return paperModel;
    }

    public void setPaperModel(PaperModel paperModel) {
        this.paperModel = paperModel;
    }
}
