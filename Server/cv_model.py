import keras.models
import os
import mediapipe as mp
import numpy as np
import cv2
from os.path import join

model_weights='Model_13ws_4p_5fps_new.h5'
frame_rate = 5
bufferLen=3
predictionThreshold=2
videoBufferLen=20
videoPredictionThreshold=15

frames = []
videoFrames = []
sequence = []
sentence = []
predictions = []
mp_holistic = mp.solutions.holistic # Holistic model - make our detection
mp_drawing = mp.solutions.drawing_utils # Drawing utilities - make our drawings
holistic = mp_holistic.Holistic(min_detection_confidence=0.5, min_tracking_confidence=0.5)
holistic.__enter__()
# actions = np.array(['NoSign','hello', 'thanks', 'please', 'sorry', 'you', 'work', 'where'])
# actions = np.array(['NoSign','hello', 'thanks', 'iloveyou'])
# actions = np.array(['NoSign', 'hello', 'you', 'work', 'where', 'how', 'your', 'day', 'b', 'o'])
actions = np.array(['NoSign','hello','you','work','where','how','your','day','b','o','me','live','university'])

cv_wts = keras.models.load_model(os.path.join('models', model_weights))
frameCount = []

# To extract keypoint values from frame using mediapipe
def mediapipe_detection(image, cv_wts):
	image = cv2.cvtColor(image, cv2.COLOR_BGR2RGB) # COLOR CONVERSION BGR 2 RGB
	image.flags.writeable = False                  # Image is no longer writeable
	results = cv_wts.process(image)                 # Make prediction
	image.flags.writeable = True                   # Image is now writeable
	image = cv2.cvtColor(image, cv2.COLOR_RGB2BGR) # COLOR COVERSION RGB 2 BGR
	return image, results

# To draw landmarks and pose connections on the frame using the results extracted
def draw_landmarks(image, results):
	mp_drawing.draw_landmarks(image, results.face_landmarks, mp_holistic.FACEMESH_TESSELATION) # Draw face connections
	mp_drawing.draw_landmarks(image, results.pose_landmarks, mp_holistic.POSE_CONNECTIONS) # Draw pose connections
	mp_drawing.draw_landmarks(image, results.left_hand_landmarks, mp_holistic.HAND_CONNECTIONS) # Draw left hand connections
	mp_drawing.draw_landmarks(image, results.right_hand_landmarks, mp_holistic.HAND_CONNECTIONS) # Draw right hand connections

def extract_keypoints(results):
	pose = np.array([[res.x, res.y, res.z, res.visibility] for res in results.pose_landmarks.landmark]).flatten() if results.pose_landmarks else np.zeros(33*4)
	face = np.array([[res.x, res.y, res.z] for res in results.face_landmarks.landmark]).flatten() if results.face_landmarks else np.zeros(468*3)
	lh = np.array([[res.x, res.y, res.z] for res in results.left_hand_landmarks.landmark]).flatten() if results.left_hand_landmarks else np.zeros(21*3)
	rh = np.array([[res.x, res.y, res.z] for res in results.right_hand_landmarks.landmark]).flatten() if results.right_hand_landmarks else np.zeros(21*3)
	return np.concatenate([pose, face, lh, rh])

# returns "nothing" when	the prediction is nosign and theres no predictions other till now, 
# 							or when the new prediction is the same as the last prediction
def run_model_frame_batches(imageBytes):
	sequence = []
	result_p = "nothing"
	nparr = np.frombuffer(imageBytes, np.uint8)
	frame = cv2.imdecode(nparr, cv2.IMREAD_COLOR)
	# frame = cv2.flip(frame, 1)
	frames.append(frame)
	threshold = 0.95

	# cv2.imwrite('frames/img'+str(len(frames))+'.jpg', frame)
	print("received", len(frames))
	
	if (len(frames) == frame_rate):
		# with mp_holistic.Holistic(min_detection_confidence=0.5, min_tracking_confidence=0.5) as holistic:
		for frame in frames:
			# Make detections
			image, results = mediapipe_detection(frame, holistic)
			
			# Draw landmarks
			draw_landmarks(image, results)

			# Prediction logic
			keypoints = extract_keypoints(results)
			sequence.append(keypoints)
		frames.clear()
		res = cv_wts.predict(np.expand_dims(sequence, axis=0))[0]
		print("actual:", actions[np.argmax(res)])
		
		if res[np.argmax(res)] > threshold:
			# check if prediction is nosign and predictions array is empty
			if len(predictions) == 0:
				if np.argmax(res) == 0:
					return "nothing"
			# check duplicate prediction
			if len(predictions) > 0:
				if predictions[-1]==np.argmax(res):
					return "nothing"
			predictions.append(np.argmax(res))
			result_p = actions[np.argmax(res)]
			print("filtered:", result_p)
	return (result_p)

