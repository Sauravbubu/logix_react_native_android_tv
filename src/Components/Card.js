import React, {useState} from 'react';
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
      onPressIn={() => onPress(data.video_url)}
      onFocus={handleFocus}
      onBlur={handleBlur}
      onMouseEnter={handleFocus}
      onMouseLeave={handleBlur}>
      <View
        style={[
          styles.cardContainer,
          isFocused && styles.focused, // Apply focused style when isFocused is true
        ]}>
        <Image
          source={
            imageSource || {
              uri: thumbnail_url || 'https://i.imgur.com/66xXWPa.jpg',
            }
          }
          style={[styles.image, imageStyle]}
        />
        <Text style={[styles.title, titleStyle]}>
          {title || data.title || 'Test'}
        </Text>
      </View>
    </TouchableWithoutFeedback>
  );
};

const styles = StyleSheet.create({
  cardContainer: {
    borderRadius: 30,
    margin: 8,
    shadowColor: 'black', 
    shadowRadius: 2,
    width: 250,
    height: 150,
  },
  focused: {
    borderColor: 'white',
    borderWidth: 4,
  },
  image: {
    borderRadius: 30,
    width: '100%',
    height: '100%',
    resizeMode: 'cover',
  },
  title: {
    fontSize: FontSize.cardTitle,
    color: Color.logituitWhite700,
    marginTop: 8,
  },
});

export default Card;
