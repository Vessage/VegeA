package cn.bahamut.vessage.conversation.chat.views;

import android.content.Context;
import android.graphics.Color;
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
        void onStateChanged(VideoPlayer player,VideoPlayerState old,VideoPlayerState newState);
        void onVideoCompleted(VideoPlayer player);
    }

    public enum VideoPlayerState {
        NO_FILE,READY_TO_LOAD,LOADING,LOADED,LOAD_ERROR,PLAYING,PAUSE,COMPLETED
    }

    private VideoView mVideoView;
    private ImageButton mVideoCenterButton;
    private ProgressBar mVideoProgressBar;
    private VideoPlayerDelegate delegate;

    private VideoPlayerState videoPlayerState;

    protected void setVideoPlayerState(VideoPlayerState videoPlayerState) {
        VideoPlayerState old = this.videoPlayerState;
        this.videoPlayerState = videoPlayerState;
        if (delegate != null){
            delegate.onStateChanged(this,old,videoPlayerState);
        }
    }

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
            mVideoProgressBar.setVisibility(View.INVISIBLE);
            mVideoCenterButton.setVisibility(View.VISIBLE);
            mVideoCenterButton.setImageResource(R.drawable.play_gray);
            setVideoPlayerState(VideoPlayerState.COMPLETED);
            if (delegate != null){
                delegate.onVideoCompleted(VideoPlayer.this);
            }
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
        setVideoPlayerState(VideoPlayerState.PLAYING);
        mVideoCenterButton.setVisibility(View.INVISIBLE);
        mVideoView.start();
    }

    public void pauseVideo(){
        if(mVideoView.canPause()){
            setVideoPlayerState(VideoPlayerState.PAUSE);
            mVideoCenterButton.setVisibility(View.VISIBLE);
            mVideoView.pause();
        }
    }

    public void resumeVideo(){
        setVideoPlayerState(VideoPlayerState.PLAYING);
        mVideoCenterButton.setVisibility(View.INVISIBLE);
        mVideoView.resume();
    }

    public void setNoFile(){
        mVideoView.suspend();
        mVideoView.setBackgroundColor(Color.BLACK);
        setVideoPlayerState(VideoPlayerState.NO_FILE);
        mVideoCenterButton.setVisibility(View.VISIBLE);
        mVideoProgressBar.setVisibility(View.INVISIBLE);
        mVideoCenterButton.setImageResource(R.drawable.no_file);
    }

    public void setReadyToLoadVideo(){
        mVideoView.setBackgroundColor(Color.BLACK);
        setVideoPlayerState(VideoPlayerState.READY_TO_LOAD);
        mVideoCenterButton.setVisibility(View.VISIBLE);
        mVideoProgressBar.setVisibility(View.INVISIBLE);
        mVideoCenterButton.setImageResource(R.drawable.play_gray);
    }

    public void setLoadingVideo(){
        setVideoPlayerState(VideoPlayerState.LOADING);
        mVideoProgressBar.setVisibility(View.VISIBLE);
        mVideoCenterButton.setVisibility(View.INVISIBLE);
    }

    public void setLoadedVideo(){
        setVideoPlayerState(VideoPlayerState.LOADED);
        mVideoProgressBar.setVisibility(View.INVISIBLE);
        mVideoCenterButton.setVisibility(View.VISIBLE);
        mVideoCenterButton.setImageResource(R.drawable.play_gray);
    }

    public void setLoadVideoError(){
        setVideoPlayerState(VideoPlayerState.LOAD_ERROR);
        mVideoProgressBar.setVisibility(View.INVISIBLE);
        mVideoCenterButton.setVisibility(View.VISIBLE);
        mVideoCenterButton.setImageResource(R.drawable.refresh);
    }
}
