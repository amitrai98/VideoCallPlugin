//
//  CallingView.h
//  HelloCordova
//
//  Created by jagandeep on 7/5/16.
//
//

#import <UIKit/UIKit.h>
@protocol CallingViewDelegates <NSObject>
- (void)callMuted:(BOOL)mute;
- (void)videoCallSwitch:(BOOL)showVideo;
- (void)cameraRotated;
- (void)showTipView;
- (void)callEnded;
- (void)showAddCreditView;
@end

@interface CallingView : UIView{
    
    __weak IBOutlet UIView *viewTimer;
    __weak IBOutlet UIView *subscriberView;
    __weak IBOutlet UIView *publisherView;
    __weak IBOutlet UILabel *lblTimer;
    __weak IBOutlet UIButton *btnEndCall;
    __weak IBOutlet UIButton *btnAudioSwitch;
    __weak IBOutlet UIButton *btnVideoSwitch;
    __weak IBOutlet UIButton *btnTipUser;
    __weak IBOutlet UIButton *btnRotateCamera;
    
    __weak IBOutlet UIView *tipView;
    __weak IBOutlet UILabel *tipLbl;
    //Constraints IBOutlets
    __weak IBOutlet NSLayoutConstraint *constraintRightCameraSwitchBtn;
    __weak IBOutlet NSLayoutConstraint *constraintLeftTipBtn;
    __weak IBOutlet NSLayoutConstraint *constraintBottomTimeLbl;
    
}
@property (nonatomic, weak) id <CallingViewDelegates> callingViewDelegate;
- (id)initializeCallingView:(UIView *)superView;
- (void)addToFullView:(UIView *)subscriber;
- (void)addToSmallView:(UIView *)publisher;
- (void)stopHidingButtons;
- (void)hideTipButton;
- (void)removeAndStopTimers;
- (void)hideLowWarningView;
- (void)showUserName:(NSString *)name;
- (void)showLowWarningView:(NSString *)time;
- (BOOL)callIsDisconnectedAfterConnecting;
- (void)showTipViewWithAmount:(NSString *)amount sent:(BOOL)check;
@end
