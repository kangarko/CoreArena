<p align="center">
  Do you want to code your own CoreArena?<br>
  <a href="https://mineacademy.org/gh-join">
    <img src="https://i.imgur.com/HGc2VG3.png" />
  </a>
</p>

# CoreArena
CoreArena is a premium quality Bukkit plugin that brings a whole new experience to your Minecraft server in the form of a monster fight arena. Its unique approach is determined by

New: Get your CoreArena maps package here:
https://mega.nz/#F!FzRBQJDb!eDg6i4NYGt7zl30NPQ3aIQ

* Realistic physical engine making arenas fully destroyable.
* Class upgrades and reward system. Can by synchronized over MySQL (and across BungeeCord).
* Well-made menus anda  powerful user interface in-game.
* Unique monster spawner options (clickable using a GUI).
* 5 minutes setup with visual creation without complicated config files.
* Fully automated, no admin required to manage games.

Check out https://mineacademy.org/plugins for more information.

You are welcome to read the **[CoreArena Wikipedia](https://github.com/kangarko/CoreArena/wiki)**, where you fill find tons of information about the installation, configuring and using this plugin.

If you have any **questions or bugs to report**, try seeing if [those can be resolved quickly](https://github.com/kangarko/CoreArena/wiki/Common-Issues) by yourself. If that does not help, you are welcome to **fill an issue**. Please read the [Getting Help the Best Way](https://github.com/kangarko/CoreArena/wiki/Getting-Help-the-Right-Way) to obtain help as quickly as possible.

# Compiling

1. Obtain Foundation from github.com/kangarko/Foundation
2. Create library/ folder in CoreArena/ and obtain binaries described in pom.xml. You have to obtain them yourself. Regarding Boss, you can just remove the very few references to it in the source code and remove the dependency from pom.xml.
3. Compile Foundation and CoreArena using Maven with the "clean install" goal.

<hr>

Dave Thomas, founder of OTI, godfather of the Eclipse strategy:

<i>Clean code can be read, and enhanced by a developer other than its original author. It has unit and acceptance tests. It has meaningful names. It provides one way rather than many ways for doing one thing. It has minimal dependencies, which are explicitly defined, and provides a clear and minimal API. Code should be literate since depending on the language, not all necessary information can be expressed clearly in code alone.</i>

