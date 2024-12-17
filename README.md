# Desktop multiplayer cardgame "OK/ÖKK"


## 1. Aim of the game

OKGame is a card game based on the board game "Trial by Trolley". The objective of the game is to build
a better team than the other players, thereby increasing your average score. Players are divided into
Players (2 teams) and the Decider. The game continues until everyone has had a turn as the Decider.


## 2. Downloading the Project

Clone the project folder from Git into IntelliJ:
git clone https://gitlab.cs.taltech.ee/kadi.jairus/iti0301-2024-ok.git


## 3. Running the Game

The game needs three game windows: 2 Players and 1 Decider.
Players build their teams and sabotage the opponents team, Decider is just watching until last row.
f you can't start the game, please contact in Discord: KadiJ (kadijairus) or Kelli (kelli_48450) or Ketlin (ketlin2).

### 3.1. Download the project and open in IntelliJ
Download the project from Git (see 2. Downloading the project)
Open the "client" folder in IntelliJ.
Check, if it is possible to launch multiple games. If not, adjust in menu next to the Run button: under "Configuration" select Edit, then under "Run" click "Modify options" and select "Allow multiple instances"

## 3.2. Open three game windows
Run the client by clicking the "Run" button. The game's main view will open. 
Click the "Play" button, insert your name and click "Mängima".
You are playing as left player.
Repeat this two more times:
Click "Run" again to open the game window for the right player.
Click "Run" for the third time to open the game window for the decider (player without cards).

## 3.3. Play first row (good team-members)
Both players get three cards.
In the first row both players have to select the beast team-member for their own team.
Select the best card from three and drag it to your own team area.
Click "Play" to allow moving to the next row.
If both players are finished, second row starts.

## 3.4. Play second row (good team-members)
The same as first row.
Both players get new three cards.
In the second row both players have to select the best team-member for their own team.
Select the best card from three and drag it to your own team area.
Click "Play" to allow moving to the next row.

## 3.5. Play third row (bad team-members)
Both players get new three cards, but now these have negative meaning.
In the third row both players have to select the worst team-member for the opposite team.
Select the worst card from three and drag it to the opponents team area.
Click "Play" to allow moving to the next row.

## 3.6. Play fourth row (modifier cards)
Both players get three modifier cards.
Modifier cards give additional information about team-member cards on the game-board.
You can use negative modifiers to make the opponents team-members even worse.
Or you can use positive modifiers to make your own team better.
Select one from three cards and drag next to the person, that you wish to modify.
Click "Play" to allow moving to the next row.

## 3.7. Play fifth row (modifier cards)
Both players get new three modifier cards.
Modifier cards give additional information about team-member cards on the game-board.
You can use negative modifiers to make the opponents team-members even worse.
Or you can use positive modifiers to make your own team better.
Select one from three cards and drag next to the person, that you wish to modify.
Click "Play" to allow moving to the next row.

## 3.8. Decide the best team
Players turns have ended.
Decider sees the final state of both teams, and selects one of them.
Players receive the result.
To be added in the future: Winner gets the grade "5" and loser gets "1". The average grade of the players changes.
To be added in the future: The roles change. The game continues until everyone has had a turn as the Decider.


## 4. Data Flow

### 4.1. General principle of the data flow.
GameClient object is created.
In GameClient MenuScreen is initialized.
Player presses "Play", PacketIsPlayPressed is sent to server.
Server puts created client ID to map and sends as playerID to all connections.
Server creates AI player.
Server sends PacketPlayerID back to current player only.
Current player creates Player class object using playerID sent by server.
Player moves card. Position info is sent as PacketCardPositionInfo to server.
Server puts PacketCardPositionInfo to Map gameState and sends Map to all connections.

### 4.2. Data packet classes

All data packet classes are in the "packet" folder (present under "server" and "client").
In the server/client terminal, you can see the data sent from the client and debug messages to confirm data flow.

