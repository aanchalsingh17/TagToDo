# TAG TO DO

An advance note taking and reminder application for Android.  


## Table of Contents

- [Features](#features)
- [App Permissions](#apppermissions)
- [Note for contributors](#noteforcontributors)
- [Feedback](#feedback)
- [Contributors](#contributors)


## Features

Tag To Do holds following features -

* Take user notes with title and content
* User account
  - Email based signup
  - Anonymous (guest) login
* Cloud storage of user data for easy restore when logged in again
* Location
  - Search for location using marker
  - Search for location using text field
  - View your saved location on a map
* Reminder
  - Set note-specific custom date and time based alarms
  - Automatically get notified when user is near 100 m of any of his note based location
  - This location service can be enabled or disabled manually
* Images
  - User can also add one image per note
  - Image can be retrieved from gallery or taken instantly from camera


## App permissions

* Internet access
* Foreground location access
* Background location access (to run location service)
* Storage access(Read and write)
* Camera permissions


## Note for contributors

* All Suggestions are welcome.
* Fork repository and Contribute.
* Download the source code from https://github.com/aditya2548/TagToDo
* Extract the content of zip file.
- (If you are comfortable using git then you can also clone the repo and skip above two steps)

* Open the project in Android Studio
* Create a mapbox account and generate your access token.
* Make a keys.xml file in /app/src/main/res/values/ to store your Mapbox key as:
    <string name="access_token">Replace this with your key</string>


## Feedback

Feel free to report issues and bugs.It will be helpful for future launches of application.
Send us feedback on [Email](mailto:aditya25dec2000@gmail.com)


## Contributors

<ul>
  <li> <a href="https://github.com/aditya2548">Aditya Sharma</a></li>
  <li> <a href="https://github.com/aanchalsingh17">Aanchal Singh</a></li>
</ul>
