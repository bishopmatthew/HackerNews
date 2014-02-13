Hacker News
==========

[![Come chat with us!](https://badges.gitter.im/bishopmatthew/HackerNews.png)](https://gitter.im/bishopmatthew/HackerNews)

![Screenshot of the app](http://i.imgur.com/XxS04KP.png)

An open source Hacker News client for Android phones & tablets.

![Get it on Google Play!](https://developer.android.com/images/brand/en_generic_rgb_wo_60.png)

[Available on the Google Play Store](https://play.google.com/store/apps/details?id=com.airlocksoftware.hackernews)

# How to build

The basic outline is this: there are 6 jars in the libs folder that the project depends on. Additionally you need one of my library projects, HoloTheme. It's basically a collection of utility classes & method's I've built up over time. HoloTheme also has a dependency on android-support-v4.jar (as does HackerNews).

To get what you need, run these commands from a shell:
```
git clone https://github.com/bishopmatthew/HackerNews.git
cd HackerNews/libs
git clone https://github.com/bishopmatthew/HoloTheme.git
```
##### IntelliJ
In IntelliJ, add HoloTheme as a module and create a module dependency from HackerNews on HoloTheme. Then create a library out of the 6 jars, and add it as a compile dependency to HackerNews and a provided dependency to HoloTheme.

##### Eclipse
I haven't used Eclipse in a while, but you should be able to add HoloTheme as a library project. Then add the 6 jars to the build path of HackerNews if it doesn't happen automatically. You may also have to copy android-support-v4 from HackerNews/libs into HackerNews/libs/HoloTheme/libs.

##### Android Studio
If you're using Android Studio:
- Go into `File > Project Structure...` and add the HoloTheme repository you cloned earlier as Android Library modules (`Modules > New Module > Library Module` under the Android section in the `New Module` dialog).
- Under the `HoloTheme` module, add all the jars in the `./libs/` folder as "provided" dependencies.
- Under the `HackerNews` module, add all of the jars in the `./libs/` folder as "compile" dependencies.
- Under the `HackerNews` module, add the `HoloTheme`module as dependencies (Using the `Module Dependency` option when adding them).

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
