import React, {useState} from 'react';
import {
  View,
  Image,
  Text,
  ScrollView,
  StyleSheet,
  TouchableOpacity,
  Dimensions,
} from 'react-native';
import RadialGradient from 'react-native-radial-gradient';
import Cast from '../assets/cast.png';
import Logo from '../assets/Logituit_logo.png';
import {Color, FontSize} from '../GlobalStyles';
const {width, height} = Dimensions.get('window');
const Carousel = ({data, onClick}) => {
  const {width, height} = Dimensions.get('window');
  const [currentIndex, setCurrentIndex] = useState(0);
  return (
    <>
      <ScrollView
        horizontal
        pagingEnabled
        showsHorizontalScrollIndicator={false}
        onMomentumScrollEnd={event => {
          const contentOffset = event.nativeEvent.contentOffset;
          const newPageIndex = Math.round(contentOffset.x / 320);
          setCurrentIndex(newPageIndex);
        }}>
        {data.map((item, index) => (
          <View key={index} style={styles.carouselItem}>
            <Image
              source={{uri: item.thumbnail_urls[0].img1}}
              style={styles.image}
            />
            <RadialGradient
              style={{width, height: height - 200, position: 'absolute'}}
              colors={[
                'rgba(13, 11, 37, 0)',
                '#0d0b2582',
                'rgba(15, 12, 50, 0.981)',
              ]}
              stops={[0.2, 0.5, 0.6]}
              center={[width + 15, height / 10]}
              radius={width + 100}
            />
            <View
              style={{
                display: 'flex',
                position: 'absolute',
                top: '80%',
                zIndex: 999,
              }}>
              <Text style={styles.title}>{item.thumbnametitle}</Text>
              {item?.duration && (
                <Text style={[styles.description]}>{item.duration}</Text>
              )}
              <TouchableOpacity onPress={onClick(item)} style={styles.button}>
                <Text style={styles.buttonText}>Play Now</Text>
              </TouchableOpacity>
            </View>
          </View>
        ))}
      </ScrollView>
      <View
        style={{
          position: 'absolute',
          width: width,
          paddingHorizontal: 20,
          display: 'flex',
          flexDirection: 'row',
          justifyContent: 'space-between',
        }}>
        <Image
          style={{width: 40, height: 40, resizeMode: 'contain'}}
          source={Logo}
        />
        <Image style={{width: 40, height: 40}} source={Cast} />
      </View>
    </>
  );
};

const styles = StyleSheet.create({
  carouselItem: {
    width,
    height: height - 200,
    alignItems: 'center',
    justifyContent: 'center',
  },
  description: {
    fontSize: FontSize.androidDescripion_size,
    color: Color.logituitWhite700,
    // marginLeft: '3%',
  },
  image: {
    width: '100%',
    height: height - 200,
    resizeMode: 'cover',
  },
  title: {
    fontSize: 16,
    // marginTop: 10,
    textAlign: 'center',
    color: 'white',
  },
  button: {
    // marginTop: 20,
    // backgroundColor: 'white',
    padding: 10,
    borderWidth: 2,
    borderColor: 'white',
    borderRadius: 5,
    marginTop: 10,
  },
  buttonText: {
    color: 'white',
    fontWeight: 'bold',
    textAlign: 'center',
  },
});

export default Carousel;
