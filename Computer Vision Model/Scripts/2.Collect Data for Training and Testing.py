# imports
import pandas as pd
import numpy as np
import cv2
import os
import mediapipe as mp

mp_holistic = mp.solutions.holistic # Holistic model - make our detection
mp_drawing = mp.solutions.drawing_utils # Drawing utilities - make our drawings

# FUNCTIONS:

# To extract keypoint values from frame using mediapipe
def mediapipe_detection(image, model):
    image = cv2.cvtColor(image, cv2.COLOR_BGR2RGB) # COLOR CONVERSION BGR 2 RGB
    image.flags.writeable = False                  # Image is no longer writeable
    results = model.process(image)                 # Make prediction
    image.flags.writeable = True                   # Image is now writeable
    image = cv2.cvtColor(image, cv2.COLOR_RGB2BGR) # COLOR COVERSION RGB 2 BGR
    return image, results

# To draw landmarks and pose connections on the frame using the results extracted
def draw_landmarks(image, results):
    mp_drawing.draw_landmarks(image, results.face_landmarks, mp_holistic.FACEMESH_TESSELATION) # Draw face connections
    mp_drawing.draw_landmarks(image, results.pose_landmarks, mp_holistic.POSE_CONNECTIONS) # Draw pose connections
    mp_drawing.draw_landmarks(image, results.left_hand_landmarks, mp_holistic.HAND_CONNECTIONS) # Draw left hand connections
    mp_drawing.draw_landmarks(image, results.right_hand_landmarks, mp_holistic.HAND_CONNECTIONS) # Draw right hand connections

# To extract keypoints from frame
def extract_keypoints(results):
    pose = np.array([[res.x, res.y, res.z, res.visibility] for res in results.pose_landmarks.landmark]).flatten() if results.pose_landmarks else np.zeros(33*4)
    face = np.array([[res.x, res.y, res.z] for res in results.face_landmarks.landmark]).flatten() if results.face_landmarks else np.zeros(468*3)
    lh = np.array([[res.x, res.y, res.z] for res in results.left_hand_landmarks.landmark]).flatten() if results.left_hand_landmarks else np.zeros(21*3)
    rh = np.array([[res.x, res.y, res.z] for res in results.right_hand_landmarks.landmark]).flatten() if results.right_hand_landmarks else np.zeros(21*3)
    return np.concatenate([pose, face, lh, rh])
    
# ------------- MAIN - Start Collection Loop -----------

def Data_Collection(DATA_PATH, actions, no_sequences, sequence_length):

    # establish video capture to webcam
    cap = cv2.VideoCapture(0)
    # Set mediapipe model
    with mp_holistic.Holistic(min_detection_confidence=0.5, min_tracking_confidence=0.5) as holistic:
        # Wait 2 secs before starting collection
        cv2.waitKey(2000)
        
        # Loop through actions
        for action in actions:
            
            # Display a prompt for next action to perform
            blank_screen = np.zeros((480,640,3), dtype="uint8")
            cv2.imshow('OpenCV Feed', blank_screen)
            cv2.putText(blank_screen, 'Action: {}'.format(action), (170,210), cv2.FONT_HERSHEY_SIMPLEX, 1.5, (0,255,0), 2, cv2.LINE_AA)
            cv2.imshow('OpenCV Feed', blank_screen)
            cv2.waitKey(2000)
            
            # Loop through sequences aka videos
            for sequence in range(no_sequences):
                # Loop through video length aka sequence length
                for frame_num in range(sequence_length):

                    # Read feed
                    ret, frame = cap.read()

                    # Make detections
                    image, results = mediapipe_detection(frame, holistic)
                    # print(results)

                    # Draw landmarks
                    draw_landmarks(image, results)

                    # NEW Apply wait logic
                    if frame_num == 0:
                        cv2.putText(image, 'STARTING COLLECTION', (120,200),
                                   cv2.FONT_HERSHEY_SIMPLEX, 1, (0,255, 0), 4, cv2.LINE_AA)
                        cv2.putText(image, 'Collecting frames for {} Video Number {}'.format(action, sequence), (15,12),
                                   cv2.FONT_HERSHEY_SIMPLEX, 0.5, (0, 0, 255), 1, cv2.LINE_AA)
                        # Show to screen
                        cv2.imshow('OpenCV Feed', image)
                        cv2.waitKey(2000)
                    else:
                        cv2.putText(image, 'Collecting frames for {} Video Number {}'.format(action, sequence), (15,12),
                                   cv2.FONT_HERSHEY_SIMPLEX, 0.5, (0, 0, 255), 1, cv2.LINE_AA)
                        # Show to screen
                        cv2.imshow('OpenCV Feed', image)

                    # NEW Export keypoints
                    keypoints = extract_keypoints(results)
                    npy_path = os.path.join(DATA_PATH, action, str(sequence), str(frame_num))
                    np.save(npy_path, keypoints)

                    # Break gracefully
                    if cv2.waitKey(1) & 0xFF == ord('q'):
                        break

        cap.release()
        cv2.destroyAllWindows()
        print('Data Collection Complete!!!')


# --------------- Function Call - Start Collection ------------ #

# Path for exported data, numpy arrays
DATA_PATH = os.path.join('Data Collection')
# Actions that we try to detect
actions = np.array(['NoSign'])
# 30 videos worth of data
no_sequences = 40
# Videos are going to be 25 frames in length
sequence_length = 25

Data_Collection(DATA_PATH, actions, no_sequences, sequence_length)


cap.release()
cv2.destroyAllWindows()
