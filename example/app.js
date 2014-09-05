var pebble = require("org.beuckman.tipebble");

// Pebble demo UUID; change to the UUID of your Pebble application
pebble.setAppUUID("226834ae-786e-4302-a52f-6e7efc9f990b");

function watchConnected(e) {
	Ti.API.info("Watch Connected");
}

function watchDisconnected(e) {
	Ti.API.info("Watch Disconnected");
}

function watchMessageReceived(_message) {
	Ti.API.info("Message Received: " + _message.message);
}

pebble.addEventListener("watchConnected", watchConnected);
pebble.addEventListener("watchDisconnected", watchDisconnected);
pebble.addEventListener("update", watchMessageReceived);

pebble.connect({
	success: function(_event) {
		alert("Connected to Pebble");
	},
	error: function(_event) {
		alert("Cannot connect to Pebble");
	}
});