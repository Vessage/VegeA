package cn.bahamut.vessage.activities.sns;

import com.umeng.analytics.MobclickAgent;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import cn.bahamut.restfulkit.BahamutRFKit;
import cn.bahamut.restfulkit.client.APIClient;
import cn.bahamut.restfulkit.client.base.OnRequestCompleted;
import cn.bahamut.service.ServicesProvider;
import cn.bahamut.vessage.activities.sns.model.SNSMainBoardData;
import cn.bahamut.vessage.activities.sns.model.SNSPost;
import cn.bahamut.vessage.activities.sns.model.SNSPostComment;
import cn.bahamut.vessage.activities.sns.model.SNSPostLike;
import cn.bahamut.vessage.activities.sns.request.DeleteSNSPostRequest;
import cn.bahamut.vessage.activities.sns.request.GetMySNSPostRequest;
import cn.bahamut.vessage.activities.sns.request.GetSNSMainBoardDataRequest;
import cn.bahamut.vessage.activities.sns.request.GetSNSMyCommentsRequest;
import cn.bahamut.vessage.activities.sns.request.GetSNSMyReceivedLikesRequest;
import cn.bahamut.vessage.activities.sns.request.GetSNSPostCommentRequest;
import cn.bahamut.vessage.activities.sns.request.GetSNSPostReqeust;
import cn.bahamut.vessage.activities.sns.request.GetSNSValuesRequestBase;
import cn.bahamut.vessage.activities.sns.request.ReportObjectionableSNSPostRequest;
import cn.bahamut.vessage.activities.sns.request.SNSGodBlockMemberRequest;
import cn.bahamut.vessage.activities.sns.request.SNSGodDeletePostRequest;
import cn.bahamut.vessage.activities.sns.request.SNSGodLikePostRequest;
import cn.bahamut.vessage.activities.sns.request.SNSLikePostRequest;
import cn.bahamut.vessage.activities.sns.request.SNSNewCommentRequest;
import cn.bahamut.vessage.activities.sns.request.SNSPostNewRequest;
import cn.bahamut.vessage.main.AppMain;
import cn.bahamut.vessage.main.UserSetting;
import cn.bahamut.vessage.services.LocationService;
import cn.bahamut.vessage.services.conversation.ConversationService;
import cn.bahamut.vessage.services.user.UserService;
import cn.bahamut.vessage.services.user.VessageUser;

/**
 * Created by alexchow on 2016/11/13.
 */

public class SNSPostManager {

    private static SNSPostManager instance = null;

    public static SNSPostManager getInstance(){
        if (instance == null){
            instance = new SNSPostManager();
        }
        return instance;
    }

    public static String SNS_LIKED_POSTIDS_KEY = "SNS_LIKED_POSTIDS";

    private VessageUser userProfile;

    public void initManager() {
        userProfile = ServicesProvider.getService(UserService.class).getMyProfile();
        likedPost = new HashMap<>();
        Set<String> postIds = UserSetting.getUserSettingPreferences().getStringSet(SNS_LIKED_POSTIDS_KEY,null);
        if (postIds != null){
            for (String postId : postIds) {
                likedPost.put(postId,true);
            }
        }
    }

    public void releaseManager() {
        UserSetting.getUserSettingPreferences().edit().putStringSet(SNS_LIKED_POSTIDS_KEY,likedPost.keySet());
        likedPost = null;
        userProfile = null;
    }

    public VessageUser getUserProfile() {
        return userProfile;
    }

    public interface GetMainBoardDataCallback{
        void onGetMainBoardDataCompleted(SNSMainBoardData data);
    }

    private Map<String,Boolean> likedPost;

    public void getMainBoardData(int postCnt, final GetMainBoardDataCallback callback) {
        GetSNSMainBoardDataRequest req = new GetSNSMainBoardDataRequest();
        req.setPostCount(postCnt);
        Set<String> userIds = ServicesProvider.getService(ConversationService.class).getChattingNormalUserIds();
        userIds.add(UserSetting.getUserId());
        req.setFocusIds(userIds.toArray(new String[0]));
        req.setLocation(ServicesProvider.getService(LocationService.class).getHereShortString());
        BahamutRFKit.getClient(APIClient.class).executeRequest(req, new OnRequestCompleted<JSONObject>() {
            @Override
            public void callback(Boolean isOk, int statusCode, JSONObject result) {
                if (isOk){
                    callback.onGetMainBoardDataCompleted(SNSMainBoardData.prase(result));
                }else {
                    callback.onGetMainBoardDataCompleted(null);
                }
            }
        });
    }

