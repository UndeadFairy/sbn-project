# Italian referendum v3 2016 

## Social and behavioral network analysis project 2018-2019

#### Created by:
Kristyna Dolezalova (@UndeadFairy)

Angie Catalina Carrillo Chappe

## Necessary setup

### 1. Clone the repository
The repository can be downloaded by using the github file download link or can be cloned locally using the following command.
```
git clone git@github.com:UndeadFairy/sbn-project.git
```

### 2. Provide needed datasets

#### Tweet stream files

All provided gzipped tweet stream files (*TW-1480170614348.gz* and other) need to be manually placed (or soft-linked) to a folder **src/main/resources/part0/given_data/**, to be reachable by the app.

For Part 1, the app needs a different tweet stream files structure, where in order to be able to fit files content into the RAM, they were ungzipped and split to files with size 1GB.
#### User graph

Because of the fact that different parts of the project were worked on separately, some parts of the code use gzipped graph file and other used raw text file. Because of that, in order to reproduce the analysis, both provided user graph file **Official_SBN-ITA-2016-Net** and gzipped version **Official_SBN-ITA-2016-Net.gz** should be placed to a folder **src/main/resources/part2/given_graph/**.

### 3. Run separate main files for respective parts of the project: 
- Part 0: **src/main/java/mainTemporalAnalysis.java**
- Part 1: **src/main/java/MainPartOne.java**
- Part 2: **src/main/java/mainSpreadOfInfluence.java**

Part 0 can be run in two different modes - using provided tweet stream data or downloading own data for the analysis. The mode is controlled by a Boolean variable in file **src/main/java/mainTemporalAnalysis.java**

`public static Boolean useOwnData = false;` If changed to `true`, in order to be able to access the Twitter API, a user also needs to insert his own Twitter API keys in file **src/main/java/configbuilder.java** in a following way:
```
cfg.setOAuthAccessToken("ACCESS-TOKEN");

cfg.setOAuthAccessTokenSecret("ACCESS-TOKEN-SECRET");

cfg.setOAuthConsumerKey("CONSUMER-KEY");

cfg.setOAuthConsumerSecret("CONSUMER-SECRET");

```

## **REPORT**

[Link to report (PDF).](SBN-Report.pdf)

