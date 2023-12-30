import pandas as pd
import os
from pathlib import Path
from pm4py.objects.conversion.log import converter as log_converter
from pm4py.objects.log.exporter.xes import exporter as xes_exporter

basedir = str(Path(__file__).resolve().parent.parent)
data_path = basedir + "/sample-data"
filename = "DSA_ADK.csv"

print(f"Processing folder: {data_path}")
print(f"Processing file: {filename}")
with open(os.path.join(data_path, filename), "r") as file:
    event_log_df = pd.read_csv(file)
    event_log_renamed = event_log_df.rename(
        columns={
            "PackageLogId": "case:id",
            "PackageLogName": "case:name",
            "PackageLogDetailName": "concept:name",
            "PackageLogDetailStart": "start_timestamp",
            "PackageLogDetailEnd": "end_timestamp",
            "PackageId": "package:id",
            "PackageName": "package:name",
            "ProjectName": "project:name",
            "StepId": "step:id",
            "PackageLogDetailObjectName": "step:object:name",
        }
    )
    parameters = {
        log_converter.Variants.TO_EVENT_LOG.value.Parameters.CASE_ID_KEY: "case:id"
    }
    event_log = log_converter.apply(
        event_log_renamed,
        parameters=parameters,
        variant=log_converter.Variants.TO_EVENT_LOG,
    )
    xes_path = os.path.join(data_path, filename.replace(".csv", ".xes"))
    xes_exporter.apply(event_log, xes_path)