    public void getSNSNormalPosts(long startTimeSpan,int pageCount,GetPostCallback callback) {
        getSNSPosts(SNSPost.TYPE_NORMAL_POST, startTimeSpan, pageCount, callback);
    }

    public void getSNSMyPosts(long startTimeSpan,int pageCount,GetPostCallback callback) {
        getSNSPosts(SNSPost.TYPE_MY_POST, startTimeSpan, pageCount, callback);
    }

    public void getSNSPosts(int type, long startTimeSpan, int pageCount, final GetPostCallback callback) {

        GetSNSValuesRequestBase req = null;
        if (type == SNSPost.TYPE_MY_POST) {
            req = new GetMySNSPostRequest();
        }else{
            req = new GetSNSPostReqeust();
        }
        req.setPageCount(pageCount);
        req.setTimeSpan(startTimeSpan);
        BahamutRFKit.getClient(APIClient.class).executeRequestArray(req, new OnRequestCompleted<JSONArray>() {
            @Override
            public void callback(Boolean isOk, int statusCode, JSONArray result) {
                if (isOk){
                    callback.onGetPosts(SNSPost.praseArray(result));
                }else {
                    callback.onGetPosts(new SNSPost[0]);
                }
            }
        });
    }

    public boolean likedInCached(String postId) {
        return likedPost.containsKey(postId);
    }

    public void likePost(String postId, final RequestSuccessCallback callback) {

        SNSLikePostRequest req = new SNSLikePostRequest();
        req.setPostId(postId);
        req.setNick(getUserProfile().nickName);
        BahamutRFKit.getClient(APIClient.class).executeRequest(req, new OnRequestCompleted<JSONObject>() {
            @Override
            public void callback(Boolean isOk, int statusCode, JSONObject result) {
                if (isOk){
                    MobclickAgent.onEvent(AppMain.getInstance() ,"SNS_LikePost");
                }
                callback.onCompleted(isOk);
            }
        });
    }


    public void newPost(String imageId, final PostNewSNSPostCallback callback) {
        SNSPostNewRequest req = new SNSPostNewRequest();
        req.setImage(imageId);
        req.setNick(getUserProfile().nickName);
        BahamutRFKit.getClient(APIClient.class).executeRequest(req, new OnRequestCompleted<JSONObject>() {
            @Override
            public void callback(Boolean isOk, int statusCode, JSONObject result) {
                if (isOk){
                    MobclickAgent.onEvent(AppMain.getInstance() ,"SNS_NewPost");
                }
                callback.onPostNewSNSPost(new SNSPost());
            }
        });


    }

    public void newPostComment(String postId, String comment, String senderNick, String atUser, String atUserNick, final PostNewCommentCallback callback) {
        SNSNewCommentRequest req = new SNSNewCommentRequest();
        req.setPostId(postId);
        req.setComment(comment);
        req.setAtUserId(atUser);
        req.setAtUserNick(atUserNick);
        req.setSenderNick(senderNick);
        BahamutRFKit.getClient(APIClient.class).executeRequest(req, new OnRequestCompleted<JSONObject>() {
            @Override
            public void callback(Boolean isOk, int statusCode, JSONObject result) {
                if (isOk){
                    MobclickAgent.onEvent(AppMain.getInstance() ,"SNS_NewComment");
                }
                try {
                    String msg = result.getString("msg");
                    callback.onPostNewComment(isOk,msg);
                } catch (JSONException e) {
                    callback.onPostNewComment(isOk,null);
                }
            }
        });

    }

    public void getPostComment(String postId, long ts,int pageCount, final GetPostCommentCallback callback) {

        GetSNSPostCommentRequest req = new GetSNSPostCommentRequest();
        req.setPostId(postId);
        req.setTimeSpan(ts);
        req.setPageCount(pageCount);
        BahamutRFKit.getClient(APIClient.class).executeRequestArray(req, new OnRequestCompleted<JSONArray>() {
            @Override
            public void callback(Boolean isOk, int statusCode, JSONArray result) {
                callback.onGetPostComment(SNSPostComment.praseArray(result));
            }
        });
    }

