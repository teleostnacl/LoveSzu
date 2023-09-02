package com.teleostnacl.common.android.database.converter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import androidx.room.TypeConverter;

import java.io.ByteArrayOutputStream;

public class BitmapConverter {
    //Bitmap转String
    @TypeConverter
    public static String BitmapConvertToString(Bitmap bitmap) {
        if(bitmap == null) return null;
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        //转为byte数组
        byte[] bytes = stream.toByteArray();
        return Base64.encodeToString(bytes,Base64.DEFAULT);
    }

    //String转Bitmap
    @TypeConverter
    public static Bitmap StringConvertToBitmap(String string) {
        if(string == null || string.equals("")) return null;
        byte[] bytes= Base64.decode(string, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }
}
