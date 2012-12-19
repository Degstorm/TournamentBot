TournamentBot
=============

 * This IRC bot is part of PokemonLake, one of my projects.
 * 
 * PokemonLake is closed source but I can post the bot here.
 * 
 * It was created to help the #tournament channel
 * 
 * where users were being lined up to have live battles in the game by human operators
 * 
 * and the winner would get a prize.
 * 
 * The package consists of 3 classes
 * 
 * IRCClient: connects to irc to a server and room that can be configured. Waits for people to say !tournament to start the tournament,
 * 
 * and for 4 people to say !me <game-username-here> and join. It stores their game username and IRC nickname so that it can identify them,
 * 
 * and pairs them up on 1 VS 1 battles.
 * 
 * When one player from each pair wins, the rest 2 players are matched against each other, to determine the final winner.
 * 
 * Pretty simple, but so far it seems to work OK.
 * 
 * This bot was created to help what human ops were doing in a tournament channel
 * 
 * And as such doesn't entirely operate on its own, it can need some human action
 * 
 * (for example ops giving prizes to winners etc)
 * 
 * SocketServer:
 * 
 * This is how the bot knows when someone has won. When someone wins a live battle on the game, the game creates a socket client to the same address and port
 * 
 * that the bot's socket server listens to and sends WIN username_here.
 * 
 * The bot then announces it in the channel and takes the appropriate lineup action.
 * 
 * TournamentBot:
 * 
 * It just starts the IRCClient and SocketServer.



Also, if you preview it the intendation may be messed up. Not my fault, really D:

it looks ok in my text editor
