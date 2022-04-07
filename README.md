![Logo](logo.png?raw=true&v=4&s=200)

[![Build Status](https://travis-ci.org/rpau/junit4git.svg?branch=master)](https://travis-ci.org/rpau/junit4git)
[![codecov](https://codecov.io/gh/rpau/junit4git/branch/master/graph/badge.svg)](https://codecov.io/gh/rpau/junit4git)

This is a JUnit extension that ignores those tests that are not related with 
your last changes in your Git repository. This is not a new idea, since big companies, specially
with big mono-repos have worked on it.

[Martin Fowler](https://martinfowler.com/articles/rise-test-impact-analysis.html) also describes how to use the test
impact to infer the minimum tests to run.

You can use it from Maven and Gradle projects in any Junit based
project (written in any JVM based language: e.g Kotlin, Java, Scala)

## Getting Started
These instructions will get you a copy of the project up and running on your local machine.

### Requirements

- JRE 8
- Git
- Maven or Gradle
- Master as a default base branch

### 1. Configure your build

#### 1.1 If you are using Maven

Declare a new test dependency in your `pom.xml`:
```xml
  <dependency>
    <groupId>org.walkmod</groupId>
    <artifactId>junit4git</artifactId>
    <version>${version}</version>
    <scope>test</scope>
  </dependency>
```
Add/Edit also the `maven-surefire-plugin` adding the `listener` property:

 ```xml
 <plugin>
    <groupId>org.apache.maven.plugins</groupId>
     <artifactId>maven-surefire-plugin</artifactId>
     <version>2.20.1</version>
     <configuration>
       <properties>
         <property>
           <name>listener</name>
           <value>org.walkmod.junit4git.junit4.Junit4GitListener</value>
         </property>
       </properties>
     </configuration>
  </plugin>
 ```

Commit these changes into your `master` branch.

```bash
git checkout master
git add pom.xml
git commit -m 'junit4git setup'
```

#### 1.2 If you are using Gradle

In this case, is preferable to use Junit5 because Gradle does not support to setup Junit listeners. 
However, there is a workaround for Junit4 and scalatest.

##### 1.2.1 If you are using Junit4 

- Modify your `build.gradle` file with the following
contents:

```
  testCompile("org.walkmod:junit4git:${version}")
```
- Moreover, you need to setup a different runner for your tests. Therefore, you need to add
 `@RunWith(Junit4GitRunner.class)` in all your tests classes.
 
- Setup the agent in your `build.gradle` file.

```
configurations {
    agent
}

dependencies {
  agent "org.walkmod:junit4git-agent:${version}"
}

test.doFirst {
    jvmArgs "-javaagent:${configurations.agent.singleFile}"
}
```

##### 1.2.2 If you are using Junit5 

Modify your `build.gradle` file with the following
contents:

```
  testCompile("org.walkmod:junit5git:${version}")
```
And then:

```bash
git checkout master
git add pom.xml
git commit -m 'junit4git setup'
```

##### 1.2.3 If you are using Scalatest 

- Modify your `build.gradle` file with the following
contents:

```
  testCompile("org.walkmod:scalatest4git_2.11:${version}")
```
- Moreover, you need to setup a different runner for your tests. Therefore, you need to add
 `@RunWith(classOf[ScalaGitRunner])` in all your tests classes.
 
- Setup the agent in your `build.gradle` file.

```
configurations {
    agent
}

dependencies {
  agent "org.walkmod:junit4git-agent:${version}"
}

test.doFirst {
    jvmArgs "-javaagent:${configurations.agent.singleFile}"
}
```

### 2. Generate a Test Impact Report

After having configured your build, run your `master` branch tests. 

```bash
mvn test -- or gradle check
.
.
[WARNING] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0
```
A Test Impact Report is generated as a Git note at (`refs/notes/tests`). You can
check the contents with:

```bash
export GIT_NOTES_REF=refs/notes/tests
git notes show
```
A report, similar to the next one, will be printed in your console.

```json
[
  {
    "test": "CalculatorTest",
    "method": "testSum",
    "classes": [
      "Calculator"
     ]
  }
  ...
]
```

This report specifies classes that are instantiated for each one 
of your test methods.

### 3. Run the Impacted Tests

After generating your test impact report, run the tests again (from the master branch or in a new local branch)

```bash
mvn tests -- or gradle check
.
.
[WARNING] Tests run: 0, Failures: 0, Errors: 0, Skipped: 4
```

Voilà! You will see that all the tests have been skipped because there is no change that may 
affect in our test results. 

However, if you add / modify 
new test files or modify an existing source file, only the affected tests are executed.

### 4. Use Travis to Upgrade the Test Impact Report

Tests can be run incrementally once the base testing report is generated. However, how can we 
make them updated every time a pull request is merged?

Our strategy consist of delegating this work to Travis with the following steps: 

- **Generate an OAuth token for your GitHub User**. Go to
 `Settings > Developer Settings > Personal access tokens`. Then `Generate new token`
 with `repo` permissions. Copy the generated token into the clipboard.
- **Add a new Travis Environment Variable**. Go to your last build, and then:
 `More Options > Settings > Environment Variables`.
 Add an environment variable named `GITHUB_TOKEN` and paste the contents of your clipboad.
- **Edit your `.travis.yml` file** adding the following contents:

```yml
before_install:
  - echo -e "machine github.com\n  login $GITHUB_TOKEN" >> ~/.netrc
after_script:
  - git push origin refs/notes/tests:refs/notes/tests
```
- **Commit your `.travis.yml` changes** to master.

## License

This project is licensed under The Apache Software License.

## Build

This is a Gradle project that you can easily build running

```bash
gradle build customFatJar
```

Or, if you want to generate a version to test it from a Maven project

```bash
gradle publishToMavenLocal
```

## FAQs

- The agent fails when Jacoco is Running.

By default, Jacoco is appending an agent before running tests. Jacoco agent needs 
to be disabled or added as first agent because it requires non-altered classes 
by other agents.

To disable jacoco:

```
test.extensions.findByType(JacocoTaskExtension.class).enabled = false
```

To setup as first agent: 
https://discuss.gradle.org/t/jacoco-gradle-adds-the-agent-last-to-jvm-args/7124/5

