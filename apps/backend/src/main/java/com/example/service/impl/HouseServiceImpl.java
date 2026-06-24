package com.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.dto.HouseSaveRequest;
import com.example.dto.HouseSearchQuery;
import com.example.entity.Availability;
import com.example.entity.Favorite;
import com.example.entity.House;
import com.example.entity.Order;
import com.example.entity.SystemConfig;
import com.example.exception.BusinessException;
import com.example.mapper.AvailabilityMapper;
import com.example.mapper.FavoriteMapper;
import com.example.mapper.HouseMapper;
import com.example.mapper.OrderMapper;
import com.example.mapper.SystemConfigMapper;
import com.example.service.HouseService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class HouseServiceImpl implements HouseService {

    private final HouseMapper houseMapper;

    private final OrderMapper orderMapper;

    private final AvailabilityMapper availabilityMapper;

    private final FavoriteMapper favoriteMapper;

    private final SystemConfigMapper systemConfigMapper;

    public HouseServiceImpl(HouseMapper houseMapper, OrderMapper orderMapper,
                            AvailabilityMapper availabilityMapper, FavoriteMapper favoriteMapper,
                            SystemConfigMapper systemConfigMapper) {
        this.houseMapper = houseMapper;
        this.orderMapper = orderMapper;
        this.availabilityMapper = availabilityMapper;
        this.favoriteMapper = favoriteMapper;
        this.systemConfigMapper = systemConfigMapper;
    }

    @Override
    public IPage<House> search(HouseSearchQuery query) {
        long pageNum = query.getPage() == null ? 1 : query.getPage();
        long pageSize = query.getPageSize() == null ? 10 : query.getPageSize();
        Page<House> page = new Page<>(pageNum, pageSize);
        return houseMapper.searchHouses(page, query);
    }

    @Override
    public House getById(Long id) {
        House house = houseMapper.selectById(id);
        if (house == null) {
            throw new BusinessException(404, "房源不存在");
        }
        return house;
    }

    @Override
    public Map<String, Object> checkAvailability(Long houseId, String checkIn, String checkOut) {
        House house = houseMapper.selectById(houseId);
        if (house == null) {
            throw new BusinessException(404, "房源不存在");
        }

        LocalDate in = LocalDate.parse(checkIn);
        LocalDate out = LocalDate.parse(checkOut);

        Map<String, Object> result = new HashMap<>();
        result.put("available", true);
        result.put("message", "");

        // Check house status
        if (!"ON".equals(house.getStatus())) {
            result.put("available", false);
            result.put("message", "该房源已下架");
            return result;
        }

        // Check date validity
        if (!in.isBefore(out)) {
            result.put("available", false);
            result.put("message", "入住日期必须早于离店日期");
            return result;
        }
        if (in.isBefore(LocalDate.now())) {
            result.put("available", false);
            result.put("message", "入住日期不能早于今天");
            return result;
        }

        // Check LOCKED dates
        List<LocalDate> lockedDates = getLockedDates(houseId, in, out.minusDays(1));
        if (!lockedDates.isEmpty()) {
            result.put("available", false);
            result.put("message", "所选日期中有日期已被锁定");
            result.put("lockedDates", lockedDates.stream().map(LocalDate::toString).collect(Collectors.toList()));
            return result;
        }

        // Check order conflicts
        List<Order> conflicts = getConflictingOrders(houseId, in, out);
        if (!conflicts.isEmpty()) {
            result.put("available", false);
            result.put("message", "所选日期已被预订");
            return result;
        }

        long nights = out.toEpochDay() - in.toEpochDay();
        int discount = getConfigInt("default_discount", 30);
        BigDecimal roomFee = house.getPrice().multiply(BigDecimal.valueOf(nights));
        BigDecimal cleanFee = house.getCleanFee() != null ? house.getCleanFee() : BigDecimal.ZERO;
        BigDecimal total = roomFee.add(cleanFee).subtract(BigDecimal.valueOf(discount)).max(BigDecimal.ZERO);

        Map<String, Object> priceDetail = new HashMap<>();
        priceDetail.put("roomFee", roomFee.setScale(2, RoundingMode.HALF_UP));
        priceDetail.put("cleanFee", cleanFee.setScale(2, RoundingMode.HALF_UP));
        priceDetail.put("discount", discount);
        priceDetail.put("total", total.setScale(2, RoundingMode.HALF_UP));

        result.put("price", house.getPrice());
        result.put("cleanFee", cleanFee);
        result.put("nights", nights);
        result.put("priceDetail", priceDetail);
        return result;
    }

    @Override
    public IPage<House> adminList(String keyword, String status, Integer pageNum, Integer pageSize) {
        long pn = pageNum == null ? 1 : pageNum;
        long ps = pageSize == null ? 10 : pageSize;
        Page<House> page = new Page<>(pn, ps);
        QueryWrapper<House> wrapper = new QueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w
                    .like("name", keyword)
                    .or().like("city", keyword)
                    .or().like("address", keyword)
                    .or().like("host", keyword));
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq("status", status);
        }
        wrapper.orderByDesc("create_time");
        return houseMapper.selectPage(page, wrapper);
    }

    @Override
    public House create(HouseSaveRequest request) {
        House house = convertFromRequest(request);
        house.setStatus("ON");
        houseMapper.insert(house);
        return house;
    }

    @Override
    public House update(Long id, HouseSaveRequest request) {
        House house = houseMapper.selectById(id);
        if (house == null) {
            throw new BusinessException(404, "房源不存在");
        }
        House updated = convertFromRequest(request);
        updated.setId(id);
        houseMapper.updateById(updated);
        return houseMapper.selectById(id);
    }

    @Override
    public void updateStatus(Long id, String status) {
        House house = houseMapper.selectById(id);
        if (house == null) {
            throw new BusinessException(404, "房源不存在");
        }
        House update = new House();
        update.setId(id);
        update.setStatus(status);
        houseMapper.updateById(update);
    }

    @Override
    public void delete(Long id) {
        if (houseMapper.selectById(id) == null) {
            throw new BusinessException(404, "房源不存在");
        }
        houseMapper.deleteById(id);
        // 删除收藏记录
        QueryWrapper<Favorite> favWrapper = new QueryWrapper<>();
        favWrapper.eq("house_id", id);
        favoriteMapper.delete(favWrapper);
    }

    private House convertFromRequest(HouseSaveRequest request) {
        House house = new House();
        house.setName(request.getName());
        house.setCity(request.getCity());
        house.setPrice(request.getPrice());
        house.setCleanFee(request.getCleanFee());
        house.setType(request.getType());
        house.setRooms(request.getRooms());
        house.setArea(request.getArea());
        house.setCapacity(request.getCapacity());
        house.setAddress(request.getAddress());
        house.setHost(request.getHost());
        house.setCoverImage(request.getCoverImage());
        house.setFacilities(request.getFacilities());
        house.setDescription(request.getDescription());
        house.setRule(request.getRule());
        return house;
    }

    private List<LocalDate> getLockedDates(Long houseId, LocalDate start, LocalDate end) {
        QueryWrapper<Availability> wrapper = new QueryWrapper<>();
        wrapper.eq("house_id", houseId)
                .ge("date", start)
                .le("date", end);
        return availabilityMapper.selectList(wrapper).stream()
                .map(Availability::getDate)
                .collect(Collectors.toList());
    }

    private List<Order> getConflictingOrders(Long houseId, LocalDate checkIn, LocalDate checkOut) {
        QueryWrapper<Order> wrapper = new QueryWrapper<>();
        wrapper.eq("house_id", houseId)
                .notIn("status", "CANCELLED", "REFUNDED")
                .lt("check_in", checkOut)
                .gt("check_out", checkIn);
        return orderMapper.selectList(wrapper);
    }

    private int getConfigInt(String key, int defaultValue) {
        SystemConfig config = systemConfigMapper.selectById(key);
        if (config == null || config.getConfigValue() == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(config.getConfigValue().trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
