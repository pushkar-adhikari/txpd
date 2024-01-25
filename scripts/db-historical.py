import logging
import time
from datetime import datetime, timedelta
from sqlalchemy import create_engine
import pandas as pd
import json
from pandas import Timestamp
from confluent_kafka import Producer

# Configure logging
logging.basicConfig(level=logging.INFO, format='%(asctime)s - %(levelname)s - %(message)s')

# Database configuration
db_config = {

}

# Kafka configuration
kafka_config = {
    'bootstrap.servers': 'localhost:9092'
}

# Kafka topic
topic = 'log_replay'

# Connect to the database
def connect_db():
    try:
        logging.info("Connecting to database..")
        connection_str = f"mssql+pyodbc://{db_config['username']}:{db_config['password']}@{db_config['server']}/{db_config['database']}?driver={db_config['driver']}&TrustServerCertificate=yes"
        engine = create_engine(connection_str)
        logging.info("Database connection successful!?!")
        return engine.connect()
    except Exception as e:
        logging.error("Database connection failed: %s", e)
        return None

# Query the database
def query_db(conn, last_processed_id):
    try:
        query = f"""
                SELECT TOP(200)
                    [ExecutionPackageLogDetails].[ExecutionPackageLogDetailId] as PackageLogDetailId,
                    [ExecutionPackageLogDetails].[Name] as PackageLogDetailName,
                    [ExecutionPackageLogDetails].[ObjectName] as PackageLogDetailObjectName,
                    CAST([ExecutionPackageLogDetails].[Start] AS DATETIME2) as PackageLogDetailStart,
                    CAST([ExecutionPackageLogDetails].[End] AS DATETIME2) as PackageLogDetailEnd,
                    [ExecutionPackageLogDetails].[EndStatus] as PackageLogDetailEndStatus,
                    [ExecutionPackageLogs].[ExecutionId] as PackageLogId,
                    [ExecutionPackageLogs].[ExecutionPackageName] as PackageLogName,
                    CAST([ExecutionPackageLogs].[Start] AS DATETIME2) as PackageLogStart,
                    CAST([ExecutionPackageLogs].[End] AS DATETIME2) as PackageLogEnd,
                    [ExecutionPackageLogs].[EndStatus] as PackageLogEndStatus,
                    [ExecutionPackages].[ExecutionPackageId] as PackageId,
                    [ExecutionPackages].[Name] as PackageName,
                    [Projects].[ProjectId] as ProjectId,
                    [Projects].[Name] as ProjectName
                FROM [TX_REPOSITORY].[dbo].[ExecutionPackageLogs]
                    INNER JOIN [TX_REPOSITORY].[dbo].[ExecutionPackages] ON [ExecutionPackages].[ExecutionPackageId] = [ExecutionPackageLogs].[ExecutionPackageId]
                    INNER JOIN [TX_REPOSITORY].[dbo].[ExecutionPackageLogDetails] ON [ExecutionPackageLogDetails].[ExecutionId] = [ExecutionPackageLogs].[ExecutionId]
                    INNER JOIN [TX_REPOSITORY].[dbo].[Projects] ON [Projects].[ProjectId] = [ExecutionPackages].[ProjectId]
                WHERE [Projects].[IsDeployedVersion] = 1
                    AND [Projects].[ValidTo] = 99999999
                    AND [ExecutionPackages].ValidTo = 99999999
                    AND [ExecutionPackageLogs].ExecutionPackageId in (
                        SELECT DISTINCT [ExecutionPackages].[ExecutionPackageId]
                        FROM [TX_REPOSITORY].[dbo].[ExecutionPackages]
                            INNER JOIN [TX_REPOSITORY].[dbo].[Projects] ON [ExecutionPackages].ProjectId = [Projects].ProjectId
                        where [Projects].IsDeployedVersion = 1
                            and [Projects].ValidTo = 99999999
                            and [ExecutionPackages].ValidTo = 99999999
                    )
                    AND [ExecutionPackageLogs].[Start] < '2024-01-24 00:00:00.000'
                    AND [ExecutionPackageLogs].[Start] > '2023-12-01 00:00:00.000'
                    AND [ExecutionPackageLogDetails].[End] is NOT null
                    AND [ExecutionPackageLogs].[End] is NOT null
                    AND [ExecutionPackageLogDetails].[ExecutionPackageLogDetailId] > {last_processed_id}
                ORDER BY PackageLogDetailId;
                """
        logging.info("Executing query: %d", last_processed_id)
        df = pd.read_sql_query(query, conn)
        logging.info("Query executed successfully!")
        return df
    except Exception as e:
        logging.error("Query failed: %s", e)
        return None

# Produce messages to Kafka and return the last processed ID
def produce_messages(producer, df):
    last_id = None
    for index, row in df.iterrows():
        try:
            row_dict = row.to_dict()
            message = json.dumps(row_dict, default=timestamp_serializer)
            producer.produce(topic, message.encode('utf-8'))
            producer.flush() # Ensure the message is sent before continuing
            last_id = row['PackageLogDetailId'] # Update last_id for each row
        except Exception as e:
            logging.error("Failed to send record to Kafka: %s", e)
            # Consider breaking out of the loop or handling this error differently
    if last_id is not None:
        logging.info("Last processed ID: %s", last_id)
    return last_id

def timestamp_serializer(obj):
    """ Custom serializer for objects not serializable by default json code """
    if isinstance(obj, (datetime, Timestamp)):
        return obj.strftime("%Y-%m-%d %H:%M:%S.%f")
    raise TypeError(f"Type {type(obj)} not serializable")

# Main polling function
def poll_database():
    conn = connect_db()
    if conn is None:
        return

    producer = Producer(kafka_config)
    last_processed_id = load_last_processed_id()

    while True:
        df = query_db(conn, last_processed_id)
        if df is not None and not df.empty:
            logging.info("Found %s records", df.shape[0])
            new_last_id = produce_messages(producer, df)
            if new_last_id is not None:
                last_processed_id = new_last_id
                save_last_processed_id(last_processed_id)
        else:
            logging.info("No new records found.")

        logging.info("Sleeping for 15 seconds..")
        time.sleep(15)

def save_last_processed_id(last_id):
    with open('last_historical_processed_id.txt', 'w') as file:
        file.write(str(last_id))

def load_last_processed_id():
    try:
        with open('last_historical_processed_id.txt', 'r') as file:
            return int(file.read().strip())
    except FileNotFoundError:
        return 0

# Run the script
if __name__ == "__main__":
    try:
        poll_database()
    except KeyboardInterrupt:
        logging.info("Script terminated by user.")
    except Exception as e:
        logging.error("Unexpected error occurred: %s", e)