package gliby.minecraft.physics.common.entity;

public enum EnumRigidBodyProperty {
	BIGGESTDISTANCE("BiggestDistance"),
	MAGNET("Magnet"),
	BLOCKSTATE("BlockState"),
	DEAD("Dead");

	private EnumRigidBodyProperty(String name) {
		this.name = name;
	}

	private String name;

	public String getName() {
		return name;
	}

}
