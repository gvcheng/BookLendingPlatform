package com.booklending.state;

import com.booklending.entity.BorrowRecord;

import java.util.Date;

public interface BorrowState {
    // 计算罚款
    double calculateFine(BorrowRecord record, Date currentDate);
    
    // 获取状态名称
    String getStateName();
    
    // 检查是否逾期
    boolean isOverdue(BorrowRecord record, Date currentDate);
    
    // 处理归还
    void handleReturn(BorrowRecord record, Date returnDate);
}