package cn.bahamut.vessage.conversation.vessagehandler;

import android.content.Context;
import android.graphics.Point;
import android.view.ViewGroup;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

import cn.bahamut.common.DateHelper;
import cn.bahamut.vessage.R;
import cn.bahamut.vessage.conversation.view.ConversationViewPlayManager;
import cn.bahamut.vessage.conversation.view.FaceTextView;
import cn.bahamut.vessage.main.AppUtil;
import cn.bahamut.vessage.main.LocalizedStringHelper;
import cn.bahamut.vessage.services.vessage.Vessage;

/**
 * Created by alexchow on 16/9/7.
 */
public class FaceTextVessageHandler extends VessageHandlerBase {
    private final ViewGroup contentContainer;
    private FaceTextView faceTextView;
    private Context context;
    private TextView dateTextView;

    public FaceTextVessageHandler(ConversationViewPlayManager playVessageManager, ViewGroup vessageContainer) {
        super(playVessageManager, vessageContainer);
        context = playVessageManager.getConversationViewActivity();
        contentContainer = (ViewGroup)playVessageManager.getConversationViewActivity().getLayoutInflater().inflate(R.layout.face_text_vessage_container,null);
        faceTextView = new FaceTextView(playVessageManager.getConversationViewActivity(),contentContainer);
        dateTextView = (TextView)contentContainer.findViewById(R.id.vsg_date_tv);
    }

    private void updateVideoDateTextView() {
        if (presentingVessage != null){
            Date sendTime = DateHelper.stringToAccurateDate(presentingVessage.sendTime);
            String friendlyDateString = AppUtil.dateToFriendlyString(playVessageManager.getConversationViewActivity(),sendTime);
            String readStatus = LocalizedStringHelper.getLocalizedString(presentingVessage.isRead ? R.string.vsg_readed : R.string.vsg_unreaded);
            dateTextView.setText(String.format("%s %s",friendlyDateString,readStatus));
            dateTextView.bringToFront();
        }
    }

    private void setContainerLayoutParams() {
        Point size = new Point();
        playVessageManager.getConversationViewActivity().getWindowManager().getDefaultDisplay().getSize(size);
        ViewGroup.LayoutParams params = contentContainer.getLayoutParams();
        params.height = size.y / 2;
        params.width = params.height * 3 / 4;
        contentContainer.setLayoutParams(params);
    }

    @Override
    public void onPresentingVessageSeted(Vessage oldVessage, Vessage newVessage) {
        super.onPresentingVessageSeted(oldVessage, newVessage);
        if (oldVessage == null || oldVessage.typeId != newVessage.typeId){
            vessageContainer.removeAllViews();
            vessageContainer.addView(contentContainer);
        }
        setContainerLayoutParams();
        try {
            JSONObject body = new JSONObject(newVessage.body);
            String msg = body.getString("textMessage");
            faceTextView.setFaceText(newVessage.fileId,msg);
            if (!newVessage.isRead){
                playVessageManager.readVessage();
            }
        } catch (JSONException e) {
            faceTextView.setFaceText(newVessage.fileId,"");
            e.printStackTrace();
        }
        updateVideoDateTextView();
    }
}
