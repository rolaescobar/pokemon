package com.codigoit.pokemon

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.codigoit.pokemon.databinding.ActivityMainBinding
import com.bumptech.glide.Glide
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var tvApiStatus: TextView
    private lateinit var pokemonImageView: ImageView
    private lateinit var btnSearchPokemon: Button // El botón para buscar otro Pokémon

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        tvApiStatus = findViewById(R.id.tvApiStatus)
        pokemonImageView = findViewById(R.id.pokemonImageView)
        btnSearchPokemon = findViewById(R.id.btnSearchPokemon)

        // Al iniciar, cargamos el Pokémon con ID 1 (Bulbasaur)
        testApiCall(1)

        // Al presionar el botón, se genera un nuevo ID y se busca otro Pokémon
        btnSearchPokemon.setOnClickListener {
            val randomPokemonId = Random.nextInt(1, 150) // ID aleatorio entre 1 y 150
            testApiCall(randomPokemonId)
        }
    }

    // Modificamos la función testApiCall para aceptar un ID de Pokémon
    private fun testApiCall(pokemonId: Int) {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://pokeapi.co/api/v2/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service = retrofit.create(PokeApiService::class.java)
        service.getPokemon(pokemonId.toString()).enqueue(object : Callback<Pokemon> {
            override fun onResponse(call: Call<Pokemon>, response: Response<Pokemon>) {
                if (response.isSuccessful) {
                    val pokemonName = response.body()?.name ?: "Nombre no disponible"
                    val pokemonWeight = response.body()?.weight ?: 0
                    val pokemonImageUrl = response.body()?.sprites?.front_shiny ?: ""

                    Log.d("API_TEST", "Nombre: $pokemonName, Peso: $pokemonWeight, Imagen: $pokemonImageUrl")

                    tvApiStatus.text = "Nombre: $pokemonName, Peso: $pokemonWeight"

                    // Usamos Glide para cargar la imagen en el ImageView
                    Glide.with(this@MainActivity)
                        .load(pokemonImageUrl)
                        .into(pokemonImageView)

                } else {
                    Log.e("API_TEST", "Respuesta no exitosa: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<Pokemon>, t: Throwable) {
                tvApiStatus.text = "API Status: Error - ${t.message}"
            }
        })
    }

    // Define la interfaz del servicio API
    interface PokeApiService {
        @GET("pokemon/{id}")
        fun getPokemon(@Path("id") id: String): Call<Pokemon>
    }

    // Data class para mapear la respuesta de la API
    data class Pokemon(
        val name: String,
        val weight: Int,
        val sprites: Sprites
    )

    data class Sprites(
        val front_default: String,
        val front_shiny: String
    )
}

