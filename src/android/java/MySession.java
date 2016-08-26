package com.evontech.VideoPlugin;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.opentok.android.BaseVideoRenderer;
import com.opentok.android.Connection;
import com.opentok.android.OpentokError;
import com.opentok.android.Publisher;
import com.opentok.android.Session;
import com.opentok.android.Stream;
import com.opentok.android.Subscriber;

import java.util.HashMap;


/**
 * Created by amit rai on 3/7/2016
 */

public class MySession extends Session {

    private final SessionListeners mSessionListener;
    private Context mContext;

    // Interface
    private ViewGroup mSubscribersViewContainer;
    private ViewGroup mPreview;

    // Players Status
    private MySubscriber mSubscriber;
    private HashMap<Stream, MySubscriber> mSubscriberStream = new HashMap<Stream, MySubscriber>();
    private HashMap<String, MySubscriber> mSubscriberConnection = new HashMap<String, MySubscriber>();

    private Publisher mPublisher;
    private View mPublisherView;
    private View mSubscriberView;


    public static boolean CALL_CONNECTED = false;
    public static boolean CALL_STARTED= false;
    private boolean mCaller = false;

    private int CALL_QUALITY = Constants.LOW;

    private final String TAG = getClass().getSimpleName();


    public MySession(Context context, SessionListeners listeners, String apiKey, String sessonId,
                     boolean mCaller, int CALL_QUALITY) {
        super(context, apiKey, sessonId);
        this.mContext = context;
        this.mSessionListener = listeners;
        this.mCaller = mCaller;
        CALL_STARTED = false;
        this.CALL_QUALITY = CALL_QUALITY;
    }

    // public methods
    public void setPlayersViewContainer(ViewGroup container) {
        this.mSubscribersViewContainer = container;
    }

    public void setPreviewView(ViewGroup preview) {
        this.mPreview = preview;
    }


    // callbacks
    @Override
    protected void onConnected() {
        try {
            CALL_CONNECTED = true;
            if (CALL_QUALITY == Constants.LOW){
                mPublisher = new Publisher(mContext,
                        "MyPublisher",
                        Publisher.CameraCaptureResolution.LOW,
                        Publisher.CameraCaptureFrameRate.FPS_7);
            }else if(CALL_QUALITY == Constants.MEDIUM){
                mPublisher = new Publisher(mContext,
                        "MyPublisher",
                        Publisher.CameraCaptureResolution.MEDIUM,
                        Publisher.CameraCaptureFrameRate.FPS_15);
            }else if(CALL_QUALITY == Constants.HIGH){
                mPublisher = new Publisher(mContext,
                        "MyPublisher",
                        Publisher.CameraCaptureResolution.HIGH,
                        Publisher.CameraCaptureFrameRate.FPS_15);
            }else {
                mPublisher = new Publisher(mContext,
                        "MyPublisher",
                        Publisher.CameraCaptureResolution.LOW,
                        Publisher.CameraCaptureFrameRate.FPS_7);
            }
            publish(mPublisher);
            // Add video preview
            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
            mPublisherView = mPublisher.getView();
            mPreview.addView(mPublisher.getView(), lp);
            mPublisher.setStyle(BaseVideoRenderer.STYLE_VIDEO_SCALE, BaseVideoRenderer.STYLE_VIDEO_FILL);
        }catch (Exception e){
            e.printStackTrace();
            mSessionListener.onPluginError(Constants.ERROR_ON_CONNECT);
        }

//        }

//        presentText("Welcome to OpenTok Chat.");
    }


    @Override
    protected void onStreamReceived(Stream stream) {

        try {
            CALL_STARTED = true;

            mSessionListener.onCallConnected();

            mSessionListener.onCallStarted();

            MySubscriber p = new MySubscriber(mContext, stream);
            // we can use connection data to obtain each user id
            p.setUserId(stream.getConnection().getData());
            p.setStyle(BaseVideoRenderer.STYLE_VIDEO_SCALE, BaseVideoRenderer.STYLE_VIDEO_FILL);

            // Subscribe to this player
            this.subscribe(p);
            mSubscriber = p;
            mSubscriberStream.put(stream, p);
            mSubscriberConnection.put(stream.getConnection().getConnectionId(), p);

            mSubscriberView = p.getView();
            mSubscribersViewContainer.addView(mSubscriberView);
        }catch (Exception e){
            e.printStackTrace();
            mSessionListener.onPluginError(Constants.ERROR_ON_SESSION_CONNECT);
        }


    }

