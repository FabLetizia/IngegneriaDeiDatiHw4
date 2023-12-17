import os
from datetime import datetime
from concurrent.futures import ThreadPoolExecutor

def download_file(id):
    os.system("aws s3 cp s3://pmc-oa-opendata/oa_comm/xml/all/" + id + ".xml ../docs --no-sign-request")

with open("pmcid_203600_254500.txt", 'r') as fp:
    id_list = fp.readline().strip("[]").replace("'", "").split(", ")

start = datetime.now()

with ThreadPoolExecutor() as executor:
    executor.map(download_file, id_list)

end = datetime.now()

print("Fine dell'Estrazione")

print(f"Tempo totale necessario per l'estrazione dei documenti: {end - start}")