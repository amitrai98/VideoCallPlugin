//
//  VideoCallingDataModel.h
//  HelloCordova
//
//  Created by jagandeep on 7/12/16.
//
//

#import <Foundation/Foundation.h>

@interface VideoCallingDataModel : NSObject
@property (nonatomic,strong) NSString *apikey;
@property (nonatomic,strong) NSString *sessionId;
@property (nonatomic,strong) NSString *token;
@property (nonatomic,strong) NSString *userType;
@property (nonatomic,assign) BOOL isAbleToCall;
@property (nonatomic,strong) NSString *profileImage;
@property (nonatomic,strong) NSString* callPerMinute;
@property (nonatomic,strong) NSString* amount;
@property (nonatomic,strong) NSString* userName;
-(id)initializeWithDictionary:(NSDictionary *)data;
-(BOOL)checkForValidModel;
@end

