# Synaera-server
REST API server implementation using Flask for hosting machine learning models (backend of Synaera project)

# To send a HTTP POST request with array as data

Powershell:
`Invoke-WebRequest -Method POST -Uri "http://localhost:80/predict" -ContentType "application/json" -Body "[5.9,3.0,5.1,1.8]"`

Unix:
`curl -X POST 0.0.0.0:80/predict -H 'Content-Type: application/json' -d '[5.9,3.0,5.1,1.8]'`

Android:
Use okhttp framework