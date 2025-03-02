// Модуль для работы с датой
const DateDisplay = {
  init() {
    const dateDisplay = document.getElementById('date-display');
    if (dateDisplay) {
      const now = new Date();
      const options = { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric' };
      dateDisplay.textContent = now.toLocaleDateString('ru-RU', options);
    }
  }
};

// Модуль для работы с уведомлениями
const Notification = {
  init() {
    const alertContainer = document.querySelector('.alert-container');
    if (alertContainer) {
      // Автоматически скрываем уведомление через 5 секунд
      setTimeout(function() {
        alertContainer.style.opacity = '0';
        alertContainer.style.transition = 'opacity 1s ease';
        
        // Полностью удаляем элемент после завершения анимации
        setTimeout(function() {
          alertContainer.remove();
        }, 1000);
      }, 5000);
    }
  }
};

// Модуль для работы с модальным окном
const Modal = {
  open() {
    document.querySelector('.modal-overlay').classList.add('active');
    // Сбрасываем форму при открытии
    if (document.getElementById('transactionForm')) {
      document.getElementById('transactionForm').reset();
      // По умолчанию выбираем доход
      Form.setIncome();
    }
  },
  close() {
    document.querySelector('.modal-overlay').classList.remove('active');
  }
};

// Модуль для работы с мобильным меню
const MobileMenu = {
  open() {
    document.querySelector('aside').style.display = 'block';
  },
  close() {
    document.querySelector('aside').style.display = 'none';
  }
};

// Модуль для переключения темы
const ThemeToggler = {
  Light() {
    document.body.classList.remove('dark-theme-variables');
    document.querySelector('#toggle-light').classList.add('active');
    document.querySelector('#toggle-dark').classList.remove('active');
    localStorage.setItem('theme', 'light');
  },
  Dark() {
    document.body.classList.add('dark-theme-variables');
    document.querySelector('#toggle-light').classList.remove('active');
    document.querySelector('#toggle-dark').classList.add('active');
    localStorage.setItem('theme', 'dark');
  },
  init() {
    // Проверяем сохраненную тему
    const savedTheme = localStorage.getItem('theme');
    if (savedTheme === 'dark') {
      this.Dark();
    } else {
      this.Light();
    }
  }
};

// Инициализация формы
const Form = {
  setIncome() {
    document.getElementById('isExpense').value = 'false';
    document.querySelector('#incomeButton').classList.add('income');
    document.querySelector('#expenseButton').classList.remove('expense');
  },
  setExpense() {
    document.getElementById('isExpense').value = 'true';
    document.querySelector('#expenseButton').classList.add('expense');
    document.querySelector('#incomeButton').classList.remove('income');
  },
  init() {
    // Инициализация кнопок дохода/расхода
    const incomeButton = document.getElementById('incomeButton');
    const expenseButton = document.getElementById('expenseButton');
    
    if (incomeButton && expenseButton) {
      incomeButton.addEventListener('click', this.setIncome);
      expenseButton.addEventListener('click', this.setExpense);
      
      // По умолчанию выбираем доход
      this.setIncome();
    }
    
    // Обработка отправки формы
    const form = document.getElementById('transactionForm');
    if (form) {
      form.addEventListener('submit', function(event) {
        // Валидация формы перед отправкой
        if (!form.checkValidity()) {
          event.preventDefault();
          alert('Пожалуйста, заполните все обязательные поля формы.');
          return;
        }
        
        // Если форма валидна, позволяем ей отправиться на сервер
        // После успешной отправки пользователь будет перенаправлен на /dashboard
        // благодаря контроллеру на сервере
      });
    }
  }
};

// Инициализация графика
const Chart = {
  init() {
    var ctx = document.getElementById('myChart');
    if (!ctx) return;

    // Получаем данные из элементов на странице
    const incomeText = document.getElementById('incomeDisplay').textContent;
    const expenseText = document.getElementById('expenseDisplay').textContent;
    
    // Извлекаем числовые значения
    const incomeValue = parseFloat(incomeText.replace(/[^0-9.-]+/g, ''));
    const expenseValue = parseFloat(expenseText.replace(/[^0-9.-]+/g, ''));
    
    // Создаем график
    new window.Chart(ctx, {
      type: 'doughnut',
      data: {
        datasets: [{
          data: [incomeValue, expenseValue],
          backgroundColor: ['#28D39A', '#ff7782'],
          borderWidth: 0
        }],
        labels: ['Доходы', 'Расходы']
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        legend: {
          position: 'bottom',
          labels: {
            fontColor: '#7d8da1',
            fontSize: 12,
            boxWidth: 15
          }
        },
        tooltips: {
          callbacks: {
            label: function(tooltipItem, data) {
              return data.labels[tooltipItem.index] + ': ₽' + data.datasets[0].data[tooltipItem.index].toFixed(2);
            }
          }
        }
      }
    });
  }
};

// Инициализация приложения
document.addEventListener('DOMContentLoaded', function() {
  DateDisplay.init();
  ThemeToggler.init();
  Form.init();
  Chart.init();
  Notification.init();
});
