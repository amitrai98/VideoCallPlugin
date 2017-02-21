package com.evontech.VideoPlugin;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.text.Html;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.opentok.android.Connection;
import com.opentok.android.OpentokError;
import com.opentok.android.Stream;
import com.ringhop.MainActivity;
import com.ringhop.R;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.LOG;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import fr.bmartel.speedtest.ISpeedTestListener;
import fr.bmartel.speedtest.SpeedTestError;
import fr.bmartel.speedtest.SpeedTestReport;
import fr.bmartel.speedtest.SpeedTestSocket;

public class VideoPlugin extends CordovaPlugin implements SessionListeners, ActivityListener, View.OnClickListener {
    public static final String ACTION_INIT_CALL = "initializeVideoCalling";
    public static final String ACTION_ENDCALL = "endCalling";
    public static final String ACTION_BALANCE_WARNING = "showLowBalanceWarning";
    public static final String ACTION_GETUSERBALANCE = "getUserBalance";
    public static final String ACTION_SENDSIGNAL = "sendSignal";
    public static final String ACTION_APIRESPONSE = "receivedResponseFromAPI";
    public static final String ACTION_TIPRECEIVED = "tipReceived";
    public static final String ACTION_ENDCALL_DUETO_LOWBAL = "EndCallDueToLowBal";
    public static final String SIGNAL_MESSAGE = "signal_message";
    private static final String TAG = CordovaPlugin.class.getSimpleName();
    private static final String TIP = "tip";
    private static MySession mSession;
    private ImageView mVideoCallBtn, mMicBtn, mDisconnectBtn, mSwipeBtn;
    private static View mCallView, mNoneView;
    private ViewGroup mViewGroup;
    private LinearLayout mParentProgressDialog;
    private Chrono mTimerTxt;
    private CardView mPricePopUp = null;
    private String mCallPerMinute, mProfileImageUrl, mUserBalance = "0";
    private RelativeLayout mCallingViewParent;
    private boolean isCallingViewVisible = true;
    private Handler handler = new Handler();
    private Runnable callRunnable;
    private CallbackContext mCallBackContext;
    private long mCallTime = 0;
    private boolean resumeHasRun = false;
    private ImageView mProfilePicConnecting = null;
    private ImageView mImageNonView = null;
    private JSONArray mJsonData = null;
    private boolean CALL_DISCONNECT = false;
    private String data;
    private boolean SESSION_STARTED = false;
//    private com.listeners.SessionListeners sessionListeners = null;

    public static final String SUCCESS = "success";
    public static final String ERROR = "error";

    private String MISSED_CALL = null;
    private boolean prev_command = false;
    private boolean call_initialized = false;
    private boolean mCaller = false;
    private boolean mDisconnect = false;
    private boolean mMissedCall = false;

    private CallBean callBean = null;

    // new view changes
    private RelativeLayout layout_tip_send_receive = null;
    private RelativeLayout layout_low_credit = null;
    private RelativeLayout layout_tip = null;
    //    private ImageView img_add_credit = null;
    private RelativeLayout layout_plus_credit = null;
    private ProgressDialog dialogWait = null;
    private TextView txt_tipsent = null;
    private TextView tv_username = null;
    private LinearLayout layout_header_addcredit = null;
    private TextView txt_low_balance = null;


    private RelativeLayout layout_enable_disable_video = null;
    private TextView txt_connecting = null;
    private ProgressBar progressbar = null;
    public static RelativeLayout layout_publisher = null;
    private TextView txt_callcoast = null;

//    private RelativeLayout layout_send_tip = null;

    // tip dialog
    private Dialog dialogTip = null;
    private RelativeLayout layout_close = null;
    private LinearLayout layout_addmore = null;
    private Button btn_sendtip = null;
    private Button btn_ten_dollar = null;
    private Button btn_twenty_dollar = null;
    private Button btn_fourty_dollar = null;
    private Button btn_sixty_dollar = null;
    private EditText edt_tipamount = null;
    private LinearLayout layout_progress_tip = null;
    private RelativeLayout layout_others = null;
    private Button btn_lowbal = null;

    //add credit
    private Dialog dialogAddAmount = null;
    private RelativeLayout layout_close_add_credit = null;
    private Button btn_buy_ten = null;
    private Button btn_buy_twetnty = null;
    private Button btn_buy_fourty = null;
    private Button btn_buy_sixty = null;
    private LinearLayout layout_progress = null;
    private LinearLayout layout_credit_btns = null;
    private TextView txt_creditbal = null;
    private boolean isConnected = false;

    //success dialog
    private Dialog dialogCreditSuccess = null;


    private JSONObject jsonResponse = null;
    private MediaPlayer mediaPlayer = null;

    private int QUALITY = Constants.LOW;

    private boolean CALL_END_LOW_BAL = false;
    private String sessonToken = null;
    private boolean reconnect = false;
    private RelativeLayout layout_username = null;
    private String SIGNAL_MESSAGE_KEY = "signal_message_key";


