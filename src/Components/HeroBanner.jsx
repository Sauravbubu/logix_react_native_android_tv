import React from 'react';
import {View, Image, StyleSheet, Dimensions, Text} from 'react-native';
import RadialGradient from 'react-native-radial-gradient';
import LogoWithText from '../assets/Logituit_logo.png';

const {width, height} = Dimensions.get('window');

const HeroBanner = ({imageUrl, title, description}) => {
  return (
    <>
      <View focusable={false} style={styles.heroBanner}>
        <Image
          source={{uri: imageUrl}}
          style={styles.heroBannerImage}
          resizeMode="cover"
        />
      </View>
      <RadialGradient
        style={{width, height: height, position: 'absolute'}}
        colors={['rgba(13, 11, 37, 0)', '#0d0b25a6', 'rgba(15, 12, 50, 0.981)']}
        stops={[0.2, 0.5, 0.6]}
        center={[width + 29, height / 10]}
        radius={width}
      />
      <View focusable={false} style={styles.textContainer}>
        <Text style={styles.titleText}>{title}</Text>
        <Text style={styles.descriptionText}>{description}</Text>
      </View>
      <View focusable={false} style={styles.logoContainer}>
        <Image style={styles.logoImage} source={LogoWithText} />
      </View>
    </>
  );
};

const styles = StyleSheet.create({
  heroBanner: {
    position: 'absolute',
    top: 0,
    left: 0,
    width: width,
    height: height,
    zIndex: -1, // Place it behind other components
  },
  heroBannerImage: {
    flex: 1,
    width: '100%',
    height: height,
  },
  textContainer: {
    position: 'relative',
    top: '40%',
    left: '5%',
  },
  titleText: {
    color: 'white',
    fontSize: 35,
    fontFamily: 'Roboto',
  },
  descriptionText: {
    color: 'white',
    width: '60%',
    marginLeft: 5,
  },
  logoContainer: {
    position: 'relative',
    width: '7%',
    height: '7%',
    left: '5%',
  },
  logoImage: {
    width: '100%',
    height: '100%',
    resizeMode: 'center',
  },
});

export default HeroBanner;
