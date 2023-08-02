package de.rapha149.signgui.version;

import de.rapha149.signgui.SignEditor;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.MessageToMessageDecoder;
import net.minecraft.core.BlockPosition;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.PacketPlayInUpdateSign;
import net.minecraft.network.protocol.game.PacketPlayOutOpenSignEditor;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.network.PlayerConnection;
import net.minecraft.world.item.EnumColor;
import net.minecraft.world.level.block.entity.TileEntitySign;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_19_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;

public class Wrapper1_19_R3 implements VersionWrapper {

    private final Field NETWORK_MANAGER_FIELD;

    {
        Field networkManagerField = null;
        for (Field field : PlayerConnection.class.getDeclaredFields()) {
            if (field.getType() == NetworkManager.class) {
                field.setAccessible(true);
                networkManagerField = field;
                break;
            }
        }

        NETWORK_MANAGER_FIELD = networkManagerField;
    }

    @Override
    public Material getDefaultType() {
        return Material.OAK_SIGN;
    }

    @Override
    public List<Material> getSignTypes() {
        return Arrays.asList(Material.OAK_SIGN, Material.BIRCH_SIGN, Material.SPRUCE_SIGN, Material.JUNGLE_SIGN,
                Material.ACACIA_SIGN, Material.DARK_OAK_SIGN, Material.CRIMSON_SIGN, Material.WARPED_SIGN);
    }

    @Override
    public void openSignEditor(Player player, String[] lines, Material type, DyeColor color, Location signLoc, BiConsumer<SignEditor, String[]> onFinish) throws IllegalAccessException {
        EntityPlayer p = ((CraftPlayer) player).getHandle();
        PlayerConnection conn = p.b;

        if (NETWORK_MANAGER_FIELD == null)
            throw new IllegalStateException("Unable to find NetworkManager field in PlayerConnection class.");
        if (!NETWORK_MANAGER_FIELD.canAccess(conn)) {
            NETWORK_MANAGER_FIELD.setAccessible(true);
            if (!NETWORK_MANAGER_FIELD.canAccess(conn))
                throw new IllegalStateException("Unable to access NetworkManager field in PlayerConnection class.");
        }

        Location loc = signLoc != null ? signLoc : getDefaultLocation(player);
        BlockPosition pos = new BlockPosition(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());

        TileEntitySign sign = new TileEntitySign(pos, null);
        sign.a(EnumColor.valueOf(color.toString()));
        for (int i = 0; i < lines.length; i++)
            sign.a(i, IChatBaseComponent.a(lines[i]));

        player.sendBlockChange(loc, type.createBlockData());
        conn.a(sign.f());
        conn.a(new PacketPlayOutOpenSignEditor(pos));

        NetworkManager manager = (NetworkManager) NETWORK_MANAGER_FIELD.get(conn);
        ChannelPipeline pipeline = manager.m.pipeline();
        if (pipeline.names().contains("SignGUI"))
            pipeline.remove("SignGUI");

        SignEditor signEditor = new SignEditor(sign, loc, pos, pipeline);
        pipeline.addAfter("decoder", "SignGUI", new MessageToMessageDecoder<Packet<?>>() {
            @Override
            protected void decode(ChannelHandlerContext chc, Packet<?> packet, List<Object> out) {
                try {
                    if (packet instanceof PacketPlayInUpdateSign updateSign) {
                        if (updateSign.a().equals(pos))
                            onFinish.accept(signEditor, updateSign.c());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                out.add(packet);
            }
        });
    }

    @Override
    public void displayNewLines(Player player, SignEditor signEditor, String[] lines) {
        TileEntitySign sign = (TileEntitySign) signEditor.getSign();
        for (int i = 0; i < lines.length; i++)
            sign.a(i, IChatBaseComponent.a(lines[i]));

        PlayerConnection conn = ((CraftPlayer) player).getHandle().b;
        conn.a(sign.f());
        conn.a(new PacketPlayOutOpenSignEditor((BlockPosition) signEditor.getBlockPosition()));
    }

    @Override
    public void closeSignEditor(Player player, SignEditor signEditor) {
        Location loc = signEditor.getLocation();
        signEditor.getPipeline().remove("SignGUI");
        player.sendBlockChange(loc, loc.getBlock().getBlockData());
    }
}
