package com.noshricardo.dynamictweaker.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapelessRecipe;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DatapackRecipeWriter {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static void saveRecipeToDatapack(MinecraftServer server, ResourceLocation recipeId, Recipe<?> recipe) {
        try {
            Path worldDir = server.getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT);
            Path datapackDir = worldDir.resolve("datapacks").resolve("dynamic_tweaker_generated");

            ensurePackMetaExists(datapackDir);

            Path recipeJsonPath = datapackDir
                    .resolve("data")
                    .resolve(recipeId.getNamespace())
                    .resolve("recipe")
                    .resolve(recipeId.getPath() + ".json");

            Files.createDirectories(recipeJsonPath.getParent());

            JsonObject json = new JsonObject();
            var registryAccess = server.registryAccess();
            var ops = RegistryOps.create(JsonOps.INSTANCE, registryAccess);

            if (recipe instanceof ShapedRecipe shaped) {
                json.addProperty("type", "minecraft:crafting_shaped");
                try {
                    json.addProperty("category", shaped.category().getSerializedName());
                } catch (Exception e) {
                    json.addProperty("category", "misc");
                }

                int width = shaped.getWidth();
                int height = shaped.getHeight();
                List<Ingredient> ingredients = shaped.getIngredients();

                JsonArray patternArr = new JsonArray();
                JsonObject keyObj = new JsonObject();
                Map<String, Character> ingredientToChar = new HashMap<>();
                char currentChar = 'A';

                for (int h = 0; h < height; h++) {
                    StringBuilder rowBuilder = new StringBuilder();
                    for (int w = 0; w < width; w++) {
                        int index = h * width + w;
                        if (index < ingredients.size()) {
                            Ingredient ing = ingredients.get(index);
                            if (ing.isEmpty()) {
                                rowBuilder.append(' ');
                            } else {
                                var encodedIng = Ingredient.CODEC.encodeStart(ops, ing);
                                String ingKey = encodedIng.result().isPresent() ? encodedIng.result().get().toString() : String.valueOf(index);

                                if (!ingredientToChar.containsKey(ingKey)) {
                                    if (currentChar > 'Z') currentChar = 'a';
                                    char symbol = currentChar++;
                                    ingredientToChar.put(ingKey, symbol);
                                    keyObj.add(String.valueOf(symbol), encodedIng.result().orElse(new JsonObject()));
                                }
                                rowBuilder.append(ingredientToChar.get(ingKey));
                            }
                        } else {
                            rowBuilder.append(' ');
                        }
                    }
                    patternArr.add(rowBuilder.toString());
                }
                json.add("pattern", patternArr);
                json.add("key", keyObj);

                ItemStack resultStack = shaped.getResultItem(registryAccess);
                var encodedResult = ItemStack.CODEC.encodeStart(ops, resultStack);
                if (encodedResult.result().isPresent()) {
                    json.add("result", encodedResult.result().get());
                }

            } else if (recipe instanceof ShapelessRecipe shapeless) {
                json.addProperty("type", "minecraft:crafting_shapeless");
                try {
                    json.addProperty("category", shapeless.category().getSerializedName());
                } catch (Exception e) {
                    json.addProperty("category", "misc");
                }

                JsonArray ingredientsArr = new JsonArray();
                for (Ingredient ingredient : shapeless.getIngredients()) {
                    var encodedIng = Ingredient.CODEC.encodeStart(ops, ingredient);
                    if (encodedIng.result().isPresent()) {
                        ingredientsArr.add(encodedIng.result().get());
                    }
                }
                json.add("ingredients", ingredientsArr);

                ItemStack resultStack = shapeless.getResultItem(registryAccess);
                var encodedResult = ItemStack.CODEC.encodeStart(ops, resultStack);
                if (encodedResult.result().isPresent()) {
                    json.add("result", encodedResult.result().get());
                }

            } else {
                // Safe generic codec fallback for any other custom recipe types
                try {
                    @SuppressWarnings({"rawtypes", "unchecked"})
                    var rawCodec = (com.mojang.serialization.Codec<Recipe<?>>) (Object) Recipe.CODEC;
                    var dataResult = rawCodec.encodeStart(ops, recipe);
                    if (dataResult.result().isPresent()) {
                        json = (JsonObject) dataResult.result().get();
                    }
                } catch (Exception ignored) {}

                var serializer = recipe.getSerializer();
                if (!json.has("type") && serializer != null) {
                    ResourceLocation serializerId = BuiltInRegistries.RECIPE_SERIALIZER.getKey(serializer);
                    if (serializerId != null) {
                        json.addProperty("type", serializerId.toString());
                    }
                }
            }

            try (FileWriter writer = new FileWriter(recipeJsonPath.toFile())) {
                GSON.toJson(json, writer);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void removeRecipeFromDatapack(MinecraftServer server, ResourceLocation recipeId) {
        try {
            Path worldDir = server.getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT);
            Path datapackDir = worldDir.resolve("datapacks").resolve("dynamic_tweaker_generated");

            ensurePackMetaExists(datapackDir);

            Path recipeJsonPath = datapackDir
                    .resolve("data")
                    .resolve(recipeId.getNamespace())
                    .resolve("recipe")
                    .resolve(recipeId.getPath() + ".json");

            Files.createDirectories(recipeJsonPath.getParent());

            JsonObject nullifiedRecipe = new JsonObject();
            nullifiedRecipe.addProperty("type", "minecraft:crafting_shapeless");
            nullifiedRecipe.addProperty("category", "misc");

            com.google.gson.JsonArray ingredients = new com.google.gson.JsonArray();
            JsonObject barrierIngredient = new JsonObject();
            barrierIngredient.addProperty("item", "minecraft:barrier");
            ingredients.add(barrierIngredient);
            nullifiedRecipe.add("ingredients", ingredients);

            JsonObject resultObj = new JsonObject();
            resultObj.addProperty("id", "minecraft:barrier");
            resultObj.addProperty("count", 0);
            nullifiedRecipe.add("result", resultObj);

            try (FileWriter writer = new FileWriter(recipeJsonPath.toFile())) {
                GSON.toJson(nullifiedRecipe, writer);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void ensurePackMetaExists(Path datapackDir) throws IOException {
        Path metaPath = datapackDir.resolve("pack.mcmeta");
        if (!Files.exists(metaPath)) {
            Files.createDirectories(datapackDir);
            JsonObject packRoot = new JsonObject();
            JsonObject packObj = new JsonObject();
            packObj.addProperty("pack_format", 48);
            packObj.addProperty("description", "Auto-generated persistent recipes by DynamicTweaker");
            packRoot.add("pack", packObj);

            try (FileWriter writer = new FileWriter(metaPath.toFile())) {
                GSON.toJson(packRoot, writer);
            }
        }
    }

    private static JsonObject serializeRecipe(MinecraftServer server, ResourceLocation id, Recipe<?> recipe) {
        try {
            JsonObject json = new JsonObject();
            var registryAccess = server.registryAccess();
            var ops = RegistryOps.create(JsonOps.INSTANCE, registryAccess);

            if (recipe instanceof ShapedRecipe shaped) {
                json.addProperty("type", "minecraft:crafting_shaped");
                try {
                    json.addProperty("category", shaped.category().getSerializedName());
                } catch (Exception ignored) {
                    json.addProperty("category", "misc");
                }

                // Serialize result item stack
                ItemStack resultStack = shaped.getResultItem(registryAccess);
                var encodedResult = ItemStack.CODEC.encodeStart(ops, resultStack);
                if (encodedResult.result().isPresent()) {
                    json.add("result", encodedResult.result().get());
                }
                return json;

            } else if (recipe instanceof ShapelessRecipe shapeless) {
                json.addProperty("type", "minecraft:crafting_shapeless");
                try {
                    json.addProperty("category", shapeless.category().getSerializedName());
                } catch (Exception ignored) {
                    json.addProperty("category", "misc");
                }

                com.google.gson.JsonArray ingredientsArr = new com.google.gson.JsonArray();
                for (Ingredient ingredient : shapeless.getIngredients()) {
                    var encodedIng = Ingredient.CODEC.encodeStart(ops, ingredient);
                    if (encodedIng.result().isPresent()) {
                        ingredientsArr.add(encodedIng.result().get());
                    }
                }
                json.add("ingredients", ingredientsArr);

                ItemStack resultStack = shapeless.getResultItem(registryAccess);
                var encodedResult = ItemStack.CODEC.encodeStart(ops, resultStack);
                if (encodedResult.result().isPresent()) {
                    json.add("result", encodedResult.result().get());
                }
                return json;
            }

            // Generic fallback for any other custom recipe type via serializer codec
            var serializer = recipe.getSerializer();
            if (serializer != null && serializer.codec() != null) {
                var codec = serializer.codec().codec();
                @SuppressWarnings({"rawtypes", "unchecked"})
                com.mojang.serialization.Codec<Recipe<?>> rawCodec = (com.mojang.serialization.Codec<Recipe<?>>) (Object) codec;
                var result = rawCodec.encodeStart(ops, recipe);
                if (result.result().isPresent()) {
                    JsonObject encoded = (JsonObject) result.result().get();
                    ResourceLocation serializerId = BuiltInRegistries.RECIPE_SERIALIZER.getKey(serializer);
                    if (serializerId != null && !encoded.has("type")) {
                        encoded.addProperty("type", serializerId.toString());
                    }
                    return encoded;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}