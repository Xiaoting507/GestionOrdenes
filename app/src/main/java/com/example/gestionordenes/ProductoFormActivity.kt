package com.example.gestionordenes

import android.content.ContentValues
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.gestionordenes.databinding.ActivityProductoFormBinding

class ProductoFormActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProductoFormBinding

    private var idEditar = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityProductoFormBinding.inflate(layoutInflater)
        setContentView(binding.root)

        idEditar = intent.getIntExtra("id", -1)
        if (idEditar != -1) {
            binding.tvTitulo.text = "Editar Producto"
            binding.etNombre.setText(intent.getStringExtra("nombre"))
            binding.etPrecio.setText(intent.getStringExtra("precio"))
            binding.etDescripcion.setText(intent.getStringExtra("descripcion"))
            binding.etCategoria.setText(intent.getStringExtra("categoria"))
            binding.etStock.setText(intent.getStringExtra("stock"))
        }

        binding.btnGuardar.setOnClickListener { guardarProducto() }
        binding.ivVolver.setOnClickListener { finish() }
    }

    private fun guardarProducto() {
        val nombre = binding.etNombre.text.toString().trim()
        val textoPrecio = binding.etPrecio.text.toString().trim()
        val descripcion = binding.etDescripcion.text.toString().trim()
        val categoria = binding.etCategoria.text.toString().trim()
        val textoStock = binding.etStock.text.toString().trim()

        // Validacion: nombre y precio obligatorios
        if (nombre.isEmpty() || textoPrecio.isEmpty()) {
            Toast.makeText(this, "Nombre y precio son obligatorios", Toast.LENGTH_SHORT).show()
            return
        }

        // Validacion: el precio debe ser un numero mayor que cero
        val precio = textoPrecio.toDoubleOrNull()
        if (precio == null || precio <= 0) {
            Toast.makeText(this, "El precio debe ser mayor que cero", Toast.LENGTH_SHORT).show()
            return
        }

        // Validacion: el stock debe ser un numero (0 o mas). Si se deja vacio, es 0.
        val stock = if (textoStock.isEmpty()) 0 else textoStock.toIntOrNull()
        if (stock == null || stock < 0) {
            Toast.makeText(this, "El stock debe ser un numero valido", Toast.LENGTH_SHORT).show()
            return
        }

        val valores = ContentValues().apply {
            put("nombreProducto", nombre)
            put("precio", precio)
            put("descripcion", descripcion)
            put("categoria", categoria)
            put("stock", stock)
        }

        val admin = AdministradorBD(this)
        val db = admin.writableDatabase

        if (idEditar == -1) {
            db.insert("Productos", null, valores)
            Toast.makeText(this, "Producto registrado", Toast.LENGTH_SHORT).show()
        } else {
            db.update("Productos", valores, "idProducto = ?", arrayOf(idEditar.toString()))
            Toast.makeText(this, "Producto actualizado", Toast.LENGTH_SHORT).show()
        }
        db.close()
        finish()
    }
}
