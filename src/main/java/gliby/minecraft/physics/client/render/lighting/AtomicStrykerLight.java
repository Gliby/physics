package gliby.minecraft.physics.client.render.lighting;


import atomicstryker.dynamiclights.client.DynamicLights;
import atomicstryker.dynamiclights.client.IDynamicLightSource;
import net.minecraft.entity.Entity;

public class AtomicStrykerLight implements gliby.minecraft.physics.client.render.lighting.IDynamicLightHandler {

    @Override
    public void create(Entity light, int lightValue) {
//        DynamicLight
        DynamicLights.addLightSource(new IDynamicLightSource() {
            @Override
            public Entity getAttachmentEntity() {
                return light;
            }

            @Override
            public int getLightLevel() {
                return lightValue;
            }
        });
    }
}
