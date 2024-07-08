package com.amefure.mimamori.Utility

class ValidationUtility {
    companion object {

        /** Emailバリデーション */
        public fun validateEmail(email: String): Boolean {
            val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"
            return Regex(emailRegex).matches(email)
        }

        /** パスワードバリデーション */
        public fun validatePassword(password: String): Boolean {
            return password.length >= 8
        }
    }
}