package com.teleostnacl.common.android.view.edittext;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;

/**
 * 移除回车符的TextWatcher
 */
public class RemoveEnterTextWatcher implements TextWatcher {

    // 标记输入的回车的起始位置
    private int replaceStart = -1;
    private int replaceEnd = -1;

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(@NonNull CharSequence s, int start, int before, int count) {
        Log.d("TAG", "onTextChanged() called with: s = [" + s + "], start = [" + start + "], before = [" + before + "], count = [" + count + "]");
        String s1 = s.subSequence(start, start + count).toString();

        // 输入的为回车 标记删除
        if ("\r\n".equals(s1) || "\r".equals(s1) || "\n".equals(s1)) {
            replaceStart = start;
            replaceEnd = start + count;
        }
    }

    @Override
    @CallSuper
    public void afterTextChanged(Editable s) {
        Log.d("TAG", "afterTextChanged() called with: s = [" + s + "]");
        if (s == null) {
            return;
        }


        // 删除输入的回车
        if (replaceStart != -1 && replaceEnd != -1 && replaceEnd >= replaceStart && replaceEnd <= s.length()) {
            StringBuilder stringBuilder = new StringBuilder(s);
            stringBuilder.replace(replaceStart, replaceEnd, "");
            replaceStart = replaceEnd = -1;
            s.clear();
            s.append(stringBuilder.toString());
            return;
        }
        replaceStart = replaceEnd = -1;

        String s1 = s.toString();
        // 去除回车符
        if (s1.contains("\r\n") || s1.contains("\r") || s1.contains("\n")) {
            s.clear();
            s.append(s1.replaceAll("\r\n|\r|\n", " "));
        }
    }
}
