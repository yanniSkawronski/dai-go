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

The client can then create a new game or join an existing game of go.

The server must verify that the game has space for a player and,
if yes, allows the player to join.

When 2 clients have joined a game, the server decides who goes first,
and the clients can start playing on their turn.

A client can play a stone, pass or forfeit. If the play is valid,
the server lets the second player play, otherwise it responds with an error message.

A client can also request a list of open games, and players in them.
The server responds with all games that are waiting for enough players.

On an unknown message, the server must send an error message to the client.

When a client disconnects, the server removes him from the list of clients.
