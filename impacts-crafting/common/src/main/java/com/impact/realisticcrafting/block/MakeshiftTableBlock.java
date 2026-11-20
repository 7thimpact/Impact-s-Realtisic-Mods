package com.impact.realisticcrafting.block;

import com.impact.lib.item.UsesDurability;
import com.impact.lib.platform.LoaderPlatform;
import com.impact.realisticcrafting.menu.TieredCraftingMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * The Makeshift Table: a crude table with a 3×3 grid that takes non-metal recipes and wears out after a set number of
 * crafts. Its wear stage shows in its texture; the exact count lives in its block entity and on the item when picked up.
 */
public class MakeshiftTableBlock extends Block implements EntityBlock {
    public static final IntegerProperty WEAR = IntegerProperty.create("wear", TableWear.NEW, TableWear.FALLING_APART);
    private static final VoxelShape SHAPE = Shapes.or(
            Block.box(0.0, 12.0, 0.0, 16.0, 16.0, 16.0),
            Block.box(1.0, 0.0, 1.0, 4.0, 12.0, 4.0),
            Block.box(12.0, 0.0, 1.0, 15.0, 12.0, 4.0),
            Block.box(1.0, 0.0, 12.0, 4.0, 12.0, 15.0),
            Block.box(12.0, 0.0, 12.0, 15.0, 12.0, 15.0));

    private final MakeshiftTable table;

    public MakeshiftTableBlock(Properties properties) {
        this(MakeshiftTable.WOODEN, properties);
    }

    protected MakeshiftTableBlock(MakeshiftTable table, Properties properties) {
        super(properties);
        this.table = table;
        registerDefaultState(stateDefinition.any().setValue(WEAR, TableWear.NEW));
    }

    public MakeshiftTable table() {
        return table;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(WEAR);
    }

    @Override
    @SuppressWarnings("deprecation")
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos position, CollisionContext context) {
        return SHAPE;
    }

    @Override
    @SuppressWarnings("deprecation")
    public InteractionResult use(BlockState state, Level level, BlockPos position, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.SUCCESS;
        }
        SimpleMenuProvider menuProvider = new SimpleMenuProvider((containerId, inventory, menuPlayer) ->
                new TieredCraftingMenu(containerId, inventory, ContainerLevelAccess.create(level, position), table),
                Component.translatable(table.containerTitleKey()));
        LoaderPlatform.current().openMenuWithData(serverPlayer, menuProvider, buffer -> buffer.writeVarInt(table.ordinal()));
        return InteractionResult.CONSUME;
    }

    // A table picked up with uses spent keeps them when placed again.
    @Override
    public void setPlacedBy(Level level, BlockPos position, BlockState state, LivingEntity placer, ItemStack stack) {
        if (level.getBlockEntity(position) instanceof MakeshiftTableBlockEntity tableEntity) {
            tableEntity.setUsesSpent(UsesDurability.usesSpent(stack));
        }
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos position, BlockState state) {
        return new MakeshiftTableBlockEntity(position, state);
    }
}
