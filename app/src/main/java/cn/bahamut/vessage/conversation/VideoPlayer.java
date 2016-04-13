package cn.bahamut.vessage.conversation;

import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.VideoView;

import cn.bahamut.vessage.R;

/**
 * Created by alexchow on 16/4/13.
 */
public class VideoPlayer {

    public VideoPlayerState getVideoPlayerState() {
        return videoPlayerState;
    }

    static public interface VideoPlayerDelegate{
        void onClickPlayButton(VideoPlayer player,VideoPlayerState state);
        void onClickPlayer(VideoPlayer player,VideoPlayerState state);
    }

    static public enum VideoPlayerState {
        NO_FILE,READY_TO_LOAD,LOADING,LOADED,LOAD_ERROR,PLAYING,PAUSE
    }

    private VideoView mVideoView;
    private ImageButton mVideoCenterButton;
    private ProgressBar mVideoProgressBar;
    private VideoPlayerDelegate delegate;

    private VideoPlayerState videoPlayerState;

    private View.OnClickListener onClickVideoButton = new View.OnClickListener(){
        @Override
        public void onClick(View v) {
            if(delegate != null){
                delegate.onClickPlayButton(VideoPlayer.this, videoPlayerState);
            }
        }
    };

    private View.OnClickListener onClickVideoView = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(delegate != null){
                delegate.onClickPlayer(VideoPlayer.this, videoPlayerState);
            }
        }
    };

    public VideoPlayer(VideoView videoView, ImageButton videoCenterButton, ProgressBar videoProgressBar) {
        this.mVideoView = videoView;
        this.mVideoCenterButton = videoCenterButton;
        this.mVideoProgressBar = videoProgressBar;
        mVideoView.setOnClickListener(onClickVideoView);
        mVideoCenterButton.setOnClickListener(onClickVideoButton);
    }

    public VideoView getCorePlayer(){
        return mVideoView;
    }

    public void setVideoPath(String videoPath,boolean autoPlay){
        mVideoView.setVideoPath(videoPath);
        if(autoPlay){
            playVideo();
        }
    }

    public void playVideo(){
        videoPlayerState = VideoPlayerState.PLAYING;
        mVideoView.start();
    }

    public void pauseVideo(){
        if(mVideoView.canPause()){
            videoPlayerState = VideoPlayerState.PAUSE;
            mVideoView.pause();
        }
    }

    public void resumeVideo(){
        videoPlayerState = VideoPlayerState.PLAYING;
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
