const {StyleSheet} = require('react-native');

export const style = StyleSheet.create({
  TopSection: {
    display: 'flex',
    // justifyContent: 'center',
    alignItems: 'center',
    // // width: '100%',
    // position: 'absolute',
    // top: 0,
    // paddingLeft: '20%',
  },
  FlattListWrapper: {paddingLeft: '20%', width: '100%'},
  addButton: {width: '20%'},
  gameTitleText: {
    width: '100%',
    height: '10',
    textAlign: 'center',
    backgroundColor: 'red',
    color: 'white',
  },
  TextBoxView: {
    padding: 10,
    marginTop: 20,
    borderRadius: 10,
    borderWidth: 1,
    width: 100,
    textAlign: 'center',
    borderColor: 'black',
  },
  card: {
    borderWidth: 0.4,
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    padding: 10,
    width: '80%',
    marginLeft: 40,
    borderRadius: 30,
  },
  commonText: {textAlign: 'center', fontWeight: '900'},
  shadowProp: {
    shadowColor: 'black',
    shadowOffset: {width: -2, height: 4},
    shadowOpacity: 0.2,
    shadowRadius: 3,
  },
  ButtonGroup: {
    display: 'flex',
    justifyContent: 'space-between',
    width: '80%',
    flexDirection: 'row',
    marginTop: 10,
    marginBottom: 10,
  },

  CommonButton: {
    width: 100,
    backgroundColor: '#ec19a2',
    paddingHorizontal: 10,
    paddingVertical: 10,
    borderRadius: 10,
    display: 'flex',
    alignContent: 'center',
    justifyContent: 'center',
  },
  historyListCard: {
    display: 'flex',
    backgroundColor: 'orange',
    flexDirection: 'row',
    width: '70%',
    padding: '5%',
    marginBottom: '2%',
    justifyContent: 'space-between',
  },
});