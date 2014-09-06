README
=======

Put the build.xml from the `Utils` folder in the root of the Android Pebble Kit

Copy the build.properties.example file in the module and name it build.properties
also, copy that file next to build.xml in the root of the Android Pebble Kit.
build.properties needs to be in the root of the android module and in APK.

From the terminal go to the root of Android Pebble Kit and run ant.
'$ ant'
This will generate a file named `pebble-sdk.jar`.

Copy the `pebble-sdk.jar` and any jars from the libs folder of the APK project into the lib folder of the android module

Build the anroid module
`$ ant`
The built module will be in `dist`.