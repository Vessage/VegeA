package cn.bahamut.vessage.conversation.chat;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.umeng.analytics.MobclickAgent;

import java.io.File;
import java.io.IOException;

import cn.bahamut.common.AndroidHelper;
import cn.bahamut.common.AnimationHelper;
import cn.bahamut.common.FileHelper;
import cn.bahamut.common.StringHelper;
import cn.bahamut.vessage.R;
import cn.bahamut.vessage.conversation.sendqueue.SendVessageQueue;
import cn.bahamut.vessage.conversation.sendqueue.SendVessageTaskSteps;
import cn.bahamut.vessage.helper.ImageHelper;
import cn.bahamut.vessage.services.conversation.Conversation;
import cn.bahamut.vessage.services.vessage.Vessage;

/**
 * Created by alexchow on 2016/9/26.
 */

public class SendMoreTypeVessageManager {
    private static String TAG = "SMTVM";

    private static final int IMAGE_VESSAGE_IMAGE_WIDTH = 600;
    private static final int IMAGE_VESSAGE_IMAGE_QUALITY = 60;
    private ConversationViewActivity activity;
    private RecyclerView mVessageTypesListView;
    private ViewGroup mVessageTypesViewContainer;
    private ViewGroup mVessageTypesView;
    private VessageTypeGralleryAdapter vessageTypeGralleryAdapter;

