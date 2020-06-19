package com.example.study1;

import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
//String:需要传入一个字符串参数；Integer:进度显示单位;Integer反馈执行结果
public class DownloadTask extends AsyncTask<String,Integer,Integer> {
    //四个下载状态
    public static final int TYPE_SUCCESS=0;
    public static final int TYPE_FAILED=1;
    public static final int TYPE_PAUSED=2;
    public static final int TYPE_CANCELED=3;

    private DownloadListener listener;
    private boolean isCanceled=false;
    private boolean isPause=false;
    private int lastProgress;
    //构造函数；通过参数状态回调
    public DownloadTask(DownloadListener listener){
        this.listener=listener;
    }
    @Override
    protected Integer doInBackground(String... params) {
        InputStream is = null;
        RandomAccessFile savedFile = null;
        File file = null;
        try {
            long downloadedLength = 0;//记录已下载文件的长度
            String downloadUrl = params[0];//下载URL地址
            //存放下载文件位置
            String fileName = downloadUrl.substring(downloadUrl.lastIndexOf("/"));
            String directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath();
            file = new File(directory + fileName);
            if (file.exists()) {
                //Log.d("dd","操");
                downloadedLength = file.length();
               // Log.d("dd","操="+downloadedLength);
            }
            //获取待下载文件总长度
            long contentLength = getContentLength(downloadUrl);
            if (contentLength == 0) {
                return TYPE_FAILED;
            } else if (contentLength == downloadedLength) {
                return TYPE_SUCCESS;
            }
            //使用OkHttp发送网络请求
            OkHttpClient client = new OkHttpClient();
            //加header，告诉服务器从哪个字节开始下载
            Request request = new Request.Builder().addHeader("RANGE", "bytes=" + downloadedLength + "-")
                    .url(downloadUrl)
                    .build();
            Response response = client.newCall(request).execute();
            if (response != null) {
                //读取下载数据，写入本地文件
                is = response.body().byteStream();
                savedFile = new RandomAccessFile(file, "rw");
                savedFile.seek(downloadedLength);
                byte[] b = new byte[1024];
                int total = 0;
                int len;
                //判断取消或者暂停
                while ((len = is.read(b)) != -1) {
                    if (isCanceled) {
                        return TYPE_CANCELED;
                    } else if (isPause) {
                        return TYPE_PAUSED;
                    } else {
                        total += len;
                        savedFile.write(b, 0, len);
                        int progress = (int) ((total + downloadedLength) * 100 / contentLength);
                        Log.d("SS","s="+progress);
                        //调用publishProgress通知实时进度
                        publishProgress(progress);
                    }
                }
                response.body().close();
                return TYPE_SUCCESS;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            try {
                if(is!=null){
                    is.close();
                }if(savedFile!=null){
                    savedFile.close();
                }
                if(isCanceled&&file!=null){
                    file.delete();
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        return TYPE_FAILED;
    }
    @Override
    protected void onProgressUpdate(Integer... values){
        int progress=values[0];
        if(progress>lastProgress){
            listener.onProgress(progress);
            lastProgress=progress;
        }
    }
    @Override
    protected void onPostExecute(Integer status){
        switch (status){
            case TYPE_SUCCESS:
                listener.onSuccess();
            case TYPE_FAILED:
                listener.onFailed();
            case TYPE_PAUSED:
                listener.onPaused();
            case TYPE_CANCELED:
                listener.onCanceled();
            default:
                break;
        }
    }
    public void pauseDownload(){
        isPause=true;
    }
    public void cancelDownload(){
        isCanceled=true;
    }
    //获取待下载文件总长度
    private long getContentLength(String downloadUrl)throws IOException{
        OkHttpClient client=new OkHttpClient();
        Request request=new Request.Builder()
                .url(downloadUrl)
                .build();
        Response response=client.newCall(request).execute();
        if(response!=null&&response.isSuccessful()){
            long contentLength=response.body().contentLength();
            response.close();
            return contentLength;
        }
        return 0;
    }
}
