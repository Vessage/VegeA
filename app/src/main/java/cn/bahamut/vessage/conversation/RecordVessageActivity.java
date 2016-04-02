package cn.bahamut.vessage.conversation;

import android.annotation.SuppressLint;
import android.os.PersistableBundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import com.seu.magicfilter.*;
import com.seu.magicfilter.widget.MagicCameraView;

import cn.bahamut.vessage.*;
import cn.bahamut.vessage.R;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class RecordVessageActivity extends AppCompatActivity {

    private Button leftButton;
    private Button middleButton;
    private Button rightButton;
    private View previewView;
    private MagicEngine magicEngine;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(cn.bahamut.vessage.R.layout.activity_record_vessage);
        leftButton = (Button)findViewById(R.id.leftButton);
        middleButton = (Button) findViewById(R.id.middleButton);
        rightButton = (Button)findViewById(R.id.rightButton);
        previewView = (View)findViewById(R.id.previewView);
        initControls();

        hideView(leftButton);
        hideView(rightButton);

        MagicEngine.Builder builder = new MagicEngine.Builder((MagicCameraView) findViewById(R.id.previewView));
        magicEngine = builder
                .setVideoSize(480, 640)
                .build();


    }

    private void hideView(View v){
        v.setVisibility(View.INVISIBLE);
    }

    private void hidePreview(){
        hideView(previewView);
    }

    private void showView(View v){
        v.setVisibility(View.VISIBLE);
    }
    private void showPreview(){
        showView(previewView);
    }

    @Override
    protected void onResume() {
        super.onResume();
        magicEngine.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        magicEngine.onPause();
    }

    private void initControls(){
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        previewView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
    }
}
