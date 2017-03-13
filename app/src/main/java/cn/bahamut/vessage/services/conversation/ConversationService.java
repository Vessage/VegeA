package cn.bahamut.vessage.services.conversation;

import android.content.Context;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cn.bahamut.common.DateHelper;
import cn.bahamut.common.IDUtil;
import cn.bahamut.common.StringHelper;
import cn.bahamut.observer.Observable;
import cn.bahamut.service.OnServiceInit;
import cn.bahamut.service.OnServiceUserLogin;
import cn.bahamut.service.OnServiceUserLogout;
import cn.bahamut.service.ServicesProvider;
import cn.bahamut.vessage.services.groupchat.ChatGroup;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

/**
 * Created by alexchow on 16/3/30.
 */
public class ConversationService extends Observable implements OnServiceUserLogin, OnServiceUserLogout, OnServiceInit {

    public static final long MAX_PIN_CONVERSATION_LIMIT = 6;
    public static final String NOTIFY_CONVERSATION_LIST_UPDATED = "NOTIFY_CONVERSATION_LIST_UPDATED";

    public Conversation openConversation(String conversationId) {
        Realm realm = Realm.getDefaultInstance();
        Conversation conversation = realm.where(Conversation.class).equalTo("conversationId", conversationId).findFirst();
        Conversation result = conversation != null ? conversation.copyToObject() : null;

        return result;
    }

    @Override
    public void onServiceInit(Context applicationContext) {

    }

    public Conversation openConversationVessageInfo(String chatterId, boolean isGroup) {
        try (Realm realm = Realm.getDefaultInstance()) {
            Conversation conversation = realm.where(Conversation.class)
                    .equalTo("chatterId", chatterId)
                    .findFirst();
            if (conversation == null) {
                realm.beginTransaction();
                conversation = realm.createObject(Conversation.class);
                conversation.chatterId = chatterId;
                conversation.conversationId = IDUtil.generateUniqueId();
                conversation.lstTs = DateHelper.getUnixTimeSpan();
                conversation.type = isGroup ? Conversation.TYPE_GROUP_CHAT : Conversation.TYPE_SINGLE_CHAT;
                realm.commitTransaction();

            }
            return conversation.copyToObject();
        }
    }

    public Conversation openConversationByGroup(ChatGroup group) {
        return openConversationByGroup(group, null);
    }

    public Conversation openConversationByGroup(ChatGroup group, Dictionary<String, Object> extraInfo) {
        try (Realm realm = Realm.getDefaultInstance()) {
            Conversation conversation = realm.where(Conversation.class).equalTo("chatterId", group.groupId).findFirst();
            if (conversation == null) {
                realm.beginTransaction();
                conversation = realm.createObject(Conversation.class);
                conversation.lstTs = DateHelper.getUnixTimeSpan();
                conversation.chatterId = group.groupId;
                conversation.conversationId = IDUtil.generateUniqueId();
                conversation.type = Conversation.TYPE_GROUP_CHAT;
                long beforeRemovedMs = Conversation.MAX_LEFT_TIME_MS;
                String activityId = null;
                if (extraInfo != null) {
                    Long ms = (Long) extraInfo.get("beforeRemoveMS");
                    if (ms != null) {
                        beforeRemovedMs = ms;
                    }

                    activityId = (String) extraInfo.get("activityId");
                }
                conversation.activityId = activityId;
                conversation.lstTs = DateHelper.getUnixTimeSpan() + beforeRemovedMs - Conversation.MAX_LEFT_TIME_MS;

                realm.commitTransaction();
            }
            return conversation.copyToObject();
        }
    }

    public Conversation openConversationByUserInfo(String userId) {
        return openConversationByUserInfo(userId, null);
    }

    public Conversation openConversationByUserInfo(String userId, Map<String, Object> extraInfo) {
        try (Realm realm = Realm.getDefaultInstance()) {
            Conversation conversation = realm.where(Conversation.class).equalTo("chatterId", userId).findFirst();
            if (conversation == null) {
                realm.beginTransaction();
                conversation = realm.createObject(Conversation.class);

                long beforeRemovedMs = Conversation.MAX_LEFT_TIME_MS;
                String activityId = null;
                if (extraInfo != null) {
                    Long ms = (Long) extraInfo.get("beforeRemoveMS");
                    if (ms != null) {
                        beforeRemovedMs = ms;
                    }

                    activityId = (String) extraInfo.get("activityId");
                }

                conversation.lstTs = DateHelper.getUnixTimeSpan() + beforeRemovedMs - Conversation.MAX_LEFT_TIME_MS;
                conversation.chatterId = userId;
                conversation.conversationId = IDUtil.generateUniqueId();
                conversation.activityId = activityId;

                conversation.type = Conversation.TYPE_SINGLE_CHAT;
                realm.commitTransaction();
            }
            return conversation.copyToObject();
        }
    }

