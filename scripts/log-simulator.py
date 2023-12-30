import pandas as pd
import json
from pathlib import Path
from kafka import KafkaProducer

basedir = str(Path(__file__).resolve().parent.parent)

def simulate_log_producer(file_path, topic_name, bootstrap_servers='localhost:9092', delay=1):
    producer = KafkaProducer(bootstrap_servers=bootstrap_servers,
                             value_serializer=lambda v: json.dumps(v).encode('utf-8'))

    df = pd.read_csv(file_path, quotechar='"')

    for index, row in df.iterrows():
        print(f"Sending message: {index}: {row}")
        producer.send(topic_name, row.to_dict())
        producer.flush()

    producer.close()

topic_name = 'log_replay'
file_path =  basedir + "/sample-data/consolidated.csv"
print(f"Processing file: {file_path}")
simulate_log_producer(file_path, topic_name)