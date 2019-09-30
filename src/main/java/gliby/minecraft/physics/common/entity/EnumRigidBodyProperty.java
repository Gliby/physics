package gliby.minecraft.physics.common.entity;

public enum EnumRigidBodyProperty {
    BIGGESTDISTANCE("BiggestDistance"),
    MAGNET("Magnet"),
    BLOCKSTATE("BlockState"),
    DEAD("Dead");

    private String name;

    EnumRigidBodyProperty(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

}
