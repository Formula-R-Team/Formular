define(function (require) {
  // var exec = cordova.require('cordova/exec');
  // var Game = require('./game');
  ons.ready(function() {
     exec(function(result) { ons.notification.alert(result); }, function(error) { ons.notification.alert(error) }, 'Formular', 'hello', []);
  });
});
