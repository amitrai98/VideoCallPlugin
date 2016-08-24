#import "VideoPlugin.h"
#import <CoreTelephony/CTTelephonyNetworkInfo.h>
#import <NewRelicAgent/NewRelic.h>
#import "NetworkDownloadBandwidthClass.h"
#import "Reachability.h"
#import "VideoCallingDataModel.h"
#import "CallingView.h"
#import "VideoCallHelper.h"
#import "UserTipView.h"
#import "Constants.h"

@interface VideoPlugin()<CallingViewDelegates,TransactionDelegate>
{
    NSString *networkType;
    enum NetworkBandwidth callSpeed;
    Reachability *reachability;
    CallingView *callingView;
    VideoCallingDataModel *videoDataModel;
    UserTipView *overlay;
    BOOL callCanEnded;
}
@end

@implementation VideoPlugin

@synthesize allButtonVisible;

#pragma mark - Public methods -

- (void)initializeVideoCalling:(CDVInvokedUrlCommand*)command{
    NR_TRACE_METHOD_START(0);
    // Check command.arguments here.
    
    @try {
        NSString* callbackId = [command callbackId];
        callBack = callbackId;
        NSString *jsonString = [[command arguments] objectAtIndex:0];
        jsonString = [jsonString stringByReplacingOccurrencesOfString:@" " withString:@""];
        if(![self dataInitialization:jsonString]){
            return;
        }
        
        [self.commandDelegate runInBackground:^{
            
            [UIApplication sharedApplication].idleTimerDisabled = YES;
            SubscribeToSelf  = NO;
            [self initializeClass];
            
            
            if ([videoDataModel.callPerMinute floatValue]==0) {
                [self sendMessagesToPlugin:receiverInitMsg status:success];
            }
            else{
                [self sendMessagesToPlugin:senderInitMsg status:success];
            }
            
            if (publisher) {
                NSError *err;
                [session unpublish:publisher error:&err];
                [session disconnect:&err];
            }
            
            [[[NetworkDownloadBandwidthClass alloc] init] testDownloadSpeedWithTimout:5.0 completionHandler:^(CGFloat kilobytesPerSecond,enum NetworkBandwidth netSpeed, NSError * _Nullable error) {
                callSpeed = netSpeed;
                if (videoDataModel.isAbleToCall) {
                    [self startConnecting];
                     callCanEnded = true;
                }
                else{
                    [self showCreditViewandBtnEnabled:NO];
                }
            }];
        }];
    } @catch (NSException *exception) {
        [self sendMessagesToPlugin:[self failureResponse:exception.reason] status:failure];
    }
   
    NR_TRACE_METHOD_STOP;
    
}

- (void)showCreditViewandBtnEnabled:(BOOL)enabled{
    dispatch_async(dispatch_get_main_queue(), ^{
        // code here
        overlay = [[UserTipView alloc] initializeUserTipView:self.view];
         [overlay setUpLayoutAmount:[videoDataModel.amount floatValue] isTipView:NO tipButtonEnabled:[callingView callIsDisconnectedAfterConnecting]];
        overlay.transactionDelgate = self;
    });
    
}

-(void)showLowBalanceWarning:(CDVInvokedUrlCommand*)command{
    NR_TRACE_METHOD_START(0);
    @try {
        NSString *message = [[command arguments] objectAtIndex:0];
        [callingView showLowWarningView:message];
    } @catch (NSException *exception) {
        [self sendMessagesToPlugin:[self failureResponse:exception.reason] status:failure];
    }
    NR_TRACE_METHOD_STOP;
}

