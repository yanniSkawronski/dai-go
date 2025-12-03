# DAIGO

The DAIGO (heiGO) project is both a CLI client and a server made to
play a game of Go over the internet.
They use the [DAIGO](#daigo-application-protocol)
protocol.

I you wanna enjoy quick game of Go with your friends on the CLI,
please [install](#installing-the-application) the app
and enjoy!

You can check the Go rules [here](https://www.britgo.org/intro/intro2.html) if you don't know them.


## Table of contents

<!-- TOC -->
* [DAIGO](#daigo)
  * [Table of contents](#table-of-contents)

  * [Installing the application](#installing-the-application)
    * [From source](#from-source)
    * [With the Docker image](#with-the-docker-image)
  * [Usage](#usage)
    * [Launching the server](#launching-the-server)
    * [Launching the client](#launching-the-client)
  * [Contributing](#contributing)
    * [Build Docker image and push it to Github Container Registry](#build-docker-image-and-push-it-to-github-container-registry)
  * [Sources](#sources)
  * [Authors](#authors)
<!-- TOC -->

## DAIGO application protocol

The daigo appplication protocol defines a simple way to play a game of Go over TCP.

For more details, please check out the specification: [DAIGO Applicaiton protocol](./DAIGO.md)

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
$ docker run --rm -p 1919:<port> ghcr.io/yanniskawronski/dai-go server [-p=<port>]
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
