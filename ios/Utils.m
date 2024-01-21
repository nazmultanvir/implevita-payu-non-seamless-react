//
//  Utils.m
//  react-native-biz-sdk
//
//  Created by Umang Arya on 04/09/20.
//

#import "Utils.h"
#import <PayUCustomBrowser/PayUCustomBrowser.h>
#import "NSDictionary+Safe.h"

@implementation Utils

+(NSDictionary *)transformResponse:(NSDictionary *) aDict{
    NSMutableDictionary *dict = [[NSMutableDictionary alloc] initWithDictionary:aDict];
    [dict setValue:[self getString:dict[@"merchantResponse"]] forKey:@"merchantResponse"];
    [dict setValue:[self getString:dict[@"payuResponse"]] forKey:@"payuResponse"];
    return dict;
}

+(NSString*)getString:(id) response {
    if ([response isKindOfClass: [NSDictionary class]]) {
        NSData *data = [NSJSONSerialization dataWithJSONObject:response options:kNilOptions error:nil];
        NSString *string = [[NSString alloc] initWithData:data encoding:NSUTF8StringEncoding];
        return string;
    } else {
        return [NSString stringWithFormat:@"%@", response];
    }
}

+ (UIColor *)colorFromHexString:(NSString *)hexString {
    if ([hexString isKindOfClass:[NSString class]]) {
        unsigned rgbValue = 0;
        NSScanner *scanner = [NSScanner scannerWithString:hexString];
        [scanner setScanLocation:1]; // bypass '#' character
        [scanner scanHexInt:&rgbValue];
        return [UIColor colorWithRed:((rgbValue & 0xFF0000) >> 16)/255.0 green:((rgbValue & 0xFF00) >> 8)/255.0 blue:(rgbValue & 0xFF)/255.0 alpha:1.0];
    } else {
        return  nil;
    }
}

+(PayUPaymentParam *)paymentParam:(NSDictionary *)aPaymentParamDict {
    NSDictionary *paymentParamDict = [aPaymentParamDict removeNullKeys];
    if ([paymentParamDict isKindOfClass:[NSDictionary class]]) {
        Environment env = EnvironmentProduction;
        if ([(NSNumber*)paymentParamDict[@"environment"] integerValue] == 1) {
            env = EnvironmentTest;
        }
        
        PayUPaymentParam *paymentParam = [[PayUPaymentParam alloc] initWithKey:paymentParamDict[@"key"]
                                                                 transactionId:paymentParamDict[@"transactionId"]
                                                                        amount:paymentParamDict[@"amount"]
                                                                   productInfo:paymentParamDict[@"productInfo"]
                                                                     firstName:paymentParamDict[@"firstName"]
                                                                         email:paymentParamDict[@"email"]
                                                                         phone:paymentParamDict[@"phone"]
                                                                          surl:paymentParamDict[@"ios_surl"]
                                                                          furl:paymentParamDict[@"ios_furl"]
                                                                   environment:env];
        NSString *userCredential = [paymentParamDict objectForKey:@"userCredential"];
        if (userCredential != nil) {
            paymentParam.userCredential = userCredential;
        }
        
      NSDictionary *payUSIParams = [paymentParamDict objectForKey:@"payUSIParams"];
      if (payUSIParams != nil) {
        paymentParam.siParam = [self getPayUSIParams:payUSIParams];
      }

        // set additional params
        [self setAdditionalParamIn:paymentParam fromPaymentParamDict:paymentParamDict];
        
        return paymentParam;
    }
    return nil;
}

