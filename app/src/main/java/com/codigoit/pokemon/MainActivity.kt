package com.codigoit.pokemon

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.codigoit.pokemon.databinding.ActivityMainBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var tvApiStatus: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

  tvApiStatus = findViewById<TextView>(R.id.tvApiStatus)

        testApiCall()

    }

    private fun testApiCall() {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://pokeapi.co/api/v2/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service = retrofit.create(PokeApiService::class.java)
        service.getPokemon("1").enqueue(object : Callback<Pokemon> {
            override fun onResponse(call: Call<Pokemon>, response: Response<Pokemon>) {
                if (response.isSuccessful) {
                    val pokemonName = response.body()?.name ?: "Nombre no disponible"
                    val pokemonWeight = response.body()?.weight ?: 0

                    Log.d("API_TEST", "Nombre: ${response.body()?.name}, Peso: ${response.body()?.weight}")

                    tvApiStatus.text = "Nombre: $pokemonName, Peso: $pokemonWeight"

                } else {
                    Log.e("API_TEST", "Respuesta no exitosa: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<Pokemon>, t: Throwable) {
                tvApiStatus.text = "API Status: Error - ${t.message}"

            }
        })
    }

    // Define the API service interface
    interface PokeApiService {
        @GET("pokemon/{id}")
        fun getPokemon(@Path("id") id: String): Call<Pokemon>
    }

    // Define a data class to match the API response
    data class Pokemon(
        val name: String,
        val weight: Int
    )
}

