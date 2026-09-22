package top.jatus.ibkr;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

public final class ClashAppListStore extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "clash_app_list.db";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_PACKAGES = "packages";
    private static final String COLUMN_PACKAGE_NAME = "package_name";
    private static final String APP_LIST_URL =
            "https://alist.jatus.top/d/Download/config/applist.txt"
                    + "?sign=YPJiF9hmvI3GlRFck4w-_JYltu8LMcpriOUgJMGcdRI=:0";
    private static final Pattern PACKAGE_NAME_PATTERN =
            Pattern.compile("[A-Za-z][A-Za-z0-9_]*(?:\\.[A-Za-z][A-Za-z0-9_]*)+");
    private static final ExecutorService DOWNLOAD_EXECUTOR = Executors.newSingleThreadExecutor();

    public ClashAppListStore(Context context) {
        super(context.getApplicationContext(), DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static void refreshAsync(Context context) {
        Context applicationContext = context.getApplicationContext();
        DOWNLOAD_EXECUTOR.execute(() -> new ClashAppListStore(applicationContext).refresh());
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL("CREATE TABLE " + TABLE_PACKAGES + " ("
                + COLUMN_PACKAGE_NAME + " TEXT PRIMARY KEY)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        database.execSQL("DROP TABLE IF EXISTS " + TABLE_PACKAGES);
        onCreate(database);
    }

    public boolean contains(String packageName) {
        try (Cursor cursor = getReadableDatabase().query(
                TABLE_PACKAGES,
                new String[]{COLUMN_PACKAGE_NAME},
                COLUMN_PACKAGE_NAME + " = ?",
                new String[]{packageName},
                null,
                null,
                null,
                "1")) {
            return cursor.moveToFirst();
        }
    }

    private void refresh() {
        Set<String> packageNames = downloadPackageNames();
        if (packageNames.isEmpty()) {
            return;
        }

        SQLiteDatabase database = getWritableDatabase();
        database.beginTransaction();
        try {
            database.delete(TABLE_PACKAGES, null, null);
            for (String packageName : packageNames) {
                ContentValues values = new ContentValues();
                values.put(COLUMN_PACKAGE_NAME, packageName);
                database.insertOrThrow(TABLE_PACKAGES, null, values);
            }
            database.setTransactionSuccessful();
        } finally {
            database.endTransaction();
        }
    }

    private Set<String> downloadPackageNames() {
        Set<String> packageNames = new HashSet<>();
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) new URL(APP_LIST_URL).openConnection();
            connection.setConnectTimeout(10_000);
            connection.setReadTimeout(10_000);
            connection.setRequestMethod("GET");
            connection.setInstanceFollowRedirects(true);
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                return packageNames;
            }

            try (InputStream inputStream = connection.getInputStream();
                 BufferedReader reader = new BufferedReader(new InputStreamReader(
                         inputStream, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String packageName = line.trim();
                    if (PACKAGE_NAME_PATTERN.matcher(packageName).matches()) {
                        packageNames.add(packageName);
                    }
                }
            }
        } catch (Exception ignored) {
            packageNames.clear();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return packageNames;
    }
}