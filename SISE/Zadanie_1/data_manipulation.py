import pandas as pd
import matplotlib.pyplot as plt
import seaborn as sns
from matplotlib.ticker import MaxNLocator, AutoLocator

level = "level"
id = "id"
algorythm = "algorythm"
order = "order"
sol_length = "sol_length"
visited = "visited"
processed = "processed"
max_depth = "max_depth"
time = "time"

data = pd.read_csv("all_stats.csv",
                   names=[level, id, algorythm, order, sol_length, visited, processed, max_depth, time], delimiter=";")


def plot_function(given_data, ylabel, title, category):
    # Group the filtered data by 'level' and 'order', then calculate the mean of 'category' for each group
    average_data = given_data.groupby([level, order])[category].mean().reset_index()

    # Set the seaborn style with gridlines
    sns.set_style("whitegrid")

    # Plot the average data, specifying the correct 'category' for the y-axis
    plt.figure(figsize=(11.5, 6))
    plt.xticks(rotation=0)
    sns.barplot(x='level', y=category, hue='order', data=average_data)
    plt.xlabel('Głębokość')
    plt.ylabel(ylabel)
    plt.title(title)
    plt.rcParams.update({'font.size': 14})
    # plt.yscale("log")
    plt.legend(title='Order')
    plt.grid(True, which='both')  # Show both horizontal and vertical gridlines
    plt.legend(title='Order', bbox_to_anchor=(0.995, 1), loc='upper left')
    plt.tight_layout()
    plt.savefig(f"{category}_{title}.png".replace("*", "str"))


def plot_function_all(given_data, ylabel, category):
    # Calculate the average solution length for each level separately for each algorithm
    average_data = given_data.groupby(['algorythm', 'level'])[category].mean().unstack('algorythm')

    # Set the seaborn style with gridlines
    sns.set_style("whitegrid")

    # Plot the average solution length for each level separately for each algorithm
    ax = average_data.plot(kind='bar', xlabel='Głębokość', ylabel=ylabel,
                           title='Ogółem', figsize=(11.5, 6))

    # Set font size for various elements
    plt.xticks(rotation=0, fontsize=12)
    plt.yticks(fontsize=12)
    plt.title('Ogółem', fontsize=14)
    plt.xlabel('Głębokość', fontsize=14)
    plt.ylabel(ylabel, fontsize=14)
    plt.legend(title='Algorithm', bbox_to_anchor=(0.995, 1), loc='upper left', fontsize=12, title_fontsize=12)

    # Set font size for tick labels on the y-axis
    ax.tick_params(axis='y', labelsize=12)

    # plt.yscale("log")
    plt.tight_layout()
    plt.savefig(f"{category}_ALL.png")


def all_plots(data, ylabel, category):
    plot_function_all(data, ylabel, category)

    for a in ["astr", "bfs", "dfs"]:
        algorythm_data = data[(data[algorythm] == a)]
        plot_function(algorythm_data, ylabel, get_name(a), category)


def get_name(algorythm):
    if algorythm == "astr":
        return "A*"
    if algorythm == "bfs":
        return "BFS"
    if algorythm == "dfs":
        return "DFS"


# ------------------------------------------------ sol_length
filtered_data = data[data[sol_length] != -1]
all_plots(filtered_data, "Długość rozwiązania", sol_length)

# ------------------------------------------------ visited
all_plots(data, "Stany odwiedzone", visited)

# ------------------------------------------------ processed
all_plots(data, "Stany przetworzone", processed)

# ------------------------------------------------ max_depth
all_plots(data, "Maksymalna głębokość rekursji", max_depth)

# ------------------------------------------------ time
all_plots(data, "Czas procesu [s]", time)
