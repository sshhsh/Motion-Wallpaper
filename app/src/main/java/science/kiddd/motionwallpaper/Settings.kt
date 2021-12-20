package science.kiddd.motionwallpaper

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")
val MODE_KEY = intPreferencesKey("mode")

enum class MODE {
    MOTION, SCROLL
}

suspend fun getMode(context: Context): MODE {
    var res = MODE.MOTION
    context.dataStore.data.map { preferences ->
        preferences[MODE_KEY] ?: MODE.MOTION.ordinal
    }.first().let {
        res = MODE.values().first { mode ->
            mode.ordinal == it
        }
    }
    return res
}

suspend fun setMode(context: Context, value: MODE) {
    context.dataStore.edit { settings ->
        settings[MODE_KEY] = value.ordinal
    }
}