import sys, getopt
import re
import eventlet
import socketio
import cv2
import numpy as np
import threading
from engineio.payload import Payload
import cv_model
import nlp_model

# Default is 16 which can create a bottleneck for video streaming
Payload.max_decode_packets = 256

sio = socketio.Server()
app = socketio.WSGIApp(sio)

# Default server IP and server Port
ip = "0.0.0.0" 
port = 5000

# Display the image on a OpenCV window
isDisplay = False

# Use authentication to validate users
isAuth = False

# Dummy in-memory key-value pairs user database for dummy authentication using 
dummyUserDB = { 
	# Add more users as needed
	"user1": "pass1"
	"Alice": "123",
	"Bob": "456"
}

# Map of authenticated session id's and respective usernames
activeSessions = {}
# Map of authenticated usernames and respective session id's
activeUsers = {}

transcript = []

def gloss_to_english(recordingStopped):
	glossInput = ""
	decoded_sentence = ""
	if len(cv_model.predictions) > 1:
		# last sign was nosign
		if cv_model.predictions[-1] == 0 or recordingStopped:
			if recordingStopped and cv_model.predictions[-1] != 0:
				for res in cv_model.predictions:
					glossInput += cv_model.actions[res] + " "
			else:
				for res in cv_model.predictions[:-1]:
					glossInput += cv_model.actions[res] + " "
			glossInput = glossInput[:-1]
			print(glossInput)
			decoded_sentence = nlp_model.get_nlp_prediction(glossInput)
			cv_model.predictions.clear()
	return (decoded_sentence)

def gloss_to_english2(recordingStopped):
	glossInput = ""
	decoded_sentence = ""
	if len(cv_model.sentence) > 1:
		# last sign was nosign
		if cv_model.sentence[-1] == "NoSign" or recordingStopped:
			if recordingStopped and cv_model.sentence[-1] != "NoSign":
				for word in cv_model.sentence:
					glossInput += word + " "
			else:
				for word in cv_model.sentence[:-1]:
					glossInput += word + " "
			glossInput = glossInput[:-1]
			print(glossInput)
			decoded_sentence = nlp_model.get_nlp_prediction(glossInput)
			cv_model.sentence.clear()
	return (decoded_sentence)

def video_gloss_to_english(videoSentence):
	glossInput = ""
	final_sentence = ""
	if len(videoSentence) == 0:
		print("no video predictions")
		return
	print("START NLP EXEC ON VIDEO PREDICTIONS")
	for prediction in videoSentence:
		if prediction != "NoSign":
			# glossInput += cv_model.actions[prediction] + " "
			glossInput += prediction + " "
		# if nosign and gloss input is not empty
		elif len(glossInput) > 0:
			print("gloss:", glossInput)
			final_sentence += nlp_model.get_nlp_prediction(glossInput) + ". "
			glossInput = ""
	if len(glossInput) > 0:
		print("gloss:", glossInput)
		final_sentence += nlp_model.get_nlp_prediction(glossInput) + ". "
		glossInput = ""
	final_sentence = '. '.join(i.capitalize() for i in final_sentence.split('. '))
	return (final_sentence)

@sio.event
def connect(sid, environ):
	print('connect', sid)
	print(activeUsers)

# Method used for user "dummy" authentication using an in-memory dummy database. 
# This can be used to authenticate the user with other server/service.
@sio.event
def authenticate(sid, username, password, clientCallbackEvent):
	user = dummyUserDB.get(username)
	if isAuth == False or (user is not None and user == password):
		# add username to the session
		addUserSession(sid, username)
		sio.emit(clientCallbackEvent, True)
		print("User [" + username +"] authenticated.")
	else:
		sio.emit(clientCallbackEvent, False)
		sio.sleep(2) # give time to the socket emit the callback to the user
		sio.disconnect(sid)
		print("User [" + username +"] authentication failed.")

# This is the main method that the client calls when streaming the pictures to 
# the server. Each receiveImage event is already processed in a new thread.
# The image format is JPEG and is sent by the client in as binary data of byte[] 
# received in python as Bytes.
@sio.event
def receiveImage(sid, imageBytes, clientCallBackEvent):
	# gloss = cv_model.run_model_frame_batches_filter(imageBytes)
	# real_text = gloss_to_english2(False)
	
	# gloss = cv_model.run_model_frame_batches(imageBytes)
	# real_text = gloss_to_english(False)
	
	gloss = cv_model.run_model_dup_check(imageBytes)
	real_text = gloss_to_english2(False)
	
	if gloss != "nothing":
		if (len(real_text) == 0):
			data = {'result': gloss, 'isGloss': True}
			sio.emit(clientCallBackEvent, data)
			print("gloss result:", gloss)
		else:
			data = {'result': real_text, 'isGloss': False}
			sio.emit(clientCallBackEvent, data)
			print("real result:", real_text)
	if(isDisplay):
		displayImage(activeSessions[sid], bytes(imageBytes))

