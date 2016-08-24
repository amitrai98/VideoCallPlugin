//
//  UserCreditTableViewCell.m
//  HelloCordova
//
//  Created by jagandeep on 7/7/16.
//
//

#import "UserCreditTableViewCell.h"

@implementation UserCreditTableViewCell

- (void)awakeFromNib {
    [super awakeFromNib];
    // Initialization code
}

- (void)setSelected:(BOOL)selected animated:(BOOL)animated {
    [super setSelected:selected animated:animated];

    // Configure the view for the selected state
}

- (void)setCreditCellData:(NSString *)data{
    [self.creditsView.layer setCornerRadius:8.0];
    [self.creditsView.layer setMasksToBounds:YES];
    self.creditsLbl.text = data;
}

@end
