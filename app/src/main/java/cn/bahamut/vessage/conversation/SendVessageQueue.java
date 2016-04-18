package cn.bahamut.vessage.conversation;

import java.io.File;

import cn.bahamut.observer.Observable;
import cn.bahamut.service.ServicesProvider;
import cn.bahamut.vessage.models.SendVessageTask;
import cn.bahamut.vessage.services.UserService;
import cn.bahamut.vessage.services.VessageService;
import cn.bahamut.vessage.services.file.FileAccessInfo;
import cn.bahamut.vessage.services.file.FileService;

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

    private void onSendCompleted(boolean isOk, final SendVessageTask task){
        if(isOk){
            ServicesProvider.getService(FileService.class).uploadFile(task.videoPath,".mp4",task,new FileService.OnFileListenerAdapter(){
                @Override
                public void onFileSuccess(FileAccessInfo info,Object tag) {
                    super.onFileSuccess(info,tag);
                    ServicesProvider.getService(VessageService.class).finishSendVessage(task.vessageBoxId,task.vessageId);
                }
            });
        }else {

        }
    }

    public boolean sendVessageToUser(String userId, File videoFile) {
        VessageService vessageService = ServicesProvider.getService(VessageService.class);
        UserService userService = ServicesProvider.getService(UserService.class);

        vessageService.sendVessageToUser(userId, videoFile.getAbsolutePath(), userService.getMyProfile().nickName, userService.getMyProfile().mobile, new VessageService.OnSendVessageCompleted() {
            @Override
            public void onSendVessageCompleted(boolean isOk, SendVessageTask taskModel) {
                onSendCompleted(isOk,taskModel);
            }
        });
        return true;
    }

    public boolean sendVessageToMobile(String mobile, File videoFile) {
        VessageService vessageService = ServicesProvider.getService(VessageService.class);
        UserService userService = ServicesProvider.getService(UserService.class);

        vessageService.sendVessageToMobile(mobile, videoFile.getAbsolutePath(), userService.getMyProfile().nickName, userService.getMyProfile().mobile, new VessageService.OnSendVessageCompleted() {
            @Override
            public void onSendVessageCompleted(boolean isOk, SendVessageTask taskModel) {
                onSendCompleted(isOk,taskModel);
            }
        });
        return true;
    }
}
