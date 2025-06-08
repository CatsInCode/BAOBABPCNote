package com.example.bar.ui.gallery

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.bar.databinding.FragmentGalleryBinding
import com.example.bar.databinding.FragmentSlideshowBinding
import com.example.bar.LibraryManager
import com.example.bar.R
import com.example.bar.ui.home.HomeFragment

class GalleryFragment : Fragment() {

    private var _binding: FragmentGalleryBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentGalleryBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val cardLayout = binding.cardLayout
        val firstAnchorView  = binding.build2 // ID первого объекта для привязки
        LibraryManager.importLibrary(
            context = requireContext(),
            cardLayout = cardLayout,
            firstAnchorView = firstAnchorView,
            fragment = this
        ) { buildId ->
            binding.textView6.text = "ID: $buildId"  // Теперь здесь будет отображаться ID сборки
        }


        val btn = binding.btn
        btn.setOnClickListener {
            // Создаем экземпляр HomeFragment
            val navController = findNavController()
            val bundle = Bundle().apply {
                putString("message","")
            }
            navController.navigate(R.id.nav_slideshow, bundle)
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}