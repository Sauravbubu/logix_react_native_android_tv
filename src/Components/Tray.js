// Modify Tray.js
import React, {useState} from 'react';
import {
  View,
  ScrollView,
  Text,
  TouchableOpacity,
  Dimensions,
  StyleSheet,
} from 'react-native';
import Card from './Card';
import {Image} from 'react-native';
import Logo from '../assets/logo.png';
const Tray = ({title, cardDataTray, cards, setFocusedCard, onCardBlur}) => {
  const {width, height} = Dimensions.get('window');

  const handleCardFocus = cardData => {
    if (setFocusedCard) {
      setFocusedCard(cardData);
    }
  };

  const handleCardBlur = () => {
    if (onCardBlur) {
      onCardBlur();
    }
  };

  return (
    <View>
      <View
        style={{
          width:'20%',
          display: 'flex',
          flexDirection: 'row',
          alignItems: 'center',
        }}>
        <Image style={{width: 20, height: 20}} source={Logo} />
        <Text style={{color: 'white'}}>{title}</Text>
      </View>
      <ScrollView horizontal>
        {cards.map((card, index) => (
          <Card
            cardDataTray={cardDataTray}
            key={index}
            data={card}
            onPress={() => {}}
            onFocus={() => handleCardFocus(card)} // Handle card focus
            onBlur={handleCardBlur} // Handle card blur
          />
        ))}
      </ScrollView>
    </View>
  );
};

export default Tray;
