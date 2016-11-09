package cn.bahamut.vessage.usersettings;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.lsjwzh.widget.recyclerviewpager.RecyclerViewPager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cn.bahamut.common.StringHelper;
import cn.bahamut.service.ServicesProvider;
import cn.bahamut.vessage.R;
import cn.bahamut.vessage.helper.ImageHelper;
import cn.bahamut.vessage.main.LocalizedStringHelper;
import cn.bahamut.vessage.services.user.ChatImage;
import cn.bahamut.vessage.services.user.UserService;

public class ChatImageManageActivity extends AppCompatActivity {

    private RecyclerViewPager chatImageLists;
    private ChatImagesManageGalleryAdapter adapter;

    private static class ChatImageGalleryType{
        String typeName;
        String typeSettedMessage;
        String typeNotSetMessage;

        ChatImageGalleryType(){

        }

        ChatImageGalleryType(int typeNameResId,int typeNotSetMessageResId,int typeSettedMessageResId){
            this.typeName = LocalizedStringHelper.getLocalizedString(typeNameResId);
            this.typeNotSetMessage = LocalizedStringHelper.getLocalizedString(typeNotSetMessageResId);
            this.typeSettedMessage = LocalizedStringHelper.getLocalizedString(typeSettedMessageResId);
        }
    }

    static private ChatImageGalleryType[] defaultChatImageTypes = new ChatImageGalleryType[]{
            new ChatImageGalleryType(R.string.chat_image_type_default,R.string.chat_image_type_default_nm,R.string.chat_image_type_default_sm),
            new ChatImageGalleryType(R.string.chat_image_type_doubi,R.string.chat_image_type_doubi_nm,R.string.chat_image_type_doubi_sm),
            new ChatImageGalleryType(R.string.chat_image_type_maimeng,R.string.chat_image_type_maimeng_nm,R.string.chat_image_type_maimeng_sm),
            new ChatImageGalleryType(R.string.chat_image_type_gaoxing,R.string.chat_image_type_gaoxing_nm,R.string.chat_image_type_gaoxing_sm),
            new ChatImageGalleryType(R.string.chat_image_type_shangxin,R.string.chat_image_type_shangxin_nm,R.string.chat_image_type_shangxin_sm),
            new ChatImageGalleryType(R.string.chat_image_type_aojiao,R.string.chat_image_type_aojiao_nm,R.string.chat_image_type_aojiao_sm)
    };

