Hacker News
==========

![Screenshot of the app](http://i.imgur.com/XxS04KP.png)

An open source Hacker News client for Android phones & tablets.

![Get it on Google Play!](https://developer.android.com/images/brand/en_generic_rgb_wo_60.png)

[Available on the Google Play Store](https://play.google.com/store/apps/details?id=com.airlocksoftware.hackernews)

# How to build

**You'll need 3 jar files (or a more recent version):**

- [commons-lang3-3.1.jar](http://archive.apache.org/dist/commons/lang/binaries/commons-lang3-3.1-bin.zip)
- [gson-2.2.2.jar](https://google-gson.googlecode.com/files/google-gson-2.2.2-release.zip)
- [jsoup-1.7.2.jar](http://jsoup.org/packages/jsoup-1.7.2.jar)

Create a "libs" folder at the project root and add these jars to it.

**Additionally, you'll need two of my library projects:**

- [HoloTheme](https://github.com/bishopmatthew/HoloTheme)
- [DatabaseUtils](https://github.com/bishopmatthew/DatabaseUtils)

HoloTheme is a collection of useful code I share between projects. Some of it is oriented towards using Holo-themed widgets on Android 2.1+. It requires the Android support library be put in the "libs" directory.

DatabaseUtils is a really simple ORM that uses reflection to do CRUD operations on objects that extends SqlObject. 

Clone both repositories, and then import them into Eclipse. Then add them as library projects to HackerNews. 

If you're using Android Studio:
- Put all the .jars into `./libs/`
- Clone both [HoloTheme](https://github.com/bishopmatthew/HoloTheme) and [DatabaseUtils](https://github.com/bishopmatthew/DatabaseUtils) into `./libs/` or into your choice of directory outside the project.
- Go into `File > Project Structure...` and add the two repositories you cloned earlier as Android Library modules (`Modules > New Module > Library Module` under the Android section in the `New Module` dialog).
- Add [Android Support V4](http://developer.android.com/tools/support-library/setup.html) as a Global Library, and select `HackerNews` and `HoloTheme` as projects it should be added to.
- Under the `HackerNews` module, add all of the jars in the `./libs/` folder as dependencies.
- Under the `HackerNews` module, add the `HoloTheme` and `DatabaseUtils` modules as dependencies (Using the `Module Dependency` option when adding them).

**If you recieve errors on finding symbols like `@color/grey_30`, try going into HoloTheme and running `git checkout 5c7fca98`**

# Design Overview

I've outlined the design in in [this wiki article](https://github.com/bishopmatthew/HackerNews/wiki/Design-Overview). This might also be helpful if you're interested in writing Android apps.

# How to help

If you tackle any of the issues on the tracker, that would be great! I've added a few enhancements there with a pretty detailed description of what needs to be done.

# License

The MIT License (MIT)
Copyright (c) 2013 Matthew Bishop

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.


The code will be here later today, for now you can use issues for bug reports or feature requests.