    public void getMyComments(long ts, int cnt, final GetPostCommentCallback callback) {
        GetSNSMyCommentsRequest req = new GetSNSMyCommentsRequest();
        req.setPageCount(cnt);
        req.setTimeSpan(ts);
        BahamutRFKit.getClient(APIClient.class).executeRequestArray(req, new OnRequestCompleted<JSONArray>() {
            @Override
            public void callback(Boolean isOk, int statusCode, JSONArray result) {
                callback.onGetPostComment(SNSPostComment.praseArray(result));
            }
        });
    }

    public void getMyReceivedLikes(long ts, int cnt, final GetPostLikeCallback callback) {
        GetSNSMyReceivedLikesRequest req = new GetSNSMyReceivedLikesRequest();
        req.setTimeSpan(ts);
        req.setPageCount(cnt);
        BahamutRFKit.getClient(APIClient.class).executeRequestArray(req, new OnRequestCompleted<JSONArray>() {
            @Override
            public void callback(Boolean isOk, int statusCode, JSONArray result) {
                callback.onGetPostLike(SNSPostLike.praseArray(result));
            }
        });
    }


    ////

    public void deletePost(String postId, final RequestSuccessCallback callback) {
        DeleteSNSPostRequest req = new DeleteSNSPostRequest();
        req.setPostId(postId);
        BahamutRFKit.getClient(APIClient.class).executeRequest(req, new OnRequestCompleted<JSONObject>() {
            @Override
            public void callback(Boolean isOk, int statusCode, JSONObject result) {
                callback.onCompleted(isOk);
            }
        });
    }

    public void reportObjectionablePost(String postId,final RequestSuccessCallback callback) {
        ReportObjectionableSNSPostRequest req = new ReportObjectionableSNSPostRequest();
        req.setPostId(postId);
        BahamutRFKit.getClient(APIClient.class).executeRequest(req, new OnRequestCompleted<JSONObject>() {
            @Override
            public void callback(Boolean isOk, int statusCode, JSONObject result) {
                callback.onCompleted(isOk);
            }
        });
    }

    ///

    public void godLikePost(String postId,final RequestSuccessCallback callback) {
        SNSGodLikePostRequest req = new SNSGodLikePostRequest();
        req.setPostId(postId);
        BahamutRFKit.getClient(APIClient.class).executeRequest(req, new OnRequestCompleted<JSONObject>() {
            @Override
            public void callback(Boolean isOk, int statusCode, JSONObject result) {
                callback.onCompleted(isOk);
            }
        });
    }

    public void godDeletePost(String postId,final RequestSuccessCallback callback) {
        SNSGodDeletePostRequest req = new SNSGodDeletePostRequest();
        req.setPostId(postId);
        BahamutRFKit.getClient(APIClient.class).executeRequest(req, new OnRequestCompleted<JSONObject>() {
            @Override
            public void callback(Boolean isOk, int statusCode, JSONObject result) {
                callback.onCompleted(isOk);
            }
        });
    }

    public void godBlockMember(String memberId, final RequestSuccessCallback callback) {
        SNSGodBlockMemberRequest req = new SNSGodBlockMemberRequest();
        req.setMemberId(memberId);
        BahamutRFKit.getClient(APIClient.class).executeRequest(req, new OnRequestCompleted<JSONObject>() {
            @Override
            public void callback(Boolean isOk, int statusCode, JSONObject result) {
                callback.onCompleted(isOk);
            }
        });
    }

    public interface GetPostCallback{
        void onGetPosts(SNSPost[] posts);
    }

    public interface RequestSuccessCallback {
        void onCompleted(Boolean isOk);
    }

    public interface PostNewSNSPostCallback {
        void onPostNewSNSPost(SNSPost newPost);
    }

    public interface PostNewCommentCallback {
        void onPostNewComment(boolean posted,String msg);
    }

    public interface GetPostCommentCallback {
        void onGetPostComment(SNSPostComment[] comments);
    }

    public interface GetPostLikeCallback {
        void onGetPostLike(SNSPostLike[] comments);
    }
}
