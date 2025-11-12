package com.booklending.controller;

import com.booklending.entity.BorrowRecord;
import com.booklending.entity.User;
import com.booklending.service.BorrowService;
import com.booklending.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import javax.servlet.http.HttpSession;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Controller
@RequestMapping("/borrow")
public class BorrowController {

    @Autowired
    private BorrowService borrowService;
    
    @Autowired
    private UserService userService;

    // 查看所有借阅记录（管理员功能）
    @GetMapping("/list")
    public String listBorrowRecords(Model model) {
        System.out.println("访问借阅记录列表");
        List<BorrowRecord> records = borrowService.getAllBorrowRecords();
        // 确保records非null（如果service返回null，手动创建空集合）
        model.addAttribute("records", records != null ? records : new ArrayList<>());
        return "borrow/list";
    }
    
    // 搜索借阅记录（管理员功能）
    @GetMapping("/search")
    public String searchBorrowRecords(@RequestParam String keyword, Model model) {
        System.out.println("搜索借阅记录，关键词: " + keyword);
        List<BorrowRecord> records = borrowService.searchBorrowRecords(keyword);
        // 确保records非null（如果service返回null，手动创建空集合）
        model.addAttribute("records", records != null ? records : new ArrayList<>());
        model.addAttribute("searchKeyword", keyword);
        return "borrow/list";
    }
    
    // 借阅图书
    @PostMapping("/borrow-book")
    public String borrowBook(@RequestParam Long bookId, RedirectAttributes redirectAttributes) {
        System.out.println("收到借阅请求，图书ID: " + bookId);
        try {
            // 从Spring Security获取当前登录用户
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            System.out.println("Authentication: " + authentication + ", Authenticated: " + (authentication != null && authentication.isAuthenticated()));
            
            if (authentication == null || !authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken) {
                System.out.println("用户未登录，重定向到登录页");
                redirectAttributes.addFlashAttribute("error", "请先登录");
                return "redirect:/login?error=need_login";
            }
            
            // 获取当前用户
            String username = authentication.getName();
            System.out.println("当前登录用户: " + username);
            
            User user = userService.getUserByUsername(username);
            if (user == null) {
                System.out.println("用户不存在: " + username);
                redirectAttributes.addFlashAttribute("error", "用户不存在");
                return "redirect:/borrow/userBorrows";
            }
            
            System.out.println("用户信息: ID=" + user.getId() + ", 激活状态=" + user.getActive());
            
            if (!user.getActive()) {
                System.out.println("用户账号未激活: " + username);
                redirectAttributes.addFlashAttribute("error", "用户账号未激活");
                return "redirect:/borrow/userBorrows";
            }
            
            System.out.println("用户ID: " + user.getId() + ", 尝试借阅图书ID: " + bookId);
            
            // 执行借阅操作
            BorrowRecord record = borrowService.borrowBook(user.getId(), bookId);
            System.out.println("借阅成功，记录ID: " + record.getId());
            redirectAttributes.addFlashAttribute("message", "图书借阅成功！");
        } catch (Exception e) {
            System.err.println("借阅失败: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "借阅失败: " + e.getMessage());
        }
        System.out.println("借阅请求处理完成，重定向到图书列表");
        return "redirect:/borrow/userBorrows";
    }

    // 查看当前登录用户的借阅记录
    @GetMapping("/userBorrows")
    public String listMyBorrowRecords(Model model) {
        System.out.println("访问我的借阅记录");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken) {
            return "redirect:/login?error=need_login";
        }
        
        String username = authentication.getName();
        User user = userService.getUserByUsername(username);
        if (user == null) {
            model.addAttribute("error", "用户不存在");
            return "redirect:/book/list";
        }
        
        List<BorrowRecord> borrowRecords = borrowService.getBorrowRecordsByUser(user.getId());
        int activeBorrowCount = borrowService.countUserActiveBorrows(user.getId());
        
        model.addAttribute("borrowRecords", borrowRecords);
        model.addAttribute("user", user);
        model.addAttribute("activeBorrowCount", activeBorrowCount);
        return "borrow/userBorrows";
    }

