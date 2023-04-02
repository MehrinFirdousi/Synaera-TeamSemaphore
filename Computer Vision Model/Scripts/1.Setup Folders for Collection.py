# imports
import numpy as np
import os

# Path for exported data BY Default the folder is present in the directory
if not os.path.exists(os.path.join('Data Collection')):
    os.makedirs(os.path.join('Data Collection'))

DATA_PATH = os.path.join('Data Collection')

# Actions that we try to detect
# *** CHANGE THE ACTIONS HERE ***
actions = np.array(['NoSign'])

# 30 videos worth of data
no_sequences = 40
# Videos are going to be 30 frames in length
# sequence_length = 25

# ---------- FOLDER CREATION --------------#
def Folder_Setup(DATA_PATH, actions, no_sequences):
    for action in actions:
        for sequence in range(no_sequences):
            try:
                os.makedirs(os.path.join(DATA_PATH, action, str(sequence)))
            except:
                pass
    print('Folders Created!!!!')

# ---------- Function call ------------#
Folder_Setup(DATA_PATH, actions, no_sequences)



# -------------------- FOLDER SETUP EXAMPLE:-------------------
# The training set will be stored in a directory of folders:
#
# -> MAIN Folder
#     -> Sign/Label 1
#         -> Video 1
#             -> Frame 1.npy  (x,y,z coordinates for each landmark for each frame)
#             -> Frame 2
#             -> Frame 3
#             -> Frame 4
#             -> Frame 5
#             …...
#
#         -> Video 2
#         -> Video 3
#         -> Video 4
#         -> Video 5
#             …..
#
#     -> Sign/Label 2
#     -> Sign/Label 3
#     -> Sign/Label 4
#     -> Sign/Label 5
#        ....
