package com.booklending.service.impl;

import com.booklending.entity.Reservation;
import com.booklending.mapper.ReservationMapper;
import com.booklending.service.ReservationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
public class ReservationServiceImpl implements ReservationService {

    @Autowired
    private ReservationMapper reservationMapper;

    @Override
    @Transactional
    public Reservation createReservation(Long userId, Long bookId) {
        // 检查用户是否已经预约了该图书
        if (hasReservation(userId, bookId)) {
            throw new RuntimeException("用户已预约该图书");
        }
        
        Reservation reservation = new Reservation();
        reservation.setUserId(userId);
        reservation.setBookId(bookId);
        reservation.setReservationDate(new Date());
        
        // 设置过期时间（默认7天）
        Date expirationDate = new Date();
        expirationDate.setTime(expirationDate.getTime() + 7L * 24 * 60 * 60 * 1000);
        reservation.setExpirationDate(expirationDate);
        
        reservation.setStatus("ACTIVE");
        // 简化处理，不设置具体优先级，使用预约时间作为优先级依据
        reservation.setPriority(null);
        reservation.setCreatedAt(new Date());
        reservation.setUpdatedAt(new Date());
        
        reservationMapper.insert(reservation);
        return reservation;
    }

    @Override
    @Transactional
    public void cancelReservation(Long reservationId) {
        Reservation reservation = reservationMapper.selectById(reservationId);
        if (reservation != null && "ACTIVE".equals(reservation.getStatus())) {
            reservation.setStatus("CANCELLED");
            reservation.setUpdatedAt(new Date());
            reservationMapper.update(reservation);
            // 简化处理，移除优先级更新逻辑
        }
    }

    @Override
    public Reservation getReservation(Long id) {
        return reservationMapper.selectById(id);
    }

    @Override
    public List<Reservation> getReservationsByUser(Long userId) {
        return reservationMapper.selectByUserId(userId);
    }

    @Override
    public List<Reservation> getReservationsByBook(Long bookId) {
        return reservationMapper.selectByBookId(bookId);
    }

    @Override
    public List<Reservation> getAllReservations() {
        return reservationMapper.selectAll();
    }

    @Override
    @Transactional
    public void updateReservationStatus(Long reservationId, String status) {
        Reservation reservation = reservationMapper.selectById(reservationId);
        if (reservation != null) {
            reservation.setStatus(status);
            reservation.setUpdatedAt(new Date());
            reservationMapper.update(reservation);
        }
    }

    @Override
    public List<Reservation> getExpiredReservations() {
        return reservationMapper.selectExpired(new Date());
    }

    @Override
    public boolean hasReservation(Long userId, Long bookId) {
        // 使用现有的selectByUserAndBook方法
        Reservation reservation = reservationMapper.selectByUserAndBook(userId, bookId);
        return reservation != null && "ACTIVE".equals(reservation.getStatus());
    }

    @Override
    public Reservation getNextActiveReservation(Long bookId) {
        return reservationMapper.selectNextActiveReservation(bookId);
    }
}