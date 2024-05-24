import pandas as pd
import os

stat_f8 = './dane/f8/stat'
stat_f10 = './dane/f10/stat'

dyn_f8 = './dane/f8/dyn'
dyn_f10 = './dane/f10/dyn'


def combine(directory_path_f8, directory_path_f10, mode):
    # Lista przechowująca wszystkie DataFrame
    dfs = []

    # Iteracja po wszystkich plikach z f8
    for filename in os.listdir(directory_path_f8):
        if filename.endswith('.csv'):
            file_path = os.path.join(directory_path_f8, filename)
            df = pd.read_csv(file_path, header=None)  # Wczytujemy dane bez nagłówka
            dfs.append(df)

    # Iteracja po wszystkich plikachz f10
    for filename in os.listdir(directory_path_f10):
        if filename.endswith('.csv'):
            file_path = os.path.join(directory_path_f10, filename)
            df = pd.read_csv(file_path, header=None)  # Wczytujemy dane bez nagłówka
            dfs.append(df)

    # Połączenie wszystkich DataFrame w jeden
    combined_df = pd.concat(dfs, ignore_index=True)

    # Zapisanie połączonego DataFrame do nowego pliku CSV
    combined_df.to_csv(f'combined_{mode}.csv', index=False, header=False)

    print("Pliki zostały pomyślnie połączone i zapisane w combined.csv")


combine(stat_f8, stat_f10, 'stat')
combine(dyn_f8, dyn_f10, 'dyn')
