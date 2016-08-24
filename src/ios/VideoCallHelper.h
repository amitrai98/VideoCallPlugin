//
//  VideoCallHelper.h
//  HelloCordova
//
//  Created by jagandeep on 7/4/16.
//
//

#import <Foundation/Foundation.h>
#import "Constants.h"

@interface VideoCallHelper : NSObject{
    UIView *loadingView;
    UIView *priceView;
    UIView *logView;
    
}
typedef void (^completionBlock)(NSString *isFinished);
+ (instancetype)sharedInstance;
- (void)showCallInitializeView:(UIView *)superView imageUrl:(NSString *)imageUrl rates:(NSString *)rates userType:(NSString *)userType;
- (void)hideLoadingBar;
- (void)registerforDeviceLockNotify:(void (^)(NSString*))callbackBlock;
- (void)identifyTheChangedNetwork:(void (^)(NSString*))callbackBlock;
- (void)addCallingViewConstraints:(UIView *)view andSuperView:(UIView *)superView;
- (void)addDispatch:(int)time completion:(completionBlock)completionBlk;
@end
