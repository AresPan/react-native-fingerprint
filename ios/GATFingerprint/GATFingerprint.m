//
//  GATFingerprint.m
//  GATFingerprint
//
//  Created by gengwenming@icloud.com on 16/8/26.
//  Copyright © 2016年 www.guanaitong.com. All rights reserved.
//

#import "GATFingerprint.h"
#import <LocalAuthentication/LAContext.h>
#import "RCTBridge.h"
#import "RCTEventDispatcher.h"

@interface GATFingerprint ()

@property (nonatomic, strong) LAContext *myContext;
@end

@implementation GATFingerprint
@synthesize bridge = _bridge;

- (LAContext *)myContext {
    return [[LAContext alloc] init];
}

RCT_EXPORT_MODULE(GATFingerprint);

#pragma mark - EXPORT_MOTHOD_TO_REACT-NATIVE

// 判断设备是否支持指纹识别
RCT_REMAP_METHOD(isSupport,
                 resolver:(RCTPromiseResolveBlock)resolve
                 rejecter:(RCTPromiseRejectBlock)reject) {
    NSError *authError = nil;
    BOOL isEvaluate = [self.myContext canEvaluatePolicy:LAPolicyDeviceOwnerAuthenticationWithBiometrics error:&authError];
    NSNumber *isSupport = [NSNumber numberWithBool:isEvaluate];
    NSString *errorCode, *errorMessage;
    if (isEvaluate) {
        errorCode = @"0";
        errorMessage = @"support";
    } else {
        errorCode = @"102";
        errorMessage = @"TouchID不可用";
    }
    resolve(@{@"isSupport":isSupport, @"errorCode":errorCode, @"errorMessage":errorMessage});
}

RCT_EXPORT_METHOD(startTouch:(NSString *)localizedReasonString) {
    [self.myContext evaluatePolicy:LAPolicyDeviceOwnerAuthenticationWithBiometrics localizedReason:localizedReasonString reply:^(BOOL success, NSError * _Nullable error) {
        NSMutableDictionary *dictM = [NSMutableDictionary dictionary];
        NSNumber *isSuccess = [NSNumber numberWithBool:success];
        dictM[@"isSuccess"] = isSuccess;
        if (success) {
            // User authenticated successfully, take appropriate action
            dictM[@"errorCode"] = @0;
            dictM[@"errorMessage"] = @"success";
            [self sendAppEvent:dictM];
        } else {
            // 错误码 error.code
            // -1: 连续三次指纹识别错误
            // -2: 在TouchID对话框中点击了取消按钮
            // -3: 在TouchID对话框中点击了输入密码按钮
            // -4: TouchID对话框被系统取消，例如按下Home或者电源键
            // -8: 连续五次指纹识别错误，TouchID功能被锁定，下一次需要输入系统密码
            NSNumber *errorCode = [NSNumber numberWithInteger:error.code];
            dictM[@"errorCode"] = errorCode;
            switch (error.code) {
                case -1:
                    dictM[@"errorMessage"] = @"连续三次指纹识别错误";
                    break;
                case -2:
                    dictM[@"errorMessage"] = @"在TouchID对话框中点击了取消按钮";
                    break;
                case -3:
                    dictM[@"errorMessage"] = @"在TouchID对话框中点击了输入密码按钮";
                    break;
                case -4:
                    dictM[@"errorMessage"] = @"TouchID对话框被系统取消，例如按下Home或者电源键";
                    break;
                case -8:
                    dictM[@"errorMessage"] = @"连续五次指纹识别错误，TouchID功能被锁定，下一次需要输入系统密码";
                    break;
                    
                default:
                    break;
            }
            [self sendAppEvent:dictM];
        }
    }];
}

// 进行设置发送事件通知给JavaScript端
- (void)sendAppEvent:(NSDictionary *)dictionary {
    [self.bridge.eventDispatcher sendAppEventWithName:@"fingerprintCallBack" body: dictionary];
}

@end
