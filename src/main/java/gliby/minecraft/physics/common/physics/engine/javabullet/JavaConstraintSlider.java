package gliby.minecraft.physics.common.physics.engine.javabullet;

import com.bulletphysicsx.dynamics.constraintsolver.SliderConstraint;
import gliby.minecraft.physics.common.physics.engine.IConstraintSlider;

import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;

public class JavaConstraintSlider implements IConstraintSlider {

    private SoftReference<SliderConstraint> constraint;

    JavaConstraintSlider(SliderConstraint sliderConstraint) {
        this.constraint = new SoftReference<SliderConstraint>(sliderConstraint);
    }

    @Override
    public Object getConstraint() {
        return constraint.get();
    }

    @Override
    public boolean isPoint2Point() {
        return false;
    }

    @Override
    public boolean isGeneric6Dof() {
        return false;
    }

    @Override
    public boolean isSlider() {
        return true;
    }

}
