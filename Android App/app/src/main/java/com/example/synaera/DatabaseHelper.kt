package com.example.synaera

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper


open class DatabaseHelper(context: Context?) :
    SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(CREATE_USER_TABLE)
        db.execSQL(CREATE_LOGGEDIN_TABLE)
        db.execSQL(CREATE_VIDEOS_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 6) {
            db.execSQL("DROP TABLE IF EXISTS $USER_TABLE_NAME")
            db.execSQL("DROP TABLE IF EXISTS $LOGGEDIN_TABLE_NAME")
            db.execSQL("DROP TABLE IF EXISTS $VIDEOS_TABLE_NAME")
            db.execSQL("DROP TABLE IF EXISTS COUNTRIES")
            db.execSQL(CREATE_USER_TABLE)
            db.execSQL(CREATE_LOGGEDIN_TABLE)
            db.execSQL(CREATE_VIDEOS_TABLE)
        }
    }

    companion object {
        // Table Name
        const val USER_TABLE_NAME = "USERS"
        const val LOGGEDIN_TABLE_NAME = "loggedin"
        const val VIDEOS_TABLE_NAME = "videos"

        // Table columns
        const val ID = "rowid"
        const val EMAIL = "email"
        const val NAME = "name"
        const val PASSWORD = "password"

        const val TITLE = "title"
        const val STATUS = "status"
        const val THUMBNAIL = "thumbnail"
        const val TRANSCRIPT = "transcript"
        const val DELETEMODE = "deletemode"

        // Database Information
        const val DB_NAME = "SYNAERA.DB"

        // database version
        const val DB_VERSION = 5

        // Creating table query
        private const val CREATE_USER_TABLE = ("create table $USER_TABLE_NAME($EMAIL " +
                "TEXT NOT NULL PRIMARY KEY, $NAME TEXT NOT NULL, $PASSWORD TEXT NOT NULL);")

        private const val CREATE_LOGGEDIN_TABLE = ("create table $LOGGEDIN_TABLE_NAME ($EMAIL TEXT NOT NULL PRIMARY KEY," +
                " $NAME TEXT NOT NULL, $PASSWORD TEXT NOT NULL, id INTEGER NOT NULL);")

        private const val CREATE_VIDEOS_TABLE = ("create table $VIDEOS_TABLE_NAME ($TITLE TEXT NOT NULL PRIMARY KEY," +
                " $STATUS TEXT NOT NULL, $THUMBNAIL TEXT NOT NULL, $TRANSCRIPT TEXT NOT NULL, $DELETEMODE INTEGER NOT NULL);")
    }

    open fun addUser(user: User) {
        val db = this.writableDatabase

        val values = ContentValues()
        values.put(EMAIL, user.email)
        values.put(NAME, user.name)
        values.put(PASSWORD, user.password)

        db.insert(USER_TABLE_NAME, null, values)
        db.close()

    }

    open fun addVideo(videoItem: VideoItem) {
        val db = this.writableDatabase

        val values = ContentValues()
        values.put(TITLE, videoItem.title)
        values.put(STATUS, videoItem.status)
        values.put(THUMBNAIL, videoItem.convertBitmapToString())
        values.put(TRANSCRIPT, videoItem.transcript)
        values.put(DELETEMODE, videoItem.deleteModeToInt())

        db.insert(VIDEOS_TABLE_NAME, null, values)
        db.close()

    }

    open fun addLoggedInUser(user: User) {
        val db = this.writableDatabase

        val values = ContentValues()
        values.put(EMAIL, user.email)
        values.put(NAME, user.name)
        values.put(PASSWORD, user.password)
        values.put("id", user.id)

        db.insert(LOGGEDIN_TABLE_NAME, null, values)
        db.close()
    }

    open fun drop() {
        val db = this.writableDatabase

        db.execSQL("DROP TABLE IF EXISTS $USER_TABLE_NAME")
        db.execSQL("DROP TABLE IF EXISTS $LOGGEDIN_TABLE_NAME")
        db.execSQL("DROP TABLE IF EXISTS $VIDEOS_TABLE_NAME")
    }

    open fun getUser(email: String): User {
        val db = this.readableDatabase

        val cursor: Cursor? = db.query(
            USER_TABLE_NAME,
            arrayOf(
                ID,
                EMAIL,
                NAME, PASSWORD
            ),
            "$EMAIL=?",
            arrayOf(email),
            null,
            null,
            null,
            null
        )
        cursor!!.moveToFirst()
        val user = User(cursor.getInt(0),cursor.getString(1), cursor.getString(2), cursor.getString(3))
        cursor.close()
        return user

    }

    open fun getUser(id: Int): User {
        val db = this.readableDatabase

        val cursor: Cursor? = db.query(
            USER_TABLE_NAME,
            arrayOf(
                ID,
                EMAIL,
                NAME, PASSWORD
            ),
            "$ID=?",
            arrayOf(id.toString()),
            null,
            null,
            null,
            null
        )
        cursor!!.moveToFirst()
        val user = User(cursor.getInt(0),cursor.getString(1), cursor.getString(2), cursor.getString(3))
        cursor.close()
        return user

    }

    open fun getVideo(title: String): VideoItem {
        val db = this.readableDatabase

        val cursor: Cursor? = db.query(
            VIDEOS_TABLE_NAME,
            arrayOf(
                TITLE,
                STATUS,
                THUMBNAIL, TRANSCRIPT,
                DELETEMODE
            ),
            "$TITLE=?",
            arrayOf(title),
            null,
            null,
            null,
            null
        )
        cursor!!.moveToFirst()
        val videoItem = VideoItem(cursor.getString(0),cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getInt(4))
        cursor.close()
        return videoItem

    }

    fun getAllUsers() : ArrayList<User> {
        val userList = ArrayList<User>()
        // Select All Query
        val selectQuery = "SELECT $ID, $EMAIL, $NAME, $PASSWORD FROM $USER_TABLE_NAME"

        val db = this.writableDatabase
        val cursor = db.rawQuery(selectQuery, null)

        if (cursor.moveToFirst()) {
            do {
                val user = User(cursor.getInt(0),cursor.getString(1), cursor.getString(2),
                    cursor.getString(3))

                userList.add(user)
            } while (cursor.moveToNext())

            cursor.close()
        }
        return userList
    }

    fun getAllVideos() : ArrayList<VideoItem> {
        val videoList = ArrayList<VideoItem>()
        // Select All Query
        val selectQuery = "SELECT * FROM $VIDEOS_TABLE_NAME"

        val db = this.writableDatabase
        val cursor = db.rawQuery(selectQuery, null)

        if (cursor.moveToFirst()) {
            do {
                val videoItem = VideoItem(cursor.getString(0),cursor.getString(1), cursor.getString(2),
                    cursor.getString(3), cursor.getInt(4))

                videoList.add(videoItem)
            } while (cursor.moveToNext())

            cursor.close()
        }
        return videoList
    }

    fun getLoggedIn() : User {
        var user = User (0,"", "" , "")
        // Select All Query
        val selectQuery = "SELECT * FROM loggedin"

        val db = this.writableDatabase
        val cursor = db.rawQuery(selectQuery, null)

        if (cursor.moveToFirst()) {

            user = User(
                cursor.getInt(3), cursor.getString(0),
                cursor.getString(1), cursor.getString(2)
            )
        }

        cursor.close()
        return user
    }

    open fun updateUser(user : User, id : Int) {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(NAME, user.name)
        values.put(EMAIL, user.email)

        // updating row
        db.update(
            USER_TABLE_NAME,
            values,
            "$ID = ?",
            arrayOf(id.toString())
        )

        db.close()

    }

    open fun updateVideoStatus(id : Int, status : String) {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(STATUS, status)

        // updating row
        db.update(
            VIDEOS_TABLE_NAME,
            values,
            "$ID = ?",
            arrayOf(id.toString())
        )

        db.close()

    }

    open fun updateVideoTranscript(id : Int, transcript : String) {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(TRANSCRIPT, transcript)

        // updating row
        db.update(
            VIDEOS_TABLE_NAME,
            values,
            "$ID = ?",
            arrayOf(id.toString())
        )

        db.close()

    }

    open fun updateVideoDeleteMode(id : Int, deleteMode : Int) {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(DELETEMODE, deleteMode)

        // updating row
        db.update(
            VIDEOS_TABLE_NAME,
            values,
            "$ID = ?",
            arrayOf(id.toString())
        )

        db.close()

    }

    open fun deleteUser(user: User) {
        val db = this.writableDatabase
        db.delete(
            USER_TABLE_NAME,
            "$ID = ?",
            arrayOf(user.id.toString())
        )
        db.close()
    }

    open fun deleteVideo(videoItem: VideoItem) {
        val db = this.writableDatabase
        db.delete(
            VIDEOS_TABLE_NAME,
            "$TITLE = ?",
            arrayOf(videoItem.title)
        )
        db.close()
    }

    open fun deleteLoggedInUser(user: User) {
        val db = this.writableDatabase
        db.delete(
            "loggedin",
            "$EMAIL = ?",
            arrayOf(user.email)
        )
        db.close()
    }


    open fun getUsersCount(): Int {
        val countQuery = "SELECT * FROM $USER_TABLE_NAME"
        val db = this.readableDatabase
        val cursor = db.rawQuery(countQuery, null)
        val count = cursor.count
        cursor.close()

        // return count
        return count
    }
}