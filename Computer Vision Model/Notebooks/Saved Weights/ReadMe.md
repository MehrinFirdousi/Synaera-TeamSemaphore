------- Final Models ---------

Model_4p_13ws_5fps_new.h5 => 
- People: 4ppl
- Words: ['NoSign','hello','you','work','where','how','your','day','b','o','me','live','university']
- No. of Vids per word: 300 vids
- Length of vid: 5 frames

Model_4p_13ws_9fps_new.h5 => 
- People: 4ppl
- Words: ['NoSign','hello','you','work','where','how','your','day','b','o','me','live','university']
- No. of Vids per word: 300 vids
- Length of vid: 9 frames

Model_4p_13ws_13fps_new.h5 => 
- People: 4ppl
- Words: ['NoSign','hello','you','work','where','how','your','day','b','o','me','live','university']
- No. of Vids per word: 300 vids
- Length of vid: 13 frames

------- Model Tests ---------

## Demo3 only A including How

Demo3_A_8ws.h5 => (Aamir – 120 vids) including ‘HOW’ 
- People: [Aamir]
- Words: ['NoSign','hello','thanks','sorry','you','work','where','how'] 
- No. of Vids per word: 120 vids
- Length of vid: 25 frames
- camera position: front cam

Demo3_A_8ws_dup.h5 => (Aamir – 120 vids x2 duplicate = 240) including ‘HOW’ 
- People: [Aamir]
- Words: ['NoSign','hello','thanks','sorry','you','work','where', 'how'] 
- No. of Vids per word: 240 vids
- Length of vid: 25 frames
- camera position: front cam


## Same as Demo.h5 but WITHOUT PLEASE

Demo2_AAA.h5 => 
- People: [Aamir, Adam, Ayesha]
- Words: ['NoSign','hello','thanks','sorry','you','work','where'] 
- No. of Vids per word: 120 vids
- Length of vid: 25 frames
- camera position: front cam

Demo2_AAA_dup.h5 => (Duplicated x2 – same vids used twice from Demo.h5 model)
- People: [Aamir, Adam, Ayesha]
- Words: ['NoSign','hello','thanks','sorry','you','work','where'] 
- No. of Vids per word: 120 vids x2 = 240
- Length of vid: 25 frames
- camera position: front cam


Demo.h5 / Demo2.h5 => 
- People: [Aamir, Adam, Ayesha]
- Words: ['NoSign','hello','thanks','please','sorry','you','work','where'] 
- No. of Vids per word: 40 vids
- Length of vid: 25 frames
- Camera position: front cam


------- Parameter Test Cases-----------
### test1 => 3 words, 30 vids, 30 frames (camera position - waist height)
    # avg performance, takes time to recognize, when not doing any sign by default it always detects hello
    
### test2 => 3 words + 1 default, 30 vids, 30 frames  (camera position - shoulder height)
    # Was good for nosign, hello and thank you but iloveyou wasnt good

### test3 => 3 words + 1 default, 40 vids, 30 frames  (camera position - shoulder height)
    # good performance, used in the proof of concept as well

### test4 => Ayesha -> 3 words + 1 default, 30 vids, 30 frames (camera position - shoulder height)
    # 

### test5 => HADI -> 3 words + 1 default, 30 vids, 30 frames (camera position - shoulder height)
    # 

### test5 => 3 words + 1 default, 30 vids, 30 frames  (camera position - shoulder height)
    # 

### test6 => 3 words, 30 vids, 30 frames  (camera position - waist height)

### test7 => Combination of tests 3,4,5 and check with 3 words, 100 vids, 30 frames  (camera position - shoulder height)
    #
### test8 => 3 words + 1 default, 30 vids, 25 frames  (camera position - shoulder height)
    # 
### test9 => 3 words + 1 default, 30 vids, 20 frames  (camera position - shoulder height)
    # Faster recognition of words but it can become very frantic sometimes

For real time mobile vid testing:
### test10 => 3 words + 1 default, 30 vids, 30 frames  (camera position - standing)
### test11 => 3 words + 1 default, 30 vids, 30 frames  (camera position - standing) 
### test12 => 3 words + 1 default, 30 vids, 30 frames  (camera position - sitting)
### test13 => 3 words + 1 default, 30 vids, 30 frames  (camera position -sitting)

###  test14 => ['NoSign','hello', 'thanks', 'iloveyou'], 30 vids, 25 frames  (camera position – front cam)
###  test15 => ['NoSign',’please , ‘yourewelcome, ’sorry’], 30 vids, 25 frames  (camera position – front cam)
###  test16 => ['NoSign',’please , ‘yourewelcome, ’sorry’], 30 vids, 25 frames  (camera position – front cam)
