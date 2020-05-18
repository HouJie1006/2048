package kylec.hj.g2048.service;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;

import kylec.hj.g2048.R;

/**
 * 音乐播放服务
 */
public class MusicService extends Service implements MediaPlayer.OnCompletionListener{

    private MediaPlayer mediaPlayer;
    private final IBinder binder = new MusicBinder();

    public MusicService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        //初始化时就创建一个MediaPlayer进行资源链接
        mediaPlayer = MediaPlayer.create(this, R.raw.login_back);
        mediaPlayer.setOnCompletionListener(this);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        if (!mediaPlayer.isPlaying()){
            //后台播放
            mediaPlayer.start();
        }
        return START_STICKY;

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //先停止 再释放
        if (mediaPlayer.isPlaying()){
            mediaPlayer.stop();
        }
        mediaPlayer.release();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        // 结束Service
        stopSelf();
    }

    public class MusicBinder extends Binder{
        public MusicService getMusicService(){
            return MusicService.this;
        }
    }
}
