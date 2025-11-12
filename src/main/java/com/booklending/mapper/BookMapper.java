package com.booklending.mapper;

import com.booklending.entity.Book;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface BookMapper {
    // 添加图书
    int insert(Book book);
    
    // 更新图书
    int update(Book book);
    
    // 删除图书
    int delete(Long id);
    
    // 根据ID查询图书
    Book selectById(Long id);
    
    // 根据ISBN查询图书
    Book selectByIsbn(String isbn);
    
    // 查询所有图书
    List<Book> selectAll();
    
    // 分页查询图书
    List<Book> selectByPage(int offset, int limit);
    
    // 根据标题搜索图书
    List<Book> searchByTitle(String title);
    
    // 根据作者搜索图书
    List<Book> searchByAuthor(String authorName);
    
    // 根据分类搜索图书
    List<Book> searchByCategory(Long categoryId);
    
    // 更新可借数量
    int updateAvailableCopies(@Param("id") Long id, @Param("availableCopies") int availableCopies);
    
    // 获取热门图书
    List<Book> selectPopularBooks(int limit);
}