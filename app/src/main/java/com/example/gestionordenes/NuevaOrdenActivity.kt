package com.example.gestionordenes

import android.content.ContentValues
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.gestionordenes.databinding.ActivityNuevaOrdenBinding
import com.example.gestionordenes.databinding.ItemLineaOrdenBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Clase simple para guardar cada producto agregado a la orden (en memoria)
data class LineaOrden(
    val idProducto: Int,
    val nombre: String,
    val precio: Double,
    val cantidad: Int
) {
    val subtotal: Double get() = precio * cantidad
}

class NuevaOrdenActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNuevaOrdenBinding

    // Listas que respaldan los spinners (datos venidos de la base de datos)
    private val clienteIds = ArrayList<Int>()
    private val clienteNombres = ArrayList<String>()

    private val productoIds = ArrayList<Int>()
    private val productoNombres = ArrayList<String>()
    private val productoPrecios = ArrayList<Double>()
    private val productoStocks = ArrayList<Int>()

    // Lista temporal de productos agregados a la orden
    private val itemsOrden = ArrayList<LineaOrden>()

    private var fecha = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityNuevaOrdenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Fecha automatica (hoy)
        fecha = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
        binding.tvFecha.text = fecha

        // Cargamos los spinners con datos de la base de datos
        cargarClientes()
        cargarProductos()

        binding.btnAgregarProducto.setOnClickListener { agregarProducto() }
        binding.btnGuardarOrden.setOnClickListener { guardarOrden() }
        binding.ivVolver.setOnClickListener { finish() }
    }

    // Consulta los clientes y llena el spinner de clientes
    private fun cargarClientes() {
        val db = AdministradorBD(this).readableDatabase
        val cursor = db.rawQuery("SELECT idCliente, nombre FROM Clientes ORDER BY nombre", null)
        while (cursor.moveToNext()) {
            clienteIds.add(cursor.getInt(0))
            clienteNombres.add(cursor.getString(1))
        }
        cursor.close()
        db.close()

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, clienteNombres)
        binding.spinnerCliente.adapter = adapter
    }

    // Consulta los productos y llena el spinner de productos
    private fun cargarProductos() {
        val db = AdministradorBD(this).readableDatabase
        val cursor = db.rawQuery("SELECT idProducto, nombreProducto, precio, stock FROM Productos ORDER BY nombreProducto", null)
        while (cursor.moveToNext()) {
            productoIds.add(cursor.getInt(0))
            productoNombres.add(cursor.getString(1))
            productoPrecios.add(cursor.getDouble(2))
            productoStocks.add(cursor.getInt(3))
        }
        cursor.close()
        db.close()

        // Mostramos nombre, precio y stock disponible en el spinner
        val textos = ArrayList<String>()
        for (i in productoNombres.indices) {
            textos.add("${productoNombres[i]} - B/. ${productoPrecios[i]} (stock: ${productoStocks[i]})")
        }
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, textos)
        binding.spinnerProducto.adapter = adapter
    }

    // Agrega el producto seleccionado a la lista temporal de la orden
    private fun agregarProducto() {
        if (productoIds.isEmpty()) {
            Toast.makeText(this, "No hay productos. Registre uno primero.", Toast.LENGTH_SHORT).show()
            return
        }

        val cantidadTexto = binding.etCantidad.text.toString().trim()
        val cantidad = cantidadTexto.toIntOrNull()

        // Validacion: la cantidad debe ser mayor que cero
        if (cantidad == null || cantidad <= 0) {
            Toast.makeText(this, "La cantidad debe ser mayor que cero", Toast.LENGTH_SHORT).show()
            return
        }

        val pos = binding.spinnerProducto.selectedItemPosition
        val stockDisponible = productoStocks[pos]

        // Validacion: si el producto no tiene stock, no se puede vender
        if (stockDisponible <= 0) {
            Toast.makeText(this, "No hay stock de este producto", Toast.LENGTH_SHORT).show()
            return
        }

        // Contamos cuantas unidades de este producto ya se agregaron a la orden actual
        var yaAgregado = 0
        for (linea in itemsOrden) {
            if (linea.idProducto == productoIds[pos]) yaAgregado += linea.cantidad
        }

        // Validacion: no vender mas de lo que hay en stock
        if (yaAgregado + cantidad > stockDisponible) {
            val quedan = stockDisponible - yaAgregado
            Toast.makeText(this, "Solo quedan $quedan unidades en stock", Toast.LENGTH_SHORT).show()
            return
        }

        val linea = LineaOrden(
            idProducto = productoIds[pos],
            nombre = productoNombres[pos],
            precio = productoPrecios[pos],
            cantidad = cantidad
        )
        itemsOrden.add(linea)

        binding.etCantidad.setText("")
        mostrarItems()
    }

    // Dibuja la lista de productos agregados y calcula el total
    private fun mostrarItems() {
        binding.llItems.removeAllViews()
        var total = 0.0

        for ((indice, linea) in itemsOrden.withIndex()) {
            val item = ItemLineaOrdenBinding.inflate(layoutInflater, binding.llItems, false)
            item.tvNombre.text = linea.nombre
            item.tvDetalle.text = "${linea.cantidad} x B/. ${linea.precio}"
            item.tvSubtotal.text = String.format(Locale.getDefault(), "B/. %.2f", linea.subtotal)

            // Quitar este producto de la lista
            item.btnQuitar.setOnClickListener {
                itemsOrden.removeAt(indice)
                mostrarItems()
            }

            binding.llItems.addView(item.root)
            total += linea.subtotal
        }

        binding.tvTotal.text = String.format(Locale.getDefault(), "B/. %.2f", total)
    }

    // Guarda la orden: primero en Ordenes y luego cada producto en DetalleOrden
    private fun guardarOrden() {
        // Validacion: debe haber un cliente seleccionado
        if (clienteIds.isEmpty()) {
            Toast.makeText(this, "Debe registrar y seleccionar un cliente", Toast.LENGTH_SHORT).show()
            return
        }

        // Validacion: debe haber al menos un producto agregado
        if (itemsOrden.isEmpty()) {
            Toast.makeText(this, "Agregue al menos un producto a la orden", Toast.LENGTH_SHORT).show()
            return
        }

        // Calculamos el total general
        var total = 0.0
        for (linea in itemsOrden) {
            total += linea.subtotal
        }

        val posCliente = binding.spinnerCliente.selectedItemPosition
        val idCliente = clienteIds[posCliente]

        val db = AdministradorBD(this).writableDatabase

        // 1) Insertamos la orden en la tabla Ordenes
        val valoresOrden = ContentValues().apply {
            put("idCliente", idCliente)
            put("fecha", fecha)
            put("total", total)
        }
        val idOrden = db.insert("Ordenes", null, valoresOrden)

        // 2) Insertamos cada producto en la tabla DetalleOrden
        //    y descontamos la cantidad vendida del stock del producto
        for (linea in itemsOrden) {
            val valoresDetalle = ContentValues().apply {
                put("idOrden", idOrden)
                put("idProducto", linea.idProducto)
                put("cantidad", linea.cantidad)
                put("subtotal", linea.subtotal)
            }
            db.insert("DetalleOrden", null, valoresDetalle)

            // Restamos del inventario lo que se vendio (UPDATE)
            db.execSQL(
                "UPDATE Productos SET stock = stock - ? WHERE idProducto = ?",
                arrayOf<Any>(linea.cantidad, linea.idProducto)
            )
        }
        db.close()

        Toast.makeText(this, "Orden guardada correctamente", Toast.LENGTH_SHORT).show()
        finish()
    }
}
