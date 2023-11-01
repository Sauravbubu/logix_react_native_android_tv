import React from 'react';
import {Text, TextInput, View} from 'react-native';
import {style} from '../style';
import CommonButton from './Button';

const Card = ({
  setDisplaySelection,
  selectedNumber,
  setSelectedNumber,
  displaySelection,
  setNextPage,
}) => {
  const handleReset = () => {
    setSelectedNumber(''); // Reset the selectedNumber state to an empty string
  };

  return (
    <View>
      <Text style={style.commonText}>Start a New Game</Text>
      <View style={style.card}>
        <Text>Select a Number</Text>
        <TextInput
          style={style.TextBoxView}
          value={selectedNumber}
          onChangeText={text => {
            if (!text) {
              setDisplaySelection(false);
            }
            setSelectedNumber(text);
          }}
          keyboardType="numeric"
          placeholder="Enter a Number"
        />

        <View style={style.ButtonGroup}>
          <CommonButton onPress={handleReset} title="Reset" color="#ec19a2" />
          <CommonButton
            onPress={() => {
              console.log(selectedNumber, 'selectedNumber');
              if (
                selectedNumber &&
                !isNaN(selectedNumber) &&
                selectedNumber >= 0 &&
                selectedNumber <= 100
              ) {
                setDisplaySelection(true);
              }
            }}
            title="Confirm"
            color="#ec19a2"
          />
        </View>
        {displaySelection && (
          <View style={style.card}>
            <Text>You selected</Text>
            <Text style={style.TextBoxView}>{selectedNumber}</Text>
            <CommonButton
              onPress={() => {
                setNextPage(true);
              }}
              title="Start Game"
              color="#ec19a2"
            />
          </View>
        )}
      </View>
    </View>
  );
};

export default Card;
