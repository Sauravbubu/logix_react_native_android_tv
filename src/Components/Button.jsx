import React from 'react';
import {TouchableOpacity, Text} from 'react-native';
import {style} from '../style';

const CommonButton = ({title, onPress, width, color}) => {
  return (
    <TouchableOpacity
      onPress={onPress}
      style={{...style.CommonButton, width: width || 100}}>
      <Text
        style={{
          textAlign: 'center',
          fontWeight: '700',
          backgroundColor: color || '#ec19a2',
          padding: 5,
          borderRadius: 5,
          color: 'white',
        }}>
        {title}
      </Text>
    </TouchableOpacity>
  );
};

export default CommonButton;
