package cn.bahamut.vessage.conversation.view;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
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

import java.io.File;
import java.io.IOException;
import java.util.Date;

import cn.bahamut.common.AnimationHelper;
import cn.bahamut.common.DateHelper;
import cn.bahamut.common.FileHelper;
import cn.bahamut.vessage.R;
import cn.bahamut.vessage.conversation.sendqueue.SendVessageQueue;
import cn.bahamut.vessage.conversation.sendqueue.SendVessageTaskSteps;
import cn.bahamut.vessage.helper.ImageHelper;
import cn.bahamut.vessage.main.UserSetting;
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

    private void handlerNewVessage(int typeId,int actionId) {
        switch (typeId) {
            case Vessage.TYPE_IMAGE:
                if (actionId == 0) {
                    newImageVessageFromAlbum();
                } else {
                    newImageVessageFromCamera();
                }
        }
    }

    private void newImageVessageFromAlbum(){
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);//调用android的图库
        getActivity().startActivityForResult(intent, ActivityRequestCode.IMG_FROM_ALBUM);
    }

    private File imgFile = new File(Environment.getExternalStorageDirectory(), "tmpVsgImg.jpg");
    private void newImageVessageFromCamera(){
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (imgFile.exists()){
            imgFile.delete();
        }
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(imgFile));

        getActivity().startActivityForResult(intent, ActivityRequestCode.CAPTURE_IMG);
    }



    public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ActivityRequestCode.IMG_FROM_ALBUM || requestCode == ActivityRequestCode.CAPTURE_IMG){
            if (resultCode == Activity.RESULT_OK){
                Bitmap bitmap = null;
                Uri uri = ActivityRequestCode.CAPTURE_IMG == requestCode ? Uri.fromFile(imgFile) : data.getData();
                Log.i(TAG,uri.getPath());
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(this.getActivity().getContentResolver(), uri);
                } catch (IOException e) {
                    Toast.makeText(this.getActivity(), R.string.read_image_error, Toast.LENGTH_SHORT).show();
                    return true;
                }

                Bitmap newBitmap = ImageHelper.scaleImageToMaxWidth(bitmap,IMAGE_VESSAGE_IMAGE_WIDTH);
                File tmpImageFile = FileHelper.generateTempFile(getActivity(),"jpg");
                ImageHelper.storeBitmap(this.getActivity(),newBitmap, tmpImageFile,IMAGE_VESSAGE_IMAGE_QUALITY);

                getActivity().startSendingProgress();
                Vessage vessage = new Vessage();
                vessage.isGroup = getPlayManager().getConversation().isGroup;
                vessage.typeId = Vessage.TYPE_IMAGE;
                vessage.extraInfo = getActivity().getSendVessageExtraInfo();
                vessage.sendTime = DateHelper.toAccurateDateTimeString(new Date());
                SendVessageQueue.getInstance().pushSendVessageTask(getPlayManager().getConversation().chatterId,vessage, SendVessageTaskSteps.SEND_FILE_VESSAGE_STEPS,tmpImageFile.getAbsolutePath());
                return true;
            }
        }
        return false;
    }

    public SendMoreTypeVessageManager(ConversationViewActivity activity){
        this.activity = activity;
        initImageChatInputView();
    }

    private ConversationViewActivity getActivity() {
        return activity;
    }

    private ConversationViewPlayManager getPlayManager(){
        return activity.playManager;
    }

    public void showVessageTypesHub() {
        mVessageTypesViewContainer.addView(this.mVessageTypesView);
        AnimationHelper.startAnimation(getActivity(),this.mVessageTypesView,R.anim.view_move_up_anim,showHubAnimationListener);
    }

    public void hideVessageTypesHub(){
        AnimationHelper.startAnimation(getActivity(), this.mVessageTypesView, R.anim.view_move_down_anim, hideHubAnimationListener);
    }

    private Animation.AnimationListener showHubAnimationListener = new Animation.AnimationListener() {
        @Override
        public void onAnimationStart(Animation animation) {
            mVessageTypesListView.setVisibility(View.INVISIBLE);
        }

        @Override
        public void onAnimationEnd(Animation animation) {
            mVessageTypesListView.setVisibility(View.VISIBLE);
        }

        @Override
        public void onAnimationRepeat(Animation animation) {

        }
    };

    private Animation.AnimationListener hideHubAnimationListener = new Animation.AnimationListener() {
        @Override
        public void onAnimationStart(Animation animation) {
            mVessageTypesListView.setVisibility(View.INVISIBLE);
        }

        @Override
        public void onAnimationEnd(Animation animation) {
            mVessageTypesViewContainer.removeView(mVessageTypesView);
        }

        @Override
        public void onAnimationRepeat(Animation animation) {

        }
    };

    private void initImageChatInputView(){
        this.mVessageTypesViewContainer = (ViewGroup) getActivity().findViewById(R.id.vessage_types_container);
        this.mVessageTypesView = (ViewGroup) getActivity().getLayoutInflater().inflate(R.layout.vessage_types_view,null);

        this.mVessageTypesView.setOnClickListener(onClickListener);

        this.mVessageTypesListView = (RecyclerView) mVessageTypesView.findViewById(R.id.list_view);

        this.vessageTypeGralleryAdapter = new VessageTypeGralleryAdapter(getActivity());
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        this.mVessageTypesListView.setLayoutManager(linearLayoutManager);
        this.mVessageTypesListView.setAdapter(this.vessageTypeGralleryAdapter);
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(final View v) {
            if (v.getId() == R.id.root_view_v){
                hideVessageTypesHub();
                return;
            }
        }
    };

    public void onDestory(){
        vessageTypeGralleryAdapter.onDestory();
    }

    static class VessageTypeInfo{
        int action = 0;
        int typeIconResId;
        int typeTitleResId;
        int vessageTypeId;

        VessageTypeInfo(int action,int vessageTypeId,int typeIconResId,int typeTitleResId) {
            this.action = action;
            this.typeIconResId = typeIconResId;
            this.typeTitleResId = typeTitleResId;
            this.vessageTypeId = vessageTypeId;
        }
    }

    class VessageTypeGralleryAdapter extends RecyclerView.Adapter<VessageTypeGralleryAdapter.ViewHolder>{
        private LayoutInflater mInflater;
        private Activity context;

        VessageTypeGralleryAdapter(Activity context){
            this.context = context;
            mInflater = this.context.getLayoutInflater();
        }

        public void onDestory(){

        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = mInflater.inflate(R.layout.vessage_types_item_view, parent, false);
            ViewHolder viewHolder = new ViewHolder(view);
            viewHolder.imageView = (ImageView)view.findViewById(R.id.type_icon);
            viewHolder.titleView = (TextView)view.findViewById(R.id.type_title);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, final int position) {
            VessageTypeInfo info = vessageTypes[position];
            holder.titleView.setText(info.typeTitleResId);
            holder.imageView.setImageResource(info.typeIconResId);
            holder.itemView.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
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
            handlerNewVessage(info.vessageTypeId,info.action);
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
