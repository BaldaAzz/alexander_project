// Глобальные переменные для хранения данных
let allTransactions = [];
let filteredTransactions = [];
let currentSort = { column: 'date', direction: 'desc' };

// Инициализация при загрузке страницы
document.addEventListener('DOMContentLoaded', function() {
    initializeMonthlyReport();
});

// Инициализация отчета
function initializeMonthlyReport() {
    // Загрузка данных при открытии модального окна
    const monthlyReportModal = document.getElementById('monthlyReportModal');
    monthlyReportModal.addEventListener('show.bs.modal', loadMonthlyReport);

    // Инициализация обработчиков событий
    initializeEventHandlers();
    
    // Загрузка списков для фильтров
    loadFilterOptions();
}

// Загрузка отчета
async function loadMonthlyReport() {
    try {
        const response = await fetch('/dashboard/monthly-report');
        if (!response.ok) {
            throw new Error('Ошибка загрузки отчета');
        }
        
        const data = await response.json();
        allTransactions = data;
        filteredTransactions = [...data];
        
        // Обновление отображения
        updateTransactionsTable();
        updateSummary();
    } catch (error) {
        console.error('Ошибка:', error);
        showError('Не удалось загрузить отчет');
    }
}

// Инициализация обработчиков событий
function initializeEventHandlers() {
    // Поиск
    document.getElementById('transactionSearch').addEventListener('input', filterTransactions);
    
    // Фильтры
    document.getElementById('transactionType').addEventListener('change', filterTransactions);
    document.getElementById('accountFilter').addEventListener('change', filterTransactions);
    document.getElementById('categoryFilter').addEventListener('change', filterTransactions);
    document.getElementById('dateRange').addEventListener('change', filterTransactions);
    
    // Сортировка
    document.querySelectorAll('#transactionsTable th.sortable').forEach(header => {
        header.addEventListener('click', () => {
            const column = header.dataset.sort;
            sortTransactions(column);
        });
    });
    
    // Экспорт
    document.getElementById('exportReport').addEventListener('click', exportToExcel);
}

// Загрузка опций для фильтров
async function loadFilterOptions() {
    try {
        // Загрузка списка счетов
        const accountsResponse = await fetch('/api/accounts');
        const accounts = await accountsResponse.json();
        const accountFilter = document.getElementById('accountFilter');
        accounts.forEach(account => {
            const option = document.createElement('option');
            option.value = account.id;
            option.textContent = account.name;
            accountFilter.appendChild(option);
        });
        
        // Загрузка списка категорий
        const categoriesResponse = await fetch('/api/categories');
        const categories = await categoriesResponse.json();
        const categoryFilter = document.getElementById('categoryFilter');
        categories.forEach(category => {
            const option = document.createElement('option');
            option.value = category.id;
            option.textContent = category.name;
            categoryFilter.appendChild(option);
        });
    } catch (error) {
        console.error('Ошибка загрузки опций фильтров:', error);
    }
}

// Фильтрация транзакций
function filterTransactions() {
    const searchText = document.getElementById('transactionSearch').value.toLowerCase();
    const type = document.getElementById('transactionType').value;
    const accountId = document.getElementById('accountFilter').value;
    const categoryId = document.getElementById('categoryFilter').value;
    const dateRange = document.getElementById('dateRange').value;
    
    filteredTransactions = allTransactions.filter(transaction => {
        // Поиск по тексту
        const matchesSearch = transaction.description.toLowerCase().includes(searchText) ||
                            transaction.category.name.toLowerCase().includes(searchText) ||
                            transaction.account.name.toLowerCase().includes(searchText);
        
        // Фильтр по типу
        const matchesType = type === 'all' || transaction.type === type;
        
        // Фильтр по счету
        const matchesAccount = accountId === 'all' || transaction.account.id === parseInt(accountId);
        
        // Фильтр по категории
        const matchesCategory = categoryId === 'all' || transaction.category.id === parseInt(categoryId);
        
        // Фильтр по дате
        const transactionDate = new Date(transaction.date);
        const now = new Date();
        let matchesDate = true;
        
        switch(dateRange) {
            case 'current':
                matchesDate = transactionDate.getMonth() === now.getMonth() &&
                             transactionDate.getFullYear() === now.getFullYear();
                break;
            case 'last':
                const lastMonth = new Date(now.getFullYear(), now.getMonth() - 1);
                matchesDate = transactionDate.getMonth() === lastMonth.getMonth() &&
                             transactionDate.getFullYear() === lastMonth.getFullYear();
                break;
            // Для custom можно добавить календарь
        }
        
        return matchesSearch && matchesType && matchesAccount && matchesCategory && matchesDate;
    });
    
    // Применяем текущую сортировку
    sortTransactions(currentSort.column);
}

