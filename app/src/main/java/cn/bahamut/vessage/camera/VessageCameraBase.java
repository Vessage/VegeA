package cn.bahamut.vessage.camera;

import android.content.Context;
import android.support.annotation.CallSuper;
import android.view.View;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by alexchow on 16/4/25.
 */
public abstract class VessageCameraBase {
    private static final String TAG = "VessageCamera";
    protected Context context;
    private volatile boolean recording = false;
    private Timer recordingTimer;
    private volatile int recordedTime = 0;
    protected int cameraId = 1;

    protected boolean cameraForTakePictureInited = false;
    protected boolean cameraForRecordVideoInited = false;

    public VessageCameraBase(Context context){
        this.context = context;
    }
    protected TimerTask recordingTimeTask;

    public boolean isRecording() {
        return recording;
    }

    public void setHandler(OnRecordingTiming handler) {
        this.handler = handler;
    }

    public abstract void takePicture(OnTokePicture onTokePicture);

    public static abstract class OnTokePicture{
        public void onTokeJEPGPicture(byte[] jpeg){}
        public void onTakeRawPicture(byte[] raw){}
    }

    public interface OnRecordingTiming {
        void onRecordingTiming(int recordedTime);
    }

    public interface CameraOnSavedVideo{
        void onVideoSaved(File file);
    }

    private OnRecordingTiming handler;

    final public void initCameraForRecordVideo(View previewView){
        if(cameraInitVideoRecorder(previewView)){
            cameraForRecordVideoInited = true;
        }
    }

    protected abstract boolean cameraInitVideoRecorder(View previewView);

    final public void initCameraForRecordTakePicture(View previewView){
        cameraInitTakePicture(previewView);
    }

    protected abstract boolean cameraInitTakePicture(View previewView);

    protected TimerTask generateRecordingTimeTask() {
        return new TimerTask() {
            @Override
            public void run() {
                recordedTime++;
                if(handler!=null && recording){
                    handler.onRecordingTiming(recordedTime);
                }
            }
        };
    }

    protected File getVideoTmpFile(){
        return new File(context.getCacheDir(),"tmpRecordVideo.mp4");
    }

    final public boolean startRecord(){
        if(!cameraForRecordVideoInited || isRecording()){
            return false;
        }
        if(cameraStartRecord()){
            recordedTime = 0;
            recording = true;
            recordingTimer = new Timer();
            recordingTimeTask = generateRecordingTimeTask();
            recordingTimer.schedule(recordingTimeTask,1000,1000);
            return true;
        }
        return false;
    }

    protected abstract boolean cameraStartRecord();

    final public void stopAndSaveRecordedVideo(CameraOnSavedVideo onSavedVideo){
        if(!cameraForRecordVideoInited){
            return;
        }
        if(recording){
            recording = false;
            if(recordingTimer != null){
                recordingTimer.cancel();
            }
            cameraStopRecordAndSave(onSavedVideo);
        }
    }

    protected abstract void cameraStopRecordAndSave(CameraOnSavedVideo onSavedVideo);

    final public void cancelRecord(){
        if(!cameraForRecordVideoInited){
            return;
        }
        if(recording){
            recording = false;
            if(recordingTimer != null){
                recordingTimer.cancel();
            }
            cameraCancelRecord();
        }
    }

    protected abstract void cameraCancelRecord();

    final public void pauseRecord(){
        if(!cameraForRecordVideoInited){
            return;
        }
        if(recording){
            cameraPauseRecord();
        }
    }

    protected abstract void cameraPauseRecord();

    final public void resumeRecord(){
        if(!cameraForRecordVideoInited){
            return;
        }
        if(recording){
            cameraResumeRecord();
        }
    }

    protected abstract void cameraResumeRecord();

    public abstract void stopPreview();
    public abstract void startPreview();
    @CallSuper
    public void release(){
        if(recordingTimer != null){
            recordingTimer.cancel();
        }
    }
}
