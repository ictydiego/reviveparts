package br.unasp.reviveparts.data.repo

import android.util.Patterns
import br.unasp.reviveparts.data.db.dao.UserDao
import br.unasp.reviveparts.data.db.entities.UserEntity
import br.unasp.reviveparts.domain.model.Role
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

class UserRepository(
    private val dao: UserDao,
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {
    private val ownerEmail = "dono@reviveparts.com"
    private val ownerPassword = "dono123"
    private val usersCollection get() = firestore.collection("users")

    suspend fun login(email: String, password: String): Result<UserEntity> = try {
        val normEmail = email.trim().lowercase()
        validateLogin(normEmail, password)
        val firebaseUser = signInOrCreateSeedOwner(normEmail, password)
        Result.success(
            syncFirebaseUser(
                uid = firebaseUser.uid,
                email = firebaseUser.email ?: normEmail,
                name = firebaseUser.displayName.orEmpty()
            )
        )
    } catch (t: Throwable) {
        Result.failure(readableAuthError(t))
    }

    suspend fun register(
        name: String,
        email: String,
        password: String,
        phone: String,
        cpf: String,
        address: String
    ): Result<UserEntity> = try {
        val normEmail = email.trim().lowercase()
        val cleanPhone = phone.filter { it.isDigit() }
        val cleanCpf = cpf.filter { it.isDigit() }
        validateRegister(name, normEmail, password, cleanPhone, cleanCpf)

        val firebaseUser = auth.createUserWithEmailAndPassword(normEmail, password).await().user
            ?: return Result.failure(IllegalStateException("Nao foi possivel criar este usuario"))

        Result.success(
            syncFirebaseUser(
                uid = firebaseUser.uid,
                email = normEmail,
                name = name,
                phone = cleanPhone,
                cpf = cleanCpf,
                address = address
            )
        )
    } catch (t: Throwable) {
        Result.failure(readableAuthError(t))
    }

    suspend fun findById(id: Long) = dao.findById(id)
    fun observeById(id: Long) = dao.observeById(id)

    suspend fun findCloudByUid(uid: String): UserEntity? {
        if (uid.isBlank()) return null
        val doc = runCatching { usersCollection.document(uid).get().await() }.getOrNull() ?: return null
        if (!doc.exists()) return null
        val role = doc.getString("role")?.let { runCatching { Role.valueOf(it) }.getOrNull() } ?: Role.CUSTOMER
        return UserEntity(
            id = 0,
            name = doc.getString("name").orEmpty(),
            email = doc.getString("email").orEmpty(),
            password = "",
            phone = doc.getString("phone").orEmpty(),
            cpf = doc.getString("cpf").orEmpty(),
            address = doc.getString("address").orEmpty(),
            role = role,
            firebaseUid = uid
        )
    }

    suspend fun update(u: UserEntity) {
        dao.update(u)
        if (u.firebaseUid.isNotBlank()) {
            runCatching {
                usersCollection.document(u.firebaseUid)
                    .set(u.toCloudMap(), SetOptions.merge())
                    .await()
            }
        }
    }

    fun hasActiveFirebaseUser(firebaseUid: String): Boolean =
        firebaseUid.isNotBlank() && auth.currentUser?.uid == firebaseUid

    private suspend fun signInOrCreateSeedOwner(email: String, password: String) =
        try {
            auth.signInWithEmailAndPassword(email, password).await().user
                ?: throw IllegalStateException("Nao foi possivel autenticar este usuario")
        } catch (t: Throwable) {
            if (email == ownerEmail && password == ownerPassword) {
                auth.createUserWithEmailAndPassword(email, password).await().user
                    ?: throw IllegalStateException("Nao foi possivel criar o usuario dono")
            } else {
                throw t
            }
        }

    private suspend fun syncFirebaseUser(
        uid: String,
        email: String,
        name: String = "",
        phone: String = "",
        cpf: String = "",
        address: String = ""
    ): UserEntity {
        val normEmail = email.trim().lowercase()
        val doc = runCatching { usersCollection.document(uid).get().await() }.getOrNull()
        val role = doc.getString("role")?.let { value ->
            runCatching { Role.valueOf(value) }.getOrNull()
        } ?: roleFor(normEmail)

        val cloudName = doc.getString("name").orEmpty()
        val cloudPhone = doc.getString("phone").orEmpty()
        val cloudCpf = doc.getString("cpf").orEmpty()
        val cloudAddress = doc.getString("address").orEmpty()

        val local = dao.findByFirebaseUid(uid) ?: dao.findByEmail(normEmail)
        val entity = UserEntity(
            id = local?.id ?: 0,
            name = name.trim().ifBlank { cloudName.ifBlank { normEmail.substringBefore("@") } },
            email = normEmail,
            password = "",
            phone = phone.ifBlank { cloudPhone },
            cpf = cpf.ifBlank { cloudCpf },
            address = address.trim().ifBlank { cloudAddress },
            role = role,
            firebaseUid = uid
        )

        runCatching {
            usersCollection.document(uid).set(entity.toCloudMap(), SetOptions.merge()).await()
        }

        val id = if (local == null) {
            dao.insert(entity)
        } else {
            dao.update(entity.copy(id = local.id))
            local.id
        }
        return dao.findById(id) ?: entity.copy(id = id)
    }

    private fun validateLogin(email: String, password: String) {
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            throw IllegalArgumentException("Informe um e-mail valido")
        }
        if (password.isBlank()) {
            throw IllegalArgumentException("Informe a senha")
        }
    }

    private fun validateRegister(
        name: String,
        email: String,
        password: String,
        phone: String,
        cpf: String
    ) {
        if (name.trim().length < 3) {
            throw IllegalArgumentException("Informe o nome completo")
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            throw IllegalArgumentException("Informe um e-mail valido")
        }
        if (password.length < 6) {
            throw IllegalArgumentException("A senha precisa ter pelo menos 6 caracteres")
        }
        if (phone.isNotBlank() && phone.length !in 10..11) {
            throw IllegalArgumentException("Telefone invalido")
        }
        if (cpf.isNotBlank() && cpf.length != 11) {
            throw IllegalArgumentException("CPF invalido")
        }
    }

    private fun roleFor(email: String): Role =
        if (email == ownerEmail) Role.OWNER else Role.CUSTOMER

    private fun DocumentSnapshot?.getString(field: String): String? = this?.getString(field)

    private fun UserEntity.toCloudMap(): Map<String, Any> = mapOf(
        "name" to name,
        "email" to email,
        "phone" to phone,
        "cpf" to cpf,
        "address" to address,
        "role" to role.name,
        "updatedAt" to System.currentTimeMillis()
    )

    private fun readableAuthError(t: Throwable): Throwable = when (t) {
        is IllegalArgumentException, is IllegalStateException -> t
        is FirebaseAuthWeakPasswordException ->
            IllegalArgumentException("A senha precisa ter pelo menos 6 caracteres")
        is FirebaseAuthUserCollisionException ->
            IllegalStateException("E-mail ja cadastrado")
        is FirebaseAuthInvalidUserException ->
            IllegalArgumentException("E-mail nao encontrado")
        is FirebaseAuthInvalidCredentialsException ->
            IllegalArgumentException("E-mail ou senha invalidos")
        is FirebaseNetworkException ->
            IllegalStateException("Sem conexao com o Firebase. Verifique a internet e tente novamente")
        else ->
            IllegalStateException(t.message ?: "Falha na autenticacao")
    }
}
