Gliby's Physics (1.8 branch)
=============
<img src="https://raw.githubusercontent.com/Gliby/physics/1.8/src/main/resources/logo2.png" alt="Gliby's Physics">



Gliby's Physics is a Minecraft Forge modification which adds realistic physics simulation.

### Building for release.

```sh
$ ./export.sh
```
## Development
### Setting up workspace from terminal.
```sh
$ ./setupWorkspace.sh
```

### Setting up IDE (InteliJ)
1. First run:
```sh
$ ./gradlew setupDecompWorkspace
```
```sh
$ ./gradlew idea
```
2. Follow https://mcforge.readthedocs.io/en/latest/gettingstarted/#terminal-free-intellij-idea-configuration

### Setting up IDE (Eclipse)
1. Get the MDK https://files.minecraftforge.net/maven/net/minecraftforge/forge/index_1.8.html, copy the ``'eclipse'`` from the MDK to this project's root. Continue:
```sh
$ ./setupWorkspace.sh
```
```sh
$ ./gradlew -b setupWorkspace.gradle eclipse
```