package com.example.data

import android.content.Context
import androidx.room.*
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.oportunyfam_mobile.model.Instituicao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// =================================================================
// 1. ENTIDADE ROOM (Tabela SQLite) - ATUALIZADA
// =================================================================

/**
 * Entidade para armazenar o estado de autenticação da Instituição
 * Inclui dados da instituição e tokens de acesso
 */
@Entity(tableName = "instituicao_auth_state")
data class InstituicaoAuthEntity(
    @PrimaryKey(autoGenerate = false)
    val id: Int = 1, // ID fixo para garantir que haja apenas uma linha
    @ColumnInfo(name = "instituicao_json")
    val instituicaoJson: String,
    @ColumnInfo(name = "access_token")
    val accessToken: String? = null,
    @ColumnInfo(name = "token_expiry")
    val tokenExpiry: Long? = null, // Timestamp de expiração
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)

// =================================================================
// 2. DATA ACCESS OBJECT (DAO) - ATUALIZADO
// =================================================================

@Dao
interface InstituicaoAuthDao {
    /**
     * Insere ou substitui o estado de autenticação completo
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAuth(entity: InstituicaoAuthEntity)

    /**
     * Busca o estado de autenticação completo
     */
    @Query("SELECT * FROM instituicao_auth_state WHERE id = 1")
    suspend fun getAuth(): InstituicaoAuthEntity?

    /**
     * Busca apenas o access token
     */
    @Query("SELECT access_token FROM instituicao_auth_state WHERE id = 1")
    suspend fun getAccessToken(): String?

    /**
     * Atualiza apenas o access token
     */
    @Query("UPDATE instituicao_auth_state SET access_token = :token, token_expiry = :expiry WHERE id = 1")
    suspend fun updateAccessToken(token: String?, expiry: Long?)

    /**
     * Verifica se existe auth válido (não expirado)
     */
    @Query("SELECT COUNT(*) > 0 FROM instituicao_auth_state WHERE id = 1 AND (token_expiry IS NULL OR token_expiry > :currentTime)")
    suspend fun hasValidAuth(currentTime: Long = System.currentTimeMillis()): Boolean

    /**
     * Limpa o estado de autenticação
     */
    @Query("DELETE FROM instituicao_auth_state")
    suspend fun clearAuth()
}

// =================================================================
// 3. DATABASE ROOM - ATUALIZADO
// =================================================================

@Database(
    entities = [InstituicaoAuthEntity::class],
    version = 2, // Incrementado devido às mudanças na entidade
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun instituicaoAuthDao(): InstituicaoAuthDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        private const val DATABASE_NAME = "oportunyfam_instituicao_db"

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                )
                .fallbackToDestructiveMigration() // Para migração automática
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

// =================================================================
// 4. DATA STORE - APRIMORADO
// =================================================================

/**
 * Utilitário aprimorado para gerenciar autenticação de Instituições
 * com suporte completo a tokens JWT
 */
class InstituicaoAuthDataStore(context: Context) {

    private val authDao = AppDatabase.getDatabase(context).instituicaoAuthDao()
    private val gson = Gson()

    companion object {
        // StateFlow compartilhado entre todas as instâncias
        private val instituicaoFlow = MutableStateFlow<Instituicao?>(null)
    }

    init {
        // Carrega valor inicial em background se ainda não carregado
        CoroutineScope(Dispatchers.IO).launch {
            if (instituicaoFlow.value == null) {
                try {
                    android.util.Log.d("AuthDataStore", "Carregando instituição do banco...")
                    val entity = authDao.getAuth()
                    val loaded = entity?.let { gson.fromJson(it.instituicaoJson, Instituicao::class.java) }
                    instituicaoFlow.value = loaded
                    android.util.Log.d("AuthDataStore", "Instituição carregada: ${loaded?.nome ?: "null"}")
                } catch (e: Exception) {
                    android.util.Log.e("AuthDataStore", "Erro ao carregar instituição: ${e.message}", e)
                }
            }
        }
    }

    /**
     * Salva dados completos de login (instituição + token)
     */
    suspend fun saveLoginData(
        instituicao: Instituicao,
        accessToken: String? = null,
        tokenExpiryMinutes: Int = 15 // JWT geralmente expira em 15 minutos
    ) = withContext(Dispatchers.IO) {
        val json = gson.toJson(instituicao)
        val expiry = if (accessToken != null) {
            System.currentTimeMillis() + (tokenExpiryMinutes * 60 * 1000)
        } else null

        val entity = InstituicaoAuthEntity(
            instituicaoJson = json,
            accessToken = accessToken,
            tokenExpiry = expiry
        )
        authDao.insertAuth(entity)
        // Atualiza o flow compartilhado para observadores
        InstituicaoAuthDataStore.instituicaoFlow.value = instituicao
    }

    /**
     * Salva apenas a instituição (compatibilidade com código existente)
     */
    suspend fun saveInstituicao(instituicao: Instituicao) =
        saveLoginData(instituicao)

    /**
     * Expondo StateFlow para coleta reativa
     */
    fun instituicaoStateFlow(): StateFlow<Instituicao?> = InstituicaoAuthDataStore.instituicaoFlow.asStateFlow()

    /**
     * Carrega os dados da instituição
     */
    suspend fun loadInstituicao(): Instituicao? = withContext(Dispatchers.IO) {
        val authEntity = authDao.getAuth() ?: return@withContext null
        val json = authEntity.instituicaoJson

        return@withContext try {
            gson.fromJson(json, Instituicao::class.java)
        } catch (e: JsonSyntaxException) {
            e.printStackTrace()
            authDao.clearAuth()
            null
        }
    }

    /**
     * Obtém o access token válido
     */
    suspend fun getAccessToken(): String? = withContext(Dispatchers.IO) {
        val entity = authDao.getAuth() ?: return@withContext null

        // Verifica se o token não expirou
        val expiry = entity.tokenExpiry
        if (expiry != null && System.currentTimeMillis() > expiry) {
            // Token expirado, limpa os dados
            authDao.clearAuth()
            return@withContext null
        }

        return@withContext entity.accessToken
    }

    /**
     * Atualiza apenas o access token
     */
    suspend fun updateAccessToken(
        token: String,
        expiryMinutes: Int = 15
    ) = withContext(Dispatchers.IO) {
        val expiry = System.currentTimeMillis() + (expiryMinutes * 60 * 1000)
        authDao.updateAccessToken(token, expiry)
    }

    /**
     * Verifica se há autenticação válida
     */
    suspend fun isUserLoggedIn(): Boolean = withContext(Dispatchers.IO) {
        return@withContext authDao.hasValidAuth()
    }

    /**
     * Verifica se há token válido
     */
    suspend fun hasValidToken(): Boolean = withContext(Dispatchers.IO) {
        getAccessToken() != null
    }

    /**
     * Logout - limpa todos os dados
     */
    suspend fun logout() = withContext(Dispatchers.IO) {
        authDao.clearAuth()
    }
}