    //  change color of the tip bottom and hide it put check for low balance and corner rounded (done)
    //  increase the text size of credit (done)
    //  add checks for pro and normal user for showing different views (done)
    //  add check if user is able the call in not then freeze on the add credit screen. (done)
    //  add check for caller init and receiver init on start of call (done)

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
    }

    @Override
    protected void pluginInitialize() {
        super.pluginInitialize();
    }

    @Override
    public boolean execute(String action, final JSONArray args, CallbackContext callbackContext) throws JSONException {

        if (action != null && !action.isEmpty()) {

            if (action.equalsIgnoreCase(ACTION_INIT_CALL)) {
                //checks current connection bandwidth
                return initVideoCall(action, args, callbackContext);
            }

            if (action.equalsIgnoreCase(ACTION_ENDCALL)) {
                String object = args.getString(0);
                if (object != null && !object.isEmpty() && object.equalsIgnoreCase(ACTION_ENDCALL_DUETO_LOWBAL))
                    CALL_END_LOW_BAL = true;
                mMissedCall = true;
                endCall(object);

//                return true;
            }


            //responses receivedResponseFromAPI("credit","success","0");
            // done check the credit of tip send success and remove the dialogs
            // done "status - credit or tip " "response type- success or error", "error message in case of failure or updated amount in case of success"
            // done close the status or credit dialog after reading response
            // done in case of 0 amount, remove low balance dialog if i am pro
            if (action.equalsIgnoreCase(ACTION_APIRESPONSE)) {


                cordova.getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String transaction_type = null;
                        String amount = null;
                        String status = null;



                        try {
                            if (dialogTip != null && dialogTip.isShowing())
                                dialogTip.dismiss();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        if (args != null && args.length() > 0) {
                            try {
                                transaction_type = args.get(0).toString();
                                status = args.get(1).toString();
                                amount = args.get(2).toString();
//                                if(amount != null && !amount.isEmpty())
//                                    mUserBalance = amount;



                                if (transaction_type.equalsIgnoreCase("tip")) {
                                    if (status.equalsIgnoreCase("success")) {
                                        showTipSendReceive(amount, false);
                                        sendSignal(TIP, amount);
                                    } else {
                                        Toast.makeText(cordova.getActivity(), cordova.getActivity()
                                                        .getString(R.string.tip_send_failure_message),
                                                Toast.LENGTH_SHORT).show();
                                    }

                                } else if (transaction_type.equalsIgnoreCase("credit")) {
                                    if (status.equalsIgnoreCase("success")) {
                                        if (amount != null && !amount.isEmpty())
                                            mUserBalance = amount;


                                        if (!isConnected && !callBean.getUserType().equalsIgnoreCase(Constants.USER_TYPE_PRO)) {
                                            reconnectOnCreditAdd();
                                        }
                                        if (callBean != null && callBean.getUserType().equalsIgnoreCase(Constants.USER_TYPE_PRO)) {
                                            layout_low_credit.setVisibility(View.INVISIBLE);
                                            return;
                                        } else if (layout_low_credit != null &&
                                                layout_low_credit.getVisibility() == View.VISIBLE) {
                                            layout_low_credit.setVisibility(View.INVISIBLE);
                                        }
                                        if (dialogAddAmount != null && dialogAddAmount.isShowing())
                                            dialogAddAmount.dismiss();
                                        showCreditSuccessDialog(amount);

                                    } else {
                                        Toast.makeText(cordova.getActivity(), cordova.getActivity()
                                                        .getString(R.string.credit_add_failure_message),
                                                Toast.LENGTH_SHORT).show();
                                    }
                                }

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });

                return true;
            }

            if (action.equalsIgnoreCase(ACTION_BALANCE_WARNING)) {
                if (args.length() > 0 && !args.get(0).toString().equalsIgnoreCase("null") &&
                        callBean != null && callBean.getUserType().equalsIgnoreCase(Constants.USER_TYPE_PRO))
                    showLowBalanceWarning(true, null);
                else {
                    try {
                        if (args.length() == 0)
                            return false;
                        String amount = args.get(0).toString();
                        showLowBalanceWarning(false, amount);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                return true;

            }

            // todo show tip received dialog to pro only
            if (action.equalsIgnoreCase(ACTION_TIPRECEIVED)) {
                if (callBean.getUserType().equalsIgnoreCase(Constants.USER_TYPE_PRO)) {
                    try {
                        if (args.length() > 0){
                            showTipSendReceive(args.get(0).toString(), true);

                        }


                        return true;
                    } catch (Exception e) {
                        e.printStackTrace();
                        return false;
                    }
                }

                return true;

            }

            if (action.equalsIgnoreCase(ACTION_GETUSERBALANCE)) {
                if (args.length() > 0) {
                    String balance_object = args.get(0).toString();
//                    mUserBalance =
                    JSONObject object = new JSONObject(balance_object);
                    Log.e(TAG, ""+object);

                    //{"userBalance":"23.91","shareBalance":"0.05"}
                    if(object.has("userBalance"))
                        mUserBalance = object.getString("userBalance");


                    if (mUserBalance != null)
                        updateUserBalance(mUserBalance);

                    try {

                        sendSignal("callCost", mUserBalance);
                    }catch (Exception exp){
                        exp.printStackTrace();
                    }

                }

                return true;
            }

//            if (action.equalsIgnoreCase(ACTION_SENDSIGNAL)) {
//                try {
//                    if (args.length() > 0) {
//                        //SIGNAL_MESSAGE_KEY = args.get(0).toString();
//                        data = args.get(0).toString();
//                        //sendSignal(SIGNAL_MESSAGE_KEY , data);
////                        sendSignal(data);
//                    }
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
        }
        return false;
    }

    /**
     * initiates video calling
     *
     * @param callBean all initial json data for calling
     */
    private void initCall(CallBean callBean, int CALL_QUALITY) {
        try {
            isConnected = false;
            mDisconnect = false;
            call_initialized = true;
            CALL_DISCONNECT = false;
            SESSION_STARTED = false;
            mUserBalance = callBean.getAmount();
            String apiKey = callBean.getApiKey();//.getString("apiKey");
            String sessonId = callBean.getSessionId();//object.getString("sessonId");
            sessonToken = callBean.getToken();//object.getString("sessonToken");
            mCallPerMinute = callBean.getCallPerMinute();//object.getString("callPerMinute");
            mProfileImageUrl = callBean.getProfileImage();//object.getString("profileImageUrl");

//            String apiKey = args.get(0).toString();//.getString("apiKey");
//            String sessonId = args.get(1).toString();//object.getString("sessonId");
//            String sessonToken = args.get(2).toString();//object.getString("sessonToken");
//            mCallPerMinute = args.get(3).toString();//object.getString("callPerMinute");
//            mUserBalance = args.get(4).toString();//object.getString("userBalance");
//            mProfileImageUrl = args.get(5).toString();//object.getString("profileImageUrl");


//            mCallPerMinute = "0";
//            mUserBalance = "100";
//            mProfileImageUrl = "http://i2.wp.com/allindiaroundup.com/wp-content/uploads/2015/01/mr-bean-as-a-serial-killer-whatsapp-dp4.jpg";

//        String sessonId = OpenTokConfig.SESSION_ID;
//        String apiKey = OpenTokConfig.API_KEY;
//        String sessonToken = OpenTokConfig.TOKEN;


            if (callBean != null && callBean.getIsReceiverInit().equalsIgnoreCase("false")) {
                mCaller = false;
                JSONObject json = getJson(Constants.INIT_COMPLETE, SUCCESS);
                mCallBackContext.successMessage(json);
                mSession = new MySession(cordova.getActivity(), this, apiKey, sessonId, false, CALL_QUALITY);
            } else if (callBean != null && callBean.getIsReceiverInit().equalsIgnoreCase("true")) {
                mCaller = true;
                JSONObject json = getJson(Constants.RECEIVER_INITIALIZED, SUCCESS);
                mCallBackContext.successMessage(json);
                mSession = new MySession(cordova.getActivity(), this, apiKey, sessonId, true, CALL_QUALITY);
            }

            addView();

            if (callBean.getIsAbleToCall().equalsIgnoreCase("false")) {
                showAddAmountDialog();
            } else
                mSession.connect(sessonToken);

//
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * adds the video calling view to the parrent layout.
     */
    private void addView() {
        mViewGroup = (ViewGroup) webView.getView();

        LayoutInflater inflator = LayoutInflater.from(cordova.getActivity());
        mCallView = inflator.inflate(R.layout.room, null);

        layout_tip_send_receive = (RelativeLayout) mCallView.findViewById(R.id.layout_tip_send_receive);
        layout_tip = (RelativeLayout) mCallView.findViewById(R.id.layout_tip);
        layout_low_credit = (RelativeLayout) mCallView.findViewById(R.id.layout_low_credit);
        txt_low_balance = (TextView) mCallView.findViewById(R.id.txt_lowbalance);
        layout_plus_credit = (RelativeLayout) mCallView.findViewById(R.id.layout_plus_credit);
        txt_tipsent = (TextView) mCallView.findViewById(R.id.txt_tipsent);
        tv_username = (TextView) mCallView.findViewById(R.id.tv_username);
        layout_header_addcredit = (LinearLayout) mCallView.findViewById(R.id.layout_header_addcredit);

        layout_username = (RelativeLayout) mCallView.findViewById(R.id.layout_username);
//        layout_send_tip = (RelativeLayout) mCallView.findViewById(R.id.layout_send_tip);

        DisplayMetrics metrics = cordova.getActivity().getResources().getDisplayMetrics();
        int screen_height = metrics.heightPixels;

        layout_username.getLayoutParams().height = (int) (screen_height * 0.07);

        layout_enable_disable_video = (RelativeLayout) mCallView.findViewById(R.id.layout_enable_disable_video);

        layout_enable_disable_video.setOnClickListener(this);
        txt_connecting = (TextView) mCallView.findViewById(R.id.txt_connecting);

        if (callBean.getUserType().equalsIgnoreCase(Constants.USER_TYPE_PRO)) {
            layout_tip.setVisibility(View.INVISIBLE);
            layout_plus_credit.setVisibility(View.INVISIBLE);
            layout_header_addcredit.setVisibility(View.INVISIBLE);
        }


        setMargins(mCallView, 0, 500, 0, 0);

//        if(callBean.getIsAbleToCall().equalsIgnoreCase("true")){
//            preview = (ViewGroup) mCallView.findViewById(R.id.preview);  // User View
//            mSession.setPreviewView(preview);

        layout_publisher = (RelativeLayout) mCallView.findViewById(R.id.layout_publisher);  // User View
        layout_publisher.setVisibility(View.INVISIBLE);
        mSession.setmPublisherViewContainer(layout_publisher);


        RelativeLayout layout_subscriber = (RelativeLayout) mCallView.findViewById(R.id.layout_subscriber);  // Subscriber View
        mSession.setmSubscribersViewContainer(layout_subscriber);
//        }


        // Progress bar Views
        mParentProgressDialog = (LinearLayout) mCallView.findViewById(R.id.ll_parent_connecting);
        mProfilePicConnecting = (ImageView) mCallView.findViewById(R.id.iv_connecting_img);
        txt_callcoast = (TextView) mCallView.findViewById(R.id.tv_dialog_price);

        if (callBean != null && callBean.getUserType().equalsIgnoreCase(Constants.USER_TYPE_PRO))
            txt_callcoast.setText(Html.fromHtml("<font color=\"#ffffff\">" + "User will be charged " + "</font>" + "<font color=\"#F0AF32\">" + callBean.getCallPerMinute() + "$" + "</font>" + "<font color=\"#ffffff\">" + " per minute <br> once video call connects" + "</font>"));//setText(cordova.getActivity().getString(R.string.call_connect_price, callBean.getCallPerMinute()));//("Once connected this video chat \n will be billed at " + mCallPerMinute + " per min.");//setText(cordova.getActivity().getString(R.string.pro_call_coast, callBean.getCallPerMinute()));//("Once connected this video chat \n will be billed at " + mCallPerMinute + " per min.");
        else
            txt_callcoast.setText(Html.fromHtml("<font color=\"#ffffff\">" + "Once connected this video chat <br> will be billed at " + "</font>" + "<font color=\"#F0AF32\">" + callBean.getCallPerMinute() + "$" + "</font>" + "<font color=\"#ffffff\">" + " per min" + "</font>"));//setText(cordova.getActivity().getString(R.string.call_connect_price, callBean.getCallPerMinute()));//("Once connected this video chat \n will be billed at " + mCallPerMinute + " per min.");//setText(cordova.getActivity().getString(R.string.pro_call_coast, callBean.getCallPerMinute()));//("Once connected this video chat \n will be billed at " + mCallPerMinute + " per min.");
//            price.setText(cordova.getActivity().getString(R.string.call_connect_price, callBean.getCallPerMinute()));//("Once connected this video chat \n will be billed at " + mCallPerMinute + " per min.");

//        price.setText(cordova.getActivity().getString(R.string.call_connect_price, callBean.getCallPerMinute()));//("Once connected this video chat \n will be billed at " + mCallPerMinute + " per min.");

        //        creator.into(mProfilePicConnecting);
        progressbar = (ProgressBar) mCallView.findViewById(R.id.pb_connecting);
        progressbar.getIndeterminateDrawable().setColorFilter(ContextCompat.
                        getColor(cordova.getActivity(), android.R.color.holo_green_dark),
                android.graphics.PorterDuff.Mode.SRC_IN);
        mNoneView = mCallView.findViewById(R.id.non_view);
        mImageNonView = (ImageView) mCallView.findViewById(R.id.iv_no_view_img);
//        creator.into(mImageNonView);


        mPricePopUp = (CardView) mCallView.findViewById(R.id.cv_connecting_price_dialog);

        if (mCallPerMinute.equalsIgnoreCase("0")) {
            mPricePopUp.setVisibility(View.GONE);
        }

        /*Calling Views*/
        mCallingViewParent = (RelativeLayout) mCallView.findViewById(R.id.rl_calling_view);
        mVideoCallBtn = (ImageView) mCallView.findViewById(R.id.iv_video_call);
        mVideoCallBtn.setEnabled(false);
        mMicBtn = (ImageView) mCallView.findViewById(R.id.iv_audio_call);
        mMicBtn.setEnabled(false);
        mDisconnectBtn = (ImageView) mCallView.findViewById(R.id.iv_end_call);
        mSwipeBtn = (ImageView) mCallView.findViewById(R.id.iv_swipe_camera);
        mSwipeBtn.setEnabled(false);
        mTimerTxt = (Chrono) mCallView.findViewById(R.id.cm_timer);
        mVideoCallBtn.setOnClickListener(this);
        mMicBtn.setOnClickListener(this);
        mDisconnectBtn.setOnClickListener(this);
        mSwipeBtn.setOnClickListener(this);
        layout_plus_credit.setOnClickListener(this);
        layout_tip.setOnClickListener(this);
        layout_header_addcredit.setOnClickListener(this);

        layout_tip.setClickable(false);


        if (callBean != null && callBean.getUserName()
                != null && !callBean.getUserName().isEmpty())
            tv_username.setText(callBean.getUserName());

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                mViewGroup.addView(mCallView);
                RequestCreator creator = Picasso.with(cordova.getActivity()).load(mProfileImageUrl);
                creator.into(mProfilePicConnecting);
                creator.into(mImageNonView);
                mTimerTxt.setActivity(cordova.getActivity());

                mTimerTxt.setOnChronometerTickListener(new Chrono.OnChronometerTickListener() {
                    @Override
                    public void onChronometerTick(Chrono chronometer) {
//                        mTimerTxt.setText(chronometer.getText());
                    }
                });
            }
        };
        cordova.getActivity().runOnUiThread(runnable);


    }

    @Override
    public void onStreamDrop(Stream stream) {

        disconnectCall();
    }

    @Override
    public void onVideoViewChange(boolean hasVideo) {
//        mCallBackContext.success("Video View Change");
        if (hasVideo) {
            mNoneView.setVisibility(View.INVISIBLE);
        } else {
            mNoneView.setVisibility(View.VISIBLE);
        }
    }

//    @Override
//    public void onPublisherCreate() {
//        mCallBackContext.success("init complete ");
//    }

    @Override
    public void onCallConnected() {
        isConnected = true;
        JSONObject json = getJson(Constants.CONNECTION_CREATED, SUCCESS);
        mCallBackContext.successMessage(json);

        if (mCallPerMinute != null) {

//            layout_tip_send_receive.setVisibility(View.VISIBLE);
//            layout_tip.setVisibility(View.VISIBLE);
//            layout_low_credit.setVisibility(View.VISIBLE);

            JSONObject json_callstarted = getJson(Constants.CALL_STARTED, SUCCESS);
            mCallBackContext.successMessage(json_callstarted);

//            mParentProgressDialog.setVisibility(View.GONE);
            mVideoCallBtn.setEnabled(false);
            mMicBtn.setEnabled(true);
            mDisconnectBtn.setEnabled(true);
            mSwipeBtn.setEnabled(true);
            layout_tip.setClickable(true);
            mSwipeBtn.setVisibility(View.VISIBLE);
            mCallView.setEnabled(true);
            mCallView.setOnClickListener(this);

            mVideoCallBtn.setClickable(false);

            mTimerTxt.setBase(SystemClock.elapsedRealtime());
            mTimerTxt.start();

            visibleCallingViews();
            callThread();

            //aaabb/123456 susie/123456
        }
    }

    /**
     * animates the visibility of the views to visible
     */
    private void visibleCallingViews() {
//        mCallingViewParent.setVisibility(View.VISIBLE);
        SlideToAbove();
        SlideToLeft();
        addCreditSlideIn();
        mSwipeBtn.setVisibility(View.VISIBLE);
        isCallingViewVisible = true;
        callThread();
    }

    /**
     * animates the visibility of the views to invisible
     */
    private void invisibleCallingViews() {
        SlideToDown();
        SlideToRight();
        addCreditSlideOut();

        isCallingViewVisible = false;
    }


    /**
     * thread to change the visibility of the controls on ui thread
     * after some interval.
     */
    private void callThread() {
        callRunnable = new Runnable() {
            @Override
            public void run() {

                cordova.getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        invisibleCallingViews();
                    }
                });

            }
        };
        handler.postDelayed(callRunnable, 3000);
    }

    @Override
    public void onCallDisconnected() {
        disconnectCall();
        JSONObject json;
        if (mMissedCall)
            return;

        if (mCaller && !MySession.CALL_STARTED)
            json = getJson(Constants.CALL_ENDED_BY_RECEIVER, SUCCESS);
        else
            json = getJson(Constants.CALL_END, SUCCESS);

        mCallBackContext.successMessage(json);
    }

    @Override
    public void onCallRejected() {
        JSONObject json = getJson(Constants.CALL_ENDED_BY_RECEIVER, SUCCESS);
        mCallBackContext.successMessage(json);
    }

    @Override
    public void onCallStarted() {

        JSONObject json = getJson(Constants.CALL_STARTED, SUCCESS);
        mCallBackContext.successMessage(json);
        SESSION_STARTED = true;

        if (mCallPerMinute != null && !mCallPerMinute.equals("0")) {

//            mCallBackContext.success(Constants.CALL_STARTED);
//            mParentProgressDialog.setVisibility(View.GONE);
            txt_connecting.setText("WEBCAM OFF");
            txt_callcoast.setText("CONTACT'S WEB CAM DISABLED");
            txt_callcoast.setVisibility(View.GONE);
            progressbar.setVisibility(View.GONE);
            mVideoCallBtn.setEnabled(false);
            mMicBtn.setEnabled(true);
            mDisconnectBtn.setEnabled(true);
            mSwipeBtn.setEnabled(false);
            mSwipeBtn.setVisibility(View.VISIBLE);
            mCallView.setEnabled(true);
            mCallView.setOnClickListener(this);


            mVideoCallBtn.setClickable(false);

            mTimerTxt.setBase(SystemClock.elapsedRealtime());
            mTimerTxt.start();

            visibleCallingViews();
            callThread();


        }
    }

    @Override
    public void onCallEndBeforeConnect() {
        JSONObject json;

        if (mMissedCall)
            return;

        if (mDisconnect)
            json = getJson(Constants.CALL_ENDED_BY_RECEIVER, SUCCESS);
        else
            json = getJson(Constants.CALL_END, SUCCESS);

        mCallBackContext.successMessage(json);
    }

    @Override
    public void onCallEndByReceiver() {
        JSONObject json = getJson(Constants.CALL_END, SUCCESS);
        mCallBackContext.successMessage(json);
    }

    @Override
    public void onError(OpentokError error) {
        try {
            disconnectCall();
            if (error != null && error.getMessage() != null) {
                try {
                    String message = error.getMessage();
                    if (message.contains("Token expired. Token")) {
                        message = "\"" + message.split(":")[0] + "\"";
                    }
                    if (message != null && !message.isEmpty()) {
//                        JSONObject json = getJson(message, ERROR);
//                        mCallBackContext.error(json);

                        String network_type = getNetworkInfo();
//                        jsonResponse = new JSONObject("{\"data\":\"{\\\"networkType\\\":"+network_type+",\\\"error\\\":"+message+"}\",\"status\":\"failure\"}");
                        jsonResponse = new JSONObject("{\"" + "networkType" + "\":" + "\"" + network_type + "\""
                                + "," + "\"" + "error" + "\":" + message + "," + "\"" + "status"
                                + "\":" + "\"" + "failure" + "\"" + "}");
                        mCallBackContext.error(jsonResponse);


                    }

                } catch (Exception exp) {
                    exp.printStackTrace();
                }

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCallEnded() {
        JSONObject json = getJson(Constants.CALL_END, SUCCESS);
        mCallBackContext.successMessage(json);
    }

    @Override
    public void onReciverInitialized() {
        if (callBean.getIsReceiverInit().equalsIgnoreCase("false")) {
            JSONObject json = getJson(Constants.INIT_COMPLETE, SUCCESS);
            mCallBackContext.successMessage(json);
        } else {
            JSONObject json = getJson(Constants.RECEIVER_INITIALIZED, SUCCESS);
            mCallBackContext.successMessage(json);
        }

    }

    @Override
    public void onCallerInitialized() {
        if (callBean.getIsReceiverInit().equalsIgnoreCase("false")) {
            JSONObject json = getJson(Constants.INIT_COMPLETE, SUCCESS);
            mCallBackContext.successMessage(json);
        } else {
            JSONObject json = getJson(Constants.RECEIVER_INITIALIZED, SUCCESS);
            mCallBackContext.successMessage(json);
        }
    }

    @Override
    public void onReceiverInitialized() {
        JSONObject json = getJson(Constants.RECEIVER_INITIALIZED, SUCCESS);
        mCallBackContext.successMessage(json);
    }

    /**
     * if anything goes wrong
     *
     * @param error reason for the error
     */
    @Override
    public void onPluginError(String error) {
        if (error != null) {
            JSONObject json = getJson(error.toString(), ERROR);
            mCallBackContext.error(json);
        }
    }

    @Override
    public void videoReceived() {
        if (mParentProgressDialog != null)
            mParentProgressDialog.setVisibility(View.GONE);
    }

    @Override
    public void videoEnableDisabled(boolean hasVideo) {
        if (mParentProgressDialog != null && hasVideo)
            mParentProgressDialog.setVisibility(View.GONE);
        if (mParentProgressDialog != null && !hasVideo)
            mParentProgressDialog.setVisibility(View.VISIBLE);
    }

    @Override
    public void onSignalReceived(String type, String data, Connection connection) {
        // new code
        JSONObject json = new JSONObject();
        try {
            json.put(type, data);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mCallBackContext.successMessage(json);
    }


    /**
     * Disconnects the call
     */
    private void disconnectCall() {
        try {
            try {
                SESSION_STARTED = false;
                cordova.getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        try {
                            if (dialogTip != null && dialogTip.isShowing())
                                dialogTip.dismiss();
                            if (dialogAddAmount != null && dialogAddAmount.isShowing())
                                dialogAddAmount.dismiss();
                            if (dialogCreditSuccess != null && dialogCreditSuccess.isShowing())
                                dialogCreditSuccess.dismiss();

                        } catch (Exception exp) {
                            exp.printStackTrace();
                        }

                        mViewGroup.removeView(mCallView);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (CALL_DISCONNECT)
                return;

            CALL_DISCONNECT = true;


            ((MainActivity) cordova.getActivity()).setActivityListener(null);
//            if(MySession.CALL_CONNECTED){
////            JSONObject json = getJson(Constants.CALL_END, SUCCESS);
////            mCallBackContext.successMessage(json);
//                Log.e(TAG, "call connected");
//            }
//            if (mSession.getSubscriber() != null) {
////            JSONObject json = getJson(Constants.CALL_END, SUCCESS);
////            mCallBackContext.successMessage(json);
//            }
            if (!isConnected) {
                JSONObject json = getJson(Constants.CALL_ENDED_BY_RECEIVER, SUCCESS);
                mCallBackContext.successMessage(json);
            } else if (isConnected && callBean != null && callBean.getIsReceiverInit().equalsIgnoreCase("true")) {
                if (!CALL_END_LOW_BAL) {
                    JSONObject json = getJson(Constants.CALL_END, SUCCESS);
                    mCallBackContext.successMessage(json);
                }


            } else if (isConnected && callBean != null && callBean.getIsReceiverInit().equalsIgnoreCase("false")) {
//                JSONObject json = getJson(Constants.DISCONNECT_SUCCESS, SUCCESS);
                JSONObject endcalljson = getJson(Constants.CALL_END, SUCCESS);

//                mCallBackContext.successMessage(json);
                mCallBackContext.successMessage(endcalljson);
            }

            mSession.disconnect();

        } catch (Exception e) {
            e.printStackTrace();
        }
        cordova.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    mViewGroup.removeView(mCallView);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_audio_call:
                muteAudio();
                break;
            case R.id.iv_video_call:
                hideCam();
                break;
            case R.id.iv_end_call:
                mDisconnect = true;
                disconnectCall();
                break;

            case R.id.iv_swipe_camera:
                mSession.swipeCamera();
                break;
            case R.id.mainlayout:
                if (isCallingViewVisible) {
                    invisibleCallingViews();
                    handler.removeCallbacks(callRunnable);
                } else {
                    visibleCallingViews();
                }
                break;
            case R.id.layout_plus_credit:
                showAddAmountDialog();
                break;
            case R.id.layout_addmore:
                addMoreCredit();
                break;

            case R.id.layout_tip:
                showTipDialog();
                break;

            case R.id.layout_header_addcredit:
                showAddAmountDialog();
                break;

            case R.id.layout_enable_disable_video:
                try {
                    if (!isConnected)
                        return;
                    if (mSession != null && mSession.isCameraOn()) {
                        mVideoCallBtn.setImageResource(R.drawable.camera_no);
                    } else {
                        mVideoCallBtn.setImageResource(R.drawable.camera);
                    }
                    layout_enable_disable_video.setVisibility(View.GONE);
                    layout_publisher.setVisibility(View.VISIBLE);
                    mVideoCallBtn.setEnabled(true);
                    mSwipeBtn.setEnabled(true);
                    mVideoCallBtn.setClickable(true);
                    mSession.showVideo();
                } catch (Exception exp) {
                    exp.printStackTrace();
                }

                break;
        }
    }

    /**
     * Hide/Show Video Camera of Subscriber
     */
    public void hideCam() {
        mSession.hideVideo();
        if (mSession.isCameraOn()) {
            mVideoCallBtn.setImageResource(R.drawable.camera);
            layout_enable_disable_video.setVisibility(View.GONE);
            layout_publisher.setVisibility(View.VISIBLE);
        } else {
            mVideoCallBtn.setImageResource(R.drawable.camera_no);
            layout_enable_disable_video.setVisibility(View.VISIBLE);
            layout_publisher.setVisibility(View.GONE);
        }
    }

    /**
     * Enable/Disable Mic.
     */
    public void muteAudio() {
        mSession.muteMic();
        if (mSession.isMicMuted()) {
            mMicBtn.setImageResource(R.drawable.mic_no);
        } else {
            mMicBtn.setImageResource(R.drawable.mic);
        }
    }


    /**
     * animation to slide the view above.
     */
    public void SlideToAbove() {
        Animation slide = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF,
                10f, Animation.RELATIVE_TO_SELF, 0.0f);

        slide.setDuration(600);
        slide.setFillAfter(true);
        slide.setFillEnabled(true);
        mCallingViewParent.startAnimation(slide);

        slide.setAnimationListener(new Animation.AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {
                mCallingViewParent.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {

            }

        });

    }


    /**
     * animation to slide the view down.
     */
    public void SlideToDown() {
        Animation slide = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF,
                0.0f, Animation.RELATIVE_TO_SELF, 5.2f);

        slide.setDuration(600);
        slide.setFillAfter(true);
        slide.setFillEnabled(true);
        mCallingViewParent.startAnimation(slide);

        slide.setAnimationListener(new Animation.AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mCallingViewParent.setVisibility(View.GONE);
            }

        });

    }

    /**
     * animation to slide the view left.
     */
    public void SlideToLeft() {
        Animation slide = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 10.0f,
                Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF,
                0.0f, Animation.RELATIVE_TO_SELF, 0.0f);

        slide.setDuration(600);
        slide.setFillAfter(true);
        slide.setFillEnabled(true);
        mSwipeBtn.startAnimation(slide);

        slide.setAnimationListener(new Animation.AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {
                mSwipeBtn.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
            }

        });

    }

    /**
     * slides upward
     */
