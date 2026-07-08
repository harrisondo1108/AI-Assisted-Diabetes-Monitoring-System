package com.quan.diabetes.controller.admin;

import com.quan.diabetes.entity.MedicationTiming;
import com.quan.diabetes.entity.Room;

import com.quan.diabetes.service.masterdata.RoomService;
import com.quan.diabetes.service.medication.MedicationTimingService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/configure")
public class ConfigureController {

    private final RoomService roomService;
    private final MedicationTimingService timingService;

    public ConfigureController(RoomService roomService, MedicationTimingService timingService) {
        this.roomService   = roomService;
        this.timingService = timingService;
    }

    /* ── Trang Configure ── */
    @GetMapping
    public String showConfigure(@RequestParam(value = "tab",    defaultValue = "room") String tab,
                                @RequestParam(value = "search", defaultValue = "")    String search,
                                @RequestParam(value = "error",  required = false)     String error,
                                Model model) {
        String keyword = search.trim();
        model.addAttribute("roomList",   keyword.isEmpty() ? roomService.findAll()
                                                           : roomService.searchByKeyword(keyword));
        model.addAttribute("timingList", keyword.isEmpty() ? timingService.findAll()
                                                           : timingService.searchByKeyword(keyword));
        model.addAttribute("activeTab",  tab);
        model.addAttribute("search",     keyword);

        if      ("duplicate".equals(error)) model.addAttribute("errorMessage", "Tên đã tồn tại.");
        else if ("empty".equals(error))     model.addAttribute("errorMessage", "Vui lòng nhập tên.");
        else if ("notfound".equals(error))  model.addAttribute("errorMessage", "Không tìm thấy.");

        return "Admin/Configure";
    }

    /* ════ ROOM ════ */

    @PostMapping("/room/create")
    public String createRoom(@ModelAttribute Room room) {
        String name = room.getRoomName();
        if (name == null || name.trim().isEmpty()) return "redirect:/admin/configure?tab=room&error=empty";
        room.setRoomName(name.trim());
        roomService.create(room);
        return "redirect:/admin/configure?tab=room";
    }

    @PostMapping("/room/update/{id}")
    public String updateRoom(@PathVariable Integer id, @ModelAttribute Room room) {
        String name = room.getRoomName();
        if (name == null || name.trim().isEmpty()) return "redirect:/admin/configure?tab=room&error=empty";
        Room existing = roomService.findById(id).orElse(null);
        if (existing == null) return "redirect:/admin/configure?tab=room&error=notfound";
        existing.setRoomName(name.trim());
        existing.setDescription(room.getDescription());
        roomService.update(id, existing);
        return "redirect:/admin/configure?tab=room";
    }

    @PostMapping("/room/delete/{id}")
    public String deleteRoom(@PathVariable Integer id) {
        try { roomService.deleteById(id); } catch (Exception ignored) {}
        return "redirect:/admin/configure?tab=room";
    }

    /* ════ TIMING MEDICATION ════ */

    @PostMapping("/timing/create")
    public String createTiming(@RequestParam("timingName") String timingName) {
        if (timingName == null || timingName.trim().isEmpty())
            return "redirect:/admin/configure?tab=timing&error=empty";
        if (timingService.existsByTimingName(timingName.trim()))
            return "redirect:/admin/configure?tab=timing&error=duplicate";
        MedicationTiming t = new MedicationTiming(timingName.trim());
        timingService.create(t);
        return "redirect:/admin/configure?tab=timing";
    }

    @PostMapping("/timing/update/{id}")
    public String updateTiming(@PathVariable Integer id, @RequestParam("timingName") String timingName) {
        if (timingName == null || timingName.trim().isEmpty())
            return "redirect:/admin/configure?tab=timing&error=empty";
        if (timingService.existsByTimingNameAndTimingIdNot(timingName.trim(), id))
            return "redirect:/admin/configure?tab=timing&error=duplicate";
        MedicationTiming existing = timingService.findById(id).orElse(null);
        if (existing == null) return "redirect:/admin/configure?tab=timing&error=notfound";
        existing.setTimingName(timingName.trim());
        timingService.update(id, existing);
        return "redirect:/admin/configure?tab=timing";
    }

    @PostMapping("/timing/delete/{id}")
    public String deleteTiming(@PathVariable Integer id) {
        try { timingService.deleteById(id); } catch (Exception ignored) {}
        return "redirect:/admin/configure?tab=timing";
    }
}
