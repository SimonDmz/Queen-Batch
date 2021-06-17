# Queen-Back-Office
Batch service for Queen  
Batch using to implement QUEEN DB from xml files.

## Requirements
For building and running the application you need:
- [JDK 1.11](https://jdk.java.net/archive/)
- Maven 3 

# Add lunatic librairy to project
``` shell
mvn install:install-file -Dfile=lib/lunatic-model-2.1.1.jar -DgroupId=fr.insee.lunatic -DartifactId=lunatic-model -Dversion=2.1.1 -Dpackaging=jar
```

## Install and excute unit tests and ent-to-end tests
Use the maven clean and maven install  
``` shell
mvn clean install
```

## Running batch
Use the folowing cmd :
``` shell
echo $@
java8 -Xms64m -Xmx512m -classpath '/path/to/lib/*' -Dlog4j.configurationFile=file:/path/to/log4j2/config/log4j2.xml -Dproperties.path=/path/to/properties -DcheminLog=/path/to/log fr.insee.queen.batch.Lanceur $@
CODE_ERREUR=$?
echo "CODE ERREUR=$CODE_ERREUR"
exit $CODE_ERREUR
```

#### Properties file
Some properties are externalize in ${path.properties}/queen-bo.properties.  
Bellow, properties to define :
``` shell
fr.insee.queen.persistence.database.host = localhost
fr.insee.queen.persistence.database.port = 5433
fr.insee.queen.persistence.database.schema = XXXXXXX
fr.insee.queen.persistence.database.user = XXXXXXX
fr.insee.queen.persistence.database.password = XXXXXXX
fr.insee.queen.persistence.database.driver = org.postgresql.Driver
fr.insee.queen.application.persistenceType = (JPA or MONGODB)
fr.insee.queen.folder.in=path/to/in
fr.insee.queen.folder.out=path/to/out
fr.insee.queen.paradata.id=idSu
fr.insee.queen.paradata.events=events
```

## Before you commit
Before committing code please ensure,  
1 - README.md is updated  
2 - A successful build is run and all tests are sucessful  
4 - All newly added properties are documented  

## Libraries used
- spring-core
- spring-jdbc
- spring-oxm
- spring-data-jpa
- commons-lang3
- postgresql
- mongoDb
- liquibase
- spring-test
- test-containers
- json-simple
- log4j
- lunatic-model


## Developers
- Benjamin Claudel (benjamin.claudel@keyconsulting.fr)
- Samuel Corcaud (samuel.corcaud@keyconsulting.fr)
- Paul Guillemet (paul.guillemet@keyconsulting.fr)