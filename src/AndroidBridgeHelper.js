import {NativeModules} from 'react-native';
const AndroidBridge = NativeModules.AndroidBridge;

export const openExampleActivity = video_url => {
  AndroidBridge.navigateToAndroidNativeActivity(video_url);
};