@sio.event
def receiveVideoStream(sid, imageBytes, totalFrames):
	cv_model.store_frames(imageBytes, totalFrames)
	if len(cv_model.videoFrames) == totalFrames:
		print("Video upload complete. Starting processing")
		t = threading.Thread(target=processVideo)
		t.start()
		print("Thread started..")

# Post processing NLP model output, correcting sentence punctuation, spaces and case
def format_string(sentence):
	sentence = sentence.strip()
	sentence = re.sub(r"\s+", " ", sentence)
	sentence = re.sub(r"(\. +)", ". ", sentence)
	sentence = re.sub(r"( +\.)", ".", sentence)
	sentence = '. '.join(i.capitalize() for i in sentence.split('. '))
	sentence = re.sub(r"(\?\.?)", "?", sentence)
	return sentence

# This is invoked by the client when a video is uploaded from their gallery 
def processVideo():
	# sio.emit(clientCallBackEvent, video_gloss_to_english(cv_model.run_model_on_video()))
	transcript.append(video_gloss_to_english(cv_model.run_model_on_video()))
	transcript[0] = format_string(transcript[0])
	print("Transcript generated!")
	print(format_string(transcript[0]))
	if len(transcript) > 0:
		if len(transcript[0]) == 0:
			transcript.clear()
			transcript.append("No ASL detected in video")

@sio.event
def checkTranscript(sid, clientCallBackEvent):
	print("Client requested for transcript")
	if len(transcript) > 0:
		if len(transcript[0]) > 0:
			print("Transcript:", transcript[0])
			sio.emit(clientCallBackEvent, transcript[0])
			transcript.clear()
	else:
		sio.emit(clientCallBackEvent, "")

@sio.event
def stopRecord(sid, clientCallBackEvent):
	real_text = gloss_to_english2(True)
	if len(real_text) > 0:
		data = {'result': real_text, 'isGloss': False}
		sio.emit(clientCallBackEvent, data)
		print("real result:", real_text)
	cv_model.sequence.clear()
	cv_model.frameCount.clear()
	cv_model.frames.clear()

@sio.event
def disconnect(sid):
	print('disconnect', sid)
	cv_model.sequence.clear()
	cv_model.predictions.clear()
	cv_model.frameCount.clear()
	cv_model.frames.clear()
	print("cleared prediction data")
	deleteUserSession(sid)
	print(activeUsers)
	if isDisplay:
		cv2.destroyAllWindows() # Doesn't work well in Unix environments = Zombie window
		cv2.waitKey(1)

def addUserSession(sid, username):
	activeSessions[sid] = username
	activeUsers[username] = sid

def deleteUserSession(sid):
	username = activeSessions[sid]
	activeSessions.pop(sid, None)
	activeUsers.pop(username, None)

def displayImage(username, imageBytes):
	# Decode image from bytes
	nparr = np.frombuffer(imageBytes, np.uint8)
	img = cv2.imdecode(nparr, cv2.IMREAD_COLOR)
	imgNo = str(len(cv_model.frameCount))
	# cv2.imwrite('frames/img'+imgNo+'.jpg', img)
	cv_model.frameCount.append(1)
	print("received ", imgNo)
	# Show image after decoded
	cv2.namedWindow(username, cv2.WINDOW_AUTOSIZE)
	cv2.imshow(username, img)
	cv2.waitKey(1)

def executeCommandArgs(argv):
	global ip, port, isDisplay, isAuth
	scriptName = argv[0]
	try:
		opts, args = getopt.getopt(argv[1:], "adhi:p:", ["ip=", "port=", "display", "auth"])
	except getopt.GetoptError: # wrong commands
		print(scriptName + " -i <server_ip> -p <server_port>")
		sys.exit(2)

	for opt, arg in opts:
		if opt == "-h": # help command
			print(scriptName + " -i <server_ip> -p <server_port")
			sys.exit()
		elif opt in ("-i", "--ip"):
			ip = arg
		elif opt in ("-p", "--port"):
			port = int(arg)
		elif opt in ("-d", "--display"):
			isDisplay = True
		elif opt in ("-a", "--auth"):
			isAuth = True

if __name__ == '__main__':
	executeCommandArgs(sys.argv)
	eventlet.wsgi.server(eventlet.listen((ip, port)), app)
