package cn.bahamut.vessage.conversation.view;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import cn.bahamut.common.AnimationHelper;
import cn.bahamut.vessage.R;
import cn.bahamut.vessage.helper.ImageHelper;
import cn.bahamut.vessage.services.vessage.Vessage;

/**
 * Created by alexchow on 2016/9/26.
 */

public class SendMoreTypeVessageManager {
    private ConversationViewActivity activity;
    private RecyclerView mVessageTypesListView;
    private ViewGroup mVessageTypesViewContainer;
    private ViewGroup mVessageTypesView;
    private VessageTypeGralleryAdapter vessageTypeGralleryAdapter;

    static VessageTypeInfo[] vessageTypes = new VessageTypeInfo[]{
            new VessageTypeInfo(Vessage.TYPE_IMAGE,R.mipmap.picture,R.string.vsg_type_photo)
    };

    private void handlerNewVessage(int typeId) {
        switch (typeId){
            case Vessage.TYPE_IMAGE:newImageVessage();
        }
    }

    private void newImageVessage() {
        Toast.makeText(getActivity(),R.string.image_vessage_comming_soon,Toast.LENGTH_SHORT).show();
        if (1 + 1 == 2) {
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.select_image_source)
                .setCancelable(true)
                .setPositiveButton(R.string.img_from_camera, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE,MediaStore.Images.Media.EXTERNAL_CONTENT_URI);//调用android自带的照相机
                        getActivity().startActivityForResult(intent, ActivityRequestCode.CAPTURE_IMG);
                    }
                })
                .setNegativeButton(R.string.img_from_album, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);//调用android的图库
                        getActivity().startActivityForResult(i, ActivityRequestCode.IMG_FROM_ALBUM);
                    }
                });
    }

    public boolean onActivityResult(int requestCode, int resultCode, Object data) {
        
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
        int typeIconResId;
        int typeTitleResId;
        int vessageTypeId;

        VessageTypeInfo(int vessageTypeId,int typeIconResId,int typeTitleResId){
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
            handlerNewVessage(info.vessageTypeId);
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
