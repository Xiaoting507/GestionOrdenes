package com.example.gestionordenes

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.gestionordenes.databinding.ActivityDetalleOrdenBinding
import com.example.gestionordenes.databinding.ItemDetalleBinding
import java.util.Locale

class DetalleOrdenActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetalleOrdenBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDetalleOrdenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.ivVolver.setOnClickListener { finish() }

        // Recibimos el id de la orden que llego desde la lista
        val idOrden = intent.getIntExtra("idOrden", -1)
        binding.tvTitulo.text = "Orden #$idOrden"

        mostrarEncabezado(idOrden)
        mostrarProductos(idOrden)
    }

    // Consulta el cliente y la fecha de la orden (JOIN con Clientes)
    private fun mostrarEncabezado(idOrden: Int) {
        val db = AdministradorBD(this).readableDatabase
        val cursor = db.rawQuery(
            """
            SELECT c.nombre, o.fecha, o.total
            FROM Ordenes o
            LEFT JOIN Clientes c ON o.idCliente = c.idCliente
            WHERE o.idOrden = ?
            """.trimIndent(), arrayOf(idOrden.toString())
        )
        if (cursor.moveToFirst()) {
            binding.tvCliente.text = cursor.getString(0) ?: "(cliente eliminado)"
            binding.tvFecha.text = cursor.getString(1)
            binding.tvTotal.text = String.format(Locale.getDefault(), "B/. %.2f", cursor.getDouble(2))
        }
        cursor.close()
        db.close()
    }

    // Consulta los productos de la orden (JOIN entre DetalleOrden y Productos)
    private fun mostrarProductos(idOrden: Int) {
        val db = AdministradorBD(this).readableDatabase
        val cursor = db.rawQuery(
            """
            SELECT p.nombreProducto, d.cantidad, p.precio, d.subtotal
            FROM DetalleOrden d
            LEFT JOIN Productos p ON d.idProducto = p.idProducto
            WHERE d.idOrden = ?
            """.trimIndent(), arrayOf(idOrden.toString())
        )

        while (cursor.moveToNext()) {
            val nombre = cursor.getString(0) ?: "(producto eliminado)"
            val cantidad = cursor.getInt(1)
            val precio = cursor.getDouble(2)
            val subtotal = cursor.getDouble(3)

            val item = ItemDetalleBinding.inflate(layoutInflater, binding.llDetalle, false)
            item.tvNombre.text = nombre
            item.tvDetalle.text = "$cantidad x B/. $precio"
            item.tvSubtotal.text = String.format(Locale.getDefault(), "B/. %.2f", subtotal)

            binding.llDetalle.addView(item.root)
        }
        cursor.close()
        db.close()
    }
}
