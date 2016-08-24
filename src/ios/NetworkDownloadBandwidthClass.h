//
//  NetworkDownloadBandwidthClass.h
//  Hello-World
//
//  Created by jagandeep on 6/7/16.
//  Copyright © 2016 TokBox, Inc. All rights reserved.
//

#import <Foundation/Foundation.h>
enum NetworkBandwidth {low = 0, medium= 1, high = 2};
@interface NetworkDownloadBandwidthClass : NSObject
- (void)testDownloadSpeedWithTimout:(NSTimeInterval)timeout completionHandler:(nonnull void (^)(CGFloat megabytesPerSecond,enum NetworkBandwidth netSpeed, NSError * _Nullable error))completionHandler;
@end
