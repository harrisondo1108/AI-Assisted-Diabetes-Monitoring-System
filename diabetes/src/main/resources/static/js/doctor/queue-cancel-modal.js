document.addEventListener('DOMContentLoaded', function () {
    const openBtns = document.querySelectorAll('.open-cancel-modal');
    const modal = document.getElementById('cancelModal');
    const closeBtn = document.getElementById('cancelModalClose');
    const cancelBtn = document.getElementById('modalCancelBtn');
    const submitBtn = document.getElementById('modalSubmitBtn');
    const reasonInput = document.getElementById('modalCancelReason');
    const errorDiv = document.getElementById('modalCancelError');
    let currentPatientId = null;

    function openModal(patientId, patientName) {
        currentPatientId = patientId;
        modal.style.display = 'flex';
        modal.querySelector('.patient-brief').textContent = patientName ? patientName : '';
        reasonInput.value = '';
        errorDiv.style.display = 'none';
        reasonInput.focus();
    }

    function closeModal() {
        modal.style.display = 'none';
        currentPatientId = null;
    }

    openBtns.forEach(btn => {
        btn.addEventListener('click', function (e) {
            const pid = btn.getAttribute('data-patient-id');
            // find patient name in the same row
            let row = btn.closest('tr');
            let nameEl = row ? row.querySelector('.patient-name') : null;
            let pname = nameEl ? nameEl.textContent.trim() : '';
            openModal(pid, pname);
        });
    });

    closeBtn.addEventListener('click', closeModal);
    cancelBtn.addEventListener('click', closeModal);

    submitBtn.addEventListener('click', function () {
        const reason = reasonInput.value.trim();
        if (!reason) {
            errorDiv.textContent = 'Lý do hủy không được để trống.';
            errorDiv.style.display = 'block';
            return;
        }
        if (!currentPatientId) {
            errorDiv.textContent = 'Không xác định bệnh nhân.';
            errorDiv.style.display = 'block';
            return;
        }

        submitBtn.disabled = true;
        submitBtn.textContent = 'Đang xử lý...';

        // POST form-urlencoded
        fetch('/doctor/queue/' + encodeURIComponent(currentPatientId) + '/cancel', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8'
            },
            body: 'cancelReason=' + encodeURIComponent(reason)
        }).then(resp => resp.json())
          .then(data => {
              if (data && data.success) {
                  // reload to show updated status / toast
                  window.location.reload();
              } else {
                  errorDiv.textContent = data && data.message ? data.message : 'Lỗi khi hủy ca khám.';
                  errorDiv.style.display = 'block';
              }
          }).catch(err => {
              console.error(err);
              errorDiv.textContent = 'Lỗi kết nối. Vui lòng thử lại.';
              errorDiv.style.display = 'block';
          }).finally(() => {
              submitBtn.disabled = false;
              submitBtn.textContent = 'Xác nhận hủy';
          });
    });

    // Close modal when clicking outside box
    modal.addEventListener('click', function (e) {
        if (e.target === modal) closeModal();
    });
});
