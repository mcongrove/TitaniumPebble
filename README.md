# TiPebble #

[![Build Status](https://travis-ci.org/jbeuckm/TiPebble.png)](https://travis-ci.org/jbeuckm/TiPebble)

Implements basic features of the Pebble iOS SDK.

![Pebble Screenshot](photo1.jpeg)

## Quick Start

[![gitTio](http://gitt.io/badge.png)](http://gitt.io/component/org.beuckman.tipebble)

Download the latest distribution ZIP-file and consult the [Titanium Documentation](http://docs.appcelerator.com/titanium/latest/#!/guide/Using_a_Module) on how install it, or simply use the [gitTio CLI](http://gitt.io/cli):

`$ gittio install org.beuckman.tipebble`

### Usage ###

##### Configuration #####

Add this to your `<ios><plist><dict>` section in `tiapp.xml`:

```
<key>UISupportedExternalAccessoryProtocols</key>
<array>
	<string>com.getpebble.public</string>
</array>
```

To keep the connection to the Pebble active while the application is running in background mode, register a background service in Titanium and also add the following to your `tiapp.xml`:

```
<key>UIBackgroundModes</key>
<array>
	<string>external-accessory</string>
</array>
```

##### Instantiation #####

Import the TiPebble module and provide your Pebble application's UUID:

```
var pebble = require("org.beuckman.tipebble");

// This demo UUID is from the Pebble documentation
pebble.setAppUUID("226834ae-786e-4302-a52f-6e7efc9f990b");
```

##### Connecting to Pebble #####

To connect to the Pebble:

```
pebble.connect({
	success: function(_event) {
		alert("Connected to Pebble");
	},
	error: function(_event) {
		alert("Cannot Connect to Pebble");
	}
});
```

##### Handling Connection Events #####

Respond when the Pebble app connects/disconnects using the following code:

_Note: The Pebble only sends connect/disconnect events when the watch pairing status changes or the watch enter/leaves the range of the phone. For instance, the connect event does not fire if the Pebble is already connected to your phone and you then launch the application._

```
function watchConnected(_event) {
    alert("Watch Connected")
}

function watchDisconnected(_event) {
    alert("Watch Disconnected");
}

pebble.addEventListener("watchConnected", watchConnected);
pebble.addEventListener("watchDisconnected", watchDisconnected);
```

##### Launch Pebble Application #####

To launch your Pebble application on the watch from your mobile application:

```
pebble.launchApp({
	success: function(_event) {
		alert("Pebble Application Launched");
	},
	error: function(_event) {
		alert("Could Not Launch Pebble Application");
	}
});
```

##### Recieve Messages from Pebble #####

After you've connected, you can add an event listener to start watching for messages from the Pebble:

```
pebble.addEventListener("update", watchMessageReceived);

function watchMessageReceived(_message) {
	alert("Message Received: " + _message.message);
}
```

To send messages from the Pebble, use the SDKs provided by Pebble. Here's a simple and incomplete example:

```
static void sendMessageToPhone() {
	DictionaryIterator *iter;
	app_message_outbox_begin(&iter);
	
	if(iter == NULL) {
		return;
	}
	
	static char msg[64];
	
	strcpy(msg, "Hello, mobile app!");
	
	dict_write_cstring(iter, 0, msg);
	dict_write_end(iter);
	
	app_message_outbox_send();
}

static void init() {
	app_message_register_inbox_received(in_received_handler);
	app_message_register_inbox_dropped(in_dropped_handler);
	app_message_register_outbox_sent(out_sent_handler);
	app_message_register_outbox_failed(out_failed_handler);
	
	const uint32_t inbound_size = 64;
	const uint32_t outbound_size = 64;
	app_message_open(inbound_size, outbound_size);
	
	sendMessageToPhone();
}
```

##### Send Messages to Pebble #####

After you've connected, you can send messages from the phone to the Pebble:

```
pebble.sendMessage({
	message: {
		0: "Hi, Pebble!",
		1: 12345
	},
	success: function(_event) {
		alert("Message Sent");
	},
	error: function(_event) {
		alert("Message Failed");
	}
})
```

On the Pebble, use the SDKs provided by Pebble to receive the message. Here's a simple and incomplete example:

```
enum {
	KEY_MSG_A = 0x0,
	KEY_MSG_B = 0x1
};

static void in_received_handler(DictionaryIterator *iter, void *context) {
	Tuple *message_a = dict_find(iter, KEY_MSG_A);
	Tuple *message_b = dict_find(iter, KEY_MSG_B);
	
	message_a ? persist_write_string(KEY_MSG_A, message_a->value->cstring) : false;
	message_b ? persist_write_int(KEY_MSG_B, message_b->value->uint8) : false;
}

static void init() {
	app_message_register_inbox_received(in_received_handler);
	app_message_register_inbox_dropped(in_dropped_handler);
	app_message_register_outbox_sent(out_sent_handler);
	app_message_register_outbox_failed(out_failed_handler);
	
	const uint32_t inbound_size = 64;
	const uint32_t outbound_size = 64;
	app_message_open(inbound_size, outbound_size);
}

```

##### Send Images to Pebble ##### ![caution](http://img.shields.io/badge/experimental-feature-orange.svg)

This requires your Pebble app to implement [image receiving code](https://github.com/jbeuckm/TiPebble/blob/master/example/pebble-app/src/tipebble.c#L50) as appears in the [example Pebble app](https://github.com/jbeuckm/TiPebble-Example-App). Images on the Pebble must have width a multiple of 32 pixels. If your image is not a multiple of 32 pixels wide, a black border will be added to the right, expanding to the next multiple of 32.

```
function sendImage() {
	var file = Ti.Filesystem.getFile(Ti.Filesystem.resourcesDirectory, "image.png");
	
	pebble.sendImage({
		image : file.read(),
		key: 2,
		success: function(_event) {
			alert("Image Sent");
		},
		error : function(_event) {
			alert("Image Failed");
		}
	});
}
```
