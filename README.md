# Standpoint
A Perspective-API based chat filter for PaperMC and compatible servers. **Do not ask for Spigot support.**

## Features
- [x] Perspective API integration
- [x] Configurable thresholds **for each flag**
- [x] Conduct any kinds of actions on flagged messages
- [x] A cool developer

## Installation
1. Find this plugin on Spigot (link soon)
2. Obtain a Perspective API key from [here](https://developers.perspectiveapi.com/s/?language=en_US)
3. Put the key in the config.yml file
4. Restart the server

## Known issues
* The plugin will not work and nobody will be able to chat if the API key is invalid or the API is down. If this ever 
happens run `/sp disable` to remove the chat listener and allow players to chat again.
* There will be a latency of ~100ms for each message sent. There will probably be a workaround in the future where 
messages can be checked retroactively, instead of before they are sent (this will be configurable).

## Configuration
TBD

## License
This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details