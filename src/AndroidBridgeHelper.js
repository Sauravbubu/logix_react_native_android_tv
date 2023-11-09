import {NativeModules} from 'react-native';
const AndroidBridge = NativeModules.AndroidBridge;

export const openExampleActivity = video_url => {
  AndroidBridge.navigateToLivePlayer(video_url);
};
