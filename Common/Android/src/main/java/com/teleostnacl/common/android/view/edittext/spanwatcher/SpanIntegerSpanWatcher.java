package com.teleostnacl.common.android.view.edittext.spanwatcher;

import android.text.Selection;
import android.text.SpanWatcher;
import android.text.Spannable;

/**
 * 使指定Span变成整体的SpanWatcher
 * <p>
 * 实现富文本的一个核心类
 */
public class SpanIntegerSpanWatcher<T> implements SpanWatcher {

    // 记录T.class
    private final Class<T> clz;

    public SpanIntegerSpanWatcher(Class<T> clz) {
        this.clz = clz;
    }

    @Override
    public void onSpanAdded(Spannable text, Object what, int start, int end) {

    }

    @Override
    public void onSpanRemoved(Spannable text, Object what, int start, int end) {

    }

    @Override
    public void onSpanChanged(Spannable text, Object what, int ostart, int oend, int nstart, int nend) {

        // 非光标改变
        if (what != Selection.SELECTION_START && what != Selection.SELECTION_END) {
            return;
        }

        /*
         * 光标不占字符位, 所以对于光标的改变(what == Selection.SELECTION_START  | what == Selection.SELECTION_END),
         * 恒有ostart == oend, nstart == nend
         */

        // 前后无变化, 则表示光标位置未变 不处理
        if (ostart == nstart) {
            return;
        }

        // 获取当前光标开始和结束的位置
        int selectionStartIndex = Selection.getSelectionStart(text);
        int selectionEndIndex = Selection.getSelectionEnd(text);

        // 获取选择范围内的Spans
        T[] spans = text.getSpans(nstart, nend, clz);
        if (spans == null || spans.length == 0 || spans[0] == null) {
            return;
        }

        // 获取Span开始和结束的位置
        int startIndex = text.getSpanStart(spans[0]);
        int endIndex = text.getSpanEnd(spans[0]);

        // 改变光标开始位置
        if (what == Selection.SELECTION_START) {
            // Span开始或结束位置与光标开始位置相同 不处理
            if (startIndex == selectionStartIndex || endIndex == selectionStartIndex) {
                return;
            }

            // 开始光标的原位置与结束光标的位置相同, 则表示处于输入模式, 同时改变头和尾
            if (ostart == selectionEndIndex) {
                // 左光标向右移恰好进入ClickableSpan的内容, 则使光标移到Span的末尾
                if (nstart > ostart && startIndex <= nstart && endIndex >= ostart) {
                    Selection.setSelection(text, endIndex, endIndex);
                } else {
                    // 移动到头
                    Selection.setSelection(text, startIndex, startIndex);
                }
            } else {
                // 选择状态 只改变头
                Selection.setSelection(text, startIndex, selectionEndIndex);
            }
        }
        // 改变光标结束位置
        else {
            // Span开始或结束位置与光标结束位置相同 不处理
            if (startIndex == selectionEndIndex || endIndex == selectionEndIndex) {
                return;
            }

            // 输入状态 不处理, 其在what == Selection.SELECTION_START已处理完成
            if (selectionStartIndex == selectionEndIndex) {
                return;
            }

            // 右光标向左移, 若结束位置落在ClickableSpan范围中, 则将光标移到到前面
            if (nstart < ostart && startIndex <= nstart && endIndex >= nstart) {
                Selection.setSelection(text, selectionStartIndex, startIndex);
            }
            // 选择状态 只改变尾 将光标移到末尾
            else {
                Selection.setSelection(text, selectionStartIndex, endIndex);
            }
        }
    }
}
