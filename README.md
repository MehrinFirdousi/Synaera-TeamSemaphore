# Synaera-TeamSemaphore

Android application that performs sign language to text translation in real time

## Table of Contents <!-- omit in toc -->
- [Our Team](#our-team)
- [Project repository structure](#project-repository-structure)
- [Features](#features)
- [User guide](#user-guide)
- [How to use](#how-to-use)
- [Learn more](#learn-more)

## Our Team
<table>
  <tbody>
    <tr>
      <td align="center"><a href="https://github.com/MehrinFirdousi"><img src="https://avatars.githubusercontent.com/u/88845742?v=4" width="100px;"/><br/><b>Mehrin Firdousi</b></a><br/>Team Lead. App and Server development</td>
      <td align="center"><a href="https://github.com/AlbusTheDev"><img src="https://avatars.githubusercontent.com/u/39058660?v=4" width="100px;"/><br/><b>Qusai Wael</b></a><br/>Application development</td>
      <td align="center"><a href="https://github.com/aa055"><img src="https://avatars.githubusercontent.com/u/64472870?v=4" width="100px;"/><br/><b>Aamir Ali</b></a><br/>Computer Vision model development</td>
      <td align="center"><a href="https://github.com/AdamJeddy"><img src="https://avatars.githubusercontent.com/u/58102753?v=4" width="100px;"/><br/><b>Adam Ahsan</b></a><br/>NLP model development</td>
      <td align="center"><a href="https://github.com/ayeshajr"><img src="https://avatars.githubusercontent.com/u/122777235?v=4" width="100px;"/><br/><b>Ayesha Hassan</b></a><br/>NLP model development & dataset preparation</td>
    </tr>
  </tbody>
</table>

## Project repository structure 

Synaera's development process involved 4 main components; Android app running the sign language translation service among other features, Computer Vision model for human gesture/action recognition, Natural Language Processing model for sentence formation from predicted signs and finally the streaming server that connects all the above components and allows them to communicate over the network. 

### [Android App](https://github.com/MehrinFirdousi/Synaera-TeamSemaphore/tree/main/Android%20App)
Contains Synaera app's code (Kotlin and Java), ready to be tested with the streaming server. The app requires internet connection and on launch, a session is established by the app client to our [cloud-based server](#server), which is then used to run the live translation service.

### [Computer Vision Model](https://github.com/MehrinFirdousi/Synaera-TeamSemaphore/tree/main/Computer%20Vision%20Model)
Contains all code and components related to training and testing of the CV model used by the server to recognize human gestures/actions as ASL signs. The model is hosted by the [streaming server](#server). The signs detected by the CV model is represented in gloss notation, which will be processed further by the [NLP model](#natural-language-processing-model) to generate an English sentence.

### [Natural Language Processing Model](https://github.com/MehrinFirdousi/Synaera-TeamSemaphore/tree/main/NLP%20Model)
Contains all code and components related to training and testing of the NLP model used to translate ASL gloss to English. This model is hosted by the streaming server to perform live translation of ASL using the [CV model](#computer-vision-model)'s output.

### [Server](https://github.com/MehrinFirdousi/Synaera-TeamSemaphore/blob/main/Server)
Contains all the code for the video streaming server, including setting up of socket connections to communicate with the Android app client, hosting the two deep learning models and performing pre/post processing of model input/output. This server is currently hosted on an Azure Cloud VM, but can also be run locally. For more info on how to test locally, see - 


## Features

* Real-time translation of sign language to text from phone camera
* Chat with sign language user 
* Upload video containing ASL signing and generate transcript

  <img src="https://user-images.githubusercontent.com/88845742/229358780-b0ea867d-41a8-4d98-a03b-086c4d21f94f.jpg" width="334" height="744" />

## User guide 
### On-boarding Screens and Login/Registration steps


https://user-images.githubusercontent.com/88845742/229374064-92112d25-e4f7-4133-821f-aacfbd8b0d7d.mp4



### Real-time translation and Chat demo


https://user-images.githubusercontent.com/88845742/229364607-ef0f263a-adcf-46dd-a5ea-c841af0e4427.mp4

### Transcript generation for Video Upload demo


https://user-images.githubusercontent.com/88845742/229370785-60b00b32-db75-4e14-bb19-2f7725de8033.mp4


## How to use 

### Pre-requisites
* Install Android Studio latest version 
* Android mobile device with minimum Android version 5.0

### Option 1
Clone this repository on your machine, navigate to 'Android App' directory and open the project in Android Studio

```bash
git clone https://github.com/MehrinFirdousi/Synaera-TeamSemaphore.git
cd 'Android App'
```

### Option 2
Directly install `synaera-app.apk` file on your Android device (located at the root of this repository)

## Learn more

### About us
https://synaera.wixsite.com/home

### More about our deep learning models development process and data collection
https://github.com/AdamJeddy/Grad-Project-Code

### More about the server development process 
https://github.com/MehrinFirdousi/Synaera-server
