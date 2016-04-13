package cn.bahamut.restfulkit.client;

import org.json.JSONException;
import org.json.JSONObject;

import cn.bahamut.common.JsonHelper;
import cn.bahamut.restfulkit.client.base.BahamutClientBase;
import cn.bahamut.restfulkit.client.base.OnRequestCompleted;
import cn.bahamut.restfulkit.models.BahamutClientInfo;
import cn.bahamut.restfulkit.request.BahamutRequestBase;
import cn.bahamut.restfulkit.request.file.GetBahamutFireRequest;
import cn.bahamut.restfulkit.request.file.NewAliOSSFileAccessInfoListRequest;
import cn.bahamut.vessage.services.file.FileAccessInfo;

/**
 * Created by alexchow on 16/4/5.
 */
public class FireClient extends BahamutClientBase<FireClient.FireClientInfo> {

    public static interface OnGetAccessInfo{
        void onGetAccessInfo(boolean suc, FileAccessInfo info);
    }

    static public class FireClientInfo extends BahamutClientInfo{
        public String appKey;
        public String userId;
        public String appToken;
        public String fileAPIServer;
    }

    @Override
    protected void prepareRequest(BahamutRequestBase request, FireClientInfo clientInfo) {
        request.putHeader("appkey",clientInfo.appKey);
        request.setApiServerUrl(clientInfo.fileAPIServer);
        request.putHeader("userId", clientInfo.userId);
        request.putHeader("token", clientInfo.appToken);
    }

    public void getAliOSSUploadFileAccessInfo(String filePath,String fileType,OnGetAccessInfo callback){
        NewAliOSSFileAccessInfoListRequest request = new NewAliOSSFileAccessInfoListRequest();
        getUploadFileAccessInfo(request, callback);
    }

    private static FileAccessInfo generateFileAccessInfo(JSONObject jsonObject){
        FileAccessInfo fileAccessInfo = null;
        try {
            fileAccessInfo = JsonHelper.parseObject(jsonObject, FileAccessInfo.class);
        } catch (JSONException e) {
        }
        return  fileAccessInfo;
    }

    private void getUploadFileAccessInfo(BahamutRequestBase request, final OnGetAccessInfo callback){
        executeRequest(request, new OnRequestCompleted<JSONObject>() {
            @Override
            public void callback(Boolean isOk, int statusCode, JSONObject result) {
                if (isOk) {
                    FileAccessInfo fileAccessInfo = generateFileAccessInfo(result);
                    if (fileAccessInfo != null) {
                        callback.onGetAccessInfo(true, fileAccessInfo);
                        return;
                    }
                }
                callback.onGetAccessInfo(false, null);

            }
        });
    }

    public void getDownLoadFileAccessInfo(String fileId, final OnGetAccessInfo callback){
        GetBahamutFireRequest request = new GetBahamutFireRequest();
        request.setFileId(fileId);
        executeRequest(request, new OnRequestCompleted<JSONObject>() {
            @Override
            public void callback(Boolean isOk, int statusCode, JSONObject result) {
                if (isOk) {
                    FileAccessInfo fileAccessInfo = generateFileAccessInfo(result);
                    if (fileAccessInfo != null) {
                        callback.onGetAccessInfo(true, fileAccessInfo);
                        return;
                    }
                }
                callback.onGetAccessInfo(false, null);
            }
        });
    }

}
