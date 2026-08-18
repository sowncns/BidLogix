package com.sown.BidLogix.core.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception ném ra khi phiên đấu giá đã đóng, bị hủy hoặc đã quá thời gian kết thúc.
 */
public class AuctionOverException extends AppException {

    public AuctionOverException() {
        super(
            "Phiên đấu giá đã kết thúc hoặc không còn nhận lượt đặt giá mới!",
            HttpStatus.BAD_REQUEST,
            "AUCTION_OVER"
        );
    }

    public AuctionOverException(String message) {
        super(
            message,
            HttpStatus.BAD_REQUEST,
            "AUCTION_OVER"
        );
    }

    public AuctionOverException(Long auctionId) {
        super(
            String.format("Phiên đấu giá #%d đã kết thúc hoặc không còn khả dụng!", auctionId),
            HttpStatus.BAD_REQUEST,
            "AUCTION_OVER"
        );
    }
}