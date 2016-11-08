package cn.bahamut.vessage.conversation.sendqueue.handlers;

import java.io.File;
import java.util.HashMap;

import cn.bahamut.common.StringHelper;
import cn.bahamut.service.ServicesProvider;
import cn.bahamut.vessage.conversation.sendqueue.SendVessageQueue;
import cn.bahamut.vessage.conversation.sendqueue.SendVessageQueueStepHandler;
import cn.bahamut.vessage.conversation.sendqueue.SendVessageQueueTask;
import cn.bahamut.vessage.services.file.FileAccessInfo;
import cn.bahamut.vessage.services.file.FileService;
import io.realm.Realm;

/**
 * Created by alexchow on 16/8/2.
 */
public class SendAliOSSFileHandler implements SendVessageQueueStepHandler {
    public static final String HANDLER_NAME = "SendAliOSSFile";
    private HashMap<String,SendVessageQueueTask> uploadingTasks;
    @Override
    public void initHandler(SendVessageQueue queue) {
        uploadingTasks = new HashMap<>();
    }

    @Override
    public void releaseHandler() {
        uploadingTasks.clear();
    }

    @Override
    public void doTask(final SendVessageQueue queue, SendVessageQueueTask task) {
        int index = task.filePath.lastIndexOf(".");
        String fileType = task.filePath.substring(index);
        if (StringHelper.isStringNullOrWhiteSpace(fileType)){
            fileType = ".raw";
        }
        uploadingTasks.put(task.taskId,task.copyToObject());
        ServicesProvider.getService(FileService.class).uploadFile(task.filePath,fileType,task.taskId,new FileService.OnFileListenerAdapter(){
            @Override
            public void onFileSuccess(FileAccessInfo info, Object tag) {
                Realm realm = queue.getRealm();
                realm.beginTransaction();
                SendVessageQueueTask task = realm.where(SendVessageQueueTask.class).equalTo("taskId",(String)tag).findFirst();
                task.vessage.fileId = info.getFileId();
                realm.commitTransaction();
                uploadingTasks.remove(tag);
                queue.nextStep(task);
            }

            @Override
            public void onFileProgress(FileAccessInfo info, double progress, Object tag) {
                super.onFileProgress(info, progress,tag);
                SendVessageQueueTask task = uploadingTasks.get(tag);
                if (task != null){
                    queue.notifyTaskStepProgress(task, progress);
                }
            }

            @Override
            public void onFileFailure(FileAccessInfo info, Object tag) {
                super.onFileFailure(info, tag);
                Realm realm = queue.getRealm();
                SendVessageQueueTask task = realm.where(SendVessageQueueTask.class).equalTo("taskId",(String)tag).findFirst();
                queue.doTaskError(task, 0, "UPLOAD_FILE_ERROR");
            }
        });
    }
}