+(PayUCheckoutProConfig *)config:(NSDictionary *)aConfigDict {
    NSDictionary *configDict = [aConfigDict removeNullKeys];
    if ([configDict isKindOfClass:[NSDictionary class]]) {
        PayUCheckoutProConfig *checkoutProConfig = [[PayUCheckoutProConfig alloc] init];
        
        UIColor *primaryColor = [Utils colorFromHexString:configDict[@"primaryColor"]];
        UIColor *secondaryColor = [Utils colorFromHexString:configDict[@"secondaryColor"]];
        // This is to make sure both colors are set
        // As this is the strict requirement to set both colors
        if (primaryColor != nil && secondaryColor != nil) {
            [checkoutProConfig customiseUIWithPrimaryColor:primaryColor
                                                 secondaryColor:secondaryColor];
        }
        
        NSString *merchantName = configDict[@"merchantName"];
        if (merchantName.length >= 1) {
            checkoutProConfig.merchantName = merchantName;
        }
        
        UIImage *merchantLogo = [UIImage imageNamed:configDict[@"merchantLogo"]];
        if (merchantLogo != nil) {
            checkoutProConfig.merchantLogo = merchantLogo;
        }
        
        NSNumber *showExitConfirmationOnCheckoutScreen = configDict[@"showExitConfirmationOnCheckoutScreen"];
        if (showExitConfirmationOnCheckoutScreen != nil) {
            checkoutProConfig.showExitConfirmationOnCheckoutScreen = [showExitConfirmationOnCheckoutScreen boolValue];
        }
        
        NSNumber *showExitConfirmationOnPaymentScreen = configDict[@"showExitConfirmationOnPaymentScreen"];
        if (showExitConfirmationOnPaymentScreen != nil) {
            checkoutProConfig.showExitConfirmationOnPaymentScreen = [showExitConfirmationOnPaymentScreen boolValue];
        }
        
        NSArray<NSDictionary<NSString *, NSString *> *> *cartDetails = configDict[@"cartDetails"];
        if (cartDetails.count >= 1) {
            checkoutProConfig.cartDetails = cartDetails;
        }
        
        NSArray<NSDictionary<NSString *, NSString *> *> *paymentModesOrderArr = configDict[@"paymentModesOrder"];
        if (paymentModesOrderArr.count >= 1) {
            NSMutableArray<PaymentMode *> *paymentModesOrder = [NSMutableArray new];
            for (NSDictionary<NSString *, NSString *> *eachDict in paymentModesOrderArr) {
                NSString *paymentType = eachDict.allKeys.firstObject;
                NSString *paymentOptionID = [eachDict safeObjectForKey:paymentType];
                if ([paymentType caseInsensitiveCompare:@"cards"] == NSOrderedSame){
                    [paymentModesOrder addObject: [[PaymentMode alloc] initWithPaymentType: PaymentTypeCcdc paymentOptionID: nil]];
                } else if ([paymentType caseInsensitiveCompare:@"net banking"] == NSOrderedSame) {
                    [paymentModesOrder addObject: [[PaymentMode alloc] initWithPaymentType: PaymentTypeNetBanking paymentOptionID: nil]];
                } else if ([paymentType caseInsensitiveCompare:@"upi"] == NSOrderedSame) {
                    [paymentModesOrder addObject: [[PaymentMode alloc] initWithPaymentType: PaymentTypeUpi paymentOptionID: paymentOptionID]];
                } else if ([paymentType caseInsensitiveCompare:@"wallets"] == NSOrderedSame) {
                    [paymentModesOrder addObject: [[PaymentMode alloc] initWithPaymentType: PaymentTypeWallet paymentOptionID: paymentOptionID]];
                } else if ([paymentType caseInsensitiveCompare:@"emi"] == NSOrderedSame) {
                    [paymentModesOrder addObject: [[PaymentMode alloc] initWithPaymentType: PaymentTypeEmi paymentOptionID: nil]];
                }
            }
            checkoutProConfig.paymentModesOrder = paymentModesOrder;
        }
        
        NSArray<NSDictionary<NSString *, id> *> *enforcePayment = configDict[@"enforcePaymentList"];
        if (paymentModesOrderArr.count >= 1) {
            checkoutProConfig.enforcePaymentList = enforcePayment;
        }
        [self setCBConfig:configDict checkoutProConfig:checkoutProConfig];

        
        return checkoutProConfig;
    }
    return nil;
}

