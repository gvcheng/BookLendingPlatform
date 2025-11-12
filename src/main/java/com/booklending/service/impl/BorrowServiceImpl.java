package com.booklending.service.impl;

import com.booklending.entity.Book;
import com.booklending.entity.BorrowRecord;
import com.booklending.entity.User;
import com.booklending.mapper.BookMapper;
import com.booklending.mapper.BorrowRecordMapper;
import com.booklending.mapper.ReservationMapper;
import com.booklending.service.BorrowService;
import com.booklending.state.BorrowState;
import com.booklending.state.NormalState;
import com.booklending.state.OverdueState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
public class BorrowServiceImpl implements BorrowService {

    @Autowired
    private BorrowRecordMapper borrowRecordMapper;
    
    @Autowired
    private BookMapper bookMapper;
    
    @Autowired
    private ReservationMapper reservationMapper;

    @Override
    @Transactional
    public BorrowRecord borrowBook(Long userId, Long bookId) {
        // 1. 检查用户是否存在
        // 这里应该查询用户，但暂时省略
        
        // 2. 检查图书是否存在且可借
        Book book = bookMapper.selectById(bookId);
        if (book == null || book.getAvailableCopies() <= 0) {
            throw new RuntimeException("图书不存在或已被借完");
        }
        
        // 3. 检查用户是否有逾期未还的图书
        if (hasOverdueBooks(userId)) {
            throw new RuntimeException("用户有逾期未还的图书，无法继续借阅");
        }
        
        // 4. 创建借阅记录
        BorrowRecord record = new BorrowRecord();
        record.setUserId(userId);
        record.setBookId(bookId);
        record.setBorrowDate(new Date());
        
        // 设置归还期限（默认30天）
        Date dueDate = new Date();
        dueDate.setTime(dueDate.getTime() + 30L * 24 * 60 * 60 * 1000);
        record.setDueDate(dueDate);
        
        record.setStatus("BORROWED");
        record.setFineAmount(0.0);
        record.setCreatedAt(new Date());
        record.setUpdatedAt(new Date());
        
        // 5. 保存借阅记录
        borrowRecordMapper.insert(record);
        
        // 6. 更新图书可借数量
        book.setAvailableCopies(book.getAvailableCopies() - 1);
        bookMapper.updateAvailableCopies(bookId, book.getAvailableCopies()); // 注意：第一个参数名虽然在Mapper中是id，但这里传入bookId是正确的，因为MyBatis会根据参数位置绑定
        
        return record;
    }

    @Override
    @Transactional
    public BorrowRecord returnBook(Long borrowId) {
        // 1. 查询借阅记录
        BorrowRecord record = borrowRecordMapper.selectById(borrowId);
        if (record == null) {
            throw new RuntimeException("借阅记录不存在");
        }
        
        if ("RETURNED".equals(record.getStatus())) {
            throw new RuntimeException("该图书已经归还");
        }
        
        // 2. 获取当前日期
        Date returnDate = new Date();
        
        // 3. 根据状态计算罚款
        BorrowState state;
        if (returnDate.after(record.getDueDate())) {
            state = new OverdueState();
        } else {
            state = new NormalState();
        }
        
        // 4. 处理归还
        state.handleReturn(record, returnDate);
        
        // 5. 更新借阅记录
        record.setUpdatedAt(new Date());
        borrowRecordMapper.update(record);
        
        // 6. 更新图书可借数量
        Book book = bookMapper.selectById(record.getBookId());
        book.setAvailableCopies(book.getAvailableCopies() + 1);
        bookMapper.updateAvailableCopies(record.getBookId(), book.getAvailableCopies());
        
        return record;
    }

    @Override
    public BorrowRecord getBorrowRecord(Long id) {
        return borrowRecordMapper.selectById(id);
    }

    @Override
    public List<BorrowRecord> getBorrowRecordsByUser(Long userId) {
        return borrowRecordMapper.selectByUserId(userId);
    }

    @Override
    public List<BorrowRecord> getBorrowRecordsByBook(Long bookId) {
        return borrowRecordMapper.selectByBookId(bookId);
    }

    @Override
    public List<BorrowRecord> getAllBorrowRecords() {
        return borrowRecordMapper.selectAll();
    }

    @Override
    public List<BorrowRecord> getOverdueBorrowRecords() {
        return borrowRecordMapper.selectOverdue(new Date());
    }

    @Override
    public void updateBorrowRecord(BorrowRecord borrowRecord) {
        borrowRecord.setUpdatedAt(new Date());
        borrowRecordMapper.update(borrowRecord);
    }

    @Override
    public void deleteBorrowRecord(Long id) {
        borrowRecordMapper.delete(id);
    }

    @Override
    public Double calculateFine(Long borrowId) {
        BorrowRecord record = borrowRecordMapper.selectById(borrowId);
        if (record == null) {
            throw new RuntimeException("借阅记录不存在");
        }
        
        Date currentDate = new Date();
        BorrowState state;
        if (currentDate.after(record.getDueDate()) && !"RETURNED".equals(record.getStatus())) {
            state = new OverdueState();
        } else {
            state = new NormalState();
        }
        
        return state.calculateFine(record, currentDate);
    }

    @Override
    public boolean hasOverdueBooks(Long userId) {
        List<BorrowRecord> records = borrowRecordMapper.selectByUserId(userId);
        Date currentDate = new Date();
        
        for (BorrowRecord record : records) {
            if (record.getStatus() != null && "BORROWED".equals(record.getStatus()) && 
                record.getDueDate() != null && currentDate.after(record.getDueDate())) {
                return true;
            }
        }
        
        return false;
    }
    
    @Override
    public List<BorrowRecord> searchBorrowRecords(String keyword) {
        // 如果关键词为空，返回所有记录
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllBorrowRecords();
        }
        return borrowRecordMapper.search(keyword);
    }
    
    @Override
    public int countUserActiveBorrows(Long userId) {
        return borrowRecordMapper.countUserActiveBorrows(userId);
    }
    
    @Override
    @Transactional
    public BorrowRecord returnBookWithFine(Long borrowId, double fineAmount, String paymentMethod) {
        // 1. 查询借阅记录
        BorrowRecord record = borrowRecordMapper.selectById(borrowId);
        if (record == null) {
            throw new RuntimeException("借阅记录不存在");
        }
        
        if ("RETURNED".equals(record.getStatus())) {
            throw new RuntimeException("该图书已经归还");
        }
        
        // 2. 获取当前日期
        Date returnDate = new Date();
        
        // 3. 处理归还，使用逾期状态
        BorrowState state = new OverdueState();
        state.handleReturn(record, returnDate);
        
        // 4. 设置罚金金额
        record.setFineAmount(fineAmount);
        
        // 5. 更新借阅记录
        record.setUpdatedAt(new Date());
        borrowRecordMapper.update(record);
        
        // 6. 更新图书可借数量
        Book book = bookMapper.selectById(record.getBookId());
        book.setAvailableCopies(book.getAvailableCopies() + 1);
        bookMapper.updateAvailableCopies(record.getBookId(), book.getAvailableCopies());
        
        // 7. 这里可以记录支付日志
        // 例如：paymentLogService.createPaymentLog(record.getUserId(), borrowId, fineAmount, paymentMethod);
        
        return record;
    }
}