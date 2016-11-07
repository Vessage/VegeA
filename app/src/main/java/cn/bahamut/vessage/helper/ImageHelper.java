package cn.bahamut.vessage.helper;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;

import cn.bahamut.common.FileHelper;
import cn.bahamut.common.StringHelper;
import cn.bahamut.service.ServicesProvider;
import cn.bahamut.vessage.R;
import cn.bahamut.vessage.services.file.FileAccessInfo;
import cn.bahamut.vessage.services.file.FileService;

/**
 * Created by alexchow on 16/4/13.
 */
public class ImageHelper {
    static final String TAG = "ImageHelper";

    public static byte[] bitmap2Bytes(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        return baos.toByteArray();
    }

    public static class OnSetImageCallback{
        public void onSetImageSuccess(){}
        public void onSetImageFail(){}
    }

    public interface OnGetImageCallback{
        void onGetImageDrawable(Drawable drawable);
        void onGetImageResId(int resId);
        void onGetImageFailed();
    }

    public static void setImageByFileId(ImageButton imageButton, String fileId) {
        setImageByFileIdOnView(imageButton,fileId);
    }

    public static void setImageByFileId(ImageButton imageButton, String fileId, int defaultImageRId) {
        setImageByFileIdOnView(imageButton,fileId,defaultImageRId);
    }

    public static void setImageByFileId(ImageView imageView, String fileId) {
        setImageByFileIdOnView(imageView,fileId);
    }

    public static void setImageByFileId(ImageView imageView, String fileId, int defaultImageRId) {
        setImageByFileIdOnView(imageView,fileId,defaultImageRId);
    }

    public static void setImageByFileIdOnView(final View view, String fileId){
        setImageByFileIdOnView(view,fileId,-1);
    }

    public static void setImageByFileIdOnView(final View view, String fileId, int defaultImageRId){
        setImageByFileIdOnView(view,fileId,defaultImageRId,new OnSetImageCallback());
    }

    public static void getImageByFileId(String fileId, final OnGetImageCallback callback){
        FileService fileService = ServicesProvider.getService(FileService.class);
        String filePath = fileService.getFilePath(fileId,null);
        if(filePath != null){
            Drawable drawable = Drawable.createFromPath(filePath);
            callback.onGetImageDrawable(drawable);
        }else {
            fileService.fetchFileToCacheDir(fileId,null, null,new FileService.OnFileListenerAdapter() {

                @Override
                public void onFileSuccess(FileAccessInfo info,Object tag) {
                    Drawable drawable = Drawable.createFromPath(info.getLocalPath());
                    callback.onGetImageDrawable(drawable);
                }

                @Override
                public void onFileFailure(FileAccessInfo info, Object tag) {
                    super.onFileFailure(info, tag);
                    callback.onGetImageFailed();
                }

                @Override
                public void onGetFileInfoError(String fileId, Object tag) {
                    super.onGetFileInfoError(fileId, tag);
                    callback.onGetImageFailed();
                }
            });
        }
    }

    public static void setImageByFileIdOnView(final View view, String fileId, int defaultImageRId, final OnSetImageCallback callback){
        if(defaultImageRId > 0){
            setViewImage(view,defaultImageRId);
        }
        if(StringHelper.isNullOrEmpty(fileId)){
            if(callback!=null){
                callback.onSetImageFail();
            }
            return;
        }
        FileService fileService = ServicesProvider.getService(FileService.class);
        String filePath = fileService.getFilePath(fileId,null);
        if(filePath != null){
            setViewImage(view,filePath);
            if(callback!=null){
                callback.onSetImageSuccess();
            }
        }else {
            fileService.fetchFileToCacheDir(fileId,null, null,new FileService.OnFileListenerAdapter() {

                @Override
                public void onFileSuccess(FileAccessInfo info,Object tag) {
                    setViewImage(view,info.getLocalPath());
                    if(callback != null){
                        callback.onSetImageSuccess();
                    }
                }

                @Override
                public void onFileFailure(FileAccessInfo info, Object tag) {
                    super.onFileFailure(info, tag);
                    if(callback != null){
                        callback.onSetImageFail();
                    }
                }

                @Override
                public void onGetFileInfoError(String fileId, Object tag) {
                    super.onGetFileInfoError(fileId, tag);
                    if(callback != null){
                        callback.onSetImageFail();
                    }
                }
            });
        }
    }

    public static void setViewImage(View view,int resId){
        if(view instanceof ImageButton){
            ((ImageButton) view).setImageResource(resId);
        }else if(view instanceof  ImageView){
            ((ImageView) view).setImageResource(resId);
        }else {
            view.setBackgroundResource(resId);
        }
    }

    private static boolean setViewImage(View view, String imagePath){
        Drawable drawable = Drawable.createFromPath(imagePath);
        if(view instanceof ImageButton){
            ((ImageButton) view).setImageDrawable(drawable);
        }else if(view instanceof  ImageView){
            ((ImageView) view).setImageDrawable(drawable);
        }else {
            view.setBackground(drawable);
        }
        return true;
    }

    public static Bitmap scaleImage(Bitmap bitmap, float scaleRate) {
        Matrix matrix = new Matrix();
        matrix.setScale(scaleRate,scaleRate);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    public static Bitmap scaleImageToWidth(Bitmap bitmap,int width){
        float scaleRate = (float)width / bitmap.getWidth();//缩小的比例
        return scaleImage(bitmap,scaleRate);
    }

    public static Bitmap scaleImageToHeight(Bitmap bitmap,int height){
        float scaleRate = (float)height / bitmap.getHeight();//缩小的比例
        return scaleImage(bitmap,scaleRate);
    }

    public static Bitmap scaleImageToMaxWidth(Bitmap bitmap,int maxWidth){
        if (bitmap.getWidth() > maxWidth){
            return scaleImageToWidth(bitmap,maxWidth);
        }
        return bitmap.copy(bitmap.getConfig(),true);
    }

    public static Bitmap scaleImageToMaxHeight(Bitmap bitmap,int maxHeight){
        if (bitmap.getHeight() > maxHeight){
            return scaleImageToHeight(bitmap,maxHeight);
        }
        return bitmap.copy(bitmap.getConfig(),true);
    }

    public static boolean storeBitmap(Context context, Bitmap bitmap, File file,int quality){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos);
        byte[] newJpeg = baos.toByteArray();
        Log.i(TAG,String.format("Store Image Size:%d * %d",bitmap.getWidth(),bitmap.getHeight()));
        if (FileHelper.saveFile(newJpeg, file)) {
            Log.i(TAG,String.format("Image File Size:%s KB",String.valueOf(file.length() / 1024)));
            return true;
        }else {
            Toast.makeText(context,R.string.save_image_error,Toast.LENGTH_SHORT).show();
            return false;
        }
    }
}
