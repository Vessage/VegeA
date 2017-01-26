package cn.bahamut.vessage.conversation.chat;

import android.graphics.Color;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.LinkedList;
import java.util.List;

import cn.bahamut.common.DateHelper;
import cn.bahamut.common.StringHelper;
import cn.bahamut.service.ServicesProvider;
import cn.bahamut.vessage.R;
import cn.bahamut.vessage.activities.vtm.VessageTimeMachine;
import cn.bahamut.vessage.conversation.chat.views.BezierBubbleView;
import cn.bahamut.vessage.main.AppUtil;
import cn.bahamut.vessage.services.user.UserService;
import cn.bahamut.vessage.services.vessage.Vessage;

/**
 * Created by cplov on 2017/1/26.
 */

public class VessageTimeMachineManager extends ConversationViewManagerBase {


    class ViewHolder extends RecyclerView.ViewHolder {
        private TextView sublineTextView;
        private TextView titleTextView;

        public ViewHolder(View itemView) {
            super(itemView);

            titleTextView = (TextView) itemView.findViewById(R.id.title_view);
            sublineTextView = (TextView) itemView.findViewById(R.id.subline_text);
        }
    }


    class Adapter extends RecyclerView.Adapter<ViewHolder> {

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = getConversationViewActivity().getLayoutInflater().inflate(R.layout.conversation_view_time_machine_item, null);
            return new ViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            VessageTimeMachine.VessageTimeMachineRecordItem item = recordItems.get(position);

            String nick = userService.getUserNoteOrNickName(item.vessage.getVessageRealSenderId());
            String dateString = AppUtil.dateToFriendlyString(getConversationViewActivity(), DateHelper.getDateFromUnixTimeSpace(item.vessage.ts));
            holder.titleTextView.setText(String.format("%s %s", nick, dateString));
            switch (item.vessage.typeId){
                case Vessage.TYPE_CHAT_VIDEO:
                    holder.sublineTextView.setText(R.string.send_a_chat_video);
                    break;
                case Vessage.TYPE_FACE_TEXT:
                    try
                    {
                        holder.sublineTextView.setText(item.vessage.getBodyJsonObject().getString("textMessage"));
                    }catch (Exception ex){
                        holder.sublineTextView.setText(R.string.send_an_invalide_data);
                    }
                    break;
                case Vessage.TYPE_IMAGE:
                    holder.sublineTextView.setText(R.string.send_an_image);
                    break;
                case Vessage.TYPE_LITTLE_VIDEO:
                    holder.sublineTextView.setText(R.string.send_a_little_video);
                    break;
                default:
                    holder.sublineTextView.setText(R.string.send_an_unknow_msg);
                    break;
            }
        }

        @Override
        public int getItemCount() {
            return recordItems.size();
        }
    }


    private RecyclerView vessageRecordList;
    private ViewGroup contentView;
    private BezierBubbleView bubbleView;

    private List<VessageTimeMachine.VessageTimeMachineRecordItem> recordItems;
    private UserService userService;

    @Override
    public void initManager(ConversationViewActivity activity) {
        super.initManager(activity);
        userService = ServicesProvider.getService(UserService.class);

        contentView = (ViewGroup) activity.getLayoutInflater().inflate(R.layout.conversation_view_time_machine, null);

        bubbleView = (BezierBubbleView)contentView.findViewById(R.id.bubble_view);
        bubbleView.setStartRatio(0.1f);
        bubbleView.setDirection(BezierBubbleView.BezierBubbleDirection.Up);
        bubbleView.setFillColor(Color.WHITE);

        vessageRecordList = (RecyclerView) contentView.findViewById(R.id.vessage_record_list);
        vessageRecordList.setLayoutManager(new LinearLayoutManager(getConversationViewActivity(), LinearLayoutManager.VERTICAL, true));
        recordItems = new LinkedList<>();

        VessageTimeMachine.VessageTimeMachineRecordItem[] items = VessageTimeMachine.getInstance().getVessageRecords(getConversation().chatterId, DateHelper.getUnixTimeSpan(), 10);

        for (VessageTimeMachine.VessageTimeMachineRecordItem item : items) {
            recordItems.add(item);
        }

        contentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideVessageRecordList();
            }
        });

        findViewById(R.id.btn_vsg_time_machine).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showVessageRecordList();
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        userService = null;
    }

    private void hideVessageRecordList() {
        ViewGroup rootContent = (ViewGroup) getConversationViewActivity().findViewById(R.id.play_vsg_container);
        rootContent.removeView(contentView);
    }

    private void showVessageRecordList() {
        initVessageListView();
        ViewGroup rootContent = (ViewGroup) getConversationViewActivity().findViewById(R.id.play_vsg_container);
        rootContent.addView(contentView);
        contentView.getLayoutParams().height = rootContent.getLayoutParams().width;
        contentView.getLayoutParams().height = rootContent.getLayoutParams().height;
        contentView.setLayoutParams(contentView.getLayoutParams());
    }

    private void initVessageListView() {
        if (vessageRecordList.getAdapter() == null) {
            vessageRecordList.setAdapter(new Adapter());
            if (recordItems.size() > 0) {
                vessageRecordList.scrollToPosition(0);
                vessageRecordList.addOnScrollListener(new RecyclerView.OnScrollListener() {
                    @Override
                    public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                        LinearLayoutManager lm = (LinearLayoutManager) recyclerView.getLayoutManager();
                        int totalItemCount = recyclerView.getAdapter().getItemCount();
                        int lastVisibleItemPosition = lm.findLastVisibleItemPosition();
                        int visibleItemCount = recyclerView.getChildCount();

                        if (newState == RecyclerView.SCROLL_STATE_IDLE
                                && lastVisibleItemPosition == totalItemCount - 1
                                && visibleItemCount > 0) {
                            String chatterId = getConversation().chatterId;
                            long ts = recordItems.get(recordItems.size() - 1).vessage.ts;
                            VessageTimeMachine.VessageTimeMachineRecordItem[] items = VessageTimeMachine.getInstance().getVessageRecords(chatterId, ts, 30);
                            if (items.length > 0) {
                                int index = recordItems.size();
                                for (VessageTimeMachine.VessageTimeMachineRecordItem item : items) {
                                    recordItems.add(item);
                                }
                                recyclerView.getAdapter().notifyItemRangeInserted(index, items.length);
                            } else {
                                recyclerView.removeOnScrollListener(this);
                            }
                        }
                    }
                });
            }
        }
    }
}
