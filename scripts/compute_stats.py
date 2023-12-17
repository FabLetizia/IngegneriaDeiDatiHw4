import pandas as pd
import os
import glob
from datetime import datetime
from threading import Lock
from concurrent.futures import ThreadPoolExecutor

def process_file(file):
    print(file)
    f = pd.read_json(file)
    tables, figures = f.loc["tables"]["content"], f.loc["figures"]["content"]
    n_tables, n_figures = len(tables), len(figures)

    with lock:
        if n_tables not in tables_dict:
            tables_dict[n_tables] = 1
        else:
            tables_dict[n_tables] += 1

        if n_figures not in figures_dict:
            figures_dict[n_figures] = 1
        else:
            figures_dict[n_figures] += 1
    
        for table in tables:
            n_unique_cells = len(table["cells"])
            
            if n_unique_cells not in unique_cells_dict:
                unique_cells_dict[n_unique_cells] = 1
            else:
                unique_cells_dict[n_unique_cells] += 1

###########
# MAIN
###########
tables_dict, figures_dict, unique_cells_dict = dict(), dict(), dict()

directory_path = os.path.abspath("path/to/json/files") # Da sostituire
pattern = os.path.join(directory_path, "*.json")
files = glob.glob(pattern)

lock = Lock()

start = datetime.now()
print("Starting the Stats' extraction...")
with ThreadPoolExecutor() as executor:
    executor.map(process_file, files)
end = datetime.now()

print("End of Stats' extraction")
print("Needed Time: " + str(end-start))

sorted_tables_dict = dict(sorted(tables_dict.items()))
sorted_figures_dict = dict(sorted(figures_dict.items()))
sorted_unique_cells_dict = dict(sorted(unique_cells_dict.items()))

tables_stats = pd.DataFrame(data={"num_tables": list(sorted_tables_dict.keys()), "num_files": list(sorted_tables_dict.values())})
figures_stats = pd.DataFrame(data={"num_figures": list(sorted_figures_dict.keys()), "num_files": list(sorted_figures_dict.values())})
cells_stats = pd.DataFrame(data={"num_unique_cells": list(sorted_unique_cells_dict.keys()), "num_files": list(sorted_unique_cells_dict.values())})

tables_stats.to_csv("./table_stats.csv", index=False)
figures_stats.to_csv("./figures_stats.csv", index=False)
cells_stats.to_csv("./cells_stats.csv", index=False)