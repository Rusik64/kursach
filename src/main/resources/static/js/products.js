// Поиск по таблице товаров
document.getElementById('productSearchInput')?.addEventListener('input', function(e) {
    const searchText = e.target.value.toLowerCase();
    const rows = document.querySelectorAll('.data-table tbody tr');

    rows.forEach(row => {
        const productName = row.cells[0].textContent.toLowerCase();
        const categoryName = row.cells[1].textContent.toLowerCase();

        if (productName.includes(searchText) || categoryName.includes(searchText)) {
            row.style.display = '';
        } else {
            row.style.display = 'none';
        }
    });
});

function sortTable(columnIndex) {
    const table = document.querySelector('.data-table');
    const tbody = table.querySelector('tbody');
    const rows = Array.from(tbody.querySelectorAll('tr'));

    const sortedRows = rows.sort((a, b) => {
        const aValue = a.cells[columnIndex].textContent;
        const bValue = b.cells[columnIndex].textContent;

        if (columnIndex === 2 || columnIndex === 4) {
            return parseFloat(aValue) - parseFloat(bValue);
        }

        return aValue.localeCompare(bValue);
    });

    tbody.innerHTML = '';
    sortedRows.forEach(row => tbody.appendChild(row));
}