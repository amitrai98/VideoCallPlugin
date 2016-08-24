//
//  NetworkDownloadBandwidthClass.m
//  Hello-World
//
//  Created by jagandeep on 6/7/16.
//  Copyright © 2016 TokBox, Inc. All rights reserved.
//

#import "NetworkDownloadBandwidthClass.h"


@interface NetworkDownloadBandwidthClass()<NSURLSessionDelegate, NSURLSessionDataDelegate>

@property (nonatomic) CFAbsoluteTime startTime;
@property (nonatomic) CFAbsoluteTime stopTime;
@property (nonatomic) long long bytesReceived;
@property (nonatomic, copy) void (^speedTestCompletionHandler)(CGFloat megabytesPerSecond,enum NetworkBandwidth netSpeed, NSError *error);

@end



@implementation NetworkDownloadBandwidthClass
/// Test speed of download
///
/// Test the speed of a connection by downloading some predetermined resource. Alternatively, you could add the
/// URL of what to use for testing the connection as a parameter to this method.
///
/// @param timeout             The maximum amount of time for the request.
/// @param completionHandler   The block to be called when the request finishes (or times out).
///                            The error parameter to this closure indicates whether there was an error downloading
///                            the resource (other than timeout).
///
/// @note                      Note, the timeout parameter doesn't have to be enough to download the entire
///                            resource, but rather just sufficiently long enough to measure the speed of the download.

- (void)testDownloadSpeedWithTimout:(NSTimeInterval)timeout completionHandler:(nonnull void (^)(CGFloat megabytesPerSecond,enum NetworkBandwidth netSpeed, NSError * _Nullable error))completionHandler {
    NSURL *url = [NSURL URLWithString:@"https://res.cloudinary.com/tempest/image/upload/c_limit,cs_srgb,dpr_1.0,q_80,w_10000/MTM3NjEyNzY4MzgyNTU5NDc0.jpg"];
    
    self.startTime = CFAbsoluteTimeGetCurrent();
    self.stopTime = self.startTime;
    self.bytesReceived = 0;
    self.speedTestCompletionHandler = completionHandler;
    
    NSURLSessionConfiguration *configuration = [NSURLSessionConfiguration ephemeralSessionConfiguration];
    configuration.timeoutIntervalForResource = timeout;
    NSURLSession *session = [NSURLSession sessionWithConfiguration:configuration delegate:self delegateQueue:nil];
    [[session dataTaskWithURL:url] resume];
}

- (void)URLSession:(NSURLSession *)session dataTask:(NSURLSessionDataTask *)dataTask didReceiveData:(NSData *)data {
    self.bytesReceived += [data length];
    self.stopTime = CFAbsoluteTimeGetCurrent();
}

- (void)URLSession:(NSURLSession *)session task:(NSURLSessionTask *)task didCompleteWithError:(NSError *)error {
    CFAbsoluteTime elapsed = self.stopTime - self.startTime;
    CGFloat speed = elapsed != 0 ? self.bytesReceived / (CFAbsoluteTimeGetCurrent() - self.startTime) / 1024.0 : -1;
    
    // treat timeout as no error (as we're testing speed, not worried about whether we got entire resource or not
    enum NetworkBandwidth netSpeed = low;
    if (speed > 250 && speed < 400) {
        netSpeed = medium;
    }
    else if (speed >= 400){
        netSpeed = high;
    }
    
    if (error == nil || ([error.domain isEqualToString:NSURLErrorDomain] && error.code == NSURLErrorTimedOut)) {
        
        self.speedTestCompletionHandler(speed,netSpeed, nil);
    } else {
        self.speedTestCompletionHandler(speed,netSpeed, error);
    }
}

@end
