package cn.bahamut.vessage.services.file;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.alibaba.sdk.android.oss.ClientConfiguration;
import com.alibaba.sdk.android.oss.ClientException;
import com.alibaba.sdk.android.oss.OSS;
import com.alibaba.sdk.android.oss.OSSClient;
import com.alibaba.sdk.android.oss.ServiceException;
import com.alibaba.sdk.android.oss.callback.OSSCompletedCallback;
import com.alibaba.sdk.android.oss.callback.OSSProgressCallback;
import com.alibaba.sdk.android.oss.common.auth.OSSCredentialProvider;
import com.alibaba.sdk.android.oss.common.auth.OSSPlainTextAKSKCredentialProvider;
import com.alibaba.sdk.android.oss.internal.OSSAsyncTask;
import com.alibaba.sdk.android.oss.model.GetObjectRequest;
import com.alibaba.sdk.android.oss.model.GetObjectResult;
import com.alibaba.sdk.android.oss.model.ObjectMetadata;
import com.alibaba.sdk.android.oss.model.PutObjectRequest;
import com.alibaba.sdk.android.oss.model.PutObjectResult;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import cn.bahamut.common.FileHelper;
import cn.bahamut.observer.Observable;

/**
 * Created by alexchow on 16/4/11.
 */
public class AliOSSManager extends Observable{
    private String TAG = "AliOSSManager";
    static private AliOSSManager instance;
    private Context applicationContext;
    private OSSCredentialProvider credentialProvider;
    private ClientConfiguration conf;

    enum GetObjectRequestTaskStatus{
        NotRun,Success,Fail
    }

    static private class RequestState{
        private FileAccessInfo fileAccessInfo;
        private Object tag;
        private FileService.OnFileTaskListener onFileTaskListener;
        private GetObjectRequestTaskStatus taskStatus = GetObjectRequestTaskStatus.NotRun;

        public FileAccessInfo getFileAccessInfo() {
            return fileAccessInfo;
        }

        public void setFileAccessInfo(FileAccessInfo fileAccessInfo) {
            this.fileAccessInfo = fileAccessInfo;
        }

        public Object getTag() {
            return tag;
        }

        public void setTag(Object tag) {
            this.tag = tag;
        }

        public FileService.OnFileTaskListener getOnFileTaskListener() {
            return onFileTaskListener;
        }

        public void setOnFileTaskListener(FileService.OnFileTaskListener onFileTaskListener) {
            this.onFileTaskListener = onFileTaskListener;
        }

        public GetObjectRequestTaskStatus getTaskStatus() {
            return taskStatus;
        }

        public void setTaskStatus(GetObjectRequestTaskStatus taskStatus) {
            this.taskStatus = taskStatus;
        }
    }

    static private class GetObjectRequestEx extends GetObjectRequest{
        private RequestState state = new RequestState();

        public GetObjectRequestEx(String bucketName, String objectKey) {
            super(bucketName, objectKey);
        }

        public RequestState getState() {
            return state;
        }
    }

    static private class PutObjectRequestEx extends PutObjectRequest{
        private RequestState state = new RequestState();

        public PutObjectRequestEx(String bucketName, String objectKey, String uploadFilePath) {
            super(bucketName, objectKey, uploadFilePath);
        }

        public PutObjectRequestEx(String bucketName, String objectKey, String uploadFilePath, ObjectMetadata metadata) {
            super(bucketName, objectKey, uploadFilePath, metadata);
        }

        public PutObjectRequestEx(String bucketName, String objectKey, byte[] uploadData) {
            super(bucketName, objectKey, uploadData);
        }

        public PutObjectRequestEx(String bucketName, String objectKey, byte[] uploadData, ObjectMetadata metadata) {
            super(bucketName, objectKey, uploadData, metadata);
        }

        public RequestState getState() {
            return state;
        }
    }

    public static AliOSSManager getInstance() {
        if(instance == null){
            instance = new AliOSSManager();
        }
        return instance;
    }