+(void)setCBConfig:(NSDictionary *)cbConfigDict checkoutProConfig:(PayUCheckoutProConfig *)checkoutProConfig {
        if ([cbConfigDict isKindOfClass:[NSDictionary class]]) {
        
        if ([cbConfigDict[@"surePayCount"] isKindOfClass:[NSNumber class]]) {
            int surePayCount = [cbConfigDict[@"surePayCount"] intValue];
            if (surePayCount >= 0) {
                checkoutProConfig.surePayCount = surePayCount;
            }
        }
        
        if ([cbConfigDict[@"merchantResponseTimeout"] isKindOfClass:[NSNumber class]]) {
            int merchantResponseTimeout = [cbConfigDict[@"merchantResponseTimeout"] intValue];
            if (merchantResponseTimeout >= 0) {
                checkoutProConfig.merchantResponseTimeout = merchantResponseTimeout/1000;
            }
        }
        
        if ([cbConfigDict[@"autoSelectOtp"] isKindOfClass:[NSNumber class]]) {
            int autoSelectOtp = [cbConfigDict[@"autoSelectOtp"] intValue];
            if (autoSelectOtp == 1) {
                checkoutProConfig.autoSelectOtp = TRUE;
            } else if (autoSelectOtp == 0) {
                checkoutProConfig.autoSelectOtp = FALSE;
            }
        }
    }
}

+(NSError *)convertExceptionToError:(NSException *)exception{
    NSMutableDictionary * info = [NSMutableDictionary dictionary];
    [info setValue:exception.name forKey:@"ExceptionName"];
    [info setValue:exception.reason forKey:@"ExceptionReason"];
    [info setValue:exception.callStackReturnAddresses forKey:@"ExceptionCallStackReturnAddresses"];
    [info setValue:exception.callStackSymbols forKey:@"ExceptionCallStackSymbols"];
    [info setValue:exception.userInfo forKey:@"ExceptionUserInfo"];
    
    NSError *error = [[NSError alloc] initWithDomain:@"com.payu.bizReactNativeWrapper" code:100 userInfo:info];
    return error;
}


#pragma mark - Private Methods

+(void)setAdditionalParamIn:(PayUPaymentParam *) paymentParam fromPaymentParamDict:(NSDictionary *) paymentParamDict {
    NSMutableDictionary *additionalParamDict = [paymentParamDict objectForKey:@"additionalParam"];
    [additionalParamDict addEntriesFromDictionary:[self analyticsDict]];
    if ([additionalParamDict isKindOfClass:[NSDictionary class]]) {
        paymentParam.additionalParam = [additionalParamDict removeNullKeys];
    }
}

+(NSString *)checkoutProReactVersion {
    @try {
        NSBundle* bundle = [self getVersionPlistBundle];
        NSString *path = [bundle pathForResource: @"PayUBizSdkInfo" ofType: @"plist"];
        NSDictionary *dict = [NSDictionary dictionaryWithContentsOfFile: path];
        NSString *version = [dict objectForKey: @"SDKVersion"];
        return version;
    }
    @catch(NSException *e) {
        return @"";
    }
}

+(NSBundle *)getVersionPlistBundle {
  @try {
    NSBundle* bundle = [[NSBundle alloc] initWithURL: [NSBundle URLForResource:@"PayUResource" withExtension:@"bundle" subdirectory:nil inBundleWithURL:[[NSBundle bundleForClass:[self class]] bundleURL]]];
    return bundle;
  } @catch (NSException *exception) {
    return [NSBundle mainBundle];
  }
}

+(NSDictionary *)analyticsDict {
    NSDictionary *reactAnalyticsDict = @{
        @"platform": @"iOS",
        @"name": @"react",
        @"version": [self checkoutProReactVersion],
    };
    NSArray *reactAnalyticsArray = @[reactAnalyticsDict];
    NSString *reactAnalyticsStringyfied = [self stringifyJSON: reactAnalyticsArray];
    return @{
        @"analyticsData":reactAnalyticsStringyfied,
    };
}

