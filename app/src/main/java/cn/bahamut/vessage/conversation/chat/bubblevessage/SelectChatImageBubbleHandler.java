package cn.bahamut.vessage.conversation.chat.bubblevessage;

import android.app.Activity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.LinkedList;
import java.util.List;

import cn.bahamut.common.BTSize;
import cn.bahamut.observer.Observer;
import cn.bahamut.observer.ObserverState;
import cn.bahamut.service.ServicesProvider;
import cn.bahamut.vessage.R;
import cn.bahamut.vessage.helper.ImageHelper;
import cn.bahamut.vessage.services.user.ChatImage;
import cn.bahamut.vessage.services.user.UserService;
import cn.bahamut.vessage.services.vessage.Vessage;

/**
 * Created by alexchow on 2016/11/9.
 */

public class SelectChatImageBubbleHandler implements BubbleVessageHandler {
    public static final SelectChatImageBubbleHandler instance = new SelectChatImageBubbleHandler();

    private ViewGroup content;
    private ChatImagesGralleryAdapter adapter;
    private RecyclerView chatImageListView;

    public void initSelectHandler(Activity context){
        adapter = new ChatImagesGralleryAdapter(context);
        content = (ViewGroup) context.getLayoutInflater().inflate(R.layout.conversation_select_chatimg_view,null);
        chatImageListView = (RecyclerView) content.findViewById(R.id.chat_images_list);
        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        chatImageListView.setLayoutManager(layoutManager);
        chatImageListView.setAdapter(adapter);
        adapter.refreshChatImages();
    }

    public void releaseHandler(){
        chatImageListView.setAdapter(null);
        adapter.onDestory();
        content = null;
        chatImageListView = null;
        adapter = null;
    }

    public void setOnSelectedChatImageListener(OnChatImageSelectedListener listener){
        adapter.listener = listener;
    }

    public OnChatImageSelectedListener getSelectedChatImageListener(){
        return adapter.listener;
    }

    public String getSelectedChatImageId(){
        return adapter.getSelecetedImageId();
    }

    @Override
    public BTSize getContentViewSize(Activity context, Vessage vessage, BTSize maxLimitedSize, View contentView) {
        adapter.refreshChatImages();

        int specW = View.MeasureSpec.makeMeasureSpec(maxLimitedSize.getWidthInt(), View.MeasureSpec.EXACTLY);
        int specH = View.MeasureSpec.makeMeasureSpec(maxLimitedSize.getHeightInt(), View.MeasureSpec.EXACTLY);
        chatImageListView.measure(specW,specH);
        //chatImageListView.requestLayout();
        //chatImageListView.layout(0,0,0,0);
        content.measure(specW,specH);
        //content.layout(0,0,maxLimitedSize.getWidthInt(),maxLimitedSize.getHeightInt());

        int w = View.MeasureSpec.getSize(chatImageListView.getMeasuredWidth());
        int h = View.MeasureSpec.getSize(chatImageListView.getMeasuredHeight());
        return new BTSize(w, h);
    }

    @Override
    public ViewGroup getContentView(Activity context, Vessage vessage) {
        return content;
    }

    @Override
    public void presentContent(Activity context, Vessage oldVessage, Vessage newVessage, View contentView) {
    }

    @Override
    public void onUnloadVessage(Activity context) {
    }

    @Override
    public void onPrepareVessage(Activity context, Vessage vessage) {

    }

    @Override
    public BubbleVessageHandler instanceOfVessage(Activity context, Vessage vessage) {
        return instance;
    }

    public interface OnChatImageSelectedListener {
        void onChatImageSelected(int index,ChatImage chatImage);
    }

    static class ChatImagesGralleryAdapter extends RecyclerView.Adapter<ChatImagesGralleryAdapter.ViewHolder>{
        private LayoutInflater mInflater;
        private Activity context;
        private int selectedIndex = -1;
        private List<ChatImage> chatImages = new LinkedList<>();
        OnChatImageSelectedListener listener;

        public String getSelecetedImageId(){
            if (selectedIndex >= 0 && selectedIndex < chatImages.size()){
                return chatImages.get(selectedIndex).imageId;
            }
            return null;
        }

        ChatImagesGralleryAdapter(Activity context){
            this.context = context;
            mInflater = this.context.getLayoutInflater();
            ServicesProvider.getService(UserService.class).addObserver(UserService.NOTIFY_MY_CHAT_IMAGES_UPDATED,onMyChatImagesUpdated);
        }

        public void onDestory(){
            ServicesProvider.getService(UserService.class).deleteObserver(UserService.NOTIFY_MY_CHAT_IMAGES_UPDATED,onMyChatImagesUpdated);
            listener = null;
        }

        private Observer onMyChatImagesUpdated = new Observer() {
            @Override
            public void update(ObserverState state) {
                refreshChatImages();
            }
        };

        public void refreshChatImages(){
            ChatImage[] arr = ServicesProvider.getService(UserService.class).getMyChatImages();
            chatImages.clear();
            for (ChatImage chatImage : arr) {
                chatImages.add(chatImage);
            }
            if (selectedIndex == -1 && chatImages.size() > 0){
                selectedIndex = 0;
            }
            notifyDataSetChanged();
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = mInflater.inflate(R.layout.conversation_face_image_item, parent, false);
            ViewHolder viewHolder = new ViewHolder(view);
            viewHolder.checkMarkView = view.findViewById(R.id.check_mark);
            viewHolder.imageView = (ImageView)view.findViewById(R.id.imageView);
            viewHolder.titleView = (TextView)view.findViewById(R.id.title_view);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, final int position) {
            ChatImage chatImage = chatImages.get(position);
            //holder.checkMarkView.setVisibility(position == selectedIndex ? View.VISIBLE : View.INVISIBLE);
            holder.checkMarkView.setVisibility(View.INVISIBLE);
            holder.titleView.setText(chatImage.imageType);
            ImageHelper.setImageByFileId(holder.imageView,chatImage.imageId);
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
            return chatImages.size();
        }

        void onItemClick(View view, int position) {
            selectedIndex = position;
            if (listener != null){
                listener.onChatImageSelected(position,chatImages.get(position));
            }
            notifyDataSetChanged();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            public View checkMarkView;
            public ImageView imageView;
            public TextView titleView;

            public ViewHolder(View itemView) {
                super(itemView);
            }
        }
    }
}
