package com.shuyu.gsyvideoplayer.player;

import android.content.Context;
import android.media.AudioManager;
import android.media.PlaybackParams;
import android.net.TrafficStats;
import android.net.Uri;
import android.os.Build;
import android.os.Message;
import android.view.Surface;

import com.fula.CLog;
import com.shuyu.gsyvideoplayer.cache.ICacheManager;
import com.shuyu.gsyvideoplayer.model.GSYModel;
import com.shuyu.gsyvideoplayer.model.VideoOptionModel;

import java.util.List;

import tv.danmaku.ijk.media.player.AndroidMediaPlayer;
import tv.danmaku.ijk.media.player.IMediaPlayer;

/**
 * 系统播放器，总觉得不好用
 * Created by guoshuyu on 2018/1/11.
 */

public class SystemPlayerManager implements IPlayerManager {

    private Context context;

    private AndroidMediaPlayer mediaPlayer;

    private Surface surface;

    private boolean release;

    private long lastTotalRxBytes = 0;

    private long lastTimeStamp = 0;

    @Override
    public IMediaPlayer getMediaPlayer() {
        return mediaPlayer;
    }

    @Override
    public void initVideoPlayer(Context context, Message msg, List<VideoOptionModel> optionModelList, ICacheManager cacheManager) {
        this.context = context.getApplicationContext();
        mediaPlayer = new AndroidMediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        release = false;
        GSYModel gsyModel = (GSYModel) msg.obj;
        try {
            if (gsyModel.isCache() && cacheManager != null) {
                cacheManager.doCacheLogic(context, mediaPlayer, gsyModel.getUrl(), gsyModel.getMapHeadData(), gsyModel.getCachePath());
            } else {
                mediaPlayer.setDataSource(context, Uri.parse(gsyModel.getUrl()), gsyModel.getMapHeadData());
            }
            mediaPlayer.setLooping(gsyModel.isLooping());
            if (gsyModel.getSpeed() != 1 && gsyModel.getSpeed() > 0) {
                setSpeed(gsyModel.getSpeed());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void showDisplay(Message msg) {
        if (msg.obj == null && mediaPlayer != null && !release) {
            mediaPlayer.setSurface(null);
        } else if (msg.obj != null) {
            Surface holder = (Surface) msg.obj;
            surface = holder;
            if (mediaPlayer != null && holder.isValid() && !release) {
                mediaPlayer.setSurface(holder);
            }
        }
    }

    @Override
    public void setSpeed(float speed, boolean soundTouch) {
        setSpeed(speed);
    }

    @Override
    public void setNeedMute(boolean needMute) {
        try {
            if (mediaPlayer != null && !release) {
                if (needMute) {
                    mediaPlayer.setVolume(0, 0);
                } else {
                    mediaPlayer.setVolume(1, 1);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void releaseSurface() {
        if (surface != null) {
            //surface.release();
            surface = null;
        }
    }

    @Override
    public void release() {
        if (mediaPlayer != null) {
            release = true;
            try {
                mediaPlayer.release();
                return;
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
        }
        lastTotalRxBytes = 0;
        lastTimeStamp = 0;
    }

    @Override
    public int getBufferedPercentage() {
        return -1;
    }

    @Override
    public long getNetSpeed() {
        if (mediaPlayer != null) {
            return getNetSpeed(context);
        }
        return 0;
    }

    @Override
    public void setSpeedPlaying(float speed, boolean soundTouch) {

    }


    @Override
    public void start() {
        if (mediaPlayer != null) {
            try {
                mediaPlayer.start();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void stop() {
        if (mediaPlayer != null) {
            try {
                mediaPlayer.stop();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void pause() {
        if (mediaPlayer != null) {
            try {
                mediaPlayer.pause();
                return;
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public int getVideoWidth() {
        if (mediaPlayer != null) {
            try {
                return mediaPlayer.getVideoWidth();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
        }
        return 0;
    }

    @Override
    public int getVideoHeight() {
        if (mediaPlayer != null) {
            try {
                return mediaPlayer.getVideoHeight();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
        }
        return 0;
    }

    @Override
    public boolean isPlaying() {
        if (mediaPlayer != null) {
            try {
                return mediaPlayer.isPlaying();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    @Override
    public void seekTo(long time) {
        if (mediaPlayer != null) {
            try {
                mediaPlayer.seekTo(time);
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public long getCurrentPosition() {
        if (mediaPlayer != null) {
            try {
                return mediaPlayer.getCurrentPosition();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
        }
        return 0;
    }

    @Override
    public long getDuration() {
        if (mediaPlayer != null) {
            try {
                return mediaPlayer.getDuration();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
        }
        return 0;
    }

    @Override
    public int getVideoSarNum() {
        if (mediaPlayer != null) {
            try {
                return mediaPlayer.getVideoSarNum();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
        }
        return 1;
    }

    @Override
    public int getVideoSarDen() {
        if (mediaPlayer != null) {
            try {
                return mediaPlayer.getVideoSarDen();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
        }
        return 1;
    }

    @Override
    public boolean isSurfaceSupportLockCanvas() {
        return false;
    }

    private void setSpeed(float speed) {
        if (release) {
            return;
        }
        if (mediaPlayer != null && mediaPlayer.getInternalMediaPlayer() != null && mediaPlayer.isPlayable()) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    PlaybackParams playbackParams = new PlaybackParams();
                    playbackParams.setSpeed(speed);
                    mediaPlayer.getInternalMediaPlayer().setPlaybackParams(playbackParams);
                } else {
                    CLog.i(" not support setSpeed");
                }
            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    private long getNetSpeed(Context context) {
        if (context == null) {
            return 0;
        }
        long nowTotalRxBytes = TrafficStats.getUidRxBytes(context.getApplicationInfo().uid) == TrafficStats.UNSUPPORTED ? 0 : (TrafficStats.getTotalRxBytes() / 1024);//转为KB
        long nowTimeStamp = System.currentTimeMillis();
        long calculationTime = (nowTimeStamp - lastTimeStamp);
        if (calculationTime == 0) {
            return calculationTime;
        }
        //毫秒转换
        long speed = ((nowTotalRxBytes - lastTotalRxBytes) * 1000 / calculationTime);
        lastTimeStamp = nowTimeStamp;
        lastTotalRxBytes = nowTotalRxBytes;
        return speed;
    }
}
