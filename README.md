# Rivelaine

A library for scrapping and parsing a given Web page. Rivelaine divides a Web page into a set of Web Fragments. 
See the [related paper](https://hal.archives-ouvertes.fr/hal-01895955) for a description of the Web fragmentation process.
Rivelaine is inspired by Firefox's [Readability](https://github.com/mozilla/readability).
Rivelaine can be linked to a Web archives mining library such as [Archive-miner](https://github.com/lobbeque/archive-miner).

There are two available implementations of Rivelaine : **1)** `./scala/` is a deprecated implementation of Rivelaine in Scala, **2)** `./nodejs/` is the last implementation of Rivelaine as a Node.js Web server, **3)** `./addon/` is an addon for firefox to live test the results of Rivelaine on a given Web page in Firefox

## Current dependencies 

### Scala implementation dependencies

   1. Scala 2.11.8
   2. Jsoup 1.8.3

### Node.js implementation dependencies

   1. Node.js
   2. After-load 1.0.4
   3. Express 4.15.3
   4. Fathom-web 2.1.0
   5. Is-url 1.2.2
   6. Jsdom 7.2.2
   7. Underscore 1.8.3
   8. Yargs 8.0.2

### Firefox addon dependencies

   1. Firefox >= v38.0.0 & <= v57.0.0
   2. Domjson 0.1.2
   3. Fathom-web 2.1.0
   4. Fs 0.0.1-security
   5. Htmlparser2 3.9.2
   6. Jpm 1.3.1
   7. Jsdom 7.2.2
   8. Underscore 1.8.3
   9. Url 0.11.0
   10. Util 0.10.3
   11. Xhr 2.4.0
   12. Xmldom 0.1.27
   13. Xmlhttprequest 1.8.0

**Important:** Firefox addons are now deprecated, you need to download and install an old version of Firefox to use it.

## Usage 

Download or clone the source file:

```
git clone https://github.com/lobbeque/rivelaine.git
```

### Scala implementation

Build the source code using [sbt](https://www.scala-sbt.org/) and see `~/rivelaine/scala/build.sbt` for some specific configurations: 

```
cd ~/rivelaine/scala/
sbt assembly
```

Run the library on a given URL and return Web fragments (mode content) :

```
sbt
run --mode "content" --path "http://www..." 
```

Run the library on a given html file and return Web fragments (mode content) :

```
sbt
run --mode "content" --path "./path/to/a/local/file.html"
```

Run the library on a given URL an return all the href links (mode link) :

```
sbt
run --mode "link" --path "http://www..." 
```

To connect Rivelaine to an other jvm process use the [function](https://github.com/lobbeque/rivelaine/blob/master/scala/src/main/scala/qlobbe/Rivelaine.scala#L823) `getContentJava`.

### Node.js implementation

Run Rivelaine server as a script to test and debug such as :

```
node rivelaine.js --mode=script --source="http://www..." --type=url
```

Type of the Web page to be parsed can be an `url`, a `file`, or a `dom` object.

Run Rivelaine as a distributed node server listening to rest and socket requests such as :

```
node rivelaine.js --mode=server --type=url
```

Then request Rivelaine as a Web server by using the following Url :

```
http://localhost:2200/getFragment?type=url&source=http%3A%2F%2Ffoo.net%2Fbar.html
```

**Important:** the URL source has to be URl [encoded](https://meyerweb.com/eric/tools/dencoder/).

### Firefox Addon

Run Rivelaine as a Node.js Web server (see just above). 

Open the appropriate version of Firefox.

[Install](https://www.accessfirefox.org/Install_Addon_Manually.php) the Rivelaine Addon from the files.

Load a given Web page and click on the top right Firefox's icon to fragment the Web page.

## Licence

This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>.