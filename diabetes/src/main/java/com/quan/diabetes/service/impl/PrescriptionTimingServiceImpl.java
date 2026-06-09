package com.quan.diabetes.service.impl;

import com.quan.diabetes.entity.PrescriptionTiming;
import com.quan.diabetes.repository.PrescriptionTimingRepository;
import com.quan.diabetes.service.PrescriptionTimingService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PrescriptionTimingServiceImpl
        implements PrescriptionTimingService {

    private final PrescriptionTimingRepository repository;

    public PrescriptionTimingServiceImpl(
            PrescriptionTimingRepository repository) {
        this.repository = repository;
    }

    @Override
    public PrescriptionTiming save(
            PrescriptionTiming timing) {

        return repository.save(timing);
    }

    @Override
    public PrescriptionTiming update(
            PrescriptionTiming timing) {

        return repository.save(timing);
    }

    @Override
    public void deleteById(Long id) {
        repository.deleteById(id);
    }

    @Override
    public PrescriptionTiming findById(Long id) {
        return repository.findById(id)
                .orElse(null);
    }

    @Override
    public List<PrescriptionTiming> findAll() {
        return repository.findAll();
    }

    @Override
    public List<PrescriptionTiming>
    findByPrescriptionDetailId(
            String prescriptionDetailId) {

        return repository
                .findByPrescriptionDetail_PrescriptionDetailId(
                        prescriptionDetailId);
    }

    @Override
    public boolean exists(
            String prescriptionDetailId,
            Integer timingId) {

        return repository
                .existsByPrescriptionDetail_PrescriptionDetailIdAndTiming_TimingID(
                        prescriptionDetailId,
                        timingId
                );
    }
}
