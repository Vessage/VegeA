package cn.bahamut.vessage.helper;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;

import cn.bahamut.common.StringHelper;
import cn.bahamut.service.ServicesProvider;
import cn.bahamut.vessage.services.file.FileAccessInfo;
import cn.bahamut.vessage.services.file.FileService;

/**
 * Created by alexchow on 16/4/13.
 */
public class ImageHelper {

    public static class OnSetImageCallback{
        public void onSetImageSuccess(){}
        public void onSetImageFail(){}
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

    private static void setViewImage(View view,int resId){
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
}
