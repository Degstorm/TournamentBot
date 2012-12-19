package com.TournamentBot;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class IRCClient {
	private class Player {
		private String nickname; //their chat nickname
		private String username; //their game username
		private Player opponent; //the person they're fighting with
		
		public Player getOpponent () {
			return this.opponent;
		}
		
		public String getUsername() {
			return this.username;
		}
		
		public void setOpponent (Player opponent) {
			this.opponent = opponent;
		}
		
		public Player ( String nickname, String username) {
			this.nickname = nickname;
			this.username = username.toLowerCase();
		}
		
		@Override
		public String toString() {
			return this.nickname;
		}
		
	}
	
	private PrintWriter out;
	
	private ArrayList<Player> battlers; //the people in the battle
	private boolean started, battling;
	
	public PrintWriter getClient () {
		return this.out;
	}
	
	public IRCClient (final String serveraddr, final int port) {
		started = battling = false;
		battlers = new ArrayList<Player>();
		
		new Thread (new Runnable() {
			public void run() {
				try {
					doConnect(serveraddr, port);
				} catch (UnknownHostException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}).start();
	}
	
	private void doConnect ( String serveraddr, int port ) throws UnknownHostException, IOException { //connect to the IRC server
		Socket server = new Socket ( serveraddr, port );
		
		BufferedReader in = new BufferedReader(new InputStreamReader(
				server.getInputStream()));
		out = new PrintWriter(
					new BufferedWriter(new OutputStreamWriter(
							server.getOutputStream())), true);

		String message;
		
		out.println ("USER TournamentBot * * :PokemonLake Tournament bot");
		out.println ("NICK TournamentBot");

        new Thread(
            new Runnable() {
                public void run() {
                    while (true) {
                        out.println ("PING :1");
                        try {
                            Thread.sleep (60000);
                        } catch (java.lang.InterruptedException e) {
                        }
                    }
                }
            }
        ).start();
        
        /* Send a ping every 60000 ms (1 minute)
        /* You actually need to ping the server - responding to its pings is not enough.
         * I've noticed that if you do nothing for a long time, and you don't ping the server, you'll eventually be disconnected.
         */
                

		while ( (message = in.readLine()) != null) {
			//System.out.println (message); //DEBUG
			
			String[] params = message.split (" ");
			
			if (params[0].equals ("PING")) {
				out.println ("PONG " + params[1]);
			} else if (params[1].equals ("376")) {
				//:server 376 NICK :End of MOTD
				out.println ("JOIN :#tournament");
        		out.println ("NS SID TournamentBot PASSWORD");
                out.println ("TOPIC #tournament :Welcome to #tournament, the pokemonlake battle tournament channel! No tournament is happening now, type !tournament to start a tournament.");

			} else if (params[1].equals ("PRIVMSG")) {
				//USER PRIVMSG #channel/nickname :MESSAGE
				if (params[2].toLowerCase().equals("#tournament")) {
					if (params[3].equals(":!tournament")) { //!tournament starts the tournament
						params[0] = (params[0].split("!")[0]).split(":")[1];
						//nick!user@host
						
						if (started || battling) {
							out.println ("PRIVMSG #tournament " + params[0] + ": You can't join a tourney now! (Either a battle is on the way or nobody did !tournament");
						}
						else {
							out.println ("PRIVMSG #tournament :WHO WANTS A TOURNAMENT SAY !me (don't forget the !), followed by your GAME USERNAME!!!!!");
							out.println ("PRIVMSG #tournament :For example: !me errietta, !me MarcusDamon");
							out.println ("PRIVMSG #tournament :ATTENTION! If you fake your username you risk being BANNED from future tournaments");
                                            out.println ("TOPIC #tournament :Welcome to #tournament, the pokemonlake battle tournament channel! A tournament has been started: type !me GAME_USERNAME to join!");
							started = true;
						}
					}
                    else if (params[3].equals(":!endgame")) {
                        endGame();
                    }
					else if (params[3].equals(":!me")) {
						params[0] = (params[0].split("!")[0]).split(":")[1];
						//nick!user@host

						if (params.length < 5) {
							//nick!user@host PRIVMSG #tournament :!me your_username
							//1					2		3		   4 	5			- We need 5 params to privmsg or the syntax is wrong.
							
							out.println ("PRIVMSG #tournament :" + params[0] + ": It's !me GAME_USERNAME_HERE, please!!!");
							out.println ("PRIVMSG #tournament :" + params[0] + ": Example: !me errietta");
						}
						else if (!started || battling) {
							out.println ("PRIVMSG #tournament :" + params[0] + ": You can't join a tourney now! (Either a battle is on the way or nobody did !tournament");
						} else if ( getPlayerByUserName (params[4]) != null) {
                            out.println ("PRIVMSG #tournament :" + params[0] + ": You can't sing up twice!");
                        }
						else {
							out.println ("PRIVMSG #tournament :WELCOME " + params[0]);
							battlers.add ( new Player (params[0], params[4]) );
							//Their nickname, and their game username is stored.
							//So that when they win a battle by their game username, their nickname is notified.
							
							if (battlers.size() == 4) {
								//4 people joined. First 2 VS other 2 lineup.
								battling = true;
								out.println ("PRIVMSG #tournament :CURRENT LINEUP: ");
								out.println ( "PRIVMSG #tournament :" + ( battlers.get(0) + " VS " + battlers.get(1) ).toUpperCase() );
								out.println ( "PRIVMSG #tournament :" + ( battlers.get(2) + " VS " + battlers.get(3) ).toUpperCase() );
								out.println ( "MODE #tournament +mN" );
                                out.println ("TOPIC #tournament :Welcome to #tournament, the pokemonlake battle tournament channel! A tournament is on the way. Lineup: "  + ( battlers.get(0) + " VS " + battlers.get(1) ).toUpperCase()  + " and " + ( battlers.get(2) + " VS " + battlers.get(3) ).toUpperCase()  + ". If you want to join a tournament, you need to wait until the current one is over. You will not be able to speak here until then!");

								for ( int i = 0; i < 4; i++ ) {
									out.println ( "MODE #tournament +v " + battlers.get(i) );
								}
								
								battlers.get(0).setOpponent(battlers.get(1));
								battlers.get(1).setOpponent(battlers.get(0));
								battlers.get(2).setOpponent(battlers.get(3));
								battlers.get(3).setOpponent(battlers.get(2));
								out.println ("PRIVMSG #tournament :BEGIN: ");
							}
						}
					}
						
				}
			}
		}
		
		server.close();
	}
	
	private Player otherwinner = null;
	private boolean second = false;
	
	public void win(String win) { //this happens when the game notifies the bot someone has won
		//System.out.println ("win" + win); //DEBUG
		
		Player winner = getPlayerByUserName (win); //find them by their game username
		if (winner == null) {
            //System.out.println ("Something weird is going on.. :S"); //DEBUG
			/* Someone has won a battle, but they're not part of the tourney */
			return;
		}
		
		Player loser =  winner.getOpponent();
		
		/*
        System.out.println ("Announcing!");
        System.out.println (loser);
		*/
		
		out.println ("PRIVMSG #tournament :!!!!!!1ATTENTION:" + winner + " ( " + winner.getUsername() + " ) "
				+ " won the battle against " + loser + " ( " + loser.getUsername() + " ) " );
		out.println ("MODE #tournament -v " + loser);
		
		if (otherwinner == null && !second) { //only 1 pariup has a winner and this is the first battle
			otherwinner = winner;
		} else if ( otherwinner != null && !second ) { //both pairups have a winner, but this is the first battle
			out.println ("PRIVMSG #tournament :CURRENT LINEUP: ");
			out.println ("PRIVMSG #tournament :" + ( winner + " VS " + otherwinner ).toUpperCase() );
            out.println ("TOPIC #tournament :Welcome to #tournament, the pokemonlake battle tournament channel! A tournament is on the way. Lineup: "  +  (winner + " VS " + otherwinner).toUpperCase() + ". If you want to join a tournament, you need to wait until the current one is over. You will not be able to speak here until then!");
			winner.setOpponent(otherwinner);
			otherwinner.setOpponent(winner);
			second = true;
		} else { //both pairup has a winner and this is the second battle - tournament is over.
			out.println ("PRIVMSG #tournament :!!!! ATTENTION: CONGRATULATIONS " + winner + " YOU GET THE TROPHY!");
			out.println ( "MODE #tournament -v " + winner);
			out.println ( "MODE #tournament -mN" );
			endGame();
		}
		
	}
	
	private void endGame() {
        out.println ( "MODE #tournament -mN" );
        out.println ("TOPIC #tournament :Welcome to #tournament, the pokemonlake battle tournament channel! No tournament is happening now, type !tournament to start a tournament.");

		otherwinner = null;
		second = false;
		started = false;
		battling = false;
        
        for (Player player : battlers) {
            out.println ("MODE #tournament -v " + player);
        }

		battlers = new ArrayList<Player>();
	}
	private Player getPlayerByUserName(String username) {
		for (Player player : battlers) {
			if (player.getUsername().equals (username)) {
				return player;
			}
		}
		
		return null;
	}
}
