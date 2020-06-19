package com.example.study1;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import java.io.File;

import static android.os.Build.ID;

public class DownloadService extends Service {
    private DownloadTask downloadTask;
    private String downloadUrl;
    private DownloadListener listener=new DownloadListener() {
        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public void onProgress(int progress) {
            //构建一个显示下载进度通知，notify()方法去触犯这个通知
            getNotificationManager().notify(1,getNotification("Download...",progress));
        }
        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public void onSuccess() {
            downloadTask=null;
            //下载成功时通知前台服务关闭，并创建一个下载成功的通知
            stopForeground(true);
            //创建一个通知
            getNotificationManager().notify(1,getNotification("Download Success",-1));
            Toast.makeText(DownloadService.this,"Download Success",Toast.LENGTH_SHORT).show();
        }
        @Override
        public void onFailed() {
            downloadTask=null;
            //下载失败时通知前台服务关闭，并创建一个下载失败的通知
            stopForeground(true);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                getNotificationManager().notify(1,getNotification("Download Failed",-1));
            }
            Toast.makeText(DownloadService.this,"Download Failed",Toast.LENGTH_SHORT).show();
        }
        @Override
        public void onPaused() {
            downloadTask=null;
            Toast.makeText(DownloadService.this,"Paused",Toast.LENGTH_SHORT).show();
        }
        @Override
        public void onCanceled() {
            downloadTask=null;
            stopForeground(true);
            Toast.makeText(DownloadService.this,"Canceled",Toast.LENGTH_SHORT).show();
        }
    };
    private DownloadBinder mBinder=new DownloadBinder();
    @Override
    public IBinder onBind(Intent intent){
        return mBinder;
    }
    class DownloadBinder extends Binder{
        public void starDownload(String url){
            if(downloadTask==null){
                downloadUrl=url;
                downloadTask=new DownloadTask(listener);
                downloadTask.execute(downloadUrl);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startForeground(1,getNotification("Downloading...",0));
                }
                Toast.makeText(DownloadService.this,"Downloading...",Toast.LENGTH_SHORT).show();
            }
        }
        public void pauseDownload(){
            if(downloadTask!=null){
                downloadTask.pauseDownload();
            }
        }
        public void cancelDownload(){
            if(downloadTask!=null){
                downloadTask.cancelDownload();
            }else {
                if(downloadUrl!=null){
                    String fileName=downloadUrl.substring(downloadUrl.lastIndexOf("/"));
                    String directory= Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath();
                    File file=new File(directory+fileName);
                    if(file.exists()){
                        file.delete();
                    }
                    getNotificationManager().cancel(1);
                    stopForeground(true);
                    Toast.makeText(DownloadService.this,"Canceled",Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private NotificationManager getNotificationManager(){
        return (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
    }
    @RequiresApi(api = Build.VERSION_CODES.O)
    private Notification getNotification(String title, int progress){
        String id = "com.example.study1";
        String name="sd";
        Intent intent=new Intent(this,MainActivity.class);
        PendingIntent pi=PendingIntent.getActivity(this,0,intent,0);
        NotificationChannel channel = new NotificationChannel(id, name, NotificationManager.IMPORTANCE_HIGH);
        getNotificationManager().createNotificationChannel(channel);
        Notification.Builder notification=new Notification.Builder(getApplicationContext(), id)
                .setContentTitle(title)
                .setContentIntent(pi)
                .setSmallIcon(android.R.drawable.stat_notify_more)
                .setAutoCancel(true);
        Log.d("s1","aa="+progress);
        if(progress>0){

            notification.setContentText(progress+"%");
            notification.setProgress(100,progress,false);
        }
        return notification.build();
    }
}
