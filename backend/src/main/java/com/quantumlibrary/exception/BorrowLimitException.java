package com.quantumlibrary.exception;

/**
 * BorrowLimitException — thrown when a member tries to borrow more books
 * than their allowed limit (default: 3).
 *
 * The frontend detects this via errorCode = "BORROW_LIMIT_REACHED" and
 * shows an interactive prompt: "Return a book to continue."
 */
public class BorrowLimitException extends RuntimeException {
    public BorrowLimitException(String message) {
        super(message);
    }
}
