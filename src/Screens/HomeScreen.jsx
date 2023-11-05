import React, {useState, useEffect} from 'react';
import {View, Image, Text, ScrollView, Dimensions} from 'react-native';
import Carousel from '../Components/Carousel';
import Tray from '../Components/Tray';
import useFetch from '../Hooks/useFetch';
import {cardsApi, catchupApi} from '../constant';
import HeroBanner from '../Components/HeroBanner';
import Logo from '../assets/Logituit_logo.png';

const HomeScreen = ({navigation}) => {
  const {width, height} = Dimensions.get('window');
  const {data, loading, error} = useFetch(cardsApi);
  const {
    data: dataFromCatchupApi,
    loading: loadingForCatchup,
    error: errorForCatchup,
  } = useFetch(catchupApi);
  const [focusedCard, setFocusedCard] = useState(null);

  // Check screen width to determine if the app is running on TV
  const isTV = width >= 800; // You can adjust this width threshold based on your requirements
  if (loading) {
    return (
      <View
        style={{
          width,
          height: '100%',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
        }}>
        <Image
          style={{width: 200, height: 200, resizeMode: 'contain'}}
          source={Logo}
        />
      </View>
    );
  }

  if (error) {
    return (
      <View>
        <Text>Error: {error.message}</Text>
      </View>
    );
  }

  return (
    <ScrollView style={{height}}>
      {isTV ? (
        focusedCard && (
          <HeroBanner
            title={focusedCard.thumbnametitle}
            description={focusedCard.title}
            imageUrl={
              focusedCard
                ? focusedCard?.thumbnail_urls[2]?.img3 ||
                  focusedCard.thumbnail_url
                : require('../assets/Logituit_logo.png')
            }
          />
        )
      ) : (
        <Carousel data={data} />
      )}

      <ScrollView style={{marginTop: isTV && '30%', marginLeft: '5%'}}>
        {!loadingForCatchup && (
          <Tray
            title={'Live'}
            cardDataTray={true}
            setFocusedCard={setFocusedCard}
            cards={data}
          />
        )}
        {!loadingForCatchup && (
          <Tray
            title={'Catch Up'}
            setFocusedCard={setFocusedCard}
            cards={dataFromCatchupApi}
          />
        )}
      </ScrollView>
    </ScrollView>
  );
};

export default HomeScreen;
