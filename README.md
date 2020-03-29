![Project Icon](https://raw.githubusercontent.com/Gliby/physics/1.12.2/art/logowithbackground-rounded-small.png?style=for-the-badge&logo=appveyor)
# Gliby's Physics
![GitHub license](https://img.shields.io/github/license/Gliby/physics?style=for-the-badge&logo=appveyor)
![GitHub issues](https://img.shields.io/github/issues/Gliby/physics?style=for-the-badge&logo=appveyor)
[![CurseForge download](http://cf.way2muchnoise.eu/full_298126_downloads.svg?badge_style=for_the_badge)](https://www.curseforge.com/minecraft/mc-mods/glibys-physics)

Gliby's Physics is a Minecraft Forge modification which adds realistic physics simulation. (1.8 branch)

### Building for release.

```sh
$ ./export.sh
```
## Development
### Setting up workspace from terminal.
```sh
$ ./setupWorkspace.sh
```

### Setting up IDE (IntelliJ IDEA)
1. Import ```build.gradle``` into IntelliJ and follow https://mcforge.readthedocs.io/en/latest/gettingstarted/#terminal-free-intellij-idea-configuration

### Setting up IDE (Eclipse)
1. Get the MDK https://files.minecraftforge.net/maven/net/minecraftforge/forge/index_1.8.html, copy the ``'eclipse'`` from the MDK to this project's root. Continue:
```sh
$ ./setupWorkspace.sh
```
```sh
$ ./gradlew -b setupWorkspace.gradle eclipse
```

## License

Gliby's Physics is licensed under GNU GPLv3, a free and open-source license. For more information, please see the [license file](https://github.com/Gliby/physics/blob/1.12.2/LICENSE).