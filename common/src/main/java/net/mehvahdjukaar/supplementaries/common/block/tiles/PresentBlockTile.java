package net.mehvahdjukaar.supplementaries.common.block.tiles;


import net.mehvahdjukaar.moonlight.api.platform.PlatformHelper;
import net.mehvahdjukaar.supplementaries.common.block.blocks.PresentBlock;
import net.mehvahdjukaar.supplementaries.common.inventories.PresentContainerMenu;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.mehvahdjukaar.supplementaries.reg.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class PresentBlockTile extends AbstractPresentBlockTile {

    //"" means not packed. this is used for packed but can be opened by everybody
    public static final String PUBLIC_KEY = "@e";

    private String recipient = "";
    private String sender = "";
    private String description = "";

    public PresentBlockTile(BlockPos pos, BlockState state) {
        super(ModRegistry.PRESENT_TILE.get(), pos, state);
    }

    @Override
    public boolean canHoldItems() {
        return this.isPacked();
    }

    public boolean isPacked() {
        return this.getBlockState().getValue(PresentBlock.PACKED);
    }

    public String getSender() {
        return sender;
    }

    public String getDescription() {
        return description;
    }

    public String getRecipient() {
        if (this.recipient.equalsIgnoreCase(PUBLIC_KEY)) return "";
        return recipient;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public void setPublic() {
        this.setRecipient(PUBLIC_KEY);
    }

    public void updateState(boolean shouldPack, String newRecipient, String sender, String description) {
        if (shouldPack) {
            if (newRecipient.isEmpty()) newRecipient = PUBLIC_KEY;
            this.recipient = newRecipient;
            this.sender = sender;
            this.description = description;
        } else {
            this.recipient = "";
            this.sender = "";
            this.description = "";
        }

        if (!this.level.isClientSide && this.isPacked() != shouldPack) {
            if (shouldPack) {
                this.level.playSound(null, this.worldPosition,
                        ModSounds.PRESENT_PACK.get(), SoundSource.BLOCKS, 1,
                        level.random.nextFloat() * 0.1F + 0.95F);
            } else {
                this.level.playSound(null, this.worldPosition,
                        ModSounds.PRESENT_OPEN.get(), SoundSource.BLOCKS, 1F,
                        level.random.nextFloat() * 0.1F + 1.2F);

            }
            this.level.setBlock(this.getBlockPos(), this.getBlockState().setValue(PresentBlock.PACKED, shouldPack), 3);
        }
    }

    @Override
    public boolean canOpen(Player player) {
        return this.recipient.isEmpty() || this.recipient.equalsIgnoreCase(PUBLIC_KEY) ||
                this.recipient.equalsIgnoreCase(player.getName().getString()) ||
                this.sender.equalsIgnoreCase(player.getName().getString());
    }

    @Override
    public InteractionResult interact(ServerPlayer player, BlockPos pos) {
        if (this.isUnused()) {
            if (this.canOpen(player)) {
                PlatformHelper.openCustomMenu(player, this, pos);
                PiglinAi.angerNearbyPiglins(player, true);
            } else {
                player.displayClientMessage(Component.translatable("message.supplementaries.present.info", this.recipient), true);
            }
            return InteractionResult.CONSUME;
        }
        return InteractionResult.PASS;
    }

    @Override
    public Component getDefaultName() {
        return Component.translatable("gui.supplementaries.present");
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        this.recipient = "";
        this.sender = "";
        this.description = "";
        if (tag.contains("Recipient")) this.recipient = tag.getString("Recipient");
        if (tag.contains("Sender")) this.sender = tag.getString("Sender");
        if (tag.contains("Description")) this.description = tag.getString("Description");
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (!this.recipient.isEmpty()) tag.putString("Recipient", this.recipient);
        if (!this.sender.isEmpty()) tag.putString("Sender", this.sender);
        if (!this.description.isEmpty()) tag.putString("Description", this.description);
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory player) {
        return new PresentContainerMenu(id, player, this, this.worldPosition);
    }

    @Nullable
    public Component getSenderMessage() {
        return getSenderMessage(this.sender);
    }

    @Nullable
    public static Component getSenderMessage(String sender) {
        if (sender.isEmpty()) return null;
        return Component.translatable("message.supplementaries.present.from", sender);
    }

    @Nullable
    public Component getRecipientMessage() {
        return getRecipientMessage(this.recipient);
    }

    @Nullable
    public static Component getRecipientMessage(String recipient) {
        if (recipient.isEmpty()) return null;
        if (recipient.equalsIgnoreCase(PUBLIC_KEY)) {
            return Component.translatable("message.supplementaries.present.public");
        } else {
            return Component.translatable("message.supplementaries.present.to", recipient);
        }
    }
}
