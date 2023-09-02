package com.teleostnacl.szu.library.database;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.teleostnacl.common.android.database.BaseEntry;
import com.teleostnacl.common.android.utils.EncryptUtils;
import com.teleostnacl.szu.library.model.list.LibraryBookListModel;

import java.io.ByteArrayOutputStream;

/**
 * 加入清单的图书信息
 */
@Entity(tableName = "book_list")
public class LibraryBookListEntry implements BaseEntry<LibraryBookListModel> {

    //登录号
    @PrimaryKey
    @ColumnInfo(name = "accession_number")
    @NonNull
    public String accessionNumber = "";

    // 索引号
    @ColumnInfo(name = "call_number")
    public String callNumber;

    // 标题
    @ColumnInfo(name = "title")
    public String title;

    // 卷期
    @ColumnInfo(name = "volume")
    public String volume;

    // 封面图片
    @ColumnInfo(name = "cover")
    public byte[] cover;

    // 馆藏地
    @ColumnInfo(name = "location")
    public String location;

    // 排/架
    @ColumnInfo(name = "shelf_reference")
    public String shelfReference;

    // 链接
    @ColumnInfo(name = "ctrlno")
    public String ctrlno;

    // 放入收藏清单的时间
    @ColumnInfo(name = "date")
    public long date;

    @Ignore
    public LibraryBookListEntry(@NonNull LibraryBookListModel libraryBookListModel) {
        this.accessionNumber = EncryptUtils.encrypt(libraryBookListModel.accessionNumber);
        this.callNumber = EncryptUtils.encrypt(libraryBookListModel.callNumber);
        this.title = EncryptUtils.encrypt(libraryBookListModel.title);
        this.volume = EncryptUtils.encrypt(libraryBookListModel.volume);
        this.cover = bitmapConvertToBytes(libraryBookListModel.cover);
        this.location = EncryptUtils.encrypt(libraryBookListModel.collectionSite);
        this.shelfReference = EncryptUtils.encrypt(libraryBookListModel.shelfReference);
        this.ctrlno = EncryptUtils.encrypt(libraryBookListModel.ctrlno);
        this.date = libraryBookListModel.date;
    }

    public LibraryBookListEntry() {

    }

    @Override
    public LibraryBookListModel toModel() {
        LibraryBookListModel model = new LibraryBookListModel();

        model.accessionNumber = EncryptUtils.decrypt(accessionNumber);
        model.callNumber = EncryptUtils.decrypt(callNumber);
        model.title = EncryptUtils.decrypt(title);
        model.volume = EncryptUtils.decrypt(volume);
        model.cover = bytesConvertToBitmap(cover);
        model.collectionSite = EncryptUtils.decrypt(location);
        model.shelfReference = EncryptUtils.decrypt(shelfReference);
        model.ctrlno = EncryptUtils.decrypt(ctrlno);
        model.date = date;

        return model;
    }

    /**
     * 位图转byte []
     */
    private byte[] bitmapConvertToBytes(Bitmap bitmap) {
        if (bitmap == null) {
            return null;
        }
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        //转为byte数组
        return stream.toByteArray();
    }

    /**
     * byte [] 转位图
     */
    private Bitmap bytesConvertToBitmap(byte[] bytes) {
        if (bytes == null) {
            return null;
        }
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }
}
