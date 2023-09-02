package com.teleostnacl.szu.bulletin.model;

import android.text.Html;
import android.text.Spanned;

public class BulletinContentModel {
    public String content;

    public boolean rtl = false;

    public Spanned   getContent() {
        return Html.fromHtml(content, Html.FROM_HTML_MODE_COMPACT);
    }
}
