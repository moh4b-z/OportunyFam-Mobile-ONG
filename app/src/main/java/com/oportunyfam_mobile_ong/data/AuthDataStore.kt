package com.oportunyfam_mobile_ong.data

import android.content.Context
import androidx.room.*
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.oportunyfam_mobile_ong.model.Instituicao
import com.oportunyfam_mobile_ong.model.ConversaInstituicao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// =================================================================
// 1. ENTIDADE ROOM (Tabela SQLite)
// =================================================================

/**
 * Entidade única para armazenar o estado de autenticação da Instituição.
 * Usamos 'instituicaoJson' para armazenar o objeto serializado.
 */
@Entity(tableName = "instituicao_auth_state")
data class InstituicaoAuthEntity(
    @PrimaryKey(autoGenerate = false)
    val id: Int = 1, // ID fixo para garantir que haja apenas uma linha de estado de login
    @ColumnInfo(name = "instituicao_json")
    val instituicaoJson: String
    // Não precisamos de user_type, pois só há um tipo (Instituicao)
)

/**
 * Entidade para armazenar cache de conversas
 */
@Entity(tableName = "conversas_cache")
data class ConversasCacheEntity(
    @PrimaryKey(autoGenerate = false)
    val pessoaId: Int,
    @ColumnInfo(name = "conversas_json")
    val conversasJson: String,
    @ColumnInfo(name = "ultima_atualizacao")
    val ultimaAtualizacao: Long = System.currentTimeMillis()
)

// =================================================================
// 2. DATA ACCESS OBJECT (DAO)
// =================================================================

@Dao
interface InstituicaoAuthDao {
    /**
     * Insere ou substitui o estado de autenticação.
     * Como o id é sempre 1, ele substitui o registro anterior (Upsert).
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAuth(entity: InstituicaoAuthEntity)

    /**
     * Busca o estado de autenticação (a única linha com ID = 1).
     */
    @Query("SELECT * FROM instituicao_auth_state WHERE id = 1")
    suspend fun getAuth(): InstituicaoAuthEntity?

    /**
     * Limpa o estado de autenticação (simplesmente deleta a linha).
     */
    @Query("DELETE FROM instituicao_auth_state")
    suspend fun clearAuth()
}

@Dao
interface ConversasCacheDao {
    /**
     * Insere ou substitui o cache de conversas para uma pessoa
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConversasCache(entity: ConversasCacheEntity)

    /**
     * Busca o cache de conversas de uma pessoa específica
     */
    @Query("SELECT * FROM conversas_cache WHERE pessoaId = :pessoaId")
    suspend fun getConversasCache(pessoaId: Int): ConversasCacheEntity?

    /**
     * Limpa o cache de conversas de uma pessoa
     */
    @Query("DELETE FROM conversas_cache WHERE pessoaId = :pessoaId")
    suspend fun clearConversasCache(pessoaId: Int)

    /**
     * Limpa todo o cache de conversas
     */
    @Query("DELETE FROM conversas_cache")
    suspend fun clearAllCache()
}

// =================================================================
// 3. DATABASE ROOM
// =================================================================

@Database(
    entities = [InstituicaoAuthEntity::class, ConversasCacheEntity::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun instituicaoAuthDao(): InstituicaoAuthDao
    abstract fun conversasCacheDao(): ConversasCacheDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        private const val DATABASE_NAME = "oportunyfam_instituicao_db"

        fun getDatabase(context: Context): AppDatabase {
            // Usa o padrão Singleton para obter uma única instância do banco de dados
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                )
                    .fallbackToDestructiveMigration() // Permite recriar DB em mudanças de versão
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

// =================================================================
// 4. DATA STORE (Lógica de Persistência com Room)
// =================================================================

/**
 * Utilitário para salvar e carregar dados da Instituição logada
 * usando a biblioteca Room (SQLite).
 */
class InstituicaoAuthDataStore(context: Context) {

    // Inicializa o DAO através da instância única do banco de dados
    private val authDao = AppDatabase.getDatabase(context).instituicaoAuthDao()
    private val conversasDao = AppDatabase.getDatabase(context).conversasCacheDao()
    private val gson = Gson()

    /**
     * Salva o objeto Instituicao no banco de dados, serializando-o para JSON.
     * O salvamento é feito de forma segura em uma thread de I/O.
     * @param instituicao O objeto Instituicao a ser salvo.
     */
    suspend fun saveInstituicao(instituicao: Instituicao) = withContext(Dispatchers.IO) {
        val json = gson.toJson(instituicao)
        val entity = InstituicaoAuthEntity(
            instituicaoJson = json
        )
        authDao.insertAuth(entity)
    }

    /**
     * Carrega o objeto Instituicao do banco de dados.
     * @return O objeto Instituicao se encontrado, ou null se não houver dados.
     */
    suspend fun loadInstituicao(): Instituicao? = withContext(Dispatchers.IO) {
        val authEntity = authDao.getAuth() ?: return@withContext null
        val json = authEntity.instituicaoJson

        return@withContext try {
            // Deserializa o JSON para a classe Instituicao
            gson.fromJson(json, Instituicao::class.java)
        } catch (e: JsonSyntaxException) {
            e.printStackTrace()
            // Limpa dados corrompidos para garantir que o estado é limpo
            authDao.clearAuth()
            null
        }
    }

    /**
     * Verifica se há uma instituição logada.
     * @return true se houver dados salvos, false caso contrário.
     */
    suspend fun isUserLoggedIn(): Boolean = withContext(Dispatchers.IO) {
        return@withContext authDao.getAuth() != null
    }

    /**
     * Limpa todos os dados de autenticação (Logout).
     */
    suspend fun logout() = withContext(Dispatchers.IO) {
        authDao.clearAuth()
    }

    /**
     * Salva o cache de conversas para uma pessoa.
     */
    suspend fun saveConversasCache(pessoaId: Int, conversas: List<ConversaInstituicao>) = withContext(Dispatchers.IO) {
        val json = gson.toJson(conversas)
        val entity = ConversasCacheEntity(
            pessoaId = pessoaId,
            conversasJson = json
        )
        conversasDao.insertConversasCache(entity)
    }

    /**
     * Carrega o cache de conversas para uma pessoa.
     */
    suspend fun loadConversasCache(pessoaId: Int): List<ConversaInstituicao>? = withContext(Dispatchers.IO) {
        val cacheEntity = conversasDao.getConversasCache(pessoaId) ?: return@withContext null
        val json = cacheEntity.conversasJson

        return@withContext try {
            // Deserializa o JSON para a lista de ConversaInstituicao
            gson.fromJson(json, Array<ConversaInstituicao>::class.java)?.toList()
        } catch (e: JsonSyntaxException) {
            e.printStackTrace()
            // Em caso de erro, limpa o cache corrompido
            conversasDao.clearConversasCache(pessoaId)
            null
        }
    }

    /**
     * Limpa o cache de conversas de uma pessoa.
     */
    suspend fun clearConversasCache(pessoaId: Int) = withContext(Dispatchers.IO) {
        conversasDao.clearConversasCache(pessoaId)
    }

    /**
     * Limpa todo o cache de conversas.
     */
    suspend fun clearAllConversasCache() = withContext(Dispatchers.IO) {
        conversasDao.clearAllCache()
    }
}