# react-native-fingerprint
React Native Fingerprint Module

## Installation
1.add the following codes to your `your project folder/package.json`
```json
"dependencies": {
  "react-native-fingerprint":"guanaitong/react-native-fingerprint.git#0.0.2",
}
```

2.use command
```
$npm install
```

## Android
1.add the following codes to your `android/settings.gradle`
```js
include ':app', ':react-native-fingerprint'
project(':react-native-fingerprint').projectDir = new File(rootProject.projectDir, '../node_modules/react-native-fingerprint/android/app')
```

2.edit `android/app/build.gradle` and add the following line inside `dependencies`
```js
    compile project(':react-native-fingerprint')
```

3.add the following import to `MainApplication.java` of your application
```java
import com.reactnativefingerprint.FingerprintPackage;
```

4.add the following code to add the package to `MainApplication.java`
```java
@Override
    protected List<ReactPackage> getPackages() {
      return Arrays.<ReactPackage>asList(
              new MainReactPackage(),
              new FingerprintPackage() //<- this
      );
    }
```

5.run `react-native run-android` to see if everything is compilable.

## iOS
1.Automatic linking:
use command to link react-native-fingerprint

```
$rnpm link 
```

Or you can manual linking
you can reference http://facebook.github.io/react-native/docs/linking-libraries-ios.html 

2.Then add the following libraries to your "Link Binary with Libraries":`LocalAuthentication.framework`

3.run `react-native run-ios` to see if everything is compilable.

## Usage
just import the module
```js
import GATFingerprint from 'react-native-fingerprint';
```
#### isSupport()
whether support fingerprint API in the phone system .return type is Promise .Container `isSupport`,`errorCode`,`errorMessage`,
###### isSupport
Boolean
###### errorCode
Integer  
0:support.  
101:current mobile version is low.  
102:not set up a fingerprint.
###### errorMessage
String

#### startTouch(eventName,iosPrompt)
start listener for fingerprint .`eventName` is React Native EventEmitter name.`iosPrompt` is IOS Touch id Alert Dialog prompt information..return type is Promise .Container `isSuccess`,`errorCode`,`errorMessage`,
##### Android
###### isSuccess
Boolean
###### errorCode
Integer  
0:success.  
7:Too many attempts.
101:fingerprint failed.  
1011:finger put time is short.
###### errorMessage
String

##### iOS
###### isSuccess
Boolean
###### errorCode
Integer  

 0:success.
 
-1: 连续三次指纹识别错误

-2: 在TouchID对话框中点击了取消按钮

-3: 在TouchID对话框中点击了输入密码按钮

-4: TouchID对话框被系统取消，例如按下Home或者电源键

-8: 连续五次指纹识别错误，TouchID功能被锁定，下一次需要输入系统密码

###### errorMessage
String

## Simple Example
#### Android
```js
componentDidMount(){
  DeviceEventEmitter.addListener('fingerprintCallBack', this.fingerprintCallBack);
}

async  _isSupport(){
  try {
    var e = await GATFingerprint.isSupport();
      ToastAndroid.show(JSON.stringify(e),ToastAndroid.SHORT);
  } catch (e) {
    Alert.alert(JSON.stringity(e));
  }
}

 _startTouch(){
 this.setState({
     textvalue:this.state.textvalue+'\nstartTouch',
   });
  GATFingerprint.startTouch('fingerprintCallBack','ios propmpt');
}

fingerprintCallBack(e: Event){
  ToastAndroid.show(JSON.stringify(e),ToastAndroid.SHORT);
}
```

#### iOS

```js
import {NativeAppEventEmitter} from 'react-native';
import GATFingerprint from 'react-native-fingerprint';
var subscriptionFingerprint;

	componentDidMount() {
		subscriptionFingerprint = NativeAppEventEmitter.addListener(
			'fingerprintCallBack',
			(reminder) => {
				console.log('Fingerprint:'+ JSON.stringify(reminder))
				alert(reminder.errorMessage)
			}
		);
	}
	
	componentWillUnMount() {
		subscriptionFingerprint.remove();
	}

	async _isSupport() {
		try {
			var {
				isSupport,
				errorCode,
				errorMessage,
				} = await GATFingerprint.isSupport();
			console.log(isSupport + '   ' + errorCode + '   ' + errorMessage);
			if (isSupport) {
				GATFingerprint.startTouch('通过Home键验证已有手机指纹');
			} else {
				//TouchID没有设置指纹
				// 关闭密码（系统如果没有设置密码TouchID无法启用）
				console.log('TouchID 设备不可用');
			}
		} catch(e) {
			console.log(e);
		}
	}
	
```
