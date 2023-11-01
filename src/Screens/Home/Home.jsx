import React from 'react';
import {View} from 'react-native';
import TodoInputSection from '../Todo/TodoInputSection';
import GuessTheNumberGame from '../GuessTheNumberGame/GuessTheNumberGame';

const Home = () => {
  return (
    <View>
      {/* <TodoInputSection /> */}
      <GuessTheNumberGame />
    </View>
  );
};

export default Home;
