# Cordova VideoCall Plugin With OpentokSDK

This plugin performs video call between users, using opentok sdk.



## Using
Clone the plugin

    $ https://github.com/amitrai98/VideoCallPlugin.git

Create a new Cordova Project

    $ cordova create hello com.example.videoexample DemoVideoCalling
    
Install the plugin

    $ cd DemoVideoCalling
    $ cordova plugin add ../VideoCallPlugin.git
    

Edit `www/js/index.js` and add the following code inside `onDeviceReady`

```js
    var success = function(message) {
        alert(message);
    }

    var failure = function() {
        alert("Error calling Video Plugin");
    }

 var jsonObj = {//data required for video calling 
                }

    VideoPlugin.initializeVideoCalling(jsonObj, success, failure);
```

Install iOS or Android platform

    cordova platform add ios
    cordova platform add android
    
Run the code

    cordova run 

## More Info

For more information on setting up Cordova see [the documentation](http://cordova.apache.org/docs/en/4.0.0/guide_cli_index.md.html#The%20Command-Line%20Interface)

For more info on plugins see the [Plugin Development Guide](http://cordova.apache.org/docs/en/4.0.0/guide_hybrid_plugins_index.md.html#Plugin%20Development%20Guide)
