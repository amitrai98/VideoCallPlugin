//
//  UserTipView.h
//  HelloCordova
//
//  Created by jagandeep on 7/7/16.
//
//

#import <UIKit/UIKit.h>
typedef enum : NSUInteger {
    TipSent,
    CreditsBuy,
} TransactionType;

@protocol TransactionDelegate <NSObject>
-(void)transactionNeedToBeDone:(TransactionType)type amount:(int)amount;
@end

@interface UserTipView : UIView <UITableViewDelegate,UITableViewDataSource>
{
    __weak IBOutlet UILabel *balanceWarningLbl;
    __weak IBOutlet UITableView *creditsTableVIew;
    NSArray *tableDataArray;
    __weak IBOutlet UITextField *tiipTF;
    __weak IBOutlet UILabel *tipAmountLbl;
    __weak IBOutlet UILabel *creditAmountLbl;
    
    __weak IBOutlet UILabel *creditAddedLbl;
    __weak IBOutlet UIView *creditsAddedView;
     BOOL sendTipEnabled;
}
@property (nonatomic,weak) IBOutlet UIView *tipView;
@property (nonatomic,weak) IBOutlet UIView *addCreditsView;
@property (nonatomic,assign) float userAmount;
@property (nonatomic,strong) id <TransactionDelegate> transactionDelgate;
- (id)initializeUserTipView:(UIView *)superView;
- (void)setUpLayoutAmount:(float)amount isTipView:(BOOL)isTipView tipButtonEnabled:(BOOL)buttonEnabled;
- (void)creditSuccessfullyAdded:(float)amount;
- (void)stopUserInteraction;
- (void)enableUserInteraction;
@end