def run_model_frame_batches_filter(imageBytes):
	sequence = []
	result_p = "nothing"
	nparr = np.frombuffer(imageBytes, np.uint8)
	frame = cv2.imdecode(nparr, cv2.IMREAD_COLOR)
	# frame = cv2.flip(frame, 1)
	frames.append(frame)
	threshold = 0.95

	# cv2.imwrite('frames/img'+str(len(frames))+'.jpg', frame)
	print("received", len(frames))
	
	if (len(frames) == frame_rate):
		# with mp_holistic.Holistic(min_detection_confidence=0.5, min_tracking_confidence=0.5) as holistic:
		for frame in frames:
			image, results = mediapipe_detection(frame, holistic)
			draw_landmarks(image, results)
			keypoints = extract_keypoints(results)
			sequence.append(keypoints)
		frames.clear()
		res = cv_wts.predict(np.expand_dims(sequence, axis=0))[0]
		print(len(frames), "actual:", actions[np.argmax(res)])
		
		predictions.append(np.argmax(res))
		# new prediction is nosign 
		if predictions[-1] == 0:
			if len(sentence) > 0 and sentence[-1] != "NoSign":
				# vals, counts = np.unique(predictions[-2:], return_counts=True)
				# if vals[0]==predictions[-1] and counts[0]>1:
				result_p = actions[predictions[-1]]
				sentence.append(result_p)
				print("filtered1:", result_p)
		elif len(sentence) > 0:
			if actions[predictions[-1]] != sentence[-1]:
				vals, counts = np.unique(predictions[-2:], return_counts=True)
				if vals[0]==predictions[-1] and counts[0]>1:
					result_p = actions[predictions[-1]]
					sentence.append(result_p)
					print("filtered2:", result_p)
		else:
			vals, counts = np.unique(predictions[-2:], return_counts=True)
			if vals[0]==predictions[-1] and counts[0]>1:
				result_p = actions[predictions[-1]]
				sentence.append(result_p)
				print("filtered3:", result_p)
		# print("actual:", actions[np.argmax(res)])
		
		# if res[np.argmax(res)] > threshold:
		# 	# check if prediction is nosign and predictions array is empty
		# 	if len(predictions) == 0:
		# 		if np.argmax(res) == 0:
		# 			return "nothing"
		# 	# check duplicate prediction
		# 	if len(predictions) > 0:
		# 		if predictions[-1]==np.argmax(res):
		# 			return "nothing"
		# 	predictions.append(np.argmax(res))
		# 	result_p = actions[np.argmax(res)]
		# 	print("filtered:", result_p)
	return (result_p)

def run_model(imageBytes):
	result_p = "nothing"
	# with mp_holistic.Holistic(min_detection_confidence=0.5, min_tracking_confidence=0.5) as holistic:
	nparr = np.frombuffer(imageBytes, np.uint8)
	frame = cv2.imdecode(nparr, cv2.IMREAD_COLOR)
	# frame = cv2.rotate(frame, cv2.ROTATE_90_COUNTERCLOCKWISE)
	image, results = mediapipe_detection(frame, holistic)
	draw_landmarks(image, results)
	keypoints = extract_keypoints(results)
	sequence.append(keypoints)
	# cv2.imwrite('frames/img'+imgNo+'.jpg', frame)
	frameCount.append(1)
	imgNo = str(len(frameCount))
	last_frames = sequence[-frame_rate:]
	# if len(last_frames) == frame_rate:
	if len(frameCount) % frame_rate == 0:
		res = cv_wts.predict(np.expand_dims(last_frames, axis=0))[0]
		print(imgNo, actions[np.argmax(res)])
		# check if predictions array is empty and new sign is nosign
		if len(predictions) == 0:
			if np.argmax(res) == 0:
				return "nothing"
		# check duplicate prediction
		if len(predictions) > 0:
			if predictions[-1]==np.argmax(res):
				return "nothing"
		predictions.append(np.argmax(res))
		result_p = actions[np.argmax(res)]
	
	return (result_p)

