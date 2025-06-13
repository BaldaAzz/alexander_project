// Dashboard Interactions and Animations
document.addEventListener('DOMContentLoaded', function() {
    // Initialize animations
    initializeAnimations();
    
    // Initialize charts if they exist
    initializeCharts();
    
    // Add smooth scrolling
    addSmoothScrolling();
    
    // Add loading states
    addLoadingStates();
});

function initializeAnimations() {
    // Animate elements on scroll (cards, quick actions, charts)
    const animatedElements = document.querySelectorAll('.dashboard-card, .quick-actions-card, .chart-container');
    const observer = new IntersectionObserver((entries) => {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                entry.target.style.opacity = '1';
                entry.target.style.transform = 'translateY(0)';
                entry.target.classList.add('is-visible'); // Add class to mark as visible
            } else {
                // Optional: animate out when not visible
                // entry.target.style.opacity = '0';
                // entry.target.style.transform = 'translateY(20px)';
                // entry.target.classList.remove('is-visible');
            }
        });
    }, { threshold: 0.1 }); // Trigger when 10% of the element is visible

    animatedElements.forEach(element => {
        element.style.opacity = '0';
        element.style.transform = 'translateY(20px)'; // Initial state below final position
        element.style.transition = 'opacity 0.6s ease-out, transform 0.6s ease-out'; // Smooth transition
        observer.observe(element);
    });

    // Add hover effects to transaction items
    const transactionItems = document.querySelectorAll('.transaction-item');
    transactionItems.forEach(item => {
        item.addEventListener('mouseenter', () => {
            item.style.transform = 'translateX(10px) scale(1.02)'; // More pronounced effect
            item.style.boxShadow = '0 0 15px rgba(0, 255, 255, 0.4)'; // Subtle glow on hover
        });
        item.addEventListener('mouseleave', () => {
            item.style.transform = 'translateX(0) scale(1)';
            item.style.boxShadow = 'none'; // Remove glow
        });
    });

    // Add hover effects to quick action buttons (using global .btn styles, but can add more here if needed)
    // const quickActionButtons = document.querySelectorAll('.quick-actions-card .btn');
    // quickActionButtons.forEach(button => {
    //     button.addEventListener('mouseenter', () => {
    //         button.style.transform = 'scale(1.05)';
    //         button.style.boxShadow = '0 0 20px rgba(255, 0, 255, 0.6)'; // Accent color glow
    //     });
    //     button.addEventListener('mouseleave', () => {
    //         button.style.transform = 'scale(1)';
    //         button.style.boxShadow = 'var(--shadow)'; // Revert to default glow
    //     });
    // });
}

function initializeCharts() {
    // Initialize charts if Chart.js is available
    if (typeof Chart !== 'undefined') {
        const chartElements = document.querySelectorAll('.chart-container canvas');
        chartElements.forEach(canvas => {
            // Add animation to chart canvas itself
            canvas.style.opacity = '0';
            setTimeout(() => {
                canvas.style.transition = 'opacity 1s ease';
                canvas.style.opacity = '1';
            }, 500); // Delay fade-in slightly after container appears
        });
    }
}

function addSmoothScrolling() {
    // Add smooth scrolling to all anchor links
    document.querySelectorAll('a[href^="#"]').forEach(anchor => {
        anchor.addEventListener('click', function (e) {
            e.preventDefault();
            const target = document.querySelector(this.getAttribute('href'));
            if (target) {
                target.scrollIntoView({
                    behavior: 'smooth',
                    block: 'start'
                });
            }
        });
    });
}

function addLoadingStates() {
    // Add loading states to buttons
    document.querySelectorAll('.btn-primary').forEach(button => {
         // Store original text to restore later
        const originalText = button.innerHTML;
        button.setAttribute('data-original-text', originalText);

        button.addEventListener('click', function() {
            if (!this.classList.contains('loading')) {
                this.classList.add('loading');
                this.innerHTML = '<span class="loading-spinner"></span> Загрузка...'; // Localized loading text

                // Optional: Disable button while loading
                // this.disabled = true;

                // Simulate loading state (remove or replace with actual async operation in production)
                setTimeout(() => {
                    this.classList.remove('loading');
                    this.innerHTML = this.getAttribute('data-original-text'); // Restore original text
                    // this.disabled = false; // Re-enable button
                }, 1500); // Longer simulation
            }
        });
    });
    // Add loading state to secondary buttons as well
     document.querySelectorAll('.btn-secondary').forEach(button => {
         // Store original text to restore later
        const originalText = button.innerHTML;
        button.setAttribute('data-original-text', originalText);

        button.addEventListener('click', function() {
            if (!this.classList.contains('loading')) {
                this.classList.add('loading');
                this.innerHTML = '<span class="loading-spinner"></span> Загрузка...'; // Localized loading text

                // Optional: Disable button while loading
                // this.disabled = true;

                // Simulate loading state (remove or replace with actual async operation in production)
                setTimeout(() => {
                    this.classList.remove('loading');
                    this.innerHTML = this.getAttribute('data-original-text'); // Restore original text
                    // this.disabled = false; // Re-enable button
                }, 1500); // Longer simulation
            }
        });
    });
}

