// Мобильное меню
document.addEventListener('DOMContentLoaded', function() {
    // Мобильное меню
    const hamburger = document.querySelector('.hamburger');
    const mobileMenu = document.querySelector('.mobile-menu');
    const mobileMenuClose = document.querySelector('.mobile-menu-close');
    const mobileMenuLinks = document.querySelectorAll('.mobile-menu-link');
    
    // Создаем оверлей
    const menuOverlay = document.createElement('div');
    menuOverlay.className = 'menu-overlay';
    document.body.appendChild(menuOverlay);

    // Открытие мобильного меню
    hamburger.addEventListener('click', function() {
        mobileMenu.classList.add('active');
        menuOverlay.classList.add('active');
        document.body.style.overflow = 'hidden';
    });

    // Закрытие мобильного меню
    function closeMobileMenu() {
        mobileMenu.classList.remove('active');
        menuOverlay.classList.remove('active');
        document.body.style.overflow = '';
    }

    mobileMenuClose.addEventListener('click', closeMobileMenu);
    menuOverlay.addEventListener('click', closeMobileMenu);

    // Закрытие меню при клике на ссылку
    mobileMenuLinks.forEach(link => {
        link.addEventListener('click', closeMobileMenu);
    });

    // Плавная прокрутка для якорных ссылок
    document.querySelectorAll('a[href^="#"]').forEach(anchor => {
        anchor.addEventListener('click', function(e) {
            e.preventDefault();
            const target = document.querySelector(this.getAttribute('href'));
            if (target) {
                target.scrollIntoView({
                    behavior: 'smooth',
                    block: 'start'
                });
                // Close mobile menu if open
                if (mobileMenu.classList.contains('active')) {
                    closeMobileMenu();
                }
            }
        });
    });

    // Анимация появления элементов при прокрутке
    const observerOptions = {
        threshold: 0.1,
        rootMargin: '0px 0px -50px 0px'
    };

    const observer = new IntersectionObserver((entries) => {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                entry.target.classList.add('visible');
                 // Apply specific styles based on class
                if (entry.target.classList.contains('preview-content') ||
                    entry.target.classList.contains('tracking-content')) {
                    entry.target.style.opacity = '1';
                    entry.target.style.transform = 'translateY(0)';
                } else if (entry.target.classList.contains('hero-text')) {
                     entry.target.style.opacity = '1';
                     entry.target.style.transform = 'translateY(0) scale(1)';
                } else if (entry.target.classList.contains('hero-image')) {
                     entry.target.style.opacity = '1';
                     entry.target.style.transform = 'scale(1)';
                }

                // Stop observing once animated
                observer.unobserve(entry.target);
            }
        });
    }, observerOptions);

    // Наблюдаем за секциями и элементами героя
    document.querySelectorAll('.preview-content, .tracking-content, .hero-text, .hero-image').forEach(element => {
        // Set initial state based on element type (defined in CSS)
        // CSS handles the initial transform and opacity now, JS just adds 'visible' class
        observer.observe(element);
    });

    // Улучшенная анимация для кнопок (с использованием CSS transitions и ::before)
    // JS interaction not needed for basic hover effects if done in CSS

    // Анимация для форм (фокус)
    document.querySelectorAll('.form-control').forEach(input => {
        input.addEventListener('focus', function() {
            this.classList.add('is-focused'); // Add class for focused state
        });

        input.addEventListener('blur', function() {
            this.classList.remove('is-focused'); // Remove class on blur
            if (!this.value) {
                // Optional: add class for empty state if needed for styling
                // this.classList.add('is-empty');
            } else {
                 // this.classList.remove('is-empty');
            }
        });
    });

    // Анимация для карточек (с использованием CSS transitions)
    // JS interaction not needed for basic hover effects if done in CSS

    // Анимация для навигации при прокрутке
    const navbar = document.querySelector('.navbar');
    let lastScrollTop = 0;
    const navbarHeight = navbar.offsetHeight;

    window.addEventListener('scroll', () => {
        const scrollTop = window.pageYOffset || document.documentElement.scrollTop;

        if (scrollTop > lastScrollTop && scrollTop > navbarHeight) {
            // Scroll down
            navbar.classList.add('hidden');
        } else {
            // Scroll up
            navbar.classList.remove('hidden');
        }

        lastScrollTop = scrollTop <= 0 ? 0 : scrollTop; // For Mobile or negative scrolling
    });

    // Анимация для ссылок навигации (подчеркивание при наведении - в CSS)
    // JS not needed for this specific effect

    // Добавление subtle фонового эффекта при движении мыши (пример)
    // Это может быть ресурсоемким, использовать с осторожностью.
    const mainContent = document.querySelector('.main-content');
    if (mainContent) {
        mainContent.addEventListener('mousemove', (e) => {
            const mouseX = (e.pageX / window.innerWidth - 0.5) * 30; // Adjust multiplier for intensity
            const mouseY = (e.pageY / window.innerHeight - 0.5) * 30; // Adjust multiplier for intensity
            mainContent.style.transform = `translate(${mouseX}px, ${mouseY}px)`;
            mainContent.style.transition = 'transform 0.5s ease-out'; // Smooth transition for movement
        });
         mainContent.addEventListener('mouseleave', () => {
             mainContent.style.transform = 'translate(0, 0)'; // Reset on mouse leave
         });
    }

    // Динамическое изменение классов или атрибутов на основе размера экрана
    function handleResize() {
        if (window.innerWidth < 768) {
            document.body.classList.add('is-mobile');
        } else {
            document.body.classList.remove('is-mobile');
        }
    }

    // Initial check and add event listener
    handleResize();
    window.addEventListener('resize', handleResize);

});

