import React, {useState, useRef, useCallback} from 'react';
import {
  Button,
  SafeAreaView,
  StyleSheet,
  Text,
  TextInput,
  View,
} from 'react-native';

const MemoizedTextInput = React.memo(TextInput);

const TodoInputSection = () => {
  const [inputText, setInputText] = useState('');
  const [todoList, setTodoList] = useState([]);
  const inputRef = useRef(null);

  const handleClick = useCallback(() => {
    if (inputText) {
      setTodoList(prevList => [...prevList, inputText]);
    }
    setInputText('');

    // Clear the input field using the ref
    if (inputRef.current) {
      inputRef.current.clear();
    }
  }, [inputText]);

  return (
    <SafeAreaView>
      <View style={styles.TodoTopSection}>
        <MemoizedTextInput
          ref={inputRef}
          value={inputText}
          onChangeText={text => {
            setInputText(text);
          }}
          placeholder="Enter Task"
        />
        <Button title="Add" onPress={handleClick} />
      </View>
      {todoList?.map(item => (
        <View>
          <Text key={Math.random()}>{item}</Text>
        </View>
      ))}
    </SafeAreaView>
  );
};

const styles = StyleSheet.create({
  TodoTopSection: {display: 'flex'},
  addButton: {width: '20%'},
});

export default TodoInputSection;
