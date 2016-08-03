package cn.bahamut.vessage.conversation.list;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;
import android.view.ViewGroup;

import java.util.LinkedList;

import cn.bahamut.common.StringHelper;
import cn.bahamut.observer.Observer;
import cn.bahamut.observer.ObserverState;
import cn.bahamut.service.ServicesProvider;
import cn.bahamut.vessage.R;
import cn.bahamut.vessage.helper.ImageHelper;
import cn.bahamut.vessage.main.AssetsDefaultConstants;
import cn.bahamut.vessage.main.LocalizedStringHelper;
import cn.bahamut.vessage.services.conversation.Conversation;
import cn.bahamut.vessage.services.groupchat.ChatGroup;
import cn.bahamut.vessage.services.groupchat.ChatGroupService;
import cn.bahamut.vessage.services.user.UserService;

/**
 * Created by alexchow on 16/3/30.
 */
public class ConversationListSearchAdapter extends ConversationListAdapterBase {

    public void init(){
        searchManager = new SearchManager();
        searchManager.addObserver(SearchManager.NOTIFY_ON_SEARCH_RESULT_LIST_UPDATED, onSearchResultUpdated);
    }

    public void release(){
        searchManager.deleteObserver(SearchManager.NOTIFY_ON_SEARCH_RESULT_LIST_UPDATED, onSearchResultUpdated);
    }

    public void search(String keyword){
        searchManager.searchKeywork(keyword);
    }

    private SearchManager searchManager;
    public void reloadResultList(){
        data = new LinkedList<>();
        for (SearchManager.SearchResultModel model : searchManager.getSearchResultList()) {
            ItemModel itemModel = new ItemModel();
            itemModel.originModel = model;
            if(model.conversation != null){
                itemModel.headLine = LocalizedStringHelper.getLocalizedString(R.string.nameless_conversation);
                itemModel.subLine = model.conversation.getLastMessageTime();
            }else if(model.user != null){
                if (StringHelper.isStringNullOrWhiteSpace(model.user.accountId)){
                    itemModel.headLine = LocalizedStringHelper.getLocalizedString(R.string.mobile_user);
                }else if(StringHelper.isStringNullOrWhiteSpace(model.user.nickName)){
                    itemModel.headLine = model.user.accountId;
                }else {
                    itemModel.headLine = model.user.nickName;
                }
                itemModel.subLine = "新建对话";
            }else if(model.mobile != null){
                itemModel.headLine = model.mobile;
                itemModel.subLine = "新建对话";
            }
            data.add(itemModel);
        }
        notifyDataSetChanged();
    }

    public SearchManager.SearchResultModel getSearchResult(int index){
        if(data.size() > index){
            return (SearchManager.SearchResultModel)data.get(index).originModel;
        }else {
            return null;
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView =  super.getView(position, convertView, parent);
        ItemModel model = data.get(position);
        SearchManager.SearchResultModel searchResultModel = (SearchManager.SearchResultModel) model.originModel;
        ConversationListAdapterBase.ViewHolder holder = (ViewHolder) convertView.getTag();
        if (searchResultModel.conversation != null && searchResultModel.conversation.isGroup){
            Conversation c = searchResultModel.conversation;
            Bitmap bitmap = BitmapFactory.decodeStream(getContext().getResources().openRawResource(R.raw.group_chat));
            holder.avatar.setImageBitmap(bitmap);
            ChatGroup chatCroup = ServicesProvider.getService(ChatGroupService.class).getCachedChatGroup(c.chatterId);
            if(chatCroup!=null){
                holder.headline.setText(chatCroup.groupName);
            }else {
                ServicesProvider.getService(ChatGroupService.class).fetchChatGroup(c.chatterId);
            }
        }else {
            int code = 0;
            if(searchResultModel.user != null){
                code = searchResultModel.user.userId.hashCode();
                holder.headline.setText(ServicesProvider.getService(UserService.class).getUserNoteName(searchResultModel.user.userId));
            }else if(searchResultModel.conversation != null){
                code = searchResultModel.conversation.chatterId.hashCode();
                holder.headline.setText(ServicesProvider.getService(UserService.class).getUserNoteName(searchResultModel.conversation.chatterId));
            }
            ImageHelper.setImageByFileId(holder.avatar, model.avatar, AssetsDefaultConstants.getDefaultFace(code));
        }
        return convertView;
    }

    private Observer onSearchResultUpdated = new Observer() {
        @Override
        public void update(ObserverState state) {
            reloadResultList();
        }
    };

    public ConversationListSearchAdapter(Context context){
        super(context);
    }

}
