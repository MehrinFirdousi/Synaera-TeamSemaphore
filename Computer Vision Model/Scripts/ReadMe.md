# ASL Gloss to English Translator <!-- omit in toc -->

These are the scripts for 
- Testing Keypoints Extraction 
- Creating Data Collection Folders 
- Start Data Collection

## Table of Contents <!-- omit in toc -->

- [Prerequisites](#prerequisites)
- [File descriptions](#file-descriptions)
- [How to use](#how-to-use)


## Prerequisites

- Python 3.7 to 3.10
- Mediapipe
- OpenCv

## File descriptions

- `0.Extracting Keypoints using Mediapipe.py`: Contains code for Testing Keypoints Extraction using Mediapipe Holistic.
- `1.Setup Folders for Collection.py`: The file that contains the code for creating Data Collection Folders. Input(actions, sequences, sequence_length)
- `2.Collect Data for Training and Testing.py`: Code that run real-time web-camera feed to collect keypoints using Mediapipe for the words/actions specified. Input(actions, sequences, sequence_length)

## How to use

1. Clone the repository to your local machine or have the 3 files downloaded mentioned under [File descriptions](#file-descriptions)

    ``` bash
    git clone https://github.com/MehrinFirdousi/Synaera-TeamSemaphore.git
    ```

2. Navigate to the directory where the code is located. (if downloaded separately then make sure all the files are in the same directory)
