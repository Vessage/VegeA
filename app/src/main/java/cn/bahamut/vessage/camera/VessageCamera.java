package cn.bahamut.vessage.camera;

import android.content.Context;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import java.io.File;
import java.io.IOException;

/**
 * Created by alexchow on 16/4/25.
 */
public class VessageCamera extends VessageCameraBase implements MediaRecorder.OnInfoListener,MediaRecorder.OnErrorListener{
    private static final String TAG = "VessageCamera";
    private Context context;
    private Camera coreCamera;
    private MediaRecorder mediaRecorder;
    private SurfaceView previewView;

    public VessageCamera(Context context){
        super(context);
    }

    @Override
    public void takePicture(final OnTokePicture onTokePicture) {
        Camera.ShutterCallback shutter = new Camera.ShutterCallback() {
            @Override
            public void onShutter() {

            }
        };

        Camera.PictureCallback jpegCallback = new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] data, Camera camera) {
                onTokePicture.onTokeJEPGPicture(data);
                camera.startPreview();
            }
        };
        coreCamera.takePicture(shutter,null,jpegCallback);
    }

    SurfaceHolder.Callback surfaceHolderCallback = new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            createPreivewCamera(holder);
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            if (holder.getSurface() == null){
                // preview surface does not exist
                return;
            }

            // stop preview before making changes
            try {
                coreCamera.stopPreview();
            } catch (Exception e){
                // ignore: tried to stop a non-existent preview
            }

            // set preview size and make any resize, rotate or
            // reformatting changes here

            // start preview with new settings
            try {
                coreCamera.setPreviewDisplay(holder);
                coreCamera.startPreview();

            } catch (Exception e){
                Log.d(TAG, "Error starting camera preview: " + e.getMessage());
            }
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            releaseCamera();
        }
    };

    private boolean createPreivewCamera(SurfaceHolder holder) {
        if (coreCamera == null) {
            try {
                coreCamera = Camera.open(cameraId);
                coreCamera.setPreviewDisplay(holder);
                coreCamera.startPreview();

                CamcorderProfile profile = CamcorderProfile.get(cameraId,CamcorderProfile.QUALITY_LOW);

                Camera.Parameters parameters = coreCamera.getParameters();
                parameters.setPreviewSize(profile.videoFrameWidth,profile.videoFrameHeight);
                parameters.setPictureSize(600,800);
                coreCamera.setParameters(parameters);

                return true;
            } catch (IOException e) {
                e.printStackTrace();
                if(coreCamera != null){
                    coreCamera.release();
                    coreCamera = null;
                }
                return false;
            }catch (RuntimeException ex){
                return false;
            }
        }else {
            return true;
        }

    }

    private boolean resetRecorder(){
        if(coreCamera != null){
            try{
                coreCamera.stopPreview();
                coreCamera.unlock();
            }catch (Exception ex){
                ex.printStackTrace();
                return false;
            }
        }else {
            return false;
        }
        if(mediaRecorder != null){
            mediaRecorder.release();
            mediaRecorder = null;
        }
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setOnErrorListener(VessageCamera.this);
        mediaRecorder.setOnInfoListener(this);
        mediaRecorder.reset();
        mediaRecorder.setCamera(coreCamera);

        //1.设置采集声音
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        //设置采集图像
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

        //2.设置视频，音频的输出格式
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
        //3.设置音频的编码格式
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
        //设置图像的编码格式
        mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.DEFAULT);

        mediaRecorder.setVideoSize(320,240);

        mediaRecorder.setAudioChannels(1);
        mediaRecorder.setAudioSamplingRate(44100);
        mediaRecorder.setAudioEncodingBitRate(16);

        //设置选择角度，顺时针方向，因为默认是逆向90度的，这样图像就是正常显示了,这里设置的是观看保存后的视频的角度
        mediaRecorder.setOrientationHint(90);
        File videoFile = getVideoTmpFile();
        if(videoFile.exists()){
            videoFile.delete();
        }
        mediaRecorder.setPreviewDisplay(previewView.getHolder().getSurface());
        mediaRecorder.setOutputFile(videoFile.toString());
        try {
            mediaRecorder.prepare();
        } catch (IOException e) {
            mediaRecorder.release();
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    protected boolean cameraInitVideoRecorder(View previewView) {
        this.previewView = (SurfaceView) previewView;
        this.previewView.getHolder().addCallback(surfaceHolderCallback);
        this.previewView.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        this.previewView.getHolder().setKeepScreenOn(true);
        return true;
    }

    @Override
    protected boolean cameraInitTakePicture(View previewView) {
        this.previewView = (SurfaceView) previewView;
        this.previewView.getHolder().addCallback(surfaceHolderCallback);
        this.previewView.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        this.previewView.getHolder().setKeepScreenOn(true);
        return true;
    }

    @Override
    protected boolean cameraStartRecord() {
        if(resetRecorder()){
            try {
                mediaRecorder.start();
                return true;
            }catch (Exception ex){
                ex.printStackTrace();
            }
        }
        return false;
    }

    @Override
    protected void cameraStopRecordAndSave(CameraOnSavedVideo onSavedVideo) {
        try{
            mediaRecorder.stop();
            coreCamera.lock();
            File savedFile = getVideoTmpFile();
            Log.i(TAG,String.format("Recorded Video Path:%s",savedFile.getAbsolutePath()));
            Log.i(TAG,String.format("Recorded Video File Size:%s KB",String.valueOf(savedFile.length() / 1024)));
            if(onSavedVideo != null){
                onSavedVideo.onVideoSaved(savedFile);
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }

    }

    @Override
    protected void cameraCancelRecord() {
        try{
            if(mediaRecorder != null){
                mediaRecorder.stop();
                coreCamera.lock();
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }


    @Override
    protected void cameraPauseRecord() {

    }

    @Override
    protected void cameraResumeRecord() {

    }

    @Override
    public void release(){
        super.release();
        releaseRecorder();
        releaseCamera();
    }

    private void releaseRecorder() {
        if(mediaRecorder != null){
            if(!isRecording()){
                return;
            }
            try{
                mediaRecorder.stop();
                mediaRecorder.release();
                mediaRecorder = null;
            }catch (Exception ex){
                ex.printStackTrace();
            }
        }
    }

    private void releaseCamera() {
        if (coreCamera != null) {
            try{
                coreCamera.stopPreview();
                coreCamera.release();
            }catch (Exception ex){
                ex.printStackTrace();
            }
            coreCamera = null;
        }
    }

    @Override
    public void onError(MediaRecorder mr, int what, int extra) {

    }

    @Override
    public void onInfo(MediaRecorder mr, int what, int extra) {

    }
}
