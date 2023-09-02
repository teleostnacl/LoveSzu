package com.teleostnacl.szu.academic;

import android.os.Bundle;

import com.teleostnacl.szu.libs.activity.BaseLoadingActivity;
import com.teleostnacl.szu.scheme.R;

public class AcademicActivity extends BaseLoadingActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_academic);
    }
}