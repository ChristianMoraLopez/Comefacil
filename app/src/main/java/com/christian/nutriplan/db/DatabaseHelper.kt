import android.content.Context
import android.util.Log
import at.favre.lib.crypto.bcrypt.BCrypt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.javatime.CurrentDateTime
import org.jetbrains.exposed.sql.transactions.transaction
import java.sql.SQLException
import org.jetbrains.exposed.sql.javatime.datetime

open class DatabaseHelper protected constructor(context: Context) {
    // Tabla Usuarios
    object Usuarios : Table("Usuarios") {
        val usuarioId = integer("usuario_id").autoIncrement()
        val nombre = varchar("nombre", 100)
        val email = varchar("email", 100).uniqueIndex()
        val contrasena = varchar("contrasena", 255)
        val aceptaTerminos = bool("acepta_terminos")
        val rol = varchar("rol", 20)
        val fechaRegistro = datetime("fecha_registro").defaultExpression(CurrentDateTime)

        override val primaryKey = PrimaryKey(usuarioId, name = "PK_Usuario_ID")
    }

    companion object {
        private const val TAG = "DatabaseHelper"
        private var instance: DatabaseHelper? = null

        private lateinit var DB_URL: String
        private lateinit var DB_USER: String
        private lateinit var DB_PASSWORD: String

        fun initDbCredentials(context: Context, user: String, password: String) {
            DB_URL = "jdbc:postgresql://us-east-1.6ee52f41-94b3-4ae4-bebf-d70946d7508e.aws.yugabyte.cloud:5433/yugabyte?sslmode=require"
            DB_USER = user
            DB_PASSWORD = password

            // Inicializa la instancia
            getInstance(context)
        }

        fun getInstance(context: Context): DatabaseHelper {
            return instance ?: synchronized(this) {
                instance ?: DatabaseHelper(context).also {
                    instance = it
                    initializeDatabase()
                }
            }
        }

        private fun initializeDatabase() {
            Database.connect(
                DB_URL,
                driver = "org.postgresql.Driver",
                user = DB_USER,
                password = DB_PASSWORD
            )

            transaction {
                SchemaUtils.createMissingTablesAndColumns(Usuarios)
            }
        }
    }

    open suspend fun registerUser(
        nombre: String,
        email: String,
        password: String,
        aceptaTerminos: Boolean
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val hashedPassword = BCrypt.withDefaults().hashToString(12, password.toCharArray())

            transaction {
                Usuarios.insert {
                    it[Usuarios.nombre] = nombre
                    it[Usuarios.email] = email
                    it[Usuarios.contrasena] = hashedPassword
                    it[Usuarios.aceptaTerminos] = aceptaTerminos
                    it[Usuarios.rol] = "usuario"
                }
            }

            Log.i(TAG, "Usuario $email registrado exitosamente")
            Result.success("Registro exitoso")
        } catch (e: SQLException) {
            val errorMsg = when {
                e.sqlState == "23505" -> {
                    Log.w(TAG, "Email ya registrado: $email")
                    "error_email_taken"
                }
                else -> {
                    Log.e(TAG, "Error de SQL: ${e.message}")
                    "error_database"
                }
            }
            Result.failure(Exception(errorMsg))
        } catch (e: Exception) {
            Log.e(TAG, "Error inesperado: ${e.message}")
            Result.failure(Exception("error_unexpected"))
        }
    }
}