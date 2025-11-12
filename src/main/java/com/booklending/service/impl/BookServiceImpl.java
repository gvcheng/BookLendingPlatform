package com.booklending.service.impl;

import com.booklending.entity.Book;
import com.booklending.mapper.BookMapper;
import com.booklending.service.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
public class BookServiceImpl implements BookService {

    @Autowired
    private BookMapper bookMapper;

    @Override
    @Transactional
    public Book addBook(Book book) {
        book.setCreatedAt(new Date());
        book.setUpdatedAt(new Date());
        // 新添加的图书，可借数量等于总数量
        if (book.getAvailableCopies() == null) {
            book.setAvailableCopies(book.getTotalCopies());
        }
        bookMapper.insert(book);
        return book;
    }

    @Override
    @Transactional
    public Book updateBook(Book book) {
        book.setUpdatedAt(new Date());
        bookMapper.update(book);
        return bookMapper.selectById(book.getId());
    }

    @Override
    @Transactional
    public void deleteBook(Long id) {
        bookMapper.delete(id);
    }

    @Override
    public Book getBookById(Long id) {
        return bookMapper.selectById(id);
    }

    @Override
    public Book getBookByIsbn(String isbn) {
        return bookMapper.selectByIsbn(isbn);
    }

    @Override
    public List<Book> getAllBooks() {
        return bookMapper.selectAll();
    }

    @Override
    public List<Book> getBooksByPage(int page, int pageSize) {
        int offset = (page - 1) * pageSize;
        return bookMapper.selectByPage(offset, pageSize);
    }

    @Override
    public List<Book> searchBooksByTitle(String title) {
        return bookMapper.searchByTitle(title);
    }

    @Override
    public List<Book> searchBooksByAuthor(String authorName) {
        return bookMapper.searchByAuthor(authorName);
    }

    @Override
    public List<Book> searchBooksByCategory(Long categoryId) {
        return bookMapper.searchByCategory(categoryId);
    }

    @Override
    public boolean isBookAvailable(Long bookId) {
        Book book = bookMapper.selectById(bookId);
        return book != null && book.getAvailableCopies() > 0;
    }

    @Override
    @Transactional
    public void updateAvailableCopies(Long bookId, int change) {
        Book book = bookMapper.selectById(bookId);
        if (book != null) {
            int newAvailableCopies = book.getAvailableCopies() + change;
            // 确保可借数量不会小于0或大于总数量
            if (newAvailableCopies >= 0 && newAvailableCopies <= book.getTotalCopies()) {
                bookMapper.updateAvailableCopies(bookId, newAvailableCopies);
            }
        }
    }

    @Override
    public List<Book> getPopularBooks(int limit) {
        return bookMapper.selectPopularBooks(limit);
    }
}