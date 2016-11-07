package cn.bahamut.vessage.conversation.view;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.makeramen.roundedimageview.RoundedImageView;

import java.util.ArrayList;

import cn.bahamut.common.StringHelper;
import cn.bahamut.service.ServicesProvider;
import cn.bahamut.vessage.helper.ImageHelper;
import cn.bahamut.vessage.main.AssetsDefaultConstants;
import cn.bahamut.vessage.main.UserSetting;
import cn.bahamut.vessage.services.user.ChatImage;
import cn.bahamut.vessage.services.user.UserService;
import cn.bahamut.vessage.services.user.VessageUser;

/**
 * Created by alexchow on 2016/11/4.
 */

public class ChattersBoard extends ViewGroup {
    public class ChatterItem{
        private VessageUser chatter;
        private String itemImage;

        public void setChatter(VessageUser chatter) {
            this.chatter = chatter;
            if (StringHelper.isStringNullOrWhiteSpace(this.itemImage)){
                if (chatter != null){
                    if(StringHelper.isStringNullOrWhiteSpace(chatter.mainChatImage) == false){
                        this.itemImage = chatter.mainChatImage;
                    }else if(StringHelper.isStringNullOrWhiteSpace(chatter.avatar) == false){
                        this.itemImage = chatter.avatar;
                    }
                }
            }
        }

        public void setItemImage(String itemImage) {
            this.itemImage = itemImage;
        }

        public String getItemImage() {
            return itemImage;
        }

        public VessageUser getChatter() {
            return chatter;
        }
    }

    enum ItemHorizontalLayout {
        Average,Center,MiddleAverage,Left,Right
    }

    private ArrayList<ChatterItem> chatterItems;
    private ArrayList<RoundedImageView> chatterImageViews;

    private float minItemSpace = 10;
    private float minTopBottomPadding = 10;

    private ItemHorizontalLayout itemHorizontalLayout = ItemHorizontalLayout.Average;

    public ChattersBoard(Context context) {
        super(context);
        init();
    }


    public ChattersBoard(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        chatterItems = new ArrayList<>(10);
        chatterImageViews = new ArrayList<>(10);
    }

    public int indexOfChatter(String chatterId) {
        for (int i = 0; i < chatterItems.size(); i++) {
            if (chatterItems.get(i).getChatter().userId.equals(chatterId)) {
                return i;
            }
        }
        return -1;
    }

    public ChatterItem getChatterItem(String chatterId){
        return getChatterItem(indexOfChatter(chatterId));
    }

    public ChatterItem getChatterItem(int index){
        if (chatterItems.size() > index && index >= 0) {
            return chatterItems.get(index);
        }
        return null;
    }

    public ImageView getChatterImageView(int index) {
        if (chatterImageViews.size() > index && index >= 0) {
            return chatterImageViews.get(index);
        }
        return null;
    }

    public ImageView getChatterImageView(String chatterId){
        return getChatterImageView(indexOfChatter(chatterId));
    }

    public boolean removeChatter(VessageUser user,boolean isDrawNow){
        for (int i = chatterItems.size() - 1; i >= 0; i--) {
            if (chatterItems.get(i).chatter.userId.equals(user.userId)){
                chatterItems.remove(i);
                if (isDrawNow){
                    drawBoard();
                }
                return true;
            }
        }
        return false;
    }

    public ChatterItem[] clearAllChatters(boolean isDrawNow) {
        ChatterItem[] items = chatterItems.toArray(new ChatterItem[0]);
        chatterItems.clear();
        if (isDrawNow){
            drawBoard();
        }
        return items;
    }

    public void removeChatters(VessageUser[] users) {
        boolean needDraw = false;
        for (int i = chatterItems.size() - 1; i >= 0; i--) {
            VessageUser chatter = chatterItems.get(i).chatter;
            for (VessageUser user : users) {
                if (chatter.userId.equals(user.userId)){
                    chatterItems.remove(i);
                    needDraw = true;
                    break;
                }
            }
        }

        if (needDraw) {
            drawBoard();
        }
    }

    public void addChatters(ChatterItem[] items) {
        boolean updated = false;
        for (ChatterItem item : items) {
            boolean contain = false;
            for (ChatterItem chatterItem : chatterItems) {
                if (item.chatter.userId.equals(chatterItem.chatter.userId)){
                    contain = true;
                    break;
                }
            }
            if (contain == false){
                updated = true;
                chatterItems.add(item);
            }
        }

        if (updated) {
            drawBoard();
        }
    }

    public void removeChatters(ChatterItem[] items) {
        boolean needDraw = false;
        for (int i = chatterItems.size() - 1; i >= 0; i--) {
            VessageUser chatter = chatterItems.get(i).chatter;
            for (ChatterItem item : items) {
                if (chatter.userId.equals(item.chatter.userId)){
                    chatterItems.remove(i);
                    needDraw = true;
                    break;
                }
            }
        }

        if (needDraw) {
            drawBoard();
        }
    }

    public void addChatters(VessageUser[] users) {
        for (VessageUser user : users) {
            addChatter(user,false);
        }
        drawBoard();
    }

    public void addChatter(VessageUser user,boolean isDrawNow) {
        if (getChatterImageView(user.userId) == null){
            ChatterItem newItem = new ChatterItem();
            newItem.setChatter(user);
            chatterItems.add(newItem);
            if (user.userId.equals(UserSetting.getUserId())){
                ChatImage[] arr = ServicesProvider.getService(UserService.class).getMyChatImages(false);
                if (arr.length > 0){
                    newItem.itemImage = arr[0].imageId;
                }
            }
            if (isDrawNow){
                drawBoard();
            }
        }
    }

