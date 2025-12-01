# heiGO

## What does it do?

heiGO is a Java program to play the ancient boardgame of Go. It ships with a client with a user-friendly CLI
interface that allows a user to connect, choose a username, and play some epic Go games with other
connected users. The program takes into account illegal moves, game end through two consecutive passes,
and automatic counting of the score. It also ships with a server able to handle multiple clients
through multithreading.

The rules of the game can be found [here](https://www.britgo.org/intro/intro2.html).


## Installation

The program can be compiled from source. First, clone the repo.

```
$ git clone git@github.com:yanniSkawronski/dai-go.git
```

Then move into the main folder and compile the package.
This requires Java Temurin 21 (jdk21).

```
$ cd dai-go
$ ./mvnw dependency:go-offline clean compile package
```

Our program is now ready to go in `target/`.

```
$ cd target
$ java -jar dai-go-1.0-SNAPSHOT.jar -V
```

This should display the version.

## Usage

Both the server and the client require Java.

### Server

To lauch the server, write in a terminal:

```
$ java -jar dai-go-1.0-SNAPSHOT.jar server [-hV] [-p=<port>]
```

`<port>` is the port the server will listen to. By default, it is the port 1919.

Are also available as options:

```
  -h, --help                Show help message and exit.
  -V, --version             Print version information and exit.

```

When the server starts, it should display

```
heiGO server starting...
```

It will proceed to display relevant infos as it's running, such as the
users connected, when a game is launched, when a game is finished, etc.

The server can be stopped with `Ctrl+C`

### Client

To launch the client, write in a terminal

```
$ java -jar dai-go-1.0-SNAPSHOT.jar client [-hV] -H=<host> [-p=<port>]
```

`<host>` is the server's adress. If the server is on the same machine, write `-H=localhost`

`<port>` is the server's port. By default, it is the port 1919.

Are also available as options:

```
  -h, --help                Show help message and exit.
  -V, --version             Print version information and exit.

```

When the client starts, it should display

```
heiGo Client starting...
```

If the connection is successful, it will prompt the user for a username, then display
the list of available games, and this menu:

```
1 - Refresh list
2 - Create a game
3 - Join a game
4 - Quit
```

 - `1` to refresh the list of available games, in case somebody connected in the meantime
 - `2` to create a game, wich will make the username appear in the list of available games
 - `3` to join a game. The user will then be prompted for the name of the opponent
 - `4` to quit the client
 
During a game, the program will display the board like this:

```
    1   2   3   4   5   6   7   8   9
 1    —   —   —   —   —   —   —   —  
    |   |   |   |   |   |   |   |   |
 2    —   —   —   —   —   —   —   —  
    |   |   |   |   |   |   |   |   |
 3    —   —   —   — W —   —   —   —  
    |   |   |   |   |   |   |   |   |
 4    —   —   —   —   —   —   —   —  
    |   |   |   |   |   |   |   |   |
 5    —   —   —   — b —   —   —   —  
    |   |   |   |   |   |   |   |   |
 6    —   —   —   —   —   —   —   —  
    |   |   |   |   |   |   |   |   |
 7    —   —   —   —   —   —   —   —  
    |   |   |   |   |   |   |   |   |
 8    —   —   —   —   —   —   —   —  
    |   |   |   |   |   |   |   |   |
 9    —   —   —   —   —   —   —   —  

Black to play

X,Y - Play a stone at column X, line Y
pass - Pass
forfeit - Forfeit the game
```

`w` stands for a white stone, `b` for a black stone. If the letter is in uppercase,
It means it was the last move played so far.

After two consective passes, the game will end and the program will calculate the result.

```
    1   2   3   4   5   6   7   8   9
 1    —   —   —   — w — b —   —   —  
    |   |   |   |   |   |   |   |   |
 2    —   —   —   — w — b —   —   —  
    |   |   |   |   |   |   |   |   |
 3    —   — w —   — w — b —   —   —  
    |   |   |   |   |   |   |   |   |
 4    —   —   — w — b — b —   —   —  
    |   |   |   |   |   |   |   |   |
 5    —   —   — w — b —   —   —   —  
    |   |   |   |   |   |   |   |   |
 6    —   —   — w — b —   —   —   —  
    |   |   |   |   |   |   |   |   |
 7    —   —   — w — b —   —   —   —  
    |   |   |   |   |   |   |   |   |
 8    —   —   — w — b —   —   —   —  
    |   |   |   |   |   |   |   |   |
 9    —   —   — w — b —   —   —   —  
White passed
Game over!
White won by 4 points

You lost :(
```

If a player disconnects from a game, their opponent is automatically
declared the winner.

## Authors

To contact us please open an Issue.
Improvements and bugixes are welcome.

- [Yanni Skawronski](https://github.com/yanniSkawronski)
- [Tadeusz Kondracki](https://github.com/GlysVenture)
- [Jules Rossier](https://github.com/julesrossier)
