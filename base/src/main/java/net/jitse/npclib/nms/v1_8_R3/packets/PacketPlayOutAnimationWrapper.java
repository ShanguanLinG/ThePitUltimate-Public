package net.jitse.npclib.nms.v1_8_R3.packets;

import net.jitse.npclib.api.state.NPCAnimation;
import net.minecraft.server.v1_8_R3.PacketPlayOutAnimation;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;

public class PacketPlayOutAnimationWrapper {
    static VarHandle varHandle;
    static VarHandle varHandle2;

    {
        try {
            MethodHandles.Lookup lookup = MethodHandles.privateLookupIn(PacketPlayOutAnimation.class,MethodHandles.lookup());
            varHandle = lookup.findVarHandle(PacketPlayOutAnimation.class, "a", int.class);
            varHandle2 = lookup.findVarHandle(PacketPlayOutAnimation.class, "b", int.class);
        } catch (Throwable e){
            e.printStackTrace();
        }
    }



    public PacketPlayOutAnimation create(NPCAnimation npcAnimation, int entityId)  {
        PacketPlayOutAnimation packetPlayOutAnimation = new PacketPlayOutAnimation();
        varHandle.set(packetPlayOutAnimation,entityId);
        varHandle2.set(packetPlayOutAnimation, npcAnimation.getId());
        return packetPlayOutAnimation;
    }

}
