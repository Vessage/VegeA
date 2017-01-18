package cn.bahamut.vessage.conversation.list;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;

import java.util.LinkedList;

import cn.bahamut.common.DateHelper;
import cn.bahamut.common.StringHelper;
import cn.bahamut.observer.Observer;
import cn.bahamut.observer.ObserverState;
import cn.bahamut.service.ServicesProvider;
import cn.bahamut.vessage.R;
import cn.bahamut.vessage.helper.ImageHelper;
import cn.bahamut.vessage.main.AppUtil;
import cn.bahamut.vessage.main.AssetsDefaultConstants;
import cn.bahamut.vessage.main.LocalizedStringHelper;
import cn.bahamut.vessage.services.conversation.Conversation;
import cn.bahamut.vessage.services.groupchat.ChatGroup;
import cn.bahamut.vessage.services.groupchat.ChatGroupService;
import cn.bahamut.vessage.services.user.UserService;
import cn.bahamut.vessage.services.user.VessageUser;

/**
 * Created by alexchow on 16/3/30.
 */
public class ConversationListSearchAdapter extends ConversationListAdapterBase {

    public void init() {
        searchManager = new SearchManager();
        searchManager.addObserver(SearchManager.NOTIFY_ON_SEARCH_RESULT_LIST_UPDATED, onSearchResultUpdated);
    }

    public void release() {
        searchManager.deleteObserver(SearchManager.NOTIFY_ON_SEARCH_RESULT_LIST_UPDATED, onSearchResultUpdated);
    }

    public void searchLocal(String keyword) {
        searchManager.searchKeywordLocal(keyword);
    }

    public void searchOnline(String keyword, SearchManager.SearchCallback callback) {
        searchManager.searchKeywordOnline(keyword, callback);
    }

    private SearchManager searchManager;

    public void reloadResultList() {
        data = new LinkedList<>();
        for (SearchManager.SearchResultModel model : searchManager.getSearchResultList()) {
            ItemModel itemModel = new ItemModel();
            itemModel.originModel = model;
            if (model.conversation != null) {
                itemModel.headLine = LocalizedStringHelper.getLocalizedString(R.string.nameless_conversation);
                itemModel.subLine = AppUtil.dateToFriendlyString(getContext(), DateHelper.getDateFromUnixTimeSpace(model.conversation.lstTs));
                itemModel.uniqueId = model.conversation.chatterId;
            } else if (model.user != null) {
                if (StringHelper.isStringNullOrWhiteSpace(model.user.accountId)) {
                    itemModel.headLine = LocalizedStringHelper.getLocalizedString(R.string.mobile_user);
                } else if (StringHelper.isStringNullOrWhiteSpace(model.user.nickName)) {
                    itemModel.headLine = model.user.accountId;
                } else {
                    itemModel.headLine = model.user.nickName;
                }

                if (model.userType == 1) {
                    itemModel.subLine = LocalizedStringHelper.getLocalizedString(R.string.near_users);
                } else if (model.userType == 2) {
                    itemModel.subLine = LocalizedStringHelper.getLocalizedString(R.string.active_user);
                } else if (model.userType == 3) {
                    itemModel.subLine = LocalizedStringHelper.getLocalizedString(R.string.near_active_user);

                } else {
                    itemModel.subLine = LocalizedStringHelper.getLocalizedString(R.string.new_chat);
                }
                itemModel.avatar = model.user.avatar;
                itemModel.uniqueId = model.user.userId;
            } else if (model.mobile != null) {
                itemModel.headLine = model.mobile;
                itemModel.subLine = LocalizedStringHelper.getLocalizedString(R.string.new_chat);
                itemModel.uniqueId = model.mobile;
            }
            data.add(itemModel);
        }
        notifyDataSetChanged();
    }

    @Override
    protected void onUserProfileUpdated(VessageUser updatedUser) {
        notifyModelUpdated(updatedUser.userId);
    }

    @Override
    protected void onChatGroupUpdated(ChatGroup updatedChatGroup) {
        notifyModelUpdated(updatedChatGroup.groupId);
    }

    private void notifyModelUpdated(String uniqueId) {
        if (data == null || data.size() == 0) {
            return;
        }
        int index = 0;
        for (ItemModel itemModel : data) {
            if (StringHelper.isStringNullOrWhiteSpace(itemModel.uniqueId) == false && itemModel.uniqueId.equals(uniqueId)) {
                notifyItemChanged(index);
            }
            index += 1;
        }
    }

    public SearchManager.SearchResultModel getSearchResult(int index) {
        if (data.size() > index) {
            return (SearchManager.SearchResultModel) data.get(index).originModel;
        } else {
            return null;
        }
    }

    private Observer onSearchResultUpdated = new Observer() {
        @Override
        public void update(ObserverState state) {
            reloadResultList();
        }
    };

    public ConversationListSearchAdapter(Context context) {
        super(context);
    }

    @Override
    public int getItemViewType(int position) {
        return ViewHolder.TYPE_NORMAL_ITEM;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        ItemModel model = data.get(position);
        SearchManager.SearchResultModel searchResultModel = (SearchManager.SearchResultModel) model.originModel;
        if (searchResultModel.conversation != null && searchResultModel.conversation.type == Conversation.TYPE_GROUP_CHAT) {
            Conversation c = searchResultModel.conversation;
            Bitmap bitmap = BitmapFactory.decodeStream(getContext().getResources().openRawResource(R.raw.group_chat));
            holder.avatar.setImageBitmap(bitmap);
            ChatGroup chatCroup = ServicesProvider.getService(ChatGroupService.class).getCachedChatGroup(c.chatterId);
            if (chatCroup != null) {
                holder.headline.setText(chatCroup.groupName);
            } else {
                ServicesProvider.getService(ChatGroupService.class).fetchChatGroup(c.chatterId);
            }
        } else {
            int code = 0;
            int sex = 0;
            UserService userService = ServicesProvider.getService(UserService.class);
            if (searchResultModel.user != null) {
                code = searchResultModel.user.userId.hashCode();
                sex = searchResultModel.user.sex;
                String noteName = userService.getUserNotedNameIfExists(searchResultModel.user.userId);
                if (noteName != null) {
                    holder.headline.setText(noteName);
                } else {
                    holder.headline.setText(searchResultModel.user.nickName);
                }
            } else if (searchResultModel.conversation != null) {
                code = searchResultModel.conversation.chatterId.hashCode();
                VessageUser user = userService.getUserById(searchResultModel.conversation.chatterId);
                if (user != null) {
                    sex = user.sex;
                }
                holder.headline.setText(ServicesProvider.getService(UserService.class).getUserNoteOrNickName(searchResultModel.conversation.chatterId));
            }

            ImageHelper.setImageByFileId(holder.avatar, model.avatar, AssetsDefaultConstants.getDefaultFace(code, sex));
        }
        holder.headline.setText(data.get(position).headLine);
        holder.subline.setText(data.get(position).subLine);
        String badge = data.get(position).badge;
        try {
            int badgeValue = Integer.parseInt(badge);
            holder.setBadge(badgeValue);
        } catch (NumberFormatException e) {
            holder.setBadge(0);
        }
        holder.pinnedMark.setVisibility(View.INVISIBLE);
        holder.timeProgress.setProgress(0);
    }

    @Override
    public int getItemCount() {
        return data != null ? data.size() : 0;
    }
}
