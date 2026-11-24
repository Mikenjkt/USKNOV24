package my.lspmi.usk

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.security.MessageDigest

data class User(
    val id: Long = 0,
    val username: String,
    val name: String,
    val password: String,
    val phone: String
)

class DatabaseHelper private constructor(context : Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    companion object {
        private const val DATABASE_NAME = "users.db"
        private const val DATABASE_VERSION = 1
        const val TABLE_USERS = "users"
        const val COLUMN_ID = "id"
        const val COLUMN_USERNAME = "username"
        const val COLUMN_NAME = "name"
        const val COLUMN_PASSWORD = "password"
        const val COLUMN_PHONE = "phone"

        @Volatile
        private var instance: DatabaseHelper? = null
        fun getInstance(context: Context): DatabaseHelper {
            return instance ?: synchronized(this) {
                instance ?: DatabaseHelper(context.applicationContext).also {
                    instance = it
                    it.writableDatabase
                }
            }
        }
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTableQuery = """
            CREATE TABLE $TABLE_USERS (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_USERNAME TEXT UNIQUE NOT NULL,
                $COLUMN_NAME TEXT NOT NULL,
                $COLUMN_PASSWORD TEXT NOT NULL,
                $COLUMN_PHONE TEXT NOT NULL
            )
        """.trimIndent()
        db.execSQL(createTableQuery)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")
        onCreate(db)
    }

    fun registerUser(user: User): Boolean {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_USERNAME, user.username)
            put(COLUMN_NAME, user.name)
            put(COLUMN_PASSWORD, user.password)
            put(COLUMN_PHONE, user.phone)
        }
        val result = db.insert(TABLE_USERS, null, values)
        return result != -1L
    }

    fun isUsernameTaken(username: String): Boolean {
        val db = this.readableDatabase
        val cursor = db.query(
            TABLE_USERS,
            arrayOf(COLUMN_ID),
            "$COLUMN_USERNAME = ?",
            arrayOf(username),
            null,
            null,
            null
        )
        val exists = cursor.count > 0
        cursor.close()
        return exists
    }

    fun loginUser(key: String, password: String): User? {
        val db = this.readableDatabase
        val cursor = db.query(
            TABLE_USERS,
            arrayOf(
                COLUMN_ID,
                COLUMN_USERNAME,
                COLUMN_NAME,
                COLUMN_PASSWORD,
                COLUMN_PHONE
            ),
            "$COLUMN_USERNAME = ? AND $COLUMN_PASSWORD = ?",
            arrayOf(key, password),
            null,
            null,
            null
        )

        var user: User? = null
        if (cursor.moveToFirst()) {
            val id = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID))
            val userStr = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USERNAME))
            val name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME))
            val pass = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PASSWORD))
            val phone = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PHONE))
            user = User(id, userStr, name, pass, phone)
        }
        cursor.close()

        return user
    }

    fun getUserByUsername(username: String): User? {
        val db = this.readableDatabase
        val cursor = db.query(
            TABLE_USERS,
            arrayOf(
                COLUMN_ID,
                COLUMN_USERNAME,
                COLUMN_NAME,
                COLUMN_PASSWORD,
                COLUMN_PHONE
            ),
            "$COLUMN_USERNAME = ?",
            arrayOf(username),
            null,
            null,
            null
        )

        var user: User? = null
        if (cursor.moveToFirst()) {
            val id = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID))
            val userStr = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USERNAME))
            val name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME))
            val pass = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PASSWORD))
            val phone = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PHONE))
            user = User(id, userStr, name, pass, phone)
        }
        cursor.close()
        return user
    }

    fun updateUser(
        username: String,
        newName: String,
        newPhone: String
    ): Boolean {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_NAME, newName)
            put(COLUMN_PHONE, newPhone)
        }
        val rows = db.update(TABLE_USERS, values, "$COLUMN_USERNAME = ?", arrayOf(username))
        return rows > 0
    }
}

fun String.hashWith(algorithm: String): String {
    val bytes = MessageDigest
        .getInstance(algorithm)
        .digest(this.toByteArray(Charsets.UTF_8))
    return bytes.joinToString("") { "%02x".format(it) }
}