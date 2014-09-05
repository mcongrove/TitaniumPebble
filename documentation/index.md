# TiPebble

For a complete guide on integrating TiPebble in your project, please refer to the README. This document is only a function reference.

## setAppUUID

Sets the Pebble application UUID.

###### Parameters
| Name | Type | Value |
|------|------|-------|
| uuid | String | The Pebble application UUID |

###### Return
_None_

## checkWatchConnect

Checks if a Pebble is connected to the mobile device.

###### Parameters
_None_

###### Return
| Type | Value |
|------|-------|
| Boolean | Whether a watch is connected |

## connectedCount

How many Pebble devices are connected to the mobile device.

###### Parameters
_None_

###### Return
| Type | Value |
|------|-------|
| Number | The number of connected Pebbles |

## connect

Connect to the Pebble watch.

###### Parameters
| Name | Type | Value |
|------|------|-------|
| params | Object |  |
| params.success | Function | The success callback |
| params.error | Function | The error callback |

###### Return
_None_


## getVersionInfo

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


## launchApp

Launches the Pebble application on the watch.

###### Parameters
| Name | Type | Value |
|------|------|-------|
| params | Object |  |
| params.success | Function | The success callback |
| params.error | Function | The error callback |

###### Return
_None_

## killApp

Closes the Pebble application on the watch.

###### Parameters
| Name | Type | Value |
|------|------|-------|
| params | Object |  |
| params.success | Function | The success callback |
| params.error | Function | The error callback |

###### Return
_None_

## sendMessage

Sends a message to the Pebble.

###### Parameters
| Name | Type | Value |
|------|------|-------|
| params | Object |  |
| params.message | Object | The message object (see README for formatting example) |
| params.success | Function | The success callback |
| params.error | Function | The error callback |

###### Return
_None_

## sendImage

Sends an image to the Pebble.

###### Parameters
| Name | Type | Value |
|------|------|-------|
| params | Object |  |
| params.image | String | The image data (see README for example) |
| params.key | Number | The enum key used by the Pebble (see README for example) |
| params.success | Function | The success callback |
| params.error | Function | The error callback |

###### Return
_None_