    // 查看用户的借阅记录（管理员功能）
    @GetMapping("/user/{userId}")
    public String listUserBorrowRecords(@PathVariable Long userId, Model model) {
        System.out.println("访问用户借阅记录，用户ID: " + userId);
        List<BorrowRecord> records = borrowService.getBorrowRecordsByUser(userId);
        model.addAttribute("records", records);
        model.addAttribute("userId", userId);
        return "borrow/userBorrows";
    }

    // 查看逾期的借阅记录
    @GetMapping("/overdue")
    public String listOverdueRecords(Model model) {
        System.out.println("访问逾期借阅记录");
        List<BorrowRecord> records = borrowService.getOverdueBorrowRecords();
        
        // 确保records非null
        List<BorrowRecord> safeRecords = records != null ? records : new ArrayList<>();
        
        // 计算并添加totalFine和maxDaysOverdue，确保非null值
        Double totalFine = 0.00;
        Integer maxDaysOverdue = 0;
        Date currentDate = new Date();
        
        // 为每个记录计算逾期天数并存储在Map中
        Map<Long, Integer> daysOverdueMap = new HashMap<>();
        
        if (!safeRecords.isEmpty()) {
            for (BorrowRecord record : safeRecords) {
                // 计算总罚款 - 使用记录中已有的罚款金额
                if (record.getFineAmount() != null) {
                    totalFine += record.getFineAmount();
                } else {
                    // 如果罚款金额为null，使用计算值但不更新数据库
                    double fine = borrowService.calculateFine(record.getId());
                    record.setFineAmount(fine);
                    totalFine += fine;
                }
                
                // 计算逾期天数 - 考虑图书是否已归还的情况
                if (record.getDueDate() != null) {
                    // 确定用于计算逾期天数的日期：如果已归还则使用归还日期，否则使用当前日期
                    Date compareDate = record.getReturnDate() != null ? record.getReturnDate() : currentDate;
                    
                    long diffInMillies = compareDate.getTime() - record.getDueDate().getTime();
                    // 使用TimeUnit.DAYS.convert确保精确计算天数差
                    long diffInDays = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);
                    // 确保天数不为负数
                    int daysOverdue = Math.max(0, (int)diffInDays);
                    daysOverdueMap.put(record.getId(), daysOverdue);
                    
                    // 更新最大逾期天数
                    if (daysOverdue > maxDaysOverdue) {
                        maxDaysOverdue = daysOverdue;
                    }
                }
            }
        }
        
        model.addAttribute("overdueRecords", safeRecords);
        model.addAttribute("totalFine", totalFine);
        model.addAttribute("maxDaysOverdue", maxDaysOverdue);
        model.addAttribute("daysOverdueMap", daysOverdueMap);
        model.addAttribute("currentDate", currentDate);
        
