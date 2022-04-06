# About MZmine 2
[![Build Status](https://travis-ci.org/mzmine/mzmine2.svg?branch=master)](https://travis-ci.org/mzmine/mzmine2)

MZmine 2 is an open-source software for mass-spectrometry data processing. The goals of the project is to provide a user-friendly, flexible and easily extendable software with a complete set of modules covering the entire MS data analysis workflow.

More information about the software can be found on the [MZmine](http://mzmine.github.io) website.


## License
MZmine 2 is a free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either [version 2](http://www.gnu.org/licenses/gpl-2.0.html) of the License, or (at your option) any [later version](http://www.gnu.org/licenses/gpl.html).

MZmine 2 is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.


## Development

### Tutorial

Please read our brief [tutorial](http://mzmine.github.io/development.html) on how to contribute new code to MZmine.

### Java version

MZmine development requires Java Development Kit (JDK) version 12 or newer (http://jdk.java.net).

### Building

To build the MZmine package from the sources, run the following command:

    ./gradlew

or

    gradlew.bat

The final MZmine distribution will be placed in build/MZmine-version-platform.zip

If you encounter any problems, please contact the developers:
https://github.com/mzmine/mzmine2/issues

### Code style

Since this is a collaborative project, please adhere to the following code formatting conventions:
* We use the Google Java Style Guide (https://github.com/google/styleguide)
* Please write JavaDoc comments as full sentences, starting with a capital letter and ending with a period. Brevity is preferred (e.g., "Calculates standard deviation" is preferred over "This method calculates and returns a standard deviation of given set of numbers").

