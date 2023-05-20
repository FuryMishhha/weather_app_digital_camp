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
import com.example.weatherapp.utils.LocalKeyStorage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.fragment_login.backbtn2
import kotlinx.android.synthetic.main.fragment_login.buttonOut
import kotlinx.android.synthetic.main.fragment_login.buttonEnter
import kotlinx.android.synthetic.main.fragment_login.buttonRegistration
import kotlinx.android.synthetic.main.fragment_login.editEmail
import kotlinx.android.synthetic.main.fragment_login.editPassword
import kotlinx.android.synthetic.main.fragment_login.headreview
import kotlinx.android.synthetic.main.fragment_login.nameUserActually
import kotlinx.android.synthetic.main.fragment_login.userNotNull
import kotlinx.android.synthetic.main.fragment_login.userNull

class LoginFragment : Fragment() {
    private lateinit var myFirebaseAuth: FirebaseAuth
    private lateinit var myDataBase: DatabaseReference
    lateinit var localKeyStorage: LocalKeyStorage

    override fun onStart() {
        super.onStart()
        val currentUser: FirebaseUser? = myFirebaseAuth.currentUser
        if (currentUser != null) {
            currentUser.email?.let { showSignedIn(it) }
        } else {
            showSignedOut()
            Toast.makeText(
                context,
                "Зарегистрируйтесь или войдите в свой профиль",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_login, container, false)
        localKeyStorage = LocalKeyStorage(requireContext())
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (requireActivity() as AppCompatActivity).supportActionBar?.hide()

        backbtn2.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_homeFragment)
        }

        myFirebaseAuth = FirebaseAuth.getInstance()
        myDataBase = FirebaseDatabase.getInstance().getReference("City")

        buttonRegistration.setOnClickListener {
            val email: String = editEmail.text.toString()
            val password: String = editPassword.text.toString()
            var key1 = true
            var key2 = true
            if (TextUtils.isEmpty(email)) {
                editEmail.error = "Введите Вашу почту"
                editEmail.requestFocus()
                key1 = false
            } else {
                if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    key1 = false
                    editEmail.error = "Введите корректную почту"
                    editEmail.requestFocus()
                } else {
                    if (TextUtils.isEmpty(password)) {
                        editPassword.error = "Введите Ваш пароль"
                        editPassword.requestFocus()
                        key2 = false
                    }
                }
            }

            if (key1 && key2) {
                myFirebaseAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener {
                        if (it.isSuccessful) {
                            Toast.makeText(
                                context,
                                "Пользователь успешно зарегистрирован",
                                Toast.LENGTH_SHORT
                            ).show()
                            sendEmailVer()
                        } else {
                            Toast.makeText(
                                context,
                                "Пользователь с такой почтой уже существует",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
            }
        }

        buttonEnter.setOnClickListener {
            val email: String = editEmail.text.toString()
            val password: String = editPassword.text.toString()
            var key3 = true
            var key4 = true
            if (TextUtils.isEmpty(email)) {
                editEmail.error = "Введите Вашу почту"
                editEmail.requestFocus()
                key3 = false
            } else {
                if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    key3 = false
                    editEmail.error = "Введите корректную почту"
                    editEmail.requestFocus()
                } else {
                    if (TextUtils.isEmpty(password)) {
                        editPassword.error = "Введите Ваш пароль"
                        editPassword.requestFocus()
                        key4 = false
                    }
                }
            }
            if (key3 && key4) {
                myFirebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener {
                    if (it.isSuccessful) {
                        val currentUser: FirebaseUser? = myFirebaseAuth.currentUser
                        if (currentUser != null) {
                            if (!currentUser.isEmailVerified) {
                                Toast.makeText(
                                    context,
                                    "Проверьте вашу почту для подтверждения email-адреса",
                                    Toast.LENGTH_SHORT
                                ).show()
                                FirebaseAuth.getInstance().signOut()
                                showSignedOut()
                            } else {
                                Toast.makeText(
                                    context, "Авторизация успешна", Toast.LENGTH_SHORT
                                ).show()
                                showSignedIn(email)
                            }
                        }
                    } else {
                        Toast.makeText(
                            context,
                            "Ошибка авторизации",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }

        buttonOut.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            showSignedOut()
            Toast.makeText(
                context,
                "Вы успешно вышли из учётной записи",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun showSignedIn(email: String) {
        val currentUser: FirebaseUser? = myFirebaseAuth.currentUser
        if (currentUser != null) {
            println(1)
            if (!currentUser.isEmailVerified) {
                println(3)
                Toast.makeText(
                    context,
                    "Проверьте вашу почту для подтверждения email-адреса",
                    Toast.LENGTH_SHORT
                ).show()
                FirebaseAuth.getInstance().signOut()
                showSignedOut()
            } else {
                println(2)
                nameUserActually.text = "Привет ${email}!"
                headreview.text = "Личный кабинет"
                userNotNull.visibility = View.VISIBLE
                userNull.visibility = View.GONE
            }
        }
    }

    private fun showSignedOut() {
        headreview.text = "Авторизация"
        userNotNull.visibility = View.GONE
        userNull.visibility = View.VISIBLE
    }

    private fun sendEmailVer() {
        myFirebaseAuth.currentUser?.sendEmailVerification()?.addOnCompleteListener {
            if (it.isSuccessful) {
                Toast.makeText(
                    context,
                    "Проверьте вашу почту для подтверждения email-адреса",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(
                    context,
                    "Ошибка отправки письма",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}