package cn.bahamut.vessage.services.file;

import android.content.Context;

import java.io.File;

import cn.bahamut.common.StringHelper;
import cn.bahamut.observer.Observable;
import cn.bahamut.observer.ObserverState;
import cn.bahamut.restfulkit.BahamutRFKit;
import cn.bahamut.restfulkit.client.FireClient;
import cn.bahamut.service.OnServiceInit;
import cn.bahamut.service.OnServiceUserLogin;
import cn.bahamut.service.OnServiceUserLogout;
import cn.bahamut.service.ServicesProvider;
import cn.bahamut.vessage.main.VessageConfig;

/**
 * Created by alexchow on 16/4/9.
 */
public class FileService extends Observable implements OnServiceUserLogin,OnServiceUserLogout,OnServiceInit{

    public static final String NOTIFY_FILE_UPLOAD_SUCCESS = "NOTIFY_FILE_UPLOAD_SUCCESS";
    public static final String NOTIFY_FILE_UPLOAD_FAIL = "NOTIFY_FILE_UPLOAD_FAIL";
    public static final String NOTIFY_FILE_UPLOAD_PROGRESS = "NOTIFY_FILE_UPLOAD_PROGRESS";

    public static final String NOTIFY_FILE_DOWNLOAD_SUCCESS = "NOTIFY_FILE_DOWNLOAD_SUCCESS";
    public static final String NOTIFY_FILE_DOWNLOAD_FAIL = "NOTIFY_FILE_DOWNLOAD_FAIL";
    public static final String NOTIFY_FILE_DOWNLOAD_PROGRESS = "NOTIFY_FILE_DOWNLOAD_PROGRESS";

    public class FileNotifyState{
        private FileAccessInfo fileAccessInfo;
        private Object tag;
        private double progress;

        public FileNotifyState(FileAccessInfo fileAccessInfo,Object tag){
            this.fileAccessInfo = fileAccessInfo;
            this.tag = tag;
        }

        public FileAccessInfo getFileAccessInfo() {
            return fileAccessInfo;
        }

        public Object getTag() {
            return tag;
        }

        public double getProgress() {
            return progress;
        }

        void setProgress(double progress) {
            this.progress = progress;
        }
    }

    public interface OnFileTaskListener{
        void onFileSuccess(FileAccessInfo info,Object tag);
        void onFileFailure(FileAccessInfo info, Object tag);
        void onFileProgress(FileAccessInfo info,double progress,Object tag);
    }

    public interface OnFileListener extends OnFileTaskListener{
        void onGetFileInfo(FileAccessInfo info,Object tag);
        void onGetFileInfoError(String fileId, Object tag);
    }

    static public class OnFileListenerAdapter implements OnFileListener{

        @Override
        public void onGetFileInfo(FileAccessInfo info,Object tag) {

        }

        @Override
        public void onGetFileInfoError(String fileId,Object tag) {

        }

        @Override
        public void onFileSuccess(FileAccessInfo info,Object tag) {

        }

        @Override
        public void onFileFailure(FileAccessInfo info, Object tag) {

        }

        @Override
        public void onFileProgress(FileAccessInfo info, double progress,Object tag) {

        }
    }

    private class InnerServiceFileListener extends OnFileListenerAdapter{
        private OnFileListener outterListener;
        public InnerServiceFileListener(OnFileListener listener){
            this.outterListener = listener;
        }

        @Override
        public void onGetFileInfo(FileAccessInfo info,Object tag) {
            if(outterListener != null){
                outterListener.onGetFileInfo(info,tag);
            }
        }

        @Override
        public void onGetFileInfoError(String fileId,Object tag) {
            if(outterListener != null){
                outterListener.onGetFileInfoError(fileId,tag);
            }
        }

        @Override
        public void onFileSuccess(FileAccessInfo info,Object tag) {
            if(outterListener != null){
                outterListener.onFileSuccess(info,tag);
            }
        }

        @Override
        public void onFileFailure(FileAccessInfo info, Object tag) {
            if(outterListener != null){
                outterListener.onFileFailure(info,tag);
            }
        }

        @Override
        public void onFileProgress(FileAccessInfo info, double progress,Object tag) {
            if (outterListener != null) {
                outterListener.onFileProgress(info, progress, tag);
            }
        }
    }

    private Context applicationContext;

    @Override
    public void onUserLogin(String userId) {
        ServicesProvider.setServiceReady(FileService.class);
    }

    @Override
    public void onUserLogout() {
        ServicesProvider.setServiceNotReady(FileService.class);
    }

    @Override
    public void onServiceInit(Context applicationContext) {
        this.applicationContext = applicationContext;
        AliOSSManager.getInstance().initManager(applicationContext, VessageConfig.getBahamutConfig().getAliOssAccessKey(),VessageConfig.getBahamutConfig().getAliOssSecretKey());
    }

