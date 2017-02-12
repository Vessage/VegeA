package cn.bahamut.vessage.userprofile;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import cn.bahamut.common.FullScreenImageViewer;
import cn.bahamut.common.StringHelper;
import cn.bahamut.observer.Observer;
import cn.bahamut.observer.ObserverState;
import cn.bahamut.service.ServicesProvider;
import cn.bahamut.vessage.R;
import cn.bahamut.vessage.activities.ExtraActivitiesActivity;
import cn.bahamut.vessage.activities.sns.SNSPostManager;
import cn.bahamut.vessage.helper.ImageHelper;
import cn.bahamut.vessage.main.AssetsDefaultConstants;
import cn.bahamut.vessage.services.user.UserService;
import cn.bahamut.vessage.services.user.VessageUser;

/**
 * Created by alexchow on 2017/1/4.
 */

public class UserProfileView {
    private VessageUser profile;

    private Activity context;
    public UserProfileViewDelegate delegate;
    private ViewGroup content;

    public Activity getContext() {
        return context;
    }

    private UserProfileViewListener listener;

    public UserProfileViewListener getListener() {
        return listener;
    }

    public void setListener(UserProfileViewListener listener) {
        this.listener = listener;
    }

    public interface UserProfileViewListener {
        void onProfileViewWillShow(UserProfileView sender);

        void onProfileViewWillClose(UserProfileView sender);
    }

    public UserProfileView(Activity context, VessageUser profile) {
        this.context = context;
        this.profile = profile;
        content = (ViewGroup) context.getLayoutInflater().inflate(R.layout.user_profile_view, null);
    }

    private void addListeners() {
        content.findViewById(R.id.dialog).setOnClickListener(onViewClicked);
        content.findViewById(R.id.background).setOnClickListener(onViewClicked);
        content.findViewById(R.id.btn_close).setOnClickListener(onViewClicked);
        content.findViewById(R.id.sns).setOnClickListener(onViewClicked);
        content.findViewById(R.id.btn_right).setOnClickListener(onViewClicked);
        content.findViewById(R.id.avatar).setOnClickListener(onViewClicked);
    }

    private void removeListeners() {
        content.findViewById(R.id.background).setOnClickListener(null);
        content.findViewById(R.id.btn_close).setOnClickListener(null);
        content.findViewById(R.id.sns).setOnClickListener(null);
        content.findViewById(R.id.btn_right).setOnClickListener(null);
        content.findViewById(R.id.avatar).setOnClickListener(null);
    }

    private void updateContentView() {
        if (profile != null) {
            ImageView imageView = (ImageView) content.findViewById(R.id.avatar);
            int defaultAvatarId = profile.userId == null ? 0 : profile.userId.hashCode();
            ImageHelper.setImageByFileId(imageView, profile.avatar, AssetsDefaultConstants.getDefaultFace(defaultAvatarId, profile.sex));

            if (profile.sex > 0) {
                ImageHelper.setViewImage(content.findViewById(R.id.sex), R.drawable.sex_male);
            } else if (profile.sex < 0) {
                ImageHelper.setViewImage(content.findViewById(R.id.sex), R.drawable.sex_female);
            } else {
                ImageHelper.setViewImage(content.findViewById(R.id.sex), R.drawable.sex_middle);
            }

            String rightButtonTitle = delegate != null ? delegate.getRightButtonTitle(this, profile) : null;
            if (StringHelper.isStringNullOrWhiteSpace(rightButtonTitle) == false) {
                ((TextView) content.findViewById(R.id.btn_right)).setText(rightButtonTitle);
            }

            TextView accountTextView = (TextView) content.findViewById(R.id.account_id);
            if (delegate == null || delegate.showAccountId(this, profile)) {
                if (StringHelper.isStringNullOrWhiteSpace(profile.accountId)) {
                    accountTextView.setText(R.string.mobile_user);
                } else {
                    accountTextView.setText(profile.accountId);
                }
                accountTextView.setVisibility(View.VISIBLE);
            } else {
                accountTextView.setVisibility(View.INVISIBLE);
            }

            TextView nick = (TextView) content.findViewById(R.id.nick);
            String notedName = ServicesProvider.getService(UserService.class).getUserNotedNameIfExists(profile.userId);
            if (StringHelper.isStringNullOrWhiteSpace(notedName)) {
                nick.setText(profile.nickName);
            } else {
                nick.setText(String.format("%s(%s)", profile.nickName, notedName));
            }

            TextView motto = (TextView) content.findViewById(R.id.motto_text_view);
            if (StringHelper.isStringNullOrWhiteSpace(profile.motto)) {
                motto.setText(R.string.default_motto);
            } else {
                motto.setText(profile.motto);
            }
        } else {
            throw new NullPointerException("User Profile Can't Be Null");
        }
    }

    private View.OnClickListener onViewClicked = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.background:
                case R.id.btn_close:
                    close();
                    break;
                case R.id.sns:
                    showSNS();
                    break;
                case R.id.btn_right:
                    clickRightButton();
                    break;
                case R.id.avatar:
                    clickAvatar();
                    break;

                default:
                    break;
            }
        }
    };

    private void clickAvatar() {
        ImageHelper.getImageByFileId(profile.avatar, new ImageHelper.OnGetImageCallback() {
            @Override
            public void onGetImageDrawable(Drawable drawable) {
                new FullScreenImageViewer.Builder(context).setImageFileId(profile.avatar).show();
            }

            @Override
            public void onGetImageResId(int resId) {
                new FullScreenImageViewer.Builder(context).setImageResId(resId).show();
            }

            @Override
            public void onGetImageFailed() {
                Toast.makeText(context, R.string.no_image, Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void clickRightButton() {
        if (delegate != null) {
            delegate.onClickButtonRight(this, profile);
        }
    }

    private void showSNS() {
        Intent intent = new Intent();
        intent.putExtra("specificUserId", profile.userId);
        String nick = ServicesProvider.getService(UserService.class).getUserNotedNameIfExists(profile.userId);
        if (StringHelper.isStringNullOrWhiteSpace(nick)) {
            nick = profile.nickName;
        }
        intent.putExtra("specificUserNick", nick);
        intent.putExtra("userPageMode", true);
        ExtraActivitiesActivity.startExtraActivity(this.getContext(), SNSPostManager.ACTIVITY_ID, intent);
    }

    private Observer onProfileUpdated = new Observer() {
        @Override
        public void update(ObserverState state) {
            VessageUser user = (VessageUser) state.getInfo();
            if (user != null && user.userId.equals(UserProfileView.this.profile.userId)) {
                updateContentView();
            }
        }
    };

    public void show() {
        if (listener != null) {
            listener.onProfileViewWillShow(this);
        }
        ViewGroup container = (ViewGroup) context.findViewById(android.R.id.content);
        container.addView(content);
        updateContentView();
        addListeners();
        UserService userService = ServicesProvider.getService(UserService.class);
        userService.addObserver(UserService.NOTIFY_USER_PROFILE_UPDATED, onProfileUpdated);
        userService.setForceFetchUserProfileOnece();
        userService.fetchUserByUserId(profile.userId);
    }

    public void close() {
        if (listener != null) {
            listener.onProfileViewWillClose(this);
        }
        ViewGroup container = (ViewGroup) context.findViewById(android.R.id.content);
        container.removeView(content);
        removeListeners();
        ServicesProvider.getService(UserService.class).deleteObserver(UserService.NOTIFY_USER_PROFILE_UPDATED, onProfileUpdated);
    }
}
