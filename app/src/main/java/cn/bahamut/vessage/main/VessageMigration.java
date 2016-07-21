package cn.bahamut.vessage.main;

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
    public final int schemaVersion = 3;
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
    }

    @Override
    public boolean equals(Object o) {
        return schemaVersion == ((VessageMigration)o).schemaVersion;
    }
}