    void initManager(Context applicationContext, String accessKey, String accessKeySecret) {
        this.applicationContext = applicationContext;
        credentialProvider = new OSSPlainTextAKSKCredentialProvider(accessKey, accessKeySecret);

        conf = new ClientConfiguration();
        conf.setConnectionTimeout(15 * 1000); // 连接超时，默认15秒
        conf.setSocketTimeout(15 * 1000); // socket超时，默认15秒
        conf.setMaxConcurrentRequest(5); // 最大并发请求，默认5个
        conf.setMaxErrorRetry(2); // 失败后最大重试次数，默认2次
    }

    public void downLoadFile(FileAccessInfo info, final Object tag, FileService.OnFileTaskListener listener){
        GetObjectRequestEx get = new AliOSSManager.GetObjectRequestEx(info.getBucket(), info.getFileId());
        get.getState().setFileAccessInfo(info);
        get.getState().setTag(tag);
        get.getState().setOnFileTaskListener(listener);

        OSS oss = new OSSClient(applicationContext, info.getServer(), credentialProvider, conf);
        oss.asyncGetObject(get, new OSSCompletedCallback<GetObjectRequest, GetObjectResult>() {
            @Override
            public void onSuccess(GetObjectRequest request, GetObjectResult result) {
                // 请求成功
                AsyncTask asyncTask = new AsyncTask() {
                    @Override
                    protected Object doInBackground(Object[] params) {
                        try {
                            File tmpFile = applicationContext.getCacheDir().createTempFile(FileHelper.generateTempFileName(),"tmp");
                            GetObjectRequestEx getObjectRequest = (GetObjectRequestEx)params[0];
                            GetObjectResult getObjectResult = (GetObjectResult)params[1];
                            InputStream inputStream = getObjectResult.getObjectContent();
                            long contentLength = getObjectResult.getContentLength();
                            byte[] buffer = new byte[2048];
                            int len;

                            FileOutputStream fos = new FileOutputStream(tmpFile);
                            int readLength = 0;
                            while ((len = inputStream.read(buffer)) != -1) {
                                // 处理下载的数据
                                readLength += len;
                                fos.write(buffer, 0, len);
                                publishProgress(getObjectRequest,1.0 * readLength / contentLength);
                            }

                            fos.close();
                            if(readLength == contentLength){
                                String savePath = getObjectRequest.getState().getFileAccessInfo().getLocalPath();
                                tmpFile.renameTo(new File(savePath));
                                getObjectRequest.getState().setTaskStatus(GetObjectRequestTaskStatus.Success);
                            }else {
                                tmpFile.delete();
                                getObjectRequest.getState().setTaskStatus(GetObjectRequestTaskStatus.Fail);
                            }
                            return getObjectRequest;

                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        return null;
                    }

                    @Override
                    protected void onProgressUpdate(Object[] values) {
                        super.onProgressUpdate(values);
                        GetObjectRequestEx getObjectRequest = (GetObjectRequestEx)values[0];
                        double progress = (double) values[1];
                        getObjectRequest.getState().getOnFileTaskListener().onFileProgress(getObjectRequest.getState().getFileAccessInfo(),progress,getObjectRequest.getState().getTag());
                    }

                    @Override
                    protected void onPostExecute(Object o) {
                        super.onPostExecute(o);
                        GetObjectRequestEx request = (GetObjectRequestEx) o;

                        if(request.getState().getTaskStatus() == GetObjectRequestTaskStatus.Success){
                            request.getState().getOnFileTaskListener().onFileSuccess(request.getState().getFileAccessInfo(),request.getState().getTag());
                        }else {
                            request.getState().getOnFileTaskListener().onFileFailure(request.getState().getFileAccessInfo(),request.getState().getTag());
                        }
                    }
                };
                asyncTask.execute(request,result);
            }

            @Override
            public void onFailure(GetObjectRequest request, ClientException clientExcepion, ServiceException serviceException) {
                GetObjectRequestEx getObjectRequest = (GetObjectRequestEx)request;

                getObjectRequest.getState().setTaskStatus(GetObjectRequestTaskStatus.Fail);
                // 请求异常
                if (clientExcepion != null) {
                    // 本地异常如网络异常等
                    clientExcepion.printStackTrace();
                }
                if (serviceException != null) {
                    // 服务异常
                    Log.e(TAG, "ErrorCode:" + serviceException.getErrorCode());
                    Log.e(TAG, "RequestId:" + serviceException.getRequestId());
                    Log.e(TAG, "HostId" + serviceException.getHostId());
                    Log.e(TAG, "RawMessage" + serviceException.getRawMessage());
                }

                AsyncTask asyncTask = new AsyncTask() {
                    @Override
                    protected Object doInBackground(Object[] params) {
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Object o) {
                        super.onPostExecute(o);
                        try {
                            GetObjectRequestEx request = (GetObjectRequestEx) o;
                            request.getState().getOnFileTaskListener().onFileFailure(request.getState().getFileAccessInfo(), request.getState().getTag());
                        } catch (Exception ex) {
                            Log.e(TAG, ex.getMessage());
                        }
                    }
                };
                asyncTask.execute(getObjectRequest);
            }
        });
    }

    public void sendFileToAliOSS(FileAccessInfo info, Object tag, FileService.OnFileTaskListener listener) {
        OSS oss = new OSSClient(applicationContext, info.getServer(), credentialProvider, conf);
        // 构造上传请求
        PutObjectRequestEx put = new PutObjectRequestEx(info.getBucket(), info.getFileId(), info.getLocalPath());
        put.setBucketName(info.getBucket());
        put.setObjectKey(info.getFileId());
        put.getState().setTag(tag);
        put.getState().setOnFileTaskListener(listener);
        put.getState().setFileAccessInfo(info);
        // 异步上传时可以设置进度回调
        put.setProgressCallback(new OSSProgressCallback<PutObjectRequest>() {
            @Override
            public void onProgress(PutObjectRequest request, long currentSize, long totalSize) {
                PutObjectRequestEx requestEx = (PutObjectRequestEx)request;
                double progress = 1.0 * currentSize / totalSize;
                requestEx.getState().getOnFileTaskListener().onFileProgress(requestEx.getState().getFileAccessInfo(),progress, requestEx.getState().getTag());
            }
        });

        OSSAsyncTask task = oss.asyncPutObject(put, new OSSCompletedCallback<PutObjectRequest, PutObjectResult>() {
            @Override
            public void onSuccess(final PutObjectRequest request, PutObjectResult result) {
                PutObjectRequestEx requestEx = (PutObjectRequestEx)request;
                AsyncTask asyncTask = new AsyncTask() {
                    @Override
                    protected Object doInBackground(Object[] params) {
                        return params[0];
                    }

                    @Override
                    protected void onPostExecute(Object o) {
                        super.onPostExecute(o);
                        PutObjectRequestEx requestEx = (PutObjectRequestEx)o;
                        requestEx.getState().getOnFileTaskListener().onFileSuccess(requestEx.getState().getFileAccessInfo(),requestEx.getState().getTag());
                    }
                };
                asyncTask.execute(requestEx);
            }

            @Override
            public void onFailure(PutObjectRequest request, ClientException clientExcepion, ServiceException serviceException) {
                // 请求异常
                if (clientExcepion != null) {
                    // 本地异常如网络异常等
                    clientExcepion.printStackTrace();
                }
                if (serviceException != null) {
                    // 服务异常
                    Log.e("ErrorCode", serviceException.getErrorCode());
                    Log.e("RequestId", serviceException.getRequestId());
                    Log.e("HostId", serviceException.getHostId());
                    Log.e("RawMessage", serviceException.getRawMessage());
                }
                PutObjectRequestEx requestEx = (PutObjectRequestEx)request;
                requestEx.getState().setTaskStatus(GetObjectRequestTaskStatus.Fail);
                AsyncTask asyncTask = new AsyncTask() {
                    @Override
                    protected Object doInBackground(Object[] params) {
                        return params[0];
                    }

                    @Override
                    protected void onPostExecute(Object o) {
                        super.onPostExecute(o);
                        PutObjectRequestEx requestEx = (PutObjectRequestEx)o;
                        requestEx.getState().getOnFileTaskListener().onFileFailure(requestEx.getState().getFileAccessInfo(),requestEx.getState().getTag());
                    }
                };
                asyncTask.execute(requestEx);


            }
        });

        // task.cancel(); // 可以取消任务
        // task.waitUntilFinished(); // 可以等待任务完成
    }

}
