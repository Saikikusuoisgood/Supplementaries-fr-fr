package net.mehvahdjukaar.supplementaries.common.block.tiles;

import net.mehvahdjukaar.supplementaries.common.block.IKeyLockable;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class KeyLockableTile extends BlockEntity implements IKeyLockable {

    private String password = null;

    public KeyLockableTile(BlockPos pos, BlockState state) {
        super(ModRegistry.KEY_LOCKABLE_TILE.get(), pos, state);
    }

    @Override
    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public void clearPassword() {
        this.password = null;
    }

    @Override
    public String getPassword() {
        return password;
    }

    //returns true if door has to open
    public boolean handleAction(Player player, InteractionHand handIn, String translName) {
        if (player.isSpectator()) return false;
        ItemStack stack = player.getItemInHand(handIn);

        String keyPassword = IKeyLockable.getKeyPassword(stack);
        //clear ownership
        if (player.isSecondaryUseActive() && keyPassword != null) {
            if (tryClearingKey(player, stack)) return false;
        }
        //set key
        else if (this.password == null) {
            if (keyPassword != null) {
                this.setPassword(keyPassword);
                this.onKeyAssigned(level, worldPosition, player, keyPassword);
                return false;
            }
            return true;
        }
        //open
        return player.isCreative() || this.testIfHasCorrectKey(player, this.password, true, translName);
    }

    public boolean tryClearingKey(Player player, ItemStack stack) {
        if ((player.isCreative() || this.getKeyStatus(stack) == KeyStatus.CORRECT_KEY)) {
            this.clearPassword();
            this.onPasswordCleared(player, worldPosition);
            return true;
        }
        return false;
    }



    @Override
    public void load(CompoundTag compound) {
        super.load(compound);
        if (compound.contains("Password")) {
            this.password = compound.getString("Password");
        } else this.password = null;
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (this.password != null) {
            tag.putString("Password", this.password);
        }
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

}
