# DAIGO Application Protocol

The DAIGO protocol allows
to play a game of GO between two users.

## Transport

The DAIGO protocol must use tcp and usually uses port 1919.

The messages are treated as text,
are encoded in UTF-8 and must be delimited
by `\n` aka newline character.

The initial connection is established by the client.

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
  - 1 - Name already taken

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
  - 1 - The client is already in a game

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

### Join an existing game

The client requests to join the game with a certain player.

**Request**
```
JOIN <name>
```
- `name` : name of the player to join in game

**Response**

- `OK` : game joined succesfully
- `ERROR <code>` : an error occurred,
  the code is an integer among the following list:
    - 1 - The client is already in a game
    - 2 - The requested game does not exist

### A player joins a game

The server tells the client that a player joined his game.

**Request**
```
JOINED <name>
```
- `name` : name of the player that just joined

**Response**

None.

### Get your turn

The server tells the client that it is his turn to play.

**Request**

```
PLAY
```

**Response**

None.

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
    - 1 - The client is not in a game
    - 2 - It is not the clients turn
    - 3 - The move is invalid

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
    - 1 - The client is not in a game
    - 2 - It is not the clients turn

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
    - 1 - The client is not in a game

### Receive stone played

The server informs the client where his opponent played a stone.

**Request**

```
PLAYER STONE <x> <y>
```
- `x` : horizontal position on the board, starting from the left
- `y` : vertical position on the board, starting from the top

**Response**

None.

### Receive opponent passed

The server informs the client that his opponent passed his turn.

**Request**

```
PLAYER PASS
```

**Response**

None.

### Receive opponent forfeit

The server informs the client that his opponent forfeited the game.
This means the game has been terminated and the client won.

**Request**

```
PLAYER FORFEIT
```

**Response**

None.

### Receive opponent disconnect

The server informs the client that his opponent disconnected.
This means the game has been terminated and the client won.

**Request**

```
PLAYER DISCONNECT
```

**Response**

None.

### Game ends

The server informs the client of the result of the game.
This means the game has been terminated.

**Request**

```
GAME <result>
```
- `result` : `1` for win and `0` for a loss.

**Response**

None.

### Exit the server

The client informs the server that he quits.
The server should close the connection.

**Request**

```
EXIT
```

**Response**

None.
