//
//  CallingView.m
//  HelloCordova
//
//  Created by jagandeep on 7/5/16.
//
//

#import "CallingView.h"
#import "VideoCallHelper.h"
#import <QuartzCore/QuartzCore.h>

@interface CallingView(){
    UITapGestureRecognizer *tapGestureForSubscriber;
    UITapGestureRecognizer *tapGestureForPublisher;
    NSTimer *timerToHideButton;
    BOOL alwaysShowButton;
    int timeForCall;
    NSTimer *timer;
    __weak IBOutlet UILabel *userNameLbl;
    __weak IBOutlet UILabel *lowCreditLbl;
    __weak IBOutlet UIView *lowWarningView;
    __weak IBOutlet UIButton *addCreditNavbtn;
}
@property (nonatomic,assign) BOOL allButtonVisible;
@end

@implementation CallingView

@synthesize allButtonVisible;
-(void)drawRect:(CGRect)rect{
    viewTimer.layer.cornerRadius = 8;
    viewTimer.layer.masksToBounds = YES;
}

//Initialization method of class
- (id)initializeCallingView:(UIView *)superView {
    // Adding Xib in class
    NSArray *nib = [[NSBundle mainBundle] loadNibNamed:@"CallingView" owner:self options:nil];
    UIView *view = [nib objectAtIndex:0];
    [superView addSubview:view];
    [view setTranslatesAutoresizingMaskIntoConstraints:NO];
    
    //Adding constraints to xib
    [[VideoCallHelper sharedInstance] addCallingViewConstraints:view andSuperView:superView];
    return view;
}


- (void)hideTipButton{
    [btnTipUser setHidden:YES];
    [addCreditNavbtn setHidden:YES];
}
- (void)showUserName:(NSString *)name{
    userNameLbl.text = name;
}

- (void)stopHidingButtons{
    [timerToHideButton invalidate];
    timerToHideButton = nil;
    allButtonVisible = YES;
}

//Adding subscriber view OR full view
- (void)addToFullView:(UIView *)subscriber{
    for (UIView *subview in [subscriberView subviews]) {
        [subview removeFromSuperview];
    }
    if (!subscriber) {
        alwaysShowButton = YES;
        allButtonVisible = NO;
        [[NSNotificationCenter defaultCenter] postNotificationName:@"allButtonVisible" object:nil];
        return;
    }
    
    subscriber.frame = subscriberView.frame;
    [subscriberView addSubview:subscriber];
    [subscriber setTranslatesAutoresizingMaskIntoConstraints:NO];
    [[VideoCallHelper sharedInstance] addCallingViewConstraints:subscriber andSuperView:subscriberView];
    BOOL haveGesture = false;
    for (UIGestureRecognizer *recognizer in subscriber.gestureRecognizers) {
        if ([recognizer isKindOfClass:[UITapGestureRecognizer class]]) {
            haveGesture = true;
        }
    }
    
    if (!haveGesture) {
    tapGestureForSubscriber =
    [[UITapGestureRecognizer alloc] initWithTarget:self
                                            action:@selector(handleSingleTap:)];
    tapGestureForSubscriber.cancelsTouchesInView = NO;
    [subscriber addGestureRecognizer:tapGestureForSubscriber];
    [self callStarted];
    }
    alwaysShowButton = NO;
    [self hideButtonWithDelay];
}

//Adding Publisher view Or Small view
- (void)addToSmallView:(UIView *)publisher{
    for (UIView *subview in [publisherView subviews]) {
        [subview removeFromSuperview];
    }
    if (!publisher) {
        return;
    }
    publisher.frame = publisherView.frame;
    [publisherView addSubview:publisher];
    [publisherView bringSubviewToFront:publisher];
    [publisher setTranslatesAutoresizingMaskIntoConstraints:NO];
    [[VideoCallHelper sharedInstance] addCallingViewConstraints:publisher andSuperView:publisherView];
    
    BOOL haveGesture = false;
    for (UIGestureRecognizer *recognizer in publisher.gestureRecognizers) {
        if ([recognizer isKindOfClass:[UITapGestureRecognizer class]]) {
            haveGesture = true;
        }
    }

    if (!haveGesture) {
        tapGestureForPublisher =
        [[UITapGestureRecognizer alloc] initWithTarget:self
                                                action:@selector(handleSingleTap:)];
        tapGestureForPublisher.cancelsTouchesInView = NO;
        [publisher addGestureRecognizer:tapGestureForPublisher];
    }
    [self hideButtonWithDelay];
    
}

//Call Started
- (void)callStarted{
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(keyValueChangedForButtonVisiblity) name:@"allButtonVisible" object:nil];
    [self enableAllButton];
    [self hideButtonWithDelay];
    if(timer){
        [timer invalidate];
        timer = nil;
    }
    
    timer = [NSTimer scheduledTimerWithTimeInterval:1.0
                                             target:self
                                           selector:@selector(updateTime:)
                                           userInfo:nil
                                            repeats:YES];
    NSRunLoop *runner = [NSRunLoop currentRunLoop];
    [runner addTimer:timer forMode: NSDefaultRunLoopMode];
}

