package cn.bahamut.vessage.camera;
/*
import android.content.Context;
import android.view.View;

import com.seu.magicfilter.MagicEngine;
import com.seu.magicfilter.camera.CameraEngine;
import com.seu.magicfilter.widget.MagicCameraView;

import java.io.File;

import cn.bahamut.common.AndroidHelper;

/**
 * Created by alexchow on 16/4/25.
 */
/*
public class MagicVessageCamera extends VessageCameraBase {
    private MagicEngine magicEngine;

    public MagicVessageCamera(Context context) {
        super(context);
    }

    private String createVideoTmpFile(){
        File tmpVideoFile = getVideoTmpFile();
        if(tmpVideoFile.exists()){
            tmpVideoFile.delete();
        }
        return tmpVideoFile.getAbsolutePath();
    }

    @Override
    public void initCameraForRecordVideo(View previewView) {

        String videoFilePath = createVideoTmpFile();
        CameraEngine.setCameraID(1);
        MagicEngine.Builder builder = new MagicEngine.Builder((MagicCameraView)previewView);
        magicEngine = builder
                .setVideoSize(480, 640)
                .setVideoPath(videoFilePath)
                .build();
    }

    @Override
    protected boolean cameraStartRecord() {
        createVideoTmpFile();
        if(!AndroidHelper.isEmulator(context)){
            magicEngine.changeRecordingState(true);
        }
        return true;
    }

    @Override
    protected void cameraStopRecordAndSave(CameraOnSavedVideo onSavedVideo) {
        magicEngine.changeRecordingState(false);
    }

    @Override
    protected void cameraCancelRecord() {
        magicEngine.changeRecordingState(false);
    }

    @Override
    protected void cameraPauseRecord() {
        magicEngine.onPause();
    }

    @Override
    protected void cameraResumeRecord() {
        magicEngine.onResume();
    }

    @Override
    public void release() {
        super.release();
        magicEngine.onDestroy();
    }
}
*/
