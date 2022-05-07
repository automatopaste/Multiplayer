package data.scripts;

import com.fs.starfarer.api.BaseModPlugin;

public class mpModPlugin extends BaseModPlugin {
    @Override
    public void onApplicationLoad() {
        System.out.println(System.getProperty("io.netty.tryReflectionSetAccessible"));

        // toy for later
        //System.setProperty("io.netty.noUnsafe", String.valueOf(true));
    }
}