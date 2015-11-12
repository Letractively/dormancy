# Getting Dormancy #
Depending on the build system used by the project, there are several possibilities of how to integrate Dormancy.

## Using Dormancy in a Maven project ##
This is the most recommended option because Dormancy is a Maven project aswell.
Thus by adding the Maven Repository to the project, the latest version of Dormancy can be obtained automatically.
Therefore, the the following repository definition has to be added to the `pom.xml`:
```
<repositories>
	...
	<repository>
		<id>dormancy.googlecode.com</id>
		<name>Dormancy Repository</name>
		<url>https://dormancy.googlecode.com/svn/m2/repository/</url>
	</repository>
</repositories>
```

Dormancy supports Hibernate 3.x, Hibernate 4.x as well as JPA with Hibernate or EclipseLink.
Depending on the used JPA provider, the appropriate dependency has to be added to the `pom.xml`:
```
<dependencies>
	...
	<dependency>
		<groupId>at.dormancy</groupId>
		<artifactId>dormancy-hibernate3</artifactId>
		<version>2.0.0</version>
	</dependency>
</dependencies>
```
**or**
```
<dependencies>
	...
	<dependency>
		<groupId>at.dormancy</groupId>
		<artifactId>dormancy-hibernate4</artifactId>
		<version>2.0.0</version>
	</dependency>
</dependencies>
```
**or**
```
<dependencies>
	...
	<dependency>
		<groupId>at.dormancy</groupId>
		<artifactId>dormancy-jpa-hibernate</artifactId>
		<version>2.0.0</version>
	</dependency>
</dependencies>
```
**or**
```
<dependencies>
	...
	<dependency>
		<groupId>at.dormancy</groupId>
		<artifactId>dormancy-jpa-eclipselink</artifactId>
		<version>2.0.0</version>
	</dependency>
</dependencies>
```
Even if Hibernate 4 is not fully compatible to Hibernate 3, those modules can simply be exchanged by replacing `dormancy-hibernate3` by `dormancy-hibernate4` or the other way round.
The `dormancy-core` module and all required dependencies are included automatically.

However, a project contains several complex JPA entities that require custom logic for serialization/deserialization, it is recommended to add the `dormancy-closure` module, which contains `ContextFunction` and other helpful classes supporting quick extensions.
Further information about how to customize Dormancy can be found in the chapters [Using ContextFunctions](ContextFunction.md) and [Custom EntityPersisters](CustomEntityPersisters.md).
```
<dependency>
	<groupId>at.dormancy</groupId>
	<artifactId>dormancy-closure</artifactId>
	<version>2.0.0</version>
</dependency>
```

## Building Dormancy from scratch ##
The latest source code of Dormancy can be downloaded from the SVN repository by invoking the following command:

`svn checkout http://dormancy.googlecode.com/svn/trunk/ dormancy-read-only`

Please refer to the following page for further information about [checking out the source code](https://code.google.com/p/dormancy/source/checkout).

The project can be built with Maven by typing the following command:

`mvn -P hibernate3,fast install`

or

`mvn -P hibernate4,fast install`

or

`mvn -P jpa-hibernate,fast install`

or

`mvn -P jpa-eclipselink,fast install`

For executing the supplied JUnit tests, the `test` Maven profile has to be activated in addition to the `hibernate*` profile:

`mvn -P hibernate3,test install`

The tests for `jpa-hibernate` and `jpa-eclipselink` are located within the very same module. Thus, they can be executed by removing the `fast` profile e.g.,

`mvn -P jpa-eclipselink install`

Optionally, JAR files containing the source code and the Javadoc can be built by invoking the appropriate Maven plugins:

`mvn -P hibernate3 source:jar javadoc:jar install`

## Downloading a Dormancy release build ##
The official release builds can be downloaded from the [Downloads](https://code.google.com/p/dormancy/downloads/list) page.
For getting further information about the required libraries, please refer to the [Frequently Asked Questions](FrequentlyAskedQuestions.md).