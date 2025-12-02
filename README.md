# DAIGO

Tired of having some bloated games with intense graphics that slow down your PC?
Want to experience playing the board game Go with your friends in simple textual
visualisation? Then heiGO is all you need! Its purpose is to launch a
server that can host multiple go games, and also some clients, so people can
interact with the server, and actually play the game with others, isn't that cool?

You can check the Go rules [here](https://www.britgo.org/intro/intro2.html) if you don't know them.


## Installing the application

There are two ways possible :

### From source

**IMPORTANT** : This project has been developed with the Java Temurin 21 JDK. It is then strongly advised to use the same JDK as us.

First clone the project :
```sh
$ git clone git@github.com:yanniSkawronski/dai-go.git
```

Then move into the main folder and compile the package.

```sh
$ cd dai-go
$ ./mvnw dependency:go-offline clean compile package
```

Our program is now ready to go in `target/`.

### With the Docker image

Just pull the Docker image :

```sh
$ docker pull ghcr.io/yanniskawronski/dai-go:latest
```

## Usage

### Launching the server

If you installed from source
```sh
$ java -jar target/dai-go*.jar server [-p=<port>]
```

If you pulled the Docker image :
```sh
$ docker run --rm ghcr.io/yanniskawronski/dai-go server -H=<host> [-p=<port>]
```

`<port>` is the port the server will listen to. By default, it is the port 1919.

### Launching the client

If you installed from source :
```sh
$ java -jar dai-go-1.0-SNAPSHOT.jar client -H=<host> [-p=<port>]
```

If you pulled the Docker image :
```sh
$ docker run --rm ghcr.io/yanniskawronski/dai-go client -H=<host> [-p=<port>]
```

`<host>` is the server's adress. If the server is on the same machine, write `-H=localhost`

`<port>` is the server's port. By default, it is the port 1919.

## Contributing

Contributions via issues and pull requests are welcome. You don't need to build and push yourself a new Docker image (actually you can't). Once your pull request has been accepted, it will automatically build and publish one, via Github Actions.

If you want details about the application protocol, see [](DAIGO.md).

## Authors

- [Yanni Skawronski](https://github.com/yanniSkawronski)
- [Tadeusz Kondracki](https://github.com/GlysVenture)
- [Jules Rossier](https://github.com/julesrossier)
