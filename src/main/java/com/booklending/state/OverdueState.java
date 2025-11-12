package com.booklending.state;

import com.booklending.entity.BorrowRecord;

import java.util.Date;
import java.util.concurrent.TimeUnit;

public class OverdueState implements BorrowState {
    // 每天罚款金额
    private static final double DAILY_FINE = 0.5;

    @Override
    public double calculateFine(BorrowRecord record, Date currentDate) {
        // 确保日期非空
        if (record.getDueDate() == null) {
            return 0.0;
        }
        
        // 确定用于计算逾期天数的日期：如果已归还则使用归还日期，否则使用当前日期
        Date compareDate = record.getReturnDate() != null ? record.getReturnDate() : currentDate;
        
        // 计算时间差，确保使用TimeUnit.DAYS.convert进行精确计算
        long diffInMillies = compareDate.getTime() - record.getDueDate().getTime();
        // 使用TimeUnit.DAYS.convert确保精确计算天数差
        long diffInDays = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);
        // 确保天数不为负数
        return Math.max(0, diffInDays) * DAILY_FINE;
    }

    @Override
    public String getStateName() {
        return "OVERDUE";
    }

    @Override
    public boolean isOverdue(BorrowRecord record, Date currentDate) {
        return true; // 已经是逾期状态
    }

    @Override
    public void handleReturn(BorrowRecord record, Date returnDate) {
        record.setReturnDate(returnDate);
        record.setStatus("RETURNED");
        // 计算并设置罚款
        double fine = calculateFine(record, returnDate);
        record.setFineAmount(fine);
    }
}