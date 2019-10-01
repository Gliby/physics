Gliby's Physics (1.8 branch)
=============

Gliby's Physics is a Minecraft Forge modification which adds realistic physics simulation.

### Building for release.

```sh
$ ./export.sh
```

### Setting up workspace from terminal.
```sh
$ ./setupWorkspace.sh
```

### Setting IDE (InteliJ)
* Import ```build.gradle``` and follow https://mcforge.readthedocs.io/en/latest/gettingstarted/#terminal-free-intellij-idea-configuration

### Setting IDE (Eclipse)
* Get the MDK https://files.minecraftforge.net/maven/net/minecraftforge/forge/index_1.8.html, and put the 'eclipse' dir in the root of the project. Then run:
```sh
$ ./gradlew -b setupWorkspace.gradle eclipse
```