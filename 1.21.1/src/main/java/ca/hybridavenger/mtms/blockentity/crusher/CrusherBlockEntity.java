package ca.hybridavenger.mtms.blockentity.crusher;

import ca.hybridavenger.mtms.blockentity.ModBlockEntities;
import ca.hybridavenger.mtms.recipe.ModRecipes;
import ca.hybridavenger.mtms.recipe.crusher.CrusherBlockRecipe;
import ca.hybridavenger.mtms.recipe.crusher.CrusherBlockRecipeInput;
import ca.hybridavenger.mtms.screen.crusher.CrusherMenu;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class CrusherBlockEntity extends BlockEntity implements MenuProvider {
    // 3 slots: INPUT, PRIMARY_OUTPUT, BONUS_OUTPUT
    public final ItemStackHandler itemHandler = new ItemStackHandler(3) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            if(level != null && !level.isClientSide()) {
                level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
            }
        }
    };

    private static final int INPUT_SLOT = 0;
    private static final int PRIMARY_OUTPUT_SLOT = 1;
    private static final int BONUS_OUTPUT_SLOT = 2;

    private LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();

    // Energy Storage
    private final CrusherEnergyStorage energyStorage = new CrusherEnergyStorage(50000, 500) {
        @Override
        public void onEnergyChanged() {
            setChanged();
            if(level != null && !level.isClientSide()) {
                level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
            }
        }
    };
    private LazyOptional<IEnergyStorage> lazyEnergyHandler = LazyOptional.empty();

    private int currentRecipeEnergy = 50; // Energy per tick for current recipe

    protected final ContainerData data;
    private int progress = 0;
    private int maxProgress = 100;

    public CrusherBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(ModBlockEntities.CRUSHER_BE.get(), pPos, pBlockState);
        data = new ContainerData() {
            @Override
            public int get(int i) {
                return switch (i) {
                    case 0 -> CrusherBlockEntity.this.progress;
                    case 1 -> CrusherBlockEntity.this.maxProgress;
                    case 2 -> CrusherBlockEntity.this.energyStorage.getEnergyStored() & 0xFFFF;
                    case 3 -> (CrusherBlockEntity.this.energyStorage.getEnergyStored() >> 16) & 0xFFFF;
                    case 4 -> CrusherBlockEntity.this.energyStorage.getMaxEnergyStored() & 0xFFFF;
                    case 5 -> (CrusherBlockEntity.this.energyStorage.getMaxEnergyStored() >> 16) & 0xFFFF;
                    default -> 0;
                };
            }

            @Override
            public void set(int i, int value) {
                switch (i) {
                    case 0: CrusherBlockEntity.this.progress = value; break;
                    case 1: CrusherBlockEntity.this.maxProgress = value; break;
                    case 2: CrusherBlockEntity.this.energyStorage.setEnergy(
                            (CrusherBlockEntity.this.energyStorage.getEnergyStored() & 0xFFFF0000) | (value & 0xFFFF)); break;
                    case 3: CrusherBlockEntity.this.energyStorage.setEnergy(
                            (CrusherBlockEntity.this.energyStorage.getEnergyStored() & 0x0000FFFF) | ((value & 0xFFFF) << 16)); break;
                    case 4: break;
                    case 5: break;
                }
            }

            @Override
            public int getCount() {
                return 6;
            }
        };
    }

    @Override
    public void onLoad() {
        super.onLoad();
        lazyItemHandler = LazyOptional.of(() -> itemHandler);
        lazyEnergyHandler = LazyOptional.of(() -> energyStorage);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        lazyItemHandler.invalidate();
        lazyEnergyHandler.invalidate();
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if(cap == ForgeCapabilities.ITEM_HANDLER) {
            return lazyItemHandler.cast();
        }
        if(cap == ForgeCapabilities.ENERGY) {
            return lazyEnergyHandler.cast();
        }
        return super.getCapability(cap, side);
    }

    public void drops() {
        SimpleContainer inventory = new SimpleContainer(itemHandler.getSlots());
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            inventory.setItem(i, itemHandler.getStackInSlot(i));
        }
        Containers.dropContents(this.level, this.worldPosition, inventory);
    }

    @Override
    protected void saveAdditional(CompoundTag pTag, HolderLookup.Provider pRegistries) {
        pTag.put("inventory", itemHandler.serializeNBT(pRegistries));
        pTag.putInt("crusher.progress", progress);
        pTag.putInt("crusher.max_progress", maxProgress);
        pTag.putInt("crusher.energy", energyStorage.getEnergyStored());
        pTag.putInt("crusher.current_recipe_energy", currentRecipeEnergy);

        super.saveAdditional(pTag, pRegistries);
    }

    @Override
    protected void loadAdditional(CompoundTag pTag, HolderLookup.Provider pRegistries) {
        super.loadAdditional(pTag, pRegistries);

        itemHandler.deserializeNBT(pRegistries, pTag.getCompound("inventory"));
        progress = pTag.getInt("crusher.progress");
        maxProgress = pTag.getInt("crusher.max_progress");
        energyStorage.setEnergy(pTag.getInt("crusher.energy"));
        currentRecipeEnergy = pTag.getInt("crusher.current_recipe_energy");
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.mtms.crusher");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int pContainerId, Inventory pPlayerInventory, Player pPlayer) {
        return new CrusherMenu(pContainerId, pPlayerInventory, this, this.data);
    }

    public void tick(Level level, BlockPos blockPos, BlockState blockState) {
        if(hasRecipe()) {
            Optional<RecipeHolder<CrusherBlockRecipe>> recipe = getCurrentRecipe();
            if(recipe.isPresent()) {
                int totalEnergy = recipe.get().value().energyCost();
                currentRecipeEnergy = Math.max(1, totalEnergy / maxProgress);
            }

            if(hasEnoughEnergy()) {
                increaseCraftingProgress();
                energyStorage.extractEnergy(currentRecipeEnergy, false);
                setChanged(level, blockPos, blockState);

                if (hasCraftingFinished()) {
                    craftItem();
                    resetProgress();
                }
            }
        } else {
            resetProgress();
        }
    }

    private boolean hasEnoughEnergy() {
        return energyStorage.getEnergyStored() >= currentRecipeEnergy;
    }

    private void resetProgress() {
        this.progress = 0;
        this.maxProgress = 100;
    }

    private void craftItem() {
        Optional<RecipeHolder<CrusherBlockRecipe>> recipe = getCurrentRecipe();
        CrusherBlockRecipe recipeValue = recipe.get().value();
        ItemStack primaryOutput = recipeValue.primaryOutput();
        ItemStack bonusOutput = recipeValue.bonusOutput();
        int inputCount = recipeValue.inputCount();
        float bonusChance = recipeValue.bonusChance();

        // Extract input items
        itemHandler.extractItem(INPUT_SLOT, inputCount, false);

        // Add primary output
        itemHandler.setStackInSlot(PRIMARY_OUTPUT_SLOT, new ItemStack(primaryOutput.getItem(),
                itemHandler.getStackInSlot(PRIMARY_OUTPUT_SLOT).getCount() + primaryOutput.getCount()));

        // Add bonus output with chance
        if (!bonusOutput.isEmpty() && level.random.nextFloat() < bonusChance) {
            itemHandler.setStackInSlot(BONUS_OUTPUT_SLOT, new ItemStack(bonusOutput.getItem(),
                    itemHandler.getStackInSlot(BONUS_OUTPUT_SLOT).getCount() + bonusOutput.getCount()));
        }
    }

    private boolean hasCraftingFinished() {
        return this.progress >= this.maxProgress;
    }

    private void increaseCraftingProgress() {
        progress++;
    }

    private boolean hasRecipe() {
        Optional<RecipeHolder<CrusherBlockRecipe>> recipe = getCurrentRecipe();
        if(recipe.isEmpty()) {
            return false;
        }

        CrusherBlockRecipe recipeValue = recipe.get().value();
        ItemStack primaryOutput = recipeValue.primaryOutput();
        ItemStack bonusOutput = recipeValue.bonusOutput();

        return canInsertItemIntoSlot(PRIMARY_OUTPUT_SLOT, primaryOutput) &&
                canInsertAmountIntoSlot(PRIMARY_OUTPUT_SLOT, primaryOutput.getCount()) &&
                (bonusOutput.isEmpty() || (canInsertItemIntoSlot(BONUS_OUTPUT_SLOT, bonusOutput) &&
                        canInsertAmountIntoSlot(BONUS_OUTPUT_SLOT, bonusOutput.getCount())));
    }

    private Optional<RecipeHolder<CrusherBlockRecipe>> getCurrentRecipe() {
        return this.level.getRecipeManager()
                .getRecipeFor(ModRecipes.CRUSHER_TYPE.get(), new CrusherBlockRecipeInput(itemHandler.getStackInSlot(INPUT_SLOT)), level);
    }

    private boolean canInsertItemIntoSlot(int slot, ItemStack output) {
        return itemHandler.getStackInSlot(slot).isEmpty() ||
                itemHandler.getStackInSlot(slot).getItem() == output.getItem();
    }

    private boolean canInsertAmountIntoSlot(int slot, int count) {
        int maxCount = itemHandler.getStackInSlot(slot).isEmpty() ? 64 : itemHandler.getStackInSlot(slot).getMaxStackSize();
        int currentCount = itemHandler.getStackInSlot(slot).getCount();
        return maxCount >= currentCount + count;
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider pRegistries) {
        return saveWithoutMetadata(pRegistries);
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    // Energy Storage Implementation
    private static class CrusherEnergyStorage implements IEnergyStorage {
        private int energy;
        private final int capacity;
        private final int maxReceive;

        public CrusherEnergyStorage(int capacity, int maxReceive) {
            this.capacity = capacity;
            this.maxReceive = maxReceive;
            this.energy = 0;
        }

        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            int energyReceived = Math.min(capacity - energy, Math.min(this.maxReceive, maxReceive));
            if (!simulate) {
                energy += energyReceived;
                onEnergyChanged();
            }
            return energyReceived;
        }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            int energyExtracted = Math.min(energy, maxExtract);
            if (!simulate) {
                energy -= energyExtracted;
                onEnergyChanged();
            }
            return energyExtracted;
        }

        @Override
        public int getEnergyStored() {
            return energy;
        }

        @Override
        public int getMaxEnergyStored() {
            return capacity;
        }

        @Override
        public boolean canExtract() {
            return false;
        }

        @Override
        public boolean canReceive() {
            return true;
        }

        public void setEnergy(int energy) {
            this.energy = Math.max(0, Math.min(capacity, energy));
            onEnergyChanged();
        }

        public void onEnergyChanged() {
            // Override this to notify block entity of changes
        }
    }
}