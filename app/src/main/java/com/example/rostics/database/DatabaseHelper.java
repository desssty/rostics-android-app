package com.example.rostics.database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import com.example.rostics.utils.PasswordUtils;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "restaurant.db";
    private static final int DATABASE_VERSION = 2;

    public DatabaseHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("PRAGMA foreign_keys = ON;");
        db.execSQL("CREATE TABLE IF NOT EXISTS categories (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL UNIQUE, description TEXT);");
        db.execSQL("CREATE TABLE IF NOT EXISTS ingredients (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL, price_cents INTEGER NOT NULL DEFAULT 0, description TEXT, is_addon BOOLEAN NOT NULL DEFAULT 0);");
        db.execSQL("CREATE TABLE IF NOT EXISTS allowed_addons (id INTEGER PRIMARY KEY AUTOINCREMENT, base_ingredient_id INTEGER NOT NULL, addon_ingredient_id INTEGER NOT NULL, extra_price_cents INTEGER, UNIQUE(base_ingredient_id, addon_ingredient_id), FOREIGN KEY (base_ingredient_id) REFERENCES ingredients(id) ON DELETE CASCADE, FOREIGN KEY (addon_ingredient_id) REFERENCES ingredients(id) ON DELETE CASCADE);");
        db.execSQL("CREATE TABLE IF NOT EXISTS menu_items (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL, description TEXT, base_price_cents INTEGER NOT NULL, category_id INTEGER, available BOOLEAN NOT NULL DEFAULT 1, is_new BOOLEAN NOT NULL DEFAULT 0, for_delivery_only BOOLEAN NOT NULL DEFAULT 0, FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE SET NULL);");
        db.execSQL("CREATE TABLE IF NOT EXISTS menu_item_ingredients (id INTEGER PRIMARY KEY AUTOINCREMENT, menu_item_id INTEGER NOT NULL, ingredient_id INTEGER NOT NULL, position INTEGER DEFAULT 0, required BOOLEAN NOT NULL DEFAULT 0, FOREIGN KEY (menu_item_id) REFERENCES menu_items(id) ON DELETE CASCADE, FOREIGN KEY (ingredient_id) REFERENCES ingredients(id) ON DELETE CASCADE, UNIQUE(menu_item_id, ingredient_id));");
        db.execSQL("CREATE TABLE IF NOT EXISTS restaurants (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL, address TEXT, latitude REAL, longitude REAL, phone TEXT, opening_hours TEXT);");
        db.execSQL("CREATE TABLE IF NOT EXISTS users (id INTEGER PRIMARY KEY AUTOINCREMENT, surname TEXT, name TEXT, phone TEXT UNIQUE, email TEXT UNIQUE, password_hash TEXT NOT NULL, created_at DATETIME DEFAULT (datetime('now')));");
        db.execSQL("CREATE TABLE IF NOT EXISTS carts (id INTEGER PRIMARY KEY AUTOINCREMENT, user_id INTEGER NOT NULL, restaurant_id INTEGER, created_at DATETIME DEFAULT (datetime('now')), updated_at DATETIME DEFAULT (datetime('now')), FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE, FOREIGN KEY (restaurant_id) REFERENCES restaurants(id) ON DELETE SET NULL);");
        db.execSQL("CREATE TABLE IF NOT EXISTS cart_items (id INTEGER PRIMARY KEY AUTOINCREMENT, cart_id INTEGER NOT NULL, menu_item_id INTEGER NOT NULL, quantity INTEGER NOT NULL DEFAULT 1, note TEXT, item_price_cents INTEGER NOT NULL, FOREIGN KEY (cart_id) REFERENCES carts(id) ON DELETE CASCADE, FOREIGN KEY (menu_item_id) REFERENCES menu_items(id) ON DELETE RESTRICT);");
        db.execSQL("CREATE TABLE IF NOT EXISTS cart_item_addons (id INTEGER PRIMARY KEY AUTOINCREMENT, cart_item_id INTEGER NOT NULL, ingredient_id INTEGER NOT NULL, addon_price_cents INTEGER NOT NULL, FOREIGN KEY (cart_item_id) REFERENCES cart_items(id) ON DELETE CASCADE, FOREIGN KEY (ingredient_id) REFERENCES ingredients(id) ON DELETE RESTRICT, UNIQUE(cart_item_id, ingredient_id));");
        db.execSQL("CREATE TABLE IF NOT EXISTS order_statuses (id INTEGER PRIMARY KEY AUTOINCREMENT, code TEXT NOT NULL UNIQUE, display_name TEXT NOT NULL);");
        db.execSQL("INSERT OR IGNORE INTO order_statuses (code, display_name) VALUES ('PENDING','Оформлен'),('PREPARING','Готовится'),('READY','Готов'),('CANCELLED','Отменен');");
        db.execSQL("CREATE TABLE IF NOT EXISTS orders (id INTEGER PRIMARY KEY AUTOINCREMENT, user_id INTEGER NOT NULL, restaurant_id INTEGER NOT NULL, status_id INTEGER NOT NULL DEFAULT 1, total_price_cents INTEGER NOT NULL DEFAULT 0, created_at DATETIME DEFAULT (datetime('now')), updated_at DATETIME DEFAULT (datetime('now')), phone TEXT, note TEXT, FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL, FOREIGN KEY (restaurant_id) REFERENCES restaurants(id) ON DELETE SET NULL, FOREIGN KEY (status_id) REFERENCES order_statuses(id) ON DELETE RESTRICT);");
        db.execSQL("CREATE TABLE IF NOT EXISTS order_items (id INTEGER PRIMARY KEY AUTOINCREMENT, order_id INTEGER NOT NULL, menu_item_id INTEGER NOT NULL, quantity INTEGER NOT NULL DEFAULT 1, item_price_cents INTEGER NOT NULL, note TEXT, FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE, FOREIGN KEY (menu_item_id) REFERENCES menu_items(id) ON DELETE RESTRICT);");
        db.execSQL("CREATE TABLE IF NOT EXISTS order_item_addons (id INTEGER PRIMARY KEY AUTOINCREMENT, order_item_id INTEGER NOT NULL, ingredient_id INTEGER NOT NULL, addon_price_cents INTEGER NOT NULL, FOREIGN KEY (order_item_id) REFERENCES order_items(id) ON DELETE CASCADE, FOREIGN KEY (ingredient_id) REFERENCES ingredients(id) ON DELETE RESTRICT);");

        db.execSQL("CREATE INDEX IF NOT EXISTS idx_menu_items_category ON menu_items(category_id);");
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_cart_user ON carts(user_id);");
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_cart_items_cart ON cart_items(cart_id);");
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_cart_item_addons_cartitem ON cart_item_addons(cart_item_id);");
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_orders_user ON orders(user_id);");
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_order_status ON orders(status_id);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("PRAGMA foreign_keys = ON;");
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE users ADD COLUMN role TEXT NOT NULL DEFAULT 'user';");
            Cursor cursor = db.rawQuery("SELECT id FROM users WHERE role='admin' LIMIT 1", null);
            if (!cursor.moveToFirst()) {

                String adminPassword = "admin123";
                String hashedPassword = PasswordUtils.hashPassword(adminPassword);
                db.execSQL(
                        "INSERT INTO users (surname, name, phone, email, password_hash, role) VALUES (" +
                                "'Admin', 'Admin', '0000000000', 'admin@example.com', '" + hashedPassword + "', 'admin');"
                );
            }
            cursor.close();
        }
    }

}
