package cn.bahamut.vessage.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
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

import java.util.List;

import cn.bahamut.common.StringHelper;
import cn.bahamut.observer.Observer;
import cn.bahamut.observer.ObserverState;
import cn.bahamut.service.ServicesProvider;
import cn.bahamut.vessage.R;
import cn.bahamut.vessage.services.activities.ExtraActivitiesService;
import cn.bahamut.vessage.services.activities.ExtraActivityInfo;

public class ExtraActivitiesActivity extends AppCompatActivity {

    private ListView activityListView;
    private ExtraActivitiesListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setTitle(R.string.new_intersting);

        setContentView(R.layout.extra_activity_extra_list);
        activityListView = (ListView) findViewById(R.id.activities_lv);
        activityListView.setOnItemClickListener(onClickItemListener);
        adapter = new ExtraActivitiesListAdapter(ExtraActivitiesActivity.this);
        adapter.reloadActivities();
        activityListView.setAdapter(adapter);
        ServicesProvider.getService(ExtraActivitiesService.class).addObserver(ExtraActivitiesService.ON_ACTIVITIES_NEW_BADGES_UPDATED,onBadgedUpdated);
        ServicesProvider.getService(ExtraActivitiesService.class).addObserver(ExtraActivitiesService.ON_ACTIVITY_BADGE_UPDATED,onBadgedUpdated);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ServicesProvider.getService(ExtraActivitiesService.class).deleteObserver(ExtraActivitiesService.ON_ACTIVITY_BADGE_UPDATED,onBadgedUpdated);
        ServicesProvider.getService(ExtraActivitiesService.class).deleteObserver(ExtraActivitiesService.ON_ACTIVITIES_NEW_BADGES_UPDATED,onBadgedUpdated);
    }

    private Observer onBadgedUpdated = new Observer() {
        @Override
        public void update(ObserverState state) {
            adapter.refreshBadge();
        }
    };

    //ViewHolder静态类
    protected static class ViewHolder
    {
        public ImageView icon;
        public TextView headline;
        public TextView badge;
        public View badgeDot;

        public void setBadge(int badge){
            if(badge == 0){
                setBadge(null);
            }else {
                setBadge(String.valueOf(badge));
            }
        }

        private void setBadge(String badgeValue){
            if(StringHelper.isNullOrEmpty(badgeValue)){
                badge.setVisibility(View.INVISIBLE);
            }else {
                badge.setVisibility(View.VISIBLE);
                badge.setText(badgeValue);
            }
        }
    }
    private AdapterView.OnItemClickListener onClickItemListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            ExtraActivityInfo info = adapter.activityInfoList.get(position);
            try {
                Class<?> cls = Class.forName(info.activityClassName);
                Intent intent = new Intent(ExtraActivitiesActivity.this,cls);
                startActivity(intent);
            } catch (ClassNotFoundException e) {
                Toast.makeText(ExtraActivitiesActivity.this,R.string.not_found_activity_class_name,Toast.LENGTH_SHORT).show();
            }
        }
    };

    class ExtraActivitiesListAdapter extends BaseAdapter{
        private LayoutInflater mInflater;
        protected Context context;
        ExtraActivitiesListAdapter(Context context){
            this.context = context;
            this.mInflater = LayoutInflater.from(context);
        }
        private List<ExtraActivityInfo> activityInfoList;
        public void reloadActivities(){
            activityInfoList = ServicesProvider.getService(ExtraActivitiesService.class).getEnabledActivities();
            this.notifyDataSetChanged();
        }
        @Override
        public int getCount() {
            return activityInfoList != null ? activityInfoList.size() : 0;
        }

        @Override
        public Object getItem(int position) {
            return activityInfoList.get(position);
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
                convertView = mInflater.inflate(R.layout.extra_activity_list_item, null);
                holder.icon = (ImageView) convertView.findViewById(R.id.icon_img_view);
                holder.headline = (TextView) convertView.findViewById(R.id.headline_text);
                holder.badge = (TextView) convertView.findViewById(R.id.badge_tv);
                holder.badgeDot = convertView.findViewById(R.id.badge_dot);
                //将设置好的布局保存到缓存中，并将其设置在Tag里，以便后面方便取出Tag
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            ExtraActivitiesService service = ServicesProvider.getService(ExtraActivitiesService.class);
            ExtraActivityInfo info = activityInfoList.get(position);
            holder.icon.setImageResource(info.iconResId);
            holder.headline.setText(info.title);
            if(service.isAcitityShowLittleBadge(info.activityId)){
                holder.badgeDot.setVisibility(View.VISIBLE);
            }else {
                holder.badgeDot.setVisibility(View.INVISIBLE);
            }
            int badge = service.getEnabledActivityBadge(info.activityId);
            holder.setBadge(badge);
            return convertView;
        }

        public void refreshBadge(){
            notifyDataSetChanged();
        }
    }
}
