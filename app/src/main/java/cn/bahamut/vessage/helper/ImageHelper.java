package cn.bahamut.vessage.helper;

import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;

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
        ServicesProvider.getService(FileService.class).fetchFileToCacheDir(fileId, new FileService.OnFileListenerAdapter() {

            @Override
            public void onFileSuccess(FileAccessInfo info) {
                setViewImage(view,info.getLocalPath());
            }

        });
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
        if(view instanceof ImageButton){

        }else if(view instanceof  ImageView){

        }else {

        }
    }
}