    public void uploadFile(String filePath, String fileType, final Object tag, final OnFileListener listener){
        final OnFileListener handler = new InnerServiceFileListener(listener){
            @Override
            public void onFileFailure(FileAccessInfo info, Object tag) {
                super.onFileFailure(info, tag);
                FileNotifyState state = new FileNotifyState(info,tag);
                postNotification(NOTIFY_FILE_UPLOAD_FAIL,state);
            }

            @Override
            public void onFileSuccess(FileAccessInfo info, Object tag) {
                super.onFileSuccess(info, tag);
                FileNotifyState state = new FileNotifyState(info,tag);
                postNotification(NOTIFY_FILE_UPLOAD_SUCCESS,state);
            }

            @Override
            public void onFileProgress(FileAccessInfo info, double progress, Object tag) {
                super.onFileProgress(info, progress, tag);
                FileNotifyState state = new FileNotifyState(info,tag);
                state.setProgress(progress);
                postNotification(NOTIFY_FILE_UPLOAD_PROGRESS,state);
            }
        };
        BahamutRFKit.getClient(FireClient.class).getAliOSSUploadFileAccessInfo(filePath, fileType, new FireClient.OnGetAccessInfo() {
            @Override
            public void onGetAccessInfo(boolean suc, FileAccessInfo info) {
                if(suc){
                    handler.onGetFileInfo(info,tag);
                    if(info.isOnAliOSSServer()){
                        AliOSSManager.getInstance().sendFileToAliOSS(info,tag,handler);
                    }
                }else {
                    handler.onGetFileInfoError(null,tag);
                    ObserverState state = new ObserverState();
                    state.setNotifyType(NOTIFY_FILE_UPLOAD_FAIL);
                    postNotification(state);
                }
            }
        });
    }

    public String getFilePath(String fileId,String fileType){

        File file = getFile(fileId,fileType);
        if(file != null && file.exists()){
            return file.getAbsolutePath();
        }
        return null;
    }

    public File getFile(String fileId,String fileType){
        File file = new File(generateCacheFilePath(fileId,fileType));
        if(file.exists()){
            return file;
        }
        return null;
    }

    private String generateCacheFilePath(String fileId,String fileType){
        String type = "";
        if(!StringHelper.isNullOrEmpty(fileType)){
            if(fileType.startsWith(".")){
                type = fileType;
            }else {
                type = "." + type;
            }
        }

        return String.format("%s/%s%s",applicationContext.getCacheDir().getAbsolutePath(),fileId,type);
    }

    public void fetchFileToCacheDir(String fileId,String fileType,Object tag,OnFileListener listener){
        fetchFile(fileId,fileType,generateCacheFilePath(fileId,fileType),tag,listener);
    }

    public void fetchFile(final String fileId,String fileType, final String saveForPath, final Object tag, final OnFileListener listener){
        if(StringHelper.isNullOrEmpty(fileId)){
            return;
        }
        final OnFileListener handler = new InnerServiceFileListener(listener){
            @Override
            public void onFileFailure(FileAccessInfo info, Object tag) {
                super.onFileFailure(info, tag);
                FileNotifyState state = new FileNotifyState(info,tag);
                postNotification(NOTIFY_FILE_DOWNLOAD_FAIL,state);
            }

            @Override
            public void onFileSuccess(FileAccessInfo info, Object tag) {
                super.onFileSuccess(info, tag);
                FileNotifyState state = new FileNotifyState(info,tag);
                postNotification(NOTIFY_FILE_DOWNLOAD_SUCCESS,state);
            }

            @Override
            public void onFileProgress(FileAccessInfo info, double progress, Object tag) {
                super.onFileProgress(info, progress, tag);
                FileNotifyState state = new FileNotifyState(info,tag);
                postNotification(NOTIFY_FILE_DOWNLOAD_PROGRESS,state);
            }
        };
        String existsPath = getFilePath(fileId,fileType);
        if(!StringHelper.isNullOrEmpty(existsPath)){
            FileAccessInfo info = new FileAccessInfo();
            info.setLocalPath(existsPath);
            info.setFileId(fileId);
            handler.onFileSuccess(info,tag);
            return;
        }
        BahamutRFKit.getClient(FireClient.class).getDownLoadFileAccessInfo(fileId, new FireClient.OnGetAccessInfo() {
            @Override
            public void onGetAccessInfo(boolean suc, FileAccessInfo info) {
                if(suc){
                    handler.onGetFileInfo(info,null);
                    info.setLocalPath(saveForPath);
                    if(info.isOnAliOSSServer()){
                        AliOSSManager.getInstance().downLoadFile(info,tag ,handler);
                    }
                }else {
                    handler.onGetFileInfoError(fileId,null);
                    ObserverState state = new ObserverState();
                    state.setInfo(info);
                    state.setNotifyType(NOTIFY_FILE_DOWNLOAD_FAIL);
                    postNotification(state);
                }
            }
        });
    }

}
