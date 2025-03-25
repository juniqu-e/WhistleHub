package com.whistlehub.common.view.signup

import java.time.LocalDate

object ValidationUtils {
    // 아이디: 4-20자, 영어 대소문자 및 숫자만 허용
    fun isValidUserId(id: String): Boolean {
        val regex = Regex("^[A-Za-z0-9]{4,20}$")
        return regex.matches(id)
    }

    // 비밀번호: 8-64자, 최소 하나의 숫자, 하나의 영문 대문자, 하나의 영문 소문자, 하나의 특수문자 포함
    fun isValidPassword(password: String): Boolean {
        val regex = Regex("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#\$%^&*()_+\\-=[\\]{};':\"\\\\|,.<>/?]).{8,64}$")
        return regex.matches(password)
    }

    // 이메일: 기본 이메일 형식 검증
    fun isValidEmail(email: String): Boolean {
        val regex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")
        return regex.matches(email)
    }

    // 닉네임: 2-20자, 한글 및 영어만 허용
    fun isValidNickname(nickname: String): Boolean {
        val regex = Regex("^[가-힣A-Za-z]{2,20}$")
        return regex.matches(nickname)
    }

    // 생년월일: 각 필드가 채워졌으며 실제 날짜로 존재하는지 검증
    fun isValidBirthDate(year: String, month: String, day: String): Boolean {
        return try {
            LocalDate.of(year.toInt(), month.toInt(), day.toInt())
            true
        } catch (e: Exception) {
            false
        }
    }
}