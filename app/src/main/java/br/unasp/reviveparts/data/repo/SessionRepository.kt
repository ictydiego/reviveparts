package br.unasp.reviveparts.data.repo

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import br.unasp.reviveparts.domain.model.Role
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "session")

class SessionRepository(private val ctx: Context) {
    private object Keys {
        val USER_ID = longPreferencesKey("user_id")
        val ROLE = stringPreferencesKey("role")
    }

    data class Session(val userId: Long, val role: Role)

    val session: Flow<Session?> = ctx.dataStore.data.map { p ->
        val id = p[Keys.USER_ID] ?: return@map null
        val role = p[Keys.ROLE]?.let { Role.valueOf(it) } ?: return@map null
        Session(id, role)
    }

    suspend fun current(): Session? = session.first()

    suspend fun login(userId: Long, role: Role) {
        ctx.dataStore.edit {
            it[Keys.USER_ID] = userId
            it[Keys.ROLE] = role.name
        }
    }

    suspend fun logout() {
        ctx.dataStore.edit { it.clear() }
    }
}
