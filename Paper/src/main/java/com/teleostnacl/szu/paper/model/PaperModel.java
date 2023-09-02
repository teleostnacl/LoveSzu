package com.teleostnacl.szu.paper.model;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;
import androidx.databinding.library.baseAdapters.BR;

import com.teleostnacl.common.android.context.ContextUtils;
import com.teleostnacl.common.android.context.ResourcesUtils;
import com.teleostnacl.common.java.util.ExecutorServiceUtils;
import com.teleostnacl.szu.paper.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 所需生成论文的数据
 */
@SuppressWarnings("ResultOfMethodCallIgnored")
public class PaperModel extends BaseObservable {
    private static final String FOLDER = ContextUtils.getContext().getFilesDir() + File.separator + "paper";

    private static final String PICTURE_FOLDER = "pictures";

    /**
     * 保存论文的临时文件夹
     */
    private final String fileCachePath;

    /**
     * 保存论文的文件
     */
    public File file;

    /**
     * 论文题目
     */
    public String title = "";

    /**
     * 英文题目
     */
    public String titleEn = "";

    /**
     * 论文作者
     */
    public String author = "";

    /**
     * 论文作者的专业
     */
    public String major = "";

    /**
     * 学院
     */
    public String college = "";

    /**
     * 学号
     */
    public String no = "";

    /**
     * 指导教师
     */
    public String teacher = "";

    /**
     * 职称
     */
    public String professionalQualification = "";

    /**
     * 摘要
     */
    public String abstractString = "";

    /**
     * 英文摘要
     */
    public String abstractStringEn = "";

    /**
     * 关键字
     */
    public final List<String> keywords = new ArrayList<>();

    /**
     * 英文关键词
     */
    public final List<String> keywordsEn = new ArrayList<>();

    /**
     * 内容
     */
    private final List<BaseContentModel> contents = new ArrayList<>();

    /**
     * 参考文献
     */
    public final List<QuotationModel> quotations = new ArrayList<>();

    /**
     * 致谢
     */
    public String thanks = "";

    /**
     * 创建成功的表示
     */
    private boolean createSuccess;

    /**
     * 是否自动生成图片编号
     */
    private boolean autoGeneratePictureNo = false;

    public PaperModel() {
        // 添加默认的第一条内容
        contents.add(new StringModel());

        // 创建存储临时文件的文件夹
        fileCachePath = FOLDER + File.separator + System.currentTimeMillis();
        File cacheFile = new File(fileCachePath);
        if (cacheFile.exists()) {
            cacheFile.delete();
        }
        cacheFile.mkdirs();
        // 创建存放照片的文件夹
        cacheFile = new File(getFileCachePath() + File.separator + PICTURE_FOLDER);
        cacheFile.mkdirs();
    }

    /**
     * 克隆的构造方法
     */
    public PaperModel(@NonNull PaperModel paperModel) {
        this.fileCachePath = paperModel.fileCachePath;
        this.file = paperModel.file;
        this.title = paperModel.title;
        this.titleEn = paperModel.titleEn;
        this.author = paperModel.author;
        this.major = paperModel.major;
        this.college = paperModel.college;
        this.no = paperModel.no;
        this.teacher = paperModel.teacher;
        this.professionalQualification = paperModel.professionalQualification;
        this.abstractString = paperModel.abstractString;
        this.abstractStringEn = paperModel.abstractStringEn;
        this.thanks = paperModel.thanks;
        this.keywords.addAll(paperModel.keywords);
        this.keywordsEn.addAll(paperModel.keywordsEn);
        this.contents.clear();
        for (BaseContentModel model : paperModel.contents) {
            this.contents.add(model.clone(this));
        }
        this.createSuccess = false;
        this.autoGeneratePictureNo = paperModel.autoGeneratePictureNo;
    }

    /**
     * 生成关键词字符串 使用"；"隔开每一个关键词
     */
    public String getKeywords() {
        if (keywords.size() == 0) {
            return "";
        }

        StringBuilder stringBuilder = new StringBuilder();
        for (String keyword : keywords) {
            stringBuilder.append(keyword).append("；");
        }

        return stringBuilder.substring(0, stringBuilder.length() - 1);
    }


    /**
     * 生成英文关键词字符串 使用"；"隔开每一个关键词
     */
    public String getKeywordsEn() {
        if (keywordsEn.size() == 0) {
            return "";
        }

        StringBuilder stringBuilder = new StringBuilder();
        for (String keyword : keywordsEn) {
            stringBuilder.append(keyword).append("; ");
        }

        return stringBuilder.substring(0, stringBuilder.length() - 1);
    }

    /**
     * 将一个字符串以回车拆分成多个字符串
     *
     * @param s 待拆分的字符串
     * @return 拆分后的字符串
     */
    public static String[] spiltLines(String s) {
        return s.split("\r\n|\r|\n");
    }

    /**
     * 获取中英文所有的关键词
     */
    public List<String> getKeywordList() {
        List<String> list = new ArrayList<>();
        list.addAll(keywords);
        list.addAll(keywordsEn);
        return list;
    }