-(void)getUserBalance:(CDVInvokedUrlCommand*)command{
    NR_TRACE_METHOD_START(0);
    @try {
        NSString *userBalance = [[command arguments] objectAtIndex:0];
        videoDataModel.amount = userBalance;
        if ([videoDataModel.amount floatValue] < 0.1f && callCanEnded) {
            [self callEnded];
        }
    } @catch (NSException *exception) {
        [self sendMessagesToPlugin:[self failureResponse:exception.reason] status:failure];
    }
    NR_TRACE_METHOD_STOP;
}
-(void)receivedResponseFromAPI:(CDVInvokedUrlCommand*)command{
    NR_TRACE_METHOD_START(0);
    @try {
        NSString *responseType = [[command arguments] objectAtIndex:0];
        NSString *status = [[command arguments] objectAtIndex:1];
        NSString *userBalance = [[command arguments] objectAtIndex:2];
        
        if ([responseType isEqualToString:credit] && [status isEqualToString:success]) {
            videoDataModel.amount = userBalance;
                [callingView hideLowWarningView];
                overlay = [[UserTipView alloc] initializeUserTipView:self.view];
                [overlay setUpLayoutAmount:[videoDataModel.amount floatValue] isTipView:NO tipButtonEnabled:[callingView callIsDisconnectedAfterConnecting]];
                if (![callingView callIsDisconnectedAfterConnecting] && !callCanEnded) {
                    [self startConnecting];
                    callCanEnded = true;
                }
                overlay.transactionDelgate = self;
            [overlay creditSuccessfullyAdded:[userBalance floatValue]];
        }
        else if([responseType isEqualToString:tip] && [status isEqualToString:success]){
            [overlay removeFromSuperview];
            [callingView showTipViewWithAmount:userBalance sent:YES];
        }
        else{
            [[[UIAlertView alloc] initWithTitle:@"Ringhop" message:@"Transaction Failed" delegate:nil cancelButtonTitle:@"Dismiss" otherButtonTitles:nil] show];
        }
    } @catch (NSException *exception) {
        [self sendMessagesToPlugin:[self failureResponse:exception.reason] status:failure];
    }
    NR_TRACE_METHOD_STOP;
}

-(void)tipReceived:(CDVInvokedUrlCommand*)command{
    NR_TRACE_METHOD_START(0);
    @try {
        NSString *tipAmount = [[command arguments] objectAtIndex:0];
        [callingView showTipViewWithAmount:tipAmount sent:NO];
    } @catch (NSException *exception) {
        [self sendMessagesToPlugin:[self failureResponse:exception.reason] status:failure];
    }
    NR_TRACE_METHOD_STOP;
}

-(BOOL)dataInitialization:(NSString *)jsonString{
    NSData *data = [jsonString dataUsingEncoding:NSUTF8StringEncoding];
    NSError *err;
    NSDictionary *json = [NSJSONSerialization JSONObjectWithData:data options:NSJSONReadingMutableContainers error:&err];
    
    videoDataModel = [[VideoCallingDataModel alloc] initializeWithDictionary:json];
    if (![videoDataModel checkForValidModel]) {
        [self sendMessagesToPlugin:jsonError status:failure];
        return NO;
    }
    return YES;
}

- (NSDictionary *)failureResponse:(NSString *)error{
    
    return @{@"networkType":networkType,
             @"error":error};
}

-(void)initializeClass{
    dispatch_async(dispatch_get_main_queue(), ^{
        self.view  = self.webView.superview;
        callingView = [[CallingView alloc] initializeCallingView:self.view];
        // Pro
        if ([videoDataModel.userType isEqualToString:@"Pro"]) {
            [callingView hideTipButton];
        }
        [callingView showUserName:videoDataModel.userName];
        callingView.callingViewDelegate = self;
         [[VideoCallHelper sharedInstance] showCallInitializeView:self.view imageUrl:videoDataModel.profileImage rates:videoDataModel.callPerMinute userType:videoDataModel.userType];
        [[VideoCallHelper sharedInstance] identifyTheChangedNetwork:^(NSString *type) {
            networkType = type;
        }];
    });
}

-(void)startConnecting{
    session = [[OTSession alloc] initWithApiKey:videoDataModel.apikey sessionId:videoDataModel.sessionId delegate:self];
    [self doConnect];
}

#pragma mark - Calling View Delegates
- (void)callEnded{
    [self endCalling:(id)[[UIButton alloc] init]];
}

- (void)callMuted:(BOOL)mute{
    publisher.publishAudio = mute;
}

- (void)videoCallSwitch:(BOOL)showVideo{
    publisher.publishVideo = showVideo;
    [self checkAndAddVideoViews];
}

- (void)cameraRotated{
    if (publisher.cameraPosition == AVCaptureDevicePositionFront) {
        publisher.cameraPosition = AVCaptureDevicePositionBack;
    } else {
        publisher.cameraPosition = AVCaptureDevicePositionFront;
    }
}

- (void)showTipView{
    [self sendMessagesToPlugin:getAmount status:transaction];
    overlay = [[UserTipView alloc] initializeUserTipView:self.view];
    [overlay setUpLayoutAmount:[videoDataModel.amount floatValue] isTipView:YES tipButtonEnabled:[callingView callIsDisconnectedAfterConnecting]];
    overlay.transactionDelgate = self;
}

-(void)showAddCreditView{
    [self sendMessagesToPlugin:getAmount status:transaction];
    overlay = [[UserTipView alloc] initializeUserTipView:self.view];
    [overlay setUpLayoutAmount:[videoDataModel.amount floatValue] isTipView:NO tipButtonEnabled:[callingView callIsDisconnectedAfterConnecting]];
    overlay.transactionDelgate = self;
}

