package com.nammahasiru.app

import android.widget.Button
import android.widget.EditText
import android.widget.Toast
override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    val speciesInput = view.findViewById<EditText>(R.id.speciesInput)
    val saveBtn = view.findViewById<Button>(R.id.saveBtn)

    saveBtn.setOnClickListener {

        val plantName = speciesInput.text.toString()

        if (plantName.isEmpty()) {
            Toast.makeText(requireContext(), "Enter plant name", Toast.LENGTH_SHORT).show()
            return@setOnClickListener
        }

        Toast.makeText(requireContext(), "$plantName saved 🌱", Toast.LENGTH_SHORT).show()
    }
}