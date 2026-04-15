package com.quantumlibrary.dto;

import lombok.Data;

/** Request body for POST /api/borrow */
@Data
public class BorrowRequestDto {
    private Long bookId;
}
