package com.example.gestionordenes

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.gestionordenes.databinding.ActivityClientesBinding
import com.example.gestionordenes.databinding.ItemClienteBinding

class ClientesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityClientesBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityClientesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.ivVolver.setOnClickListener { finish() }

        // Boton agregar -> abre el formulario en modo NUEVO
        binding.btnAgregar.setOnClickListener {
            startActivity(Intent(this, ClienteFormActivity::class.java))
        }
    }

    // Recargamos la lista cada vez que volvemos a esta pantalla
    override fun onResume() {
        super.onResume()
        consultarClientes()
    }

    private fun consultarClientes() {
        binding.llClientes.removeAllViews()

        val admin = AdministradorBD(this)
        val db = admin.readableDatabase

        // CONSULTA (SELECT) de todos los clientes
        val cursor = db.rawQuery("SELECT * FROM Clientes ORDER BY idCliente DESC", null)

        if (cursor.count == 0) {
            binding.tvVacio.visibility = View.VISIBLE
        } else {
            binding.tvVacio.visibility = View.GONE

            while (cursor.moveToNext()) {
                val id = cursor.getInt(0)
                val nombre = cursor.getString(1)
                val correo = cursor.getString(2)
                val telefono = cursor.getString(3)
                val direccion = cursor.getString(4)

                val item = ItemClienteBinding.inflate(layoutInflater, binding.llClientes, false)
                item.tvNombre.text = nombre
                item.tvCorreo.text = correo
                item.tvTelefono.text = telefono

                // Editar -> abre el formulario con los datos cargados
                item.btnEditar.setOnClickListener {
                    val intent = Intent(this, ClienteFormActivity::class.java)
                    intent.putExtra("id", id)
                    intent.putExtra("nombre", nombre)
                    intent.putExtra("correo", correo)
                    intent.putExtra("telefono", telefono)
                    intent.putExtra("direccion", direccion)
                    startActivity(intent)
                }

                // Eliminar (DELETE) y refrescar
                item.btnEliminar.setOnClickListener {
                    val dbBorrar = AdministradorBD(this).writableDatabase
                    dbBorrar.delete("Clientes", "idCliente = ?", arrayOf(id.toString()))
                    dbBorrar.close()
                    consultarClientes()
                }

                binding.llClientes.addView(item.root)
            }
        }
        cursor.close()
        db.close()
    }
}
