package cn.bahamut.vessage.conversation;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import cn.bahamut.common.StringHelper;
import cn.bahamut.observer.Observer;
import cn.bahamut.observer.ObserverState;
import cn.bahamut.service.ServicesProvider;
import cn.bahamut.vessage.R;
import cn.bahamut.vessage.helper.ImageHelper;
import cn.bahamut.vessage.services.user.UserService;
import cn.bahamut.vessage.services.user.VessageUser;

/**
 * Created by alexchow on 16/4/2.
 */
public abstract class ConversationListAdapterBase extends BaseAdapter {
    private Context context;

    protected Context getContext() {
        return context;
    }

    protected static class ItemModel {
        public String avatar;
        public String headLine;
        public String subLine;
        public String badge;
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
        public TextView badge;

        public void setBadge(int badge){
            if(badge == 0){
                setBadge(null);
            }else {
                setBadge(String.valueOf(badge));
            }
        }

        private void setBadge(String badgeValue){
            if(StringHelper.isStringNullOrEmpty(badgeValue)){
                badge.setVisibility(View.INVISIBLE);
            }else {
                badge.setVisibility(View.VISIBLE);
                badge.setText(badgeValue);
            }
        }
    }

    protected List<ItemModel> data;
    protected LayoutInflater mInflater = null;

    public ConversationListAdapterBase(Context context){
        this.context = context;
        ServicesProvider.getService(UserService.class).addObserver(UserService.NOTIFY_USER_PROFILE_UPDATED,onUserProfileUpdated);
        this.mInflater = LayoutInflater.from(context);
    }

    private Observer onUserProfileUpdated = new Observer() {
        @Override
        public void update(ObserverState state) {
            VessageUser user = (VessageUser) state.getInfo();
            if(user != null){
                notifyDataSetChanged();
            }
        }
    };

    @Override
    protected void finalize() throws Throwable {
        ServicesProvider.getService(UserService.class).deleteObserver(UserService.NOTIFY_USER_PROFILE_UPDATED,onUserProfileUpdated);
        super.finalize();
    }

    @Override
    public int getCount() {
        if(data == null){
            return 0;
        }
        return data.size();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder = null;
        //如果缓存convertView为空，则需要创建View
        if (convertView == null || ((ViewHolder) convertView.getTag()) == null) {
            holder = new ViewHolder();
            //根据自定义的Item布局加载布局
            convertView = mInflater.inflate(R.layout.conversation_list_view_item, null);
            holder.avatar = (ImageView) convertView.findViewById(R.id.avatar_img_view);
            holder.headline = (TextView) convertView.findViewById(R.id.headline_text);
            holder.subline = (TextView) convertView.findViewById(R.id.subline_text);
            holder.badge = (TextView) convertView.findViewById(R.id.badge_tv);
            //将设置好的布局保存到缓存中，并将其设置在Tag里，以便后面方便取出Tag
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        ImageHelper.setImageByFileId(holder.avatar, data.get(position).avatar, R.mipmap.default_avatar);
        holder.headline.setText(data.get(position).headLine);
        holder.subline.setText(data.get(position).subLine);
        String badge = data.get(position).badge;
        try {
            int badgeValue = Integer.parseInt(badge);
            holder.setBadge(badgeValue);
        } catch (NumberFormatException e) {
            holder.setBadge(0);
        }

        return convertView;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public Object getItem(int position) {
        return data.get(position);
    }
}
