import sys
import os
from sentence_transformers import SentenceTransformer

if len(sys.argv) < 3:
    print('Missing file with sentences and/or output file name')
    exit(1)

sentences_file = sys.argv[1]
output_file = sys.argv[2]
model = SentenceTransformer("all-MiniLM-L6-v2")

if not os.path.exists(sentences_file):
    print('File \'' + sentences_file + '\' does not exist')
    exit(1)

sentences = []

with open(sentences_file, 'r') as file:
    for line in file:
        sentence = line.strip()
        sentences.append(sentence)

embeddings = model.encode(sentences)

with open(output_file, 'w') as file:
    for embedding in embeddings:
        for val in embedding:
            file.write(str(val))
            file.write(' ')

        file.write('\n')