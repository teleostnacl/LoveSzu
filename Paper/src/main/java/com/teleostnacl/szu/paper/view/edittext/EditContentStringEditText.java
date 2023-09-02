package com.teleostnacl.szu.paper.view.edittext;

import android.content.Context;
import android.text.Editable;
import android.text.Layout;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.teleostnacl.common.android.view.edittext.spanwatcher.SpanIntegerSpanWatcher;
import com.teleostnacl.szu.paper.view.edittext.spannable.QuotationSpannable;

/**
 * 编辑论文正文所使用的EditText
 */
public class EditContentStringEditText extends EditContentSimpleTextEditText {

    /**
     * 使引用变为整体性的Span, 禁止从中间选择
     */
    private static final Editable.Factory factory = new Editable.Factory() {
        @Override
        @SuppressWarnings("rawtypes")
        public Editable newEditable(CharSequence source) {
            SpannableStringBuilder spannableStringBuilder;
            if (source instanceof SpannableStringBuilder) {
                spannableStringBuilder = (SpannableStringBuilder) source;
            } else {
                spannableStringBuilder = new SpannableStringBuilder(source);
            }

            // 移除旧的SpanIntegerSpanWatcher
            SpanIntegerSpanWatcher[] spans = spannableStringBuilder.getSpans(
                    0, spannableStringBuilder.length(), SpanIntegerSpanWatcher.class);
            if (spans != null) {
                for (SpanIntegerSpanWatcher span : spans) {
                    spannableStringBuilder.removeSpan(span);
                }
            }

            // 添加新的QuoteIntegerSpan
            spannableStringBuilder.setSpan(new SpanIntegerSpanWatcher<>(QuotationSpannable.class), 0, source.length(),
                    Spanned.SPAN_INCLUSIVE_INCLUSIVE | Spanned.SPAN_PRIORITY);

            return spannableStringBuilder;
        }
    };

    public EditContentStringEditText(@NonNull Context context) {
        super(context);
        init();
    }

    public EditContentStringEditText(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public EditContentStringEditText(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setEditableFactory(factory);
        setOnTouchListener(new ClickableSpanTouchListener());
    }

    public static class ClickableSpanTouchListener implements View.OnTouchListener {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (!(v instanceof TextView)) {
                return false;
            }
            TextView widget = (TextView) v;
            CharSequence text = widget.getText();
            if (!(text instanceof Spanned)) {
                return false;
            }
            int action = event.getAction();
            if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_DOWN) {
                int index = getTouchedIndex(widget, event);
                QuotationSpannable link = getQuotationSpannableByIndex(widget, index);
                if (link != null) {
                    if (action == MotionEvent.ACTION_UP) {
                        link.onClick(widget);
                    }
                    return true;
                }
            }
            return false;
        }

        public static QuotationSpannable getQuotationSpannableByIndex(TextView widget, int index) {
            if (widget == null || index < 0) {
                return null;
            }
            CharSequence charSequence = widget.getText();
            if (!(charSequence instanceof Spanned)) {
                return null;
            }
            Spanned buffer = (Spanned) charSequence;
            // end 应该是 index + 1，如果也是 index，得到的结果会往左偏
            QuotationSpannable[] links = buffer.getSpans(index, index + 1, QuotationSpannable.class);
            if (links != null && links.length > 0) {
                return links[0];
            }
            return null;
        }

        public static int getTouchedIndex(TextView widget, MotionEvent event) {
            if (widget == null || event == null) {
                return -1;
            }
            int x = (int) event.getX();
            int y = (int) event.getY();

            x -= widget.getTotalPaddingLeft();
            y -= widget.getTotalPaddingTop();

            x += widget.getScrollX();
            y += widget.getScrollY();

            Layout layout = widget.getLayout();
            // 根据 y 得到对应的行 line
            int line = layout.getLineForVertical(y);
            // 判断得到的 line 是否正确
            if (x < layout.getLineLeft(line) || x > layout.getLineRight(line)
                    || y < layout.getLineTop(line) || y > layout.getLineBottom(line)) {
                return -1;
            }
            // 根据 line 和 x 得到对应的下标
            int index = layout.getOffsetForHorizontal(line, x);
            // 这里考虑省略号的问题，得到真实显示的字符串的长度，超过就返回 -1
            int showedCount = widget.getText().length() - layout.getEllipsisCount(line);
            if (index > showedCount) {
                return -1;
            }
            // getOffsetForHorizontal 获得的下标会往右偏
            // 获得下标处字符左边的左边，如果大于点击的 x，就可能点的是前一个字符
            if (layout.getPrimaryHorizontal(index) > x) {
                index -= 1;
            }
            return index;
        }
    }
}
