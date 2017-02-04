package cn.bahamut.vessage.conversation.chat;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.Toast;

import com.nguyenhoanglam.imagepicker.activity.ImagePicker;
import com.nguyenhoanglam.imagepicker.activity.ImagePickerActivity;
import com.nguyenhoanglam.imagepicker.model.Image;
import com.umeng.analytics.MobclickAgent;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import cn.bahamut.common.FileHelper;
import cn.bahamut.common.StringHelper;
import cn.bahamut.vessage.R;
import cn.bahamut.vessage.conversation.sendqueue.SendVessageQueue;
import cn.bahamut.vessage.conversation.sendqueue.SendVessageTaskSteps;
import cn.bahamut.vessage.helper.ImageHelper;
import cn.bahamut.vessage.main.LocalizedStringHelper;
import cn.bahamut.vessage.services.conversation.Conversation;
import cn.bahamut.vessage.services.vessage.Vessage;

import static android.app.Activity.RESULT_OK;

/**
 * Created by alexchow on 2016/9/26.
 */

public class SendMoreTypeVessageManager {
    private static String TAG = "SMTVM";

    private static final int IMAGE_VESSAGE_IMAGE_WIDTH = 600;
    private static final int IMAGE_VESSAGE_IMAGE_QUALITY = 60;
    private ConversationViewActivity activity;

    private void handlerNewVessage(int typeId, int actionId) {
        switch (typeId) {
            case Vessage.TYPE_IMAGE:
                if (actionId == 0) {
                    newImageVessageFromAlbum();
                } else {
                    newImageVessageFromCamera();
                }
        }
    }

    private void newImageVessageFromAlbum() {
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);//调用android的图库
        getActivity().startActivityForResult(intent, ActivityRequestCode.IMG_FROM_ALBUM);
    }

    private File imgFile = new File(Environment.getExternalStorageDirectory(), "tmpVsgImg.jpg");

    private void newImageVessageFromCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (imgFile.exists()) {
            imgFile.delete();
        }
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(imgFile));

        getActivity().startActivityForResult(intent, ActivityRequestCode.CAPTURE_IMG);
    }

    public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
        return handleImagePickerResult(requestCode, resultCode, data) ||
                handleImageResult(requestCode, resultCode, data) ||
                handleRecordChatVideoResult(requestCode, resultCode, data);
    }

    private boolean handleImagePickerResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ActivityRequestCode.IMAGE_PICKER_REQ && resultCode == RESULT_OK && data != null) {
            ArrayList<Image> images = data.getParcelableArrayListExtra(ImagePickerActivity.INTENT_EXTRA_SELECTED_IMAGES);

            Uri uri = Uri.fromFile(new File(images.get(0).getPath()));
            sendImageWithUri(uri);
            return true;
        }
        return false;
    }

    private boolean handleImageResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ActivityRequestCode.IMG_FROM_ALBUM || requestCode == ActivityRequestCode.CAPTURE_IMG) {
            if (resultCode == RESULT_OK) {
                Uri uri = ActivityRequestCode.CAPTURE_IMG == requestCode ? Uri.fromFile(imgFile) : data.getData();
                sendImageWithUri(uri);
            }
            return true;
        }
        return false;
    }

    private void sendImageWithUri(Uri uri) {
        Bitmap bitmap = null;
        Log.i(TAG, uri.getPath());
        try {
            bitmap = MediaStore.Images.Media.getBitmap(this.getActivity().getContentResolver(), uri);
        } catch (IOException e) {
            Toast.makeText(this.getActivity(), R.string.read_image_error, Toast.LENGTH_SHORT).show();
            return;
        }
        Bitmap newBitmap = ImageHelper.scaleImageToMaxWidth(bitmap, IMAGE_VESSAGE_IMAGE_WIDTH);
        File tmpImageFile = FileHelper.generateTempFile(getActivity(), "jpg");
        ImageHelper.storeBitmap2JPEG(this.getActivity(), newBitmap, tmpImageFile, IMAGE_VESSAGE_IMAGE_QUALITY);

        getActivity().startSendingProgress();
        Vessage vessage = new Vessage();
        boolean isGroup = getActivity().getConversation().type == Conversation.TYPE_GROUP_CHAT;
        vessage.typeId = Vessage.TYPE_IMAGE;
        SendVessageQueue.getInstance().pushSendVessageTask(getActivity().getConversation().chatterId, isGroup, vessage, SendVessageTaskSteps.SEND_FILE_VESSAGE_STEPS, tmpImageFile.getAbsolutePath());
    }


    private boolean handleRecordChatVideoResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ActivityRequestCode.RECORD_CHAT_VIDEO_REQUEST_ID) {
            if (resultCode == RESULT_OK) {
                final String filePath = data.getStringExtra("file");
                if (StringHelper.isStringNullOrWhiteSpace(filePath) == false) {

                    if (data.getBooleanExtra("confirm", false)) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                                .setTitle(R.string.ask_send_vessage)
                                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        sendVessageVideo(filePath);
                                    }
                                });

                        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                MobclickAgent.onEvent(getActivity(), "Vege_CancelSendVessage");
                            }
                        });
                        builder.setCancelable(false);
                        builder.show();
                    } else {
                        sendVessageVideo(filePath);
                    }
                }
            }
            return true;
        }

        return false;
    }

    private void sendVessageVideo(String filePath) {
        if (!StringHelper.isNullOrEmpty(getActivity().getConversation().chatterId)) {
            getActivity().startSendingProgress();
            Vessage vessage = new Vessage();
            boolean isGroup = getActivity().getConversation().type == Conversation.TYPE_GROUP_CHAT;
            vessage.typeId = Vessage.TYPE_CHAT_VIDEO;
            /*
            if (AndroidHelper.isEmulator(getActivity())) {
                vessage.fileId = "5790435e99cc251974a42f61";
                new File(filePath).delete();
                SendVessageQueue.getInstance().pushSendVessageTask(getPlayManager().getConversation().chatterId, isGroup, vessage, SendVessageTaskSteps.SEND_NORMAL_VESSAGE_STEPS, null);
            } else {
            }
            */
            SendVessageQueue.getInstance().pushSendVessageTask(getActivity().getConversation().chatterId, isGroup, vessage, SendVessageTaskSteps.SEND_FILE_VESSAGE_STEPS, filePath);
        }
    }

    public SendMoreTypeVessageManager(ConversationViewActivity activity) {
        this.activity = activity;
        //initImageChatInputView();
    }

    private ConversationViewActivity getActivity() {
        return activity;
    }

    public void showVessageTypesHub() {
        ImagePicker.create(getActivity())
                .single()
                .showCamera(true)
                .imageTitle(LocalizedStringHelper.getLocalizedString(R.string.choose_or_capture_img))
                .start(ActivityRequestCode.IMAGE_PICKER_REQ);
    }

    public void onDestory() {

    }
}
