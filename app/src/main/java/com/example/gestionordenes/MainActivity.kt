package com.example.gestionordenes

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.gestionordenes.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Navegacion entre pantallas con Intents explicitos
        binding.cardClientes.setOnClickListener {
            startActivity(Intent(this, ClientesActivity::class.java))
        }
        binding.cardProductos.setOnClickListener {
            startActivity(Intent(this, ProductosActivity::class.java))
        }
        binding.cardNuevaOrden.setOnClickListener {
            startActivity(Intent(this, NuevaOrdenActivity::class.java))
        }
        binding.cardOrdenes.setOnClickListener {
            startActivity(Intent(this, OrdenesActivity::class.java))
        }
        binding.btnSalir.setOnClickListener { finish() }
    }
}
