# Frequently Asked Questions #



## General ##

### Which JARs do I need? ###
You can simply download `dormancy-core-<version>-dist.zip` and `dormancy-hibernate3-<version>-dist.zip` or `dormancy-hibernate4-<version>-dist.zip` from the [Downloads](http://code.google.com/p/dormancy/downloads/list) section (use either `hibernate3` or `hibernate4`).
Furthermore, you can download `dormancy-closure-<version>-dist.zip`, which contains closure like functions for building extensions fast and easily.
They contain all dependencies required for Dormancy including Hibernate. However, you can of course replace the shipped JARs with the desired version.

The file `dormancy-<version>.zip` contains all Dormancy modules (including `closure`, `hibernate3`, `hibernate4`, `jpa-hibernate` and `jpa-eclipselink`) but contains no dependencies.
Please also refer to the next question.

### I maintain the libraries myself. Which third-party JARs do I need? ###
Basically, this depends on the JPA provider and the features you want to use. In general, you will need the following libraries:
| antlr								| antlr								| 2.7.7			|
|:-------------|:-------------|:--------|
| aopalliance							| aopalliance							| 1.0				 |
| asm									 | asm									 | 3.3.1			|
| cglib								| cglib								| 2.2.2			|
| com.google.code.findbugs				| jsr305								| 2.0.1			|
| commons-beanutils					| commons-beanutils					| 1.8.3			|
| commons-collections					| commons-collections					| 3.2.1			|
| commons-lang							| commons-lang							| 2.6				 |
| commons-logging						| commons-logging						| 1.1.1			|
| dom4j								| dom4j								| 1.6.1			|
| javax.inject							| javax.inject							| 1				   |
| javax.persistence					| persistence-api						| 1.0.2			|
| javax.transaction					| jta									 | 1.1				 |
| log4j								| log4j								| 1.2.17			|
| org.aspectj							| aspectjrt							| 1.7.1			|
| org.aspectj							| aspectjweaver						| 1.7.1			|
| org.eclipse.persistence				| org.eclipse.persistence.asm			| 2.5.0			|
| org.eclipse.persistence				| org.eclipse.persistence.antlr		| 2.5.0			|
| org.eclipse.persistence				| org.eclipse.persistence.core			| 2.5.0			|
| org.eclipse.persistence				| org.eclipse.persistence.jpa			| 2.5.0			|
| org.eclipse.persistence				| org.eclipse.persistence.jpa.jpql		| 2.5.0			|
| org.hibernate.common					| hibernate-commons-annotations		| 4.0.1.Final		|
| org.hibernate						| ejb3-persistence						| 1.0.2.GA			|
| org.hibernate						| hibernate-annotations				| 3.4.0.GA			|
| org.hibernate						| hibernate-commons-annotations		| 3.1.0.GA			|
| org.hibernate						| hibernate-core						| 3.3.2.GA			|
| org.hibernate						| hibernate-core						| 4.2.0.Final		|
| org.hibernate.javax.persistence		| hibernate-jpa-2.0-api				| 1.0.0.Final		|
| org.javassist						| javassist							| 3.16.1-GA		|
| org.jboss.logging					| jboss-logging						| 3.1.0.CR2		|
| org.jboss.spec.javax.transaction		| jboss-transaction-api\_1.1\_spec		| 1.0.0.Final		|
| org.slf4j							| slf4j-api							| 1.5.8			|
| org.springframework					| spring-aop							| 3.1.0.RELEASE	|
| org.springframework					| spring-asm							| 3.1.0.RELEASE	|
| org.springframework					| spring-beans							| 3.1.0.RELEASE	|
| org.springframework					| spring-context						| 3.1.0.RELEASE	|
| org.springframework					| spring-core							| 3.1.0.RELEASE	|
| org.springframework					| spring-expression					| 3.1.0.RELEASE	|
| org.springframework					| spring-jdbc							| 3.1.0.RELEASE	|
| org.springframework					| spring-orm							| 3.1.0.RELEASE	|
| org.springframework					| spring-tx							| 3.1.0.RELEASE	|

### Which JPA providers are supported? ###
Currently Dormancy supports Hibernate 3, Hibernate 4 and EclipseLink.
There are no plans to support other JPA providers in the near future.
However, if you plan to adapt Dormancy to support another JPA implementation or persistence framework, please let me know.

## Maven ##

### The Dormancy modules are not available in any public repository. How do I install them? ###
All modules are hosted on Google Code. To use the repository in your project, simply add the following lines to your `pom.xml`:
```
<repositories>
	<repository>
		<id>dormancy.googlecode.com</id>
		<name>Maven Repository for Dormancy</name>
		<url>http://dormancy.googlecode.com/svn/m2/repository</url>
	</repository>
</repositories>
```

### Which modules do I need? ###
You basically need one of `dormancy-hibernate3`, `dormancy-hibernate4`, `dormancy-jpa-hibernate` or `dormancy-jpa-eclipselink` depending on the JPA provider you use.
Therefore, the appropriate dependency has to be added to the `pom.xml`:
```
<dependency>
	<groupId>at.dormancy</groupId>
	<artifactId>dormancy-hibernate3</artifactId>
	<version>2.0.0</version>
</dependency>
```
or
```
<dependency>
	<groupId>at.dormancy</groupId>
	<artifactId>dormancy-hibernate4</artifactId>
	<version>2.0.0</version>
</dependency>
```
or
```
<dependency>
	<groupId>at.dormancy</groupId>
	<artifactId>dormancy-jpa-hibernate</artifactId>
	<version>2.0.0</version>
</dependency>
```
or
```
<dependency>
	<groupId>at.dormancy</groupId>
	<artifactId>dormancy-jpa-eclipselink</artifactId>
	<version>2.0.0</version>
</dependency>
```
The `dormancy-core` module and all required dependencies are included automatically.

## Spring ##

### Why does Dormancy require many Spring modules? ###
Spring provides many aspect oriented utility classes for beans.
Thus it is a logical consequence to make use of them instead of providing proprietary mechanisms.
Furthermore, it is very likely that a dependency injection framework is used for wiring the business logic together.
However, Dormancy does not force you to use Spring as your IoC container.