##### PacketCardPositionInfo (client -> server -> all clients)
Currently not in use anymore.
Sent from: client/Card touchUp (listener)
Is created if the player moves a card to gameboard and releases it.
This packet is created under Card class, under listener "touchUp".
Server response:
Packet added to gameState Map, gameState sent to all connections.
Client response:
Card positions updated based on gameState
Contains:
cardID (int)
x (int)
y (int)
##### PacketCardPositionMap (server -> all clients)
Sent from: /server/GameServer (receiver)
After getting any PacketCardPositionInfo object, server gameState map is updated.
PacketCardPositionMap is updated and sent to all connections.
Client response:
GameClient (receiver) updates gameState Map.
GameBoard state rendered to current state.
Contains:
cardPositionInfoMap <cardID, PacketCardPositionInfo>
##### PacketCardSlotPosition (client -> server -> all clients)
Sent from: client/Card touchUp (listener)
Is created if the player moves a card to gameboard and releases it.
This packet is created under Card class, under listener "touchUp".
Server response:
Packet added to gameState Map, gameState sent to all connections.
Client response:
Card positions updated based on gameState
Contains:
cardID (int)
slotID (int)
##### PacketEndTurnPressed
Sent from: client
Informs the server, that client is willing to go to next round.
The round is chenged, when both players have sent this packet.
endTurnPassed (boolean)
##### PacketIsPlayPressed (client -> server)
Sent from: /client/screens/MenuScreen - playButton (listener)
PackagePlayPressed object is sent to the server when the client clicks the "Play" button and "Mängima".
(a message "Play button pressed" appears in the server terminal).
Server response:
The server responds to the client with PacketPlayerID object (see below).
Contains:
playPressed (boolean)
playerName (String)
##### PacketOtherPlayersInfo (server -> client)
Sent from: /server/GameServer (receiver)
Sent to other players, when new player joins.
id (int)
role (String)
name (String)
##### PacketPlayerCardsMap (server -> client)
Sent from: /server/GameServer (receiver)
Sent to give each Player their unique three cards.
Cards are given as map: playerID and list of cardID-s.
playerCardsMap (Map<Integer, List<Integer>>)
##### PacketPlayerExitMessage (client -> server -> clients)
Sent from: client -> server -> clients
Exiting player sends its ID to inform server.
Server sends packet to all players and clears all variables.
Other players disconnect.
currentPlayerID (int)
##### PacketPlayerID (server -> client)
Sent from: /server/GameServer - PlayButton (listener)
If server receives PackagePlayPressed, then it responses to sender with PacketPlayerID object,
PlayerID is connection ID (assigned by server automatically, starting with 1).
Role is assigned based on count from 1 to 3.
Client response:
PacketPlayerID.playerID is used in Player constructor.
Player.type string value is assigned based on this number.
Contains:
playerID (int)
playerName (String)
playerRole (String: LeftPlayer, RightPlayer, Decider)
##### PacketRequestSlotOccupancy (client -> server)
slotName (String)
##### PacketRespondSlotOccupancy (server -> client)
slotName (String)
isOccupied (boolean)
##### PacketRoundNumber
roundNumber (int)
##### PacketSlotOccupancy
slotOccupancy (Map<String slotName, boolean isOccupied)

## 5. Description of Game Classes

### 5.1. In the "client" folder

##### OKGame
The main game class, creates a new GameClient object and renders the image.
##### GameClient
the class representing a unique player, establishes a connection between the client and server.
##### Card
An libGDX "Actor" object moved by the Player. Object movement data is sent under touchUp listener.
##### GameStateManager
Applies the rules of different rounds.
##### Hud
Buttons to move to next round or quit.
##### SlotInfo
Rectangular positions on the game board where players are allowed to place cards.

#### Packet

See under "Data flow".

#### Players

###### Player
Player object associated with current connection. Has playerID (given by server) which modifies the rules.

#### Rules

##### RulesRoundOne, RulesRoundTwo
Specifies, that card can be positioned only once on your own side of the game-board.
##### RulesRoundThree
Specifies, that card can be positioned only once on your opponents side of the game-board.
##### RulesRoundFour, RulesRoundFive
Card can be positioned on your opponents or on your own side of the game-board.

#### Screens

##### MenuScreen
First screen when starting the game. If "Play" is pressed, then connection data is sent to server
and GameBoardScreen is initialized.
##### LobbyScreen
Currently empty. Place to wait for other players to start.
##### GameBoardScreen
GameBoardScreen - the visual representation of the game board, areas for viewing and placing cards.
The class where visible gameboard with slots is created and Cards are moved.


### 5.2. In the "server" folder

##### GameServer
Class describing the operation of the game server (receives information from the client
and sends it back).

##### AI
AI player based on LibGDX-ai Behaviour Tree.
Gets each cards positivity value.
Calculates average positivity score for both teams (using only cards on the table.)
When playing, chooses a card that makes the two teams average positivity scores most similar.
Communicates with the player only through server.

##### Folder: json
Same JSON files as client has. To get cardID and positivity values as a map for AI.
PositiveCardsJson.json - used in first two rows.

##### Folder: packet

See under "Data flow".


## About the project

This project was developed during "ITI0301 Tarkvaraarenduse projekt" course
at Tallinn University of Technology in 2024.

If you can't start the game, please contact in Discord: KadiJ (kadijairus) or Kelli (kelli_48450) or Ketlin (ketlin2). 
