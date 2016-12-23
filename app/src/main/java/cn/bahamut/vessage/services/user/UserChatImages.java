package cn.bahamut.vessage.services.user;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by alexchow on 16/8/2.
 */
public class UserChatImages extends RealmObject {
    @PrimaryKey
    public String userId;
    public RealmList<ChatImage> chatImages;

    public UserChatImages copyToObject(){
        UserChatImages copy = new UserChatImages();
        copy.userId = userId;
        copy.chatImages = new RealmList<>();
        for (ChatImage chatImage : chatImages) {
            ChatImage ci = chatImage.copyToObject();
            chatImages.add(ci);
        }
        return copy;
    }
}
