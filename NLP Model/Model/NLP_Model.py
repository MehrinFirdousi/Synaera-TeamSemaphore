import re
from keras.layers import Input, LSTM, Embedding, Dense
from keras.models import Model
import numpy as np
import time
import ast
import os

class colors:
    RED_BOLD = '\033[91m' + '\033[1m'
    WARNING = '\033[93m'
    FAIL = '\033[91m'
    ENDC = '\033[0m'
    UNDERLINE = '\033[4m'
    UNDERLINE_GREEN = '\033[4m' + '\033[92m'

def read_list_from_file():
    inputFile = open( "myVars.txt", "r")
    lines = inputFile.readlines()

    objects = []
    for line in lines:
        objects.append(ast.literal_eval(line))
    
    return objects[0][0], objects[0][1], objects[0][2], objects[0][3], objects[0][4], objects[0][5], objects[0][6]

# get the start time
st_final = time.time()
st = time.time()

max_length_src, max_length_tar, num_encoder_tokens, num_decoder_tokens, input_token_index, target_token_index, reverse_target_char_index = read_list_from_file()

print(colors.UNDERLINE_GREEN + 'Importing Variables:' + colors.ENDC, round(time.time() - st, 2), 'seconds')
st = time.time()

latent_dim = 50

# Encoder
encoder_inputs = Input(shape=(None,))
enc_emb =  Embedding(num_encoder_tokens, latent_dim, mask_zero = True)(encoder_inputs)
encoder_lstm = LSTM(latent_dim, return_state=True)
encoder_outputs, state_h, state_c = encoder_lstm(enc_emb)
# We discard `encoder_outputs` and only keep the states.
encoder_states = [state_h, state_c]

# Set up the decoder, using `encoder_states` as initial state.
decoder_inputs = Input(shape=(None,))
dec_emb_layer = Embedding(num_decoder_tokens, latent_dim, mask_zero = True)
dec_emb = dec_emb_layer(decoder_inputs)

'''
We set up our decoder to return full output sequences, and to return internal states as well. 
We don't use the return states in the training model, but we will use them in inference.
'''
decoder_lstm = LSTM(latent_dim, return_sequences=True, return_state=True)
decoder_outputs, _, _ = decoder_lstm(dec_emb, initial_state=encoder_states)
decoder_dense = Dense(num_decoder_tokens, activation='softmax')
decoder_outputs = decoder_dense(decoder_outputs)
# Define the model that will turn `encoder_input_data` & `decoder_input_data` into `decoder_target_data`
model = Model([encoder_inputs, decoder_inputs], decoder_outputs)

print(colors.UNDERLINE_GREEN + 'Setting up Model:' + colors.ENDC, round(time.time() - st, 2), 'seconds')
st = time.time()

model.load_weights('nmt_weights_v8.h5')

print(colors.UNDERLINE_GREEN + 'Loading Weights:' + colors.ENDC, round(time.time() - st, 2), 'seconds')
st = time.time()

### INFERENCING ###
encoder_model = Model(encoder_inputs, encoder_states) # Encode the input sequence to get the "thought vectors"

# Decoder setup - Below tensors will hold the states of the previous time step
decoder_state_input_h = Input(shape=(latent_dim,))
decoder_state_input_c = Input(shape=(latent_dim,))
decoder_states_inputs = [decoder_state_input_h, decoder_state_input_c]
dec_emb2 = dec_emb_layer(decoder_inputs) # Get the embeddings of the decoder sequence

# To predict the next word in the sequence, set the initial states to the states from the previous time step
decoder_outputs2, state_h2, state_c2 = decoder_lstm(dec_emb2, initial_state=decoder_states_inputs)
decoder_states2 = [state_h2, state_c2]
decoder_outputs2 = decoder_dense(decoder_outputs2) # A dense softmax layer to generate prob dist. over the target vocabulary

# Final decoder model
decoder_model = Model(
    [decoder_inputs] + decoder_states_inputs,
    [decoder_outputs2] + decoder_states2)

print(colors.UNDERLINE_GREEN + 'Setting up Decoder:' + colors.ENDC, round(time.time() - st, 2), 'seconds')
st = time.time()

