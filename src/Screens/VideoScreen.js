// VideoScreen.js
import React from 'react';
import {View} from 'react-native';
import Video from 'react-native-video';

const VideoScreen = ({route}) => {
  const {videoUrl} = route.params;

  return (
    <View>
      <Video source={{uri: videoUrl}} />
    </View>
  );
};

export default VideoScreen;
