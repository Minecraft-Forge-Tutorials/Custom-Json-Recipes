# Custom-Json-Recipes
This mod provides a basic example of how mods can add their own custom recipe systems which are compatible with the vanilla recipe serialization framework, without breaking mods like CraftTweaker and JEI. In this example mod we create a "Click Block Recipe" which allows items to be crafted into other items when left clicked on a specific block. 

With this system your recipes will
- load from json files, which can be defined by any mod, datapack, or modpack.
- synchronized to the client when a player joins a server.
- reloadable using the /reload command.
- have the potential to be compatible with CraftTweaker and JEI without using additional hacks. 

This example mod includes all the neccesary source code, along with comments on what the code does, and why certain decisions were made. You will still need a basic understanding of Java to make use of this code. This mod also provides several example json files. These files are in the `data.modid.recipes` directory, but you can also load recipes from subdirectories of this folder. The following example recipes are included.
- Left clicking bone on stone will give the player 3 bone meal.
- Left clicking any wool on a saw mill will give the player 4 string.
- Left clicking dirt on a beacon will give the player a diamond.

The Java source files for this project are licensed under [Creative Commons 0](https://creativecommons.org/publicdomain/zero/1.0/legalcode). The build scripts used are licensed under LGPL 2.1. Any third party tools or plugins such as ForgeGralde and Minecraft are licensed under their respective copyrights. 
