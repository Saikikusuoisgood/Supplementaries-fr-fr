package net.mehvahdjukaar.supplementaries.integration.fabric;

import dan200.computercraft.api.ComputerCraftAPI;
import dan200.computercraft.api.lua.LuaFunction;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.api.peripheral.IPeripheralProvider;
import dan200.computercraft.shared.media.items.ItemPrintout;
import net.mehvahdjukaar.supplementaries.common.block.blocks.SpeakerBlock;
import net.mehvahdjukaar.supplementaries.common.block.tiles.SpeakerBlockTile;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class CCCompatImpl {

    public static void setup() {
        ComputerCraftAPI.registerPeripheralProvider((IPeripheralProvider) ModRegistry.SPEAKER_BLOCK.get());
    }

    public static int getPages(ItemStack itemstack) {
        return ItemPrintout.getPageCount(itemstack);
    }

    public static String[] getText(ItemStack itemstack) {
        return ItemPrintout.getText(itemstack);
    }

    public static boolean isPrintedBook(Item item) {
        return item instanceof ItemPrintout;
    }

    public static SpeakerBlock makeSpeaker(BlockBehaviour.Properties properties) {
        //try loading this now, freaking classloader
        class SpeakerCC extends SpeakerBlock implements IPeripheralProvider {

            public SpeakerCC(Properties properties) {
                super(properties);
            }

            @Override
            public IPeripheral getPeripheral(Level world, BlockPos pos, Direction side) {
                var tile = world.getBlockEntity(pos);
                if (tile instanceof SpeakerBlockTile t) {
                    if (t.ccHack == null) t.ccHack = new SpeakerPeripheral(t);
                    return (IPeripheral) t.ccHack;
                }
                return null;
            }
        }
        return new SpeakerCC(properties);
    }

    @SuppressWarnings({"ClassCanBeRecord"})
    public static final class SpeakerPeripheral implements IPeripheral {
        private final SpeakerBlockTile tile;

        public SpeakerPeripheral(SpeakerBlockTile tile) {
            this.tile = tile;
        }

        @LuaFunction
        public void setNarrator(SpeakerBlockTile.Mode mode) {
            tile.setMode(mode);
            tile.setChanged();
        }

        @LuaFunction
        public SpeakerBlockTile.Mode getMode() {
            return tile.getMode();
        }

        @LuaFunction
        public void setMessage(String message) {
            tile.setMessage(message);
            tile.setChanged();
        }

        @LuaFunction
        public String getMessage() {
            return tile.getMessage();
        }

        @LuaFunction
        public void setName(String name) {
            tile.setCustomName(Component.literal(name));
            tile.setChanged();
        }

        @LuaFunction
        public String getName() {
            return tile.getName().getString();
        }

        @LuaFunction
        public double getVolume() {
            return tile.getVolume();
        }

        @LuaFunction
        public void setVolume(double volume) {
            tile.setVolume(volume);
            tile.setChanged();
        }

        @LuaFunction
        public void activate() {
            tile.sendMessage();
        }

        @NotNull
        @Override
        public String getType() {
            return "speaker_block";
        }

        @Override
        public boolean equals(@Nullable IPeripheral other) {
            return Objects.equals(this, other);
        }

        public SpeakerBlockTile tile() {
            return tile;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (obj == null || obj.getClass() != this.getClass()) return false;
            var that = (SpeakerPeripheral) obj;
            return Objects.equals(this.tile, that.tile);
        }

        @Override
        public int hashCode() {
            return Objects.hash(tile);
        }

        @Override
        public String toString() {
            return "SpeakerPeripheral[" +
                    "tile=" + tile + ']';
        }

    }
}
