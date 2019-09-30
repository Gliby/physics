package gliby.minecraft.gman;

import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.util.AxisAlignedBB;

import java.util.ArrayList;

public class ModelUtility {

    public static boolean modelsOverlap(ModelRenderer modelA, ModelRenderer modelB) {
        ArrayList<ModelBox> cubeGroupsA = new ArrayList<ModelBox>();
        for (Object obj : modelA.cubeList) {
            if (obj instanceof ModelBox) {
                cubeGroupsA.add((ModelBox) obj);
            }
        }

        ArrayList<ModelBox> cubeGroupsB = new ArrayList<ModelBox>();
        for (Object obj : modelB.cubeList) {
            if (obj instanceof ModelBox) {
                cubeGroupsB.add((ModelBox) obj);
            }
        }

        for (ModelBox cubeA : cubeGroupsA) {
            AxisAlignedBB boundingBoxA = getAxisAlignedBB(cubeA);
            boundingBoxA = boundingBoxA.addCoord(modelA.rotationPointX + modelA.offsetX,
                    modelA.rotationPointY + modelA.offsetY, modelA.rotationPointZ + modelA.offsetZ);
            for (ModelBox cubeB : cubeGroupsB) {
                AxisAlignedBB boundingBoxB = getAxisAlignedBB(cubeB);
                boundingBoxB = boundingBoxB.addCoord(modelB.rotationPointX + modelB.offsetX,
                        modelB.rotationPointY + modelB.offsetY, modelB.rotationPointZ + modelB.offsetZ);
                if (boundingBoxA.intersectsWith(boundingBoxB))
                    return true;
            }

        }
        return false;
    }

    public static AxisAlignedBB getAxisAlignedBB(ModelBox box) {
        return AxisAlignedBB.fromBounds(box.posX1, box.posY1, box.posZ1, box.posX2, box.posY2, box.posZ2);
    }

}
