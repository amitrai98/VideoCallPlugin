/*global cordova, module*/

module.exports = {
     initializeVideoCalling: function (object, successCallback, errorCallback) {
            cordova.exec(successCallback, errorCallback, "VideoPlugin", "initializeVideoCalling", [object]);
    },
    endCalling: function (messageInReturn,successCallback, errorCallback) {
    cordova.exec(successCallback, errorCallback, "VideoPlugin", "endCalling", [messageInReturn]);
},
    showLowBalanceWarning: function (message,successCallback, errorCallback) {
    cordova.exec(successCallback, errorCallback, "VideoPlugin", "showLowBalanceWarning", [message]);
},
    getUserBalance: function (balance,successCallback, errorCallback) {
    cordova.exec(successCallback, errorCallback, "VideoPlugin", "getUserBalance", [balance]);
},
    receivedResponseFromAPI: function (responseType,status,userBalance,successCallback, errorCallback) {
    cordova.exec(successCallback, errorCallback, "VideoPlugin", "receivedResponseFromAPI", [responseType,status,userBalance]);
},
    tipReceived: function (tipAmount,successCallback, errorCallback) {
    cordova.exec(successCallback, errorCallback, "VideoPlugin", "tipReceived", [tipAmount]);
}
               
};