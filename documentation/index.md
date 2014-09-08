# TitaniumPebble

For a complete guide on integrating TitaniumPebble in your project, please refer to the [README](https://github.com/mcongrove/TitaniumPebble/blob/master/README.md). This document is only a function reference.

## prop: connectedCount

How many Pebble devices are connected to the mobile device.

_Note: On Android, this only returns `0` or `1`_

###### Return
| Type | Value |
|------|-------|
| Number | The number of connected Pebbles |

## func: setAppUUID

Sets the Pebble application UUID.

###### Parameters
| Name | Type | Value |
|------|------|-------|
| uuid | String | The Pebble application UUID |

###### Return
_None_

## func: checkWatchConnected

Checks if a Pebble is connected to the mobile device.

###### Parameters
_None_

###### Return
| Type | Value |
|------|-------|
| Boolean | Whether a watch is connected |

## func: connect

Connect to the Pebble watch.

###### Parameters
| Name | Type | Value |
|------|------|-------|
| params | Object |  |
| params.success | Function | The success callback |
| params.error | Function | The error callback |

###### Return
_None_


## func: getVersionInfo

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


## func: launchApp

Launches the Pebble application on the watch.

###### Parameters
| Name | Type | Value |
|------|------|-------|
| params | Object |  |
| params.success | Function | The success callback |
| params.error | Function | The error callback |

###### Return
_None_

## func: killApp

Closes the Pebble application on the watch.

###### Parameters
| Name | Type | Value |
|------|------|-------|
| params | Object |  |
| params.success | Function | The success callback |
| params.error | Function | The error callback |

###### Return
_None_

## func: sendMessage

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