Codename One Offline Build Tools
Author: Steve Hannah <steve@weblite.ca>

Synopsis
========

A set of tools for building CodenameOne applications offline (i.e. without using the build server).

Currently there is support only for iOS builds, but I plan to work on the rest as required.

Requirements
============

1. Running Mac OS X 10.6 or higher
2. JDK 1.7 or higher installed
3. Xcode 4 or higher installed

Installation:
=============

1. Check out the build tools from SVN:

$ svn checkout https://codenameone-incubator.googlecode.com/svn/trunk/shannah/offline-build-tools codenameone-build-tools

2. Install the build tools using the ant install script:

$ cd codenameone-build-tools
$ ant install

3. Assuming the installation went properly, you should see a file named build-ios.xml inside the dist directory.  You just
need to include this file inside the build.xml file of the codenameone applicaton project that you are developing.

e.g.

<import file="/path/to/codenameone-build-tools/dist/build-ios.xml"/>

This will add an ANT target called "build-for-ios-device-locally".  You can either build this target from the command line:

$ cd /path/to/my-project
$ ant build-for-ios-device-locally

Or you can do it from inside Netbeans's File tab, by right-clicking "build.xml", then select "Run Target" > "Other" > "build-for-ios-device-locally".
