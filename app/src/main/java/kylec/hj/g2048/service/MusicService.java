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
        mediaPlayer = MediaPlayer.create(this, R.raw.login_back);
        mediaPlayer.setOnCompletionListener(this);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        if (!mediaPlayer.isPlaying()){
            mediaPlayer.start();
        }
        return START_STICKY;

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
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
        stopSelf();
    }

    public class MusicBinder extends Binder{
        public MusicService getMusicService(){
            return MusicService.this;
        }
    }
}
