//
//  UserTipView.m
//  HelloCordova
//
//  Created by jagandeep on 7/7/16.
//
//

#import "UserTipView.h"
#import "VideoCallHelper.h"
#import "UserCreditTableViewCell.h"
#import <QuartzCore/QuartzCore.h>

@implementation UserTipView

//Initialization method of class
- (id)initializeUserTipView:(UIView *)superView{
    // Adding Xib in class
    NSArray *nib = [[NSBundle mainBundle] loadNibNamed:@"UserTipView" owner:self options:nil];
    UIView *view = [nib objectAtIndex:0];
    [superView addSubview:view];
    [view setTranslatesAutoresizingMaskIntoConstraints:NO];
    //Adding constraints to xib
    [[VideoCallHelper sharedInstance] addCallingViewConstraints:view andSuperView:superView];
    return view;
}

-(void)setUpLayoutAmount:(float)amount isTipView:(BOOL)isTipView tipButtonEnabled:(BOOL)buttonEnabled{
    self.userAmount = amount;
    sendTipEnabled = buttonEnabled;
    [self.superview bringSubviewToFront:self];
    tableDataArray = @[BUY_10_CREDITS,BUY_20_CREDITS,BUY_40_CREDITS,BUY_60_CREDITS];
    [creditsTableVIew registerNib:[UINib nibWithNibName:@"UserCreditTableViewCell" bundle:nil]forCellReuseIdentifier:@"UserCreditTableViewCell"];
    creditsTableVIew.estimatedRowHeight = 64;
    creditsTableVIew.rowHeight = UITableViewAutomaticDimension;
    [creditsTableVIew setSeparatorStyle:UITableViewCellSeparatorStyleNone];
    [creditsTableVIew setBackgroundColor:[UIColor clearColor]];
    creditAmountLbl.attributedText = [self convertToAttributedString:amount];
    tipAmountLbl.attributedText = [self convertToAttributedString:amount];
    if (!isTipView) {
        [_addCreditsView setHidden:NO];
        [_tipView setHidden:YES];
        [creditsTableVIew reloadData];
    }
    [self setCornerRadiusOfView:creditsAddedView];
    [self setCornerRadiusOfView:_tipView];
    [self setCornerRadiusOfView:_addCreditsView];
}

- (void)updateAmount:(float)amount{
    creditAmountLbl.attributedText = [self convertToAttributedString:amount];
    tipAmountLbl.attributedText = [self convertToAttributedString:amount];
    creditAddedLbl.attributedText = [self convertToAttributedString:amount];
}

- (void)setCornerRadiusOfView:(UIView*)myView{
    [myView.layer setCornerRadius:8.0f];
    [myView.layer setMasksToBounds:YES];
    [myView.layer setBorderColor:[UIColor grayColor].CGColor];
    [myView.layer setBorderWidth:1.5];
}

-(NSMutableAttributedString *)convertToAttributedString:(float)amount{
    NSString *lblText = [NSString stringWithFormat:@"You have %.02f credits",amount];
    NSMutableAttributedString * string = [[NSMutableAttributedString alloc] initWithString:lblText];
    [string addAttribute:NSForegroundColorAttributeName value:[UIColor yellowColor] range:NSMakeRange(@"You have ".length,[NSString stringWithFormat:@"%.02f",amount].length)];
    return string;
}

-(NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section{
    return tableDataArray.count;
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
    static NSString *simpleTableIdentifier = @"UserCreditTableViewCell";
    
    UserCreditTableViewCell *cell = [tableView dequeueReusableCellWithIdentifier:simpleTableIdentifier];
    
    if (cell == nil) {
        NSArray *nib = [[NSBundle mainBundle] loadNibNamed:simpleTableIdentifier owner:self options:nil];
        cell = (UserCreditTableViewCell *)[nib objectAtIndex:0];
    }
    
    [cell setCreditCellData:[tableDataArray objectAtIndex:indexPath.row]];
    [cell setSelectionStyle:UITableViewCellSelectionStyleNone];
    return cell;
}

-(void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath{
    int amount = 0;
    switch (indexPath.row) {
        case 0:
            amount = 10;
            break;
            
        case 1:
            amount = 20;
            break;
            
        case 2:
            amount = 40;
            break;
            
        case 3:
            amount = 60;
            break;
    }
    [_transactionDelgate transactionNeedToBeDone:CreditsBuy amount:amount];
}

- (IBAction)closeViewAction:(id)sender {
    [self removeFromSuperview];
}

- (IBAction)showCreditsView:(id)sender {
    [_addCreditsView setHidden:NO];
    [creditsAddedView setHidden:YES];
    [_tipView setHidden:YES];
    [creditsTableVIew reloadData];
}

- (IBAction)showTipView:(id)sender {
    if(!sendTipEnabled){
        return;
    }
    [_addCreditsView setHidden:YES];
    [creditsAddedView setHidden:YES];
    [_tipView setHidden:NO];
}

- (void)creditSuccessfullyAdded:(float)amount{
    creditAddedLbl.attributedText = [self convertToAttributedString:amount];
    [_addCreditsView setHidden:YES];
    [creditsAddedView setHidden:NO];
    [_tipView setHidden:YES];

}

- (void)stopUserInteraction{
    if (![_addCreditsView isHidden]) {
        [self enableUserInteractionOfView:_addCreditsView enable:NO];
    }
    else if(![creditsAddedView isHidden]){
        [self enableUserInteractionOfView:creditsAddedView enable:NO];
    }
    else{
        [self enableUserInteractionOfView:_tipView enable:NO];
    }
    
}

- (void)enableUserInteraction{
    if (![_addCreditsView isHidden]) {
        [self enableUserInteractionOfView:_addCreditsView enable:YES];
    }
    else if(![creditsAddedView isHidden]){
        [self enableUserInteractionOfView:creditsAddedView enable:YES];
    }
    else{
        [self enableUserInteractionOfView:_tipView enable:YES];
    }
}

- (void)enableUserInteractionOfView:(UIView *)myVIew enable:(BOOL)check{
    for (int i = 0; i < [[myVIew subviews] count]; i++)
    {
        UIView* view = [[myVIew subviews] objectAtIndex: i];
        [view setUserInteractionEnabled:check];
    }
}

- (void)touchesBegan:(NSSet<UITouch *> *)touches withEvent:(UIEvent *)event{
    [self endEditing:YES];
}

- (IBAction)tip10Action:(id)sender {
     [self tipSend:10];
}
- (IBAction)tip20Action:(id)sender {
    [self tipSend:20];
}
- (IBAction)tip40Action:(id)sender {
    [self tipSend:40];
}
- (IBAction)tip60Action:(id)sender {
    [self tipSend:60];
}
- (IBAction)sendCustomTip:(id)sender {
    [self tipSend:[tiipTF.text intValue]];
}

-(void)tipSend:(int)amount{
    [self endEditing:YES];
    if (amount>self.userAmount) {
        [balanceWarningLbl setHidden:NO];
        [[VideoCallHelper sharedInstance]addDispatch:3 completion:^(NSString *isFinished) {
            [balanceWarningLbl setHidden:YES];
        }];
        return;
    }
    [self removeFromSuperview];
    [_transactionDelgate transactionNeedToBeDone:TipSent amount:amount];
}

@end
