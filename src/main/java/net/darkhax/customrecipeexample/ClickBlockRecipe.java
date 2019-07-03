package net.darkhax.customrecipeexample;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapedRecipe;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistryEntry;

// The IRecipe system is designed to be used with an inventory, but that isn't strictly required. In your system you can choose to ignore any vanilla method here you want.
public class ClickBlockRecipe implements IRecipe<IInventory> {
    
    public static final Serializer SERIALIZER = new Serializer();
    
    private final Ingredient input;
    private final ItemStack output;
    private final Block block;
    private final ResourceLocation id;
    
    public ClickBlockRecipe(ResourceLocation id, Ingredient input, ItemStack output, Block block) {
        
        this.id = id;
        this.input = input;
        this.output = output;
        this.block = block;
        
        // This output is not required, but it can be used to detect when a recipe has been
        // loaded into the game.
        System.out.println("Loaded " + this.toString());
    }
    
    @Override
    public String toString () {
        
        // Overriding toString is not required, it's just useful for debugging.
        return "ClickBlockRecipe [input=" + this.input + ", output=" + this.output + ", block=" + this.block.getRegistryName() + ", id=" + this.id + "]";
    }
    
    @Override
    public boolean matches (IInventory inv, World worldIn) {
        
        // This method is ignored by our custom recipe system, and only has partial
        // functionality. isValid is used instead.
        return this.input.test(inv.getStackInSlot(0));
    }
    
    @Override
    public ItemStack getCraftingResult (IInventory inv) {
        
        // This method is ignored by our custom recipe system. getRecipeOutput().copy() is used
        // instead.
        return this.output.copy();
    }
    
    @Override
    public ItemStack getRecipeOutput () {
        
        return this.output;
    }
    
    @Override
    public ResourceLocation getId () {
        
        return this.id;
    }
    
    @Override
    public IRecipeSerializer<?> getSerializer () {
        
        return SERIALIZER;
    }
    
    @Override
    public IRecipeType<?> getType () {
        
        return CustomRecipesMod.CLICK_BLOCK_RECIPE;
    }
    
    @Override
    public ItemStack getIcon () {
        
        return new ItemStack(Blocks.STONE);
    }
    
    public boolean isValid (ItemStack input, Block block) {
        
        return this.input.test(input) && this.block == block;
    }
    
    private static class Serializer extends ForgeRegistryEntry<IRecipeSerializer<?>> implements IRecipeSerializer<ClickBlockRecipe> {
        
        Serializer() {
            
            // This registry name is what people will specify in their json files.
            this.setRegistryName(new ResourceLocation("customrecipeexample", "click_block_recipe"));
        }
        
        @Override
        public ClickBlockRecipe read (ResourceLocation recipeId, JsonObject json) {
            
            // Reads a recipe from json.
            
            // Reads the input. Accepts items, tags, and anything else that
            // Ingredient.deserialize can understand.
            final JsonElement inputElement = JSONUtils.isJsonArray(json, "input") ? JSONUtils.getJsonArray(json, "input") : JSONUtils.getJsonObject(json, "input");
            final Ingredient input = Ingredient.deserialize(inputElement);
            
            // Reads the output. The common utility method in ShapedRecipe is what all vanilla
            // recipe classes use for this.
            final ItemStack output = ShapedRecipe.deserializeItem(JSONUtils.getJsonObject(json, "output"));
            
            // Reads a resource location, which is used to look up the target block.
            final ResourceLocation blockId = new ResourceLocation(JSONUtils.getString(json, "blockId"));
            final Block block = ForgeRegistries.BLOCKS.getValue(blockId);
            
            // If something is invalid or null an exception should be thrown. This is used to
            // let the game and end user know a recipe was bad.
            if (block == null || block == Blocks.AIR) {
                
                throw new IllegalStateException("The block " + blockId + " does not exist.");
            }
            
            return new ClickBlockRecipe(recipeId, input, output, block);
        }
        
        @Override
        public ClickBlockRecipe read (ResourceLocation recipeId, PacketBuffer buffer) {
            
            // Reads a recipe from a packet buffer. This code is called on the client.
            final Ingredient input = Ingredient.read(buffer);
            final ItemStack output = buffer.readItemStack();
            final ResourceLocation blockId = buffer.readResourceLocation();
            final Block block = ForgeRegistries.BLOCKS.getValue(blockId);
            
            if (block == null) {
                
                throw new IllegalStateException("The block " + blockId + " does not exist.");
            }
            
            return new ClickBlockRecipe(recipeId, input, output, block);
        }
        
        @Override
        public void write (PacketBuffer buffer, ClickBlockRecipe recipe) {
            
            // Writes the recipe to a packet buffer. This is called on the server when a player
            // connects or when /reload is used.
            recipe.input.write(buffer);
            buffer.writeItemStack(recipe.output);
            buffer.writeResourceLocation(recipe.block.getRegistryName());
        }
    }
}