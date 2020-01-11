# ChunkTransfer-Spigot-Plugin
A simple plugin that bypasses the chunk regeneration problem from Spigot 1.14 onwards.
Author: Dan Negura (contact.dann@icloud.com)

## The Motivation
Spigot 1.13 and below used to allow chunk regeneration through the API. With 1.14 that method wasn't supported anymore and the only way to regenerate a chunk was to copy it from another world, which is time consuming and is a good source of headaches.

## The Solution
The current solution implemented by this plugin is an easy in-game command-line interface. This solution doesn't really regenerate the chunk in the same world, but allows easy trasnfer of chunks from world-to-world at the same coordinates. This means that to regenerate a portion of a world, you'd have to create a new world with the same seed, and use this plugin's commands to transfer over the chunks.
This is done easily with the following commands:
/select a ->
/select b ->
/transferfrom donorworldabc
