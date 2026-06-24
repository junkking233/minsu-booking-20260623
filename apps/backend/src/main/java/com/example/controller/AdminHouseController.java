package com.example.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.common.Result;
import com.example.dto.HouseSaveRequest;
import com.example.entity.House;
import com.example.service.HouseService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
public class AdminHouseController {

    private final HouseService houseService;

    public AdminHouseController(HouseService houseService) {
        this.houseService = houseService;
    }

    @GetMapping("/houses")
    public Result<IPage<House>> list(@RequestParam(required = false) String keyword,
                                     @RequestParam(required = false) String status,
                                     @RequestParam(defaultValue = "1") Integer page,
                                     @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.ok(houseService.adminList(keyword, status, page, pageSize));
    }

    @PostMapping("/houses")
    public Result<House> create(@RequestBody HouseSaveRequest request) {
        return Result.ok("创建成功", houseService.create(request));
    }

    @PutMapping("/houses/{id}")
    public Result<House> update(@PathVariable Long id, @RequestBody HouseSaveRequest request) {
        return Result.ok("更新成功", houseService.update(id, request));
    }

    @PutMapping("/houses/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestBody java.util.Map<String, String> body) {
        houseService.updateStatus(id, body.get("status"));
        return Result.ok("操作成功", null);
    }

    @DeleteMapping("/houses/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        houseService.delete(id);
        return Result.ok("删除成功", null);
    }
}
