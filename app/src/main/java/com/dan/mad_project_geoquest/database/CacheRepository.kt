package com.dan.mad_project_geoquest.database

import android.content.Context
import kotlinx.coroutines.flow.Flow

class CacheRepository(context: Context) {

    private val dao = CacheDatabase.getDatabase(context).cacheDao()

    val foundCaches: Flow<List<Cache>> = dao.getFoundCaches()
    val allCaches: Flow<List<Cache>> = dao.getAllCaches()

    suspend fun seedIfEmpty() {
        if (dao.getCount() == 0) {
            dao.insertAll(kingstonCaches)
        }
    }

    suspend fun markAsFound(cacheId: Int) {
        dao.markAsFound(cacheId)
    }

    suspend fun markNotificationSent(cacheId: Int) {
        dao.markNotificationSent(cacheId)
    }

    private val kingstonCaches = listOf(
        Cache(title = "Market Place Mystery", description = "A cache hidden in the heart of Kingston's ancient market.", latitude = 51.4123, longitude = -0.3007, clue = "Look where traders have stood for centuries", points = 100),
        Cache(title = "Riverside Riddle", description = "Along the Thames, something waits near the water.", latitude = 51.4098, longitude = -0.3063, clue = "Facing the river, check what's beneath the benches", points = 150),
        Cache(title = "KU Penrhyn Secret", description = "Hidden near Kingston University's Penrhyn Road campus.", latitude = 51.4102, longitude = -0.3064, clue = "Near the main entrance, under something green", points = 125),
        Cache(title = "All Saints Enigma", description = "The old church holds a secret nearby.", latitude = 51.4118, longitude = -0.3033, clue = "Walk around the church walls and look low", points = 200),
        Cache(title = "Charter Quay Cache", description = "Down by the waterfront development.", latitude = 51.4087, longitude = -0.3071, clue = "Where boats once docked, near the iron post", points = 175),
        Cache(title = "Clattern Bridge Find", description = "One of the oldest bridges in Surrey hides a secret.", latitude = 51.4108, longitude = -0.3042, clue = "Ancient stones, modern secret — look downward", points = 250)
    )
}