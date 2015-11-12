### Announcments ###
  * **2015-03-13: Dormancy is going to be migrated to Github and released as 2.0 with `EclipseLink` support and many other new features.**

  * **2013-05-20: Development of Version 2.0 has started.**
> It will be shipped with improved Hibernate compatibility i.e., `@AccessType` and JPA support and more.

  * **2012-12-31: Version 1.1.1 has been released.** Dormancy starts with a new version into 2013. Happy new Year!

  * **Dormancy now has its own Google+ page.**
> For getting the last information, follow <a href='https://plus.google.com/110160360744487784036'>Dormancy</a>**on**<a href='https://plus.google.com/110160360744487784036'><img src='https://ssl.gstatic.com/images/icons/gplus-32.png' /></a>


### What is Dormancy? ###
Dormancy is a framework that supports transparent exposure of attached JPA entities to GWT clients as well as merging new or detached entities back to the database.
Dormancy provides a modular and extensible architecture guaranteeing easy modifications.

In July 2009 Google released an article explaining the technical issues regarding the usage of GWT with Hibernate. Furthermore, they compared various approaches and listed their advantages and disadvantages. For detailed information, please refer to the article [Using GWT with Hibernate](https://developers.google.com/web-toolkit/articles/using_gwt_with_hibernate).

The following picture gives an overview about the role of Dormancy in typical projects using GWT and JPA.
A detailed explanation about the capabilities can be found [here](Architecture.md).

![http://dormancy.googlecode.com/svn/wiki/images/architecture.png](http://dormancy.googlecode.com/svn/wiki/images/architecture.png)

### Getting Started & Documentation ###
Please refer to the [Wiki](http://code.google.com/p/dormancy/wiki/GettingStarted) as well as the [Javadoc](http://dormancy.googlecode.com/svn/javadoc/index.html).

### News ###
  * 2012-12-31: [Version 1.1.1](History.md) released
  * 2012-11-18: [Version 1.1.0](History.md) released
  * 2012-08-27: [Version 1.0.3](History.md) released
  * 2012-08-02: [Version 1.0.2](History.md) released
  * 2012-07-15: [Report shows 93% code coverage](https://dormancy.googlecode.com/svn/report/coverage/index.html)
  * 2012-07-02: [Version 1.0.1](History.md) released
  * 2012-05-29: Version 1.0.0 released

### Similar Projects: ###
  * [Gilead](http://noon.gilead.free.fr/gilead/index.php?page=gwt)
> > [Gilead](http://sourceforge.net/projects/gilead/) permits you to use your Persistent POJO (and especially the partially loaded ones) outside the JVM (GWT, Flex, XML, Google AppEngine...) without pain. No lazy initialisation or serialization exception. Just POJO and Domain Driven Design :) !
  * [hibernate-pojo-bridge](http://code.google.com/p/hibernate-pojo-bridge/)
> > This library helps to deal with Hibernate entities and collections in GWT applications - using the simplest available solutions. You can use this library as a Gilead alternative for simple projects with Hibernate persistence (please note that Gilead currently has a wider set of features).
  * [JPA cloner](https://github.com/nociar/jpa-cloner)
> > The project allows cloning of JPA entity subgraphs. Entity subgraphs are defined by string patterns. String patterns define included relations which will be cloned.