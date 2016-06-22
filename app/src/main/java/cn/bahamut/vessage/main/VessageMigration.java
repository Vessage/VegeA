package cn.bahamut.vessage.main;

import io.realm.DynamicRealm;
import io.realm.FieldAttribute;
import io.realm.RealmMigration;
import io.realm.RealmSchema;

/**
 * Created by alexchow on 16/6/20.
 */
public class VessageMigration implements RealmMigration {
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
    }
}
