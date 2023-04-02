# Computer Vision - Action/Sign to Word  <!-- omit in toc -->

This repository contains the necessary components to train and run a deep learning model for action recognition and translating the sign action to words.

These are the folders:

## Notebooks

The `Notebooks` folder contains the code for training the model and testing the model. The code is provided in a Jupyter notebook, which allows for an interactive and intuitive way to train the model. The notebook includes instructions for training the model.
- `Recording Data Notebook`: Contains the code to create folders and Collect Data for training the model by specifying the words and sequences.
- `Training Notebook`: Contains the code to train the model using the Data collection folders created after recording the data.
- `Real-time Testing Notebook`: Includes the code necessary to load the trained model and run real-time tests to convert sign actions to words using OpenCV.
- `Main Notebook`: Conatains the code and scripts for all the above notebooks and additional testing and parameter tuning.

## Scripts

The `Scripts` folder contains the scripts for training the model. The code is provided, which allows for way to create the respective folders to Collect your own data for training. The notebook includes instructions for collecting data for training the model.

## Getting Started

- To get started, you will need to have Python version between 3.7 to 3.10.
- Have these dependecies/libraries installed: opencv-python, mediapipe, tensorflow, scikit-learn
- Once you have these dependencies installed, you can open the Jupyter notebook in the `Notebooks` folder and follow the instructions to either create your dataset, train the model or test the model in real-time The trained model can then be used to test the model in real-time to perfrom action/sign prediction. If you want to test the model you can go to the respective notebook for it.
- If you want to perform all these functions you can access the `Main Notebook`.
