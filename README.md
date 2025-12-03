# DAIGO

Tired of having some bloated games with intense graphics that slow down your PC?
Want to experience playing the board game Go with your friends in simple textual
visualisation? Then heiGO is all you need! Its purpose is to launch a
server that can host multiple go games, and also some clients, so people can
interact with the server, and actually play the game with others, isn't that cool?

You can check the Go rules [here](https://www.britgo.org/intro/intro2.html) if you don't know them.


## Table of contents

1. [Installing the application](#installing-the-application)
   - [From source](#from-source)
   - [With the Docker image](#with-the-docker-image)
2. [Usage](#usage)
    - [Launching the server](#launching-the-server)
    - [Launching the client](#launching-the-client)
3. [Contributing](#contributing)
4. [Sources](#sources)
5. [Authors](#authors)


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
$ docker run --rm -p 1919:1919 ghcr.io/yanniskawronski/dai-go server [-p=<port>]
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

Contributions via issues and pull requests are welcome.

If you want details about the application protocol, see [](DAIGO.md).

### Build Docker image and push it to Github Container Registry

To build the Docker image, when in project folder :

```sh
$ docker build -t dai-go .
```

To push the Image (you need to be logged in to Github Container Registry) :

```sh
$ docker tag dai-go ghcr.io/yanniskawronski/dai-go:<tag>
$ docker push ghcr.io/yanniskawronski/dai-go:<tag>
```

`<tag>` is the tag you want to put to your image.

## Sources

We used the TCP programming practical work template ([link here](https://github.com/heig-vd-dai-course/heig-vd-dai-course-java-tcp-programming-practical-content-template)) as base project.

## Authors

- [Yanni Skawronski](https://github.com/yanniSkawronski)
- [Tadeusz Kondracki](https://github.com/GlysVenture)
- [Jules Rossier](https://github.com/julesrossier)
