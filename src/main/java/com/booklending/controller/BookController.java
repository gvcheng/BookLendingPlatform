package com.booklending.controller;

import com.booklending.entity.Book;
import com.booklending.service.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.context.request.WebRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/book")
public class BookController {

    @Autowired
    private BookService bookService;

    @GetMapping("/list")
    public String listBooks(Model model, WebRequest request) {
        List<Book> books = bookService.getAllBooks();
        model.addAttribute("books", books);
        // 从session中获取flash属性并添加到model中
        if (request.getAttribute("message", WebRequest.SCOPE_SESSION) != null) {
            model.addAttribute("message", request.getAttribute("message", WebRequest.SCOPE_SESSION));
            request.removeAttribute("message", WebRequest.SCOPE_SESSION);
        }
        if (request.getAttribute("error", WebRequest.SCOPE_SESSION) != null) {
            model.addAttribute("error", request.getAttribute("error", WebRequest.SCOPE_SESSION));
            request.removeAttribute("error", WebRequest.SCOPE_SESSION);
        }
        return "book/list";
    }

    @GetMapping("/available")
    public String listAvailableBooks(Model model) {
        List<Book> books = bookService.getAllBooks();
        // 过滤出可借的图书
        List<Book> availableBooks = books.stream()
                .filter(book -> book.getAvailableCopies() > 0)
                .collect(Collectors.toList());
        model.addAttribute("books", availableBooks);
        return "book/available";
    }
    
    @GetMapping("/detail/{id}")
    public String viewBookDetail(@PathVariable Long id, Model model) {
        Book book = bookService.getBookById(id);
        if (book == null) {
            model.addAttribute("error", "找不到该书籍");
            return "error";
        }
        model.addAttribute("book", book);
        return "book/detail";
    }
    
    @GetMapping("/search")
    public String searchBooks(@RequestParam("keyword") String keyword, Model model) {
        if (keyword == null || keyword.trim().isEmpty()) {
            // 如果关键字为空，重定向到图书列表
            return "redirect:/book/list";
        }
        
        // 执行模糊搜索
        List<Book> searchResults = new ArrayList<>();
        
        // 搜索标题
        List<Book> titleResults = bookService.searchBooksByTitle(keyword);
        if (titleResults != null) {
            searchResults.addAll(titleResults);
        }
        
        // 搜索作者
        List<Book> authorResults = bookService.searchBooksByAuthor(keyword);
        if (authorResults != null) {
            // 合并结果，去除重复项
            for (Book book : authorResults) {
                if (!searchResults.contains(book)) {
                    searchResults.add(book);
                }
            }
        }
        
        model.addAttribute("books", searchResults);
        model.addAttribute("searchKeyword", keyword);
        
        // 返回图书列表页面
        return "book/list";
    }
    
    @GetMapping("/available/search")
    public String availableSearch(@RequestParam("keyword") String keyword, Model model) {
        if (keyword == null || keyword.trim().isEmpty()) {
            // 如果关键字为空，重定向到可借阅图书页面
            return "redirect:/book/available";
        }
        
        // 执行模糊搜索
        List<Book> searchResults = new ArrayList<>();
        
        // 搜索标题
        List<Book> titleResults = bookService.searchBooksByTitle(keyword);
        if (titleResults != null) {
            searchResults.addAll(titleResults);
        }
        
        // 搜索作者
        List<Book> authorResults = bookService.searchBooksByAuthor(keyword);
        if (authorResults != null) {
            // 合并结果，去除重复项
            for (Book book : authorResults) {
                if (!searchResults.contains(book)) {
                    searchResults.add(book);
                }
            }
        }
        
        // 过滤出可借的图书
        List<Book> availableResults = searchResults.stream()
                .filter(book -> book.getAvailableCopies() > 0)
                .collect(Collectors.toList());
        
        model.addAttribute("books", availableResults);
        model.addAttribute("searchKeyword", keyword);
        
        // 返回可借阅图书页面，使用卡片样式展示结果
        return "book/available";
    }
    
    // 显示修改图书页面
    @GetMapping("/edit/{id}")
    public String editBook(@PathVariable Long id, Model model) {
        Book book = bookService.getBookById(id);
        model.addAttribute("book", book);
        return "book/edit";
    }
    
    // 修改图书
    @PostMapping("/edit/{id}")
    public String updateBook(@PathVariable Long id, Book book, RedirectAttributes redirectAttributes) {
        try {
            book.setId(id);
            bookService.updateBook(book);
            redirectAttributes.addFlashAttribute("message", "图书修改成功！");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "图书修改失败：" + e.getMessage());
        }
        return "redirect:/book/list";
    }
    
    // 删除图书
    @PostMapping("/delete/{id}")
    public String deleteBook(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            bookService.deleteBook(id);
            redirectAttributes.addFlashAttribute("message", "图书删除成功！");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "图书删除失败：" + e.getMessage());
        }
        return "redirect:/book/list";
    }
}