    /**
     * 获取所有内容
     */
    public List<BaseContentModel> getContents() {
        return new ArrayList<>(contents);
    }

    /**
     * 在指定位置新增
     *
     * @param index        指定位置
     * @param contentModel 需要新增的Model
     */
    public void addContent(int index, BaseContentModel contentModel) {
        this.contents.add(index, contentModel);
    }

    /**
     * 更新指定位置的内容
     */
    public void updateContent(int index, BaseContentModel contentModel) {
        this.contents.set(index, contentModel);
    }

    /**
     * 移除指定内容
     */
    public void removeContent(BaseContentModel model) {
        this.contents.remove(model);

        // 检查是否拥有文本段, 没有的话 自动在末尾增加
        if (this.contents.size() == 0) {
            contents.add(new StringModel());
        }
    }

    /**
     * 更新所有的内容, 并更新引用信息
     */
    public void updateContents() {
        quotations.clear();
        // 遍历所有StringModel
        for (BaseContentModel baseContentModel : contents) {
            if (baseContentModel instanceof StringModel) {
                StringModel model = (StringModel) baseContentModel;
                model.updateContent();
            }
        }
    }

    /**
     * 删除存储临时文件的目录中未用到的资源
     */
    public void deleteUnusedResources() {
        ExecutorServiceUtils.executeByCached(() -> {
            File pictureFolder = new File(getFileCachePath() + File.separator + PICTURE_FOLDER);
            if (pictureFolder.exists() && pictureFolder.isDirectory()) {
                // 获取照片文件夹下所有的文件
                File[] files = pictureFolder.listFiles();
                if (files != null) {
                    List<String> fileNames = new ArrayList<>();

                    for (File file1 : files) {
                        fileNames.add(file1.getAbsolutePath());
                    }

                    // 遍历contents, 移除当前有引用的文件名
                    for (BaseContentModel model : contents) {
                        if (model instanceof PictureModel) {
                            fileNames.remove(((PictureModel) model).filePath);
                        }
                    }

                    // 剩下的是未被引用的 删除
                    for (String fileName : fileNames) {
                        new File(fileName).delete();
                    }
                }
            }
        });
    }

    /**
     * 获取临时存放路径
     */
    public String getFileCachePath() {
        return fileCachePath;
    }

    /**
     * 获取指定文件名的照片路径
     */
    public String getPicturePath(String pictureName) {
        return getFileCachePath() + File.separator + PICTURE_FOLDER + File.separator + pictureName;
    }

    // region DataBinding

    public String getTitleOrUnnamed() {
        return TextUtils.isEmpty(title) ? ResourcesUtils.getString(R.string.paper_edit_paper_title_undefined) : title;
    }

    @Bindable
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
        notifyPropertyChanged(BR.title);
    }

    @Bindable
    public String getTitleEn() {
        return titleEn;
    }

    public void setTitleEn(String titleEn) {
        this.titleEn = titleEn;
        notifyPropertyChanged(BR.titleEn);
    }

    @Bindable
    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
        notifyPropertyChanged(BR.author);
    }

    @Bindable
    public String getMajor() {
        return major;
    }

    public void setMajor(String major) {
        this.major = major;
        notifyPropertyChanged(BR.major);
    }

    @Bindable
    public String getCollege() {
        return college;
    }

    public void setCollege(String college) {
        this.college = college;
        notifyPropertyChanged(BR.college);
    }

    @Bindable
    public String getNo() {
        return no;
    }

    public void setNo(String no) {
        this.no = no;
        notifyPropertyChanged(BR.no);
    }

    @Bindable
    public String getTeacher() {
        return teacher;
    }

    public void setTeacher(String teacher) {
        this.teacher = teacher;
        notifyPropertyChanged(BR.teacher);
    }

    @Bindable
    public String getProfessionalQualification() {
        return professionalQualification;
    }

    public void setProfessionalQualification(String professionalQualification) {
        this.professionalQualification = professionalQualification;
        notifyPropertyChanged(BR.professionalQualification);
    }

    @Bindable
    public String getAbstractString() {
        return abstractString;
    }

    public void setAbstractString(String abstractString) {
        this.abstractString = abstractString;
        notifyPropertyChanged(BR.abstractString);
    }

    @Bindable
    public String getThanks() {
        return thanks;
    }

    public void setThanks(String thanks) {
        this.thanks = thanks;
        notifyPropertyChanged(BR.thanks);
    }

    @Bindable
    public boolean isCreateSuccess() {
        return createSuccess;
    }

    public void setCreateSuccess(boolean createSuccess) {
        this.createSuccess = createSuccess;
        notifyPropertyChanged(BR.createSuccess);
    }

    @Bindable
    public boolean isAutoGeneratePictureNo() {
        return autoGeneratePictureNo;
    }

    public void setAutoGeneratePictureNo(boolean autoGeneratePictureNo) {
        this.autoGeneratePictureNo = autoGeneratePictureNo;
        notifyPropertyChanged(BR.autoGeneratePictureNo);
    }

    // endregion
}
