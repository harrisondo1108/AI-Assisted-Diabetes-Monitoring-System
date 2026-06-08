/* Premium Landing Page Scripts */

document.addEventListener('DOMContentLoaded', () => {
  
  // 1. Navigation Bar Scroll Handling
  const navbar = document.querySelector('.navbar');
  window.addEventListener('scroll', () => {
    if (window.scrollY > 50) {
      navbar.classList.add('scrolled');
    } else {
      navbar.classList.remove('scrolled');
    }
  });

  // 2. Background Particles Generator
  const particlesContainer = document.getElementById('particles-container');
  if (particlesContainer) {
    const particleCount = 20;
    for (let i = 0; i < particleCount; i++) {
      const particle = document.createElement('div');
      particle.classList.add('particle');
      
      // Randomize position and duration
      const size = Math.random() * 8 + 4;
      const left = Math.random() * 100;
      const duration = Math.random() * 6 + 6;
      const delay = Math.random() * 5;
      
      particle.style.width = `${size}px`;
      particle.style.height = `${size}px`;
      particle.style.left = `${left}%`;
      particle.style.animationDuration = `${duration}s`;
      particle.style.animationDelay = `${delay}s`;
      
      particlesContainer.appendChild(particle);
    }
  }

  // 3. Scroll Reveal Animation (Intersection Observer)
  const revealElements = document.querySelectorAll('.reveal');
  const revealObserver = new IntersectionObserver((entries) => {
    entries.forEach(entry => {
      if (entry.isIntersecting) {
        entry.target.classList.add('active');
        revealObserver.unobserve(entry.target);
      }
    });
  }, {
    threshold: 0.1,
    rootMargin: '0px 0px -50px 0px'
  });

  revealElements.forEach(el => revealObserver.observe(el));

  // 4. Statistics Counter Animation
  const statsElements = document.querySelectorAll('.stat-number');
  const statsObserver = new IntersectionObserver((entries) => {
    entries.forEach(entry => {
      if (entry.isIntersecting) {
        const target = entry.target;
        const targetVal = parseInt(target.getAttribute('data-target'), 10);
        let currentVal = 0;
        const duration = 2000; // 2 seconds
        const stepTime = Math.max(Math.floor(duration / targetVal), 15);
        
        const counter = setInterval(() => {
          currentVal += Math.ceil(targetVal / 100);
          if (currentVal >= targetVal) {
            target.textContent = targetVal.toLocaleString() + (target.getAttribute('data-suffix') || '');
            clearInterval(counter);
          } else {
            target.textContent = currentVal.toLocaleString() + (target.getAttribute('data-suffix') || '');
          }
        }, stepTime);
        
        statsObserver.unobserve(target);
      }
    });
  }, { threshold: 0.5 });

  statsElements.forEach(el => statsObserver.observe(el));

  // 5. FAQ Accordion Toggle
  const faqHeaders = document.querySelectorAll('.faq-header');
  faqHeaders.forEach(header => {
    header.addEventListener('click', () => {
      const item = header.parentElement;
      const isActive = item.classList.contains('active');
      
      // Close all FAQs first
      document.querySelectorAll('.faq-item').forEach(faqItem => {
        faqItem.classList.remove('active');
        faqItem.querySelector('.faq-body').style.maxHeight = null;
      });
      
      // Toggle current FAQ
      if (!isActive) {
        item.classList.add('active');
        const body = item.querySelector('.faq-body');
        body.style.maxHeight = body.scrollHeight + 'px';
      }
    });
  });

  // 6. Interactive Dashboard Preview Chat Simulator
  const chatHistory = document.getElementById('chat-history');
  const chatInput = document.getElementById('chat-input');
  const chatSendBtn = document.getElementById('chat-send-btn');
  
  if (chatHistory && chatInput && chatSendBtn) {
    const aiConversationData = [
      {
        trigger: 'đường huyết',
        response: 'Chỉ số đường huyết tiêu chuẩn lúc đói của bạn nên ở mức 4.4 - 7.2 mmol/L. Bạn vừa kiểm tra vào thời điểm nào thế?'
      },
      {
        trigger: 'uống thuốc',
        response: 'Theo lịch trình uống thuốc sáng nay (sau ăn sáng 30 phút), bạn cần uống Metformin 500mg. Bạn đã dùng thuốc chưa?'
      },
      {
        trigger: 'ăn gì',
        response: 'Đối với bệnh tiểu đường, bạn nên hạn chế tinh bột hấp thụ nhanh (cơm trắng, bánh mì) và tăng cường rau xanh, protein nạc. Nên chia nhỏ bữa ăn ra nhé!'
      },
      {
        trigger: 'mệt mỏi',
        response: 'Mệt mỏi có thể là dấu hiệu của việc hạ hoặc tăng đường huyết đột ngột. Hãy đo ngay đường huyết lúc này và báo lại cho trợ lý nhé.'
      }
    ];

    const appendMessage = (text, sender) => {
      const msg = document.createElement('div');
      msg.classList.add('chat-msg', sender);
      msg.textContent = text;
      chatHistory.appendChild(msg);
      chatHistory.scrollTop = chatHistory.scrollHeight;
    };

    const simulateAiResponse = (userText) => {
      // Append typing indicator
      const typingIndicator = document.createElement('div');
      typingIndicator.classList.add('chat-msg', 'ai', 'typing');
      typingIndicator.innerHTML = `
        <span class="typing-dot"></span>
        <span class="typing-dot"></span>
        <span class="typing-dot"></span>
      `;
      chatHistory.appendChild(typingIndicator);
      chatHistory.scrollTop = chatHistory.scrollHeight;

      // Find matching response or default
      const textLower = userText.toLowerCase();
      let responseText = 'Cảm ơn thông tin của bạn. Trợ lý AI đã ghi nhận và đồng bộ chỉ số này vào hồ sơ sức khỏe gửi tới Bác sĩ điều trị.';
      
      for (const item of aiConversationData) {
        if (textLower.includes(item.trigger)) {
          responseText = item.response;
          break;
        }
      }

      setTimeout(() => {
        // Remove typing indicator and add response
        typingIndicator.remove();
        appendMessage(responseText, 'ai');
      }, 1500);
    };

    const handleSendMessage = () => {
      const text = chatInput.value.trim();
      if (!text) return;
      
      appendMessage(text, 'patient');
      chatInput.value = '';
      
      simulateAiResponse(text);
    };

    chatSendBtn.addEventListener('click', handleSendMessage);
    chatInput.addEventListener('keypress', (e) => {
      if (e.key === 'Enter') {
        handleSendMessage();
      }
    });

    // Auto prompt starter chat
    setTimeout(() => {
      simulateAiResponse('đường huyết');
    }, 1000);
  }

  // 7. Interactive Routine Synced Medicine Showcase
  const routineItems = document.querySelectorAll('.routine-item');
  const medicineName = document.getElementById('med-name');
  const medicineDosage = document.getElementById('med-dosage');
  const medicineInstruction = document.getElementById('med-instruction');
  const routineDescription = document.getElementById('routine-desc');

  if (routineItems.length > 0 && medicineName) {
    const routineMedicines = {
      breakfast: {
        name: 'Metformin 500mg',
        dosage: '1 viên',
        instruction: 'Uống sau bữa ăn sáng 30 phút. Tránh uống lúc đói.',
        description: 'Đồng bộ lúc: 07:30 (Giờ ăn sáng)'
      },
      lunch: {
        name: 'Gliclazide 80mg',
        dosage: '1/2 viên',
        instruction: 'Uống trong bữa ăn trưa để duy trì nồng độ insulin ổn định.',
        description: 'Đồng bộ lúc: 12:00 (Giờ ăn trưa)'
      },
      dinner: {
        name: 'Atorvastatin 10mg',
        dosage: '1 viên',
        instruction: 'Uống trước khi đi ngủ. Hỗ trợ kiểm soát mỡ máu.',
        description: 'Đồng bộ lúc: 19:00 (Giờ ăn tối)'
      }
    };

    routineItems.forEach(item => {
      item.addEventListener('click', () => {
        // Toggle active class
        routineItems.forEach(r => r.classList.remove('active'));
        item.classList.add('active');

        // Update medicine card
        const routineKey = item.getAttribute('data-routine');
        const medData = routineMedicines[routineKey];

        if (medData) {
          medicineName.textContent = medData.name;
          medicineDosage.textContent = `Liều dùng: ${medData.dosage}`;
          medicineInstruction.textContent = medData.instruction;
          routineDescription.textContent = medData.description;

          // Add a subtle fade animation
          const card = medicineName.closest('.preview-card');
          card.style.animation = 'none';
          card.offsetHeight; // trigger reflow
          card.style.animation = 'fadeInCard 0.4s ease-out';
        }
      });
    });
  }
});

// CSS Injection for dynamic card animations
const style = document.createElement('style');
style.textContent = `
  @keyframes fadeInCard {
    from { opacity: 0; transform: translateY(5px); }
    to { opacity: 1; transform: translateY(0); }
  }
`;
document.head.appendChild(style);
