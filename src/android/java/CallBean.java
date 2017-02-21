
package com.evontech.VideoPlugin;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class CallBean {

    @SerializedName("ApiKey")
    @Expose
    private String apiKey;
    @SerializedName("SessionId")
    @Expose
    private String sessionId;
    @SerializedName("Token")
    @Expose
    private String token;
    @SerializedName("UserType")
    @Expose
    private String userType;
    @SerializedName("IsAbleToCall")
    @Expose
    private String isAbleToCall;
    @SerializedName("ProfileImage")
    @Expose
    private String profileImage;
    @SerializedName("CallPerMinute")
    @Expose
    private String callPerMinute;
    @SerializedName("Amount")
    @Expose
    private String amount;
    @SerializedName("UserName")
    @Expose
    private String userName;
    @SerializedName("isReceiverInit")
    @Expose
    private String isReceiverInit;

    @SerializedName("proCharge")
    @Expose
    private String proCharge;
    /**
     * 
     * @return
     *     The apiKey
     */
    public String getApiKey() {
        return apiKey;
    }

    /**
     * 
     * @param apiKey
     *     The ApiKey
     */
    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    /**
     * 
     * @return
     *     The sessionId
     */
    public String getSessionId() {
        return sessionId;
    }

    /**
     * 
     * @param sessionId
     *     The SessionId
     */
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    /**
     * 
     * @return
     *     The token
     */
    public String getToken() {
        return token;
    }

    /**
     * 
     * @param token
     *     The Token
     */
    public void setToken(String token) {
        this.token = token;
    }

    /**
     * 
     * @return
     *     The userType
     */
    public String getUserType() {
        return userType;
    }

    /**
     * 
     * @param userType
     *     The UserType
     */
    public void setUserType(String userType) {
        this.userType = userType;
    }

    /**
     * 
     * @return
     *     The isAbleToCall
     */
    public String getIsAbleToCall() {
        return isAbleToCall;
    }

    /**
     * 
     * @param isAbleToCall
     *     The IsAbleToCall
     */
    public void setIsAbleToCall(String isAbleToCall) {
        this.isAbleToCall = isAbleToCall;
    }

    /**
     * 
     * @return
     *     The profileImage
     */
    public String getProfileImage() {
        return profileImage;
    }

    /**
     * 
     * @param profileImage
     *     The ProfileImage
     */
    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    /**
     * 
     * @return
     *     The callPerMinute
     */
    public String getCallPerMinute() {
        return callPerMinute;
    }

    /**
     * 
     * @param callPerMinute
     *     The CallPerMinute
     */
    public void setCallPerMinute(String callPerMinute) {
        this.callPerMinute = callPerMinute;
    }

    /**
     * 
     * @return
     *     The amount
     */
    public String getAmount() {
        return amount;
    }

    /**
     * 
     * @param amount
     *     The Amount
     */
    public void setAmount(String amount) {
        this.amount = amount;
    }

    /**
     * 
     * @return
     *     The userName
     */
    public String getUserName() {
        return userName;
    }

    /**
     * 
     * @param userName
     *     The UserName
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }

    /**
     * 
     * @return
     *     The isReceiverInit
     */
    public String getIsReceiverInit() {
        return isReceiverInit;
    }

    /**
     * 
     * @param isReceiverInit
     *     The isReceiverInit
     */
    public void setIsReceiverInit(String isReceiverInit) {
        this.isReceiverInit = isReceiverInit;
    }

    public String getProCharge() {
        return proCharge;
    }

    public void setProCharge(String proCharge) {
        this.proCharge = proCharge;
    }
}