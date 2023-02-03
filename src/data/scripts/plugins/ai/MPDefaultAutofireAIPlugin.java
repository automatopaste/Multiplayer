package data.scripts.plugins.ai;

import com.fs.starfarer.api.combat.AutofireAIPlugin;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

public class MPDefaultAutofireAIPlugin implements AutofireAIPlugin {

    private final WeaponAPI weapon;

    private float targetFacing;
    private boolean t1;
    private boolean t2;

    public MPDefaultAutofireAIPlugin(WeaponAPI weapon) {
        this.weapon = weapon;
    }

    @Override
    public void advance(float amount) {

    }

    @Override
    public boolean shouldFire() {
        boolean fire = t1 && !t2;
        t2 = t1;
        t1 = false;
        return fire;
    }

    @Override
    public void forceOff() {

    }

    public void trigger() {
        t1 = true;
    }

    public void setTargetFacing(float facing) {
        targetFacing = facing;
    }

    @Override
    public Vector2f getTarget() {
        return MathUtils.getPointOnCircumference(weapon.getLocation(), weapon.getRange(), targetFacing);
    }

    @Override
    public ShipAPI getTargetShip() {
        return null;
    }

    @Override
    public WeaponAPI getWeapon() {
        return weapon;
    }

    @Override
    public MissileAPI getTargetMissile() {
        return null;
    }
}
