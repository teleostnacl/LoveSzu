package com.teleostnacl.szu.file.repository;

import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.teleostnacl.common.android.log.Logger;
import com.teleostnacl.common.android.context.ResourcesUtils;
import com.teleostnacl.common.java.util.IOUtils;
import com.teleostnacl.szu.file.R;
import com.teleostnacl.szu.file.model.FileModel;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import io.reactivex.rxjava3.core.Single;

public class FileRepository {

    /**
     * 读取json 并获取 FileModel list
     */
    public Single<List<FileModel>> readJson() {
        try {
            //Json的解析类对象
            JsonParser parser = new JsonParser();
            //将JSON的String 转成一个JsonArray对象
            JsonArray jsonArray = parser.parse(readRawJson()).getAsJsonArray();

            Gson gson = new Gson();

            // 使用Set过滤重复文件
            Set<FileModel> fileModels = new LinkedHashSet<>();

            //加强for循环遍历JsonArray
            for (JsonElement user : jsonArray) {
                //使用GSON，直接转成Bean对象
                FileModel fileModel = gson.fromJson(user, FileModel.class);

                if (!TextUtils.isEmpty(fileModel.fileName) && !TextUtils.isEmpty(fileModel.fileUrl)) {
                    fileModels.add(fileModel);
                }
            }

            return Single.just(new ArrayList<>(fileModels));
        } catch (Exception e) {
            Logger.e(e);
            return Single.error(e);
        }
    }

    /**
     * 读取json, 并转换成string
     */
    @NonNull
    private String readRawJson() throws Exception {
        InputStreamReader inputReader = null;
        BufferedReader bufReader = null;
        try {
            StringBuilder stringBuilder = new StringBuilder();

            inputReader = new InputStreamReader(ResourcesUtils.getResources().openRawResource(R.raw.file));
            bufReader = new BufferedReader(inputReader);

            String line;
            while ((line = bufReader.readLine()) != null) {
                stringBuilder.append(line);
            }

            return stringBuilder.toString();
        } finally {
            IOUtils.close(inputReader, bufReader);
        }
    }
}