// Сортировка транзакций
function sortTransactions(column) {
    if (currentSort.column === column) {
        currentSort.direction = currentSort.direction === 'asc' ? 'desc' : 'asc';
    } else {
        currentSort.column = column;
        currentSort.direction = 'asc';
    }
    
    filteredTransactions.sort((a, b) => {
        let valueA, valueB;
        
        switch(column) {
            case 'date':
                valueA = new Date(a.date);
                valueB = new Date(b.date);
                break;
            case 'category':
                valueA = a.category.name;
                valueB = b.category.name;
                break;
            case 'description':
                valueA = a.description;
                valueB = b.description;
                break;
            case 'account':
                valueA = a.account.name;
                valueB = b.account.name;
                break;
            case 'amount':
                valueA = a.amount;
                valueB = b.amount;
                break;
            default:
                return 0;
        }
        
        if (currentSort.direction === 'asc') {
            return valueA > valueB ? 1 : -1;
        } else {
            return valueA < valueB ? 1 : -1;
        }
    });
    
    updateTransactionsTable();
}

// Обновление таблицы транзакций
function updateTransactionsTable() {
    const tbody = document.getElementById('transactionsTableBody');
    tbody.innerHTML = '';
    
    // Если нет транзакций, отображаем сообщение
    if (filteredTransactions.length === 0) {
        tbody.innerHTML = '<tr><td colspan="5" class="text-center text-muted">Нет данных</td></tr>';
        return;
    }

    filteredTransactions.forEach(transaction => {
        const row = document.createElement('tr');
        row.innerHTML = `
            <td>${formatDate(transaction.date)}</td>
            <td>${transaction.categoryName || '-'}</td>
            <td>${transaction.description}</td>
            <td>${transaction.accountName || '-'}</td>
            <td class="${transaction.type === 'INCOME' ? 'text-success' : 'text-danger'}">
                ${transaction.type === 'INCOME' ? '+' : ''}${formatAmount(transaction.amount)} BYN
            </td>
        `;
        tbody.appendChild(row);
    });
}

// Обновление сводной информации
function updateSummary() {
    const income = filteredTransactions
        .filter(t => t.type === 'INCOME')
        .reduce((sum, t) => sum + t.amount, 0);
    
    const expense = filteredTransactions
        .filter(t => t.type === 'EXPENSE')
        .reduce((sum, t) => sum + t.amount, 0);
    
    const balance = income - expense;
    
    document.getElementById('totalIncome').textContent = `+${formatAmount(income)} BYN`;
    document.getElementById('totalExpense').textContent = `${formatAmount(expense)} BYN`;
    document.getElementById('totalBalance').textContent = `${formatAmount(balance)} BYN`;
    document.getElementById('totalBalance').className = `card-text ${balance >= 0 ? 'text-success' : 'text-danger'}`;
}

// Форматирование даты
function formatDate(dateString) {
    const date = new Date(dateString);
    return date.toLocaleDateString('ru-RU', {
        day: '2-digit',
        month: '2-digit',
        year: 'numeric'
    });
}

// Форматирование суммы
function formatAmount(amount) {
    return amount.toLocaleString('ru-RU', {
        minimumFractionDigits: 2,
        maximumFractionDigits: 2
    });
}

// Экспорт в Excel
function exportToExcel() {
    // Преобразование данных транзакций в формат массива массивов для SheetJS
    const dataToExport = filteredTransactions.map(transaction => [
        formatDate(transaction.date), // Дата
        transaction.category ? transaction.category.name : '', // Категория (учитываем null)
        transaction.description, // Описание
        transaction.account ? transaction.account.name : '', // Счет (учитываем null)
        `${transaction.type === 'INCOME' ? '+' : '-'}${formatAmount(transaction.amount)}` // Сумма с знаком
    ]);
    
    // Добавление заголовков таблицы
    const header = ['Дата', 'Категория', 'Описание', 'Счет', 'Сумма'];
    const worksheetData = [header, ...dataToExport];

    // Создание рабочего листа и книги
    const worksheet = XLSX.utils.aoa_to_sheet(worksheetData);
    const workbook = XLSX.utils.book_new();
    XLSX.utils.book_append_sheet(workbook, worksheet, 'Отчет по транзакциям');

    // Генерация файла и скачивание
    const excelBuffer = XLSX.write(workbook, { bookType: 'xlsx', type: 'array' });
    const blob = new Blob([excelBuffer], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=UTF-8' });
    
    const fileName = 'monthly_report_' + new Date().toLocaleDateString('ru-RU') + '.xlsx';
    saveAs(blob, fileName); // Используем saveAs из FileSaver.js (нужно добавить)
}

// Функция saveAs для скачивания файла (нужна библиотека FileSaver.js)
// Временно добавим здесь или предположим, что она будет добавлена через CDN
// Для простоты сейчас используем window.saveAs

// Отображение ошибки
function showError(message) {
    const tbody = document.getElementById('transactionsTableBody');
    tbody.innerHTML = `<tr><td colspan="5" class="text-center text-danger">${message}</td></tr>`;
} 