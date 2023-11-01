import {useNavigation} from '@react-navigation/native';
import {
  Text,
  View,
  ScrollView,
  TouchableWithoutFeedback,
  Image,
  Dimensions,
} from 'react-native';

const {width, height} = Dimensions.get('window');
export default function CatchUpRail({title, data}) {
  console.log('Helllooo:::', data[1]?.video_url);
  const navigation = useNavigation();
  return (
    <View className="mb-8 space-y-4">
      <View className="mx-4 flex-row justify-between items-center">
        <Text className="text-white text-xl">{title}</Text>
      </View>
      <ScrollView
        horizontal
        showsHorizontalScrollIndicator={false}
        contentContainerStyle={{paddingHorizontal: 15}}>
        {data.map((data, index) => {
          return (
            <TouchableWithoutFeedback
              key={index}
              onPress={() => {
                navigation.navigate('Player', {playbackUrl: data?.video_url});
              }}>
              <View className="space-y-1 mr-4">
                <Image
                  className="rounded-3xl"
                  source={{uri: data?.['thumbnail_urls']?.[0]?.['605x340']}}
                  style={{width: width * 0.8, height: height * 0.22}}
                />
              </View>
            </TouchableWithoutFeedback>
          );
        })}
      </ScrollView>
    </View>
  );
}