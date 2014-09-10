# TitaniumPebble

For a complete guide on integrating TitaniumPebble in your project, please refer to the [README](https://github.com/mcongrove/TitaniumPebble/blob/master/README.md). This document is only a function reference.

 * [Properties](https://github.com/mcongrove/TitaniumPebble/blob/master/documentation/index.md#properties)
 * [Functions](https://github.com/mcongrove/TitaniumPebble/blob/master/documentation/index.md#functions)
 * [Events](https://github.com/mcongrove/TitaniumPebble/blob/master/documentation/index.md#events)

## Properties

#### connectedCount

How many Pebble devices are connected to the mobile device.

_Note: On Android, this only returns `0` or `1`_

###### Return

| Type | Value |
|------|-------|
| Number | The number of connected Pebbles |

## Functions

#### setAppUUID

Sets the Pebble application UUID.

###### Parameters

| Name | Type | Value |
|------|------|-------|
| uuid | String | The Pebble application UUID |

###### Return

_None_

#### checkWatchConnected

Checks if a Pebble is connected to the mobile device.

###### Parameters

_None_

###### Return

| Type | Value |
|------|-------|
| Boolean | Whether a watch is connected |

#### connect

Connect to the Pebble watch.

###### Parameters

| Name | Type | Value |
|------|------|-------|
| params | Object |  |
| params.success | Function | The success callback |
| params.error | Function | The error callback |

###### Return

_None_


#### getVersionInfo

Retrieves version information from the Pebble.

###### Parameters

| Name | Type | Value |
|------|------|-------|
| params | Object |  |
| params.success | Function | The success callback |
| params.error | Function | The error callback |

###### Return

| Type | Value |
|------|-------|
| Object | The version information |


#### launchApp

Launches the Pebble application on the watch.

###### Parameters

| Name | Type | Value |
|------|------|-------|
| params | Object |  |
| params.success | Function | The success callback |
| params.error | Function | The error callback |

###### Return

_None_

#### killApp

Closes the Pebble application on the watch.

###### Parameters

| Name | Type | Value |
|------|------|-------|
| params | Object |  |
| params.success | Function | The success callback |
| params.error | Function | The error callback |

###### Return

_None_

#### sendMessage

Sends a message to the Pebble.

_Note: On Android, the `success` and `error` callback are not fired. Instead, a general `ACK` or `NACK` is received_

###### Parameters

| Name | Type | Value |
|------|------|-------|
| params | Object |  |
| params.message | Object | The message object (see README for formatting example) |
| params.success | Function | The success callback |
| params.error | Function | The error callback |

###### Return

_None_

## Events

#### watchConnected

Fires when a Pebble connects to the device.

_Note: The Pebble only sends connect/disconnect events when the watch pairing status changes or the watch enter/leaves the range of the phone. For instance, the connect event does not fire if the Pebble is already connected to your phone and you then launch the application._

__Event Data__

_None_

#### watchDisconnected

Fires when a Pebble disconnects from the device.

_Note: The Pebble only sends connect/disconnect events when the watch pairing status changes or the watch enter/leaves the range of the phone. For instance, the connect event does not fire if the Pebble is already connected to your phone and you then launch the application._

__Event Data__

_None_

#### update

Fires when a Pebble disconnects from the device.

_Note: The Pebble only sends connect/disconnect events when the watch pairing status changes or the watch enter/leaves the range of the phone. For instance, the connect event does not fire if the Pebble is already connected to your phone and you then launch the application._

__Event Data__

| Type | Value |
|------|-------|
| Object | The message data |
