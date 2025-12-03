# DAIGO Application Protocol

The DAIGO protocol allows
to play a game of GO between two users.

## Transport

The DAIGO protocol must use tcp and usually uses port 1919.

The messages are treated as text,
are encoded in UTF-8 and must be delimited
by `\n` aka newline character.

The initial connection is established by the client.

All messages are initiated by the client.

After that the client can join the server with a username.
The server must verify that the username is not
already taken by another user,
otherwise he denies access to the user.

The client can then create a new game or join an existing game of go,
specifying the user to join.

A client can also request a list of open games, and players in them.
The server responds with all games that are waiting for enough players.

The server must verify that the game has space for a player and,
if yes, allows the player to join.

When 2 clients have joined a game, the server decides who goes first,
and the clients can start playing on their turn.

A client has to ask the server if it is his turn to play.
The server informs him of the status of the game.

A client can play a stone, pass or forfeit. If the play is valid,
the server lets the second player play, otherwise it responds with an error message.

Coordinates for stones are x - horizontal and y - vertical, starting from the
top left corner.

On an unknown message, the server must send an error message to the client.

When a client disconnects, the server removes him from the list of clients.

## Messages

### Join the server
The client sends a hello message to the server indicating the client's username

**Request**

```
HELO <name>
```
- `name`: the name of the client

**Response**
- `OK`: the client
- `ERROR <code>`: an error occurred,
  the code is an integer among the following list:
    - 7 - The client has already identified themselves
    - 8 - Name already taken

### Create a new game 

The client creates a new game

**Request**
```
CREATE
```

**Response**

- `OK`: The game has been created and is open to be joined by other players
- `ERROR <code>` : an error occurred,
  the code is an integer among the following list:
    - 1 - The client has not yet identified himself.
    - 3 - The client is already in a game

### List open games

The client requests a list of open games.

**Request**
```
LIST
```

**Response**

- `GAMES <list>`
    - `list` a list of space separated names of players
        which are waiting in a game that is not yet full
- `ERROR <code>` : an error occurred,
  the code is an integer among the following list:
    - 1 - The client has not yet identified himself.

### Join an existing game

The client requests to join the game with a certain player.

**Request**
```
JOIN <name>
```
- `name` : name of the player to join in game

**Response**

- `OK` : game joined successfully
- `ERROR <code>` : an error occurred,
  the code is an integer among the following list:
    - 1 - The client has not yet identified himself.
    - 3 - The client is already in a game
    - 6 - The requested game does not exist

### A player checks if he can play

The client asks what is the status of the game.
The server answers asking the client to wait for his turn or informs him what happened
just before.

**Request**

```
PLAY
```

**Response**

- `WAIT` - Waiting on another player to join
- `START <name>` - another player joined, it is the clients turn to start as black.
  - `name` - name of the other player
- `WAIT <name>` - waiting for the other player
  - `name` - name of the other player
- `STONE <x> <y>`- the other player played a stone.
  - `x` : horizontal position on the board, starting from the left
  - `y` : vertical position on the board, starting from the top
- `PASS` - the other player passed.
- `FORFEIT` - the other player forfeited the game, the client wins.
- `DISCONNECT` - the other player disconnected, the client wins.
- `RESULT <1|0|-1>` - the game is done: 1 for win, 0 for draw, -1 for loss.
- `ERROR <code>` : an error occurred,
  the code is an integer among the following list:
    - 1 - The client has not yet identified himself.
    - 2 - The client is not in a game.

### Play a stone

The client informs the server where he plays a stone.

**Request**

```
STONE <x> <y>
```
- `x` : horizontal position on the board, starting from the left
- `y` : vertical position on the board, starting from the top

**Response**

- `OK` : The stone has been placed on the board successfully.
- `ERROR <code>` : an error occurred,
  the code is an integer among the following list:
    - 1 - The client has not yet identified himself.
    - 2 - The client is not in a game
    - 4 - It is not the clients turn
    - 5 - The move is invalid
    - 9 - The game has not yet started
    - 10 - The game

### Pass your turn

The client tells the server he passes his turn.

**Request**

```
PASS
```

**Response**

- `OK` : The client passed the turn successfully.
- `ERROR <code>` : an error occurred,
  the code is an integer among the following list:
    - 1 - The client has not yet identified himself.
    - 2 - The client is not in a game
    - 4 - It is not the clients turn
    - 5 - The move is invalid
    - 9 - The game has not yet started
    - 10 - The game

### Forfeit the game

The client tells the server he forfeits the current game.

**Request**

```
FORFEIT
```

**Response**

- `OK` : forfeit was successfull, game has been terminated with a loss for the client.
- `ERROR <code>` : an error occurred,
  the code is an integer among the following list:
    - 1 - The client has not yet identified himself.
    - 2 - The client is not in a game
    - 4 - It is not the clients turn
    - 5 - The move is invalid
    - 9 - The game has not yet started
    - 10 - The game 

### Exit the server
To exit the server, the client simply disconnects.
The server then closes the client socket and updates the game, if any he was in.

### Unknown Message

On an unknown message, the server answers with an error.

**Response**

- `ERROR <code>` : an error occurred,
  the code is an integer among the following list:
    - -1 - Unknown message received.
