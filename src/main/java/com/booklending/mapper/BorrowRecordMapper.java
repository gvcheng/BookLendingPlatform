package com.booklending.mapper;

import com.booklending.entity.BorrowRecord;
import org.apache.ibatis.annotations.Mapper;

import java.util.Date;
import java.util.List;

@Mapper
public interface BorrowRecordMapper {
    // 添加借阅记录
    int insert(BorrowRecord record);
    
    // 更新借阅记录
    int update(BorrowRecord record);
    
    // 删除借阅记录
    int delete(Long id);
    
    // 根据ID查询借阅记录
    BorrowRecord selectById(Long id);
    
    // 根据用户ID查询借阅记录
    List<BorrowRecord> selectByUserId(Long userId);
    
    // 根据图书ID查询借阅记录
    List<BorrowRecord> selectByBookId(Long bookId);
    
    // 查询所有借阅记录
    List<BorrowRecord> selectAll();
    
    // 查询逾期的借阅记录
    List<BorrowRecord> selectOverdue(Date currentDate);
    
    // 查询用户当前借阅的图书数量
    int countUserActiveBorrows(Long userId);
    
    // 查询图书当前被借阅的数量
    int countBookActiveBorrows(Long bookId);
    
    // 搜索借阅记录
    List<BorrowRecord> search(String keyword);
}