    @Override
    protected void onStreamDropped(Stream stream) {

        try {
            if(CALL_STARTED)
                mSessionListener.onCallEnded();
            else
                mSessionListener.onCallRejected();

            mSubscriberStream.remove(stream);
            mSubscriberConnection.remove(stream.getConnection().getConnectionId());
            mSessionListener.onStreamDrop(stream);
        }catch (Exception e){
            e.printStackTrace();
            mSessionListener.onPluginError(Constants.ERROR_STREAM_DROPED);
        }

    }

    @Override
    protected void onSignalReceived(String type, String data,
                                    Connection connection) {
        Log.e(TAG , "signal received");
    }

    public void hideVideo() {
        try {
            mPublisher.setPublishVideo(!mPublisher.getPublishVideo());
            if (mPublisher.getPublishVideo()) {
                mPublisherView.setVisibility(View.VISIBLE);
            } else {
                mPublisherView.setVisibility(View.INVISIBLE);
            }
        }catch (Exception e){
            e.printStackTrace();
            mSessionListener.onPluginError(Constants.ERROR_OCCURED);
        }

    }

    public void muteMic() {
        try {
            if (mPublisher != null) {
                mPublisher.setPublishAudio(!mPublisher.getPublishAudio());
            }
        }catch (Exception e){
            e.printStackTrace();
            mSessionListener.onPluginError(Constants.ERROR_OCCURED);
        }

    }

    @Override
    protected void onStreamHasVideoChanged(Stream stream, int hasVideo) {
        super.onStreamHasVideoChanged(stream, hasVideo);
        try {
            if (mPublisher.getStream().toString().equalsIgnoreCase(stream.toString())) {
            } else {
                if (hasVideo == 0) {
                    mSubscriber.setSubscribeToVideo(false);
                    mSubscribersViewContainer.removeView(mSubscriberView);
                    mPreview.removeView(mPublisherView);
                    mSubscribersViewContainer.addView(mPublisherView);
                } else {
                    mSubscriber.setSubscribeToVideo(true);
                    if (mSubscribersViewContainer.getChildAt(mSubscribersViewContainer.getChildCount() - 1).equals(mPublisherView)) {
                        mSubscribersViewContainer.removeView(mPublisherView);
                    }
                    mPreview.addView(mPublisherView);
                    mSubscribersViewContainer.addView(mSubscriberView);
                }
            }
            if(mSubscriber!=null) {
                if (mSubscriber.getSubscribeToVideo() || mPublisher.getPublishVideo()) {
                    mSessionListener.onVideoViewChange(true);
                } else {
                    mSessionListener.onVideoViewChange(false);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
            mSessionListener.onPluginError(Constants.ERROR_OCCURED);
        }

    }

    /**
     * check that mic is muted or not
     *
     * @return if mic is mute or not
     */
    public boolean isMicMuted() {
        return !mPublisher.getPublishAudio();
    }

    /**
     * check that mic is muted or not
     *
     * @return
     */
    public boolean isCameraOn() {
        return mPublisher.getPublishVideo();
    }

    /**
     * swipe Cameras
     */
    public void swipeCamera() {
        try{
            mPublisher.swapCamera();
        }catch (Exception e ){
            e.printStackTrace();
            mSessionListener.onPluginError(Constants.ERROR_OCCURED);
        }
    }

    public Subscriber getSubscriber()
    {
        return mSubscriber;
    }

    @Override
    protected void onConnectionDestroyed(Connection connection) {
        super.onConnectionDestroyed(connection);
        mSessionListener.onCallDisconnected();
    }

    @Override
    protected void onConnectionCreated(Connection connection) {
        super.onConnectionCreated(connection);
        if(!mCaller)
            mSessionListener.onReciverInitialized();
    }

    @Override
    protected void onArchiveStarted(String id, String name) {
        super.onArchiveStarted(id, name);
    }

    @Override
    protected void onArchiveStopped(String id) {
        super.onArchiveStopped(id);
    }

    @Override
    protected void onDisconnected() {
        super.onDisconnected();
        if(!CALL_STARTED)
            mSessionListener.onCallEndBeforeConnect();
        else
            mSessionListener.onCallDisconnected();
    }

    @Override
    protected void onError(OpentokError error) {
        super.onError(error);
        mSessionListener.onError(error);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    protected void onReconnected() {
        super.onReconnected();
    }

    @Override
    protected void onReconnecting() {
        super.onReconnecting();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

}
