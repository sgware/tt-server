# Tandem Tales Server

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
java -jar jar/tt-server.jar -help
```

Start the server with logs that will be written to the default locations, with
the default database file, and using the self-signed certificate like this.
Replace `***` below with the server keystore password used above.
```
java -Djavax.net.ssl.keyStore="server.keystore" -Djavax.net.ssl.keyStorePassword="***" -jar jar/tt-server.jar -l -s -db
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

## Security and Privacy

The following policies are in place to ensure the security of the server and the
privacy of those who use it:
- Clients rarely send arbitrary text to the server. The exceptions are names,
  passwords, and comments on reports. This text is never shown to other clients.
  So while clients are free to choose offensive names for themselves, these
  names are never shown to other clients (unless that name has been explicitly
  added to the list of publicly approved names by the server administrator).
  Comments in reports are never shown to other clients. Thus, clients have no
  way to send arbitrary or potentially offensive text to one another.
- No identifying information about clients is collected, except for their IP
  addresses. IP addresses are recorded in the system log, but not in session
  logs. This means session logs are fully anonymized and can only be used to
  identify individual users if those users intentionally include their
  identifying information in their client name or report comments, which they
  are encouraged not to do.
- Clients cannot take arbitrary actions in a session. When offered a choice of
  actions, clients simply respond with the index of their choice, meaning they
  cannot take turns out of sequence and cannot take actions which are not
  allowed in the current state.
- The server defines a maximum amount of time that a client can wait before
  taking a valid turn during a session. If a client exceeds this amount of time,
  they will be automatically disconnected. This prevents malicious clients from
  starting a session and then never taking turns or taking turns very slowly.
- The validity of client messages and each part of those messages (including
  whether important fields are null) is always checked by the server before they
  are processed. This prevents the server from crashing if malicious clients
  intentionally send malformed messages.
- The messages that clients send to the server are typically short and are
  terminated by a new line character. The server defines a limit on the length
  of lines. If a client sends more than the allowed number of characters without
  sending a new line character, that client is automatically disconnected. This
  prevents malicious clients from sending messages so long they would cause the
  server to run out of memory.
- The Java I/O utilities used in this server are memory safe, preventing
  malicious clients from using buffer overflow attacks.

## License

Tandem Tales was developed by Stephen G. Ware PhD, Associate Professor of
Computer Science at the University of Kentucky. Development was sponsored in
part by a grant from the US National Science Foundation, #2145153.

Tandem Tales is released under the GNU General Public License version 3.0 (GPL
3). This means you are free to share and modify this software, even for
commercial purposes, as long as you give credit to the original creators and you
also release your modifications under the GPL 3 license. See the license file
for details. The University of Kentucky retains all right not specifically
granted.

To license Tandem Tales for a project not compatible with the terms of the GPL
license, contact the University of Kentucky Office of Technology
Commercialization at <otcinfo@uky.edu>.

Special thanks to Molly Siler for her help with testing and development.