# Tandem Tales Server #

Tandem Tales is a platform that facilities simple text-based two-player
interactive storytelling sessions between a player who controls one character in
the story and a game master who controls all of the other characters and the
environment. Tandem Tales connects human players with each other and with AI
agents to study collaborative storytelling.

The server maintains a database of story worlds and a list of known storytelling
agents. Using a simple JSON protocol, clients can connect, specify what story
world they want to play in, what role they want to play as, and who they want to
play with. The server matches players, tracks actions as they occur in the
story, and records logs of all events.

## Pre-Compiled Executable

The JAR file containing the server executable and all dependencies can be
[downloaded here](jar).

## Build from Source

The Tandem Tales server is written in Java and published as a Maven project.

Two dependencies need to be installed in Maven:
- [Google GSON](https://github.com/google/gson)
- [Serial Server Sockets](https://github.com/sgware/serialsoc)

Assuming you have [Git](https://git-scm.com/install), the
[Java Development Kit](https://www.oracle.com/java/technologies/downloads/), and
[Maven](https://maven.apache.org/) installed and on your path, you can download
the dependencies and compile Tandem Tales Server from source like this.
```
git clone https://github.com/google/gson.git
cd gson
mvn clean install
cd ..
git clone https://github.com/sgware/serialsoc.git
cd serialsoc
mvn clean install
cd ..
git clone https://github.com/sgware/tt-server.git
cd tt-server
mvn clean install
```

For testing, you may also want to install the
[Tandem Tales Test Client](https://github.com/sgware/tt-test-client).

## Usage

Tandem Tales uses secure sockets via the
[Java Secure Socket Extension (JSSE)](https://docs.oracle.com/en/java/javase/25/security/java-secure-socket-extension-jsse-reference-guide.html).
When running a public Tandom Tales server, you should obtain a certificate from
a certificate authority.

You can test the server locally by creating a self-signed certificate like this.
Replace `***` below with a password.
```
keytool -genkeypair -keystore server.keystore -storepass *** -alias test -keyalg RSA -validity 365
keytool -exportcert -keystore server.keystore -storepass *** -alias test -file test.cer
```

The above commands create a file called `server.keystore`, which stores the
server's private key, and `test.cer`, which stores the public key. The public
key is needed by any client that wishes to connect.

Assuming you are in the project root directory (`tt-server`), you can show the
Tandem Tales Server usage message like this.
```
java -jar jar/tt-server-0.9.0.jar -help
```

Start the server with logs that will be written to the default locations, with
the default database file, and using the self-signed certificate like this.
Replace `***` below with the server keystore password used above.
```
java -Djavax.net.ssl.keyStore="server.keystore" -Djavax.net.ssl.keyStorePassword="***" -jar jar/tt-server-0.9.0.jar -l -s -db
```

The server's database contains two important lists. The first is a list of story
worlds. Only story worlds in the database can be played. The second is a list of
reserved agent names. When an agent name is reserved, the server will only allow
an agent with that name to connect if it provides the correct password. This
allows the server to limit the use of certain agent names to specific trusted
agents.

Worlds and agents each have a name, a title, and a description. They can either
be listed or unlisted. When a world or agent is listed, the server will
advertise that it is available when a new agent connects to the server. Unlisted
worlds can still be played, but agents wishing to play them will need to know
the world's name and request it specifically; unlisted worlds will not appear on
the list of available worlds. Unlisted agents can still connect, but other
agents wishing to play with them will need to know the unlisted agent's name and
request it specifically; unlisted agents will not appear on the list of
available agents.

You can edit the database JSON file directly before the server starts or modify
it from the terminal while the server is running. This is how you would add the
tutorial world.
```
add world worlds/tutorial.json
set world tutorial title Tutorial
set world tutorial description A short story about buying a drink that shows you how to play as either the player or game master.
list tutorial
```

## Documentation

The JavaDoc API for all Java source files can be
[found here](http://sgware.github.io/tt-server).

## Author and License ##

Tandem Tales was created by Stephen G. Ware in 2026 while he was an Associate
Professor of Computer Science at the University of Kentucky.

This software is still in the process of being disclosed to the University of
Kentucky's Technology Commercialization team, but the author has requested that
it be released under the
[GNU General Public License 3.0](https://www.gnu.org/licenses/gpl-3.0.en.html).
When the university makes an official decision, the license for this repository
will be updated. Until then, the University of Kentucky reserves all rights.