    public Conversation getConversationByChatterId(String chatterId) {
        if (StringHelper.isNullOrEmpty(chatterId)) {
            return null;
        }
        try (Realm realm = Realm.getDefaultInstance()) {
            Conversation conversation = realm.where(Conversation.class).equalTo("chatterId", chatterId).findFirst();
            if (conversation == null) {
                return null;
            }
            return conversation.copyToObject();
        }
    }

    static private List<Conversation> conversationRealmResultToList(RealmResults<Conversation> results) {
        List<Conversation> conversationList = new ArrayList<>(results.size());
        for (Conversation result : results) {
            conversationList.add(result.copyToObject());
        }
        return conversationList;
    }

    public List<Conversation> getAllConversations() {
        try (Realm realm = Realm.getDefaultInstance()) {
            RealmResults<Conversation> results = realm.where(Conversation.class).findAllSorted("lstTs", Sort.DESCENDING);
            return conversationRealmResultToList(results);
        }
    }

    public List<Conversation> getNotActivityConversations() {
        try (Realm realm = Realm.getDefaultInstance()) {
            RealmResults<Conversation> results = realm.where(Conversation.class).isNull("activityId").findAllSorted("lstTs", Sort.DESCENDING);
            return conversationRealmResultToList(results);
        }
    }

    public boolean canPinMoreConversation() {
        try (Realm realm = Realm.getDefaultInstance()) {
            return realm.where(Conversation.class).equalTo("isPinned", true).count() < MAX_PIN_CONVERSATION_LIMIT;
        }
    }

    @Override
    public void onUserLogin(String userId) {
        ServicesProvider.setServiceReady(ConversationService.class);
    }

    @Override
    public void onUserLogout() {
        ServicesProvider.setServiceNotReady(ConversationService.class);
    }

    public Set<String> getChattingNormalUserIds() {
        return getChattingConversationChatterIds(Conversation.TYPE_SINGLE_CHAT);
    }

    private Set<String> getChattingConversationChatterIds(int conversationType) {
        Set<String> chatterIds = new HashSet<>();
        for (Conversation conversation : getAllConversations()) {
            if (conversation.type == conversationType && StringHelper.isStringNullOrWhiteSpace(conversation.activityId)) {
                chatterIds.add(conversation.chatterId);
            }
        }
        return chatterIds;
    }

    public Set<String> getChattingUserIds() {
        Set<String> chatterIds = new HashSet<>();
        for (Conversation conversation : getAllConversations()) {
            if (conversation.type == Conversation.TYPE_SINGLE_CHAT) {
                chatterIds.add(conversation.chatterId);
            }
        }
        return chatterIds;
    }

    public boolean setConversationPinned(String conversationId, boolean pinned) {
        try (Realm realm = Realm.getDefaultInstance()) {
            Conversation conversation = realm.where(Conversation.class).equalTo("conversationId", conversationId).findFirst();
            if (conversation != null) {
                realm.beginTransaction();
                conversation.isPinned = pinned;
                realm.commitTransaction();
                return true;
            }
            return false;
        }
    }

    public boolean expireConversation(String chatterId) {
        if (StringHelper.isStringNullOrWhiteSpace(chatterId)) {
            return false;
        }
        try (Realm realm = Realm.getDefaultInstance()) {
            Conversation conversation = realm.where(Conversation.class).equalTo("chatterId", chatterId).findFirst();
            if (conversation != null) {
                realm.beginTransaction();
                conversation.lstTs = DateHelper.getUnixTimeSpan();
                realm.commitTransaction();
                postNotification(NOTIFY_CONVERSATION_LIST_UPDATED);
                return true;
            }
            return false;
        }
    }

    public List<Conversation> clearTimeupConversations() {
        try (Realm realm = Realm.getDefaultInstance()) {
            List<Conversation> list = realm.where(Conversation.class).findAll();
            realm.beginTransaction();
            List<Conversation> timeUpConversations = new LinkedList<>();
            for (Conversation conversation : list) {
                if (!conversation.isPinned && conversation.getTimeUpMinutesLeft() < 3) {
                    timeUpConversations.add(conversation.copyToObject());
                    conversation.deleteFromRealm();
                }
            }
            realm.commitTransaction();
            return timeUpConversations;
        }
    }

    public boolean removeConversation(String conversationId) {
        try(Realm realm = Realm.getDefaultInstance()) {
            Conversation conversation = realm.where(Conversation.class).equalTo("conversationId", conversationId).findFirst();
            realm.beginTransaction();
            conversation.deleteFromRealm();
            realm.commitTransaction();
        }
        return false;
    }
}
