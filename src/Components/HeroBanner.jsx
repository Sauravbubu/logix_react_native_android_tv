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
      <View style={{position: 'relative', top: '40%', left: '5%'}}>
        <Text style={{color: 'white', fontSize: 30}}>
          {title || 'ABSBSBBDS'}
        </Text>
        <Text style={{color: 'white'}}>
          {description || 'jdgsadjashuagjahcsahgd huahd uhda dwjhd '}
        </Text>
      </View>
      <View
        style={{
          position: 'relative',
          width: '10%',
          height: '10%',
          left: '5%',
        }}>
        <Image
          style={{width: '100%', height: '100%', resizeMode: 'cover'}}
          source={LogoWithText}
        />
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
    // marginLeft: '20%'    ,
    width: '100%',
    height: height,
  },
});

export default HeroBanner;
