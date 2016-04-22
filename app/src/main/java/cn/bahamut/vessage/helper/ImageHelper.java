package cn.bahamut.vessage.helper;

import android.graphics.drawable.Drawable;
import android.os.Build;
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
        if(defaultImageRId > 0){
            setViewImage(view,defaultImageRId);
        }
        if(StringHelper.isStringNullOrEmpty(fileId)){
            return;
        }
        FileService fileService = ServicesProvider.getService(FileService.class);
        String filePath = fileService.getFilePath(fileId);
        if(filePath != null){
            setViewImage(view,filePath);
        }else {
            fileService.fetchFileToCacheDir(fileId, null,new FileService.OnFileListenerAdapter() {

                @Override
                public void onFileSuccess(FileAccessInfo info,Object tag) {
                    setViewImage(view,info.getLocalPath());
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

    private static void setViewImage(View view, String imagePath){
        Drawable drawable = Drawable.createFromPath(imagePath);
        if(view instanceof ImageButton){
            ((ImageButton) view).setImageDrawable(drawable);
        }else if(view instanceof  ImageView){
            ((ImageView) view).setImageDrawable(drawable);
        }else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                view.setBackground(drawable);
            }else {

            }
        }
    }
}
