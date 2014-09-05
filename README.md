# TiPebble

Implements basic features of the Pebble iOS SDK.

## Downloading [![gitTio](http://gitt.io/badge.png)](http://gitt.io/component/org.beuckman.tipebble)

Download the latest distribution ZIP-file and consult the [Titanium Documentation](http://docs.appcelerator.com/titanium/latest/#!/guide/Using_a_Module) on how to install it, or simply use the [gitTio CLI](http://gitt.io/cli):

`$ gittio install org.beuckman.tipebble`

## Sample

An example [Titanium Mobile](https://github.com/mcongrove/TiPebble-Example-Mobile) application and [Pebble](https://github.com/mcongrove/TiPebble-Example-Pebble) application are available for download.

## Documentation

Aside from the Quick Start guide below, you can also view the full module reference in the [documentation](https://github.com/mcongrove/TiPebble/blob/master/documentation/index.md).

## Credits

TiPebble developed by [Joe Beuckman](https://github.com/jbeuckm) with contributions from [Matthew Congrove](https://github.com/mcongrove).

## Quick Start

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
		Ti.API.info("Connected to Pebble");
	},
	error: function(_event) {
		Ti.API.error("Cannot Connect to Pebble");
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
		Ti.API.info("Pebble Application Launched");
	},
	error: function(_event) {
		Ti.API.error("Could Not Launch Pebble Application");
	}
});
```

##### Recieve Messages from Pebble #####

After you've connected, you can add an event listener to start watching for messages from the Pebble:

```
pebble.addEventListener("update", watchMessageReceived);

function watchMessageReceived(_message) {
	Ti.API.info("Message Received: " + _message.message);
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
	
	static char message[64] = "Hello, mobile app!";
	
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
	
	const uint32_t inbound_size = app_message_inbox_size_maximum();
	const uint32_t outbound_size = app_message_outbox_size_maximum();
	app_message_open(inbound_size, outbound_size);
}
```