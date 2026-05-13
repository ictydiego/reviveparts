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
        val FIREBASE_UID = stringPreferencesKey("firebase_uid")
        val LAST_USER_ID = longPreferencesKey("last_user_id")
        val LAST_ROLE = stringPreferencesKey("last_role")
        val LAST_FIREBASE_UID = stringPreferencesKey("last_firebase_uid")
    }

    data class Session(val userId: Long, val role: Role, val firebaseUid: String)

    val session: Flow<Session?> = ctx.dataStore.data.map { p ->
        val id = p[Keys.USER_ID] ?: return@map null
        val role = p[Keys.ROLE]?.let { Role.valueOf(it) } ?: return@map null
        Session(id, role, p[Keys.FIREBASE_UID].orEmpty())
    }

    val biometricSession: Flow<Session?> = ctx.dataStore.data.map { p ->
        val id = p[Keys.LAST_USER_ID] ?: return@map null
        val role = p[Keys.LAST_ROLE]?.let { Role.valueOf(it) } ?: return@map null
        val firebaseUid = p[Keys.LAST_FIREBASE_UID].orEmpty()
        if (firebaseUid.isBlank()) null else Session(id, role, firebaseUid)
    }

    suspend fun current(): Session? = session.first()

    suspend fun login(userId: Long, role: Role, firebaseUid: String) {
        ctx.dataStore.edit {
            it[Keys.USER_ID] = userId
            it[Keys.ROLE] = role.name
            it[Keys.FIREBASE_UID] = firebaseUid
            if (firebaseUid.isNotBlank()) {
                it[Keys.LAST_USER_ID] = userId
                it[Keys.LAST_ROLE] = role.name
                it[Keys.LAST_FIREBASE_UID] = firebaseUid
            }
        }
    }

    suspend fun restoreBiometricSession(): Session? {
        val saved = biometricSession.first() ?: return null
        login(saved.userId, saved.role, saved.firebaseUid)
        return saved
    }

    suspend fun logout() {
        ctx.dataStore.edit {
            it.remove(Keys.USER_ID)
            it.remove(Keys.ROLE)
            it.remove(Keys.FIREBASE_UID)
        }
    }
}