#pragma mark - Overlay (tip and credit view) Delagate
-(void)transactionNeedToBeDone:(TransactionType)type amount:(int)amount{
    NSDictionary *dictionary = [[NSDictionary alloc] init];
    switch (type) {
        case TipSent:
        dictionary = @{@"type":tip,
                       @"amount":[NSString stringWithFormat:@"%d",amount]};
        break;
        case CreditsBuy:
        dictionary = @{@"type":credit,
                       @"amount":[NSString stringWithFormat:@"%d",amount]};
        
    }
    [self sendMessagesToPlugin:dictionary status:transaction];
    [overlay removeFromSuperview];
}

-(void)endCalling:(CDVInvokedUrlCommand*)command{
    NR_TRACE_METHOD_START(0);
    @try {
        
        BOOL callEndedByUser = NO;
//         NSString *userEndedCall = nil;
//        if (command && [command isKindOfClass:[UIButton class]]){
//            callEndedByUser = YES;
//             userEndedCall = userEndHisCall;
//        }
        NSString *messageForPlugin = [self stopCall:callEndedByUser];
        if ([callingView callIsDisconnectedAfterConnecting]) {
            messageForPlugin = CALL_ENDED;
//            userEndedCall != nil ? [self sendMessagesToPlugin:userEndedCall status:success] : nil ;
        }
        else{
            messageForPlugin = CALL_ENDED_BY_USER;
        }
        [self removeAll];
        NSString *param;
        if (command && [command isKindOfClass:[CDVInvokedUrlCommand class]] && [[command arguments] count] >0 ){
            param = [[command arguments] objectAtIndex:0];
        }
        
        [self sendMessagesToPlugin:param!=nil?param:messageForPlugin status:success];
    } @catch (NSException *exception) {
        [self sendMessagesToPlugin:[self failureResponse:exception.reason] status:failure];
    }
    NR_TRACE_METHOD_STOP;
}

-(NSString *)stopCall:(BOOL)callEndedByUser{
    OTError* error = nil;
    [[VideoCallHelper sharedInstance]hideLoadingBar];
    [UIApplication sharedApplication].idleTimerDisabled = NO;
    [self doUnSubscribe];
    
    if (session.sessionConnectionStatus == OTSessionConnectionStatusConnected || session.sessionConnectionStatus == OTSessionConnectionStatusConnecting) {
        [session unpublish:publisher error:&error];
        [session disconnect:&error];
    }
    
    NSString *messageForPlugin;
    
    if (error) {
        messageForPlugin = [NSString stringWithFormat:@"StopCall ERROR %@",error.localizedDescription];
    }
    else{
        messageForPlugin = callEndedByUser==true?CALL_ENDED_BY_USER:SUCCESSFULL_DISCONNECTED;
    }
    
    return messageForPlugin;
}

-(void)removeAll{
    NR_TRACE_METHOD_START(0);
    @try {
        [[NSNotificationCenter defaultCenter] removeObserver:self];
    }
    @catch (NSException *exception) {
        
    }
    [[VideoCallHelper sharedInstance]hideLoadingBar];
    profileImage?[profileImage removeFromSuperview]:nil;
    publisher.view?[publisher.view removeFromSuperview]:nil;
    subscriber.view?[subscriber.view removeFromSuperview]:nil;
    [callingView removeAndStopTimers];
    callingView?[callingView removeFromSuperview]:nil;
    callingView = nil;
    overlay?[overlay removeFromSuperview]:nil;
    overlay = nil;
    NR_TRACE_METHOD_STOP;
}

-(void)checkAndAddVideoViews{
    [self removeBothViews];
    if(publisher.publishVideo && subscriber.subscribeToVideo && subscriber.stream.hasVideo){
        [self showBothVideosViews];
    }
    else if (publisher.publishVideo && subscriber.subscribeToVideo){
        [self showOnlyPublisher];
    }
    else if (subscriber.subscribeToVideo && subscriber.stream.hasVideo){
        [self showOnlySubsriber];
    }
    else{
        [self removeBothViews];
    }
}

-(void)showBothVideosViews{
    [callingView addToFullView:subscriber.view];
    [callingView addToSmallView:publisher.view];
}

-(void)showOnlyPublisher{
    [callingView addToFullView:publisher.view];
    [callingView addToSmallView:nil];
}

-(void)showOnlySubsriber{
    [callingView addToFullView:subscriber.view];
    [callingView addToSmallView:nil];
}

