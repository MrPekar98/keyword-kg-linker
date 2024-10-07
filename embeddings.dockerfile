FROM python:3.9

RUN pip install --upgrade gensim
ENTRYPOINT bash -c "python -m gensim.downloader --download 'word2vec-google-news-300' && python -c 'import shutil ; shutil.move(\"/root/gensim-data/word2vec-google-news-300/word2vec-google-news-300.gz\", \"/model/model.bin.gz\")'"
