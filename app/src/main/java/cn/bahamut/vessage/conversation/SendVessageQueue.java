package cn.bahamut.vessage.conversation;

import org.apache.commons.codec1.digest.DigestUtils;

import java.io.File;
import java.util.Dictionary;
import java.util.Hashtable;

import cn.bahamut.common.StringHelper;
import cn.bahamut.observer.Observable;
import cn.bahamut.observer.Observer;
import cn.bahamut.observer.ObserverState;
import cn.bahamut.service.ServicesProvider;
import cn.bahamut.vessage.main.UserSetting;
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
    public static final String ON_SENDED_VESSAGE = "ON_SENDED_VESSAGE";
    public static final String ON_SENDING_PROGRESS = "ON_SENDING_PROGRESS";
    public static final String ON_SEND_VESSAGE_FAILURE = "ON_SEND_VESSAGE_FAILURE";

    public class SendingInfo{

        public static final int STATE_SENDING = 10;
        public static final int STATE_SENDED = 11;
        public static final int STATE_SEND_FINISH_FAILURE = -10;
        public static final int STATE_SEND_FILE_FAILURE = -11;

        public String sendingVessageId;
        public String receiverId;
        public double progress;
        public int state = STATE_SENDING;
    }

    private String sendVessageExtraInfo;
    private String getSendVessageExtraInfo(){
        if(sendVessageExtraInfo == null){
            generateVessageExtraInfo();
        }
        return sendVessageExtraInfo;
    }

    static private SendVessageQueue instance;
    public static SendVessageQueue getInstance() {
        if(instance == null){
            instance = new SendVessageQueue();
        }
        return instance;
    }

    private Dictionary<String,String> sendingUser = new Hashtable<>();

    public void init() {
        ServicesProvider.getService(VessageService.class).addObserver(VessageService.NOTIFY_NEW_VESSAGE_SENDED,onVessageSended);
        ServicesProvider.getService(VessageService.class).addObserver(VessageService.NOTIFY_FINISH_SEND_VESSAGE_FAILED,onVessageSendFailure);
    }

    public void release(){
        sendingUser = new Hashtable<>();
        ServicesProvider.getService(VessageService.class).deleteObserver(VessageService.NOTIFY_FINISH_SEND_VESSAGE_FAILED,onVessageSendFailure);
        ServicesProvider.getService(VessageService.class).deleteObserver(VessageService.NOTIFY_NEW_VESSAGE_SENDED,onVessageSended);
    }

    private Observer onVessageSendFailure = new Observer() {
        @Override
        public void update(ObserverState state) {
            SendVessageTask task = (SendVessageTask) state.getInfo();
            SendingInfo info = generateSendingInfo(task);
            info.state = SendingInfo.STATE_SEND_FINISH_FAILURE;
            postNotification(ON_SEND_VESSAGE_FAILURE,info);
        }
    };

    private Observer onVessageSended = new Observer() {
        @Override
        public void update(ObserverState state) {
            SendVessageTask task = (SendVessageTask) state.getInfo();
            sendingUser.remove(task.vessageId);
            SendingInfo info = generateSendingInfo(task);
            info.state = SendingInfo.STATE_SENDED;
            postNotification(ON_SENDED_VESSAGE,info);
        }
    };

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

                    @Override
                    public void onFileProgress(FileAccessInfo info, double progress, final Object tag) {
                        super.onFileProgress(info, progress, tag);
                        String sendedVessageId = (String) tag;
                        SendingInfo state = generateSendingInfo(sendedVessageId);
                        state.progress = progress;
                        state.state = SendingInfo.STATE_SENDING;
                        postNotification(ON_SENDING_PROGRESS,state);
                    }

                    @Override
                    public void onFileFailure(FileAccessInfo info, Object tag) {
                        super.onFileFailure(info, tag);
                        String sendedVessageId = (String) tag;
                        SendingInfo state = generateSendingInfo(sendedVessageId);
                        state.progress = 20;
                        state.state = SendingInfo.STATE_SEND_FILE_FAILURE;
                        postNotification(ON_SEND_VESSAGE_FAILURE,state);
                    }
                });
                return;
            }

        }
    }

    private SendingInfo generateSendingInfo(String vessageId) {
        SendingInfo state = new SendingInfo();
        state.receiverId = sendingUser.get(vessageId);
        state.sendingVessageId = vessageId;
        return state;
    }

    private SendingInfo generateSendingInfo(SendVessageTask task) {
        SendingInfo state = new SendingInfo();
        state.receiverId = task.receiverId;
        state.sendingVessageId = task.vessageId;
        return state;
    }

    private void generateVessageExtraInfo(){
        UserService userService = ServicesProvider.getService(UserService.class);
        String nick = userService.getMyProfile().nickName;
        String mobile = userService.getMyProfile().mobile;
        String mobileHash = "";
        if(!StringHelper.isStringNullOrWhiteSpace(mobile)){
            mobileHash = DigestUtils.md5Hex(mobile);
        }
        sendVessageExtraInfo = String.format("{\"accountId\":\"%s\",\"nickName\":\"%s\",\"mobileHash\":\"%s\"}", UserSetting.getLastUserLoginedAccount(),nick,mobileHash);
    }

    public boolean sendVessageToUser(final String receiverId, File videoFile,boolean isGroup) {
        VessageService vessageService = ServicesProvider.getService(VessageService.class);

        vessageService.sendVessageToReceiver(receiverId, videoFile.getAbsolutePath(), isGroup, getSendVessageExtraInfo(), new VessageService.OnSendVessageCompleted() {
            @Override
            public void onSendVessageCompleted(boolean isOk, String sendedVessageId) {
                sendingUser.put(sendedVessageId, receiverId);
                onSendCompleted(isOk, sendedVessageId);
            }
        });
        return true;
    }

    public boolean sendVessageToMobile(final String mobile, File videoFile) {
        VessageService vessageService = ServicesProvider.getService(VessageService.class);
        vessageService.sendVessageToMobile(mobile, videoFile.getAbsolutePath(), getSendVessageExtraInfo(), new VessageService.OnSendVessageCompleted() {
            @Override
            public void onSendVessageCompleted(boolean isOk, String sendedVessageId) {
                sendingUser.put(sendedVessageId,mobile);
                onSendCompleted(isOk,sendedVessageId);
            }
        });
        return true;
    }
}
