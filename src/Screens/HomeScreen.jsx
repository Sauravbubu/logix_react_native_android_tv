import React, {useState} from 'react';
import {
  View,
  Image,
  Text,
  ActivityIndicator,
  Dimensions,
  ScrollView,
} from 'react-native';
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
  const [focusedCard, setFocusedCard] = useState(null); // State to store focused card data

  if (loading) {
    return (
      <View
        style={{
          width,
          height,
          backgroundColor: 'black',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
        }}>
        {/* <ActivityIndicator size="large" /> */}
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
    <ScrollView>
      {focusedCard && (
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
      )}

      <ScrollView style={{marginTop: '30%', marginLeft: '5%'}}>
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