-(void)updateTime:(id)timer{
    @try {
        timeForCall++;
        lblTimer.text = [NSString stringWithFormat:@"%02d : %02d",(int)timeForCall/60, (int)timeForCall%60];
    } @catch (NSException *exception) {
    }
}

- (BOOL)callIsDisconnectedAfterConnecting{
    if (timeForCall != 0) {
        return YES ;
    }
    else{
        return NO;
    }
}

- (void)enableAllButton{
    [btnTipUser setEnabled:YES];
    [btnAudioSwitch setEnabled:YES];
    [btnVideoSwitch setEnabled:YES];
    [btnRotateCamera setEnabled:YES];
}

- (void)dealloc{
    [self removeAndStopTimers];
}

- (void)removeAndStopTimers{
    @try{
        [[NSNotificationCenter defaultCenter] removeObserver:self];
        [timerToHideButton invalidate];
        timerToHideButton = nil;
        [timer invalidate];
        timer = nil;
    } @catch (NSException *exception) {
        NSLog(@" esception occer.");
    }
    
}

- (IBAction)tipAction:(id)sender {
     [_callingViewDelegate showTipView];
}

- (IBAction)rotateCameraAction:(id)sender {
    [sender setSelected:![sender isSelected]];
    [_callingViewDelegate cameraRotated];
}

- (IBAction)videoSwitch:(id)sender {
    [sender setSelected:![sender isSelected]];
    [_callingViewDelegate videoCallSwitch:![sender isSelected]];
}

- (IBAction)micSwitch:(id)sender {
    [sender setSelected:![sender isSelected]];
    [_callingViewDelegate callMuted:![sender isSelected]];
}
- (IBAction)endCallAction:(id)sender {
    [timerToHideButton invalidate];
    timerToHideButton = nil;
    [timer invalidate];
    timer = nil;
    
    [_callingViewDelegate callEnded];
}
- (IBAction)addCreditAction:(id)sender {
    [_callingViewDelegate showAddCreditView];
}

- (void)showLowWarningView:(NSString *)time{
    [lowWarningView.layer setCornerRadius:5.0];
    lowCreditLbl.text = time;
    [UIView animateWithDuration:2.0 animations:^{
        [lowWarningView setHidden:NO];
    } completion:^(BOOL finished) {
    }];
}
- (void)hideLowWarningView{
    [UIView animateWithDuration:2.0 animations:^{
        [lowWarningView setHidden:YES];
    } completion:^(BOOL finished) {
    }];
}

- (void)showTipViewWithAmount:(NSString *)amount sent:(BOOL)check{
    [tipView.layer setCornerRadius:5.0];
    NSString *message;
    if (check) {
        message = [NSString stringWithFormat:@"%@ tips sent",amount];
    }
    else{
        message = [NSString stringWithFormat:@"%@ tips received",amount];
    }
    tipLbl.text = message;
    [UIView animateWithDuration:2.0 animations:^{
        [tipView setHidden:NO];
    } completion:^(BOOL finished) {
        [[VideoCallHelper sharedInstance]addDispatch:3 completion:^(NSString *isFinished) {
            [tipView setHidden:YES];
        }];
    }];
}

//The event handling method for tap gesture
- (void)handleSingleTap:(UITapGestureRecognizer *)recognizer {
    NSLog(@"here it comes ");
    [timerToHideButton invalidate];
    timerToHideButton = nil;
    allButtonVisible = !allButtonVisible;
    [[NSNotificationCenter defaultCenter] postNotificationName:@"allButtonVisible" object:nil];
}

- (void)keyValueChangedForButtonVisiblity{
    
    [UIView animateWithDuration:0.3 delay:0.0
                        options:UIViewAnimationOptionAllowUserInteraction
                     animations:^
     {
         if (!allButtonVisible) {
             constraintBottomTimeLbl.constant = 3;
             constraintLeftTipBtn.constant = 15;
             constraintRightCameraSwitchBtn.constant = 15;
         }
         else{
             constraintBottomTimeLbl.constant = -100;
             constraintLeftTipBtn.constant = -70;
             constraintRightCameraSwitchBtn.constant = -70;
             
         }
         [self hideButtonWithDelay];
         [self layoutIfNeeded];
     } completion:^(BOOL check){
     }];
}

-(void)hideButtonWithDelay{
    if (alwaysShowButton) {
        [timerToHideButton invalidate];
        timerToHideButton = nil;
        return;
    }
    timerToHideButton = [NSTimer scheduledTimerWithTimeInterval:3.0
                                                         target:self
                                                       selector:@selector(hideButtonWithDelaySelector)
                                                       userInfo:nil
                                                        repeats:NO];
    NSRunLoop *runner = [NSRunLoop currentRunLoop];
    [runner addTimer:timerToHideButton forMode: NSDefaultRunLoopMode];
}

-(void)hideButtonWithDelaySelector{
    [timerToHideButton invalidate];
    timerToHideButton = nil;
    allButtonVisible = YES;
    [[NSNotificationCenter defaultCenter] postNotificationName:@"allButtonVisible" object:nil];
}

@end
