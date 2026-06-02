document.getElementById('dateFilter')?.addEventListener('change', function(e) {
    const selectedDate = e.target.value;
    const buyCards = document.querySelectorAll('.buy-card');

    buyCards.forEach(card => {
        const buyDate = card.dataset.date;
        if (!selectedDate || buyDate.startsWith(selectedDate)) {
            card.style.display = '';
        } else {
            card.style.display = 'none';
        }
    });
});

document.getElementById('checkSearch')?.addEventListener('input', function(e) {
    const searchText = e.target.value.toLowerCase();
    const buyCards = document.querySelectorAll('.buy-card');

    buyCards.forEach(card => {
        const checkNumber = card.querySelector('.check-number').textContent.toLowerCase();
        if (checkNumber.includes(searchText)) {
            card.style.display = '';
        } else {
            card.style.display = 'none';
        }
    });
});

function toggleCheckDetails(checkId) {
    const details = document.getElementById('checkDetails' + checkId);
    if (details.style.display === 'none') {
        details.style.display = 'block';
        loadCheckProducts(checkId);
    } else {
        details.style.display = 'none';
    }
}

function loadCheckProducts(buyId) {
    fetch(`/buys/${buyId}/products`)
        .then(response => response.json())
        .then(products => {
            const tbody = document.querySelector(`#checkProducts${buyId} tbody`);
            tbody.innerHTML = '';

            products.forEach(product => {
                const row = tbody.insertRow();
                row.innerHTML = `
                    <td>${product.productName}</td>
                    <td>${product.price.toFixed(2)} руб.</td>
                    <td>${product.count}</td>
                    <td>${(product.price * product.count).toFixed(2)} руб.</td>
                `;
            });
        })
        .catch(error => {
            console.error('Ошибка загрузки товаров чека:', error);
        });
}