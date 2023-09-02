package com.teleostnacl.szu.paper.model;

import android.graphics.BitmapFactory;

import androidx.databinding.Bindable;

import com.teleostnacl.szu.paper.BR;

import org.apache.poi.common.usermodel.PictureType;
import org.apache.poi.util.Units;

/**
 * 图片的内容
 */
public class PictureModel extends BaseContentModel {

    // 一张图片的最大宽和高(单位: 磅)
    private static final int MAX_WIDTH = Units.toEMU(400);
    private static final int MAX_HEIGHT = Units.toEMU(600);

    public String filePath;

    public PictureType pictureType;

    // 图例
    public String pictureName = "";

    public int width;

    public int height;

    public PictureModel() {
        setType(PICTURE);
    }

    public PictureModel(String filePath, PictureType pictureType, String pictureName, int width, int height, boolean isParagraph) {
        setType(PICTURE);
        this.filePath = filePath;
        this.pictureType = pictureType;
        this.pictureName = pictureName;
        this.height = height;
        this.width = width;
        this.isParagraph = isParagraph;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;

        // 获取图片的分辨率
        BitmapFactory.Options options = new BitmapFactory.Options();
        // 这个方式不会在内存创建一张图片
        options.inJustDecodeBounds = true;
        // 将分辨率信息存储进options中
        BitmapFactory.decodeFile(filePath, options);
        setSize(options.outWidth, options.outHeight);
        if (options.outMimeType == null) {
            pictureType = PictureType.UNKNOWN;
        } else {
            // 获取图片格式
            switch (options.outMimeType) {
                case "image/x-emf":
                    pictureType = PictureType.EMF;
                    break;
                case "image/x-wmf":
                    pictureType = PictureType.WMF;
                    break;
                case "image/x-pict":
                    pictureType = PictureType.PICT;
                    break;
                case "image/jpeg":
                    pictureType = PictureType.JPEG;
                    break;
                case "image/png":
                    pictureType = PictureType.PNG;
                    break;
                case "image/dib":
                    pictureType = PictureType.DIB;
                    break;
                case "image/gif":
                    pictureType = PictureType.GIF;
                    break;
                case "image/tiff":
                    pictureType = PictureType.TIFF;
                    break;
                case "image/x-eps":
                    pictureType = PictureType.EPS;
                    break;
                case "image/x-ms-bmp":
                    pictureType = PictureType.BMP;
                    break;
                case "image/x-wpg":
                    pictureType = PictureType.WPG;
                    break;
                case "image/vnd.ms-photo":
                    pictureType = PictureType.WDP;
                    break;
                case "image/svg+xml":
                    pictureType = PictureType.SVG;
                    break;
                default:
                    pictureType = PictureType.UNKNOWN;
                    break;
            }
        }
    }

    /**
     * 设置图片的尺寸 适应纸张大小
     */
    private void setSize(int width, int height) {
        this.width = Units.pixelToEMU(width);
        this.height = Units.pixelToEMU(height);

        // 如果尺寸大于最大尺寸 则进行调整
        while (this.width > MAX_WIDTH || this.height > MAX_HEIGHT) {
            // 调整宽
            if (this.width > MAX_WIDTH) {
                this.width = MAX_WIDTH;
                this.height = (int) ((double) height / (double) width * (double) this.width);
            }

            // 调整高
            if (this.height > MAX_HEIGHT) {
                this.height = MAX_HEIGHT;
                this.width = (int) ((double) width / (double) height * (double) this.height);
            }
        }
    }

    // region DataBinding

    @Bindable
    public String getPictureName() {
        return pictureName;
    }

    public void setPictureName(String pictureName) {
        this.pictureName = pictureName;
        notifyPropertyChanged(BR.pictureName);
    }


    // endregion
}
