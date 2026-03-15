package dev.amble.space.datagen.recipe

import dev.amble.lib.datagen.AmbleRecipeProvider
import dev.amble.space.api.SpaceAPI
import dev.amble.space.datagen.IXplatIngredients
import net.minecraft.core.HolderLookup
import net.minecraft.data.PackOutput
import net.minecraft.data.recipes.RecipeOutput
import java.util.concurrent.CompletableFuture

@Suppress("unused")
class SpaceXplatRecipes(
    output: PackOutput,
    future: CompletableFuture<HolderLookup.Provider>,
    private val ingredients: IXplatIngredients
) : AmbleRecipeProvider(output, future, SpaceAPI.MOD_ID) {

    override fun buildRecipes(recipeOutput: RecipeOutput) {
        // register recipes here
    }
}

