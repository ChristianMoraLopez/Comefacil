package com.christian.nutriplan.network

import com.christian.nutriplan.models.Menu
import com.christian.nutriplan.models.SeleccionIngrediente

class MenuRepository : BaseRepository() {
    suspend fun getAllMenus(token: String): Result<List<Menu>> = 
        getRequest("/menus", token)

    suspend fun getMenu(id: Int, token: String): Result<Menu> = 
        getRequest("/menus/$id", token)

    suspend fun createMenu(menu: Menu, token: String): Result<Menu> = 
        postRequest("/menus", menu, token)

    suspend fun updateMenu(id: Int, menu: Menu, token: String): Result<Menu> = 
        putRequest("/menus/$id", menu, token)

    suspend fun deleteMenu(id: Int, token: String): Result<Unit> = 
        deleteRequest("/menus/$id", token)

    suspend fun getMenuIngredients(menuId: Int, token: String): Result<List<SeleccionIngrediente>> = 
        getRequest("/menus/$menuId/ingredientes", token)

    suspend fun addIngredientToMenu(
        menuId: Int, 
        selection: SeleccionIngrediente, 
        token: String
    ): Result<SeleccionIngrediente> = 
        postRequest("/menus/$menuId/ingredientes", selection, token)

    suspend fun updateMenuIngredient(
        menuId: Int,
        selectionId: Int,
        selection: SeleccionIngrediente,
        token: String
    ): Result<SeleccionIngrediente> = 
        putRequest("/menus/$menuId/ingredientes/$selectionId", selection, token)

    suspend fun removeIngredientFromMenu(menuId: Int, selectionId: Int, token: String): Result<Unit> = 
        deleteRequest("/menus/$menuId/ingredientes/$selectionId", token)
}
