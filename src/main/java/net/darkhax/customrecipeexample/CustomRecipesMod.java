package net.darkhax.customrecipeexample;

import java.util.Map;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.item.crafting.RecipeManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent.Register;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.items.ItemHandlerHelper;

@Mod("customrecipeexample")
public class CustomRecipesMod {
    
    // Creates a new recipe type. This is used for storing recipes in the map, and looking them
    // up.
    public static final IRecipeType<ClickBlockRecipe> CLICK_BLOCK_RECIPE = new RecipeTypeClickBlock();
    
    public CustomRecipesMod() {
        
        // Registers an event with the mod specific event bus. This is needed to register new
        // stuff.
        FMLJavaModLoadingContext.get().getModEventBus().addGenericListener(IRecipeSerializer.class, this::registerRecipeSerializers);
        
        // Registers an event with the general game event bus. This is used to perform click
        // block crafting.
        MinecraftForge.EVENT_BUS.addListener(this::onPlayerClickBlock);
    }
    
    private void registerRecipeSerializers (Register<IRecipeSerializer<?>> event) {
        
        // Recipe types have to be registered, but Forge doesn't currently wrap the IRecipeType
        // registry.
        // Registering it before you register the serializer should be fine in the short term.
        Registry.register(Registry.RECIPE_TYPE, new ResourceLocation(CLICK_BLOCK_RECIPE.toString()), CLICK_BLOCK_RECIPE);
        
        // Register the recipe serializer. This handles from json, from packet, and to packet.
        event.getRegistry().register(ClickBlockRecipe.SERIALIZER);
    }
    
    private void onPlayerClickBlock (PlayerInteractEvent.LeftClickBlock event) {
        
        // Check that the world is server side, and the player actually exists.
        if (!event.getWorld().isRemote && event.getEntityPlayer() != null) {
            
            // Get the currently held item of the player, for the hand that was used in the
            // event.
            final ItemStack heldItem = event.getEntityPlayer().getHeldItem(event.getHand());
            
            // Iterates all the recipes for the custom recipe type. If you have lots of recipes
            // you may want to consider adding some form of recipe caching. In this case we
            // could store the last successful recipe in a global field to lower the lookup
            // time for repeat crafting. You could also use RecipesUpdatedEvent to build a
            // cache of your recipes. Make sure to build the cache on LOWEST priority so mods
            // like CraftTweaker can work with your recipes.
            for (final IRecipe<?> recipe : this.getRecipes(CLICK_BLOCK_RECIPE, event.getWorld().getRecipeManager()).values()) {
                
                // If you need access to custom recipe methods you will need to check and cast
                // to your recipe type. This step could be skipped if you did it during a cache
                // process.
                if (recipe instanceof ClickBlockRecipe) {
                    
                    final ClickBlockRecipe clickBlockRecipe = (ClickBlockRecipe) recipe;
                    
                    // isValid is a custom recipe which checks if the held item and block match
                    // a known recipe. If this were cached to a multimap you could use Block as
                    // a key and only check the held item.
                    if (clickBlockRecipe.isValid(heldItem, event.getWorld().getBlockState(event.getPos()).getBlock())) {
                        
                        // When the recipe is valid, shrink the held item by one.
                        heldItem.shrink(1);
                        
                        // This forge method tries to give a player an item. If they have no
                        // room it drops on the ground. We're giving them a copy of the output
                        // item.
                        ItemHandlerHelper.giveItemToPlayer(event.getEntityPlayer(), clickBlockRecipe.getRecipeOutput().copy());
                        event.setCanceled(true);
                        break;
                    }
                }
            }
        }
    }
    
    /**
     * This method lets you get all of the recipe data for a given recipe type. The existing
     * methods for this require an IInventory, and this allows you to skip that overhead. This
     * method uses reflection to get the recipes map, but an access transformer would also
     * work.
     * 
     * @param recipeType The type of recipe to grab.
     * @param manager The recipe manager. This is generally taken from a World.
     * @return A map containing all recipes for the passed recipe type. This map is immutable
     *         and can not be modified.
     */
    private Map<ResourceLocation, IRecipe<?>> getRecipes (IRecipeType<?> recipeType, RecipeManager manager) {
        
        final Map<IRecipeType<?>, Map<ResourceLocation, IRecipe<?>>> recipesMap = ObfuscationReflectionHelper.getPrivateValue(RecipeManager.class, manager, "field_199522_d");
        return recipesMap.get(recipeType);
    }
}