    static VessageTypeInfo[] vessageTypes = new VessageTypeInfo[]{
            new VessageTypeInfo(0, Vessage.TYPE_IMAGE, R.mipmap.picture, R.string.vsg_type_photo_album),
            new VessageTypeInfo(1, Vessage.TYPE_IMAGE, R.mipmap.camera, R.string.vsg_type_photo_camera)
    };

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
        return handleImageResult(requestCode, resultCode, data) ||
                handleRecordChatVideoResult(requestCode, resultCode, data);
    }

    private boolean handleImageResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ActivityRequestCode.IMG_FROM_ALBUM || requestCode == ActivityRequestCode.CAPTURE_IMG) {
            if (resultCode == Activity.RESULT_OK) {
                Bitmap bitmap = null;
                Uri uri = ActivityRequestCode.CAPTURE_IMG == requestCode ? Uri.fromFile(imgFile) : data.getData();
                Log.i(TAG, uri.getPath());
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(this.getActivity().getContentResolver(), uri);
                } catch (IOException e) {
                    Toast.makeText(this.getActivity(), R.string.read_image_error, Toast.LENGTH_SHORT).show();
                    return true;
                }

                Bitmap newBitmap = ImageHelper.scaleImageToMaxWidth(bitmap, IMAGE_VESSAGE_IMAGE_WIDTH);
                File tmpImageFile = FileHelper.generateTempFile(getActivity(), "jpg");
                ImageHelper.storeBitmap(this.getActivity(), newBitmap, tmpImageFile, IMAGE_VESSAGE_IMAGE_QUALITY);

                getActivity().startSendingProgress();
                Vessage vessage = new Vessage();
                boolean isGroup = getPlayManager().getConversation().type == Conversation.TYPE_GROUP_CHAT;
                vessage.typeId = Vessage.TYPE_IMAGE;
                SendVessageQueue.getInstance().pushSendVessageTask(getPlayManager().getConversation().chatterId, isGroup, vessage, SendVessageTaskSteps.SEND_FILE_VESSAGE_STEPS, tmpImageFile.getAbsolutePath());
            }
            return true;
        }
        return false;
    }


    private boolean handleRecordChatVideoResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ActivityRequestCode.RECORD_CHAT_VIDEO_REQUEST_ID) {
            if (resultCode == Activity.RESULT_OK) {
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
        if (!StringHelper.isNullOrEmpty(getPlayManager().getConversation().chatterId)) {
            getActivity().startSendingProgress();
            Vessage vessage = new Vessage();
            boolean isGroup = getPlayManager().getConversation().type == Conversation.TYPE_GROUP_CHAT;
            vessage.typeId = Vessage.TYPE_CHAT_VIDEO;
            /*
            if (AndroidHelper.isEmulator(getActivity())) {
                vessage.fileId = "5790435e99cc251974a42f61";
                new File(filePath).delete();
                SendVessageQueue.getInstance().pushSendVessageTask(getPlayManager().getConversation().chatterId, isGroup, vessage, SendVessageTaskSteps.SEND_NORMAL_VESSAGE_STEPS, null);
            } else {
            }
            */
            SendVessageQueue.getInstance().pushSendVessageTask(getPlayManager().getConversation().chatterId, isGroup, vessage, SendVessageTaskSteps.SEND_FILE_VESSAGE_STEPS, filePath);
        }
    }

    public SendMoreTypeVessageManager(ConversationViewActivity activity) {
        this.activity = activity;
        initImageChatInputView();
    }

    private ConversationViewActivity getActivity() {
        return activity;
    }

    private PlayVessageManager getPlayManager() {
        return activity.playManager;
    }

    public void showVessageTypesHub() {
        if (this.mVessageTypesView.getParent() == null) {
            mVessageTypesViewContainer.addView(this.mVessageTypesView);
        }
        mVessageTypesViewContainer.setVisibility(View.VISIBLE);
        AnimationHelper.startAnimation(getActivity(), this.mVessageTypesView, R.anim.view_move_up_anim, showHubAnimationListener);
    }

    public void hideVessageTypesHub() {
        AnimationHelper.startAnimation(getActivity(), this.mVessageTypesView, R.anim.view_move_down_anim, hideHubAnimationListener);
    }

    private Animation.AnimationListener showHubAnimationListener = new AnimationHelper.AnimationListenerAdapter() {

    };

    private Animation.AnimationListener hideHubAnimationListener = new AnimationHelper.AnimationListenerAdapter() {
        @Override
        public void onAnimationEnd(Animation animation) {
            mVessageTypesViewContainer.setVisibility(View.INVISIBLE);
        }
    };

    private void initImageChatInputView() {
        this.mVessageTypesViewContainer = (ViewGroup) getActivity().findViewById(R.id.vessage_types_container);
        this.mVessageTypesView = (ViewGroup) getActivity().getLayoutInflater().inflate(R.layout.conversation_vsg_types_container, null);

        this.mVessageTypesListView = (RecyclerView) mVessageTypesView.findViewById(R.id.list_view);
        this.mVessageTypesView.setOnClickListener(onClickListener);
        this.mVessageTypesView.findViewById(R.id.bottom_view_disable).setOnClickListener(onClickListener);
        this.vessageTypeGralleryAdapter = new VessageTypeGralleryAdapter(getActivity());
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        this.mVessageTypesListView.setLayoutManager(linearLayoutManager);
        this.mVessageTypesListView.setAdapter(this.vessageTypeGralleryAdapter);
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(final View v) {
            hideVessageTypesHub();
        }
    };

    public void onDestory() {
        vessageTypeGralleryAdapter.onDestory();
    }

    static class VessageTypeInfo {
        int action = 0;
        int typeIconResId;
        int typeTitleResId;
        int vessageTypeId;

        VessageTypeInfo(int action, int vessageTypeId, int typeIconResId, int typeTitleResId) {
            this.action = action;
            this.typeIconResId = typeIconResId;
            this.typeTitleResId = typeTitleResId;
            this.vessageTypeId = vessageTypeId;
        }
    }

    class VessageTypeGralleryAdapter extends RecyclerView.Adapter<VessageTypeGralleryAdapter.ViewHolder> {
        private LayoutInflater mInflater;
        private Activity context;

        VessageTypeGralleryAdapter(Activity context) {
            this.context = context;
            mInflater = this.context.getLayoutInflater();
        }

        public void onDestory() {

        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = mInflater.inflate(R.layout.conversation_vsg_types_view, parent, false);
            ViewHolder viewHolder = new ViewHolder(view);
            viewHolder.imageView = (ImageView) view.findViewById(R.id.type_icon);
            viewHolder.titleView = (TextView) view.findViewById(R.id.type_title);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, final int position) {
            VessageTypeInfo info = vessageTypes[position];
            holder.titleView.setText(info.typeTitleResId);
            holder.imageView.setImageResource(info.typeIconResId);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onItemClick(holder.itemView, position);
                }
            });
        }

        @Override
        public int getItemCount() {
            return vessageTypes.length;
        }

        public void onItemClick(View view, int position) {
            hideVessageTypesHub();
            VessageTypeInfo info = vessageTypes[position];
            handlerNewVessage(info.vessageTypeId, info.action);
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            public ImageView imageView;
            public TextView titleView;

            public ViewHolder(View itemView) {
                super(itemView);
            }
        }
    }
}