    static private ChatImageGalleryType videoChatChatImageGalleryType = new ChatImageGalleryType(R.string.chat_image_type_video_chat,R.string.chat_image_type_video_chat_nm,R.string.chat_image_type_video_chat_sm);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_image_manage);
        getSupportActionBar().setTitle(R.string.my_face_chat_images);
        chatImageLists = (RecyclerViewPager) findViewById(R.id.chat_images_list);
        adapter = new ChatImagesManageGalleryAdapter(this);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        chatImageLists.setLayoutManager(linearLayoutManager);
        adapter.selectedIndex = getIntent().getIntExtra("openIndex", 0);
        chatImageLists.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        adapter.refreshChatImages();
        chatImageLists.scrollToPosition(adapter.selectedIndex);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        adapter.onDestory();
    }

    public static void show(Context context, int openIndex) {
        Intent intent = new Intent(context,ChatImageManageActivity.class);
        intent.putExtra("openIndex",openIndex);
        context.startActivity(intent);
    }

    private static class ChatImagesManageGalleryAdapter extends RecyclerView.Adapter<ChatImagesManageGalleryAdapter.ViewHolder> {
        private static final int UPDATE_CHAT_IMAGE_REQUEST_CODE = 1;
        private LayoutInflater mInflater;
        private Activity context;
        private List<ChatImageGalleryItemModel> chatImages = new ArrayList<>();
        int selectedIndex = 0;

        ChatImagesManageGalleryAdapter(Activity context) {
            this.context = context;
            mInflater = this.context.getLayoutInflater();
        }

        public void onDestory() {
        }

        public void refreshChatImages() {
            UserService userService = ServicesProvider.getService(UserService.class);
            ChatImage videoChatImage = userService.getMyVideoChatImage();

            ChatImage[] arr = userService.getMyChatImages(false);
            HashMap<String, ChatImage> hm = new HashMap<>();
            for (ChatImage chatImage : arr) {
                hm.put(chatImage.imageType, chatImage);
            }
            chatImages.clear();
            if (videoChatImage == null) {
                videoChatImage = new ChatImage();
                videoChatImage.imageId = null;
                videoChatImage.imageType = LocalizedStringHelper.getLocalizedString(R.string.chat_image_type_video_chat);
            }
            chatImages.add(new ChatImageGalleryItemModel(videoChatImage, videoChatChatImageGalleryType));
            for (ChatImageGalleryType chatImageType : defaultChatImageTypes) {
                ChatImage ci = hm.remove(chatImageType.typeName);
                if (ci == null) {
                    ci = new ChatImage();
                    ci.imageId = null;
                    ci.imageType = chatImageType.typeName;
                }
                chatImages.add(new ChatImageGalleryItemModel(ci, chatImageType));
            }
            for (ChatImage chatImage : hm.values()) {
                ChatImageGalleryType type = new ChatImageGalleryType();
                type.typeName = chatImage.imageType;
                type.typeSettedMessage = String.format(LocalizedStringHelper.getLocalizedString(R.string.chat_image_type_x_sm), type.typeName);
                type.typeNotSetMessage = String.format(LocalizedStringHelper.getLocalizedString(R.string.chat_image_type_x_nm), type.typeName);
                chatImages.add(new ChatImageGalleryItemModel(chatImage, type));
            }
            notifyDataSetChanged();
        }

        @Override
        public ChatImagesManageGalleryAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = mInflater.inflate(R.layout.face_image_mgr_item, parent, false);
            ChatImagesManageGalleryAdapter.ViewHolder viewHolder = new ChatImagesManageGalleryAdapter.ViewHolder(view);
            viewHolder.faceImage = (ImageView) view.findViewById(R.id.face_image);
            viewHolder.titleView = (TextView) view.findViewById(R.id.title_view);
            viewHolder.faceTips = (TextView) view.findViewById(R.id.face_tips);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(final ChatImagesManageGalleryAdapter.ViewHolder holder, final int position) {
            ChatImageGalleryItemModel model = chatImages.get(position);
            holder.faceImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
            if (StringHelper.isStringNullOrWhiteSpace(model.chatImage.imageId)){
                holder.titleView.setText(String.format(LocalizedStringHelper.getLocalizedString(R.string.x_image_type_not_set),model.chatImage.imageType));
                ImageHelper.setViewImage(holder.faceImage,R.raw.default_face);
                holder.faceTips.setText(model.type.typeNotSetMessage);
            }else {
                holder.titleView.setText(model.chatImage.imageType);
                ImageHelper.setImageByFileId(holder.faceImage,model.chatImage.imageId,R.raw.default_face);
                holder.faceTips.setText(model.type.typeSettedMessage);
            }
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onItemClick(holder.itemView, position);
                }
            });
        }

        @Override
        public int getItemCount() {
            return chatImages.size();
        }

        public void onItemClick(View view, int position) {
            ChatImageGalleryItemModel model = chatImages.get(position);
            int type = UpdateChatImageActivity.TYPE_NORMAL_CHAT_IMAGE;
            if (position == 0) {
                type = UpdateChatImageActivity.TYPE_VIDEO_CHAT_IMAGE;
            }
            selectedIndex = position;
            UpdateChatImageActivity.startUpdateChatImageActivity(this.context, UPDATE_CHAT_IMAGE_REQUEST_CODE, type, model.type.typeName);
        }

        private static class ChatImageGalleryItemModel{
            ChatImageGalleryType type;
            ChatImage chatImage;

            ChatImageGalleryItemModel(ChatImage chatImage,ChatImageGalleryType type){
                this.type = type;
                this.chatImage = chatImage;
            }
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            public ImageView faceImage;
            public TextView titleView;
            public TextView faceTips;
            public ViewHolder(View itemView) {
                super(itemView);
            }
        }
    }

}
