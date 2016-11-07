package cn.bahamut.vessage.conversation.bubblevessage;

import android.app.Activity;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.json.JSONObject;

import cn.bahamut.common.BTSize;
import cn.bahamut.service.ServicesProvider;
import cn.bahamut.vessage.R;
import cn.bahamut.vessage.services.vessage.Vessage;
import cn.bahamut.vessage.services.vessage.VessageService;

/**
 * Created by alexchow on 2016/11/4.
 */

public class TextBubbleVessageHandler implements BubbleVessageHandler {
    private static final int bubbleColorMyVessageTextColor = Color.parseColor("#eeeeee");
    private static final int bubbleColorNormalVessageTextColor = Color.parseColor("#333333");
    @Override
    public BTSize getContentViewSize(Activity context, Vessage vessage, BTSize maxLimitedSize, View contentView) {
        if (contentView instanceof ViewGroup) {
            String textMessage = getTextContent(vessage);
            TextView tv = (TextView) contentView.findViewById(R.id.content_text_view);
            tv.setText(textMessage);
            int specW = View.MeasureSpec.makeMeasureSpec((int)maxLimitedSize.width, View.MeasureSpec.EXACTLY);
            int specH = View.MeasureSpec.makeMeasureSpec((int)maxLimitedSize.height, View.MeasureSpec.EXACTLY);
            contentView.measure(specW,specH);
            contentView.layout(0,0,(int)maxLimitedSize.width,(int)maxLimitedSize.height);
            int w = View.MeasureSpec.getSize(tv.getMeasuredWidth());
            int h = View.MeasureSpec.getSize(tv.getMeasuredHeight());
            return new BTSize(w,h);
        }
        return BTSize.ZERO;
    }

    @Override
    public ViewGroup getContentView(Activity context, Vessage vessage) {
        ViewGroup vg = (ViewGroup) context.getLayoutInflater().inflate(R.layout.vessage_content_text,null);
        return vg;
    }

    @Override
    public void presentContent(Activity context,Vessage oldVessage, Vessage newVessage, View contentView) {
        if (contentView instanceof ViewGroup){
            ServicesProvider.getService(VessageService.class).readVessage(newVessage);
            TextView tv = (TextView) contentView.findViewById(R.id.content_text_view);
            tv.setTextColor(newVessage.isMySendingVessage() ? bubbleColorMyVessageTextColor : bubbleColorNormalVessageTextColor);
            tv.setText(getTextContent(newVessage));
        }
    }

    @Override
    public void onUnloadVessage(Activity context) {

    }

    @Override
    public void onPrepareVessage(Activity context, Vessage vessage) {

    }

    protected String getTextContent(Vessage vessage){
        try
        {
            JSONObject body = vessage.getBodyJsonObject();
            return body.getString("textMessage");
        }catch (Exception e){
            return null;
        }
    }

    @Override
    public BubbleVessageHandler instanceOfVessage(Activity context, Vessage vessage) {
        return new TextBubbleVessageHandler();
    }
}
