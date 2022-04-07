# com.typelead.eta, com.typelead.eta.android, com.typelead.eta.base 0.8.1
- (#43) Fix Gradle support for Gradle 5.0+

# com.typelead.eta, com.typelead.eta.android, com.typelead.eta.base 0.8.0
- (#33) Fix git dependencies handling
- Fix a race condition that was causing sporadic ConcurrentModificationExceptions

# com.typelead.eta, com.typelead.eta.android, com.typelead.eta.base 0.7.9
- (#28) Add updateEtlas task
- Fix the periodic ConcurrentModificationException
- Added blockedIndefinitelyOnSTM to keep in progurad rules

# com.typelead.eta, com.typelead.eta.android, com.typelead.eta.base 0.7.8
- (#27) Support Gradle 4.8+ by avoiding use of internal APIs.

# com.typelead.eta, com.typelead.eta.android, com.typelead.eta.base 0.7.7
- Fix problem with bytestring-builder package (also doesn't generate code).

# com.typelead.eta, com.typelead.eta.android, com.typelead.eta.base 0.7.6
- Fix problem with nats package (and packages that don't generate code).

# com.typelead.eta, com.typelead.eta.android, com.typelead.eta.base 0.7.5
- (#20) Fix problem with module hear Main

# com.typelead.eta, com.typelead.eta.android, com.typelead.eta.base 0.7.4
- (#23) Avoid reporting errors twice
- -P properties will now override build.gradle
- Added args property for EtaRepl task

# com.typelead.eta, com.typelead.eta.android, com.typelead.eta.base 0.7.3
- (#24) Allow any version to be set and translate to a valid Etlas version

# com.typelead.eta, com.typelead.eta.android, com.typelead.eta.base 0.7.2
- Fix bug with Etlas Maven layout.

# com.typelead.eta, com.typelead.eta.android, com.typelead.eta.base 0.7.1
- Improve warning for REPL tasks to suggest using --console plain.

# com.typelead.eta, com.typelead.eta.android, com.typelead.eta.base 0.7.0
- Added 'repl' task for a global REPL.
- Added 'repl[SourceSet]Eta' tasks for sourceSet-based REPL.
- Minor bug fix which makes the Gradle Eta package cache versioned.

# com.typelead.eta, com.typelead.eta.android, com.typelead.eta.base 0.6.6
- When preInstalledDependencies = true, Eta dependencies are injected into non-Eta projects that depend on Eta projects.
- Fix a bug with injecting dependencies failing to occur with a root non-Eta project.

# com.typelead.eta, com.typelead.eta.android, com.typelead.eta.base 0.6.5
- Fix how Main module is handled

# com.typelead.eta, com.typelead.eta.android, com.typelead.eta.base 0.6.4
- Fix regression in which projects with Main.hs and additional modules were failing.

# com.typelead.eta, com.typelead.eta.android, com.typelead.eta.base 0.6.3
- Fix bug in the Eta plugin that caused jar files to duplicate every file twice.

# com.typelead.eta, com.typelead.eta.android, com.typelead.eta.base 0.6.2
- Allow dependency constraints like eta('[package-name]') to mean that any version is OK.
- Inject dependencies task now has proper synchronization to avoid adding dependencies to a resolved configuration.
- Generate library components for the underlying .cabal file, even for sourcesets with a Main.hs file.

# com.typelead.eta, com.typelead.eta.android, com.typelead.eta.base 0.6.1
- Fixed bug with maven dependency parsing that caused build failure when including eta-kafka-client as a dependency.

# com.typelead.eta, com.typelead.eta.android, com.typelead.eta.base 0.6.0
- Resolve relative paths in includeDirs in EtaOptions against project directory.
- Add option 'preInstallDependencies' to EtaExtension which will install dependencies after all projects are evaluated and before the task graph is resolved.

# com.typelead.eta, com.typelead.eta.android, com.typelead.eta.base 0.5.9
- Correct naming scheme for main sourceset to avoid breakages

# com.typelead.eta, com.typelead.eta.android, com.typelead.eta.base 0.5.8
- Throws exception if Gradle version is less than 4.3
- Properly feed dependencies for compileTestEta

# com.typelead.eta, com.typelead.eta.android, com.typelead.eta.base 0.5.7
- Fix bug with includeDirs property in which the user has to know the plugin internals
- Add `useEtaTest()` method that uses simple Eta-style testing

# com.typelead.eta, com.typelead.eta.android, com.typelead.eta.base 0.5.6
- Support configuration DSL for the sourceSet extension.

# com.typelead.eta, com.typelead.eta.android, com.typelead.eta.base 0.5.5

- Added new properties to EtaOptions:
	- List<String> installIncludes
	  Include files to install with the package installation
	- List<String> includeDirs
	  Directories for which to search include files
- Fixed sourceSet extension behavior to use conventions instead.

# com.typelead.eta, com.typelead.eta.android, com.typelead.eta.base 0.5.4

- Use friendly version when printing out error messages for binary installation.

# com.typelead.eta, com.typelead.eta.android, com.typelead.eta.base 0.5.3

- Added EtaOptions configuration for EtaCompile task:
  - String language
    - Either Haskell98 or Haskell2010
  - NamedDomainObjectContainer<String> extensions
    - Language extensions you want to enable
  - List<String> args
    - Arguments that you want to pass to the Eta compiler directly
  - List<String> cpp
    - Preprocessor arguments to send to the Eta compiler
- Added getDefaultEtaProguardFile() function to EtaPluginConvention that provides an out-of-the-box config file to use.
