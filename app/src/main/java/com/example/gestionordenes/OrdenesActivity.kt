package com.example.gestionordenes

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.gestionordenes.databinding.ActivityOrdenesBinding
import com.example.gestionordenes.databinding.ItemOrdenBinding
import java.util.Locale

class OrdenesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOrdenesBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityOrdenesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.ivVolver.setOnClickListener { finish() }
        binding.btnNueva.setOnClickListener {
            startActivity(Intent(this, NuevaOrdenActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        consultarOrdenes()
    }

    private fun consultarOrdenes() {
        binding.llOrdenes.removeAllViews()

        val db = AdministradorBD(this).readableDatabase

        // CONSULTA con relacion (JOIN) entre Ordenes y Clientes
        val cursor = db.rawQuery(
            """
            SELECT o.idOrden, c.nombre, o.fecha, o.total
            FROM Ordenes o
            LEFT JOIN Clientes c ON o.idCliente = c.idCliente
            ORDER BY o.idOrden DESC
            """.trimIndent(), null
        )

        if (cursor.count == 0) {
            binding.tvVacio.visibility = View.VISIBLE
        } else {
            binding.tvVacio.visibility = View.GONE

            while (cursor.moveToNext()) {
                val idOrden = cursor.getInt(0)
                val cliente = cursor.getString(1) ?: "(cliente eliminado)"
                val fecha = cursor.getString(2)
                val total = cursor.getDouble(3)

                val item = ItemOrdenBinding.inflate(layoutInflater, binding.llOrdenes, false)
                item.tvOrden.text = "Orden #$idOrden"
                item.tvCliente.text = cliente
                item.tvFecha.text = fecha
                item.tvTotal.text = String.format(Locale.getDefault(), "B/. %.2f", total)

                // Al tocar la tarjeta vamos al detalle de la orden
                item.cardOrden.setOnClickListener {
                    val intent = Intent(this, DetalleOrdenActivity::class.java)
                    intent.putExtra("idOrden", idOrden)
                    startActivity(intent)
                }

                binding.llOrdenes.addView(item.root)
            }
        }
        cursor.close()
        db.close()
    }
}
