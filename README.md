# Creators - Track YouTubers on your server!


## Usage:

For version 1.16

Every time a player with the permission tag 'creators.youtuber' joins, they will be asked to give their YouTube channel ID.
If the channel is valid, the address will be recorded alongside the player's UUID to the database specified in the config.
If they refuse to give their ID, it can be added later by /creators register.
All players with permission 'creators.view' may type /creators to open a GUI where all the registered creators' heads are contained.
Alternatively, typing /creators link <playername> will show the YouTube link of the given creator (if they are registered).

This plugin relies on the server having its own API-key for YouTube queries in order to check channel ID validity and subscriber count.

## Commands | permissions:

/creators | creators.view - Open a chest GUI containing heads of all creators registered on database.

/creators link <playername> | creators.view - Get the link to given creator's YouTube channel
 
/creators register | creators.youtuber - Register yourself as a creator by giving your YouTube channel ID.


## Potentially upcoming features (feel free to give suggestions :)

- Announcements every time a registered creator joins the server
