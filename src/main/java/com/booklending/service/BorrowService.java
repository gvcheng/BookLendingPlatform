package com.booklending.service;

import com.booklending.entity.BorrowRecord;

import java.util.List;

public interface BorrowService {
    // 借阅图书
    BorrowRecord borrowBook(Long userId, Long bookId);
    
    // 归还图书
    BorrowRecord returnBook(Long borrowId);
    
    // 查询借阅记录
    BorrowRecord getBorrowRecord(Long id);
    
    // 查询用户的借阅记录
    List<BorrowRecord> getBorrowRecordsByUser(Long userId);
    
    // 查询图书的借阅记录
    List<BorrowRecord> getBorrowRecordsByBook(Long bookId);
    
    // 查询所有借阅记录
    List<BorrowRecord> getAllBorrowRecords();
    
    // 查询逾期的借阅记录
    List<BorrowRecord> getOverdueBorrowRecords();
    
    // 更新借阅记录
    void updateBorrowRecord(BorrowRecord borrowRecord);
    
    // 删除借阅记录
    void deleteBorrowRecord(Long id);
    
    // 计算罚款
    Double calculateFine(Long borrowId);
    
    // 检查用户是否有逾期未还的图书
    boolean hasOverdueBooks(Long userId);
    
    // 搜索借阅记录
    List<BorrowRecord> searchBorrowRecords(String keyword);
    
    // 获取用户当前借阅的图书数量
    int countUserActiveBorrows(Long userId);
    
    // 支付罚金后归还图书
    BorrowRecord returnBookWithFine(Long borrowId, double fineAmount, String paymentMethod);
}