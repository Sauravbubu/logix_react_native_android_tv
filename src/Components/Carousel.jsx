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

const {width, height} = Dimensions.get('window');
const Carousel = ({data}) => {
  const [currentIndex, setCurrentIndex] = useState(0);
  return (
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
          <View
            style={{
              display: 'flex',
              position: 'absolute',
              top: '60%',
              zIndex: 999,
            }}>
            <Text style={styles.title}>{item.thumbnametitle}</Text>
            <TouchableOpacity style={styles.button}>
              <Text style={styles.buttonText}>Play Now</Text>
            </TouchableOpacity>
          </View>
        </View>
      ))}
    </ScrollView>
  );
};

const styles = StyleSheet.create({
  carouselItem: {
    width,
    // height: height / 2,
    alignItems: 'center',
    justifyContent: 'center',
  },
  image: {
    width: '100%',
    height: height / 3,
    resizeMode: 'contain',
  },
  title: {
    fontSize: 16,
    // marginTop: 10,
    textAlign: 'center',
    color: 'white',
  },
  button: {
    // marginTop: 20,
    backgroundColor: 'white',
    padding: 10,
    borderRadius: 5,
  },
  buttonText: {
    // color: 'white',
    fontWeight: 'bold',
    textAlign: 'center',
  },
});

export default Carousel;
