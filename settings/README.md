Eclipse setup instructions
===================

These instructions are intended for contributors to the PWT source
code repository that want to run the Eclipse IDE.
It describes how to configure Eclipse for the correct coding styles and how to setup a PWT project.


# Configure Eclipse Environment #

All relative paths are relative to the PWT source repository's
'putnami-pwt' folder.

## Encoding ##

Window->Preferences->General->Workspace->Text file encoding
Use "UTF-8"

## Spelling ##

Window->Preferences->General->Editors->Text Editors->Spelling
Enable spell checking
Use "settings/eclipse/english.dictionary".

## Code style/formatting ##

Window->Preferences->Java->Code Style->Formatter->Import...
  settings/eclipse/pwt-format.xml
Window->Preferences->Java->Code Style->Clean up->Import...
  settings/eclipse/pwt-cleanup.xml

## Import organization ##

Window->Preferences->Java->Code Style->Organize Imports->Import...
  settings/eclipse/pwt.importorder

## Member sort order ##

Window->Preferences->Java->Appearance->Members Sort Order

First, members should be sorted by category.
1) Types
2) Static Fields
3) Static Initializers
4) Static Methods
5) Fields
6) Initializers
7) Constructors
8) Methods

Second, members in the same category should be sorted by visibility.
1) Public
2) Protected
3) Default
4) Private

Third, within a category/visibility combination, members should be sorted
alphabetically.

## Compiler errors & warnings ##
Window->Preferences->Java->Compiler->Errors/Warnings


### Code Style ###
```
WARNING - Non-staticaccess to static member
ERROR - Parameter assignment
ERROR - Method with a constructor name
```

### Potential programming problems ###
```
ERROR - Comparing identical values
ERROR - Assignment has no effect
ERROR - Possible accidental boolean assignment
ERROR - Using a char array in string concatentation
ERROR - Inexact type match for vararg arguments
ERROR - Empty statement
WARNING - Unused object allocation
ERROR - Incomplete 'switch' cases on enum
ERROR - Hidden catch block
ERROR - 'finally' does not complete normally
ERROR - Dead code
WARNING - Resource leak
IGNORE - Serializable class without serialVersionUID
ERROR - Missing synchronized modifier on inherited method
ERROR - Class overrides 'equals()' but not 'hashCode()'
```

### Name shadowing and conflicts ###
all except "Local variable" hiding

### Deprecated and restricted API: ###
all expect "Deprecated API"

### Unnecessary code ###
```
ERROR - Value of local variable is not used
WARNING - Value of parameter is not used (checked Ignore in overriding and ...)
WARNING - Unused type parameter (checked Ignore unsuded parameters documented with '@param' tag)
ERROR - Unused import
ERROR - Unused private member
ERROR - Unnecessary 'else' statement
ERROR - Unnecessary cast or 'instanceof'
ERROR - Unnecessary declaration of thrown exception (checked all sub items)
ERROR - Unused 'break' or 'continue' label
ERROR - Redundant super interface
```

### Generic types ###
```
WARNING - Unchecked generic type operation
WARNING - Usage of a raw type
WARNING - Generic type parameter declared with a final type bound
IGNORE - Redundant type arguments
```

### Annotations ###
```
ERROR - Missing '@Override' annotation (Checked Include implementations of...)
WARNING - Missing '@Deprecated' annotation
ERROR - Annotation is used as super interface
ERROR - Unhandled token in '@SuppressWarnings' (checked Enable '@SuppressWarnings' annontations)
ERROR - Unused '@SuppressWarnings' token (unchecked Suppress optional error with '@SuppressWarnings')
```

### Null analysis ###
```
ERROR - Null pointer access
IGNORE - Potential null pointer access
IGNORE - Redundant null check
```

# Checkstyle #

Checkstyle is used to enforce good programming style. Its use in
Eclipse is optional, since it is also run as part of the acceptance
test suite.

1. Install the Checkstyle plugin version 5.7.

The Eclipse Checkstyle plugin can be found at:
  http://eclipse-cs.sourceforge.net/

2. Import PWT Checks:

Window->Preferences->Checkstyle->New...
Set the Type to "External Configuration File"
Set the Name to "PWT Checks" (important)
Set the location to "settings/checkstyle/pwt-checkstyle.xml".
Suggested: Check "Protect Checkstyle configuration file".
Click "Ok".

# Findbugs #

We also use Findbugs to enforce good programming style. Its use in
Eclipse is optional, since it is also run as part of the acceptance
test suite.

1. Install the FindBugs plugin

The Eclipse Findbugs plugin can be found at:
  http://findbugs.sourceforge.net/manual/eclipse.html

2. Add PWT exclude filter file:

Window->Preferences->Java->FindBugs->Filter files
Click on the "Add..." button to add an exclude filter file.
Select the following file  "settings/findbugs/excludeFilter.xml".
Click "Ok".
