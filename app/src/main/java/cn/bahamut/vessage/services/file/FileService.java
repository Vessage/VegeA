package cn.bahamut.vessage.services.file;

import android.content.Context;

import cn.bahamut.observer.Observable;
import cn.bahamut.observer.ObserverState;
import cn.bahamut.restfulkit.BahamutRFKit;
import cn.bahamut.restfulkit.client.FireClient;
import cn.bahamut.service.OnServiceInit;
import cn.bahamut.service.OnServiceUserLogin;
import cn.bahamut.service.OnServiceUserLogout;
import cn.bahamut.service.ServicesProvider;
import cn.bahamut.vessage.main.AppMain;
import cn.bahamut.vessage.main.VessageConfig;
import cn.bahamut.vessage.models.Conversation;

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

    static public interface OnFileTaskListener{
        void onFileSuccess(FileAccessInfo info,Object tag);
        void onFileFailure(FileAccessInfo info, Object tag);
        void onFileProgress(FileAccessInfo info,double progress,Object tag);
    }

    static public interface OnFileListener extends OnFileTaskListener{
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

    static private OnFileListener defaultListener = new OnFileListenerAdapter();

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
        final OnFileListener handler;
        if(listener == null){
            handler = defaultListener;
        }else {
            handler = listener;
        }
        BahamutRFKit.getClient(FireClient.class).getAliOSSUploadFileAccessInfo(filePath, fileType, new FireClient.OnGetAccessInfo() {
            @Override
            public void onGetAccessInfo(boolean suc, FileAccessInfo info) {
                if(suc){
                    handler.onGetFileInfo(info,tag);
                    if(info.isOnAliOSSServer()){
                        AliOSSManager.getInstance().sendFileToAliOSS(info,tag,listener);
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

    public void fetchFileToCacheDir(String fileId,Object tag,OnFileListener listener){
        fetchFile(fileId,applicationContext.getCacheDir().getAbsolutePath() + "/" + fileId,tag,listener);
    }

    public void fetchFile(final String fileId, final String saveForPath, final Object tag, final OnFileListener listener){
        final OnFileListener handler;
        if(listener == null){
            handler = defaultListener;
        }else {
            handler = listener;
        }
        BahamutRFKit.getClient(FireClient.class).getDownLoadFileAccessInfo(fileId, new FireClient.OnGetAccessInfo() {
            @Override
            public void onGetAccessInfo(boolean suc, FileAccessInfo info) {
                if(suc){
                    handler.onGetFileInfo(info,null);
                    info.setLocalPath(saveForPath);
                    if(info.isOnAliOSSServer()){
                        AliOSSManager.getInstance().downLoadFile(info,tag ,listener);
                    }
                }else {
                    handler.onGetFileInfoError(fileId,null);
                    ObserverState state = new ObserverState();
                    state.setNotifyType(NOTIFY_FILE_UPLOAD_FAIL);
                    postNotification(state);
                }
            }
        });
    }

}