-(void)removeBothViews{
    [callingView addToFullView:nil];
    [callingView addToSmallView:nil];
}

// Send message to plugin in json form
-(void)sendMessagesToPlugin:(id)message status:(NSString *)status{
    
    NSDictionary *inJsonform = @{@"data":message,
                                 @"status":status
                                 };
    
    int callBackType;
    if ([status isEqualToString:failure]) {
        callBackType = CDVCommandStatus_ERROR;
    }else{
        callBackType = CDVCommandStatus_OK;
    }
    
    CDVPluginResult* result = [CDVPluginResult
                               resultWithStatus:callBackType
                               messageAsDictionary:inJsonform];
    [result setKeepCallback:[NSNumber numberWithBool:YES]];
    [self.commandDelegate sendPluginResult:result callbackId:callBack];
    
}


#pragma mark - Session Delegates -
//checked
-(void)doConnect{
    if(session){
        OTError *mayBeError;
        
        [session connectWithToken:videoDataModel.token error:&mayBeError];
        if(mayBeError){
            [self removeAll];
            [self sendMessagesToPlugin:[self failureResponse:[NSString stringWithFormat:@"SessionConnectError: %@",mayBeError.localizedDescription]] status:failure];
        }
        else{
            [self sendMessagesToPlugin:MAKING_CONNECTION status:success];
        }
    }
}

-(void)sessionDidConnect:(OTSession *)session{
    NR_TRACE_METHOD_START(0);
    @try{
        [self doPublish];
    } @catch (NSException *exception) {
        [self sendMessagesToPlugin:[self failureResponse:exception.reason] status:failure];
    }
    NR_TRACE_METHOD_STOP;
}

-(void)sessionDidDisconnect:(OTSession *)sessions{
    
    NR_TRACE_METHOD_START(0);
    @try{
        NSLog(@"Session disconnected = %@",sessions.sessionId);
        if(sessions.sessionConnectionStatus == OTSessionConnectionStatusConnected || sessions.sessionConnectionStatus == OTSessionConnectionStatusConnecting){
            [self endCalling:nil];
        }
        
    } @catch (NSException *exception) {
        [self sendMessagesToPlugin:[self failureResponse:exception.reason] status:failure];
    }
    
    NR_TRACE_METHOD_STOP;
    
}

-(void)session:(OTSession *)session streamCreated:(OTStream *)stream{
    NR_TRACE_METHOD_START(0);
    @try {
        if (!subscriber && !SubscribeToSelf) {
            [self doSubscribe:stream];
        }
    } @catch (NSException *exception) {
        [self sendMessagesToPlugin:[self failureResponse:exception.reason] status:failure];
    }
    NR_TRACE_METHOD_STOP;
}

-(void)session:(OTSession *)session streamDestroyed:(OTStream *)stream{
    NR_TRACE_METHOD_START(0);
    @try {
        if (subscriber.stream.streamId == stream.streamId) {
            [self doUnSubscribe];
        }
    } @catch (NSException *exception) {
        [self sendMessagesToPlugin:[self failureResponse:exception.reason] status:failure];
    }
    NR_TRACE_METHOD_STOP;
}

-(void)session:(OTSession *)session connectionCreated:(OTConnection *)connection{
    [self sendMessagesToPlugin:CONNECTION_CREATED status:success];
}

-(void)session:(OTSession *)session connectionDestroyed:(OTConnection *)connection{
    [self endCalling:nil];
    [self sendMessagesToPlugin:[self failureResponse:SESSION_CONNECTION_DESTROYED] status:failure];
}

-(void)session:(OTSession *)session didFailWithError:(OTError *)error{
    [self endCalling:nil];
    [self sendMessagesToPlugin:[self failureResponse:[NSString stringWithFormat:@"SessionDidFailWithError: %@",error.localizedDescription]] status:failure];
}

#pragma mark - Subscribers Delegates -

-(void)subscriberDidConnectToStream:(OTSubscriberKit *)subscriberr{
    NR_TRACE_METHOD_START(0);
    @try {
        [[VideoCallHelper sharedInstance] hideLoadingBar];
        
        NSURL *url = [NSURL URLWithString:videoDataModel.profileImage];
        NSData *imageData = [NSData dataWithContentsOfURL:url];
        
        screenRect = [[UIScreen mainScreen] bounds];
        //Now data is decoded. You can convert them to UIImage
        UIImage *image = [UIImage imageWithData:imageData];
        profileImage = [[UIImageView alloc] initWithFrame:CGRectMake(0, 58, screenRect.size.width, screenRect.size.height-58)];
        profileImage.image = image?image:[UIImage imageNamed:@"one"];
        profileImage.contentMode = UIViewContentModeCenter;
        [self.view addSubview:profileImage];
        [self.view bringSubviewToFront:callingView];
        
        [callingView addToFullView:subscriber.view];
        [self sendMessagesToPlugin:CALL_STARTED status:success];
    } @catch (NSException *exception) {
        [self sendMessagesToPlugin:[self failureResponse:exception.reason] status:failure];
    }
    NR_TRACE_METHOD_STOP;
}

