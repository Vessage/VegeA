package cn.bahamut.vessage.conversation;

import android.content.Context;
import android.media.MediaPlayer;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.VideoView;

import cn.bahamut.vessage.R;

/**
 * Created by alexchow on 16/4/13.
 */
public class VideoPlayer {

    private String videoPath;

    public VideoPlayerState getVideoPlayerState() {
        return videoPlayerState;
    }

    public interface VideoPlayerDelegate{
        void onClickPlayButton(VideoPlayer player,VideoPlayerState state);
    }

    public enum VideoPlayerState {
        NO_FILE,READY_TO_LOAD,LOADING,LOADED,LOAD_ERROR,PLAYING,PAUSE
    }

    private VideoView mVideoView;
    private ImageButton mVideoCenterButton;
    private ProgressBar mVideoProgressBar;
    private VideoPlayerDelegate delegate;

    private VideoPlayerState videoPlayerState;

    public void setDelegate(VideoPlayerDelegate delegate) {
        this.delegate = delegate;
    }

    private View.OnClickListener onClickVideoButton = new View.OnClickListener(){
        @Override
        public void onClick(View v) {
            if(delegate != null){
                delegate.onClickPlayButton(VideoPlayer.this, videoPlayerState);
            }
        }
    };

    public VideoPlayer(Context context,VideoView videoView, ImageButton videoCenterButton, ProgressBar videoProgressBar) {
        this.mVideoView = videoView;
        this.mVideoView.setOnCompletionListener(onCompletionListener);
        this.mVideoCenterButton = videoCenterButton;
        this.mVideoProgressBar = videoProgressBar;
        mVideoCenterButton.setOnClickListener(onClickVideoButton);
    }

    private MediaPlayer.OnCompletionListener onCompletionListener = new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mp) {
            mVideoView.stopPlayback();
            setVideoPath(videoPath,false);
        }
    };

    public VideoView getCorePlayer(){
        return mVideoView;
    }

    public void setVideoPath(String videoPath,boolean autoPlay){
        this.videoPath = videoPath;
        mVideoView.setVideoPath(videoPath);
        if(autoPlay){
            playVideo();
        }else {
            setLoadedVideo();
        }
    }

    public void playVideo(){
        mVideoView.setBackgroundColor(0);
        videoPlayerState = VideoPlayerState.PLAYING;
        mVideoCenterButton.setVisibility(View.INVISIBLE);
        mVideoView.start();
    }

    public void pauseVideo(){
        if(mVideoView.canPause()){
            videoPlayerState = VideoPlayerState.PAUSE;
            mVideoCenterButton.setVisibility(View.VISIBLE);
            mVideoView.pause();
        }
    }

    public void resumeVideo(){
        videoPlayerState = VideoPlayerState.PLAYING;
        mVideoCenterButton.setVisibility(View.INVISIBLE);
        mVideoView.resume();
    }

    public void setNoFile(){
        videoPlayerState = VideoPlayerState.NO_FILE;
        mVideoCenterButton.setVisibility(View.VISIBLE);
        mVideoProgressBar.setVisibility(View.INVISIBLE);
        mVideoCenterButton.setImageResource(R.mipmap.no_file);
    }

    public void setReadyToLoadVideo(){
        videoPlayerState = VideoPlayerState.READY_TO_LOAD;
        mVideoCenterButton.setVisibility(View.VISIBLE);
        mVideoProgressBar.setVisibility(View.INVISIBLE);
        mVideoCenterButton.setImageResource(R.mipmap.play_gray);
    }

    public void setLoadingVideo(){
        videoPlayerState = VideoPlayerState.LOADING;
        mVideoProgressBar.setVisibility(View.VISIBLE);
        mVideoCenterButton.setVisibility(View.INVISIBLE);
    }

    public void setLoadedVideo(){
        videoPlayerState = VideoPlayerState.LOADED;
        mVideoProgressBar.setVisibility(View.INVISIBLE);
        mVideoCenterButton.setVisibility(View.VISIBLE);
        mVideoCenterButton.setImageResource(R.mipmap.play_gray);
    }

    public void setLoadVideoError(){
        videoPlayerState = VideoPlayerState.LOAD_ERROR;
        mVideoProgressBar.setVisibility(View.INVISIBLE);
        mVideoCenterButton.setVisibility(View.VISIBLE);
        mVideoCenterButton.setImageResource(R.mipmap.refresh);
    }
}
