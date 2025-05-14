package lk.javainstitute.homesphre.helpers;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.ContentValues;
import android.database.Cursor;

public class AdminDbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "admin.db";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_NAME = "admin";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_EMAIL = "email";
    private static final String COLUMN_MOBILE = "mobile";
    private static final String COLUMN_PASSWORD = "password";

    public AdminDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_NAME + " TEXT,"
                + COLUMN_EMAIL + " TEXT,"
                + COLUMN_MOBILE + " TEXT,"
                + COLUMN_PASSWORD + " TEXT"
                + ")";
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public void insertAdmin(String name, String email, String mobile, String password) {
        SQLiteDatabase db = this.getWritableDatabase();

        // Check if email exists
        Cursor cursor = db.query(TABLE_NAME, null, COLUMN_EMAIL + "=?", new String[]{email}, null, null, null);
        if (cursor.getCount() > 0) {
            cursor.close();
            db.close();
            return;
        }
        cursor.close();

        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, name);
        values.put(COLUMN_EMAIL, email);
        values.put(COLUMN_MOBILE, mobile);
        values.put(COLUMN_PASSWORD, password);

        db.insert(TABLE_NAME, null, values);
        db.close();
    }

    public boolean validateAdmin(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_NAME,
                null,
                COLUMN_EMAIL + "=? AND " + COLUMN_PASSWORD + "=?",
                new String[]{email, password},
                null, null, null);

        boolean exists = (cursor.getCount() > 0);
        cursor.close();
        db.close();

        return exists;
    }

    public Admin getAdmin() {
        SQLiteDatabase db = this.getReadableDatabase();
        Admin admin = null;

        Cursor cursor = db.query("Admin", null, null, null, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
            String email = cursor.getString(cursor.getColumnIndexOrThrow("email"));
            String mobile = cursor.getString(cursor.getColumnIndexOrThrow("mobile"));
            String password = cursor.getString(cursor.getColumnIndexOrThrow("password"));

            admin = new Admin(name, email, mobile, password);

            cursor.close();
        }
        db.close();
        return admin;
    }

    public boolean updateAdmin(String email, String newName, String newEmail, String newMobile, String newPassword) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, newName);
        values.put(COLUMN_EMAIL, newEmail);
        values.put(COLUMN_MOBILE, newMobile);
        values.put(COLUMN_PASSWORD, newPassword);


        int rowsAffected = db.update(TABLE_NAME, values, COLUMN_EMAIL + "=?", new String[]{email});
        db.close();

        return rowsAffected > 0;
    }



}

