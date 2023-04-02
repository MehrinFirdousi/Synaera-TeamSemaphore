# Synaera Streaming Server
Streaming server that receives live video frames from an Android client implemented using SocketIO. The server hosts two Deep learning models (Computer Vision Model and Natural Language Processing model) and connects the two of them to perform ASL to English translation. Synaera Android client can make requests to this server and receive translation results using Android's SocketIO API. 

# To send a HTTP POST request with array as data

Powershell:
`Invoke-WebRequest -Method POST -Uri "http://localhost:80/predict" -ContentType "application/json" -Body "[5.9,3.0,5.1,1.8]"`

Unix:
`curl -X POST 0.0.0.0:80/predict -H 'Content-Type: application/json' -d '[5.9,3.0,5.1,1.8]'`

Android:
Use okhttp framework
