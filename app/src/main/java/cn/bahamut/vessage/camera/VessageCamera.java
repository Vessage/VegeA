package cn.bahamut.vessage.camera;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import java.io.File;
import java.io.IOException;
import java.util.List;

import cn.bahamut.common.AndroidHelper;

/**
 * Created by alexchow on 16/4/25.
 */
public class VessageCamera extends VessageCameraBase implements MediaRecorder.OnInfoListener,MediaRecorder.OnErrorListener{
    private static final String TAG = "VessageCamera";
    private Camera coreCamera;
    private MediaRecorder mediaRecorder;
    private SurfaceView previewView;
    private volatile boolean faceDetectionEnabled = false;
    private volatile boolean isDetectedFaces = false;
    private volatile boolean canDetectFaces = false;
    private volatile byte[] previewData;
    public VessageCamera(Context context){
        super(context);
    }

    private Camera.PreviewCallback previewCallback = new Camera.PreviewCallback() {
        @Override
        public void onPreviewFrame(byte[] data, Camera camera) {
            previewData = data;
        }
    };

    @Override
    public void takePicture(final OnTokePicture onTokePicture) {
        if (cameraForTakePictureInited) {
            try {
                coreCamera.takePicture(null, null, new Camera.PictureCallback() {
                    @Override
                    public void onPictureTaken(byte[] data, Camera camera) {
                        Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                        Configuration config = context.getResources().getConfiguration();
                        if (config.orientation == 1) { // 坚拍
                            Matrix matrix = new Matrix();
                            matrix.reset();
                            matrix.postRotate(270);
                            if (cameraId == 1) {
                                matrix.postScale(-1, 1); //翻转
                            }
                            Bitmap bMapRotate = Bitmap.createBitmap(bitmap, 0, 0,
                                    bitmap.getWidth(), bitmap.getHeight(),
                                    matrix, true);
                            bitmap = bMapRotate;
                        }

                        onTokePicture.onTokeJEPGPicture(bitmap);
                    }
                });
            } catch (Exception ex) {
                onTokePicture.onTokeJEPGPicture(null);
            }
        }
    }

    SurfaceHolder.Callback surfaceHolderCallback = new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            createPreivewCamera(holder);
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
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
                coreCamera.setPreviewCallback(previewCallback);
                coreCamera.setDisplayOrientation(90);
                coreCamera.setPreviewDisplay(holder);
                coreCamera.setFaceDetectionListener(faceDetectionListener);
                if(faceDetectionEnabled){
                    coreCamera.startFaceDetection();
                }

                Camera.Parameters parameters = coreCamera.getParameters();
                parameters.setPreviewSize(holder.getSurfaceFrame().height(),holder.getSurfaceFrame().width());
                if(cameraForTakePictureInited){
                    List<Camera.Size> pictureSizes = parameters.getSupportedPictureSizes();
                    if(pictureSizes.size() > 0){
                        Camera.Size maxSize = pictureSizes.get(0);
                        for (Camera.Size pictureSize : pictureSizes) {
                            Log.i("Camera Picture Size",String.format("%d x %d",pictureSize.width,pictureSize.height));
                            if(pictureSize.width > maxSize.width){
                                maxSize = pictureSize;
                            }
                        }
                        parameters.setPictureSize(maxSize.width,maxSize.height);
                        Log.i("Set Picture Size",String.format("%d x %d",maxSize.width,maxSize.height));
                    }
                    parameters.setPictureFormat(ImageFormat.JPEG);
                }
                coreCamera.setParameters(parameters);
                coreCamera.startPreview();