// Add tab switching animations (if applicable)
document.querySelectorAll('.nav-tabs .nav-link').forEach(tab => {
    tab.addEventListener('click', function(e) {
        e.preventDefault();

        // Remove active class from all tabs
        document.querySelectorAll('.nav-tabs .nav-link').forEach(t => {
            t.classList.remove('active');
        });

        // Add active class to clicked tab
        this.classList.add('active');

        // Show corresponding content with animation
        const targetId = this.getAttribute('href').substring(1);
        const targetContent = document.getElementById(targetId);

        if (targetContent) {
            // Hide all tab contents with animation
            document.querySelectorAll('.tab-content > div').forEach(content => {
                content.style.opacity = '0';
                content.style.transition = 'opacity 0.4s ease-out';
                // Use setTimeout to set display: none after animation
                setTimeout(() => {
                    content.style.display = 'none';
                }, 400);

            });

            // Show target content with animation
            targetContent.style.display = 'block';
            setTimeout(() => {
                 targetContent.style.opacity = '1';
                 targetContent.style.transition = 'opacity 0.4s ease-in';
            }, 50); // Small delay to allow display: block to take effect
        }
    });
});

// Add number counter animation
function animateValue(element, start, end, duration) {
    let startTimestamp = null;
    const step = (timestamp) => {
        if (!startTimestamp) startTimestamp = timestamp;
        const progress = Math.min((timestamp - startTimestamp) / duration, 1);
        const value = Math.floor(progress * (end - start) + start);
        // Format number with commas for better readability (basic example)
        element.textContent = value.toLocaleString('ru-RU'); // Use Russian locale for formatting
        if (progress < 1) {
            window.requestAnimationFrame(step);
        }
    };
    window.requestAnimationFrame(step);
}

// Initialize number animations for statistics
document.querySelectorAll('.stat-number').forEach(stat => {
    // Ensure element has a data-value attribute
    const finalValue = parseInt(stat.getAttribute('data-value'));
    if (!isNaN(finalValue)) {
        animateValue(stat, 0, finalValue, 2000); // 2 second animation
    }
});

// Add more dynamic background effects (optional - might require additional libraries or complex CSS)
// Example: Particle effects, subtle parallax, background gradients that shift over time

// Improve mobile/adaptive interactions (using JS for more complex scenarios)
// Example: Swipe gestures for transaction items, dynamic column layouts based on content

// Function to check screen size and apply classes or modify elements if needed
function checkScreenSize() {
    if (window.innerWidth < 768) {
        // Apply mobile-specific JS behaviors
        document.body.classList.add('is-mobile');
    } else {
        document.body.classList.remove('is-mobile');
        // Apply desktop-specific JS behaviors
    }
}

// Initial check and add listener for resize
checkScreenSize();
window.addEventListener('resize', checkScreenSize);

// Add animation for alerts
document.addEventListener('DOMContentLoaded', function() {
    document.querySelectorAll('.alert').forEach(alert => {
        alert.style.opacity = '0';
        alert.style.transform = 'translateY(-20px)';
        alert.style.transition = 'opacity 0.5s ease-out, transform 0.5s ease-out';
        setTimeout(() => {
            alert.style.opacity = '1';
            alert.style.transform = 'translateY(0)';
        }, 100); // Small delay to ensure transition is applied

        // Auto-dismiss alerts after 5 seconds
        setTimeout(() => {
            alert.style.opacity = '0';
            alert.style.transform = 'translateY(-20px)';
            setTimeout(() => {
                 alert.remove(); // Remove element after animation
            }, 500);
        }, 5000);
    });
});

// Currency converter logic
const amountToConvertInput = document.getElementById('amountToConvert');
const targetCurrencySelect = document.getElementById('targetCurrency');
const convertedAmountInput = document.getElementById('convertedAmount');
const convertButton = document.getElementById('convertButton');
const exchangeRateElements = document.querySelectorAll('.small > div'); // Elements displaying rates

if (convertButton) {
    convertButton.addEventListener('click', function() {
        convertCurrency();
    });
}

// Optional: Convert on input change as well
if (amountToConvertInput) {
    amountToConvertInput.addEventListener('input', function() {
        convertCurrency();
    });
}

if (targetCurrencySelect) {
    targetCurrencySelect.addEventListener('change', function() {
        convertCurrency();
    });
}

function convertCurrency() {
    const amount = parseFloat(amountToConvertInput.value);
    const targetCurrency = targetCurrencySelect.value;
    let exchangeRate = 0;

    // Find the exchange rate for the target currency
    exchangeRateElements.forEach(rateElement => {
        if (rateElement.getAttribute('data-currency') === targetCurrency) {
            exchangeRate = parseFloat(rateElement.getAttribute('data-rate'));
        }
    });

    // Check if amount is a valid number and exchange rate is found and valid
    if (!isNaN(amount) && amount > 0 && exchangeRate > 0) {
        const convertedAmount = amount * exchangeRate;
        convertedAmountInput.value = convertedAmount.toFixed(2); // Format to 2 decimal places
    } else if (isNaN(amount) || amount <= 0) {
        convertedAmountInput.value = 'Введите сумму'; // Inform user to enter a valid amount
    } else if (exchangeRate <= 0) {
        convertedAmountInput.value = 'Курс недоступен'; // Inform user if rate is not found or invalid
    } else {
         convertedAmountInput.value = ''; // Clear in case of other unexpected issues
    }
} 