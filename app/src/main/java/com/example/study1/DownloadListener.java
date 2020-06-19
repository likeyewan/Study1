package com.example.study1;

public interface DownloadListener {
    void onProgress(int progress);//下载进度
    void onSuccess();//成功
    void onFailed();//失败
    void onPaused();//暂停
    void onCanceled();//取消
}