    public void drawBoard() {
        for (RoundedImageView view : chatterImageViews) {
            this.removeView(view);
        }
        for (int i = 0; i < chatterItems.size(); i++){
            if (chatterImageViews.size() > i){
                ImageView imageView = chatterImageViews.get(i);
                this.addView(imageView);
            }else {
                RoundedImageView imageView = new RoundedImageView(getContext());
                chatterImageViews.add(imageView);
                this.addView(imageView);
            }
        }
        this.forceLayout();
    }

    public boolean updateChatter(VessageUser chatter){
        for (int i = 0; i < chatterItems.size(); i++) {
            if(chatterItems.get(i).chatter.userId.equals(chatter.userId)){
                chatterItems.get(i).setChatter(chatter);
                return true;
            }
        }
        return false;
    }

    public boolean setImageOfChatter(String chatterId,String imgId) {
        for (int i = 0; i < chatterItems.size(); i++) {
            if(chatterItems.get(i).chatter.userId.equals(chatterId)){
                chatterItems.get(i).setItemImage(imgId);
                return true;
            }
        }
        return false;

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        measureImageViewsSize(MeasureSpec.getSize(widthMeasureSpec),MeasureSpec.getSize(heightMeasureSpec));
        if (chatterItems.size() == 0){
            setMeasuredDimension(widthMeasureSpec,MeasureSpec.makeMeasureSpec(0,MeasureSpec.EXACTLY));
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        layoutImageView(r - l, b - t);
    }

    private float chatterImageHeightOfNum(int chattersNum,int boardWidth,int boardHeight){
        if (chattersNum == 0){
            return 0;
        }
        float itemWidth = (boardWidth - (chattersNum + 1) * minItemSpace) / chattersNum;
        float itemHeight = boardHeight - minTopBottomPadding;
        float h = Math.min(itemWidth,itemHeight);
        Log.d("ChattersBoard","h:"+h);
        return h;
    }

    private void layoutImageView(int boardWidth,int boardHeight){
        for (int i = 0; i < chatterItems.size(); i++) {
            ChatterItem chatterItem = chatterItems.get(i);
            RoundedImageView imgv = chatterImageViews.get(i);
            int chattersCount = chatterItems.size();
            int itemWidthHeight = imgv.getMeasuredWidth();
            float firstItemX = 0;
            float itemSpace = 0;
            switch (itemHorizontalLayout) {
                case Average:
                    itemSpace = (boardWidth - itemWidthHeight * chattersCount) / (chattersCount + 1);
                    firstItemX = itemSpace;
                    break;
                case Center:
                    itemSpace = minItemSpace;
                    firstItemX = (boardWidth - itemWidthHeight * chattersCount - itemSpace * (chattersCount - 1)) / 2;
                    break;
                case MiddleAverage:
                    firstItemX = minItemSpace;
                    itemSpace = (boardWidth - 2 * firstItemX - itemWidthHeight * chattersCount) / (chattersCount - 1);
                    break;
                case Left:
                    firstItemX = minItemSpace;
                    itemSpace = minItemSpace;
                    break;
                case Right:
                    firstItemX = boardWidth - chattersCount * (minItemSpace + itemWidthHeight);
                    itemSpace = minItemSpace;
                    break;
            }
            int x = (int)(firstItemX + i * (itemWidthHeight + itemSpace));
            int y = (int)((boardHeight - itemWidthHeight) / 2.0);

            imgv.layout(x,y,x+itemWidthHeight,y+itemWidthHeight);
            int defaultResId = AssetsDefaultConstants.getDefaultFace(chatterItem.chatter.userId.hashCode());
            if (StringHelper.isStringNullOrWhiteSpace(chatterItem.itemImage) == false){
                setImageViewImage(imgv,chatterItem.itemImage,defaultResId);
            }else {
                imgv.setImageResource(defaultResId);
            }

            /*
            let indicator = (imgv.subviews.filter{$0 is UIActivityIndicatorView}).first as? UIActivityIndicatorView
            indicator?.center = CGPointMake(itemWidthHeight / 2, itemWidthHeight / 2)
            */
        }
    }

    private void setImageViewImage(final ImageView imageView, String fileId, final int defaultResId) {
        ImageHelper.getImageByFileId(fileId, new ImageHelper.OnGetImageCallback() {
            @Override
            public void onGetImageDrawable(Drawable drawable) {
                imageView.setImageDrawable(drawable);
            }

            @Override
            public void onGetImageResId(int resId) {
                imageView.setImageResource(resId);
            }

            @Override
            public void onGetImageFailed() {
                imageView.setImageResource(defaultResId);
            }
        });
    }

    private void measureImageViewsSize(int boardWidth,int boardHeight){
        Log.d("ChattersBoard","w:"+boardWidth+",h:"+boardHeight);
        if (chatterItems.size() > chatterImageViews.size()) {
            return;
        }
        int chattersCount = chatterItems.size();
        int itemWidthHeight = MeasureSpec.makeMeasureSpec((int)chatterImageHeightOfNum(chattersCount, boardWidth,boardHeight),MeasureSpec.EXACTLY);
        Log.d("ChattersBoard","itemWidthHeight:"+itemWidthHeight);
        for (int i = 0; i < chatterItems.size(); i++) {
            RoundedImageView imgv = chatterImageViews.get(i);
            imgv.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imgv.measure(itemWidthHeight,itemWidthHeight);
            imgv.setCornerRadius(itemWidthHeight / 2);
            imgv.setBorderColor(Color.WHITE);
            imgv.setBorderWidth(1f);
        }
    }
}
