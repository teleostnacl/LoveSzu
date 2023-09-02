package com.teleostnacl.szu.library.model.search;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;

import com.teleostnacl.common.android.context.ResourcesUtils;
import com.teleostnacl.common.android.model.KeyValueModel;
import com.teleostnacl.common.android.utils.UrlUtils;
import com.teleostnacl.szu.library.BR;
import com.teleostnacl.szu.library.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class SearchModel extends BaseObservable {

    //存储排列方式的List(题名, 出版社, 出版年, 著者, 主题词, 分类号, 进馆日期)
    public final static List<KeyValueModel> sortWayModels = Arrays.asList(
            new KeyValueModel(R.string.M_TITLE, "M_TITLE"),
            new KeyValueModel(R.string.M_PUBLISHER, "M_PUBLISHER"),
            new KeyValueModel(R.string.M_PUB_YEAR, "M_PUB_YEAR"),
            new KeyValueModel(R.string.M_AUTHOR, "M_AUTHOR"),
            new KeyValueModel(R.string.M_SUBJECT, "M_SUBJECT"),
            new KeyValueModel(R.string.M_CLC, "M_CLC"),
            new KeyValueModel(R.string.M_CATALOGDATE, "M_CATALOGDATE"));

    // 排序方向的List (倒序, 顺序)
    public final static List<KeyValueModel> sortOrientationModels = Arrays.asList(
            new KeyValueModel(R.string.search_result_DESC, "DESC"),
            new KeyValueModel(R.string.search_result_ASC, "ASC"));

    public final static List<SearchModeModel> searchModeModels = Arrays.asList(
            new SearchModeModel("anywords", R.string.ANYWORDS, R.string.ANYWORDS_tip),
            new SearchModeModel("title_f", R.string.TITLEFORWARD, R.string.TITLEFORWARD_tip),
            new SearchModeModel("title", R.string.TITLEANY, R.string.TITLEANY_tip),
            new SearchModeModel("author_f", R.string.AUTHORFORWARD, R.string.AUTHORFORWARD_tip),
            new SearchModeModel("author", R.string.AUTHORANY, R.string.AUTHORANY_tip),
            new SearchModeModel("keyword_f", R.string.KEYWORDFORWARD, R.string.KEYWORDFORWARD_tip),
            new SearchModeModel("publisher_f", R.string.PUBLISHERFORWARD, R.string.PUBLISHERFORWARD_tip),
            new SearchModeModel("clc_f", R.string.CLCFORWARD, R.string.CLCFORWARD_tip),
            new SearchModeModel("isbn_f", R.string.ISBNFORWARD, R.string.ISBNFORWARD_tip),
            new SearchModeModel("issn_f", R.string.ISSNFORWARD, R.string.ISSNFORWARD_tip),
            new SearchModeModel("callno_f", R.string.CALLNOFORWARD, R.string.CALLNOFORWARD_tip));

    // region 搜索表单
    // 一页显示的数量(最大为50)
    public static final int dp = 50;
    // 呈现方式(一定为table)
    public static final String sm = "table";
    // 检索方式
    public SearchModeModel searchModeModel = searchModeModels.get(0);
    // 检索的关键字
    public String keyword;
    // 排序类别
    public KeyValueModel sf = sortWayModels.get(2);
    // 排序方式(升序 ASC/ 降序 DESC)
    public KeyValueModel ob = sortOrientationModels.get(0);
    // 分类
    public CfModel cf;
    // 文献类型大类
    public DocumentTypeModel documentTypeModel;
    // 典藏地点
    public DeptModel dept;
    // 出版年
    public PyfModel pyf;
    // endregion

    // 搜索结果中所有的分类
    public final List<CfModel> cfModels = new ArrayList<>();
    // 搜索结果中所有的文献类型
    public final List<DocumentTypeModel> documentTypeModels = new ArrayList<>();
    // 搜索结果中所有的典藏地点
    public final List<DeptModel> deptModels = new ArrayList<>();
    // 搜索结果中所有的出版日期
    public final List<PyfModel> pyfModels = new ArrayList<>();

    // 是否仅显示可借阅的图书
    public boolean onlyShownCanLend = false;

    // 结果数
    public int resultSum;

    /**
     * 清除数据
     */
    public void clear() {
        searchModeModel = searchModeModels.get(0);
        keyword = null;
        sf = sortWayModels.get(2);
        ob = sortOrientationModels.get(0);
        cf = null;
        documentTypeModel = null;
        dept = null;
        pyf = null;
        resultSum = 0;
        cfModels.clear();
        documentTypeModels.clear();
        deptModels.clear();
        pyfModels.clear();
    }

    @Bindable
    public SearchModeModel getSearchModeModel() {
        return searchModeModel;
    }

    public void setSearchModeModel(SearchModeModel searchModeModel) {
        this.searchModeModel = searchModeModel;
        notifyPropertyChanged(BR.searchModeModel);
    }

    /**
     * @return 获取搜索请求表单, 并使用URLEncoder进行编码
     */
    public String getSearchPath(int page) {
        return "showpageforlucenesearchAjax.aspx?" + getPath(String.valueOf(page), null);
    }

    /**
     * @param reqkey reqkey
     * @return 获取搜索范围的请求表单, 并使用URLEncoder进行编码
     */
    public String getSearchFiledPath(String reqkey) {
        return "showfacetforlucenesearchAjax.aspx?" + getPath(null, reqkey);
    }

    /**
     * @param page   页码
     * @param reqkey 获取搜索范围时使用的reqkey
     * @return 获取请求链接中使用URLEncode编码部分的链接
     */
    @NonNull
    private String getPath(@Nullable String page, @Nullable String reqkey) {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append(searchModeModel.modeVal).append("=").append(keyword)
                .append("&").append("dp").append("=").append(dp)
                .append("&").append("sm").append("=").append(sm);

        putString(stringBuilder, "sf", sf);
        putString(stringBuilder, "ob", ob);
        putString(stringBuilder, "cf", cf);
        putString(stringBuilder, "documentTypeModel", documentTypeModel);
        putString(stringBuilder, "dept", dept);
        putString(stringBuilder, "pyf", pyf);
        putString(stringBuilder, "page", page);
        putString(stringBuilder, "reqkey", reqkey);

        String path = UrlUtils.encode(stringBuilder.toString(), "utf-8");

        return path + "=&_=";
    }

    /**
     * 是否将字符串添加进请求表单中
     *
     * @param stringBuilder 请求的组合url
     * @param key           请求的key
     * @param value         请求的value
     */
    private void putString(StringBuilder stringBuilder, String key, Object value) {
        if (value != null) {
            // 文献类型
            if (value instanceof DocumentTypeModel) {
                putString(stringBuilder, "cl", ((DocumentTypeModel) value).cl);
                putString(stringBuilder, "dt", ((DocumentTypeModel) value).dt);
            } else {
                String tmp = value instanceof KeyValueModel ?
                        // Key - Value Model类型
                        ((KeyValueModel) value).getValue() :
                        // 普通
                        value.toString();
                if (!TextUtils.isEmpty(tmp)) {
                    stringBuilder.append("&").append(key).append("=").append(tmp);
                }
            }
        }
    }

    // region DataBinding
    @Bindable
    public boolean isOnlyShownCanLend() {
        return onlyShownCanLend;
    }

    public void setOnlyShownCanLend(boolean onlyShownCanLend) {
        this.onlyShownCanLend = onlyShownCanLend;
        notifyPropertyChanged(BR.onlyShownCanLend);
    }

    @Bindable
    public String getResultSum() {
        return ResourcesUtils.getString(R.string.library_search_result_sum, resultSum);
    }

    public void setResultSum(int resultSum) {
        this.resultSum = resultSum;
        notifyPropertyChanged(BR.resultSum);
    }

    // endregion

    /**
     * 分类使用的Model
     */
    public static class CfModel extends KeyValueModel {

        public CfModel(String title, String value) {
            super(title, value);
        }
    }

    /**
     * 馆藏地点所使用的Model
     */
    public static class DeptModel extends KeyValueModel {

        public DeptModel(String title, String value) {
            super(title, value);
        }
    }

    /**
     * 出版年所使用的Model
     */
    public static class PyfModel extends KeyValueModel {
        public PyfModel(String title, String value) {
            super(title, value);
        }
    }

    /**
     * 文献类型使用的Model
     */
    public static class DocumentTypeModel extends KeyValueModel {
        // 文献类型大类
        public final String cl;
        // 文献类型小类
        public final String dt;

        public DocumentTypeModel(String title, String cl, String dt) {
            super(title, null);
            this.cl = cl;
            this.dt = dt;
        }

        @NonNull
        @Override
        public String toString() {
            return key;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            DocumentTypeModel that = (DocumentTypeModel) o;
            return Objects.equals(cl, that.cl) && Objects.equals(dt, that.dt);
        }

        @Override
        public int hashCode() {
            return Objects.hash(cl, dt);
        }
    }

    /**
     * 检索方式的Model
     */
    public static class SearchModeModel {
        // 方式的val
        public final String modeVal;
        // 方式的名字
        public final String modeName;
        // 方式的提示语
        public final String modeTips;

        public SearchModeModel(String modeVal, @StringRes int modeNameId, @StringRes int modeTipId) {
            this.modeVal = modeVal;
            this.modeName = ResourcesUtils.getString(modeNameId);
            this.modeTips = ResourcesUtils.getString(modeTipId);
        }
    }
}
