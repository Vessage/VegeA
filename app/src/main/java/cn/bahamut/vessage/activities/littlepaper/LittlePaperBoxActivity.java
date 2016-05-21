package cn.bahamut.vessage.activities.littlepaper;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

import cn.bahamut.vessage.R;
import cn.bahamut.vessage.activities.littlepaper.model.LittlePaperManager;
import cn.bahamut.vessage.activities.littlepaper.model.LittlePaperMessage;

public class LittlePaperBoxActivity extends Activity {

    private ListView listView;
    private int selectedPaperListType;
    private PaperListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_little_paper_box);

        ImageView backgroundImageView = (ImageView)findViewById(R.id.backgroundImageView);
        Bitmap bitmap = BitmapFactory.decodeStream(getResources().openRawResource(R.raw.little_paper_white));
        backgroundImageView.setImageBitmap(bitmap);

        listView = (ListView)findViewById(R.id.paperListView);
        adapter = new PaperListAdapter(LittlePaperBoxActivity.this);

        listView.setAdapter(adapter);
        listView.setOnItemClickListener(onClickItemListener);
        findViewById(R.id.not_deal_button).setOnClickListener(onClickBottomButton);
        findViewById(R.id.posted_button).setOnClickListener(onClickBottomButton);
        findViewById(R.id.opened_button).setOnClickListener(onClickBottomButton);
        findViewById(R.id.sended_button).setOnClickListener(onClickBottomButton);

        findViewById(R.id.no_paper_message_text_view).setOnClickListener(onClickNoPaper);

        findViewById(R.id.clearButton).setOnClickListener(onClickClearPapers);

        onSelectBox(LittlePaperManager.TYPE_MY_NOT_DEAL);
        findViewById(R.id.not_deal_button).setEnabled(false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        onSelectBox(selectedPaperListType);
    }

    private View.OnClickListener onClickClearPapers = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            LittlePaperManager.getInstance().clearPaperMessageList(selectedPaperListType);
            adapter.loadPaperList(selectedPaperListType);
            refreshClearButton();
        }
    };

    private void refreshClearButton() {
        if(selectedPaperListType == LittlePaperManager.TYPE_MY_NOT_DEAL){
            findViewById(R.id.clearButton).setVisibility(View.INVISIBLE);
        }else {
            findViewById(R.id.clearButton).setVisibility(View.VISIBLE);
            int count = LittlePaperManager.getInstance().getTypedMessages(selectedPaperListType).size();
            findViewById(R.id.clearButton).setEnabled(count > 0);
        }
    }

    private View.OnClickListener onClickNoPaper = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(LittlePaperBoxActivity.this,WriteLittlePaperActivity.class);
            startActivity(intent);
        }
    };

    private View.OnClickListener onClickBottomButton = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            switch (v.getId()){
                case R.id.not_deal_button:onSelectBox(LittlePaperManager.TYPE_MY_NOT_DEAL);break;
                case R.id.posted_button:onSelectBox(LittlePaperManager.TYPE_MY_POSTED);break;
                case R.id.opened_button:onSelectBox(LittlePaperManager.TYPE_MY_OPENED);break;
                case R.id.sended_button:onSelectBox(LittlePaperManager.TYPE_MY_SENDED);break;
            }
        }
    };

    private void diableView(int viewId) {
        findViewById(viewId).setEnabled(false);
    }

    private void enableView(int viewId) {
        findViewById(viewId).setEnabled(true);
    }

    private void onSelectBox(int paperListType) {
        enableView(R.id.not_deal_button);
        enableView(R.id.posted_button);
        enableView(R.id.opened_button);
        enableView(R.id.sended_button);

        this.selectedPaperListType = paperListType;
        adapter.loadPaperList(paperListType);
        findViewById(R.id.no_paper_message_text_view).setVisibility(adapter.getCount() > 0 ? View.INVISIBLE : View.VISIBLE);

        switch (paperListType){
            case LittlePaperManager.TYPE_MY_NOT_DEAL:diableView(R.id.not_deal_button);break;
            case LittlePaperManager.TYPE_MY_POSTED:diableView(R.id.posted_button);break;
            case LittlePaperManager.TYPE_MY_OPENED:diableView(R.id.opened_button);break;
            case LittlePaperManager.TYPE_MY_SENDED:diableView(R.id.sended_button);break;
        }

        refreshClearButton();
    }

    //ViewHolder静态类
    protected static class ViewHolder
    {
        public ImageView icon;
        public TextView headline;
        public View badge;
    }

    private AdapterView.OnItemClickListener onClickItemListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            LittlePaperMessage message = (LittlePaperMessage) adapter.getItem(position);
            Intent intent = new Intent(LittlePaperBoxActivity.this,LittlePaperDetailActivity.class);
            intent.putExtra("paperId",message.paperId);
            startActivity(intent);
        }
    };

    class PaperListAdapter extends BaseAdapter {
        private LayoutInflater mInflater;
        protected Context context;
        PaperListAdapter(Context context){
            this.context = context;
            this.mInflater = LayoutInflater.from(context);
        }
        private List<LittlePaperMessage> paperMessages;
        public void loadPaperList(int type){
            paperMessages = LittlePaperManager.getInstance().getTypedMessages(type);
            this.notifyDataSetChanged();
        }
        @Override
        public int getCount() {
            return paperMessages != null ? paperMessages.size() : 0;
        }

        @Override
        public Object getItem(int position) {
            return paperMessages.get(position);
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
                convertView = mInflater.inflate(R.layout.little_paper_box_item, null);
                holder.icon = (ImageView) convertView.findViewById(R.id.iconImageView);
                holder.headline = (TextView) convertView.findViewById(R.id.headlineTextView);
                holder.badge = (TextView) convertView.findViewById(R.id.badgeTextView);
                //将设置好的布局保存到缓存中，并将其设置在Tag里，以便后面方便取出Tag
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            LittlePaperMessage msg = paperMessages.get(position);
            holder.headline.setText(msg.receiverInfo);
            holder.badge.setVisibility(msg.isUpdated ? View.VISIBLE : View.INVISIBLE);

            return convertView;
        }
    }
}
