package cn.bahamut.vessage.conversation.chat.bubblevessage;

import android.app.Activity;
import android.graphics.Color;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.json.JSONObject;

import java.util.Date;

import cn.bahamut.common.BTSize;
import cn.bahamut.service.ServicesProvider;
import cn.bahamut.vessage.R;
import cn.bahamut.vessage.conversation.chat.TextMessageViewerActivity;
import cn.bahamut.vessage.services.vessage.Vessage;
import cn.bahamut.vessage.services.vessage.VessageService;

/**
 * Created by alexchow on 2016/11/4.
 */

public class TextBubbleVessageHandler implements BubbleVessageHandler {
    private static final int bubbleColorMyVessageTextColor = Color.parseColor("#eeeeee");
    private static final int bubbleColorNormalVessageTextColor = Color.parseColor("#333333");
    private static VessageViewHandlerPool<TextBubbleVessageHandler> pool = new VessageViewHandlerPool<>();

    @Override
    public BTSize getContentViewSize(Activity context, Vessage vessage, BTSize maxLimitedSize, View contentView) {
        if (contentView instanceof ViewGroup) {
            String textMessage = getTextContent(vessage);
            TextView tv = (TextView) contentView.findViewById(R.id.content_text_view);
            tv.setAutoLinkMask(Linkify.ALL);
            tv.setMovementMethod(LinkMovementMethod.getInstance());
            tv.setText(textMessage);
            int specW = View.MeasureSpec.makeMeasureSpec((int)(maxLimitedSize.width * 0.8), View.MeasureSpec.EXACTLY);
            int specH = View.MeasureSpec.makeMeasureSpec((int)(maxLimitedSize.height * 0.8), View.MeasureSpec.EXACTLY);
            contentView.measure(specW,specH);
            contentView.layout(0,0,(int)maxLimitedSize.width,(int)maxLimitedSize.height);
            int w = View.MeasureSpec.getSize(tv.getMeasuredWidth());
            int h = View.MeasureSpec.getSize(tv.getMeasuredHeight());
            if (h <= tv.getMinHeight()){
                tv.setGravity(Gravity.CENTER);
            }else {
                tv.setGravity(Gravity.LEFT);
            }
            return new BTSize(w,h);
        }
        return BTSize.ZERO;
    }

    @Override
    public ViewGroup getContentView(Activity context, Vessage vessage) {
        ViewGroup vg = (ViewGroup) context.getLayoutInflater().inflate(R.layout.vessage_content_text,null);
        return vg;
    }

    private class OnClickContentView implements View.OnClickListener {
        private Activity context;
        private String content;
        private Date date;

        OnClickContentView(Activity context, String content, Date date) {
            this.context = context;
            this.date = date;
            this.content = content;
        }

        @Override
        public void onClick(View v) {
            TextMessageViewerActivity.showTextMessageViewerActivity(context, content, date);
        }
    }

    @Override
    public void presentContent(Activity context,Vessage oldVessage, Vessage newVessage, View contentView) {
        if (contentView instanceof ViewGroup) {
            ServicesProvider.getService(VessageService.class).readVessage(newVessage);
            TextView tv = (TextView) contentView.findViewById(R.id.content_text_view);
            tv.setTextColor(newVessage.isMySendingVessage() ? bubbleColorMyVessageTextColor : bubbleColorNormalVessageTextColor);
            tv.setText(getTextContent(newVessage));
            contentView.setOnClickListener(new OnClickContentView(context, getTextContent(newVessage), new Date(newVessage.ts)));
        }
    }

    @Override
    public void onUnloadVessage(Activity context) {
        pool.recycleHandler(context, this);
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
        TextBubbleVessageHandler handler = pool.getHandler(context, vessage);
        if (handler == null) {
            handler = new TextBubbleVessageHandler();
            pool.registHandler(context, handler);
        }
        return handler;
    }
}
