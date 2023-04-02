# ASL Gloss to English Translator <!-- omit in toc -->

This is a script for translating American Sign Language (ASL) gloss to English. It uses a neural machine translation (NMT) model built with the Keras library. The NMT model is a combination of an encoder and a decoder, both implemented using Long Short-Term Memory (LSTM) networks. The script loads pre-trained model weights and perform inference to translate a given ASL gloss sentence.

## Table of Contents <!-- omit in toc -->

- [Prerequisites](#prerequisites)
- [File descriptions](#file-descriptions)
- [How to use](#how-to-use)
- [How it works](#how-it-works)


## Prerequisites

- Python 3.x
- Keras
- Numpy

## File descriptions

- `NLP_Model.py`: The main code file that contains the code to translate ASL gloss to English.
- `myVars.txt`: The file that contains the variables required for the code to run.
- `nmt_weights_v8.h5`: The trained model weights file.

## How to use

1. Clone the repository to your local machine or have the 3 files downloaded mentioned under [File descriptions](#file-descriptions)

    ``` bash
    git clone https://github.com/MehrinFirdousi/Synaera-TeamSemaphore.git
    ```

2. Navigate to the directory where the code is located. (if downloaded separately then make sure all the files are in the same directory)

3. If you want to use the terminal make sure python is added to PATH variable

    ``` bash
    python NLP_Model.py 
    ```

4. There is a loop that allows you to keep translating, if you want to exit the loop and end the program just type `exit`

    ``` bash
    Input ASL sentence: [Enter ASl text you want to translate]
    # or 
    Input ASL sentence: exit # to end the program
    ```

## How it works

When the script is run, it will prompt the user to input an ASL gloss sentence. The input sentence is then processed by the encoder to obtain the internal state vectors, which is then passed to the decoder to generate the translated English sentence . The generated sentence is then printed to the console.

In more detail:

1. Encoder reads the input sequence and summarizes the information in something called as the internal state vectors (in case of LSTM these are called as the hidden state and cell state vectors). We discard the outputs of the encoder and only preserve the internal states.

2. Decoder is an LSTM whose initial states are initialized to the final states of the Encoder LSTM. Using these initial states, decoder starts generating the output sequence.

3. The decoder behaves a bit differently during the training and inference procedure. During the training, we use a technique call teacher forcing which helps to train the decoder faster. During inference, the input to the decoder at each time step is the output from the previous time step.

4. Intuitively, the encoder summarizes the input sequence into state vectors (sometimes also called as Thought vectors), which are then fed to the decoder which starts generating the output sequence given the Thought vectors. The decoder is just a language model conditioned on the initial states.

___
**Note**: The model's performance may be improved by fine-tuning the model on a larger and more diverse training dataset, or by using a more advanced NMT architecture (which will increase processing time). Additionally, the script uses a greedy decoding strategy, meaning that it always chooses the most likely English word at each time step without considering the full context of the sentence. More sophisticated decoding strategies, such as beam search, could be implemented to generate more accurate translations.
