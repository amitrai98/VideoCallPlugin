//
//  VideoCallHelper.m
//  HelloCordova
//
//  Created by jagandeep on 7/4/16.
//
//

#import "VideoCallHelper.h"
#import "JWGCircleCounter.h"
#import <CoreTelephony/CTTelephonyNetworkInfo.h>
#import "Reachability.h"

@interface VideoCallHelper()<JWGCircleCounterDelegate>
{
    Reachability *reachability;
    NSString * networkType;
}
@end

@implementation VideoCallHelper

+ (instancetype)sharedInstance
{
    static dispatch_once_t onceQueue;
    static VideoCallHelper *__sharedClient = nil;
    
    dispatch_once(&onceQueue, ^{
        __sharedClient = [[self alloc] init];
    });
    return __sharedClient;
}

-(void)showCallInitializeView:(UIView *)superView imageUrl:(NSString *)imageUrl rates:(NSString *)rates userType:(NSString *)userType{
    CGRect screen = [[UIScreen mainScreen] bounds];
    loadingView = [[UIView alloc] initWithFrame:CGRectMake(screen.size.width/2 - 50, 90, 100, 150)];
    loadingView.backgroundColor = [UIColor colorWithRed:65.0/255 green:65.0/255 blue:65.0/255 alpha:1];
    
    
    UIImageView *profileImg = [[UIImageView alloc] initWithFrame:CGRectMake(5,5, 90, 100)];
    
    NSURL *url = [NSURL URLWithString:imageUrl];
    NSData *imageData = [NSData dataWithContentsOfURL:url];
    
    //Now data is decoded. You can convert them to UIImage
    UIImage *image = [UIImage imageWithData:imageData];
    
    profileImg.image = image?image:[UIImage imageNamed:@"one"];
    [loadingView addSubview:profileImg];
    
    JWGCircleCounter *loader = [[JWGCircleCounter alloc]initWithFrame:CGRectMake(12, 120, 18, 18)];
    loader.circleTimerWidth = 2.2;
    loader.delegate = self;
    loader.circleBackgroundColor = [UIColor clearColor];
    loader.circleColor = [UIColor colorWithRed:137.0/255 green:244.0/255 blue:43.0/255 alpha:1];
    [loader startWithSeconds:15];
    [loadingView addSubview:loader];
    
    UILabel *label = [[UILabel alloc] initWithFrame:CGRectMake(33, 120, 80, 18)];
    label.text = @"contacting...";
    label.textColor = [UIColor colorWithRed:137.0/255 green:244.0/255 blue:43.0/255 alpha:1];
    label.font = [UIFont fontWithName:@"HelveticaNeue-CondensedBold" size:12.0f];
    [loadingView addSubview:label];
    [superView addSubview:loadingView];
    [superView bringSubviewToFront:loadingView];
    if(![userType isEqualToString:@"Pro"]){
        [self addForProUser:superView rates:rates];
    }
}

- (void)addForProUser:(UIView *)superView rates:(NSString *)rates{
    CGRect screen = [[UIScreen mainScreen] bounds];
    priceView = [[UIView alloc] initWithFrame:CGRectMake(screen.size.width/2 - 100, 250, 200, 60)];
    priceView.backgroundColor = [UIColor colorWithRed:32.0/255 green:32.0/255 blue:32.0/255 alpha:0.5];
    priceView.layer.cornerRadius = 8.0;
    
    UILabel *labela = [[UILabel alloc] initWithFrame:CGRectMake(20, 10, 160, 40)];
    labela.text = [NSString stringWithFormat:VIDEO_CALL_MESSAGE,rates];
    labela.lineBreakMode = NSLineBreakByWordWrapping;
    labela.numberOfLines = 0;
    labela.textColor = [UIColor whiteColor];
    labela.textAlignment = NSTextAlignmentCenter;
    labela.font = [UIFont fontWithName:@"HelveticaNeue-CondensedBold" size:12.0f];
    [priceView addSubview:labela];
    
    if ([rates floatValue]!=0) {
        [superView addSubview:priceView];
    }

}

-(void)hideLoadingBar{
    [loadingView removeFromSuperview];
    [priceView removeFromSuperview];
}

- (void)circleCounterTimeDidExpire:(JWGCircleCounter *)circleCounter {
    [circleCounter startWithSeconds:10];
}

// To see logs in view
-(void)addViewToSeeLogs:(NSString *)log superView:(UIView *)superview{
    UITextView *logTextView;
    if (logView.superview == nil) {
        logView = [[UIView alloc]initWithFrame:CGRectMake(50, 50, 250, 250)];
        [logView setBackgroundColor:[UIColor blackColor]];
        
        UIButton *closeBtn = [[UIButton alloc] initWithFrame:CGRectMake(0, 0, 250, 50)];
        [closeBtn setTitle:@"Close" forState:UIControlStateNormal];
        [closeBtn addTarget:self action:@selector(close:) forControlEvents:UIControlEventTouchUpInside];
        [logView addSubview:closeBtn];
        
        UITextView *logTextView = [[UITextView alloc] initWithFrame:CGRectMake(0, 50, 250, 200)];
        [logTextView setUserInteractionEnabled:false];
        [logView addSubview:logTextView];
        
        [superview addSubview:logView];
        
    }
    
    logTextView.text = [NSString stringWithFormat:@"%@ \n %@",logTextView.text,log];
}

