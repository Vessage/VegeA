package cn.bahamut.vessage.conversation;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import cn.bahamut.vessage.R;

/**
 * Created by alexchow on 16/4/2.
 */
public abstract class ConversationListAdapterBase extends BaseAdapter {
    protected static class ItemModel {
        public String avatar;
        public String headLine;
        public String subLine;
        public Object originModel;
        public ItemModel(){

        }
    }

    //ViewHolder静态类
    protected static class ViewHolder
    {
        public ImageView avatar;
        public TextView headline;
        public TextView subline;
    }

    protected List<ItemModel> data;
    protected LayoutInflater mInflater = null;

    public ConversationListAdapterBase(Context context){

        this.mInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder = null;
        //如果缓存convertView为空，则需要创建View
        if(convertView == null || ((ViewHolder)convertView.getTag()) == null)
        {
            holder = new ViewHolder();
            //根据自定义的Item布局加载布局
            convertView = mInflater.inflate(R.layout.conversation_list_view_item, null);
            holder.avatar = (ImageView)convertView.findViewById(R.id.avatarImageView);
            holder.headline = (TextView)convertView.findViewById(R.id.headlineTextView);
            holder.subline = (TextView)convertView.findViewById(R.id.sublineTextView);
            //将设置好的布局保存到缓存中，并将其设置在Tag里，以便后面方便取出Tag
            convertView.setTag(holder);
        }else
        {
            holder = (ViewHolder)convertView.getTag();
        }
        //TODO:
        //holder.avatar.setBackgroundResource((Integer)data.get(position).avatar);
        holder.headline.setText((String)data.get(position).headLine);
        holder.subline.setText((String)data.get(position).subLine);

        return convertView;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public Object getItem(int position) {
        return position;
    }
}
