package com.teleostnacl.szu.paper.model;

import androidx.annotation.NonNull;
import androidx.databinding.BaseObservable;

/**
 * 内容的基类
 */
public abstract class BaseContentModel extends BaseObservable implements Cloneable {

    /**
     * 引用 类型常量
     */
    public static final int QUOTATION = -3;

    /**
     * 图片 类型常量
     */
    public static final int PICTURE = -2;

    /**
     * 一级标题 类型常量
     */
    public static final int FIRST_TITLE = 1;
    /**
     * 二级标题 类型常量
     */
    public static final int SECOND_TITLE = 2;
    /**
     * 三级标题 类型常量
     */
    public static final int THIRD_TITLE = 3;
    /**
     * 正文 类型常量
     */
    public static final int CONTENT = 4;

    /**
     * 内容类型
     */
    private int type = FIRST_TITLE;

    /**
     * 是否独立成一段
     */
    public boolean isParagraph = true;

    @NonNull
    public BaseContentModel clone(PaperModel paperModel) {
        try {
            return (BaseContentModel) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

    public int getType() {
        return type;
    }

    protected void setType(int type) {
        this.type = type;
    }
}