                return true;
            } catch (IOException e) {
                e.printStackTrace();
                if(coreCamera != null){
                    coreCamera.setPreviewCallback(null);
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

    @Override
    public void setFaceDetectedEnable(boolean faceDetectedEnable) {
        this.faceDetectionEnabled = faceDetectedEnable;
        if(coreCamera == null){
            return;
        }
        if(faceDetectedEnable){
            coreCamera.startFaceDetection();
        }else if(faceDetectedEnable == false){
            coreCamera.stopFaceDetection();
        }
    }

    @Override
    public boolean isFaceDetectedEnable() {
        return faceDetectionEnabled;
    }

    private Camera.FaceDetectionListener faceDetectionListener = new Camera.FaceDetectionListener() {
        @Override
        public void onFaceDetection(Camera.Face[] faces, Camera camera) {
            if(faces.length > 0){
                canDetectFaces = true;
            }
            isDetectedFaces = faces.length > 0;
        }
    };

    @Override
    public void stopPreview() {
        try {
            coreCamera.stopPreview();
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    @Override
    public void startPreview() {
        try {
            coreCamera.startPreview();
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    private boolean resetRecorder(){
        if(mediaRecorder != null){
            mediaRecorder.stop();
            mediaRecorder.reset();
            mediaRecorder.release();
            mediaRecorder = null;
        }

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

        mediaRecorder = new MediaRecorder();
        mediaRecorder.setOnErrorListener(VessageCamera.this);
        mediaRecorder.setOnInfoListener(this);
        mediaRecorder.setCamera(coreCamera);

        if(AndroidHelper.isEmulator(context)){
            //1.设置采集声音
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
            //设置采集图像
            mediaRecorder.setVideoSource(MediaRecorder.VideoSource.DEFAULT);
            //2.设置视频，音频的输出格式
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            //3.设置音频的编码格式
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            //设置图像的编码格式
            mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);

            mediaRecorder.setVideoSize(320,240);
            mediaRecorder.setAudioChannels(1);
            mediaRecorder.setAudioSamplingRate(44100);
            mediaRecorder.setAudioEncodingBitRate(8);
        }else {
            //1.设置采集声音
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
            //设置采集图像
            mediaRecorder.setVideoSource(MediaRecorder.VideoSource.DEFAULT);
            CamcorderProfile profile = CamcorderProfile.get(cameraId,CamcorderProfile.QUALITY_CIF);
            profile.fileFormat = MediaRecorder.OutputFormat.MPEG_4;
            profile.videoCodec = MediaRecorder.VideoEncoder.H264;
            profile.audioCodec = MediaRecorder.AudioEncoder.AAC;
            mediaRecorder.setProfile(profile);
        }

        //设置选择角度，顺时针方向，因为默认是逆向90度的，这样图像就是正常显示了,这里设置的是观看保存后的视频的角度
        mediaRecorder.setOrientationHint(270);
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

    private void initPreviewView(View previewView){
        this.previewView = (SurfaceView) previewView;
        this.previewView.getHolder().addCallback(surfaceHolderCallback);
        this.previewView.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        this.previewView.getHolder().setKeepScreenOn(true);
    }

    @Override
    protected boolean cameraInitVideoRecorder(View previewView) {
        this.cameraForRecordVideoInited = true;
        this.cameraForTakePictureInited = false;
        initPreviewView(previewView);
        return true;
    }

    @Override
    protected boolean cameraInitTakePicture(View previewView) {
        this.cameraForRecordVideoInited = false;
        this.cameraForTakePictureInited = true;
        initPreviewView(previewView);
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
            mediaRecorder.reset();
            mediaRecorder.release();
            mediaRecorder = null;
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
                mediaRecorder.reset();
                mediaRecorder.release();
                mediaRecorder = null;
                coreCamera.lock();
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }


    @Override
    protected void cameraPauseRecord() {
        stopPreview();
    }

    @Override
    protected void cameraResumeRecord() {
        startPreview();
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
                mediaRecorder.reset();
                mediaRecorder.release();
                coreCamera.lock();
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
                coreCamera.setPreviewCallback(null);
                coreCamera.release();
                coreCamera.lock();
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

    @Override
    public boolean isDetectedFaces() {
        if(canDetectFaces){
            return isDetectedFaces;
        }else {
            return true;
        }
    }
}
