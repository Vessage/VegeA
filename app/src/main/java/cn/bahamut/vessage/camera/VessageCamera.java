package cn.bahamut.vessage.camera;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import java.io.ByteArrayOutputStream;
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
                coreCamera.stopPreview();
                if(previewData != null){

                    try {
                        int imageFormat=coreCamera.getParameters().getPreviewFormat();
                        int w=coreCamera.getParameters().getPreviewSize().width;
                        int h=coreCamera.getParameters().getPreviewSize().height;
                        Rect rect=new Rect(0,0,w,h);
                        YuvImage yuvImg = new YuvImage(previewData,imageFormat,w,h,null);
                        ByteArrayOutputStream outputstream = new ByteArrayOutputStream();
                        yuvImg.compressToJpeg(rect, 100, outputstream);
                        Bitmap bitmap = BitmapFactory.decodeByteArray(outputstream.toByteArray(), 0, outputstream.size());
                        Configuration config = context.getResources().getConfiguration();
                        if (config.orientation==1)
                        { // 坚拍
                            Matrix matrix = new Matrix();
                            matrix.reset();
                            matrix.postRotate(270);
                            if(cameraId == 1){
                                matrix.postScale(-1, 1); //翻转
                            }
                            Bitmap bMapRotate = Bitmap.createBitmap(bitmap, 0, 0,
                                    bitmap.getWidth(), bitmap.getHeight(),
                                    matrix, true);
                            bitmap = bMapRotate;
                        }

                        onTokePicture.onTokeJEPGPicture(bitmap);
                    }catch (Exception e){
                        coreCamera.startPreview();
                        onTokePicture.onTokeJEPGPicture(null);
                    }
                }else {
                    coreCamera.startPreview();
                    onTokePicture.onTokeJEPGPicture(null);
                }
            }catch (Exception ex){
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
            if (holder.getSurface() == null){
                return;
            }

            try {
                coreCamera.stopPreview();
            } catch (Exception e){
                // ignore: tried to stop a non-existent preview
            }
            try {
                coreCamera.setPreviewCallback(previewCallback);
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
                coreCamera.setPreviewCallback(previewCallback);
                coreCamera.setDisplayOrientation(90);
                coreCamera.setPreviewDisplay(holder);
                coreCamera.setFaceDetectionListener(faceDetectionListener);
                if(faceDetectionEnabled){
                    coreCamera.startFaceDetection();
                }
                coreCamera.startPreview();
                CamcorderProfile profile;
                if(AndroidHelper.isEmulator(context)) {
                    profile = CamcorderProfile.get(cameraId, CamcorderProfile.QUALITY_LOW);
                }else {
                    profile = CamcorderProfile.get(cameraId, CamcorderProfile.QUALITY_CIF);
                }
                Camera.Parameters parameters = coreCamera.getParameters();
                parameters.setPreviewSize(profile.videoFrameWidth,profile.videoFrameHeight);
                List<Camera.Size> pictureSizes = parameters.getSupportedPictureSizes();
                for (Camera.Size pictureSize : pictureSizes) {
                    Log.d("pictureSize",String.format("%dx%d",pictureSize.width,pictureSize.height));
                }
                if(pictureSizes.size() > 0){
                    Camera.Size size = pictureSizes.get(0);
                    parameters.setPictureSize(size.width,size.height);
                }
                parameters.setPictureFormat(ImageFormat.JPEG);
                coreCamera.setParameters(parameters);


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
        this.cameraForTakePictureInited = true;
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
                coreCamera.setPreviewCallback(null);
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

    @Override
    public boolean isDetectedFaces() {
        return isDetectedFaces;
    }
}
