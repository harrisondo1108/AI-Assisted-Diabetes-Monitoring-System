package com.quan.diabetes.controller;

import com.quan.diabetes.entity.Room;
import com.quan.diabetes.service.RoomService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/configure")
public class ConfigureController {

    private final RoomService roomService;

    public ConfigureController(RoomService roomService) {
        this.roomService = roomService;
    }

    /* ── Trang Configure ── */
    @GetMapping
    public String showConfigure(@RequestParam(value = "tab", defaultValue = "room") String tab,
                                @RequestParam(value = "error", required = false) String error,
                                Model model) {
        model.addAttribute("roomList", roomService.findAll());
        model.addAttribute("activeTab", tab);

        if ("duplicate".equals(error))
            model.addAttribute("errorMessage", "Tên phòng đã tồn tại.");
        else if ("empty".equals(error))
            model.addAttribute("errorMessage", "Vui lòng nhập tên phòng.");
        else if ("notfound".equals(error))
            model.addAttribute("errorMessage", "Không tìm thấy phòng.");

        return "Admin/Configure";
    }

    /* ── Tạo Room ── */
    @PostMapping("/room/create")
    public String createRoom(@ModelAttribute Room room) {
        String name = room.getRoomName();
        if (name == null || name.trim().isEmpty())
            return "redirect:/admin/configure?tab=room&error=empty";

        room.setRoomName(name.trim());
        roomService.create(room);
        return "redirect:/admin/configure?tab=room";
    }

    /* ── Cập nhật Room ── */
    @PostMapping("/room/update/{id}")
    public String updateRoom(@PathVariable("id") Integer id,
                             @ModelAttribute Room room) {
        String name = room.getRoomName();
        if (name == null || name.trim().isEmpty())
            return "redirect:/admin/configure?tab=room&error=empty";

        Room existing = roomService.findById(id).orElse(null);
        if (existing == null)
            return "redirect:/admin/configure?tab=room&error=notfound";

        existing.setRoomName(name.trim());
        existing.setDescription(room.getDescription());
        roomService.update(id, existing);
        return "redirect:/admin/configure?tab=room";
    }

    /* ── Xóa Room ── */
    @PostMapping("/room/delete/{id}")
    public String deleteRoom(@PathVariable("id") Integer id) {
        roomService.findById(id).ifPresent(r -> roomService.update(id, r)); // just verify exists
        try {
            roomService.deleteById(id);
        } catch (Exception ignored) {}
        return "redirect:/admin/configure?tab=room";
    }
}
