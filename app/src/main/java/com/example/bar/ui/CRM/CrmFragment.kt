package com.example.bar.ui.CRM

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.bar.R
import com.example.bar.User
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

class CrmFragment : Fragment() {

    // Модели данных
    data class AuthSuccessResponse(val token: String)
    data class AuthErrorResponse(val message: String? = null)

    interface ApiService {
        @POST("register")
        suspend fun register(@Body user: User): Response<ResponseBody>

        @POST("login")
        suspend fun login(@Body user: User): Response<ResponseBody>
    }

    private lateinit var apiService: ApiService

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_crm, container, false)

        // Инициализация Retrofit
        val retrofit = Retrofit.Builder()
            .baseUrl("https://baobab-servak.onrender.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        apiService = retrofit.create(ApiService::class.java)

        // Инициализация элементов
        val loginButton: Button = view.findViewById(R.id.button4)
        val regButton: Button = view.findViewById(R.id.button5)
        val loginEditText: EditText = view.findViewById(R.id.editTextText2)
        val passwordEditText: EditText = view.findViewById(R.id.editTextText3)
        val regLoginEditText: EditText = view.findViewById(R.id.editTextText4)
        val regPasswordEditText: EditText = view.findViewById(R.id.editTextText5)
        val regUsernameEditText: EditText = view.findViewById(R.id.editTextText6)
        val regEmailEditText: EditText = view.findViewById(R.id.editTextText7)

        // Обработка логина
        loginButton.setOnClickListener {
            val login = loginEditText.text.toString()
            val password = passwordEditText.text.toString()

            if (login.isEmpty() || password.isEmpty()) {
                showToast("Заполните все поля")
            } else {
                loginUser(login, password)
            }
        }

        // Обработка регистрации
        regButton.setOnClickListener {
            val login = regLoginEditText.text.toString()
            val password = regPasswordEditText.text.toString()
            val username = regUsernameEditText.text.toString()
            val email = regEmailEditText.text.toString()

            if (login.isEmpty() || password.isEmpty() || username.isEmpty() || email.isEmpty()) {
                showToast("Заполните все поля")
            } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                showToast("Некорректный email")
            } else {
                registerUser(login, password, username, email)
            }
        }

        return view
    }

    private fun loginUser(login: String, password: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = apiService.login(User(login = login, password = password))
                activity?.runOnUiThread {
                    handleResponse(response, "Вход выполнен")
                }
            } catch (e: Exception) {
                activity?.runOnUiThread {
                    showToast("Ошибка сети: ${e.message}")
                }
            }
        }
    }

    private fun registerUser(login: String, password: String, username: String, email: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = apiService.register(
                    User(
                        login = login,
                        password = password,
                        username = username,
                        email = email
                    )
                )
                activity?.runOnUiThread {
                    if (handleResponse(response, "Регистрация успешна")) {
                        clearRegistrationFields()
                    }
                }
            } catch (e: Exception) {
                activity?.runOnUiThread {
                    showToast("Ошибка сети: ${e.message}")
                }
            }
        }
    }

    private fun handleResponse(response: Response<ResponseBody>, successMessage: String): Boolean {
        return if (response.isSuccessful) {
            try {
                val json = response.body()?.string()
                val successResponse = Gson().fromJson(json, AuthSuccessResponse::class.java)
                showToast("$successMessage! Токен: ${successResponse.token}")
                true
            } catch (e: Exception) {
                showToast(successMessage)
                true
            }
        } else {
            try {
                val errorBody = response.errorBody()?.string()
                showToast(errorBody ?: "Неизвестная ошибка")
            } catch (e: Exception) {
                showToast("Ошибка при обработке ответа")
            }
            false
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private fun clearRegistrationFields() {
        view?.findViewById<EditText>(R.id.editTextText4)?.text?.clear()
        view?.findViewById<EditText>(R.id.editTextText5)?.text?.clear()
        view?.findViewById<EditText>(R.id.editTextText6)?.text?.clear()
        view?.findViewById<EditText>(R.id.editTextText7)?.text?.clear()
    }

    companion object {
        fun newInstance() = CrmFragment()
    }
}