package com.example.bar.ui.slideshow

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.bar.FirebaseManager.getAndCreateCardsByViewName
import com.example.bar.FirebaseManager.getAndCreateCardsByViewNamePUB
import com.example.bar.R
import com.example.bar.databinding.FragmentSlideshowBinding

class SlideshowFragment : Fragment() {

    private var _binding: FragmentSlideshowBinding? = null
    private val binding get() = _binding!!
    private lateinit var sharedPreferences: SharedPreferences
    private val searchHistoryKey = "search_history"
    private val maxHistoryItems = 5
    private var isHistoryVisible = false
    private val handler = Handler(Looper.getMainLooper())
    private val MIN_LOADING_DURATION = 2000L
    private val INACTIVITY_TIMEOUT = 7000L // 7 секунд бездействия
    private val inactivityRunnable = Runnable { clearFocusAndHideKeyboard() }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSlideshowBinding.inflate(inflater, container, false)
        val root: View = binding.root
        sharedPreferences = requireContext().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)

        val message = arguments?.getString("message")
        message?.let {
            getAndCreateCardsByViewName(message, requireContext(), binding.cards)
        }

        setupViews()
        return root
    }

    private fun setupViews() {
        setupProgressBar()
        setupNameInputListener()
        setupSearchHistoryDropdown()
        setupClickListeners()
    }

    private fun setupProgressBar() {
        binding.progressBar.visibility = View.GONE
    }

    private fun showProgress() {
        binding.progressBar.visibility = View.VISIBLE
        binding.cards.visibility = View.GONE
        binding.addComp3.isEnabled = false
    }

    private fun hideProgress() {
        binding.progressBar.visibility = View.GONE
        binding.cards.visibility = View.VISIBLE
        binding.addComp3.isEnabled = true
    }

    private fun setupClickListeners() {
        binding.editTextText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                resetInactivityTimer()
                if (getSearchHistory().isNotEmpty()) {
                    showHistoryDropdown()
                }
            } else {
                hideHistoryDropdown()
            }
        }

        binding.addComp3.setOnClickListener {
            val viewName = binding.editTextText.text.toString().trim()
            if (viewName.isNotEmpty()) {
                showProgress()
                addToSearchHistory(viewName)
                resetInactivityTimer()

                val startTime = System.currentTimeMillis()

                fun fetchCards(
                    viewName: String,
                    context: Context,
                    parentLayout: ConstraintLayout,
                    onComplete: () -> Unit
                ) {
                    getAndCreateCardsByViewNamePUB(viewName, context, parentLayout) { result ->
                        result.onSuccess { buildId ->  // Получаем buildId из успешного результата
                            Toast.makeText(context, "Сборка загружена \"${buildId}\"", Toast.LENGTH_SHORT).show()
                            binding.textView10.visibility = View.GONE
                            binding.addComp3.visibility = View.GONE

                            // Заменяем текст в поисковой строке на ID сборки
                            binding.editTextText.setText(buildId)

                            onComplete()
                        }.onFailure { error ->
                            Toast.makeText(context, "Сбой: ${error.message}", Toast.LENGTH_SHORT).show()
                            binding.textView10.visibility = View.VISIBLE
                            binding.addComp3.visibility = View.VISIBLE

                            for (i in binding.cards.childCount - 1 downTo 0) {
                                val child = binding.cards.getChildAt(i)
                                if (child != binding.textView10 && child != binding.addComp3) {
                                    binding.cards.removeViewAt(i)
                                }
                            }
                            onComplete()
                        }
                    }
                }
            } else {
                Toast.makeText(requireContext(), "Имя не может быть пустым", Toast.LENGTH_SHORT).show()
            }
        }

        binding.imageView3.setOnClickListener {
            binding.editTextText.text.clear()
            binding.imageView3.setImageResource(R.drawable.ic_custom)
            hideHistoryDropdown()
            resetInactivityTimer()
        }

        binding.root.setOnClickListener {
            if (isHistoryVisible) {
                hideHistoryDropdown()
                clearFocusAndHideKeyboard()
            }
        }
    }

    private fun resetInactivityTimer() {
        // Удаляем предыдущий таймер
        handler.removeCallbacks(inactivityRunnable)
        // Запускаем новый таймер на 7 секунд
        handler.postDelayed(inactivityRunnable, INACTIVITY_TIMEOUT)
    }

    private fun showHistoryDropdown() {
        if (getSearchHistory().isNotEmpty()) {
            setupSearchHistoryDropdown()
            binding.historyDropdown.visibility = View.VISIBLE
            isHistoryVisible = true
        }
    }

    private fun hideHistoryDropdown() {
        binding.historyDropdown.visibility = View.GONE
        isHistoryVisible = false
    }

    private fun clearFocusAndHideKeyboard() {
        binding.editTextText.clearFocus()
        hideKeyboard()
    }

    @SuppressLint("ServiceCast")
    private fun fetchCards(
        viewName: String,
        context: Context,
        parentLayout: ConstraintLayout,
        onComplete: () -> Unit
    ) {
        getAndCreateCardsByViewNamePUB(viewName, context, parentLayout) { result ->
            result.onSuccess { buildId ->  // Получаем buildId из успешного результата
                Toast.makeText(context, "Сборка загружена", Toast.LENGTH_SHORT).show()
                binding.textView10.visibility = View.GONE
                binding.addComp3.visibility = View.GONE

                // Заменяем текст в поисковой строке на ID сборки
                binding.editTextText.setText(buildId)

                onComplete()
            }.onFailure { error ->
                Toast.makeText(context, "Сбой: ${error.message}", Toast.LENGTH_SHORT).show()
                binding.textView10.visibility = View.VISIBLE
                binding.addComp3.visibility = View.VISIBLE

                for (i in binding.cards.childCount - 1 downTo 0) {
                    val child = binding.cards.getChildAt(i)
                    if (child != binding.textView10 && child != binding.addComp3) {
                        binding.cards.removeViewAt(i)
                    }
                }
                onComplete()
            }
        }
    }

    private fun setupNameInputListener() {
        binding.editTextText.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                val viewName = binding.editTextText.text.toString().trim()
                if (viewName.isNotEmpty()) {
                    showProgress()
                    addToSearchHistory(viewName)
                    resetInactivityTimer()

                    val startTime = System.currentTimeMillis()
                    fetchCards(viewName, requireContext(), binding.cards) {
                        val elapsed = System.currentTimeMillis() - startTime
                        val remainingDelay = if (elapsed < MIN_LOADING_DURATION) {
                            MIN_LOADING_DURATION - elapsed
                        } else {
                            0L
                        }

                        handler.postDelayed({
                            hideProgress()
                            clearFocusAndHideKeyboard()
                        }, remainingDelay)
                    }
                    true
                } else {
                    Toast.makeText(requireContext(), "Имя не может быть пустым", Toast.LENGTH_SHORT).show()
                    false
                }
            } else {
                false
            }
        }

        binding.editTextText.addTextChangedListener(object : android.text.TextWatcher {
            override fun afterTextChanged(s: android.text.Editable?) {
                if (!s.isNullOrEmpty()) {
                    binding.imageView3.setImageResource(R.drawable.ic_trash)
                    binding.imageView3.visibility = View.VISIBLE
                } else {
                    binding.imageView3.setImageResource(R.drawable.ic_custom)
                    binding.imageView3.visibility = View.VISIBLE
                }
                // Сбрасываем таймер при каждом изменении текста
                resetInactivityTimer()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun setupSearchHistoryDropdown() {
        binding.historyDropdown.removeAllViews()
        val history = getSearchHistory()

        if (history.isEmpty()) {
            hideHistoryDropdown()
            return
        }

        val header = TextView(requireContext()).apply {
            text = "История поиска"
            setTextColor(Color.GRAY)
            setPadding(16.dpToPx(), 8.dpToPx(), 16.dpToPx(), 8.dpToPx())
            textSize = 12f
        }
        binding.historyDropdown.addView(header)

        val divider = View(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                1
            ).apply {
                setMargins(16.dpToPx(), 4.dpToPx(), 16.dpToPx(), 4.dpToPx())
            }
            setBackgroundColor(Color.parseColor("#444444"))
        }
        binding.historyDropdown.addView(divider)

        for (item in history.reversed()) {
            val historyItem = TextView(requireContext()).apply {
                text = item
                setTextColor(Color.WHITE)
                setPadding(16.dpToPx(), 12.dpToPx(), 16.dpToPx(), 12.dpToPx())
                gravity = Gravity.CENTER_VERTICAL
                background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_history_item)
            }

            historyItem.setOnClickListener {
                binding.editTextText.setText(item)
                binding.editTextText.setSelection(item.length)
                resetInactivityTimer()
                clearFocusAndHideKeyboard()
            }

            binding.historyDropdown.addView(historyItem)
        }

        val clearHistory = TextView(requireContext()).apply {
            text = "Очистить историю"
            setTextColor(Color.parseColor("#FF5722"))
            setPadding(16.dpToPx(), 12.dpToPx(), 16.dpToPx(), 12.dpToPx())
            gravity = Gravity.CENTER
            setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_clear, 0, 0, 0)
            compoundDrawablePadding = 8.dpToPx()
        }

        clearHistory.setOnClickListener {
            clearSearchHistory()
        }

        binding.historyDropdown.addView(clearHistory)
    }

    private fun clearSearchHistory() {
        sharedPreferences.edit().remove(searchHistoryKey).apply()
        hideHistoryDropdown()
    }

    private fun addToSearchHistory(query: String) {
        val history = getSearchHistory().toMutableList()
        history.remove(query)
        history.add(query)

        val updatedHistory = if (history.size > maxHistoryItems) {
            history.takeLast(maxHistoryItems)
        } else {
            history
        }

        sharedPreferences.edit()
            .putStringSet(searchHistoryKey, updatedHistory.toSet())
            .apply()
    }

    private fun getSearchHistory(): List<String> {
        return sharedPreferences.getStringSet(searchHistoryKey, emptySet())?.toList() ?: emptyList()
    }

    private fun hideKeyboard() {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.editTextText.windowToken, 0)
    }

    private fun Int.dpToPx(): Int {
        return (this * resources.displayMetrics.density).toInt()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Удаляем все callback'и Handler при уничтожении View
        handler.removeCallbacksAndMessages(null)
        _binding = null
    }
}