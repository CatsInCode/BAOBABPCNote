package com.example.bar.ui.slideshow

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.bar.FirebaseManager.getAndCreateCardsByViewName
import com.example.bar.FirebaseManager.getAndCreateCardsByViewNamePUB
import com.example.bar.R
import com.example.bar.databinding.FragmentSlideshowBinding


class SlideshowFragment : Fragment() {

    private var _binding: FragmentSlideshowBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentSlideshowBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val message = arguments?.getString("message")

        message?.let {
            getAndCreateCardsByViewName(message, requireContext(), binding.cards)
        }


        @SuppressLint("ServiceCast")

        fun fetchCards(viewName: String, context: Context, parentLayout: ConstraintLayout) {
            getAndCreateCardsByViewNamePUB(viewName, context, parentLayout) { result ->
                result.onSuccess {
                    Toast.makeText(context, "Сборка загружена", Toast.LENGTH_SHORT).show()
                    binding.textView10.visibility = View.GONE
                    binding.addComp3.visibility = View.GONE
                }.onFailure { error ->
                    Toast.makeText(context, "Сбой", Toast.LENGTH_SHORT).show()
                    binding.textView10.visibility = View.VISIBLE
                    binding.addComp3.visibility = View.VISIBLE

                    // Очищаем все кроме textView10 и addComp3
                    for (i in binding.cards.childCount - 1 downTo 0) {
                        val child = binding.cards.getChildAt(i)
                        if (child != binding.textView10 && child != binding.addComp3) {
                            binding.cards.removeViewAt(i)
                        }
                    }
                }
            }
        }

        fun setupNameInputListener(context: Context, parentLayout: ConstraintLayout, editText: EditText) {
            editText.setOnEditorActionListener { v, actionId, event ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    val viewName = editText.text.toString().trim()
                    if (viewName.isNotEmpty()) {
                        fetchCards(viewName, context, parentLayout)
                    } else {
                        Toast.makeText(context, "Имя не может быть пустым", Toast.LENGTH_SHORT).show()
                    }
                    true
                } else {
                    false
                }
            }

            editText.addTextChangedListener(object : android.text.TextWatcher {
                override fun afterTextChanged(s: android.text.Editable?) {
                    if (!s.isNullOrEmpty()) {
                        binding.imageView3.setImageResource(R.drawable.ic_trash)
                        binding.imageView3.visibility = View.VISIBLE
                    } else {
                        binding.imageView3.setImageResource(R.drawable.ic_custom)
                        binding.imageView3.visibility = View.VISIBLE
                    }
                }

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            })

            binding.imageView3.setOnClickListener {
                editText.text.clear()
                binding.imageView3.setImageResource(R.drawable.ic_custom)
            }
        }

        setupNameInputListener(requireContext(), binding.cards, binding.editTextText)

        binding.addComp3.setOnClickListener {
            val viewName = binding.editTextText.text.toString().trim()
            if (viewName.isNotEmpty()) {
                fetchCards(viewName, requireContext(), binding.cards)
            } else {
                Toast.makeText(requireContext(), "Имя не может быть пустым", Toast.LENGTH_SHORT).show()
            }
        }

        return root
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}