# Reverse-lookup token index to decode sequences back to something readable.
def decode_sequence(input_text):
    encoder_input_data = np.zeros((1, max_length_src), dtype='float32')
    error_word = ''
    try:
        for i, input_text in enumerate([input_text]):
            #print(colors.WARNING + "i:", i, " | input_text: ", input_text, "" + colors.ENDC)
            for t, word in enumerate(input_text.split()):
                error_word = word
                encoder_input_data[i, t] = input_token_index[word]
    except:
        return colors.RED_BOLD + '"' + error_word + '" doesn\'t exist in the dataset.' + colors.ENDC
    
    states_value = encoder_model.predict(encoder_input_data)
    
    target_seq = np.zeros((1, 1))
    target_seq[0, 0] = target_token_index['START_']
    stop_condition = False
    decoded_sentence = ''

    while not stop_condition:
        output_tokens, h, c = decoder_model.predict([target_seq] + states_value)
        sampled_token_index = np.argmax(output_tokens[0, -1, :])
        sampled_char = reverse_target_char_index[sampled_token_index]
        decoded_sentence += ' ' + sampled_char
        
        if (sampled_char == '_END' or len(decoded_sentence) > 50):
            stop_condition = True
        
        target_seq = np.zeros((1, 1))
        target_seq[0, 0] = sampled_token_index
        states_value = [h, c]
    
    return decoded_sentence[:-4]

def preprocess_sentence(sentence):
    # lower case to standardize the sentence and remove extra spaces
    sentence = sentence.lower().strip()
    # if QM-wig or 6 Ws or How is in the sentence, then it is a question
    words = ['who', 'what', 'when', 'where', 'why', 'how']
    question_flag = 0
    if 'qm-wig' in sentence or any(word in sentence for word in words):
        question_flag = 1
    sentence = sentence.replace('qm-wig', '')

    # remove punctuation (isn't required but im still including it)
    sentence = re.sub(r"([?.!,])", "", sentence)
    # replace numbers with words
    number_replacements = {'1': " one ", '2':" two ", '3':" three ", '4':" four ", 
                           '5':" five ", '6':" six ", '7':" seven ", '8':" eight ", 
                           '9':" nine ", '0':" zero "}
    for key, value in number_replacements.items():
        sentence = sentence.replace(key, value)
    # remove extra spaces
    sentence = re.sub(r'[" "]+', " ", sentence)
    sentence = sentence.strip()

    words = sentence.split()
    result = []
    # Empty temporary list to store single letters
    temp = []
    for word in words:
        if len(word) == 1:
            temp.append(word)
        else:
            # If there are any single letters in the temporary list,
            # join them with a dash and append to the result list
            if temp:
                result.append('-'.join(temp))
                temp = []
            # Append the non-single letter word to the result list
            result.append(word)
    if temp:
        result.append('-'.join(temp))
    
    # Save the dashed words in a list so that it can be replaced later
    replaced_words = [match for match in result if "-" in match]
    # Replace the single letters with 'XXXXX' in the result list
    result = ["xxxxx" if '-' in element else element for element in result]
    # Join the words in the result list back into a string sentence
    sentence = ' '.join(result)

    return sentence, question_flag, replaced_words

while True:
    input_text = input(colors.WARNING + 'Input ASL sentence: ' + colors.ENDC)
    # os.system('cls')
    st = time.time()
    prep_input, question_flag, replaced_words = preprocess_sentence(input_text)
    #prep_input, question_flag, replaced_words = preprocess_sentence(sentences_to_test[sentences_counter])
    if prep_input == 'exit':
        break
    
    # if only 1 word is given, then no need to decode
    decoded_sentence = decode_sequence(prep_input) if len(prep_input.split()) > 1 else prep_input

    # if '?' not in decoded sentence and original input had 'QM-wig' then add '?' at the end
    if '?' not in decoded_sentence and question_flag == 1:
        decoded_sentence = decoded_sentence.strip() + '?'

    # Replace the 'XXXXX' with the original single letter words
    for word in replaced_words:
        decoded_sentence = decoded_sentence.replace('xxxxx', word.replace('-',''), 1)
    decoded_sentence = decoded_sentence.replace('xxxxx', '')
    
    # if decoded sentence contains ['who', 'what', 'when', 'where', 'why', 'how'] then add '?' at the end
    if any(word in decoded_sentence for word in ['who', 'what', 'when', 'where', 'why', 'how']) and '?' not in decoded_sentence:
        decoded_sentence = decoded_sentence.strip() + '?'
     
    #sentences_translation.append(decoded_sentence)
    # Outputs 
    print(colors.WARNING + '\nInput ASL sentence:' + colors.ENDC + "'" + input_text + "'")
    print(colors.WARNING + 'Preprocessed Input:' + colors.ENDC + "'" + prep_input + "'")
    print(colors.WARNING + 'Predicted English Translation:' + colors.ENDC, decoded_sentence)
    print(colors.UNDERLINE_GREEN + 'Decoding Sequence:' + colors.ENDC, round(time.time() - st, 2), 'seconds')

print(colors.UNDERLINE_GREEN + 'Total Execution time:' + colors.ENDC, round(time.time() - st_final, 2), 'seconds')