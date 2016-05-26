package cn.bahamut.vessage.conversation;

import java.io.File;

import cn.bahamut.observer.Observable;
import cn.bahamut.service.ServicesProvider;
import cn.bahamut.vessage.services.file.FileAccessInfo;
import cn.bahamut.vessage.services.file.FileService;
import cn.bahamut.vessage.services.user.UserService;
import cn.bahamut.vessage.services.vessage.SendVessageTask;
import cn.bahamut.vessage.services.vessage.VessageService;
import io.realm.Realm;

/**
 * Created by alexchow on 16/4/12.
 */
public class SendVessageQueue extends Observable {

    static private SendVessageQueue instance;
    public static SendVessageQueue getInstance() {
        if(instance == null){
            instance = new SendVessageQueue();
        }
        return instance;
    }

    private void onSendCompleted(boolean isOk, String sendedVessageId){
        if(isOk){
            final Realm realm = ServicesProvider.getService(VessageService.class).getRealm();
            SendVessageTask task = realm.where(SendVessageTask.class).equalTo("vessageId",sendedVessageId).findFirst();
            if(task != null){
                ServicesProvider.getService(FileService.class).uploadFile(task.videoPath,".mp4",sendedVessageId,new FileService.OnFileListenerAdapter(){
                    @Override
                    public void onFileSuccess(FileAccessInfo info,Object tag) {
                        String sendedVessageId = (String) tag;
                        SendVessageTask task = realm.where(SendVessageTask.class).equalTo("vessageId", sendedVessageId).findFirst();
                        realm.beginTransaction();
                        task.fileId = info.getFileId();
                        realm.commitTransaction();
                        ServicesProvider.getService(VessageService.class).finishSendVessage(info.getFileId(), task.vessageBoxId, task.vessageId);
                    }
                });
                return;
            }

        }
        //TODO: error process
    }

    public boolean sendVessageToUser(String userId, File videoFile) {
        VessageService vessageService = ServicesProvider.getService(VessageService.class);
        UserService userService = ServicesProvider.getService(UserService.class);

        vessageService.sendVessageToUser(userId, videoFile.getAbsolutePath(), userService.getMyProfile().nickName, userService.getMyProfile().mobile, new VessageService.OnSendVessageCompleted() {
            @Override
            public void onSendVessageCompleted(boolean isOk, String sendedVessageId) {
                onSendCompleted(isOk,sendedVessageId);
            }
        });
        return true;
    }

    public boolean sendVessageToMobile(String mobile, File videoFile) {
        VessageService vessageService = ServicesProvider.getService(VessageService.class);
        UserService userService = ServicesProvider.getService(UserService.class);

        vessageService.sendVessageToMobile(mobile, videoFile.getAbsolutePath(), userService.getMyProfile().nickName, userService.getMyProfile().mobile, new VessageService.OnSendVessageCompleted() {
            @Override
            public void onSendVessageCompleted(boolean isOk, String sendedVessageId) {
                onSendCompleted(isOk,sendedVessageId);
            }
        });
        return true;
    }
}