+(NSString *)stringifyJSON:(id)json {
    @try {
        NSData *jsonData = [NSJSONSerialization dataWithJSONObject:json options:0 error:nil];
        return  [[NSString alloc] initWithData:jsonData encoding:NSUTF8StringEncoding];
    }
    @catch(NSException *e) {
        return @"";
    }
}

+(NSDate *)getConvertedDate:(NSString*)dateInString {
  NSDateFormatter *dateFormatter = [[NSDateFormatter alloc] init];
  [dateFormatter setDateFormat:@"yyyy-MM-dd"];
  return [dateFormatter dateFromString:dateInString];
}

+(PayUSIParams *)getPayUSIParams:(NSDictionary*)dict {
  NSString *billingAmount = dict[@"billingAmount"];
  NSNumber *billingInterval = dict[@"billingInterval"];
  NSDate *paymentStartDate = [self getConvertedDate:dict[@"paymentStartDate"]];
  NSDate *paymentEndDate = [self getConvertedDate:dict[@"paymentEndDate"]];
  
  PayUSIParams *siParam = [[PayUSIParams alloc] initWithBillingAmount:billingAmount
                                                     paymentStartDate:paymentStartDate
                                                       paymentEndDate:paymentEndDate
                                                         billingCycle: [self getBillingCycle:dict[@"billingCycle"]]
                                                      billingInterval:billingInterval];
  siParam.remarks = dict[@"remarks"];

  NSNumber *isFreeTrial = dict[@"isFreeTrial"];
  if (isFreeTrial != nil) {
    siParam.isFreeTrial = [isFreeTrial boolValue];
  }

  return siParam;
}

+(PayUBillingCycle) getBillingCycle:(NSString*)string {
  enum PayUBillingCycle billingCycle;
  if ([string isEqualToString:@"daily"]) {
    billingCycle = PayUBillingCycleDaily;
  } else  if ([string isEqualToString:@"weekly"]) {
    billingCycle = PayUBillingCycleWeekly;
  } else  if ([string isEqualToString:@"yearly"]) {
    billingCycle = PayUBillingCycleYearly;
  } else  if ([string isEqualToString:@"adhoc"]) {
    billingCycle = PayUBillingCycleAdhoc;
  } else  if ([string isEqualToString:@"monthly"]) {
    billingCycle = PayUBillingCycleMonthly;
  } else {
    billingCycle = PayUBillingCycleOnce;
  }
  return billingCycle;
}



+(NSArray<NSNumber *> *)getPaymentTypes:(NSArray*)array {
  NSMutableArray<NSNumber *> *paymentTypes = [NSMutableArray new];
  for (NSString *paymentType in array) {
    if ([paymentType caseInsensitiveCompare:@"cards"] == NSOrderedSame){
      [paymentTypes addObject:[NSNumber numberWithInt:PaymentTypeCcdc]];
    } else if ([paymentType caseInsensitiveCompare:@"net banking"] == NSOrderedSame) {
      [paymentTypes addObject:[NSNumber numberWithInt:PaymentTypeNetBanking]];
    } else if ([paymentType caseInsensitiveCompare:@"upi"] == NSOrderedSame) {
      [paymentTypes addObject:[NSNumber numberWithInt:PaymentTypeUpi]];
    } else if ([paymentType caseInsensitiveCompare:@"wallets"] == NSOrderedSame) {
      [paymentTypes addObject:[NSNumber numberWithInt:PaymentTypeWallet]];
    } else if ([paymentType caseInsensitiveCompare:@"emi"] == NSOrderedSame) {
      [paymentTypes addObject:[NSNumber numberWithInt:PaymentTypeEmi]];
    }
  }
  return paymentTypes;
}
@end
