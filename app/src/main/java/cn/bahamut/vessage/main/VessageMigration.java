package cn.bahamut.vessage.main;

import java.util.Date;

import cn.bahamut.common.DateHelper;
import cn.bahamut.vessage.services.conversation.Conversation;
import io.realm.DynamicRealm;
import io.realm.DynamicRealmObject;
import io.realm.FieldAttribute;
import io.realm.RealmMigration;
import io.realm.RealmObjectSchema;
import io.realm.RealmSchema;

/**
 * Created by alexchow on 16/6/20.
 */
public class VessageMigration implements RealmMigration {
    @Override
    public boolean equals(Object o) {
        return schemaVersion == ((VessageMigration)o).schemaVersion;
    }

    @Override
    public void migrate(DynamicRealm realm, long oldVersion, long newVersion) {
        RealmSchema schema = realm.getSchema();

        if (oldVersion == 0){
            schema.create("LittlePaperReadResponse")
                    .addField("paperId", String.class, FieldAttribute.PRIMARY_KEY)
                    .addField("asker",String.class)
                    .addField("askerNick",String.class)
                    .addField("paperReceiver",String.class)
                    .addField("type",int.class)
                    .addField("code",int.class)
                    .addField("isRead",boolean.class);
            oldVersion++;
        }

        if (oldVersion == 1){
            schema.get("Vessage").addField("isGroup",boolean.class);
            schema.get("Conversation")
                    .addField("isGroup",boolean.class);
            schema.get("SendVessageTask")
                    .addField("receiverId",String.class)
                    .transform(new RealmObjectSchema.Function() {
                        @Override
                        public void apply(DynamicRealmObject obj) {
                            obj.set("receiverId",obj.getString("toMobile"));
                        }
                    })
                    .removeField("toMobile")
                    .addField("isGroup",boolean.class);

            schema.create("ChatGroup")
            .addField("groupId",String.class,FieldAttribute.PRIMARY_KEY)
            .addField("inviteCode",String.class)
            .addField("groupName",String.class)
            .addField("hostersString",String.class)
            .addField("chattersString",String.class);

            oldVersion++;
        }

        if(oldVersion == 2){
            schema.get("Conversation").removeField("noteName");
            oldVersion++;
        }

        if (oldVersion == 3) {
            schema.get("Vessage")
                    .addField("body", String.class)
                    .addField("typeId", int.class);

            schema.create("ChatImage")
                    .addField("imageId", String.class, FieldAttribute.PRIMARY_KEY)
                    .addField("imageType", String.class);

            schema.create("UserChatImages")
                    .addField("userId", String.class, FieldAttribute.PRIMARY_KEY)
                    .addRealmListField("chatImages", schema.get("ChatImage"));

            schema.create("SendVessageQueueTask")
                    .addField("taskId", String.class, FieldAttribute.PRIMARY_KEY)
                    .addField("filePath", String.class)
                    .addField("receiverId", String.class)
                    .addRealmObjectField("vessage", schema.get("Vessage"))
                    .addField("steps", String.class)
                    .addField("currentStep", int.class);

            schema.create("SendVessageResultModel")
                    .addField("vessageId",String.class, FieldAttribute.PRIMARY_KEY)
                    .addField("vessageBoxId",String.class);

            schema.remove("SendVessageTask");
            oldVersion++;
        }

        if(oldVersion == 4){
            schema.get("Conversation").addField("isPinned",boolean.class);
            schema.get("VessageUser").addField("sex",int.class);
            oldVersion++;
        }

        if (oldVersion == 5){
            schema.get("Vessage").addField("gSender",String.class);
            schema.get("VessageUser").addField("acTs",long.class);
            oldVersion++;
        }

        if (oldVersion == 6){
            schema.get("Vessage").addField("ts",long.class).transform(new RealmObjectSchema.Function() {
                @Override
                public void apply(DynamicRealmObject obj) {
                    String sendTime = obj.getString("sendTime");
                    Date date = DateHelper.stringToAccurateDate(sendTime);
                    obj.set("ts", DateHelper.getUnixTimeSpanMSFromDate(date));
                }
            }).removeField("sendTime");

            schema.get("Conversation").addField("lstTs",long.class).transform(new RealmObjectSchema.Function() {
                @Override
                public void apply(DynamicRealmObject obj) {
                    Date date = obj.getDate("sLastMessageTime");
                    obj.set("lstTs", DateHelper.getUnixTimeSpanMSFromDate(date));
                }
            }).removeField("sLastMessageTime");

            schema.get("LittlePaperMessage").addField("uTs",long.class).transform(new RealmObjectSchema.Function() {
                @Override
                public void apply(DynamicRealmObject obj) {
                    String dt = obj.getString("updatedTime");
                    Date date = DateHelper.stringToAccurateDate(dt);
                    obj.set("uTs", DateHelper.getUnixTimeSpanMSFromDate(date));
                }
            }).removeField("updatedTime");

            oldVersion++;
        }

        if (oldVersion == 7){
            schema.get("SendVessageQueueTask").addField("returnVId",String.class);
            oldVersion++;
        }

        if (oldVersion == 8){
            schema.get("Conversation").addField("type",int.class).transform(new RealmObjectSchema.Function() {
                @Override
                public void apply(DynamicRealmObject obj) {
                    boolean isGroup = obj.getBoolean("isGroup");
                    obj.setInt("type", isGroup ? Conversation.TYPE_GROUP_CHAT : Conversation.TYPE_SINGLE_CHAT);
                }
            }).removeField("isGroup");
            oldVersion++;
        }
    }

    public final int schemaVersion = 9;

}
