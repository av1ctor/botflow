### Alfresco JODConverter

The Alfresco JODConverter (Java OpenDocument Converter) was forked from  https://github.com/mirkonasato/jodconverter which stopped
being maintaining in 2012. It contains patched code used by the Alfresco content services repository. The repository uses the
JODConverter to help automates document conversions using LibreOffice or OpenOffice.org.

Prior to this github repository, a patched version of JODConverter (source and jar) were available in the Alfresco maven repository
as com.artofsolving:jodconverter:2.1.0-alfresco-patched.

### Building and testing
The core project can be built by running Maven command:
~~~
cd jodconverter-core
mvn clean install -DskipTests
~~~

### Artifacts
The artifacts can be obtained by:
* downloading from [Alfresco repository](https://artifacts.alfresco.com/nexus/content/groups/public)
* getting as Maven dependency by adding the dependency to your pom file:
~~~
<dependency>
  <groupId>org.alfresco</groupId>
  <artifactId>alfresco-jodconverter-core</artifactId>
  <version>3.0.1</version>
</dependency>
~~~
and Alfresco Maven repository:
~~~
<repository>
  <id>alfresco-maven-repo</id>
  <url>https://artifacts.alfresco.com/nexus/content/groups/public</url>
</repository>
~~~
The SNAPSHOT version of the artifact is **never** published.

### Bamboo
The build plan in Bamboo is [PLAT-JOD](https://bamboo.alfresco.com/bamboo/browse/PLAT-JOD/latest)

### Contributing guide
Please use [this guide](CONTRIBUTING.md) to make a contribution to the project.