package cn.bahamut.vessage.activities.littlepaper;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.kaopiz.kprogresshud.KProgressHUD;

import java.util.List;

import cn.bahamut.common.ProgressHUDHelper;
import cn.bahamut.vessage.R;
import cn.bahamut.vessage.activities.littlepaper.model.LittlePaperManager;
import cn.bahamut.vessage.activities.littlepaper.model.LittlePaperMessage;
import cn.bahamut.vessage.activities.littlepaper.model.LittlePaperReadResponse;
import cn.bahamut.vessage.conversation.ConversationViewActivity;
import cn.bahamut.vessage.main.LocalizedStringHelper;

public class LittlePaperResponsesActivity extends AppCompatActivity {

    private ListView listView;
    private ResponseAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_little_paper_responses);
        getSupportActionBar().setTitle(R.string.little_paper_msg_box);
        ImageView backgroundImageView = (ImageView)findViewById(R.id.bcg_img_view);
        Bitmap bitmap = BitmapFactory.decodeStream(getResources().openRawResource(R.raw.little_paper_white));
        backgroundImageView.setImageBitmap(bitmap);
        listView = (ListView)findViewById(R.id.list_view);
        adapter = new ResponseAdapter(this);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(onClickItem);
        reloadData();
    }

    private void refreshTips() {
        findViewById(R.id.no_msg_tips_text_view).setVisibility(adapter.getCount() == 0 ? View.VISIBLE:View.INVISIBLE);
    }

    private AdapterView.OnItemClickListener onClickItem = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            final LittlePaperReadResponse response = (LittlePaperReadResponse) adapter.getItem(position);
            if(response.type == LittlePaperReadResponse.TYPE_ASK_SENDER)
            {
                showAskSenerAlert(response);
            }else if(response.type == LittlePaperReadResponse.TYPE_RETURN_ASKER){
                if (response.code == LittlePaperReadResponse.CODE_ACCEPT_READ){
                    LittlePaperMessage paper = LittlePaperManager.getInstance().getPaperMessageByPaperId(response.paperId);
                    if(paper == null){
                        Toast.makeText(LittlePaperResponsesActivity.this,R.string.little_paper_not_found,Toast.LENGTH_SHORT).show();
                    }else if(paper.isOpened){
                        LittlePaperDetailActivity.showLittlePaperDetailActivity(LittlePaperResponsesActivity.this,paper.paperId);
                        LittlePaperManager.getInstance().removeReadResponse(response.paperId);
                        reloadData();
                    }else {
                        final KProgressHUD hud = ProgressHUDHelper.showSpinHUD(LittlePaperResponsesActivity.this);
                        LittlePaperManager.getInstance().refreshPaperMessageById(response.paperId, new LittlePaperManager.LittlePaperManagerOperateCallback() {
                            @Override
                            public void onCallback(boolean isOk, String errorMessage) {
                                hud.dismiss();
                                if(isOk){
                                    LittlePaperDetailActivity.showLittlePaperDetailActivity(LittlePaperResponsesActivity.this,response.paperId);
                                    LittlePaperManager.getInstance().removeReadResponse(response.paperId);
                                    reloadData();
                                }else {
                                    Toast.makeText(LittlePaperResponsesActivity.this,errorMessage,Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                }else {
                    LittlePaperMessage paper = LittlePaperManager.getInstance().getPaperMessageByPaperId(response.paperId);
                    if(paper == null){
                        Toast.makeText(LittlePaperResponsesActivity.this,R.string.little_paper_not_found,Toast.LENGTH_SHORT).show();
                    }else{
                        LittlePaperDetailActivity.showLittlePaperDetailActivity(LittlePaperResponsesActivity.this,paper.paperId);
                    }
                    LittlePaperManager.getInstance().removeReadResponse(response.paperId);
                    reloadData();
                }
            }
        }
    };

    private void reloadData() {
        adapter.loadData();
        refreshTips();
    }

    private void showAskSenerAlert(final LittlePaperReadResponse response) {
        String[] actions = new String[]{
                LocalizedStringHelper.getLocalizedString(R.string.little_paper_talk_with_ta),
                LocalizedStringHelper.getLocalizedString(R.string.little_paper_reject_read),
                LocalizedStringHelper.getLocalizedString(R.string.little_paper_accept_read)
        };
        DialogInterface.OnClickListener onClickDialogItem = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0){
                    ConversationViewActivity.openConversation(LittlePaperResponsesActivity.this,response.asker,response.askerNick);
                }else{
                    final KProgressHUD hud = ProgressHUDHelper.showSpinHUD(LittlePaperResponsesActivity.this);
                    LittlePaperManager.LittlePaperManagerOperateCallback callback = new LittlePaperManager.LittlePaperManagerOperateCallback() {
                        @Override
                        public void onCallback(boolean isOk, String errorMessage) {
                            hud.dismiss();
                            if(isOk){
                                Toast.makeText(LittlePaperResponsesActivity.this,R.string.little_paper_response_msg_sended,Toast.LENGTH_SHORT).show();
                                reloadData();
                            }else {
                                Toast.makeText(LittlePaperResponsesActivity.this,errorMessage,Toast.LENGTH_SHORT).show();
                            }
                        }
                    };
                    if(which == 1){
                        LittlePaperManager.getInstance().rejectReadPaper(response.paperId, response.asker, callback);
                        reloadData();
                    }else if(which == 2){
                        LittlePaperManager.getInstance().acceptReadPaper(response.paperId, response.asker, callback);
                        reloadData();
                    }
                }
            }
        };
        new AlertDialog.Builder(LittlePaperResponsesActivity.this)
                .setItems(actions, onClickDialogItem)
                .show();
    }

    //ViewHolder静态类
    protected static class ViewHolder
    {
        public TextView headline;
        public TextView subline;
    }

    class ResponseAdapter extends BaseAdapter {
        private LayoutInflater mInflater;
        protected Context context;
        ResponseAdapter(Context context){
            this.context = context;
            this.mInflater = LayoutInflater.from(context);
        }
        private List<LittlePaperReadResponse> data;
        public void loadData(){
            data = LittlePaperManager.getInstance().getReadPaperResponses();
            this.notifyDataSetChanged();
        }
        @Override
        public int getCount() {
            return data != null ? data.size() : 0;
        }

        @Override
        public Object getItem(int position) {
            return data.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            //如果缓存convertView为空，则需要创建View
            if (convertView == null || ((ViewHolder) convertView.getTag()) == null) {
                holder = new ViewHolder();
                //根据自定义的Item布局加载布局
                convertView = mInflater.inflate(R.layout.little_paper_response_item, null);
                holder.headline = (TextView) convertView.findViewById(R.id.headline_text);
                holder.subline = (TextView) convertView.findViewById(R.id.subline_text);
                //将设置好的布局保存到缓存中，并将其设置在Tag里，以便后面方便取出Tag
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            LittlePaperReadResponse response = data.get(position);
            holder.headline.setText(String.format(LocalizedStringHelper.getLocalizedString(R.string.little_paper_send_to_x),response.paperReceiver));
            if(response.type == LittlePaperReadResponse.TYPE_ASK_SENDER){
                holder.subline.setText(String.format(LocalizedStringHelper.getLocalizedString(R.string.little_paper_x_ask_read),response.askerNick));
            }else if(response.code == LittlePaperReadResponse.CODE_ACCEPT_READ){
                holder.subline.setText(LocalizedStringHelper.getLocalizedString(R.string.little_paper_sender_accept));
            }else if(response.code == LittlePaperReadResponse.CODE_REJECT_READ){
                holder.subline.setText(LocalizedStringHelper.getLocalizedString(R.string.little_paper_sender_reject));
            }
            return convertView;
        }
    }
}