-(void)close:(UIButton *)closeBtn{
    [logView removeFromSuperview];
}

// To Check Device Is Locked or unlocked
-(void)registerforDeviceLockNotify:(void (^)(NSString*))callbackBlock
{
    @try {
        CFNotificationCenterAddObserver(CFNotificationCenterGetDarwinNotifyCenter(), //center
                                        NULL, // observer
                                        displayStatusChanged, // callback
                                        CFSTR("com.apple.springboard.lockcomplete"), // event name
                                        NULL, // object
                                        CFNotificationSuspensionBehaviorDeliverImmediately);
        
        CFNotificationCenterAddObserver(CFNotificationCenterGetDarwinNotifyCenter(), //center
                                        NULL, // observer
                                        displayStatusChanged, // callback
                                        CFSTR("com.apple.springboard.lockstate"), // event name
                                        NULL, // object
                                        CFNotificationSuspensionBehaviorDeliverImmediately);
    } @catch (NSException *exception) {
        callbackBlock(exception.reason);
        //
    }
}

//call back
static void displayStatusChanged(CFNotificationCenterRef center, void *observer, CFStringRef name, const void *object, CFDictionaryRef userInfo)
{
    // the "com.apple.springboard.lockcomplete" notification will always come after the "com.apple.springboard.lockstate" notification
    CFStringRef nameCFString = (CFStringRef)name;
    NSString *lockState = (__bridge NSString*)nameCFString;
    if([lockState isEqualToString:@"com.apple.springboard.lockcomplete"])
    {
        NSLog(@"DEVICE LOCKED");
        //Logic to disable the GPS
    }
    else
    {
        //Logic to enable the GPS
        [[NSNotificationCenter defaultCenter] postNotificationName:@"removeCallViews" object:nil];
    }
}


-(void)reachabilityDidChange:(id)object{
    [self identifyTheChangedNetwork:nil];
}

-(void)identifyTheChangedNetwork:(void (^)(NSString*))callbackBlock{
    
    //3G // Set your code here for cellular data
    CTTelephonyNetworkInfo *telephonyInfo = [CTTelephonyNetworkInfo new];
    networkType = telephonyInfo.currentRadioAccessTechnology;
    
    [NSNotificationCenter.defaultCenter addObserverForName:CTRadioAccessTechnologyDidChangeNotification
                                                    object:nil
                                                     queue:nil
                                                usingBlock:^(NSNotification *note)
     {
         networkType = telephonyInfo.currentRadioAccessTechnology;
     }];
    
    NetworkStatus status = [reachability currentReachabilityStatus];
    if (status == ReachableViaWiFi)
    {
        //WiFi
        networkType = @"WiFi";
    }
    else if (status == ReachableViaWWAN)
    {
        networkType = telephonyInfo.currentRadioAccessTechnology;
    }
    else if (status == NotReachable){
        networkType = @"Wifi";
    }
    callbackBlock(networkType);
    
}

-(void)checkNetworkChanged{
    
    reachability = [Reachability reachabilityForInternetConnection];
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(reachabilityDidChange:) name:kReachabilityChangedNotification object:nil];
    [reachability startNotifier];
    
    [self identifyTheChangedNetwork:false];
}

// Add dynamic constraint of view to superview
- (void)addCallingViewConstraints:(UIView *)view andSuperView:(UIView *)superView{
    //Trailing
    NSLayoutConstraint *trailing =[NSLayoutConstraint
                                   constraintWithItem:view
                                   attribute:NSLayoutAttributeTrailing
                                   relatedBy:NSLayoutRelationEqual
                                   toItem:superView
                                   attribute:NSLayoutAttributeTrailing
                                   multiplier:1.0f
                                   constant:0.f];
    
    //Leading
    NSLayoutConstraint *leading = [NSLayoutConstraint
                                   constraintWithItem:view
                                   attribute:NSLayoutAttributeLeading
                                   relatedBy:NSLayoutRelationEqual
                                   toItem:superView
                                   attribute:NSLayoutAttributeLeading
                                   multiplier:1.0f
                                   constant:0.f];
    
    //Bottom
    NSLayoutConstraint *bottom =[NSLayoutConstraint
                                 constraintWithItem:view
                                 attribute:NSLayoutAttributeBottom
                                 relatedBy:NSLayoutRelationEqual
                                 toItem:superView
                                 attribute:NSLayoutAttributeBottom
                                 multiplier:1.0f
                                 constant:0.f];
    
    //Top
    NSLayoutConstraint *top = [NSLayoutConstraint
                               constraintWithItem:view
                               attribute:NSLayoutAttributeTop
                               relatedBy:NSLayoutRelationEqual
                               toItem:superView
                               attribute:NSLayoutAttributeTop
                               multiplier:1.0f
                               constant:0.f];
    [superView addConstraint:trailing];
    [superView addConstraint:bottom];
    [superView addConstraint:leading];
    [superView addConstraint:top];
    
    [superView layoutIfNeeded];
}

- (void)addDispatch:(int)time completion:(completionBlock)completionBlk{
    // Delay execution of my block for 10 seconds.
    dispatch_after(dispatch_time(DISPATCH_TIME_NOW, time * NSEC_PER_SEC), dispatch_get_main_queue(), ^{
        completionBlk(@"completed");
    });
}

@end