// Анимации для карточек (дублируется, удалить или объединить)
// const cards = document.querySelectorAll('.card');
// const observerOptions = {
//     threshold: 0.1
// };

// const observer = new IntersectionObserver((entries) => {
//     entries.forEach(entry => {
//         if (entry.isIntersecting) {
//             entry.target.style.opacity = '1';
//             entry.target.style.transform = 'translateY(0)';
//         }
//     });
// }, observerOptions);

// cards.forEach(card => {
//     card.style.opacity = '0';
//     card.style.transform = 'translateY(20px)';
//     card.style.transition = 'all 0.5s ease-out';
//     observer.observe(card);
// });

// Обработка форм (добавление класса loading)
const forms = document.querySelectorAll('form');
forms.forEach(form => {
    form.addEventListener('submit', function(e) {
        // Анимация кнопки отправки
        const submitButton = form.querySelector('button[type="submit"]');
        if (submitButton) {
            submitButton.classList.add('loading');
            // submitButton.disabled = true; // Опционально: отключить кнопку
        }
        
        // Форма будет отправлена после выполнения слушателя
    });
});

// Обработка уведомлений
function showNotification(message, type = 'success') {
    const notification = document.createElement('div');
    notification.className = `notification ${type}`;
    notification.textContent = message;
    
    document.body.appendChild(notification);
    
    // Trigger animation
    requestAnimationFrame(() => {
        notification.classList.add('show');
    });
    
    // Auto-hide notification
    setTimeout(() => {
        notification.classList.remove('show');
        // Remove element after animation
        notification.addEventListener('transitionend', () => {
            notification.remove();
        }, { once: true });
    }, 3000); // Adjust display duration
}

// Добавление стилей для уведомлений (перенесено в CSS файл, если возможно)
// Вместо добавления стилей через JS, лучше определить их в отдельном CSS файле
/*
const style = document.createElement('style');
style.textContent = `
    .notification {
        position: fixed;
        bottom: 20px;
        right: 20px;
        padding: 15px 25px;
        border-radius: 4px;
        color: white;
        transform: translateY(100px);
        opacity: 0;
        transition: all 0.3s ease;
        z-index: 1050; // Ensure it's above modals
    }
    
    .notification.show {
        transform: translateY(0);
        opacity: 1;
    }
    
    .notification.success {
        background-color: var(--success-color);
    }
    
    .notification.error {
        background-color: var(--error-color);
    }
    
    .btn.loading {
        position: relative;
        color: transparent; // Hide text
    }
    
    .btn.loading::after {
        content: '';
        position: absolute;
        width: 20px;
        height: 20px;
        top: 50%;
        left: 50%;
        margin: -10px 0 0 -10px;
        border: 2px solid white;
        border-top-color: transparent;
        border-radius: 50%;
        animation: button-loading 0.8s linear infinite;
    }
    
    @keyframes button-loading {
        to {
            transform: rotate(360deg);
        }
    }
`;
document.head.appendChild(style);
*/ 