//    private void slideToUp(){
//        Animation slide = null;
//        slide = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f,
//                Animation.RELATIVE_TO_SELF, 5.2f, Animation.RELATIVE_TO_SELF,
//                0.0f, Animation.RELATIVE_TO_SELF, 0.0f);
//
//        slide.setDuration(600);
//        slide.setFillAfter(true);
//        slide.setFillEnabled(true);
//        mSwipeBtn.startAnimation(slide);
//
//        slide.setAnimationListener(new Animation.AnimationListener() {
//
//            @Override
//            public void onAnimationStart(Animation animation) {
//            }
//
//            @Override
//            public void onAnimationRepeat(Animation animation) {
//            }
//
//            @Override
//            public void onAnimationEnd(Animation animation) {
//
//                mSwipeBtn.setVisibility(View.GONE);
////                mCallingViewParent.clearAnimation();
////
////                RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
////                        mCallingViewParent.getWidth(), mCallingViewParent.getHeight());
////                lp.setMargins(0, mCallingViewParent.getWidth(), 0, 0);
////                lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
////                mCallingViewParent.setLayoutParams(lp);
//
//            }
//
//        });
//    }

    /**
     * slides the view to the right
     */
    public void SlideToRight() {
        Animation slide = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, 5.2f, Animation.RELATIVE_TO_SELF,
                0.0f, Animation.RELATIVE_TO_SELF, 0.0f);

        slide.setDuration(600);
        slide.setFillAfter(true);
        slide.setFillEnabled(true);
        mSwipeBtn.startAnimation(slide);

        slide.setAnimationListener(new Animation.AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {

                mSwipeBtn.setVisibility(View.INVISIBLE);
//                mCallingViewParent.clearAnimation();
//
//                RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
//                        mCallingViewParent.getWidth(), mCallingViewParent.getHeight());
//                lp.setMargins(0, mCallingViewParent.getWidth(), 0, 0);
//                lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
//                mCallingViewParent.setLayoutParams(lp);

            }

        });

    }

    /**
     * slides out the credit button
     */
    private void addCreditSlideIn() {

        if (!callBean.getUserType().equalsIgnoreCase(Constants.USER_TYPE_PRO)) {
            Animation animation = new TranslateAnimation(-500, 0, 0, 0);
            animation.setDuration(600);
            animation.setFillAfter(true);
            animation.setFillEnabled(true);
            layout_tip.startAnimation(animation);

            animation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    if (!callBean.getUserType().equalsIgnoreCase(Constants.USER_TYPE_PRO))
                        layout_tip.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
        }

    }

    /**
     * slides out the credit button
     */
    private void addCreditSlideOut() {
        if (!callBean.getUserType().equalsIgnoreCase(Constants.USER_TYPE_PRO)) {
            Animation animation = new TranslateAnimation(0, -500, 0, 0);
            animation.setDuration(600);
            animation.setFillAfter(true);
            animation.setFillEnabled(true);
            layout_tip.startAnimation(animation);
            animation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    layout_tip.setVisibility(View.INVISIBLE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
        }

    }

    @Override
    public void onPauseActivity() {
        if (mSession != null) {
            mSession.onPause();
        }
    }

    @Override
    public void onResumeActivity() {
        if (!resumeHasRun) {
            resumeHasRun = true;
        } else {
            if (mSession != null) {
                mSession.onResume();
            }
        }
    }

    @Override
    public void onStoppedActivity() {
//        if (mSession != null) {
//            mSession.disconnect();
//        }
    }

    @Override
    public void onDestroyActivity() {
        if (mSession != null) {
            mSession.disconnect();
        }
    }

    @Override
    public void onRequestAccessed() {
        Log.e(TAG, "y m i commented");
    }

    /**
     * returns json for the message
     *
     * @param message      to be sent to cordova
     * @param message_type is success or failure
     * @return returns json object for the message
     */
    private JSONObject getJson(String message, String message_type) {


        try {
            if (message == null || message.equalsIgnoreCase(" ")) {
                return new JSONObject("{\"data\":\" \",\"status\":\"success\"}");
            } else if (message.isEmpty()) {
                return new JSONObject("{\"data\":\" \",\"status\":\"success\"}");
            }

            if (message_type.equals(SUCCESS)) {
                if (message.equals("Initialization completed !!"))
                    jsonResponse = new JSONObject("{\"data\":\"Initialization completed !!\",\"status\":\"success\"}");
                else if (message.equals("Successfully disconnected !!"))
                    jsonResponse = new JSONObject("{\"data\":\"Successfully disconnected !!\",\"status\":\"success\"}");
                else
                    jsonResponse = new JSONObject("{\"data\":" + message + ",\"status\":\"success\"}");

                return jsonResponse;

            } else if (message_type.equals(ERROR)) {
                String network_type = getNetworkInfo();
                jsonResponse = new JSONObject("{\"data\":\"{\\\"networkType\\\":" + network_type + ",\\\"error\\\":" + message + "}\",\"status\":\"failure\"}");
                return jsonResponse;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return null;
    }

    /**
     * sets margin to a view
     *
     * @param v vertical
     * @param l from left
     * @param t from top
     * @param r from right
     * @param b from bottom
     */
    public static void setMargins(View v, int l, int t, int r, int b) {
        if (v.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
            p.setMargins(l, t, r, b);
            v.requestLayout();
        }
    }

    /**
     * this method returns the call back as it is and removes the calling view.
     */
    private void endCall(String message) {
        try {

            if (!call_initialized)
                return;

            if (prev_command)
                return;

            prev_command = true;


            if (mSession != null)
                mSession.disconnect();

            cordova.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mViewGroup.removeView(mCallView);
                }
            });


            if (message != null && message.equalsIgnoreCase("missedCall")) {
                JSONObject json = getJson(message, SUCCESS);
                mCallBackContext.successMessage(json);
            } else if (message == null || message.equalsIgnoreCase("null")) {
                JSONObject json = getJson(null, SUCCESS);
                mCallBackContext.successMessage(json);
            } else {
                JSONObject json = getJson(message, SUCCESS);
                mCallBackContext.successMessage(json);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * initialize video call
     */
    private boolean initVideoCall(String action, JSONArray args, CallbackContext callbackContext) {
        try {
            prev_command = false;
            mMissedCall = false;

            dialogWait = new ProgressDialog(cordova.getActivity());
            dialogWait.setMessage(cordova.getActivity().getResources().getString(R.string.wait_message));

            Log.e(TAG, "" + args);

            Gson gson = new Gson();
            callBean = gson.fromJson(args.get(0).toString(), CallBean.class);
            Log.e(TAG, "" + callBean);
//            initCall(callBean);
            if (ACTION_INIT_CALL.equals(action)) {
                mCallBackContext = callbackContext;
                ((MainActivity) cordova.getActivity()).setActivityListener(this);
                mJsonData = args;

                int permissionCheck = ContextCompat.checkSelfPermission(cordova.getActivity(),
                        Manifest.permission.CAMERA);

                int permissionCheckAudio = ContextCompat.checkSelfPermission(cordova.getActivity(),
                        Manifest.permission.RECORD_AUDIO);

                int permissionCheckModifyAudio = ContextCompat.checkSelfPermission(cordova.getActivity(),
                        Manifest.permission.MODIFY_AUDIO_SETTINGS);

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        getBandWidthSpeed(new SpeedTestListener() {
                            @Override
                            public void onDownloadComplete(final int quality) {
                                QUALITY = quality;
                                cordova.getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        initCall(callBean, QUALITY);
                                    }
                                });
                            }

                            @Override
                            public void onDownloadFailed() {
                                cordova.getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        QUALITY = Constants.LOW;
                                        initCall(callBean, Constants.LOW);
                                    }
                                });
                            }
                        });
                    }
                }).start();


                if (permissionCheck == PackageManager.PERMISSION_GRANTED && permissionCheckAudio == PackageManager.PERMISSION_GRANTED && permissionCheckModifyAudio == PackageManager.PERMISSION_GRANTED) {
//                    cordova.getThreadPool().execute(new Runnable() {
//                        @Override
//                        public void run() {
//
//                        }
//                    });
                    Log.e(TAG, "call received");

                } else {

                    // Should we show an explanation?
                    if (ActivityCompat.shouldShowRequestPermissionRationale(cordova.getActivity(),
                            Manifest.permission.CAMERA)) {
                        ActivityCompat.requestPermissions(cordova.getActivity(),
                                new String[]{Manifest.permission.CAMERA},
                                100);
                    } else if (ActivityCompat.shouldShowRequestPermissionRationale(cordova.getActivity(),
                            Manifest.permission.RECORD_AUDIO)) {
                        ActivityCompat.requestPermissions(cordova.getActivity(),
                                new String[]{Manifest.permission.RECORD_AUDIO},
                                100);
                    } else if (ActivityCompat.shouldShowRequestPermissionRationale(cordova.getActivity(),
                            Manifest.permission.MODIFY_AUDIO_SETTINGS)) {
                        ActivityCompat.requestPermissions(cordova.getActivity(),
                                new String[]{Manifest.permission.MODIFY_AUDIO_SETTINGS},
                                100);
                    } else {
                        ActivityCompat.requestPermissions(cordova.getActivity(),
                                new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO, Manifest.permission.MODIFY_AUDIO_SETTINGS},
                                100);
                    }
                }
                return true;
            } else if (action != null && action.equalsIgnoreCase("missedCall")) {
                mMissedCall = true;
                endCall(action);
            } else if (ACTION_ENDCALL.equalsIgnoreCase(action)) {
                if (args != null) {
                    String object = args.getString(0);
                    mMissedCall = true;
                    endCall(object);
                } else
                    disconnectCall();
            } else if (action == null || action.equalsIgnoreCase("null")) {
                endCall(null);
            } else
                endCall(action);
            callbackContext.error("Invalid action");
            return false;
        } catch (Exception e) {
            System.err.println("Exception: " + e.getMessage());
            callbackContext.error(e.getMessage());
            return false;
        }
    }

    /**
     * shows low balance warning
     */
    private void showLowBalanceWarning(final boolean isPro, final String amount) {
        playAudio(cordova.getActivity(), "alert_asterisk_13.mp3");
        cordova.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (isPro && txt_low_balance != null) {
                        txt_low_balance.setText(Html.fromHtml("<font color=\"#ffffff\">" + "Warning. User does not meet your minimum credit balance " + "</font>"));
                        layout_low_credit.setVisibility(View.VISIBLE);

                        if (!isConnected) {
                            RelativeLayout.LayoutParams parma = (RelativeLayout.LayoutParams) layout_low_credit.getLayoutParams();

                            DisplayMetrics metrics = cordova.getActivity().getResources().getDisplayMetrics();
                            int screen_height = metrics.heightPixels;
                            int top_marging = (int) (screen_height * 0.4);
                            int bottom_marging = (int) (screen_height * 0.30);

                            parma.setMargins(0, top_marging, 0, bottom_marging);
                        }

                    } else if (txt_low_balance != null) {
                        txt_low_balance.setText(Html.fromHtml("<font color=\"#ffffff\">" + "LOW CREDIT WARNING..." + "</font>" + "<font color=\"#F0AF32\">" + amount));
                        layout_low_credit.setVisibility(View.VISIBLE);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });
    }

    /**
     * shows credit dialog to send
     * money from one dialog to another
     */
    private void showTipDialog() {
        try {

            layout_low_credit.setVisibility(View.INVISIBLE);
            layout_tip_send_receive.setVisibility(View.INVISIBLE);
//            layout_send_tip.setVisibility(View.VISIBLE);

            cordova.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // custom dialog
                    // custom dialog
                    if (dialogTip != null && dialogTip.isShowing())
                        dialogTip.dismiss();

                    dialogTip = new Dialog(cordova.getActivity());
                    dialogTip.setContentView(R.layout.send_tip);
                    dialogTip.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
                    WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
                    lp.copyFrom(dialogTip.getWindow().getAttributes());
                    DisplayMetrics displaymetrics = new DisplayMetrics();
                    cordova.getActivity().getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
                    int height = displaymetrics.heightPixels;
                    int width = displaymetrics.widthPixels;


                    layout_close = (RelativeLayout) dialogTip.findViewById(R.id.layout_close);

                    layout_addmore = (LinearLayout) dialogTip.findViewById(R.id.layout_addmore);
                    btn_sendtip = (Button) dialogTip.findViewById(R.id.btn_sendtip);
                    btn_ten_dollar = (Button) dialogTip.findViewById(R.id.btn_ten_dollar);
                    btn_twenty_dollar = (Button) dialogTip.findViewById(R.id.btn_twenty_dollar);
                    btn_fourty_dollar = (Button) dialogTip.findViewById(R.id.btn_fourty_dollar);
                    btn_sixty_dollar = (Button) dialogTip.findViewById(R.id.btn_sixty_dollar);
                    edt_tipamount = (EditText) dialogTip.findViewById(R.id.edt_tipamount);
                    layout_progress_tip = (LinearLayout) dialogTip.findViewById(R.id.layout_progress_tip);
                    layout_others = (RelativeLayout) dialogTip.findViewById(R.id.layout_others);
                    txt_creditbal = (TextView) dialogTip.findViewById(R.id.txt_creditbal);
                    btn_lowbal = (Button) dialogTip.findViewById(R.id.btn_lowbal);

                    layout_close.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialogTip.dismiss();
                        }
                    });
                    layout_addmore.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialogTip.dismiss();
                            showAddAmountDialog();
                        }
                    });
                    btn_sendtip.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            try {
                                InputMethodManager imm = (InputMethodManager) cordova.
                                        getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                                imm.hideSoftInputFromWindow(edt_tipamount.getWindowToken(), 0);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            String tipAmount = edt_tipamount.getText().toString();
                            int tipvalue = 0;
                            if (!tipAmount.isEmpty()) {
                                tipvalue = Integer.parseInt(tipAmount);
                                if (tipvalue > 0) {
                                    if (!tipAmount.isEmpty()) {
                                        sendCreditTip(tipAmount, true);
                                    } else
                                        edt_tipamount.setError("Please add amount greater than zero");
                                } else {
                                    btn_lowbal.setText(cordova.getActivity().getString(R.string.please_add_greater_value));
                                    btn_lowbal.setVisibility(View.VISIBLE);

                                    Timer timer = new Timer();
                                    timer.schedule(new TimerTask() {
                                        public void run() {
                                            cordova.getActivity().runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    btn_lowbal.setVisibility(View.INVISIBLE);
                                                }
                                            });
                                        }
                                    }, 2000);
                                }
                            } else
                                edt_tipamount.setError("enter tip amount");


                        }
                    });
                    btn_ten_dollar.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            sendCreditTip(Constants.TEN_DOLLARS, true);
                        }
                    });
                    btn_twenty_dollar.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            sendCreditTip(Constants.TWENTY_DOLLARS, true);
                        }
                    });
                    btn_fourty_dollar.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            sendCreditTip(Constants.FOURTY_DOLLARS, true);
                        }
                    });
                    btn_sixty_dollar.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            sendCreditTip(Constants.SIXTY_DOLLARS, true);
                        }
                    });


                    lp.width = (int) (width * 0.8);
                    lp.height = (int) (height * 0.8);
                    dialogTip.show();
                    dialogTip.getWindow().setAttributes(lp);
                    dialogTip.show();
                    updateUserBalance(mUserBalance);
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * add more credit
     */
    private void addMoreCredit() {

    }


    /**
     * sennds amount to different users
     *
     * @param amount to be sent to other users
     * @param isTip  if it is a tip sendCreditTipor a credit
     */
    private void sendCreditTip(final String amount, final boolean isTip) {
        try {

            if (!isTip) {
                try {
                    if (dialogAddAmount != null && dialogAddAmount.isShowing())
                        dialogAddAmount.dismiss();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            cordova.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        float tipamount = Float.parseFloat(amount);
                        float currentbal = Float.parseFloat(mUserBalance);
                        if (!amount.isEmpty()) {
                            if (currentbal < tipamount && isTip) {
                                if (isTip) {
                                    btn_lowbal.setText(cordova.getActivity().getString(R.string.you_must));
                                    btn_lowbal.setVisibility(View.VISIBLE);
                                    Timer timer = new Timer();
                                    timer.schedule(new TimerTask() {
                                        public void run() {
                                            cordova.getActivity().runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    btn_lowbal.setVisibility(View.INVISIBLE);
                                                }
                                            });
                                        }
                                    }, 2000);
                                }

                            } else {
                                if (isTip) {
                                    layout_progress_tip.setVisibility(View.VISIBLE);
                                    layout_others.setVisibility(View.GONE);
                                } else {
                                    layout_credit_btns.setVisibility(View.INVISIBLE);
                                    layout_progress.setVisibility(View.VISIBLE);
                                }

                                JSONObject sendTipJson = new JSONObject();
                                JSONObject dataJson = new JSONObject();
                                try {
                                    if (isTip)
                                        dataJson.put("type", "tip");
                                    else
                                        dataJson.put("type", "credit");

                                    int tip_amt = Integer.parseInt(amount);
                                    dataJson.put("amount", "" + tip_amt);
                                    sendTipJson.put("status", "transaction");
                                    sendTipJson.put("data", dataJson);
                                    Log.e(TAG, "" + sendTipJson.toString());
                                    mCallBackContext.successMessage(sendTipJson);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        } else if (tipamount < 1) {
                            btn_lowbal.setVisibility(View.VISIBLE);
                            Timer timer = new Timer();
                            timer.schedule(new TimerTask() {
                                public void run() {
                                    cordova.getActivity().runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            btn_lowbal.setVisibility(View.INVISIBLE);
                                        }
                                    });
                                }
                            }, 2000);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }


                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * shows amount to be added
     */
    private void showAddAmountDialog() {
        try {

            layout_low_credit.setVisibility(View.INVISIBLE);
            layout_tip_send_receive.setVisibility(View.INVISIBLE);
//            layout_send_tip.setVisibility(View.VISIBLE);

            cordova.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    if (dialogAddAmount != null && dialogAddAmount.isShowing())
                        dialogAddAmount.dismiss();

                    dialogAddAmount = new Dialog(cordova.getActivity());
                    dialogAddAmount.setContentView(R.layout.buy_credit);
                    dialogAddAmount.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
                    WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
                    lp.copyFrom(dialogAddAmount.getWindow().getAttributes());
                    DisplayMetrics displaymetrics = new DisplayMetrics();
                    cordova.getActivity().getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
                    int height = displaymetrics.heightPixels;
                    int width = displaymetrics.widthPixels;


                    layout_close_add_credit = (RelativeLayout) dialogAddAmount.findViewById(R.id.layout_close_add_credit);


                    btn_buy_ten = (Button) dialogAddAmount.findViewById(R.id.btn_buy_ten);
                    btn_buy_twetnty = (Button) dialogAddAmount.findViewById(R.id.btn_buy_twetnty);
                    btn_buy_fourty = (Button) dialogAddAmount.findViewById(R.id.btn_buy_fourty);
                    btn_buy_sixty = (Button) dialogAddAmount.findViewById(R.id.btn_buy_sixty);
                    layout_progress = (LinearLayout) dialogAddAmount.findViewById(R.id.layout_progress);
                    layout_credit_btns = (LinearLayout) dialogAddAmount.findViewById(R.id.layout_credit_btns);
                    txt_creditbal = (TextView) dialogAddAmount.findViewById(R.id.txt_creditbal);

                    layout_close_add_credit.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialogAddAmount.dismiss();
                        }
                    });
                    btn_buy_ten.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            sendCreditTip(Constants.TEN_DOLLARS, false);
                        }
                    });
                    btn_buy_twetnty.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            sendCreditTip(Constants.TWENTY_DOLLARS, false);
                        }
                    });
                    btn_buy_fourty.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            sendCreditTip(Constants.FOURTY_DOLLARS, false);
                        }
                    });
                    btn_buy_sixty.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            sendCreditTip(Constants.SIXTY_DOLLARS, false);
                        }
                    });


                    lp.width = (int) (width * 0.8);
                    lp.height = (int) (height * 0.8);
                    dialogAddAmount.show();
                    dialogAddAmount.getWindow().setAttributes(lp);
                    dialogAddAmount.show();
                    updateUserBalance(mUserBalance);
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * shows dialog on screen if user has received or send any tip
     *
     * @param amount     to be sent or received
     * @param isReceived or sent
     */
    private void showTipSendReceive(final String amount, final boolean isReceived) {
        try {
            if (txt_tipsent == null)
                return;
            cordova.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (isReceived) {
                        txt_tipsent.setText(cordova.getActivity().
                                getString(R.string.tip_received_value, amount));
                        playAudio(cordova.getActivity(), "Attack_MetalBlip01.mp3");
                    } else
                        txt_tipsent.setText(cordova.getActivity().
                                getString(R.string.tip_sent_value, amount));
                }
            });


            cordova.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    layout_tip_send_receive.setVisibility(View.VISIBLE);
                }
            });
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                public void run() {

                    cordova.getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            layout_tip_send_receive.setVisibility(View.INVISIBLE);
                        }
                    });
                }
            }, 4000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * updates user balance
     *
     * @param mUserBalance is the credit balance of the user
     */
    private void updateUserBalance(final String mUserBalance) {
        try {
            cordova.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (dialogAddAmount != null && dialogAddAmount.isShowing() &&
                            txt_creditbal != null) {
                        txt_creditbal.setText(Html.fromHtml("<font color=\"#c5c5c5\">" + "You have " + "</font>" + "<font color=\"#F0AF32\">" + mUserBalance + "</font>" + "<font color=\"#c5c5c5\">" + " credits" + "</font>"));
                    } else if (dialogTip != null && dialogTip.isShowing() &&
                            txt_creditbal != null) {
                        txt_creditbal.setText(Html.fromHtml("<font color=\"#c5c5c5\">" + "You have " + "</font>" + "<font color=\"#F0AF32\">" + mUserBalance + "</font>" + "<font color=\"#c5c5c5\">" + " credits" + "</font>"));
                    } else if (dialogCreditSuccess != null && dialogCreditSuccess.isShowing()
                            && txt_creditbal != null) {
                        txt_creditbal.setText(Html.fromHtml("<font color=\"#c5c5c5\">" + "You have " + "</font>" + "<font color=\"#F0AF32\">" + mUserBalance + "</font>" + "<font color=\"#c5c5c5\">" + " credits" + "</font>"));
                    }
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * calculates band-width speed
     */
    private void getBandWidthSpeed(final SpeedTestListener speedTestListener) {
        SpeedTestSocket speedTestSocket = new SpeedTestSocket();
        speedTestSocket.addSpeedTestListener(new ISpeedTestListener() {
            @Override
            public void onDownloadPacketsReceived(final long packetSize, final float transferRateBitPerSeconds, final
            float transferRateOctetPerSeconds) {
                float current_speed = (transferRateBitPerSeconds / Constants.VALUE_PER_SECONDS);
                if (current_speed < Constants.MIN) {
                    speedTestListener.onDownloadComplete(Constants.LOW);
                } else if (current_speed > Constants.MIN && current_speed < Constants.MAX) {
                    speedTestListener.onDownloadComplete(Constants.MEDIUM);
                } else if (current_speed > Constants.MAX) {
                    speedTestListener.onDownloadComplete(Constants.HIGH);
                }
//                LogUtils.logFinishedTask(SpeedTestMode.DOWNLOAD, packetSize, transferRateBitPerSeconds,
//                        transferRateOctetPerSeconds, true);

            }

            @Override
            public void onDownloadError(final SpeedTestError speedTestError, final String errorMessage) {
//                if (true) {
                speedTestListener.onDownloadFailed();
                LOG.e(TAG, "Download error " + speedTestError + " : " + errorMessage);
//                }
            }

            @Override
            public void onUploadPacketsReceived(final long packetSize, final float transferRateBitPerSeconds, final
            float transferRateOctetPerSeconds) {

//                LogUtils.logFinishedTask(SpeedTestMode.UPLOAD, packetSize, transferRateBitPerSeconds,
//                        transferRateOctetPerSeconds, true);

                float current_speed = (transferRateBitPerSeconds / Constants.VALUE_PER_SECONDS);
                if (current_speed < Constants.MIN) {
                    speedTestListener.onDownloadComplete(Constants.LOW);
                } else if (current_speed > Constants.MIN && current_speed < Constants.MAX) {
                    speedTestListener.onDownloadComplete(Constants.MEDIUM);
                } else if (current_speed > Constants.MAX) {
                    speedTestListener.onDownloadComplete(Constants.HIGH);
                }

            }

            @Override
            public void onUploadError(final SpeedTestError speedTestError, final String errorMessage) {
//                if (true) {
                Log.e(TAG, "Upload error " + speedTestError + " : " + errorMessage);
//                }
            }

            @Override
            public void onDownloadProgress(final float percent, final SpeedTestReport downloadReport) {

//                LogUtils.logSpeedTestReport(downloadReport, LOGGER, true);
            }

            @Override
            public void onUploadProgress(final float percent, final SpeedTestReport uploadReport) {

//                LogUtils.logSpeedTestReport(uploadReport, LOGGER, true);
            }

        });


        speedTestSocket.startDownload(Constants.SPEED_TEST_SERVER_HOST,
                Constants.SPEED_TEST_SERVER_PORT, Constants.SPEED_TEST_SERVER_URI_DL);
    }


    /**
     * shows credit added success message
     *
     * @param amount current user balance
     */
    private void showCreditSuccessDialog(final String amount) {
        try {

            layout_low_credit.setVisibility(View.INVISIBLE);
            layout_tip_send_receive.setVisibility(View.INVISIBLE);
//            layout_send_tip.setVisibility(View.VISIBLE);

            cordova.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    dialogCreditSuccess = new Dialog(cordova.getActivity());
                    dialogCreditSuccess.setContentView(R.layout.credit_added);
                    dialogCreditSuccess.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
                    WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
                    lp.copyFrom(dialogCreditSuccess.getWindow().getAttributes());
                    DisplayMetrics displaymetrics = new DisplayMetrics();
                    cordova.getActivity().getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
                    int height = displaymetrics.heightPixels;
                    int width = displaymetrics.widthPixels;

                    layout_close = (RelativeLayout) dialogCreditSuccess.findViewById(R.id.layout_close);
                    layout_addmore = (LinearLayout) dialogCreditSuccess.findViewById(R.id.layout_addmore);
                    txt_creditbal = (TextView) dialogCreditSuccess.findViewById(R.id.txt_creditbal);

                    layout_close.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialogCreditSuccess.dismiss();
                        }
                    });

                    layout_addmore.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialogCreditSuccess.dismiss();
                            showAddAmountDialog();
                        }
                    });

                    lp.width = (int) (width * 0.8);
                    lp.height = (int) (height * 0.8);
                    dialogCreditSuccess.show();
                    dialogCreditSuccess.getWindow().setAttributes(lp);
                    dialogCreditSuccess.show();
                    updateUserBalance(mUserBalance);
                    txt_creditbal.setText(Html.fromHtml("<font color=\"#c5c5c5\">" + "You have " + "</font>" + "<font color=\"#F0AF32\">" + amount + "</font>" + "<font color=\"#c5c5c5\">" + " credits" + "</font>"));
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * checks conneciton type
     */
    private String getNetworkInfo() {
        String networkType = "unknown";
        try {

            NetworkInfo type = Connectivity.getNetworkInfo(cordova.getActivity());
            Log.e(TAG, "" + type);
            if (!type.getTypeName().equalsIgnoreCase("WIFI")) {
                networkType = Connectivity.getNetworkClass(cordova.getActivity());
            } else
                networkType = "wifi";

        } catch (Exception e) {
            e.printStackTrace();
        }

        return networkType;
    }


    /**
     * plays audio from assets.
     *
     * @param context context of the application
     * @param file    name of the file to be played
     */
    private void playAudio(Context context, String file) {
        try {
            AssetFileDescriptor afd = context.getAssets().openFd(file);

            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(
                    afd.getFileDescriptor(),
                    afd.getStartOffset(),
                    afd.getLength()
            );
            afd.close();
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * credit added re-initiate the call
     */
    private void reconnectOnCreditAdd() {
        try {

//            cordova.getActivity().runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    mViewGroup.removeView(mCallView);
//                }
//            });

            cordova.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        if (dialogAddAmount != null && dialogAddAmount.isShowing())
                            dialogAddAmount.dismiss();
                    } catch (Exception exp) {
                        exp.printStackTrace();
                    }
                }
            });

            reconnect = true;
            callBean.setIsAbleToCall("true");
//            initCall(callBean, QUALITY);
            if (sessonToken != null)
                mSession.connect(sessonToken);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void sendSignal(String type, String data) {
        try {
            if (mSession != null && SESSION_STARTED) {
                //mSession.sendSignal("key", data);
                //Log.e("sendSignal Check", key);
                JSONObject json = new JSONObject();
                json.put(SIGNAL_MESSAGE, data);
                //mSession.sendSignal(key, data);
                mSession.sendSignal(type, data);
//                mCallBackContext.successMessage(json);
            } else {

                JSONObject json = getJson(Constants.ERROR_ON_SIGNAL_SEND, ERROR);
                mCallBackContext.error(json);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            JSONObject json = getJson(Constants.ERROR_ON_SIGNAL_SEND, ERROR);
            mCallBackContext.error(json);
        }
    }

}