def run_model_dup_check(imageBytes):
	result_p = "nothing"
	nparr = np.frombuffer(imageBytes, np.uint8)
	frame = cv2.imdecode(nparr, cv2.IMREAD_COLOR)
	# frame = cv2.flip(frame, 1)
	
	image, results = mediapipe_detection(frame, holistic)
	draw_landmarks(image, results)
	keypoints = extract_keypoints(results)
	sequence.append(keypoints)
	frameCount.append(1)
	imgNo = str(len(frameCount))
	print("received", imgNo)
# 	cv2.imwrite('frames/img'+imgNo+'.jpg', frame)
	
	last_frames = sequence[-frame_rate:]
	# if len(last_frames) == frame_rate:
	if len(frameCount) >= frame_rate and len(frameCount) % 2 == 1:
		res = cv_wts.predict(np.expand_dims(last_frames, axis=0))[0]
		print(imgNo, "actual:", actions[np.argmax(res)])
		predictions.append(np.argmax(res))

		# new prediction is nosign 
		if predictions[-1] == 0:
			if len(sentence) > 0 and sentence[-1] != "NoSign":
				vals, counts = np.unique(predictions[-2:], return_counts=True)
				if vals[0]==predictions[-1] and counts[0]>1:
					result_p = actions[predictions[-1]]
					sentence.append(result_p)
					print("filtered1:", result_p)
		elif len(sentence) > 0:
			if actions[predictions[-1]] != sentence[-1]:
				vals, counts = np.unique(predictions[-bufferLen:], return_counts=True)
				if vals[0]==predictions[-1] and counts[0]>predictionThreshold:
					result_p = actions[predictions[-1]]
					sentence.append(result_p)
					print("filtered2:", result_p)
		else:
			vals, counts = np.unique(predictions[-bufferLen:], return_counts=True)
			if vals[0]==predictions[-1] and counts[0]>predictionThreshold:
				result_p = actions[predictions[-1]]
				sentence.append(result_p)
				print("filtered3:", result_p)
			# result_p = actions[predictions[-1]]
			# sentence.append(result_p)

	return (result_p)

def store_frames(imageBytes, totalFrames):
	nparr = np.frombuffer(imageBytes, np.uint8)
	frame = cv2.imdecode(nparr, cv2.IMREAD_COLOR)
	videoFrames.append(frame)
	print("received", len(videoFrames))

def run_model_on_video():
	videoSequence = []
	videoPredictions = []
	videoSentences = []
	print("START MODEL EXEC ON VIDEO")
	with mp_holistic.Holistic(min_detection_confidence=0.5, min_tracking_confidence=0.5) as holistic:
		for frame in videoFrames:
			print("processing frame:", len(videoSequence))
			image, results = mediapipe_detection(frame, holistic)
			draw_landmarks(image, results)
			keypoints = extract_keypoints(results)
			videoSequence.append(keypoints)
			last_frames = videoSequence[-frame_rate:]
			if len(last_frames) == frame_rate:
				res = cv_wts.predict(np.expand_dims(last_frames, axis=0))[0]
				print(len(videoSequence), "actual: ", actions[np.argmax(res)])
				
				videoPredictions.append(np.argmax(res))
				if len(videoSentences) > 0:
					if actions[np.argmax(res)] != videoSentences[-1]:
						vals, counts = np.unique(videoPredictions[-videoBufferLen:], return_counts=True)
						if vals[0]==videoPredictions[-1] and counts[0]>videoPredictionThreshold:
							print(len(videoSequence), "filtered: ", actions[np.argmax(res)])
							videoSentences.append(actions[np.argmax(res)])
				else:
					print(len(videoSequence), "filtered: ", actions[np.argmax(res)])
					videoSentences.append(actions[np.argmax(res)])
				
			'''
			if len(videoPredictions) == 0:
				if np.argmax(res) == 0:
					continue
			# check duplicate prediction
			if len(videoPredictions) > 0:
				if videoPredictions[-1]==np.argmax(res):
					continue
			videoPredictions.append(np.argmax(res))
			'''

	videoFrames.clear()
	# return videoPredictions
	return videoSentences
