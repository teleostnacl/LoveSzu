package com.teleostnacl.common.android.view.edittext;

/**
 * 关联View的接口
 */
public interface IRelationView {
    /**
     * 判断所触摸位置的坐标是否在关联的View中
     *
     * @param rawX 触摸位置的X坐标
     * @param rawY 触摸位置的Y坐标
     * @return 触摸位置的坐标是否在关联的View中
     */
    boolean isInRelation(int rawX, int rawY);
}
