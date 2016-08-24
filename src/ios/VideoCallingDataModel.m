//
//  VideoCallingDataModel.m
//  HelloCordova
//
//  Created by jagandeep on 7/12/16.
//
//

#import "VideoCallingDataModel.h"

@implementation VideoCallingDataModel
-(id)initializeWithDictionary:(NSDictionary *)data{
    self.apikey = [[data allKeys] containsObject:@"ApiKey"]?[data objectForKey:@"ApiKey"]:nil;
     self.sessionId = [[data allKeys] containsObject:@"SessionId"]?[data objectForKey:@"SessionId"]:nil;
     self.token = [[data allKeys] containsObject:@"Token"]?[data objectForKey:@"Token"]:nil;
     self.userType = [[data allKeys] containsObject:@"UserType"]?[data objectForKey:@"UserType"]:nil;
    self.isAbleToCall = [[data allKeys] containsObject:@"IsAbleToCall"]?[[data objectForKey:@"IsAbleToCall"]  isEqualToString: @"true"]?true:false:false;
     self.profileImage = [[data allKeys] containsObject:@"ProfileImage"]?[data objectForKey:@"ProfileImage"]:nil;
    self.callPerMinute = [[data allKeys] containsObject:@"CallPerMinute"]?[data objectForKey:@"CallPerMinute"] :nil;
     self.amount = [[data allKeys] containsObject:@"Amount"]?[data objectForKey:@"Amount"] :nil;
    self.userName = [[data allKeys] containsObject:@"UserName"]?[data objectForKey:@"UserName"] :nil;
    
    return self;
}

-(BOOL)checkForValidModel{
    if (self.apikey && self.sessionId && self.token && self.userType && self.profileImage && self.callPerMinute && self.amount && self.userName) {
        return YES;
    }
    return YES;
}

@end
