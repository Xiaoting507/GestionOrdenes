package com.example.gestionordenes

import android.content.ContentValues
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.gestionordenes.databinding.ActivityClienteFormBinding

class ClienteFormActivity : AppCompatActivity() {

    private lateinit var binding: ActivityClienteFormBinding

    // id del cliente a editar. -1 = nuevo cliente
    private var idEditar = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityClienteFormBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Si venimos a EDITAR, cargamos los datos
        idEditar = intent.getIntExtra("id", -1)
        if (idEditar != -1) {
            binding.tvTitulo.text = "Editar Cliente"
            binding.etNombre.setText(intent.getStringExtra("nombre"))
            binding.etCorreo.setText(intent.getStringExtra("correo"))
            binding.etTelefono.setText(intent.getStringExtra("telefono"))
            binding.etDireccion.setText(intent.getStringExtra("direccion"))
        }

        binding.btnGuardar.setOnClickListener { guardarCliente() }
        binding.ivVolver.setOnClickListener { finish() }
    }

    private fun guardarCliente() {
        val nombre = binding.etNombre.text.toString().trim()
        val correo = binding.etCorreo.text.toString().trim()
        val telefono = binding.etTelefono.text.toString().trim()
        val direccion = binding.etDireccion.text.toString().trim()

        // Validacion: no permitir guardar con campos vacios (nombre y correo obligatorios)
        if (nombre.isEmpty() || correo.isEmpty()) {
            Toast.makeText(this, "El nombre y el correo son obligatorios", Toast.LENGTH_SHORT).show()
            return
        }

        val valores = ContentValues().apply {
            put("nombre", nombre)
            put("correo", correo)
            put("telefono", telefono)
            put("direccion", direccion)
        }

        val admin = AdministradorBD(this)
        val db = admin.writableDatabase

        if (idEditar == -1) {
            // INSERT
            db.insert("Clientes", null, valores)
            Toast.makeText(this, "Cliente registrado", Toast.LENGTH_SHORT).show()
        } else {
            // UPDATE
            db.update("Clientes", valores, "idCliente = ?", arrayOf(idEditar.toString()))
            Toast.makeText(this, "Cliente actualizado", Toast.LENGTH_SHORT).show()
        }
        db.close()
        finish()
    }
}
