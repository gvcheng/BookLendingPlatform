package com.booklending.mapper;

import com.booklending.entity.Reservation;
import org.apache.ibatis.annotations.Mapper;

import java.util.Date;
import java.util.List;

@Mapper
public interface ReservationMapper {
    // 添加预约
    int insert(Reservation reservation);
    
    // 更新预约
    int update(Reservation reservation);
    
    // 删除预约
    int delete(Long id);
    
    // 根据ID查询预约
    Reservation selectById(Long id);
    
    // 根据用户ID查询预约
    List<Reservation> selectByUserId(Long userId);
    
    // 根据图书ID查询预约
    List<Reservation> selectByBookId(Long bookId);
    
    // 查询所有预约
    List<Reservation> selectAll();
    
    // 查询过期的预约
    List<Reservation> selectExpired(Date currentDate);
    
    // 检查用户是否已预约某图书
    Reservation selectByUserAndBook(Long userId, Long bookId);
    
    // 获取图书的下一个有效预约（优先级最高的）
    Reservation selectNextActiveReservation(Long bookId);
}