package com.booklending.service;

import com.booklending.entity.Reservation;

import java.util.List;

public interface ReservationService {
    // 创建预约
    Reservation createReservation(Long userId, Long bookId);
    
    // 取消预约
    void cancelReservation(Long reservationId);
    
    // 查询预约
    Reservation getReservation(Long id);
    
    // 查询用户的预约
    List<Reservation> getReservationsByUser(Long userId);
    
    // 查询图书的预约
    List<Reservation> getReservationsByBook(Long bookId);
    
    // 查询所有预约
    List<Reservation> getAllReservations();
    
    // 更新预约状态
    void updateReservationStatus(Long reservationId, String status);
    
    // 过期未处理的预约
    List<Reservation> getExpiredReservations();
    
    // 检查用户是否已预约某图书
    boolean hasReservation(Long userId, Long bookId);
    
    // 获取图书的下一个有效预约
    Reservation getNextActiveReservation(Long bookId);
}