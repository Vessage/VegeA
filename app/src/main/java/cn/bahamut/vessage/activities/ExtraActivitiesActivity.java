package cn.bahamut.vessage.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import cn.bahamut.vessage.R;

public class ExtraActivitiesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setTitle(R.string.new_intersting);
        setContentView(R.layout.activity_extra_activities);
    }
}
