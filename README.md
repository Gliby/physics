Gliby's Physics
=============

Gliby's Physics is a Minecraft Forge modification which adds realistic physics simulation.

### Building

```sh
$ ./export.sh
```

### Setting up workspace 
```sh
$ ./setupWorkspace.sh
```

### Setting IDE (Eclipse)
#### Make sure you run this after you set up the workspace, order of setup matters. Get the MDK https://files.minecraftforge.net/maven/net/minecraftforge/forge/index_1.8.html, and put the 'eclipse' dir in the root of the project.
```sh
$ ./gradlew -b setupWorkspace.gradle eclipse
```