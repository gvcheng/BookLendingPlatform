package com.booklending.state;

import com.booklending.entity.BorrowRecord;

import java.util.Date;

public class NormalState implements BorrowState {
    @Override
    public double calculateFine(BorrowRecord record, Date currentDate) {
        // 正常状态下没有罚款
        return 0.0;
    }

    @Override
    public String getStateName() {
        return "BORROWED";
    }

    @Override
    public boolean isOverdue(BorrowRecord record, Date currentDate) {
        return currentDate.after(record.getDueDate());
    }

    @Override
    public void handleReturn(BorrowRecord record, Date returnDate) {
        record.setReturnDate(returnDate);
        record.setStatus("RETURNED");
        record.setFineAmount(0.0);
    }
}