-(void)subscriber:(OTSubscriberKit *)subscriber didFailWithError:(OTError *)error{
    NR_TRACE_METHOD_START(0);
    @try {
        [self endCalling:nil];
        [self removeAll];
        [self sendMessagesToPlugin:[self failureResponse:[NSString stringWithFormat:@"SubscriberDidFailWithError : %@",error.localizedDescription]] status:failure];
    } @catch (NSException *exception) {
        [self sendMessagesToPlugin:[self failureResponse:exception.reason] status:failure];
    }
    NR_TRACE_METHOD_STOP;
}

-(void)doSubscribe:(OTStream*)stream{
    if(session){
        subscriber = [[OTSubscriber alloc] initWithStream:stream delegate:self];
        
        OTError *mayBeError;
        [session subscribe:subscriber error:&mayBeError];
        if (mayBeError) {
            [self sendMessagesToPlugin:[self failureResponse:[NSString stringWithFormat:@"doSubscribeError: %@",mayBeError.localizedDescription]] status:failure];
        }
    }
}

-(void)doUnSubscribe{
    if(subscriber){
        OTError *mayBeError;
        [session unsubscribe:subscriber error:&mayBeError];
        
        if (mayBeError && mayBeError.code != 1112) {
            [self sendMessagesToPlugin:[self failureResponse:[NSString stringWithFormat:@"doUnSubscribeError: %@",mayBeError.localizedDescription]] status:failure];
        }
        [subscriber.view removeFromSuperview];
        subscriber = nil;
    }
}

- (void)subscriberVideoDisabled:(OTSubscriber *)subscriber
                         reason:(OTSubscriberVideoEventReason)reason
{
    [self checkAndAddVideoViews];
}
-(void)subscriberVideoEnabled:(OTSubscriberKit *)subscriber reason:(OTSubscriberVideoEventReason)reason{
    [self checkAndAddVideoViews];
}

#pragma mark - Publisher Delegates -

-(void)doPublish{
    switch (callSpeed) {
        case low:
        publisher = [[OTPublisher alloc] initWithDelegate:self
                                                     name:@"RingHop"
                                         cameraResolution:OTCameraCaptureResolutionLow
                                          cameraFrameRate:OTCameraCaptureFrameRate7FPS];
        break;
        case medium:
        publisher = [[OTPublisher alloc] initWithDelegate:self
                                                     name:@"RingHop"
                                         cameraResolution:OTCameraCaptureResolutionMedium
                                          cameraFrameRate:OTCameraCaptureFrameRate15FPS];
        publisher = [[OTPublisher alloc] initWithDelegate:self];
        break;
        case high:
        publisher = [[OTPublisher alloc] initWithDelegate:self];
        break;
    }
    
    OTError *mayBeError;
    if(session){
        [session publish:publisher error:&mayBeError];
        
        if(mayBeError){
            [self endCalling:nil];
            [self sendMessagesToPlugin:[self failureResponse:[NSString stringWithFormat:@"doPublishError: %@",mayBeError.localizedDescription]] status:failure];
        }
        else{
            [self sendMessagesToPlugin:START_PUBLISHING status:success];
            [callingView addToSmallView:publisher.view];
        }
    }
}

-(void)publisher:(OTPublisherKit *)publisher streamCreated:(OTStream *)stream{
    if (!subscriber && SubscribeToSelf) {
        [self doSubscribe:stream];
    }
}

-(void)publisher:(OTPublisherKit *)publisher streamDestroyed:(OTStream *)stream{
    if (subscriber.stream.streamId == stream.streamId) {
        [self doUnSubscribe];
    }
}

-(void)publisher:(OTPublisherKit *)publisher didFailWithError:(OTError *)error{
    [self endCalling:nil];
    [self removeAll];
    [self sendMessagesToPlugin:@"CallEndedDueToError" status:@"success"];
    [self sendMessagesToPlugin:[self failureResponse:[NSString stringWithFormat:@"publisherDidFailWithError: %@",error.localizedDescription]] status:failure];
}
@end