        return "borrow/overdue";
    }

    // 归还图书
    @PostMapping("/return")
    public String returnBook(@RequestParam Long borrowId, RedirectAttributes redirectAttributes) {
        System.out.println("收到归还请求，借阅ID: " + borrowId);
        try {
            // 获取当前登录用户信息
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken) {
                redirectAttributes.addFlashAttribute("error", "请先登录");
                return "redirect:/login?error=need_login";
            }
            
            String username = authentication.getName();
            User currentUser = userService.getUserByUsername(username);
            
            // 获取借阅记录，验证是否属于当前用户
            BorrowRecord record = borrowService.getBorrowRecord(borrowId);
            if (record == null) {
                redirectAttributes.addFlashAttribute("error", "借阅记录不存在");
                return "redirect:/borrow/userBorrows";
            }
            
            // 检查权限：只有管理员或书籍的借阅者可以归还书籍
            boolean isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
            boolean isOwner = record.getUserId().equals(currentUser.getId());
            
            if (!isAdmin && !isOwner) {
                redirectAttributes.addFlashAttribute("error", "无权归还此图书");
                return "redirect:/borrow/userBorrows";
            }
            
            // 检查是否逾期
            boolean isOverdue = false;
            double fineAmount = 0.0;
            Date currentDate = new Date();
            
            if (record.getDueDate() != null && currentDate.after(record.getDueDate()) && 
                !"RETURNED".equals(record.getStatus())) {
                // 计算罚金
                fineAmount = borrowService.calculateFine(borrowId);
                if (fineAmount > 0) {
                    isOverdue = true;
                }
            }
            
            // 如果逾期且有罚金，跳转到支付罚金页面
            if (isOverdue && fineAmount > 0) {
                return "redirect:/borrow/pay-fine/" + borrowId;
            }
            
            // 执行归还操作（正常归还，无逾期）
            borrowService.returnBook(borrowId);
            redirectAttributes.addFlashAttribute("message", "图书归还成功！");
        } catch (Exception e) {
            System.err.println("归还失败: " + e.getMessage());
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        // 重定向到用户自己的借阅记录页面，而不是需要管理员权限的列表页面
        return "redirect:/borrow/userBorrows";
    }

    // 计算罚款
    @GetMapping("/calculate-fine/{borrowId}")
    @ResponseBody
    public Double calculateFine(@PathVariable Long borrowId) {
        return borrowService.calculateFine(borrowId);
    }
    
    // 显示修改借阅记录页面
    @GetMapping("/edit/{id}")
    public String editBorrowRecord(@PathVariable Long id, Model model) {
        BorrowRecord record = borrowService.getBorrowRecord(id);
        model.addAttribute("record", record);
        return "borrow/edit";
    }
    
    // 修改借阅记录
    @PostMapping("/edit/{id}")
    public String updateBorrowRecord(@PathVariable Long id, 
                                    @RequestParam("userId") Long userId,
                                    @RequestParam("bookId") Long bookId,
                                    @RequestParam("borrowDate") String borrowDateStr,
                                    @RequestParam("dueDate") String dueDateStr,
                                    @RequestParam(value = "returnDate", required = false) String returnDateStr,
                                    @RequestParam(value = "fineAmount", defaultValue = "0") Double fineAmount,
                                    @RequestParam("status") String status,
                                    RedirectAttributes redirectAttributes) {
        try {
            // 日期格式转换
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            BorrowRecord record = new BorrowRecord();
            record.setId(id);
            record.setUserId(userId);
            record.setBookId(bookId);
            record.setBorrowDate(dateFormat.parse(borrowDateStr));
            record.setDueDate(dateFormat.parse(dueDateStr));
            
            // 处理可选的归还日期
            if (returnDateStr != null && !returnDateStr.isEmpty()) {
                record.setReturnDate(dateFormat.parse(returnDateStr));
            }
            
            // 设置罚款金额和状态
            record.setFineAmount(fineAmount);
            
            // 验证状态值的有效性
            if (!Arrays.asList("BORROWED", "RETURNED", "OVERDUE").contains(status)) {
                redirectAttributes.addFlashAttribute("error", "无效的借阅状态值");
                return "redirect:/borrow/edit/" + id;
            }
            record.setStatus(status);
            
            // 调用服务层更新记录
            borrowService.updateBorrowRecord(record);
            redirectAttributes.addFlashAttribute("message", "借阅记录修改成功！");
        } catch (ParseException e) {
            redirectAttributes.addFlashAttribute("error", "日期格式错误，请使用YYYY-MM-DD格式");
            return "redirect:/borrow/edit/" + id;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "借阅记录修改失败：" + e.getMessage());
            return "redirect:/borrow/edit/" + id;
        }
        return "redirect:/borrow/list";
    }
    
    // 删除借阅记录
    @PostMapping("/delete/{id}")
    public String deleteBorrowRecord(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            borrowService.deleteBorrowRecord(id);
            redirectAttributes.addFlashAttribute("message", "借阅记录删除成功！");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "借阅记录删除失败：" + e.getMessage());
        }
        return "redirect:/borrow/list";
    }
    
    /**
     * 显示支付罚金页面
     */
    @GetMapping("/pay-fine/{id}")
    public String showPayFinePage(@PathVariable("id") Long borrowId, Model model, HttpSession session) {
        // 获取当前用户
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        
        // 获取借阅记录
        BorrowRecord borrowRecord = borrowService.getBorrowRecord(borrowId);
        if (borrowRecord == null) {
            model.addAttribute("error", "借阅记录不存在！");
            return "redirect:/borrow/userBorrows";
        }
        
        // 检查是否是当前用户的借阅记录（非管理员）
        if (!user.getRole().equals("ADMIN") && !borrowRecord.getUserId().equals(user.getId())) {
            model.addAttribute("error", "无权访问此借阅记录！");
            return "redirect:/borrow/userBorrows";
        }
        
        // 计算逾期天数和罚金
        int daysOverdue = 0;
        Date currentDate = new Date();
        if (borrowRecord.getDueDate() != null) {
            Date compareDate = borrowRecord.getReturnDate() != null ? borrowRecord.getReturnDate() : currentDate;
            long diffInMillies = compareDate.getTime() - borrowRecord.getDueDate().getTime();
            long diffInDays = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);
            daysOverdue = Math.max(0, (int)diffInDays);
        }
        double fineAmount = borrowService.calculateFine(borrowId);
        
        // 验证是否确实需要支付罚金
        if (daysOverdue <= 0 || fineAmount <= 0) {
            model.addAttribute("error", "该借阅记录无需支付罚金！");
            return "redirect:/borrow/userBorrows";
        }
        
        // 添加数据到模型
        model.addAttribute("borrowRecord", borrowRecord);
        model.addAttribute("daysOverdue", daysOverdue);
        model.addAttribute("fineAmount", fineAmount);
        model.addAttribute("user", user);
        
        return "borrow/payFine";
    }
    
    /**
     * 处理支付罚金
     */
    @PostMapping("/pay-fine")
    public String payFine(@RequestParam("borrowId") Long borrowId,
                         @RequestParam("fineAmount") double fineAmount,
                         @RequestParam("paymentMethod") String paymentMethod,
                         Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        // 获取当前用户
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        
        try {
            // 1. 处理支付逻辑
            boolean paymentSuccess = false;
            
            // 根据支付方式处理
            if ("balance".equals(paymentMethod)) {
                // 检查余额是否足够
                if (user.getBalance() >= fineAmount) {
                    // 扣除余额
                    user.setBalance(user.getBalance() - fineAmount);
                    userService.saveUser(user);
                    paymentSuccess = true;
                } else {
                    redirectAttributes.addFlashAttribute("error", "账户余额不足！");
                    return "redirect:/borrow/pay-fine/" + borrowId;
                }
            } else if ("alipay".equals(paymentMethod) || "wechat".equals(paymentMethod)) {
                // 模拟第三方支付（实际项目中需要集成真实的支付接口）
                paymentSuccess = true; // 假设支付成功
            }
            
            if (paymentSuccess) {
                // 2. 支付成功后执行归还操作，使用专门的带罚金归还方法
                borrowService.returnBookWithFine(borrowId, fineAmount, paymentMethod);
                
                redirectAttributes.addFlashAttribute("message", "罚金支付成功！图书已归还。");
                return "redirect:/borrow/userBorrows";
            } else {
                redirectAttributes.addFlashAttribute("error", "支付失败，请重试！");
                return "redirect:/borrow/pay-fine/" + borrowId;
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "支付过程中发生错误：" + e.getMessage());
            return "redirect:/borrow/pay-fine/" + borrowId;
        }
    }
}