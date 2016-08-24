//
//  UserCreditTableViewCell.h
//  HelloCordova
//
//  Created by jagandeep on 7/7/16.
//
//

#import <UIKit/UIKit.h>

@interface UserCreditTableViewCell : UITableViewCell
@property (weak, nonatomic) IBOutlet UIView *creditsView;
@property (weak, nonatomic) IBOutlet UILabel *creditsLbl;
- (void)setCreditCellData:(NSString *)data;
@end
