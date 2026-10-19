package com.example.gestionordenes

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

// AdministradorBD crea y administra la base de datos SQLite.
// Solo crea la base y las 4 tablas relacionadas (onCreate)
// y las recrea cuando cambia la version (onUpgrade).
class AdministradorBD(context: Context) : SQLiteOpenHelper(
    context,
    "Ordenes.db",   // nombre del archivo de la base de datos
    null,
    2               // version de la base de datos (subio a 2 por la columna stock)
) {

    // onCreate se ejecuta UNA sola vez: aqui se crean las tablas
    override fun onCreate(db: SQLiteDatabase) {

        // Tabla 1: Clientes
        db.execSQL(
            """
            CREATE TABLE Clientes(
                idCliente INTEGER PRIMARY KEY AUTOINCREMENT,
                nombre TEXT NOT NULL,
                correo TEXT,
                telefono TEXT,
                direccion TEXT
            )
            """.trimIndent()
        )

        // Tabla 2: Productos
        db.execSQL(
            """
            CREATE TABLE Productos(
                idProducto INTEGER PRIMARY KEY AUTOINCREMENT,
                nombreProducto TEXT NOT NULL,
                precio REAL NOT NULL,
                descripcion TEXT,
                categoria TEXT,
                stock INTEGER DEFAULT 0
            )
            """.trimIndent()
        )

        // Tabla 3: Ordenes (relacionada con Clientes)
        db.execSQL(
            """
            CREATE TABLE Ordenes(
                idOrden INTEGER PRIMARY KEY AUTOINCREMENT,
                idCliente INTEGER,
                fecha TEXT,
                total REAL,
                FOREIGN KEY(idCliente) REFERENCES Clientes(idCliente)
            )
            """.trimIndent()
        )

        // Tabla 4: DetalleOrden (relacionada con Ordenes y Productos)
        db.execSQL(
            """
            CREATE TABLE DetalleOrden(
                idDetalle INTEGER PRIMARY KEY AUTOINCREMENT,
                idOrden INTEGER,
                idProducto INTEGER,
                cantidad INTEGER,
                subtotal REAL,
                FOREIGN KEY(idOrden) REFERENCES Ordenes(idOrden),
                FOREIGN KEY(idProducto) REFERENCES Productos(idProducto)
            )
            """.trimIndent()
        )
    }

    // onUpgrade se ejecuta cuando cambia la version de la base de datos
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS DetalleOrden")
        db.execSQL("DROP TABLE IF EXISTS Ordenes")
        db.execSQL("DROP TABLE IF EXISTS Productos")
        db.execSQL("DROP TABLE IF EXISTS Clientes")
        onCreate(db)
    }
}
