package muddykat.alchemia.proxy;

import muddykat.alchemia.Alchemia;
import muddykat.alchemia.client.ClientSetup;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

public class ClientProxy implements ISidedProxy {
    @Override
    public Player getPlayer() {
        return Minecraft.getInstance().player;
    }

    @Override
    public Level getWorld() {
        return Minecraft.getInstance().level;
    }

    @Override
    public void init() {
    }

    @Override
    public void openCodexGui() {
        //Minecraft.getInstance().setScreen(CodexGui.getInstance());
    }
}