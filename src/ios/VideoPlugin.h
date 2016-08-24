#import <Cordova/CDV.h>
#import <OpenTok/OpenTok.h>
#import "JWGCircleCounter.h"

@interface VideoPlugin : CDVPlugin <UINavigationControllerDelegate,UIImagePickerControllerDelegate,OTSessionDelegate, OTSubscriberKitDelegate, OTPublisherDelegate,JWGCircleCounterDelegate>
{
//    NSString *apiKey;
//    NSString *sessionId;
//    NSString *sessionToken;
//    NSString *callPerMinute;
//    NSString *userBalance;
//    NSString *profileImageUrl;
    bool SubscribeToSelf;
    OTSession *session;
    OTPublisher *publisher;
    OTSubscriber *subscriber;
    NSString *callBack;
//    BOOL publisherVideoDisplay;
//    BOOL subscriberVideoDisplay;
    BOOL alwaysShowButton;
//    CGRect fullView;
//    CGRect smallView;
//    UIButton *endcall;
//    UIButton *videoCallSwitch;
//    UIButton *muteMic;
//    UIButton *cameraSwitch;
    CGRect screenRect;
//    UIView *loadingView;
//    UIView *priceView;
//    UILabel *duration;
//    NSTimer *timer;
//    int timeForCall;
//    CGFloat side;
    UIImageView *profileImage;
//    NSTimer *timerToHideButton;
//    UITapGestureRecognizer *singleFingerTap;
//    UITapGestureRecognizer *singleFingerTapP;
}
@property(assign,nonatomic) BOOL allButtonVisible;
@property (nonatomic,strong) UIView *view;
-(void)initializeVideoCalling:(CDVInvokedUrlCommand*)command;
-(void)endCalling:(CDVInvokedUrlCommand*)command;
-(void)showLowBalanceWarning:(CDVInvokedUrlCommand*)command;
-(void)getUserBalance:(CDVInvokedUrlCommand*)command;
-(void)receivedResponseFromAPI:(CDVInvokedUrlCommand*)command;
-(void)tipReceived:(CDVInvokedUrlCommand*)command;

@end