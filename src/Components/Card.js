import React, {useEffect, useState} from 'react';
import {View, Text, Image, TouchableOpacity, StyleSheet} from 'react-native';
import {Color, FontSize} from '../GlobalStyles';
import {TouchableWithoutFeedback} from 'react-native';
import Logo from '../assets/Logituit_logo.png';
const Card = ({
  data,
  cardDataTray,
  onPress,
  fromCarousel,
  imageSource,
  imageStyle,
  title,
  titleStyle,
  onFocus,
  onBlur,
  index,
}) => {
  const thumbnail_url = cardDataTray
    ? data.thumbnail_urls[2].img3
    : data.thumbnail_url;

  const [isFocused, setIsFocused] = useState(false);
  const [imageSrc, setImageSrc] = useState({image: Logo});
  const handleFocus = () => {
    setIsFocused(true);
    if (onFocus) {
      onFocus(index);
    }
  };

  const handleBlur = () => {
    setIsFocused(false);
    if (onBlur) {
      onBlur();
    }
  };

  return (
    <TouchableWithoutFeedback
      onPress={() => onPress(data.video_url)}
      onFocus={handleFocus}
      onBlur={handleBlur}
      onMouseEnter={handleFocus}
      onMouseLeave={handleBlur}>
      <View style={[styles.cardContainer]}>
        <Image
          source={
            imageSource || {
              uri: thumbnail_url || 'https://i.imgur.com/66xXWPa.jpg',
            }
          }
          style={[
            styles.image,
            imageStyle,
            isFocused && styles.focused, // Apply focused style when isFocused is true
          ]}
        />

        <Text style={[styles.title, titleStyle]}>
          {title || data.thumbnametitle || 'Test'}
        </Text>
        {data?.duration && (
          <Text style={[styles.description, titleStyle]}>{data.duration}</Text>
        )}
      </View>
    </TouchableWithoutFeedback>
  );
};

const styles = StyleSheet.create({
  cardContainer: {
    borderRadius: 20,
    margin: 8,
    shadowColor: 'black',
    shadowRadius: 2,
    width: 250,
    height: 200,
  },
  focused: {
    borderColor: 'white',
    borderWidth: 4,
  },
  image: {
    borderRadius: 20,
    width: '100%',
    height: '70%',
    resizeMode: 'cover',
  },
  title: {
    fontSize: FontSize.cardTitle,
    color: Color.tickerWhite,
    marginTop: 8,
    marginLeft: '3%',
  },
  description: {
    fontSize: FontSize.androidDescripion_size,
    color: Color.logituitWhite700,
    marginLeft: '3%',
  },
});

export default Card;
