Hacker News
==========

![Screenshot of the app](http://i.imgur.com/XxS04KP.png)

An open source Hacker News client for Android phones & tablets.

![Get it on Google Play!](https://developer.android.com/images/brand/en_generic_rgb_wo_60.png)

[Available on the Google Play Store](https://play.google.com/store/apps/details?id=com.airlocksoftware.hackernews)

# How to build

```
git clone https://github.com/bishopmatthew/HackerNews.git
cd HackerNews/
git checkout v3-redesign
cd libs/
git clone https://github.com/bishopmatthew/HoloTheme.git
cd ..
android update project -p .
```

Then from Android Studio go:

File -> Import Project -> Select the HackerNews directory. It'll bring up the gradle import screen and you should be able to keep the default options.

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
