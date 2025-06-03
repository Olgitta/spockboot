package com.olg.services.booking.exceptions;

public class SeatOperationException extends RuntimeException {

    private final String errorCode; // Добавляем поле для кода ошибки

    // Конструктор с сообщением и кодом ошибки
    public SeatOperationException(SeatErrorCodes errorCode, String message) {
        super(message);
        this.errorCode = errorCode.getCode(); // Сохраняем строковое представление кода
    }

    // Конструктор с сообщением, причиной и кодом ошибки
    public SeatOperationException(SeatErrorCodes errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode.getCode();
    }

    // Добавляем геттер для кода ошибки
    public String getErrorCode() {
        return errorCode;
    }

    // Опционально: можно переопределить toString для вывода кода ошибки
    @Override
    public String toString() {
        return "SeatOperationException{" +
                "errorCode='" + errorCode + '\'' +
                ", message='" + getLocalizedMessage() + '\'' +
                '}';
    }
}