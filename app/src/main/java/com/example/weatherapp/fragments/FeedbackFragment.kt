package com.example.weatherapp.fragments

import android.os.Bundle
import android.text.TextUtils
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.weatherapp.R
import com.example.weatherapp.data.Feedback
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.fragment_feedback.*

class FeedbackFragment : Fragment() {
    private lateinit var myDataBase: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_feedback, container, false)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (requireActivity() as AppCompatActivity).supportActionBar?.hide()

        backbtn1.setOnClickListener {
            findNavController().navigate(R.id.action_feedbackFragment_to_homeFragment)
        }

        myDataBase = FirebaseDatabase.getInstance().getReference("Feedback")

        send.setOnClickListener {
            var validate = true
            val name = nameInput.text.toString()
            val email = emailInput.text.toString()
            val msg = messageInput.text.toString()
            if (name.isEmpty()) {
                nameInput.error = "Введите Ваше имя"
                nameInput.requestFocus()
                validate = false
            } else {
                if (TextUtils.isEmpty(email)) {
                    emailInput.error = "Введите Вашу почту"
                    emailInput.requestFocus()
                    validate = false
                } else {
                    if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                        validate = false
                        emailInput.error = "Введите корректную почту"
                        emailInput.requestFocus()
                    }
                    else{
                        if (TextUtils.isEmpty(msg)){
                            messageInput.error = "Введите Ваше сообщение"
                            messageInput.requestFocus()
                            validate = false
                        }
                    }
                }
            }
            if (validate) {
                myDataBase.push().setValue(Feedback(name, email, msg))
                Toast.makeText(context, "Спасибо за отзыв!", Toast.LENGTH_SHORT).show()
                findNavController().navigate(R.id.action_feedbackFragment_to_homeFragment)
            }
        }
    }
}