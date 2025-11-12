package com.booklending.service;

import com.booklending.entity.Book;

import java.util.List;

public interface BookService {
    // 添加图书
    Book addBook(Book book);
    
    // 更新图书
    Book updateBook(Book book);
    
    // 删除图书
    void deleteBook(Long id);
    
    // 根据ID查询图书
    Book getBookById(Long id);
    
    // 根据ISBN查询图书
    Book getBookByIsbn(String isbn);
    
    // 查询所有图书
    List<Book> getAllBooks();
    
    // 分页查询图书
    List<Book> getBooksByPage(int page, int pageSize);
    
    // 根据标题搜索图书
    List<Book> searchBooksByTitle(String title);
    
    // 根据作者搜索图书
    List<Book> searchBooksByAuthor(String authorName);
    
    // 根据分类搜索图书
    List<Book> searchBooksByCategory(Long categoryId);
    
    // 检查图书是否可借
    boolean isBookAvailable(Long bookId);
    
    // 更新图书可借数量
    void updateAvailableCopies(Long bookId, int change);
    
    // 获取热门图书（借阅次数最多的）
    List<Book> getPopularBooks(int limit);
}