package com.example.gestionordenes

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.gestionordenes.databinding.ActivityProductosBinding
import com.example.gestionordenes.databinding.ItemProductoBinding

class ProductosActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProductosBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityProductosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.ivVolver.setOnClickListener { finish() }
        binding.btnAgregar.setOnClickListener {
            startActivity(Intent(this, ProductoFormActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        consultarProductos()
    }

    private fun consultarProductos() {
        binding.llProductos.removeAllViews()

        val admin = AdministradorBD(this)
        val db = admin.readableDatabase

        // CONSULTA (SELECT) de todos los productos
        val cursor = db.rawQuery("SELECT * FROM Productos ORDER BY idProducto DESC", null)

        if (cursor.count == 0) {
            binding.tvVacio.visibility = View.VISIBLE
        } else {
            binding.tvVacio.visibility = View.GONE

            while (cursor.moveToNext()) {
                val id = cursor.getInt(0)
                val nombre = cursor.getString(1)
                val precio = cursor.getDouble(2)
                val descripcion = cursor.getString(3)
                val categoria = cursor.getString(4)
                val stock = cursor.getInt(5)

                val item = ItemProductoBinding.inflate(layoutInflater, binding.llProductos, false)
                item.tvNombre.text = nombre
                item.tvPrecio.text = "B/. $precio"
                item.tvCategoria.text = categoria

                // Mostramos el stock; si es 0 lo pintamos de rojo (agotado)
                if (stock == 0) {
                    item.tvStock.text = "Sin stock"
                    item.tvStock.setTextColor(getColor(R.color.rojo))
                } else {
                    item.tvStock.text = "Stock: $stock"
                    item.tvStock.setTextColor(getColor(R.color.verde))
                }

                // Boton "+" : agrega mas unidades a este mismo producto
                item.btnAgregarStock.setOnClickListener {
                    dialogoAgregarStock(id, nombre, stock)
                }

                item.btnEditar.setOnClickListener {
                    val intent = Intent(this, ProductoFormActivity::class.java)
                    intent.putExtra("id", id)
                    intent.putExtra("nombre", nombre)
                    intent.putExtra("precio", precio.toString())
                    intent.putExtra("descripcion", descripcion)
                    intent.putExtra("categoria", categoria)
                    intent.putExtra("stock", stock.toString())
                    startActivity(intent)
                }

                item.btnEliminar.setOnClickListener {
                    val dbBorrar = AdministradorBD(this).writableDatabase
                    dbBorrar.delete("Productos", "idProducto = ?", arrayOf(id.toString()))
                    dbBorrar.close()
                    consultarProductos()
                }

                binding.llProductos.addView(item.root)
            }
        }
        cursor.close()
        db.close()
    }

    // Muestra un dialogo para agregar mas unidades al stock de un producto
    private fun dialogoAgregarStock(id: Int, nombre: String, stockActual: Int) {
        val input = EditText(this)
        input.inputType = InputType.TYPE_CLASS_NUMBER
        input.hint = "Cantidad a agregar"

        AlertDialog.Builder(this)
            .setTitle("Agregar stock: $nombre")
            .setMessage("Stock actual: $stockActual")
            .setView(input)
            .setPositiveButton("Agregar") { _, _ ->
                val cantidad = input.text.toString().trim().toIntOrNull()
                if (cantidad == null || cantidad <= 0) {
                    Toast.makeText(this, "Ingrese una cantidad valida", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                // Sumamos las unidades al stock existente (UPDATE)
                val db = AdministradorBD(this).writableDatabase
                db.execSQL(
                    "UPDATE Productos SET stock = stock + ? WHERE idProducto = ?",
                    arrayOf<Any>(cantidad, id)
                )
                db.close()
                Toast.makeText(this, "Se agregaron $cantidad unidades", Toast.LENGTH_SHORT).show()
                consultarProductos()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}
