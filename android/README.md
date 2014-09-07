# TiPebble Android Build Notes

Because the Pebble SDK does not include a JAR file, we must create one for use in the Titanium Android module for TiPebble.

 * Place the `{MODULE}/utils/build.xml` in the root of the PebbleKit-Android directory
 * Copy the `{MODULE}/build.properties.example` file to the root of the PebbleKit-Android directory
 	* Rename to `{PEBBLEKIT}/build.properties`
 * Copy the `{MODULE}/build.properties.example` file to the root of the Android module
 	* Rename to `{MODULE}/build.properties`
 * Open terminal and navigate to the root of the PebbleKit-Android directory
 	* Run `$ ant`
 		* This will generate a file named `{PEBBLEKIT}/lib/pebble-sdk.jar`
 * Copy `{PEBBLEKIT}/lib/pebble-sdk.jar` (and any other JAR files from the `libs` folder) into `{MODULE}/lib`
 * Build the Android module by running `$ ant`
 	* The module will be built to `{MODULE}/dist`