**Explainable Machine Learning via Argumentation (EMLA)**

This repo will host a generalized version of [ArgEML](https://github.com/nicolepr/argeml), 
a general Explainable Machine Learning Framework and Methodology based on Argumentation. 
For more information please refer to paper [Explainable Machine Learning via Argumentation](https://link.springer.com/chapter/10.1007/978-3-031-44070-0_19).

At the moment, the repo includes the `oner` library that learns single condition rules from a given dataset using the concepts of 
"One Rule" classification algorithm [OneR](https://www.saedsayad.com/oner.htm). Limitations: 

## Usage
- Add `emla` module as dependency to your project

```xml
    <dependency>
        <groupId>emla.org</groupId>
        <artifactId>emla</artifactId>
        <version>0.0.1-SNAPSHOT</version>
    </dependency>
```

- Load data into a `Dataset` object, create a new `LearningSession` and calculate frequencies.

```Java
Dataset ds = new Dataset("./playtennis.csv", "playtennis", "play", 1, 0);
LearningSession emlaSession = new LearningSession(ds,"test");
List<FrequencyTable> frequencyTables = emlaSession.calculateFrequencyTables(ds, "train",null);
frequencyTables.forEach(f -> System.out.println(f.toString()));
```
- Frequency examples:
```
Frequency Table for predictor 'outlook':

>> for outlook=overcast (Coverage= 0.29 )  (Best target value = yes, with error=0 )  ) :
	[yes, 4]
>> for outlook=rain (Coverage= 0.36 )  (Best target value = yes, with error=0.4 )  ) :
	[no, 2]	[yes, 3]

Frequency Table for predictor 'temp':

>> for temp=cool (Coverage= 0.29 )  (Best target value = yes, with error=0.25 )  ) :
	[no, 1]	[yes, 3]
>> for temp=hot (Coverage= 0.29 )  (Best target value = no, with error=0.5 )  ) :
	[no, 2]	[yes, 2]
```
- Select a frequency to create a rule:
```Java
Frequency freqHighCoverageLowError = emlaSession.calculateFrequencyHighCoverageLowError(frequencyTables);
System.out.println(freqHighCoverageLowError.toString());
```
```
>> for outlook=overcast (Coverage= 0.29 )  (Best target value = yes, with error=0 )  ) :
	[yes, 4]
```